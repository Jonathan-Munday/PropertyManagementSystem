package za.ac.mycput.controller;

import org.junit.jupiter.api.Test;

public class ManagerControllerTest {

    import org.junit.jupiter.api.Test;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
     import org.springframework.boot.test.mock.mockito.MockBean;
     import org.springframework.test.web.servlet.MockMvc;
     import za.ac.mycput.service.IInvoiceService;

    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

    @WebMvcTest(InvoiceController.class)
    class InvoiceControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private IInvoiceService invoiceService;

        @Test
        void testGetAll() throws Exception {
            mockMvc.perform(get("/invoice/getAll"))
                    .andExpect(status().isOk());
        }

        @Test
        void testRead() throws Exception {
            mockMvc.perform(get("/invoice/read/I001"))
                    .andExpect(status().isOk());
        }
    }
}
