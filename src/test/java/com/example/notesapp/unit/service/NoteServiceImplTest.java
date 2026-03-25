package com.example.notesapp.unit.service;

import com.example.notesapp.dto.CreateNoteDto;
import com.example.notesapp.dto.NoteDto;
import com.example.notesapp.dto.UpdateNoteDto;
import com.example.notesapp.entity.Note;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.NoteNotFoundException;
import com.example.notesapp.exception.UserNotFoundException;
import com.example.notesapp.repository.NoteRepository;
import com.example.notesapp.repository.UserRepository;
import com.example.notesapp.service.NoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User testUser;
    private Note testNote;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test Note");
        testNote.setContent("Test Content");
        testNote.setUser(testUser);
        testNote.setCreatedAt(LocalDateTime.now());
        testNote.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createNote_Success - успешное создание заметки")
    void createNote_Success() {
        // Given
        CreateNoteDto createNoteDto = new CreateNoteDto();
        createNoteDto.setTitle("New Note");
        createNoteDto.setContent("New Content");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(1L);
            note.setCreatedAt(LocalDateTime.now());
            note.setUpdatedAt(LocalDateTime.now());
            return note;
        });

        // When
        NoteDto result = noteService.createNote(1L, createNoteDto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Note");
        assertThat(result.getContent()).isEqualTo("New Content");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("createNote_UserNotFound_ThrowsException - пользователь не найден")
    void createNote_UserNotFound_ThrowsException() {
        // Given
        CreateNoteDto createNoteDto = new CreateNoteDto();
        createNoteDto.setTitle("New Note");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noteService.createNote(999L, createNoteDto))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getNoteById_OwnerAccess_Success - владелец получает доступ")
    void getNoteById_OwnerAccess_Success() {
        // Given
        when(noteRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testNote));

        // When
        NoteDto result = noteService.getNoteById(1L, 1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Note");
    }

    @Test
    @DisplayName("getNoteById_NotOwner_ThrowsException - чужую заметку получить нельзя")
    void getNoteById_NotOwner_ThrowsException() {
        // Given
        when(noteRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noteService.getNoteById(2L, 1L))
                .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    @DisplayName("updateNote_Success - успешное обновление")
    void updateNote_Success() {
        // Given
        UpdateNoteDto updateNoteDto = new UpdateNoteDto();
        updateNoteDto.setTitle("Updated Title");
        updateNoteDto.setContent("Updated Content");

        when(noteRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        // When
        NoteDto result = noteService.updateNote(1L, 1L, updateNoteDto);

        // Then
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("deleteNote_Success - успешное удаление")
    void deleteNote_Success() {
        // Given
        when(noteRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testNote));

        // When
        noteService.deleteNote(1L, 1L);

        // Then
        verify(noteRepository).delete(testNote);
    }

    @Test
    @DisplayName("deleteNote_NotOwner_ThrowsException - нельзя удалить чужую заметку")
    void deleteNote_NotOwner_ThrowsException() {
        // Given
        when(noteRepository.findByIdAndUserId(1L, 2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> noteService.deleteNote(2L, 1L))
                .isInstanceOf(NoteNotFoundException.class);
    }

    @Test
    @DisplayName("searchNotes_ReturnsMatchingResults - поиск возвращает результаты")
    void searchNotes_ReturnsMatchingResults() {
        // Given
        List<Note> notes = Arrays.asList(testNote);
        Page<Note> notePage = new PageImpl<>(notes);
        Pageable pageable = PageRequest.of(0, 10);

        when(noteRepository.searchNotes(eq(1L), eq("test"), any(Pageable.class))).thenReturn(notePage);

        // When
        Page<NoteDto> result = noteService.searchNotes(1L, "test", pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Note");
    }

    @Test
    @DisplayName("getNotes_WithPagination - пагинация работает")
    void getNotes_WithPagination() {
        // Given
        List<Note> notes = Arrays.asList(testNote);
        Page<Note> notePage = new PageImpl<>(notes, PageRequest.of(0, 5), 1);
        Pageable pageable = PageRequest.of(0, 5);

        when(noteRepository.findByUserId(1L, pageable)).thenReturn(notePage);

        // When
        Page<NoteDto> result = noteService.getNotesByUserId(1L, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }
}
