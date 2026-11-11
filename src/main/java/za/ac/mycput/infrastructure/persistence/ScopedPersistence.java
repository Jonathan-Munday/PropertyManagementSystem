package za.ac.mycput.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import za.ac.mycput.infrastructure.persistence.adapter.AuditLogJpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.AuditLogJpaEntity;
import za.ac.mycput.infrastructure.security.OrganisationContext;
import za.ac.mycput.infrastructure.security.OrganisationScope;

import java.time.Instant;
import java.util.UUID;

/**
 * Records the caller for this transaction and writes audit rows.
 * Repository queries filter by organisation id. The Java database login owns the tables,
 * so row-level security does not filter these queries.
 */
@Component
public class ScopedPersistence {

    private final EntityManager entityManager;
    private final AuditLogJpaRepository auditLogs;

    public ScopedPersistence(EntityManager entityManager, AuditLogJpaRepository auditLogs) {
        this.entityManager = entityManager;
        this.auditLogs = auditLogs;
    }

    public UUID organisationId() {
        return OrganisationContext.require().organisationId();
    }

    public void bind() {
        OrganisationScope scope = OrganisationContext.require();
        setConfig("app.current_organisation_id", scope.organisationId());
        setConfig("app.current_user_id", scope.userId());
    }

    public void bindUser(UUID userId) {
        setConfig("app.current_user_id", userId);
    }

    public void flush() {
        entityManager.flush();
    }

    public void refresh(Object entity) {
        entityManager.refresh(entity);
    }

    public void audit(String action, String entityType, UUID entityId) {
        OrganisationScope scope = OrganisationContext.require();
        AuditLogJpaEntity row = new AuditLogJpaEntity();
        row.setId(UUID.randomUUID());
        row.setOrganisationId(scope.organisationId());
        row.setActorUserId(scope.userId());
        row.setAction(action);
        row.setEntityType(entityType);
        row.setEntityId(entityId);
        row.setRecordedAt(Instant.now());
        auditLogs.save(row);
    }

    private void setConfig(String name, UUID value) {
        entityManager.createNativeQuery("select set_config(:name, :value, true)")
                .setParameter("name", name)
                .setParameter("value", value.toString())
                .getSingleResult();
    }
}
