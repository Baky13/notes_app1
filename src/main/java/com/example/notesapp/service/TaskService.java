package com.example.notesapp.service;

import com.example.notesapp.dto.TaskDto;
import com.example.notesapp.dto.CreateTaskDto;
import com.example.notesapp.dto.UpdateTaskDto;
import com.example.notesapp.dto.MoveTaskDto;

import java.util.List;

public interface TaskService {
    TaskDto createTask(Long userId, Long boardId, Long columnId, CreateTaskDto createTaskDto);
    List<TaskDto> getTasksByColumnId(Long userId, Long boardId, Long columnId);
    TaskDto getTaskById(Long userId, Long boardId, Long columnId, Long taskId);
    TaskDto updateTask(Long userId, Long boardId, Long columnId, Long taskId, UpdateTaskDto updateTaskDto);
    void deleteTask(Long userId, Long boardId, Long columnId, Long taskId);
    TaskDto moveTask(Long userId, Long boardId, Long taskId, MoveTaskDto moveTaskDto);
}
