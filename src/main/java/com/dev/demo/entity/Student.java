package com.dev.demo.entity;

import com.dev.demo.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students") // Nên đặt tên bảng rõ ràng (số nhiều)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Chuyển từ 'long' sang 'Long' (Wrapper class) để tránh lỗi giá trị mặc định là
                     // 0 trước khi lưu

    @Column(name = "student_code", unique = true, nullable = false, length = 20)
    private String studentCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING) // QUAN TRỌNG: Sẽ lưu vào DB dưới dạng chữ "MALE", "FEMALE" thay vì số 0, 1
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

    @Column(name = "class_code", nullable = false, length = 20)
    private String classCode;

    @Column(name = "cohort", length = 20)
    private String cohort;

    @Column(name = "major", length = 100)
    private String major;

    // Liên kết 1-1 với bảng User
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    private Set<CourseClass> courseClasses = new HashSet<>();
}