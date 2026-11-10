package za.ac.mycput.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.mycput.application.dto.PaymentCommand;
import za.ac.mycput.application.service.RepositoryOutcomes;
import za.ac.mycput.domain.factory.PaymentFactory;
import za.ac.mycput.domain.model.Payment;
import za.ac.mycput.domain.repository.ILeaseRepository;
import za.ac.mycput.domain.repository.IPaymentRepository;
import za.ac.mycput.domain.service.RentCollectionPolicy;

@Service
public class RecordRentPaymentUseCase {

    private final ILeaseRepository leases;
    private final IPaymentRepository payments;
    private final RentCollectionPolicy rentCollection;

    public RecordRentPaymentUseCase(ILeaseRepository leases,
                                     IPaymentRepository payments,
                                     RentCollectionPolicy rentCollection) {
        this.leases = leases;
        this.payments = payments;
        this.rentCollection = rentCollection;
    }

    @Transactional
    public Payment execute(PaymentCommand command) {
        rentCollection.assertCanRecord(command.leaseId(), command.amount(), command.paymentMethod());
        RepositoryOutcomes.existing(leases.read(command.leaseId()), "Lease", command.leaseId());
        Payment payment = RepositoryOutcomes.built(
                PaymentFactory.createPayment(
                        command.paymentId(),
                        command.leaseId(),
                        command.amount(),
                        command.paymentDate(),
                        command.paymentMethod()),
                "A rent payment needs an id, a lease, a positive amount, a date, and a method");
        RepositoryOutcomes.absent(payments.read(payment.getPaymentId()), "Payment", payment.getPaymentId());
        return RepositoryOutcomes.created(payments.create(payment), "Payment", payment.getPaymentId());
    }
}
