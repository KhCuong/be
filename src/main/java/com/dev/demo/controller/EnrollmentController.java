package com.dev.demo.controller;

import com.dev.demo.dto.request.AttendanceSubmitRequest;
import com.dev.demo.dto.request.ScoreUpdateRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.AttendanceWeekResponse;
import com.dev.demo.dto.response.EnrollmentResponse;
import com.dev.demo.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
@Validated
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * API 2: Dành cho GIẢNG VIÊN
     * Giảng viên gọi API này để cập nhật điểm cho 1 sinh viên cụ thể trong 1 lớp cụ thể
     */
    @PutMapping("/scores/{classCode}/{studentCode}")
    @PreAuthorize("hasAuthority('SCORE_UPDATE')")
    public ApiResponse<String> updateScore(
            @PathVariable String classCode,
            @PathVariable String studentCode,
            @RequestBody @Valid ScoreUpdateRequest request) {

        return ApiResponse.<String>builder()
                .result(enrollmentService.updateScore(studentCode, classCode, request))
                .message("Lưu điểm thành công")
                .build();
    }
    /**
     * API: Dành cho Giảng viên bấm nút "Tạo buổi học mới"
     */
    @PostMapping("/attendance/class/{classCode}/new-lesson")
    @PreAuthorize("hasAuthority('ATTENDANCE_MANAGE')")
    public ApiResponse<String> createNewLesson(@PathVariable String classCode) {
        return ApiResponse.<String>builder()
                .result(enrollmentService.createNewLesson(classCode))
                .build();
    }
    /**
     * API: Dành cho Giảng viên xóa một buổi học bị tạo nhầm
     */
    @DeleteMapping("/attendance/class/{classCode}/lesson/{lessonNumber}")
    @PreAuthorize("hasAuthority('ATTENDANCE_MANAGE')")
    public ApiResponse<String> deleteLesson(
            @PathVariable String classCode,
            @PathVariable int lessonNumber) {

        return ApiResponse.<String>builder()
                .result(enrollmentService.deleteLesson(classCode, lessonNumber))
                .message("Xóa buổi học thành công")
                .build();
    }

    /**
     * API: Giảng viên xem danh sách điểm danh theo Tuần
     * Trả về danh sách sinh viên kèm trạng thái vắng mặt của tuần đó
     */
    @GetMapping("/attendance/class/{classCode}/week/{weekNumber}")
    @PreAuthorize("hasAuthority('CLASS_VIEW')")
    public ApiResponse<AttendanceWeekResponse> getAttendanceByWeek(
            @PathVariable String classCode,
            @PathVariable int weekNumber) {

        return ApiResponse.<AttendanceWeekResponse>builder()
                .result(enrollmentService.getAttendanceByWeek(classCode, weekNumber))
                .build();
    }

    @PostMapping("/attendance/submit")
    @PreAuthorize("hasAuthority('ATTENDANCE_MANAGE')")
    public ApiResponse<String> submitClassAttendance(@RequestBody @Valid AttendanceSubmitRequest request) {

        enrollmentService.submitClassAttendance(request);

        return ApiResponse.<String>builder()
                .result("Lưu điểm danh tuần " + request.getWeekNumber() + " thành công!")
                .build();
    }
    // API: Sinh viên xem điểm của mình
    @GetMapping("/student/{studentCode}")
    @PreAuthorize("hasAuthority('SCORE_VIEW')")
    public ApiResponse<List<EnrollmentResponse>> getStudentScores(@PathVariable String studentCode) {
        return ApiResponse.<List<EnrollmentResponse>>builder()
                .result(enrollmentService.getStudentScores(studentCode))
                .build();
    }





//    @PostMapping("/attendance/initialize/{classCode}")
//    public ApiResponse<String> initializeAttendanceForClass(@PathVariable String classCode) {
//        return ApiResponse.<String>builder()
//                .result(enrollmentService.initializeAttendanceForClass(classCode))
//                .message("Khởi tạo dữ liệu điểm danh cho 16 tuần thành công")
//                .build();
//    }
}