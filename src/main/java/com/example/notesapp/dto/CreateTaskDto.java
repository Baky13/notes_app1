package com.example.notesapp.dto;

import com.example.notesapp.entity.Priority;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDto {
    
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title must be at most 200 characters")
    private String title;
    
    private String description;
    private Priority priority;
    
    @FutureOrPresent(message = "Due date must be today or in the future")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate dueDate;
}
