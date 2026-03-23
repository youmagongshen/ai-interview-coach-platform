package com.aiinterview.question.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionActiveUpdateReq {

    @NotNull(message = "active is required")
    private Boolean active;
}