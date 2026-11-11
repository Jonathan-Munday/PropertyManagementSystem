package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationMembershipJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationMembershipJpaRepository extends JpaRepository<OrganisationMembershipJpaEntity, UUID> {
    List<OrganisationMembershipJpaEntity> findByUserIdAndStatus(UUID userId, String status);

    List<OrganisationMembershipJpaEntity> findByOrganisationIdAndStatus(UUID organisationId, String status);

    Optional<OrganisationMembershipJpaEntity> findByOrganisationIdAndUserId(UUID organisationId, UUID userId);

    long countByOrganisationIdAndStatus(UUID organisationId, String status);
}
