-- ==================== POSID核心表创建脚本 ====================
-- 消费模块完整企业级实现 - 数据库重构
-- 使用POSID_*命名规范，完全符合业务文档ER图设计
-- 创建时间: 2025-12-23
-- 作者: IOE-DREAM架构团队
-- 版本: V1.0.0

-- ==================== 1. POSID_ACCOUNT 账户主表 ====================
CREATE TABLE IF NOT EXISTS `POSID_ACCOUNT` (
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
  `account_status` TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态（0-冻结，1-正常，2-注销）',
  `password` VARCHAR(255) DEFAULT NULL COMMENT '支付密码',
  `enable_auto_recharge` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用自动充值',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID账户主表';

-- ==================== 2. POSID_ACCOUNTKIND 账户类别表 ====================
CREATE TABLE IF NOT EXISTS `POSID_ACCOUNTKIND` (
  `kind_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '类别ID',
  `kind_code` VARCHAR(50) NOT NULL COMMENT '类别编码',
  `kind_name` VARCHAR(100) NOT NULL COMMENT '类别名称',
  `kind_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类别类型（1-员工，2-访客，3-临时）',
  `mode_config` JSON NOT NULL COMMENT '各模式参数配置（6种消费模式配置）',
  `discount_type` TINYINT NOT NULL DEFAULT 0 COMMENT '折扣类型（0-无折扣，1-固定折扣，2-阶梯折扣）',
  `discount_value` DECIMAL(5,4) DEFAULT NULL COMMENT '折扣值',
  `date_max_money` DECIMAL(10,2) DEFAULT NULL COMMENT '每日最大消费金额',
  `date_max_count` INT DEFAULT NULL COMMENT '每日最大消费次数',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越小优先级越高）',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用，1-启用）',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`kind_id`),
  UNIQUE KEY `uk_kind_code` (`kind_code`),
  KEY `idx_kind_type` (`kind_type`),
  KEY `idx_is_enabled` (`is_enabled`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID账户类别表（含mode_config）';

-- ==================== 3. POSID_AREA 区域表 ====================
CREATE TABLE IF NOT EXISTS `POSID_AREA` (
  `area_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '区域ID',
  `area_code` VARCHAR(50) NOT NULL COMMENT '区域编码',
  `area_name` VARCHAR(100) NOT NULL COMMENT '区域名称',
  `parent_area_id` BIGINT DEFAULT NULL COMMENT '父区域ID',
  `area_level` TINYINT NOT NULL DEFAULT 1 COMMENT '区域层级（1-园区，2-建筑，3-楼层，4-房间）',
  `manage_mode` TINYINT NOT NULL DEFAULT 1 COMMENT '管理模式（1-餐别制，2-超市制，3-混合）',
  `fixed_value_config` JSON DEFAULT NULL COMMENT '定值配置（各餐别定值金额）',
  `area_config` JSON DEFAULT NULL COMMENT '区域权限配置（子区域权限等）',
  `business_hours` JSON DEFAULT NULL COMMENT '营业时间配置',
  `device_ids` JSON DEFAULT NULL COMMENT '关联设备ID列表',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `update_user_id` BIGINT DEFAULT NULL COMMENT '更新人ID',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`area_id`),
  UNIQUE KEY `uk_area_code` (`area_code`),
  KEY `idx_parent_area_id` (`parent_area_id`),
  KEY `idx_area_level` (`area_level`),
  KEY `idx_manage_mode` (`manage_mode`),
  KEY `idx_is_enabled` (`is_enabled`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID区域表（含fixed_value_config和area_config）';

-- ==================== 4. POSID_MEAL_CATEGORY 餐别分类表 ====================
CREATE TABLE IF NOT EXISTS `POSID_MEAL_CATEGORY` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_code` VARCHAR(50) NOT NULL COMMENT '分类编码',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `category_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_is_enabled` (`is_enabled`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID餐别分类表';

-- ==================== 5. POSID_MEAL 餐别表 ====================
CREATE TABLE IF NOT EXISTS `POSID_MEAL` (
  `meal_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '餐别ID',
  `meal_code` VARCHAR(50) NOT NULL COMMENT '餐别编码',
  `meal_name` VARCHAR(50) NOT NULL COMMENT '餐别名称',
  `category_id` BIGINT NOT NULL COMMENT '所属分类ID',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `advance_days` INT NOT NULL DEFAULT 0 COMMENT '提前订餐天数',
  `cutoff_time` TIME DEFAULT NULL COMMENT '截单时间',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`meal_id`),
  UNIQUE KEY `uk_meal_code` (`meal_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_is_enabled` (`is_enabled`, `deleted_flag`),
  CONSTRAINT `fk_meal_category` FOREIGN KEY (`category_id`) REFERENCES `POSID_MEAL_CATEGORY` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID餐别表';

-- ==================== 6. POSID_TRANSACTION 交易流水表（按月分区）====================
CREATE TABLE IF NOT EXISTS `POSID_TRANSACTION` (
  `transaction_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(50) NOT NULL COMMENT '用户姓名',
  `device_id` VARCHAR(32) NOT NULL COMMENT '设备ID',
  `device_name` VARCHAR(50) DEFAULT NULL COMMENT '设备名称',
  `merchant_id` BIGINT DEFAULT NULL COMMENT '商户ID',
  `merchant_name` VARCHAR(100) DEFAULT NULL COMMENT '商户名称',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
  `consume_mode` VARCHAR(20) NOT NULL COMMENT '消费模式（FIXED_AMOUNT/FREE_AMOUNT/METERED/PRODUCT/ORDER/INTELLIGENCE）',
  `consume_type` VARCHAR(20) NOT NULL COMMENT '消费类型（MEAL-餐饮，SNACK-零食，DRINK-饮品）',
  `consume_type_name` VARCHAR(50) DEFAULT NULL COMMENT '消费类型名称',
  `product_detail` TEXT DEFAULT NULL COMMENT '商品明细（JSON格式）',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式（BALANCE-余额，SUBSIDY-补贴，CARD-卡）',
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
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`transaction_id`, `create_time`),
  UNIQUE KEY `uk_order_no` (`order_no`, `deleted_flag`),
  KEY `idx_account_id` (`account_id`, `consume_time`),
  KEY `idx_user_id` (`user_id`, `consume_time`),
  KEY `idx_transaction_no` (`transaction_no`),
  KEY `idx_consume_time` (`consume_time`),
  KEY `idx_device_id` (`device_id`),
  KEY `idx_offline_sync` (`offline_flag`, `sync_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID交易流水表（按月分区）'
PARTITION BY RANGE (TO_DAYS(create_time)) (
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
  PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
  PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
  PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
  PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
  PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ==================== 7. POSID_SUBSIDY_TYPE 补贴类型表 ====================
CREATE TABLE IF NOT EXISTS `POSID_SUBSIDY_TYPE` (
  `subsidy_type_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '补贴类型ID',
  `type_code` VARCHAR(50) NOT NULL COMMENT '类型编码',
  `type_name` VARCHAR(50) NOT NULL COMMENT '类型名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越小优先级越高）',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`subsidy_type_id`),
  UNIQUE KEY `uk_type_code` (`type_code`),
  KEY `idx_is_enabled` (`is_enabled`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID补贴类型表';

-- ==================== 8. POSID_SUBSIDY_ACCOUNT 补贴账户表 ====================
CREATE TABLE IF NOT EXISTS `POSID_SUBSIDY_ACCOUNT` (
  `subsidy_account_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '补贴账户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `subsidy_type_id` BIGINT NOT NULL COMMENT '补贴类型ID',
  `account_code` VARCHAR(50) NOT NULL COMMENT '账户编码',
  `balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '补贴余额',
  `original_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '原始发放金额',
  `used_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已使用金额',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `account_status` TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态（1-正常，2-冻结，3-已过期，4-已清零）',
  `grant_time` DATETIME NOT NULL COMMENT '发放时间',
  `clear_time` DATETIME DEFAULT NULL COMMENT '清零时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`subsidy_account_id`),
  UNIQUE KEY `uk_account_code` (`account_code`),
  KEY `idx_user_id` (`user_id`, `subsidy_type_id`),
  KEY `idx_subsidy_type_id` (`subsidy_type_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_account_status` (`account_status`, `deleted_flag`),
  CONSTRAINT `fk_subsidy_type` FOREIGN KEY (`subsidy_type_id`) REFERENCES `POSID_SUBSIDY_TYPE` (`subsidy_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID补贴账户表';

-- ==================== 9. POSID_SUBSIDY_FLOW 补贴流水表 ====================
CREATE TABLE IF NOT EXISTS `POSID_SUBSIDY_FLOW` (
  `flow_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `subsidy_account_id` BIGINT NOT NULL COMMENT '补贴账户ID',
  `flow_type` VARCHAR(20) NOT NULL COMMENT '流水类型（GRANT-发放，USE-使用，EXPIRE-过期清零，MANUAL_CLEAR-手动清零，FREEZE-冻结，UNFREEZE-解冻）',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额（正数=增加，负数=减少）',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '操作前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '操作后余额',
  `related_order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`flow_id`),
  KEY `idx_subsidy_account_id` (`subsidy_account_id`),
  KEY `idx_flow_type` (`flow_type`),
  KEY `idx_related_order_no` (`related_order_no`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_subsidy_flow_account` FOREIGN KEY (`subsidy_account_id`) REFERENCES `POSID_SUBSIDY_ACCOUNT` (`subsidy_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID补贴流水表';

-- ==================== 10. POSID_RECHARGE_ORDER 充值订单表（按月分区）====================
CREATE TABLE IF NOT EXISTS `POSID_RECHARGE_ORDER` (
  `recharge_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '充值ID',
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `recharge_no` VARCHAR(64) NOT NULL COMMENT '充值单号',
  `recharge_amount` DECIMAL(10,2) NOT NULL COMMENT '充值金额',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式',
  `payment_channel` VARCHAR(50) DEFAULT NULL COMMENT '支付渠道',
  `order_status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态（1-待支付，2-支付中，3-成功，4-失败）',
  `transaction_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方交易流水号',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `success_time` DATETIME DEFAULT NULL COMMENT '到账时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`recharge_id`, `create_time`),
  UNIQUE KEY `uk_recharge_no` (`recharge_no`, `deleted_flag`),
  KEY `idx_account_id` (`account_id`, `create_time`),
  KEY `idx_user_id` (`user_id`, `create_time`),
  KEY `idx_order_status` (`order_status`, `create_time`),
  KEY `idx_transaction_no` (`transaction_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID充值订单表（按月分区）'
PARTITION BY RANGE (TO_DAYS(create_time)) (
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
  PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
  PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
  PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
  PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
  PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ==================== 11. POSID_CAPITAL_FLOW 资金流水表（按月分区）====================
CREATE TABLE IF NOT EXISTS `POSID_CAPITAL_FLOW` (
  `flow_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '流水ID',
  `account_id` BIGINT NOT NULL COMMENT '账户ID',
  `flow_type` VARCHAR(20) NOT NULL COMMENT '流水类型（RECHARGE-充值，CONSUME-消费，REFUND-退款，GRANT-补贴发放，SUBSIDY_USE-补贴使用）',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额（正数=增加，负数=减少）',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '操作前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '操作后余额',
  `related_record_id` BIGINT DEFAULT NULL COMMENT '关联交易记录ID',
  `related_order_no` VARCHAR(64) DEFAULT NULL COMMENT '关联订单号',
  `transaction_no` VARCHAR(64) DEFAULT NULL COMMENT '交易流水号',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`flow_id`, `create_time`),
  KEY `idx_account_id` (`account_id`, `create_time`),
  KEY `idx_flow_type` (`flow_type`, `create_time`),
  KEY `idx_related_record_id` (`related_record_id`),
  KEY `idx_related_order_no` (`related_order_no`),
  KEY `idx_transaction_no` (`transaction_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='POSID资金流水表（按月分区）'
PARTITION BY RANGE (TO_DAYS(create_time)) (
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
  PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
  PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
  PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
  PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
  PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ==================== 索引优化查询性能 ====================
-- POSID_ACCOUNT索引
CREATE INDEX idx_user_status ON POSID_ACCOUNT(user_id, account_status, deleted_flag);
CREATE INDEX idx_balance_range ON POSID_ACCOUNT(balance, account_status);

-- POSID_ACCOUNTKIND索引 - JSON字段虚拟列（MySQL 8.0+）
ALTER TABLE POSID_ACCOUNTKIND ADD COLUMN consume_mode VARCHAR(20) AS (JSON_UNQUOTE(JSON_EXTRACT(mode_config, '$.consumeMode'))) VIRTUAL;
CREATE INDEX idx_consume_mode ON POSID_ACCOUNTKIND(consume_mode);

-- POSID_AREA索引 - JSON字段虚拟列
ALTER TABLE POSID_AREA ADD COLUMN manage_mode_val TINYINT AS (JSON_UNQUOTE(JSON_EXTRACT(fixed_value_config, '$.manageMode'))) VIRTUAL;
CREATE INDEX idx_manage_mode_val ON POSID_AREA(manage_mode_val);

-- POSID_SUBSIDY_ACCOUNT复合索引
CREATE INDEX idx_user_expire_status ON POSID_SUBSIDY_ACCOUNT(user_id, expire_time, account_status);
CREATE INDEX idx_expire_status ON POSID_SUBSIDY_ACCOUNT(expire_time, account_status);

-- ==================== 外键约束 ====================
-- POSID_TRANSACTION外键
ALTER TABLE POSID_TRANSACTION ADD CONSTRAINT fk_transaction_account FOREIGN KEY (account_id) REFERENCES POSID_ACCOUNT(account_id);

-- POSID_RECHARGE_ORDER外键
ALTER TABLE POSID_RECHARGE_ORDER ADD CONSTRAINT fk_recharge_account FOREIGN KEY (account_id) REFERENCES POSID_ACCOUNT(account_id);

-- POSID_CAPITAL_FLOW外键
ALTER TABLE POSID_CAPITAL_FLOW ADD CONSTRAINT fk_capital_account FOREIGN KEY (account_id) REFERENCES POSID_ACCOUNT(account_id);

-- ==================== 初始化数据 ====================
-- 初始化餐别分类数据
INSERT INTO POSID_MEAL_CATEGORY (category_code, category_name, category_order) VALUES
('BREAKFAST', '早餐', 1),
('LUNCH', '午餐', 2),
('DINNER', '晚餐', 3),
('SNACK', '小食', 4);

-- 初始化补贴类型数据
INSERT INTO POSID_SUBSIDY_TYPE (type_code, type_name, description, priority) VALUES
('MEAL', '餐补', '用于餐费消费的补贴', 1),
('TRAFFIC', '交通补', '用于交通费的补贴', 2),
('COMMUNICATION', '通讯补', '用于通讯费的补贴', 3),
('ACCOMMODATION', '住宿补', '用于住宿费的补贴', 4),
('OTHER', '其他补贴', '其他用途的补贴', 5);
