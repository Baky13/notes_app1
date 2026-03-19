package com.example.notesapp.controller;

import com.example.notesapp.dto.CreateNoteDto;
import com.example.notesapp.dto.NoteDto;
import com.example.notesapp.dto.UpdateNoteDto;
import com.example.notesapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // Временная заглушка для тестирования
    private static final Long CURRENT_USER_ID = 1L;

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody CreateNoteDto createNoteDto) {
        NoteDto createdNote = noteService.createNote(CURRENT_USER_ID, createNoteDto);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<NoteDto> getNotes(Pageable pageable) {
        return noteService.getNotesByUserId(CURRENT_USER_ID, pageable);
    }

    @GetMapping("/{noteId}")
    public NoteDto getNoteById(@PathVariable Long noteId) {
        return noteService.getNoteById(CURRENT_USER_ID, noteId);
    }

    @PutMapping("/{noteId}")
    public NoteDto updateNote(@PathVariable Long noteId, @Valid @RequestBody UpdateNoteDto updateNoteDto) {
        return noteService.updateNote(CURRENT_USER_ID, noteId, updateNoteDto);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long noteId) {
        noteService.deleteNote(CURRENT_USER_ID, noteId);
    }

    @GetMapping("/search")
    public Page<NoteDto> searchNotes(@RequestParam String query, Pageable pageable) {
        return noteService.searchNotes(CURRENT_USER_ID, query, pageable);
    }
}
