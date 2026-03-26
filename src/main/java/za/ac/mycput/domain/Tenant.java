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

    public String gettenantId()  { return tenantId; }
    public String getfirstName() { return firstName; }
    public String getlastName()  { return lastName; }
    public String getemail()  { return email; }
    public String getphoneNumber() { return phoneNumber; }

    public String getFirstName() {
        return null;
    }

    public String getTenantId() {
        return null;
    }

    public String getLastName() {
        return null;
    }

    public String getEmail() {
        return null;
    }

    public String getPhoneNumber() {
        return null;
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

        public Builder setFirstName(String firstName){
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
            public Builder setphoneNumber(String phoneNumber){
                this.phoneNumber = phoneNumber;
                return this;
        }




            public Tenant build() {
               return new Tenant(this);

            }

        public Builder setPhoneNumber(String phoneNumber) {
            return null;
        }
    }
}

