/*
 * IPaymentRepository.java
 * Payment repository interface
 * Author: Zia Modarrisi (230671942)
 * Date: 22 March 2026
 */
package za.ac.mycput.repository;

import za.ac.mycput.domain.Payment;
import java.util.List;

public interface IPaymentRepository extends IRepository<Payment, String> {
    // search by payment method
    List<Payment> findByPaymentMethod(String paymentMethod);
}
