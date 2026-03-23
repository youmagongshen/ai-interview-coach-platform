package com.aiinterview.report.controller;

import com.aiinterview.common.auth.AuthContextHolder;
import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.report.dto.ReportVO;
import com.aiinterview.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/interview/sessions")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{sessionId}/report")
    public ApiResponse<ReportVO> report(@PathVariable Long sessionId) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(reportService.getBySession(userId, sessionId));
    }
}