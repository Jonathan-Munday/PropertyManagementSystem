package za.ac.mycput.service;

/*
 * TenantServiceTest.java
 * Service test for Tenant entity.
 * Author: Imaad Petersen (230166040)
 * Date: 19 July 2026
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.Tenant;
import za.ac.mycput.factory.TenantFactory;
import za.ac.mycput.service.impl.TenantService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TenantServiceTest {

    private static ITenantService service = new TenantService();
    private static Tenant tenant;

    @BeforeEach
    void setUp() {
        if (tenant == null) {
            tenant = TenantFactory.createTenant("John", "Doe", "john@test.com", "0123456789");
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        Tenant created = service.create(tenant);
        assertNotNull(created);
        assertEquals(tenant.getTenantId(), created.getTenantId());
        System.out.println("Created Tenant: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        Tenant read = service.read(tenant.getTenantId());
        assertNotNull(read);
        assertEquals(tenant.getTenantId(), read.getTenantId());
        System.out.println("Read Tenant: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        Tenant updatedTenant = new Tenant.Builder()
                .setTenantId(tenant.getTenantId())
                .setFirstName("Johnny")
                .setLastName(tenant.getLastName())
                .setEmail(tenant.getEmail())
                .setPhoneNumber(tenant.getPhoneNumber())
                .build();
        Tenant updated = service.update(updatedTenant);
        assertNotNull(updated);
        assertEquals("Johnny", updated.getFirstName());
        System.out.println("Updated Tenant: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<Tenant> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All Tenants: " + all);
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = service.delete(tenant.getTenantId());
        assertTrue(success);
        System.out.println("Deleted Tenant successfully: " + success);
    }
}
