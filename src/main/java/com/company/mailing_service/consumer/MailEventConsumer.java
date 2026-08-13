package com.company.mailing_service.consumer;

import com.company.mailing_service.domain.MailEvent;
import com.company.mailing_service.service.MailingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailEventConsumer {

    private final MailingService mailingService;

    @KafkaListener(topics = "mail-events")
    public void onMessage(MailEvent event, Acknowledgment acknowledgment) {
        MDC.put("eventId", event.eventId());
        try {
            mailingService.process(event);
            acknowledgment.acknowledge();
        } finally {
            MDC.remove("eventId");
        }
    }
}