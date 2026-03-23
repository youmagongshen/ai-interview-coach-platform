package com.aiinterview.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TaskCreateReq {

    private Long sessionId;

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title too long")
    private String title;

    @NotBlank(message = "taskType is required")
    @Size(max = 20, message = "taskType too long")
    private String taskType;

    @NotBlank(message = "content is required")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
