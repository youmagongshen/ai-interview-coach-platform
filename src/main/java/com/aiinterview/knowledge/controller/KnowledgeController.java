package com.aiinterview.knowledge.controller;

import com.aiinterview.common.auth.AuthContextHolder;
import com.aiinterview.common.response.ApiResponse;
import com.aiinterview.common.response.PageResponse;
import com.aiinterview.knowledge.dto.KnowledgeChunkCreateReq;
import com.aiinterview.knowledge.dto.KnowledgeChunkVO;
import com.aiinterview.knowledge.dto.KnowledgeDocCreateReq;
import com.aiinterview.knowledge.dto.KnowledgeDocUpdateReq;
import com.aiinterview.knowledge.dto.KnowledgeDocVO;
import com.aiinterview.knowledge.dto.KnowledgeRetrievalLogVO;
import com.aiinterview.knowledge.service.KnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inv/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @GetMapping("/documents")
    public ApiResponse<PageResponse<KnowledgeDocVO>> pageDocuments(
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(knowledgeService.pageDocuments(roleId, status, null, keyword, page, pageSize));
    }

    @PostMapping("/documents")
    public ApiResponse<KnowledgeDocVO> createDocument(@Valid @RequestBody KnowledgeDocCreateReq req) {
        Long userId = AuthContextHolder.requireUserId();
        return ApiResponse.ok(knowledgeService.createDocument(userId, req));
    }

    @PutMapping("/documents/{docId}")
    public ApiResponse<Boolean> updateDocument(
            @PathVariable Long docId,
            @Valid @RequestBody KnowledgeDocUpdateReq req) {
        return ApiResponse.ok(knowledgeService.updateDocument(docId, req));
    }

    @GetMapping("/documents/{docId}/chunks")
    public ApiResponse<PageResponse<KnowledgeChunkVO>> pageChunks(
            @PathVariable Long docId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(knowledgeService.pageChunks(docId, page, pageSize));
    }

    @PostMapping("/documents/{docId}/chunks")
    public ApiResponse<KnowledgeChunkVO> createChunk(
            @PathVariable Long docId,
            @Valid @RequestBody KnowledgeChunkCreateReq req) {
        return ApiResponse.ok(knowledgeService.createChunk(docId, req));
    }

    @GetMapping("/retrieval-logs")
    public ApiResponse<PageResponse<KnowledgeRetrievalLogVO>> pageRetrievalLogs(
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(knowledgeService.pageRetrievalLogs(sessionId, roleId, page, pageSize));
    }
}