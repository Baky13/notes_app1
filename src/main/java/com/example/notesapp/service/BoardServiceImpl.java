package com.example.notesapp.service;

import com.example.notesapp.dto.BoardColumnDto;
import com.example.notesapp.dto.BoardDto;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.UpdateBoardDto;
import com.example.notesapp.entity.Board;
import com.example.notesapp.entity.BoardColumn;
import com.example.notesapp.entity.User;
import com.example.notesapp.exception.BoardNotFoundException;
import com.example.notesapp.exception.UserNotFoundException;
import com.example.notesapp.repository.BoardRepository;
import com.example.notesapp.repository.BoardColumnRepository;
import com.example.notesapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
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
    public List<BoardDto> getBoardsByUserId(Long userId) {
        return boardRepository.findByUserId(userId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDto getBoardById(Long userId, Long boardId) {
        Board board = boardRepository.findByIdAndUserId(boardId, userId)
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
