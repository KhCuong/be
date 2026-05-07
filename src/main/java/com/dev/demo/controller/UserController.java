package com.dev.demo.controller;

import com.dev.demo.dto.request.UserCreationRequest;
import com.dev.demo.dto.request.UserUpdateRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.PageResponse;
import com.dev.demo.dto.response.UserResponse;
import com.dev.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Tầng giao diện / API
@RestController
@RequestMapping("/users")
@Slf4j
// Đặt ổ khóa ngay trên đầu Class: Chỉ Admin mới được quản lý Tài khoản & cấp Role
@PreAuthorize("hasAuthority('SYSTEM_USER_MANAGE')")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {

        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.createUser(request));

        return response;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("username: {}", authentication.getName());
        authentication.getAuthorities()
                .forEach(e -> log.info(e.getAuthority()));

        List<UserResponse> users = userService.getUsers();
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(users);
        return response;
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.getUser(userId));
        return response;
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody UserUpdateRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.updateUser(userId, request));
        return response;
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return "Xoa user thanh cong";
    }


    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SYSTEM_USER_MANAGE')")
    public ApiResponse<PageResponse<UserResponse>> searchUsers(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.searchUsers(keyword, page, size))
                .message("Tra cứu danh sách tài khoản thành công")
                .build();
    }
}
