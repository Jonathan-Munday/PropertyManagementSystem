package za.ac.mycput.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.mycput.application.dto.PaymentCommand;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.exception.EntityNotFoundException;
import za.ac.mycput.domain.factory.LeaseFactory;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPaymentRepository;
import za.ac.mycput.domain.service.RentCollectionPolicy;
import za.ac.mycput.infrastructure.persistence.memory.LeaseRepositoryImpl;
import za.ac.mycput.infrastructure.persistence.memory.PaymentRepositoryImpl;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RecordRentPaymentUseCaseTest {

    private ILeaseRepository leases;
    private IPaymentRepository payments;
    private RecordRentPaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        leases = new LeaseRepositoryImpl();
        payments = new PaymentRepositoryImpl();
        useCase = new RecordRentPaymentUseCase(leases, payments, new RentCollectionPolicy());
        leases.create(LeaseFactory.createLease("L1", "P1", "Ada Lovelace", 5000));
    }

    @Test
    void recordsARentPaymentAgainstAnExistingLease() {
        var payment = useCase.execute(new PaymentCommand("PAY1", "L1", 5000, LocalDate.of(2026, 11, 1), "EFT"));

        assertEquals("PAY1", payment.getPaymentId());
        assertEquals(5000, payments.read("PAY1").getAmount());
    }

    @Test
    void rejectsAPaymentForAMissingLease() {
        assertThrows(EntityNotFoundException.class,
                () -> useCase.execute(new PaymentCommand("PAY2", "missing", 100, LocalDate.of(2026, 11, 1), "EFT")));
        assertNull(payments.read("PAY2"));
    }

    @Test
    void rejectsANonPositiveRentAmount() {
        assertThrows(DomainRuleException.class,
                () -> useCase.execute(new PaymentCommand("PAY3", "L1", 0, LocalDate.of(2026, 11, 1), "EFT")));
    }
}
