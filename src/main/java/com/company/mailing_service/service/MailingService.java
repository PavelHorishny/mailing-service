package com.company.mailing_service.service;

import com.company.mailing_service.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailingService {

    private static final String DEFAULT_TEMPLATE_KEY = TemplateTypes.NONE.getTemplate();

    private final MailRepository mailRepository;
    private final MailSender mailSender;

    @Value("${mailing.default-locale:en}")
    private String defaultLocale = "en";

    @Value("${mailing.max-send-attempts:5}")
    private int maxSendAttempts = 5;

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public void process(MailEvent event) {
        if (isBlank(event.recipient())) {
            log.warn("Skipping mail event {} — recipient is missing", event.eventId());
            return;
        }

        MailRecord record = MailRecord.builder()
                .idempotencyKey(event.eventId())
                .recipient(event.recipient())
                .templateKey(isBlank(event.templateKey()) ? DEFAULT_TEMPLATE_KEY : event.templateKey())
                .locale(isBlank(event.locale()) ? defaultLocale : event.locale())
                .status(MailStatus.NEW)
                .attemptCount(0)
                .build();

        MailRecord saved;
        try {
            saved = mailRepository.save(record);
            log.info("Saved new mail record for event {}", event.eventId());
        } catch (DataIntegrityViolationException e) {
            log.debug("Duplicate mail event {}, already processed", event.eventId());
            return;
        }

        attemptSend(saved);
    }

    private void attemptSend(MailRecord record) {
        try {
            mailSender.send(record);
            mailRepository.updateStatus(record.getId(), MailStatus.SENT);
            log.info("Mail sent for record {}", record.getId());
        } catch (MailSendException e) {
            mailRepository.incrementAttempt(record.getId(), e.getMessage());
            int attemptsSoFar = record.getAttemptCount() + 1;
            if (attemptsSoFar >= maxSendAttempts) {
                mailRepository.updateStatus(record.getId(), MailStatus.UNDELIVERED);
                log.warn(
                        "Mail record {} exceeded max attempts ({}), marking UNDELIVERED",
                        record.getId(),
                        maxSendAttempts);
            } else {
                mailRepository.updateStatus(record.getId(), MailStatus.FAILED_RETRYING);
                log.warn(
                        "Mail record {} failed to send (attempt {}/{}), will retry",
                        record.getId(),
                        attemptsSoFar,
                        maxSendAttempts);
            }
        }
    }
}
