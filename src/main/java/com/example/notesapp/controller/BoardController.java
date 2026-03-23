package com.example.notesapp.controller;

import com.example.notesapp.dto.BoardDto;
import com.example.notesapp.dto.BoardSummaryDto;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.UpdateBoardDto;
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
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

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
    public ResponseEntity<BoardDto> createBoard(@Valid @RequestBody CreateBoardDto createBoardDto) {
        Long currentUserId = getCurrentUserId();
        BoardDto createdBoard = boardService.createBoard(currentUserId, createBoardDto);
        return new ResponseEntity<>(createdBoard, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BoardSummaryDto>> getBoards() {
        Long currentUserId = getCurrentUserId();
        List<BoardSummaryDto> boards = boardService.getBoardsByUserId(currentUserId);
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> getBoardById(@PathVariable Long boardId) {
        Long currentUserId = getCurrentUserId();
        BoardDto board = boardService.getBoardById(currentUserId, boardId);
        return ResponseEntity.ok(board);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<BoardDto> updateBoard(
        @PathVariable Long boardId,
        @Valid @RequestBody UpdateBoardDto updateBoardDto) {
        Long currentUserId = getCurrentUserId();
        BoardDto updatedBoard = boardService.updateBoard(currentUserId, boardId, updateBoardDto);
        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId) {
        Long currentUserId = getCurrentUserId();
        boardService.deleteBoard(currentUserId, boardId);
        return ResponseEntity.noContent().build();
    }
}
