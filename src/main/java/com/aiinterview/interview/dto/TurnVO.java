package com.aiinterview.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TurnVO {

    private Long id;
    private Long sessionId;
    private Integer roundNo;
    private String turnType;
    private Long questionId;
    private String questionText;
    private Boolean isFollowUp;
    private String followUpReason;
    private String answerMode;
    private String answerText;
    private String audioUrl;
    private String asrText;
    private String aiReplyText;
    private String aiAdvice;
    private Integer responseSec;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime evaluatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}