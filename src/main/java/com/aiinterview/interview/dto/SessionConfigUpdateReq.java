package com.aiinterview.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SessionConfigUpdateReq {

    private String difficulty;
    private String followupMode;
    private String interviewType;

    @Min(value = 60, message = "timeLimitSec must be >= 60")
    @Max(value = 7200, message = "timeLimitSec must be <= 7200")
    private Integer timeLimitSec;

    private Boolean voiceEnabled;
    private Boolean videoEnabled;
}
