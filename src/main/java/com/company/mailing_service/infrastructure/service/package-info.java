/**
 * Adapters for the service layer's collaborators (mail sending, template rendering — not the
 * orchestrator itself, which is {@link com.company.mailing_service.service.MailingService}).
 * Mock implementations live in {@link com.company.mailing_service.infrastructure.service.mock};
 * real implementations live in {@link com.company.mailing_service.infrastructure.service.impl}.
 */
package com.company.mailing_service.infrastructure.service;
