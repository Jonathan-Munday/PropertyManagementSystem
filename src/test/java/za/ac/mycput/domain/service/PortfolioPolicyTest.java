package za.ac.mycput.domain.service;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.factory.LeaseFactory;
import za.ac.mycput.domain.factory.PropertyFactory;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.domain.model.Property;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioPolicyTest {

    private final PropertyOccupancyPolicy occupancy = new PropertyOccupancyPolicy();
    private final RentCollectionPolicy rent = new RentCollectionPolicy();
    private final RentalAmountPolicy rentalAmounts = new RentalAmountPolicy();
    private final MaintenanceCostPolicy maintenance = new MaintenanceCostPolicy();

    @Test
    void openingALeaseUsesTheTermRatherThanTheAvailabilityFlag() {
        Property unavailable = PropertyFactory.createProperty("P1", "1 Main", "Flat", 8000, false);
        LocalDate today = LocalDate.of(2026, 11, 9);
        Lease expired = LeaseFactory.createLease(
                "L0", "P1", "T1", "Ada Lovelace", 8000, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
        Lease next = LeaseFactory.createLease(
                "L1", "P1", "T1", "Ada Lovelace", 8000, today, today.plusMonths(6));
        Lease touching = LeaseFactory.createLease(
                "L2", "P1", "T1", "Ada Lovelace", 8000, LocalDate.of(2020, 12, 31), LocalDate.of(2021, 6, 30));
        Lease following = LeaseFactory.createLease(
                "L3", "P1", "T1", "Ada Lovelace", 8000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 6, 30));
        Lease free = LeaseFactory.createLease("L4", "P1", "T1", "Ada Lovelace", 0, today, today.plusMonths(1));
        Lease legacy = LeaseFactory.createLease("L5", "P1", "Ada Lovelace", 8000);

        assertDoesNotThrow(() -> occupancy.assertCanOpen(unavailable, next, List.of(expired)));
        assertThrows(DomainRuleException.class, () -> occupancy.assertCanOpen(unavailable, touching, List.of(expired)));
        assertDoesNotThrow(() -> occupancy.assertCanOpen(unavailable, following, List.of(expired)));
        assertThrows(DomainRuleException.class, () -> occupancy.assertCanOpen(unavailable, free, List.of()));
        assertThrows(DomainRuleException.class, () -> occupancy.assertCanOpen(unavailable, next, List.of(legacy)));
    }

    @Test
    void leasedPropertyCannotBeMarkedAvailable() {
        Property available = PropertyFactory.createProperty("P1", "1 Main", "Flat", 8000, true);
        Lease lease = LeaseFactory.createLease("L1", "P1", "Ada Lovelace", 8000);
        LocalDate today = LocalDate.of(2026, 11, 9);
        assertThrows(DomainRuleException.class, () -> occupancy.assertAvailability(available, List.of(lease), today));

        Lease ended = LeaseFactory.createLease(
                "L2", "P1", "T1", "Ada Lovelace", 8000, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
        assertDoesNotThrow(() -> occupancy.assertAvailability(available, List.of(ended), today));
    }

    @Test
    void rentPaymentMustBePositiveAndLinkedToALease() {
        assertThrows(DomainRuleException.class, () -> rent.assertCanRecord(" ", 100, "EFT"));
        assertThrows(DomainRuleException.class, () -> rent.assertCanRecord("L1", 0, "EFT"));
        assertThrows(DomainRuleException.class, () -> rent.assertCanRecord("L1", 100, ""));
        assertDoesNotThrow(() -> rent.assertCanRecord("L1", 100, "EFT"));
    }

    @Test
    void rentalAndMaintenanceAmountsCannotBeNegative() {
        assertThrows(DomainRuleException.class, () -> rentalAmounts.assertNonNegative(-1));
        assertDoesNotThrow(() -> rentalAmounts.assertNonNegative(0));
        assertThrows(DomainRuleException.class, () -> maintenance.assertValid("Leak", "OPEN", -5));
        assertThrows(DomainRuleException.class, () -> maintenance.assertValid(" ", "OPEN", 0));
        assertDoesNotThrow(() -> maintenance.assertValid("Leak", "OPEN", 0));
        assertThrows(DomainRuleException.class, () -> maintenance.assertValid("Leak", "BROKEN", 0));
    }
}
