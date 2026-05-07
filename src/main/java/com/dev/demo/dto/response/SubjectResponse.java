package com.dev.demo.dto.response;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectResponse {
    private Long id;
    private String subjectCode;
    private String subjectName;
    private Integer credits;
}