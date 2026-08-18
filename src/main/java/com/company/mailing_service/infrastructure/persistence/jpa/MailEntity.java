package com.company.mailing_service.infrastructure.persistence.jpa;
import com.company.mailing_service.domain.MailStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

@Entity
@Table(name = "mail_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_mail_records_idempotency_key", columnNames = "idempotency_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailEntity {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String recipient;

    @Column(name = "template_key", nullable = false)
    private String templateKey;

    @Column(nullable = false)
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    @Generated(event = EventType.INSERT)
    private Instant updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
