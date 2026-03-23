package com.example.notesapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderColumnsDto {
    private List<ColumnOrder> columns;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnOrder {
        private Long id;
        private Integer position;
    }
}
