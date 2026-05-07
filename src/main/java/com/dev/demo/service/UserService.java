package com.dev.demo.service;

import com.dev.demo.dto.request.UserCreationRequest;
import com.dev.demo.dto.request.UserUpdateRequest;
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.UserResponse;
import com.dev.demo.entity.Role;
import com.dev.demo.entity.User;

import com.dev.demo.exception.AppException;
import com.dev.demo.exception.ErrorCode;
import com.dev.demo.mapper.UserMapper;
import com.dev.demo.repository.RoleRepository;
import com.dev.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Tâng nghiệp vụ
@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Transactional
    public UserResponse createUser(UserCreationRequest request) {

        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // XỬ LÝ ROLE LINH HOẠT
        Set<String> requestedRoles = request.getRoles();

        if (requestedRoles != null && !requestedRoles.isEmpty()) {

            // 1. Dùng trực tiếp requestedRoles (Vì DTO đã là Set nên tự động không có trùng lặp)
            var roles = roleRepository.findAllById(requestedRoles);

            // 2. So sánh size để phát hiện có kẻ truyền mã Role ảo không có thật dưới DB
            if (roles.size() != requestedRoles.size()) {
                throw new AppException(ErrorCode.ROLE_NOT_EXISTED);
            }

            user.setRoles(new HashSet<>(roles));

        } else {
            // 3. Fallback: Không gửi Role thì mặc định cho làm học sinh
            Role defaultRole = roleRepository.findById("STUDENT")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

            user.setRoles(new HashSet<>(Set.of(defaultRole)));
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }
    public User createDefaultStudentAccount(String studentCode) {
        Role studentRole = roleRepository.findByName("STUDENT").orElseThrow(()
                -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        return userRepository.save(User.builder()
                .username(studentCode)
                .password(passwordEncoder.encode(studentCode))
                 .roles(new HashSet<>(Set.of(studentRole))) // PHÂN QUYỀN
                .build());
    }

    public User createDefaultTeacherAccount(String teacherCode) {
        Role teacherRole = roleRepository.findByName("TEACHER").orElseThrow(()
                -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        return userRepository.save(User.builder()
                .username(teacherCode)
                .password(passwordEncoder.encode(teacherCode))
                .roles(new HashSet<>(Set.of(teacherRole))) // PHÂN QUYỀN
                .build());
    }

//    @PreAuthorize("hasRole('ADMIN')") // Kiểm tra quyền xog ms chạy vào method (phân quyền chặt hơn)
//    @PreAuthorize("hasAuthority('CREATE_USER')") //cấp quyền theo permission

    @PreAuthorize("hasAuthority('SYSTEM_USER_MANAGE')")
    public List<UserResponse> getUsers() {
        log.info("In method getUser()");
        return userRepository.findAll().stream().map(userMapper :: toUserResponse).toList();
    }

//    @PostAuthorize("hasRole('ADMIN')") // Chạy xog method , nếu ko thỏa đk mới chặn.
//    @PostAuthorize("returnObject.username == authentication.name")
    // Kiểm tra xem UserResponse.username có == username(authentication) đang đăng nhập ko.
    // Ý nghĩa : chỉ lấy đc thông tin của chính mình


    @PreAuthorize("hasAuthority('SYSTEM_USER_MANAGE')")
    public UserResponse getUser(String id) {
        log.info("In method getUser by id");
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("user not found"))) ;
    }
    @Transactional
    @PreAuthorize("hasAuthority('SYSTEM_USER_MANAGE')")
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("user not found"));
//        user.setFullName(request.getPassword());
//        user.setStudentCode(request.getUsername());
//        user.setFirstName(request.getFirstName());
//        user.setEmail(request.getLastName());
//        user.setDob(request.getDob());
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Tuyệt đối không được phép chỉnh sửa tài khoản Quản trị viên gốc!");
        }
        userMapper.updateUser(user, request);
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        List<Role> roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    @PreAuthorize("hasAuthority('SYSTEM_USER_MANAGE')")
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("user not found"));
        // CHẶN BẢO VỆ ADMIN
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new RuntimeException("Tuyệt đối không được xóa tài khoản Quản trị viên gốc!");
        }
        userRepository.delete(user);
    }

    @PreAuthorize("hasAuthority('SYSTEM_USER_MANAGE')")
    public PageResponse<UserResponse> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("username").ascending());
        Page<User> userPage = userRepository.searchUsers(keyword, pageable);

        return PageResponse.<UserResponse>builder()
                .currentPage(page)
                .totalPages(userPage.getTotalPages())
                .pageSize(size)
                .totalElements(userPage.getTotalElements())
                .data(userMapper.toUserResponseList(userPage.getContent()))
                .build();
    }
}
