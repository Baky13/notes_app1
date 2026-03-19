package com.example.notesapp.service;

import com.example.notesapp.dto.CreateNoteDto;
import com.example.notesapp.dto.NoteDto;
import com.example.notesapp.dto.UpdateNoteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteService {
    NoteDto createNote(Long userId, CreateNoteDto createNoteDto);
    Page<NoteDto> getNotesByUserId(Long userId, Pageable pageable);
    NoteDto getNoteById(Long userId, Long noteId);
    NoteDto updateNote(Long userId, Long noteId, UpdateNoteDto updateNoteDto);
    void deleteNote(Long userId, Long noteId);
    Page<NoteDto> searchNotes(Long userId, String query, Pageable pageable);
}
