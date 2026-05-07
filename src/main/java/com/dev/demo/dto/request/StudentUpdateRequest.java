package com.dev.demo.dto.request;

import com.dev.demo.enums.Gender;
import jakarta.validation.constraints.*;
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
public class StudentUpdateRequest {

    @NotBlank(message = "Tên sinh viên không được để trống")
    @Size(min = 2, max = 100, message = "Tên phải từ 2-100 ký tự")

    private String fullName;

    @NotNull(message = "Giới tính không được để trống")

    private Gender gender;

    @PastOrPresent(message = "Ngày sinh không được ở tương lai")

    private LocalDate dateOfBirth;

    @Pattern(regexp = "^\\d{12}$", message = "CCCD phải là 12 số")

    private String citizenId;

    @Size(max = 255, message = "Địa chỉ không vượt quá 255 ký tự")

    private String address;

    @Pattern(regexp = "^0[35789]\\d{8}$", message = "Số điện thoại không hợp lệ")

    private String phoneNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")

    private String email;

    @NotBlank(message = "Mã lớp không được để trống")

    private String classCode;

    @NotBlank(message = "Khóa học không được để trống")
    @Size(max = 20, message = "Khóa học không vượt quá 20 ký tự")

    private String cohort; // Ví dụ: "K64", "2023-2027"

    @NotBlank(message = "Ngành học không được để trống")
    @Size(max = 100, message = "Ngành học không vượt quá 100 ký tự")

    private String major; // Ví dụ: "Công nghệ thông tin", "Kinh tế"

}