package com.aiinterview.role.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RoleVO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal weightCorrectness;
    private BigDecimal weightDepth;
    private BigDecimal weightLogic;
    private BigDecimal weightMatch;
    private BigDecimal weightExpression;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}