package com.aiinterview.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sessions")
public class SessionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long roleId;
    private String title;
    private String status;
    private Integer totalRounds;
    private Integer currentRound;
    private String difficulty;
    private String interviewStage;
    private String followupMode;
    private String interviewType;
    private Integer timeLimitSec;
    private LocalDateTime autoFinishAt;
    private Boolean voiceEnabled;
    private Boolean videoEnabled;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String finishReason;
    private Integer durationSec;
    private BigDecimal overallScore;
    private String evaluationStatus;
    private LocalDateTime evaluationStartedAt;
    private LocalDateTime evaluationFinishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActiveAt;
}
