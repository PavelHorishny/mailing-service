package com.company.mailing_service.service;

import com.company.mailing_service.domain.MailEvent;
import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailEventFixture;
import com.company.mailing_service.repository.MailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void savesNewMailRecordWhenEventIsNotDuplicate() {
        MailEvent event = new MailEvent("evt-1", "user@example.com", "welcome", "ru", Map.of());
        when(mailRepository.findByIdempotencyKey("evt-1")).thenReturn(Optional.empty());

        mailingService.process(event);

        verify(mailRepository).save(recordCaptor.capture());
        MailRecord saved = recordCaptor.getValue();

        assertThat(saved.getIdempotencyKey()).isEqualTo("evt-1");
        assertThat(saved.getRecipient()).isEqualTo("user@example.com");
        assertThat(saved.getTemplateKey()).isEqualTo("welcome");
        assertThat(saved.getLocale()).isEqualTo("ru");
        assertThat(saved.getStatus()).isEqualTo(MailStatus.NEW);
        assertThat(saved.getAttemptCount()).isZero();
    }

    @Test
    void skipsProcessingWhenEventIsDuplicate() {
        MailEvent event = new MailEvent("evt-2", "user@example.com", "welcome", "ru", Map.of());
        MailRecord existing = MailRecord.builder().idempotencyKey("evt-2").build();
        when(mailRepository.findByIdempotencyKey("evt-2")).thenReturn(Optional.of(existing));

        mailingService.process(event);

        verify(mailRepository, never()).save(any());
    }

    @Test
    void skipsProcessingWhenRecipientIsMissing() {
        MailEvent event = new MailEvent("evt-3", null, "welcome", "ru", Map.of());

        mailingService.process(event);

        verify(mailRepository, never()).findByIdempotencyKey(any());
        verify(mailRepository, never()).save(any());
    }

    @Test
    void defaultsTemplateKeyWhenMissing() {
        //MailEvent event = new MailEvent("evt-4", "user@example.com", null, "ru", Map.of());
        MailEvent event = MailEventFixture.getInstance().withEventId("evt-4").withRecipient("user@example.com").withTemplateKey(null).withLocale("ru").toMailEvent();

        when(mailRepository.findByIdempotencyKey("evt-4")).thenReturn(Optional.empty());

        mailingService.process(event);

        verify(mailRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getTemplateKey()).isEqualTo("none");
    }

    @Test
    void defaultsLocaleWhenMissing() {
        //MailEvent event = new MailEvent("evt-5", "user@example.com", "welcome", null, Map.of());
        MailEvent event = MailEventFixture.getInstance().withEventId("evt-5").withRecipient("user@example.com").withTemplateKey("welcome").withLocale(null).toMailEvent();
        when(mailRepository.findByIdempotencyKey("evt-5")).thenReturn(Optional.empty());

        mailingService.process(event);

        verify(mailRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getLocale()).isEqualTo("en");
    }
}