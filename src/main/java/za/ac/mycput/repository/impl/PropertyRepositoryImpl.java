/*
 * PropertyRepositoryImpl.java
 * In-memory implementation of IPropertyRepository.
 * Uses a {@link HashSet} as the backing store to guarantee
 * uniqueness by propertyId.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */

package za.ac.mycput.repository.impl;

import za.ac.mycput.domain.Property;
import za.ac.mycput.repository.IPropertyRepository;

import java.util.*;


public class PropertyRepositoryImpl implements IPropertyRepository {

    /** Simulated database. */
    private final Set<Property> database = new HashSet<>();

    // ---------------------------------------------------------------
    // IRepository<Property, String> implementation
    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     * <p>Rejects duplicates (same {@code propertyId}).</p>
     */
    @Override
    public Property create(Property property) {
        if (property == null) return null;
        boolean added = database.add(property);   // false if already present
        return added ? property : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Property read(String propertyId) {
        if (propertyId == null) return null;
        return database.stream()
                .filter(p -> propertyId.equals(p.getPropertyId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     * <p>Removes the old record and inserts the updated one.</p>
     */
    @Override
    public Property update(Property property) {
        if (property == null) return null;
        boolean removed = database.removeIf(
                p -> property.getPropertyId().equals(p.getPropertyId()));
        if (!removed) return null;          // nothing to update
        database.add(property);
        return property;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(String propertyId) {
        if (propertyId == null) return false;
        return database.removeIf(p -> propertyId.equals(p.getPropertyId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Property> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(database));
    }

    // ---------------------------------------------------------------
    // IPropertyRepository-specific queries
    // ---------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Property> findAvailable() {
        List<Property> result = new ArrayList<>();
        for (Property p : database) {
            if (p.isAvailable()) result.add(p);
        }
        return Collections.unmodifiableList(result);
    }
}
