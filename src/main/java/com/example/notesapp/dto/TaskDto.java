package com.example.notesapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.notesapp.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate dueDate;
    
    private Integer position;
    
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime updatedAt;
}
