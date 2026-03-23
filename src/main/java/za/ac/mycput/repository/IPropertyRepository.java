/*
 * IPropertyRepository.java
 * Property-specific repository interface.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */
package za.ac.mycput.repository;

import za.ac.mycput.domain.Property;
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
}
