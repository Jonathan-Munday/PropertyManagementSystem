package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.MaintenanceJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceJpaRepository extends JpaRepository<MaintenanceJpaEntity, UUID> {
    Optional<MaintenanceJpaEntity> findByOrganisationIdAndRequestId(UUID organisationId, UUID requestId);
    List<MaintenanceJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndRequestId(UUID organisationId, UUID requestId);
    void deleteByOrganisationIdAndRequestId(UUID organisationId, UUID requestId);
}
