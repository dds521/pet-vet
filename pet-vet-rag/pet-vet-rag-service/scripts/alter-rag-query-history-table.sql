-- ============================================
-- RAG 查询历史记录表 - 字段更新脚本
-- 用于更新现有表结构，添加新字段
-- ============================================

USE `pet_vet_rag`;

-- 检查并添加新字段（如果不存在）

-- 1. 添加 retrieved_documents 字段（JSON格式，存储检索到的文档）
SET @dbname = DATABASE();
SET @tablename = 'rag_query_history';
SET @columnname = 'retrieved_documents';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' JSON DEFAULT NULL COMMENT ''检索到的文档（JSON格式）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. 添加 conversation_context 字段（JSON格式，存储对话上下文）
SET @columnname = 'conversation_context';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' JSON DEFAULT NULL COMMENT ''对话上下文（历史对话摘要，JSON格式）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 3. 添加 confidence 字段（DECIMAL类型，存储置信度分数）
SET @columnname = 'confidence';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' DECIMAL(3,2) DEFAULT 0.00 COMMENT ''置信度分数（0.0-1.0）''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 4. 修改 user_id 字段为 NOT NULL（如果当前允许NULL）
-- 注意：如果表中已有NULL值，需要先处理数据
-- ALTER TABLE `rag_query_history` MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID';

-- 5. 更新 model_name 默认值
ALTER TABLE `rag_query_history` MODIFY COLUMN `model_name` VARCHAR(64) DEFAULT 'deepseek' COMMENT '使用的模型名称';

-- 6. 更新 retrieved_count 默认值
ALTER TABLE `rag_query_history` MODIFY COLUMN `retrieved_count` INT DEFAULT 0 COMMENT '检索到的文档数量';

-- 7. 更新 enable_generation 默认值
ALTER TABLE `rag_query_history` MODIFY COLUMN `enable_generation` TINYINT(1) DEFAULT 1 COMMENT '是否启用了生成（0-否，1-是）';

-- 8. 更新 query_time 默认值
ALTER TABLE `rag_query_history` MODIFY COLUMN `query_time` BIGINT DEFAULT 0 COMMENT '查询耗时（毫秒）';

-- 显示更新后的表结构
DESC `rag_query_history`;
