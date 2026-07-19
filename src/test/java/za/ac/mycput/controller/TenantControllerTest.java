/*
 * TenantControllerTest.java
 * TDD test class for TenantController.
 * Author: Imaad Petersen (230166040)
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
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/tenant";

    @Test
    @Order(1)
    void testCreateTenant() throws Exception {
        String tenantJson = "{\"tenantId\":\"T100\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\",\"phoneNumber\":\"0211234567\"}";
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tenantJson))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void testReadTenant() throws Exception {
        mockMvc.perform(get(BASE_URL + "/read/T100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void testUpdateTenant() throws Exception {
        String updatedJson = "{\"tenantId\":\"T100\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"email\":\"jane@example.com\",\"phoneNumber\":\"0219876543\"}";
        mockMvc.perform(post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testGetAllTenants() throws Exception {
        mockMvc.perform(get(BASE_URL + "/getall"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void testDeleteTenant() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/delete/T100"))
                .andExpect(status().isOk());
    }
}
