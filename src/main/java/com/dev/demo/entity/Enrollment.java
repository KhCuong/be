package com.dev.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "course_class_id"})
                // Đảm bảo 1 sinh viên chỉ đăng ký 1 lớp học phần đúng 1 lần
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment { // Đăng ký

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N-1: Nhiều bản ghi đăng ký thuộc về 1 Sinh viên
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // N-1: Nhiều bản ghi đăng ký thuộc về 1 Lớp học phần
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_class_id", nullable = false)
    private CourseClass courseClass;

    // --- QUẢN LÝ ĐIỂM SỐ (Dùng Double hoặc Float để chứa số thập phân, cho phép null khi chưa có điểm) ---
    @Column(name = "attendance_score")
    private Double attendanceScore; // Điểm chuyên cần (10%)

    @Column(name = "midterm_score")
    private Double midtermScore; // Điểm giữa kỳ (30%)

    @Column(name = "final_score")
    private Double finalScore; // Điểm cuối kỳ (60%)

    @Column(name = "total_score")
    private Double totalScore; // Điểm tổng kết (Tự động tính)

    // --- CHỨC NĂNG ĐIỂM DANH ---
    @Column(name = "absence_count", nullable = false)
    @Builder.Default
    private Integer absenceCount = 0; // Đếm số buổi vắng mặt

    // Trạng thái Cấm thi : absenceCount > 3
    @Column(name = "is_banned_from_exam", nullable = false)
    @Builder.Default
    private Boolean isBannedFromExam = false;

    @Column(name = "attendance_history", nullable = false)
    @Builder.Default
    private String attendanceHistory = "";
}