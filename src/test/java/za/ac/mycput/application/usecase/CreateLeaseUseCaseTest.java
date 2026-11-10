package za.ac.mycput.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.mycput.application.dto.LeaseCommand;
import za.ac.mycput.application.service.PropertyAvailability;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.EntityNotFoundException;
import za.ac.mycput.domain.factory.LeaseFactory;
import za.ac.mycput.domain.factory.PropertyFactory;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPropertyRepository;
import za.ac.mycput.domain.service.LeaseTermPolicy;
import za.ac.mycput.domain.service.PropertyOccupancyPolicy;
import za.ac.mycput.infrastructure.persistence.memory.LeaseRepositoryImpl;
import za.ac.mycput.infrastructure.persistence.memory.PropertyRepositoryImpl;

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

class CreateLeaseUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-09T10:00:00Z"), ZoneOffset.UTC);

    private IPropertyRepository properties;
    private ILeaseRepository leases;
    private CreateLeaseUseCase useCase;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        properties = new PropertyRepositoryImpl();
        leases = new LeaseRepositoryImpl();
        today = LocalDate.now(CLOCK);
        useCase = new CreateLeaseUseCase(
                properties,
                leases,
                new PropertyOccupancyPolicy(),
                new LeaseTermPolicy(),
                new PropertyAvailability(properties, leases, new PropertyOccupancyPolicy(), CLOCK));
    }

    @Test
    void openingALeaseMarksThePropertyUnavailable() {
        properties.create(PropertyFactory.createProperty("P1", "1 Main Road", "Flat", 9000, true));

        var lease = useCase.execute(command("L1", "P1", "T1", today.minusDays(1), today.plusMonths(6), 9000));

        assertEquals("L1", lease.getLeaseId());
        assertEquals("T1", lease.getTenantId());
        assertEquals(today.minusDays(1), lease.getStartDate());
        assertEquals(today.plusMonths(6), lease.getEndDate());
        assertFalse(properties.read("P1").isAvailable());
        assertNotNull(leases.read("L1"));
    }

    @Test
    void anAlreadyEndedLeaseDoesNotOccupyTheProperty() {
        properties.create(PropertyFactory.createProperty("P1", "1 Main Road", "Flat", 9000, true));

        useCase.execute(command("L1", "P1", "T1", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31), 9000));

        assertTrue(properties.read("P1").isAvailable());
    }

    @Test
    void aStaleUnavailableFlagDoesNotBlockANonOverlappingLease() {
        properties.create(PropertyFactory.createProperty("P1", "1 Main Road", "Flat", 9000, false));
        leases.create(LeaseFactory.createLease(
                "L0", "P1", "T0", "Past Tenant", 9000, LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)));

        var lease = useCase.execute(command("L1", "P1", "T1", today, today.plusMonths(1), 9000));

        assertEquals("L1", lease.getLeaseId());
        assertNotNull(leases.read("L1"));
        assertFalse(properties.read("P1").isAvailable());
    }

    @Test
    void rejectsAnOverlappingLeaseWithoutSavingIt() {
        properties.create(PropertyFactory.createProperty("P1", "1 Main Road", "Flat", 9000, true));
        useCase.execute(command("L1", "P1", "T1", today.minusDays(5), today.plusDays(5), 9000));

        assertThrows(DomainRuleException.class,
                () -> useCase.execute(command("L2", "P1", "T2", today, today.plusMonths(1), 9000)));
        assertNull(leases.read("L2"));
        assertFalse(properties.read("P1").isAvailable());
    }

    @Test
    void rejectsALeaseForAMissingProperty() {
        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(command("L1", "missing", "T1", today, today.plusMonths(1), 9000)));
    }

    private static LeaseCommand command(String leaseId, String propertyId, String tenantId,
                                        LocalDate start, LocalDate end, double rent) {
        return new LeaseCommand(leaseId, propertyId, "Ada Lovelace", rent, tenantId, start, end);
    }
}
