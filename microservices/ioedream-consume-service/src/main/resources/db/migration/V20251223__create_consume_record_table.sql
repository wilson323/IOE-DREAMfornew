-- ==================== 消费记录表 ====================
-- 用于记录用户的所有消费交易信息
-- 支持在线/离线消费、退款、撤销等操作
-- 创建时间: 2025-12-23
-- 作者: IOE-DREAM架构团队

CREATE TABLE IF NOT EXISTS `t_consume_record` (
  `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) NOT NULL COMMENT '用户姓名',
  `device_id` VARCHAR(32) NOT NULL COMMENT '设备ID',
  `device_name` VARCHAR(50) DEFAULT NULL COMMENT '设备名称',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID',
  `merchant_name` VARCHAR(100) DEFAULT NULL COMMENT '商户名称',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '消费金额',
  `original_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '原始金额（优惠前）',
  `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `consume_type` VARCHAR(20) NOT NULL COMMENT '消费类型（MEAL-餐饮，SNACK-零食，DRINK-饮品）',
  `consume_type_name` VARCHAR(50) DEFAULT NULL COMMENT '消费类型名称',
  `product_detail` TEXT DEFAULT NULL COMMENT '商品明细（JSON格式）',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式（BALANCE-余额，CARD-卡，CASH-现金）',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `transaction_no` VARCHAR(64) DEFAULT NULL COMMENT '交易流水号',
  `transaction_status` TINYINT NOT NULL DEFAULT 1 COMMENT '交易状态（1-成功，2-处理中，3-失败）',
  `consume_status` TINYINT NOT NULL DEFAULT 1 COMMENT '消费状态（1-正常，2-已退款，3-已撤销）',
  `consume_time` DATETIME NOT NULL COMMENT '消费时间',
  `consume_location` VARCHAR(100) DEFAULT NULL COMMENT '消费地点',
  `refund_status` TINYINT NOT NULL DEFAULT 0 COMMENT '退款状态（0-未退款，1-部分退款，2-全额退款）',
  `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  `refund_reason` VARCHAR(200) DEFAULT NULL COMMENT '退款原因',
  `offline_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '离线标记（0-在线，1-离线）',
  `sync_status` TINYINT NOT NULL DEFAULT 1 COMMENT '同步状态（0-未同步，1-已同步）',
  `sync_time` DATETIME DEFAULT NULL COMMENT '同步时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`record_id`),
  UNIQUE KEY `uk_order_no` (`order_no`, `deleted_flag`),
  KEY `idx_account_id` (`account_id`, `consume_time`),
  KEY `idx_user_id` (`user_id`, `consume_time`),
  KEY `idx_transaction_no` (`transaction_no`),
  KEY `idx_sync_status` (`offline_flag`, `sync_status`),
  KEY `idx_consume_time` (`consume_time`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- 添加索引优化查询性能
CREATE INDEX idx_user_time ON t_consume_record(user_id, consume_time, account_id);
CREATE INDEX idx_payment_method ON t_consume_record(payment_method, transaction_status);
CREATE INDEX idx_consume_type ON t_consume_record(consume_type, consume_time);
CREATE INDEX idx_refund_status ON t_consume_record(refund_status, consume_status);
