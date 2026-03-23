package com.aiinterview.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("refresh_tokens")
public class RefreshTokenEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String tokenHash;
    private String deviceLabel;
    private Integer rememberDays;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}
