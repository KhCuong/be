package com.dev.demo.controller;

import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.MyClassResponse;
import com.dev.demo.dto.response.StudentResponse;
import com.dev.demo.service.EnrollmentService;
import com.dev.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@Slf4j
public class MeController {

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    /**
     * API 1: Xem Hồ sơ cá nhân của chính mình
     * GET /me/profile
     */
    @GetMapping("/profile")
    public ApiResponse<StudentResponse> getMyProfile() {
        // Lấy username (Mã SV) từ Token của người đang đăng nhập
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Student {} is viewing their profile", currentUsername);

        return ApiResponse.<StudentResponse>builder()
                .result(studentService.getStudent(currentUsername))
                .build();
    }

    /**
     * API 2: Xem danh sách lớp và điểm của chính mình trong 1 học kỳ
     * GET /me/enrollments?semester=HK1&year=2024
     */
    @GetMapping("/enrollments")
    public ApiResponse<List<MyClassResponse>> getMyEnrollments(
            @RequestParam String semester,
            @RequestParam Integer year) {

        // Tuyệt đối không nhận studentCode từ RequestParam để tránh bị xem trộm điểm
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Student {} is viewing grades for {} - {}", currentUsername, semester, year);

        return ApiResponse.<List<MyClassResponse>>builder()
                .result(enrollmentService.getMyClassesInSemester(currentUsername, semester, year))
                .build();
    }
}