package za.ac.mycput.repository;
/*
 * TenantRepositoryTest.java
 * Entity for the Tenant
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026
 */
import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Tenant;
import za.ac.mycput.factory.TenantFactory;
import za.ac.mycput.repository.impl.TenantRepositoryImpl;

import static org.junit.jupiter.api.Assertions.*;

public class TenantRepositoryTest {

    private ITenantRepository repository = new TenantRepositoryImpl();

    private Tenant tenant = TenantFactory.createTenant(
            "john",
            "Doe",
            "john@test.com",
            "0123456789"
    );
    @Test
    void create() {
        Tenant created = repository.create(tenant);
        assertNotNull(created);
    }

    @Test
    void update() {
        repository.create(tenant);

        Tenant updatedTenant = new Tenant.Builder()
                .setTenantId(tenant.getTenantId())
                .setFirstName("Mike")
                .setLastName(tenant.getLastName())
                .setEmail(tenant.getEmail())
                .setPhoneNumber(tenant.getPhoneNumber())
                .build();

        Tenant updated = repository.update(updatedTenant);
        assertEquals("Mike", updated.getFirstName());
    }
    @Test
    void delete() {
        repository.create(tenant);
        assertFalse(repository.getAll().isEmpty());

    }
}
