package com.dev.demo.service;

import com.dev.demo.dto.request.PermissionRequest;
import com.dev.demo.dto.response.PermissionResponse;
import com.dev.demo.entity.Permission;
import com.dev.demo.mapper.PermissionMapper;
import com.dev.demo.repository.PermissionRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class PermissionService {
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private PermissionMapper permissionMapper;
//    public PermissionResponse create(PermissionRequest request) {
//        Permission permission = permissionMapper.toPermission(request);
//        permission = permissionRepository.save(permission);
//        return permissionMapper.toPermissionResponse(permission);
//    }
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll()
                .stream().map(permissionMapper :: toPermissionResponse).toList();
    }
//    public void delete(String permission) {
//        // Kiểm tra xem permission có tồn tại không trước khi xóa
//        if (!permissionRepository.existsById(permission)) {
//            throw new RuntimeException("Không tìm thấy quyền này để xóa!");
//        }
//        permissionRepository.deleteById(permission);
//    }
}
