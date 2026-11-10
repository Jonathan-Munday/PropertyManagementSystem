package za.ac.mycput.infrastructure.persistence.mapper;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.factory.LeaseFactory;
import za.ac.mycput.domain.model.Lease;
import za.ac.mycput.infrastructure.persistence.entity.LeaseJpaEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LeasePersistenceMapperTest {

    private final LeasePersistenceMapper mapper = new LeasePersistenceMapper();

    @Test
    void tenantPropertyAndTermSurviveAMappingRoundTrip() {
        UUID leaseId = UUID.fromString("c3000000-0000-4000-8000-000000000001");
        UUID propertyId = UUID.fromString("c1000000-0000-4000-8000-000000000001");
        UUID tenantId = UUID.fromString("c2000000-0000-4000-8000-000000000001");
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2027, 12, 31);

        Lease lease = LeaseFactory.createLease(
                leaseId.toString(),
                propertyId.toString(),
                tenantId.toString(),
                "Ada Lovelace",
                9500,
                start,
                end);

        LeaseJpaEntity stored = mapper.toEntity(lease);
        Lease reloaded = mapper.toDomain(stored);

        assertEquals(leaseId, stored.getLeaseId());
        assertEquals(propertyId, stored.getPropertyId());
        assertEquals(tenantId, stored.getTenantId());
        assertEquals(start, stored.getStartDate());
        assertEquals(end, stored.getEndDate());
        assertEquals(tenantId.toString(), reloaded.getTenantId());
        assertEquals(propertyId.toString(), reloaded.getPropertyId());
        assertEquals(start, reloaded.getStartDate());
        assertEquals(end, reloaded.getEndDate());
        assertEquals(9500, reloaded.getMonthlyRent());
    }

    @Test
    void aLegacyRowWithoutDatesReloadsWithoutInventedDates() {
        LeaseJpaEntity stored = new LeaseJpaEntity();
        stored.setLeaseId(UUID.fromString("c3000000-0000-4000-8000-000000000099"));
        stored.setPropertyId(UUID.fromString("c1000000-0000-4000-8000-000000000099"));
        stored.setTenantId(UUID.fromString("c2000000-0000-4000-8000-000000000099"));
        stored.setTenantName("Legacy Tenant");
        stored.setMonthlyRent(MoneyColumns.fromDouble(1000));

        Lease reloaded = mapper.toDomain(stored);

        assertEquals("c2000000-0000-4000-8000-000000000099", reloaded.getTenantId());
        assertNull(reloaded.getStartDate());
        assertNull(reloaded.getEndDate());
    }
}
