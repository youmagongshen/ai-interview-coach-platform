package com.aiinterview.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionCreateReq {

    @NotNull(message = "roleId is required")
    private Long roleId;

    private String difficulty;

    @Min(value = 1, message = "totalRounds must be >= 1")
    @Max(value = 20, message = "totalRounds must be <= 20")
    private Integer totalRounds;

    @Min(value = 60, message = "timeLimitSec must be >= 60")
    @Max(value = 7200, message = "timeLimitSec must be <= 7200")
    private Integer timeLimitSec;

    private String interviewStage;
    private String followupMode;
    private String interviewType;
    private Boolean voiceEnabled;
    private Boolean videoEnabled;
}