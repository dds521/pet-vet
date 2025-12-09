-- ============================================
-- 文本Chunk表
-- 用于存储简历解析后的文本Chunk信息
-- ============================================

USE `pet_vet_embedding`;

-- 创建文本Chunk表
CREATE TABLE IF NOT EXISTS `text_chunk` (
    `chunk_id` VARCHAR(255) NOT NULL COMMENT 'Chunk ID（主键，向量数据库中的ID）',
    `resume_id` VARCHAR(255) NOT NULL COMMENT '关联的简历ID',
    `text` TEXT NOT NULL COMMENT 'Chunk文本内容',
    `sequence` INT DEFAULT NULL COMMENT 'Chunk序号（在同一简历中的顺序）',
    `field_type` VARCHAR(50) DEFAULT NULL COMMENT '所属字段类型（如：工作经历、教育背景、技能等）',
    `start_position` INT DEFAULT NULL COMMENT '在原文中的起始位置',
    `end_position` INT DEFAULT NULL COMMENT '在原文中的结束位置',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`chunk_id`),
    KEY `idx_resume_id` (`resume_id`),
    KEY `idx_field_type` (`field_type`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_chunk_resume` FOREIGN KEY (`resume_id`) REFERENCES `resume_metadata` (`resume_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文本Chunk表';
