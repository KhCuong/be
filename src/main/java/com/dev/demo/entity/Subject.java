package com.dev.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject { // Môn học : Lập trình java , mã môn học: MAT2505

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", unique = true, nullable = false, length = 20)
    private String subjectCode; // Ví dụ: MAT2505

    @Column(name = "subject_name",nullable = false, length = 150)
    private String subjectName; // Ví dụ: Cơ sở dữ liệu

    @Column(nullable = false)
    private Integer credits; // Số tín chỉ (Ví dụ: 3)
}