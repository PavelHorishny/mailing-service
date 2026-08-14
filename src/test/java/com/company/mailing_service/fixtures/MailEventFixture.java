package com.company.mailing_service.fixtures;

import com.company.mailing_service.domain.MailEvent;
import lombok.*;

import java.util.Map;

@With
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MailEventFixture {

    private String eventId = "evt-1";

    private String recipient = "user@example.com";

    private String templateKey = "test";

    private String locale = "en";

    private Map<String, Object> variables = Map.of();

    public static MailEventFixture getInstance() {
        return new MailEventFixture();
    }

    public MailEvent toMailEvent() {
        return MailEvent.builder()
                .eventId(eventId)
                .recipient(recipient)
                .templateKey(templateKey)
                .locale(locale)
                .variables(variables)
                .build();
    }
}
