package com.aiinterview.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("reports")
public class ReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String summary;
    private String highlightPoints;
    private String improvementPoints;
    private String suggestions;
    private String nextPlan;
    private LocalDateTime createdAt;
}
