package com.aiinterview.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("kb_retrieval_logs")
public class KbRetrievalLogEntity {

    @TableId(type = IdType.AUTO)
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
    private LocalDateTime createdAt;
}
