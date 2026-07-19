/*
 * PaymentControllerTest.java
 * TDD test class for PaymentController.
 * Author: Zia Modarrisi (230671942)
 * Date: 19 July 2026
 */
package za.ac.mycput.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/payment";

    @Test
    @Order(1)
    void testCreatePayment() throws Exception {
        String paymentJson = "{\"paymentId\":\"PAY100\",\"leaseId\":\"L001\",\"amount\":8500.0,\"paymentDate\":\"2026-07-01\",\"paymentMethod\":\"EFT\"}";
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void testReadPayment() throws Exception {
        mockMvc.perform(get(BASE_URL + "/read/PAY100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void testUpdatePayment() throws Exception {
        String updatedJson = "{\"paymentId\":\"PAY100\",\"leaseId\":\"L001\",\"amount\":9000.0,\"paymentDate\":\"2026-07-01\",\"paymentMethod\":\"Card\"}";
        mockMvc.perform(post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testGetAllPayments() throws Exception {
        mockMvc.perform(get(BASE_URL + "/getall"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void testDeletePayment() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/delete/PAY100"))
                .andExpect(status().isOk());
    }
}
