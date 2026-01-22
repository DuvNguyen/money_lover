package com.example.money_lover.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String[] PUBLIC_ENDPOINTS = {
            "/users",           // Register
            "/auth/token",      // Login
            "/auth/introspect", // Check token (Bên trong service đã validate rồi)
            "/auth/refresh",     // Refresh public vì lúc này User chưa có Token sống
           
    };

    //AUTHENTICATED ENDPOINTS: Phải có Token sống mới vào được
    //không cần khai báo mảng này, vì .anyRequest().authenticated() sẽ lo hết


    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .authorizeHttpRequests(request ->
                request
                    // Cho phép Public Endpoints
                    .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                    
                    // Tất cả request còn lại bắt buộc phải có Token hợp lệ
                    .anyRequest().authenticated()
            )
            
            // Resource Server (Check JWT)
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder()))
            )
            
            // Disable CSRF
            .csrf(AbstractHttpConfigurer::disable)
            
            // Security Headers (Phần này bạn làm rất tốt, giữ nguyên)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .xssProtection(xss -> {})
                .httpStrictTransportSecurity(hsts -> 
                    hsts.maxAgeInSeconds(31536000).includeSubDomains(true)
                )
            );

        return httpSecurity.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        // Validation key (Good practice!)
        if (signerKey == null || signerKey.trim().isEmpty()) {
            throw new IllegalStateException("JWT signer key is not configured");
        }
        
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            signerKey.getBytes(), 
            "HS512"
        );
        
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}