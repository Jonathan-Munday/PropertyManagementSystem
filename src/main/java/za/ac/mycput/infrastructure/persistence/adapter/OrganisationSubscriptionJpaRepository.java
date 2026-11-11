package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationSubscriptionJpaEntity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationSubscriptionJpaRepository extends JpaRepository<OrganisationSubscriptionJpaEntity, UUID> {
    Optional<OrganisationSubscriptionJpaEntity> findFirstByOrganisationIdAndStatusIn(
            UUID organisationId, Collection<String> statuses);

    boolean existsByOrganisationIdAndStatus(UUID organisationId, String status);

    boolean existsByOrganisationId(UUID organisationId);
}
