package com.dev.demo.dto.request;

import com.dev.demo.entity.Role;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequest {
    @Id
    private String name;
    private String description;
    private Set<String> permissions;

}
