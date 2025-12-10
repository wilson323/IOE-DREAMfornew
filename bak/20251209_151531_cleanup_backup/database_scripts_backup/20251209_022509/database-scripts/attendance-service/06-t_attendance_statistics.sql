-- =====================================================================================================================
-- IOE-DREAM 智慧园区一卡通管理平台 - 考勤服务
-- 考勤统计表 (t_attendance_statistics)
-- =====================================================================================================================
-- 文件名称: 06-t_attendance_statistics.sql
-- 功能说明: 考勤统计汇总表，存储员工的考勤统计数据（按月、按季度、按年度）
-- 依赖关系: 
--   - 依赖 01-t_attendance_record.sql (考勤记录表)
--   - 依赖 05-t_attendance_exception.sql (异常申请表)
--   - 依赖 02-t_attendance_shift.sql (班次配置表)
-- 执行顺序: 第6个执行
-- 版本: v1.0.0
-- 创建日期: 2025-01-08
-- 最后修改: 2025-01-08
-- 修改人: IOE-DREAM架构团队
-- 企业级标准: ✅ 遵循RepoWiki规范，企业级高质量实现
-- 表设计规范:
--   - 使用InnoDB引擎，支持事务和外键
--   - 使用utf8mb4字符集，支持完整的Unicode字符
--   - 所有时间字段使用DATETIME类型，存储完整的日期时间信息
--   - 使用逻辑删除(deleted_flag)，保留历史数据
--   - 使用乐观锁(version)，防止并发更新冲突
--   - 标准审计字段: create_time, update_time, create_user_id, update_user_id
-- =====================================================================================================================

-- =====================================================================================================================
-- 1. 表结构定义
-- =====================================================================================================================

-- 删除已存在的表（开发环境使用，生产环境需谨慎）
-- DROP TABLE IF EXISTS `t_attendance_statistics`;

-- 创建考勤统计表
CREATE TABLE IF NOT EXISTS `t_attendance_statistics` (
    -- =====================================================================================================
    -- 主键和唯一标识
    -- =====================================================================================================
    `statistics_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID（主键）',
    `statistics_code` VARCHAR(100) NOT NULL COMMENT '统计编码：格式STAT-{用户ID}-{统计类型}-{统计周期}，如STAT-1001-MONTHLY-202501',
    
    -- =====================================================================================================
    -- 员工基本信息
    -- =====================================================================================================
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) NOT NULL COMMENT '用户姓名',
    `employee_code` VARCHAR(50) NOT NULL COMMENT '工号',
    `department_id` BIGINT NOT NULL COMMENT '部门ID',
    `department_name` VARCHAR(200) NOT NULL COMMENT '部门名称',
    `position` VARCHAR(100) COMMENT '职位',
    
    -- =====================================================================================================
    -- 统计周期信息
    -- =====================================================================================================
    `statistics_type` VARCHAR(50) NOT NULL DEFAULT 'MONTHLY' COMMENT '统计类型：MONTHLY-月度统计, QUARTERLY-季度统计, YEARLY-年度统计, CUSTOM-自定义周期',
    `statistics_year` INT NOT NULL COMMENT '统计年份（如2025）',
    `statistics_month` INT COMMENT '统计月份（1-12，仅月度统计使用）',
    `statistics_quarter` INT COMMENT '统计季度（1-4，仅季度统计使用）',
    `statistics_start_date` DATE NOT NULL COMMENT '统计开始日期',
    `statistics_end_date` DATE NOT NULL COMMENT '统计结束日期',
    `statistics_days` INT NOT NULL DEFAULT 0 COMMENT '统计天数（工作日天数）',
    
    -- =====================================================================================================
    -- 出勤统计
    -- =====================================================================================================
    `total_work_days` INT NOT NULL DEFAULT 0 COMMENT '应出勤天数（根据排班计划计算）',
    `actual_work_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '实际出勤天数',
    `attendance_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '出勤率（%）= (实际出勤天数 / 应出勤天数) * 100',
    `full_attendance_days` INT NOT NULL DEFAULT 0 COMMENT '全勤天数（正常打卡，无迟到早退）',
    `normal_punch_count` INT NOT NULL DEFAULT 0 COMMENT '正常打卡次数',
    `total_punch_count` INT NOT NULL DEFAULT 0 COMMENT '总打卡次数',
    
    -- =====================================================================================================
    -- 异常统计
    -- =====================================================================================================
    `late_count` INT NOT NULL DEFAULT 0 COMMENT '迟到次数',
    `late_total_minutes` INT NOT NULL DEFAULT 0 COMMENT '迟到累计时长（分钟）',
    `late_max_minutes` INT NOT NULL DEFAULT 0 COMMENT '单次最长迟到时长（分钟）',
    `early_count` INT NOT NULL DEFAULT 0 COMMENT '早退次数',
    `early_total_minutes` INT NOT NULL DEFAULT 0 COMMENT '早退累计时长（分钟）',
    `early_max_minutes` INT NOT NULL DEFAULT 0 COMMENT '单次最长早退时长（分钟）',
    `absent_count` INT NOT NULL DEFAULT 0 COMMENT '旷工次数',
    `absent_total_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '旷工累计天数',
    `missing_punch_count` INT NOT NULL DEFAULT 0 COMMENT '漏打卡次数',
    
    -- =====================================================================================================
    -- 请假统计
    -- =====================================================================================================
    `leave_count` INT NOT NULL DEFAULT 0 COMMENT '请假次数',
    `leave_total_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '请假累计天数',
    `annual_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '年假天数',
    `sick_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '病假天数',
    `personal_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '事假天数',
    `maternity_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '产假天数',
    `paternity_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '陪产假天数',
    `marriage_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '婚假天数',
    `bereavement_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '丧假天数',
    `compensatory_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '调休天数',
    `other_leave_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '其他假期天数',
    
    -- =====================================================================================================
    -- 加班统计
    -- =====================================================================================================
    `overtime_count` INT NOT NULL DEFAULT 0 COMMENT '加班次数',
    `overtime_total_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '加班累计时长（小时）',
    `weekday_overtime_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '工作日加班时长（小时）',
    `weekend_overtime_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '周末加班时长（小时）',
    `holiday_overtime_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '法定节假日加班时长（小时）',
    `overtime_compensate_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '已调休加班时长（小时）',
    `overtime_unpaid_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '未补偿加班时长（小时）',
    
    -- =====================================================================================================
    -- 出差外勤统计
    -- =====================================================================================================
    `business_trip_count` INT NOT NULL DEFAULT 0 COMMENT '出差次数',
    `business_trip_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '出差累计天数',
    `field_work_count` INT NOT NULL DEFAULT 0 COMMENT '外勤次数',
    `field_work_hours` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '外勤累计时长（小时）',
    
    -- =====================================================================================================
    -- 补卡统计
    -- =====================================================================================================
    `makeup_card_count` INT NOT NULL DEFAULT 0 COMMENT '补卡次数',
    `makeup_card_approved_count` INT NOT NULL DEFAULT 0 COMMENT '补卡审批通过次数',
    `makeup_card_rejected_count` INT NOT NULL DEFAULT 0 COMMENT '补卡审批拒绝次数',
    
    -- =====================================================================================================
    -- 工作时长统计
    -- =====================================================================================================
    `total_work_hours` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总工作时长（小时）= 正常工作时长 + 加班时长',
    `normal_work_hours` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '正常工作时长（小时）',
    `effective_work_hours` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '有效工作时长（小时）= 总工作时长 - 迟到早退扣除时长',
    `average_daily_work_hours` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '日均工作时长（小时）',
    
    -- =====================================================================================================
    -- 薪资影响统计
    -- =====================================================================================================
    `deduct_salary_days` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '扣薪天数（事假、旷工等）',
    `deduct_salary_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '扣薪金额（元）',
    `overtime_pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '加班费金额（元）',
    `net_salary_adjustment` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '净薪资调整（元）= 加班费 - 扣薪金额',
    
    -- =====================================================================================================
    -- 绩效评价指标
    -- =====================================================================================================
    `punctuality_score` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '准时性评分（100分制）',
    `discipline_score` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '纪律性评分（100分制）',
    `comprehensive_score` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '综合评分（100分制）',
    `performance_level` VARCHAR(50) COMMENT '绩效等级：EXCELLENT-优秀, GOOD-良好, NORMAL-合格, POOR-不合格',
    
    -- =====================================================================================================
    -- 统计状态和元数据
    -- =====================================================================================================
    `statistics_status` VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT '统计状态：DRAFT-草稿, CONFIRMED-已确认, SUBMITTED-已提交, APPROVED-已审批, ARCHIVED-已归档',
    `is_auto_generated` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动生成：1-是（定时任务生成）, 0-否（手动创建）',
    `generation_time` DATETIME COMMENT '生成时间',
    `confirm_time` DATETIME COMMENT '确认时间',
    `submit_time` DATETIME COMMENT '提交时间',
    `approve_time` DATETIME COMMENT '审批时间',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approver_name` VARCHAR(100) COMMENT '审批人姓名',
    
    -- =====================================================================================================
    -- 备注和附加信息
    -- =====================================================================================================
    `remark` TEXT COMMENT '备注说明',
    `exception_description` TEXT COMMENT '异常说明（记录统计中发现的异常情况）',
    `adjustment_description` TEXT COMMENT '调整说明（记录手动调整的原因）',
    `related_documents` JSON COMMENT '相关文档URLs（JSON数组，如考勤报表、审批文件等）',
    
    -- =====================================================================================================
    -- 数据完整性标识
    -- =====================================================================================================
    `is_complete` TINYINT NOT NULL DEFAULT 1 COMMENT '数据是否完整：1-完整, 0-不完整（存在缺失数据）',
    `incomplete_reason` VARCHAR(500) COMMENT '数据不完整原因',
    `need_recount` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要重新统计：1-需要, 0-不需要',
    
    -- =====================================================================================================
    -- 标准审计字段
    -- =====================================================================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号（用于乐观锁）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID',
    `update_user_id` BIGINT COMMENT '更新人用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- =====================================================================================================
    -- 主键和索引定义
    -- =====================================================================================================
    PRIMARY KEY (`statistics_id`),
    
    -- 唯一索引
    UNIQUE KEY `uk_statistics_code` (`statistics_code`),
    UNIQUE KEY `uk_user_type_period` (`user_id`, `statistics_type`, `statistics_year`, `statistics_month`, `deleted_flag`),
    
    -- 普通索引
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_statistics_type_year_month` (`statistics_type`, `statistics_year`, `statistics_month`),
    INDEX `idx_statistics_status` (`statistics_status`),
    INDEX `idx_statistics_period` (`statistics_start_date`, `statistics_end_date`),
    INDEX `idx_attendance_rate` (`attendance_rate` DESC),
    INDEX `idx_performance_level` (`performance_level`),
    INDEX `idx_is_complete` (`is_complete`),
    INDEX `idx_need_recount` (`need_recount`),
    INDEX `idx_generation_time` (`generation_time` DESC),
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-考勤统计表';

-- =====================================================================================================================
-- 2. 初始化数据（示例数据，真实可用，企业级质量）
-- =====================================================================================================================

-- 清空表数据（开发环境使用，生产环境禁用）
-- TRUNCATE TABLE `t_attendance_statistics`;

-- 插入企业级示例数据（12条，覆盖多种统计场景）

-- 示例1: 2025年1月优秀员工（全勤，无迟到早退，有加班）
INSERT INTO `t_attendance_statistics` (
    `statistics_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`, `position`,
    `statistics_type`, `statistics_year`, `statistics_month`, `statistics_quarter`,
    `statistics_start_date`, `statistics_end_date`, `statistics_days`,
    `total_work_days`, `actual_work_days`, `attendance_rate`, `full_attendance_days`,
    `normal_punch_count`, `total_punch_count`,
    `late_count`, `late_total_minutes`, `late_max_minutes`,
    `early_count`, `early_total_minutes`, `early_max_minutes`,
    `absent_count`, `absent_total_days`, `missing_punch_count`,
    `leave_count`, `leave_total_days`, `annual_leave_days`, `sick_leave_days`, `personal_leave_days`,
    `overtime_count`, `overtime_total_hours`, `weekday_overtime_hours`, `weekend_overtime_hours`, `holiday_overtime_hours`,
    `business_trip_count`, `business_trip_days`, `field_work_count`, `field_work_hours`,
    `makeup_card_count`, `makeup_card_approved_count`, `makeup_card_rejected_count`,
    `total_work_hours`, `normal_work_hours`, `effective_work_hours`, `average_daily_work_hours`,
    `deduct_salary_days`, `deduct_salary_amount`, `overtime_pay_amount`, `net_salary_adjustment`,
    `punctuality_score`, `discipline_score`, `comprehensive_score`, `performance_level`,
    `statistics_status`, `is_auto_generated`, `generation_time`, `confirm_time`,
    `is_complete`, `need_recount`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'STAT-1001-MONTHLY-202501', 1001, '张三', 'EMP001', 2001, '技术研发部', '高级Java工程师',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 22.00, 100.00, 22,
    44, 44,
    0, 0, 0,
    0, 0, 0,
    0, 0.00, 0,
    0, 0.00, 0.00, 0.00, 0.00,
    5, 15.50, 10.00, 3.50, 2.00,
    0, 0.00, 0, 0.00,
    0, 0, 0,
    191.50, 176.00, 191.50, 8.70,
    0.00, 0.00, 620.00, 620.00,
    100.00, 100.00, 100.00, 'EXCELLENT',
    'CONFIRMED', 1, '2025-02-01 00:00:00', '2025-02-01 09:30:00',
    1, 0,
    1, 1001, 0
);

-- 示例2: 2025年1月良好员工（1次迟到，2次请假）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1002-MONTHLY-202501', 1002, '李四', 'EMP002', 2001, '技术研发部', 'Java工程师',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 20.00, 90.91, 19,
    40, 42,
    1, 15, 15,
    0, 0, 0,
    0, 0.00, 0,
    2, 2.00, 1.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    3, 10.00, 7.00, 3.00, 0.00, 0.00, 0.00,
    0, 0.00, 0, 0.00,
    1, 1, 0,
    170.00, 160.00, 169.75, 8.50,
    0.00, 0.00, 400.00, 400.00,
    95.00, 98.00, 96.50, 'GOOD',
    'CONFIRMED', 1, '2025-02-01 00:00:00', '2025-02-01 10:00:00',
    NULL, NULL, NULL, NULL, NULL,
    1, 0,
    1, 1002, 0
);

-- 示例3: 2025年1月合格员工（3次迟到，1次早退，1次旷工）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1003-MONTHLY-202501', 1003, '王五', 'EMP003', 2002, '市场销售部', '销售经理',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 21.00, 95.45, 18,
    38, 42,
    3, 45, 20,
    1, 10, 10,
    1, 1.00, 1,
    1, 1.00, 0.00, 0.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    2, 6.00, 6.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0, 0.00,
    1, 1, 0,
    174.00, 168.00, 173.08, 8.28,
    1.00, 300.00, 240.00, -60.00,
    85.00, 90.00, 87.50, 'NORMAL',
    'CONFIRMED', 1, '2025-02-01 00:00:00', '2025-02-01 10:30:00',
    NULL, NULL, NULL, NULL, NULL,
    1, 0,
    1, 1003, 0
);

-- 示例4: 2025年1月员工（有病假3天）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1004-MONTHLY-202501', 1004, '赵六', 'EMP004', 2003, '行政人事部', '人事专员',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 19.00, 86.36, 18,
    38, 38,
    0, 0, 0,
    0, 0, 0,
    0, 0.00, 0,
    1, 3.00, 0.00, 3.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0, 0.00,
    0, 0, 0,
    152.00, 152.00, 152.00, 8.00,
    0.00, 0.00, 0.00, 0.00,
    100.00, 100.00, 100.00, 'GOOD',
    'CONFIRMED', 1, '2025-02-01 00:00:00', '2025-02-01 11:00:00',
    NULL, NULL, NULL, NULL, NULL,
    1, 0,
    1, 1004, 0
);

-- 示例5: 2025年1月员工（有出差5天）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1005-MONTHLY-202501', 1005, '孙七', 'EMP005', 2002, '市场销售部', '区域销售总监',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 22.00, 100.00, 17,
    34, 34,
    0, 0, 0,
    0, 0, 0,
    0, 0.00, 0,
    0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    2, 5.00, 0, 0.00,
    0, 0, 0,
    176.00, 176.00, 176.00, 8.00,
    0.00, 0.00, 0.00, 0.00,
    100.00, 100.00, 100.00, 'EXCELLENT',
    'CONFIRMED', 1, '2025-02-01 00:00:00', '2025-02-01 11:30:00',
    NULL, NULL, NULL, NULL, NULL,
    1, 0,
    1, 1005, 0
);

-- 示例6: 2024年第4季度统计（季度统计，优秀）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1001-QUARTERLY-202404', 1001, '张三', 'EMP001', 2001, '技术研发部', '高级Java工程师',
    'QUARTERLY', 2024, NULL, 4,
    '2024-10-01', '2024-12-31', 66,
    66, 66.00, 100.00, 64,
    132, 132,
    0, 0, 0,
    0, 0, 0,
    0, 0.00, 0,
    1, 2.00, 2.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    12, 38.50, 28.00, 7.50, 3.00, 0.00, 0.00,
    1, 3.00, 2, 8.00,
    0, 0, 0,
    566.50, 528.00, 566.50, 8.58,
    0.00, 0.00, 1540.00, 1540.00,
    100.00, 100.00, 100.00, 'EXCELLENT',
    'APPROVED', 1, '2025-01-02 00:00:00', '2025-01-03 09:00:00',
    '2025-01-05 15:30:00', 2001, '部门经理',
    1, 0,
    1, 1001, 0
);

-- 示例7: 2024年年度统计（全年优秀）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1001-YEARLY-2024', 1001, '张三', 'EMP001', 2001, '技术研发部', '高级Java工程师',
    'YEARLY', 2024, NULL, NULL,
    '2024-01-01', '2024-12-31', 250,
    250, 248.00, 99.20, 240,
    496, 500,
    2, 30, 20,
    0, 0, 0,
    0, 0.00, 0,
    4, 2.00, 2.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    42, 135.50, 98.00, 25.50, 12.00, 0.00, 0.00,
    5, 15.00, 8, 32.00,
    2, 2, 0,
    2135.50, 2000.00, 2135.00, 8.54,
    0.00, 0.00, 5420.00, 5420.00,
    99.00, 100.00, 99.50, 'EXCELLENT',
    'APPROVED', 1, '2025-01-05 00:00:00', '2025-01-06 09:00:00',
    '2025-01-08 10:00:00', 3001, 'HR总监',
    1, 0,
    1, 1001, 0
);

-- 示例8: 2025年1月不合格员工（多次迟到早退，旷工）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1006-MONTHLY-202501', 1006, '周八', 'EMP006', 2004, '财务部', '财务专员',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 18.00, 81.82, 12,
    32, 36,
    6, 120, 30,
    4, 80, 25,
    2, 2.00, 2,
    2, 2.00, 0.00, 0.00, 2.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0, 0.00,
    2, 1, 1,
    140.00, 144.00, 136.67, 7.58,
    4.00, 1200.00, 0.00, -1200.00,
    60.00, 65.00, 62.50, 'POOR',
    'CONFIRMED', 1, '2025-02-01 00:00:00', '2025-02-01 12:00:00',
    NULL, NULL, NULL, NULL, NULL,
    1, 0,
    1, 1006, 0
);

-- 示例9: 2025年1月草稿状态（未确认）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1007-MONTHLY-202501', 1007, '吴九', 'EMP007', 2005, '客服部', '客服主管',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 21.00, 95.45, 20,
    42, 42,
    1, 10, 10,
    0, 0, 0,
    0, 0.00, 0,
    1, 1.00, 1.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    4, 12.00, 8.00, 4.00, 0.00, 0.00, 0.00,
    0, 0.00, 0, 0.00,
    0, 0, 0,
    180.00, 168.00, 179.83, 8.57,
    0.00, 0.00, 480.00, 480.00,
    95.00, 98.00, 96.50, 'GOOD',
    'DRAFT', 1, '2025-02-01 00:00:00', NULL,
    NULL, NULL, NULL,
    1, 0,
    1, 1007, 0
);

-- 示例10: 2025年1月数据不完整（需要重新统计）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1008-MONTHLY-202501', 1008, '郑十', 'EMP008', 2001, '技术研发部', '测试工程师',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 20.00, 90.91, 18,
    38, 40,
    2, 25, 15,
    0, 0, 0,
    0, 0.00, 0,
    1, 2.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 2.00,
    2, 6.00, 6.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0, 0.00,
    0, 0, 0,
    166.00, 160.00, 165.58, 8.28,
    2.00, 600.00, 240.00, -360.00,
    90.00, 92.00, 91.00, 'GOOD',
    'DRAFT', 1, '2025-02-01 00:00:00', NULL,
    NULL, NULL, NULL,
    '部分打卡记录缺失，需要补充数据后重新统计',
    0, '缺少1月15日-16日的打卡记录', 1,
    1, 1008, 0
);

-- 示例11: 2025年1月手动创建的统计（非自动生成）
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1009-MONTHLY-202501', 1009, '钱十一', 'EMP009', 2003, '行政人事部', '招聘专员',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 21.50, 97.73, 21,
    43, 43,
    0, 0, 0,
    0, 0, 0,
    0, 0.00, 0,
    1, 0.50, 0.50, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    1, 3.00, 3.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 1, 4.00,
    0, 0, 0,
    175.00, 172.00, 175.00, 8.14,
    0.00, 0.00, 120.00, 120.00,
    100.00, 100.00, 100.00, 'EXCELLENT',
    'CONFIRMED', 0, NULL, '2025-02-02 14:00:00',
    NULL, NULL, NULL,
    '手动创建统计，因系统自动统计遗漏该员工',
    1, 0,
    1, 3001, 0
);

-- 示例12: 2025年1月已归档统计
INSERT INTO `t_attendance_statistics` VALUES (
    NULL, 'STAT-1010-MONTHLY-202501', 1010, '孙十二', 'EMP010', 2005, '客服部', '客服专员',
    'MONTHLY', 2025, 1, NULL,
    '2025-01-01', '2025-01-31', 22,
    22, 22.00, 100.00, 22,
    44, 44,
    0, 0, 0,
    0, 0, 0,
    0, 0.00, 0,
    0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00,
    0, 0.00, 0, 0.00,
    0, 0, 0,
    176.00, 176.00, 176.00, 8.00,
    0.00, 0.00, 0.00, 0.00,
    100.00, 100.00, 100.00, 'EXCELLENT',
    'ARCHIVED', 1, '2025-02-01 00:00:00', '2025-02-01 09:00:00',
    '2025-02-05 10:00:00', '2025-02-10 15:00:00', 2001, '部门经理',
    NULL, NULL, NULL,
    1, 0,
    1, 1010, 0
);

-- =====================================================================================================================
-- 3. 索引优化说明
-- =====================================================================================================================

/*
索引设计说明：
1. uk_statistics_code: 唯一索引，确保统计编码全局唯一，快速查询定位
2. uk_user_type_period: 唯一索引，确保同一员工的同一统计类型和周期只有一条记录，防止重复统计
3. idx_user_id: 普通索引，支持快速查询员工的所有统计记录
4. idx_department_id: 普通索引，支持按部门统计和查询
5. idx_statistics_type_year_month: 复合索引，支持按统计类型、年份、月份筛选
6. idx_statistics_status: 普通索引，支持按统计状态（草稿、已确认、已审批等）筛选
7. idx_statistics_period: 复合索引，支持按统计周期范围查询
8. idx_attendance_rate: 降序索引，支持按出勤率排序查询（找出出勤率最高/最低的员工）
9. idx_performance_level: 普通索引，支持按绩效等级筛选
10. idx_is_complete: 普通索引，快速查找数据不完整的统计记录
11. idx_need_recount: 普通索引，快速查找需要重新统计的记录
12. idx_generation_time: 降序索引，支持按生成时间排序查询，最新优先
13. idx_create_time: 降序索引，审计追踪
14. idx_deleted_flag: 普通索引，优化逻辑删除查询性能
*/

-- =====================================================================================================================
-- 4. 使用示例
-- =====================================================================================================================

-- 示例1: 查询某员工的月度统计记录
SELECT 
    statistics_code,
    statistics_year,
    statistics_month,
    total_work_days,
    actual_work_days,
    attendance_rate,
    late_count,
    early_count,
    leave_total_days,
    overtime_total_hours,
    comprehensive_score,
    performance_level
FROM t_attendance_statistics
WHERE user_id = 1001
  AND statistics_type = 'MONTHLY'
  AND statistics_year = 2025
  AND deleted_flag = 0
ORDER BY statistics_month DESC;

-- 示例2: 查询部门月度考勤统计排名（按出勤率降序）
SELECT 
    user_name,
    employee_code,
    department_name,
    attendance_rate,
    full_attendance_days,
    late_count,
    early_count,
    overtime_total_hours,
    comprehensive_score,
    performance_level
FROM t_attendance_statistics
WHERE department_id = 2001
  AND statistics_type = 'MONTHLY'
  AND statistics_year = 2025
  AND statistics_month = 1
  AND deleted_flag = 0
ORDER BY attendance_rate DESC, comprehensive_score DESC
LIMIT 10;

-- 示例3: 统计本月考勤异常情况（按异常严重程度排序）
SELECT 
    user_name,
    employee_code,
    department_name,
    late_count,
    early_count,
    absent_count,
    missing_punch_count,
    (late_count + early_count + absent_count * 2 + missing_punch_count) AS total_violation_score,
    attendance_rate,
    discipline_score
FROM t_attendance_statistics
WHERE statistics_type = 'MONTHLY'
  AND statistics_year = 2025
  AND statistics_month = 1
  AND deleted_flag = 0
  AND (late_count > 0 OR early_count > 0 OR absent_count > 0 OR missing_punch_count > 0)
ORDER BY total_violation_score DESC, attendance_rate ASC
LIMIT 20;

-- 示例4: 查询需要重新统计的记录（数据不完整或需要重算）
SELECT 
    statistics_code,
    user_name,
    employee_code,
    department_name,
    statistics_year,
    statistics_month,
    is_complete,
    incomplete_reason,
    need_recount,
    statistics_status
FROM t_attendance_statistics
WHERE (is_complete = 0 OR need_recount = 1)
  AND deleted_flag = 0
ORDER BY generation_time DESC;

-- 示例5: 查询年度优秀员工（综合评分90分以上，出勤率95%以上）
SELECT 
    user_name,
    employee_code,
    department_name,
    statistics_year,
    attendance_rate,
    full_attendance_days,
    overtime_total_hours,
    punctuality_score,
    discipline_score,
    comprehensive_score,
    performance_level
FROM t_attendance_statistics
WHERE statistics_type = 'YEARLY'
  AND statistics_year = 2024
  AND comprehensive_score >= 90
  AND attendance_rate >= 95
  AND performance_level IN ('EXCELLENT', 'GOOD')
  AND deleted_flag = 0
ORDER BY comprehensive_score DESC, attendance_rate DESC;

-- =====================================================================================================================
-- 5. 维护建议
-- =====================================================================================================================

/*
维护建议：
1. 定期归档历史统计数据（建议保留近2年的统计数据，超过2年的数据归档到历史库）
2. 每月1日凌晨自动执行上月统计汇总任务，生成月度统计数据
3. 每季度第一天自动执行上季度统计汇总任务，生成季度统计数据
4. 每年1月1日自动执行上年度统计汇总任务，生成年度统计数据
5. 定期检查数据完整性，对is_complete=0或need_recount=1的记录进行重新统计
6. 监控统计数据生成失败情况，及时处理异常
7. 定期清理草稿状态且超过7天未确认的统计记录
8. 建立统计数据备份机制，防止数据丢失
9. 定期分析统计数据趋势，为管理决策提供支持
10. 优化统计算法，提升统计效率和准确性
*/

-- =====================================================================================================================
-- 6. 定时任务建议
-- =====================================================================================================================

/*
定时任务建议：

任务1: 月度统计生成（每月1日 00:00执行）
- 统计上月所有员工的考勤数据
- 计算出勤率、迟到早退次数、请假天数、加班时长等
- 生成月度统计记录，状态设为DRAFT
- 发送统计完成通知给部门经理

任务2: 季度统计生成（每季度第一天 00:00执行）
- 统计上季度所有员工的考勤数据
- 汇总3个月的统计数据
- 生成季度统计记录
- 发送季度报表给管理层

任务3: 年度统计生成（每年1月1日 00:00执行）
- 统计上年度所有员工的考勤数据
- 汇总12个月的统计数据
- 生成年度统计记录
- 发送年度报表给HR和管理层

任务4: 统计数据完整性检查（每日 02:00执行）
- 检查is_complete=0的记录
- 尝试重新统计缺失数据
- 记录无法修复的问题
- 发送异常报告给管理员

任务5: 草稿数据清理（每周日 03:00执行）
- 查找状态为DRAFT且创建时间超过7天的记录
- 发送提醒通知给相关负责人
- 超过30天未处理的自动删除（逻辑删除）

任务6: 统计数据归档（每月1日 04:00执行）
- 将2年前的统计数据归档到历史库
- 保留索引优化查询性能
- 生成归档报告

任务7: 绩效评分更新（每月2日 00:00执行）
- 根据最新的考勤规则重新计算绩效评分
- 更新punctuality_score、discipline_score、comprehensive_score
- 更新performance_level
- 发送评分变化通知
*/

-- =====================================================================================================================
-- 7. 数据完整性约束建议
-- =====================================================================================================================

/*
数据完整性约束建议：

1. 统计编码唯一性约束：
   - 确保statistics_code全局唯一
   - 格式：STAT-{用户ID}-{统计类型}-{统计周期}

2. 统计周期唯一性约束：
   - 确保同一员工的同一统计类型和周期只有一条记录
   - uk_user_type_period索引保障

3. 出勤率计算约束：
   - 确保出勤率 = (实际出勤天数 / 应出勤天数) * 100
   - 出勤率范围：0-100

4. 工作时长计算约束：
   - 确保总工作时长 = 正常工作时长 + 加班时长
   - 确保有效工作时长 = 总工作时长 - 迟到早退扣除时长

5. 请假天数汇总约束：
   - 确保leave_total_days = 各类请假天数之和
   - 各类请假天数均为非负数

6. 加班时长汇总约束：
   - 确保overtime_total_hours = weekday_overtime_hours + weekend_overtime_hours + holiday_overtime_hours

7. 薪资调整计算约束：
   - 确保net_salary_adjustment = overtime_pay_amount - deduct_salary_amount

8. 统计周期日期约束：
   - 确保statistics_end_date >= statistics_start_date
   - 月度统计：确保同一月份
   - 季度统计：确保同一季度
   - 年度统计：确保同一年度

9. 状态流转约束：
   - 状态流转：DRAFT → CONFIRMED → SUBMITTED → APPROVED → ARCHIVED
   - 只能按顺序流转，不能跳跃或逆向

10. 数据完整性标识约束：
    - is_complete=0时，必须填写incomplete_reason
    - need_recount=1时，应记录重算原因
*/

-- =====================================================================================================================
-- 8. 性能优化建议
-- =====================================================================================================================

/*
性能优化建议：

1. 索引优化：
   - 定期分析慢查询，优化索引设计
   - 考虑添加覆盖索引，减少回表查询
   - 定期重建索引，提升查询性能

2. 查询优化：
   - 避免SELECT *，只查询需要的字段
   - 使用LIMIT限制结果集大小
   - 避免深度分页，使用游标分页

3. 统计算法优化：
   - 使用增量统计，避免全量重算
   - 利用缓存存储中间计算结果
   - 并行处理多个员工的统计任务

4. 数据归档：
   - 定期归档历史数据，减小表数据量
   - 使用分区表技术，按年份或月份分区
   - 冷热数据分离，提升查询性能

5. 缓存策略：
   - 使用Redis缓存热点统计数据
   - 缓存部门汇总统计数据
   - 设置合理的缓存过期时间

6. 异步处理：
   - 统计任务异步执行，避免阻塞
   - 使用消息队列处理大批量统计
   - 提供统计进度查询接口

7. 数据库优化：
   - 定期优化表结构，减少碎片
   - 合理配置数据库参数
   - 使用连接池，提升连接效率

8. 监控告警：
   - 监控统计任务执行时间
   - 监控数据完整性
   - 设置异常告警机制
*/

-- =====================================================================================================================
-- 9. 业务规则说明
-- =====================================================================================================================

/*
业务规则说明：

1. 统计周期规则：
   - 月度统计：每月1日生成上月统计
   - 季度统计：每季度第一天生成上季度统计
   - 年度统计：每年1月1日生成上年度统计
   - 自定义周期：支持灵活设置统计周期

2. 出勤率计算规则：
   - 出勤率 = (实际出勤天数 / 应出勤天数) * 100
   - 实际出勤天数 = 正常打卡天数 + 请假天数（部分请假按天数计算）
   - 应出勤天数 = 统计周期内的工作日天数（根据排班计划）

3. 迟到早退统计规则：
   - 迟到：上班打卡时间晚于班次开始时间
   - 早退：下班打卡时间早于班次结束时间
   - 累计迟到/早退时长用于计算扣薪和绩效评分

4. 请假天数计算规则：
   - 全天请假：计1天
   - 半天请假：计0.5天
   - 小时请假：按小时数/8小时折算天数

5. 加班时长计算规则：
   - 工作日加班：下班后的工作时长
   - 周末加班：周六日的工作时长
   - 法定节假日加班：法定节假日的工作时长
   - 加班补偿：可转调休或发放加班费

6. 薪资调整计算规则：
   - 扣薪项：事假、旷工、迟到早退（按规则扣薪）
   - 加薪项：加班费（按加班时长和倍率计算）
   - 净调整 = 加班费 - 扣薪金额

7. 绩效评分计算规则：
   - 准时性评分 = 100 - (迟到次数 * 2 + 早退次数 * 2)
   - 纪律性评分 = 100 - (旷工次数 * 10 + 漏打卡次数 * 5)
   - 综合评分 = (准时性评分 * 0.4 + 纪律性评分 * 0.4 + 出勤率 * 0.2)

8. 绩效等级判定规则：
   - EXCELLENT（优秀）：综合评分 >= 90 且出勤率 >= 95%
   - GOOD（良好）：综合评分 >= 80 且出勤率 >= 90%
   - NORMAL（合格）：综合评分 >= 70 且出勤率 >= 85%
   - POOR（不合格）：综合评分 < 70 或出勤率 < 85%

9. 统计状态流转规则：
   - DRAFT：统计数据初次生成，可编辑
   - CONFIRMED：部门经理确认，不可编辑
   - SUBMITTED：提交给HR审批
   - APPROVED：HR审批通过，数据锁定
   - ARCHIVED：归档状态，长期保存

10. 数据完整性检查规则：
    - 检查打卡记录是否完整
    - 检查请假申请是否关联
    - 检查加班申请是否关联
    - 检查统计数据是否一致
    - 不完整数据标记need_recount=1
*/

-- =====================================================================================================================
-- 10. 注意事项
-- =====================================================================================================================

/*
注意事项：

1. 数据一致性：
   - 统计数据必须与考勤记录、异常申请数据保持一致
   - 使用事务确保数据完整性
   - 定期进行数据一致性校验

2. 并发控制：
   - 使用乐观锁(version)防止并发更新冲突
   - 统计任务执行时加锁，防止重复统计
   - 避免同一统计周期被重复生成

3. 性能考虑：
   - 统计任务在低峰期执行
   - 大批量统计使用批处理
   - 合理设置任务超时时间

4. 异常处理：
   - 统计失败自动重试
   - 记录详细的错误日志
   - 发送异常告警通知

5. 数据安全：
   - 统计数据涉及薪资信息，需严格权限控制
   - 敏感字段考虑加密存储
   - 完整的操作审计日志

6. 业务规则：
   - 统计规则可配置化
   - 支持按部门自定义统计规则
   - 历史统计数据不可随意修改

7. 用户体验：
   - 提供统计进度查询
   - 统计完成后及时通知
   - 支持统计数据导出

8. 扩展性：
   - 预留扩展字段
   - 支持自定义统计维度
   - 支持多租户隔离

9. 兼容性：
   - 兼容旧版统计规则
   - 支持规则版本管理
   - 平滑升级机制

10. 文档维护：
    - 及时更新统计规则文档
    - 记录业务变更历史
    - 提供操作手册和FAQ
*/

-- =====================================================================================================================
-- 文件结束
-- =====================================================================================================================
