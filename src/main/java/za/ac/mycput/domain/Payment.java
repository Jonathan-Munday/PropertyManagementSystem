/*
 * Payment.java
 * Payment entity class
 * Author: Zia Modarrisi (230671942)
 * Date: 22 March 2026
 */
package za.ac.mycput.domain;

import java.time.LocalDate;

// Payment entity using Builder Pattern
public class Payment {
    private final String paymentId;
    private final String leaseId; // Linked to a specific Lease
    private final double amount;
    private final LocalDate paymentDate;
    private final String paymentMethod;

    private Payment(Builder builder) {
        this.paymentId     = builder.paymentId;
        this.leaseId       = builder.leaseId;
        this.amount        = builder.amount;
        this.paymentDate   = builder.paymentDate;
        this.paymentMethod = builder.paymentMethod;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", leaseId='" + leaseId + '\'' +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return paymentId != null && paymentId.equals(payment.paymentId);
    }

    @Override
    public int hashCode() {
        return paymentId != null ? paymentId.hashCode() : 0;
    }

    public static class Builder {
        private String paymentId;
        private String leaseId;
        private double amount;
        private LocalDate paymentDate;
        private String paymentMethod;

        public Builder setPaymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder setLeaseId(String leaseId) {
            this.leaseId = leaseId;
            return this;
        }

        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder setPaymentDate(LocalDate paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }

        public Builder setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
