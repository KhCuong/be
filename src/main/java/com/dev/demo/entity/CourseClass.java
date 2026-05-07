package com.dev.demo.entity;

import com.dev.demo.enums.ClassStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "course_classes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseClass { // Lớp học phần VD : MAT2505 3

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_code", unique = true, nullable = false, length = 50)
    private String classCode; // Ví dụ: MAT2505 1,  MAT2505 2,  MAT2505 3, ...

    @Column(nullable = false)
    private String semester; // Học kỳ (Ví dụ: "HK1", "HK2")

    @Column(nullable = false)
    private Integer year; // Năm học (Ví dụ: 2024)

    @Column(length = 50)
    private String room; // Phòng học (Ví dụ: "Phòng 302-A2")

    // N-1: Nhiều Lớp học phần thuộc về 1 Môn học
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // N-1: Nhiều Lớp học phần do 1 Giảng viên dạy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    // N-N: 1 Lớp học phần có nhiều Sinh viên, 1 Sinh viên học nhiều Lớp học phần
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "student_course_class", // Tên bảng trung gian sẽ được tự động tạo trong DB
            joinColumns = @JoinColumn(name = "course_class_id"), // Khóa ngoại trỏ về bảng course_classes
            inverseJoinColumns = @JoinColumn(name = "student_id") // Khóa ngoại trỏ về bảng students
    )
    private Set<Student> students = new HashSet<>();


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClassStatus status = ClassStatus.ACTIVE;

    @Column(name = "start_date")
    private LocalDate startDate; // Ngày bắt đầu học

    @Column(name = "end_date")
    private LocalDate endDate;   // Ngày kết thúc học
}