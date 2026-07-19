/*
 * AgentRoleControllerTest.java
 * TDD test class for AgentRoleController.
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
class AgentRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/agentrole";

    @Test
    @Order(1)
    void testCreateAgentRole() throws Exception {
        String agentRoleJson = "{\"agentId\":\"AG100\",\"agentName\":\"James Bond\",\"role\":\"Sales Agent\",\"commissionRate\":5.5}";
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(agentRoleJson))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void testReadAgentRole() throws Exception {
        mockMvc.perform(get(BASE_URL + "/read/AG100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void testUpdateAgentRole() throws Exception {
        String updatedJson = "{\"agentId\":\"AG100\",\"agentName\":\"James Bond\",\"role\":\"Senior Sales Agent\",\"commissionRate\":7.0}";
        mockMvc.perform(post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testGetAllAgentRoles() throws Exception {
        mockMvc.perform(get(BASE_URL + "/getall"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void testDeleteAgentRole() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/delete/AG100"))
                .andExpect(status().isOk());
    }
}
