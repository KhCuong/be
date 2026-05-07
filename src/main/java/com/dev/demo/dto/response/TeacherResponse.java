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
public class TeacherResponse {

    private Long id;


    private String teacherCode;


    private String fullName;


    private Gender gender;


    private LocalDate dateOfBirth;


    private String citizenId;


    private String address;


    private String phoneNumber;


    private String email;


    private String department;


    private String specialization;
}