package com.aiinterview.score.service;

import com.aiinterview.score.dto.ScoreVO;
import com.aiinterview.score.entity.ScoreEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ScoreService extends IService<ScoreEntity> {

    List<ScoreVO> listBySessionId(Long sessionId);
}
