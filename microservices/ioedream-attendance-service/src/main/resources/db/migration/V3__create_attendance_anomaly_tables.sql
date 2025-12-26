-- =====================================================
-- IOE-DREAM 考勤异常管理模块建表脚本
-- 版本: V3
-- 创建日期: 2025-01-30
-- 说明: 创建考勤异常记录、异常申请、规则配置三张核心表
-- =====================================================

-- =====================================================
-- 1. 考勤异常记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_attendance_anomaly (
    anomaly_id BIGINT PRIMARY KEY COMMENT '异常记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(100) COMMENT '部门名称',
    shift_id BIGINT COMMENT '班次ID',
    shift_name VARCHAR(50) COMMENT '班次名称',
    attendance_date DATE NOT NULL COMMENT '考勤日期',

    -- 异常信息
    anomaly_type VARCHAR(20) NOT NULL COMMENT '异常类型: MISSING_CARD-缺卡, LATE-迟到, EARLY-早退, ABSENT-旷工',
    severity_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '异常程度: NORMAL-一般, SERIOUS-严重, CRITICAL-重大',
    expected_punch_time DATETIME COMMENT '应打卡时间',
    actual_punch_time DATETIME COMMENT '实际打卡时间',
    punch_type VARCHAR(20) COMMENT '打卡类型: CHECK_IN-上班, CHECK_OUT-下班',
    anomaly_duration INT COMMENT '异常时长（分钟）',
    anomaly_reason VARCHAR(200) COMMENT '异常原因',

    -- 处理信息
    anomaly_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '异常状态: PENDING-待处理, APPLIED-已申请, APPROVED-已批准, REJECTED-已驳回, IGNORED-已忽略',
    apply_id BIGINT COMMENT '申请ID',
    handler_id BIGINT COMMENT '处理人ID',
    handler_name VARCHAR(50) COMMENT '处理人姓名',
    handle_time DATETIME COMMENT '处理时间',
    handle_comment VARCHAR(500) COMMENT '处理意见',

    -- 修正信息
    attendance_record_id BIGINT COMMENT '关联的考勤记录ID',
    is_corrected TINYINT NOT NULL DEFAULT 0 COMMENT '是否已修正: 0-未修正, 1-已修正',
    corrected_time DATETIME COMMENT '修正时间',
    corrected_punch_time DATETIME COMMENT '修正后打卡时间',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',

    INDEX idx_user_id (user_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_anomaly_type (anomaly_type),
    INDEX idx_anomaly_status (anomaly_status),
    INDEX idx_department_id (department_id),
    INDEX idx_apply_id (apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤异常记录表';

-- =====================================================
-- 2. 考勤异常申请表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_attendance_anomaly_apply (
    apply_id BIGINT PRIMARY KEY COMMENT '申请ID',
    apply_no VARCHAR(50) NOT NULL UNIQUE COMMENT '申请单号',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(100) COMMENT '部门名称',

    -- 申请信息
    apply_type VARCHAR(20) NOT NULL COMMENT '申请类型: SUPPLEMENT_CARD-补卡, LATE_EXPLANATION-迟到说明, EARLY_EXPLANATION-早退说明, ABSENT_APPEAL-旷工申诉',
    anomaly_id BIGINT COMMENT '异常记录ID',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    shift_id BIGINT COMMENT '班次ID',
    shift_name VARCHAR(50) COMMENT '班次名称',
    punch_type VARCHAR(20) COMMENT '打卡类型: CHECK_IN-上班, CHECK_OUT-下班',
    original_punch_time DATETIME COMMENT '原打卡时间',
    applied_punch_time DATETIME COMMENT '申请打卡时间',

    -- 申请内容
    apply_reason VARCHAR(200) NOT NULL COMMENT '申请原因',
    description TEXT COMMENT '详细说明',
    attachment_path VARCHAR(500) COMMENT '附件路径',

    -- 审批信息
    apply_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态: PENDING-待审批, APPROVED-已批准, REJECTED-已驳回, CANCELLED-已撤销',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approve_time DATETIME COMMENT '审批时间',
    approve_comment VARCHAR(500) COMMENT '审批意见',
    apply_time DATETIME NOT NULL COMMENT '申请时间',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',

    INDEX idx_apply_no (apply_no),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_attendance_date (attendance_date),
    INDEX idx_apply_status (apply_status),
    INDEX idx_apply_type (apply_type),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤异常申请表';

-- =====================================================
-- 3. 考勤规则配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_attendance_rule_config (
    config_id BIGINT PRIMARY KEY COMMENT '规则配置ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    apply_scope VARCHAR(20) NOT NULL DEFAULT 'ALL' COMMENT '适用范围: ALL-全局, DEPARTMENT-部门, SHIFT-班次, EMPLOYEE-员工',
    scope_id BIGINT COMMENT '适用范围ID',
    rule_status TINYINT NOT NULL DEFAULT 1 COMMENT '规则状态: 1-启用, 0-禁用',

    -- 迟到规则
    late_check_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '迟到判定启用: 1-启用, 0-禁用',
    late_minutes INT NOT NULL DEFAULT 5 COMMENT '迟到判定分钟数',
    serious_late_minutes INT NOT NULL DEFAULT 30 COMMENT '严重迟到分钟数',
    late_handle_type VARCHAR(20) NOT NULL DEFAULT 'WARNING' COMMENT '迟到处理方式: IGNORE-忽略, WARNING-警告, DEDUCT-扣款',
    late_deduct_amount DECIMAL(10,2) COMMENT '迟到扣款金额（元/小时）',

    -- 早退规则
    early_check_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '早退判定启用: 1-启用, 0-禁用',
    early_minutes INT NOT NULL DEFAULT 5 COMMENT '早退判定分钟数',
    serious_early_minutes INT NOT NULL DEFAULT 30 COMMENT '严重早退分钟数',
    early_handle_type VARCHAR(20) NOT NULL DEFAULT 'WARNING' COMMENT '早退处理方式: IGNORE-忽略, WARNING-警告, DEDUCT-扣款',
    early_deduct_amount DECIMAL(10,2) COMMENT '早退扣款金额（元/小时）',

    -- 旷工规则
    absent_check_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '旷工判定启用: 1-启用, 0-禁用',
    absent_rule_type VARCHAR(20) NOT NULL DEFAULT 'LATE_THRESHOLD' COMMENT '旷工判定规则: MISSING_CARD-缺卡, LATE_THRESHOLD-迟到, NO_PUNCH-无打卡',
    absent_minutes INT NOT NULL DEFAULT 60 COMMENT '缺卡转旷工分钟数',
    late_to_absent_minutes INT NOT NULL DEFAULT 120 COMMENT '迟到转旷工分钟数',
    absent_handle_type VARCHAR(20) NOT NULL DEFAULT 'DEDUCT' COMMENT '旷工处理方式: WARNING-警告, DEDUCT-扣款',
    absent_deduct_rate DECIMAL(5,2) NOT NULL DEFAULT 3.0 COMMENT '旷工扣款倍数（倍日工资）',

    -- 弹性时间配置
    flexible_start_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '弹性上班时间启用: 1-启用, 0-禁用',
    flexible_start_minutes INT NOT NULL DEFAULT 15 COMMENT '弹性上班时间（分钟）',
    flexible_end_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '弹性下班时间启用: 1-启用, 0-禁用',
    flexible_end_minutes INT NOT NULL DEFAULT 15 COMMENT '弹性下班时间（分钟）',

    -- 缺卡规则
    missing_card_check_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '缺卡判定启用: 1-启用, 0-禁用',
    allowed_supplement_times INT NOT NULL DEFAULT 3 COMMENT '允许补卡次数（每月）',
    supplement_days_limit INT NOT NULL DEFAULT 3 COMMENT '补卡时间限制（天）',

    -- 描述和备注
    description VARCHAR(500) COMMENT '规则描述',
    remark VARCHAR(500) COMMENT '备注',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',

    INDEX idx_rule_name (rule_name),
    INDEX idx_apply_scope (apply_scope),
    INDEX idx_scope_id (scope_id),
    INDEX idx_rule_status (rule_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤规则配置表';

-- =====================================================
-- 4. 初始化默认规则配置
-- =====================================================
INSERT INTO t_attendance_rule_config (
    config_id,
    rule_name,
    apply_scope,
    rule_status,
    late_check_enabled,
    late_minutes,
    serious_late_minutes,
    late_handle_type,
    early_check_enabled,
    early_minutes,
    serious_early_minutes,
    early_handle_type,
    absent_check_enabled,
    absent_rule_type,
    absent_minutes,
    late_to_absent_minutes,
    absent_handle_type,
    absent_deduct_rate,
    flexible_start_enabled,
    flexible_start_minutes,
    flexible_end_enabled,
    flexible_end_minutes,
    missing_card_check_enabled,
    allowed_supplement_times,
    supplement_days_limit,
    description,
    create_time,
    create_user_id
) VALUES (
    1,
    '默认考勤规则',
    'ALL',
    1,
    1,  -- 迟到判定启用
    5,  -- 迟到5分钟算迟到
    30, -- 迟到30分钟算严重迟到
    'WARNING',
    1,  -- 早退判定启用
    5,  -- 早退5分钟算早退
    30, -- 早退30分钟算严重早退
    'WARNING',
    1,  -- 旷工判定启用
    'LATE_THRESHOLD',
    60, -- 缺卡60分钟转旷工
    120, -- 迟到120分钟转旷工
    'DEDUCT',
    3.0, -- 旷工扣3倍工资
    0,  -- 不启用弹性上班时间
    0,
    0,  -- 不启用弹性下班时间
    0,
    1,  -- 缺卡判定启用
    3,  -- 每月允许补卡3次
    3,  -- 考勤异常3天后不允许补卡
    '系统默认考勤规则，适用于所有员工',
    NOW(),
    NULL
);

-- =====================================================
-- 5. 创建视图：异常统计视图
-- =====================================================
CREATE OR REPLACE VIEW v_attendance_anomaly_statistics AS
SELECT
    department_id,
    department_name,
    attendance_date,
    anomaly_type,
    COUNT(*) AS anomaly_count,
    SUM(CASE WHEN anomaly_status = 'PENDING' THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN anomaly_status = 'APPLIED' THEN 1 ELSE 0 END) AS applied_count,
    SUM(CASE WHEN anomaly_status = 'APPROVED' THEN 1 ELSE 0 END) AS approved_count,
    SUM(CASE WHEN anomaly_status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected_count,
    SUM(CASE WHEN severity_level = 'SERIOUS' THEN 1 ELSE 0 END) AS serious_count,
    SUM(CASE WHEN severity_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_count
FROM t_attendance_anomaly
WHERE deleted_flag = 0
GROUP BY department_id, department_name, attendance_date, anomaly_type;

-- =====================================================
-- 建表脚本完成
-- =====================================================
