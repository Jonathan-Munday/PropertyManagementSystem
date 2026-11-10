package za.ac.mycput.infrastructure.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.access.OrganisationRole;
import za.ac.mycput.domain.exception.OrganisationAccessException;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationMembershipJpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationMembershipJpaEntity;

import java.util.List;
import java.util.UUID;

@Service
public class OrganisationAccessService {

    private final OrganisationMembershipJpaRepository memberships;
    private final ScopedPersistence persistence;

    public OrganisationAccessService(OrganisationMembershipJpaRepository memberships, ScopedPersistence persistence) {
        this.memberships = memberships;
        this.persistence = persistence;
    }

    @Transactional
    public OrganisationScope resolve(UUID userId, String requestedOrganisationId) {
        persistence.bindUser(userId);
        List<OrganisationMembershipJpaEntity> active = memberships.findByUserIdAndStatus(userId, "ACTIVE");
        if (active.isEmpty()) {
            throw new OrganisationAccessException("This user is not an active member of an organisation");
        }
        if (requestedOrganisationId != null && !requestedOrganisationId.isBlank()) {
            UUID organisationId = Identifiers.parse(requestedOrganisationId);
            OrganisationMembershipJpaEntity membership = active.stream()
                    .filter(row -> organisationId.equals(row.getOrganisationId()))
                    .findFirst()
                    .orElseThrow(() -> new OrganisationAccessException("You are not a member of that organisation"));
            return scope(userId, membership);
        }
        if (active.size() == 1) {
            return scope(userId, active.get(0));
        }
        throw new OrganisationAccessException("Choose an organisation with the X-Organisation-Id header");
    }

    private OrganisationScope scope(UUID userId, OrganisationMembershipJpaEntity membership) {
        return new OrganisationScope(userId, membership.getOrganisationId(), OrganisationRole.fromStored(membership.getRole()));
    }
}
