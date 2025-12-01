-- =====================================================
-- 考勤模块扩展表 - 考勤统计扩展表
-- 基于消费模块模式的考勤系统功能完善
-- 创建时间: 2025-11-25
-- 创建目的: 补充基础统计表的不足，支持多维度统计和实时计算结果存储
-- =====================================================

-- 创建考勤统计扩展表
CREATE TABLE t_attendance_statistics_ext (
    -- 主键配置
    stat_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',

    -- 统计维度和时间
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_period VARCHAR(20) NOT NULL COMMENT '统计周期（DAILY/WEEKLY/MONTHLY/QUARTERLY/YEARLY）',
    stat_period_key VARCHAR(50) NOT NULL COMMENT '统计周期键（YYYY-MM-DD, YYYY-WW01, YYYY-MM, YYYY-Q1, YYYY）',

    -- 维度分类
    dimension_type VARCHAR(30) NOT NULL COMMENT '维度类型（COMPANY/DEPARTMENT/AREA/EMPLOYEE/POSITION/SHIFT）',
    dimension_id VARCHAR(50) NOT NULL COMMENT '维度ID（对应具体实体的ID）',
    dimension_name VARCHAR(100) COMMENT '维度名称（冗余字段便于查询）',

    -- 基础统计指标
    total_employees INT DEFAULT 0 COMMENT '应到员工数',
    actual_employees INT DEFAULT 0 COMMENT '实到员工数',
    late_employees INT DEFAULT 0 COMMENT '迟到员工数',
    early_employees INT DEFAULT 0 COMMENT '早退员工数',
    absent_employees INT DEFAULT 0 COMMENT '缺勤员工数',
    leave_employees INT DEFAULT 0 COMMENT '请假员工数',

    -- 时间统计（分钟）
    total_work_minutes DECIMAL(10,2) DEFAULT 0 COMMENT '总工时（分钟）',
    standard_work_minutes DECIMAL(10,2) DEFAULT 0 COMMENT '标准工时（分钟）',
    overtime_minutes DECIMAL(10,2) DEFAULT 0 COMMENT '加班工时（分钟）',
    leave_minutes DECIMAL(10,2) DEFAULT 0 COMMENT '请假工时（分钟）',

    -- 出勤率计算
    attendance_rate DECIMAL(5,2) DEFAULT 0 COMMENT '出勤率（%）',
    punctuality_rate DECIMAL(5,2) DEFAULT 0 COMMENT '准点率（%）',
    overtime_rate DECIMAL(5,2) DEFAULT 0 COMMENT '加班率（%）',

    -- 异常统计
    exception_count INT DEFAULT 0 COMMENT '异常记录数',
    pending_exceptions INT DEFAULT 0 COMMENT '待处理异常数',
    resolved_exceptions INT DEFAULT 0 COMMENT '已处理异常数',

    -- 设备使用统计
    device_usage_count INT DEFAULT 0 COMMENT '设备使用次数',
    device_success_count INT DEFAULT 0 COMMENT '设备成功次数',
    device_failure_count INT DEFAULT 0 COMMENT '设备失败次数',

    -- 地理位置统计
    gps_validation_count INT DEFAULT 0 COMMENT 'GPS验证次数',
    gps_success_count INT DEFAULT 0 COMMENT 'GPS验证成功次数',
    out_of_geofence_count INT DEFAULT 0 COMMENT '超出围栏次数',

    -- 详细统计数据（JSON格式）
    detailed_stats JSON COMMENT '详细统计数据（JSON格式）',
    trend_data JSON COMMENT '趋势数据（JSON格式）',
    comparison_data JSON COMMENT '对比数据（JSON格式）',

    -- 计算时间和缓存信息
    calculated_time DATETIME COMMENT '计算时间',
    cache_expires_at DATETIME COMMENT '缓存过期时间',
    is_cached TINYINT DEFAULT 0 COMMENT '是否缓存数据（0-否，1-是）',
    data_source VARCHAR(50) COMMENT '数据来源（CALCULATED/MANUAL/IMPORTED）',

    -- 基础审计字段（继承BaseEntity）
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记（0-正常，1-删除）',
    version INT DEFAULT 1 COMMENT '版本号',

    -- 复合索引
    UNIQUE KEY uk_stat_period_dimension (stat_date, stat_period, dimension_type, dimension_id),

    -- 查询优化索引
    INDEX idx_stat_date (stat_date),
    INDEX idx_stat_period (stat_period, stat_period_key),
    INDEX idx_dimension (dimension_type, dimension_id),
    INDEX idx_attendance_rate (attendance_rate),
    INDEX idx_calculated_time (calculated_time),
    INDEX idx_cache_expires (cache_expires_at),
    INDEX idx_deleted_flag (deleted_flag),

    -- 复合索引
    INDEX idx_date_period_dimension (stat_date, stat_period, dimension_type),
    INDEX idx_period_type_dimension (stat_period, dimension_type, dimension_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤统计扩展表';

-- 创建统计汇总表（按天、周、月等）
CREATE TABLE t_attendance_statistics_summary (
    -- 主键配置
    summary_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '汇总ID',

    -- 汇总维度
    summary_type VARCHAR(20) NOT NULL COMMENT '汇总类型（DAILY_SUMMARY/WEEKLY_SUMMARY/MONTHLY_SUMMARY）',
    summary_date DATE NOT NULL COMMENT '汇总日期',
    summary_period_key VARCHAR(50) NOT NULL COMMENT '汇总周期键',

    -- 总体统计
    company_total_employees INT DEFAULT 0 COMMENT '公司总人数',
    company_attendance_rate DECIMAL(5,2) DEFAULT 0 COMMENT '公司出勤率',
    company_punctuality_rate DECIMAL(5,2) DEFAULT 0 COMMENT '公司准点率',
    company_overtime_rate DECIMAL(5,2) DEFAULT 0 COMMENT '公司加班率',

    -- 部门级汇总
    dept_count INT DEFAULT 0 COMMENT '参与统计的部门数',
    avg_dept_attendance_rate DECIMAL(5,2) DEFAULT 0 COMMENT '平均部门出勤率',
    max_dept_attendance_rate DECIMAL(5,2) DEFAULT 0 COMMENT '最高部门出勤率',
    min_dept_attendance_rate DECIMAL(5,2) DEFAULT 0 COMMENT '最低部门出勤率',

    -- 趋势指标
    attendance_trend DECIMAL(5,2) DEFAULT 0 COMMENT '出勤率趋势（相较于上期）',
    punctuality_trend DECIMAL(5,2) DEFAULT 0 COMMENT '准点率趋势（相较于上期）',
    overtime_trend DECIMAL(5,2) DEFAULT 0 COMMENT '加班率趋势（相较于上期）',

    -- 异常汇总
    total_exceptions INT DEFAULT 0 COMMENT '总异常数',
    exception_rate DECIMAL(5,2) DEFAULT 0 COMMENT '异常率（%）',

    -- 基础审计字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    -- 索引
    UNIQUE KEY uk_summary_type_period (summary_type, summary_date),
    INDEX idx_summary_date (summary_date),
    INDEX idx_summary_type (summary_type),
    INDEX idx_deleted_flag (deleted_flag)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤统计汇总表';

-- 创建实时统计缓存表
CREATE TABLE t_attendance_realtime_stats (
    -- 主键配置
    cache_key VARCHAR(100) PRIMARY KEY COMMENT '缓存键',

    -- 实时统计数据
    current_online_employees INT DEFAULT 0 COMMENT '当前在线员工数',
    today_punch_in_count INT DEFAULT 0 COMMENT '今日打卡次数',
    today_punch_out_count INT DEFAULT 0 COMMENT '今日下班打卡次数',

    -- 实时异常统计
    today_late_count INT DEFAULT 0 COMMENT '今日迟到次数',
    today_early_count INT DEFAULT 0 COMMENT '今日早退次数',
    today_exception_count INT DEFAULT 0 COMMENT '今日异常次数',

    -- 设备状态统计
    online_devices INT DEFAULT 0 COMMENT '在线设备数',
    offline_devices INT DEFAULT 0 COMMENT '离线设备数',
    device_error_count INT DEFAULT 0 COMMENT '设备错误次数',

    -- JSON格式数据
    detailed_realtime_data JSON COMMENT '详细实时数据（JSON格式）',

    -- 缓存控制
    last_updated DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    expires_at DATETIME COMMENT '过期时间',
    ttl_seconds INT DEFAULT 300 COMMENT '缓存时间（秒）',

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤实时统计缓存表';

-- 创建统计配置视图
CREATE VIEW v_attendance_statistics_dashboard AS
SELECT
    s.stat_date,
    s.stat_period,
    s.dimension_type,
    s.dimension_name,
    s.total_employees,
    s.actual_employees,
    s.late_employees,
    s.early_employees,
    s.absent_employees,
    s.leave_employees,
    s.attendance_rate,
    s.punctuality_rate,
    s.overtime_rate,
    s.exception_count,
    s.calculated_time,
    s.is_cached
FROM t_attendance_statistics_ext s
WHERE s.deleted_flag = 0
  AND s.dimension_type IN ('COMPANY', 'DEPARTMENT', 'AREA')
ORDER BY s.stat_date DESC, s.dimension_type, s.attendance_rate DESC;

-- 创建趋势分析视图
CREATE VIEW v_attendance_trend_analysis AS
SELECT
    stat_period_key,
    dimension_type,
    dimension_id,
    dimension_name,
    AVG(attendance_rate) as avg_attendance_rate,
    AVG(punctuality_rate) as avg_punctuality_rate,
    AVG(overtime_rate) as avg_overtime_rate,
    MIN(stat_date) as start_date,
    MAX(stat_date) as end_date,
    COUNT(*) as data_points
FROM t_attendance_statistics_ext
WHERE deleted_flag = 0
  AND stat_date >= DATE_SUB(CURRENT_DATE, INTERVAL 90 DAY)
GROUP BY stat_period_key, dimension_type, dimension_id, dimension_name
ORDER BY stat_period_key DESC;

-- 插入初始配置数据
INSERT INTO t_attendance_realtime_stats (
    cache_key, current_online_employees, today_punch_in_count,
    today_punch_out_count, today_late_count, today_early_count,
    online_devices, offline_devices, ttl_seconds
) VALUES
('company_overview', 0, 0, 0, 0, 0, 0, 0, 300),
('department_stats', 0, 0, 0, 0, 0, 0, 0, 300),
('realtime_alerts', 0, 0, 0, 0, 0, 0, 0, 300);