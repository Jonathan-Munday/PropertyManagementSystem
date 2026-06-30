package za.ac.mycput.domain;
/*
 * Vendor.java
 * Entity for the Vendor
 * Author: Olwethu Mtwazi (230036937)
 * Date: 27 June 2026
 */
public class Vendor {
    private String vendorId;
    private String companyName;
    private String contactNumber;
    private String specialty;

    private Vendor() {
    }

    private Vendor(Builder builder) {
        this.vendorId = builder.vendorId;
        this.companyName = builder.companyName;
        this.contactNumber = builder.contactNumber;
        this.specialty = builder.specialty;
    }

    public String getVendorId() { return vendorId; }
    public String getCompanyName() { return companyName; }
    public String getContactNumber() { return contactNumber; }
    public String getSpecialty() { return specialty; }

    public static class Builder {
        private String vendorId;
        private String companyName;
        private String contactNumber;
        private String specialty;

        public Builder setVendorId(String vendorId) {
            this.vendorId = vendorId;
            return this;
        }

        public Builder setCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
            return this;
        }

        public Builder setSpecialty(String specialty) {
            this.specialty = specialty;
            return this;
        }

        public Builder copy(Vendor vendor) {
            this.vendorId = vendor.vendorId;
            this.companyName = vendor.companyName;
            this.contactNumber = vendor.contactNumber;
            this.specialty = vendor.specialty;
            return this;
        }

        public Vendor build() {
            return new Vendor(this);
        }
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "vendorId='" + vendorId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", specialty='" + specialty + '\'' +
                '}';
    }
}