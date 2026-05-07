package com.dev.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportFileResponse {

    @Builder.Default
    private int totalRows = 0;

    @Builder.Default
    private int successCount = 0;

    @Builder.Default
    private int failCount = 0;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    public void addError(int rowIndex, String message) {
        this.errors.add("Dòng " + rowIndex + ": " + message);
        this.failCount++;
    }
}