package com.dev.demo.dto.request;

import com.dev.demo.enums.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherUpdateRequest {
    @NotBlank(message = "Mã giảng viên không được để trống")
    @Size(min = 5, max = 20, message = "Mã giảng viên từ 5-20 ký tự")

    private String teacherCode;

    @NotBlank(message = "Tên giảng viên không được để trống")
    @Size(min = 2, max = 100, message = "Tên giảng viên từ 2-100 ký tự")

    private String fullName;

    @NotNull(message = "Giới tính không được để trống")

    private Gender gender;

    @PastOrPresent(message = "Ngày sinh không được ở tương lai")

    private LocalDate dateOfBirth;

    @Pattern(regexp = "^\\d{12}$", message = "CCCD phải là 12 số")

    private String citizenId;

    @Size(max = 255, message = "Địa chỉ không vượt quá 255 ký tự")

    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[35789]\\d{8}$", message = "Số điện thoại không hợp lệ")

    private String phoneNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không vượt quá 100 ký tự")

    private String email;

    @NotBlank(message = "Khoa không được để trống")
    @Size(max = 100, message = "Khoa không vượt quá 100 ký tự")

    private String department;

    @NotBlank(message = "Chuyên ngành không được để trống")
    @Size(max = 100, message = "Chuyên ngành không vượt quá 100 ký tự")

    private String specialization;
}
