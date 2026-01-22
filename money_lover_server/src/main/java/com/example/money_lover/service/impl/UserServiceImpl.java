package com.example.money_lover.service.impl;

import com.example.money_lover.dto.request.UserCreationRequest;
import com.example.money_lover.dto.response.UserResponse;
import com.example.money_lover.entity.Role; // Import đúng Entity Role
import com.example.money_lover.entity.User;
import com.example.money_lover.exception.AppException;
import com.example.money_lover.exception.ErrorCode;
import com.example.money_lover.mapper.UserMapper;
import com.example.money_lover.repository.RoleRepository; // Import Repo mới
import com.example.money_lover.repository.UserRepository;
import com.example.money_lover.service.EmailService;
import com.example.money_lover.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // 1. Inject thêm RoleRepository
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        // Check trùng email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 2. Xử lý Role (Quan trọng)
        // Tìm xem trong DB có role tên "USER" chưa
        Role userRole = roleRepository.findById("USER").orElse(null);
        
        // Nếu chưa có (lần đầu chạy app) thì tạo mới luôn
        if (userRole == null) {
            userRole = roleRepository.save(Role.builder()
                    .name("USER")
                    .description("Người dùng cơ bản")
                    .build());
        }

        // Gán role vào user
        var roles = new HashSet<Role>();
        roles.add(userRole);
        user.setRoles(roles);

        // Lưu User
        try {
            user = userRepository.save(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        // Gửi mail
        try {
            emailService.sendEmail(
                request.getEmail(),
                "Chào mừng đến với Money Lover!",
                "<h1>Xin chào " + user.getFullName() + "</h1><p>Bạn đã đăng ký thành công!</p>"
            );
        } catch (Exception e) {
            log.error("Lỗi gửi mail: {}", e.getMessage());
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
}