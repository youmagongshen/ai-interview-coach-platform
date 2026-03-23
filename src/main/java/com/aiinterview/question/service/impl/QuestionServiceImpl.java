package com.aiinterview.question.service.impl;

import com.aiinterview.common.response.PageResponse;
import com.aiinterview.question.dto.QuestionCreateReq;
import com.aiinterview.question.dto.QuestionUpdateReq;
import com.aiinterview.question.dto.QuestionVO;
import com.aiinterview.question.entity.QuestionEntity;
import com.aiinterview.question.mapper.QuestionMapper;
import com.aiinterview.question.service.QuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, QuestionEntity> implements QuestionService {

    @Override
    public PageResponse<QuestionVO> page(
            Long roleId,
            String questionType,
            String difficulty,
            Boolean active,
            String keyword,
            int page,
            int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safeSize;
        long total = baseMapper.count(roleId, questionType, difficulty, active, keyword);
        List<QuestionVO> list = baseMapper.selectPage(roleId, questionType, difficulty, active, keyword, offset, safeSize)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return PageResponse.of(list, safePage, safeSize, total);
    }

    @Override
    public QuestionVO create(QuestionCreateReq req) {
        QuestionEntity entity = new QuestionEntity();
        entity.setRoleId(req.getRoleId());
        entity.setQuestionType(req.getQuestionType());
        entity.setDifficulty(req.getDifficulty());
        entity.setQuestionText(req.getQuestionText());
        entity.setExpectedPoints(req.getExpectedPoints());
        entity.setKeywords(req.getKeywords());
        entity.setActive(true);
        baseMapper.insert(entity);
        QuestionEntity latest = baseMapper.selectById(entity.getId());
        return toVO(latest == null ? entity : latest);
    }

    @Override
    public boolean update(Long questionId, QuestionUpdateReq req) {
        QuestionEntity entity = new QuestionEntity();
        entity.setId(questionId);
        entity.setRoleId(req.getRoleId());
        entity.setQuestionType(req.getQuestionType());
        entity.setDifficulty(req.getDifficulty());
        entity.setQuestionText(req.getQuestionText());
        entity.setExpectedPoints(req.getExpectedPoints());
        entity.setKeywords(req.getKeywords());
        entity.setActive(req.getActive());
        return baseMapper.updateById(entity) > 0;
    }

    @Override
    public boolean updateActive(Long questionId, Boolean active) {
        return baseMapper.updateActive(questionId, active) > 0;
    }

    private QuestionVO toVO(QuestionEntity entity) {
        QuestionVO vo = new QuestionVO();
        vo.setId(entity.getId());
        vo.setRoleId(entity.getRoleId());
        vo.setQuestionType(entity.getQuestionType());
        vo.setDifficulty(entity.getDifficulty());
        vo.setQuestionText(entity.getQuestionText());
        vo.setExpectedPoints(entity.getExpectedPoints());
        vo.setKeywords(entity.getKeywords());
        vo.setActive(entity.getActive());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}
