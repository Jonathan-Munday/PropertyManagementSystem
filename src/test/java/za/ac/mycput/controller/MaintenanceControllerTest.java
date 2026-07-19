/*
 * MaintenanceControllerTest.java
 * TDD test class for MaintenanceController.
 * Author: Olwethu Mtwazi (230036937)
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
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/maintenance";

    @Test
    @Order(1)
    void testCreateMaintenance() throws Exception {
        String maintenanceJson = "{\"requestId\":\"M100\",\"description\":\"Leaking pipe in kitchen\",\"status\":\"Pending\",\"reportedDate\":\"2026-07-15\",\"cost\":1500.0}";
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maintenanceJson))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void testReadMaintenance() throws Exception {
        mockMvc.perform(get(BASE_URL + "/read/M100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void testUpdateMaintenance() throws Exception {
        String updatedJson = "{\"requestId\":\"M100\",\"description\":\"Leaking pipe in kitchen - Fixed\",\"status\":\"Completed\",\"reportedDate\":\"2026-07-15\",\"cost\":2000.0}";
        mockMvc.perform(post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testGetAllMaintenance() throws Exception {
        mockMvc.perform(get(BASE_URL + "/getall"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void testDeleteMaintenance() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/delete/M100"))
                .andExpect(status().isOk());
    }
}
