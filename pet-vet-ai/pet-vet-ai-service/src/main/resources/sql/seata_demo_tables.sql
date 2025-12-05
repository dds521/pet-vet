-- Seata 演示所需的数据库表

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING, PAID, CANCELLED',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_pet_id` (`pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 账户表
CREATE TABLE IF NOT EXISTS `accounts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户表';

-- Seata AT 模式需要的 undo_log 表
CREATE TABLE IF NOT EXISTS `undo_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
  `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context` VARCHAR(128) NOT NULL COMMENT '上下文',
  `rollback_info` LONGBLOB NOT NULL COMMENT '回滚信息',
  `log_status` INT NOT NULL COMMENT '0-正常，1-防御性检查',
  `log_created` DATETIME NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_xid_branch_id` (`xid`, `branch_id`),
  KEY `idx_log_created` (`log_created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT 模式 undo_log 表';

-- 插入测试数据
INSERT INTO `accounts` (`user_id`, `balance`) VALUES 
(1, 1000.00),
(2, 500.00)
ON DUPLICATE KEY UPDATE `balance` = VALUES(`balance`);
