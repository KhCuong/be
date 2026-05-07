package com.dev.demo.service;
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.dev.demo.dto.request.CourseClassRequest;
import com.dev.demo.dto.request.CourseClassUpdateRequest;
import com.dev.demo.dto.response.CourseClassResponse;
import com.dev.demo.dto.response.EnrollmentResponse;
import com.dev.demo.dto.response.ImportFileResponse;
import com.dev.demo.entity.CourseClass;
import com.dev.demo.entity.Enrollment;
import com.dev.demo.entity.Subject;
import com.dev.demo.entity.Teacher;
import com.dev.demo.mapper.CourseClassMapper;
import com.dev.demo.mapper.EnrollmentMapper;
import com.dev.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseClassService {

    private final CourseClassRepository courseClassRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseClassMapper courseClassMapper; // Inject Mapper vào đây

    public CourseClassResponse createClass(CourseClassRequest request) {
        if (courseClassRepository.existsByClassCode(request.getClassCode())) {
            throw new RuntimeException("Mã lớp học phần đã tồn tại");
        }

        // 1. Tìm Môn học từ DB
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học"));

        // 2. Tìm Giảng viên (Nếu có truyền lên)
        Teacher teacher = null;
        if (request.getTeacherId() != null) {
            teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giảng viên"));
        }

        // 3. Dùng Mapper chuyển Request thành Entity và set thêm quan hệ
        CourseClass courseClass = courseClassMapper.toEntity(request);
        courseClass.setSubject(subject);
        courseClass.setTeacher(teacher);

        // --- BẮT ĐẦU THÊM MỚI ---
        // Tự động set thời gian: Bắt đầu từ hôm nay, kéo dài đúng 4 tháng
        courseClass.setStartDate(null);
        courseClass.setEndDate(null);


        // Trạng thái mặc định khi mới tạo là ACTIVE
        courseClass.setStatus(ClassStatus.PENDING);
        // --- KẾT THÚC THÊM MỚI ---

        // 4. Lưu và Map thẳng ra Response
        return courseClassMapper.toResponse(courseClassRepository.save(courseClass));
    }
//    public List<CourseClassResponse> getAllClasses() {
//        // 1. Lấy thông tin user hiện tại từ JWT Token
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String currentUsername = auth.getName(); // Sẽ lấy ra trường "sub" (vd: "admin" hoặc mã SV)
//
//        // 2. Kiểm tra Role
//        boolean isAdmin = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//        boolean isTeacher = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
//
//        List<CourseClass> classes;
//
//        // 3. Phân luồng dữ liệu
//        if (isAdmin) {
//            // Admin thấy tất cả
//            classes = courseClassRepository.findAll();
//        } else if (isTeacher) {
//            // Giảng viên thấy lớp mình dạy
//            classes = courseClassRepository.findByTeacherUsername(currentUsername);
//        } else {
//            // Sinh viên thấy lớp mình học
//            classes = courseClassRepository.findByStudentUsername(currentUsername);
//        }
//
//        // 4. Map ra DTO và trả về
//        return classes.stream()
//                .map(courseClassMapper::toResponse)
//                .toList();
//    }

    public CourseClassResponse getClass(String classCode) {
        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lớp học phần"));
        return courseClassMapper.toResponse(courseClass);
    }

    @Transactional
    public CourseClassResponse updateClass(String classCode, CourseClassUpdateRequest request) {
        // 1. Tìm lớp học phần
        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lớp học phần: " + classCode));

        // 2. Dùng mapper để cập nhật các field cơ bản từ Request (semester, year, room)
        courseClassMapper.updateEntityFromRequest(request, courseClass);

        // 3. Cập nhật Giảng viên nếu có thay đổi, HOẶC gỡ giảng viên
        if (request.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Giảng viên: " + request.getTeacherId()));
            courseClass.setTeacher(teacher);
        } else {
            // Rất quan trọng: Gỡ bỏ giảng viên cũ nếu Front-end không gửi teacherId
            courseClass.setTeacher(null);
        }

        return courseClassMapper.toResponse(courseClassRepository.save(courseClass));
    }

    @Transactional // Bắt buộc phải có vì ta đang thực hiện 2 lệnh Delete
    public void deleteClass(String classCode) {
        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lớp học phần"));

        // 1. Xóa toàn bộ danh sách sinh viên đăng ký lớp này trước
        // (Lưu ý: Bạn cần tạo thêm hàm void deleteByCourseClass_Id(Long id) trong EnrollmentRepository)
        enrollmentRepository.deleteByCourseClass_Id(courseClass.getId());

        // 2. Sau đó mới xóa lớp học phần
        courseClassRepository.delete(courseClass);
    }

    @Transactional
    public ImportFileResponse addStudentsToClass(String classCode, List<String> studentCodes) {
        ImportFileResponse response = ImportFileResponse.builder()
                .successCount(0) // Đảm bảo successCount không bị null để tránh NullPointerException
                .build();
        response.setTotalRows(studentCodes.size());

        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp"));

        // 1. Lấy dữ liệu lịch sử điểm danh cũ
        List<Enrollment> currentStudents = enrollmentRepository.findByCourseClass_Id(courseClass.getId());

        String tempHistory = "";
        if (!currentStudents.isEmpty() && currentStudents.get(0).getAttendanceHistory() != null) {
            int currentLessonCount = currentStudents.get(0).getAttendanceHistory().length();
            tempHistory = "0".repeat(currentLessonCount);
        }

        // [QUAN TRỌNG] Chốt biến final để truyền an toàn vào bên trong Lambda
        final String defaultHistory = tempHistory;

        List<Enrollment> newEnrollments = new ArrayList<>();
        int index = 1;
        for (String studentCode : studentCodes) {
            final int row = index;
            studentRepository.findByStudentCode(studentCode).ifPresentOrElse(student -> {
                boolean exists = enrollmentRepository
                        .existsByStudentIdAndCourseClassId(student.getId(), courseClass.getId());

                if (!exists) {
                    newEnrollments.add(Enrollment.builder()
                            .student(student)
                            .courseClass(courseClass)
                            .absenceCount(0)
                            .attendanceHistory(defaultHistory) // Lambda giờ đã hài lòng với biến final này
                            .build());

                    response.setSuccessCount(response.getSuccessCount() + 1);
                } else {
                    response.addError(row, "SV đã tồn tại trong lớp");
                }
            }, () -> response.addError(row, "SV không tồn tại"));

            index++;
        }

        if (!newEnrollments.isEmpty()) {
            enrollmentRepository.saveAll(newEnrollments);
        }
        return response;
    }
    @Transactional
    public ImportFileResponse importStudentsFromExcel(String classCode, MultipartFile file) {
        List<String> studentCodes = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(); // Dùng cái này

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell cell = row.getCell(0);
                if (cell == null) continue;

                // 1 dòng này thay thế cho toàn bộ khối if-else ở trên
                String code = formatter.formatCellValue(cell).trim();

                if (!code.isEmpty()) {
                    studentCodes.add(code);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel");
        }

        return addStudentsToClass(classCode, studentCodes);
    }
    public List<EnrollmentResponse> getStudentsInClass(String classCode) {

        // 1. Tìm lớp học phần
        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần"));

        // 2. Lấy danh sách Enrollment của lớp học này
        List<Enrollment> enrollments = enrollmentRepository.findByCourseClass_Id(courseClass.getId());

        // 3. [BẢO MẬT] Lấy thông tin người đang gọi API
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isStudent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")); // Bắt thêm Role Student

        // 4. [BẢO MẬT] Kiểm tra quyền truy cập dữ liệu
        if (!isAdmin) {
            if (isTeacher) {
                // Giảng viên: Chỉ cho xem nếu đúng là lớp mình dạy
                if (courseClass.getTeacher() == null || !courseClass.getTeacher().getTeacherCode().equals(currentUsername)) {
                    throw new RuntimeException("Truy cập bị từ chối: Bạn không phụ trách lớp này!");
                }
            } else if (isStudent) {
                // Sinh viên: Chỉ cho phép xem nếu sinh viên này CÓ TÊN trong danh sách lớp
                boolean isEnrolled = enrollments.stream()
                        .anyMatch(e -> e.getStudent().getStudentCode().equals(currentUsername));

                if (!isEnrolled) {
                    throw new RuntimeException("Truy cập bị từ chối: Bạn không học lớp này nên không thể xem danh sách!");
                }
            } else {
                // Các Role khác (nếu có) chặn tuyệt đối
                throw new RuntimeException("Truy cập bị từ chối!");
            }
        }

        // 5. Trả về danh sách nếu pass qua vòng kiểm tra
        return enrollmentMapper.toResponseList(enrollments);
    }
    @Transactional
    public void removeStudentFromClass(String classCode, String studentCode) {
        // 1. Tìm lớp học phần
        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần: " + classCode));

        // 2. Tìm sinh viên
        var student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên: " + studentCode));

        // 3. Tìm bản ghi Enrollment (Đăng ký) và xóa
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseClassId(student.getId(), courseClass.getId())
                .orElseThrow(() -> new RuntimeException("Sinh viên " + studentCode + " không nằm trong lớp " + classCode));

        enrollmentRepository.delete(enrollment);
    }


    public PageResponse<CourseClassResponse> searchClasses(String keyword,String statusStr, int page, int size) {
        // 1. Lấy thông tin user hiện tại (Giống hệt hàm getAllClasses cũ)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isStudent = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        // 2. Xử lý trạng thái (Chuyển chuỗi "ACTIVE" thành Enum ClassStatus.ACTIVE)
        ClassStatus classStatus = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                classStatus = ClassStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái lớp học không hợp lệ: " + statusStr);
            }
        }

        // 3. Cấu hình phân trang
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("classCode").ascending());
        Page<CourseClass> classPage;

        // 3. Phân luồng gọi Repository chặt chẽ hơn
        if (isAdmin) {
            classPage = courseClassRepository.searchAllClasses(keyword, classStatus, pageable);
        } else if (isTeacher) {
            classPage = courseClassRepository.searchByTeacher(keyword, currentUsername, classStatus, pageable);
        } else if (isStudent) {
            classPage = courseClassRepository.searchByStudent(keyword, currentUsername, classStatus, pageable);
        } else {
            // Chặn ngay lập tức nếu không có quyền
            throw new RuntimeException("Truy cập bị từ chối: Tài khoản không có quyền xem danh sách lớp!");
        }

        // 4. Map ra DTO và đóng gói PageResponse
        return PageResponse.<CourseClassResponse>builder()
                .currentPage(page)
                .totalPages(classPage.getTotalPages())
                .pageSize(size)
                .totalElements(classPage.getTotalElements())
                .data(courseClassMapper.toCourseClassResponseList(classPage.getContent()))
                .build();
    }


    // 2. CẬP NHẬT HÀM ĐỔI TRẠNG THÁI: Dùng classCode thay vì classId cho đồng bộ
    @Transactional
    public void changeClassStatus(String classCode, ClassStatus newStatus) {
        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần: " + classCode));

        courseClass.setStatus(newStatus);
        courseClassRepository.save(courseClass);
    }
}