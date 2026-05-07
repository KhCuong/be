package com.dev.demo.service;

import com.dev.demo.dto.request.SubjectExcelRequest;
import com.dev.demo.dto.request.SubjectRequest;
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.SubjectResponse;
import com.dev.demo.entity.Subject;
import com.dev.demo.mapper.SubjectMapper;
import com.dev.demo.repository.CourseClassRepository;
import com.dev.demo.repository.SubjectRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.util.ArrayList;
import com.dev.demo.dto.response.ImportFileResponse;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

// ... imports
@Service
@RequiredArgsConstructor
public class SubjectService {
    private final Validator validator;
    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final CourseClassRepository courseClassRepository;

    public SubjectResponse createSubject(SubjectRequest request) {
        if (subjectRepository.existsBySubjectCode(request.getSubjectCode())) {
            throw new RuntimeException("Mã môn học đã tồn tại");
        }
        Subject subject = Subject.builder()
                .subjectCode(request.getSubjectCode())
                .subjectName(request.getSubjectName())
                .credits(request.getCredits())
                .build();
        subject = subjectRepository.save(subject);
        return subjectMapper.toSubjectResponse(subject);
    }
    @Transactional
    public SubjectResponse updateSubject(String subjectCode, SubjectRequest request) {
        // 1. Tìm môn học theo mã (subjectCode)
        Subject subject = subjectRepository.findBySubjectCode(subjectCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với mã: " + subjectCode));

        // 2. Cập nhật thông tin (Thường thì không cho phép cập nhật mã môn học để tránh lỗi logic/ràng buộc)
        subject.setSubjectName(request.getSubjectName());
        subject.setCredits(request.getCredits());

        // 3. Lưu lại vào DB
        subject = subjectRepository.save(subject);

        return subjectMapper.toSubjectResponse(subject);
    }

    @Transactional
    public void deleteSubject(String subjectCode) {
        // 1. Kiểm tra môn học có tồn tại không
        Subject subject = subjectRepository.findBySubjectCode(subjectCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học với mã: " + subjectCode));

        // 2. [BẢO VỆ TOÀN VẸN DỮ LIỆU]: Kiểm tra xem môn học này có đang được mở Lớp Học Phần nào không?
        boolean isSubjectInUse = courseClassRepository.existsBySubject_Id(subject.getId());
        // (Hoặc existsBySubject_SubjectCode tùy cách bạn viết trong Repository)

        if (isSubjectInUse) {
            throw new RuntimeException("Không thể xóa! Môn học này đang có lớp học phần hoạt động. Vui lòng xóa các lớp học phần trước.");
        }
        // 2. Thực hiện xóa
        // Lưu ý: Nếu môn học đang có liên kết với bảng khác (Lớp học, Điểm...),
        // bạn có thể gặp lỗi rành buộc khóa ngoại (Constraint Violation).
        // Khi đó cần cân nhắc xóa mềm (soft delete) hoặc xóa các dữ liệu liên quan trước.
        subjectRepository.delete(subject);
    }
    @Transactional
    public ImportFileResponse importSubjects(MultipartFile file) {
        ImportFileResponse response = new ImportFileResponse();

        List<SubjectExcelRequest> requests = new ArrayList<>();
        List<Subject> subjectsToSave = new ArrayList<>();

        // Lấy danh sách mã môn đã tồn tại trong DB
        Set<String> existingCodes = subjectRepository.findAllSubjectCodes();

        DataFormatter formatter = new DataFormatter();

        // ===== BƯỚC 1: ĐỌC EXCEL → DTO =====
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            response.setTotalRows(sheet.getPhysicalNumberOfRows() - 1);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String creditsStr = formatter.formatCellValue(row.getCell(2)).trim();

                Integer credits = null;
                try {
                    if (!creditsStr.isEmpty()) {
                        credits = Integer.parseInt(creditsStr);
                    }
                } catch (NumberFormatException e) {
                    // để null → validator xử lý
                }

                requests.add(SubjectExcelRequest.builder()
                        .rowIndex(i + 1)
                        .subjectCode(formatter.formatCellValue(row.getCell(0)).trim())
                        .subjectName(formatter.formatCellValue(row.getCell(1)).trim())
                        .credits(credits)
                        .build());
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc Excel: " + e.getMessage());
        }

        // ===== BƯỚC 2: VALIDATE + BUILD ENTITY =====
        for (SubjectExcelRequest req : requests) {

            // 1. Validate bằng annotation
            Set<ConstraintViolation<SubjectExcelRequest>> violations = validator.validate(req);
            if (!violations.isEmpty()) {
                String errorMsg = violations.iterator().next().getMessage();
                response.addError(req.getRowIndex(), errorMsg);
                continue;
            }

            // 2. Check trùng DB
            if (existingCodes.contains(req.getSubjectCode())) {
                response.addError(req.getRowIndex(),
                        "Mã môn học " + req.getSubjectCode() + " đã tồn tại.");
                continue;
            }

            // 3. Check trùng trong file
            boolean isDuplicateInFile = subjectsToSave.stream()
                    .anyMatch(s -> s.getSubjectCode().equals(req.getSubjectCode()));

            if (isDuplicateInFile) {
                response.addError(req.getRowIndex(),
                        "Mã môn học " + req.getSubjectCode() + " bị trùng trong file.");
                continue;
            }

            // 4. Build entity
            Subject subject = Subject.builder()
                    .subjectCode(req.getSubjectCode())
                    .subjectName(req.getSubjectName())
                    .credits(req.getCredits())
                    .build();

            subjectsToSave.add(subject);
            existingCodes.add(req.getSubjectCode());
        }

        // ===== BƯỚC 3: SAVE =====
        if (!subjectsToSave.isEmpty()) {
            subjectRepository.saveAll(subjectsToSave);
            response.setSuccessCount(subjectsToSave.size());
        }

        return response;
    }

    // Hàm tiện ích để đọc giá trị Cell an toàn
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // Ép kiểu về int để loại bỏ phần thập phân (.0) nếu Excel tự động convert
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return "";
        }
    }
    public List<SubjectResponse> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        return subjectMapper.toSubjectResponseList(subjects);
    }


    public PageResponse<SubjectResponse> searchSubjects(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("subjectName").ascending());
        Page<Subject> subjectPage = subjectRepository.searchSubjects(keyword, pageable);

        return PageResponse.<SubjectResponse>builder()
                .currentPage(page)
                .totalPages(subjectPage.getTotalPages())
                .pageSize(size)
                .totalElements(subjectPage.getTotalElements())
                // Giả định bạn có hàm map list trong SubjectMapper
                .data(subjectMapper.toSubjectResponseList(subjectPage.getContent()))
                .build();
    }
}