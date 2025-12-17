-- ============================================
-- PetVet AI 模块数据库初始化脚本
-- 数据库：pet_vet
-- 模块：pet-vet-ai
-- 创建时间：2024-12-19
-- ============================================

-- 使用数据库
USE pet_vet;

-- ============================================
-- 1. 宠物表 (vet_ai_pet)
-- ============================================
DROP TABLE IF EXISTS `vet_ai_pet`;
CREATE TABLE `vet_ai_pet` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '宠物名称',
    `breed` VARCHAR(100) DEFAULT NULL COMMENT '宠物品种',
    `age` INT(11) DEFAULT NULL COMMENT '宠物年龄',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_breed` (`breed`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物表';

-- ============================================
-- 2. 账户表 (vet_ai_account)
-- ============================================
DROP TABLE IF EXISTS `vet_ai_account`;
CREATE TABLE `vet_ai_account` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- ============================================
-- 3. 订单表 (vet_ai_order)
-- ============================================
DROP TABLE IF EXISTS `vet_ai_order`;
CREATE TABLE `vet_ai_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pet_id` BIGINT(20) NOT NULL COMMENT '宠物ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_pet_id` (`pet_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 4. 症状表 (vet_ai_symptom)
-- ============================================
DROP TABLE IF EXISTS `vet_ai_symptom`;
CREATE TABLE `vet_ai_symptom` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `description` TEXT NOT NULL COMMENT '症状描述',
    `pet_id` BIGINT(20) NOT NULL COMMENT '宠物ID',
    `reported_at` DATETIME NOT NULL COMMENT '报告时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    KEY `idx_pet_id` (`pet_id`),
    KEY `idx_reported_at` (`reported_at`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='症状表';

-- ============================================
-- 5. 事务日志表 (vet_ai_transaction_log)
-- ============================================
DROP TABLE IF EXISTS `vet_ai_transaction_log`;
CREATE TABLE `vet_ai_transaction_log` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `transaction_id` VARCHAR(64) NOT NULL COMMENT '事务ID',
    `topic` VARCHAR(100) NOT NULL COMMENT '消息主题',
    `tag` VARCHAR(100) DEFAULT NULL COMMENT '消息标签',
    `message_body` TEXT COMMENT '消息体',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '事务状态：PENDING-待提交, COMMITTED-已提交, ROLLBACKED-已回滚',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`),
    KEY `idx_topic` (`topic`),
    KEY `idx_status` (`status`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事务日志表';

-- ============================================
-- 6. 微信用户表 (vet_ai_wechat_user)
-- ============================================
DROP TABLE IF EXISTS `vet_ai_wechat_user`;
CREATE TABLE `vet_ai_wechat_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `open_id` VARCHAR(64) NOT NULL COMMENT '微信用户唯一标识（openId）',
    `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信用户统一标识（unionId）',
    `nick_name` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL',
    `gender` INT(1) DEFAULT 0 COMMENT '用户性别：0-未知，1-男，2-女',
    `country` VARCHAR(50) DEFAULT NULL COMMENT '用户所在国家',
    `province` VARCHAR(50) DEFAULT NULL COMMENT '用户所在省份',
    `city` VARCHAR(50) DEFAULT NULL COMMENT '用户所在城市',
    `language` VARCHAR(20) DEFAULT NULL COMMENT '用户语言',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `status` INT(1) NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT '创建人',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT '更新人',
    `is_void` INT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
    `version` INT(11) NOT NULL DEFAULT 0 COMMENT '版本号（乐观锁）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_open_id` (`open_id`),
    KEY `idx_union_id` (`union_id`),
    KEY `idx_status` (`status`),
    KEY `idx_is_void` (`is_void`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户表';
