package com.dev.demo.dto.request;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PermissionRequest {
    private String name;
    private String description;
}
