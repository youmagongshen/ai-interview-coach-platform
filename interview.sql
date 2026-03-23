CREATE DATABASE IF NOT EXISTS `interview`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `interview`;
SET NAMES utf8mb4;
-- 用户表：保存登录账号与基础资料
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `username` VARCHAR(50) NOT NULL /* 用户名 */,
    `password_hash` VARCHAR(255) NOT NULL /* 密码哈希 */,
    `phone` VARCHAR(20) NOT NULL /* 手机号 */,
    `email` VARCHAR(100) NOT NULL /* 邮箱 */,
    `terms_accepted` TINYINT(1) NOT NULL DEFAULT 0 /* 是否同意协议 */,
    `terms_version` VARCHAR(20) DEFAULT NULL /* 协议版本 */,
    `terms_accepted_at` DATETIME DEFAULT NULL /* 协议同意时间 */,
    `status` TINYINT NOT NULL DEFAULT 1 /* 状态 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP /* 更新时间 */,
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_phone` (`phone`),
    UNIQUE KEY `uk_users_email` (`email`),
    KEY `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 岗位表：定义岗位与评分权重
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `code` VARCHAR(32) NOT NULL /* 岗位编码 */,
    `name` VARCHAR(50) NOT NULL /* 名称 */,
    `description` VARCHAR(500) DEFAULT NULL /* 描述 */,
    `weight_correctness` DECIMAL(5,2) NOT NULL DEFAULT 30.00 /* 正确性权重 */,
    `weight_depth` DECIMAL(5,2) NOT NULL DEFAULT 25.00 /* 深度权重 */,
    `weight_logic` DECIMAL(5,2) NOT NULL DEFAULT 20.00 /* 逻辑权重 */,
    `weight_match` DECIMAL(5,2) NOT NULL DEFAULT 15.00 /* 匹配度权重 */,
    `weight_expression` DECIMAL(5,2) NOT NULL DEFAULT 10.00 /* 表达权重 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP /* 更新时间 */,
    UNIQUE KEY `uk_roles_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 题库表：按岗位与难度维护题目
CREATE TABLE IF NOT EXISTS `questions` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `role_id` BIGINT UNSIGNED NOT NULL /* 岗位ID */,
    `question_type` VARCHAR(20) NOT NULL /* 题型 */,
    `difficulty` VARCHAR(20) NOT NULL DEFAULT 'MIDDLE' /* 难度 */,
    `question_text` TEXT NOT NULL /* 题目内容 */,
    `expected_points` TEXT DEFAULT NULL /* 期望要点 */,
    `keywords` VARCHAR(500) DEFAULT NULL /* 关键词 */,
    `active` TINYINT(1) NOT NULL DEFAULT 1 /* 是否启用 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP /* 更新时间 */,
    CONSTRAINT `fk_questions_role` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`),
    KEY `idx_questions_role_type_active` (`role_id`, `question_type`, `active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 会话表：记录整场模拟面试
CREATE TABLE IF NOT EXISTS `sessions` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `user_id` BIGINT UNSIGNED NOT NULL /* 用户ID */,
    `role_id` BIGINT UNSIGNED NOT NULL /* 岗位ID */,
    `title` VARCHAR(120) DEFAULT NULL /* 标题 */,
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' /* 状态 */,
    `total_rounds` INT NOT NULL DEFAULT 5 /* 总轮次 */,
    `current_round` INT NOT NULL DEFAULT 0 /* 当前轮次 */,
    `difficulty` VARCHAR(20) NOT NULL DEFAULT 'MIDDLE' /* 难度 */,
    `interview_stage` VARCHAR(30) NOT NULL DEFAULT 'TECH_FIRST' /* 面试阶段 */,
    `followup_mode` VARCHAR(20) NOT NULL DEFAULT 'AUTO' /* 追问模式 */,
    `interview_type` VARCHAR(20) NOT NULL DEFAULT 'TEXT' /* 面试类型 */,
    `time_limit_sec` INT NOT NULL DEFAULT 1500 /* 时限(秒) */,
    `auto_finish_at` DATETIME DEFAULT NULL /* 自动结束时间 */,
    `voice_enabled` TINYINT(1) NOT NULL DEFAULT 0 /* 是否启用语音 */,
    `video_enabled` TINYINT(1) NOT NULL DEFAULT 0 /* 是否启用视频 */,
    `started_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 开始时间 */,
    `ended_at` DATETIME DEFAULT NULL /* 结束时间 */,
    `finish_reason` VARCHAR(20) DEFAULT NULL /* 结束原因 */,
    `duration_sec` INT DEFAULT NULL /* 持续时长(秒) */,
    `overall_score` DECIMAL(5,2) DEFAULT NULL /* 总分 */,
    `evaluation_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' /* 评估状态 */,
    `evaluation_started_at` DATETIME DEFAULT NULL /* 评估开始时间 */,
    `evaluation_finished_at` DATETIME DEFAULT NULL /* 评估结束时间 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP /* 更新时间 */,
    `last_active_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 最后活跃时间 */,
    CONSTRAINT `fk_sessions_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_sessions_role` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`),
    KEY `idx_sessions_user_time` (`user_id`, `created_at`),
    KEY `idx_sessions_status_active` (`status`, `last_active_at`),
    KEY `idx_sessions_user_type_time` (`user_id`, `interview_type`, `created_at`),
    KEY `idx_sessions_eval_status` (`evaluation_status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 轮次表：记录每轮提问与回答
CREATE TABLE IF NOT EXISTS `turns` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `session_id` BIGINT UNSIGNED NOT NULL /* 会话ID */,
    `round_no` INT NOT NULL /* 轮次序号 */,
    `turn_type` VARCHAR(20) NOT NULL DEFAULT 'QUESTION' /* 轮次类型 */,
    `question_id` BIGINT UNSIGNED DEFAULT NULL /* 题目ID */,
    `question_text` TEXT NOT NULL /* 题目内容 */,
    `is_follow_up` TINYINT(1) NOT NULL DEFAULT 0 /* 是否追问 */,
    `follow_up_reason` VARCHAR(500) DEFAULT NULL /* 追问原因 */,
    `answer_mode` VARCHAR(20) NOT NULL DEFAULT 'TEXT' /* 回答方式 */,
    `answer_text` TEXT DEFAULT NULL /* 回答文本 */,
    `audio_url` VARCHAR(500) DEFAULT NULL /* 音频地址 */,
    `asr_text` TEXT DEFAULT NULL /* 语音识别文本 */,
    `ai_reply_text` TEXT DEFAULT NULL /* AI回复 */,
    `ai_advice` TEXT DEFAULT NULL /* AI建议 */,
    `response_sec` INT DEFAULT NULL /* 回答耗时(秒) */,
    `evaluated_at` DATETIME DEFAULT NULL /* 评估时间 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP /* 更新时间 */,
    CONSTRAINT `fk_turns_session` FOREIGN KEY (`session_id`) REFERENCES `sessions`(`id`),
    CONSTRAINT `fk_turns_question` FOREIGN KEY (`question_id`) REFERENCES `questions`(`id`),
    KEY `idx_turns_session_round` (`session_id`, `round_no`),
    KEY `idx_turns_session_created` (`session_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 刷新令牌表：用于登录续期
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `user_id` BIGINT UNSIGNED NOT NULL /* 用户ID */,
    `token_hash` CHAR(64) NOT NULL /* 令牌哈希 */,
    `device_label` VARCHAR(100) DEFAULT NULL /* 设备标识 */,
    `remember_days` INT NOT NULL DEFAULT 7 /* 记住天数 */,
    `expires_at` DATETIME NOT NULL /* 过期时间 */,
    `revoked_at` DATETIME DEFAULT NULL /* 撤销时间 */,
    `last_used_at` DATETIME DEFAULT NULL /* 最后使用时间 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    CONSTRAINT `fk_refresh_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    UNIQUE KEY `uk_refresh_tokens_hash` (`token_hash`),
    KEY `idx_refresh_tokens_user_expires` (`user_id`, `expires_at`),
    KEY `idx_refresh_tokens_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 评分表：记录每轮维度分与总分
CREATE TABLE IF NOT EXISTS `scores` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `session_id` BIGINT UNSIGNED NOT NULL /* 会话ID */,
    `turn_id` BIGINT UNSIGNED NOT NULL /* 轮次ID */,
    `correctness_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 /* 正确性得分 */,
    `depth_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 /* 深度得分 */,
    `logic_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 /* 逻辑得分 */,
    `match_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 /* 匹配度得分 */,
    `expression_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 /* 表达得分 */,
    `total_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 /* 总得分 */,
    `evidence` TEXT DEFAULT NULL /* 评分依据 */,
    `weak_points` TEXT DEFAULT NULL /* 薄弱点 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    CONSTRAINT `fk_scores_session` FOREIGN KEY (`session_id`) REFERENCES `sessions`(`id`),
    CONSTRAINT `fk_scores_turn` FOREIGN KEY (`turn_id`) REFERENCES `turns`(`id`),
    UNIQUE KEY `uk_scores_turn` (`turn_id`),
    KEY `idx_scores_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 报告表：会话结束后的总结报告
CREATE TABLE IF NOT EXISTS `reports` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `session_id` BIGINT UNSIGNED NOT NULL /* 会话ID */,
    `summary` TEXT DEFAULT NULL /* 总结 */,
    `highlight_points` TEXT DEFAULT NULL /* 亮点 */,
    `improvement_points` TEXT DEFAULT NULL /* 改进点 */,
    `suggestions` TEXT DEFAULT NULL /* 建议 */,
    `next_plan` TEXT DEFAULT NULL /* 后续计划 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    CONSTRAINT `fk_reports_session` FOREIGN KEY (`session_id`) REFERENCES `sessions`(`id`),
    UNIQUE KEY `uk_reports_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 任务表：用户后续训练任务
CREATE TABLE IF NOT EXISTS `tasks` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `user_id` BIGINT UNSIGNED NOT NULL /* 用户ID */,
    `session_id` BIGINT UNSIGNED DEFAULT NULL /* 会话ID */,
    `title` VARCHAR(200) NOT NULL /* 标题 */,
    `task_type` VARCHAR(20) NOT NULL /* 任务类型 */,
    `content` TEXT NOT NULL /* 内容 */,
    `status` VARCHAR(20) NOT NULL DEFAULT 'TODO' /* 状态 */,
    `due_date` DATE DEFAULT NULL /* 截止日期 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP /* 更新时间 */,
    CONSTRAINT `fk_tasks_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_tasks_session` FOREIGN KEY (`session_id`) REFERENCES `sessions`(`id`),
    KEY `idx_tasks_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 附件表：简历/音视频等文件元数据
CREATE TABLE IF NOT EXISTS `attachments` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `user_id` BIGINT UNSIGNED NOT NULL /* 用户ID */,
    `session_id` BIGINT UNSIGNED NOT NULL /* 会话ID */,
    `turn_id` BIGINT UNSIGNED DEFAULT NULL /* 轮次ID */,
    `file_type` VARCHAR(20) NOT NULL /* 文件类型 */,
    `file_name` VARCHAR(255) NOT NULL /* 文件名 */,
    `file_url` VARCHAR(500) NOT NULL /* 文件URL */,
    `mime_type` VARCHAR(100) DEFAULT NULL /* MIME类型 */,
    `file_size` BIGINT DEFAULT NULL /* 文件大小(字节) */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    CONSTRAINT `fk_attachments_user` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_attachments_session` FOREIGN KEY (`session_id`) REFERENCES `sessions`(`id`),
    CONSTRAINT `fk_attachments_turn` FOREIGN KEY (`turn_id`) REFERENCES `turns`(`id`),
    KEY `idx_attachments_session_type` (`session_id`, `file_type`),
    KEY `idx_attachments_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 知识库文档表：RAG 文档层
CREATE TABLE IF NOT EXISTS `kb_documents` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `role_id` BIGINT UNSIGNED NOT NULL /* 岗位ID */,
    `title` VARCHAR(200) NOT NULL /* 标题 */,
    `doc_type` VARCHAR(20) NOT NULL DEFAULT 'MARKDOWN' /* 文档类型 */,
    `source_name` VARCHAR(255) DEFAULT NULL /* 来源名称 */,
    `source_url` VARCHAR(500) DEFAULT NULL /* 来源URL */,
    `storage_path` VARCHAR(500) DEFAULT NULL /* 存储路径 */,
    `summary` TEXT DEFAULT NULL /* 总结 */,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' /* 状态 */,
    `version` INT NOT NULL DEFAULT 1 /* 版本号 */,
    `created_by` BIGINT UNSIGNED DEFAULT NULL /* 创建人ID */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP /* 更新时间 */,
    CONSTRAINT `fk_kb_documents_role` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`),
    CONSTRAINT `fk_kb_documents_creator` FOREIGN KEY (`created_by`) REFERENCES `users`(`id`),
    KEY `idx_kb_documents_role_status` (`role_id`, `status`),
    KEY `idx_kb_documents_updated` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 知识库切片表：RAG 切片层
CREATE TABLE IF NOT EXISTS `kb_chunks` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `doc_id` BIGINT UNSIGNED NOT NULL /* 文档ID */,
    `chunk_index` INT NOT NULL /* 切片序号 */,
    `chunk_text` TEXT NOT NULL /* 切片文本 */,
    `keywords` VARCHAR(500) DEFAULT NULL /* 关键词 */,
    `token_count` INT DEFAULT NULL /* Token数量 */,
    `embedding_model` VARCHAR(100) DEFAULT NULL /* 向量模型 */,
    `embedding_vector` MEDIUMTEXT DEFAULT NULL /* 向量内容 */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    CONSTRAINT `fk_kb_chunks_doc` FOREIGN KEY (`doc_id`) REFERENCES `kb_documents`(`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_kb_chunks_doc_index` (`doc_id`, `chunk_index`),
    KEY `idx_kb_chunks_doc` (`doc_id`),
    KEY `idx_kb_chunks_keywords` (`keywords`),
    FULLTEXT KEY `ft_kb_chunks_text` (`chunk_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
-- 知识库检索日志表：RAG 检索审计
CREATE TABLE IF NOT EXISTS `kb_retrieval_logs` (
    `id` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT /* 主键ID */,
    `session_id` BIGINT UNSIGNED DEFAULT NULL /* 会话ID */,
    `turn_id` BIGINT UNSIGNED DEFAULT NULL /* 轮次ID */,
    `role_id` BIGINT UNSIGNED DEFAULT NULL /* 岗位ID */,
    `query_text` TEXT NOT NULL /* 检索查询 */,
    `top_k` INT NOT NULL DEFAULT 5 /* 召回数量 */,
    `retrieved_doc_ids` VARCHAR(500) DEFAULT NULL /* 命中文档ID列表 */,
    `retrieved_chunk_ids` VARCHAR(1000) DEFAULT NULL /* 命中切片ID列表 */,
    `llm_answer_summary` TEXT DEFAULT NULL /* 大模型回答摘要 */,
    `latency_ms` INT DEFAULT NULL /* 耗时(ms) */,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP /* 创建时间 */,
    CONSTRAINT `fk_kb_logs_session` FOREIGN KEY (`session_id`) REFERENCES `sessions`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_kb_logs_turn` FOREIGN KEY (`turn_id`) REFERENCES `turns`(`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_kb_logs_role` FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`) ON DELETE SET NULL,
    KEY `idx_kb_logs_session_time` (`session_id`, `created_at`),
    KEY `idx_kb_logs_role_time` (`role_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


INSERT INTO users (username, password_hash, phone, email, terms_accepted, terms_version, terms_accepted_at, status)
VALUES ('user01', 'aad415a73c4cef1ef94a5c00b2642b571a3e5494536328ad960db61889bd9368', '13900020001', 'user01@example.com', 1, NULL, NOW(),
        1);

INSERT INTO users (username, password_hash, phone, email, terms_accepted, terms_version, terms_accepted_at, status)
VALUES ('user02', '76431fac8a187241af8f3f37156deb94732f52fb45eb07ec4f462051bd82f183', '13900020002', 'user02@example.com', 1, NULL, NOW(),
        1);

INSERT INTO roles (code, name, description, weight_correctness, weight_depth, weight_logic, weight_match, weight_expression)
VALUES ('JAVA_BACKEND', 'Java后端', '面向Java后端开发岗位的模拟面试', 30.00, 25.00, 20.00, 15.00, 10.00);

INSERT INTO roles (code, name, description, weight_correctness, weight_depth, weight_logic, weight_match, weight_expression)
VALUES ('WEB_FRONTEND', 'Web前端', '面向Web前端开发岗位的模拟面试', 30.00, 25.00, 20.00, 15.00, 10.00);