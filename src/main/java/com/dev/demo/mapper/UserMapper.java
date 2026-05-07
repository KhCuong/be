package com.dev.demo.mapper;

import com.dev.demo.dto.request.UserCreationRequest;
import com.dev.demo.dto.request.UserUpdateRequest;
import com.dev.demo.dto.response.UserResponse;
import com.dev.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    public User toUser(UserCreationRequest request);

    public UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
    List<UserResponse> toUserResponseList(List<User> users);
}
