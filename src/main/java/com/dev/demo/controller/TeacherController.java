package com.dev.demo.controller;

import com.dev.demo.dto.request.TeacherCreateRequest;
import com.dev.demo.dto.request.TeacherUpdateRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.ImportFileResponse; // Dùng chung response import
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.TeacherResponse;
import com.dev.demo.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/teachers") // Hoặc "/api/teachers" tùy cấu hình hệ thống của bạn
@RequiredArgsConstructor
@Validated // Kích hoạt validate cho các tham số
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasAuthority('TEACHER_CREATE')")
    public ApiResponse<TeacherResponse> createTeacher(@RequestBody @Valid TeacherCreateRequest request) {
        return ApiResponse.<TeacherResponse>builder()
                .result(teacherService.createTeacher(request))
                .message("Thêm giảng viên thành công")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TEACHER_VIEW')")
    public ApiResponse<List<TeacherResponse>> getTeachers() {
        return ApiResponse.<List<TeacherResponse>>builder()
                .result(teacherService.getTeachers())
                .build();
    }

    @GetMapping("/{teacherCode}")
    @PreAuthorize("hasAuthority('TEACHER_VIEW')")
    public ApiResponse<TeacherResponse> getTeacher(@PathVariable String teacherCode) {
        return ApiResponse.<TeacherResponse>builder()
                .result(teacherService.getTeacher(teacherCode))
                .build();
    }

    @PutMapping("/{teacherCode}")
    @PreAuthorize("hasAuthority('TEACHER_UPDATE')")
    public ApiResponse<TeacherResponse> updateTeacher(@PathVariable String teacherCode,
                                                      @RequestBody @Valid TeacherUpdateRequest request) {
        return ApiResponse.<TeacherResponse>builder()
                .result(teacherService.updateTeacher(teacherCode, request))
                .message("Cập nhật thông tin giảng viên thành công")
                .build();
    }

    @DeleteMapping("/{teacherCode}")
    @PreAuthorize("hasAuthority('TEACHER_DELETE')")
    public ApiResponse<String> deleteTeacher(@PathVariable String teacherCode) {
        teacherService.deleteTeacher(teacherCode);
        return ApiResponse.<String>builder()
                .result("Xóa giảng viên thành công")
                .build();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('TEACHER_CREATE')")
    public ApiResponse<ImportFileResponse> importExcel(@RequestParam("file") MultipartFile file) {
        ApiResponse<ImportFileResponse> apiResponse = new ApiResponse<>();

        // Kiểm tra tính hợp lệ của file
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            ImportFileResponse errorResponse = new ImportFileResponse();
            errorResponse.addError(0, "File không hợp lệ.");
            apiResponse.setCode(400);
            apiResponse.setMessage("Lỗi định dạng file");
            apiResponse.setResult(errorResponse);
            return apiResponse;
        }

        // Gọi Service xử lý import
        ImportFileResponse importResult = teacherService.importTeachers(file);
        apiResponse.setResult(importResult);

        // Đánh giá kết quả để trả về HTTP Code phù hợp
        if (importResult.getSuccessCount() == 0) {
            apiResponse.setCode(400);
            apiResponse.setMessage("Import thất bại toàn bộ dữ liệu.");
        } else if (importResult.getFailCount() > 0) {
            apiResponse.setCode(207); // Multi-Status (Thành công một phần)
            apiResponse.setMessage("Import thành công một phần. Vui lòng xem chi tiết lỗi.");
        } else {
            apiResponse.setCode(1000);
            apiResponse.setMessage("Import danh sách giảng viên thành công!");
        }
        return apiResponse;
    }


    @GetMapping("/search")
    @PreAuthorize("hasAuthority('TEACHER_VIEW')")
    public ApiResponse<PageResponse<TeacherResponse>> searchTeachers(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<TeacherResponse>>builder()
                .result(teacherService.searchTeachers(keyword, page, size))
                .message("Tra cứu danh sách giảng viên thành công")
                .build();
    }
}