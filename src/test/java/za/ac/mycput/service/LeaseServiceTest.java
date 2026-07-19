package za.ac.mycput.service;

/*
 * LeaseServiceTest.java
 * Service test for Lease entity.
 * Author: Malik Muhammed (230388175)
 * Date: 19 July 2026
 */

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.Lease;
import za.ac.mycput.factory.LeaseFactory;
import za.ac.mycput.service.impl.LeaseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LeaseServiceTest {

    private static ILeaseService service = new LeaseService();
    private static Lease lease;

    @BeforeAll
    static void cleanUp() {
        service.delete("L001");
    }

    @BeforeEach
    void setUp() {
        if (lease == null) {
            lease = LeaseFactory.createLease("L001", "P001", "John Doe", 8500.00);
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        Lease created = service.create(lease);
        assertNotNull(created);
        assertEquals(lease.getLeaseId(), created.getLeaseId());
        System.out.println("Created Lease: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        Lease read = service.read(lease.getLeaseId());
        assertNotNull(read);
        assertEquals(lease.getLeaseId(), read.getLeaseId());
        System.out.println("Read Lease: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        Lease updatedLease = new Lease.Builder().copy(lease).setMonthlyRent(9500.00).build();
        Lease updated = service.update(updatedLease);
        assertNotNull(updated);
        assertEquals(9500.00, updated.getMonthlyRent());
        System.out.println("Updated Lease: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<Lease> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All Leases: " + all);
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = service.delete(lease.getLeaseId());
        assertTrue(success);
        System.out.println("Deleted Lease successfully: " + success);
    }
}
