package com.example.notesapp.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateColumnDto {
    
    @Size(max = 100, message = "Column title must be at most 100 characters")
    private String title;
}
