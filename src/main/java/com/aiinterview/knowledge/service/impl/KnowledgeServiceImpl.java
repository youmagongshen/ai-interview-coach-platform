package com.aiinterview.knowledge.service.impl;

import com.aiinterview.common.response.PageResponse;
import com.aiinterview.knowledge.dto.KnowledgeChunkCreateReq;
import com.aiinterview.knowledge.dto.KnowledgeChunkVO;
import com.aiinterview.knowledge.dto.KnowledgeDocCreateReq;
import com.aiinterview.knowledge.dto.KnowledgeDocUpdateReq;
import com.aiinterview.knowledge.dto.KnowledgeDocVO;
import com.aiinterview.knowledge.dto.KnowledgeRetrievalLogVO;
import com.aiinterview.knowledge.entity.KbChunkEntity;
import com.aiinterview.knowledge.entity.KbDocumentEntity;
import com.aiinterview.knowledge.entity.KbRetrievalLogEntity;
import com.aiinterview.knowledge.mapper.KbChunkMapper;
import com.aiinterview.knowledge.mapper.KbDocumentMapper;
import com.aiinterview.knowledge.mapper.KbRetrievalLogMapper;
import com.aiinterview.knowledge.service.KnowledgeService;
import com.aiinterview.role.entity.RoleEntity;
import com.aiinterview.role.mapper.RoleMapper;
import java.util.List;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl extends ServiceImpl<KbDocumentMapper, KbDocumentEntity> implements KnowledgeService {

    private final KbChunkMapper kbChunkMapper;
    private final KbRetrievalLogMapper kbRetrievalLogMapper;
    private final RoleMapper roleMapper;

    @Override
    public PageResponse<KnowledgeDocVO> pageDocuments(Long roleId, String status, String docType, String keyword, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safeSize;
        long total = baseMapper.count(roleId, status, docType, keyword);
        List<KnowledgeDocVO> list = baseMapper.selectPage(roleId, status, docType, keyword, offset, safeSize)
                .stream().map(this::toDocVO).collect(Collectors.toList());
        return PageResponse.of(list, safePage, safeSize, total);
    }

    @Override
    public KnowledgeDocVO createDocument(Long userId, KnowledgeDocCreateReq req) {
        KbDocumentEntity entity = new KbDocumentEntity();
        entity.setRoleId(req.getRoleId());
        entity.setTitle(req.getTitle());
        entity.setDocType(req.getDocType());
        entity.setSourceName(req.getSourceName());
        entity.setSourceUrl(req.getSourceUrl());
        entity.setStoragePath(req.getStoragePath());
        entity.setSummary(req.getSummary());
        entity.setStatus("ACTIVE");
        entity.setVersion(1);
        entity.setCreatedBy(userId);
        baseMapper.insert(entity);
        KbDocumentEntity latest = baseMapper.selectById(entity.getId());
        return toDocVO(latest == null ? entity : latest);
    }

    @Override
    public boolean updateDocument(Long docId, KnowledgeDocUpdateReq req) {
        KbDocumentEntity current = baseMapper.selectById(docId);
        if (current == null) {
            return false;
        }
        KbDocumentEntity update = new KbDocumentEntity();
        update.setId(docId);
        update.setTitle(req.getTitle());
        update.setSourceName(req.getSourceName());
        update.setSourceUrl(req.getSourceUrl());
        update.setStoragePath(req.getStoragePath());
        update.setSummary(req.getSummary());
        update.setStatus(req.getStatus());
        update.setVersion((current.getVersion() == null ? 1 : current.getVersion()) + 1);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public boolean deleteDocument(Long docId) {
        kbChunkMapper.deleteByDocId(docId);
        return baseMapper.deleteById(docId) > 0;
    }

    @Override
    public PageResponse<KnowledgeChunkVO> pageChunks(Long docId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safeSize;
        long total = kbChunkMapper.countByDocId(docId);
        List<KnowledgeChunkVO> list = kbChunkMapper.selectPageByDocId(docId, offset, safeSize)
                .stream().map(this::toChunkVO).collect(Collectors.toList());
        return PageResponse.of(list, safePage, safeSize, total);
    }

    @Override
    public KnowledgeChunkVO createChunk(Long docId, KnowledgeChunkCreateReq req) {
        Integer max = kbChunkMapper.selectMaxChunkIndex(docId);
        KbChunkEntity entity = new KbChunkEntity();
        entity.setDocId(docId);
        entity.setChunkIndex((max == null ? 0 : max) + 1);
        entity.setChunkText(req.getChunkText());
        entity.setKeywords(req.getKeywords());
        entity.setTokenCount(approxToken(req.getChunkText()));
        entity.setEmbeddingModel(defaultIfBlank(req.getEmbeddingModel(), "text-embedding-v1"));
        kbChunkMapper.insert(entity);
        KnowledgeChunkVO vo = toChunkVO(entity);

        KbRetrievalLogEntity log = new KbRetrievalLogEntity();
        log.setRoleId(resolveRoleIdByDoc(docId));
        log.setQueryText("chunk-created:" + docId);
        log.setTopK(5);
        log.setRetrievedDocIds(String.valueOf(docId));
        log.setRetrievedChunkIds(String.valueOf(entity.getId()));
        log.setLlmAnswerSummary("chunk inserted");
        log.setLatencyMs(1);
        kbRetrievalLogMapper.insert(log);

        return vo;
    }

    @Override
    public boolean deleteChunk(Long chunkId) {
        return kbChunkMapper.deleteById(chunkId) > 0;
    }

    @Override
    public PageResponse<KnowledgeRetrievalLogVO> pageRetrievalLogs(Long sessionId, Long roleId, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safeSize;
        long total = kbRetrievalLogMapper.count(sessionId, roleId);
        List<KnowledgeRetrievalLogVO> list = kbRetrievalLogMapper.selectPage(sessionId, roleId, offset, safeSize)
                .stream().map(this::toLogVO).collect(Collectors.toList());
        return PageResponse.of(list, safePage, safeSize, total);
    }

    private Long resolveRoleIdByDoc(Long docId) {
        KbDocumentEntity doc = baseMapper.selectById(docId);
        return doc == null ? null : doc.getRoleId();
    }

    private int approxToken(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        return Math.max(1, text.length() / 2);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private KnowledgeDocVO toDocVO(KbDocumentEntity entity) {
        KnowledgeDocVO vo = new KnowledgeDocVO();
        vo.setId(entity.getId());
        vo.setRoleId(entity.getRoleId());
        vo.setRoleName(resolveRoleName(entity.getRoleId()));
        vo.setTitle(entity.getTitle());
        vo.setDocType(entity.getDocType());
        vo.setSourceName(entity.getSourceName());
        vo.setSourceUrl(entity.getSourceUrl());
        vo.setStoragePath(entity.getStoragePath());
        vo.setSummary(entity.getSummary());
        vo.setStatus(entity.getStatus());
        vo.setVersion(entity.getVersion());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    private KnowledgeChunkVO toChunkVO(KbChunkEntity entity) {
        KnowledgeChunkVO vo = new KnowledgeChunkVO();
        vo.setId(entity.getId());
        vo.setDocId(entity.getDocId());
        vo.setChunkIndex(entity.getChunkIndex());
        vo.setChunkText(entity.getChunkText());
        vo.setKeywords(entity.getKeywords());
        vo.setTokenCount(entity.getTokenCount());
        vo.setEmbeddingModel(entity.getEmbeddingModel());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private KnowledgeRetrievalLogVO toLogVO(KbRetrievalLogEntity entity) {
        KnowledgeRetrievalLogVO vo = new KnowledgeRetrievalLogVO();
        vo.setId(entity.getId());
        vo.setSessionId(entity.getSessionId());
        vo.setTurnId(entity.getTurnId());
        vo.setRoleId(entity.getRoleId());
        vo.setQueryText(entity.getQueryText());
        vo.setTopK(entity.getTopK());
        vo.setRetrievedDocIds(entity.getRetrievedDocIds());
        vo.setRetrievedChunkIds(entity.getRetrievedChunkIds());
        vo.setLlmAnswerSummary(entity.getLlmAnswerSummary());
        vo.setLatencyMs(entity.getLatencyMs());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private String resolveRoleName(Long roleId) {
        if (roleId == null) {
            return "Unknown";
        }
        RoleEntity role = roleMapper.selectById(roleId);
        if (role == null || !StringUtils.hasText(role.getName())) {
            return "Role-" + roleId;
        }
        return role.getName();
    }
}
