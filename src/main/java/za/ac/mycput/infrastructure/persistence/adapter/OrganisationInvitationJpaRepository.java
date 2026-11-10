package za.ac.mycput.infrastructure.persistence.adapter;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationInvitationJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganisationInvitationJpaRepository extends JpaRepository<OrganisationInvitationJpaEntity, UUID> {
    Optional<OrganisationInvitationJpaEntity> findByToken(String token);

    List<OrganisationInvitationJpaEntity> findByOrganisationIdAndStatus(UUID organisationId, String status);
}
