/*
 * LeaseControllerTest.java
 * TDD test class for LeaseController.
 * Author: Malik Muhammed (230388175)
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
class LeaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/lease";

    @Test
    @Order(1)
    void testCreateLease() throws Exception {
        String leaseJson = "{\"leaseId\":\"L100\",\"propertyId\":\"P001\",\"tenantName\":\"John Doe\",\"monthlyRent\":8500.0}";
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(leaseJson))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void testReadLease() throws Exception {
        mockMvc.perform(get(BASE_URL + "/read/L100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void testUpdateLease() throws Exception {
        String updatedJson = "{\"leaseId\":\"L100\",\"propertyId\":\"P001\",\"tenantName\":\"Jane Doe\",\"monthlyRent\":9500.0}";
        mockMvc.perform(post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testGetAllLeases() throws Exception {
        mockMvc.perform(get(BASE_URL + "/getall"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void testDeleteLease() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/delete/L100"))
                .andExpect(status().isOk());
    }
}
