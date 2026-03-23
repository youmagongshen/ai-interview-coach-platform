package com.aiinterview.interview.service.impl;

import com.aiinterview.ai.service.KnowledgeBaseFileService;
import com.aiinterview.ai.service.QuestionBankService;
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
import com.aiinterview.interview.entity.TurnEntity;
import com.aiinterview.interview.mapper.SessionMapper;
import com.aiinterview.interview.mapper.TurnMapper;
import com.aiinterview.interview.service.SessionService;
import com.aiinterview.question.entity.QuestionEntity;
import com.aiinterview.question.mapper.QuestionMapper;
import com.aiinterview.report.entity.ReportEntity;
import com.aiinterview.report.mapper.ReportMapper;
import com.aiinterview.role.entity.RoleEntity;
import com.aiinterview.role.mapper.RoleMapper;
import com.aiinterview.score.entity.ScoreEntity;
import com.aiinterview.score.mapper.ScoreMapper;
import com.aiinterview.task.entity.TaskEntity;
import com.aiinterview.task.mapper.TaskMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl extends ServiceImpl<SessionMapper, SessionEntity> implements SessionService {

    private final TurnMapper turnMapper;
    private final QuestionMapper questionMapper;
    private final RoleMapper roleMapper;
    private final ScoreMapper scoreMapper;
    private final ReportMapper reportMapper;
    private final TaskMapper taskMapper;
    private final QuestionBankService questionBankService;
    private final KnowledgeBaseFileService knowledgeBaseFileService;
    
    // 内存中的无效回答计数（sessionId -> turnId -> count）
    private final Map<String, Integer> invalidAnswerCounts = new ConcurrentHashMap<>();

    @Override
    public PageResponse<SessionVO> pageByUser(Long userId, String status, String interviewType, String keyword, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safeSize;
        long total = baseMapper.countByUser(userId, status, interviewType, keyword);
        List<SessionVO> list = baseMapper.selectPageByUser(userId, status, interviewType, keyword, offset, safeSize)
                .stream().map(this::toSessionVO).collect(Collectors.toList());
        return PageResponse.of(list, safePage, safeSize, total);
    }

    @Override
    public PageResponse<SessionVO> pageForAdmin(String status, String evaluationStatus, String keyword, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safeSize;
        long total = baseMapper.countAdmin(status, evaluationStatus, keyword);
        List<SessionVO> list = baseMapper.selectPageAdmin(status, evaluationStatus, keyword, offset, safeSize)
                .stream().map(this::toSessionVO).collect(Collectors.toList());
        return PageResponse.of(list, safePage, safeSize, total);
    }

    @Override
    public SessionVO getById(Long userId, Long sessionId) {
        SessionEntity entity = baseMapper.selectById(sessionId, userId);
        return entity == null ? null : toSessionVO(entity);
    }

    @Override
    public SessionVO getByIdForAdmin(Long sessionId) {
        SessionEntity entity = baseMapper.selectByIdAdmin(sessionId);
        return entity == null ? null : toSessionVO(entity);
    }

    @Override
    public SessionCreateResp create(Long userId, SessionCreateReq req) {
        SessionEntity session = new SessionEntity();
        session.setUserId(userId);
        session.setRoleId(req.getRoleId());
        session.setStatus("IN_PROGRESS");
        session.setTotalRounds(req.getTotalRounds() == null ? 5 : req.getTotalRounds());
        session.setCurrentRound(0);
        session.setDifficulty(defaultIfBlank(req.getDifficulty(), "MIDDLE"));
        session.setInterviewStage(defaultIfBlank(req.getInterviewStage(), "TECH_FIRST"));
        session.setFollowupMode(defaultIfBlank(req.getFollowupMode(), "AUTO"));
        session.setInterviewType(defaultIfBlank(req.getInterviewType(), "TEXT"));
        session.setTimeLimitSec(req.getTimeLimitSec() == null ? 1500 : req.getTimeLimitSec());
        session.setVoiceEnabled(req.getVoiceEnabled() != null && req.getVoiceEnabled());
        session.setVideoEnabled(req.getVideoEnabled() != null && req.getVideoEnabled());
        session.setStartedAt(LocalDateTime.now());
        session.setAutoFinishAt(LocalDateTime.now().plusSeconds(session.getTimeLimitSec()));
        session.setLastActiveAt(LocalDateTime.now());
        session.setEvaluationStatus("PENDING");
        session.setTitle(buildTitle(req.getRoleId()));
        baseMapper.insert(session);

        TurnVO firstTurn = createFirstTurn(session, req.getDifficulty());
        if (firstTurn != null) {
            SessionEntity update = new SessionEntity();
            update.setId(session.getId());
            update.setCurrentRound(1);
            update.setLastActiveAt(LocalDateTime.now());
            baseMapper.updateById(update);
        }

        SessionCreateResp resp = new SessionCreateResp();
        resp.setSessionId(session.getId());
        resp.setStatus(session.getStatus());
        resp.setTitle(session.getTitle());
        resp.setFirstTurn(firstTurn);
        return resp;
    }

    @Override
    public boolean updateConfig(Long userId, Long sessionId, SessionConfigUpdateReq req) {
        SessionEntity current = baseMapper.selectById(sessionId, userId);
        if (current == null || "FINISHED".equalsIgnoreCase(current.getStatus())) {
            return false;
        }

        SessionEntity update = new SessionEntity();
        update.setId(sessionId);
        if (StringUtils.hasText(req.getDifficulty())) {
            update.setDifficulty(req.getDifficulty());
        }
        if (StringUtils.hasText(req.getFollowupMode())) {
            update.setFollowupMode(req.getFollowupMode());
        }
        if (StringUtils.hasText(req.getInterviewType())) {
            update.setInterviewType(req.getInterviewType());
        }
        if (req.getTimeLimitSec() != null) {
            update.setTimeLimitSec(req.getTimeLimitSec());
            if (current.getStartedAt() != null) {
                update.setAutoFinishAt(current.getStartedAt().plusSeconds(req.getTimeLimitSec()));
            }
        }
        if (req.getVoiceEnabled() != null) {
            update.setVoiceEnabled(req.getVoiceEnabled());
        }
        if (req.getVideoEnabled() != null) {
            update.setVideoEnabled(req.getVideoEnabled());
        }
        update.setLastActiveAt(LocalDateTime.now());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public PageResponse<TurnVO> pageTurns(Long userId, Long sessionId, int page, int pageSize) {
        SessionEntity session = baseMapper.selectById(sessionId, userId);
        if (session == null) {
            return PageResponse.of(List.of(), Math.max(1, page), Math.max(1, pageSize), 0);
        }
        List<TurnVO> all = turnMapper.selectBySessionId(sessionId).stream().map(this::toTurnVO).collect(Collectors.toList());
        int safePage = Math.max(1, page);
        int safeSize = Math.max(1, pageSize);
        int from = (safePage - 1) * safeSize;
        int to = Math.min(from + safeSize, all.size());
        List<TurnVO> sub = from >= all.size() ? List.of() : all.subList(from, to);
        return PageResponse.of(sub, safePage, safeSize, all.size());
    }

    @Override
    public SessionAnswerResp submitAnswer(Long userId, Long sessionId, SessionAnswerReq req) {
        SessionAnswerResp resp = new SessionAnswerResp();
        resp.setTurnId(req.getTurnId());
        resp.setScoreSaved(false);
        resp.setNextAction("FINISH_REQUIRED");
    
        SessionEntity session = baseMapper.selectById(sessionId, userId);
        if (session == null || "FINISHED".equalsIgnoreCase(session.getStatus())) {
            return resp;
        }
    
        TurnEntity turn = turnMapper.selectById(req.getTurnId(), sessionId);
        if (turn == null) {
            return resp;
        }
    
        String userAnswer = req.getAnswerText();
        
        // ========== 获取题目关键词用于语义检测 ==========
        List<String> keywords = getQuestionKeywords(session.getRoleId(), turn.getQuestionText());
        
        // ========== 智能回答检测逻辑 ==========
        boolean isDontKnow = isDontKnowAnswer(userAnswer);
        // 使用增强的语义相关性检测
        boolean isIrrelevant = isAnswerIrrelevant(turn.getQuestionText(), userAnswer, keywords);
        
        // 从内存中获取当前无效回答次数
        String invalidKey = sessionId + "_" + turn.getId();
        int invalidCount = invalidAnswerCounts.getOrDefault(invalidKey, 0);
        
        // 如果是"不知道/不会"或无关回答
        if (isDontKnow || isIrrelevant) {
            if (isDontKnow) {
                log.info("用户回答不知道/不会，直接跳下一题, turnId={}", turn.getId());
                
                // 保存当前回答
                TurnEntity update = new TurnEntity();
                update.setId(turn.getId());
                update.setSessionId(sessionId);
                update.setAnswerMode(defaultIfBlank(req.getAnswerMode(), "TEXT"));
                update.setAnswerText(userAnswer);
                update.setAudioUrl(req.getAudioUrl());
                update.setAsrText(req.getAsrText());
                update.setResponseSec(req.getResponseSec());
                update.setEvaluatedAt(LocalDateTime.now());
                
                // 明确说不知道/不会，直接判0分，跳下一题
                saveScoreWithForcedScore(session, turn, 0);
                update.setAiReplyText("好的，那我们来看下一题。");
                update.setAiAdvice(null);
                turnMapper.updateAnswer(update);
                
                // 创建下一题
                TurnVO nextTurn = createNextQuestion(session, turn, null);
                if (nextTurn != null) {
                    resp.setNextTurn(nextTurn);
                    resp.setNextAction("NEXT_QUESTION");
                    resp.setRoundScore(BigDecimal.ZERO);
                }
                return resp;
            } else {
                invalidCount++;
                invalidAnswerCounts.put(invalidKey, invalidCount);
                log.info("检测到无关回答，累计次数={}, turnId={}, 关键词匹配={}", invalidCount, turn.getId(), keywords);
                
                // 保存当前回答（标记为低分）
                TurnEntity update = new TurnEntity();
                update.setId(turn.getId());
                update.setSessionId(sessionId);
                update.setAnswerMode(defaultIfBlank(req.getAnswerMode(), "TEXT"));
                update.setAnswerText(userAnswer);
                update.setAudioUrl(req.getAudioUrl());
                update.setAsrText(req.getAsrText());
                update.setResponseSec(req.getResponseSec());
                update.setEvaluatedAt(LocalDateTime.now());
                turnMapper.updateAnswer(update);
                
                // 超过3次无关回答，直接跳下一题
                if (invalidCount >= 3) {
                    log.info("无关回答超过3次，自动跳下一题, turnId={}", turn.getId());
                    
                    // 保存极低分
                    saveScoreWithForcedScore(session, turn, 0);
                    
                    // 更新反馈
                    update.setAiReplyText("您的回答与题目无关，我们来看下一题。");
                    update.setAiAdvice(null);
                    turnMapper.updateAnswer(update);
                    
                    // 创建下一题（从题库抽取）
                    TurnVO nextTurn = createNextQuestion(session, turn, null);
                    if (nextTurn != null) {
                        resp.setNextTurn(nextTurn);
                        resp.setNextAction("NEXT_QUESTION");
                        resp.setRoundScore(BigDecimal.ZERO);
                    }
                    return resp;
                } else {
                    // 提示用户重新回答，并给出关键词提示
                    String hint = keywords != null && !keywords.isEmpty() 
                        ? "您的回答与题目无关，请结合以下关键词认真回答：" + String.join("、", keywords) + "。（剩余" + (3 - invalidCount) + "次机会）"
                        : "您的回答与题目无关，请认真回答。（剩余" + (3 - invalidCount) + "次机会）";
                    resp.setAiReplyText(hint);
                    resp.setAiAdvice(null);
                    // 不保存分数，等待用户重新回答
                    return resp;
                }
            }
        }
        
        // ========== 正常回答处理 ==========
        // 使用知识库评估回答并计算分数
        KnowledgeBaseFileService.EvaluationResult evalResult = knowledgeBaseFileService.evaluateAnswer(
                String.valueOf(session.getRoleId()), keywords, userAnswer);
            
        // 生成知识库驱动的反馈和追问
        String aiFeedback = evalResult.getFeedback();
        String aiAdvice = knowledgeBaseFileService.generateFollowUpQuestion(
                String.valueOf(session.getRoleId()), keywords, userAnswer);
    
        TurnEntity update = new TurnEntity();
        update.setId(turn.getId());
        update.setSessionId(sessionId);
        update.setAnswerMode(defaultIfBlank(req.getAnswerMode(), "TEXT"));
        update.setAnswerText(userAnswer);
        update.setAudioUrl(req.getAudioUrl());
        update.setAsrText(req.getAsrText());
        update.setResponseSec(req.getResponseSec());
        update.setAiReplyText(aiFeedback);
        update.setAiAdvice(aiAdvice);
        update.setEvaluatedAt(LocalDateTime.now());
        turnMapper.updateAnswer(update);
    
        TurnEntity latestTurn = turnMapper.selectById(req.getTurnId(), sessionId);
        ScoreEntity score = saveScore(session, latestTurn == null ? turn : latestTurn);
        resp.setScoreSaved(score != null);
        resp.setRoundScore(score == null ? null : score.getTotalScore());
    
        TurnVO nextTurn = createNextQuestion(session, latestTurn == null ? turn : latestTurn, score);
        if (nextTurn != null) {
            resp.setNextTurn(nextTurn);
            resp.setNextAction(Boolean.TRUE.equals(nextTurn.getIsFollowUp()) ? "FOLLOW_UP" : "NEXT_QUESTION");
        }
        return resp;
    }
    
    /**
     * 判断用户是否回答"不知道/不会"
     */
    private boolean isDontKnowAnswer(String answer) {
        if (!StringUtils.hasText(answer)) return false;
        String lower = answer.toLowerCase();
        return lower.contains("不知道") || 
               lower.contains("不会") || 
               lower.contains("不清楚") ||
               lower.contains("不懂") ||
               lower.contains("不太懂") ||
               lower.contains("没了解过") ||
               lower.contains("没学过") ||
               lower.contains("没做过") ||
               lower.contains("dont know") ||
               lower.contains("not sure") ||
               lower.contains("no idea");
    }
    
    /**
     * 判断回答是否与题目相关（语义相关性检测）
     * @param questionText 题目文本
     * @param answerText 用户回答
     * @param keywords 题目的关键词
     * @return true 表示回答与题目无关
     */
    private boolean isAnswerIrrelevant(String questionText, String answerText, List<String> keywords) {
        if (!StringUtils.hasText(answerText) || !StringUtils.hasText(questionText)) {
            return true;
        }
        
        String answer = answerText.trim().toLowerCase();
        
        // 第一层：基础规则过滤
        if (isMeaninglessAnswer(answerText)) {
            return true;
        }
        
        // 第二层：检查是否包含题目关键词
        if (keywords != null && !keywords.isEmpty()) {
            int matchCount = 0;
            int totalKeywords = keywords.size();
            
            for (String keyword : keywords) {
                if (keyword != null && answer.contains(keyword.toLowerCase())) {
                    matchCount++;
                }
            }
            
            // 如果关键词匹配数少于30%，认为是无关回答
            double matchRate = (double) matchCount / totalKeywords;
            if (matchRate < 0.3 && matchCount < 1) {
                log.info("关键词匹配率过低: {}/{}={}, 判定为无关回答", matchCount, totalKeywords, matchRate);
                return true;
            }
        }
        
        // 第三层：检查回答长度与题目复杂度是否匹配
        // 面试题通常是几句话，回答太短说明没认真回答
        int answerLen = answer.length();
        int questionLen = questionText.length();
        
        // 如果回答长度小于题目的1/4，且少于10个字，认为不认真
        if (answerLen < 10 && answerLen < questionLen / 4) {
            log.info("回答过短: {}字，题目{}字，判定为不认真回答", answerLen, questionLen);
            return true;
        }
        
        return false;
    }
    
    /**
     * 判断回答是否无意义（乱码、纯符号、无关内容）
     */
    private boolean isMeaninglessAnswer(String answer) {
        if (!StringUtils.hasText(answer)) return true;
        
        String trimmed = answer.trim();
        
        // 纯数字
        if (trimmed.matches("^\\d+$")) return true;
        
        // 纯英文字母（太短且无意义的英文）
        if (trimmed.matches("^[a-zA-Z\\s]+$") && trimmed.length() < 10) {
            // 排除一些常见的英文回答
            String lower = trimmed.toLowerCase();
            if (lower.contains("yes") || lower.contains("no") || lower.contains("ok") || 
                lower.contains("okay") || lower.contains("thank")) {
                return false;
            }
            return true;
        }
        
        // 纯符号
        if (trimmed.matches("^[\\\\!@#$%^&*()_+=\\[\\]{};:',.<>/?|]+$")) return true;
        
        // 中文字符太少（少于3个）
        long chineseCount = trimmed.chars().filter(c -> Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS).count();
        if (chineseCount < 3 && trimmed.length() < 10) return true;
        
        // 重复字符
        if (trimmed.matches("^(.)\\1+$")) return true;
        
        return false;
    }

    @Override
    public boolean finish(Long userId, Long sessionId, SessionFinishReq req) {
        SessionEntity session = baseMapper.selectById(sessionId, userId);
        if (session == null) {
            return false;
        }
        if ("FINISHED".equalsIgnoreCase(session.getStatus())) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        List<ScoreEntity> scores = scoreMapper.selectBySessionId(sessionId);
        BigDecimal overall = avg(scores.stream().map(ScoreEntity::getTotalScore).collect(Collectors.toList()));
        BigDecimal c = avg(scores.stream().map(ScoreEntity::getCorrectnessScore).collect(Collectors.toList()));
        BigDecimal d = avg(scores.stream().map(ScoreEntity::getDepthScore).collect(Collectors.toList()));
        BigDecimal l = avg(scores.stream().map(ScoreEntity::getLogicScore).collect(Collectors.toList()));
        BigDecimal m = avg(scores.stream().map(ScoreEntity::getMatchScore).collect(Collectors.toList()));
        BigDecimal e = avg(scores.stream().map(ScoreEntity::getExpressionScore).collect(Collectors.toList()));

        List<String> improves = new ArrayList<>();
        if (c.compareTo(new BigDecimal("70")) < 0) improves.add("加强技术准确性，通过专项概念练习提升。");
        if (d.compareTo(new BigDecimal("70")) < 0) improves.add("增加深度：补充边界情况、权衡方案和备用计划。");
        if (l.compareTo(new BigDecimal("70")) < 0) improves.add("改进答案结构：使用'首先-其次-最后'的方式。");
        if (m.compareTo(new BigDecimal("70")) < 0) improves.add("结合目标岗位职责举例，增强匹配度。");
        if (e.compareTo(new BigDecimal("70")) < 0) improves.add("控制语速，提高表达简洁度。");
        if (improves.isEmpty()) improves.add("保持当前水平，可尝试增加场景难度。");

        ReportEntity report = new ReportEntity();
        report.setSessionId(sessionId);
        report.setSummary(buildSummary(overall));
        report.setHighlightPoints(String.join("\n", buildHighlights(c, d, l, m, e)));
        report.setImprovementPoints(String.join("\n", improves));
        report.setSuggestions(String.join("\n", improves.stream().map(x -> "行动建议: " + x).collect(Collectors.toList())));
        report.setNextPlan(String.join("\n", List.of(
                "本周完成2次模拟面试。",
                "每天花30分钟复习一个薄弱知识点。",
                "回看一次回答录音，优化表达。")));

        ReportEntity exists = reportMapper.selectBySessionId(sessionId);
        if (exists == null) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateBySessionId(report);
        }

        createImproveTasks(userId, sessionId, improves);

        SessionEntity update = new SessionEntity();
        update.setId(sessionId);
        update.setStatus("FINISHED");
        update.setFinishReason(defaultIfBlank(req.getFinishReason(), "MANUAL"));
        update.setEndedAt(now);
        update.setDurationSec(req.getClientElapsedSec() != null ? req.getClientElapsedSec()
                : (session.getStartedAt() == null ? 0 : (int) Math.max(0, Duration.between(session.getStartedAt(), now).getSeconds())));
        update.setOverallScore(overall);
        update.setEvaluationStatus("DONE");
        update.setEvaluationStartedAt(now);
        update.setEvaluationFinishedAt(now);
        update.setLastActiveAt(now);
        baseMapper.updateById(update);
        return true;
    }

    @Override
    public EvaluationStatusVO getEvaluationStatus(Long userId, Long sessionId) {
        SessionEntity session = baseMapper.selectById(sessionId, userId);
        if (session == null) {
            return null;
        }
        EvaluationStatusVO vo = new EvaluationStatusVO();
        vo.setSessionId(sessionId);
        vo.setEvaluationStatus(session.getEvaluationStatus());
        vo.setOverallScore(session.getOverallScore());
        vo.setEvaluationStartedAt(session.getEvaluationStartedAt());
        vo.setEvaluationFinishedAt(session.getEvaluationFinishedAt());
        return vo;
    }

    private ScoreEntity saveScore(SessionEntity session, TurnEntity turn) {
        // 从题库获取关键词进行评分
        List<String> questionKeywords = getQuestionKeywords(session.getRoleId(), turn.getQuestionText());
        return saveScoreWithKeywords(session, turn, questionKeywords);
    }
    
    private ScoreEntity saveScoreWithForcedScore(SessionEntity session, TurnEntity turn, int forcedScore) {
        ScoreEntity exists = scoreMapper.selectByTurnId(turn.getId());
        if (exists != null) {
            exists.setTotalScore(new BigDecimal(forcedScore));
            exists.setCorrectnessScore(new BigDecimal(forcedScore));
            scoreMapper.updateById(exists);
            return exists;
        }
        
        ScoreEntity entity = new ScoreEntity();
        entity.setSessionId(session.getId());
        entity.setTurnId(turn.getId());
        entity.setCorrectnessScore(new BigDecimal(forcedScore));
        entity.setDepthScore(new BigDecimal(forcedScore));
        entity.setLogicScore(new BigDecimal(forcedScore));
        entity.setMatchScore(new BigDecimal(forcedScore));
        entity.setExpressionScore(new BigDecimal(forcedScore));
        entity.setTotalScore(new BigDecimal(forcedScore));
        entity.setEvidence("irrelevant_answer");
        entity.setWeakPoints("无关回答");
        scoreMapper.insert(entity);
        return scoreMapper.selectByTurnId(turn.getId());
    }
    
    private ScoreEntity saveScoreWithKeywords(SessionEntity session, TurnEntity turn, List<String> questionKeywords) {
        ScoreEntity exists = scoreMapper.selectByTurnId(turn.getId());
        if (exists != null) {
            return exists;
        }

        String answer = answerOf(turn);
        
        // ========== 面试评分五维度计算 ==========
        // 维度1：技术基础（占比30%）- 关键词匹配
        BigDecimal correctness = calcCorrectnessWithKeywords(answer, questionKeywords);
        
        // 维度2：项目理解/深度（占比25%）- 回答完整性
        BigDecimal depth = calcDepthScore(answer);
        
        // 维度3：逻辑思维（占比20%）- 回答逻辑性
        BigDecimal logic = calcLogicScore(answer);
        
        // 维度4：沟通表达（占比15%）- 回答清晰度
        BigDecimal expression = calcExpressionScore(answer);
        
        // 维度5：态度稳定性（占比10%）- 基于是否乱答
        BigDecimal attitude = calcAttitudeScore(answer);

        // 面试评分权重：技术30 + 深度25 + 逻辑20 + 表达15 + 态度10 = 100
        BigDecimal total = correctness.multiply(new BigDecimal("0.30"))
                .add(depth.multiply(new BigDecimal("0.25")))
                .add(logic.multiply(new BigDecimal("0.20")))
                .add(expression.multiply(new BigDecimal("0.15")))
                .add(attitude.multiply(new BigDecimal("0.10")))
                .setScale(2, RoundingMode.HALF_UP);

        ScoreEntity entity = new ScoreEntity();
        entity.setSessionId(session.getId());
        entity.setTurnId(turn.getId());
        entity.setCorrectnessScore(correctness);
        entity.setDepthScore(depth);
        entity.setLogicScore(logic);
        entity.setMatchScore(attitude);  // 用 matchScore 字段存态度分
        entity.setExpressionScore(expression);
        entity.setTotalScore(total);
        
        // 评分依据
        int keywordHits = countContains(answer.toLowerCase(), 
            questionKeywords.stream().map(String::toLowerCase).collect(Collectors.toList()));
        entity.setEvidence(String.format("技术=%s, 深度=%s, 逻辑=%s, 表达=%s, 态度=%s, 关键词=%d/%d",
            correctness, depth, logic, expression, attitude, keywordHits, questionKeywords.size()));
        
        // 薄弱点分析
        entity.setWeakPoints(buildInterviewWeakPoints(correctness, depth, logic, expression, attitude));
        
        scoreMapper.insert(entity);
        return scoreMapper.selectByTurnId(turn.getId());
    }
    
    /**
     * 计算态度分 - 面试评分维度5：态度稳定性（占比10%）
     */
    private BigDecimal calcAttitudeScore(String answer) {
        if (!StringUtils.hasText(answer)) return new BigDecimal("0");
        
        // 乱码/无意义回答 = 态度差
        if (isMeaninglessAnswer(answer)) return new BigDecimal("10");
        
        // 回答太短 = 敷衍态度
        if (answer.length() < 20) return new BigDecimal("30");
        
        // 正常认真回答
        return new BigDecimal("85");
    }
    
    /**
     * 面试薄弱点分析
     */
    private String buildInterviewWeakPoints(BigDecimal c, BigDecimal d, BigDecimal l, BigDecimal e, BigDecimal a) {
        List<String> weak = new ArrayList<>();
        if (c.compareTo(new BigDecimal("60")) < 0) weak.add("技术基础");
        if (d.compareTo(new BigDecimal("60")) < 0) weak.add("回答深度");
        if (l.compareTo(new BigDecimal("60")) < 0) weak.add("逻辑思维");
        if (e.compareTo(new BigDecimal("60")) < 0) weak.add("沟通表达");
        if (a.compareTo(new BigDecimal("60")) < 0) weak.add("态度敷衍");
        if (weak.isEmpty()) return "表现良好";
        return String.join(",", weak);
    }
    
    /**
     * 使用题库关键词计算正确性分数 - 面试评分核心
     * 维度1：技术基础（最重要，占比30%）
     * 基于关键词匹配程度评分
     */
    private BigDecimal calcCorrectnessWithKeywords(String answer, List<String> questionKeywords) {
        if (!StringUtils.hasText(answer)) return new BigDecimal("0");
        
        if (questionKeywords == null || questionKeywords.isEmpty()) {
            return new BigDecimal(60);
        }
        
        String lowerAnswer = answer.toLowerCase();
        int hit = 0;
        for (String keyword : questionKeywords) {
            if (keyword != null && lowerAnswer.contains(keyword.toLowerCase())) {
                hit++;
            }
        }
        
        double hitRate = (double) hit / questionKeywords.size();
        
        // 技术基础评分：关键词匹配决定分数
        if (hit == 0) {
            return new BigDecimal("0");      // 完全没有匹配，得0分
        } else if (hitRate < 0.2) {
            return new BigDecimal("25");    // 匹配极少，25分
        } else if (hitRate < 0.4) {
            return new BigDecimal("50");    // 匹配较少，50分
        } else if (hitRate < 0.6) {
            return new BigDecimal("70");    // 基本匹配，70分
        } else if (hitRate < 0.8) {
            return new BigDecimal("85");    // 较好匹配，85分
        } else {
            return new BigDecimal("95");   // 优秀匹配，95分
        }
    }
    
    /**
     * 计算回答深度分 - 面试评分维度2：项目理解（占比25%）
     * 考察回答是否完整、有深度
     */
    private BigDecimal calcDepthScore(String answer) {
        if (!StringUtils.hasText(answer)) return new BigDecimal("0");
        
        int len = answer.length();
        
        // 回答太短说明不认真或不会
        if (len < 20) return new BigDecimal("10");
        if (len < 50) return new BigDecimal("30");
        if (len < 100) return new BigDecimal("50");
        if (len < 200) return new BigDecimal("70");
        if (len < 500) return new BigDecimal("85");
        return new BigDecimal("95");
    }
    
    /**
     * 计算逻辑思维分 - 面试评分维度3：逻辑思维（占比20%）
     * 考察回答是否有条理
     */
    private BigDecimal calcLogicScore(String answer) {
        if (!StringUtils.hasText(answer)) return new BigDecimal("0");
        
        // 检测逻辑连接词
        String[] logicWords = {"首先", "其次", "最后", "第一", "第二", "第三", "一方面", "另一方面", 
                               "首先", "其次", "然后", "最终", "综上所述",
                               "first", "second", "third", "finally", "lastly", "in conclusion"};
        int logicCount = 0;
        String lower = answer.toLowerCase();
        for (String word : logicWords) {
            if (lower.contains(word.toLowerCase())) {
                logicCount++;
            }
        }
        
        // 有1-2个连接词说明有基本逻辑
        if (logicCount == 0) return new BigDecimal("50");   // 无逻辑结构，50分
        if (logicCount == 1) return new BigDecimal("65");   // 有简单逻辑，65分
        if (logicCount == 2) return new BigDecimal("80");   // 有逻辑结构，80分
        return new BigDecimal("90");                          // 逻辑清晰，90分
    }
    
    /**
     * 计算表达分 - 面试评分维度4：沟通表达（占比15%）
     * 考察回答是否清晰、完整
     */
    private BigDecimal calcExpressionScore(String answer) {
        if (!StringUtils.hasText(answer)) return new BigDecimal("0");
        
        // 排除乱码和无意义回答
        if (isMeaninglessAnswer(answer)) return new BigDecimal("10");
        
        int len = answer.length();
        
        // 回答过短说明表达不完整
        if (len < 10) return new BigDecimal("20");
        if (len < 30) return new BigDecimal("40");
        if (len < 50) return new BigDecimal("60");
        if (len < 100) return new BigDecimal("75");
        return new BigDecimal("85");
    }

    /**
     * 创建下一道题目 - 始终从题库抽取新题，不再使用固定追问
     */
    private TurnVO createNextQuestion(SessionEntity session, TurnEntity currentTurn, ScoreEntity score) {
        int currentRound = Math.max(session.getCurrentRound() == null ? 0 : session.getCurrentRound(), 
                                   currentTurn.getRoundNo() == null ? 0 : currentTurn.getRoundNo());
        int totalRounds = session.getTotalRounds() == null ? 5 : session.getTotalRounds();
        if (currentRound >= totalRounds) {
            return null;
        }

        // 始终从题库获取真正的下一道题目
        String roleIdStr = String.valueOf(session.getRoleId());
        List<QuestionBankService.Question> allQuestions = questionBankService.getRandomQuestions(roleIdStr, 20);
        
        if (allQuestions == null || allQuestions.isEmpty()) {
            log.warn("题库为空，无法创建下一轮面试题目, roleId={}", roleIdStr);
            return null;
        }
        
        // 获取当前会话已使用过的题目内容
        List<String> usedQuestionTexts = turnMapper.selectBySessionId(session.getId()).stream()
                .map(TurnEntity::getQuestionText)
                .filter(text -> text != null)
                .collect(Collectors.toList());
        
        // 从题库中选择一道未被使用过的题目
        List<QuestionBankService.Question> unusedQuestions = allQuestions.stream()
                .filter(q -> q.getContent() != null)
                .filter(q -> usedQuestionTexts.stream().noneMatch(used -> used.contains(q.getContent().substring(0, Math.min(10, q.getContent().length())))))
                .collect(Collectors.toList());
        
        // 如果所有题目都用过了，就从剩余题目中随机选
        if (unusedQuestions.isEmpty()) {
            log.info("题库题目已用完，从剩余题目中随机选择");
            unusedQuestions = allQuestions;
        }
        
        // 随机选择一道题目
        int randomIndex = (int) (Math.random() * unusedQuestions.size());
        QuestionBankService.Question nextQ = unusedQuestions.get(randomIndex);
        
        log.info("创建第{}轮面试题: {}, 题目ID={}", currentRound + 1, nextQ.getContent().substring(0, Math.min(30, nextQ.getContent().length())), nextQ.getId());
        
        TurnEntity turn = new TurnEntity();
        turn.setSessionId(session.getId());
        turn.setRoundNo(currentRound + 1);
        turn.setTurnType("QUESTION");
        turn.setIsFollowUp(false);
        turn.setQuestionId(null);
        turn.setQuestionText(nextQ.getContent());
        turn.setAnswerMode("TEXT");
        turnMapper.insert(turn);

        // 更新会话当前轮次
        SessionEntity update = new SessionEntity();
        update.setId(session.getId());
        update.setCurrentRound(turn.getRoundNo());
        update.setLastActiveAt(LocalDateTime.now());
        baseMapper.updateById(update);
        
        return toTurnVO(turn);
    }

    private QuestionEntity pickNextQuestion(Long roleId, String difficulty, Long sessionId) {
        Set<Long> used = turnMapper.selectBySessionId(sessionId).stream()
                .map(TurnEntity::getQuestionId).filter(x -> x != null).collect(Collectors.toSet());

        List<QuestionEntity> candidates = questionMapper.selectPage(roleId, null, difficulty, true, null, 0, 100);
        QuestionEntity first = candidates.stream().filter(x -> !used.contains(x.getId())).findFirst().orElse(null);
        if (first != null) return first;

        candidates = questionMapper.selectPage(roleId, null, null, true, null, 0, 100);
        return candidates.stream().filter(x -> !used.contains(x.getId())).findFirst().orElse(null);
    }

    private TurnVO createFirstTurn(SessionEntity session, String reqDifficulty) {
        // 直接从 question-bank.json 获取题目
        String roleIdStr = String.valueOf(session.getRoleId());
        log.info("创建第一轮: sessionId=" + session.getId() + ", roleId=" + roleIdStr);
        
        // 检查题库加载情况
        List<QuestionBankService.RoleQuestions> allRoles = questionBankService.getAllRoles();
        log.info("题库已加载的岗位: " + allRoles.stream().map(r -> r.getRoleId() + "(" + r.getRoleName() + ")").collect(Collectors.joining(", ")));
        
        List<QuestionBankService.Question> questions = questionBankService.getRandomQuestions(roleIdStr, 10);
        
        if (questions == null || questions.isEmpty()) {
            log.error("题库为空，无法创建第一轮面试题目! roleId={}, 已加载岗位={}", roleIdStr, allRoles.stream().map(QuestionBankService.RoleQuestions::getRoleId).collect(Collectors.toList()));
            return null;
        }
        
        log.info("从题库获取到 " + questions.size() + " 道题目");
        QuestionBankService.Question q = questions.get(0);
        TurnEntity turn = new TurnEntity();
        turn.setSessionId(session.getId());
        turn.setRoundNo(1);
        turn.setTurnType("QUESTION");
        turn.setQuestionId(null); // 不设置外键，直接存储题目内容
        turn.setQuestionText(q.getContent());
        turn.setIsFollowUp(false);
        turn.setAnswerMode("TEXT");
        turnMapper.insert(turn);
        return toTurnVO(turn);
    }

    private BigDecimal calcCorrectness(TurnEntity turn) {
        String answer = answerOf(turn);
        if (!StringUtils.hasText(answer)) return new BigDecimal("20");
        QuestionEntity q = turn.getQuestionId() == null ? null : questionMapper.selectById(turn.getQuestionId());
        List<String> keywords = extractKeywords(q == null ? null : q.getKeywords());
        if (keywords.isEmpty()) return clamp(new BigDecimal(55 + Math.min(35, answer.length() / 8.0)));

        int hit = countContains(answer, keywords);
        return clamp(new BigDecimal(45 + ((double) hit / keywords.size()) * 45 + Math.min(10, answer.length() / 40.0)));
    }

    private List<String> buildHighlights(BigDecimal c, BigDecimal d, BigDecimal l, BigDecimal m, BigDecimal e) {
        List<String> list = new ArrayList<>();
        if (c.compareTo(new BigDecimal("80")) >= 0) list.add("技术准确性稳定。");
        if (d.compareTo(new BigDecimal("80")) >= 0) list.add("答案深度良好。");
        if (l.compareTo(new BigDecimal("80")) >= 0) list.add("逻辑结构清晰。");
        if (m.compareTo(new BigDecimal("80")) >= 0) list.add("岗位匹配度强。");
        if (e.compareTo(new BigDecimal("80")) >= 0) list.add("表达简洁自信。");
        if (list.isEmpty()) list.add("已建立基础面试能力。");
        return list;
    }

    private void createImproveTasks(Long userId, Long sessionId, List<String> improvements) {
        int limit = Math.min(2, improvements.size());
        for (int i = 0; i < limit; i++) {
            TaskEntity t = new TaskEntity();
            t.setUserId(userId);
            t.setSessionId(sessionId);
            t.setTitle("Improve: " + shortTitle(improvements.get(i)));
            t.setTaskType("MOCK");
            t.setContent(improvements.get(i));
            t.setStatus("TODO");
            t.setDueDate(LocalDate.now().plusDays(7));
            taskMapper.insert(t);
        }
    }

    private String buildSummary(BigDecimal overall) {
        if (overall.compareTo(new BigDecimal("85")) >= 0) return "整体面试表现优秀。";
        if (overall.compareTo(new BigDecimal("75")) >= 0) return "整体表现良好，有小幅提升空间。";
        if (overall.compareTo(new BigDecimal("60")) >= 0) return "整体表现可接受，建议持续专注练习。";
        return "核心面试能力仍需加强练习。";
    }

    private String shortTitle(String text) {
        String t = defaultIfBlank(text, "Interview Skill");
        return t.length() <= 60 ? t : t.substring(0, 60);
    }

    private String buildWeakPoints(BigDecimal c, BigDecimal d, BigDecimal l, BigDecimal m, BigDecimal e) {
        List<String> weak = new ArrayList<>();
        if (c.compareTo(new BigDecimal("70")) < 0) weak.add("准确性");
        if (d.compareTo(new BigDecimal("70")) < 0) weak.add("深度");
        if (l.compareTo(new BigDecimal("70")) < 0) weak.add("逻辑");
        if (m.compareTo(new BigDecimal("70")) < 0) weak.add("匹配度");
        if (e.compareTo(new BigDecimal("70")) < 0) weak.add("表达");
        return String.join(",", weak);
    }

    private List<String> extractKeywords(String raw) {
        if (!StringUtils.hasText(raw)) return List.of();
        return Arrays.stream(raw.split("[,锛?锛沑\s]+"))
                .map(String::trim).filter(StringUtils::hasText).distinct().collect(Collectors.toList());
    }

    private int countContains(String text, List<String> words) {
        if (!StringUtils.hasText(text) || words == null || words.isEmpty()) return 0;
        String lower = text.toLowerCase();
        int count = 0;
        for (String w : words) {
            if (lower.contains(w.toLowerCase())) count++;
        }
        return count;
    }

    private String answerOf(TurnEntity turn) {
        if (turn == null) return "";
        return StringUtils.hasText(turn.getAnswerText()) ? turn.getAnswerText() : defaultIfBlank(turn.getAsrText(), "");
    }

    private BigDecimal clamp(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        if (value.compareTo(new BigDecimal("100")) > 0) return new BigDecimal("100");
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal avg(List<BigDecimal> list) {
        List<BigDecimal> valid = list == null ? List.of() : list.stream().filter(x -> x != null).collect(Collectors.toList());
        if (valid.isEmpty()) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal sum = valid.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(valid.size()), 2, RoundingMode.HALF_UP);
    }

    private String buildTitle(Long roleId) {
        RoleEntity role = roleMapper.selectById(roleId);
        String roleName = role == null || !StringUtils.hasText(role.getName()) ? "Role-" + roleId : role.getName();
        return roleName + " Interview";
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private SessionVO toSessionVO(SessionEntity entity) {
        SessionVO vo = new SessionVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setRoleId(entity.getRoleId());
        vo.setTitle(entity.getTitle());
        vo.setStatus(entity.getStatus());
        vo.setTotalRounds(entity.getTotalRounds());
        vo.setCurrentRound(entity.getCurrentRound());
        vo.setDifficulty(entity.getDifficulty());
        vo.setInterviewStage(entity.getInterviewStage());
        vo.setFollowupMode(entity.getFollowupMode());
        vo.setInterviewType(entity.getInterviewType());
        vo.setTimeLimitSec(entity.getTimeLimitSec());
        vo.setAutoFinishAt(entity.getAutoFinishAt());
        vo.setVoiceEnabled(entity.getVoiceEnabled());
        vo.setVideoEnabled(entity.getVideoEnabled());
        vo.setStartedAt(entity.getStartedAt());
        vo.setEndedAt(entity.getEndedAt());
        vo.setFinishReason(entity.getFinishReason());
        vo.setDurationSec(entity.getDurationSec());
        vo.setOverallScore(entity.getOverallScore());
        vo.setEvaluationStatus(entity.getEvaluationStatus());
        vo.setEvaluationStartedAt(entity.getEvaluationStartedAt());
        vo.setEvaluationFinishedAt(entity.getEvaluationFinishedAt());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setLastActiveAt(entity.getLastActiveAt());
        return vo;
    }

    private TurnVO toTurnVO(TurnEntity entity) {
        TurnVO vo = new TurnVO();
        vo.setId(entity.getId());
        vo.setSessionId(entity.getSessionId());
        vo.setRoundNo(entity.getRoundNo());
        vo.setTurnType(entity.getTurnType());
        vo.setQuestionId(entity.getQuestionId());
        vo.setQuestionText(entity.getQuestionText());
        vo.setIsFollowUp(entity.getIsFollowUp());
        vo.setFollowUpReason(entity.getFollowUpReason());
        vo.setAnswerMode(entity.getAnswerMode());
        vo.setAnswerText(entity.getAnswerText());
        vo.setAudioUrl(entity.getAudioUrl());
        vo.setAsrText(entity.getAsrText());
        vo.setAiReplyText(entity.getAiReplyText());
        vo.setAiAdvice(entity.getAiAdvice());
        vo.setResponseSec(entity.getResponseSec());
        vo.setEvaluatedAt(entity.getEvaluatedAt());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }

    /**
     * 根据题目文本从题库中获取关键词
     */
    private List<String> getQuestionKeywords(Long roleId, String questionText) {
        List<String> keywords = new ArrayList<>();
        if (questionText == null || questionText.isEmpty()) {
            return keywords;
        }
        
        String roleIdStr = String.valueOf(roleId);
        List<QuestionBankService.Question> questions = questionBankService.getRandomQuestions(roleIdStr, 20);
        
        for (QuestionBankService.Question q : questions) {
            if (q.getContent() != null && q.getContent().contains(questionText.substring(0, Math.min(10, questionText.length())))) {
                keywords = q.getKeywords();
                break;
            }
            // 模糊匹配
            for (String kw : q.getKeywords()) {
                if (kw != null && questionText.contains(kw)) {
                    if (!keywords.contains(kw)) {
                        keywords.add(kw);
                    }
                }
            }
        }
        
        return keywords;
    }
}



