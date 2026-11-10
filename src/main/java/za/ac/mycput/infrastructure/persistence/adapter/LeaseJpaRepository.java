package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.LeaseJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaseJpaRepository extends JpaRepository<LeaseJpaEntity, UUID> {
    Optional<LeaseJpaEntity> findByOrganisationIdAndLeaseId(UUID organisationId, UUID leaseId);
    List<LeaseJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndLeaseId(UUID organisationId, UUID leaseId);
    void deleteByOrganisationIdAndLeaseId(UUID organisationId, UUID leaseId);
}
