package com.company.mailing_service.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MailJpaDao extends JpaRepository<MailEntity, UUID> {
    Optional<MailEntity> findByIdempotencyKey(String idempotencyKey);
    void deleteAll();
}
