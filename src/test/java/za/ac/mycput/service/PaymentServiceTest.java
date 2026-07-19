package za.ac.mycput.service;

/*
 * PaymentServiceTest.java
 * Service test for Payment entity.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import za.ac.mycput.domain.Payment;
import za.ac.mycput.factory.PaymentFactory;
import za.ac.mycput.service.impl.PaymentService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentServiceTest {

    private static IPaymentService service = new PaymentService();
    private static Payment payment;

    @BeforeAll
    static void cleanUp() {
        service.delete("PAY001");
    }

    @BeforeEach
    void setUp() {
        if (payment == null) {
            payment = PaymentFactory.createPayment("PAY001", "LSE001", 1500.0, LocalDate.of(2026, 3, 22), "Credit Card");
        }
    }

    @Test
    @Order(1)
    void testCreate() {
        Payment created = service.create(payment);
        assertNotNull(created);
        assertEquals(payment.getPaymentId(), created.getPaymentId());
        System.out.println("Created Payment: " + created);
    }

    @Test
    @Order(2)
    void testRead() {
        Payment read = service.read(payment.getPaymentId());
        assertNotNull(read);
        assertEquals(payment.getPaymentId(), read.getPaymentId());
        System.out.println("Read Payment: " + read);
    }

    @Test
    @Order(3)
    void testUpdate() {
        Payment updatedPayment = new Payment.Builder()
                .setPaymentId(payment.getPaymentId())
                .setLeaseId(payment.getLeaseId())
                .setAmount(1800.0)
                .setPaymentDate(payment.getPaymentDate())
                .setPaymentMethod(payment.getPaymentMethod())
                .build();
        Payment updated = service.update(updatedPayment);
        assertNotNull(updated);
        assertEquals(1800.0, updated.getAmount());
        System.out.println("Updated Payment: " + updated);
    }

    @Test
    @Order(4)
    void testGetAll() {
        List<Payment> all = service.getAll();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        System.out.println("All Payments: " + all);
    }

    @Test
    @Order(5)
    void testDelete() {
        boolean success = service.delete(payment.getPaymentId());
        assertTrue(success);
        System.out.println("Deleted Payment successfully: " + success);
    }
}
