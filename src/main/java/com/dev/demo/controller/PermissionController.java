package com.dev.demo.controller;


import com.dev.demo.dto.request.PermissionRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.PermissionResponse;
import com.dev.demo.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@Slf4j
@PreAuthorize("hasAuthority('SYSTEM_ROLE_MANAGE')")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

//    @PostMapping
//    public ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
//        ApiResponse<PermissionResponse> response = new ApiResponse<>();
//        response.setResult(permissionService.create(request));
//        return response;
//    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAll() {
        ApiResponse<List<PermissionResponse>> response = new ApiResponse<>();
        response.setResult(permissionService.getAll());
        return response;
    }

//    @DeleteMapping("/{permission}")
//    // Permission đang đc map với Role <-> có 1 Role đang có mối qhe với Permission nên ko thể xóa đc.
//    // Xóa đc khi ko có Role nào đang map với cái Permission đó
//    public ApiResponse<Void> delete(@PathVariable String permission) {
//        ApiResponse<Void> response = new ApiResponse<>();
//        permissionService.delete(permission);
//        return response;
//    }
}
