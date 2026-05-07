package com.dev.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseClassUpdateRequest {
    // Không cần gửi classCode hay subjectId lên nữa

    @NotBlank(message = "Học kỳ không được để trống")
    private String semester;

    @NotNull(message = "Năm học không được để trống")
    private Integer year;

    private String room;
    private Long teacherId; // Truyền ID giảng viên mới vào đây để đổi
}
