package com.aiinterview.ai.service;

import java.util.List;

/**
 * AI大模型服务接口
 * 支持面试出题、回答分析、追问、报告生成
 */
public interface AiService {

    /**
     * 根据岗位从知识库生成面试问题
     * @param roleId 岗位ID
     * @param roleName 岗位名称
     * @param knowledgeContent 知识库内容
     * @param round 第几轮面试
     * @return 面试问题
     */
    String generateQuestion(Long roleId, String roleName, String knowledgeContent, int round);

    /**
     * 分析用户回答并给出评价和建议
     * @param question 面试问题
     * @param userAnswer 用户回答
     * @param knowledgeContent 相关知识
     * @return 分析结果JSON
     */
    String analyzeAnswer(String question, String userAnswer, String knowledgeContent);

    /**
     * 根据上下文生成追问
     * @param question 当前问题
     * @param userAnswer 用户回答
     * @param history 对话历史
     * @return 追问内容
     */
    String generateFollowUp(String question, String userAnswer, List<String> history);

    /**
     * 生成面试总结报告
     * @param roleName 岗位名称
     * @param qaPairs 问答对列表
     * @return 报告JSON
     */
    String generateReport(String roleName, List<QaPair> qaPairs);

    /**
     * 问答对
     */
    class QaPair {
        private String question;
        private String answer;
        private String analysis;

        public QaPair(String question, String answer, String analysis) {
            this.question = question;
            this.answer = answer;
            this.analysis = analysis;
        }

        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
        public String getAnalysis() { return analysis; }
    }
}
