package com.dev.demo.service;

import com.dev.demo.dto.request.RoleRequest;
import com.dev.demo.dto.response.RoleResponse;
import com.dev.demo.entity.Permission;
import com.dev.demo.entity.Role;
import com.dev.demo.mapper.RoleMapper;
import com.dev.demo.repository.PermissionRepository;
import com.dev.demo.repository.RoleRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        // Không cho phép tạo Role trùng tên nếu đã tồn tại
        if (roleRepository.existsById(request.getName())) {
            throw new RuntimeException("Vai trò (Role) này đã tồn tại!");
        }
        Role role = roleMapper.toRole(request);
       List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());
       role.setPermissions(new HashSet<>(permissions));

       return roleMapper.toRoleResponse(roleRepository.save(role));
    }
    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream().map(roleMapper :: toRoleResponse).toList();
    }

    public RoleResponse update(String roleId, RoleRequest request) {

        //  Không ai được phép sửa Role ADMIN qua API này
        if ("ADMIN".equalsIgnoreCase(roleId)) {
            throw new RuntimeException("Tuyệt đối không được phép chỉnh sửa vai trò ADMIN gốc!");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Vai trò (Role) này"));

        // Dùng mapper để đè thông tin mới (ví dụ description)
        // roleMapper.updateRole(role, request); // Nếu bạn có hàm update trong mapper

        // Lấy danh sách Permission mới từ DB dựa trên list ID gửi lên
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissions());

        // Cập nhật lại danh sách quyền cho Role này
        role.setPermissions(new HashSet<>(permissions));

        return roleMapper.toRoleResponse(roleRepository.save(role));
    }
    public void delete(String role) {
        // Bảo vệ các Role sinh mệnh của hệ thống
        List<String> protectedRoles = List.of("ADMIN", "TEACHER", "STUDENT");
        if (protectedRoles.contains(role.toUpperCase())) {
            throw new RuntimeException("Tuyệt đối không được phép xóa vai trò mặc định của hệ thống!");
        }

        // [CHẶN 3]: (Tùy chọn) Kiểm tra xem Role này có đang được gán cho User nào không?
        // Nếu xóa Role mà vẫn còn người đang giữ Role đó thì DB có thể báo lỗi Khóa ngoại (Foreign Key)
        // Lời khuyên: Nên gọi UserRepository để check xem có user nào đang giữ
        roleRepository.deleteById(role);
    }
}
