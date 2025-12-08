-- ====================================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 考勤服务 - 班次配置表
-- ====================================================
-- 文件: 02-t_attendance_shift.sql
-- 版本: v1.0.0
-- 创建日期: 2025-12-08
-- 描述: 班次配置表，定义各种班次类型和时间安排
-- ====================================================

USE `ioedream_attendance_db`;

-- ====================================================
-- 表: t_attendance_shift - 班次配置表
-- ====================================================
-- 功能说明:
-- 1. 定义各种班次类型（标准班、早班、中班、夜班、弹性班等）
-- 2. 配置班次时间安排（上班时间、下班时间、午休时间等）
-- 3. 支持弹性工作制和特殊班次
-- 4. 支持班次有效期和适用范围
-- 5. 支持班次关联的考勤规则
-- ====================================================

CREATE TABLE IF NOT EXISTS `t_attendance_shift` (
    -- ====================================================
    -- 主键和编码
    -- ====================================================
    `shift_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '班次ID',
    `shift_code` VARCHAR(100) NOT NULL COMMENT '班次编码，唯一标识',
    `shift_name` VARCHAR(200) NOT NULL COMMENT '班次名称',
    
    -- ====================================================
    -- 班次基本信息
    -- ====================================================
    `shift_type` VARCHAR(50) NOT NULL COMMENT '班次类型：FIXED-固定班次, FLEXIBLE-弹性班次, ROTATION-轮班, FLEX_TIME-弹性时间段',
    `shift_category` VARCHAR(50) COMMENT '班次分类：STANDARD-标准班, MORNING-早班, AFTERNOON-中班, NIGHT-夜班, FLEXIBLE-弹性班, CUSTOM-自定义',
    `shift_group` VARCHAR(100) COMMENT '班次组：用于轮班分组',
    `shift_color` VARCHAR(20) COMMENT '班次颜色标识（前端展示用）',
    
    -- ====================================================
    -- 时间安排
    -- ====================================================
    `work_start_time` TIME NOT NULL COMMENT '上班时间',
    `work_end_time` TIME NOT NULL COMMENT '下班时间',
    `is_cross_day` TINYINT DEFAULT 0 COMMENT '是否跨天：0-不跨天, 1-跨天（如夜班22:00-次日06:00）',
    
    -- 午休时间配置
    `has_lunch_break` TINYINT DEFAULT 1 COMMENT '是否有午休：0-无午休, 1-有午休',
    `lunch_start_time` TIME COMMENT '午休开始时间',
    `lunch_end_time` TIME COMMENT '午休结束时间',
    `lunch_duration` INT COMMENT '午休时长（分钟）',
    
    -- 弹性时间配置
    `flexible_minutes` INT DEFAULT 0 COMMENT '弹性时间（分钟）：允许提前/延后打卡的时间',
    `flexible_start_time` TIME COMMENT '弹性上班开始时间',
    `flexible_end_time` TIME COMMENT '弹性下班结束时间',
    
    -- 工作时长
    `scheduled_hours` DECIMAL(4,2) NOT NULL COMMENT '计划工作时长（小时）',
    `actual_hours` DECIMAL(4,2) COMMENT '实际工作时长（小时，扣除午休）',
    
    -- ====================================================
    -- 考勤规则
    -- ====================================================
    `late_threshold` INT DEFAULT 0 COMMENT '迟到阈值（分钟）：超过此时间视为迟到',
    `early_threshold` INT DEFAULT 0 COMMENT '早退阈值（分钟）：提前此时间离开视为早退',
    `absent_threshold` INT DEFAULT 0 COMMENT '缺卡阈值（分钟）：未打卡超过此时间视为缺卡',
    
    -- 加班规则
    `overtime_enabled` TINYINT DEFAULT 1 COMMENT '是否允许加班：0-不允许, 1-允许',
    `overtime_start_time` TIME COMMENT '加班开始时间（默认下班后）',
    `overtime_min_duration` INT DEFAULT 60 COMMENT '最小加班时长（分钟）',
    
    -- ====================================================
    -- 适用范围
    -- ====================================================
    `apply_departments` JSON COMMENT '适用部门（JSON数组格式）',
    `apply_positions` JSON COMMENT '适用岗位（JSON数组格式）',
    `apply_employee_types` JSON COMMENT '适用员工类型（JSON数组格式）',
    `excluded_employees` JSON COMMENT '排除员工（JSON数组格式）',
    
    -- 时间范围
    `effective_date` DATE COMMENT '生效日期',
    `expire_date` DATE COMMENT '失效日期',
    `week_cycle` VARCHAR(50) COMMENT '周循环：MON-FRI-工作日, SAT-SUN-周末, DAILY-每日',
    `apply_weekdays` VARCHAR(20) COMMENT '适用星期（1-7，逗号分隔）',
    
    -- ====================================================
    -- 节假日处理
    -- ====================================================
    `holiday_policy` VARCHAR(50) COMMENT '节假日政策：NORMAL-正常上班, REST-休息, HALF-半天, OVERTIME-加班',
    `holiday_overtime_multiplier` DECIMAL(3,2) DEFAULT 1.00 COMMENT '节假日加班倍数',
    
    -- ====================================================
    -- 班次关联
    -- ====================================================
    `related_shifts` JSON COMMENT '关联班次（JSON数组格式）',
    `rotation_sequence` INT COMMENT '轮班顺序',
    `next_shift_id` BIGINT COMMENT '下一班次ID（轮班用）',
    
    -- ====================================================
    -- 扩展配置
    -- ====================================================
    `shift_config` JSON COMMENT '班次扩展配置（JSON格式）：特殊规则、自定义字段等',
    `extended_attributes` JSON COMMENT '扩展属性（JSON格式）：天气、季节等',
    
    -- ====================================================
    -- 状态和控制
    -- ====================================================
    `shift_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '班次状态：ACTIVE-激活, INACTIVE-停用, DRAFT-草稿',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认班次：0-否, 1-是',
    `priority` INT DEFAULT 10 COMMENT '优先级：数值越小优先级越高',
    `allow_override` TINYINT DEFAULT 1 COMMENT '是否允许覆盖：0-不允许, 1-允许',
    
    -- ====================================================
    -- 统计信息
    -- ====================================================
    `used_count` INT DEFAULT 0 COMMENT '使用次数',
    `last_used_time` DATETIME COMMENT '最后使用时间',
    
    -- ====================================================
    -- 描述和备注
    -- ====================================================
    `description` VARCHAR(500) COMMENT '班次描述',
    `remark` VARCHAR(500) COMMENT '备注',
    
    -- ====================================================
    -- 审计字段（标准字段，所有表必须包含）
    -- ====================================================
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- ====================================================
    -- 索引定义
    -- ====================================================
    PRIMARY KEY (`shift_id`),
    UNIQUE KEY `uk_shift_code` (`shift_code`),
    INDEX `idx_shift_name` (`shift_name`),
    INDEX `idx_shift_type` (`shift_type`),
    INDEX `idx_shift_category` (`shift_category`),
    INDEX `idx_shift_status` (`shift_status`),
    INDEX `idx_is_default` (`is_default`),
    INDEX `idx_effective_date` (`effective_date`),
    INDEX `idx_expire_date` (`expire_date`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班次配置表';

-- ====================================================
-- 索引优化说明
-- ====================================================
-- 1. uk_shift_code: 班次编码唯一索引，确保唯一性
-- 2. idx_shift_name: 班次名称索引，便于模糊查询
-- 3. idx_shift_type: 班次类型索引，分类查询
-- 4. idx_shift_category: 班次分类索引，快速筛选
-- 5. idx_shift_status: 班次状态索引，状态查询
-- 6. idx_is_default: 默认班次索引，快速获取默认班次
-- 7. idx_effective_date: 生效日期索引，时间范围查询
-- 8. idx_expire_date: 失效日期索引，过期判断
-- 9. idx_create_time: 创建时间索引，按时间排序
-- 10. idx_deleted_flag: 删除标记索引，软删除查询

-- ====================================================
-- 初始化数据：标准班次配置
-- ====================================================

-- 1. 标准工作日班次
INSERT INTO `t_attendance_shift` (
    `shift_code`, `shift_name`, `shift_type`, `shift_category`,
    `work_start_time`, `work_end_time`, `is_cross_day`,
    `has_lunch_break`, `lunch_start_time`, `lunch_end_time`, `lunch_duration`,
    `flexible_minutes`, `flexible_start_time`, `flexible_end_time`,
    `scheduled_hours`, `actual_hours`,
    `late_threshold`, `early_threshold`, `absent_threshold`,
    `overtime_enabled`, `overtime_start_time`, `overtime_min_duration`,
    `week_cycle`, `apply_weekdays`,
    `holiday_policy`, `holiday_overtime_multiplier`,
    `shift_status`, `is_default`, `priority`,
    `description`, `create_user_id`, `deleted_flag`
) VALUES (
    'SHIFT_STD_01', '标准工作日班次', 'FIXED', 'STANDARD',
    '09:00:00', '18:00:00', 0,
    1, '12:00:00', '13:00:00', 60,
    30, '08:30:00', '18:30:00',
    8.00, 7.00,
    30, 30, 120,
    1, '18:00:00', 60,
    'MON-FRI', '1,2,3,4,5',
    'REST', 2.00,
    'ACTIVE', 1, 1,
    '标准工作日班次，9:00-18:00，午休1小时', 1, 0
);

-- 2. 早班
INSERT INTO `t_attendance_shift` (
    `shift_code`, `shift_name`, `shift_type`, `shift_category`,
    `work_start_time`, `work_end_time`, `is_cross_day`,
    `has_lunch_break`, `lunch_start_time`, `lunch_end_time`, `lunch_duration`,
    `scheduled_hours`, `actual_hours`,
    `late_threshold`, `early_threshold`, `absent_threshold`,
    `week_cycle`, `apply_weekdays`,
    `shift_status`, `priority`,
    `description`, `create_user_id`, `deleted_flag`
) VALUES (
    'SHIFT_MORNING_01', '早班', 'FIXED', 'MORNING',
    '08:00:00', '17:00:00', 0,
    1, '12:00:00', '13:00:00', 60,
    8.00, 7.00,
    15, 15, 60,
    'MON-FRI', '1,2,3,4,5',
    'ACTIVE', 5,
    '早班，8:00-17:00，午休1小时', 1, 0
);

-- 3. 中班
INSERT INTO `t_attendance_shift` (
    `shift_code`, `shift_name`, `shift_type`, `shift_category`,
    `work_start_time`, `work_end_time`, `is_cross_day`,
    `has_lunch_break`, `lunch_start_time`, `lunch_end_time`, `lunch_duration`,
    `scheduled_hours`, `actual_hours`,
    `late_threshold`, `early_threshold`, `absent_threshold`,
    `week_cycle`, `apply_weekdays`,
    `shift_status`, `priority`,
    `description`, `create_user_id`, `deleted_flag`
) VALUES (
    'SHIFT_AFTERNOON_01', '中班', 'FIXED', 'AFTERNOON',
    '14:00:00', '23:00:00', 0,
    1, '18:00:00', '19:00:00', 60,
    8.00, 7.00,
    15, 15, 60,
    'MON-FRI', '1,2,3,4,5',
    'ACTIVE', 5,
    '中班，14:00-23:00，午休1小时', 1, 0
);

-- 4. 夜班
INSERT INTO `t_attendance_shift` (
    `shift_code`, `shift_name`, `shift_type`, `shift_category`,
    `work_start_time`, `work_end_time`, `is_cross_day`,
    `has_lunch_break`, `lunch_start_time`, `lunch_end_time`, `lunch_duration`,
    `scheduled_hours`, `actual_hours`,
    `late_threshold`, `early_threshold`, `absent_threshold`,
    `week_cycle`, `apply_weekdays`,
    `shift_status`, `priority`,
    `description`, `create_user_id`, `deleted_flag`
) VALUES (
    'SHIFT_NIGHT_01', '夜班', 'FIXED', 'NIGHT',
    '22:00:00', '06:00:00', 1,
    1, '02:00:00', '03:00:00', 60,
    8.00, 7.00,
    30, 30, 120,
    'MON-FRI', '1,2,3,4,5',
    'ACTIVE', 10,
    '夜班，22:00-次日06:00，午休1小时', 1, 0
);

-- 5. 弹性班次
INSERT INTO `t_attendance_shift` (
    `shift_code`, `shift_name`, `shift_type`, `shift_category`,
    `work_start_time`, `work_end_time`, `is_cross_day`,
    `flexible_minutes`, `flexible_start_time`, `flexible_end_time`,
    `scheduled_hours`, `actual_hours`,
    `late_threshold`, `early_threshold`, `absent_threshold`,
    `week_cycle`, `apply_weekdays`,
    `shift_status`, `priority`,
    `description`, `create_user_id`, `deleted_flag`
) VALUES (
    'SHIFT_FLEX_01', '弹性班次', 'FLEXIBLE', 'FLEXIBLE',
    '09:00:00', '18:00:00', 0,
    120, '07:00:00', '20:00:00',
    8.00, 8.00,
    0, 0, 240,
    'MON-FRI', '1,2,3,4,5',
    'ACTIVE', 15,
    '弹性班次，核心时间9:00-18:00，允许2小时弹性', 1, 0
);

-- 6. 周末值班班次
INSERT INTO `t_attendance_shift` (
    `shift_code`, `shift_name`, `shift_type`, `shift_category`,
    `work_start_time`, `work_end_time`, `is_cross_day`,
    `has_lunch_break`, `lunch_start_time`, `lunch_end_time`, `lunch_duration`,
    `scheduled_hours`, `actual_hours`,
    `late_threshold`, `early_threshold`, `absent_threshold`,
    `week_cycle`, `apply_weekdays`,
    `holiday_policy`, `holiday_overtime_multiplier`,
    `shift_status`, `priority`,
    `description`, `create_user_id`, `deleted_flag`
) VALUES (
    'SHIFT_WEEKEND_01', '周末值班班次', 'FIXED', 'CUSTOM',
    '09:00:00', '17:00:00', 0,
    1, '12:00:00', '13:00:00', 60,
    8.00, 7.00,
    30, 30, 120,
    'SAT-SUN', '6,7',
    'OVERTIME', 1.50,
    'ACTIVE', 20,
    '周末值班班次，9:00-17:00，午休1小时', 1, 0
);

-- ====================================================
-- 使用示例
-- ====================================================

-- 查询1：获取所有激活的班次
-- SELECT * FROM t_attendance_shift
-- WHERE shift_status = 'ACTIVE'
--   AND deleted_flag = 0
-- ORDER BY priority, shift_name;

-- 查询2：根据班次编码获取班次信息
-- SELECT * FROM t_attendance_shift
-- WHERE shift_code = 'SHIFT_STD_01'
--   AND deleted_flag = 0;

-- 查询3：获取默认班次
-- SELECT * FROM t_attendance_shift
-- WHERE is_default = 1
--   AND shift_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 查询4：获取适用于特定日期的班次
-- SELECT * FROM t_attendance_shift
-- WHERE shift_status = 'ACTIVE'
--   AND (effective_date IS NULL OR effective_date <= '2025-01-08')
--   AND (expire_date IS NULL OR expire_date >= '2025-01-08')
--   AND deleted_flag = 0;

-- 查询5：根据班次类型统计班次数量
-- SELECT shift_type, COUNT(*) as shift_count
-- FROM t_attendance_shift
-- WHERE shift_status = 'ACTIVE'
--   AND deleted_flag = 0
-- GROUP BY shift_type;

-- 查询6：获取适用于特定星期的班次
-- SELECT * FROM t_attendance_shift
-- WHERE shift_status = 'ACTIVE'
--   AND (apply_weekdays IS NULL OR apply_weekdays LIKE '%1%')
--   AND deleted_flag = 0;

-- 查询7：获取加班允许的班次
-- SELECT shift_code, shift_name, overtime_start_time, overtime_min_duration
-- FROM t_attendance_shift
-- WHERE overtime_enabled = 1
--   AND shift_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- ====================================================
-- 维护建议
-- ====================================================
-- 1. 定期检查班次的有效期，及时更新或停用过期班次
-- 2. 定期分析班次使用情况，优化班次配置
-- 3. 建立班次变更审批流程，确保配置准确性
-- 4. 定期备份班次配置，防止误操作丢失
-- 5. 建立班次版本管理机制，支持历史追溯

-- ====================================================
-- 定时任务建议
-- ====================================================
-- 1. 每日凌晨：检查班次有效期，自动停用过期班次
-- 2. 每周一凌晨：统计上周班次使用情况，生成班次分析报告
-- 3. 每月1日凌晨：生成月度班次使用统计报告
-- 4. 每季度第一天：归档历史班次配置

-- ====================================================
-- 文件结束
-- ====================================================
