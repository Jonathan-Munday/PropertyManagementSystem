package za.ac.mycput.factory;
/*
 * TenantFactoryTest.java
 * Entity for the Tenant
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026
 */

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Tenant;

import static org.junit.jupiter.api.Assertions.*;


public class TenantFactoryTest {

    @Test
    void createTenantSuccess() {
        Tenant tenant = TenantFactory.createTenant(
                "John",
                "Doe",
                "john@test.com",
                "0123456789"
        );

        assertNotNull(tenant);
        assertEquals("John", tenant.getFirstName());
        assertEquals("Doe", tenant.getLastName());

        System.out.println(tenant);
    }

    @Test
    void createTenantFailure() {
        Tenant tenant = TenantFactory.createTenant(
                "",
                "Doe",
                "john@test.com",
                "0123456789"
        );

        assertNull(tenant);
    }
}
