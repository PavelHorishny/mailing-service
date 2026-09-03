package com.company.mailing_service.infrastructure.template;

import com.company.mailing_service.domain.MailRenderException;
import com.company.mailing_service.domain.MailTemplateRenderer;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class ThymeleafMailTemplateRenderer implements MailTemplateRenderer {

    private final SpringTemplateEngine mailTemplateEngine;

    @Override
    public String render(String templateKey, String locale, Map<String, Object> variables) {
        Context context = new Context(Locale.forLanguageTag(locale));
        context.setVariables(variables != null ? variables : Map.of());
        try {
            return mailTemplateEngine.process(templateKey, context);
        } catch (Exception e) {
            throw new MailRenderException("Failed to render template " + templateKey, e);
        }
    }
}
