package com.dev.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;


/*
        Class Java        ↔        Table trong Database
        Biến (field)      ↔        Cột (column)
        Object            ↔        Dòng dữ liệu (row)
*/
@Entity // báo hiệu(hay gọi là nhãn - annotation) map(ánh xạ) class với table trong DB.
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
    private String username; // Tên đăng nhập (có thể dùng Mã SV/Mã GV)
    private String password; // Mật khẩu (sẽ được mã hóa bcrypt ở thực tế)

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles; // Ví dụ: ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN
//    private LocalDate doString;

    // private boolean isActive = true; // Trạng thái tài khoản





//    public LocalDate getDob() {
//        return dob;
//    }
//
//    public void setDob(LocalDate dob) {
//        this.dob = dob;
//    }
}
