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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test fixtures for {@link PropertyRepositoryImpl}.
 *
 * <p>Each test method is independent: {@code @BeforeEach} creates a
 * fresh repository and two sample properties before every test,
 * eliminating order-dependency between tests.</p>
 */
class PropertyRepositoryTest {

    private IPropertyRepository repository;
    private Property            prop1;
    private Property            prop2;

    @BeforeEach
    void setUp() {
        repository = new PropertyRepositoryImpl();

        prop1 = PropertyFactory.createProperty(
                "P001", "10 Main Rd, Cape Town", "Spacious 2-bed flat", 8500.00, true);

        prop2 = PropertyFactory.createProperty(
                "P002", "5 Beach Ave, Durban", "Sea-view studio", 12000.00, false);
    }

    /* ----------------------------------------------------------------
     * CREATE
     * ----------------------------------------------------------------*/

    @Test
    void testCreate_returnsCreatedProperty() {
        Property result = repository.create(prop1);
        assertNotNull(result);
        assertEquals("P001", result.getPropertyId());
    }

    @Test
    void testCreate_duplicateId_returnsNull() {
        repository.create(prop1);
        Property duplicate = repository.create(prop1);   // same ID
        assertNull(duplicate, "Duplicate propertyId must be rejected");
    }

    @Test
    void testCreate_nullInput_returnsNull() {
        assertNull(repository.create(null));
    }

    /* ----------------------------------------------------------------
     * READ
     * ----------------------------------------------------------------*/

    @Test
    void testRead_existingId_returnsProperty() {
        repository.create(prop1);
        Property found = repository.read("P001");
        assertNotNull(found);
        assertEquals("P001", found.getPropertyId());
    }

    @Test
    void testRead_nonExistingId_returnsNull() {
        assertNull(repository.read("UNKNOWN"));
    }

    @Test
    void testRead_nullId_returnsNull() {
        assertNull(repository.read(null));
    }

    /* ----------------------------------------------------------------
     * UPDATE
     * ----------------------------------------------------------------*/

    @Test
    void testUpdate_existingProperty_returnsUpdated() {
        repository.create(prop1);

        // Build updated version of the same property (new rental amount)
        Property updated = PropertyFactory.createProperty(
                "P001", "10 Main Rd, Cape Town", "Renovated flat", 9500.00, true);

        Property result = repository.update(updated);
        assertNotNull(result);
        assertEquals(9500.00, result.getRentalAmount(), 0.001);
    }

    @Test
    void testUpdate_nonExistingProperty_returnsNull() {
        // prop1 was never saved
        Property result = repository.update(prop1);
        assertNull(result, "Update of non-existent property must return null");
    }

    /* ----------------------------------------------------------------
     * DELETE
     * ----------------------------------------------------------------*/

    @Test
    void testDelete_existingId_returnsTrue() {
        repository.create(prop1);
        assertTrue(repository.delete("P001"));
    }

    @Test
    void testDelete_nonExistingId_returnsFalse() {
        assertFalse(repository.delete("GHOST"));
    }

    @Test
    void testDelete_afterDelete_readReturnsNull() {
        repository.create(prop1);
        repository.delete("P001");
        assertNull(repository.read("P001"));
    }

    /* ----------------------------------------------------------------
     * GET ALL
     * ----------------------------------------------------------------*/

    @Test
    void testGetAll_returnsAllSavedProperties() {
        repository.create(prop1);
        repository.create(prop2);
        List<Property> all = repository.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void testGetAll_emptyRepository_returnsEmptyList() {
        assertTrue(repository.getAll().isEmpty());
    }

    /* ----------------------------------------------------------------
     * FIND AVAILABLE (Property-specific query)
     * ----------------------------------------------------------------*/

    @Test
    void testFindAvailable_returnsOnlyAvailableProperties() {
        repository.create(prop1);   // isAvailable = true
        repository.create(prop2);   // isAvailable = false

        List<Property> available = repository.findAvailable();
        assertEquals(1, available.size());
        assertTrue(available.get(0).isAvailable());
    }

    @Test
    void testFindAvailable_noneAvailable_returnsEmptyList() {
        repository.create(prop2);   // isAvailable = false
        assertTrue(repository.findAvailable().isEmpty());
    }
}
