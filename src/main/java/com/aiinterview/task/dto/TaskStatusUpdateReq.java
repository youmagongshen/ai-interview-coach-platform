package com.aiinterview.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskStatusUpdateReq {

    @NotBlank(message = "status is required")
    private String status;
}
