package com.dev.demo.dto.request;

import lombok.Data;

import java.util.Set;
@Data
public class UserUpdateRequest {
    private String username;
    private String password;
    Set<String> roles;

}
