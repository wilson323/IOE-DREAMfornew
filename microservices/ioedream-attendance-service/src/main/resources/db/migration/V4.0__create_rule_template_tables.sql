-- ================================================
-- IOE-DREAM 考勤规则模板表创建脚本
-- ================================================
-- 功能: 创建考勤规则模板表，支持规则模板管理
-- 版本: V4.0__create_rule_template_tables
-- 日期: 2025-12-26
-- ================================================

-- ================================================
-- 1. 考勤规则模板表
-- ================================================
CREATE TABLE IF NOT EXISTS t_attendance_rule_template (
    template_id BIGINT PRIMARY KEY COMMENT '模板ID（主键）',
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码（唯一标识）',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_category VARCHAR(50) NOT NULL COMMENT '模板分类：TIME-时间规则 ABSENCE-缺勤规则 OVERTIME-加班规则 LEAVE-请假规则',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型：SYSTEM-系统内置 CUSTOM-用户自定义',
    template_condition TEXT NOT NULL COMMENT '模板条件（JSON格式）',
    template_action TEXT NOT NULL COMMENT '模板动作（JSON格式）',
    description VARCHAR(500) COMMENT '模板描述',
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统模板：0-否 1-是',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    sort INT DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
    use_count INT DEFAULT 0 COMMENT '使用次数（统计字段）',
    department_ids TEXT COMMENT '适用部门ID列表（JSON数组，null表示全部）',
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    version VARCHAR(50) COMMENT '版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_template_code (template_code) COMMENT '模板编码唯一索引',
    KEY idx_template_type (template_type) COMMENT '模板类型索引',
    KEY idx_template_category (template_category) COMMENT '模板分类索引',
    KEY idx_status (status) COMMENT '状态索引',
    KEY idx_use_count (use_count) COMMENT '使用次数索引',
    KEY idx_create_time (create_time) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤规则模板表';

-- ================================================
-- 2. 插入系统预置模板
-- ================================================

-- 模板1: 迟到扣款规则
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    1, 'LATE_DEDUCT_50', '迟到扣款50元', 'TIME', 'SYSTEM',
    '{"lateMinutes": 5}',
    '{"deductAmount": 50}',
    '迟到超过5分钟扣款50元', 1, 1, 1, 0, '迟到,扣款,常用', '1.0.0'
);

-- 模板2: 早退警告规则
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    2, 'EARLY_WARNING', '早退警告', 'TIME', 'SYSTEM',
    '{"earlyLeaveMinutes": 10}',
    '{"notifyManager": true}',
    '早退超过10分钟通知管理员', 1, 1, 2, 0, '早退,警告', '1.0.0'
);

-- 模板3: 加班补休规则
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    3, 'OVERTIME_COMPENSATORY', '加班补休', 'OVERTIME', 'SYSTEM',
    '{"overtimeHours": 2}',
    '{"addCompensatoryLeave": 2}',
    '加班超过2小时给予2小时调休', 1, 1, 3, 0, '加班,补休', '1.0.0'
);

-- 模板4: 缺勤通知规则
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    4, 'ABSENT_NOTIFY', '缺勤通知', 'ABSENCE', 'SYSTEM',
    '{"absentHours": 4}',
    '{"notifyUser": true, "notifyManager": true}',
    '缺勤超过4小时通知用户和管理员', 1, 1, 4, 0, '缺勤,通知', '1.0.0'
);

-- 模板5: 请假扣款规则
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    5, 'LEAVE_DEDUCT', '请假扣款', 'LEAVE', 'SYSTEM',
    '{"leaveDays": 1}',
    '{"deductAmount": 100}',
    '事假1天扣款100元', 1, 1, 5, 0, '请假,扣款', '1.0.0'
);

-- 模板6: 严重迟到重罚
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    6, 'SEVERE_LATE_PENALTY', '严重迟到重罚', 'TIME', 'SYSTEM',
    '{"lateMinutes": 30}',
    '{"deductAmount": 200, "markLate": true}',
    '迟到超过30分钟扣款200元并标记迟到', 1, 1, 6, 0, '迟到,重罚', '1.0.0'
);

-- 模板7: 频繁迟到警告
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    7, 'FREQUENT_LATE_WARNING', '频繁迟到警告', 'TIME', 'SYSTEM',
    '{"lateCount": 3}',
    '{"notifyManager": true, "sendEmail": true}',
    '月迟到3次通知管理员并发送邮件', 1, 1, 7, 0, '迟到,警告,频繁', '1.0.0'
);

-- 模板8: 加班费计算
INSERT INTO t_attendance_rule_template (
    template_id, template_code, template_name, template_category, template_type,
    template_condition, template_action, description, is_system, status,
    sort, use_count, tags, version
) VALUES (
    8, 'OVERTIME_PAYMENT', '加班费计算', 'OVERTIME', 'SYSTEM',
    '{"overtimeHours": 1, "isWorkday": true}',
    '{"overtimePayment": 1.5}',
    '工作日加班1小时支付1.5倍加班费', 1, 1, 8, 0, '加班,费用', '1.0.0'
);

-- ================================================
-- 3. 索引优化
-- ================================================
-- 复合索引用于常用查询
CREATE INDEX IF NOT EXISTS idx_template_type_status ON t_attendance_rule_template(template_type, status);
CREATE INDEX IF NOT EXISTS idx_template_category_status ON t_attendance_rule_template(template_category, status);
CREATE INDEX IF NOT EXISTS idx_template_sort_usecount ON t_attendance_rule_template(sort DESC, use_count DESC);

-- ================================================
-- 4. 数据验证
-- ================================================
-- 验证系统模板是否插入成功
SELECT
    template_id,
    template_code,
    template_name,
    template_category,
    is_system
FROM t_attendance_rule_template
WHERE is_system = 1
ORDER BY sort;

-- 显示结果应该包含8条系统预置模板
