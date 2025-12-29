package com.example.money_lover.mapper;

import com.example.money_lover.dto.request.UserCreationRequest;
import com.example.money_lover.dto.response.UserResponse;
import com.example.money_lover.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring") // Để Spring quản lý Mapper này như 1 Bean
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);
    
    // Hàm này dùng để update user (sẽ dùng sau)
    void updateUser(@MappingTarget User user, UserCreationRequest request);
}