package com.company.mailing_service.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailRepository {
  MailRecord save(MailRecord record);

  Optional<MailRecord> findByIdempotencyKey(String idempotencyKey);

  Optional<MailRecord> findById(UUID id);

  List<MailRecord> findByStatus(MailStatus status);

  void updateStatus(UUID id, MailStatus status);

  void incrementAttempt(UUID id, String lastError);
}
