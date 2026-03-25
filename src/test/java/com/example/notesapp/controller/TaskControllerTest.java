package com.example.notesapp.controller;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.CreateTaskDto;
import com.example.notesapp.dto.MoveTaskDto;
import com.example.notesapp.dto.UpdateTaskDto;
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
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private Long boardId;
    private Long todoColumnId;
    private Long inProgressColumnId;

    @BeforeEach
    void setUp() throws Exception {
        String username = "taskuser" + System.currentTimeMillis();
        
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
        
        // Получаем ID колонок
        MvcResult columnsResult = mockMvc.perform(get("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();
        
        todoColumnId = objectMapper.readTree(columnsResult.getResponse().getContentAsString()).get(0).get("id").asLong();
        inProgressColumnId = objectMapper.readTree(columnsResult.getResponse().getContentAsString()).get(1).get("id").asLong();
    }

    @Test
    @DisplayName("createTask_ReturnsCreated")
    void createTask_ReturnsCreated() throws Exception {
        CreateTaskDto createDto = new CreateTaskDto();
        createDto.setTitle("New Task");
        createDto.setDescription("Task Description");

        mockMvc.perform(post("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    @DisplayName("getTasks_ReturnsOk")
    void getTasks_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("updateTask_ReturnsOk")
    void updateTask_ReturnsOk() throws Exception {
        // Создаём задачу
        CreateTaskDto createDto = new CreateTaskDto();
        createDto.setTitle("Original Task");

        MvcResult taskResult = mockMvc.perform(post("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        // Обновляем
        UpdateTaskDto updateDto = new UpdateTaskDto();
        updateDto.setTitle("Updated Task");
        updateDto.setDescription("Updated Description");

        mockMvc.perform(put("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    @DisplayName("deleteTask_ReturnsNoContent")
    void deleteTask_ReturnsNoContent() throws Exception {
        // Создаём задачу
        CreateTaskDto createDto = new CreateTaskDto();
        createDto.setTitle("To Delete");

        MvcResult taskResult = mockMvc.perform(post("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        // Удаляем
        mockMvc.perform(delete("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks/" + taskId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("moveTask_ReturnsOk")
    void moveTask_ReturnsOk() throws Exception {
        // Создаём задачу
        CreateTaskDto createDto = new CreateTaskDto();
        createDto.setTitle("Task to Move");

        MvcResult taskResult = mockMvc.perform(post("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        // Перемещаем
        MoveTaskDto moveDto = new MoveTaskDto();
        moveDto.setColumnId(inProgressColumnId);
        moveDto.setPosition(0);

        mockMvc.perform(patch("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks/" + taskId + "/move")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveDto)))
                .andExpect(status().isOk());
    }
}
