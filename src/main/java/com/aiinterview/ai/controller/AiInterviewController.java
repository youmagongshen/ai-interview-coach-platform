package com.aiinterview.ai.controller;

import com.aiinterview.ai.service.AiService;
import com.aiinterview.ai.service.QuestionBankService;
import com.aiinterview.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI面试控制器
 * 
 * 使用方法：
 * 1. 先配置API Key（见下方）
 * 2. 调用接口进行面试
 * 
 * API配置：
 * 在 application.yml 中添加：
 * zhipu:
 *   api:
 *     key: your-api-key
 * 
 * 获取API Key：https://open.bigmodel.cn/
 */
@Slf4j
@RestController
@RequestMapping("/inv/ai")
@RequiredArgsConstructor
public class AiInterviewController {

    private final AiService aiService;
    private final QuestionBankService questionBankService;

    /**
     * 测试API连接
     * GET /inv/ai/test
     */
    @GetMapping("/test")
    public ApiResponse<String> test() {
        try {
            String result = aiService.generateQuestion(1L, "后端", "Java基础知识：面向对象、集合、多线程", 1);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("AI测试失败", e);
            return ApiResponse.error("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 生成面试问题
     * POST /inv/ai/question
     * 
     * 请求体：
     * {
     *   "roleId": 1,
     *   "roleName": "后端",
     *   "knowledgeContent": "知识库内容...",
     *   "round": 1
     * }
     */
    @PostMapping("/question")
    public ApiResponse<String> generateQuestion(@RequestBody Map<String, Object> params) {
        try {
            Long roleId = Long.valueOf(params.get("roleId").toString());
            String roleName = params.get("roleName").toString();
            String knowledgeContent = params.get("knowledgeContent").toString();
            int round = Integer.parseInt(params.get("round").toString());

            String question = aiService.generateQuestion(roleId, roleName, knowledgeContent, round);
            return ApiResponse.success(question);
        } catch (Exception e) {
            log.error("生成问题失败", e);
            return ApiResponse.error("生成问题失败: " + e.getMessage());
        }
    }

    /**
     * 分析用户回答
     * POST /inv/ai/analyze
     * 
     * 请求体：
     * {
     *   "question": "问题内容",
     *   "userAnswer": "用户回答",
     *   "knowledgeContent": "相关知识"
     * }
     */
    @PostMapping("/analyze")
    public ApiResponse<String> analyzeAnswer(@RequestBody Map<String, Object> params) {
        try {
            String question = params.get("question").toString();
            String userAnswer = params.get("userAnswer").toString();
            String knowledgeContent = params.get("knowledgeContent").toString();

            String analysis = aiService.analyzeAnswer(question, userAnswer, knowledgeContent);
            return ApiResponse.success(analysis);
        } catch (Exception e) {
            log.error("分析回答失败", e);
            return ApiResponse.error("分析失败: " + e.getMessage());
        }
    }

    /**
     * 生成追问
     * POST /inv/ai/followup
     * 
     * 请求体：
     * {
     *   "question": "当前问题",
     *   "userAnswer": "用户回答",
     *   "history": ["历史对话1", "历史对话2"]
     * }
     */
    @PostMapping("/followup")
    public ApiResponse<String> generateFollowUp(@RequestBody Map<String, Object> params) {
        try {
            String question = params.get("question").toString();
            String userAnswer = params.get("userAnswer").toString();
            @SuppressWarnings("unchecked")
            List<String> history = (List<String>) params.get("history");

            String followUp = aiService.generateFollowUp(question, userAnswer, history);
            return ApiResponse.success(followUp);
        } catch (Exception e) {
            log.error("生成追问失败", e);
            return ApiResponse.error("生成追问失败: " + e.getMessage());
        }
    }

    /**
     * 生成面试报告
     * POST /inv/ai/report
     * 
     * 请求体：
     * {
     *   "roleName": "后端",
     *   "qaPairs": [
     *     {"question": "问题1", "answer": "回答1", "analysis": "分析1"},
     *     {"question": "问题2", "answer": "回答2", "analysis": "分析2"}
     *   ]
     * }
     */
    @PostMapping("/report")
    public ApiResponse<String> generateReport(@RequestBody Map<String, Object> params) {
        try {
            String roleName = params.get("roleName").toString();
            @SuppressWarnings("unchecked")
            List<Map<String, String>> qaList = (List<Map<String, String>>) params.get("qaPairs");

            List<AiService.QaPair> qaPairs = qaList.stream()
                    .map(q -> new AiService.QaPair(q.get("question"), q.get("answer"), q.get("analysis")))
                    .collect(java.util.stream.Collectors.toList());

            String report = aiService.generateReport(roleName, qaPairs);
            return ApiResponse.success(report);
        } catch (Exception e) {
            log.error("生成报告失败", e);
            return ApiResponse.error("生成报告失败: " + e.getMessage());
        }
    }

    /**
     * 从题库随机抽题
     * GET /inv/ai/random-questions?roleId=1&count=5
     * 
     * @param roleId 岗位ID (1:后端, 2:前端)
     * @param count 题目数量，默认5道
     * @param type 题目类型（可选：专业知识、情景题、行为题），不传则所有类型
     * @return 随机抽取的题目列表
     */
    @GetMapping("/random-questions")
    public ApiResponse<List<QuestionBankService.Question>> getRandomQuestions(
            @RequestParam String roleId,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(required = false) String type) {
        try {
            List<QuestionBankService.Question> questions;
            if (type != null && !type.isEmpty()) {
                // 按类型抽题
                questions = questionBankService.getRandomQuestionsByType(roleId, type, count);
            } else {
                // 全部抽题
                questions = questionBankService.getRandomQuestions(roleId, count);
            }
            return ApiResponse.success(questions);
        } catch (Exception e) {
            log.error("随机抽题失败", e);
            return ApiResponse.error("随机抽题失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定岗位的题目（按类型分组）
     * GET /inv/ai/questions-by-type?roleId=1
     * 
     * @param roleId 岗位ID
     * @return 按类型分组的题目
     */
    @GetMapping("/questions-by-type")
    public ApiResponse<Map<String, List<QuestionBankService.Question>>> getQuestionsByType(@RequestParam String roleId) {
        try {
            Map<String, List<QuestionBankService.Question>> questions = questionBankService.getQuestionsByType(roleId);
            return ApiResponse.success(questions);
        } catch (Exception e) {
            log.error("获取题目失败", e);
            return ApiResponse.error("获取题目失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有岗位列表
     * GET /inv/ai/roles
     * 
     * @return 岗位列表
     */
    @GetMapping("/roles")
    public ApiResponse<List<QuestionBankService.RoleQuestions>> getAllRoles() {
        try {
            List<QuestionBankService.RoleQuestions> roles = questionBankService.getAllRoles();
            return ApiResponse.success(roles);
        } catch (Exception e) {
            log.error("获取岗位列表失败", e);
            return ApiResponse.error("获取岗位列表失败: " + e.getMessage());
        }
    }
}
