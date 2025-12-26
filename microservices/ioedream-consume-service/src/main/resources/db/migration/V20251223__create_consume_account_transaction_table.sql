-- ==================== 账户变动记录表 ====================
-- 用于记录消费账户的所有余额变动
-- 包含充值、消费、退款、扣减、调整等所有操作
-- 创建时间: 2025-12-23
-- 作者: IOE-DREAM架构团队

CREATE TABLE IF NOT EXISTS `t_consume_account_transaction` (
  `transaction_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `transaction_type` VARCHAR(20) NOT NULL COMMENT '交易类型（CONSUME-消费，RECHARGE-充值，REFUND-退款，DEDUCT-扣减，ADJUST-调整）',
  `transaction_no` VARCHAR(64) NOT NULL COMMENT '交易流水号',
  `business_no` VARCHAR(64) NOT NULL COMMENT '业务编号',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '变动金额（正-增加，负-减少）',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '变动前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '变动后余额',
  `frozen_amount_before` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '变动前冻结金额',
  `frozen_amount_after` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '变动后冻结金额',
  `related_record_id` BIGINT DEFAULT NULL COMMENT '关联记录ID',
  `related_order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
  `transaction_status` TINYINT NOT NULL DEFAULT 1 COMMENT '交易状态（1-成功，2-处理中，3-失败）',
  `fail_reason` VARCHAR(200) DEFAULT NULL COMMENT '失败原因',
  `transaction_time` DATETIME NOT NULL COMMENT '交易时间',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作员ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员姓名',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`transaction_id`),
  UNIQUE KEY `uk_transaction_no` (`transaction_no`),
  KEY `idx_account_id` (`account_id`, `transaction_time`),
  KEY `idx_user_id` (`user_id`, `transaction_time`),
  KEY `idx_business_no` (`business_no`),
  KEY `idx_transaction_type` (`transaction_type`, `transaction_time`),
  KEY `idx_related_order_no` (`related_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户变动记录表';

-- 添加索引优化查询性能
CREATE INDEX idx_account_time ON t_consume_account_transaction(account_id, transaction_time DESC);
CREATE INDEX idx_user_time_type ON t_consume_account_transaction(user_id, transaction_time, transaction_type);
CREATE INDEX idx_transaction_status ON t_consume_account_transaction(transaction_status, transaction_time);
CREATE INDEX idx_related_record ON t_consume_account_transaction(related_record_id);
