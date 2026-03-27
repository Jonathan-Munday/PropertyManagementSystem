/*
 * LeaseFactory.java
 * Domain entity representing a rental property.
 * Author : Malik Muhammed (230388175)
 * Date   : 20 March 2026
 */

package za.ac.mycput.factory;
/*
* Lease.java
* Lease for the tenant
* Malik Muhammed (230388175)
* 21/3/2026
 */

import za.ac.mycput.domain.Lease;

public class LeaseFactory {

    public static Lease buildLease(String leaseId, String tenantId, double amount) {

        // 1. Basic validation: If any crucial data is missing, return null
        // Note: If you have a Helper class (e.g., Helper.isNullOrEmpty), you can use that here instead!
        if (leaseId == null || leaseId.isEmpty() || tenantId == null || tenantId.isEmpty() || amount < 0) {
            return null;
        }

        // 2. Use the Builder to create and return the Lease object
        return new Lease.Builder()
                .setLeaseId(leaseId)
                .setTenantId(tenantId)
                .setAmount(amount)
                .build();
    }
}
