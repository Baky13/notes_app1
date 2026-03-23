package com.example.notesapp.dto;

import com.example.notesapp.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDto {
    private String title;
    private String description;
    private Priority priority;
}
