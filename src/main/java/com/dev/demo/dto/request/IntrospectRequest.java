package com.dev.demo.dto.request;


import lombok.Builder;

@Builder
public class IntrospectRequest {
    String token;

    public IntrospectRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
