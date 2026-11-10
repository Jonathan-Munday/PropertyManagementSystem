package za.ac.mycput.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationJpaEntity extends OrganisationOwnedEntity {

    @Id
    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "message", length = 2000, nullable = false)
    private String message;

    @Column(name = "notification_type", length = 40)
    private String type;

    @Column(name = "date_sent", length = 40)
    private String dateSent;

    public UUID getNotificationId() { return notificationId; }
    public void setNotificationId(UUID notificationId) { this.notificationId = notificationId; }
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDateSent() { return dateSent; }
    public void setDateSent(String dateSent) { this.dateSent = dateSent; }
}
