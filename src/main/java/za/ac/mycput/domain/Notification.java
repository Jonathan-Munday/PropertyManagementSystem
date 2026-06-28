
package za.ac.mycput.domain;

/*
 * Notification.java
 * Entity for the Notification
 * Author: Imaad Petersen (230166040)
 * Date: 28 June 2026
 */

public class Notification {
    private String notificationId;
    private String tenantId;
    private String message;
    private String type;           // e.g., SMS, Email, System
    private String dateSent;

    private Notification(Builder builder) {
        this.notificationId = builder.notificationId;
        this.tenantId = builder.tenantId;
        this.message = builder.message;
        this.type = builder.type;
        this.dateSent = builder.dateSent;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getDateSent() {
        return dateSent;
    }

    public static class Builder {
        private String notificationId;
        private String tenantId;
        private String message;
        private String type;
        private String dateSent;

        public Builder setNotificationId(String notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setDateSent(String dateSent) {
            this.dateSent = dateSent;
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Notification)) return false;
        Notification notification = (Notification) obj;
        return notificationId.equals(notification.notificationId);
    }

    @Override
    public int hashCode() {
        return notificationId.hashCode();
    }
}

