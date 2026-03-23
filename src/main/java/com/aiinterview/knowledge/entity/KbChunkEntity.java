package com.aiinterview.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("kb_chunks")
public class KbChunkEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long docId;
    private Integer chunkIndex;
    private String chunkText;
    private String keywords;
    private Integer tokenCount;
    private String embeddingModel;
    private String embeddingVector;
    private LocalDateTime createdAt;
}
