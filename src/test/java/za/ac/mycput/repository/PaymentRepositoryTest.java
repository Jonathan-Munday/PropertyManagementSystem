/*
 * PaymentRepositoryTest.java
 * Repository test for Payment entity
 * Author: Zia Modarrisi (230671942)
 * Date: 22 March 2026
 */
package za.ac.mycput.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Payment;
import za.ac.mycput.factory.PaymentFactory;
import za.ac.mycput.repository.impl.PaymentRepositoryImpl;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PaymentRepositoryTest {

    private IPaymentRepository repository;
    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        repository = new PaymentRepositoryImpl();
        payment1 = PaymentFactory.createPayment("PAY001", "LSE001", 1000.0, LocalDate.now(), "EFT");
        payment2 = PaymentFactory.createPayment("PAY002", "LSE002", 2000.0, LocalDate.now(), "Credit Card");
    }

    @Test
    void testCreate() {
        Payment created = repository.create(payment1);
        assertNotNull(created);
        assertEquals(payment1.getPaymentId(), created.getPaymentId());
        System.out.println("Created: " + created);
    }

    @Test
    void testRead() {
        repository.create(payment1);
        Payment read = repository.read(payment1.getPaymentId());
        assertNotNull(read);
        assertEquals(payment1.getPaymentId(), read.getPaymentId());
    }

    @Test
    void testUpdate() {
        repository.create(payment1);
        Payment updated = new Payment.Builder()
                .setPaymentId("PAY001")
                .setLeaseId("LSE001")
                .setAmount(1500.0)
                .setPaymentDate(LocalDate.now())
                .setPaymentMethod("Cash")
                .build();
        Payment result = repository.update(updated);
        assertNotNull(result);
        assertEquals(1500.0, repository.read("PAY001").getAmount());
    }

    @Test
    void testDelete() {
        repository.create(payment1);
        boolean deleted = repository.delete(payment1.getPaymentId());
        assertTrue(deleted);
        assertNull(repository.read(payment1.getPaymentId()));
    }

    @Test
    void testGetAll() {
        repository.create(payment1);
        repository.create(payment2);
        List<Payment> all = repository.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void testFindByMethod() {
        repository.create(payment1);
        repository.create(payment2);
        List<Payment> eftPayments = repository.findByPaymentMethod("EFT");
        assertEquals(1, eftPayments.size());
    }
}
