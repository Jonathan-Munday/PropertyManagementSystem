package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.AuditLogJpaEntity;

import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, UUID> {
}
