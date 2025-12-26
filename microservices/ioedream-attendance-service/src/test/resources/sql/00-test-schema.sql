-- ============================================================
-- IOE-DREAM 考勤服务 H2 测试数据库Schema脚本
-- ============================================================
-- 用途: 创建集成测试所需的数据库表结构（H2兼容）
-- 数据库: H2 (MySQL兼容模式)
-- 创建日期: 2025-12-25
-- ============================================================

-- ============================================================
-- 1. 班次表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_work_shift (
    shift_id BIGINT PRIMARY KEY,
    shift_name VARCHAR(100) NOT NULL,
    shift_code VARCHAR(50),
    shift_type TINYINT DEFAULT 1 COMMENT '1-固定 2-弹性 3-轮班',

    -- 工作时间
    work_start_time TIME NOT NULL,
    work_end_time TIME NOT NULL,
    work_duration INT DEFAULT 0 COMMENT '工作时长（分钟）',

    -- 午休时间
    lunch_start_time TIME,
    lunch_end_time TIME,
    lunch_duration INT DEFAULT 0 COMMENT '午休时长（分钟）',

    -- 容忍时间
    late_tolerance_minutes INT DEFAULT 5 COMMENT '迟到容忍（分钟）',
    early_tolerance_minutes INT DEFAULT 5 COMMENT '早退容忍（分钟）',

    -- 跨天支持
    is_cross_day TINYINT DEFAULT 0 COMMENT '是否跨天班次：0-否 1-是',
    cross_day_rule VARCHAR(20) DEFAULT 'START_DATE' COMMENT '跨天归属规则',

    -- 状态
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    sort_order INT DEFAULT 0,
    remark VARCHAR(500),

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 2. 考勤规则配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_rule_config (
    config_id BIGINT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL,
    apply_scope VARCHAR(20) DEFAULT 'ALL' COMMENT 'ALL/DEPARTMENT/SHIFT/EMPLOYEE',
    scope_id BIGINT,
    rule_status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',

    -- 迟到规则
    late_check_enabled TINYINT DEFAULT 1,
    late_minutes INT DEFAULT 5,
    serious_late_minutes INT DEFAULT 30,
    late_handle_type VARCHAR(20) DEFAULT 'WARNING',
    late_deduct_amount DECIMAL(10,2),

    -- 早退规则
    early_check_enabled TINYINT DEFAULT 1,
    early_minutes INT DEFAULT 5,
    serious_early_minutes INT DEFAULT 30,
    early_handle_type VARCHAR(20) DEFAULT 'WARNING',
    early_deduct_amount DECIMAL(10,2),

    -- 旷工规则
    absent_check_enabled TINYINT DEFAULT 1,
    absent_rule_type VARCHAR(20) DEFAULT 'LATE_THRESHOLD',
    absent_minutes INT DEFAULT 60,
    late_to_absent_minutes INT DEFAULT 120,
    absent_handle_type VARCHAR(20) DEFAULT 'DEDUCT',
    absent_deduct_rate DECIMAL(5,2) DEFAULT 3.0,

    -- 弹性时间
    flexible_start_enabled TINYINT DEFAULT 0,
    flexible_start_minutes INT DEFAULT 15,
    flexible_end_enabled TINYINT DEFAULT 0,
    flexible_end_minutes INT DEFAULT 15,

    -- 缺卡规则
    missing_card_check_enabled TINYINT DEFAULT 1,
    allowed_supplement_times INT DEFAULT 3,
    supplement_days_limit INT DEFAULT 3,

    description VARCHAR(500),
    remark VARCHAR(500),

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 3. 考勤记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_record (
    record_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    department_id BIGINT,
    department_name VARCHAR(100),

    -- 打卡信息
    attendance_date DATE NOT NULL,
    shift_id BIGINT,
    shift_name VARCHAR(50),

    -- 上班打卡
    check_in_time TIMESTAMP,
    check_in_location VARCHAR(100),
    check_in_device_id VARCHAR(50),
    check_in_status VARCHAR(20) COMMENT 'NORMAL/LATE/ABSENT',

    -- 下班打卡
    check_out_time TIMESTAMP,
    check_out_location VARCHAR(100),
    check_out_device_id VARCHAR(50),
    check_out_status VARCHAR(20) COMMENT 'NORMAL/EARLY/ABSENT',

    -- 工作时长
    work_duration INT COMMENT '实际工作时长（分钟）',
    overtime_duration INT COMMENT '加班时长（分钟）',

    -- 状态
    attendance_status VARCHAR(20) DEFAULT 'NORMAL' COMMENT 'NORMAL/LATE/EARLY/ABSENT',
    remark VARCHAR(500),

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 4. 考勤异常记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_anomaly (
    anomaly_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    department_id BIGINT,
    department_name VARCHAR(100),
    shift_id BIGINT,
    shift_name VARCHAR(50),
    attendance_date DATE NOT NULL,

    -- 异常信息
    anomaly_type VARCHAR(20) NOT NULL COMMENT 'MISSING_CARD/LATE/EARLY/ABSENT',
    severity_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT 'NORMAL/SERIOUS/CRITICAL',
    expected_punch_time TIMESTAMP,
    actual_punch_time TIMESTAMP,
    punch_type VARCHAR(20) COMMENT 'CHECK_IN/CHECK_OUT',
    anomaly_duration INT COMMENT '异常时长（分钟）',
    anomaly_reason VARCHAR(200),

    -- 处理信息
    anomaly_status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPLIED/APPROVED/REJECTED/IGNORED',
    apply_id BIGINT,
    handler_id BIGINT,
    handler_name VARCHAR(50),
    handle_time TIMESTAMP,
    handle_comment VARCHAR(500),

    -- 修正信息
    attendance_record_id BIGINT,
    is_corrected TINYINT DEFAULT 0,
    corrected_time TIMESTAMP,
    corrected_punch_time TIMESTAMP,

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 5. 考勤异常申请表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_anomaly_apply (
    apply_id BIGINT PRIMARY KEY,
    apply_no VARCHAR(50) NOT NULL UNIQUE,
    applicant_id BIGINT NOT NULL,
    applicant_name VARCHAR(50) NOT NULL,
    department_id BIGINT,
    department_name VARCHAR(100),

    -- 申请信息
    apply_type VARCHAR(20) NOT NULL COMMENT 'SUPPLEMENT_CARD/LATE_EXPLANATION/EARLY_EXPLANATION/ABSENT_APPEAL',
    anomaly_id BIGINT,
    attendance_date DATE NOT NULL,
    shift_id BIGINT,
    shift_name VARCHAR(50),
    punch_type VARCHAR(20) COMMENT 'CHECK_IN/CHECK_OUT',
    original_punch_time TIMESTAMP,
    applied_punch_time TIMESTAMP,

    -- 申请内容
    apply_reason VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    attachment_path VARCHAR(500),

    -- 审批信息
    apply_status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED',
    approver_id BIGINT,
    approver_name VARCHAR(50),
    approve_time TIMESTAMP,
    approve_comment VARCHAR(500),
    apply_time TIMESTAMP NOT NULL,

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 6. 加班申请表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_overtime_apply (
    apply_id BIGINT PRIMARY KEY,
    apply_no VARCHAR(50) NOT NULL UNIQUE,
    applicant_id BIGINT NOT NULL,
    applicant_name VARCHAR(50) NOT NULL,
    department_id BIGINT,
    department_name VARCHAR(100),

    -- 加班信息
    overtime_type VARCHAR(20) NOT NULL COMMENT 'WORKDAY/WEEKEND/HOLIDAY',
    overtime_date DATE NOT NULL,
    shift_id BIGINT,
    shift_name VARCHAR(50),

    -- 加班时长
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    planned_duration INT NOT NULL COMMENT '计划加班时长（分钟）',
    actual_duration INT COMMENT '实际加班时长（分钟）',

    -- 加班原因
    overtime_reason VARCHAR(200) NOT NULL,
    description VARCHAR(1000),

    -- 审批信息
    apply_status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED',
    approver_id BIGINT,
    approver_name VARCHAR(50),
    approve_time TIMESTAMP,
    approve_comment VARCHAR(500),
    apply_time TIMESTAMP NOT NULL,

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    update_user_id BIGINT,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 7. 考勤汇总表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_summary (
    summary_id BIGINT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    employee_name VARCHAR(100),
    department_id BIGINT,
    department_name VARCHAR(200),
    summary_month VARCHAR(10) NOT NULL COMMENT '格式：2024-01',

    -- 统计数据
    work_days INT DEFAULT 0,
    actual_days INT DEFAULT 0,
    absent_days INT DEFAULT 0,
    late_count INT DEFAULT 0,
    early_count INT DEFAULT 0,
    overtime_hours DECIMAL(10,2) DEFAULT 0.00,
    weekend_overtime_hours DECIMAL(10,2) DEFAULT 0.00,
    leave_days DECIMAL(10,2) DEFAULT 0.00,
    attendance_rate DECIMAL(5,4) DEFAULT 0.0000,

    status TINYINT DEFAULT 1,

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 8. 部门统计表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_department_statistics (
    statistics_id BIGINT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    department_name VARCHAR(200),
    statistics_month VARCHAR(10) NOT NULL COMMENT '格式：2024-01',

    -- 统计数据
    total_employees INT DEFAULT 0,
    present_employees INT DEFAULT 0,
    absent_employees INT DEFAULT 0,
    late_employees INT DEFAULT 0,
    attendance_rate DECIMAL(5,4) DEFAULT 0.0000,
    avg_work_hours DECIMAL(10,2) DEFAULT 0.00,
    total_overtime_hours DECIMAL(10,2) DEFAULT 0.00,
    exception_count INT DEFAULT 0,
    statistics_details VARCHAR(1000) COMMENT '统计详情JSON',

    status TINYINT DEFAULT 1,

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 9. 智能排班计划表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_smart_schedule_plan (
    plan_id BIGINT PRIMARY KEY,
    plan_name VARCHAR(200) NOT NULL,
    plan_code VARCHAR(100),
    plan_type TINYINT DEFAULT 1 COMMENT '1-自动 2-手动 3-混合',

    -- 排班周期
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    period_days INT NOT NULL,

    -- 排班范围
    department_id BIGINT,
    department_name VARCHAR(200),
    employee_ids VARCHAR(1000) COMMENT '员工ID列表JSON',
    employee_count INT DEFAULT 0,

    -- 优化配置
    optimization_goal TINYINT DEFAULT 5,
    optimization_weights VARCHAR(500),
    constraints VARCHAR(500),
    min_consecutive_work_days INT DEFAULT 3,
    max_consecutive_work_days INT DEFAULT 6,
    min_rest_days INT DEFAULT 1,
    min_daily_staff INT DEFAULT 5,
    max_daily_staff INT DEFAULT 20,

    -- 算法配置
    algorithm_type TINYINT DEFAULT 1 COMMENT '1-遗传 2-模拟退火 3-贪心 4-整数规划',
    algorithm_params VARCHAR(500),
    max_iterations INT DEFAULT 1000,
    convergence_threshold DECIMAL(10,6) DEFAULT 0.001,

    -- 执行状态
    execution_status TINYINT DEFAULT 0 COMMENT '0-待执行 1-执行中 2-已完成 3-失败',
    execute_start_time TIMESTAMP,
    execute_end_time TIMESTAMP,
    execution_duration_ms BIGINT,
    execution_progress DECIMAL(5,2),

    -- 优化结果
    objective_value DECIMAL(10,6),
    fitness_score DECIMAL(5,4),
    iteration_count INT,
    fairness_score DECIMAL(5,4),
    cost_score DECIMAL(5,4),
    efficiency_score DECIMAL(5,4),
    satisfaction_score DECIMAL(5,4),
    conflict_count INT DEFAULT 0,
    unsatisfied_constraint_count INT DEFAULT 0,

    -- 操作信息
    create_user_id BIGINT,
    create_user_name VARCHAR(100),
    remark VARCHAR(500),

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 10. 智能排班结果明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_smart_schedule_result (
    result_id BIGINT PRIMARY KEY,
    plan_id BIGINT NOT NULL,

    -- 员工信息
    employee_id BIGINT NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    employee_no VARCHAR(50),
    department_id BIGINT,
    department_name VARCHAR(200),

    -- 排班日期
    schedule_date DATE NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '1-周一 ... 7-周日',
    is_work_day TINYINT DEFAULT 1,

    -- 班次信息
    shift_id BIGINT NOT NULL,
    shift_code VARCHAR(50),
    shift_name VARCHAR(100),
    shift_type TINYINT,

    -- 工作时间
    work_start_time TIME NOT NULL,
    work_end_time TIME NOT NULL,
    work_duration INT NOT NULL,
    lunch_start_time TIME,
    lunch_end_time TIME,
    lunch_duration INT,

    -- 统计信息
    monthly_work_days INT,
    monthly_work_hours DECIMAL(10,2),
    consecutive_work_days INT,
    consecutive_rest_days INT,

    -- 优化信息
    fitness_score DECIMAL(5,4),
    has_conflict TINYINT DEFAULT 0,
    conflict_types VARCHAR(500),
    conflict_description VARCHAR(500),

    -- 状态信息
    schedule_status TINYINT DEFAULT 1 COMMENT '1-草稿 2-已确认 3-已执行 4-已取消',
    confirm_time TIMESTAMP,
    confirm_user_id BIGINT,
    confirm_user_name VARCHAR(100),
    remark VARCHAR(500),

    -- 审计字段
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 11. 排班记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_schedule_record (
    record_id BIGINT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    department_id BIGINT,
    department_name VARCHAR(200),

    schedule_date DATE NOT NULL,
    shift_id BIGINT NOT NULL,
    shift_name VARCHAR(100),

    schedule_type VARCHAR(20) DEFAULT 'AUTO' COMMENT 'AUTO-自动 MANUAL-手动',

    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- 12. 请假记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS t_attendance_leave (
    leave_id BIGINT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    employee_name VARCHAR(100) NOT NULL,
    department_id BIGINT,
    department_name VARCHAR(200),

    leave_type VARCHAR(20) NOT NULL COMMENT 'ANNUAL/SICK/MARRIAGE/MATERNITY/BEREAVEMENT/OTHER',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_days DECIMAL(5,2) NOT NULL,

    leave_reason VARCHAR(200) NOT NULL,
    approver_id BIGINT,
    approver_name VARCHAR(50),
    approval_time TIMESTAMP,
    apply_status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',

    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_flag TINYINT DEFAULT 0
);

-- ============================================================
-- Schema创建完成
-- ============================================================
