package com.example.notesapp.service;

import com.example.notesapp.dto.CreateNoteDto;
import com.example.notesapp.dto.NoteDto;
import com.example.notesapp.dto.UpdateNoteDto;
import com.example.notesapp.entity.Note;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.NoteNotFoundException;
import com.example.notesapp.exception.UserNotFoundException;
import com.example.notesapp.repository.NoteRepository;
import com.example.notesapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NoteDto createNote(Long userId, CreateNoteDto createNoteDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        Note note = new Note();
        note.setTitle(createNoteDto.getTitle());
        note.setContent(createNoteDto.getContent());
        note.setUser(user);
        Note savedNote = noteRepository.save(note);
        return toDto(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDto> getNotesByUserId(Long userId, Pageable pageable) {
        return noteRepository.findByUserId(userId, pageable).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDto getNoteById(Long userId, Long noteId) {
        return noteRepository.findByIdAndUserId(noteId, userId)
                .map(this::toDto)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));
    }

    @Override
    @Transactional
    public NoteDto updateNote(Long userId, Long noteId, UpdateNoteDto updateNoteDto) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));
        note.setTitle(updateNoteDto.getTitle());
        note.setContent(updateNoteDto.getContent());
        Note updatedNote = noteRepository.save(note);
        return toDto(updatedNote);
    }

    @Override
    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found with id: " + noteId));
        noteRepository.delete(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteDto> searchNotes(Long userId, String query, Pageable pageable) {
        return noteRepository.searchNotes(userId, query, pageable).map(this::toDto);
    }

    private NoteDto toDto(Note note) {
        return new NoteDto(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
