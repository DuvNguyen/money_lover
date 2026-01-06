package com.example.money_lover.service;

import java.text.ParseException;

import com.example.money_lover.dto.request.AuthenticationRequest;
import com.example.money_lover.dto.request.IntrospectRequest;
import com.example.money_lover.dto.request.LogoutRequest;
import com.example.money_lover.dto.response.AuthenticationResponse;
import com.example.money_lover.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;
import com.example.money_lover.dto.request.RefreshRequest;;

public interface IAuthenticationService {
    /**
     * Hàm xử lý đăng nhập cơ bản (Email + Password)
     * @param request chứa email và password người dùng gửi lên
     * @return token nếu đăng nhập thành công
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout(LogoutRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);

    IntrospectResponse introspect(IntrospectRequest request);
}