package com.company.mailing_service.infrastructure.persistence.jpa;

import com.company.mailing_service.domain.MailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailJpaDao extends JpaRepository<MailEntity, UUID> {
  Optional<MailEntity> findByIdempotencyKey(String idempotencyKey);

  List<MailEntity> findByStatus(MailStatus status);
}
