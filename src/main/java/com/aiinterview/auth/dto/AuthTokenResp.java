package com.aiinterview.auth.dto;

import lombok.Data;

@Data
public class AuthTokenResp {

    private Long userId;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}
