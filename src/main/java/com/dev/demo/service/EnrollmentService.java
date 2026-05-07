package com.dev.demo.service;

import com.dev.demo.dto.request.AttendanceSubmitRequest;
import com.dev.demo.dto.request.ScoreUpdateRequest;
import com.dev.demo.dto.response.AttendanceWeekResponse;
import com.dev.demo.dto.response.EnrollmentResponse;
import com.dev.demo.dto.response.MyClassResponse;
import com.dev.demo.entity.CourseClass;
import com.dev.demo.entity.Enrollment;
import com.dev.demo.enums.ClassStatus;
import com.dev.demo.mapper.EnrollmentMapper;
import com.dev.demo.repository.CourseClassRepository;
import com.dev.demo.repository.EnrollmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final CourseClassRepository courseClassRepository;

    // --- HELPER METHODS ---

    // Hàm chống lỗi NullPointerException khi lấy chuỗi điểm danh
    private String getSafeHistory(Enrollment enrollment) {
        return enrollment.getAttendanceHistory() == null ? "" : enrollment.getAttendanceHistory();
    }

    private void verifyStudentOwnership(String requestedStudentCode) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !currentUsername.equals(requestedStudentCode)) {
            throw new RuntimeException("Truy cập bị từ chối: Bạn không được phép xem dữ liệu của sinh viên khác!");
        }
    }

    private void verifyTeacherOwnership(String classCode) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần"));

        if (courseClass.getTeacher() == null || !(courseClass.getTeacher().getTeacherCode().equals(currentUsername))) {
            throw new RuntimeException("Truy cập bị từ chối: Bạn không phải là giảng viên phụ trách lớp này!");
        }
    }


    // 👇 CHỐT CHẶN 1: Dùng cho Điểm danh (Chỉ được điểm danh khi lớp ACTIVE)
    private void verifyClassCanUpdateAttendance(CourseClass courseClass) {
        if (courseClass.getStatus() == ClassStatus.COMPLETED) {
            throw new RuntimeException("Lớp học đã kết thúc hoặc đang khóa điểm danh. Không thể thay đổi dữ liệu!");
        }
    }

    // 👇 CHỐT CHẶN 2: Dùng cho Nhập điểm (Được nhập khi ACTIVE hoặc GRADING)
    private void verifyClassCanUpdateScore(CourseClass courseClass) {
        if (courseClass.getStatus() == ClassStatus.COMPLETED) {
            throw new RuntimeException("Lớp học đã kết thúc. Không thể thay đổi điểm số!");
        }
    }
    // Hàm chuẩn hóa tính điểm (Đã sửa lỗi null và bỏ chia hệ 4)
    private void calculateAndSetScores(Enrollment enrollment) {
        String history = getSafeHistory(enrollment);
        long absences = history.chars().filter(ch -> ch == '1').count();
        enrollment.setAbsenceCount((int) absences);

        if (absences > 3) {
            enrollment.setIsBannedFromExam(true);
            enrollment.setAttendanceScore(0.0);
            enrollment.setFinalScore(0.0);
            enrollment.setTotalScore(0.0);
        } else {
            enrollment.setIsBannedFromExam(false);
            double calculatedAttendance = 10.0 - absences;
            enrollment.setAttendanceScore(calculatedAttendance);

            // Gán 0.0 nếu chưa nhập điểm để vẫn tính được tổng
            double midterm = enrollment.getMidtermScore() != null ? enrollment.getMidtermScore() : 0.0;
            double finalS = enrollment.getFinalScore() != null ? enrollment.getFinalScore() : 0.0;

            // Tính theo hệ 10 chuẩn: 20% Chuyên cần + 20% Giữa kỳ + 60% Cuối kỳ
            double total = (calculatedAttendance * 0.2) + (midterm * 0.2) + (finalS * 0.6);
            enrollment.setTotalScore((double) Math.round(total * 10) / 10);
        }
    }


    // --- API METHODS ---

    // LẤY BẢNG ĐIỂM CỦA MỘT SINH VIÊN
    public List<EnrollmentResponse> getStudentScores(String studentCode) {
        verifyStudentOwnership(studentCode);
        return enrollmentRepository.findByStudent_StudentCode(studentCode).stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    // GIẢNG VIÊN VÀO ĐIỂM
    @Transactional
    public String updateScore(String studentCode, String classCode, ScoreUpdateRequest request) {
        verifyTeacherOwnership(classCode);

        Enrollment enrollment = enrollmentRepository.findByStudent_StudentCodeAndCourseClass_ClassCode(studentCode, classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký của sinh viên này trong lớp học"));
// Kiểm tra xem lớp đã bị khóa điểm chưa
        verifyClassCanUpdateScore(enrollment.getCourseClass());
        enrollmentMapper.updateScoreFromRequest(request, enrollment);
        calculateAndSetScores(enrollment);
        enrollmentRepository.save(enrollment);

        return "Cập nhật điểm thành công! Điểm tổng kết: " + enrollment.getTotalScore();
    }

    // TẠO BUỔI HỌC
    @Transactional
    public String createNewLesson(String classCode) {
        verifyTeacherOwnership(classCode);
        List<Enrollment> enrollments = enrollmentRepository.findByCourseClass_ClassCode(classCode);
        if (enrollments.isEmpty()) {
            throw new RuntimeException("Lớp học chưa có sinh viên, không thể tạo buổi học!");
        }
// Kiểm tra xem lớp còn cho phép điểm danh không
        verifyClassCanUpdateAttendance(enrollments.get(0).getCourseClass());
        int newLessonNumber = 0;
        for (Enrollment en : enrollments) {
            String currentHistory = getSafeHistory(en);
            en.setAttendanceHistory(currentHistory + "0");
            newLessonNumber = en.getAttendanceHistory().length();
        }

        enrollmentRepository.saveAll(enrollments);

        CourseClass courseClass = courseClassRepository.findByClassCode(classCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lớp học phần"));
        // 3. LOGIC MỚI: CHỐT THỜI GIAN KHI TẠO BUỔI 1
        // ==========================================
        if (courseClass.getStartDate() == null) {
            LocalDate today = LocalDate.now();
            courseClass.setStartDate(today); // Ngày bắt đầu là hôm nay
            courseClass.setEndDate(today.plusMonths(4)); // Kết thúc sau 4 tháng
// 2. Chuyển trạng thái sang ĐANG DIỄN RA
            courseClass.setStatus(ClassStatus.ACTIVE);
            // Lưu cập nhật lại lớp học
            courseClassRepository.save(courseClass);
        }
        return "Tạo thành công buổi học thứ " + newLessonNumber + " cho lớp " + classCode;
    }

    // XÓA BUỔI HỌC
    @Transactional
    public String deleteLesson(String classCode, int lessonNumber) {
        verifyTeacherOwnership(classCode);
        List<Enrollment> enrollments = enrollmentRepository.findByCourseClass_ClassCode(classCode);
        if (enrollments.isEmpty()) {
            throw new RuntimeException("Lớp học chưa có sinh viên!");
        }

        int currentLessonCount = getSafeHistory(enrollments.get(0)).length();
        if (lessonNumber < 1 || lessonNumber > currentLessonCount) {
            throw new RuntimeException("Buổi học số " + lessonNumber + " không tồn tại!");
        }
// Kiểm tra xem lớp còn cho phép điểm danh không
        verifyClassCanUpdateAttendance(enrollments.get(0).getCourseClass());

        for (Enrollment enrollment : enrollments) {
            String history = getSafeHistory(enrollment);
            StringBuilder historyBuilder = new StringBuilder(history);
            historyBuilder.deleteCharAt(lessonNumber - 1);

            enrollment.setAttendanceHistory(historyBuilder.toString());
            calculateAndSetScores(enrollment);
        }

        enrollmentRepository.saveAll(enrollments);
        return "Đã xóa thành công buổi học số " + lessonNumber + ". Các buổi phía sau đã tự động dồn lên.";
    }

    // CẬP NHẬT ĐIỂM DANH 1 BẠN TRONG 1 BUỔI
    @Transactional
    public void updateWeekAttendance(Long enrollmentId, int weekNumber, boolean isAbsent) {
        Enrollment enrollments = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu đăng ký"));
// Kiểm tra xem lớp còn cho phép điểm danh không
        verifyClassCanUpdateAttendance(enrollments.getCourseClass());
        int totalCurrentLessons = getSafeHistory(enrollments).length();

        if (weekNumber < 1 || weekNumber > totalCurrentLessons) {
            throw new RuntimeException("Buổi học số " + weekNumber + " chưa được tạo!");
        }

        StringBuilder historyBuilder = new StringBuilder(getSafeHistory(enrollments));
        char statusChar = isAbsent ? '1' : '0';
        historyBuilder.setCharAt(weekNumber - 1, statusChar);

        enrollments.setAttendanceHistory(historyBuilder.toString());
        calculateAndSetScores(enrollments);
        enrollmentRepository.save(enrollments);
    }

    // NỘP ĐIỂM DANH CỦA CẢ LỚP 1 BUỔI
    @Transactional
    public void submitClassAttendance(AttendanceSubmitRequest request) {
        verifyTeacherOwnership(request.getClassCode());
        for (AttendanceSubmitRequest.StudentAttendanceState state : request.getAttendanceList()) {
            Enrollment en = enrollmentRepository
                    .findByStudent_StudentCodeAndCourseClass_ClassCode(state.getStudentCode(), request.getClassCode())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

            updateWeekAttendance(en.getId(), request.getWeekNumber(), state.getIsAbsent());
        }
    }


    // LẤY TRẠNG THÁI ĐIỂM DANH CỦA CẢ LỚP (TỪNG BUỔI - Chỉ đọc, không cần chặn)
    public AttendanceWeekResponse getAttendanceByWeek(String classCode, int weekNumber) {

        List<Enrollment> enrollments = enrollmentRepository.findByCourseClass_ClassCode(classCode);
        if (enrollments.isEmpty()) throw new RuntimeException("Lớp trống");



        // 2. [BẢO MẬT] Kiểm tra quyền truy cập thay vì dùng verifyTeacherOwnership
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isStudent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        if (!isAdmin) {
            if (isTeacher) {
                // Tái sử dụng hàm cũ để check GV
                verifyTeacherOwnership(classCode);
            } else if (isStudent) {
                // Nếu là SV: Check xem có tên trong sổ điểm danh này không
                boolean isEnrolled = enrollments.stream()
                        .anyMatch(e -> e.getStudent().getStudentCode().equals(currentUsername));
                if (!isEnrolled) {
                    throw new RuntimeException("Truy cập bị từ chối: Bạn không có tên trong lớp này!");
                }
            } else {
                throw new RuntimeException("Truy cập bị từ chối!");
            }
        }
        int totalCurrentLessons = getSafeHistory(enrollments.get(0)).length();
        if (weekNumber < 1 || weekNumber > totalCurrentLessons) {
            throw new RuntimeException("Buổi học số " + weekNumber + " chưa được tạo!");
        }

        List<AttendanceWeekResponse.StudentAttendanceInfo> students = enrollments.stream().map(en -> {
            String history = getSafeHistory(en);
            char statusChar = history.charAt(weekNumber - 1);
            boolean isAbsentThisWeek = (statusChar == '1');

            return AttendanceWeekResponse.StudentAttendanceInfo.builder()
                    .studentCode(en.getStudent().getStudentCode())
                    .fullName(en.getStudent().getFullName())
                    .isAbsentThisWeek(isAbsentThisWeek)
                    .totalAbsences(en.getAbsenceCount())
                    .isBanned(en.getIsBannedFromExam())
                    .build();
        }).toList();

        return AttendanceWeekResponse.builder()
                .classCode(classCode)
                .weekNumber(weekNumber)
                .students(students)
                .build();
    }

    // LẤY DANH SÁCH LỚP HỌC & ĐIỂM CỦA SINH VIÊN (1 HỌC KỲ)
    public List<MyClassResponse> getMyClassesInSemester(String studentCode, String semester, Integer year) {
        verifyStudentOwnership(studentCode);
        List<Enrollment> enrollments = enrollmentRepository
                .findByStudent_StudentCodeAndCourseClass_SemesterAndCourseClass_Year(studentCode, semester, year);

        return enrollments.stream().map(en -> MyClassResponse.builder()
                .classCode(en.getCourseClass().getClassCode())
                .subjectName(en.getCourseClass().getSubject().getSubjectName())
                .teacherName(en.getCourseClass().getTeacher() != null ? en.getCourseClass().getTeacher().getFullName() : "Chưa có GV")
                .room(en.getCourseClass().getRoom())
                .attendanceScore(en.getAttendanceScore())
                .midtermScore(en.getMidtermScore())
                .finalScore(en.getFinalScore())
                .totalScore(en.getTotalScore())
                .absenceCount(en.getAbsenceCount())
                .isBannedFromExam(en.getIsBannedFromExam())
                .attendanceHistory(getSafeHistory(en))
                .build()
        ).toList();
    }
}