package za.ac.mycput.interfaces.rest.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.application.dto.PaymentCommand;
import za.ac.mycput.domain.model.Payment;
import za.ac.mycput.interfaces.rest.dto.PaymentRequest;
import za.ac.mycput.interfaces.rest.dto.PaymentResponse;

@Component
public class PaymentRestMapper {

    public PaymentCommand toCommand(PaymentRequest request) {
        return new PaymentCommand(
                request.paymentId(),
                request.leaseId(),
                request.amount(),
                request.paymentDate(),
                request.paymentMethod());
    }

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getLeaseId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getPaymentMethod());
    }
}
