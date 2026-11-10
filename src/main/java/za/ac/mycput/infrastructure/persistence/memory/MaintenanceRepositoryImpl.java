package za.ac.mycput.infrastructure.persistence.memory;

/*
*MaintenanceRepositoryImpl.Java
* Implementation of Maintenance Repository class
* Olwethu Mtwazi (230036937)
* 21/3/2026
 */


import za.ac.mycput.domain.model.Maintenance;
import za.ac.mycput.domain.repository.IMaintenanceRepository;

import java.util.*;

public class MaintenanceRepositoryImpl implements IMaintenanceRepository {
    private static MaintenanceRepositoryImpl repository = null;

    private Map<String,Maintenance> maintenanceMap;

    public MaintenanceRepositoryImpl(){
        maintenanceMap = new HashMap<>();
    }
    public static MaintenanceRepositoryImpl getRepository(){
        if(repository == null){
            repository = new MaintenanceRepositoryImpl();
        }
        return repository;
    }


    @Override
    public Maintenance save(Maintenance maintenance){
        maintenanceMap.put(maintenance.getRequestId(), maintenance);
        return maintenance;
    }

    @Override
    public Maintenance findById(String requestId){
        return maintenanceMap.get(requestId);
    }

    @Override
    public List<Maintenance> findAll(){
            return List.copyOf(maintenanceMap.values());
    }


    @Override
    public Maintenance update (Maintenance maintenance){
        if(maintenanceMap.containsKey(maintenance.getRequestId())){
            maintenanceMap.put(maintenance.getRequestId(), maintenance);
            return maintenance;
        }
        return null;
    }
    @Override
    public void delete(String requestId){
        maintenanceMap.remove(requestId);
    }
}
