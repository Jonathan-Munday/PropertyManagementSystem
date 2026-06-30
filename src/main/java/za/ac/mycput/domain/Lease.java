/*
 * Lease.java
 * Domain entity representing a rental property.
 * Author : Malik Muhammed (230388175)
 * Date   : 20 March 2026
 */

package za.ac.mycput.domain;

import java.util.Objects;

public class Lease {
    private String leaseId;
    private String propertyId;
    private String tenantName;
    private double monthlyRent;

    // Private constructor required for the Builder Pattern
    private Lease(Builder builder) {
        this.leaseId = builder.leaseId;
        this.propertyId = builder.propertyId;
        this.tenantName = builder.tenantName;
        this.monthlyRent = builder.monthlyRent;
    }

    // Getters
    public String getLeaseId() {
        return leaseId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public double getMonthlyRent() {
        return monthlyRent;
    }

    // equals() and hashCode() crucial for HashSet deduplication
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lease lease = (Lease) o;
        return Objects.equals(leaseId, lease.leaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leaseId);
    }

    @Override
    public String toString() {
        return "Lease{" +
                "leaseId='" + leaseId + '\'' +
                ", propertyId='" + propertyId + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", monthlyRent=" + monthlyRent +
                '}';
    }

    // Static Inner Builder Class
    public static class Builder {
        private String leaseId;
        private String propertyId;
        private String tenantName;
        private double monthlyRent;

        public Builder setLeaseId(String leaseId) {
            this.leaseId = leaseId;
            return this;
        }

        public Builder setPropertyId(String propertyId) {
            this.propertyId = propertyId;
            return this;
        }

        public Builder setTenantName(String tenantName) {
            this.tenantName = tenantName;
            return this;
        }

        public Builder setMonthlyRent(double monthlyRent) {
            this.monthlyRent = monthlyRent;
            return this;
        }

        // Copy method for updating existing objects
        public Builder copy(Lease lease) {
            this.leaseId = lease.leaseId;
            this.propertyId = lease.propertyId;
            this.tenantName = lease.tenantName;
            this.monthlyRent = lease.monthlyRent;
            return this;
        }

        // Build method to create the final Lease object
        public Lease build() {
            return new Lease(this);
        }
    }
}
