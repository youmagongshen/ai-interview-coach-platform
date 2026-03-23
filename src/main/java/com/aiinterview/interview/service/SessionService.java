package com.aiinterview.interview.service;

import com.aiinterview.common.response.PageResponse;
import com.aiinterview.interview.dto.EvaluationStatusVO;
import com.aiinterview.interview.dto.SessionAnswerReq;
import com.aiinterview.interview.dto.SessionAnswerResp;
import com.aiinterview.interview.dto.SessionConfigUpdateReq;
import com.aiinterview.interview.dto.SessionCreateReq;
import com.aiinterview.interview.dto.SessionCreateResp;
import com.aiinterview.interview.dto.SessionFinishReq;
import com.aiinterview.interview.dto.SessionVO;
import com.aiinterview.interview.dto.TurnVO;
import com.aiinterview.interview.entity.SessionEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SessionService extends IService<SessionEntity> {

    PageResponse<SessionVO> pageByUser(Long userId, String status, String interviewType, String keyword, int page, int pageSize);

    PageResponse<SessionVO> pageForAdmin(String status, String evaluationStatus, String keyword, int page, int pageSize);

    SessionVO getById(Long userId, Long sessionId);

    SessionVO getByIdForAdmin(Long sessionId);

    SessionCreateResp create(Long userId, SessionCreateReq req);

    boolean updateConfig(Long userId, Long sessionId, SessionConfigUpdateReq req);

    PageResponse<TurnVO> pageTurns(Long userId, Long sessionId, int page, int pageSize);

    SessionAnswerResp submitAnswer(Long userId, Long sessionId, SessionAnswerReq req);

    boolean finish(Long userId, Long sessionId, SessionFinishReq req);

    EvaluationStatusVO getEvaluationStatus(Long userId, Long sessionId);
}
