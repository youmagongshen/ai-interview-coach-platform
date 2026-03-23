package com.aiinterview.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SessionVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime autoFinishAt;

    private Boolean voiceEnabled;
    private Boolean videoEnabled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endedAt;

    private String finishReason;
    private Integer durationSec;
    private BigDecimal overallScore;
    private String evaluationStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime evaluationStartedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime evaluationFinishedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAt;
}