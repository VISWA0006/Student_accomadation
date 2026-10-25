package com.accommodation.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StudentRestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Integration Test: GET /api/students returns 200 OK and JSON list")
    void testGetAllStudentsApi() throws Exception {
        mockMvc.perform(get("/api/students")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Integration Test: POST /api/students with valid payload creates student")
    void testCreateStudentApi() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fullName", "Rohan Gupta");
        payload.put("registerNumber", "2024TEST" + System.currentTimeMillis());
        payload.put("email", "rohan.test@college.edu");
        payload.put("phoneNumber", "+91 9123456789");
        payload.put("department", "Computer Science");
        payload.put("yearOfStudy", 1);
        payload.put("gender", "MALE");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Rohan Gupta"))
                .andExpect(jsonPath("$.department").value("Computer Science"))
                .andExpect(jsonPath("$.gender").value("MALE"));
    }
}
