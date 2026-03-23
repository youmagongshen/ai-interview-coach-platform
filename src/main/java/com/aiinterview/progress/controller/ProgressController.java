package com.aiinterview.progress.controller;

import com.aiinterview.common.auth.AuthContextHolder;
import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.progress.dto.TrendPointVO;
import com.aiinterview.progress.dto.WeakPointVO;
import com.aiinterview.progress.service.ProgressService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/progress")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/trend")
    public ApiResponse<List<TrendPointVO>> trend(
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(progressService.trend(userId, roleId, limit));
    }

    @GetMapping("/latest-weak-points")
    public ApiResponse<List<WeakPointVO>> latestWeakPoints(@RequestParam(required = false) Long roleId) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(progressService.latestWeakPoints(userId, roleId));
    }
}