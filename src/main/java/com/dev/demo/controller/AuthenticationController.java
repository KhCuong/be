package com.dev.demo.controller;

import com.dev.demo.dto.request.LogoutRequest;
import com.dev.demo.dto.request.RefreshRequest;
import com.dev.demo.dto.response.ApiResponse;
import com.dev.demo.dto.request.AuthenticationRequest;
import com.dev.demo.dto.request.IntrospectRequest;
import com.dev.demo.dto.response.AuthenticationResponse;
import com.dev.demo.dto.response.IntrospectResponse;
import com.dev.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request) {

        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);;

        ApiResponse<AuthenticationResponse> response = new ApiResponse<>();
        response.setResult(authenticationResponse);
        return response;
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(
            @RequestBody RefreshRequest request) throws ParseException, JOSEException, TimeoutException {

        AuthenticationResponse authenticationResponse = authenticationService.refreshToken(request);

        ApiResponse<AuthenticationResponse> response = new ApiResponse<>();
        response.setResult(authenticationResponse);
        return response;
    }
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody LogoutRequest request) throws ParseException, JOSEException, TimeoutException {
        authenticationService.logout(request);
        return new ApiResponse<>();
    }


    // kiểm tra token hợp lệ hay k
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> verify (@RequestBody IntrospectRequest request) throws ParseException, JOSEException, TimeoutException {
        IntrospectResponse introspectResponse = authenticationService.introspect(request);
        ApiResponse<IntrospectResponse> response = new ApiResponse<>();
        response.setResult(introspectResponse);
        return response;
    }
}