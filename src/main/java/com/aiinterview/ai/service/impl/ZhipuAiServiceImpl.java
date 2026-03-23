package com.aiinterview.ai.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aiinterview.ai.service.AiService;
import com.aiinterview.ai.service.QuestionBankService;
import com.aiinterview.ai.service.QuestionBankService.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智谱清言 GLM-4 大模型服务实现
 * 
 * 使用方法：
 * 1. 前往 https://open.bigmodel.cn/ 注册账号
 * 2. 在控制台获取 API Key
 * 3. 在 application.yml 中配置 zhipu.api.key=你的API密钥
 */
@Slf4j
@Service
public class ZhipuAiServiceImpl implements AiService {

    /**
     * ====== 配置区域 ======
     * 请在 application.yml 中配置:
     * zhipu:
     *   api:
     *     key: your-api-key-here
     * 
     * 获取API Key: https://open.bigmodel.cn/
     */
    
    @Value("${zhipu.api.key:}")
    private String apiKey;
    
    private final QuestionBankService questionBankService;

    @Autowired
    public ZhipuAiServiceImpl(QuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String MODEL = "glm-4";

    @Override
    public String generateQuestion(Long roleId, String roleName, String knowledgeContent, int round) {
        // 优先使用题库，如果题库为空则使用AI生成
        List<Question> questions = questionBankService.getRandomQuestions(String.valueOf(roleId), round);
        if (questions != null && questions.size() >= round) {
            return questions.get(round - 1).getContent();
        }
        
        // 题库不够时使用AI生成
        String prompt = buildQuestionPrompt(roleName, knowledgeContent, round);
        return chat(prompt);
    }

    @Override
    public String analyzeAnswer(String question, String userAnswer, String knowledgeContent) {
        String prompt = buildAnalysisPrompt(question, userAnswer, knowledgeContent);
        return chat(prompt);
    }

    @Override
    public String generateFollowUp(String question, String userAnswer, List<String> history) {
        String prompt = buildFollowUpPrompt(question, userAnswer, history);
        return chat(prompt);
    }

 @Override
    public String generateReport(String roleName, List<QaPair> qaPairs) {
        String prompt = buildReportPrompt(roleName, qaPairs);
        return chat(prompt);
    }

    /**
     * 调用智谱API
     */
    private String chat(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "{\"error\": \"请先配置智谱API Key！配置方法：在application.yml中添加：zhipu: api: key: your-api-key\"}";
        }
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", MODEL);
            requestBody.set("temperature", 0.7);

            List<JSONObject> messages = new ArrayList<>();
            JSONObject systemMsg = new JSONObject();
            systemMsg.set("role", "system");
            systemMsg.set("content", "你是一个专业的AI面试助手，擅长根据岗位要求和知识库内容进行面试。回答要专业、准确、简洁。");
            messages.add(systemMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.set("role", "user");
            userMsg.set("content", prompt);
            messages.add(userMsg);

            requestBody.set("messages", messages);

            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                if (result.containsKey("choices")) {
                    var choices = result.getJSONArray("choices");
                    if (choices != null && choices.size() > 0) {
                        return choices.getJSONObject(0).getJSONObject("message").getStr("content");
                    }
                }
                return result.toString();
            } else {
                log.error("智谱API调用失败: {}", response.body());
                return "{\"error\": \"API调用失败: " + response.getStatus() + "\"}";
            }
        } catch (Exception e) {
            log.error("智谱API调用异常", e);
            return "{\"error\": \"调用异常: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 构建问题生成提示
     */
    private String buildQuestionPrompt(String roleName, String knowledgeContent, int round) {
        return """
            你是一个专业的面试官，现在要根据以下知识库内容为%s岗位生成面试问题。

            知识库内容：
            %s

            请生成第%d道面试问题，要求：
            1. 问题要基于上面的知识库内容
            2. 难度适中，能考察应聘者的专业知识
            3. 至少包含2-3个追问点
            4. 直接输出问题，不要输出其他内容

            问题：
            """.formatted(roleName, knowledgeContent, round);
    }

    /**
     * 构建回答分析提示
     */
    private String buildAnalysisPrompt(String question, String userAnswer, String knowledgeContent) {
        return """
            请分析以下面试回答：

            面试问题：%s

            考生回答：%s

            相关知识：
            %s

            请从以下维度进行分析（JSON格式输出）：
            {
                "score": 分数(0-100),
                "strengths": ["优点1", "优点2"],
                "weaknesses": ["不足1", "不足2"],
                "suggestions": ["改进建议1", "改进建议2"],
                "nextQuestion": "追问问题(可选)"
            }

            分析结果：
            """.formatted(question, userAnswer, knowledgeContent);
    }

    /**
     * 构建追问提示
     */
    private String buildFollowUpPrompt(String question, String userAnswer, List<String> history) {
        String historyStr = history.stream()
                .collect(Collectors.joining("\n"));
        
        return """
            基于以下对话历史，生成追问问题：

            历史对话：
            %s

            当前问题：%s
            当前回答：%s

            请基于用户的回答生成追问，问题要深入挖掘用户的知识掌握程度。
            如果用户回答已经足够完整，可以结束该轮面试。

            追问：
            """.formatted(historyStr, question, userAnswer);
    }

    /**
     * 构建报告生成提示
     */
    private String buildReportPrompt(String roleName, List<QaPair> qaPairs) {
        StringBuilder qaBuilder = new StringBuilder();
        for (int i = 0; i < qaPairs.size(); i++) {
            QaPair qa = qaPairs.get(i);
            qaBuilder.append(String.format("""
                第%d题：
                问题：%s
                回答：%s
                分析：%s
                """, i + 1, qa.getQuestion(), qa.getAnswer(), qa.getAnalysis()));
        }

        return """
            请为%s岗位生成一份完整的面试评估报告：

            面试问答记录：
            %s

            请生成以下格式的报告（JSON格式）：
            {
                "overallScore": 总分(0-100),
                "summary": "总体评价摘要",
                "dimensions": {
                    "technical": {"score": 技术能力分数, "comment": "评价"},
                    "logic": {"score": 逻辑思维分数, "comment": "评价"},
                    "expression": {"score": 表达能力分数, "comment": "评价"},
                    "depth": {"score": 深度分析分数, "comment": "评价"}
                },
                "highlights": ["亮点1", "亮点2"],
                "improvements": ["需要改进1", "需要改进2"],
                "advice": "综合建议"
            }

            报告：
            """.formatted(roleName, qaBuilder.toString());
    }
}
