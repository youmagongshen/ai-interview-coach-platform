package com.aiinterview.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterReq {

    @NotBlank(message = "username is required")
    @Size(max = 50, message = "username too long")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 64, message = "password length invalid")
    private String password;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "invalid phone")
    private String phone;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;

    private Boolean termsAccepted;
    private String termsVersion;
}
