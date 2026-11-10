package za.ac.mycput.domain.model;

/*
 * Lease.java
 * Domain entity representing a rental property.
 * Author : Malik Muhammed (230388175)
 * Date   : 20 March 2026
 */

import java.time.LocalDate;
import java.util.Objects;

public class Lease {
    private String leaseId;
    private String propertyId;
    private String tenantId;
    private String tenantName;
    private double monthlyRent;
    private LocalDate startDate;
    private LocalDate endDate;

    // Private constructor required for the Builder Pattern
    private Lease(Builder builder) {
        this.leaseId = builder.leaseId;
        this.propertyId = builder.propertyId;
        this.tenantId = builder.tenantId;
        this.tenantName = builder.tenantName;
        this.monthlyRent = builder.monthlyRent;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
    }

    // Getters
    public String getLeaseId() {
        return leaseId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public double getMonthlyRent() {
        return monthlyRent;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Active only when today falls inside a complete term. A future start is
     * upcoming. A missing date is not active.
     */
    public LeaseTermStatus termStatus(LocalDate today) {
        if (startDate == null || endDate == null || today == null) {
            return LeaseTermStatus.NEEDS_DATES;
        }
        if (endDate.isBefore(today)) {
            return LeaseTermStatus.ENDED;
        }
        if (startDate.isAfter(today)) {
            return LeaseTermStatus.UPCOMING;
        }
        return LeaseTermStatus.ACTIVE;
    }

    /**
     * The property stays reserved until the term is known to have ended.
     * A legacy row with no dates is not labelled active, and it is not released.
     */
    public boolean occupies(LocalDate today) {
        if (startDate == null || endDate == null || today == null) {
            return true;
        }
        return !endDate.isBefore(today);
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
                ", tenantId='" + tenantId + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", monthlyRent=" + monthlyRent +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    // Static Inner Builder Class
    public static class Builder {
        private String leaseId;
        private String propertyId;
        private String tenantId;
        private String tenantName;
        private double monthlyRent;
        private LocalDate startDate;
        private LocalDate endDate;

        public Builder setLeaseId(String leaseId) {
            this.leaseId = leaseId;
            return this;
        }

        public Builder setPropertyId(String propertyId) {
            this.propertyId = propertyId;
            return this;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
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

        public Builder setStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        // Copy method for updating existing objects
        public Builder copy(Lease lease) {
            this.leaseId = lease.leaseId;
            this.propertyId = lease.propertyId;
            this.tenantId = lease.tenantId;
            this.tenantName = lease.tenantName;
            this.monthlyRent = lease.monthlyRent;
            this.startDate = lease.startDate;
            this.endDate = lease.endDate;
            return this;
        }

        // Build method to create the final Lease object
        public Lease build() {
            return new Lease(this);
        }
    }
}
