/*
 * TenantService.java
 * Service implementation for Tenant entity.
 * Author: Imaad Petersen (230166040)
 * Date: 19 July 2026
 */
package za.ac.mycput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.Tenant;
import za.ac.mycput.repository.impl.TenantRepositoryImpl;
import za.ac.mycput.service.ITenantService;

import java.util.List;

@Service
public class TenantService implements ITenantService {

    private final TenantRepositoryImpl repository;

    public TenantService() {
        this.repository = TenantRepositoryImpl.getRepository();
    }

    @Override
    public Tenant create(Tenant tenant) {
        return repository.create(tenant);
    }

    @Override
    public Tenant read(String tenantId) {
        return repository.read(tenantId);
    }

    @Override
    public Tenant update(Tenant tenant) {
        return repository.update(tenant);
    }

    @Override
    public boolean delete(String tenantId) {
        return repository.delete(tenantId);
    }

    @Override
    public List<Tenant> getAll() {
        return repository.getAll();
    }
}
