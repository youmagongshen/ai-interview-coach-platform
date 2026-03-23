package com.aiinterview.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeDocCreateReq {

    @NotNull(message = "roleId is required")
    private Long roleId;

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title too long")
    private String title;

    @NotBlank(message = "docType is required")
    @Size(max = 20, message = "docType too long")
    private String docType;

    @Size(max = 255, message = "sourceName too long")
    private String sourceName;

    @Size(max = 500, message = "sourceUrl too long")
    private String sourceUrl;

    @Size(max = 500, message = "storagePath too long")
    private String storagePath;

    private String summary;
}
