package com.example.notesapp.integration;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.example.notesapp.dto.CreateNoteDto;
import com.example.notesapp.dto.UpdateNoteDto;
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
class NoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    private void setupUser() throws Exception {
        String username = "noteuser" + System.currentTimeMillis();
        
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
    @DisplayName("fullNoteCrudFlow - полный CRUD для заметок")
    void fullNoteCrudFlow() throws Exception {
        setupUser();

        // 1. Создание
        CreateNoteDto createDto = new CreateNoteDto();
        createDto.setTitle("Test Note");
        createDto.setContent("Test Content");

        MvcResult createResult = mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andReturn();

        Long noteId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Чтение
        mockMvc.perform(get("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"));

        // 3. Обновление
        UpdateNoteDto updateDto = new UpdateNoteDto();
        updateDto.setTitle("Updated Note");
        updateDto.setContent("Updated Content");

        mockMvc.perform(put("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Note"));

        // 4. Удаление
        mockMvc.perform(delete("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("userCannotSeeOtherUsersNotes")
    void userCannotSeeOtherUsersNotes() throws Exception {
        // Пользователь 1 создаёт заметку
        setupUser();

        CreateNoteDto createDto = new CreateNoteDto();
        createDto.setTitle("Private Note");
        createDto.setContent("Private Content");

        mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        // Пользователь 2 не видит заметки пользователя 1
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

        // Пользователь 2 получает свои заметки (пустой список)
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @DisplayName("searchNotes_FindsByContent")
    void searchNotes_FindsByContent() throws Exception {
        setupUser();

        // Создаём заметку
        CreateNoteDto createDto = new CreateNoteDto();
        createDto.setTitle("Shopping List");
        createDto.setContent("Buy milk and eggs");

        mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        // Ищем
        mockMvc.perform(get("/api/notes/search")
                        .header("Authorization", "Bearer " + authToken)
                        .param("query", "milk"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("pagination_WorksCorrectly")
    void pagination_WorksCorrectly() throws Exception {
        setupUser();

        // Создаём несколько заметок
        for (int i = 0; i < 3; i++) {
            CreateNoteDto createDto = new CreateNoteDto();
            createDto.setTitle("Note " + i);
            createDto.setContent("Content " + i);

            mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated());
        }

        // Проверяем пагинацию
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}
