package com.company.mailing_service.domain;

import java.util.Map;

public interface MailTemplateRenderer {

    String render(String templateKey, String locale, Map<String, Object> variables);
}
