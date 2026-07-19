/*
 * PropertyControllerTest.java
 * TDD test class for PropertyController.
 * Author: Jonathan Munday (230677797)
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
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/property";

    @Test
    @Order(1)
    void testCreateProperty() throws Exception {
        String propertyJson = "{\"propertyId\":\"P100\",\"address\":\"10 Main Rd, Cape Town\",\"description\":\"Spacious 2-bed flat\",\"rentalAmount\":8500.0,\"available\":true}";
        mockMvc.perform(post(BASE_URL + "/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(propertyJson))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void testReadProperty() throws Exception {
        mockMvc.perform(get(BASE_URL + "/read/P100"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void testUpdateProperty() throws Exception {
        String updatedJson = "{\"propertyId\":\"P100\",\"address\":\"15 Updated Rd, Cape Town\",\"description\":\"Updated flat\",\"rentalAmount\":9000.0,\"available\":true}";
        mockMvc.perform(post(BASE_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    void testGetAllProperties() throws Exception {
        mockMvc.perform(get(BASE_URL + "/getall"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(5)
    void testDeleteProperty() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/delete/P100"))
                .andExpect(status().isOk());
    }
}
