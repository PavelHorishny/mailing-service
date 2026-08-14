package com.company.mailing_service.service;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailEventFixture;
import com.company.mailing_service.domain.MailRepository;
import com.company.mailing_service.service.MailingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailingServiceTest {

    @Mock
    private MailRepository mailRepository;

    @InjectMocks
    private MailingService mailingService;

    @Captor
    private ArgumentCaptor<MailRecord> recordCaptor;

    private MailEventFixture fx;

    @BeforeEach
    void setUpFixture(){
        fx = MailEventFixture.getInstance();
    }

    @Test
    void savesNewMailRecordWhenEventIsNotDuplicate() {
        mailingService.process(fx.toMailEvent());

        verify(mailRepository).save(recordCaptor.capture());
        MailRecord saved = recordCaptor.getValue();

        assertThat(saved.getIdempotencyKey()).isEqualTo(fx.getEventId());
        assertThat(saved.getRecipient()).isEqualTo(fx.getRecipient());
        assertThat(saved.getTemplateKey()).isEqualTo(fx.getTemplateKey());
        assertThat(saved.getLocale()).isEqualTo(fx.getLocale());
        assertThat(saved.getStatus()).isEqualTo(MailStatus.NEW);
        assertThat(saved.getAttemptCount()).isZero();
    }

    @Test
    void doesNotPropagateExceptionWhenEventIsDuplicate() {
        when(mailRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatCode(() -> mailingService.process(fx.toMailEvent()))
                .doesNotThrowAnyException();

        verify(mailRepository).save(any());
    }

    @Test
    void skipsProcessingWhenRecipientIsMissing() {
        mailingService.process(fx.withRecipient(null).toMailEvent());

        verify(mailRepository, never()).save(any());
    }

    @Test
    void defaultsTemplateKeyWhenMissing() {
        mailingService.process(fx.withTemplateKey(null).toMailEvent());

        verify(mailRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getTemplateKey()).isEqualTo("none");
    }

    @Test
    void defaultsLocaleWhenMissing() {
        mailingService.process(fx.withLocale(null).toMailEvent());

        verify(mailRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getLocale()).isEqualTo(fx.getLocale());
    }
}