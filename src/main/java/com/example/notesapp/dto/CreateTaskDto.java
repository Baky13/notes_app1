package com.example.notesapp.dto;

import com.example.notesapp.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDto {
    
    @NotBlank(message = "Task title is required")
    private String title;
    
    private String description;
    private Priority priority;
}
