-- ============================================
-- PetVet RAG 模块数据库初始化脚本
-- 数据库：pet_vet
-- 模块：pet-vet-rag
-- 创建时间：2024-12-19
-- ============================================

-- 使用数据库
USE pet_vet;

-- ============================================
-- 1. RAG查询历史记录表 (vet_rag_query_history)
-- ============================================
DROP TABLE IF EXISTS `vet_rag_query_history`;
CREATE TABLE `vet_rag_query_history` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '查询ID（主键，自增）',
    `query` TEXT NOT NULL COMMENT '查询文本（用户问题）',
    `retrieved_count` INT(11) DEFAULT 0 COMMENT '检索到的文档数量',
    `enable_generation` TINYINT(1) DEFAULT 0 COMMENT '是否启用了生成：0-否，1-是',
    `generated_answer` TEXT COMMENT '生成的答案（如果启用）',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '使用的模型名称',
    `query_time` BIGINT(20) DEFAULT NULL COMMENT '查询耗时（毫秒）',
    `session_id` VARCHAR(64) DEFAULT NULL COMMENT '会话ID（用于关联同一会话的多次查询）',
    `user_id` VARCHAR(64) DEFAULT NULL COMMENT '用户ID（如果有）',
    `retrieved_documents` TEXT COMMENT '检索到的文档（JSON格式）',
    `conversation_context` TEXT COMMENT '对话上下文（历史对话摘要，JSON格式）',
    `confidence` DECIMAL(5,4) DEFAULT NULL COMMENT '置信度分数（0.0-1.0）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RAG查询历史记录表';
