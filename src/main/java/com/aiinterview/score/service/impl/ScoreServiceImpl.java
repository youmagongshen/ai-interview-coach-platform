package com.aiinterview.score.service.impl;

import com.aiinterview.score.dto.ScoreVO;
import com.aiinterview.score.entity.ScoreEntity;
import com.aiinterview.score.mapper.ScoreMapper;
import com.aiinterview.score.service.ScoreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, ScoreEntity> implements ScoreService {

    @Override
    public List<ScoreVO> listBySessionId(Long sessionId) {
        return baseMapper.selectBySessionId(sessionId)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private ScoreVO toVO(ScoreEntity entity) {
        ScoreVO vo = new ScoreVO();
        vo.setId(entity.getId());
        vo.setSessionId(entity.getSessionId());
        vo.setTurnId(entity.getTurnId());
        vo.setCorrectnessScore(entity.getCorrectnessScore());
        vo.setDepthScore(entity.getDepthScore());
        vo.setLogicScore(entity.getLogicScore());
        vo.setMatchScore(entity.getMatchScore());
        vo.setExpressionScore(entity.getExpressionScore());
        vo.setTotalScore(entity.getTotalScore());
        vo.setEvidence(entity.getEvidence());
        vo.setWeakPoints(entity.getWeakPoints());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
