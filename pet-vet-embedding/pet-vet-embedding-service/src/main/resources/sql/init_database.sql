-- ============================================
-- PetVet Embedding 模块数据库初始化脚本
-- 数据库：pet_vet
-- 模块：pet-vet-embedding
-- 创建时间：2024-12-19
-- ============================================

-- 使用数据库
USE pet_vet;

-- ============================================
-- 1. 文本Chunk表 (vet_embedding_text_chunk)
-- ============================================
DROP TABLE IF EXISTS `vet_embedding_text_chunk`;
CREATE TABLE `vet_embedding_text_chunk` (
    `chunk_id` VARCHAR(64) NOT NULL COMMENT 'Chunk ID（主键，向量数据库中的ID）',
    `resume_id` VARCHAR(64) NOT NULL COMMENT '关联的简历ID',
    `text` TEXT NOT NULL COMMENT 'Chunk文本内容',
    `sequence` INT(11) NOT NULL COMMENT 'Chunk序号（在同一简历中的顺序）',
    `field_type` VARCHAR(50) DEFAULT NULL COMMENT '所属字段类型（如：工作经历、教育背景、技能等）',
    `start_position` INT(11) DEFAULT NULL COMMENT '在原文中的起始位置',
    `end_position` INT(11) DEFAULT NULL COMMENT '在原文中的结束位置',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`chunk_id`),
    KEY `idx_resume_id` (`resume_id`),
    KEY `idx_sequence` (`sequence`),
    KEY `idx_field_type` (`field_type`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文本Chunk表';

-- ============================================
-- 2. 简历元数据表 (vet_embedding_resume_metadata)
-- ============================================
DROP TABLE IF EXISTS `vet_embedding_resume_metadata`;
CREATE TABLE `vet_embedding_resume_metadata` (
    `resume_id` VARCHAR(64) NOT NULL COMMENT '简历ID（主键）',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名（原始文件名）',
    `name` VARCHAR(100) DEFAULT NULL COMMENT '解析出的姓名',
    `position` VARCHAR(100) DEFAULT NULL COMMENT '解析出的职位',
    `version_tag` VARCHAR(50) DEFAULT NULL COMMENT '版本标识（从文件名提取，如：new）',
    `contact_info` VARCHAR(500) DEFAULT NULL COMMENT '联系方式（邮箱、电话等）',
    `file_size` BIGINT(20) DEFAULT NULL COMMENT '文件大小（字节）',
    `parse_time` DATETIME DEFAULT NULL COMMENT '解析时间',
    `chunk_count` INT(11) DEFAULT 0 COMMENT 'Chunk数量',
    `vector_ids_json` TEXT COMMENT '向量数据库中的向量ID列表（JSON格式存储）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`resume_id`),
    KEY `idx_file_name` (`file_name`),
    KEY `idx_name` (`name`),
    KEY `idx_position` (`position`),
    KEY `idx_parse_time` (`parse_time`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历元数据表';
