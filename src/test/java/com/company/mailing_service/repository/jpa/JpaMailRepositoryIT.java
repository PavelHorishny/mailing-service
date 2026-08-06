package com.company.mailing_service.repository.jpa;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class JpaMailRepositoryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    JpaMailRepository repository;

    private MailRecord newRecord(String idempotencyKey, String recipient) {
        return MailRecord.builder()
                .idempotencyKey(idempotencyKey)
                .recipient(recipient)
                .templateKey("welcome")
                .locale("ru")
                .status(MailStatus.NEW)
                .attemptCount(0)
                .build();
    }

    @Test
    void savesAndFindsByIdempotencyKey() {
        MailRecord saved = repository.save(newRecord("evt-1", "user@example.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<MailRecord> found = repository.findByIdempotencyKey("evt-1");
        assertThat(found).isPresent();
        assertThat(found.get().getRecipient()).isEqualTo("user@example.com");
    }

    @Test
    void updatesStatusAndAttempt() {
        MailRecord saved = repository.save(newRecord("evt-2", "user2@example.com"));

        repository.incrementAttempt(saved.getId(), "SMTP timeout");
        repository.updateStatus(saved.getId(), MailStatus.FAILED_RETRYING);

        MailRecord updated = repository.findByIdempotencyKey("evt-2").orElseThrow();
        assertThat(updated.getAttemptCount()).isEqualTo(1);
        assertThat(updated.getLastError()).isEqualTo("SMTP timeout");
        assertThat(updated.getStatus()).isEqualTo(MailStatus.FAILED_RETRYING);
    }

    @Test
    void uniqueConstraintPreventsDuplicateIdempotencyKey() {
        repository.save(newRecord("evt-3", "a@example.com"));

        assertThatThrownBy(() -> repository.save(newRecord("evt-3", "b@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
