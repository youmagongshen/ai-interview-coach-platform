package com.aiinterview.user.controller;

import com.aiinterview.common.auth.AuthContextHolder;
import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.user.dto.UserProfileUpdateReq;
import com.aiinterview.user.dto.UserProfileVO;
import com.aiinterview.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/users")
public class UserController {

    private final UserService userService;

    @GetMapping({"/current", "/me"})
    public ApiResponse<UserProfileVO> me() {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(userService.getCurrentUser(userId));
    }

    @PutMapping({"/current", "/me"})
    public ApiResponse<UserProfileVO> updateMe(@Valid @RequestBody UserProfileUpdateReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(userService.updateCurrentUser(userId, req));
    }
}