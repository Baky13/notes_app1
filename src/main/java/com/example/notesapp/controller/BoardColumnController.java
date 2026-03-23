package com.example.notesapp.controller;

import com.example.notesapp.dto.BoardColumnDto;
import com.example.notesapp.dto.CreateColumnDto;
import com.example.notesapp.dto.UpdateColumnDto;
import com.example.notesapp.dto.ReorderColumnsDto;
import com.example.notesapp.service.BoardService;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.UserNotFoundException;
import com.example.notesapp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards/{boardId}/columns")
@RequiredArgsConstructor
public class BoardColumnController {

    private final BoardService boardService;
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
    public ResponseEntity<BoardColumnDto> createColumn(
        @PathVariable Long boardId,
        @Valid @RequestBody CreateColumnDto createColumnDto) {
        Long currentUserId = getCurrentUserId();
        BoardColumnDto createdColumn = boardService.createColumn(currentUserId, boardId, createColumnDto);
        return new ResponseEntity<>(createdColumn, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BoardColumnDto>> getColumnsByBoard(
        @PathVariable Long boardId) {
        Long currentUserId = getCurrentUserId();
        List<BoardColumnDto> columns = boardService.getColumnsByBoardId(currentUserId, boardId);
        return ResponseEntity.ok(columns);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<BoardColumnDto> updateColumn(
        @PathVariable Long boardId,
        @PathVariable Long columnId,
        @Valid @RequestBody UpdateColumnDto updateColumnDto) {
        Long currentUserId = getCurrentUserId();
        BoardColumnDto updatedColumn = boardService.updateColumn(currentUserId, boardId, columnId, updateColumnDto);
        return ResponseEntity.ok(updatedColumn);
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<Void> deleteColumn(
        @PathVariable Long boardId,
        @PathVariable Long columnId) {
        Long currentUserId = getCurrentUserId();
        boardService.deleteColumn(currentUserId, boardId, columnId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<List<BoardColumnDto>> reorderColumns(
        @PathVariable Long boardId,
        @Valid @RequestBody ReorderColumnsDto reorderColumnsDto) {
        Long currentUserId = getCurrentUserId();
        List<BoardColumnDto> reorderedColumns = boardService.reorderColumns(currentUserId, boardId, reorderColumnsDto);
        return ResponseEntity.ok(reorderedColumns);
    }
}
