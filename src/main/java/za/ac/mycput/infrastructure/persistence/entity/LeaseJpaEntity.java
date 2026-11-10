package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "leases")
public class LeaseJpaEntity extends OrganisationOwnedEntity {

    @Id
    @Column(name = "lease_id", nullable = false)
    private UUID leaseId;

    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "tenant_name", length = 200, nullable = false)
    private String tenantName;

    @Column(name = "monthly_rent", precision = 12, scale = 2, nullable = false)
    private BigDecimal monthlyRent;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public UUID getLeaseId() { return leaseId; }
    public void setLeaseId(UUID leaseId) { this.leaseId = leaseId; }
    public UUID getPropertyId() { return propertyId; }
    public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal monthlyRent) { this.monthlyRent = monthlyRent; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
