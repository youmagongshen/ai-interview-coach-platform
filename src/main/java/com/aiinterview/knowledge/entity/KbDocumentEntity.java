package com.aiinterview.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("kb_documents")
public class KbDocumentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private String title;
    private String docType;
    private String sourceName;
    private String sourceUrl;
    private String storagePath;
    private String summary;
    private String status;
    private Integer version;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
