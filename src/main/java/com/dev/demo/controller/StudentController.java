package com.dev.demo.controller;

import com.dev.demo.dto.request.StudentCreateRequest;
import com.dev.demo.dto.request.StudentUpdateRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.ImportFileResponse;
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.StudentResponse;
import com.dev.demo.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    public ApiResponse<StudentResponse> createStudent(@RequestBody @Valid StudentCreateRequest request) {
        ApiResponse<StudentResponse> response = new ApiResponse<>();
        response.setResult(studentService.createStudent(request));
        return response;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<List<StudentResponse>> getStudents() {
        ApiResponse<List<StudentResponse>> response = new ApiResponse<>();
        response.setResult(studentService.getStudents());
        return response;
    }
    @GetMapping("/{studentCode}")
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<StudentResponse> getStudent(@PathVariable("studentCode") String studentCode) {
        ApiResponse<StudentResponse> response = new ApiResponse<>();
        response.setResult(studentService.getStudent(studentCode));
        return response;
    }
    @PutMapping("/{studentCode}")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<StudentResponse> updateStudent(@PathVariable("studentCode") String studentCode, @RequestBody @Valid StudentUpdateRequest request) {
        ApiResponse<StudentResponse> response = new ApiResponse<>();
        response.setResult(studentService.updateStudent(studentCode, request));
        return response;
    }

    @DeleteMapping("/{studentCode}")
    @PreAuthorize("hasAuthority('STUDENT_DELETE')")
    public ApiResponse<String> deleteStudent(@PathVariable String studentCode) {
        studentService.deleteStudent(studentCode);

        return ApiResponse.<String>builder()
                .result("Xóa sinh viên thành công")
                .build();
    }
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    public ApiResponse<ImportFileResponse> importExcel(@RequestParam("file") MultipartFile file) {
        ApiResponse<ImportFileResponse> apiResponse = new ApiResponse<>();
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            ImportFileResponse errorResponse = new ImportFileResponse();
            errorResponse.addError(0, "File không hợp lệ.");
            apiResponse.setCode(400);
            apiResponse.setMessage("Lỗi định dạng");
            apiResponse.setResult(errorResponse);
            return apiResponse;
        }

        // 2. Chỉ gọi Service khi file đã an toàn
        ImportFileResponse importResult = studentService.importStudents(file);
        apiResponse.setResult(importResult);

        // 3. Xử lý mã Code và Message trả về theo kết quả thực tế
        if (importResult.getSuccessCount() == 0) {
            // Thất bại hoàn toàn
            apiResponse.setCode(400);
            apiResponse.setMessage("Import thất bại toàn bộ dữ liệu.");
        } else if (importResult.getFailCount() > 0) {
            // Thành công một phần (có dòng lưu được, có dòng báo lỗi)
            apiResponse.setCode(207); // 207 Multi-Status
            apiResponse.setMessage("Import thành công một phần. Vui lòng xem chi tiết lỗi.");
        } else {
            // Thành công 100%
            apiResponse.setCode(1000); // Giữ nguyên code 1000 mặc định
            apiResponse.setMessage("Import danh sách sinh viên thành công!");
        }
        return apiResponse;
    }


    // GET /students/search?keyword=Cường&page=1&size=10
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<PageResponse<StudentResponse>> searchStudents(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<StudentResponse>>builder()
                .result(studentService.searchStudents(keyword, page, size))
                .message("Tra cứu danh sách sinh viên thành công")
                .build();
    }
}
