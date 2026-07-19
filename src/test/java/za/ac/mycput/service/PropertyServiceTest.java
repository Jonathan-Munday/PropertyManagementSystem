package za.ac.mycput.service;

/*
 * PropertyServiceTest.java
 * Service test for Property entity.
 * Author: Jonathan Munday (230677797)
 * Date: 19 July 2026
 */

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.Property;
import za.ac.mycput.factory.PropertyFactory;
import za.ac.mycput.service.impl.PropertyService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PropertyServiceTest {

    private static IPropertyService service = new PropertyService();
    private static Property property;

    @BeforeAll
    static void cleanUp() {
        service.delete("P001");
    }

    @BeforeEach
    void setUp() {
        if (property == null) {
            property = PropertyFactory.createProperty("P001", "10 Main Rd, Cape Town", "Spacious flat", 8500.00, true);
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        Property created = service.create(property);
        assertNotNull(created);
        assertEquals(property.getPropertyId(), created.getPropertyId());
        System.out.println("Created Property: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        Property read = service.read(property.getPropertyId());
        assertNotNull(read);
        assertEquals(property.getPropertyId(), read.getPropertyId());
        System.out.println("Read Property: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        Property updatedProperty = new Property.Builder()
                .propertyId(property.getPropertyId())
                .address(property.getAddress())
                .description(property.getDescription())
                .rentalAmount(9000.0)
                .isAvailable(property.isAvailable())
                .build();
        Property updated = service.update(updatedProperty);
        assertNotNull(updated);
        assertEquals(9000.0, updated.getRentalAmount());
        System.out.println("Updated Property: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<Property> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All Properties: " + all);
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = service.delete(property.getPropertyId());
        assertTrue(success);
        System.out.println("Deleted Property successfully: " + success);
    }
}
