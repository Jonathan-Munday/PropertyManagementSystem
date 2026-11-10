package za.ac.mycput.infrastructure.persistence;

import za.ac.mycput.domain.exception.DomainRuleException;

import java.util.UUID;

public final class Identifiers {

    private Identifiers() {
    }

    public static UUID parse(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainRuleException("An id is required");
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new DomainRuleException("Identifiers must be UUIDs");
        }
    }

    public static String format(UUID id) {
        return id.toString();
    }
}
