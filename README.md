# Mailing Service

A Spring Boot service that consumes mail-send requests from Kafka, renders them into HTML via
Thymeleaf templates, sends them (SMTP or mock), and retries failed deliveries with exponential
backoff. Delivery state is tracked per-event via an idempotency key, so replayed Kafka messages
never send duplicate mail.

## How it works

```
Kafka topic (mailing.topic)
        │
        ▼
MailEventConsumer  ──▶  MailingService.process(event)
                              │
                              ├─ save MailRecord (status=NEW), keyed by event.eventId()
                              │   duplicate idempotency key → silently ignored (already processed)
                              │
                              └─ render template ──▶ MailSender.send(...)
                                      success  → status=SENT
                                      failure  → status=FAILED_RETRYING (or UNDELIVERED after
                                                 mailing.max-send-attempts)

RetryScheduler (cron: mailing.retry.poll-cron)
        │
        └─ claims due FAILED_RETRYING records (exponential backoff, SKIP LOCKED)
           and retries them through MailingService.retry(...)
```

A manual retry is also exposed over HTTP: `POST /api/mails/{id}/retry` forces a retry of a record
that is stuck in `FAILED_RETRYING`.

### Package layout

- `domain` — framework-free model and ports (`MailEvent`, `MailRecord`, `MailStatus`,
  `MailRepository`, `MailSender`, `MailTemplateRenderer`).
- `infrastructure.consumer` — Kafka listener, delegates straight to the service layer.
- `infrastructure.persistence.jpa` / `infrastructure.persistence.mock` — two interchangeable
  `MailRepository` implementations (Postgres via Liquibase, or in-memory), picked by
  `mailing.repository-mode`.
- `infrastructure.service.impl` — `MailingService`, the orchestrator described above.
- `infrastructure.service.mock` / `infrastructure.service.smtp` — the two `MailSender`
  implementations, picked by `mailing.sender-mode`.
- `infrastructure.template` — Thymeleaf-based `MailTemplateRenderer`; templates live under
  `src/main/resources/templates/mail/`.
- `infrastructure.retry` — backoff calculation (`RetryService`) and the polling
  `RetryScheduler`.
- `infrastructure.web` — the `MailController` retry endpoint, documented with Swagger/OpenAPI.

## Running modes (Spring profiles)

The service can run fully in-memory (no external dependencies) or against real infrastructure —
switching is a matter of the active profile, never a code change.

| Profile | `repository-mode` | `sender-mode` | Needs |
|---|---|---|---|
| `mock` (default) | mock (in-memory) | mock (simulated, incl. random failures) | nothing |
| `local` | jpa | mock (inherited) | local Postgres + Kafka |
| `prod` | jpa | smtp | Postgres, Kafka, SMTP server via env vars |

Activate a profile with `--spring.profiles.active=local` (or `SPRING_PROFILES_ACTIVE=local`).

### `prod` environment variables

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Postgres connection |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | SMTP credentials |

### Key `mailing.*` properties (see `application.yaml`)

| Property | Default | Meaning |
|---|---|---|
| `mailing.topic` | `mailing-events` | Kafka topic the consumer subscribes to |
| `mailing.repository-mode` | `mock` | `mock` or `jpa` |
| `mailing.sender-mode` | `mock` | `mock` or `smtp` |
| `mailing.max-send-attempts` | `5` | attempts before marking `UNDELIVERED` |
| `mailing.retry.base-delay-seconds` | `10` | backoff base, doubled per attempt |
| `mailing.retry.batch-size` | `10` | records claimed per scheduler tick |
| `mailing.retry.poll-cron` | every minute | retry scheduler cron expression |

## Mail event contract

Consumers publish plain JSON to `mailing.topic` (no Spring/Jackson type headers required):

```json
{
  "eventId": "unique-idempotency-key",
  "recipient": "user@example.com",
  "templateKey": "new_user",
  "locale": "en",
  "variables": { "name": "Ada" }
}
```

`eventId` is the idempotency key — redelivering the same `eventId` is a safe no-op.

## Running locally

```bash
./mvnw spring-boot:run
```

By default this starts with the `mock` profile — no database, Kafka, or SMTP server required.
Swagger UI is available at `http://localhost:8080/swagger-ui/index.html` once the app is up.

To run against real Postgres/Kafka, start them yourself and activate the `local` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Testing

```bash
./mvnw test                 # unit tests
./mvnw test -Dtest='*IT'     # integration tests (Testcontainers: Postgres and/or Kafka)
```

Integration tests use dedicated base classes under `testConf`, chosen by what the test actually
needs, so tests that don't need Kafka don't pay for spinning up a broker:

- `PostgresITConfig` — Postgres only.
- `KafkaITConfig` — Postgres + Kafka.
- `KafkaOnlyITConfig` — Kafka only (mock repository/sender, no database).

Integration tests require Docker (Testcontainers spins up Postgres/Kafka containers).
