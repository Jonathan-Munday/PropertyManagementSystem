/*
 * ILeaseService.java
 * Service interface for Lease entity.
 * Author: Malik Muhammed (230388175)
 * Date: 19 July 2026
 */
package za.ac.mycput.service;

import za.ac.mycput.domain.Lease;
import java.util.List;

public interface ILeaseService extends IService<Lease, String> {
    List<Lease> getAll();
}
