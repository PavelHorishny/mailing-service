package com.company.mailing_service.fixtures;

import com.company.mailing_service.domain.MailEvent;
import lombok.*;

import java.util.Map;

@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MailEventFixture {
    @Builder.Default
    private String eventId = "evt-1";
    @Builder.Default
    private String recipient = "user@example.com";
    @Builder.Default
    private String templateKey = "test";
    @Builder.Default
    private String locale = "en";
    @Builder.Default
    private Map<String, Object> variables = Map.of();

    public static MailEventFixture getInstance() {
        return MailEventFixture.builder().build();
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
