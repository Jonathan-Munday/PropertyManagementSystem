/*
 * LeaseFactory.java
 * Domain entity representing a rental property.
 * Author : Malik Muhammed (230388175)
 * Date   : 20 March 2026
 */

package za.ac.mycput.domain.factory;
/*
* Lease.java
* Lease for the tenant
* Malik Muhammed (230388175)
* 21/3/2026
 */

import za.ac.mycput.domain.model.Lease;

import java.time.LocalDate;

public class LeaseFactory {

    public static Lease createLease(String leaseId, String propertyId, String tenantName, double monthlyRent) {
        return createLease(leaseId, propertyId, null, tenantName, monthlyRent, null, null);
    }

    public static Lease createLease(String leaseId,
                                    String propertyId,
                                    String tenantId,
                                    String tenantName,
                                    double monthlyRent,
                                    LocalDate startDate,
                                    LocalDate endDate) {

        if (leaseId == null || leaseId.isEmpty() || propertyId == null || propertyId.isEmpty() ||
            tenantName == null || tenantName.isEmpty() || monthlyRent < 0) {
            return null;
        }

        Lease.Builder builder = new Lease.Builder()
                .setLeaseId(leaseId)
                .setPropertyId(propertyId)
                .setTenantName(tenantName)
                .setMonthlyRent(monthlyRent)
                .setStartDate(startDate)
                .setEndDate(endDate);
        if (tenantId != null && !tenantId.isBlank()) {
            builder.setTenantId(tenantId.trim());
        }
        return builder.build();
    }
}
