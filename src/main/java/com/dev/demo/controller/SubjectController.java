package com.dev.demo.controller;

import com.dev.demo.dto.request.SubjectRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.SubjectResponse;
import com.dev.demo.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
// Thêm import MultipartFile
import org.springframework.web.multipart.MultipartFile;
import com.dev.demo.dto.response.ImportFileResponse;
import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Validated
public class SubjectController {

    private final SubjectService subjectService;

    // 1. Thêm mới môn học
    @PreAuthorize("hasAuthority('SUBJECT_CREATE')")
    @PostMapping
    public ApiResponse<SubjectResponse> createSubject(@RequestBody @Valid SubjectRequest request) {
        return ApiResponse.<SubjectResponse>builder()
                .result(subjectService.createSubject(request))
                .message("Thêm môn học thành công")
                .build();
    }

    // 4. Cập nhật môn học
    @PreAuthorize("hasAuthority('SUBJECT_UPDATE')")
    @PutMapping("/{subjectCode}")

    public ApiResponse<SubjectResponse> updateSubject(
            @PathVariable String subjectCode,
            @RequestBody @Valid SubjectRequest request) {

        return ApiResponse.<SubjectResponse>builder()
                .result(subjectService.updateSubject(subjectCode, request))
                .message("Cập nhật môn học thành công")
                .build();
    }

    // 5. Xóa môn học
    @PreAuthorize("hasAuthority('SUBJECT_DELETE')")
    @DeleteMapping("/{subjectCode}")

    public ApiResponse<Void> deleteSubject(@PathVariable String subjectCode) {

        subjectService.deleteSubject(subjectCode);

        return ApiResponse.<Void>builder()
                .message("Xóa môn học thành công")
                .build();
    }
    @PreAuthorize("hasAuthority('SUBJECT_CREATE')")
    @PostMapping("/import")

    public ApiResponse<ImportFileResponse> importExcel(@RequestParam("file") MultipartFile file) {
        ApiResponse<ImportFileResponse> apiResponse = new ApiResponse<>();

        // 1. Validate định dạng file
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".xlsx")) {
            ImportFileResponse errorResponse = new ImportFileResponse();
            errorResponse.addError(0, "File không hợp lệ. Vui lòng tải lên file .xlsx");
            apiResponse.setCode(400);
            apiResponse.setMessage("Lỗi định dạng file");
            apiResponse.setResult(errorResponse);
            return apiResponse;
        }

        // 2. Gọi Service xử lý file
        ImportFileResponse importResult = subjectService.importSubjects(file);
        apiResponse.setResult(importResult);

        // 3. Xử lý trạng thái trả về
        if (importResult.getSuccessCount() == 0 && importResult.getFailCount() > 0) {
            // Thất bại hoàn toàn
            apiResponse.setCode(400);
            apiResponse.setMessage("Import thất bại toàn bộ dữ liệu.");
        } else if (importResult.getFailCount() > 0) {
            // Thành công một phần
            apiResponse.setCode(207);
            apiResponse.setMessage("Import thành công một phần. Vui lòng xem chi tiết lỗi.");
        } else {
            // Thành công 100%
            apiResponse.setCode(1000);
            apiResponse.setMessage("Import danh sách môn học thành công!");
        }

        return apiResponse;
    }

// ... (Bên trong SubjectController)
    // 2. Lấy danh sách toàn bộ môn học
    @PreAuthorize("hasAuthority('SUBJECT_VIEW')")
    @GetMapping
    public ApiResponse<List<SubjectResponse>> getAllSubjects() {
        return ApiResponse.<List<SubjectResponse>>builder()
                .result(subjectService.getAllSubjects())
                .build();
    }

    // (Tùy chọn) 3. Lấy chi tiết 1 môn học theo Code
    @GetMapping("/{subjectCode}")
    @PreAuthorize("hasAuthority('SUBJECT_VIEW')")
    public ApiResponse<SubjectResponse> getSubject(@PathVariable String subjectCode) {
        // Cần thêm hàm getSubject(subjectCode) vào SubjectService
        // return ApiResponse.<SubjectResponse>builder()
        //         .result(subjectService.getSubject(subjectCode))
        //         .build();
        throw new UnsupportedOperationException("Chưa implement lấy chi tiết theo mã.");
    }


    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SUBJECT_VIEW')")
    public ApiResponse<PageResponse<SubjectResponse>> searchSubjects(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<SubjectResponse>>builder()
                .result(subjectService.searchSubjects(keyword, page, size))
                .message("Tra cứu danh sách môn học thành công")
                .build();
    }
}