package com.aiinterview.fujian.controller;

import com.aiinterview.common.auth.AuthContextHolder;
import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.fujian.dto.AttachmentVO;
import com.aiinterview.fujian.service.AttachmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AttachmentVO> upload(
            @RequestParam Long sessionId,
            @RequestParam(required = false) Long turnId,
            @RequestParam String fileType,
            @RequestParam("file") MultipartFile file) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(attachmentService.upload(userId, sessionId, turnId, fileType, file));
    }

    @GetMapping
    public ApiResponse<List<AttachmentVO>> list(
            @RequestParam Long sessionId,
            @RequestParam(required = false) String fileType) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(attachmentService.listBySession(userId, sessionId, fileType));
    }
}