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
    private String tenantId;
    private double amount;

    // Private constructor required for the Builder Pattern
    private Lease(Builder builder) {
        this.leaseId = builder.leaseId;
        this.tenantId = builder.tenantId;
        this.amount = builder.amount;
    }

    // Getters
    public String getLeaseId() {
        return leaseId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public double getAmount() {
        return amount;
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
                ", tenantId='" + tenantId + '\'' +
                ", amount=" + amount +
                '}';
    }

    // Static Inner Builder Class
    public static class Builder {
        private String leaseId;
        private String tenantId;
        private double amount;

        public Builder setLeaseId(String leaseId) {
            this.leaseId = leaseId;
            return this;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        // Copy method for updating existing objects
        public Builder copy(Lease lease) {
            this.leaseId = lease.leaseId;
            this.tenantId = lease.tenantId;
            this.amount = lease.amount;
            return this;
        }

        // Build method to create the final Lease object
        public Lease build() {
            return new Lease(this);
        }
    }
}
