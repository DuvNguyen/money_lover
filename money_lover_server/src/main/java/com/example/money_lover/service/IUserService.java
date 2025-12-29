package com.example.money_lover.service;

import com.example.money_lover.dto.request.UserCreationRequest;
import com.example.money_lover.dto.response.UserResponse;
import java.util.List;

public interface IUserService {
    UserResponse createUser(UserCreationRequest request);
    List<UserResponse> getAllUsers();
}