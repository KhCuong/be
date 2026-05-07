package com.dev.demo.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectRequest {
    @NotBlank(message = "Mã môn học không được để trống")
    private String subjectCode;

    @NotBlank(message = "Tên môn học không được để trống")
    private String subjectName;

    @NotNull(message = "Số tín chỉ không được để trống")
    @Min(value = 1, message = "Số tín chỉ tối thiểu là 1")
    private Integer credits;
}