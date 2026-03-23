package com.aiinterview.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionUpdateReq {

    @NotNull(message = "roleId is required")
    private Long roleId;

    @NotBlank(message = "questionType is required")
    @Size(max = 20, message = "questionType too long")
    private String questionType;

    @NotBlank(message = "difficulty is required")
    @Size(max = 20, message = "difficulty too long")
    private String difficulty;

    @NotBlank(message = "questionText is required")
    private String questionText;

    private String expectedPoints;

    @Size(max = 500, message = "keywords too long")
    private String keywords;

    @NotNull(message = "active is required")
    private Boolean active;
}