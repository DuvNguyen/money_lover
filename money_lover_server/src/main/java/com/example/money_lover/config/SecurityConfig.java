package com.example.money_lover.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 1. Bean PasswordEncoder:
     * Spring Security cần biết ta mã hóa mật khẩu bằng thuật toán gì.
     * BCrypt là chuẩn công nghiệp hiện nay (an toàn, chống dò ngược).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 2. SecurityFilterChain:
     * Đây là "người gác cổng". Mọi request gửi đến đều phải đi qua cái lưới lọc này.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì chúng ta dùng Token (JWT), không dùng Cookie/Session
            // Nếu dùng MVC truyền thống mới cần bật cái này.
            .csrf(AbstractHttpConfigurer::disable)

            // Phân quyền truy cập (Authorize)
            .authorizeHttpRequests(request -> request
                // Cho phép ai cũng được truy cập vào API đăng nhập/đăng ký
                .requestMatchers("/auth/**").permitAll()
                // Set this up temporarily to test - must comment it when finishing testing
                .requestMatchers("/users/**").permitAll()
                // Cho phép truy cập Swagger UI (để test API sau này)
                // Cho phép POST (Tạo user) để test
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/users").permitAll()

                // Còn lại tất cả các request khác (xem ví, thêm giao dịch...) bắt buộc phải có Token
                .anyRequest().authenticated()
            );

        // Sau này chúng ta sẽ chèn thêm cái filter kiểm tra JWT ở đây (addFilterBefore)
        
        return http.build();
    }
}