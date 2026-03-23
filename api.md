# API 文档

Base URL: `/inv`  
Auth: `Authorization: Bearer <accessToken>`（除登录/注册/刷新外都需要）

## 1. 统一约定

### 1.1 统一响应
```json
{
  "code": 0,
  "message": "ok",
  "data": {},
  "requestId": "9f6f9f8f0b31447b"
}
```

### 1.2 分页响应
```json
{
  "list": [],  
  "page": 1,   
  "pageSize": 10,
  "total": 0,
  "hasMore": false
}
```

### 1.3 枚举建议
- `sessionStatus`: `DRAFT` | `IN_PROGRESS` | `FINISHED` | `ABANDONED`
- `difficulty`: `JUNIOR` | `MIDDLE` | `SENIOR`
- `followupMode`: `AUTO` | `MANUAL` | `OFF`
- `interviewType`: `TEXT` | `VOICE` | `VIDEO`
- `turnType`: `QUESTION` | `FOLLOW_UP`
- `answerMode`: `TEXT` | `VOICE` | `VIDEO`
- `finishReason`: `MANUAL` | `TIMEOUT`
- `evaluationStatus`: `PENDING` | `PROCESSING` | `DONE` | `FAILED`
- `taskStatus`: `TODO` | `DOING` | `DONE`
- `taskType`: `KNOWLEDGE` | `EXPRESSION` | `PROJECT` | `MOCK`
- `fileType`: `RESUME` | `AUDIO` | `VIDEO` | `OTHER`

## 2. 认证与用户（`users` + `refresh_tokens`）

### POST `/auth/register`
请求：
```json
{
  "username": "alice",
  "password": "12345678",
  "phone": "13800001111",
  "email": "alice@example.com",
  "termsAccepted": true,
  "termsVersion": "2026.03"
}
```
响应：
```json
{
  "userId": 1
}
```

### POST `/auth/login`
请求：
```json
{
  "username": "alice",
  "password": "12345678",
  "rememberDays": 7,
  "deviceLabel": "Chrome-Win11"
}
```
响应：
```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "expiresIn": 7200
}
```

### POST `/auth/refresh`
请求：
```json
{
  "refreshToken": "jwt-refresh-token"
}
```
响应：
```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token",
  "expiresIn": 7200
}
```

### POST `/auth/logout`
请求：
```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### GET `/users/current ` //当前用户
响应：
```json
{
  "id": 1,
  "username": "alice",
  "phone": "13800001111",
  "email": "alice@example.com",
  "status": 1
}
```

## 3. 岗位与题库（`roles` + `questions`）

### GET `/roles`
响应：
```json
[
  {
    "id": 1,
    "code": "JAVA_BACKEND",
    "name": "Java后端工程师",
    "description": "面向后端开发岗位",
    "weights": {
      "correctness": 30,
      "depth": 25,
      "logic": 20,
      "match": 15,
      "expression": 10
    }
  }
]
```

### GET `/questions?roleId=1&questionType=TECH&difficulty=MIDDLE&page=1&pageSize=20`
返回岗位题库列表（可供后台维护或前端预览）。

### POST `/questions`（管理端）
### PUT `/questions/{questionId}`（管理端）
### PATCH `/questions/{questionId}/active`（管理端）

## 4. 模拟面试会话（`sessions` + `turns` + `scores`）

### POST `/interview/sessions`
创建会话并返回首题。
请求：
```json
{
  "roleId": 1,
  "difficulty": "MIDDLE",
  "totalRounds": 5,
  "timeLimitSec": 1500,
  "interviewStage": "TECH_FIRST",
  "followupMode": "AUTO",
  "interviewType": "TEXT",
  "voiceEnabled": false,
  "videoEnabled": false
}
```
响应：
```json
{
  "sessionId": 1001,
  "status": "IN_PROGRESS",
  "title": "Java后端 - 技术一面",
  "firstTurn": {
    "turnId": 5001,
    "roundNo": 1,
    "turnType": "QUESTION",
    "questionId": 2001,
    "questionText": "请介绍一个你主导过的高并发项目。"
  }
}
```

### GET `/interview/sessions?page=1&pageSize=20&status=IN_PROGRESS&keyword=后端`
会话列表（历史记录/继续练习）。

### GET `/interview/sessions/{sessionId}`
会话详情（配置、进度、计时、状态）。

### PATCH `/interview/sessions/{sessionId}/config`
动态调整会话配置（难度、是否语音、时长等）。

### GET `/interview/sessions/{sessionId}/turns?page=1&pageSize=50`
会话轮次明细（题目、回答、追问、AI建议）。

### POST `/interview/sessions/{sessionId}/answers`
提交回答并触发单轮评分与追问。
请求：
```json
{
  "turnId": 5001,
  "answerMode": "TEXT",
  "answerText": "我做过秒杀系统...",
  "responseSec": 42
}
```
响应：
```json
{
  "turnId": 5001,
  "scoreSaved": true,
  "nextAction": "FOLLOW_UP",
  "nextTurn": {
    "turnId": 5002,
    "turnType": "FOLLOW_UP",
    "questionText": "你如何平衡一致性和性能？",
    "isFollowUp": true
  }
}
```

### POST `/interview/sessions/{sessionId}/finish`
结束会话，进入总评估流程。
请求：
```json
{
  "finishReason": "MANUAL",
  "clientElapsedSec": 980
}
```

### GET `/interview/sessions/{sessionId}/evaluation-status`
查询报告是否生成完成。

## 5. 评估报告与成长（`reports` + `scores` + `sessions`）

### GET `/interview/sessions/{sessionId}/report`
响应：
```json
{
  "sessionId": 1001,
  "overallScore": 82.6,
  "dimensionScore": {
    "correctness": 83,
    "depth": 80,
    "logic": 79,
    "match": 86,
    "expression": 85
  },
  "summary": "技术正确性较好，故障预案可加强。",
  "highlightPoints": [
    "高并发场景拆解清晰"
  ],
  "improvementPoints": [
    "一致性补偿细节不够完整"
  ],
  "suggestions": [
    "补充分布式事务与可观测性案例"
  ],
  "nextPlan": [
    "本周完成2次一致性专题训练"
  ]
}
```

### GET `/progress/trend?roleId=1&limit=10`
成长曲线（用于折线图）。

### GET `/progress/latest-weak-points?roleId=1`
近期薄弱项聚合（用于推荐训练任务）。

## 6. 能力提升任务（`tasks`）

### GET `/tasks?page=1&pageSize=20&status=TODO`
我的训练任务列表。

### POST `/tasks`
请求：
```json
{
  "sessionId": 1001,
  "title": "分布式事务补强",
  "taskType": "KNOWLEDGE",
  "content": "完成两阶段提交与TCC对比总结",
  "dueDate": "2026-03-20"
}
```

### PATCH `/tasks/{taskId}/status`
请求：
```json
{
  "status": "DONE"
}
```

### DELETE `/tasks/{taskId}`

## 7. 附件与语音（`attachments` + 语音服务）

### POST `/speech/asr`（`multipart/form-data`）
表单：
- `file`（必传）
- `sessionId`（可选）
- `turnId`（可选）
响应：
```json
{
  "text": "识别后的文本",
  "confidence": 0.93,
  "durationMs": 5400
}
```

### POST `/attachments/upload`（`multipart/form-data`）
表单：
- `file`（必传）
- `sessionId`（必传）
- `turnId`（可选）
- `fileType`（必传）
响应：
```json
{
  "fileId": 3001,
  "fileName": "resume.pdf",
  "fileUrl": "https://xxx/resume.pdf",
  "fileType": "RESUME",
  "fileSize": 231122
}
```

### GET `/attachments?sessionId=1001&fileType=RESUME`
按会话查询附件列表。

## 8. 与 A05 赛题的映射说明

- 岗位化题库与评估模型：`roles` + `questions` + `/roles` + `/questions`
- 多轮对话与智能追问：`sessions` + `turns` + `/answers`
- 多维度评分与报告：`scores` + `reports` + `/report`
- 个性化提升闭环：`tasks` + `/progress/*` + `/tasks/*`
- 多模态（文本/语音）：`answerMode` + `/speech/asr` + `attachments`

## 9. 错误码建议

- `40001` 参数校验失败
- `40101` 未登录或令牌无效
- `40301` 无权限访问资源
- `40401` 会话不存在
- `40901` 会话状态不允许当前操作
- `50001` 大模型服务异常
- `50002` 语音识别服务异常

## 10. 知识库与检索（`kb_documents` + `kb_chunks` + `kb_retrieval_logs`）

### GET `/knowledge/documents?roleId=1&status=ACTIVE&keyword=限流&page=1&pageSize=20`
知识文档分页查询。

### POST `/knowledge/documents`
请求：
```json
{
  "roleId": 1,
  "title": "高并发系统面试知识手册",
  "docType": "MARKDOWN",
  "sourceName": "后端组知识库",
  "sourceUrl": "",
  "storagePath": "/kb/backend/high-concurrency.md",
  "summary": "覆盖限流、熔断、降级、一致性。"
}
```

### PUT `/knowledge/documents/{docId}`
更新文档基础信息与状态。

### GET `/knowledge/documents/{docId}/chunks?page=1&pageSize=20`
文档切片分页查询。

### POST `/knowledge/documents/{docId}/chunks`
请求：
```json
{
  "chunkText": "限流要区分网关限流和服务限流...",
  "keywords": "限流,网关",
  "embeddingModel": "text-embedding-v1"
}
```

### GET `/knowledge/retrieval-logs?sessionId=20001&roleId=1&page=1&pageSize=20`
检索日志分页查询（用于 RAG 追溯和评估展示）。
## 11. 管理端接口补充（建议前缀：`/admin/*`）

> 说明：为避免与学生端接口混用，管理端建议统一加 `/admin` 前缀。

### 11.1 岗位与权重管理

### GET `/admin/roles`
返回岗位与五维权重列表（管理视角）。

### PUT `/admin/roles/{roleId}/weights`
请求：
```json
{
  "description": "面向服务端开发岗位",
  "weightCorrectness": 30,
  "weightDepth": 25,
  "weightLogic": 20,
  "weightMatch": 15,
  "weightExpression": 10
}
```
响应：
```json
{
  "updated": true,
  "weightTotal": 100
}
```

### 11.2 题库管理

### GET `/admin/questions?roleId=1&questionType=TECH&difficulty=MIDDLE&active=1&page=1&pageSize=20`
管理端题库分页查询。

### POST `/admin/questions`
请求：
```json
{
  "roleId": 1,
  "questionType": "TECH",
  "difficulty": "MIDDLE",
  "questionText": "请说明你在高并发下单链路中的限流策略。",
  "expectedPoints": "网关限流,应用限流,降级策略",
  "keywords": "限流,降级,高并发"
}
```

### PUT `/admin/questions/{questionId}`
更新题目内容与标签。

### PATCH `/admin/questions/{questionId}/active`
请求：
```json
{
  "active": false
}
```

### DELETE `/admin/questions/{questionId}`
逻辑删除或物理删除（按实现选一种）。

### 11.3 会话与报告管理

### GET `/admin/interview/sessions?status=FINISHED&evaluationStatus=DONE&page=1&pageSize=20`
管理端会话列表，支持按状态筛选。

### GET `/admin/interview/sessions/{sessionId}`
会话详情（含基础配置、轮次统计、时长、分数）。

### GET `/admin/interview/sessions/{sessionId}/report`
查看指定会话报告详情（管理端复核）。

### 11.4 知识库管理

### GET `/admin/knowledge/documents?roleId=1&status=ACTIVE&docType=MARKDOWN&keyword=限流&page=1&pageSize=20`
知识文档分页查询。

### POST `/admin/knowledge/documents`
请求：
```json
{
  "roleId": 1,
  "title": "高并发系统面试知识手册",
  "docType": "MARKDOWN",
  "sourceName": "后端组知识库",
  "sourceUrl": "",
  "storagePath": "/kb/backend/high-concurrency.md",
  "summary": "覆盖限流、熔断、降级、一致性。"
}
```

### PUT `/admin/knowledge/documents/{docId}`
请求：
```json
{
  "title": "高并发系统面试知识手册（更新版）",
  "sourceName": "后端组知识库",
  "sourceUrl": "",
  "storagePath": "/kb/backend/high-concurrency-v2.md",
  "summary": "新增缓存一致性案例",
  "status": "ACTIVE"
}
```

### DELETE `/admin/knowledge/documents/{docId}`
删除文档（建议级联删除切片）。

### GET `/admin/knowledge/documents/{docId}/chunks?page=1&pageSize=20`
文档切片分页查询。

### POST `/admin/knowledge/documents/{docId}/chunks`
请求：
```json
{
  "chunkText": "一致性策略可按核心链路和非核心链路分层处理。",
  "keywords": "一致性,分层",
  "embeddingModel": "text-embedding-v1"
}
```

### DELETE `/admin/knowledge/chunks/{chunkId}`
删除指定切片。

### GET `/admin/knowledge/retrieval-logs?sessionId=20001&roleId=1&page=1&pageSize=20`
检索日志查询（用于 RAG 质量复盘）。

## 12. 错误码细化（建议）

### 12.1 通用错误码
- `40001` 参数校验失败
- `40002` 枚举值非法（如 `difficulty`、`questionType`）
- `40101` 未登录或令牌无效
- `40301` 无权限访问资源
- `40401` 资源不存在
- `40901` 状态冲突（如会话已结束仍提交回答）
- `42901` 请求过于频繁
- `50001` 大模型服务异常
- `50002` 语音识别服务异常
- `50003` 文件存储服务异常

### 12.2 业务错误码
- `41011` 会话已结束，禁止继续答题
- `41012` 会话未开始，不能提交回答
- `41021` 报告生成中，请稍后重试
- `41022` 报告生成失败
- `42011` 权重总和不为 100
- `42021` 题目已停用，不能被抽题
- `43011` 知识文档不存在或已禁用
- `43021` 知识切片为空或未命中

### 12.3 错误响应示例
```json
{
  "code": 42011,
  "message": "岗位权重总和必须为100",
  "data": null,
  "requestId": "4fd4cc8e62f34a0b"
}
```