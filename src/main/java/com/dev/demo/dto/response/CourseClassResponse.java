package com.dev.demo.dto.response;
import com.dev.demo.enums.ClassStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
public class CourseClassResponse {
    private Long id;
    private String classCode;
    private String semester;
    private Integer year;
    private String room;

    // Gộp thông tin trả về cho Frontend dễ hiển thị
    private String subjectName;
    private String teacherName;

    private ClassStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}