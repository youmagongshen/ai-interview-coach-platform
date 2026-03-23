package com.aiinterview.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class KnowledgeChunkVO {

    private Long id;
    private Long docId;
    private Integer chunkIndex;
    private String chunkText;
    private String keywords;
    private Integer tokenCount;
    private String embeddingModel;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}