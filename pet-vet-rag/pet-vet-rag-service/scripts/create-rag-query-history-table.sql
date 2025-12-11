-- ============================================
-- RAG 查询历史记录表
-- 用于存储 RAG 查询历史记录，包括查询文本、检索结果、生成答案等信息
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `pet_vet_rag` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `pet_vet_rag`;

-- 创建 RAG 查询历史记录表
CREATE TABLE IF NOT EXISTS `rag_query_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '查询ID（主键，自增）',
    `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话ID（用于关联同一会话的多次查询）',
    `query` TEXT NOT NULL COMMENT '查询文本（用户问题）',
    `retrieved_count` INT DEFAULT 0 COMMENT '检索到的文档数量',
    `enable_generation` TINYINT(1) DEFAULT 1 COMMENT '是否启用了生成（0-否，1-是）',
    `generated_answer` TEXT DEFAULT NULL COMMENT '生成的答案（如果启用）',
    `retrieved_documents` JSON DEFAULT NULL COMMENT '检索到的文档（JSON格式）',
    `conversation_context` JSON DEFAULT NULL COMMENT '对话上下文（历史对话摘要，JSON格式）',
    `model_name` VARCHAR(64) DEFAULT 'deepseek' COMMENT '使用的模型名称',
    `query_time` BIGINT DEFAULT 0 COMMENT '查询耗时（毫秒）',
    `confidence` DECIMAL(3,2) DEFAULT 0.00 COMMENT '置信度分数（0.0-1.0）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_user_create_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG查询历史记录表';
