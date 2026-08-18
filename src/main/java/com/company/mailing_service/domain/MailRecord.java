package com.company.mailing_service.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class MailRecord {
    UUID id;
    String idempotencyKey;
    String recipient;
    String templateKey;
    String locale;
    MailStatus status;
    int attemptCount;
    String lastError;
    Instant createdAt;
    Instant updatedAt;
}
