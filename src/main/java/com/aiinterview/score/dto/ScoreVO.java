package com.aiinterview.score.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ScoreVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
