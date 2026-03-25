package com.example.notesapp.service;

import com.example.notesapp.dto.BoardColumnDto;
import com.example.notesapp.dto.BoardDto;
import com.example.notesapp.dto.BoardSummaryDto;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.CreateColumnDto;
import com.example.notesapp.dto.UpdateBoardDto;
import com.example.notesapp.dto.UpdateColumnDto;
import com.example.notesapp.dto.ReorderColumnsDto;

import java.util.List;

public interface BoardService {
    BoardDto createBoard(Long userId, CreateBoardDto createBoardDto);
    List<BoardSummaryDto> getBoardsByUserId(Long userId);
    BoardDto getBoardById(Long userId, Long boardId);
    BoardDto updateBoard(Long userId, Long boardId, UpdateBoardDto updateBoardDto);
    void deleteBoard(Long userId, Long boardId);
    
    // Column operations
    BoardColumnDto createColumn(Long userId, Long boardId, CreateColumnDto createColumnDto);
    List<BoardColumnDto> getColumnsByBoardId(Long userId, Long boardId);
    BoardColumnDto updateColumn(Long userId, Long boardId, Long columnId, UpdateColumnDto updateColumnDto);
    void deleteColumn(Long userId, Long boardId, Long columnId);
    List<BoardColumnDto> reorderColumns(Long userId, Long boardId, ReorderColumnsDto reorderColumnsDto);
}
