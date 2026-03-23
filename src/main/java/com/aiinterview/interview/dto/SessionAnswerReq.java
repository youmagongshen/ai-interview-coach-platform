package com.aiinterview.interview.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SessionAnswerReq {

    @NotNull(message = "turnId is required")
    private Long turnId;

    @Size(max = 20, message = "answerMode too long")
    private String answerMode;

    private String answerText;

    @Size(max = 500, message = "audioUrl too long")
    private String audioUrl;

    private String asrText;

    private Integer responseSec;
}
