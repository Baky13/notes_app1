package com.example.notesapp.controller;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.CreateColumnDto;
import com.example.notesapp.dto.UpdateColumnDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BoardColumnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long boardId;

    @BeforeEach
    void setUp() throws Exception {
        String username = "coluser" + System.currentTimeMillis();
        
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(username);
        registerReq.setEmail(username + "@test.com");
        registerReq.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response).get("token").asText();

        // Создаём доску
        CreateBoardDto boardDto = new CreateBoardDto();
        boardDto.setTitle("Test Board");
        
        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardDto)))
                .andExpect(status().isCreated())
                .andReturn();

        boardId = objectMapper.readTree(boardResult.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("createColumn_ReturnsCreated")
    void createColumn_ReturnsCreated() throws Exception {
        CreateColumnDto createDto = new CreateColumnDto();
        createDto.setTitle("New Column");

        mockMvc.perform(post("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Column"));
    }

    @Test
    @DisplayName("getColumns_ReturnsOk")
    void getColumns_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("updateColumn_ReturnsOk")
    void updateColumn_ReturnsOk() throws Exception {
        // Получаем ID существующей колонки
        MvcResult columnsResult = mockMvc.perform(get("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        Long columnId = objectMapper.readTree(columnsResult.getResponse().getContentAsString()).get(0).get("id").asLong();

        // Обновляем
        UpdateColumnDto updateDto = new UpdateColumnDto();
        updateDto.setTitle("Updated Column");

        mockMvc.perform(put("/api/boards/" + boardId + "/columns/" + columnId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Column"));
    }

    @Test
    @DisplayName("deleteColumn_ReturnsNoContent")
    void deleteColumn_ReturnsNoContent() throws Exception {
        // Создаём новую колонку для удаления
        CreateColumnDto createDto = new CreateColumnDto();
        createDto.setTitle("To Delete");

        MvcResult columnResult = mockMvc.perform(post("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long columnId = objectMapper.readTree(columnResult.getResponse().getContentAsString()).get("id").asLong();

        // Удаляем
        mockMvc.perform(delete("/api/boards/" + boardId + "/columns/" + columnId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }
}
