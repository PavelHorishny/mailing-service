package com.company.mailing_service.service;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.fixtures.MailEventFixture;
import com.company.mailing_service.testConf.ITConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


class MailingServiceIT extends ITConfig {

    private MailEventFixture fx;

    @BeforeEach
    void setUpFixture(){
        fx = MailEventFixture.getInstance();
    }

    @Test
    void persistsNewMailEventAsMailRecord() {

        getMailingService().process(fx.toMailEvent());

        Optional<MailRecord> saved = getMailRepository().findByIdempotencyKey(fx.getEventId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getRecipient()).isEqualTo(fx.getRecipient());
    }

    @Test
    void doesNotCreateDuplicateRecordForSameEventId() {
        getMailingService().process(fx.withEventId("evt-int-2").toMailEvent());
        getMailingService().process(fx.withEventId("evt-int-2").toMailEvent());

        assertThat(getMailJpaDao().count()).isEqualTo(1);
    }
}