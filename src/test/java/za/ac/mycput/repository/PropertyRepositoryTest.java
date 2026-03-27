
/*
 * PropertyRepositoryTest.java
 * TDD test class for PropertyRepositoryImpl.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */

package za.ac.mycput.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Property;
import za.ac.mycput.factory.PropertyFactory;
import za.ac.mycput.repository.impl.PropertyRepositoryImpl;

import static org.junit.jupiter.api.Assertions.*;

class PropertyRepositoryTest {

    private IPropertyRepository repository;
    private Property prop1;
    private Property prop2;

    @BeforeEach
    void setUp() {
        // Use the Singleton accessor
        repository = PropertyRepositoryImpl.getRepository();

        // Manual clear since state persists in a Singleton during a test run
        repository.getAll().forEach(p -> repository.delete(p.getPropertyId()));

        prop1 = PropertyFactory.createProperty(
                "P001", "10 Main Rd, Cape Town", "Spacious 2-bed flat", 8500.00, true);

        prop2 = PropertyFactory.createProperty(
                "P002", "5 Beach Ave, Durban", "Sea-view studio", 12000.00, false);
    }

    @Test
    void testCreate() {
        Property created = repository.create(prop1);
        assertNotNull(created);
        assertEquals(prop1.getPropertyId(), created.getPropertyId());
    }

    @Test
    void testRead() {
        repository.create(prop1);
        Property read = repository.read(prop1.getPropertyId());
        assertNotNull(read);
    }

    @Test
    void testUpdate() {
        repository.create(prop1);
        Property updated = new Property.Builder().propertyId("P001")
                .address("Updated Address")
                .build();
        assertNotNull(repository.update(updated));
    }

    @Test
    void testDelete() {
        repository.create(prop1);
        assertTrue(repository.delete(prop1.getPropertyId()));
    }

    @Test
    void testGetAll() {
        repository.create(prop1);
        repository.create(prop2);
        assertEquals(2, repository.getAll().size());
    }
}
