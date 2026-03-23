package com.aiinterview.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileUpdateReq {

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "invalid phone")
    private String phone;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email")
    private String email;
}
