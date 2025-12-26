-- ==================== 消费账户表 ====================
-- 用于管理用户的消费账户余额和支付信息
-- 支持在线/离线消费、余额查询、自动充值等功能
-- 创建时间: 2025-12-23
-- 作者: IOE-DREAM架构团队

CREATE TABLE IF NOT EXISTS `t_consume_account` (
  `account_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `account_code` VARCHAR(50) NOT NULL COMMENT '账户编码',
  `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
  `account_type` TINYINT NOT NULL DEFAULT 1 COMMENT '账户类型（1-员工账户，2-访客账户，3-临时账户）',
  `balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
  `frozen_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
  `credit_limit` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '信用额度',
  `total_recharge` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值',
  `total_consume` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费',
  `account_status` TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态（0-冻结，1-正常）',
  `password` VARCHAR(255) DEFAULT NULL COMMENT '支付密码',
  `enable_auto_recharge` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用自动充值（0-否，1-是）',
  `auto_recharge_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '自动充值金额',
  `auto_recharge_threshold` DECIMAL(12,2) DEFAULT NULL COMMENT '自动充值阈值',
  `last_consume_time` DATETIME DEFAULT NULL COMMENT '最后消费时间',
  `last_recharge_time` DATETIME DEFAULT NULL COMMENT '最后充值时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `uk_account_code` (`account_code`),
  UNIQUE KEY `uk_user_id` (`user_id`, `deleted_flag`),
  KEY `idx_account_status` (`account_status`, `deleted_flag`),
  KEY `idx_account_type` (`account_type`),
  KEY `idx_last_consume_time` (`last_consume_time`),
  KEY `idx_balance` (`balance`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费账户表';

-- 添加索引优化查询性能
CREATE INDEX idx_user_status ON t_consume_account(user_id, account_status);
CREATE INDEX idx_balance_range ON t_consume_account(balance, account_status);
CREATE INDEX idx_auto_recharge ON t_consume_account(enable_auto_recharge, account_status);
