package com.dev.demo.mapper;

import com.dev.demo.dto.request.RoleRequest;
import com.dev.demo.dto.response.RoleResponse;
import com.dev.demo.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
