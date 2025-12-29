package com.example.money_lover.service.impl;

import com.example.money_lover.dto.request.UserCreationRequest;
import com.example.money_lover.dto.response.UserResponse;
import com.example.money_lover.entity.User;
import com.example.money_lover.exception.AppException;
import com.example.money_lover.exception.ErrorCode;
import com.example.money_lover.mapper.UserMapper;
import com.example.money_lover.repository.UserRepository;
import com.example.money_lover.service.IUserService;


import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // Tự động tạo Constructor cho các biến final (Thay cho @Autowired)
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa password

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
}