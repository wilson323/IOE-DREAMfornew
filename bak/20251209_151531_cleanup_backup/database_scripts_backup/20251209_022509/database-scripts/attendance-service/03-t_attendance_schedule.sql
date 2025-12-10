-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 考勤服务 - 排班计划表 SQL脚本
-- =============================================
-- 功能说明：
--   排班计划表是考勤系统的核心数据表，负责管理所有员工的排班计划安排。
--   支持固定排班、轮班、临时调班等多种排班模式，与班次配置表关联。
--
-- 核心业务场景：
--   1. 固定排班：长期固定班次的员工排班（如行政人员标准工作日）
--   2. 轮班管理：需要多班次轮换的员工（如生产车间三班倒）
--   3. 临时调班：特殊情况的临时班次调整（如加班、替班）
--   4. 批量排班：按部门、按班组批量生成排班计划
--   5. 排班审批：排班计划的审批流程管理
--   6. 排班冲突检测：自动检测排班时间冲突
--
-- 企业级特性：
--   - 完整的排班生命周期管理（草稿→审批→生效→归档）
--   - 支持排班模板快速创建排班计划
--   - 支持排班冲突自动检测和告警
--   - 完整的排班审批流程
--   - 支持排班变更历史追溯
--   - 与考勤打卡数据联动验证
--
-- 作者: IOE-DREAM 开发团队
-- 创建时间: 2025-01-08
-- 版本: v1.0.0
-- =============================================

USE `ioedream_attendance_db`;

-- =============================================
-- 1. 删除已存在的表（开发环境）
-- =============================================
DROP TABLE IF EXISTS `t_attendance_schedule`;

-- =============================================
-- 2. 创建排班计划表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_attendance_schedule` (
    -- ========== 主键与业务标识 ==========
    `schedule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班计划ID，主键，自增',
    `schedule_code` VARCHAR(100) NOT NULL COMMENT '排班计划编码，格式：SCH_YYYYMMDD_序号，唯一标识',
    
    -- ========== 员工与部门信息 ==========
    `user_id` BIGINT NOT NULL COMMENT '用户ID，关联用户表',
    `user_name` VARCHAR(100) NOT NULL COMMENT '用户姓名，冗余字段便于查询',
    `employee_code` VARCHAR(50) NOT NULL COMMENT '工号，冗余字段便于导出',
    `department_id` BIGINT NOT NULL COMMENT '部门ID，关联部门表',
    `department_name` VARCHAR(200) NOT NULL COMMENT '部门名称，冗余字段便于查询',
    
    -- ========== 排班时间信息 ==========
    `schedule_date` DATE NOT NULL COMMENT '排班日期，格式：YYYY-MM-DD',
    `schedule_year` INT NOT NULL COMMENT '排班年份，便于年度统计',
    `schedule_month` INT NOT NULL COMMENT '排班月份，便于月度统计',
    `schedule_week` INT COMMENT '排班周数（一年中的第几周），便于周统计',
    `day_of_week` INT NOT NULL COMMENT '星期几：1-星期一, 7-星期日',
    
    -- ========== 班次信息 ==========
    `shift_id` BIGINT COMMENT '班次ID，关联班次配置表，NULL表示休息日',
    `shift_code` VARCHAR(100) COMMENT '班次编码，冗余字段便于查询',
    `shift_name` VARCHAR(200) COMMENT '班次名称，冗余字段便于显示',
    `shift_type` VARCHAR(50) COMMENT '班次类型：FIXED-固定班次, FLEXIBLE-弹性班次, ROTATION-轮班',
    
    -- ========== 排班类型与来源 ==========
    `schedule_type` VARCHAR(50) NOT NULL DEFAULT 'NORMAL' COMMENT '排班类型：NORMAL-正常排班, REST-休息日, OVERTIME-加班, TEMPORARY-临时调班, REPLACEMENT-替班',
    `schedule_source` VARCHAR(50) NOT NULL DEFAULT 'MANUAL' COMMENT '排班来源：MANUAL-手动创建, TEMPLATE-模板生成, BATCH-批量导入, AUTO-自动生成',
    `is_rest_day` TINYINT NOT NULL DEFAULT 0 COMMENT '是否休息日：0-工作日, 1-休息日（周末或节假日）',
    `is_holiday` TINYINT DEFAULT 0 COMMENT '是否法定节假日：0-否, 1-是',
    
    -- ========== 工作时长信息 ==========
    `scheduled_start_time` TIME COMMENT '计划上班时间，NULL表示休息日',
    `scheduled_end_time` TIME COMMENT '计划下班时间，NULL表示休息日',
    `scheduled_hours` DECIMAL(4,2) COMMENT '计划工作时长（小时），扣除午休时间',
    `actual_hours` DECIMAL(4,2) COMMENT '实际工作时长（小时），根据考勤记录计算',
    
    -- ========== 排班状态 ==========
    `schedule_status` VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT '排班状态：DRAFT-草稿, PENDING-待审批, APPROVED-已审批, PUBLISHED-已发布, EFFECTIVE-已生效, EXPIRED-已过期, CANCELLED-已取消',
    `is_locked` TINYINT DEFAULT 0 COMMENT '是否锁定：0-未锁定（可修改）, 1-已锁定（不可修改）',
    `lock_time` DATETIME COMMENT '锁定时间，排班生效后自动锁定',
    
    -- ========== 审批信息 ==========
    `approval_status` VARCHAR(50) COMMENT '审批状态：PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝',
    `approval_user_id` BIGINT COMMENT '审批人ID',
    `approval_user_name` VARCHAR(100) COMMENT '审批人姓名',
    `approval_time` DATETIME COMMENT '审批时间',
    `approval_remark` VARCHAR(500) COMMENT '审批备注',
    
    -- ========== 调班信息 ==========
    `is_adjusted` TINYINT DEFAULT 0 COMMENT '是否调班：0-正常排班, 1-已调班',
    `original_shift_id` BIGINT COMMENT '原班次ID，调班前的班次',
    `adjust_reason` VARCHAR(500) COMMENT '调班原因',
    `adjust_user_id` BIGINT COMMENT '调班操作人ID',
    `adjust_time` DATETIME COMMENT '调班时间',
    
    -- ========== 替班信息 ==========
    `is_replacement` TINYINT DEFAULT 0 COMMENT '是否替班：0-正常排班, 1-替班',
    `replacement_user_id` BIGINT COMMENT '被替班人ID',
    `replacement_user_name` VARCHAR(100) COMMENT '被替班人姓名',
    `replacement_reason` VARCHAR(500) COMMENT '替班原因',
    
    -- ========== 考勤关联信息 ==========
    `has_attendance` TINYINT DEFAULT 0 COMMENT '是否有考勤记录：0-无, 1-有',
    `attendance_status` VARCHAR(50) COMMENT '考勤状态：NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-缺卡, ABSENTEEISM-旷工',
    `sign_in_time` DATETIME COMMENT '实际签到时间',
    `sign_out_time` DATETIME COMMENT '实际签退时间',
    
    -- ========== 特殊标记 ==========
    `is_overtime` TINYINT DEFAULT 0 COMMENT '是否加班：0-否, 1-是',
    `overtime_hours` DECIMAL(4,2) COMMENT '加班时长（小时）',
    `is_night_shift` TINYINT DEFAULT 0 COMMENT '是否夜班：0-否, 1-是',
    
    -- ========== 备注与扩展 ==========
    `remark` VARCHAR(500) COMMENT '备注说明',
    `extended_attributes` JSON COMMENT '扩展属性（JSON格式），存储业务特定字段',
    
    -- ========== 审计字段（企业级标准） ==========
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除（逻辑删除）',
    
    -- ========== 主键与索引 ==========
    PRIMARY KEY (`schedule_id`),
    UNIQUE KEY `uk_schedule_code` (`schedule_code`),
    UNIQUE KEY `uk_user_date` (`user_id`, `schedule_date`, `deleted_flag`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_schedule_date` (`schedule_date`),
    INDEX `idx_shift_id` (`shift_id`),
    INDEX `idx_schedule_status` (`schedule_status`),
    INDEX `idx_approval_status` (`approval_status`),
    INDEX `idx_year_month` (`schedule_year`, `schedule_month`),
    INDEX `idx_create_time` (`create_time`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-排班计划表';

-- =============================================
-- 3. 初始化示例数据（企业级生产环境标准）
-- =============================================

-- 说明：
--   以下数据为2025年1月第一周的排班计划示例，涵盖多种排班场景：
--   - 固定工作日班次（行政人员）
--   - 三班倒轮班（生产车间）
--   - 休息日排班
--   - 临时加班
--   - 调班和替班

-- 3.1 行政人员固定排班（标准工作日，周一至周五）
INSERT INTO `t_attendance_schedule` (
    `schedule_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `schedule_date`, `schedule_year`, `schedule_month`, `schedule_week`, `day_of_week`,
    `shift_id`, `shift_code`, `shift_name`, `shift_type`,
    `schedule_type`, `schedule_source`, `is_rest_day`, `is_holiday`,
    `scheduled_start_time`, `scheduled_end_time`, `scheduled_hours`,
    `schedule_status`, `is_locked`, `approval_status`,
    `create_user_id`, `deleted_flag`
) VALUES
-- 行政部-张三-周一至周五标准班次
('SCH_20250106_001', 1001, '张三', 'EMP001', 2001, '行政部', '2025-01-06', 2025, 1, 2, 1, 1, 'SHIFT_STD_01', '标准工作日班次', 'FIXED', 'NORMAL', 'TEMPLATE', 0, 0, '09:00:00', '18:00:00', 8.00, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250107_001', 1001, '张三', 'EMP001', 2001, '行政部', '2025-01-07', 2025, 1, 2, 2, 1, 'SHIFT_STD_01', '标准工作日班次', 'FIXED', 'NORMAL', 'TEMPLATE', 0, 0, '09:00:00', '18:00:00', 8.00, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250108_001', 1001, '张三', 'EMP001', 2001, '行政部', '2025-01-08', 2025, 1, 2, 3, 1, 'SHIFT_STD_01', '标准工作日班次', 'FIXED', 'NORMAL', 'TEMPLATE', 0, 0, '09:00:00', '18:00:00', 8.00, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250109_001', 1001, '张三', 'EMP001', 2001, '行政部', '2025-01-09', 2025, 1, 2, 4, 1, 'SHIFT_STD_01', '标准工作日班次', 'FIXED', 'NORMAL', 'TEMPLATE', 0, 0, '09:00:00', '18:00:00', 8.00, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250110_001', 1001, '张三', 'EMP001', 2001, '行政部', '2025-01-10', 2025, 1, 2, 5, 1, 'SHIFT_STD_01', '标准工作日班次', 'FIXED', 'NORMAL', 'TEMPLATE', 0, 0, '09:00:00', '18:00:00', 8.00, 'PUBLISHED', 1, 'APPROVED', 1, 0);

-- 3.2 生产车间轮班（早中夜三班倒）
INSERT INTO `t_attendance_schedule` (
    `schedule_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `schedule_date`, `schedule_year`, `schedule_month`, `schedule_week`, `day_of_week`,
    `shift_id`, `shift_code`, `shift_name`, `shift_type`,
    `schedule_type`, `schedule_source`, `is_rest_day`, `is_holiday`,
    `scheduled_start_time`, `scheduled_end_time`, `scheduled_hours`, `is_night_shift`,
    `schedule_status`, `is_locked`, `approval_status`,
    `create_user_id`, `deleted_flag`
) VALUES
-- 生产部-李四-三班倒（早班→中班→夜班→休息）
('SCH_20250106_002', 1002, '李四', 'EMP002', 2002, '生产部', '2025-01-06', 2025, 1, 2, 1, 2, 'SHIFT_EARLY_01', '早班', 'ROTATION', 'NORMAL', 'AUTO', 0, 0, '06:00:00', '14:00:00', 8.00, 0, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250107_002', 1002, '李四', 'EMP002', 2002, '生产部', '2025-01-07', 2025, 1, 2, 2, 3, 'SHIFT_MID_01', '中班', 'ROTATION', 'NORMAL', 'AUTO', 0, 0, '14:00:00', '22:00:00', 8.00, 0, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250108_002', 1002, '李四', 'EMP002', 2002, '生产部', '2025-01-08', 2025, 1, 2, 3, 4, 'SHIFT_NIGHT_01', '夜班', 'ROTATION', 'NORMAL', 'AUTO', 0, 0, '22:00:00', '06:00:00', 8.00, 1, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250109_002', 1002, '李四', 'EMP002', 2002, '生产部', '2025-01-09', 2025, 1, 2, 4, NULL, NULL, NULL, NULL, 'REST', 'AUTO', 1, 0, NULL, NULL, 0.00, 0, 'PUBLISHED', 1, 'APPROVED', 1, 0);

-- 3.3 周末休息日排班
INSERT INTO `t_attendance_schedule` (
    `schedule_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `schedule_date`, `schedule_year`, `schedule_month`, `schedule_week`, `day_of_week`,
    `shift_id`, `shift_code`, `shift_name`, `shift_type`,
    `schedule_type`, `schedule_source`, `is_rest_day`, `is_holiday`,
    `scheduled_start_time`, `scheduled_end_time`, `scheduled_hours`,
    `schedule_status`, `is_locked`, `approval_status`,
    `create_user_id`, `deleted_flag`
) VALUES
-- 行政部-张三-周末休息
('SCH_20250111_001', 1001, '张三', 'EMP001', 2001, '行政部', '2025-01-11', 2025, 1, 2, 6, NULL, NULL, NULL, NULL, 'REST', 'TEMPLATE', 1, 0, NULL, NULL, 0.00, 'PUBLISHED', 1, 'APPROVED', 1, 0),
('SCH_20250112_001', 1001, '张三', 'EMP001', 2001, '行政部', '2025-01-12', 2025, 1, 2, 7, NULL, NULL, NULL, NULL, 'REST', 'TEMPLATE', 1, 0, NULL, NULL, 0.00, 'PUBLISHED', 1, 'APPROVED', 1, 0);

-- 3.4 临时加班排班
INSERT INTO `t_attendance_schedule` (
    `schedule_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `schedule_date`, `schedule_year`, `schedule_month`, `schedule_week`, `day_of_week`,
    `shift_id`, `shift_code`, `shift_name`, `shift_type`,
    `schedule_type`, `schedule_source`, `is_rest_day`, `is_overtime`,
    `scheduled_start_time`, `scheduled_end_time`, `scheduled_hours`, `overtime_hours`,
    `schedule_status`, `approval_status`, `approval_user_id`, `approval_user_name`, `approval_time`,
    `create_user_id`, `deleted_flag`
) VALUES
-- 技术部-王五-周六临时加班
('SCH_20250111_003', 1003, '王五', 'EMP003', 2003, '技术部', '2025-01-11', 2025, 1, 2, 6, 5, 'SHIFT_OVERTIME_01', '加班班次', 'FIXED', 'OVERTIME', 'MANUAL', 0, 1, '09:00:00', '18:00:00', 8.00, 8.00, 'APPROVED', 'APPROVED', 9001, '部门经理', '2025-01-10 15:30:00', 1003, 0);

-- 3.5 临时调班示例
INSERT INTO `t_attendance_schedule` (
    `schedule_code`, `user_id`, `user_name`, `employee_code`, `department_id`, `department_name`,
    `schedule_date`, `schedule_year`, `schedule_month`, `schedule_week`, `day_of_week`,
    `shift_id`, `shift_code`, `shift_name`, `shift_type`,
    `schedule_type`, `schedule_source`, `is_rest_day`,
    `scheduled_start_time`, `scheduled_end_time`, `scheduled_hours`,
    `schedule_status`, `is_adjusted`, `original_shift_id`, `adjust_reason`, `adjust_user_id`, `adjust_time`,
    `approval_status`,
    `create_user_id`, `deleted_flag`
) VALUES
-- 销售部-赵六-因客户拜访临时调整为弹性班次
('SCH_20250108_003', 1004, '赵六', 'EMP004', 2004, '销售部', '2025-01-08', 2025, 1, 2, 3, 6, 'SHIFT_FLEX_01', '弹性工作制', 'FLEXIBLE', 'TEMPORARY', 'MANUAL', 0, '08:00:00', '20:00:00', 8.00, 'APPROVED', 1, 1, '客户拜访需要弹性安排时间', 1004, '2025-01-07 16:00:00', 'APPROVED', 1004, 0);

-- =============================================
-- 4. 索引优化说明
-- =============================================
-- 索引设计原则：
--   1. uk_schedule_code：排班计划编码唯一索引，保证编码唯一性
--   2. uk_user_date：用户+日期联合唯一索引，防止同一天重复排班
--   3. idx_user_id：用户ID索引，优化按员工查询排班
--   4. idx_department_id：部门ID索引，优化按部门查询排班
--   5. idx_schedule_date：排班日期索引，优化按日期查询
--   6. idx_shift_id：班次ID索引，优化按班次查询
--   7. idx_schedule_status：排班状态索引，优化按状态过滤
--   8. idx_approval_status：审批状态索引，优化审批查询
--   9. idx_year_month：年月复合索引，优化月度统计查询
--   10. idx_create_time：创建时间索引，优化时间范围查询

-- =============================================
-- 5. 使用示例
-- =============================================

-- 示例1: 查询员工本月的排班计划
-- SELECT * FROM t_attendance_schedule
-- WHERE user_id = 1001
--   AND schedule_year = 2025
--   AND schedule_month = 1
--   AND deleted_flag = 0
-- ORDER BY schedule_date ASC;

-- 示例2: 查询部门本周的排班计划
-- SELECT * FROM t_attendance_schedule
-- WHERE department_id = 2001
--   AND schedule_week = 2
--   AND schedule_year = 2025
--   AND deleted_flag = 0
-- ORDER BY schedule_date ASC, user_name ASC;

-- 示例3: 查询待审批的排班计划
-- SELECT * FROM t_attendance_schedule
-- WHERE approval_status = 'PENDING'
--   AND deleted_flag = 0
-- ORDER BY create_time ASC;

-- 示例4: 统计部门本月工作日和休息日天数
-- SELECT 
--     department_id,
--     department_name,
--     SUM(CASE WHEN is_rest_day = 0 THEN 1 ELSE 0 END) AS work_days,
--     SUM(CASE WHEN is_rest_day = 1 THEN 1 ELSE 0 END) AS rest_days
-- FROM t_attendance_schedule
-- WHERE schedule_year = 2025
--   AND schedule_month = 1
--   AND deleted_flag = 0
-- GROUP BY department_id, department_name;

-- 示例5: 查询加班排班记录
-- SELECT * FROM t_attendance_schedule
-- WHERE is_overtime = 1
--   AND schedule_year = 2025
--   AND schedule_month = 1
--   AND deleted_flag = 0
-- ORDER BY schedule_date ASC;

-- =============================================
-- 6. 维护建议
-- =============================================
-- 1. 定期清理已过期的排班计划（保留近2年数据）
-- 2. 定期归档历史排班数据到历史表
-- 3. 监控排班冲突和异常排班数据
-- 4. 定期检查排班审批流程的及时性
-- 5. 定期同步考勤打卡数据到排班计划
-- 6. 定期优化索引性能，确保查询效率

-- =============================================
-- 7. 定时任务建议
-- =============================================
-- 1. 每日凌晨01:00：生成次日的自动排班计划
-- 2. 每周一凌晨02:00：生成下周的轮班计划
-- 3. 每月最后一天23:00：生成下月的排班模板
-- 4. 每日22:00：同步当日考勤数据到排班计划
-- 5. 每周日23:00：统计本周排班执行情况
-- 6. 每月1日00:00：归档上月排班数据

-- =============================================
-- SQL脚本结束
-- =============================================
