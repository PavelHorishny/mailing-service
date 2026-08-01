package com.company.mailing_service.domain;

import java.util.Map;

public record MailEvent(
        String eventId,
        String recipient,
        String templateKey,
        String locale,
        Map<String, Object> variables
) {}
