/*
 * PaymentFactoryTest.java
 * Factory test for Payment entity
 * Author: Zia Modarrisi (230671942)
 * Date: 22 March 2026
 */
package za.ac.mycput.factory;

import org.junit.jupiter.api.Test;
import za.ac.mycput.domain.Agent;
import za.ac.mycput.domain.Payment;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PaymentFactoryTest {

    @Test
    void testCreatePaymentSuccess() {
        Payment p = PaymentFactory.createPayment("PAY001", "LSE001", 1500.0, LocalDate.of(2026, 3, 22), "Credit Card");
        assertNotNull(p);
        System.out.println(p.toString());
    }

    @Test
    void testCreatePaymentFields() {
        LocalDate date = LocalDate.of(2026, 3, 22);
        Payment p = PaymentFactory.createPayment("PAY001", "LSE001", 1500.0, date, "EFT");
        assertNotNull(p);
        assertEquals("PAY001", p.getPaymentId());
        assertEquals("LSE001", p.getLeaseId());
        assertEquals(1500.0, p.getAmount());
        assertEquals(date, p.getPaymentDate());
        assertEquals("EFT", p.getPaymentMethod());
    }

    @Test
    void testCreatePaymentFailure() {
        // test with null ID
        Payment p = PaymentFactory.createPayment("", "LSE001", 1500.0, LocalDate.now(), "Cash");
        assertNull(p);
    }

    @Test
    void testCreateAgentSuccess() {
        Agent agent = PaymentFactory.createAgent("AGT01", "Alice Smith", "alice.smith@example.com", "Administrator");
        assertNotNull(agent);
        assertEquals("AGT01", agent.getAgentId());
        assertEquals("Alice Smith", agent.getName());
        assertEquals("alice.smith@example.com", agent.getEmail());
        assertEquals("Administrator", agent.getRole());
        System.out.println(agent.toString());
    }

    @Test
    void testCreateAgentFailure() {
        Agent agent = PaymentFactory.createAgent("", "Alice Smith", "alice.smith@example.com", "Administrator");
        assertNull(agent);
    }
}
