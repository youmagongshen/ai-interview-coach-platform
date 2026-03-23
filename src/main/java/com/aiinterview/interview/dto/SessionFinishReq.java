package com.aiinterview.interview.dto;

import lombok.Data;

@Data
public class SessionFinishReq {

    private String finishReason;
    private Integer clientElapsedSec;
}
