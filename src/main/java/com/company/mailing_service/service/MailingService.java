package com.company.mailing_service.service;

import com.company.mailing_service.domain.MailEvent;
import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.domain.MailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailingService {

    private static final String DEFAULT_TEMPLATE_KEY = "none";
    // should i add enum types for a future templates?

    private final MailRepository mailRepository;

    @Value("${mailing.default-locale:en}")
    private String defaultLocale = "en";

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
        try{
            mailRepository.save(record);
            log.info("Saved new mail record for event {}", event.eventId());
        }catch (DataIntegrityViolationException e){
            log.debug("Duplicate mail event {}, already processed", event.eventId());
        }

    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
