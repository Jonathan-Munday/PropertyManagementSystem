/*
 * PaymentService.java
 * Service implementation for Payment entity.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */
package za.ac.mycput.service.impl;

import org.springframework.stereotype.Service;
import za.ac.mycput.domain.Payment;
import za.ac.mycput.repository.impl.PaymentRepositoryImpl;
import za.ac.mycput.service.IPaymentService;

import java.util.List;

@Service
public class PaymentService implements IPaymentService {

    private final PaymentRepositoryImpl repository;

    public PaymentService() {
        this.repository = new PaymentRepositoryImpl();
    }

    @Override
    public Payment create(Payment payment) {
        return repository.create(payment);
    }

    @Override
    public Payment read(String paymentId) {
        return repository.read(paymentId);
    }

    @Override
    public Payment update(Payment payment) {
        return repository.update(payment);
    }

    @Override
    public boolean delete(String paymentId) {
        return repository.delete(paymentId);
    }

    @Override
    public List<Payment> getAll() {
        return repository.getAll();
    }
}
