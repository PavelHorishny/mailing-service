package com.company.mailing_service.infrastructure.retry;

import com.company.mailing_service.domain.MailSendException;
import com.company.mailing_service.domain.MailSender;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.fixtures.MailRecordFixture;
import com.company.mailing_service.infrastructure.persistence.jpa.MailEntity;
import com.company.mailing_service.infrastructure.persistence.jpa.MailEntityMapper;
import com.company.mailing_service.infrastructure.persistence.jpa.MailJpaDao;
import com.company.mailing_service.testConf.ITConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;


class RetrySchedulerIT extends ITConfig {

    @Autowired
    private RetryScheduler retryScheduler;
    @Autowired
    private MailJpaDao mailJpaDao;
    @Autowired
    private MailEntityMapper mailEntityMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private MailSender mailSender;

    private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private UUID seed(MailRecordFixture fixture) {
        MailEntity entity = mailEntityMapper.toEntity(fixture.toMailRecord());
        UUID id = mailJpaDao.save(entity).getId();

        jdbcTemplate.update(
                "UPDATE mail_records SET updated_at = ? WHERE id = ?",
                ps -> {
                    ps.setTimestamp(1, Timestamp.from(fixture.getUpdatedAt()), UTC);
                    ps.setObject(2, id);
                });
        return id;
    }

    @Test
    void skipsRecordThatIsNotYetDue() {
        UUID id = seed(MailRecordFixture.getInstance()
                .withStatus(MailStatus.FAILED_RETRYING)
                .withUpdatedAt(Instant.now()));

        retryScheduler.pollAndRetry();

        verifyNoInteractions(mailSender);
        assertThat(mailJpaDao.findById(id).orElseThrow().getStatus()).isEqualTo(MailStatus.FAILED_RETRYING);
    }

    @Test
    void marksRecordSentWhenRetrySucceeds() {
        doNothing().when(mailSender).send(any(), any());
        UUID id = seed(MailRecordFixture.getInstance()
                .withStatus(MailStatus.FAILED_RETRYING)
                .withAttemptCount(1)
                .withUpdatedAt(Instant.now().minusSeconds(120)));

        retryScheduler.pollAndRetry();

        MailEntity result = mailJpaDao.findById(id).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(MailStatus.SENT);
        assertThat(result.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void reSchedulesForRetryWhenSendFailsButAttemptsRemain() {
        doThrow(new MailSendException("boom")).when(mailSender).send(any(), any());
        UUID id = seed(MailRecordFixture.getInstance()
                .withStatus(MailStatus.FAILED_RETRYING)
                .withAttemptCount(1)
                .withUpdatedAt(Instant.now().minusSeconds(120)));

        retryScheduler.pollAndRetry();

        MailEntity result = mailJpaDao.findById(id).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(MailStatus.FAILED_RETRYING);
        assertThat(result.getAttemptCount()).isEqualTo(2);
    }

    @Test
    void marksUndeliveredWhenAttemptsExhausted() {
        doThrow(new MailSendException("boom")).when(mailSender).send(any(), any());
        UUID id = seed(MailRecordFixture.getInstance()
                .withStatus(MailStatus.FAILED_RETRYING)
                .withAttemptCount(4)
                .withUpdatedAt(Instant.now().minusSeconds(600)));

        retryScheduler.pollAndRetry();

        MailEntity result = mailJpaDao.findById(id).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(MailStatus.UNDELIVERED);
        assertThat(result.getAttemptCount()).isEqualTo(5);
    }

    @Test
    void revertsToFailedRetryingWhenUnexpectedExceptionEscapesMailingService() {
        doThrow(new RuntimeException("db is on fire")).when(mailSender).send(any(), any());
        UUID id = seed(MailRecordFixture.getInstance()
                .withStatus(MailStatus.FAILED_RETRYING)
                .withAttemptCount(1)
                .withUpdatedAt(Instant.now().minusSeconds(120)));

        retryScheduler.pollAndRetry();

        MailEntity result = mailJpaDao.findById(id).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(MailStatus.FAILED_RETRYING);
        assertThat(result.getAttemptCount()).isEqualTo(1);
    }
}
