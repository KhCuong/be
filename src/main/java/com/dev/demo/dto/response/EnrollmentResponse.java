package com.dev.demo.dto.response;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
public class EnrollmentResponse {
    private long id;
    private String studentCode;
    private String studentName;
    private LocalDate dateOfBirth;
    private String classCode;
    private String subjectName;
    private Double attendanceScore;
    private Double midtermScore;
    private Double finalScore;
    private Double totalScore;
    private Integer absenceCount;
    private String attendanceHistory;
}