package com.aiinterview.interview.controller;

import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.common.response.PageResponse;
import com.aiinterview.interview.dto.SessionVO;
import com.aiinterview.interview.service.SessionService;
import com.aiinterview.report.dto.ReportVO;
import com.aiinterview.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/admin/interview/sessions")
public class AdminSessionController {

    private final SessionService sessionService;
    private final ReportService reportService;

    @GetMapping
    public ApiResponse<PageResponse<SessionVO>> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String evaluationStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(sessionService.pageForAdmin(status, evaluationStatus, keyword, page, pageSize));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<SessionVO> detail(@PathVariable Long sessionId) {
        return ApiResponse.ok(sessionService.getByIdForAdmin(sessionId));
    }

    @GetMapping("/{sessionId}/report")
    public ApiResponse<ReportVO> report(@PathVariable Long sessionId) {
        return ApiResponse.ok(reportService.getBySessionAdmin(sessionId));
    }
}
