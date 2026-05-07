package com.dev.demo.controller;

import com.dev.demo.dto.request.RoleRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.response.RoleResponse;
import com.dev.demo.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@Slf4j
@PreAuthorize("hasAuthority('SYSTEM_ROLE_MANAGE')")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PostMapping
    public ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        ApiResponse<RoleResponse> response = new ApiResponse<>();
        response.setResult(roleService.create(request));
        return response;
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll() {
        ApiResponse<List<RoleResponse>> response = new ApiResponse<>();
        response.setResult(roleService.getAll());
        return response;
    }
    @PutMapping("/{role}")
    public ApiResponse<RoleResponse> update(@PathVariable String role, @RequestBody RoleRequest request) {
        ApiResponse<RoleResponse> response = new ApiResponse<>();
        response.setResult(roleService.update(role, request));
        return response;
    }
    @DeleteMapping("/{role}")
    public ApiResponse<Void> delete(@PathVariable String role) {
        ApiResponse<Void> response = new ApiResponse<>();
        roleService.delete(role);
        return response;
    }
}

