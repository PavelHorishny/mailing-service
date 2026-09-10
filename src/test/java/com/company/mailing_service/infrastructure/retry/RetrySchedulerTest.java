package com.company.mailing_service.infrastructure.retry;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailRepository;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.infrastructure.service.impl.MailingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetrySchedulerTest {

    @Mock
    private RetryService retryService;
    @Mock
    private MailRepository mailRepository;
    @Mock
    private MailingService mailingService;

    private RetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new RetryScheduler(retryService, mailRepository, mailingService);
        ReflectionTestUtils.setField(scheduler, "batchSize", 100);
    }

    @Test
    void doesNothingWhenNoRecordsClaimed() {
        when(mailRepository.claimFailedRetrying(eq(100), any())).thenReturn(List.of());

        scheduler.pollAndRetry();

        verifyNoInteractions(mailingService);
    }

    @Test
    void retriesEachClaimedRecord() {
        MailRecord record1 = MailRecordFixture.getInstance()
                .withId(UUID.randomUUID())
                .withStatus(MailStatus.RETRYING)
                .toMailRecord();
        MailRecord record2 = MailRecordFixture.getInstance()
                .withId(UUID.randomUUID())
                .withStatus(MailStatus.RETRYING)
                .toMailRecord();
        when(mailRepository.claimFailedRetrying(eq(100), any())).thenReturn(List.of(record1, record2));
        when(mailRepository.findById(record1.getId()))
                .thenReturn(Optional.of(record1.toBuilder().status(MailStatus.SENT).build()));
        when(mailRepository.findById(record2.getId()))
                .thenReturn(Optional.of(record2.toBuilder().status(MailStatus.SENT).build()));

        scheduler.pollAndRetry();

        verify(mailingService).retry(record1);
        verify(mailingService).retry(record2);
        verify(mailRepository, never()).incrementAttempt(any(), any());
    }

    @Test
    void revertsRecordStuckInRetryingAfterUnexpectedException() {
        MailRecord record = MailRecordFixture.getInstance()
                .withId(UUID.randomUUID())
                .withStatus(MailStatus.RETRYING)
                .toMailRecord();
        when(mailRepository.claimFailedRetrying(eq(100), any())).thenReturn(List.of(record));
        doThrow(new RuntimeException("boom")).when(mailingService).retry(record);
        when(mailRepository.findById(record.getId())).thenReturn(Optional.of(record));

        scheduler.pollAndRetry();

        verify(mailRepository).updateStatus(record.getId(), MailStatus.FAILED_RETRYING);
        verify(mailRepository, never()).incrementAttempt(any(), any());
    }

    @Test
    void continuesProcessingRemainingRecordsAfterOneFails() {
        MailRecord failing = MailRecordFixture.getInstance()
                .withId(UUID.randomUUID())
                .withStatus(MailStatus.RETRYING)
                .toMailRecord();
        MailRecord ok = MailRecordFixture.getInstance()
                .withId(UUID.randomUUID())
                .withStatus(MailStatus.RETRYING)
                .toMailRecord();
        when(mailRepository.claimFailedRetrying(eq(100), any())).thenReturn(List.of(failing, ok));
        doThrow(new RuntimeException("boom")).when(mailingService).retry(failing);
        when(mailRepository.findById(failing.getId())).thenReturn(Optional.of(failing));
        when(mailRepository.findById(ok.getId()))
                .thenReturn(Optional.of(ok.toBuilder().status(MailStatus.SENT).build()));

        scheduler.pollAndRetry();

        verify(mailingService).retry(ok);
        verify(mailRepository).updateStatus(failing.getId(), MailStatus.FAILED_RETRYING);
    }
}
