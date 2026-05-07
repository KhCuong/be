package com.dev.demo.service;

import com.dev.demo.dto.request.TeacherCreateRequest;
import com.dev.demo.dto.request.TeacherExcelRequest;
import com.dev.demo.dto.request.TeacherUpdateRequest;
import com.dev.demo.dto.response.ImportFileResponse; // Dùng chung response import với Student cho tiện
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.TeacherResponse;
import com.dev.demo.entity.CourseClass;
import com.dev.demo.entity.Teacher;
import com.dev.demo.entity.User;
import com.dev.demo.enums.Gender;
import com.dev.demo.exception.AppException;
import com.dev.demo.exception.ErrorCode;
import com.dev.demo.mapper.TeacherMapper;
import com.dev.demo.repository.CourseClassRepository;
import com.dev.demo.repository.TeacherRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserService userService;
    private final Validator validator;
    private final CourseClassRepository courseClassRepository;

    @Autowired
    private TeacherMapper teacherMapper;

    // 1. THÊM MỚI GIẢNG VIÊN
    @Transactional
    public TeacherResponse createTeacher(TeacherCreateRequest request) {
        if (teacherRepository.existsByTeacherCode(request.getTeacherCode())) {
            throw new AppException(ErrorCode.USER_EXISTED); // Tùy chỉnh ErrorCode cho phù hợp
        }

        // 1. Map request sang Entity
        Teacher teacher = teacherMapper.toTeacher(request);

        // 2. Tạo tài khoản đăng nhập gắn kèm (Cần tạo hàm này trong UserService)
        User user = userService.createDefaultTeacherAccount(request.getTeacherCode());
        teacher.setUser(user);

        return teacherMapper.toTeacherResponse(teacherRepository.save(teacher));
    }

    // 2. LẤY DANH SÁCH GIẢNG VIÊN
    public List<TeacherResponse> getTeachers() {
        return teacherMapper.toTeacherResponseList(teacherRepository.findAll());
    }

    // 3. LẤY CHI TIẾT 1 GIẢNG VIÊN
    public TeacherResponse getTeacher(String teacherCode) {
        Teacher teacher = teacherRepository.findByTeacherCode(teacherCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));
        return teacherMapper.toTeacherResponse(teacher);
    }

    // 4. CẬP NHẬT THÔNG TIN GIẢNG VIÊN
    @Transactional
    public TeacherResponse updateTeacher(String teacherCode, TeacherUpdateRequest request) {
        Teacher teacher = teacherRepository.findByTeacherCode(teacherCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên để cập nhật!"));

        // Mapper sẽ tự động map các trường từ request đè lên object teacher hiện tại
        teacherMapper.updateTeacherFromRequest(request, teacher);

        return teacherMapper.toTeacherResponse(teacherRepository.save(teacher));
    }

    // 5. XÓA GIẢNG VIÊN
    @Transactional
    public void deleteTeacher(String teacherCode) {
        Teacher teacher = teacherRepository.findByTeacherCode(teacherCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên để xóa"));

        // 1. Gỡ giảng viên này khỏi tất cả các lớp đang dạy trước khi xóa
        // Bạn cần tiêm CourseClassRepository vào Service này
        List<CourseClass> teachingClasses = courseClassRepository.findByTeacherUsername(teacherCode);
        for (CourseClass courseClass : teachingClasses) {
            courseClass.setTeacher(null);
        }
        courseClassRepository.saveAll(teachingClasses);

        // 2. Xóa tài khoản User
        if (teacher.getUser() != null) {
            userService.deleteUser(teacher.getUser().getId());
        }

        // 3. Cuối cùng mới xóa Giảng viên
        teacherRepository.delete(teacher);
    }
    @Transactional
    public ImportFileResponse importTeachers(MultipartFile file) {
        ImportFileResponse response = new ImportFileResponse();
        Set<String> existingCodes = teacherRepository.findAllTeacherCodes();

        List<TeacherExcelRequest> requests = new ArrayList<>();
        List<Teacher> teachersToSave = new ArrayList<>();

        // Bước 1: Đọc file Excel
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            response.setTotalRows(sheet.getPhysicalNumberOfRows() - 1);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Đọc 10 cột từ Excel và build DTO
                requests.add(TeacherExcelRequest.builder()
                        .rowIndex(i + 1)
                        .teacherCode(formatter.formatCellValue(row.getCell(0)).trim())
                        .fullName(formatter.formatCellValue(row.getCell(1)).trim())
                        .gender(parseGender(formatter.formatCellValue(row.getCell(2)).trim()))
                        .dateOfBirth(parseDate(formatter.formatCellValue(row.getCell(3)).trim()))
                        .citizenId(formatter.formatCellValue(row.getCell(4)).trim())
                        .address(formatter.formatCellValue(row.getCell(5)).trim())
                        .phoneNumber(formatter.formatCellValue(row.getCell(6)).trim())
                        .email(formatter.formatCellValue(row.getCell(7)).trim())
                        .department(formatter.formatCellValue(row.getCell(8)).trim())
                        .specialization(formatter.formatCellValue(row.getCell(9)).trim())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc Excel: " + e.getMessage());
        }

        // Bước 2: Validate và tạo User + Teacher
        for (TeacherExcelRequest req : requests) {

            Set<ConstraintViolation<TeacherExcelRequest>> violations = validator.validate(req);
            if (!violations.isEmpty()) {
                String errorMsg = violations.iterator().next().getMessage();
                response.addError(req.getRowIndex(), errorMsg);
                continue;
            }

            if (existingCodes.contains(req.getTeacherCode())) {
                response.addError(req.getRowIndex(), "Mã GV " + req.getTeacherCode() + " đã tồn tại.");
                continue;
            }

            // GỌI HÀM TẠO ACCOUNT CỦA GIẢNG VIÊN (Cần thêm hàm này vào UserService)
            User userAccount = userService.createDefaultTeacherAccount(req.getTeacherCode());

            Teacher newTeacher = Teacher.builder()
                    .teacherCode(req.getTeacherCode())
                    .fullName(req.getFullName())
                    .gender(req.getGender())
                    .dateOfBirth(req.getDateOfBirth())
                    .citizenId(req.getCitizenId())
                    .address(req.getAddress())
                    .phoneNumber(req.getPhoneNumber())
                    .email(req.getEmail())
                    .department(req.getDepartment())
                    .specialization(req.getSpecialization())
                    .user(userAccount)
                    .build();

            teachersToSave.add(newTeacher);
            existingCodes.add(req.getTeacherCode());
        }

        // Bước 3: Lưu vào Database
        if (!teachersToSave.isEmpty()) {
            teacherRepository.saveAll(teachersToSave);
            response.setSuccessCount(teachersToSave.size());
        }

        return response;
    }

    // --- CÁC HÀM HELPER HỖ TRỢ ĐỌC EXCEL (Giống hệt Student) ---
    private Gender parseGender(String genderStr) {
        if (genderStr == null || genderStr.isEmpty()) return null;
        if (genderStr.equalsIgnoreCase("Nam")) return Gender.MALE;
        if (genderStr.equalsIgnoreCase("Nữ") || genderStr.equalsIgnoreCase("Nu")) return Gender.FEMALE;
        return null;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, dtf);
        } catch (Exception e) {
            return null;
        }
    }

    public PageResponse<TeacherResponse> searchTeachers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("fullName").ascending());
        Page<Teacher> teacherPage = teacherRepository.searchTeachers(keyword, pageable);

        return PageResponse.<TeacherResponse>builder()
                .currentPage(page)
                .totalPages(teacherPage.getTotalPages())
                .pageSize(size)
                .totalElements(teacherPage.getTotalElements())
                .data(teacherMapper.toTeacherResponseList(teacherPage.getContent()))
                .build();
    }

}