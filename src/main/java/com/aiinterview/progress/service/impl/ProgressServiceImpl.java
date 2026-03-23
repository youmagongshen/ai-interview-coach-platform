package com.aiinterview.progress.service.impl;

import com.aiinterview.interview.entity.SessionEntity;
import com.aiinterview.interview.mapper.SessionMapper;
import com.aiinterview.progress.dto.TrendPointVO;
import com.aiinterview.progress.dto.WeakPointVO;
import com.aiinterview.progress.service.ProgressService;
import com.aiinterview.score.entity.ScoreEntity;
import com.aiinterview.score.mapper.ScoreMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final SessionMapper sessionMapper;
    private final ScoreMapper scoreMapper;

    @Override
    public List<TrendPointVO> trend(Long userId, Long roleId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));

        LambdaQueryWrapper<SessionEntity> wrapper = new LambdaQueryWrapper<SessionEntity>()
                .eq(SessionEntity::getUserId, userId)
                .eq(SessionEntity::getStatus, "FINISHED")
                .orderByDesc(SessionEntity::getCreatedAt)
                .last("limit 300");
        if (roleId != null) {
            wrapper.eq(SessionEntity::getRoleId, roleId);
        }

        List<SessionEntity> sessions = sessionMapper.selectList(wrapper);
        return sessions.stream()
                .filter(s -> s.getOverallScore() != null)
                .sorted(Comparator.comparing(SessionEntity::getEndedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(safeLimit)
                .map(s -> {
                    TrendPointVO vo = new TrendPointVO();
                    vo.setSessionId(s.getId());
                    vo.setOverallScore(s.getOverallScore());
                    vo.setEndedAt(s.getEndedAt() == null ? s.getUpdatedAt() : s.getEndedAt());
                    return vo;
                })
                .sorted(Comparator.comparing(TrendPointVO::getEndedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public List<WeakPointVO> latestWeakPoints(Long userId, Long roleId) {
        LambdaQueryWrapper<SessionEntity> wrapper = new LambdaQueryWrapper<SessionEntity>()
                .eq(SessionEntity::getUserId, userId)
                .eq(SessionEntity::getStatus, "FINISHED")
                .orderByDesc(SessionEntity::getCreatedAt)
                .last("limit 20");
        if (roleId != null) {
            wrapper.eq(SessionEntity::getRoleId, roleId);
        }

        List<SessionEntity> sessions = sessionMapper.selectList(wrapper);
        Map<String, Integer> counter = new HashMap<>();

        for (SessionEntity session : sessions) {
            List<ScoreEntity> scores = scoreMapper.selectList(
                    new LambdaQueryWrapper<ScoreEntity>().eq(ScoreEntity::getSessionId, session.getId()));
            for (ScoreEntity score : scores) {
                if (!StringUtils.hasText(score.getWeakPoints())) {
                    continue;
                }
                String[] parts = score.getWeakPoints().split(",");
                for (String part : parts) {
                    String key = part.trim();
                    if (!StringUtils.hasText(key)) {
                        continue;
                    }
                    counter.put(key, counter.getOrDefault(key, 0) + 1);
                }
            }
        }

        return counter.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> {
                    WeakPointVO vo = new WeakPointVO();
                    vo.setWeakPoint(e.getKey());
                    vo.setCount(e.getValue());
                    vo.setSuggestion(suggestion(e.getKey()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private String suggestion(String weak) {
        if ("correctness".equalsIgnoreCase(weak)) {
            return "Review core concepts and summarize one-page cheat sheets.";
        }
        if ("depth".equalsIgnoreCase(weak)) {
            return "Prepare tradeoffs, edge cases, and failure-handling examples.";
        }
        if ("logic".equalsIgnoreCase(weak)) {
            return "Use structured answer templates and practice timed responses.";
        }
        if ("match".equalsIgnoreCase(weak)) {
            return "Map your examples directly to role responsibilities.";
        }
        if ("expression".equalsIgnoreCase(weak)) {
            return "Record mock answers and optimize pace and clarity.";
        }
        return "Continue deliberate practice with focused feedback.";
    }
}
