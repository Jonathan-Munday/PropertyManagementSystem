/*
 * ManagerService.java
 * Service implementation for Manager entity.
 * Author: Anas Pansari (230195342)
 * Date: 19 July 2026
 */
package za.ac.mycput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.Manager;
import za.ac.mycput.repository.impl.ManagerRepositoryImpl;
import za.ac.mycput.service.IManagerService;

import java.util.List;

@Service
public class ManagerService implements IManagerService {

    private final ManagerRepositoryImpl repository;

    public ManagerService() {
        this.repository = ManagerRepositoryImpl.getRepository();
    }

    @Override
    public Manager create(Manager manager) {
        return repository.create(manager);
    }

    @Override
    public Manager read(String managerId) {
        return repository.read(managerId);
    }

    @Override
    public Manager update(Manager manager) {
        return repository.update(manager);
    }

    @Override
    public boolean delete(String managerId) {
        return repository.delete(managerId);
    }

    @Override
    public List<Manager> getAll() {
        return repository.getAll();
    }
}
