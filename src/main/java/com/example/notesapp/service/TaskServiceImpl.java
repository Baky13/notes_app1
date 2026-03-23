package com.example.notesapp.service;

import com.example.notesapp.dto.TaskDto;
import com.example.notesapp.dto.CreateTaskDto;
import com.example.notesapp.dto.UpdateTaskDto;
import com.example.notesapp.dto.MoveTaskDto;
import com.example.notesapp.entity.Task;
import com.example.notesapp.entity.BoardColumn;
import com.example.notesapp.exception.TaskNotFoundException;
import com.example.notesapp.exception.BoardColumnNotFoundException;
import com.example.notesapp.exception.BoardNotFoundException;
import com.example.notesapp.repository.TaskRepository;
import com.example.notesapp.repository.BoardColumnRepository;
import com.example.notesapp.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public TaskDto createTask(Long userId, Long boardId, Long columnId, CreateTaskDto createTaskDto) {
        // Verify board belongs to user and column belongs to board
        var board = boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        BoardColumn column = boardColumnRepository.findByIdAndBoardId(columnId, boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        // Get the next position
        List<Task> tasksInColumn = taskRepository.findByColumnIdOrderByPosition(columnId);
        Integer nextPosition = tasksInColumn.isEmpty() ? 0 : tasksInColumn.get(tasksInColumn.size() - 1).getPosition() + 1;

        Task task = new Task();
        task.setColumn(column);
        task.setTitle(createTaskDto.getTitle());
        task.setDescription(createTaskDto.getDescription());
        task.setPriority(createTaskDto.getPriority());
        task.setPosition(nextPosition);

        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByColumnId(Long userId, Long boardId, Long columnId) {
        // Verify board belongs to user and column belongs to board
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        boardColumnRepository.findByIdAndBoardId(columnId, boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        return taskRepository.findByColumnIdOrderByPosition(columnId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long userId, Long boardId, Long columnId, Long taskId) {
        // Verify board belongs to user and column belongs to board
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        boardColumnRepository.findByIdAndBoardId(columnId, boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        Task task = taskRepository.findByIdAndColumnId(taskId, columnId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        return convertToDto(task);
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long userId, Long boardId, Long columnId, Long taskId, UpdateTaskDto updateTaskDto) {
        // Verify board belongs to user and column belongs to board
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        boardColumnRepository.findByIdAndBoardId(columnId, boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        Task task = taskRepository.findByIdAndColumnId(taskId, columnId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (updateTaskDto.getTitle() != null) {
            task.setTitle(updateTaskDto.getTitle());
        }
        if (updateTaskDto.getDescription() != null) {
            task.setDescription(updateTaskDto.getDescription());
        }
        if (updateTaskDto.getPriority() != null) {
            task.setPriority(updateTaskDto.getPriority());
        }

        Task updatedTask = taskRepository.save(task);
        return convertToDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long boardId, Long columnId, Long taskId) {
        // Verify board belongs to user and column belongs to board
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        boardColumnRepository.findByIdAndBoardId(columnId, boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        Task task = taskRepository.findByIdAndColumnId(taskId, columnId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public TaskDto moveTask(Long userId, Long boardId, Long taskId, MoveTaskDto moveTaskDto) {
        // Verify board belongs to user
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        // Verify target column belongs to board
        BoardColumn targetColumn = boardColumnRepository.findByIdAndBoardId(moveTaskDto.getColumnId(), boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        // Find task in any column of this board
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        // Verify task belongs to this board (by checking its column)
        if (!task.getColumn().getBoard().getId().equals(boardId)) {
            throw new TaskNotFoundException("Task not found in this board");
        }

        // If moving to a different column, update old column's positions
        BoardColumn oldColumn = task.getColumn();
        if (!oldColumn.getId().equals(moveTaskDto.getColumnId())) {
            List<Task> oldColumnTasks = taskRepository.findByColumnIdOrderByPosition(oldColumn.getId());
            int removedPosition = task.getPosition();
            for (Task t : oldColumnTasks) {
                if (t.getPosition() > removedPosition) {
                    t.setPosition(t.getPosition() - 1);
                    taskRepository.save(t);
                }
            }

            task.setColumn(targetColumn);
        }

        // Update position in new/current column
        List<Task> targetColumnTasks = taskRepository.findByColumnIdOrderByPosition(moveTaskDto.getColumnId());
        int newPosition = moveTaskDto.getPosition();

        // Ensure new position is within bounds
        if (newPosition < 0) newPosition = 0;
        if (newPosition > targetColumnTasks.size()) newPosition = targetColumnTasks.size();

        // Shift tasks in target column
        for (Task t : targetColumnTasks) {
            if (!t.getId().equals(taskId)) {
                if (t.getPosition() >= newPosition) {
                    t.setPosition(t.getPosition() + 1);
                    taskRepository.save(t);
                }
            }
        }

        task.setPosition(newPosition);
        Task movedTask = taskRepository.save(task);
        return convertToDto(movedTask);
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setPosition(task.getPosition());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }
}
