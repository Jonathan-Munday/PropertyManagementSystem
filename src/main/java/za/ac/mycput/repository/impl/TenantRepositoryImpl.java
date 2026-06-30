package za.ac.mycput.repository.impl;
/*
 * TenantRepositoryImpl.java
 * Entity for the Tenant
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026
 */

import za.ac.mycput.domain.Tenant;
import za.ac.mycput.repository.ITenantRepository;

import java.util.ArrayList;
import java.util.List;

public class TenantRepositoryImpl implements ITenantRepository {


    private static TenantRepositoryImpl repository = null;


    private List<Tenant> tenantList;


    private TenantRepositoryImpl() {
        tenantList = new ArrayList<>();
    }


    public static TenantRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new TenantRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Tenant create(Tenant tenant) {
        tenantList.add(tenant);
        return tenant;
    }

    @Override
    public Tenant read(String id) {
        return tenantList.stream()
                .filter(t -> t.getTenantId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Tenant update(Tenant tenant) {
        Tenant existing = read(tenant.getTenantId());
        if (existing != null) {
            tenantList.remove(existing);
            tenantList.add(tenant);
            return tenant;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        Tenant tenant = read(id);
        if (tenant != null) {
            tenantList.remove(tenant);
            return true;
        }
        return false;
    }

    @Override
    public List<Tenant> getAll() {
        return tenantList;
    }
}