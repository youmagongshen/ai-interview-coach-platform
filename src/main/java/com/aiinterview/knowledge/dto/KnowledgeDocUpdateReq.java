package com.aiinterview.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeDocUpdateReq {

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title too long")
    private String title;

    @Size(max = 255, message = "sourceName too long")
    private String sourceName;

    @Size(max = 500, message = "sourceUrl too long")
    private String sourceUrl;

    @Size(max = 500, message = "storagePath too long")
    private String storagePath;

    private String summary;

    @NotBlank(message = "status is required")
    @Size(max = 20, message = "status too long")
    private String status;
}
