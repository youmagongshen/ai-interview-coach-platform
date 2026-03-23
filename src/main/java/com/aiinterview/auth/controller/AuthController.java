package com.aiinterview.auth.controller;

import com.aiinterview.auth.dto.AuthTokenResp;
import com.aiinterview.auth.dto.LoginReq;
import com.aiinterview.auth.dto.LogoutReq;
import com.aiinterview.auth.dto.RefreshReq;
import com.aiinterview.auth.dto.RegisterReq;
import com.aiinterview.auth.dto.RegisterResp;
import com.aiinterview.auth.service.AuthService;
import com.aiinterview.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<RegisterResp> register(@Valid @RequestBody RegisterReq req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResp> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResp> refresh(@Valid @RequestBody RefreshReq req) {
        return ApiResponse.ok(authService.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(@Valid @RequestBody LogoutReq req) {
        return ApiResponse.ok(authService.logout(req.getRefreshToken()));
    }
}

