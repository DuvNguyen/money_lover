package com.example.money_lover.service;

import com.example.money_lover.dto.request.AuthenticationRequest;
import com.example.money_lover.dto.response.AuthenticationResponse;

public interface IAuthenticationService {
    /**
     * Hàm xử lý đăng nhập cơ bản (Email + Password)
     * @param request chứa email và password người dùng gửi lên
     * @return token nếu đăng nhập thành công
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);
}