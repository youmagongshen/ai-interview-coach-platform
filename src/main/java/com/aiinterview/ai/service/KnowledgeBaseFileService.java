package com.aiinterview.ai.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 知识库服务 - 读取本地文件系统中的知识库文档
 * 根据题目关键词匹配相关知识库内容，用于面试评估和追问
 */
@Service
@Slf4j
public class KnowledgeBaseFileService {

    // 知识库根目录（相对于classpath或文件系统）
    @Value("${knowledge.base-path:knowledge-base}")
    private String knowledgeBasePath;

    // 知识库缓存：roleId -> 关键词 -> 知识库内容
    private final Map<String, Map<String, KnowledgeDoc>> knowledgeCache = new ConcurrentHashMap<>();

    /**
     * 关键词到知识库文件的映射
     */
    private static final Map<String, String> KEYWORD_TO_FILE = new HashMap<>();
    
    static {
        // HTTP相关
        KEYWORD_TO_FILE.put("HTTP", "HTTP与HTTPS.md");
        KEYWORD_TO_FILE.put("HTTPS", "HTTP与HTTPS.md");
        KEYWORD_TO_FILE.put("SSL", "HTTP与HTTPS.md");
        KEYWORD_TO_FILE.put("TLS", "HTTP与HTTPS.md");
        KEYWORD_TO_FILE.put("加密", "HTTP与HTTPS.md");
        
        // MySQL索引相关
        KEYWORD_TO_FILE.put("MySQL", "MySQL索引.md");
        KEYWORD_TO_FILE.put("索引", "MySQL索引.md");
        KEYWORD_TO_FILE.put("B+树", "MySQL索引.md");
        
        // MySQL事务相关
        KEYWORD_TO_FILE.put("事务", "MySQL事务.md");
        KEYWORD_TO_FILE.put("ACID", "MySQL事务.md");
        KEYWORD_TO_FILE.put("隔离级别", "MySQL事务.md");
        KEYWORD_TO_FILE.put("脏读", "MySQL事务.md");
        KEYWORD_TO_FILE.put("幻读", "MySQL事务.md");
        
        // Redis相关
        KEYWORD_TO_FILE.put("Redis", "Redis缓存.md");
        KEYWORD_TO_FILE.put("缓存穿透", "Redis缓存.md");
        KEYWORD_TO_FILE.put("缓存击穿", "Redis缓存.md");
        KEYWORD_TO_FILE.put("缓存雪崩", "Redis缓存.md");
        KEYWORD_TO_FILE.put("布隆过滤器", "Redis缓存.md");
        
        // RESTful API相关
        KEYWORD_TO_FILE.put("RESTful", "RESTful API设计.md");
        KEYWORD_TO_FILE.put("API设计", "RESTful API设计.md");
        
        // JWT相关
        KEYWORD_TO_FILE.put("JWT", "JWT令牌.md");
        KEYWORD_TO_FILE.put("Token", "JWT令牌.md");
        KEYWORD_TO_FILE.put("Session", "JWT令牌.md");
        KEYWORD_TO_FILE.put("鉴权", "JWT令牌.md");
        
        // 性能排查相关
        KEYWORD_TO_FILE.put("性能问题", "线上问题排查.md");
        KEYWORD_TO_FILE.put("排查", "线上问题排查.md");
        KEYWORD_TO_FILE.put("线程池", "线上问题排查.md");
        KEYWORD_TO_FILE.put("数据库慢查询", "线上问题排查.md");
        
        // 死锁和幂等相关
        KEYWORD_TO_FILE.put("死锁", "数据库死锁.md");
        KEYWORD_TO_FILE.put("接口幂等", "接口幂等与防重.md");
        KEYWORD_TO_FILE.put("幂等", "接口幂等与防重.md");
    }

    /**
     * 知识库文档类
     */
    public static class KnowledgeDoc {
        private String fileName;
        private String content;
        private List<String> sections;

        public KnowledgeDoc(String fileName, String content) {
            this.fileName = fileName;
            this.content = content;
            this.sections = extractSections(content);
        }

        public String getFileName() { return fileName; }
        public String getContent() { return content; }
        public List<String> getSections() { return sections; }

        private List<String> extractSections(String content) {
            List<String> sections = new ArrayList<>();
            String[] lines = content.split("\n");
            StringBuilder current = new StringBuilder();
            for (String line : lines) {
                if (line.startsWith("#")) {
                    if (current.length() > 0) {
                        sections.add(current.toString().trim());
                        current = new StringBuilder();
                    }
                }
                current.append(line).append("\n");
            }
            if (current.length() > 0) {
                sections.add(current.toString().trim());
            }
            return sections;
        }
    }

    @PostConstruct
    public void init() {
        loadKnowledgeBase();
    }

    /**
     * 加载知识库文件
     */
    private void loadKnowledgeBase() {
        try {
            // 尝试从文件系统加载
            Path basePath = Paths.get(knowledgeBasePath);
            if (!Files.exists(basePath)) {
                // 尝试从classpath加载
                log.info("知识库路径不存在，尝试从classpath加载: {}", knowledgeBasePath);
                return;
            }

            // 加载java-backend目录
            Path javaBackendPath = basePath.resolve("java-backend");
            if (Files.exists(javaBackendPath)) {
                loadDirectory(javaBackendPath, "1");
            }

            // 加载web-frontend目录
            Path webFrontendPath = basePath.resolve("web-frontend");
            if (Files.exists(webFrontendPath)) {
                loadDirectory(webFrontendPath, "2");
            }

            log.info("知识库加载完成，共 {} 个岗位", knowledgeCache.size());
        } catch (Exception e) {
            log.error("加载知识库失败", e);
        }
    }

    private void loadDirectory(Path dirPath, String roleId) {
        try {
            Map<String, KnowledgeDoc> docMap = new ConcurrentHashMap<>();
            Files.list(dirPath)
                    .filter(p -> p.toString().endsWith(".md"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            String fileName = path.getFileName().toString();
                            docMap.put(fileName, new KnowledgeDoc(fileName, content));
                            log.info("加载知识库文件: {}", fileName);
                        } catch (IOException e) {
                            log.error("读取知识库文件失败: {}", path, e);
                        }
                    });
            knowledgeCache.put(roleId, docMap);
        } catch (IOException e) {
            log.error("加载知识库目录失败: {}", dirPath, e);
        }
    }

    /**
     * 根据关键词获取相关的知识库内容
     * @param roleId 岗位ID (1:后端, 2:前端)
     * @param keywords 题目关键词列表
     * @return 匹配的知识库内容列表
     */
    public List<String> getRelevantKnowledge(String roleId, List<String> keywords) {
        List<String> results = new ArrayList<>();
        Map<String, KnowledgeDoc> docs = knowledgeCache.get(roleId);
        
        if (docs == null || docs.isEmpty()) {
            log.warn("未找到知识库，roleId={}", roleId);
            return results;
        }

        // 根据关键词匹配知识库文件
        Set<String> matchedFiles = new HashSet<>();
        for (String keyword : keywords) {
            if (keyword == null) continue;
            
            // 精确匹配
            String fileName = KEYWORD_TO_FILE.get(keyword);
            if (fileName != null) {
                matchedFiles.add(fileName);
            }
            
            // 模糊匹配
            for (Map.Entry<String, String> entry : KEYWORD_TO_FILE.entrySet()) {
                if (keyword.contains(entry.getKey()) || entry.getKey().contains(keyword)) {
                    matchedFiles.add(entry.getValue());
                }
            }
        }

        // 获取匹配文件的内容
        for (String fileName : matchedFiles) {
            KnowledgeDoc doc = docs.get(fileName);
            if (doc != null) {
                results.add(doc.getContent());
            }
        }

        return results;
    }

    /**
     * 根据关键词获取追问建议
     * @param roleId 岗位ID
     * @param keywords 题目关键词
     * @param userAnswer 用户回答
     * @return 追问内容
     */
    public String generateFollowUpQuestion(String roleId, List<String> keywords, String userAnswer) {
        List<String> knowledge = getRelevantKnowledge(roleId, keywords);
        
        // 根据知识库生成追问
        StringBuilder followUp = new StringBuilder();
        
        for (String kw : keywords) {
            if (kw == null) continue;
            
            // 根据关键词添加针对性的追问
            switch (kw) {
                case "索引":
                    followUp.append("你能详细说说B+树的结构吗？它为什么适合作为索引？");
                    break;
                case "事务":
                    followUp.append("能举例说明一下脏读和幻读的区别吗？");
                    break;
                case "缓存穿透":
                    followUp.append("布隆过滤器的工作原理了解吗？它有什么优缺点？");
                    break;
                case "Redis":
                    followUp.append("Redis除了做缓存，还能用在哪些场景？");
                    break;
                case "JWT":
                    followUp.append("JWT适合在什么场景使用？如果需要强制下线用户该怎么处理？");
                    break;
                case "HTTP":
                case "HTTPS":
                    followUp.append("HTTPS的TLS握手过程了解吗？能详细说说吗？");
                    break;
                case "RESTful":
                    followUp.append("RESTful API设计时，如何处理版本迭代？");
                    break;
                case "性能问题":
                    followUp.append("如果让你用Linux命令排查CPU占用高的问题，你会怎么操作？");
                    break;
                case "死锁":
                    followUp.append("如何避免死锁？有没有实际案例可以分享？");
                    break;
                case "接口幂等":
                    followUp.append("接口幂等性和防重放攻击有什么区别？");
                    break;
                default:
                    // 从知识库中提取相关内容生成追问
                    for (String content : knowledge) {
                        if (content.contains(kw) && content.length() > 100) {
                            // 提取包含关键词的段落作为追问
                            String[] lines = content.split("\n");
                            for (String line : lines) {
                                if (line.contains(kw) && line.length() > 20 && !line.startsWith("#")) {
                                    followUp.append("关于").append(kw).append("：").append(line.trim()).append("能详细说说吗？");
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
            
            if (followUp.length() > 0) {
                break; // 只生成一个追问
            }
        }

        // 如果没有匹配到关键词，使用通用追问
        if (followUp.length() == 0) {
            followUp.append("能展开说说你的理解吗？有没有实际项目经验可以分享？");
        }

        return followUp.toString();
    }

    /**
     * 评估用户回答与知识库的匹配度
     * @param roleId 岗位ID
     * @param keywords 题目关键词
     * @param userAnswer 用户回答
     * @return 评估结果
     */
    public EvaluationResult evaluateAnswer(String roleId, List<String> keywords, String userAnswer) {
        EvaluationResult result = new EvaluationResult();
        
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            result.setScore(0);
            result.setFeedback("回答为空，请输入你的答案。");
            return result;
        }

        List<String> knowledge = getRelevantKnowledge(roleId, keywords);
        
        // 计算关键词覆盖率
        int matchedKeywords = 0;
        StringBuilder feedback = new StringBuilder();
        
        for (String keyword : keywords) {
            if (keyword == null) continue;
            
            // 检查回答中是否包含关键词（不区分大小写）
            if (userAnswer.toLowerCase().contains(keyword.toLowerCase())) {
                matchedKeywords++;
            } else {
                // 检查相关词
                boolean relatedFound = false;
                for (String kw : KEYWORD_TO_FILE.keySet()) {
                    if (userAnswer.toLowerCase().contains(kw.toLowerCase())) {
                        matchedKeywords++;
                        relatedFound = true;
                        break;
                    }
                }
                
                if (!relatedFound && feedback.length() < 100) {
                    // 添加缺失关键词的建议
                    feedback.append("建议提及[").append(keyword).append("]相关概念。");
                }
            }
        }

        // 计算得分
        double coverage = keywords.isEmpty() ? 0 : (matchedKeywords * 100.0 / keywords.size());
        int baseScore = Math.min(80, 30 + (int)(coverage * 0.5));
        
        // 答案长度加分
        if (userAnswer.length() > 100) baseScore += 5;
        if (userAnswer.length() > 200) baseScore += 5;
        if (userAnswer.contains("首先") || userAnswer.contains("其次") || userAnswer.contains("最后")) baseScore += 5;
        
        result.setScore(Math.min(100, baseScore));
        
        if (coverage >= 70) {
            result.setFeedback("回答得不错，涵盖了主要知识点！");
        } else if (coverage >= 40) {
            result.setFeedback(feedback.length() > 0 ? feedback.toString() : "回答基本正确，可以更全面一些。");
        } else {
            result.setFeedback("建议补充更多" + (keywords.isEmpty() ? "相关" : keywords.get(0)) + "的知识点。");
        }

        return result;
    }

    /**
     * 评估结果类
     */
    public static class EvaluationResult {
        private int score;
        private String feedback;

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
    }
}
