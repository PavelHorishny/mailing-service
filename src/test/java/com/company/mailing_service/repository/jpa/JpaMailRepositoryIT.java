package com.company.mailing_service.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.infrastructure.persistence.jpa.JpaMailRepository;
import com.company.mailing_service.infrastructure.service.impl.MailingService;
import com.company.mailing_service.testConf.ITConfig;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class JpaMailRepositoryIT extends ITConfig {
    @Autowired
    private MailingService mailingService;


    @Autowired
    private JpaMailRepository repository;


    private MailRecordFixture fx;

    @BeforeEach
    void setUpFixture(){
        fx = MailRecordFixture.getInstance();
    }

    @Test
    void savesAndFindsByIdempotencyKey() {
        MailRecord saved = repository.save(fx.withIdempotencyKey("evt-1").withRecipient("user@example.com").toMailRecord());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<MailRecord> found = repository.findByIdempotencyKey("evt-1");
        assertThat(found).isPresent();
        assertThat(found.get().getRecipient()).isEqualTo("user@example.com");
    }

    @Test
    void updatesStatusAndAttempt() {
        MailRecord saved = repository.save(fx.withIdempotencyKey("evt-2").withRecipient("user2@example.com").toMailRecord());

        repository.incrementAttempt(saved.getId(), "SMTP timeout");
        repository.updateStatus(saved.getId(), MailStatus.FAILED_RETRYING);

        MailRecord updated = repository.findByIdempotencyKey("evt-2").orElseThrow();
        assertThat(updated.getAttemptCount()).isEqualTo(1);
        assertThat(updated.getLastError()).isEqualTo("SMTP timeout");
        assertThat(updated.getStatus()).isEqualTo(MailStatus.FAILED_RETRYING);
    }

    @Test
    void uniqueConstraintPreventsDuplicateIdempotencyKey() {
        repository.save(fx.withIdempotencyKey("evt-3").withRecipient("a@example.com").toMailRecord());

        assertThatThrownBy(() -> repository.save(fx.withIdempotencyKey("evt-3").withRecipient("b@example.com").toMailRecord()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
