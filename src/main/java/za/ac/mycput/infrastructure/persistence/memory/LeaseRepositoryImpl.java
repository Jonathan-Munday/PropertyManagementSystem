/*
 * LeaseRepositoryImpl.java
 * Domain entity representing a rental property.
 * Author : Malik Muhammed (230388175)
 * Date   : 20 March 2026
 */

package za.ac.mycput.infrastructure.persistence.memory;


/*
* Lease.java
* Lease for the tenant
* Malik Muhammed (230388175)
* 21/3/2026
 */

import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.domain.repository.ILeaseRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeaseRepositoryImpl implements ILeaseRepository {

    private static LeaseRepositoryImpl repository = null;
    private Set<Lease> leaseDB;

    public LeaseRepositoryImpl() {
        this.leaseDB = new HashSet<>();
    }

    public static LeaseRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new LeaseRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Lease create(Lease lease) {
        boolean success = leaseDB.add(lease);
        if (!success) return null;
        return lease;
    }

    @Override
    public Lease read(String leaseId) {
        for (Lease l : leaseDB) {
            if (l.getLeaseId().equals(leaseId)) {
                return l;
            }
        }
        return null;
    }

    @Override
    public Lease update(Lease lease) {
        Lease oldLease = read(lease.getLeaseId());
        if (oldLease != null) {
            leaseDB.remove(oldLease);
            leaseDB.add(lease);
            return lease;
        }
        return null;
    }

    @Override
    public boolean delete(String leaseId) {
        Lease leaseToDelete = read(leaseId);
        if (leaseToDelete != null) {
            return leaseDB.remove(leaseToDelete);
        }
        return false;
    }

    @Override
    public List<Lease> getAll() {
        return List.copyOf(leaseDB);
    }
}
