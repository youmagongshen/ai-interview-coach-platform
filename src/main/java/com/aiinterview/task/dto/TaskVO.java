package com.aiinterview.task.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TaskVO {

    private Long id;
    private Long userId;
    private Long sessionId;
    private String title;
    private String taskType;
    private String content;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
