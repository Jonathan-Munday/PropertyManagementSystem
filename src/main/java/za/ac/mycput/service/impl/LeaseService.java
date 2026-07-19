/*
 * LeaseService.java
 * Service implementation for Lease entity.
 * Author: Malik Muhammed (230388175)
 * Date: 19 July 2026
 */
package za.ac.mycput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.Lease;
import za.ac.mycput.repository.impl.LeaseRepositoryImpl;
import za.ac.mycput.service.ILeaseService;

import java.util.List;

@Service
public class LeaseService implements ILeaseService {

    private final LeaseRepositoryImpl repository;

    public LeaseService() {
        this.repository = LeaseRepositoryImpl.getRepository();
    }

    @Override
    public Lease create(Lease lease) {
        return repository.create(lease);
    }

    @Override
    public Lease read(String leaseId) {
        return repository.read(leaseId);
    }

    @Override
    public Lease update(Lease lease) {
        return repository.update(lease);
    }

    @Override
    public boolean delete(String leaseId) {
        return repository.delete(leaseId);
    }

    @Override
    public List<Lease> getAll() {
        return repository.getAll();
    }
}
