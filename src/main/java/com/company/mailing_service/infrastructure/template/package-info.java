/**
 * {@code MailTemplateRepository} abstraction for looking up mail templates by key and locale.
 * Concrete implementations live in the {@link com.company.mailing_service.template.mock} and {@link
 * com.company.mailing_service.template.jpa} sub-packages, selected via Spring profile /
 * {@code @ConditionalOnProperty} — never by code changes.
 */
package com.company.mailing_service.infrastructure.template;
