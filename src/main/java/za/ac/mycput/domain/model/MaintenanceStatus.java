package za.ac.mycput.domain.model;

import za.ac.mycput.domain.exception.DomainRuleException;

/**
 * Controlled maintenance workflow. Stored as the enum name.
 */
public enum MaintenanceStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED;

    public static MaintenanceStatus parse(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainRuleException("A maintenance request needs a status");
        }
        String key = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        if ("INPROGRESS".equals(key)) {
            key = "IN_PROGRESS";
        }
        try {
            return valueOf(key);
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleException("Maintenance status must be OPEN, IN_PROGRESS, or RESOLVED");
        }
    }
}
