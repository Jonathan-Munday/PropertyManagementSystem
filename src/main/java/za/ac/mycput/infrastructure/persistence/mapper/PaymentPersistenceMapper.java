package za.ac.mycput.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import za.ac.mycput.domain.model.Payment;
import za.ac.mycput.infrastructure.persistence.Identifiers;
import za.ac.mycput.infrastructure.persistence.entity.PaymentJpaEntity;

@Component
public class PaymentPersistenceMapper {

    public PaymentJpaEntity toEntity(Payment payment) {
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setPaymentId(Identifiers.parse(payment.getPaymentId()));
        entity.setLeaseId(Identifiers.parse(payment.getLeaseId()));
        entity.setAmount(MoneyColumns.fromDouble(payment.getAmount()));
        entity.setPaymentDate(payment.getPaymentDate());
        entity.setPaymentMethod(payment.getPaymentMethod());
        return entity;
    }

    public Payment toDomain(PaymentJpaEntity entity) {
        return new Payment.Builder()
                .setPaymentId(Identifiers.format(entity.getPaymentId()))
                .setLeaseId(Identifiers.format(entity.getLeaseId()))
                .setAmount(MoneyColumns.toDouble(entity.getAmount()))
                .setPaymentDate(entity.getPaymentDate())
                .setPaymentMethod(entity.getPaymentMethod())
                .build();
    }
}
