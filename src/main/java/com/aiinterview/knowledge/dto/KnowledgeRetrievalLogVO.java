package com.aiinterview.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class KnowledgeRetrievalLogVO {

    private Long id;
    private Long sessionId;
    private Long turnId;
    private Long roleId;
    private String queryText;
    private Integer topK;
    private String retrievedDocIds;
    private String retrievedChunkIds;
    private String llmAnswerSummary;
    private Integer latencyMs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}