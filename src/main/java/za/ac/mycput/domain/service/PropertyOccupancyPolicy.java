package za.ac.mycput.domain.service;

import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.domain.model.Property;
import za.ac.mycput.domain.valueobject.Money;

import java.time.LocalDate;
import java.util.List;

public class PropertyOccupancyPolicy {

    public void assertCanOpen(Property property, Lease lease, List<Lease> existing) {
        if (property == null) {
            throw new DomainRuleException("A lease needs an existing property");
        }
        if (lease == null || !property.getPropertyId().equals(lease.getPropertyId())) {
            throw new DomainRuleException("A lease must refer to the property being let");
        }
        if (!Money.zar(lease.getMonthlyRent()).isPositive()) {
            throw new DomainRuleException("Monthly rent must be positive");
        }
        assertTermIsFree(lease, existing);
    }

    /**
     * A stored availability flag is not enough. Fully dated terms conflict only
     * when the inclusive ranges overlap. A lease with a missing date still
     * reserves the property.
     */
    public void assertTermIsFree(Lease candidate, List<Lease> existing) {
        if (candidate == null || existing == null) {
            return;
        }
        for (Lease other : existing) {
            if (other == null || !candidate.getPropertyId().equals(other.getPropertyId())) {
                continue;
            }
            if (candidate.getLeaseId() != null && candidate.getLeaseId().equals(other.getLeaseId())) {
                continue;
            }
            if (other.getStartDate() == null || other.getEndDate() == null) {
                throw new DomainRuleException("A lease without dates still reserves this property");
            }
            if (candidate.getStartDate() == null || candidate.getEndDate() == null) {
                throw new DomainRuleException("A lease needs a start date and an end date");
            }
            if (overlaps(candidate.getStartDate(), candidate.getEndDate(), other.getStartDate(), other.getEndDate())) {
                throw new DomainRuleException("The lease term overlaps another lease on this property");
            }
        }
    }

    public static boolean overlaps(LocalDate start, LocalDate end, LocalDate otherStart, LocalDate otherEnd) {
        return !start.isAfter(otherEnd) && !otherStart.isAfter(end);
    }

    public void assertAvailability(Property property, List<Lease> leases, LocalDate today) {
        if (property == null || !property.isAvailable() || today == null) {
            return;
        }
        if (isOccupied(property.getPropertyId(), leases, today)) {
            throw new DomainRuleException("A leased property cannot be marked available");
        }
    }

    public boolean isOccupied(String propertyId, List<Lease> leases, LocalDate today) {
        if (propertyId == null || leases == null) {
            return false;
        }
        return leases.stream().anyMatch(lease ->
                propertyId.equals(lease.getPropertyId()) && lease.occupies(today));
    }
}
