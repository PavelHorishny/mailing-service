package com.company.mailing_service.infrastructure.consumer;

import com.company.mailing_service.domain.MailEvent;
import com.company.mailing_service.infrastructure.service.impl.MailingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailEventConsumer extends AbstractLoggingConsumer<MailEvent>{

  private final MailingService mailingService;

  @KafkaListener(topics = "${mailing.topic}")
  public void onMessage(MailEvent event, Acknowledgment acknowledgment) {
    handle(event, acknowledgment);
  }

  @Override
  protected String extractEventId(MailEvent event) {
    return event.eventId();
  }

  @Override
  protected String describe(MailEvent event) {
    return "mail event for recipient " + event.recipient();
  }

  @Override
  protected void processEvent(MailEvent event) {
    mailingService.process(event);
  }
}
