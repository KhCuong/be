package com.dev.demo.dto.request;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollRequest {
    private String studentCode; // Sinh viên nào?
    private String classCode;   // Đăng ký lớp nào?
}