package com.example.notesapp.integration;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.CreateColumnDto;
import com.example.notesapp.dto.CreateTaskDto;
import com.example.notesapp.dto.MoveTaskDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class BoardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    private void setupUser() throws Exception {
        String username = "boarduser" + System.currentTimeMillis();
        
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
    @DisplayName("fullBoardFlow - полный цикл работы с доской")
    void fullBoardFlow() throws Exception {
        setupUser();

        // 1. Создание доски
        CreateBoardDto boardDto = new CreateBoardDto();
        boardDto.setTitle("Test Board");
        boardDto.setDescription("Test Description");

        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Board"))
                .andReturn();

        Long boardId = objectMapper.readTree(boardResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Получение колонок (должны быть To Do, In Progress, Done)
        mockMvc.perform(get("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 3. Создание колонки
        CreateColumnDto columnDto = new CreateColumnDto();
        columnDto.setTitle("Custom Column");

        mockMvc.perform(post("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(columnDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Custom Column"));

        // 4. Удаление доски
        mockMvc.perform(delete("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("createTask_AndMove_BetweenColumns")
    void createTask_AndMove_BetweenColumns() throws Exception {
        setupUser();

        // Создаём доску
        CreateBoardDto boardDto = new CreateBoardDto();
        boardDto.setTitle("Kanban Board");

        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long boardId = objectMapper.readTree(boardResult.getResponse().getContentAsString()).get("id").asLong();

        // Получаем колонки
        MvcResult columnsResult = mockMvc.perform(get("/api/boards/" + boardId + "/columns")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andReturn();

        Long todoColumnId = objectMapper.readTree(columnsResult.getResponse().getContentAsString()).get(0).get("id").asLong();
        Long inProgressColumnId = objectMapper.readTree(columnsResult.getResponse().getContentAsString()).get(1).get("id").asLong();

        // Создаём задачу
        CreateTaskDto taskDto = new CreateTaskDto();
        taskDto.setTitle("New Task");
        taskDto.setDescription("Task Description");

        MvcResult taskResult = mockMvc.perform(post("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = objectMapper.readTree(taskResult.getResponse().getContentAsString()).get("id").asLong();

        // Перемещаем задачу
        MoveTaskDto moveDto = new MoveTaskDto();
        moveDto.setColumnId(inProgressColumnId);
        moveDto.setPosition(0);

        mockMvc.perform(patch("/api/boards/" + boardId + "/columns/" + todoColumnId + "/tasks/" + taskId + "/move")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(moveDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("userCannotAccessOtherUsersBoard")
    void userCannotAccessOtherUsersBoard() throws Exception {
        // Пользователь 1 создаёт доску
        setupUser();

        CreateBoardDto boardDto = new CreateBoardDto();
        boardDto.setTitle("Private Board");

        mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardDto)))
                .andExpect(status().isCreated());

        // Пользователь 2
        String username2 = "otheruser" + System.currentTimeMillis();
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(username2);
        registerReq.setEmail(username2 + "@test.com");
        registerReq.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username2);
        loginReq.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String token2 = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Пользователь 2 видит пустой список досок
        mockMvc.perform(get("/api/boards")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("deleteBoard_CascadeDeletesEverything")
    void deleteBoard_CascadeDeletesEverything() throws Exception {
        setupUser();

        // Создаём доску
        CreateBoardDto boardDto = new CreateBoardDto();
        boardDto.setTitle("Board to Delete");

        MvcResult boardResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(boardDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long boardId = objectMapper.readTree(boardResult.getResponse().getContentAsString()).get("id").asLong();

        // Удаляем доску
        mockMvc.perform(delete("/api/boards/" + boardId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        // Проверяем что доска удалена
        mockMvc.perform(get("/api/boards")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
