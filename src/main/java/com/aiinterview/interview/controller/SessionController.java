package com.aiinterview.interview.controller;

import com.aiinterview.common.auth.AuthContextHolder;
import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.common.response.PageResponse;
import com.aiinterview.interview.dto.EvaluationStatusVO;
import com.aiinterview.interview.dto.SessionAnswerReq;
import com.aiinterview.interview.dto.SessionAnswerResp;
import com.aiinterview.interview.dto.SessionConfigUpdateReq;
import com.aiinterview.interview.dto.SessionCreateReq;
import com.aiinterview.interview.dto.SessionCreateResp;
import com.aiinterview.interview.dto.SessionFinishReq;
import com.aiinterview.interview.dto.SessionVO;
import com.aiinterview.interview.dto.TurnVO;
import com.aiinterview.interview.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/interview/sessions")
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ApiResponse<PageResponse<SessionVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String interviewType,
            @RequestParam(required = false) String keyword) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.pageByUser(userId, status, interviewType, keyword, page, pageSize));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<SessionVO> detail(@PathVariable Long sessionId) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.getById(userId, sessionId));
    }

    @PostMapping
    public ApiResponse<SessionCreateResp> create(@Valid @RequestBody SessionCreateReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.create(userId, req));
    }

    @PatchMapping("/{sessionId}/config")
    public ApiResponse<Boolean> updateConfig(
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionConfigUpdateReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.updateConfig(userId, sessionId, req));
    }

    @GetMapping("/{sessionId}/turns")
    public ApiResponse<PageResponse<TurnVO>> pageTurns(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.pageTurns(userId, sessionId, page, pageSize));
    }

    @PostMapping("/{sessionId}/answers")
    public ApiResponse<SessionAnswerResp> answer(
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionAnswerReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.submitAnswer(userId, sessionId, req));
    }

    @PostMapping("/{sessionId}/finish")
    public ApiResponse<Boolean> finish(
            @PathVariable Long sessionId,
            @RequestBody(required = false) SessionFinishReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.finish(userId, sessionId, req == null ? new SessionFinishReq() : req));
    }

    @GetMapping("/{sessionId}/evaluation-status")
    public ApiResponse<EvaluationStatusVO> evaluationStatus(@PathVariable Long sessionId) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(sessionService.getEvaluationStatus(userId, sessionId));
    }
}