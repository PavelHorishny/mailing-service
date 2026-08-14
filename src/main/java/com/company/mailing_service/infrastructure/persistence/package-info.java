/**
 * {@code MailRepository} abstraction for persisting mail records. Concrete implementations
 * live in the {@link com.company.mailing_service.repository.mock} and
 * {@link com.company.mailing_service.repository.jpa} sub-packages, selected via
 * Spring profile / {@code @ConditionalOnProperty} — never by code changes.
 */
package com.company.mailing_service.infrastructure.persistence;
