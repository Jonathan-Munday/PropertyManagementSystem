/*
 * IPropertyRepository.java
 * Property-specific repository interface.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */
package za.ac.mycput.domain.repository;

import za.ac.mycput.domain.model.Property;
import java.util.List;

/**
 * Extends the generic {@link IRepository} with Property-specific queries.
 *
 * <p>The team lead creates this interface; each member creates a
 * corresponding interface for their own entity.</p>
 */
public interface IPropertyRepository extends IRepository<Property, String> {

    /**
     * Returns all properties that are currently available for rent.
     *
     * @return list of available properties (never {@code null})
     */
    List<Property> findAvailable();

    /**
     * Locks the property row for this organisation until the current transaction
     * commits. Callers use it before reading lease terms so two creates cannot
     * both decide the same property is free.
     *
     * @return {@code false} when this organisation has no such property
     */
    boolean lock(String propertyId);
}
