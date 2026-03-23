package com.aiinterview.task.controller;

import com.aiinterview.common.auth.AuthContextHolder;
import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.common.response.PageResponse;
import com.aiinterview.task.dto.TaskCreateReq;
import com.aiinterview.task.dto.TaskStatusUpdateReq;
import com.aiinterview.task.dto.TaskVO;
import com.aiinterview.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/inv/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ApiResponse<PageResponse<TaskVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String status) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(taskService.pageByUser(userId, status, page, pageSize));
    }

    @PostMapping
    public ApiResponse<TaskVO> create(@Valid @RequestBody TaskCreateReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(taskService.create(userId, req));
    }

    @PatchMapping("/{taskId}/status")
    public ApiResponse<Boolean> updateStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskStatusUpdateReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(taskService.updateStatus(userId, taskId, req.getStatus()));
    }

    @DeleteMapping("/{taskId}")
    public ApiResponse<Boolean> delete(@PathVariable Long taskId) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(taskService.delete(userId, taskId));
    }
}