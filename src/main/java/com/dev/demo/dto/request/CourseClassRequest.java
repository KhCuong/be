package com.dev.demo.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseClassRequest {
    @NotBlank(message = "Mã lớp không được để trống")
    private String classCode;

    @NotBlank(message = "Học kỳ không được để trống")
    private String semester;

    @NotNull(message = "Năm học không được để trống")
    private Integer year;

    private String room;

    @NotNull(message = "ID Môn học không được để trống")
    private Long subjectId;

    private Long teacherId; // Có thể null (Lớp chưa có người dạy)


}