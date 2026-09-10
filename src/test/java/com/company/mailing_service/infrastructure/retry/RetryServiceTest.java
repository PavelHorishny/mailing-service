package com.company.mailing_service.infrastructure.retry;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.fixtures.MailRecordFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RetryServiceTest {

    private RetryService retryService;

    @BeforeEach
    void setUp() {
        retryService = new RetryService();
        ReflectionTestUtils.setField(retryService, "baseDelaySeconds", 10L);
    }

    @Test
    void computeDelaySecondsGrowsExponentiallyWithAttemptCount() {
        assertThat(retryService.computeDelaySeconds(0)).isEqualTo(10L);
        assertThat(retryService.computeDelaySeconds(1)).isEqualTo(20L);
        assertThat(retryService.computeDelaySeconds(2)).isEqualTo(40L);
        assertThat(retryService.computeDelaySeconds(3)).isEqualTo(80L);
    }

    @Test
    void isDueReturnsFalseWhenDelayHasNotElapsed() {
        MailRecord record = MailRecordFixture.getInstance()
                .withAttemptCount(0)
                .withUpdatedAt(Instant.now())
                .toMailRecord();

        assertThat(retryService.isDue(record)).isFalse();
    }

    @Test
    void isDueReturnsTrueWhenDelayHasElapsed() {
        MailRecord record = MailRecordFixture.getInstance()
                .withAttemptCount(0)
                .withUpdatedAt(Instant.now().minusSeconds(11))
                .toMailRecord();

        assertThat(retryService.isDue(record)).isTrue();
    }

    @Test
    void isDueAccountsForAttemptCountBackoff() {
        MailRecord record = MailRecordFixture.getInstance()
                .withAttemptCount(2)
                .withUpdatedAt(Instant.now().minusSeconds(30))
                .toMailRecord();

        assertThat(retryService.isDue(record)).isFalse();
    }
}
