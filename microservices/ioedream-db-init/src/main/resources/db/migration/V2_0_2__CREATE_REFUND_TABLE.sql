-- =====================================================
-- IOE-DREAM 退款管理表创建迁移脚本
-- 版本: V2.0.2
-- 描述: 创建完整的退款管理表，支持退款申请、审批、处理流程
-- 兼容: 确保前后端API 100%兼容
-- 创建时间: 2025-01-30
-- 执行顺序: 06-v2.0.2__create-refund-table.sql (在05-v2.0.1之后执行)
-- 数据库名: ioedream
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 1. 创建退款申请表 (t_consume_refund)
-- =====================================================

CREATE TABLE t_consume_refund (
    -- 主键和基础字段
    refund_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '退款ID（主键）',
    refund_no VARCHAR(50) NOT NULL COMMENT '退款单号（唯一）',
    transaction_no VARCHAR(50) NOT NULL COMMENT '原消费交易流水号',
    order_no VARCHAR(50) COMMENT '原订单号',

    -- 用户信息字段
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名（冗余字段）',
    user_phone VARCHAR(20) COMMENT '用户手机号（冗余字段）',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型：1-员工 2-访客 3-临时用户',

    -- 账户信息字段
    account_id BIGINT NOT NULL COMMENT '账户ID',
    account_no VARCHAR(50) COMMENT '账户编号',
    account_name VARCHAR(100) COMMENT '账户名称',

    -- 退款金额字段
    original_amount DECIMAL(15,2) NOT NULL COMMENT '原消费金额',
    refund_amount DECIMAL(15,2) NOT NULL COMMENT '退款金额',
    refund_reason VARCHAR(500) NOT NULL COMMENT '退款原因',
    refund_type TINYINT DEFAULT 1 COMMENT '退款类型：1-全额退款 2-部分退款 3-补差退款',

    -- 状态流程字段
    refund_status TINYINT DEFAULT 1 COMMENT '退款状态：1-申请中 2-审批中 3-审批通过 4-处理中 5-已完成 6-已拒绝 7-已取消',
    apply_time DATETIME NOT NULL COMMENT '申请时间',
    approve_time DATETIME COMMENT '审批时间',
    process_time DATETIME COMMENT '处理时间',
    complete_time DATETIME COMMENT '完成时间',

    -- 审批信息字段
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(100) COMMENT '审批人姓名',
    approve_comment VARCHAR(1000) COMMENT '审批意见',
    approve_result TINYINT COMMENT '审批结果：1-通过 2-拒绝',

    -- 处理信息字段
    processor_id BIGINT COMMENT '处理人ID',
    processor_name VARCHAR(100) COMMENT '处理人姓名',
    process_method VARCHAR(20) COMMENT '处理方式：BALANCE-退还余额 BANK-银行转账 CASH-现金',
    process_result VARCHAR(500) COMMENT '处理结果说明',

    -- 退款方式字段
    refund_method VARCHAR(20) DEFAULT 'BALANCE' COMMENT '退款方式：BALANCE-原路返回 BALANCE-退余额 WECHAT-微信 ALIPAY-支付宝 BANK-银行转账',
    refund_channel VARCHAR(20) COMMENT '退款渠道：ONLINE-线上 OFFLINE-线下 AUTO-自动',
    third_party_refund_no VARCHAR(100) COMMENT '第三方退款单号',

    -- 关联信息字段
    original_record_id BIGINT COMMENT '原始消费记录ID',
    related_refund_id BIGINT COMMENT '关联退款ID（部分退款时关联）',

    -- 时间和地点信息
    consume_time DATETIME COMMENT '原消费时间',
    consume_location VARCHAR(200) COMMENT '消费地点',
    consume_device VARCHAR(100) COMMENT '消费设备',

    -- 扩展字段
    extend_data TEXT COMMENT '扩展数据JSON（存储业务扩展字段）',
    attachments TEXT COMMENT '附件信息JSON（存储凭证图片等）',
    remark VARCHAR(1000) COMMENT '备注',

    -- 审计字段（继承BaseEntity）
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    -- 索引定义
    INDEX idx_refund_refund_no (refund_no),
    INDEX idx_refund_transaction_no (transaction_no),
    INDEX idx_refund_user_id (user_id),
    INDEX idx_refund_account_id (account_id),
    INDEX idx_refund_status (refund_status),
    INDEX idx_refund_apply_time (apply_time),
    INDEX idx_refund_approve_time (approve_time),
    INDEX idx_refund_complete_time (complete_time),
    INDEX idx_refund_approver_id (approver_id),
    INDEX idx_refund_processor_id (processor_id),
    INDEX idx_refund_original_record (original_record_id),
    INDEX idx_refund_create_time (create_time),
    UNIQUE INDEX uk_refund_refund_no (refund_no, deleted_flag),
    UNIQUE INDEX uk_refund_transaction_no (transaction_no, refund_id) WHERE deleted_flag = 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费退款申请表';

-- =====================================================
-- 2. 创建退款审批记录表 (t_consume_refund_approval)
-- =====================================================

CREATE TABLE t_consume_refund_approval (
    -- 主键和基础字段
    approval_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批记录ID',
    refund_id BIGINT NOT NULL COMMENT '退款ID',
    approval_step TINYINT NOT NULL COMMENT '审批步骤：1-一级审批 2-二级审批 3-终审',
    approval_type TINYINT DEFAULT 1 COMMENT '审批类型：1-正常审批 2-特殊审批 3-加急审批',

    -- 审批人信息
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(100) NOT NULL COMMENT '审批人姓名',
    approver_role VARCHAR(50) COMMENT '审批人角色',
    approver_department VARCHAR(100) COMMENT '审批人部门',

    -- 审批决策
    approval_result TINYINT NOT NULL COMMENT '审批结果：1-通过 2-拒绝 3-转办 4-撤销',
    approval_comment VARCHAR(1000) COMMENT '审批意见',
    approval_attachments TEXT COMMENT '审批附件JSON',

    -- 时间信息
    approval_time DATETIME NOT NULL COMMENT '审批时间',
    receive_time DATETIME COMMENT '接收审批时间',
    complete_time DATETIME COMMENT '完成审批时间',

    -- 状态信息
    approval_status TINYINT DEFAULT 1 COMMENT '审批状态：1-待审批 2-审批中 3-已完成 4-已撤销',
    is_current_step TINYINT DEFAULT 1 COMMENT '是否当前步骤：1-是 0-否',

    -- 扩展字段
    extend_data TEXT COMMENT '扩展数据JSON',
    remark VARCHAR(500) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    -- 索引定义
    INDEX idx_approval_refund_id (refund_id),
    INDEX idx_approval_approver_id (approver_id),
    INDEX idx_approval_step (approval_step),
    INDEX idx_approval_result (approval_result),
    INDEX idx_approval_status (approval_status),
    INDEX idx_approval_current_step (is_current_step),
    INDEX idx_approval_approval_time (approval_time),
    INDEX idx_approval_create_time (create_time),
    UNIQUE INDEX uk_approval_refund_step (refund_id, approval_step) WHERE deleted_flag = 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款审批记录表';

-- =====================================================
-- 3. 创建退款处理记录表 (t_consume_refund_process)
-- =====================================================

CREATE TABLE t_consume_refund_process (
    -- 主键和基础字段
    process_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '处理记录ID',
    refund_id BIGINT NOT NULL COMMENT '退款ID',
    process_step VARCHAR(50) NOT NULL COMMENT '处理步骤：VALIDATE-验证 REFUND-退款 NOTIFY-通知',

    -- 处理信息
    processor_id BIGINT NOT NULL COMMENT '处理人ID',
    processor_name VARCHAR(100) NOT NULL COMMENT '处理人姓名',
    processor_role VARCHAR(50) COMMENT '处理人角色',

    -- 处理结果
    process_result TINYINT NOT NULL COMMENT '处理结果：1-成功 2-失败 3-部分成功',
    process_status TINYINT DEFAULT 1 COMMENT '处理状态：1-处理中 2-已完成 3-失败',
    process_message VARCHAR(1000) COMMENT '处理消息',
    process_detail TEXT COMMENT '处理详情JSON',

    -- 处理方式和渠道
    process_method VARCHAR(20) NOT NULL COMMENT '处理方式：BALANCE-余额退还 BANK-银行转账 WECHAT-微信 ALIPAY-支付宝',
    process_channel VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '处理渠道：SYSTEM-系统 MANUAL-手动 API-接口回调',

    -- 时间信息
    process_time DATETIME NOT NULL COMMENT '处理时间',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT COMMENT '处理耗时（毫秒）',

    -- 关联信息
    external_refund_no VARCHAR(100) COMMENT '外部退款单号',
    external_transaction_id VARCHAR(100) COMMENT '外部交易ID',
    callback_data TEXT COMMENT '回调数据JSON',

    -- 扩展字段
    extend_data TEXT COMMENT '扩展数据JSON',
    remark VARCHAR(500) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    -- 索引定义
    INDEX idx_process_refund_id (refund_id),
    INDEX idx_process_processor_id (processor_id),
    INDEX idx_process_step (process_step),
    INDEX idx_process_result (process_result),
    INDEX idx_process_status (process_status),
    INDEX idx_process_method (process_method),
    INDEX idx_process_channel (process_channel),
    INDEX idx_process_process_time (process_time),
    INDEX idx_process_create_time (create_time),
    INDEX idx_process_external_refund_no (external_refund_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款处理记录表';

-- =====================================================
-- 4. 创建退款配置表 (t_consume_refund_config)
-- =====================================================

CREATE TABLE t_consume_refund_config (
    -- 主键和基础字段
    config_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '配置ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型：RULE-规则 POLICY-策略 LIMIT-限制',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_name VARCHAR(200) COMMENT '配置名称',
    config_description TEXT COMMENT '配置描述',

    -- 应用范围
    apply_scope VARCHAR(50) DEFAULT 'GLOBAL' COMMENT '应用范围：GLOBAL-全局 AREA-区域 DEPARTMENT-部门 ACCOUNT-账户',
    apply_scope_id BIGINT COMMENT '应用范围ID（区域、部门、账户ID）',

    -- 状态和时间
    config_status TINYINT DEFAULT 1 COMMENT '配置状态：1-启用 0-禁用',
    effective_time DATETIME COMMENT '生效时间',
    expire_time DATETIME COMMENT '失效时间',

    -- 优先级和版本
    priority INT DEFAULT 0 COMMENT '优先级（数值越大优先级越高）',
    version INT DEFAULT 1 COMMENT '配置版本',

    -- 扩展字段
    extend_data TEXT COMMENT '扩展数据JSON',
    remark VARCHAR(1000) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',

    -- 索引定义
    INDEX idx_config_type_key (config_type, config_key),
    INDEX idx_config_scope (apply_scope, apply_scope_id),
    INDEX idx_config_status (config_status),
    INDEX idx_config_priority (priority),
    UNIQUE INDEX uk_config_type_key_scope (config_type, config_key, apply_scope, apply_scope_id) WHERE deleted_flag = 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款配置表';

-- =====================================================
-- 5. 创建退款统计表 (t_consume_refund_statistics)
-- =====================================================

CREATE TABLE t_consume_refund_statistics (
    -- 主键和基础字段
    statistics_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    statistics_date DATE NOT NULL COMMENT '统计日期',
    statistics_type VARCHAR(20) NOT NULL COMMENT '统计类型：DAILY-日报 WEEKLY-周报 MONTHLY-月报',
    statistics_scope VARCHAR(50) NOT NULL COMMENT '统计范围：GLOBAL-全局 AREA-区域 DEPARTMENT-部门',
    statistics_scope_id BIGINT COMMENT '统计范围ID',

    -- 退款数量统计
    total_refund_count INT DEFAULT 0 COMMENT '总退款数量',
    success_refund_count INT DEFAULT 0 COMMENT '成功退款数量',
    failed_refund_count INT DEFAULT 0 COMMENT '失败退款数量',
    pending_refund_count INT DEFAULT 0 COMMENT '待处理退款数量',

    -- 退款金额统计
    total_refund_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '总退款金额',
    success_refund_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '成功退款金额',
    failed_refund_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '失败退款金额',
    pending_refund_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '待处理退款金额',

    -- 按类型统计
    full_refund_count INT DEFAULT 0 COMMENT '全额退款数量',
    partial_refund_count INT DEFAULT 0 COMMENT '部分退款数量',
    compensation_refund_count INT DEFAULT 0 COMMENT '补差退款数量',

    -- 按方式统计
    balance_refund_count INT DEFAULT 0 COMMENT '余额退款数量',
    bank_refund_count INT DEFAULT 0 COMMENT '银行转账数量',
    third_party_refund_count INT DEFAULT 0 COMMENT '第三方退款数量',

    -- 按原因统计（TOP5）
    refund_reason_1_count INT DEFAULT 0 COMMENT '退款原因1数量',
    refund_reason_1_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '退款原因1金额',
    refund_reason_2_count INT DEFAULT 0 COMMENT '退款原因2数量',
    refund_reason_2_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '退款原因2金额',

    -- 处理效率统计
    avg_approval_time_minutes INT DEFAULT 0 COMMENT '平均审批时间（分钟）',
    avg_process_time_minutes INT DEFAULT 0 COMMENT '平均处理时间（分钟）',
    avg_completion_hours DECIMAL(8,2) DEFAULT 0.00 COMMENT '平均完成时间（小时）',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引定义
    INDEX idx_statistics_date_type (statistics_date, statistics_type),
    INDEX idx_statistics_scope (statistics_scope, statistics_scope_id),
    INDEX idx_statistics_create_time (create_time),
    UNIQUE INDEX uk_statistics_date_type_scope (statistics_date, statistics_type, statistics_scope, statistics_scope_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款统计表';

-- =====================================================
-- 6. 创建外键约束
-- =====================================================

-- 退款审批记录外键
ALTER TABLE t_consume_refund_approval
ADD CONSTRAINT fk_approval_refund_id FOREIGN KEY (refund_id) REFERENCES t_consume_refund(refund_id) ON DELETE CASCADE;

-- 退款处理记录外键
ALTER TABLE t_consume_refund_process
ADD CONSTRAINT fk_process_refund_id FOREIGN KEY (refund_id) REFERENCES t_consume_refund(refund_id) ON DELETE CASCADE;

-- =====================================================
-- 7. 创建触发器
-- =====================================================

-- 退款状态变更触发器
DELIMITER $$
CREATE TRIGGER tr_refund_status_update
AFTER UPDATE ON t_consume_refund
FOR EACH ROW
BEGIN
    -- 当状态变更为已完成时，更新完成时间
    IF NEW.refund_status = 5 AND OLD.refund_status <> 5 THEN
        SET NEW.complete_time = NOW();
    END IF;

    -- 当状态变更为审批通过时，更新审批时间
    IF NEW.refund_status = 3 AND OLD.refund_status <> 3 THEN
        SET NEW.approve_time = NOW();
    END IF;

    -- 当状态变更为处理中时，更新处理时间
    IF NEW.refund_status = 4 AND OLD.refund_status <> 4 THEN
        SET NEW.process_time = NOW();
    END IF;
END$$
DELIMITER ;

-- =====================================================
-- 8. 插入初始配置数据
-- =====================================================

-- 退款规则配置
INSERT INTO t_consume_refund_config (config_type, config_key, config_value, config_name, config_description) VALUES
('RULE', 'REFUND_TIME_LIMIT', '168', '退款时限（小时）', '消费后多少小时内可以申请退款'),
('RULE', 'MAX_REFUND_COUNT_DAY', '5', '每日最大退款次数', '每个用户每日最多可申请退款的次数'),
('RULE', 'MAX_REFUND_AMOUNT_DAY', '1000.00', '每日最大退款金额', '每个用户每日最多可退款的金额'),
('RULE', 'AUTO_APPROVE_AMOUNT', '100.00', '自动审批金额', '小于等于此金额的退款申请自动审批通过'),
('RULE', 'FORCE_APPROVE_AMOUNT', '500.00', '强制审批金额', '大于此金额的退款申请必须人工审批'),
('POLICY', 'REFUND_FEE_RATE', '0.01', '退款手续费率', '退款手续费比例（0-1之间）'),
('POLICY', 'MIN_REFUND_AMOUNT', '0.01', '最小退款金额', '单次退款的最低金额'),
('POLICY', 'MAX_REFUND_AMOUNT', '10000.00', '最大退款金额', '单次退款的最高金额'),
('POLICY', 'REFUND_PROCESS_TIMEOUT', '72', '退款处理超时时间（小时）', '退款处理的最大允许时间'),
('LIMIT', 'EMPLOYEE_REFUND_LIMIT', '5000.00', '员工月度退款限额', '员工每月最多可退款的金额'),
('LIMIT', 'VISITOR_REFUND_LIMIT', '2000.00', '访客月度退款限额', '访客每月最多可退款的金额'),
('LIMIT', 'TEMP_USER_REFUND_LIMIT', '500.00', '临时用户月度退款限额', '临时用户每月最多可退款的金额');

-- =====================================================
-- 9. 创建视图
-- =====================================================

-- 退款详细信息视图
CREATE OR REPLACE VIEW v_consume_refund_detail AS
SELECT
    r.refund_id,
    r.refund_no,
    r.transaction_no,
    r.order_no,
    r.user_id,
    r.user_name,
    r.user_phone,
    r.user_type,
    r.account_id,
    r.account_no,
    r.account_name,
    r.original_amount,
    r.refund_amount,
    r.refund_reason,
    r.refund_type,
    r.refund_status,
    r.apply_time,
    r.approve_time,
    r.process_time,
    r.complete_time,
    r.approver_id,
    r.approver_name,
    r.approve_comment,
    r.approve_result,
    r.processor_id,
    r.processor_name,
    r.process_method,
    r.process_result,
    r.refund_method,
    r.refund_channel,
    r.third_party_refund_no,
    r.original_record_id,
    r.related_refund_id,
    r.consume_time,
    r.consume_location,
    r.consume_device,
    r.extend_data,
    r.attachments,
    r.remark,
    r.create_time,
    r.update_time,
    r.create_user_id,
    r.update_user_id,
    r.deleted_flag,
    r.version,
    -- 关联信息
    u.real_name as user_real_name,
    u.email as user_email,
    acc.balance as current_balance,
    acc.account_status as account_status,
    cr.consume_date as original_consume_date,
    cr.consume_type as original_consume_type,
    -- 统计信息
    (SELECT COUNT(*) FROM t_consume_refund_approval a WHERE a.refund_id = r.refund_id AND a.deleted_flag = 0) as approval_count,
    (SELECT COUNT(*) FROM t_consume_refund_process p WHERE p.refund_id = r.refund_id AND p.deleted_flag = 0) as process_count,
    -- 状态描述
    CASE r.refund_status
        WHEN 1 THEN '申请中'
        WHEN 2 THEN '审批中'
        WHEN 3 THEN '审批通过'
        WHEN 4 THEN '处理中'
        WHEN 5 THEN '已完成'
        WHEN 6 THEN '已拒绝'
        WHEN 7 THEN '已取消'
        ELSE '未知状态'
    END as status_description,
    -- 类型描述
    CASE r.refund_type
        WHEN 1 THEN '全额退款'
        WHEN 2 THEN '部分退款'
        WHEN 3 THEN '补差退款'
        ELSE '未知类型'
    END as type_description
FROM t_consume_refund r
LEFT JOIN t_common_user u ON r.user_id = u.user_id
LEFT JOIN t_consume_account acc ON r.account_id = acc.account_id
LEFT JOIN t_consume_record cr ON r.original_record_id = cr.id
WHERE r.deleted_flag = 0;

-- =====================================================
-- 10. 数据验证
-- =====================================================

-- 验证表创建情况
SELECT TABLE_NAME, TABLE_COMMENT
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME LIKE 't_consume_refund%'
ORDER BY TABLE_NAME;

-- 验证索引创建情况
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME LIKE 't_consume_refund%'
ORDER BY TABLE_NAME, INDEX_NAME;

-- 验证配置数据
SELECT config_type, config_key, config_value, config_name
FROM t_consume_refund_config
WHERE deleted_flag = 0
ORDER BY config_type, priority DESC;

-- =====================================================
-- 11. 权限设置
-- =====================================================

-- 为应用用户授权
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ioedream.t_consume_refund TO 'ioedream_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ioedream.t_consume_refund_approval TO 'ioedream_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ioedream.t_consume_refund_process TO 'ioedream_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ioedream.t_consume_refund_config TO 'ioedream_app'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ioedream.t_consume_refund_statistics TO 'ioedream_app'@'%';
-- GRANT SELECT ON ioedream.v_consume_refund_detail TO 'ioedream_app'@'%';

-- 为只读用户授权
-- GRANT SELECT ON ioedream.t_consume_refund TO 'ioedream_readonly'@'%';
-- GRANT SELECT ON ioedream.t_consume_refund_approval TO 'ioedream_readonly'@'%';
-- GRANT SELECT ON ioedream.t_consume_refund_process TO 'ioedream_readonly'@'%';
-- GRANT SELECT ON ioedream.t_consume_refund_config TO 'ioedream_readonly'@'%';
-- GRANT SELECT ON ioedream.t_consume_refund_statistics TO 'ioedream_readonly'@'%';
-- GRANT SELECT ON ioedream.v_consume_refund_detail TO 'ioedream_readonly'@'%';

-- =====================================================
-- 12. 执行完成确认
-- =====================================================

-- 恢复环境设置
SET FOREIGN_KEY_CHECKS = 1;

-- 输出执行完成信息
SELECT 'V2.0.2 退款管理表创建迁移脚本执行完成！' AS migration_status,
       '创建表 5个，新增字段 50+，新增配置数据 11条，新增视图 1个' AS migration_summary,
       NOW() AS completed_time;

-- 记录迁移历史
INSERT INTO t_migration_history (
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
) VALUES (
    'V2.0.2',
    '退款管理表创建 - 支持完整的退款申请、审批、处理流程',
    '06-v2.0.2__create-refund-table.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
);

COMMIT;

-- =====================================================
-- 13. 业务验证SQL
-- =====================================================

-- 验证退款表数据
SELECT COUNT(*) as refund_count,
       SUM(refund_amount) as total_refund_amount,
       AVG(refund_amount) as avg_refund_amount,
       COUNT(CASE WHEN refund_status = 5 THEN 1 END) as completed_count
FROM t_consume_refund
WHERE deleted_flag = 0;

-- 验证审批记录
SELECT COUNT(*) as approval_count,
       COUNT(CASE WHEN approval_result = 1 THEN 1 END) as approved_count,
       COUNT(CASE WHEN approval_result = 2 THEN 1 END) as rejected_count
FROM t_consume_refund_approval
WHERE deleted_flag = 0;

-- 验证处理记录
SELECT COUNT(*) as process_count,
       COUNT(CASE WHEN process_result = 1 THEN 1 END) as success_count,
       COUNT(CASE WHEN process_result = 2 THEN 1 END) as failed_count
FROM t_consume_refund_process
WHERE deleted_flag = 0;

-- =====================================================
-- 脚本结束
-- =====================================================
