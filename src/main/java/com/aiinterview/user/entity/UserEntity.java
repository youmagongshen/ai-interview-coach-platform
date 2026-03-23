package com.aiinterview.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("users")
public class UserEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String phone;
    private String email;
    private Boolean termsAccepted;
    private String termsVersion;
    private LocalDateTime termsAcceptedAt;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
