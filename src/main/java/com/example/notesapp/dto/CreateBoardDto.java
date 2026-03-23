package com.example.notesapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBoardDto {
    
    @NotBlank(message = "Board title is required")
    private String title;
    
    private String description;
}
