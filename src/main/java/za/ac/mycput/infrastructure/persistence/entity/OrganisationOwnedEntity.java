package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class OrganisationOwnedEntity {

    @Column(name = "organisation_id", nullable = false, updatable = false)
    private UUID organisationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getOrganisationId() {
        return organisationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markCreated(UUID organisationId) {
        Instant now = Instant.now();
        this.organisationId = organisationId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markUpdated(UUID organisationId, Instant createdAt) {
        this.organisationId = organisationId;
        this.createdAt = createdAt;
        this.updatedAt = Instant.now();
    }
}
