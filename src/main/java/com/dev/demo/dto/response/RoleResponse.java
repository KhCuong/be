package com.dev.demo.dto.response;

import com.dev.demo.entity.Permission;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    @Id
    private String name;
    private String description;
    Set<PermissionResponse> permissions;
}
