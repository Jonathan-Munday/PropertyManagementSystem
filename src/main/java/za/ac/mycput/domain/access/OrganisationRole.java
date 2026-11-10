package za.ac.mycput.domain.access;

import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.PermissionDeniedException;

public enum OrganisationRole {
    OWNER,
    ADMIN,
    PROPERTY_MANAGER,
    ACCOUNTANT,
    VIEWER;

    public static OrganisationRole fromStored(String value) {
        if (value == null || value.isBlank()) {
            throw new PermissionDeniedException("The membership role is not recognised");
        }
        try {
            return OrganisationRole.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new PermissionDeniedException("The membership role is not recognised");
        }
    }

    public static OrganisationRole fromRequest(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainRuleException("A role is required");
        }
        try {
            return OrganisationRole.valueOf(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleException("The role must be OWNER, ADMIN, PROPERTY_MANAGER, ACCOUNTANT, or VIEWER");
        }
    }
}
