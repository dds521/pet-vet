-- ============================================
-- 微信用户表建表SQL
-- 用于存储微信小程序用户的登录信息
-- ============================================

CREATE TABLE IF NOT EXISTS `wechat_users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `open_id` VARCHAR(64) NOT NULL COMMENT '微信openId，每个用户在每个小程序中的唯一标识',
  `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId，同一微信开放平台账号下的应用，unionId是唯一的',
  `nick_name` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL',
  `gender` TINYINT DEFAULT 0 COMMENT '用户性别：0-未知，1-男，2-女',
  `country` VARCHAR(50) DEFAULT NULL COMMENT '用户所在国家',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '用户所在省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '用户所在城市',
  `language` VARCHAR(20) DEFAULT NULL COMMENT '用户语言',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `status` TINYINT DEFAULT 1 COMMENT '用户状态：0-禁用，1-启用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  KEY `idx_union_id` (`union_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户表';
