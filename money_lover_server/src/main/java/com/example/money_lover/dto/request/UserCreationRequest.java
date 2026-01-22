package com.example.money_lover.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {
    @NotBlank(message = "EMAIL_REQUIRED")
    @Size(min = 4, message = "USERNAME_INVALID")
    private String email;

    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;

    private String fullName;
}