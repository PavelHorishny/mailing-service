package com.company.mailing_service.repository.jpa;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.repository.MailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mailing", name = "repository-mode", havingValue = "jpa")
public class JpaMailRepository implements MailRepository {

    private final MailJpaDao dao;

    @Override
    public MailRecord save(MailRecord record) {
        UUID id = record.getId() != null ? record.getId() : UUID.randomUUID();
        Instant now = Instant.now();

        MailEntity entity = new MailEntity(
                id,
                record.getIdempotencyKey(),
                record.getRecipient(),
                record.getTemplateKey(),
                record.getLocale(),
                record.getStatus(),
                record.getAttemptCount(),
                record.getLastError(),
                record.getCreatedAt() != null ? record.getCreatedAt() : now,
                now
        );

        return toRecord(dao.save(entity));
    }

    @Override
    public Optional<MailRecord> findByIdempotencyKey(String idempotencyKey) {
        return dao.findByIdempotencyKey(idempotencyKey).map(this::toRecord);
    }

    @Override
    public void updateStatus(UUID id, MailStatus status) {
        dao.findById(id).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setUpdatedAt(Instant.now());
            dao.save(entity);
        });
    }

    @Override
    public void incrementAttempt(UUID id, String lastError) {
        dao.findById(id).ifPresent(entity -> {
            entity.setAttemptCount(entity.getAttemptCount() + 1);
            entity.setLastError(lastError);
            entity.setUpdatedAt(Instant.now());
            dao.save(entity);
        });
    }

    private MailRecord toRecord(MailEntity e) {
        return MailRecord.builder()
                .id(e.getId())
                .idempotencyKey(e.getIdempotencyKey())
                .recipient(e.getRecipient())
                .templateKey(e.getTemplateKey())
                .locale(e.getLocale())
                .status(e.getStatus())
                .attemptCount(e.getAttemptCount())
                .lastError(e.getLastError())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}

