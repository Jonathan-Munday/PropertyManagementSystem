/*
 * ITenantService.java
 * Service interface for Tenant entity.
 * Author: Imaad Petersen (230166040)
 * Date: 19 July 2026
 */
package za.ac.mycput.service;

import za.ac.mycput.domain.Tenant;
import java.util.List;

public interface ITenantService extends IService<Tenant, String> {
    List<Tenant> getAll();
}
