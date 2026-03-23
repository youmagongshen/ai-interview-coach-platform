package com.aiinterview.question.controller;

import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.common.response.PageResponse;
import com.aiinterview.question.dto.QuestionVO;
import com.aiinterview.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/questions")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public ApiResponse<PageResponse<QuestionVO>> page(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(questionService.page(roleId, questionType, difficulty, active, keyword, page, pageSize));
    }
}
