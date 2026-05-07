package com.dev.demo.mapper;


import com.dev.demo.dto.request.PermissionRequest;
import com.dev.demo.dto.response.PermissionResponse;
import com.dev.demo.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
