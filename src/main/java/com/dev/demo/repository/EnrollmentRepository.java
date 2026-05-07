package com.dev.demo.repository;
import com.dev.demo.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    // Tránh 1 sinh viên đăng ký 1 lớp 2 lần
    boolean existsByStudentIdAndCourseClassId(Long studentId, Long courseClassId);

    // Tìm bản ghi điểm của 1 sinh viên trong 1 lớp
    Optional<Enrollment> findByStudent_StudentCodeAndCourseClass_ClassCode(String studentCode, String classCode);

    // Lấy bảng điểm của 1 sinh viên (để Sinh viên tự xem)
    List<Enrollment> findByStudent_StudentCode(String studentCode);

    void deleteByCourseClass_Id(Long id);
    void deleteByStudent_Id(Long studentId);

    // Lấy danh sách sinh viên của 1 lớp học phần (để Giảng viên xem và nhập điểm)
    List<Enrollment> findByCourseClass_Id(Long classId);
    Optional<Enrollment> findByStudentIdAndCourseClassId(Long studentId, Long courseClassId);
    List<Enrollment> findByCourseClass_ClassCode(String classCode);
    // Tìm các lớp sinh viên đang học theo Mã SV + Học kỳ + Năm học
    List<Enrollment> findByStudent_StudentCodeAndCourseClass_SemesterAndCourseClass_Year(
            String studentCode, String semester, Integer year);
}