package za.ac.mycput.repository.impl;
/*
 * TenantRepositoryImpl.java
 * Entity for the Tenant
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026
 */

import za.ac.mycput.domain.Tenant;
import za.ac.mycput.repository.ITenantRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TenantRepositoryImpl implements ITenantRepository {

    private final Set<Tenant> tenants = new HashSet<>();

    @Override
    public Tenant create(Tenant tenant) {
        tenants.add(tenant);
        return tenant;
    }
    @Override
    public Tenant read(String id) {
        for (Tenant tenant : tenants){
            if (tenant.getTenantId().equals(id)) {
                return tenant;
        }
    }
        return null;
}
@Override
public Tenant update(Tenant tenant) {
        Tenant oldTenant = read(tenant.getTenantId());
        if (oldTenant != null) {
            tenants.remove(oldTenant);
            tenants.add(tenant);
            return tenant;
        }
        return null;
    }
    @Override
    public boolean delete(String id) {
        Tenant tenant = read(id);
        if (tenant != null) {
            tenants.remove(tenant);
            return true;
        }
        return false;
    }
    @Override
    public List<Tenant> getAll() {
        return (List<Tenant>) tenants;
    }
}
