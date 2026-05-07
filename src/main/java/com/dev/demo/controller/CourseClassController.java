package com.dev.demo.controller;

import com.dev.demo.dto.request.CourseClassRequest;
import com.dev.demo.dto.request.CourseClassUpdateRequest;
import com.dev.demo.dto.response.*;
import com.dev.demo.enums.ClassStatus;
import com.dev.demo.service.CourseClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/course-classes")
@RequiredArgsConstructor
@Validated
public class CourseClassController {

    private final CourseClassService courseClassService;

    // 1. Admin tạo Lớp học phần mới
    @PostMapping
    @PreAuthorize("hasAuthority('CLASS_CREATE')")
    public ApiResponse<CourseClassResponse> createClass(@RequestBody @Valid CourseClassRequest request) {
        return ApiResponse.<CourseClassResponse>builder()
                .result(courseClassService.createClass(request))
                .message("Tạo lớp học phần thành công")
                .build();
    }


//    @GetMapping
//    @PreAuthorize("hasAuthority('CLASS_VIEW')") //(Vì ai cũng có quyền xem danh sách của mình)
//    public ApiResponse<List<CourseClassResponse>> getAllClasses() {
//        return ApiResponse.<List<CourseClassResponse>>builder()
//                .result(courseClassService.getAllClasses())
//                .message("Lấy danh sách lớp học phần thành công")
//                .build();
//    }

    @GetMapping("/{classCode}")
    @PreAuthorize("hasAuthority('CLASS_VIEW')")
    public ApiResponse<CourseClassResponse> getClass(@PathVariable String classCode) {
        return ApiResponse.<CourseClassResponse>builder()
                .result(courseClassService.getClass(classCode))
                .build();
    }

    @PutMapping("/{classCode}")
    @PreAuthorize("hasAuthority('CLASS_UPDATE')")
    public ApiResponse<CourseClassResponse> updateClass(
            @PathVariable String classCode,
            @RequestBody @Valid CourseClassUpdateRequest request) {
        return ApiResponse.<CourseClassResponse>builder()
                .result(courseClassService.updateClass(classCode, request))
                .message("Cập nhật lớp học phần thành công")
                .build();
    }

    @DeleteMapping("/{classCode}")
    @PreAuthorize("hasAuthority('CLASS_DELETE')")
    public ApiResponse<String> deleteClass(@PathVariable String classCode) {
        courseClassService.deleteClass(classCode);
        return ApiResponse.<String>builder()
                .result("Xóa lớp học phần thành công")
                .build();
    }

    // ===== LẤY DANH SÁCH SINH VIÊN TRONG LỚP =====
    @GetMapping("/{classCode}/students")
    @PreAuthorize("hasAuthority('CLASS_VIEW')")
    public ApiResponse<List<EnrollmentResponse>> getStudents(@PathVariable String classCode) {
        return ApiResponse.<List<EnrollmentResponse>>builder()
                .result(courseClassService.getStudentsInClass(classCode))
                .build();
    }

    // ===== THÊM SINH VIÊN THỦ CÔNG =====
    @PostMapping("/{classCode}/students")
    @PreAuthorize("hasAuthority('CLASS_STUDENT_MANAGE')")
    public ApiResponse<ImportFileResponse> addStudents(
            @PathVariable String classCode,
            @RequestBody List<String> studentCodes) {

        ImportFileResponse result = courseClassService.addStudentsToClass(classCode, studentCodes);
        return buildImportResponse(result, "Thêm sinh viên");
    }

    // ===== IMPORT EXCEL =====
    @PostMapping("/{classCode}/students/import")
    @PreAuthorize("hasAuthority('CLASS_STUDENT_MANAGE')")
    public ApiResponse<ImportFileResponse> importStudents(
            @PathVariable String classCode,
            @RequestParam("file") MultipartFile file) {

        ImportFileResponse result = courseClassService.importStudentsFromExcel(classCode, file);
        return buildImportResponse(result, "Import Excel");
    }

    private ApiResponse<ImportFileResponse> buildImportResponse(ImportFileResponse result, String action) {
        ApiResponse<ImportFileResponse> res = new ApiResponse<>();
        res.setResult(result);

        if (result.getSuccessCount() == 0 && result.getFailCount() > 0) {
            res.setCode(400);
            res.setMessage(action + " thất bại toàn bộ");
        } else if (result.getFailCount() > 0) {
            res.setCode(207);
            res.setMessage(action + " thành công một phần");
        } else {
            res.setCode(1000);
            res.setMessage(action + " thành công");
        }
        return res;
    }
    @DeleteMapping("/{classCode}/students/{studentCode}")
    @PreAuthorize("hasAuthority('CLASS_STUDENT_MANAGE')")
    public ApiResponse<String> removeStudent(
            @PathVariable String classCode,
            @PathVariable String studentCode) {

        courseClassService.removeStudentFromClass(classCode, studentCode);

        return ApiResponse.<String>builder()
                .result("Xóa sinh viên " + studentCode + " khỏi lớp " + classCode + " thành công")
                .build();
    }


    @GetMapping("/search")
    @PreAuthorize("hasAuthority('CLASS_VIEW')")
    public ApiResponse<PageResponse<CourseClassResponse>> searchClasses(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "") String status, // Thêm dòng này để hứng status từ Frontend
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<CourseClassResponse>>builder()
                .result(courseClassService.searchClasses(keyword,status, page, size))
                .message("Tra cứu danh sách lớp học thành công")
                .build();
    }
    // ===== ADMIN KHÓA / MỞ LỚP THỦ CÔNG =====
    @PutMapping("/{classCode}/status")
    @PreAuthorize("hasAuthority('CLASS_UPDATE')") // Bạn có thể giữ quyền này hoặc đổi thành 'ROLE_ADMIN' tùy thiết kế
    public ApiResponse<String> changeClassStatus(
            @PathVariable String classCode,
            @RequestParam ClassStatus status) {

        courseClassService.changeClassStatus(classCode, status);

        return ApiResponse.<String>builder()
                .result("Cập nhật trạng thái lớp " + classCode + " thành " + status + " thành công")
                .build();
    }
}