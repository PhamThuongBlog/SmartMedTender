package com.medbid.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medbid.auth.dto.LoginRequest;
import com.medbid.auth.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldLoginAndReturnTokens() throws Exception {
        LoginRequest request = new LoginRequest("admin", "12345678@Abc");

        String responseJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LoginResponse response = objectMapper.readValue(responseJson, LoginResponse.class);

        // Test authenticated endpoint with token
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + response.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/tenders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/tenders")
                        .header("Authorization", "Bearer invalid-token-here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAccessHealthWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
