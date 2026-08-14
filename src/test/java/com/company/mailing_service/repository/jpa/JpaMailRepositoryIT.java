package com.company.mailing_service.repository.jpa;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.testConf.ITConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JpaMailRepositoryIT extends ITConfig {

    private MailRecordFixture fx;

    @BeforeEach
    void setUpFixture(){
        fx = MailRecordFixture.getInstance();
    }

    @Test
    void savesAndFindsByIdempotencyKey() {
        MailRecord saved = getRepository().save(fx.withIdempotencyKey("evt-1").withRecipient("user@example.com").toMailRecord());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<MailRecord> found = getRepository().findByIdempotencyKey("evt-1");
        assertThat(found).isPresent();
        assertThat(found.get().getRecipient()).isEqualTo("user@example.com");
    }

    @Test
    void updatesStatusAndAttempt() {
        MailRecord saved = getRepository().save(fx.withIdempotencyKey("evt-2").withRecipient("user2@example.com").toMailRecord());

        getRepository().incrementAttempt(saved.getId(), "SMTP timeout");
        getRepository().updateStatus(saved.getId(), MailStatus.FAILED_RETRYING);

        MailRecord updated = getRepository().findByIdempotencyKey("evt-2").orElseThrow();
        assertThat(updated.getAttemptCount()).isEqualTo(1);
        assertThat(updated.getLastError()).isEqualTo("SMTP timeout");
        assertThat(updated.getStatus()).isEqualTo(MailStatus.FAILED_RETRYING);
    }

    @Test
    void uniqueConstraintPreventsDuplicateIdempotencyKey() {
        getRepository().save(fx.withIdempotencyKey("evt-3").withRecipient("a@example.com").toMailRecord());

        assertThatThrownBy(() -> getRepository().save(fx.withIdempotencyKey("evt-3").withRecipient("b@example.com").toMailRecord()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
