# 07 大模型接入方法（Java Spring Boot）

## 目标

在现有 Spring Boot 项目中接入大模型，完成以下能力：

1. 面试官提问/追问
2. 回答评分（结构化 JSON）
3. 生成面试报告

## 推荐方案

- Java 编排框架：LangChain4j
- 模型接入方式：OpenAI 兼容 API（可替换为通义/豆包/其他）
- 语音识别：独立 ASR API（讯飞/阿里云/Whisper 服务）

## Maven 依赖示例

```xml
<dependency>
  <groupId>dev.langchain4j</groupId>
  <artifactId>langchain4j</artifactId>
  <version>0.35.0</version>
</dependency>
<dependency>
  <groupId>dev.langchain4j</groupId>
  <artifactId>langchain4j-open-ai</artifactId>
  <version>0.35.0</version>
</dependency>
```

## 配置（application.yml）

```yml
llm:
  base-url: https://api.openai.com/v1
  api-key: ${LLM_API_KEY}
  model: gpt-4o-mini
  temperature: 0.2
```

## 配置类示例

```java
@Configuration
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
    private Double temperature = 0.2;
}

@Configuration
@RequiredArgsConstructor
public class LlmConfig {
    private final LlmProperties p;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(p.getBaseUrl())
                .apiKey(p.getApiKey())
                .modelName(p.getModel())
                .temperature(p.getTemperature())
                .build();
    }
}
```

## LLM 服务封装（建议统一出口）

```java
@Service
@RequiredArgsConstructor
public class LlmService {
    private final ChatLanguageModel model;

    public String ask(String systemPrompt, String userPrompt) {
        return model.generate(
                List.of(
                        SystemMessage.from(systemPrompt),
                        UserMessage.from(userPrompt)
                )
        ).content().text();
    }
}
```

## 三类 Prompt

1. 面试官 Prompt：输入岗位、轮次、历史回答，输出下一题或追问。
2. 评分 Prompt：输入题目+回答+参考要点，输出固定 JSON。
3. 报告 Prompt：输入多轮评分结果，输出亮点/不足/改进计划。

## 评分 JSON 建议

```json
{
  "correctness": 0,
  "depth": 0,
  "logic": 0,
  "match": 0,
  "expression": 0,
  "total": 0,
  "evidence": ["..."],
  "weak_points": ["..."],
  "improvement_suggestions": ["..."],
  "follow_up_needed": true,
  "follow_up_question": "..."
}
```

## 追问策略（建议规则）

- 任一核心维度低于阈值（如 correctness < 18）-> 触发追问
- 关键词命中不足 -> 触发追问
- 每题最多追问 1 次，避免无限对话

## 接口调用链

1. /interview/start -> 生成首题
2. /interview/answer -> LLM评分 + 判断追问/下一题
3. /interview/end -> 汇总评分 -> LLM生成报告
4. /interview/{id}/report -> 返回结构化报告

## 异常与降级

1. 模型超时：返回“稍后重试”，并记录失败日志
2. JSON 解析失败：二次请求“仅返回JSON”
3. 连续失败：启用本地规则评分兜底（关键词+长度+结构）

## 安全建议

1. API Key 放环境变量，不写死代码
2. 记录调用日志时脱敏处理
3. 对用户输入做长度限制和敏感词过滤