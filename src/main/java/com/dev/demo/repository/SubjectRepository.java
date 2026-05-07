package com.dev.demo.repository;
import com.dev.demo.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    @Query("SELECT s.subjectCode FROM Subject s")
    Set<String> findAllSubjectCodes();
    boolean existsBySubjectCode(String subjectCode);
    Optional<Subject> findBySubjectCode(String subjectCode);

    @Query("SELECT s FROM Subject s WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Subject> searchSubjects(@Param("keyword") String keyword, Pageable pageable);
}