package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.domain.access.OrganisationRole;
import za.ac.mycput.domain.access.Permission;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.DuplicateEntityException;
import za.ac.mycput.domain.exception.OrganisationAccessException;
import za.ac.mycput.domain.exception.PermissionDeniedException;
import za.ac.mycput.domain.service.RolePermissionPolicy;
import za.ac.mycput.infrastructure.persistence.ScopedPersistence;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationInvitationJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.OrganisationMembershipJpaRepository;
import za.ac.mycput.infrastructure.persistence.adapter.UserProfileJpaRepository;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationInvitationJpaEntity;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationJpaEntity;
import za.ac.mycput.infrastructure.persistence.entity.OrganisationMembershipJpaEntity;
import za.ac.mycput.infrastructure.persistence.entity.UserProfileJpaEntity;
import za.ac.mycput.infrastructure.security.AuthenticatedUser;
import za.ac.mycput.infrastructure.security.OrganisationScope;
import za.ac.mycput.application.dto.AccountResponse;
import za.ac.mycput.application.dto.InvitationResponse;
import za.ac.mycput.application.dto.MemberResponse;
import za.ac.mycput.application.dto.OrganisationMembershipResponse;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccountApplicationService {

    private static final Duration INVITATION_TTL = Duration.ofDays(14);

    private final UserProfileJpaRepository profiles;
    private final OrganisationJpaRepository organisations;
    private final OrganisationMembershipJpaRepository memberships;
    private final OrganisationInvitationJpaRepository invitations;
    private final ScopedPersistence persistence;
    private final RolePermissionPolicy policy;
    private final SecureRandom random = new SecureRandom();

    public AccountApplicationService(UserProfileJpaRepository profiles,
                                     OrganisationJpaRepository organisations,
                                     OrganisationMembershipJpaRepository memberships,
                                     OrganisationInvitationJpaRepository invitations,
                                     ScopedPersistence persistence,
                                     RolePermissionPolicy policy) {
        this.profiles = profiles;
        this.organisations = organisations;
        this.memberships = memberships;
        this.invitations = invitations;
        this.persistence = persistence;
        this.policy = policy;
    }

    @Transactional
    public void ensureProfile(AuthenticatedUser actor) {
        persistence.bindUser(actor.userId());
        saveProfile(actor, null);
    }

    @Transactional
    public AccountResponse me(AuthenticatedUser actor) {
        persistence.bindUser(actor.userId());
        UserProfileJpaEntity profile = profiles.findById(actor.userId()).orElse(null);
        return account(actor, profile);
    }

    @Transactional
    public AccountResponse updateProfile(AuthenticatedUser actor, String displayName) {
        persistence.bindUser(actor.userId());
        UserProfileJpaEntity profile = saveProfile(actor, requiredText(displayName, "A display name is required"));
        return account(actor, profile);
    }

    @Transactional
    public List<OrganisationMembershipResponse> listOrganisations(AuthenticatedUser actor) {
        persistence.bindUser(actor.userId());
        return memberships.findByUserIdAndStatus(actor.userId(), "ACTIVE").stream()
                .map(this::membershipResponse)
                .sorted(Comparator.comparing(OrganisationMembershipResponse::organisationName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public OrganisationMembershipResponse createOrganisation(AuthenticatedUser actor, String name, String displayName) {
        persistence.bindUser(actor.userId());
        saveProfile(actor, requiredText(displayName, "A display name is required"));
        OrganisationJpaEntity organisation = new OrganisationJpaEntity();
        organisation.setId(UUID.randomUUID());
        organisation.setName(requiredText(name, "An organisation name is required"));
        organisation.setCreatedAt(Instant.now());
        organisation.setUpdatedAt(Instant.now());
        organisations.save(organisation);
        OrganisationMembershipJpaEntity membership = newMembership(organisation.getId(), actor.userId(), OrganisationRole.OWNER);
        memberships.save(membership);
        return new OrganisationMembershipResponse(organisation.getId(), organisation.getName(), membership.getRole(), membership.getStatus());
    }

    @Transactional
    public void deleteOrganisation(OrganisationScope scope) {
        requirePermission(scope, Permission.DELETE_ORGANISATION, "You do not have permission to delete this organisation");
        persistence.bind();
        organisations.deleteById(scope.organisationId());
    }

    @Transactional
    public List<MemberResponse> members(OrganisationScope scope) {
        requirePermission(scope, Permission.VIEW_MEMBERS, "You do not have permission to view members");
        persistence.bind();
        List<OrganisationMembershipJpaEntity> rows = memberships.findByOrganisationIdAndStatus(scope.organisationId(), "ACTIVE");
        Map<UUID, UserProfileJpaEntity> people = profiles.findAllById(rows.stream()
                        .map(OrganisationMembershipJpaEntity::getUserId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(UserProfileJpaEntity::getId, Function.identity()));
        return rows.stream()
                .map(row -> {
                    UserProfileJpaEntity profile = people.get(row.getUserId());
                    return new MemberResponse(
                            row.getUserId(),
                            profile == null ? null : profile.getEmail(),
                            profile == null ? null : profile.getDisplayName(),
                            row.getRole(),
                            row.getStatus());
                })
                .sorted(Comparator.comparing(MemberResponse::role).thenComparing(MemberResponse::email, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Transactional
    public InvitationResponse invite(AuthenticatedUser actor, OrganisationScope scope, String email, String requestedRole) {
        if (!scope.userId().equals(actor.userId())) {
            throw new OrganisationAccessException("The access token does not match the organisation membership");
        }
        requirePermission(scope, Permission.MANAGE_USERS, "You do not have permission to invite members");
        OrganisationRole granted = OrganisationRole.fromRequest(requestedRole);
        if (!policy.canGrant(scope.role(), granted)) {
            throw new PermissionDeniedException("You cannot grant that role");
        }
        String normalised = normaliseEmail(email);
        if (normalised == null) {
            throw new DomainRuleException("An email address is required");
        }
        persistence.bind();
        profiles.findByEmailIgnoreCase(normalised).ifPresent(existing -> memberships
                .findByOrganisationIdAndUserId(scope.organisationId(), existing.getId())
                .filter(row -> "ACTIVE".equals(row.getStatus()))
                .ifPresent(row -> {
                    throw new DuplicateEntityException("Member", normalised);
                }));
        boolean pending = invitations.findByOrganisationIdAndStatus(scope.organisationId(), "PENDING").stream()
                .anyMatch(row -> normalised.equalsIgnoreCase(row.getEmail()));
        if (pending) {
            throw new DuplicateEntityException("Invitation", normalised);
        }
        Instant now = Instant.now();
        OrganisationInvitationJpaEntity invitation = new OrganisationInvitationJpaEntity();
        invitation.setId(UUID.randomUUID());
        invitation.setOrganisationId(scope.organisationId());
        invitation.setEmail(normalised);
        invitation.setRole(granted.name());
        invitation.setStatus("PENDING");
        invitation.setToken(newToken());
        invitation.setExpiresAt(now.plus(INVITATION_TTL));
        invitation.setInvitedBy(actor.userId());
        invitation.setCreatedAt(now);
        invitation.setUpdatedAt(now);
        invitations.save(invitation);
        return invitationResponse(invitation);
    }

    @Transactional
    public OrganisationMembershipResponse acceptInvitation(AuthenticatedUser actor, String token) {
        String email = normaliseEmail(actor.email());
        if (email == null) {
            throw new PermissionDeniedException("This invitation does not match the signed-in account");
        }
        persistence.bindUser(actor.userId());
        saveProfile(actor, null);
        String presented = token == null ? "" : token.trim();
        OrganisationInvitationJpaEntity invitation = invitations.findByToken(presented)
                .orElseThrow(() -> new PermissionDeniedException("This invitation is not valid"));
        if (!"PENDING".equals(invitation.getStatus())) {
            throw new PermissionDeniedException("This invitation is no longer valid");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus("EXPIRED");
            invitation.setUpdatedAt(Instant.now());
            invitations.save(invitation);
            throw new PermissionDeniedException("This invitation has expired");
        }
        if (!email.equals(invitation.getEmail().toLowerCase(Locale.ROOT))) {
            throw new PermissionDeniedException("This invitation does not match the signed-in account");
        }
        if (memberships.findByOrganisationIdAndUserId(invitation.getOrganisationId(), actor.userId()).isPresent()) {
            throw new DuplicateEntityException("Membership", email);
        }
        OrganisationRole role = OrganisationRole.fromStored(invitation.getRole());
        OrganisationMembershipJpaEntity membership = newMembership(invitation.getOrganisationId(), actor.userId(), role);
        memberships.save(membership);
        invitation.setStatus("ACCEPTED");
        invitation.setUpdatedAt(Instant.now());
        invitations.save(invitation);
        OrganisationJpaEntity organisation = organisations.findById(invitation.getOrganisationId())
                .orElseThrow(() -> new OrganisationAccessException("You are not a member of that organisation"));
        return new OrganisationMembershipResponse(organisation.getId(), organisation.getName(), membership.getRole(), membership.getStatus());
    }

    private AccountResponse account(AuthenticatedUser actor, UserProfileJpaEntity profile) {
        String displayName = profile == null ? null : profile.getDisplayName();
        String email = profile != null && profile.getEmail() != null ? profile.getEmail() : normaliseEmail(actor.email());
        return new AccountResponse(
                actor.userId(),
                email,
                displayName,
                actor.emailVerified(),
                displayName != null && !displayName.isBlank(),
                listOrganisations(actor));
    }

    private OrganisationMembershipResponse membershipResponse(OrganisationMembershipJpaEntity membership) {
        OrganisationJpaEntity organisation = organisations.findById(membership.getOrganisationId()).orElse(null);
        String name = organisation == null ? "" : organisation.getName();
        return new OrganisationMembershipResponse(membership.getOrganisationId(), name, membership.getRole(), membership.getStatus());
    }

    private void requirePermission(OrganisationScope scope, Permission permission, String message) {
        if (!policy.permits(scope.role(), permission)) {
            throw new PermissionDeniedException(message);
        }
    }

    private UserProfileJpaEntity saveProfile(AuthenticatedUser actor, String displayName) {
        Instant now = Instant.now();
        UserProfileJpaEntity profile = profiles.findById(actor.userId()).orElseGet(() -> {
            UserProfileJpaEntity created = new UserProfileJpaEntity();
            created.setId(actor.userId());
            created.setCreatedAt(now);
            return created;
        });
        if (displayName != null && !displayName.isBlank()) {
            profile.setDisplayName(displayName.trim());
        }
        String email = normaliseEmail(actor.email());
        if (email != null) {
            profile.setEmail(email);
        }
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(now);
        }
        profile.setUpdatedAt(now);
        return profiles.save(profile);
    }

    private OrganisationMembershipJpaEntity newMembership(UUID organisationId, UUID userId, OrganisationRole role) {
        Instant now = Instant.now();
        OrganisationMembershipJpaEntity membership = new OrganisationMembershipJpaEntity();
        membership.setId(UUID.randomUUID());
        membership.setOrganisationId(organisationId);
        membership.setUserId(userId);
        membership.setRole(role.name());
        membership.setStatus("ACTIVE");
        membership.setCreatedAt(now);
        membership.setUpdatedAt(now);
        return membership;
    }

    private InvitationResponse invitationResponse(OrganisationInvitationJpaEntity invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getToken());
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String requiredText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DomainRuleException(message);
        }
        return value.trim();
    }

    private String normaliseEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
