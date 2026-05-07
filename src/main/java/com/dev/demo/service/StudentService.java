package com.dev.demo.service;


import com.dev.demo.dto.request.StudentCreateRequest;
import com.dev.demo.dto.request.StudentExcelRequest;
import com.dev.demo.dto.request.StudentUpdateRequest;
import com.dev.demo.dto.response.ImportFileResponse;
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.StudentResponse;
import com.dev.demo.entity.Student;
import com.dev.demo.entity.User;
import com.dev.demo.enums.Gender;
import com.dev.demo.exception.AppException;
import com.dev.demo.exception.ErrorCode;
import com.dev.demo.mapper.StudentMapper;
import com.dev.demo.repository.EnrollmentRepository;
import com.dev.demo.repository.StudentRepository;
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
public class StudentService {
    private final Validator validator;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private UserService userService;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Transactional // Bắt buộc có: Nếu tạo Student lỗi thì không được tạo User thừa
    public StudentResponse createStudent(StudentCreateRequest request) {
        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new AppException(ErrorCode.USER_EXISTED); // Nên đổi tên ErrorCode cho chuẩn
        }

        // 1. Dùng mapper để lấy toàn bộ 10 trường dữ liệu thay vì gõ tay
        Student student = studentMapper.toStudent(request);

        // 2. Tạo tài khoản đăng nhập gắn kèm
        User user = userService.createDefaultStudentAccount(request.getStudentCode());
        student.setUser(user); // liên kết 1-1

        return studentMapper.toStudentResponse(studentRepository.save(student));
    }

    public List<StudentResponse> getStudents() {
        return studentMapper.toStudentResponseList(studentRepository.findAll());
    }

    public StudentResponse getStudent(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));
        return studentMapper.toStudentResponse(student);
    }

    // 4. CẬP NHẬT THÔNG TIN SINH VIÊN
    @Transactional
    public StudentResponse updateStudent(String studentCode, StudentUpdateRequest request) {
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên để cập nhật!"));


        // Mapper sẽ tự động lấy toàn bộ field từ request đè lên object student hiện tại
        studentMapper.updateStudentFromRequest(request, student);

//        // Cập nhật các trường cho phép
//        student.setFullName(request.getFullName());
//        student.setEmail(request.getEmail());

        return studentMapper.toStudentResponse(studentRepository.save(student));
    }

    public void deleteStudent(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên để xóa"));

        //  Xóa toàn bộ lịch sử học tập/đăng ký của sinh viên này trước
        enrollmentRepository.deleteByStudent_Id(student.getId());
        // [QUAN TRỌNG]: Phải xóa tài khoản User trước/cùng lúc với xóa Student
        if (student.getUser() != null) {
            userService.deleteUser(student.getUser().getId());
        }
        studentRepository.delete(student);
    }

    @Transactional // Nếu lỗi giữa chừng, toàn bộ user và student sẽ được rollback
    public ImportFileResponse importStudents(MultipartFile file) {
        ImportFileResponse response = new ImportFileResponse();
        Set<String> existingCodes = studentRepository.findAllStudentCodes();

        List<StudentExcelRequest> requests = new ArrayList<>();
        List<Student> studentsToSave = new ArrayList<>();

        // Bước 1: Đọc file Excel
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            response.setTotalRows(sheet.getPhysicalNumberOfRows() - 1);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Đọc 11 cột từ Excel và build DTO
                requests.add(StudentExcelRequest.builder()
                        .rowIndex(i + 1)
                        .studentCode(formatter.formatCellValue(row.getCell(0)).trim())
                        .fullName(formatter.formatCellValue(row.getCell(1)).trim())
                        .gender(parseGender(formatter.formatCellValue(row.getCell(2)).trim()))
                        .dateOfBirth(parseDate(formatter.formatCellValue(row.getCell(3)).trim()))
                        .citizenId(formatter.formatCellValue(row.getCell(4)).trim())
                        .address(formatter.formatCellValue(row.getCell(5)).trim())
                        .phoneNumber(formatter.formatCellValue(row.getCell(6)).trim())
                        .email(formatter.formatCellValue(row.getCell(7)).trim())
                        .classCode(formatter.formatCellValue(row.getCell(8)).trim())
                        .cohort(formatter.formatCellValue(row.getCell(9)).trim())
                        .major(formatter.formatCellValue(row.getCell(10)).trim())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc Excel: " + e.getMessage());
        }

        // Bước 2: Validate và tạo User + Student
        for (StudentExcelRequest req : requests) {

            // 1. Kích hoạt toàn bộ luật (@Email, @Pattern, @Size...) trong DTO
            Set<ConstraintViolation<StudentExcelRequest>> violations = validator.validate(req);
            if (!violations.isEmpty()) {
                // Lấy câu thông báo lỗi đầu tiên (ví dụ: "Số điện thoại không hợp lệ")
                String errorMsg = violations.iterator().next().getMessage();
                response.addError(req.getRowIndex(), errorMsg);
                continue; // Bỏ qua dòng này, đi tiếp dòng sau
            }

            // 2. Check trùng lặp DB
            if (existingCodes.contains(req.getStudentCode())) {
                response.addError(req.getRowIndex(), "Mã SV " + req.getStudentCode() + " đã tồn tại.");
                continue;
            }

            // A. Tạo tài khoản User đăng nhập (Gọi từ UserService)
            User userAccount = userService.createDefaultStudentAccount(req.getStudentCode());

            // B. Tạo hồ sơ Student đầy đủ 11 trường
            Student newStudent = Student.builder()
                    .studentCode(req.getStudentCode())
                    .fullName(req.getFullName())
                    .gender(req.getGender())
                    .dateOfBirth(req.getDateOfBirth())
                    .citizenId(req.getCitizenId())
                    .address(req.getAddress())
                    .phoneNumber(req.getPhoneNumber())
                    .email(req.getEmail())
                    .classCode(req.getClassCode())
                    .cohort(req.getCohort())
                    .major(req.getMajor())
                    .user(userAccount) // Gắn tài khoản vừa tạo
                    .build();

            studentsToSave.add(newStudent);
            existingCodes.add(req.getStudentCode());
        }

        // Bước 3: Lưu vào Database
        if (!studentsToSave.isEmpty()) {
            studentRepository.saveAll(studentsToSave);
            response.setSuccessCount(studentsToSave.size());
        }

        return response;
    }

    // --- CÁC HÀM HELPER HỖ TRỢ ĐỌC EXCEL ---

    /**
     * Chuyển đổi chữ "Nam", "Nữ" trong Excel thành Enum Gender
     */
    private Gender parseGender(String genderStr) {
        if (genderStr == null || genderStr.isEmpty()) return null;
        if (genderStr.equalsIgnoreCase("Nam")) return Gender.MALE;
        if (genderStr.equalsIgnoreCase("Nữ") || genderStr.equalsIgnoreCase("Nu")) return Gender.FEMALE;
        return null; // Hoặc ném lỗi tùy logic của bạn
    }

    /**
     * Chuyển đổi chuỗi ngày tháng từ Excel (ví dụ 15/08/2003) sang LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            // Hỗ trợ định dạng dd/MM/yyyy (phổ biến ở VN)
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, dtf);
        } catch (Exception e) {
            // Nếu người dùng nhập sai format (vd: 15-08-2003), trả về null
            // Validator @PastOrPresent ở DTO sẽ bắt lỗi này (nếu bạn có gắn @NotNull cho NgaySinh)
            return null;
        }
    }


    public PageResponse<StudentResponse> searchStudents(String keyword, int page, int size) {
        // Spring Data JPA đếm trang từ số 0, nhưng UI thường đếm từ trang 1
        // Nên ta phải trừ đi 1. Sắp xếp danh sách theo tên (hoặc theo ID tùy bạn)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("fullName").ascending());

        // Gọi DB
        Page<Student> studentPage = studentRepository.searchStudents(keyword, pageable);

        // Map từ Student sang StudentResponse
        List<StudentResponse> studentResponses = studentMapper.toStudentResponseList(studentPage.getContent());

        // Đóng gói vào PageResponse chuẩn
        return PageResponse.<StudentResponse>builder()
                .currentPage(page)
                .totalPages(studentPage.getTotalPages())
                .pageSize(size)
                .totalElements(studentPage.getTotalElements())
                .data(studentResponses)
                .build();
    }
}
