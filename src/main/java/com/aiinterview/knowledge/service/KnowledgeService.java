package com.aiinterview.knowledge.service;

import com.aiinterview.common.response.PageResponse;
import com.aiinterview.knowledge.dto.KnowledgeChunkCreateReq;
import com.aiinterview.knowledge.dto.KnowledgeChunkVO;
import com.aiinterview.knowledge.dto.KnowledgeDocCreateReq;
import com.aiinterview.knowledge.dto.KnowledgeDocUpdateReq;
import com.aiinterview.knowledge.dto.KnowledgeDocVO;
import com.aiinterview.knowledge.dto.KnowledgeRetrievalLogVO;
import com.aiinterview.knowledge.entity.KbDocumentEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface KnowledgeService extends IService<KbDocumentEntity> {

    PageResponse<KnowledgeDocVO> pageDocuments(Long roleId, String status, String docType, String keyword, int page, int pageSize);

    KnowledgeDocVO createDocument(Long userId, KnowledgeDocCreateReq req);

    boolean updateDocument(Long docId, KnowledgeDocUpdateReq req);

    boolean deleteDocument(Long docId);

    PageResponse<KnowledgeChunkVO> pageChunks(Long docId, int page, int pageSize);

    KnowledgeChunkVO createChunk(Long docId, KnowledgeChunkCreateReq req);

    boolean deleteChunk(Long chunkId);

    PageResponse<KnowledgeRetrievalLogVO> pageRetrievalLogs(Long sessionId, Long roleId, int page, int pageSize);
}
