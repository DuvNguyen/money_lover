package com.example.money_lover.controller;

import com.example.money_lover.dto.request.AuthenticationRequest;
import com.example.money_lover.dto.request.IntrospectRequest;
import com.example.money_lover.dto.request.LogoutRequest;
import com.example.money_lover.dto.request.RefreshRequest;
import com.example.money_lover.dto.response.ApiResponse;
import com.example.money_lover.dto.response.AuthenticationResponse;
import com.example.money_lover.dto.response.IntrospectResponse;
import com.example.money_lover.service.EmailService;
import com.example.money_lover.service.IAuthenticationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth") // Spring sẽ ghép với context-path thành: /api/v1/auth
@RequiredArgsConstructor
public class AuthenticationController {

    private final IAuthenticationService authenticationService;

    // API đầy đủ sẽ là: POST http://localhost:8080/api/v1/auth/token
   // 1. Đăng nhập (Login)
    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    // 2. Kiểm tra Token (Introspect)
    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    // 3. Làm mới Token (Refresh)
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    // 4. Đăng xuất (Logout)
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }
    
    
}