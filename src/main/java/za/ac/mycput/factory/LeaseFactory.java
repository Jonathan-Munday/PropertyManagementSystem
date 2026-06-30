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

    public static Lease createLease(String leaseId, String propertyId, String tenantName, double monthlyRent) {

        if (leaseId == null || leaseId.isEmpty() || propertyId == null || propertyId.isEmpty() || 
            tenantName == null || tenantName.isEmpty() || monthlyRent < 0) {
            return null;
        }

        return new Lease.Builder()
                .setLeaseId(leaseId)
                .setPropertyId(propertyId)
                .setTenantName(tenantName)
                .setMonthlyRent(monthlyRent)
                .build();
    }
}
