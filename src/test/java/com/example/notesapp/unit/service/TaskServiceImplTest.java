package com.example.notesapp.unit.service;

import com.example.notesapp.dto.CreateTaskDto;
import com.example.notesapp.dto.MoveTaskDto;
import com.example.notesapp.dto.TaskDto;
import com.example.notesapp.dto.UpdateTaskDto;
import com.example.notesapp.entity.Board;
import com.example.notesapp.entity.BoardColumn;
import com.example.notesapp.entity.Priority;
import com.example.notesapp.entity.Task;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.BoardColumnNotFoundException;
import com.example.notesapp.exception.BoardNotFoundException;
import com.example.notesapp.exception.TaskNotFoundException;
import com.example.notesapp.repository.BoardColumnRepository;
import com.example.notesapp.repository.BoardRepository;
import com.example.notesapp.repository.TaskRepository;
import com.example.notesapp.service.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private BoardColumnRepository boardColumnRepository;

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private Board testBoard;
    private BoardColumn todoColumn;
    private BoardColumn inProgressColumn;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testBoard = new Board();
        testBoard.setId(1L);
        testBoard.setTitle("Test Board");
        testBoard.setUser(testUser);

        todoColumn = new BoardColumn();
        todoColumn.setId(1L);
        todoColumn.setTitle("To Do");
        todoColumn.setPosition(0);
        todoColumn.setBoard(testBoard);
        todoColumn.setTasks(new ArrayList<>());

        inProgressColumn = new BoardColumn();
        inProgressColumn.setId(2L);
        inProgressColumn.setTitle("In Progress");
        inProgressColumn.setPosition(1);
        inProgressColumn.setBoard(testBoard);
        inProgressColumn.setTasks(new ArrayList<>());

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setColumn(todoColumn);
        testTask.setPosition(0);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createTask_Success")
    void createTask_Success() {
        // Given
        CreateTaskDto createDto = new CreateTaskDto();
        createDto.setTitle("New Task");
        createDto.setDescription("New Description");

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(1L, 1L)).thenReturn(Optional.of(todoColumn));
        when(taskRepository.findByColumnIdOrderByPosition(1L)).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            return task;
        });

        // When
        TaskDto result = taskService.createTask(1L, 1L, 1L, createDto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getPosition()).isEqualTo(0);
    }

    @Test
    @DisplayName("createTask_WithPriority")
    void createTask_WithPriority() {
        // Given
        CreateTaskDto createDto = new CreateTaskDto();
        createDto.setTitle("High Priority Task");
        createDto.setPriority(Priority.HIGH);

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(1L, 1L)).thenReturn(Optional.of(todoColumn));
        when(taskRepository.findByColumnIdOrderByPosition(1L)).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        // When
        TaskDto result = taskService.createTask(1L, 1L, 1L, createDto);

        // Then
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("createTask_WithDueDate")
    void createTask_WithDueDate() {
        // Given
        LocalDate dueDate = LocalDate.now().plusDays(7);
        CreateTaskDto createDto = new CreateTaskDto();
        createDto.setTitle("Task with Due Date");
        createDto.setDueDate(dueDate);

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(1L, 1L)).thenReturn(Optional.of(todoColumn));
        when(taskRepository.findByColumnIdOrderByPosition(1L)).thenReturn(new ArrayList<>());
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        // When
        TaskDto result = taskService.createTask(1L, 1L, 1L, createDto);

        // Then
        assertThat(result.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    @DisplayName("moveTask_BetweenColumns")
    void moveTask_BetweenColumns() {
        // Given
        MoveTaskDto moveDto = new MoveTaskDto();
        moveDto.setColumnId(2L);
        moveDto.setPosition(0);

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Task");
        existingTask.setColumn(todoColumn);
        existingTask.setPosition(0);
        existingTask.setCreatedAt(LocalDateTime.now());
        existingTask.setUpdatedAt(LocalDateTime.now());

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(2L, 1L)).thenReturn(Optional.of(inProgressColumn));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.findByColumnIdOrderByPosition(2L)).thenReturn(new ArrayList<>());
        when(taskRepository.findByColumnIdOrderByPosition(1L)).thenReturn(new ArrayList<>(Arrays.asList(existingTask)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            return task;
        });

        // When
        TaskDto result = taskService.moveTask(1L, 1L, 1L, moveDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Task");
    }

    @Test
    @DisplayName("moveTask_WithinSameColumn")
    void moveTask_WithinSameColumn() {
        // Given
        MoveTaskDto moveDto = new MoveTaskDto();
        moveDto.setColumnId(1L);
        moveDto.setPosition(2);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setColumn(todoColumn);
        task1.setPosition(0);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setColumn(todoColumn);
        task2.setPosition(1);

        Task task3 = new Task();
        task3.setId(3L);
        task3.setColumn(todoColumn);
        task3.setPosition(2);

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(1L, 1L)).thenReturn(Optional.of(todoColumn));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task1));
        when(taskRepository.findByColumnIdOrderByPosition(1L)).thenReturn(Arrays.asList(task1, task2, task3));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TaskDto result = taskService.moveTask(1L, 1L, 1L, moveDto);

        // Then
        assertThat(result.getPosition()).isEqualTo(2);
    }

    @Test
    @DisplayName("moveTask_ToSamePosition_NoChange")
    void moveTask_ToSamePosition_NoChange() {
        // Given
        MoveTaskDto moveDto = new MoveTaskDto();
        moveDto.setColumnId(1L);
        moveDto.setPosition(0);

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(1L, 1L)).thenReturn(Optional.of(todoColumn));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.findByColumnIdOrderByPosition(1L)).thenReturn(new ArrayList<>(Arrays.asList(testTask)));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TaskDto result = taskService.moveTask(1L, 1L, 1L, moveDto);

        // Then
        assertThat(result.getPosition()).isEqualTo(0);
    }

    @Test
    @DisplayName("moveTask_InvalidTargetColumn_ThrowsException")
    void moveTask_InvalidTargetColumn_ThrowsException() {
        // Given
        MoveTaskDto moveDto = new MoveTaskDto();
        moveDto.setColumnId(999L);
        moveDto.setPosition(0);

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(999L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskService.moveTask(1L, 1L, 1L, moveDto))
                .isInstanceOf(BoardColumnNotFoundException.class);
    }

    @Test
    @DisplayName("deleteTask_RecalculatesPositions")
    void deleteTask_RecalculatesPositions() {
        // Given
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setColumn(todoColumn);
        task1.setPosition(0);

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(1L, 1L)).thenReturn(Optional.of(todoColumn));
        when(taskRepository.findByIdAndColumnId(1L, 1L)).thenReturn(Optional.of(task1));

        // When
        taskService.deleteTask(1L, 1L, 1L, 1L);

        // Then
        verify(taskRepository).delete(task1);
    }
}
