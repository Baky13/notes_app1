package com.example.notesapp.controller;

import com.example.notesapp.dto.auth.LoginRequest;
import com.example.notesapp.dto.auth.RegisterRequest;
import com.example.notesapp.dto.CreateNoteDto;
import com.example.notesapp.dto.UpdateNoteDto;
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
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;
    private String username;

    @BeforeEach
    void setUp() throws Exception {
        username = "noteuser" + System.currentTimeMillis();
        
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
    @DisplayName("createNote_ReturnsCreated")
    void createNote_ReturnsCreated() throws Exception {
        CreateNoteDto createDto = new CreateNoteDto();
        createDto.setTitle("New Note");
        createDto.setContent("New Content");

        mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Note"));
    }

    @Test
    @DisplayName("getNotes_ReturnsOkWithPagination")
    void getNotes_ReturnsOkWithPagination() throws Exception {
        mockMvc.perform(get("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("updateNote_ReturnsOk")
    void updateNote_ReturnsOk() throws Exception {
        CreateNoteDto createDto = new CreateNoteDto();
        createDto.setTitle("Original Title");
        createDto.setContent("Original Content");

        MvcResult createResult = mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long noteId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        UpdateNoteDto updateDto = new UpdateNoteDto();
        updateDto.setTitle("Updated Title");
        updateDto.setContent("Updated Content");

        mockMvc.perform(put("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("deleteNote_ReturnsNoContent")
    void deleteNote_ReturnsNoContent() throws Exception {
        CreateNoteDto createDto = new CreateNoteDto();
        createDto.setTitle("To Delete");
        createDto.setContent("Content");

        MvcResult createResult = mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long noteId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("getNoteById_ReturnsOk")
    void getNoteById_ReturnsOk() throws Exception {
        CreateNoteDto createDto = new CreateNoteDto();
        createDto.setTitle("Test Note");
        createDto.setContent("Test Content");

        MvcResult createResult = mockMvc.perform(post("/api/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long noteId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/notes/" + noteId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"));
    }
}
