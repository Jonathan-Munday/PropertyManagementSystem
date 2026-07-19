/*
 * IMaintenanceService.java
 * Service interface for Maintenance entity.
 * Author: Olwethu Mtwazi (230036937)
 * Date: 19 July 2026
 */
package za.ac.mycput.service;

import za.ac.mycput.domain.Maintenance;
import java.util.List;

public interface IMaintenanceService extends IService<Maintenance, String> {
    List<Maintenance> getAll();
}
