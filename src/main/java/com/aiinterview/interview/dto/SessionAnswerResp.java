package com.aiinterview.interview.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SessionAnswerResp {

    private Long turnId;
    private boolean scoreSaved;
    private BigDecimal roundScore;
    private String nextAction;
    private TurnVO nextTurn;
    private String aiReplyText; // AI回复文本（用于提示用户重新回答）
    private String aiAdvice;     // AI建议（目前不使用）
}
