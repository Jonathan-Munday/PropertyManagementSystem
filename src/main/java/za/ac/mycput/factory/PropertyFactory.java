/*
 * PropertyFactory.java
 * Factory class for creating Property domain objects.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */
package za.ac.mycput.factory;

import za.ac.mycput.domain.Property;

/**
 * Factory for constructing {@link Property} instances.
 *
 * <p>Centralises object creation so that the rest of the application
 * never calls {@code new Property.Builder()} directly – keeping
 * construction logic in one place (Domain-Driven Design: Factory).</p>
 */
public class PropertyFactory {

    // Utility class – prevent instantiation
    private PropertyFactory() {}

    /**
     * Creates a fully populated {@link Property}.
     *
     * @param propertyId   unique identifier (must not be null/blank)
     * @param address      physical address  (must not be null/blank)
     * @param description  optional description of the property
     * @param rentalAmount monthly rental in ZAR
     * @param isAvailable  {@code true} if the property is currently vacant
     * @return a new {@link Property} instance, or {@code null} if creation fails
     */
    public static Property createProperty(String  propertyId,
                                          String  address,
                                          String  description,
                                          double  rentalAmount,
                                          boolean isAvailable) {
        try {
            return new Property.Builder()
                    .propertyId(propertyId)
                    .address(address)
                    .description(description)
                    .rentalAmount(rentalAmount)
                    .isAvailable(isAvailable)
                    .build();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
