package com.aiinterview.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("turns")
public class TurnEntity {

    @TableId(type = IdType.AUTO)
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
    private LocalDateTime evaluatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
