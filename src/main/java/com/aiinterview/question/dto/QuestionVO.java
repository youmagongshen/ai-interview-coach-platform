package com.aiinterview.question.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class QuestionVO {

    private Long id;
    private Long roleId;
    private String questionType;
    private String difficulty;
    private String questionText;
    private String expectedPoints;
    private String keywords;
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}