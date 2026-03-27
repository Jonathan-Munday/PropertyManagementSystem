/*
 * LeaseFactoryTest.java
 * Factory Test
 * Malik Muhammed (230388175)
 * 21/3/2026
 */

package za.ac.mycput.factory;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Lease;

import static org.junit.jupiter.api.Assertions.*;

class LeaseFactoryTest {

    @Test
    void testCreateLeaseSuccess() {
        Lease lease = LeaseFactory.createLease(
                "L001",
                "P001",
                "John Doe",
                8500.00
        );

        assertNotNull(lease);
        assertEquals("L001", lease.getLeaseId());
        assertEquals("P001", lease.getPropertyId());
        assertEquals("John Doe", lease.getTenantName());
        assertEquals(8500.00, lease.getMonthlyRent());
        System.out.println("Lease created successfully: " + lease);
    }

    @Test
    void testCreateLeaseWithNullLeaseId() {
        Lease lease = LeaseFactory.createLease(
                null,
                "P001",
                "John Doe",
                8500.00
        );

        assertNull(lease);
        System.out.println("Failed to create lease with null leaseId as expected.");
    }

    @Test
    void testCreateLeaseWithEmptyTenantName() {
        Lease lease = LeaseFactory.createLease(
                "L001",
                "P001",
                "",
                8500.00
        );

        assertNull(lease);
        System.out.println("Failed to create lease with empty tenantName as expected.");
    }
}
