/*
 * MaintenanceService.java
 * Service implementation for Maintenance entity.
 * Author: Olwethu Mtwazi (230036937)
 * Date: 19 July 2026
 */
package za.ac.mycput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.Maintenance;
import za.ac.mycput.repository.impl.MaintenanceRepositoryImpl;
import za.ac.mycput.service.IMaintenanceService;

import java.util.List;

@Service
public class MaintenanceService implements IMaintenanceService {

    private final MaintenanceRepositoryImpl repository;

    public MaintenanceService() {
        this.repository = MaintenanceRepositoryImpl.getRepository();
    }

    @Override
    public Maintenance create(Maintenance maintenance) {
        return repository.save(maintenance);
    }

    @Override
    public Maintenance read(String requestId) {
        return repository.findById(requestId);
    }

    @Override
    public Maintenance update(Maintenance maintenance) {
        return repository.update(maintenance);
    }

    @Override
    public boolean delete(String requestId) {
        if (repository.findById(requestId) != null) {
            repository.delete(requestId);
            return true;
        }
        return false;
    }

    @Override
    public List<Maintenance> getAll() {
        return repository.findAll();
    }
}
