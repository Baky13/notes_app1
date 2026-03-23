package com.example.notesapp.exception;

public class BoardColumnNotFoundException extends RuntimeException {
    public BoardColumnNotFoundException(String message) {
        super(message);
    }
}
