package com.aiinterview.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class KnowledgeDocVO {

    private Long id;
    private Long roleId;
    private String roleName;
    private String title;
    private String docType;
    private String sourceName;
    private String sourceUrl;
    private String storagePath;
    private String summary;
    private String status;
    private Integer version;
    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}