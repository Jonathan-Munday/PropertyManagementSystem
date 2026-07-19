/*
 * ManagerControllerTest.java
 * TDD test class for ManagerController.
 * Author: Anas Pansari (230195342)
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
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/manager";

    @Test
    @Order(1)
    void testCreateManager() throws Exception {
        String managerJson = "{\"managerId\":\"MG100\",\"name\":\"Alice Smith\",\"contactNumber\":\"0211234567\",\"employeeId\":\"EMP001\"}";
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerJson))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void testReadManager() throws Exception {
        mockMvc.perform(get(BASE_URL + "/read/MG100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void testUpdateManager() throws Exception {
        String updatedJson = "{\"managerId\":\"MG100\",\"name\":\"Alice Johnson\",\"contactNumber\":\"0219876543\",\"employeeId\":\"EMP001\"}";
        mockMvc.perform(post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testGetAllManagers() throws Exception {
        mockMvc.perform(get(BASE_URL + "/getall"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void testDeleteManager() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/delete/MG100"))
                .andExpect(status().isOk());
    }
}
