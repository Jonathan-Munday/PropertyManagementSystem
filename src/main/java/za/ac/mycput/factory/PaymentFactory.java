/*
 * PaymentFactory.java
 * Factory for Payment entity
 * Author: Zia Modarrisi (230671942)
 * Date: 22 March 2026
 */
package za.ac.mycput.factory;

import za.ac.mycput.domain.Agent;
import za.ac.mycput.domain.Payment;
import java.time.LocalDate;

public class PaymentFactory {

    // helper method to create a payment
    public static Payment createPayment(String paymentId, String leaseId, double amount, LocalDate paymentDate, String paymentMethod) {
        if (paymentId == null || paymentId.isEmpty() || leaseId == null || leaseId.isEmpty() || amount <= 0 || paymentDate == null || paymentMethod == null || paymentMethod.isEmpty()) {
            return null;
        }

        return new Payment.Builder()
                .setPaymentId(paymentId)
                .setLeaseId(leaseId)
                .setAmount(amount)
                .setPaymentDate(paymentDate)
                .setPaymentMethod(paymentMethod)
                .build();
    }

    // helper method to create an agent
    public static Agent createAgent(String agentId, String name, String email, String role) {
        if (agentId == null || agentId.isEmpty() || name == null || name.isEmpty() || email == null || email.isEmpty() || role == null || role.isEmpty()) {
            return null;
        }
        return new Agent.Builder()
                .setAgentId(agentId)
                .setName(name)
                .setEmail(email)
                .setRole(role)
                .build();
    }
}
