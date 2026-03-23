package com.aiinterview.report.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DimensionScoreVO {

    private BigDecimal correctness;
    private BigDecimal depth;
    private BigDecimal logic;
    private BigDecimal match;
    private BigDecimal expression;
}
