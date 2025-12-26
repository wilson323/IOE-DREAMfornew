-- ==================== 账户服务补偿记录表 ====================
-- 用于存储账户服务调用失败时的补偿记录
-- 支持自动重试和人工补偿
-- 创建时间: 2025-12-23
-- 作者: IOE-DREAM架构团队

CREATE TABLE IF NOT EXISTS `t_account_compensation` (
  `compensation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '补偿记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `operation` VARCHAR(20) NOT NULL COMMENT '操作类型（INCREASE-增加, DECREASE-扣减）',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '金额',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型（SUBSIDY_GRANT, CONSUME等）',
  `business_no` VARCHAR(100) NOT NULL COMMENT '业务编号（用于幂等性控制）',
  `related_business_no` VARCHAR(100) DEFAULT NULL COMMENT '关联业务编号',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING-待处理, SUCCESS-成功, FAILED-失败, CANCELLED-已取消）',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误码',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `extended_params` TEXT DEFAULT NULL COMMENT '扩展参数（JSON格式）',
  `next_retry_time` DATETIME DEFAULT NULL COMMENT '下次重试时间',
  `last_retry_time` DATETIME DEFAULT NULL COMMENT '最后重试时间',
  `success_time` DATETIME DEFAULT NULL COMMENT '成功时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`compensation_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_business_no` (`business_no`),
  KEY `idx_status` (`status`),
  KEY `idx_next_retry_time` (`next_retry_time`),
  KEY `idx_create_time` (`create_time`),
  UNIQUE KEY `uk_business_no` (`business_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户服务补偿记录表';

-- 添加索引优化查询性能
CREATE INDEX idx_operation_status ON t_account_compensation(operation, status);
CREATE INDEX idx_retry_count ON t_account_compensation(retry_count, max_retry_count);
