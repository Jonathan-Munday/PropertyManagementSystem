package za.ac.mycput.domain.repository;
import za.ac.mycput.domain.repository.ITenantRepository;

/*
 * TenantRepositoryTest.java
 * Entity for the Tenant
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.model.Tenant;
import za.ac.mycput.domain.factory.TenantFactory;
import za.ac.mycput.infrastructure.persistence.memory.TenantRepositoryImpl;

import static org.junit.jupiter.api.Assertions.*;

public class TenantRepositoryTest {

    private ITenantRepository repository;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        repository = TenantRepositoryImpl.getRepository();

        tenant = TenantFactory.createTenant(
                "John",
                "Doe",
                "john@test.com",
                "0123456789"
        );
    }

    @Test
    void create() {
        Tenant created = repository.create(tenant);
        assertNotNull(created);
        assertEquals(tenant.getTenantId(), created.getTenantId());
    }

    @Test
    void read() {
        repository.create(tenant);
        Tenant read = repository.read(tenant.getTenantId());
        assertNotNull(read);
        assertEquals(tenant.getTenantId(), read.getTenantId());
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

        assertNotNull(updated);
        assertEquals("Mike", updated.getFirstName());
    }

    @Test
    void delete() {
        repository.create(tenant);

        boolean deleted = repository.delete(tenant.getTenantId());

        assertTrue(deleted);
        assertNull(repository.read(tenant.getTenantId()));
    }

    @Test
    void getAll() {
        repository.create(tenant);

        assertFalse(repository.getAll().isEmpty());
    }
}