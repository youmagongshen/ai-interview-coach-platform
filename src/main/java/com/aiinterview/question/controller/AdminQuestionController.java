package com.aiinterview.question.controller;

import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.common.response.PageResponse;
import com.aiinterview.question.dto.QuestionActiveUpdateReq;
import com.aiinterview.question.dto.QuestionCreateReq;
import com.aiinterview.question.dto.QuestionUpdateReq;
import com.aiinterview.question.dto.QuestionVO;
import com.aiinterview.question.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/admin/questions")
public class AdminQuestionController {

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

    @PostMapping
    public ApiResponse<QuestionVO> create(@Valid @RequestBody QuestionCreateReq req) {
        return ApiResponse.ok(questionService.create(req));
    }

    @PutMapping("/{questionId}")
    public ApiResponse<Boolean> update(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionUpdateReq req) {
        return ApiResponse.ok(questionService.update(questionId, req));
    }

    @PatchMapping("/{questionId}/active")
    public ApiResponse<Boolean> updateActive(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionActiveUpdateReq req) {
        return ApiResponse.ok(questionService.updateActive(questionId, req.getActive()));
    }
    @DeleteMapping("/{questionId}")
    public ApiResponse<Boolean> delete(@PathVariable Long questionId) {
        return ApiResponse.ok(questionService.removeById(questionId));
    }
}
