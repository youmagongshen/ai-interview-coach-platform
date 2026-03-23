package com.aiinterview.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeChunkCreateReq {

    @NotBlank(message = "chunkText is required")
    private String chunkText;

    @Size(max = 500, message = "keywords too long")
    private String keywords;

    @Size(max = 100, message = "embeddingModel too long")
    private String embeddingModel;
}
