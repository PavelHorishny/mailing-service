/**
 * Application/orchestration layer: {@code MailingService} coordinates domain objects and the {@code
 * MailRepository} port to process a {@code MailEvent}. Depends only on {@link
 * com.company.mailing_service.domain} — no infrastructure imports allowed here. Concrete adapters
 * for this layer's future collaborators (mail sending, template rendering) live under {@link
 * com.company.mailing_service.infrastructure.service}.
 */
package com.company.mailing_service.service;
