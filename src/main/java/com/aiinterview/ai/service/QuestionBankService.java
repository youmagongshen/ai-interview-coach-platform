package com.aiinterview.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题库服务
 * 负责读取和管理面试题库
 */
@Slf4j
@Service
public class QuestionBankService {

    private Map<String, RoleQuestions> questionBank = new HashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        loadQuestionBank();
    }

    /**
     * 加载题库文件
     */
    private void loadQuestionBank() {
        // 加载后端题库
        loadQuestionBankFromFile("question-bank.json");
        // 加载前端题库
        loadQuestionBankFromFile("question-web.json");
        log.info("题库加载完成，共 {} 个岗位: {}", questionBank.size(), questionBank.keySet());
    }
    
    /**
     * 从指定文件加载题库
     */
    private void loadQuestionBankFromFile(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource(filename);
            log.info("尝试加载题库文件: {} - {}", filename, resource.exists() ? "存在" : "不存在");
            
            if (!resource.exists()) {
                log.warn("题库文件不存在: {}", filename);
                return;
            }
            
            log.info("题库文件路径: {}", resource.getURL());
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(resource.getInputStream(), Map.class);
            log.info("JSON解析成功，根节点: {}", data.keySet());
            
            Map<String, Object> roles = (Map<String, Object>) data.get("roles");
            if (roles == null) {
                log.error("JSON中没有roles节点，可用节点: {}", data.keySet());
                return;
            }
            log.info("找到roles节点，包含岗位: {}", roles.keySet());
            
            for (Map.Entry<String, Object> entry : roles.entrySet()) {
                String roleId = entry.getKey();
                log.info("处理岗位: {} = {}", roleId, entry.getValue().getClass().getName());
                
                @SuppressWarnings("unchecked")
                Map<String, Object> roleData = (Map<String, Object>) entry.getValue();
                String roleName = (String) roleData.get("name");
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> questions = (List<Map<String, Object>>) roleData.get("questions");
                if (questions == null) {
                    log.warn("岗位 {} 没有questions或questions为null", roleId);
                    continue;
                }
                
                log.info("岗位 {} ({}) 有 {} 道题目", roleId, roleName, questions.size());
                
                List<Question> questionList = questions.stream()
                        .map(q -> new Question(
                                ((Number) q.get("id")).intValue(),
                                (String) q.get("type"),
                                (String) q.get("content"),
                                (List<String>) q.get("keywords"),
                                q.get("answer") != null ? (String) q.get("answer") : ""
                        ))
                        .collect(Collectors.toList());
                
                // 如果岗位已存在，合并题目；否则新增
                if (questionBank.containsKey(roleId)) {
                    RoleQuestions existing = questionBank.get(roleId);
                    existing.getQuestions().addAll(questionList);
                    log.info("岗位 {} 合并后共有 {} 道题目", roleId, existing.getQuestions().size());
                } else {
                    questionBank.put(roleId, new RoleQuestions(roleId, roleName, questionList));
                }
            }
            
        } catch (IOException e) {
            log.error("加载题库失败 - IOException: {}", filename, e);
        } catch (Exception e) {
            log.error("加载题库失败 - 未知异常: {}", filename, e);
        }
    }

    /**
     * 获取指定岗位的随机题目
     * @param roleId 岗位ID (1:后端, 2:前端)
     * @param count 题目数量
     * @return 题目列表
     */
    public List<Question> getRandomQuestions(String roleId, int count) {
        RoleQuestions roleQuestions = questionBank.get(roleId);
        if (roleQuestions == null) {
            log.warn("题库中未找到岗位: {}, 已加载的岗位: {}", roleId, questionBank.keySet());
            return Collections.emptyList();
        }
        
        List<Question> allQuestions = roleQuestions.getQuestions();
        log.info("岗位 {} 获取到 {} 道题目", roleId, allQuestions.size());
        if (allQuestions.size() <= count) {
            return new ArrayList<>(allQuestions);
        }
        
        // 随机打乱并取前count个
        List<Question> shuffled = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, count);
    }

    /**
     * 获取指定岗位的随机题目（按类型筛选）
     * @param roleId 岗位ID
     * @param type 题目类型（如：专业知识、情景题、行为题）
     * @param count 题目数量
     * @return 题目列表
     */
    public List<Question> getRandomQuestionsByType(String roleId, String type, int count) {
        RoleQuestions roleQuestions = questionBank.get(roleId);
        if (roleQuestions == null) {
            log.warn("题库中未找到岗位: {}", roleId);
            return Collections.emptyList();
        }
        
        // 筛选指定类型的题目
        List<Question> filteredQuestions = roleQuestions.getQuestions().stream()
                .filter(q -> q.getType().equals(type))
                .collect(Collectors.toList());
        
        log.info("岗位 {} 类型 {} 获取到 {} 道题目", roleId, type, filteredQuestions.size());
        
        if (filteredQuestions.size() <= count) {
            return new ArrayList<>(filteredQuestions);
        }
        
        // 随机打乱并取前count个
        List<Question> shuffled = new ArrayList<>(filteredQuestions);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, count);
    }

    /**
     * 获取指定岗位的题目（按类型分组）
     */
    public Map<String, List<Question>> getQuestionsByType(String roleId) {
        RoleQuestions roleQuestions = questionBank.get(roleId);
        if (roleQuestions == null) {
            return Collections.emptyMap();
        }
        
        return roleQuestions.getQuestions().stream()
                .collect(Collectors.groupingBy(Question::getType));
    }

    /**
     * 获取指定岗位某道题目的详细信息
     * @param roleId 岗位ID
     * @param questionId 题目ID
     * @return 题目对象
     */
    public Question getQuestionById(String roleId, int questionId) {
        RoleQuestions roleQuestions = questionBank.get(roleId);
        if (roleQuestions == null) {
            return null;
        }
        
        return roleQuestions.getQuestions().stream()
                .filter(q -> q.getId() == questionId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有岗位
     */
    public List<RoleQuestions> getAllRoles() {
        return new ArrayList<>(questionBank.values());
    }

    /**
     * 题目类
     */
    public static class Question {
        private int id;
        private String type;
        private String content;
        private List<String> keywords;
        private String answer; // 标准答案

        public Question(int id, String type, String content, List<String> keywords, String answer) {
            this.id = id;
            this.type = type;
            this.content = content;
            this.keywords = keywords;
            this.answer = answer;
        }

        public int getId() { return id; }
        public String getType() { return type; }
        public String getContent() { return content; }
        public List<String> getKeywords() { return keywords; }
        public String getAnswer() { return answer; }
    }

    /**
     * 岗位题目类
     */
    public static class RoleQuestions {
        private String roleId;
        private String roleName;
        private List<Question> questions;

        public RoleQuestions(String roleId, String roleName, List<Question> questions) {
            this.roleId = roleId;
            this.roleName = roleName;
            this.questions = questions;
        }

        public String getRoleId() { return roleId; }
        public String getRoleName() { return roleName; }
        public List<Question> getQuestions() { return questions; }
    }
}
