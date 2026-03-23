package com.aiinterview.score.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("scores")
public class ScoreEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long turnId;
    private BigDecimal correctnessScore;
    private BigDecimal depthScore;
    private BigDecimal logicScore;
    private BigDecimal matchScore;
    private BigDecimal expressionScore;
    private BigDecimal totalScore;
    private String evidence;
    private String weakPoints;
    private LocalDateTime createdAt;
}
