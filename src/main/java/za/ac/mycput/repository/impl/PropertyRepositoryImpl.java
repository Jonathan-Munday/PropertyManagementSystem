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

    private static PropertyRepositoryImpl repository = null; // Singleton instance
    private final Set<Property> database = new HashSet<>();

    // Private constructor - prevents external instantiation [cite: 28]
    private PropertyRepositoryImpl() {}

    // Public static method to provide the single instance [cite: 92, 98]
    public static PropertyRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new PropertyRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Property create(Property property) {
        if (property == null) return null;
        boolean added = database.add(property);
        return added ? property : null;
    }

    @Override
    public Property read(String propertyId) {
        if (propertyId == null) return null;
        return database.stream()
                .filter(p -> propertyId.equals(p.getPropertyId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Property update(Property property) {
        if (property == null) return null;
        if (read(property.getPropertyId()) != null) {
            database.removeIf(p -> p.getPropertyId().equals(p.getPropertyId()));
            database.add(property);
            return property;
        }
        return null;
    }

    @Override
    public boolean delete(String propertyId) {
        if (propertyId == null) return false;
        return database.removeIf(p -> propertyId.equals(p.getPropertyId()));
    }

    @Override
    public List<Property> getAll() {
        return new ArrayList<>(database);
    }

    @Override
    public List<Property> findAvailable() {
        List<Property> result = new ArrayList<>();
        for (Property p : database) {
            if (p.isAvailable()) result.add(p);
        }
        return result;
    }
}
