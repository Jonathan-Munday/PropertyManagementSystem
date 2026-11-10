package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "maintenance_requests")
public class MaintenanceJpaEntity extends OrganisationOwnedEntity {

    @Id
    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "description", length = 4000, nullable = false)
    private String description;

    @Column(name = "status", length = 40, nullable = false)
    private String status;

    @Column(name = "reported_date", nullable = false)
    private LocalDate reportedDate;

    @Column(name = "cost", precision = 12, scale = 2, nullable = false)
    private BigDecimal cost;

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getReportedDate() { return reportedDate; }
    public void setReportedDate(LocalDate reportedDate) { this.reportedDate = reportedDate; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}
