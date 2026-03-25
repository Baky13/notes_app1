package com.example.notesapp.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBoardDto {
    
    @Size(max = 100, message = "Board title must be at most 100 characters")
    private String title;
    
    private String description;
}
