package com.dev.demo.dto.response;

import com.dev.demo.enums.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {

    private Long id;


    private String studentCode;


    private String fullName;


    private Gender gender;

    private LocalDate dateOfBirth;


    private String citizenId;


    private String address;


    private String phoneNumber;


    private String email;


    private String classCode;


    private String cohort; // Ví dụ: "K64", "2023-2027"

    private String major; // Ví dụ: "Công nghệ thông tin", "Kinh tế"
}