package com.example.notesapp.service;

import com.example.notesapp.dto.BoardDto;
import com.example.notesapp.dto.CreateBoardDto;
import com.example.notesapp.dto.UpdateBoardDto;

import java.util.List;

public interface BoardService {
    BoardDto createBoard(Long userId, CreateBoardDto createBoardDto);
    List<BoardDto> getBoardsByUserId(Long userId);
    BoardDto getBoardById(Long userId, Long boardId);
    BoardDto updateBoard(Long userId, Long boardId, UpdateBoardDto updateBoardDto);
    void deleteBoard(Long userId, Long boardId);
}
