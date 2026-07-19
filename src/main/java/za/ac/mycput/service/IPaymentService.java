/*
 * IPaymentService.java
 * Service interface for Payment entity.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */
package za.ac.mycput.service;

import za.ac.mycput.domain.Payment;
import java.util.List;

public interface IPaymentService extends IService<Payment, String> {
    List<Payment> getAll();
}
