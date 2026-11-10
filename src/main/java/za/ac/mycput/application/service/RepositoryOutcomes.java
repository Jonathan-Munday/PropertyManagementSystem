package za.ac.mycput.application.service;

import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.DuplicateEntityException;
import za.ac.mycput.domain.exception.EntityNotFoundException;

public final class RepositoryOutcomes {

    private RepositoryOutcomes() {
    }

    public static <T> T built(T value, String message) {
        if (value == null) {
            throw new DomainRuleException(message);
        }
        return value;
    }

    public static void absent(Object existing, String entity, String id) {
        if (existing != null) {
            throw new DuplicateEntityException(entity, id);
        }
    }

    public static <T> T created(T saved, String entity, String id) {
        if (saved == null) {
            throw new DuplicateEntityException(entity, id);
        }
        return saved;
    }

    public static <T> T existing(T found, String entity, String id) {
        if (found == null) {
            throw new EntityNotFoundException(entity, id);
        }
        return found;
    }

    public static <T> T updated(T saved, String entity, String id) {
        if (saved == null) {
            throw new EntityNotFoundException(entity, id);
        }
        return saved;
    }

    public static void deleted(boolean removed, String entity, String id) {
        if (!removed) {
            throw new EntityNotFoundException(entity, id);
        }
    }
}
