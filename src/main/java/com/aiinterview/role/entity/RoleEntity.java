package com.aiinterview.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("roles")
public class RoleEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal weightCorrectness;
    private BigDecimal weightDepth;
    private BigDecimal weightLogic;
    private BigDecimal weightMatch;
    private BigDecimal weightExpression;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
