package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.ManagerJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManagerJpaRepository extends JpaRepository<ManagerJpaEntity, UUID> {
    Optional<ManagerJpaEntity> findByOrganisationIdAndManagerId(UUID organisationId, UUID managerId);
    List<ManagerJpaEntity> findByOrganisationId(UUID organisationId);
    boolean existsByOrganisationIdAndManagerId(UUID organisationId, UUID managerId);
    void deleteByOrganisationIdAndManagerId(UUID organisationId, UUID managerId);
}
