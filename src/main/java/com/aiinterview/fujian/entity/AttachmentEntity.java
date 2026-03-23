package com.aiinterview.fujian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("attachments")
public class AttachmentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sessionId;
    private Long turnId;
    private String fileType;
    private String fileName;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime createdAt;
}
