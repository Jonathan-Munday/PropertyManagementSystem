package za.ac.mycput.domain.repository;

import za.ac.mycput.domain.model.Maintenance;

import java.util.List;

/*
*IMaintenanceRepository.Java
* Repository interface for Maintenance
* Olwethu Mtwazi (230036937)
* 21/3/2026
 */
public interface IMaintenanceRepository {
    Maintenance save (Maintenance maintenance);
    Maintenance findById(String requestId);
    List<Maintenance> findAll();
    Maintenance update (Maintenance maintenance);
    void delete (String requestId);
}
