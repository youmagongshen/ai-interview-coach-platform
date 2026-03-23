package com.aiinterview.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginReq {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    private Integer rememberDays;

    @Size(max = 100, message = "device label too long")
    private String deviceLabel;
}
