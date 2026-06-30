package za.ac.mycput.domain;
/*
 * Tenant.java
 * Entity for the Tenant
 * Author: Imaad Petersen (230166040)
 * Date: 24 March 2026
 */

public class Tenant {
    private String tenantId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;


    private Tenant(Builder builder) {
        this.tenantId = builder.tenantId;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public static class Builder {
        private String tenantId;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }
        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Tenant build() {
            return new Tenant(this);
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tenant)) return false;
        Tenant tenant = (Tenant) obj;
        return tenantId.equals(tenant.tenantId);
    }

    @Override
    public int hashCode() {
        return tenantId.hashCode();
    }
}
