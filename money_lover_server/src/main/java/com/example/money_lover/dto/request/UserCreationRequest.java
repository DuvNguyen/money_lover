package com.example.money_lover.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    private String email;

    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;

    private String fullName;
}