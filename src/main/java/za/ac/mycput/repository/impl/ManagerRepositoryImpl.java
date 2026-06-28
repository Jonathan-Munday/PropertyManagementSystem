package za.ac.mycput.repository.impl;

/*
  ManagerRepositoryImpl.java
  Entity for the Manager
  Author: Anas Pansari (230195342)
  Date: 25th March 2026
 */

import za.ac.mycput.domain.Manager;
import za.ac.mycput.repository.IManagerRepository;

import java.util.ArrayList;
import java.util.List;

public class ManagerRepositoryImpl implements IManagerRepository {

    private static ManagerRepositoryImpl repository = null;

    private List<Manager>managerList;

    public ManagerRepositoryImpl() {
        managerList = new ArrayList<>();
    }

    public static ManagerRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new ManagerRepositoryImpl();
        }
        return repository;
    }

    @Override
     public Manager create(Manager manager) {
        managerList.add(manager);
        return manager;
    }

    @Override
    public Manager read(String id) {
        return managerList.stream()
                .filter(m -> m.getManagerId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Manager update(Manager manager) {
        Manager existing = read(manager.getManagerId());
        if (existing != null) {
            managerList.remove(existing);
            managerList.add(manager);
            return manager;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        Manager manager = read(id);
        if (manager != null) {
            managerList.remove(manager);
            return true;

        }
        return false;
    }

    @Override
    public List<Manager> getAll() {
        return managerList;
    }


}
