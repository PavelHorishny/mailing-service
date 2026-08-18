package com.company.mailing_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.company.mailing_service.domain.*;
import com.company.mailing_service.fixtures.MailEventFixture;
import com.company.mailing_service.fixtures.MailRecordFixture;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class MailingServiceTest {

    @Mock
    private MailRepository mailRepository;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private MailingService mailingService;

    @Captor
    private ArgumentCaptor<MailRecord> recordCaptor;

    private MailEventFixture fx;
    private MailRecordFixture savedFixture;

    @BeforeEach
    void setUpFixture() {
        fx = MailEventFixture.getInstance();
        savedFixture = MailRecordFixture.getInstance().withId(UUID.randomUUID());
        lenient().when(mailRepository.save(any())).thenReturn(savedFixture.toMailRecord());
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

        assertThatCode(() -> mailingService.process(fx.toMailEvent())).doesNotThrowAnyException();

        verify(mailRepository).save(any());
        verify(mailSender, never()).send(any());
    }

    @Test
    void skipsProcessingWhenRecipientIsMissing() {
        mailingService.process(fx.withRecipient(null).toMailEvent());

        verify(mailRepository, never()).save(any());
        verify(mailSender, never()).send(any());
    }

    @Test
    void defaultsTemplateKeyWhenMissing() {
        mailingService.process(fx.withTemplateKey(null).toMailEvent());

        verify(mailRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getTemplateKey()).isEqualTo(TemplateTypes.NONE.getTemplate());
    }

    @Test
    void defaultsLocaleWhenMissing() {
        mailingService.process(fx.withLocale(null).toMailEvent());

        verify(mailRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getLocale()).isEqualTo(fx.getLocale());
    }

    @Test
    void marksRecordAsSentWhenSendSucceeds() {
        mailingService.process(fx.toMailEvent());

        verify(mailSender).send(any());
        verify(mailRepository).updateStatus(savedFixture.getId(), MailStatus.SENT);
        verify(mailRepository, never()).incrementAttempt(any(), any());
    }

    @Test
    void incrementsAttemptAndMarksFailedRetryingWhenSendFailsAndAttemptsRemain() {
        doThrow(new MailSendException("boom")).when(mailSender).send(any());

        mailingService.process(fx.toMailEvent());

        verify(mailRepository).incrementAttempt(eq(savedFixture.getId()), any());
        verify(mailRepository).updateStatus(savedFixture.getId(), MailStatus.FAILED_RETRYING);
    }

    @Test
    void marksUndeliveredWhenSendFailsAndAttemptsExhausted() {
        savedFixture = MailRecordFixture.getInstance()
                .withId(UUID.randomUUID())
                .withAttemptCount(4);
        when(mailRepository.save(any())).thenReturn(savedFixture.toMailRecord());
        doThrow(new MailSendException("boom")).when(mailSender).send(any());

        mailingService.process(fx.toMailEvent());

        verify(mailRepository).updateStatus(savedFixture.getId(), MailStatus.UNDELIVERED);
    }
}
