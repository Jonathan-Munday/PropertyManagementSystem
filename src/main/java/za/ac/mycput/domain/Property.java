/*
 * Property.java
 * Domain entity representing a rental property.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */
package za.ac.mycput.domain;

/**
 * Immutable Property entity built with the Builder Pattern.
 * All fields are set once at construction time; there are no setters.
 */
public class Property {

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------
    private final String  propertyId;
    private final String  address;
    private final String  description;
    private final double  rentalAmount;
    private final boolean isAvailable;

    // ---------------------------------------------------------------
    // Private constructor – only Builder may call this
    // ---------------------------------------------------------------
    private Property(Builder builder) {
        this.propertyId    = builder.propertyId;
        this.address       = builder.address;
        this.description   = builder.description;
        this.rentalAmount  = builder.rentalAmount;
        this.isAvailable   = builder.isAvailable;
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------
    public String  getPropertyId()   { return propertyId;   }
    public String  getAddress()      { return address;       }
    public String  getDescription()  { return description;   }
    public double  getRentalAmount() { return rentalAmount;  }
    public boolean isAvailable()     { return isAvailable;   }

    // ---------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------
    @Override
    public String toString() {
        return "Property{" +
                "propertyId='"   + propertyId   + '\'' +
                ", address='"    + address       + '\'' +
                ", description='"+ description   + '\'' +
                ", rentalAmount="+ rentalAmount  +
                ", isAvailable=" + isAvailable   +
                '}';
    }

    // ---------------------------------------------------------------
    // equals & hashCode (identity based on propertyId)
    // ---------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;
        Property that = (Property) o;
        return propertyId != null && propertyId.equals(that.propertyId);
    }

    @Override
    public int hashCode() {
        return propertyId != null ? propertyId.hashCode() : 0;
    }

    // ---------------------------------------------------------------
    // Static nested Builder class
    // ---------------------------------------------------------------
    public static class Builder {

        private String  propertyId;
        private String  address;
        private String  description;
        private double  rentalAmount;
        private boolean isAvailable;

        public Builder propertyId(String propertyId) {
            this.propertyId = propertyId;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder rentalAmount(double rentalAmount) {
            this.rentalAmount = rentalAmount;
            return this;
        }

        public Builder isAvailable(boolean isAvailable) {
            this.isAvailable = isAvailable;
            return this;
        }

        /**
         * Validates mandatory fields and returns the immutable Property.
         *
         * @throws IllegalStateException if propertyId or address is null/blank
         */
        public Property build() {
            if (propertyId == null || propertyId.isBlank()) {
                throw new IllegalStateException("propertyId is required");
            }
            if (address == null || address.isBlank()) {
                throw new IllegalStateException("address is required");
            }
            return new Property(this);
        }
    }
}
