package za.ac.mycput.interfaces.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.mycput.application.service.AccountApplicationService;
import za.ac.mycput.domain.exception.OrganisationAccessException;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.security.CurrentUser;
import za.ac.mycput.infrastructure.security.OrganisationContext;
import za.ac.mycput.infrastructure.security.OrganisationScope;
import za.ac.mycput.interfaces.rest.dto.AcceptInvitationRequest;
import za.ac.mycput.interfaces.rest.dto.CreateInvitationRequest;
import za.ac.mycput.interfaces.rest.dto.CreateOrganisationRequest;
import za.ac.mycput.application.dto.AccountResponse;
import za.ac.mycput.application.dto.InvitationResponse;
import za.ac.mycput.application.dto.MemberResponse;
import za.ac.mycput.application.dto.OrganisationMembershipResponse;
import za.ac.mycput.interfaces.rest.dto.UpdateProfileRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountApplicationService accounts;

    public AccountController(AccountApplicationService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/me")
    public AccountResponse me() {
        return accounts.me(CurrentUser.require());
    }

    @PutMapping("/me")
    public AccountResponse updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return accounts.updateProfile(CurrentUser.require(), request.displayName());
    }

    @GetMapping("/organisations")
    public List<OrganisationMembershipResponse> organisations() {
        return accounts.listOrganisations(CurrentUser.require());
    }

    @PostMapping("/organisations")
    public ResponseEntity<OrganisationMembershipResponse> createOrganisation(@Valid @RequestBody CreateOrganisationRequest request) {
        OrganisationMembershipResponse created = accounts.createOrganisation(
                CurrentUser.require(), request.name(), request.displayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/organisations/{organisationId}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable String organisationId) {
        accounts.deleteOrganisation(scoped(organisationId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/organisations/{organisationId}/members")
    public List<MemberResponse> members(@PathVariable String organisationId) {
        return accounts.members(scoped(organisationId));
    }

    @PostMapping("/organisations/{organisationId}/invitations")
    public ResponseEntity<InvitationResponse> invite(@PathVariable String organisationId,
                                                     @Valid @RequestBody CreateInvitationRequest request) {
        InvitationResponse created = accounts.invite(CurrentUser.require(), scoped(organisationId), request.email(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/invitations/accept")
    public OrganisationMembershipResponse accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return accounts.acceptInvitation(CurrentUser.require(), request.token());
    }

    private OrganisationScope scoped(String organisationId) {
        OrganisationScope scope = OrganisationContext.require();
        UUID requested = Identifiers.parse(organisationId);
        if (!scope.organisationId().equals(requested) || !scope.userId().equals(CurrentUser.require().userId())) {
            throw new OrganisationAccessException("You are not a member of that organisation");
        }
        return scope;
    }
}
