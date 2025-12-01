-- =====================================================
-- 考勤模块扩展表 - 考勤规则配置表
-- 基于消费模块模式的考勤系统功能完善
-- 创建时间: 2025-11-25
-- 创建目的: 提供考勤业务规则引擎的数据基础，支持灵活的考勤规则定义
-- =====================================================

-- 创建考勤规则配置表
CREATE TABLE t_attendance_rule (
    -- 规则标识
    rule_id VARCHAR(50) PRIMARY KEY COMMENT '规则ID（业务标识）',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码（系统内部使用）',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_description TEXT COMMENT '规则描述',

    -- 规则类型和分类
    rule_type VARCHAR(30) NOT NULL COMMENT '规则类型（LATE/EARLY/ABSENTEEISM/OVERTIME/LEAVE/BREAK）',
    rule_category VARCHAR(20) COMMENT '规则分类（WORK_TIME/ABSENCE/OVERTIME/EXCEPTION）',
    severity_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT '严重级别（LOW/NORMAL/HIGH/CRITICAL）',

    -- 规则配置（JSON格式存储）
    condition_config TEXT COMMENT '触发条件（JSON格式）',
    action_config TEXT COMMENT '处理动作（JSON格式）',
    notification_config TEXT COMMENT '通知配置（JSON格式）',
    penalty_config TEXT COMMENT '处罚配置（JSON格式）',

    -- 应用范围配置
    apply_scope VARCHAR(100) COMMENT '应用范围（ALL/DEPARTMENT/AREA/EMPLOYEE/POSITION）',
    scope_config TEXT COMMENT '范围配置（JSON格式，包含具体的部门、区域、员工ID列表）',

    -- 时间配置
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '失效日期',
    apply_time_config TEXT COMMENT '时间应用配置（JSON格式，工作日、节假日、特殊日期）',

    -- 规则状态和优先级
    enabled_flag TINYINT DEFAULT 1 COMMENT '启用标记（0-禁用，1-启用）',
    priority INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    is_system_rule TINYINT DEFAULT 0 COMMENT '是否系统规则（0-自定义，1-系统）',
    is_mandatory TINYINT DEFAULT 0 COMMENT '是否强制执行（0-可选，1-强制）',

    -- 执行配置
    execution_mode VARCHAR(20) DEFAULT 'AUTO' COMMENT '执行模式（AUTO-自动/MANUAL-手动）',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    retry_interval INT DEFAULT 0 COMMENT '重试间隔（分钟）',

    -- 基础审计字段（继承BaseEntity）
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0-正常，1-删除）',
    version INT DEFAULT 1 COMMENT '版本号',

    -- 索引优化
    INDEX idx_rule_code (rule_code),
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled_flag (enabled_flag),
    INDEX idx_priority (priority),
    INDEX idx_effective_date (effective_date, expire_date),
    INDEX idx_apply_scope (apply_scope),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_rule_type_enabled (rule_type, enabled_flag)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤规则配置表';

-- 插入系统默认规则
INSERT INTO t_attendance_rule (
    rule_id, rule_code, rule_name, rule_type, rule_category,
    condition_config, action_config, enabled_flag, priority,
    is_system_rule, is_mandatory, create_user_id
) VALUES
-- 迟到规则
('RULE_LATE_001', 'LATE_STANDARD', '标准迟到规则', 'LATE', 'WORK_TIME',
 '{"condition": "punch_in_time > work_start_time", "grace_period": 5}',
 '{"action": "mark_late", "deduct_minutes": 0, "auto_approve": true}',
 1, 100, 1, 1, 1),

-- 早退规则
('RULE_EARLY_001', 'EARLY_STANDARD', '标准早退规则', 'EARLY', 'WORK_TIME',
 '{"condition": "punch_out_time < work_end_time", "min_early_minutes": 10}',
 '{"action": "mark_early", "deduct_minutes": 0, "require_reason": false}',
 1, 100, 1, 1, 1),

-- 缺勤规则
('RULE_ABSENTEEISM_001', 'ABSENTEEISM_NO_PUNCH', '无打卡缺勤规则', 'ABSENTEEISM', 'ABSENCE',
 '{"condition": "no_punch_in AND no_punch_out", "date_type": "work_day"}',
 '{"action": "mark_absenteeism", "require_approval": false, "notify_manager": true}',
 1, 200, 1, 1, 1),

-- 加班规则
('RULE_OVERTIME_001', 'OVERTIME_AUTO', '自动加班识别规则', 'OVERTIME', 'OVERTIME',
 '{"condition": "work_hours > standard_hours", "min_overtime_minutes": 30}',
 '{"action": "auto_record_overtime", "pay_rate": 1.5, "require_approval": false}',
 1, 80, 1, 0, 1),

-- 请假规则
('RULE_LEAVE_001', 'LEAVE_SICK', '病假处理规则', 'LEAVE', 'EXCEPTION',
 '{"condition": "leave_type = SICK", "require_medical_proof": true}',
 '{"action": "approve_with_proof", "max_days": 3, "pay_rate": 0.8}',
 1, 90, 1, 0, 1);

-- 创建规则应用范围配置示例
INSERT INTO t_attendance_rule (
    rule_id, rule_code, rule_name, rule_type, rule_category,
    condition_config, action_config, apply_scope, scope_config,
    enabled_flag, priority, is_system_rule, create_user_id
) VALUES
('RULE_LATE_DEPT_001', 'LATE_DEPT_TECH', '技术部门迟到规则', 'LATE', 'WORK_TIME',
 '{"condition": "punch_in_time > work_start_time", "grace_period": 10}',
 '{"action": "mark_late", "deduct_minutes": 0, "auto_approve": true}',
 'DEPARTMENT',
 '{"department_ids": [1, 2, 3], "exclude_positions": ["MANAGER"]}',
 1, 150, 0, 1),

('RULE_LATE_AREA_001', 'LATE_AREA_OFFICE', '办公区域迟到规则', 'LATE', 'WORK_TIME',
 '{"condition": "punch_in_time > work_start_time", "grace_period": 3}',
 '{"action": "mark_late", "deduct_minutes": 30, "auto_approve": false}',
 'AREA',
 '{"area_ids": [101, 102, 103], "apply_all_employees": true}',
 1, 120, 0, 1);

-- 创建规则配置示例视图
CREATE VIEW v_attendance_rule_summary AS
SELECT
    rule_id,
    rule_code,
    rule_name,
    rule_type,
    rule_category,
    severity_level,
    apply_scope,
    enabled_flag,
    priority,
    is_system_rule,
    is_mandatory,
    effective_date,
    expire_date,
    create_time,
    update_time
FROM t_attendance_rule
WHERE deleted_flag = 0
ORDER BY priority DESC, rule_type, rule_id;