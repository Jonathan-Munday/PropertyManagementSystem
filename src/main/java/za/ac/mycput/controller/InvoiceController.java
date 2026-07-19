package za.ac.mycput.controller;

import za.ac.mycput.domain.Invoice;

public class InvoiceController {

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.*;
    import za.ac.mycput.domain.Invoice;
    import za.ac.mycput.service.IInvoiceService;

     import java.util.Set;

    @RestController
    @RequestMapping("/invoice")
    public class InvoiceController {

        private final IInvoiceService service;

        @Autowired
        public InvoiceController(IInvoiceService service) {
            this.service = service;
        }

        @PostMapping("/create")
        public Invoice create(@RequestBody Invoice invoice) {
            return service.create(invoice);
        }

        @GetMapping("/read/{invoiceId}")
        public Invoice read(@PathVariable String invoiceId) {
            return service.read(invoiceId);
        }

        @PutMapping("/update")
        public Invoice update(@RequestBody Invoice invoice) {
            return service.update(invoice);
        }

        @DeleteMapping("/delete/{invoiceId}")
        public boolean delete(@PathVariable String invoiceId) {
            return service.delete(invoiceId);
        }

        @GetMapping("/getAll")
        public Set<Invoice> getAll() {
            return service.getAll();
        }
    }
}
