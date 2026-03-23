package com.aiinterview.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshReq {

    @NotBlank(message = "refreshToken is required")
    private String refreshToken;
}
