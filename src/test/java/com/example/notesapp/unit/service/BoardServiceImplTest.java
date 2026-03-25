package com.example.notesapp.unit.service;

import com.example.notesapp.dto.BoardColumnDto;
import com.example.notesapp.dto.BoardDto;
import com.example.notesapp.dto.BoardSummaryDto;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.CreateColumnDto;
import com.example.notesapp.dto.ReorderColumnsDto;
import com.example.notesapp.dto.UpdateBoardDto;
import com.example.notesapp.entity.Board;
import com.example.notesapp.entity.BoardColumn;
import com.example.notesapp.entity.Task;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.BoardNotFoundException;
import com.example.notesapp.exception.BoardColumnNotFoundException;
import com.example.notesapp.repository.BoardRepository;
import com.example.notesapp.repository.BoardColumnRepository;
import com.example.notesapp.repository.TaskRepository;
import com.example.notesapp.repository.UserRepository;
import com.example.notesapp.service.BoardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardColumnRepository boardColumnRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    private User testUser;
    private Board testBoard;
    private BoardColumn testColumn1;
    private BoardColumn testColumn2;
    private BoardColumn testColumn3;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testBoard = new Board();
        testBoard.setId(1L);
        testBoard.setTitle("Test Board");
        testBoard.setDescription("Test Description");
        testBoard.setUser(testUser);
        testBoard.setColumns(new ArrayList<>());

        testColumn1 = new BoardColumn();
        testColumn1.setId(1L);
        testColumn1.setTitle("To Do");
        testColumn1.setPosition(0);
        testColumn1.setBoard(testBoard);
        testColumn1.setTasks(new ArrayList<>());

        testColumn2 = new BoardColumn();
        testColumn2.setId(2L);
        testColumn2.setTitle("In Progress");
        testColumn2.setPosition(1);
        testColumn2.setBoard(testBoard);
        testColumn2.setTasks(new ArrayList<>());

        testColumn3 = new BoardColumn();
        testColumn3.setId(3L);
        testColumn3.setTitle("Done");
        testColumn3.setPosition(2);
        testColumn3.setBoard(testBoard);
        testColumn3.setTasks(new ArrayList<>());

        testBoard.getColumns().addAll(Arrays.asList(testColumn1, testColumn2, testColumn3));
    }

    @Test
    @DisplayName("createBoard_CreatesWithThreeDefaultColumns")
    void createBoard_CreatesWithThreeDefaultColumns() {
        // Given
        CreateBoardDto createDto = new CreateBoardDto();
        createDto.setTitle("New Board");
        createDto.setDescription("New Description");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
            Board board = invocation.getArgument(0);
            board.setId(1L);
            board.setCreatedAt(LocalDateTime.now());
            board.setUpdatedAt(LocalDateTime.now());
            return board;
        });
        when(boardColumnRepository.save(any(BoardColumn.class))).thenAnswer(invocation -> {
            BoardColumn column = invocation.getArgument(0);
            column.setId((long) (testBoard.getColumns().size() + 1));
            column.setCreatedAt(LocalDateTime.now());
            return column;
        });

        // When
        BoardDto result = boardService.createBoard(1L, createDto);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getColumns()).hasSize(3);
        assertThat(result.getColumns().get(0).getTitle()).isEqualTo("To Do");
        assertThat(result.getColumns().get(1).getTitle()).isEqualTo("In Progress");
        assertThat(result.getColumns().get(2).getTitle()).isEqualTo("Done");
    }

    @Test
    @DisplayName("getBoardById_ReturnsWithColumnsAndTasks")
    void getBoardById_ReturnsWithColumnsAndTasks() {
        // Given
        when(boardRepository.findByIdAndUserIdWithColumnsAndTasks(1L, 1L)).thenReturn(Optional.of(testBoard));

        // When
        BoardDto result = boardService.getBoardById(1L, 1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Board");
        assertThat(result.getColumns()).hasSize(3);
    }

    @Test
    @DisplayName("getBoardById_NotOwner_ThrowsException")
    void getBoardById_NotOwner_ThrowsException() {
        // Given
        when(boardRepository.findByIdAndUserIdWithColumnsAndTasks(1L, 2L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> boardService.getBoardById(2L, 1L))
                .isInstanceOf(BoardNotFoundException.class);
    }

    @Test
    @DisplayName("getBoardsByUserId_ReturnsOnlyOwnBoards")
    void getBoardsByUserId_ReturnsOnlyOwnBoards() {
        // Given
        List<Board> boards = Arrays.asList(testBoard);
        when(boardRepository.findByUserId(1L)).thenReturn(boards);

        // When
        List<BoardSummaryDto> result = boardService.getBoardsByUserId(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Board");
    }

    @Test
    @DisplayName("deleteBoard_CascadeDeletesColumnsAndTasks")
    void deleteBoard_CascadeDeletesColumnsAndTasks() {
        // Given
        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));

        // When
        boardService.deleteBoard(1L, 1L);

        // Then
        verify(boardRepository).delete(testBoard);
    }

    @Test
    @DisplayName("createColumn_AddsToEnd")
    void createColumn_AddsToEnd() {
        // Given
        CreateColumnDto createDto = new CreateColumnDto();
        createDto.setTitle("New Column");

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByBoardIdOrderByPosition(1L)).thenReturn(Arrays.asList(testColumn1, testColumn2, testColumn3));
        when(boardColumnRepository.save(any(BoardColumn.class))).thenAnswer(invocation -> {
            BoardColumn column = invocation.getArgument(0);
            column.setId(4L);
            column.setCreatedAt(LocalDateTime.now());
            return column;
        });

        // When
        BoardColumnDto result = boardService.createColumn(1L, 1L, createDto);

        // Then
        assertThat(result.getPosition()).isEqualTo(3);
        assertThat(result.getTitle()).isEqualTo("New Column");
    }

    @Test
    @DisplayName("reorderColumns_UpdatesPositions")
    void reorderColumns_UpdatesPositions() {
        // Given
        ReorderColumnsDto reorderDto = new ReorderColumnsDto();
        reorderDto.setColumns(Arrays.asList(
                new ReorderColumnsDto.ColumnOrder(3L, 0),
                new ReorderColumnsDto.ColumnOrder(2L, 1),
                new ReorderColumnsDto.ColumnOrder(1L, 2)
        ));

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByBoardIdOrderByPosition(1L)).thenReturn(Arrays.asList(testColumn1, testColumn2, testColumn3));
        when(boardColumnRepository.save(any(BoardColumn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<BoardColumnDto> result = boardService.reorderColumns(1L, 1L, reorderDto);

        // Then
        verify(boardColumnRepository, org.mockito.Mockito.times(3)).save(any(BoardColumn.class));
    }

    @Test
    @DisplayName("deleteColumn_DeletesTasksInside")
    void deleteColumn_DeletesTasksInside() {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setColumn(testColumn1);
        testColumn1.getTasks().add(task);

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardColumnRepository.findByIdAndBoardId(1L, 1L)).thenReturn(Optional.of(testColumn1));

        // When
        boardService.deleteColumn(1L, 1L, 1L);

        // Then
        verify(boardColumnRepository).delete(testColumn1);
    }

    @Test
    @DisplayName("updateBoard_Success")
    void updateBoard_Success() {
        // Given
        UpdateBoardDto updateDto = new UpdateBoardDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");

        when(boardRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(testBoard);

        // When
        BoardDto result = boardService.updateBoard(1L, 1L, updateDto);

        // Then
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(boardRepository).save(testBoard);
    }
}
