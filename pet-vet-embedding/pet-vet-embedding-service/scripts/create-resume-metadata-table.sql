-- ============================================
-- 简历元数据表
-- 用于存储简历解析后的元数据信息
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `pet_vet_embedding` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `pet_vet_embedding`;

-- 创建简历元数据表
CREATE TABLE IF NOT EXISTS `resume_metadata` (
    `resume_id` VARCHAR(255) NOT NULL COMMENT '简历ID（主键）',
    `file_name` VARCHAR(500) DEFAULT NULL COMMENT '文件名（原始文件名）',
    `name` VARCHAR(100) DEFAULT NULL COMMENT '解析出的姓名',
    `position` VARCHAR(200) DEFAULT NULL COMMENT '解析出的职位',
    `version` VARCHAR(50) DEFAULT NULL COMMENT '版本标识（从文件名提取，如：new）',
    `contact_info` VARCHAR(500) DEFAULT NULL COMMENT '联系方式（邮箱、电话等）',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `parse_time` DATETIME DEFAULT NULL COMMENT '解析时间',
    `chunk_count` INT DEFAULT NULL COMMENT 'Chunk数量',
    `vector_ids_json` TEXT DEFAULT NULL COMMENT '向量数据库中的向量ID列表（JSON格式存储）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`resume_id`),
    KEY `idx_name` (`name`),
    KEY `idx_position` (`position`),
    KEY `idx_parse_time` (`parse_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历元数据表';
