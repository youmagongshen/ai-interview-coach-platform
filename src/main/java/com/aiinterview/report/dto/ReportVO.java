package com.aiinterview.report.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ReportVO {

    private Long sessionId;
    private BigDecimal overallScore;
    private DimensionScoreVO dimensionScore;
    private String summary;
    private List<String> highlightPoints;
    private List<String> improvementPoints;
    private List<String> suggestions;
    private List<String> nextPlan;
}
