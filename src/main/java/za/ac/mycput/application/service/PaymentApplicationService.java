package za.ac.mycput.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.PaymentCommand;
import za.ac.mycput.application.usecase.RecordRentPaymentUseCase;
import za.ac.mycput.domain.exception.DomainRuleException;
import za.ac.mycput.domain.factory.PaymentFactory;
import za.ac.mycput.domain.model.Payment;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPaymentRepository;
import za.ac.mycput.domain.service.RentCollectionPolicy;

import java.util.List;

@Service
public class PaymentApplicationService {

    private final IPaymentRepository payments;
    private final ILeaseRepository leases;
    private final RecordRentPaymentUseCase recordRentPayment;
    private final RentCollectionPolicy rentCollection;

    public PaymentApplicationService(IPaymentRepository payments,
                                     ILeaseRepository leases,
                                     RecordRentPaymentUseCase recordRentPayment,
                                     RentCollectionPolicy rentCollection) {
        this.payments = payments;
        this.leases = leases;
        this.recordRentPayment = recordRentPayment;
        this.rentCollection = rentCollection;
    }

    @Transactional
    public List<Payment> list() {
        return payments.getAll();
    }

    @Transactional
    public Payment get(String paymentId) {
        return RepositoryOutcomes.existing(payments.read(paymentId), "Payment", paymentId);
    }

    public Payment record(PaymentCommand command) {
        return recordRentPayment.execute(command);
    }

    @Transactional
    public Payment update(String paymentId, PaymentCommand command) {
        if (!paymentId.equals(command.paymentId())) {
            throw new DomainRuleException("The payment id in the path and body must match");
        }
        RepositoryOutcomes.existing(payments.read(paymentId), "Payment", paymentId);
        rentCollection.assertCanRecord(command.leaseId(), command.amount(), command.paymentMethod());
        RepositoryOutcomes.existing(leases.read(command.leaseId()), "Lease", command.leaseId());
        Payment payment = RepositoryOutcomes.built(
                PaymentFactory.createPayment(
                        paymentId, command.leaseId(), command.amount(), command.paymentDate(), command.paymentMethod()),
                "A rent payment needs an id, a lease, a positive amount, a date, and a method");
        return RepositoryOutcomes.updated(payments.update(payment), "Payment", paymentId);
    }

    @Transactional
    public void delete(String paymentId) {
        RepositoryOutcomes.deleted(payments.delete(paymentId), "Payment", paymentId);
    }
}
