package com.company.mailing_service.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.fixtures.MailEventFixture;
import com.company.mailing_service.infrastructure.persistence.jpa.JpaMailRepository;
import com.company.mailing_service.infrastructure.service.impl.MailingService;
import com.company.mailing_service.testConf.ITConfig;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MailingServiceIT extends ITConfig {
    @Autowired
    private MailingService mailingService;


    @Autowired
    private JpaMailRepository repository;

    private MailEventFixture fx;

    @BeforeEach
    void setUpFixture(){
        fx = MailEventFixture.getInstance();
    }

    @Test
    void persistsNewMailEventAsMailRecord() {

        mailingService.process(fx.toMailEvent());

        Optional<MailRecord> saved = repository.findByIdempotencyKey(fx.getEventId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getRecipient()).isEqualTo(fx.getRecipient());
    }

    @Test
    void doesNotCreateDuplicateRecordForSameEventId() {
        mailingService.process(fx.withEventId("evt-int-2").toMailEvent());
        mailingService.process(fx.withEventId("evt-int-2").toMailEvent());

        assertThat(getMailJpaDao().count()).isEqualTo(1);
    }
}