-- =====================================================
-- 考勤规则配置表优化 - 消除冗余
-- 基于OpenSpec规范，优化数据库设计
-- 创建时间: 2025-11-25
-- 创建目的: 消除t_attendance_rule表中的冗余字段，简化数据结构
-- =====================================================

-- 备份现有规则数据（如果存在数据）
CREATE TABLE IF NOT EXISTS t_attendance_rule_backup AS
SELECT * FROM t_attendance_rule;

-- 重新创建优化后的考勤规则配置表
DROP TABLE IF EXISTS t_attendance_rule;

CREATE TABLE t_attendance_rule (
    -- 统一规则标识（移除rule_id和rule_code冗余）
    rule_id VARCHAR(50) PRIMARY KEY COMMENT '规则唯一标识',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_description TEXT COMMENT '规则描述',

    -- 简化规则分类（合并rule_type和rule_category）
    rule_type VARCHAR(30) NOT NULL COMMENT '规则类型（LATE/EARLY/ABSENTEEISM/OVERTIME/LEAVE/BREAK/WORK_TIME）',
    severity_level ENUM('LOW', 'NORMAL', 'HIGH', 'CRITICAL') DEFAULT 'NORMAL' COMMENT '严重级别',

    -- 统一规则配置（合并多个config字段）
    rule_config JSON NOT NULL COMMENT '规则配置（包含条件、动作、通知、处罚等）',

    -- 简化应用范围
    apply_scope ENUM('ALL', 'DEPARTMENT', 'AREA', 'EMPLOYEE', 'POSITION') NOT NULL DEFAULT 'ALL' COMMENT '应用范围',
    scope_config JSON COMMENT '范围配置（具体的部门、区域、员工ID列表）',

    -- 统一时间配置
    time_config JSON COMMENT '时间配置（包含生效时间、失效时间、工作日配置等）',

    -- 规则状态
    enabled_flag TINYINT DEFAULT 1 COMMENT '启用标记（0-禁用，1-启用）',
    priority INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    is_system_rule TINYINT DEFAULT 0 COMMENT '是否系统规则（0-自定义，1-系统）',
    execution_mode ENUM('AUTO', 'MANUAL') DEFAULT 'AUTO' COMMENT '执行模式',

    -- 基础审计字段（继承BaseEntity）
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0-正常，1-删除）',
    version INT DEFAULT 1 COMMENT '版本号',

    -- 优化后的索引
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled_flag (enabled_flag),
    INDEX idx_priority (priority),
    INDEX idx_apply_scope (apply_scope),
    INDEX idx_deleted_flag (deleted_flag),
    INDEX idx_rule_type_enabled (rule_type, enabled_flag)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优化后的考勤规则配置表';

-- 插入优化后的系统默认规则
INSERT INTO t_attendance_rule (
    rule_id, rule_name, rule_type, rule_config, enabled_flag, priority,
    is_system_rule, create_user_id
) VALUES
-- 迟到规则
('RULE_LATE_STANDARD', '标准迟到规则', 'LATE',
'{
    "condition": {"punch_in_time": ">work_start_time", "grace_period": 5},
    "action": {"type": "mark_late", "deduct_minutes": 0, "auto_approve": true},
    "notification": {"enabled": false},
    "penalty": {"enabled": false}
}',
1, 100, 1, 1),

-- 早退规则
('RULE_EARLY_STANDARD', '标准早退规则', 'EARLY',
'{
    "condition": {"punch_out_time": "<work_end_time", "min_early_minutes": 10},
    "action": {"type": "mark_early", "deduct_minutes": 0, "require_reason": false},
    "notification": {"enabled": false},
    "penalty": {"enabled": false}
}',
1, 100, 1, 1),

-- 缺勤规则
('RULE_ABSENTEEISM_NO_PUNCH', '无打卡缺勤规则', 'ABSENTEEISM',
'{
    "condition": {"no_punch_in": true, "no_punch_out": true, "date_type": "work_day"},
    "action": {"type": "mark_absenteeism", "require_approval": false},
    "notification": {"enabled": true, "recipients": ["manager", "hr"]},
    "penalty": {"enabled": true, "type": "warning"}
}',
1, 200, 1, 1),

-- 加班规则
('RULE_OVERTIME_AUTO', '自动加班识别规则', 'OVERTIME',
'{
    "condition": {"work_hours": ">standard_hours", "min_overtime_minutes": 30},
    "action": {"type": "auto_record_overtime", "pay_rate": 1.5, "require_approval": false},
    "notification": {"enabled": true, "recipients": ["employee"]},
    "penalty": {"enabled": false}
}',
1, 80, 1, 1),

-- 请假规则
('RULE_LEAVE_SICK', '病假处理规则', 'LEAVE',
'{
    "condition": {"leave_type": "SICK"},
    "action": {"type": "approve_with_proof", "max_days": 3, "pay_rate": 0.8},
    "notification": {"enabled": true, "recipients": ["manager"]},
    "penalty": {"enabled": false}
}',
1, 90, 1, 1),

-- 班次时间规则
('RULE_WORK_TIME_STANDARD', '标准工时规则', 'WORK_TIME',
'{
    "condition": {"work_hours": "<standard_hours", "min_short_minutes": 30},
    "action": {"type": "record_short_time", "require_reason": true},
    "notification": {"enabled": true, "recipients": ["manager"]},
    "penalty": {"enabled": true, "type": "deduction"}
}',
1, 60, 1, 1);

-- 插入带应用范围和时间配置的示例规则
INSERT INTO t_attendance_rule (
    rule_id, rule_name, rule_type, rule_config, apply_scope, scope_config, time_config,
    enabled_flag, priority, create_user_id
) VALUES
-- 技术部门特殊迟到规则
('RULE_LATE_DEPT_TECH', '技术部门迟到规则', 'LATE',
'{
    "condition": {"punch_in_time": ">work_start_time", "grace_period": 10},
    "action": {"type": "mark_late", "deduct_minutes": 0, "auto_approve": true},
    "notification": {"enabled": false},
    "penalty": {"enabled": false}
}',
'DEPARTMENT',
'{"department_ids": [1, 2, 3], "exclude_positions": ["MANAGER"]}',
'{"effective_date": "2025-01-01", "work_days": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]}',
1, 150, 1),

-- 办公区域特殊早退规则
('RULE_EARLY_AREA_OFFICE', '办公区域早退规则', 'EARLY',
'{
    "condition": {"punch_out_time": "<work_end_time", "min_early_minutes": 5},
    "action": {"type": "mark_early", "deduct_minutes": 0, "require_reason": false},
    "notification": {"enabled": true, "recipients": ["supervisor"]},
    "penalty": {"enabled": false}
}',
'AREA',
'{"area_ids": [101, 102, 103], "apply_all_employees": true}',
'{"effective_date": "2025-01-01", "exclude_holidays": true}',
1, 120, 1);

-- 更新统计视图以适配新结构
DROP VIEW IF EXISTS v_attendance_rule_summary;
CREATE VIEW v_attendance_rule_summary AS
SELECT
    rule_id,
    rule_name,
    rule_type,
    severity_level,
    apply_scope,
    enabled_flag,
    priority,
    is_system_rule,
    execution_mode,
    JSON_EXTRACT(time_config, '$.effective_date') as effective_date,
    JSON_EXTRACT(time_config, '$.expire_date') as expire_date,
    create_time,
    update_time
FROM t_attendance_rule
WHERE deleted_flag = 0
ORDER BY priority DESC, rule_type, rule_id;

-- 创建规则配置示例视图（便于查询JSON字段）
CREATE VIEW v_attendance_rule_config_examples AS
SELECT
    rule_id,
    rule_name,
    rule_type,
    JSON_EXTRACT(rule_config, '$.condition') as condition_config,
    JSON_EXTRACT(rule_config, '$.action') as action_config,
    JSON_EXTRACT(rule_config, '$.notification') as notification_config,
    JSON_EXTRACT(rule_config, '$.penalty') as penalty_config
FROM t_attendance_rule
WHERE deleted_flag = 0 AND enabled_flag = 1;

-- 添加注释说明优化内容
ALTER TABLE t_attendance_rule COMMENT = '优化后的考勤规则配置表 - 消除冗余，统一使用JSON配置格式';