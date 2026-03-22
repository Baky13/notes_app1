package com.example.notesapp.controller;

import com.example.notesapp.dto.CreateNoteDto;
import com.example.notesapp.dto.NoteDto;
import com.example.notesapp.dto.UpdateNoteDto;
import com.example.notesapp.service.NoteService;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.UserNotFoundException;
import com.example.notesapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new UserNotFoundException("Authenticated user not found");
        }
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return user.getId();
    }

    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody CreateNoteDto createNoteDto) {
        Long currentUserId = getCurrentUserId();
        NoteDto createdNote = noteService.createNote(currentUserId, createNoteDto);
        return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
    }

    @GetMapping
    public Page<NoteDto> getNotes(Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        return noteService.getNotesByUserId(currentUserId, pageable);
    }

    @GetMapping("/{noteId}")
    public NoteDto getNoteById(@PathVariable Long noteId) {
        Long currentUserId = getCurrentUserId();
        return noteService.getNoteById(currentUserId, noteId);
    }

    @PutMapping("/{noteId}")
    public NoteDto updateNote(@PathVariable Long noteId, @Valid @RequestBody UpdateNoteDto updateNoteDto) {
        Long currentUserId = getCurrentUserId();
        return noteService.updateNote(currentUserId, noteId, updateNoteDto);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long noteId) {
        Long currentUserId = getCurrentUserId();
        noteService.deleteNote(currentUserId, noteId);
    }

    @GetMapping("/search")
    public Page<NoteDto> searchNotes(@RequestParam String query, Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        return noteService.searchNotes(currentUserId, query, pageable);
    }
}
