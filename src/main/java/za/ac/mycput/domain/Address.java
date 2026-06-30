package za.ac.mycput.domain;

/*
 * Address.java
 * Domain entity representing a physical address.
 * Author : Jonathan Munday (230677797)
 * Date   : 22 June 2026
 */
package za.ac.mycput.domain;

/**
 * Immutable Address entity built with the Builder Pattern.
 * All fields are set once at construction time; there are no setters.
 */
public class Address {

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------
    private final String  addressId;
    private final String  streetAddress;
    private final String  city;
    private final String  province;
    private final String  postalCode;

    // ---------------------------------------------------------------
    // Private constructor – only Builder may call this
    // ---------------------------------------------------------------
    private Address(Builder builder) {
        this.addressId      = builder.addressId;
        this.streetAddress  = builder.streetAddress;
        this.city           = builder.city;
        this.province       = builder.province;
        this.postalCode     = builder.postalCode;
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    public String  getAddressId()     { return addressId;     }
    public String  getStreetAddress() { return streetAddress; }
    public String  getCity()          { return city;          }
    public String  getProvince()      { return province;      }
    public String  getPostalCode()    { return postalCode;    }

    // ---------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------
    @Override
    public String toString() {
        return "Address{" +
                "addressId='"       + addressId     + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                ", city='"          + city          + '\'' +
                ", province='"      + province      + '\'' +
                ", postalCode='"    + postalCode    + '\'' +
                '}';
    }

    // ---------------------------------------------------------------
    // equals & hashCode (identity based on addressId)
    // ---------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address that = (Address) o;
        return addressId != null && addressId.equals(that.addressId);
    }

    @Override
    public int hashCode() {
        return addressId != null ? addressId.hashCode() : 0;
    }

    // ---------------------------------------------------------------
    // Static nested Builder class
    // ---------------------------------------------------------------
    public static class Builder {

        private String  addressId;
        private String  streetAddress;
        private String  city;
        private String  province;
        private String  postalCode;

        public Builder addressId(String addressId) {
            this.addressId = addressId;
            return this;
        }

        public Builder streetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder province(String province) {
            this.province = province;
            return this;
        }

        public Builder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        /**
         * Validates mandatory fields and returns the immutable Address.
         *
         * @throws IllegalStateException if addressId or streetAddress is null/blank
         */
        public Address build() {
            if (addressId == null || addressId.trim().isEmpty()) {
                throw new IllegalStateException("addressId is required");
            }
            if (streetAddress == null || streetAddress.trim().isEmpty()) {
                throw new IllegalStateException("streetAddress is required");
            }
            return new Address(this);
        }
    }
}