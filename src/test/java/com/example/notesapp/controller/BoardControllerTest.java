package com.example.notesapp.controller;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.UpdateBoardDto;
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
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private String username;

    @BeforeEach
    void setUp() throws Exception {
        username = "boarduser" + System.currentTimeMillis();
        
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
    }

    @Test
    @DisplayName("createBoard_ReturnsCreated")
    void createBoard_ReturnsCreated() throws Exception {
        CreateBoardDto createDto = new CreateBoardDto();
        createDto.setTitle("New Board");
        createDto.setDescription("New Description");

        mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Board"));
    }

    @Test
    @DisplayName("getBoards_ReturnsOk")
    void getBoards_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/boards")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("updateBoard_ReturnsOk")
    void updateBoard_ReturnsOk() throws Exception {
        CreateBoardDto createDto = new CreateBoardDto();
        createDto.setTitle("Original Board");

        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long boardId = objectMapper.readTree(boardResult.getResponse().getContentAsString()).get("id").asLong();

        UpdateBoardDto updateDto = new UpdateBoardDto();
        updateDto.setTitle("Updated Board");
        updateDto.setDescription("Updated Description");

        mockMvc.perform(put("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Board"));
    }

    @Test
    @DisplayName("deleteBoard_ReturnsNoContent")
    void deleteBoard_ReturnsNoContent() throws Exception {
        CreateBoardDto createDto = new CreateBoardDto();
        createDto.setTitle("To Delete");

        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long boardId = objectMapper.readTree(boardResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("createMultipleBoards_ReturnsOk")
    void createMultipleBoards_ReturnsOk() throws Exception {
        CreateBoardDto board1 = new CreateBoardDto();
        board1.setTitle("Board 1");
        mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(board1)))
                .andExpect(status().isCreated());

        CreateBoardDto board2 = new CreateBoardDto();
        board2.setTitle("Board 2");
        mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(board2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/boards")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
