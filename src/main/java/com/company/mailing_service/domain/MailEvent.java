package com.company.mailing_service.domain;

import java.util.Map;
import lombok.Builder;

@Builder
public record MailEvent(
        String eventId,
        String recipient,
        String templateKey,
        String locale,
        Map<String, Object> variables
) {}
