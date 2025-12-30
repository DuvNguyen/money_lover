package com.example.money_lover.controller;

import com.example.money_lover.dto.request.AuthenticationRequest;
import com.example.money_lover.dto.response.ApiResponse;
import com.example.money_lover.dto.response.AuthenticationResponse;
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
    @PostMapping("/token") 
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
}