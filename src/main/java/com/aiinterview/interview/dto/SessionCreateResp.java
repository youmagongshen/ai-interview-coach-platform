package com.aiinterview.interview.dto;

import lombok.Data;

@Data
public class SessionCreateResp {

    private Long sessionId;
    private String status;
    private String title;
    private TurnVO firstTurn;
}