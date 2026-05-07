package com.dev.demo.repository;
import com.dev.demo.entity.CourseClass;
import com.dev.demo.enums.ClassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseClassRepository extends JpaRepository<CourseClass, Long> {
    boolean existsByClassCode(String classCode);
    Optional<CourseClass> findByClassCode(String classCode);

    // BỔ SUNG 2: Dùng cho Admin (Bảo vệ toàn vẹn dữ liệu)
    // ====================================================================
    // Nhiệm vụ: Kiểm tra xem một môn học có đang được dùng để mở lớp nào không
    // Nơi gọi: SubjectService (Hàm deleteSubject - Chặn xóa môn học nếu đang có lớp)
    // LƯU Ý: Nếu ID của bảng Subject là kiểu String (vd: subjectCode) thì bạn đổi Long thành String nhé.
    boolean existsBySubject_Id(Long subjectId);

    // 1. Tìm lớp do giảng viên dạy (Dựa vào username - mã sv (gv)  )
    @Query("SELECT c FROM CourseClass c WHERE c.teacher.user.username = :username")
    List<CourseClass> findByTeacherUsername(@Param("username") String username);

    // 2. Tìm lớp mà sinh viên đang học (Thông qua bảng Enrollment)
    @Query("SELECT e.courseClass FROM Enrollment e WHERE e.student.user.username = :username")
    List<CourseClass> findByStudentUsername(@Param("username") String username);

    // Tìm các lớp theo trạng thái (Ví dụ: Lấy tất cả lớp ACTIVE để điểm danh)
    List<CourseClass> findByStatus(ClassStatus status);
    @Query("SELECT c FROM CourseClass c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.room) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<CourseClass> searchClasses(@Param("keyword") String keyword,
                                    @Param("status") ClassStatus status,
                                    Pageable pageable);

    // 1. Dành cho ADMIN (Tìm trên toàn trường)
    @Query("SELECT c FROM CourseClass c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.room) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<CourseClass> searchAllClasses(@Param("keyword") String keyword,
                                       @Param("status") ClassStatus status,
                                       Pageable pageable);

    // 2. Cho GIẢNG VIÊN (Chỉ tìm lớp mình dạy)
    @Query("SELECT c FROM CourseClass c WHERE " +
            "c.teacher.teacherCode = :teacherCode AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<CourseClass> searchByTeacher(@Param("keyword") String keyword,
                                      @Param("teacherCode") String teacherCode,
                                      @Param("status") ClassStatus status,
                                      Pageable pageable);

    // 3. Cho SINH VIÊN (Chỉ tìm lớp mình có đăng ký học)
    // 3. Cho SINH VIÊN (Chỉ tìm lớp mình có đăng ký học)
    @Query("SELECT c FROM CourseClass c WHERE " +
            "EXISTS (SELECT 1 FROM Enrollment e WHERE e.courseClass = c AND e.student.studentCode = :studentCode) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR c.status = :status)")
    Page<CourseClass> searchByStudent(@Param("keyword") String keyword,
                                      @Param("studentCode") String studentCode,
                                      @Param("status") ClassStatus status,
                                      Pageable pageable);
}