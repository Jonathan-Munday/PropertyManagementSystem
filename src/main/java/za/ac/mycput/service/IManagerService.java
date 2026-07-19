/*
 * IManagerService.java
 * Service interface for Manager entity.
 * Author: Anas Pansari (230195342)
 * Date: 19 July 2026
 */
package za.ac.mycput.service;

import za.ac.mycput.domain.Manager;
import java.util.List;

public interface IManagerService extends IService<Manager, String> {
    List<Manager> getAll();
}
