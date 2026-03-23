package com.aiinterview.score.controller;

import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.score.dto.ScoreVO;
import com.aiinterview.score.service.ScoreService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/interview/sessions")
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping("/{sessionId}/scores")
    public ApiResponse<List<ScoreVO>> listBySession(@PathVariable Long sessionId) {
        return ApiResponse.ok(scoreService.listBySessionId(sessionId));
    }
}

