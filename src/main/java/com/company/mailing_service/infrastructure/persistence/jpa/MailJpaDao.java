package com.company.mailing_service.infrastructure.persistence.jpa;

import com.company.mailing_service.domain.MailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MailJpaDao extends JpaRepository<MailEntity, UUID> {
  Optional<MailEntity> findByIdempotencyKey(String idempotencyKey);

  List<MailEntity> findByStatus(MailStatus status);

  @Query(value = """
    SELECT * FROM mail_records
    WHERE status = 'FAILED_RETRYING'
    ORDER BY created_at
    LIMIT :limit
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
  List<MailEntity> findByStatusFailed(@Param("limit") int limit);
}
