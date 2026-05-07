package com.dev.demo.config;

import com.dev.demo.entity.Permission;
import com.dev.demo.entity.Role;
import com.dev.demo.entity.User;
import com.dev.demo.repository.PermissionRepository;
import com.dev.demo.repository.RoleRepository;
import com.dev.demo.repository.UserRepository;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Tạo User - Admin khi start
@Configuration
@Slf4j
public class ApplicationInitConfig {
    @NonFinal
    static final String ADMIN_USER_NAME = "admin";
    @NonFinal
    static final String ADMIN_PASSWORD = "admin";
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        PermissionRepository permissionRepository) {
        return args -> {
            // 1. TẠO CỨNG PERMISSIONS (Chỉ tạo nếu DB đang trống)
            if (permissionRepository.count() == 0) {
                log.info("Bắt đầu khởi tạo các Permission cứng ...");
                List<Permission> permissionsToSeed = List.of(
                        // ==========================================
                        // NHÓM 1: QUẢN LÝ MÔN HỌC (Subjects)
                        // ==========================================
                        Permission.builder().name("SUBJECT_VIEW").description("Xem danh sách và chi tiết môn học").build(),
                        Permission.builder().name("SUBJECT_CREATE").description("Thêm mới và Import môn học").build(),
                        Permission.builder().name("SUBJECT_UPDATE").description("Sửa thông tin môn học").build(),
                        Permission.builder().name("SUBJECT_DELETE").description("Xóa môn học").build(),

                        // ==========================================
                        // NHÓM 2: QUẢN LÝ LỚP HỌC PHẦN (Course Classes)
                        // ==========================================
                        Permission.builder().name("CLASS_VIEW").description("Xem danh sách và chi tiết lớp học phần").build(),
                        Permission.builder().name("CLASS_CREATE").description("Tạo lớp học phần mới").build(),
                        Permission.builder().name("CLASS_UPDATE").description("Sửa thông tin lớp học phần").build(),
                        Permission.builder().name("CLASS_DELETE").description("Xóa lớp học phần").build(),
                        Permission.builder().name("CLASS_STUDENT_MANAGE").description("Thêm/Xóa/Import sinh viên vào lớp học phần").build(),

                        // ==========================================
                        // NHÓM 3: QUẢN LÝ SINH VIÊN (Students)
                        // ==========================================
                        Permission.builder().name("STUDENT_VIEW").description("Xem danh sách và hồ sơ sinh viên").build(),
                        Permission.builder().name("STUDENT_CREATE").description("Thêm mới và Import sinh viên").build(),
                        Permission.builder().name("STUDENT_UPDATE").description("Cập nhật hồ sơ sinh viên").build(),
                        Permission.builder().name("STUDENT_DELETE").description("Xóa hồ sơ sinh viên").build(),

                        // ==========================================
                        // NHÓM 4: QUẢN LÝ GIẢNG VIÊN (Teachers)
                        // ==========================================
                        Permission.builder().name("TEACHER_VIEW").description("Xem danh sách và hồ sơ giảng viên").build(),
                        Permission.builder().name("TEACHER_CREATE").description("Thêm mới và Import giảng viên").build(),
                        Permission.builder().name("TEACHER_UPDATE").description("Cập nhật hồ sơ giảng viên").build(),
                        Permission.builder().name("TEACHER_DELETE").description("Xóa hồ sơ giảng viên").build(),

                        // ==========================================
                        // NHÓM 5: HỌC TẬP (Điểm & Điểm danh - Enrollments)
                        // ==========================================
                        Permission.builder().name("SCORE_VIEW").description("Tra cứu điểm số của sinh viên").build(),
                        Permission.builder().name("SCORE_UPDATE").description("Vào điểm, sửa điểm cho sinh viên").build(),
                        Permission.builder().name("ATTENDANCE_MANAGE").description("Tạo buổi học, điểm danh, xóa buổi học").build(),

                        // ==========================================
                        // NHÓM 6: HỆ THỐNG (Roles) - Dành riêng cho Admin
                        // ==========================================
                        Permission.builder().name("SYSTEM_ROLE_MANAGE").description("Quản lý cấu trúc Vai trò (Roles)").build(),
                        Permission.builder().name("SYSTEM_USER_MANAGE").description("Cấp phát/Thu hồi Vai trò của User").build()
                );

                // Lưu toàn bộ vào Database
                permissionRepository.saveAll(permissionsToSeed);

                // 2. TẠO CÁC ROLE MẶC ĐỊNH (ADMIN, TEACHER, STUDENT)
                // ======================================================
                if (roleRepository.count() == 0) {
                    log.info("Bắt đầu khởi tạo các Role mặc định ...");

                    // ROLE 1: ADMIN
                    Role adminRole = Role.builder()
                            .name("ADMIN")
                            .description("Quản trị viên hệ thống")
                            .permissions(new HashSet<>(permissionRepository.findAll()))
                            .build();

                    // ROLE 2: TEACHER - Chỉ chọn các quyền liên quan đến Giảng dạy
                    Role teacherRole = Role.builder()
                            .name("TEACHER")
                            .description("Giảng viên")
                            .permissions(new HashSet<>(permissionRepository.findAllById(List.of(
                                    "CLASS_VIEW", "SUBJECT_VIEW",
                                    "SCORE_VIEW", "SCORE_UPDATE", "ATTENDANCE_MANAGE"
                            ))))
                            .build();

                    // ROLE 3: STUDENT - Chỉ xem được Lớp và Môn học (Điểm cá nhân thì dùng API riêng không check quyền này)
                    Role studentRole = Role.builder()
                            .name("STUDENT")
                            .description("Sinh viên")
                            .permissions(new HashSet<>(permissionRepository.findAllById(List.of(
                                    "CLASS_VIEW", "SUBJECT_VIEW" , "SCORE_VIEW"
                            ))))
                            .build();

                    roleRepository.saveAll(List.of(adminRole, teacherRole, studentRole));
                }



                if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {

                    // Lấy Role Admin từ DB lên để gán
                    Role adminRole = roleRepository.findById("ADMIN").orElseThrow();

                    var roles = new HashSet<Role>();
                    roles.add(adminRole);

                    User user = User.builder()
                            .username(ADMIN_USER_NAME)
                            .password(passwordEncoder.encode(ADMIN_PASSWORD))
                            .roles(roles)
                            .build();

                    userRepository.save(user);
                }
            }
        };
    }
}

