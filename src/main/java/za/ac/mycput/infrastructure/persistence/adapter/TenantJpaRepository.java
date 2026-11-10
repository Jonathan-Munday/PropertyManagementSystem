package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.TenantJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantJpaRepository extends JpaRepository<TenantJpaEntity, UUID> {
    Optional<TenantJpaEntity> findByOrganisationIdAndTenantId(UUID organisationId, UUID tenantId);
    List<TenantJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndTenantId(UUID organisationId, UUID tenantId);
    void deleteByOrganisationIdAndTenantId(UUID organisationId, UUID tenantId);
}
