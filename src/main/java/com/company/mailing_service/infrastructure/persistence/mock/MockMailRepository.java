package com.company.mailing_service.infrastructure.persistence.mock;

import com.company.mailing_service.domain.MailRecord;
import com.company.mailing_service.domain.MailStatus;
import com.company.mailing_service.domain.MailRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(prefix = "mailing", name = "repository-mode", havingValue = "mock", matchIfMissing = true)
public class MockMailRepository implements MailRepository {

    private final ConcurrentHashMap<UUID, MailRecord> records = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> idempotencyIndex = new ConcurrentHashMap<>();

    @Override
    public MailRecord save(MailRecord record) {
        UUID id = record.getId() != null ? record.getId() : UUID.randomUUID();

        if(idempotencyIndex.putIfAbsent(record.getIdempotencyKey(), id) != null){
            throw new DataIntegrityViolationException(
                    "Duplicate idempotency key: " + record.getIdempotencyKey());
        }
        Instant now = Instant.now();

        MailRecord toStore = record.toBuilder()
                .id(id)
                .createdAt(record.getCreatedAt() != null ? record.getCreatedAt() : now)
                .updatedAt(now)
                .build();

        records.put(id, toStore);

        return toStore;
    }

    @Override
    public Optional<MailRecord> findByIdempotencyKey(String idempotencyKey) {
        return Optional.ofNullable(idempotencyIndex.get(idempotencyKey))
                .map(records::get);
    }

    @Override
    public void updateStatus(UUID id, MailStatus status) {
        records.computeIfPresent(id, (key, existing) -> existing.toBuilder()
                .status(status)
                .updatedAt(Instant.now())
                .build());
    }

    @Override
    public void incrementAttempt(UUID id, String lastError) {
        records.computeIfPresent(id, (key, existing) -> existing.toBuilder()
                .attemptCount(existing.getAttemptCount() + 1)
                .lastError(lastError)
                .updatedAt(Instant.now())
                .build());
    }
}
