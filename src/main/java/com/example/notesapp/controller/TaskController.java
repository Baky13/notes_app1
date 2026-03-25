package com.example.notesapp.controller;

import com.example.notesapp.dto.TaskDto;
import com.example.notesapp.dto.CreateTaskDto;
import com.example.notesapp.dto.UpdateTaskDto;
import com.example.notesapp.dto.MoveTaskDto;
import com.example.notesapp.service.TaskService;
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
@RequestMapping("/api/boards/{boardId}/columns/{columnId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
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
    public ResponseEntity<TaskDto> createTask(
        @PathVariable Long boardId,
        @PathVariable Long columnId,
        @Valid @RequestBody CreateTaskDto createTaskDto) {
        Long currentUserId = getCurrentUserId();
        TaskDto createdTask = taskService.createTask(currentUserId, boardId, columnId, createTaskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasksByColumn(
        @PathVariable Long boardId,
        @PathVariable Long columnId) {
        Long currentUserId = getCurrentUserId();
        List<TaskDto> tasks = taskService.getTasksByColumnId(currentUserId, boardId, columnId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> getTaskById(
        @PathVariable Long boardId,
        @PathVariable Long columnId,
        @PathVariable Long taskId) {
        Long currentUserId = getCurrentUserId();
        TaskDto task = taskService.getTaskById(currentUserId, boardId, columnId, taskId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
        @PathVariable Long boardId,
        @PathVariable Long columnId,
        @PathVariable Long taskId,
        @Valid @RequestBody UpdateTaskDto updateTaskDto) {
        Long currentUserId = getCurrentUserId();
        TaskDto updatedTask = taskService.updateTask(currentUserId, boardId, columnId, taskId, updateTaskDto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
        @PathVariable Long boardId,
        @PathVariable Long columnId,
        @PathVariable Long taskId) {
        Long currentUserId = getCurrentUserId();
        taskService.deleteTask(currentUserId, boardId, columnId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/move")
    public ResponseEntity<TaskDto> moveTask(
        @PathVariable Long boardId,
        @PathVariable Long columnId,
        @PathVariable Long taskId,
        @Valid @RequestBody MoveTaskDto moveTaskDto) {
        Long currentUserId = getCurrentUserId();
        TaskDto movedTask = taskService.moveTask(currentUserId, boardId, taskId, moveTaskDto);
        return ResponseEntity.ok(movedTask);
    }
}
