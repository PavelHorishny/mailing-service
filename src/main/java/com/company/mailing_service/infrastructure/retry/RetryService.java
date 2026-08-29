package com.company.mailing_service.infrastructure.retry;

import com.company.mailing_service.domain.MailRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RetryService {

    @Value("${mailing.retry.base-delay-seconds:10}")
    private long baseDelaySeconds;

    public long computeDelaySeconds(int attemptCount){
        return baseDelaySeconds * (1L<< attemptCount);
    }

    public boolean isDue (MailRecord record){
        Instant nextAttemptAt = record.getUpdatedAt().plusSeconds(computeDelaySeconds(record.getAttemptCount()));
        return !nextAttemptAt.isAfter(Instant.now());
    }
}
