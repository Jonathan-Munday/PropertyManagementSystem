/*
 * PropertyService.java
 * Service implementation for Property entity.
 * Author: Jonathan Munday (230677797)
 * Date: 19 July 2026
 */
package za.ac.mycput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.Property;
import za.ac.mycput.repository.impl.PropertyRepositoryImpl;
import za.ac.mycput.service.IPropertyService;

import java.util.List;

@Service
public class PropertyService implements IPropertyService {

    private final PropertyRepositoryImpl repository;

    public PropertyService() {
        this.repository = PropertyRepositoryImpl.getRepository();
    }

    @Override
    public Property create(Property property) {
        return repository.create(property);
    }

    @Override
    public Property read(String propertyId) {
        return repository.read(propertyId);
    }

    @Override
    public Property update(Property property) {
        return repository.update(property);
    }

    @Override
    public boolean delete(String propertyId) {
        return repository.delete(propertyId);
    }

    @Override
    public List<Property> getAll() {
        return repository.getAll();
    }
}
