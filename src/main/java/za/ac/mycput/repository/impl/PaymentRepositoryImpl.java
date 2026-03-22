/*
 * PaymentRepositoryImpl.java
 * Payment repository implementation
 * Author: Zia Modarrisi (230671942)
 * Date: 22 March 2026
 */
package za.ac.mycput.repository.impl;

import za.ac.mycput.domain.Payment;
import za.ac.mycput.repository.IPaymentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentRepositoryImpl implements IPaymentRepository {

    private final List<Payment> paymentList;

    public PaymentRepositoryImpl() {
        this.paymentList = new ArrayList<>();
    }

    @Override
    public Payment create(Payment payment) {
        if (read(payment.getPaymentId()) != null) {
            return null;
        }
        paymentList.add(payment);
        return payment;
    }

    @Override
    public Payment read(String id) {
        for (Payment p : paymentList) {
            if (p.getPaymentId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Payment update(Payment payment) {
        Payment oldPayment = read(payment.getPaymentId());
        if (oldPayment != null) {
            paymentList.remove(oldPayment);
            paymentList.add(payment);
            return payment;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        Payment paymentToDelete = read(id);
        if (paymentToDelete != null) {
            return paymentList.remove(paymentToDelete);
        }
        return false;
    }

    @Override
    public List<Payment> getAll() {
        return new ArrayList<>(paymentList);
    }

    @Override
    public List<Payment> findByPaymentMethod(String paymentMethod) {
        return paymentList.stream()
                .filter(p -> p.getPaymentMethod().equalsIgnoreCase(paymentMethod))
                .collect(Collectors.toList());
    }
}
