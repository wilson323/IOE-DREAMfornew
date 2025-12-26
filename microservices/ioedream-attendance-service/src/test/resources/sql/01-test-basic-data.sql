-- ============================================================
-- IOE-DREAM Attendance Service 基础测试数据
--
-- 功能：提供集成测试所需的基础数据（用户、部门、班次等）
-- 使用：@Sql(scripts = "/sql/01-test-basic-data.sql")
-- ============================================================

-- 清理旧测试数据
DELETE FROM t_attendance_anomaly WHERE user_id IN (1, 2, 3);
DELETE FROM t_attendance_record WHERE user_id IN (1, 2, 3);
DELETE FROM t_work_shift WHERE shift_id IN (1, 2, 3);
DELETE FROM t_attendance_rule_config WHERE config_id IN (1, 2);

-- ============================================================
-- 1. 班次测试数据
-- ============================================================

INSERT INTO t_work_shift (
    shift_id, shift_name, shift_type,
    work_start_time, work_end_time,
    late_tolerance_minutes, early_tolerance_minutes,
    status, deleted_flag, create_time, update_time
) VALUES
(
    1, -- 正常班（8:00-17:00）
    '正常班',
    1, -- 固定班次
    '08:00:00',
    '17:00:00',
    5,  -- 5分钟迟到容忍
    5,  -- 5分钟早退容忍
    1,  -- 启用
    0,  -- 未删除
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
),
(
    2, -- 早班（7:00-16:00）
    '早班',
    1,
    '07:00:00',
    '16:00:00',
    5,
    5,
    1,
    0,
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
),
(
    3, -- 晚班（16:00-00:00）
    '晚班',
    1,
    '16:00:00',
    '00:00:00',
    5,
    5,
    1,
    0,
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
);

-- ============================================================
-- 2. 考勤规则配置测试数据
-- ============================================================

INSERT INTO t_attendance_rule_config (
    config_id, rule_name, rule_type,
    late_check_enabled, late_minutes, early_check_enabled, early_minutes,
    absent_threshold_hours, status, deleted_flag, create_time, update_time
) VALUES
(
    1, -- 默认考勤规则
    '默认考勤规则',
    1, -- 全局规则
    1, -- 启用迟到检查
    5,  -- 5分钟视为迟到
    1, -- 启用早退检查
    5,  -- 5分钟视为早退
    4,  -- 4小时无打卡记录视为旷工
    1,  -- 启用
    0,  -- 未删除
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
),
(
    2, -- 严格考勤规则
    '严格考勤规则',
    2, -- 部门规则
    1,
    0,  -- 0分钟就迟到（严格）
    1,
    0,  -- 0分钟就早退（严格）
    2,  -- 2小时无打卡记录视为旷工
    1,
    0,
    '2025-01-01 00:00:00',
    '2025-01-01 00:00:00'
);

-- ============================================================
-- 3. 考勤记录测试数据
-- ============================================================

INSERT INTO t_attendance_record (
    record_id, user_id, user_name, department_id, department_name,
    shift_id, shift_name, attendance_date, attendance_time,
    attendance_status, punch_type, device_id, device_name,
    location_id, location_name, status, deleted_flag, create_time, update_time
) VALUES
-- 用户1：正常打卡记录
(
    1,  -- 正常上班打卡
    1, '张三',
    101, '研发部',
    1, '正常班',
    '2025-01-27',
    '08:00:00',  -- 准时打卡
    'NORMAL', 'IN',
    'DEV001', '研发部门禁设备',
    1001, '研发部1楼大厅',
    1,  -- 正常
    0,
    '2025-01-27 08:00:00',
    '2025-01-27 08:00:00'
),
(
    2,  -- 正常下班打卡
    1, '张三',
    101, '研发部',
    1, '正常班',
    '2025-01-27',
    '17:00:00',  -- 准时打卡
    'NORMAL', 'OUT',
    'DEV001', '研发部门禁设备',
    1001, '研发部1楼大厅',
    1,
    0,
    '2025-01-27 17:00:00',
    '2025-01-27 17:00:00'
),
-- 用户2：迟到记录
(
    3,
    2, '李四',
    102, '产品部',
    1, '正常班',
    '2025-01-27',
    '08:06:00',  -- 迟到6分钟
    'LATE', 'IN',
    'PROD001', '产品部门禁设备',
    1002, '产品部1楼大厅',
    1,
    0,
    '2025-01-27 08:06:00',
    '2025-01-27 08:06:00'
),
(
    4,  -- 下班正常
    2, '李四',
    102, '产品部',
    1, '正常班',
    '2025-01-27',
    '17:00:00',
    'NORMAL', 'OUT',
    'PROD001', '产品部门禁设备',
    1002, '产品部1楼大厅',
    1,
    0,
    '2025-01-27 17:00:00',
    '2025-01-27 17:00:00'
),
-- 用户3：早退记录
(
    5,  -- 上班正常
    3, '王五',
    103, '测试部',
    1, '正常班',
    '2025-01-27',
    '08:00:00',
    'NORMAL', 'IN',
    'TEST001', '测试部门禁设备',
    1003, '测试部1楼大厅',
    1,
    0,
    '2025-01-27 08:00:00',
    '2025-01-27 08:00:00'
),
(
    6,  -- 早退
    3, '王五',
    103, '测试部',
    1, '正常班',
    '2025-01-27',
    '16:53:00',  -- 早退7分钟
    'EARLY', 'OUT',
    'TEST001', '测试部门禁设备',
    1003, '测试部1楼大厅',
    1,
    0,
    '2025-01-27 16:53:00',
    '2025-01-27 16:53:00'
);

-- ============================================================
-- 4. 考勤异常测试数据
-- ============================================================

INSERT INTO t_attendance_anomaly (
    anomaly_id, user_id, user_name, department_id, department_name,
    shift_id, shift_name, attendance_date, anomaly_type, anomaly_status,
    punch_type, severity_level, description,
    status, deleted_flag, create_time, update_time
) VALUES
-- 用户4：全天缺卡
(
    1,
    4, '赵六',
    104, '运维部',
    1, '正常班',
    '2025-01-27',
    'MISSING_CARD',  -- 缺卡异常
    'PENDING',       -- 待处理
    'BOTH',          -- 上班和下班都缺卡
    2,               -- 中等严重
    '全天无打卡记录',
    1,
    0,
    '2025-01-27 18:00:00',
    '2025-01-27 18:00:00'
),
-- 用户5：旷工（无任何记录）
(
    2,
    5, '钱七',
    105, '市场部',
    1, '正常班',
    '2025-01-27',
    'ABSENT',        -- 旷工
    'PENDING',
    NULL,
    3,               -- 高等严重
    '全天无打卡记录且未请假',
    1,
    0,
    '2025-01-27 18:00:00',
    '2025-01-27 18:00:00'
);

-- ============================================================
-- 5. 员工扩展信息（用于复杂测试场景）
-- ============================================================

-- 注意：以下表可能不存在于所有模块中，根据实际情况调整
-- 如果需要，可以取消注释并调整

-- INSERT INTO t_attendance_overtime_apply (
--     apply_id, user_id, user_name, department_id, department_name,
--     overtime_date, overtime_type, overtime_hours, overtime_reason,
--     apply_status, status, deleted_flag, create_time, update_time
-- ) VALUES
-- (
--     1,
--     1, '张三',
--     101, '研发部',
--     '2025-01-27',
--     'WORKDAY',  -- 工作日加班
--     2.5,         -- 加班2.5小时
--     '项目紧急上线',
--     'PENDING',
--     1,
--     0,
--     '2025-01-27 18:00:00',
--     '2025-01-27 18:00:00'
-- );

-- ============================================================
-- 数据插入完成提示
-- ============================================================

-- 测试数据统计
SELECT '基础测试数据插入完成' AS message;
SELECT COUNT(*) AS work_shift_count FROM t_work_shift WHERE shift_id IN (1, 2, 3);
SELECT COUNT(*) AS rule_config_count FROM t_attendance_rule_config WHERE config_id IN (1, 2);
SELECT COUNT(*) AS attendance_record_count FROM t_attendance_record WHERE user_id IN (1, 2, 3);
SELECT COUNT(*) AS attendance_anomaly_count FROM t_attendance_anomaly WHERE user_id IN (4, 5);
