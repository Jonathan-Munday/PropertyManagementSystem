package za.ac.mycput.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import za.ac.mycput.application.dto.LeaseCommand;
import za.ac.mycput.application.usecase.CreateLeaseUseCase;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.EntityNotFoundException;
import za.ac.mycput.domain.factory.LeaseFactory;
import za.ac.mycput.domain.factory.PropertyFactory;
import za.ac.mycput.domain.factory.TenantFactory;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.domain.model.LeaseTermStatus;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.domain.repository.ITenantRepository;
import za.ac.mycput.domain.service.LeaseTermPolicy;
import za.ac.mycput.domain.service.PropertyOccupancyPolicy;
import za.ac.mycput.infrastructure.persistence.memory.LeaseRepositoryImpl;
import za.ac.mycput.infrastructure.persistence.memory.PropertyRepositoryImpl;
import za.ac.mycput.infrastructure.persistence.memory.TenantRepositoryImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaseApplicationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-09T10:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.now(CLOCK);

    private IPropertyRepository properties;
    private ILeaseRepository leases;
    private ITenantRepository tenants;
    private LeaseApplicationService service;

    @BeforeEach
    void setUp() {
        properties = new PropertyRepositoryImpl();
        leases = new LeaseRepositoryImpl();
        tenants = new TenantRepositoryImpl();
        service = service(leases);
        tenants.create(TenantFactory.createTenant("T1", "Ada", "Lovelace", "ada@example.com", "0210000000"));
        tenants.create(TenantFactory.createTenant("T2", "Grace", "Hopper", "grace@example.com", "0211111111"));
        properties.create(PropertyFactory.createProperty("P1", "1 Main Road", "Flat", 9000, true));
    }

    @Test
    void createsALeaseWithTenantPropertyTermAndRent() {
        Lease saved = service.create(active("L1", "P1", "T1", 9000));

        assertEquals("T1", saved.getTenantId());
        assertEquals("P1", saved.getPropertyId());
        assertEquals(TODAY.minusMonths(1), saved.getStartDate());
        assertEquals(TODAY.plusMonths(11), saved.getEndDate());
        assertEquals(9000, saved.getMonthlyRent());
        assertEquals(LeaseTermStatus.ACTIVE, saved.termStatus(TODAY));
        assertFalse(properties.read("P1").isAvailable());

        Lease reloaded = service.get("L1");
        assertEquals(saved.getTenantId(), reloaded.getTenantId());
        assertEquals(saved.getStartDate(), reloaded.getStartDate());
        assertEquals(saved.getEndDate(), reloaded.getEndDate());
        assertEquals(saved.getPropertyId(), reloaded.getPropertyId());
    }

    @Test
    void rejectsAMissingTenant() {
        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> service.create(new LeaseCommand("L1", "P1", "Ada Lovelace", 9000, " ", TODAY, TODAY.plusDays(1))));
        assertEquals("A lease needs a tenant", error.getMessage());
        assertNull(leases.read("L1"));
        assertTrue(properties.read("P1").isAvailable());
    }

    @Test
    void rejectsATenantOutsideTheActiveOrganisation() {
        assertThrows(EntityNotFoundException.class,
                () -> service.create(active("L1", "P1", "other-org-tenant", 9000)));
        assertTrue(properties.read("P1").isAvailable());
    }

    @Test
    void rejectsAMissingProperty() {
        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> service.create(new LeaseCommand("L1", " ", "Ada Lovelace", 9000, "T1", TODAY, TODAY.plusDays(1))));
        assertEquals("A lease needs a property", error.getMessage());
    }

    @Test
    void rejectsAPropertyOutsideTheActiveOrganisation() {
        assertThrows(EntityNotFoundException.class, () -> service.create(active("L1", "other-org-property", "T1", 9000)));
    }

    @Test
    void rejectsAMissingStartDate() {
        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> service.create(new LeaseCommand("L1", "P1", "Ada Lovelace", 9000, "T1", null, TODAY)));
        assertEquals("A lease needs a start date", error.getMessage());
    }

    @Test
    void rejectsAMissingEndDate() {
        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> service.create(new LeaseCommand("L1", "P1", "Ada Lovelace", 9000, "T1", TODAY, null)));
        assertEquals("A lease needs an end date", error.getMessage());
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> service.create(new LeaseCommand("L1", "P1", "Ada Lovelace", 9000, "T1", TODAY, TODAY.minusDays(1))));
        assertEquals("The end date cannot be before the start date", error.getMessage());
        assertNull(leases.read("L1"));
    }

    @Test
    void rejectsAnInvalidRentalAmount() {
        assertThrows(DomainRuleException.class, () -> service.create(active("L1", "P1", "T1", 0)));
        assertThrows(DomainRuleException.class, () -> service.create(active("L2", "P1", "T1", -5)));
        assertNull(leases.read("L1"));
        assertTrue(properties.read("P1").isAvailable());
    }

    @Test
    void derivesStatusFromTheTermAndLeavesLegacyRowsUndated() {
        Lease active = service.create(active("L1", "P1", "T1", 9000));
        assertEquals(LeaseTermStatus.ACTIVE, active.termStatus(TODAY));

        Lease upcoming = LeaseFactory.createLease(
                "L-future", "P1", "T1", "Ada Lovelace", 9000, TODAY.plusDays(10), TODAY.plusMonths(6));
        assertEquals(LeaseTermStatus.UPCOMING, upcoming.termStatus(TODAY));
        assertTrue(upcoming.occupies(TODAY));

        Lease ended = LeaseFactory.createLease(
                "L-past", "P1", "T1", "Ada Lovelace", 9000, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31));
        assertEquals(LeaseTermStatus.ENDED, ended.termStatus(TODAY));
        assertFalse(ended.occupies(TODAY));

        Lease legacy = LeaseFactory.createLease("L-legacy", "P1", "Ada Lovelace", 9000);
        assertNull(legacy.getStartDate());
        assertNull(legacy.getEndDate());
        assertEquals(LeaseTermStatus.NEEDS_DATES, legacy.termStatus(TODAY));
        assertTrue(legacy.occupies(TODAY));
    }

    @Test
    void updateKeepsTheTenantWhenTheRequestOmitsItAndRejectsAPropertyMove() {
        service.create(active("L1", "P1", "T1", 9000));

        Lease updated = service.update("L1", new LeaseCommand(
                "L1", "P1", "Ada Lovelace", 9500, null, TODAY.minusMonths(1), TODAY.plusMonths(11)));

        assertEquals("T1", updated.getTenantId());
        assertEquals(9500, updated.getMonthlyRent());
        assertEquals("P1", service.get("L1").getPropertyId());

        properties.create(PropertyFactory.createProperty("P2", "2 Beach Road", "Flat", 1000, true));
        assertThrows(DomainRuleException.class, () -> service.update("L1", new LeaseCommand(
                "L1", "P2", "Ada Lovelace", 9500, "T1", TODAY.minusMonths(1), TODAY.plusMonths(11))));
        assertEquals("P1", service.get("L1").getPropertyId());
        assertEquals("T1", service.get("L1").getTenantId());
    }

    @Test
    void aFutureLeaseReservesThePropertyUntilItsTermEnds() {
        service.create(new LeaseCommand(
                "L1", "P1", "Ada Lovelace", 9000, "T1", TODAY.plusDays(7), TODAY.plusMonths(6)));

        assertEquals(LeaseTermStatus.UPCOMING, service.get("L1").termStatus(TODAY));
        assertFalse(properties.read("P1").isAvailable());

        service.update("L1", new LeaseCommand(
                "L1", "P1", "Ada Lovelace", 9000, "T1", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)));

        assertEquals(LeaseTermStatus.ENDED, service.get("L1").termStatus(TODAY));
        assertTrue(properties.read("P1").isAvailable());
    }

    @Test
    void anExpiredLeaseCanBeFollowedByANewTermEvenWhenTheFlagIsStale() {
        properties.create(PropertyFactory.createProperty("P3", "3 Old Road", "Flat", 4000, false));
        leases.create(dated("L3-old", "P3", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)));

        Lease created = service.create(new LeaseCommand(
                "L3-new", "P3", "Ada Lovelace", 4500, "T1", TODAY.minusDays(1), TODAY.plusMonths(6)));

        assertEquals("L3-new", created.getLeaseId());
        assertEquals(LeaseTermStatus.ACTIVE, created.termStatus(TODAY));
        assertNotNull(leases.read("L3-old"));
        assertFalse(properties.read("P3").isAvailable());
    }

    @Test
    void anOverlappingLeaseIsRejectedAndThePropertyStaysUnavailable() {
        service.create(new LeaseCommand(
                "L1", "P1", "Ada Lovelace", 9000, "T1", TODAY.minusDays(10), TODAY.plusDays(10)));

        DomainRuleException error = assertThrows(DomainRuleException.class, () -> service.create(new LeaseCommand(
                "L2", "P1", "Grace Hopper", 8000, "T2", TODAY, TODAY.plusMonths(1))));

        assertEquals("The lease term overlaps another lease on this property", error.getMessage());
        assertNull(leases.read("L2"));
        assertFalse(properties.read("P1").isAvailable());
    }

    @Test
    void aFutureLeaseIsAcceptedWhenItStartsAfterTheCurrentTerm() {
        service.create(new LeaseCommand(
                "L1", "P1", "Ada Lovelace", 9000, "T1", TODAY.minusDays(10), TODAY.plusDays(10)));

        Lease future = service.create(new LeaseCommand(
                "L2", "P1", "Grace Hopper", 8000, "T2", TODAY.plusDays(11), TODAY.plusMonths(8)));

        assertEquals(LeaseTermStatus.UPCOMING, future.termStatus(TODAY));
        assertTrue(future.occupies(TODAY));
        assertFalse(properties.read("P1").isAvailable());
    }

    @Test
    void aLeaseThatStartsOnAnotherLeasesEndDateOverlaps() {
        service.create(new LeaseCommand(
                "L1", "P1", "Ada Lovelace", 9000, "T1", TODAY.minusDays(5), TODAY.plusDays(5)));

        assertThrows(DomainRuleException.class, () -> service.create(new LeaseCommand(
                "L2", "P1", "Grace Hopper", 8000, "T2", TODAY.plusDays(5), TODAY.plusMonths(2))));
        assertNull(leases.read("L2"));
    }

    @Test
    void aLegacyUndatedLeaseStillReservesTheProperty() {
        properties.create(PropertyFactory.createProperty("P4", "4 Loop Street", "Flat", 4000, false));
        leases.create(LeaseFactory.createLease("L4-legacy", "P4", "Ada Lovelace", 4000));

        DomainRuleException error = assertThrows(DomainRuleException.class,
                () -> service.create(active("L4-new", "P4", "T1", 4000)));

        assertEquals("A lease without dates still reserves this property", error.getMessage());
        assertNull(leases.read("L4-new"));
        assertFalse(properties.read("P4").isAvailable());
    }

    @Test
    void deletingTheLastOccupyingLeaseMakesThePropertyAvailable() {
        service.create(active("L1", "P1", "T1", 9000));
        assertFalse(properties.read("P1").isAvailable());

        service.delete("L1");

        assertNull(leases.read("L1"));
        assertTrue(properties.read("P1").isAvailable());
    }

    @Test
    void deletingOneLeaseLeavesThePropertyUnavailableWhileAnotherOccupyingLeaseRemains() {
        properties.create(PropertyFactory.createProperty("P9", "9 Long Street", "Flat", 4000, false));
        leases.create(dated("L9A", "P9", TODAY.minusDays(5), TODAY.plusMonths(3)));
        leases.create(dated("L9B", "P9", TODAY.plusDays(1), TODAY.plusMonths(8)));

        service.delete("L9A");

        assertNotNull(leases.read("L9B"));
        assertFalse(properties.read("P9").isAvailable());
    }

    @Test
    void deletingTheActiveLeaseReleasesAPropertyThatOnlyHasAnEndedLeaseLeft() {
        properties.create(PropertyFactory.createProperty("P8", "8 Short Street", "Flat", 4000, false));
        leases.create(dated("L8-old", "P8", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 6, 1)));
        leases.create(dated("L8-current", "P8", TODAY.minusDays(2), TODAY.plusDays(20)));

        service.delete("L8-current");

        assertTrue(properties.read("P8").isAvailable());
    }

    @Test
    void aLegacyLeaseStillHoldsThePropertyWhenAnotherLeaseIsDeleted() {
        properties.create(PropertyFactory.createProperty("P7", "7 Loop Street", "Flat", 4000, false));
        leases.create(LeaseFactory.createLease("L7-legacy", "P7", "Ada Lovelace", 4000));
        leases.create(dated("L7-current", "P7", TODAY.minusDays(1), TODAY.plusMonths(1)));

        service.delete("L7-current");

        assertFalse(properties.read("P7").isAvailable());
    }

    @Test
    void aDeletionBlockedByPaymentsDoesNotChangeAvailability() {
        ILeaseRepository blocked = new LeaseRepositoryImpl() {
            @Override
            public boolean delete(String leaseId) {
                throw new DataIntegrityViolationException("rent_payments_lease_org_fk");
            }
        };
        LeaseApplicationService blockedService = service(blocked);
        properties.create(PropertyFactory.createProperty("P6", "6 Bree Street", "Flat", 4000, false));
        blocked.create(dated("L6", "P6", TODAY.minusDays(1), TODAY.plusMonths(2)));

        assertThrows(DataIntegrityViolationException.class, () -> blockedService.delete("L6"));

        assertNotNull(blocked.read("L6"));
        assertFalse(properties.read("P6").isAvailable());
    }

    private LeaseApplicationService service(ILeaseRepository leaseRepository) {
        PropertyOccupancyPolicy occupancy = new PropertyOccupancyPolicy();
        LeaseTermPolicy terms = new LeaseTermPolicy();
        PropertyAvailability availability = new PropertyAvailability(properties, leaseRepository, occupancy, CLOCK);
        CreateLeaseUseCase create = new CreateLeaseUseCase(properties, leaseRepository, occupancy, terms, availability);
        return new LeaseApplicationService(
                leaseRepository, tenants, properties, create, terms, occupancy, availability);
    }

    private static LeaseCommand active(String leaseId, String propertyId, String tenantId, double rent) {
        return new LeaseCommand(
                leaseId, propertyId, "Ada Lovelace", rent, tenantId, TODAY.minusMonths(1), TODAY.plusMonths(11));
    }

    private static Lease dated(String leaseId, String propertyId, LocalDate start, LocalDate end) {
        return LeaseFactory.createLease(leaseId, propertyId, "T1", "Ada Lovelace", 4000, start, end);
    }
}
