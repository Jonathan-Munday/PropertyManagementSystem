/*
 * PropertyFactoryTest.java
 * TDD test class for PropertyFactory.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */
package za.ac.mycput.factory;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Property;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test fixtures for {@link PropertyFactory}.
 *
 * <p>Written in the RED → GREEN → REFACTOR TDD cycle:
 * write the test first, run (red), write the minimum factory
 * code to pass (green), then clean up (refactor).</p>
 */
class PropertyFactoryTest {

    /* ----------------------------------------------------------------
     * Happy-path tests
     * ----------------------------------------------------------------*/

    @Test
    void testCreateProperty_validInputs_returnsNonNull() {
        Property p = PropertyFactory.createProperty(
                "P001", "10 Main Rd, Cape Town", "Spacious flat", 8500.00, true);
        assertNotNull(p, "Factory must return a non-null Property for valid inputs");
    }

    @Test
    void testCreateProperty_propertyIdIsSet() {
        Property p = PropertyFactory.createProperty(
                "P001", "10 Main Rd, Cape Town", "Spacious flat", 8500.00, true);
        assertNotNull(p);
        assertEquals("P001", p.getPropertyId());
    }

    @Test
    void testCreateProperty_addressIsSet() {
        Property p = PropertyFactory.createProperty(
                "P001", "10 Main Rd, Cape Town", "Spacious flat", 8500.00, true);
        assertNotNull(p);
        assertEquals("10 Main Rd, Cape Town", p.getAddress());
    }

    @Test
    void testCreateProperty_rentalAmountIsSet() {
        Property p = PropertyFactory.createProperty(
                "P001", "10 Main Rd, Cape Town", "Spacious flat", 8500.00, true);
        assertNotNull(p);
        assertEquals(8500.00, p.getRentalAmount(), 0.001);
    }

    @Test
    void testCreateProperty_availabilityIsSet() {
        Property p = PropertyFactory.createProperty(
                "P002", "5 Beach Ave, Durban", "Sea view studio", 12000.00, false);
        assertNotNull(p);
        assertFalse(p.isAvailable());
    }

    /* ----------------------------------------------------------------
     * Failure / guard tests
     * ----------------------------------------------------------------*/

    @Test
    void testCreateProperty_nullPropertyId_returnsNull() {
        Property p = PropertyFactory.createProperty(
                null, "10 Main Rd", "desc", 5000.00, true);
        assertNull(p, "Factory must return null when propertyId is null");
    }

    @Test
    void testCreateProperty_blankPropertyId_returnsNull() {
        Property p = PropertyFactory.createProperty(
                "  ", "10 Main Rd", "desc", 5000.00, true);
        assertNull(p, "Factory must return null when propertyId is blank");
    }

    @Test
    void testCreateProperty_nullAddress_returnsNull() {
        Property p = PropertyFactory.createProperty(
                "P003", null, "desc", 5000.00, true);
        assertNull(p, "Factory must return null when address is null");
    }
}
