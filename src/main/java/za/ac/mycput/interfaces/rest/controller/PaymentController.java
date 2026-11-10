package za.ac.mycput.interfaces.rest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import za.ac.mycput.application.service.PaymentApplicationService;
import za.ac.mycput.interfaces.rest.dto.PaymentRequest;
import za.ac.mycput.interfaces.rest.dto.PaymentResponse;
import za.ac.mycput.interfaces.rest.mapper.PaymentRestMapper;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentApplicationService payments;
    private final PaymentRestMapper mapper;

    public PaymentController(PaymentApplicationService payments, PaymentRestMapper mapper) {
        this.payments = payments;
        this.mapper = mapper;
    }

    @GetMapping
    public List<PaymentResponse> list() {
        return payments.list().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse get(@PathVariable String paymentId) {
        return mapper.toResponse(payments.get(paymentId));
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(payments.record(mapper.toCommand(request))));
    }

    @PutMapping("/{paymentId}")
    public PaymentResponse update(@PathVariable String paymentId, @Valid @RequestBody PaymentRequest request) {
        return mapper.toResponse(payments.update(paymentId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> delete(@PathVariable String paymentId) {
        payments.delete(paymentId);
        return ResponseEntity.noContent().build();
    }
}
