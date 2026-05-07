package com.dev.demo.repository;

import com.dev.demo.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface StudentRepository extends JpaRepository<Student, String> {
    @Query("SELECT s.studentCode FROM Student s")
    Set<String> findAllStudentCodes();

    boolean existsByStudentCode(String studentCode);
    boolean existsByEmail(String email);
    Optional<Student> findByStudentCode(String studentCode);
    // Hàm tìm kiếm theo Mã SV hoặc Tên (không phân biệt hoa thường) và có phân trang
//    Page<Student> findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
//            String studentCode, String fullName, Pageable pageable);

// Hàm tìm kiếm đa năng có phân trang
    @Query("SELECT s FROM Student s WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);
}
