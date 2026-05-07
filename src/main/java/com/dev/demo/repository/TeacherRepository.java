package com.dev.demo.repository;

import com.dev.demo.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    @Query("SELECT t.teacherCode FROM Teacher t")
    Set<String> findAllTeacherCodes();

    boolean existsByTeacherCode(String teacherCode);
    boolean existsByEmail(String email);
    Optional<Teacher> findByTeacherCode(String teacherCode);
    // Trong TeacherRepository.java
    @Query("SELECT t FROM Teacher t WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.teacherCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Teacher> searchTeachers(@Param("keyword") String keyword, Pageable pageable);
}