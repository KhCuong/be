package com.dev.demo.dto.request;
import lombok.*;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreUpdateRequest {
    private Double attendanceScore;
    private Double midtermScore;
    private Double finalScore;
}