package com.aiinterview.question.service;

import com.aiinterview.common.response.PageResponse;
import com.aiinterview.question.dto.QuestionCreateReq;
import com.aiinterview.question.dto.QuestionUpdateReq;
import com.aiinterview.question.dto.QuestionVO;
import com.aiinterview.question.entity.QuestionEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface QuestionService extends IService<QuestionEntity> {

    PageResponse<QuestionVO> page(Long roleId, String questionType, String difficulty, Boolean active, String keyword, int page, int pageSize);

    QuestionVO create(QuestionCreateReq req);

    boolean update(Long questionId, QuestionUpdateReq req);

    boolean updateActive(Long questionId, Boolean active);
}
