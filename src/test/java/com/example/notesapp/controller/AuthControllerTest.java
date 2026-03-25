package com.example.notesapp.controller;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("testuser" + System.currentTimeMillis());
        validRegisterRequest.setEmail(validRegisterRequest.getUsername() + "@test.com");
        validRegisterRequest.setPassword("password123");
    }

    @Test
    @DisplayName("register_ValidRequest_ReturnsOk")
    void register_ValidRequest_ReturnsOk() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(validRegisterRequest.getUsername()));
    }

    @Test
    @DisplayName("register_EmptyUsername_ReturnsBadRequest")
    void register_EmptyUsername_ReturnsBadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("");
        invalidRequest.setEmail("test@example.com");
        invalidRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("login_ValidCredentials_ReturnsOk")
    void login_ValidCredentials_ReturnsOk() throws Exception {
        // Сначала регистрируем
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        // Теперь логинимся
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(validRegisterRequest.getUsername());
        loginReq.setPassword(validRegisterRequest.getPassword());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("login_InvalidCredentials_ReturnsUnauthorized")
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("nonexistentuser");
        loginReq.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("register_DuplicateUsername_ReturnsBadRequest")
    void register_DuplicateUsername_ReturnsBadRequest() throws Exception {
        // Регистрируем первого
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isOk());

        // Пытаемся зарегистрировать второго с тем же username
        RegisterRequest duplicateReq = new RegisterRequest();
        duplicateReq.setUsername(validRegisterRequest.getUsername());
        duplicateReq.setEmail("different@test.com");
        duplicateReq.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(status().isBadRequest());
    }
}
