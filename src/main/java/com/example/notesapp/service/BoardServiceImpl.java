package com.example.notesapp.service;

import com.example.notesapp.dto.BoardColumnDto;
import com.example.notesapp.dto.BoardDto;
import com.example.notesapp.dto.BoardSummaryDto;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.CreateColumnDto;
import com.example.notesapp.dto.UpdateBoardDto;
import com.example.notesapp.dto.UpdateColumnDto;
import com.example.notesapp.dto.ReorderColumnsDto;
import com.example.notesapp.dto.TaskDto;
import com.example.notesapp.entity.Board;
import com.example.notesapp.entity.BoardColumn;
import com.example.notesapp.entity.Task;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.BoardNotFoundException;
import com.example.notesapp.exception.BoardColumnNotFoundException;
import com.example.notesapp.exception.UserNotFoundException;
import com.example.notesapp.repository.BoardRepository;
import com.example.notesapp.repository.BoardColumnRepository;
import com.example.notesapp.repository.TaskRepository;
import com.example.notesapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BoardDto createBoard(Long userId, CreateBoardDto createBoardDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        Board board = new Board();
        board.setTitle(createBoardDto.getTitle());
        board.setDescription(createBoardDto.getDescription());
        board.setUser(user);
        board.setColumns(new ArrayList<>());

        Board savedBoard = boardRepository.save(board);

        // Automatically create three columns: To Do, In Progress, Done
        String[] columnTitles = {"To Do", "In Progress", "Done"};
        for (int i = 0; i < columnTitles.length; i++) {
            BoardColumn column = new BoardColumn();
            column.setBoard(savedBoard);
            column.setTitle(columnTitles[i]);
            column.setPosition(i);
            column.setTasks(new ArrayList<>());
            boardColumnRepository.save(column);
            savedBoard.getColumns().add(column);
        }

        return convertToDto(savedBoard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardSummaryDto> getBoardsByUserId(Long userId) {
        return boardRepository.findByUserId(userId).stream()
            .map(this::convertToSummaryDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDto getBoardById(Long userId, Long boardId) {
        Board board = boardRepository.findByIdAndUserIdWithColumnsAndTasks(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        return convertToDto(board);
    }

    @Override
    @Transactional
    public BoardDto updateBoard(Long userId, Long boardId, UpdateBoardDto updateBoardDto) {
        Board board = boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        if (updateBoardDto.getTitle() != null) {
            board.setTitle(updateBoardDto.getTitle());
        }
        if (updateBoardDto.getDescription() != null) {
            board.setDescription(updateBoardDto.getDescription());
        }

        Board updatedBoard = boardRepository.save(board);
        return convertToDto(updatedBoard);
    }

    @Override
    @Transactional
    public void deleteBoard(Long userId, Long boardId) {
        Board board = boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        boardRepository.delete(board);
    }

    @Override
    @Transactional
    public BoardColumnDto createColumn(Long userId, Long boardId, CreateColumnDto createColumnDto) {
        Board board = boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        List<BoardColumn> existingColumns = boardColumnRepository.findByBoardIdOrderByPosition(boardId);
        int nextPosition = existingColumns.isEmpty() ? 0 : existingColumns.get(existingColumns.size() - 1).getPosition() + 1;

        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        column.setTitle(createColumnDto.getTitle());
        column.setPosition(nextPosition);
        column.setTasks(new ArrayList<>());

        BoardColumn savedColumn = boardColumnRepository.save(column);
        return convertColumnToDto(savedColumn);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnDto> getColumnsByBoardId(Long userId, Long boardId) {
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        return boardColumnRepository.findByBoardIdOrderByPosition(boardId).stream()
            .map(this::convertColumnToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoardColumnDto updateColumn(Long userId, Long boardId, Long columnId, UpdateColumnDto updateColumnDto) {
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        BoardColumn column = boardColumnRepository.findByIdAndBoardId(columnId, boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        if (updateColumnDto.getTitle() != null) {
            column.setTitle(updateColumnDto.getTitle());
        }

        BoardColumn updatedColumn = boardColumnRepository.save(column);
        return convertColumnToDto(updatedColumn);
    }

    @Override
    @Transactional
    public void deleteColumn(Long userId, Long boardId, Long columnId) {
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        BoardColumn column = boardColumnRepository.findByIdAndBoardId(columnId, boardId)
            .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        boardColumnRepository.delete(column);
    }

    @Override
    @Transactional
    public List<BoardColumnDto> reorderColumns(Long userId, Long boardId, ReorderColumnsDto reorderColumnsDto) {
        boardRepository.findByIdAndUserId(boardId, userId)
            .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        List<BoardColumn> columns = boardColumnRepository.findByBoardIdOrderByPosition(boardId);

        for (ReorderColumnsDto.ColumnOrder order : reorderColumnsDto.getColumns()) {
            BoardColumn column = columns.stream()
                .filter(c -> c.getId().equals(order.getId()))
                .findFirst()
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found: " + order.getId()));
            column.setPosition(order.getPosition());
            boardColumnRepository.save(column);
        }

        return boardColumnRepository.findByBoardIdOrderByPosition(boardId).stream()
            .map(this::convertColumnToDto)
            .collect(Collectors.toList());
    }

    private BoardDto convertToDto(Board board) {
        BoardDto dto = new BoardDto();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setDescription(board.getDescription());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setUpdatedAt(board.getUpdatedAt());

        if (board.getColumns() != null) {
            dto.setColumns(board.getColumns().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(this::convertColumnToDto)
                .collect(Collectors.toList()));
        }

        return dto;
    }

    private BoardSummaryDto convertToSummaryDto(Board board) {
        BoardSummaryDto dto = new BoardSummaryDto();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setDescription(board.getDescription());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setUpdatedAt(board.getUpdatedAt());
        return dto;
    }

    private BoardColumnDto convertColumnToDto(BoardColumn column) {
        BoardColumnDto dto = new BoardColumnDto();
        dto.setId(column.getId());
        dto.setTitle(column.getTitle());
        dto.setPosition(column.getPosition());
        dto.setCreatedAt(column.getCreatedAt());

        if (column.getTasks() != null) {
            dto.setTasks(column.getTasks().stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(task -> {
                    var taskDto = new com.example.notesapp.dto.TaskDto();
                    taskDto.setId(task.getId());
                    taskDto.setTitle(task.getTitle());
                    taskDto.setDescription(task.getDescription());
                    taskDto.setPriority(task.getPriority());
                    taskDto.setDueDate(task.getDueDate());
                    taskDto.setPosition(task.getPosition());
                    taskDto.setCreatedAt(task.getCreatedAt());
                    taskDto.setUpdatedAt(task.getUpdatedAt());
                    return taskDto;
                })
                .collect(Collectors.toList()));
        }

        return dto;
    }
}
