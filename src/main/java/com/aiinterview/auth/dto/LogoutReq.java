package com.aiinterview.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutReq {

    @NotBlank(message = "refreshToken is required")
    private String refreshToken;
}
