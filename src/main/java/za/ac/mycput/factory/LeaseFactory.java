/*
 * LeaseFactory.java
 * Domain entity representing a rental property.
 * Author : Malik Muhammed (230388175)
 * Date   : 20 March 2026
 */

package za.ac.mycput.factory;
/*
 * LeaseFactory.java
 * Factory for Lease and TermsAndConditions entities
 * Malik Muhammed (230388175)
 * 21/3/2026 (Updated: 28 June 2026)
 */

import za.ac.mycput.domain.Lease;
import za.ac.mycput.domain.TermsAndConditions;

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

    public static TermsAndConditions createTermsAndConditions(String termsId, String leaseId, String description, boolean accepted) {
        if (termsId == null || termsId.isEmpty() || leaseId == null || leaseId.isEmpty() || description == null || description.isEmpty()) {
            return null;
        }
        return new TermsAndConditions.Builder()
                .setTermsId(termsId)
                .setLeaseId(leaseId)
                .setDescription(description)
                .setAccepted(accepted)
                .build();
    }
}
