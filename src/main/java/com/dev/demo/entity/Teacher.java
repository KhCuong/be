package com.dev.demo.entity;

import com.dev.demo.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_code", unique = true, nullable = false, length = 20)
    private String teacherCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "citizen_id", unique = true, length = 12)
    private String citizenId;

    @Column(length = 255)
    private String address;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "department", length = 100)
    private String department; // Ví dụ: "Khoa Công nghệ thông tin"

    @Column(name = "specialization", length = 100)
    private String specialization; // Ví dụ: "Khoa học máy tính", "Hệ thống thông tin"

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // 1-N: 1 Giảng viên có thể dạy nhiều Lớp học phần
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private Set<CourseClass> courseClasses = new HashSet<>();
}