package com.aiinterview.fujian.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttachmentVO {

    private Long id;
    private Long userId;
    private Long sessionId;
    private Long turnId;
    private String fileType;
    private String fileName;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
