-- ================================================
-- IOE-DREAM 考勤汇总表创建脚本
-- ================================================
-- 功能: 创建考勤汇总和部门统计表，支持报表持久化
-- 版本: V1.0.2
-- 日期: 2025-12-23
-- ================================================

-- ================================================
-- 1. 考勤汇总表
-- ================================================
CREATE TABLE IF NOT EXISTS t_attendance_summary (
    summary_id BIGINT PRIMARY KEY COMMENT '汇总ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(100) COMMENT '员工姓名',
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(200) COMMENT '部门名称',
    summary_month VARCHAR(10) NOT NULL COMMENT '汇总月份（格式：2024-01）',
    work_days INT DEFAULT 0 COMMENT '应工作天数',
    actual_days INT DEFAULT 0 COMMENT '实际出勤天数',
    absent_days INT DEFAULT 0 COMMENT '旷工天数',
    late_count INT DEFAULT 0 COMMENT '迟到次数',
    early_count INT DEFAULT 0 COMMENT '早退次数',
    overtime_hours DECIMAL(10,2) DEFAULT 0.00 COMMENT '加班时长(小时)',
    weekend_overtime_hours DECIMAL(10,2) DEFAULT 0.00 COMMENT '周末加班时长(小时)',
    leave_days DECIMAL(10,2) DEFAULT 0.00 COMMENT '请假天数',
    attendance_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '出勤率（0-1之间）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_employee_month (employee_id, summary_month) COMMENT '员工+月份唯一索引',
    KEY idx_department_month (department_id, summary_month) COMMENT '部门+月份索引',
    KEY idx_month (summary_month) COMMENT '月份索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤汇总表';

-- ================================================
-- 2. 部门统计表
-- ================================================
CREATE TABLE IF NOT EXISTS t_department_statistics (
    statistics_id BIGINT PRIMARY KEY COMMENT '统计ID',
    department_id BIGINT NOT NULL COMMENT '部门ID',
    department_name VARCHAR(200) COMMENT '部门名称',
    statistics_month VARCHAR(10) NOT NULL COMMENT '统计月份（格式：2024-01）',
    total_employees INT DEFAULT 0 COMMENT '部门总人数',
    present_employees INT DEFAULT 0 COMMENT '出勤人数',
    absent_employees INT DEFAULT 0 COMMENT '缺勤人数',
    late_employees INT DEFAULT 0 COMMENT '迟到人数',
    attendance_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '部门出勤率',
    avg_work_hours DECIMAL(10,2) DEFAULT 0.00 COMMENT '平均工作时长(小时)',
    total_overtime_hours DECIMAL(10,2) DEFAULT 0.00 COMMENT '总加班时长(小时)',
    exception_count INT DEFAULT 0 COMMENT '异常次数',
    statistics_details JSON COMMENT '统计详情JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：0-删除，1-正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_department_month (department_id, statistics_month) COMMENT '部门+月份唯一索引',
    KEY idx_month (statistics_month) COMMENT '月份索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门统计表';

-- ================================================
-- 3. 索引优化
-- ================================================
-- 考勤汇总表索引
CREATE INDEX IF NOT EXISTS idx_summary_employee ON t_attendance_summary(employee_id);
CREATE INDEX IF NOT EXISTS idx_summary_department ON t_attendance_summary(department_id);

-- 部门统计表索引
CREATE INDEX IF NOT EXISTS idx_statistics_department ON t_department_statistics(department_id);
