package com.company.mailing_service.infrastructure.service.mock;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailSendException;
import com.company.mailing_service.domain.MailSender;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "mailing", name = "sender-mode", havingValue = "mock", matchIfMissing = true)
public class MockMailSender implements MailSender {

    private static final double FAILURE_RATE = 0.3;

    @Override
    public void send(MailRecord record) {
        if (ThreadLocalRandom.current().nextDouble() < FAILURE_RATE) {
            log.warn("Simulated send failure for mail record {}", record.getId());
            throw new MailSendException("Simulated failure sending mail to " + record.getRecipient());
        }
        log.info("Mock-sent mail to {} (template={})", record.getRecipient(), record.getTemplateKey());
    }
}
