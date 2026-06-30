package za.ac.mycput.factory;

/*
 * AddressFactory.java
 * Factory class for creating Address domain objects.
 * Author : Jonathan Munday (230677797)
 * Date   : 28 June 2026
 */
package za.ac.mycput.factory;

import za.ac.mycput.domain.Address;

/**
 * Factory for constructing {@link Address} instances.
 *
 * <p>Centralises object creation so that the rest of the application
 * never calls {@code new Address.Builder()} directly – keeping
 * construction logic in one place (Domain-Driven Design: Factory).</p>
 */
public class AddressFactory {

    // Utility class – prevent instantiation
    private AddressFactory() {}

    /**
     * Creates a fully populated {@link Address}.
     *
     * @param addressId     unique identifier (must not be null/blank)
     * @param streetAddress physical street address line (must not be null/blank)
     * @param city          the city where the address is located
     * @param province      the province where the address is located
     * @param postalCode    the numeric postal code area code
     * @return a new {@link Address} instance, or {@code null} if creation fails
     */
    public static Address createAddress(String addressId,
                                        String streetAddress,
                                        String city,
                                        String province,
                                        String postalCode) {
        try {
            return new Address.Builder()
                    .addressId(addressId)
                    .streetAddress(streetAddress)
                    .city(city)
                    .province(province)
                    .postalCode(postalCode)
                    .build();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}