package com.dev.demo.dto.request;

import com.dev.demo.entity.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

/*
    Đóng gói và vận chuyển dữ liệu giữa client và server hoặc giữa các tầng với nhau.
    Nó giúp ẩn đi cấu trúc thực sự của database (entity) và
    chỉ trả về/nhận vào những dữ liệu thực sự cần thiết
*/
@Data
public class UserCreationRequest {

    @Size(min = 3, message = "USERNAME_INVALID") // Sửa lại lỗi chính tả INVALD -> INVALID cho chuẩn nhé
    private String username;

    @Size(min = 8, message = "PASSWORD_INVALID")
    private String password;

    // THÊM TRƯỜNG NÀY: Hứng danh sách ID của các Role (VD: ["ADMIN", "TEACHER"])
    private Set<String> roles;

    // ==========================================
    // GETTER & SETTER
    // ==========================================


}