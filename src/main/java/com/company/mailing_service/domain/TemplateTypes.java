package com.company.mailing_service.domain;

import lombok.Getter;

@Getter
public enum TemplateTypes {
    NONE("none"),
    NEW_USER("new_user"),
    PASSWORD_RESET("password_reset");


    private final String template;

    TemplateTypes(String template) {
        this.template = template;
    }
}
