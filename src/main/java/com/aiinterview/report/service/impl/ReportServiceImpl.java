package com.aiinterview.report.service.impl;

import com.aiinterview.interview.entity.SessionEntity;
import com.aiinterview.interview.mapper.SessionMapper;
import com.aiinterview.report.dto.DimensionScoreVO;
import com.aiinterview.report.dto.ReportVO;
import com.aiinterview.report.entity.ReportEntity;
import com.aiinterview.report.mapper.ReportMapper;
import com.aiinterview.report.service.ReportService;
import com.aiinterview.score.entity.ScoreEntity;
import com.aiinterview.score.mapper.ScoreMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, ReportEntity> implements ReportService {

    private final SessionMapper sessionMapper;
    private final ReportMapper baseMapper;
    private final ScoreMapper scoreMapper;

    @Override
    public ReportVO getBySession(Long userId, Long sessionId) {
        SessionEntity session = sessionMapper.selectById(sessionId, userId);
        return buildReportVO(session);
    }

    @Override
    public ReportVO getBySessionAdmin(Long sessionId) {
        SessionEntity session = sessionMapper.selectByIdAdmin(sessionId);
        return buildReportVO(session);
    }

    private ReportVO buildReportVO(SessionEntity session) {
        if (session == null) {
            return null;
        }

        Long sessionId = session.getId();
        List<ScoreEntity> scores = scoreMapper.selectBySessionId(sessionId);
        DimensionScoreVO dimension = new DimensionScoreVO();
        dimension.setCorrectness(avg(scores.stream().map(ScoreEntity::getCorrectnessScore).collect(Collectors.toList())));
        dimension.setDepth(avg(scores.stream().map(ScoreEntity::getDepthScore).collect(Collectors.toList())));
        dimension.setLogic(avg(scores.stream().map(ScoreEntity::getLogicScore).collect(Collectors.toList())));
        dimension.setMatch(avg(scores.stream().map(ScoreEntity::getMatchScore).collect(Collectors.toList())));
        dimension.setExpression(avg(scores.stream().map(ScoreEntity::getExpressionScore).collect(Collectors.toList())));

        ReportEntity report = baseMapper.selectBySessionId(sessionId);
        ReportVO vo = new ReportVO();
        vo.setSessionId(sessionId);
        vo.setOverallScore(session.getOverallScore() == null
                ? avg(scores.stream().map(ScoreEntity::getTotalScore).collect(Collectors.toList()))
                : session.getOverallScore());
        vo.setDimensionScore(dimension);
        vo.setSummary(report == null ? null : report.getSummary());
        vo.setHighlightPoints(splitLines(report == null ? null : report.getHighlightPoints()));
        vo.setImprovementPoints(splitLines(report == null ? null : report.getImprovementPoints()));
        vo.setSuggestions(splitLines(report == null ? null : report.getSuggestions()));
        vo.setNextPlan(splitLines(report == null ? null : report.getNextPlan()));
        return vo;
    }

    private List<String> splitLines(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\r?\\n"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private BigDecimal avg(List<BigDecimal> values) {
        List<BigDecimal> valid = values == null ? List.of() : values.stream().filter(x -> x != null).collect(Collectors.toList());
        if (valid.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = valid.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(valid.size()), 2, RoundingMode.HALF_UP);
    }
}
