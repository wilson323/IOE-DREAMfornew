-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 时间段配置表 (t_access_time_period)
-- =============================================
-- 功能说明: 时间段配置表，管理门禁系统的各类时间段规则
-- 业务场景: 
--   1. 门禁通行时间段控制
--   2. 考勤打卡时间段
--   3. 访客预约时间段
--   4. 消费服务时间段
-- 企业级特性:
--   - 支持多种时间段类型
--   - 支持灵活的时间段组合
--   - 支持时间段优先级
--   - 支持时间段模板复用
-- =============================================

USE `ioedream_access_db`;

-- =============================================
-- 表结构定义
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_time_period` (
    -- 主键ID
    `time_period_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '时间段ID',
    
    -- 时间段基本信息
    `time_period_code` VARCHAR(100) NOT NULL COMMENT '时间段编码（唯一标识）',
    `time_period_name` VARCHAR(200) NOT NULL COMMENT '时间段名称',
    `time_period_type` VARCHAR(50) NOT NULL COMMENT '时间段类型：ACCESS-门禁, ATTENDANCE-考勤, VISITOR-访客, CONSUME-消费, CUSTOM-自定义',
    `time_period_desc` VARCHAR(500) COMMENT '时间段描述',
    
    -- 时间段配置
    `start_time` TIME NOT NULL COMMENT '开始时间（如：08:00:00）',
    `end_time` TIME NOT NULL COMMENT '结束时间（如：18:00:00）',
    `duration_minutes` INT COMMENT '时长（分钟，自动计算）',
    `is_cross_day` TINYINT DEFAULT 0 COMMENT '是否跨天：0-不跨天, 1-跨天（如23:00-02:00）',
    
    -- 星期配置
    `monday_enabled` TINYINT DEFAULT 1 COMMENT '周一启用：0-禁用, 1-启用',
    `tuesday_enabled` TINYINT DEFAULT 1 COMMENT '周二启用：0-禁用, 1-启用',
    `wednesday_enabled` TINYINT DEFAULT 1 COMMENT '周三启用：0-禁用, 1-启用',
    `thursday_enabled` TINYINT DEFAULT 1 COMMENT '周四启用：0-禁用, 1-启用',
    `friday_enabled` TINYINT DEFAULT 1 COMMENT '周五启用：0-禁用, 1-启用',
    `saturday_enabled` TINYINT DEFAULT 0 COMMENT '周六启用：0-禁用, 1-启用',
    `sunday_enabled` TINYINT DEFAULT 0 COMMENT '周日启用：0-禁用, 1-启用',
    
    -- 节假日配置
    `holiday_mode` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '节假日模式：NORMAL-正常启用, DISABLE-节假日禁用, ONLY_HOLIDAY-仅节假日启用',
    `exclude_holidays` TEXT COMMENT '排除节假日ID列表（JSON数组）：["1", "2", "3"]',
    `include_holidays` TEXT COMMENT '包含节假日ID列表（JSON数组）：["4", "5", "6"]',
    
    -- 时间段属性
    `priority_level` INT DEFAULT 5 COMMENT '优先级：1-最低, 5-普通, 10-最高',
    `is_template` TINYINT DEFAULT 0 COMMENT '是否模板：0-普通时间段, 1-可复用模板',
    `template_category` VARCHAR(50) COMMENT '模板分类：WORK-工作时间, BREAK-休息时间, OVERTIME-加班时间',
    
    -- 弹性时间配置
    `allow_early_in` INT DEFAULT 0 COMMENT '允许提前（分钟，0表示不允许）',
    `allow_late_out` INT DEFAULT 0 COMMENT '允许延后（分钟，0表示不允许）',
    `tolerance_minutes` INT DEFAULT 0 COMMENT '容差时间（分钟，用于考勤打卡）',
    
    -- 联动配置
    `auto_unlock` TINYINT DEFAULT 0 COMMENT '自动解锁：0-不自动, 1-时间段开始时自动解锁',
    `auto_lock` TINYINT DEFAULT 0 COMMENT '自动上锁：0-不自动, 1-时间段结束时自动上锁',
    `notification_enabled` TINYINT DEFAULT 0 COMMENT '通知启用：0-禁用, 1-启用（时间段开始/结束前通知）',
    `notification_minutes_before` INT DEFAULT 10 COMMENT '提前通知时间（分钟）',
    
    -- 状态信息
    `time_period_status` TINYINT DEFAULT 1 COMMENT '时间段状态：0-禁用, 1-启用',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-停用, 1-启用',
    
    -- 使用统计
    `usage_count` INT DEFAULT 0 COMMENT '使用次数统计',
    `last_used_time` DATETIME COMMENT '最后使用时间',
    
    -- 扩展字段
    `extend_json` JSON COMMENT '扩展配置（JSON格式）',
    `remark` VARCHAR(500) COMMENT '备注说明',
    
    -- 审计字段（强制标准）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除, 1-已删除',
    
    -- 主键约束
    PRIMARY KEY (`time_period_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_time_period_code` (`time_period_code`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_time_period_type` (`time_period_type`) USING BTREE,
    INDEX `idx_time_range` (`start_time`, `end_time`) USING BTREE,
    INDEX `idx_is_template` (`is_template`, `template_category`) USING BTREE,
    INDEX `idx_priority` (`priority_level` DESC) USING BTREE,
    INDEX `idx_status` (`time_period_status`, `enabled_flag`) USING BTREE,
    INDEX `idx_create_time` (`create_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时间段配置表';

-- =============================================
-- 初始化数据（企业级标准时间段）
-- =============================================

-- 1. 标准工作日时间段（08:00-18:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`, `is_template`, `template_category`,
    `allow_early_in`, `allow_late_out`, `tolerance_minutes`,
    `auto_unlock`, `auto_lock`, `notification_enabled`, `notification_minutes_before`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_WORKDAY_STANDARD', '标准工作日时间段', 'ACCESS', '标准工作日门禁通行时间：08:00-18:00',
    '08:00:00', '18:00:00', 600, 0,
    1, 1, 1, 1, 1, 0, 0,
    'DISABLE', 8, 1, 'WORK',
    30, 60, 15,
    1, 0, 1, 10,
    1, 1, '标准工作日时间段，节假日禁用'
);

-- 2. 早班时间段（06:00-14:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`, `is_template`, `template_category`,
    `allow_early_in`, `allow_late_out`, `tolerance_minutes`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_EARLY_SHIFT', '早班时间段', 'ATTENDANCE', '早班考勤时间：06:00-14:00',
    '06:00:00', '14:00:00', 480, 0,
    1, 1, 1, 1, 1, 0, 0,
    'NORMAL', 7, 1, 'WORK',
    30, 30, 10,
    1, 1, '早班时间段'
);

-- 3. 中班时间段（14:00-22:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`, `is_template`, `template_category`,
    `allow_early_in`, `allow_late_out`, `tolerance_minutes`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_MIDDLE_SHIFT', '中班时间段', 'ATTENDANCE', '中班考勤时间：14:00-22:00',
    '14:00:00', '22:00:00', 480, 0,
    1, 1, 1, 1, 1, 0, 0,
    'NORMAL', 7, 1, 'WORK',
    30, 30, 10,
    1, 1, '中班时间段'
);

-- 4. 夜班时间段（22:00-06:00，跨天）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`, `is_template`, `template_category`,
    `allow_early_in`, `allow_late_out`, `tolerance_minutes`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_NIGHT_SHIFT', '夜班时间段', 'ATTENDANCE', '夜班考勤时间：22:00-次日06:00',
    '22:00:00', '06:00:00', 480, 1,
    1, 1, 1, 1, 1, 0, 0,
    'NORMAL', 9, 1, 'WORK',
    30, 30, 10,
    1, 1, '夜班时间段（跨天）'
);

-- 5. 午休时间段（12:00-14:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`, `is_template`, `template_category`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_LUNCH_BREAK', '午休时间段', 'CUSTOM', '午休时间：12:00-14:00',
    '12:00:00', '14:00:00', 120, 0,
    1, 1, 1, 1, 1, 0, 0,
    'DISABLE', 5, 1, 'BREAK',
    1, 1, '午休时间段，节假日禁用'
);

-- 6. 访客预约标准时间段（09:00-17:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`,
    `notification_enabled`, `notification_minutes_before`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_VISITOR_STANDARD', '访客预约标准时间段', 'VISITOR', '访客预约接待时间：09:00-17:00',
    '09:00:00', '17:00:00', 480, 0,
    1, 1, 1, 1, 1, 0, 0,
    'DISABLE', 7,
    1, 30,
    1, 1, '访客标准预约时间，提前30分钟通知'
);

-- 7. 食堂消费时间段 - 早餐（07:00-09:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_CONSUME_BREAKFAST', '早餐时间段', 'CONSUME', '食堂早餐消费时间：07:00-09:00',
    '07:00:00', '09:00:00', 120, 0,
    1, 1, 1, 1, 1, 1, 1,
    'NORMAL', 6,
    1, 1, '早餐消费时间段'
);

-- 8. 食堂消费时间段 - 午餐（11:00-13:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_CONSUME_LUNCH', '午餐时间段', 'CONSUME', '食堂午餐消费时间：11:00-13:00',
    '11:00:00', '13:00:00', 120, 0,
    1, 1, 1, 1, 1, 1, 1,
    'NORMAL', 8,
    1, 1, '午餐消费时间段'
);

-- 9. 食堂消费时间段 - 晚餐（17:00-19:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_CONSUME_DINNER', '晚餐时间段', 'CONSUME', '食堂晚餐消费时间：17:00-19:00',
    '17:00:00', '19:00:00', 120, 0,
    1, 1, 1, 1, 1, 1, 1,
    'NORMAL', 8,
    1, 1, '晚餐消费时间段'
);

-- 10. 加班时间段（18:00-22:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`, `is_template`, `template_category`,
    `allow_early_in`, `allow_late_out`, `tolerance_minutes`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_OVERTIME', '加班时间段', 'ATTENDANCE', '加班时间：18:00-22:00',
    '18:00:00', '22:00:00', 240, 0,
    1, 1, 1, 1, 1, 1, 1,
    'NORMAL', 6, 1, 'OVERTIME',
    30, 60, 15,
    1, 1, '加班时间段（全周）'
);

-- 11. 周末值班时间段（09:00-17:00）
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`, `is_template`, `template_category`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_WEEKEND_DUTY', '周末值班时间段', 'ATTENDANCE', '周末值班时间：09:00-17:00',
    '09:00:00', '17:00:00', 480, 0,
    0, 0, 0, 0, 0, 1, 1,
    'ONLY_HOLIDAY', 7, 1, 'WORK',
    1, 1, '仅周末和节假日启用'
);

-- 12. 24小时全天时间段
INSERT INTO `t_access_time_period` (
    `time_period_code`, `time_period_name`, `time_period_type`, `time_period_desc`,
    `start_time`, `end_time`, `duration_minutes`, `is_cross_day`,
    `monday_enabled`, `tuesday_enabled`, `wednesday_enabled`, `thursday_enabled`, `friday_enabled`,
    `saturday_enabled`, `sunday_enabled`,
    `holiday_mode`, `priority_level`,
    `time_period_status`, `enabled_flag`, `remark`
) VALUES (
    'TP_24_HOURS', '24小时全天时间段', 'ACCESS', '24小时不间断通行时间',
    '00:00:00', '23:59:59', 1439, 0,
    1, 1, 1, 1, 1, 1, 1,
    'NORMAL', 10,
    1, 1, '24小时全天候时间段'
);

-- =============================================
-- 索引优化说明
-- =============================================
-- 1. uk_time_period_code: 时间段编码唯一索引
-- 2. idx_time_period_type: 时间段类型索引，支持按类型查询
-- 3. idx_time_range: 时间范围索引，支持时间段查询
-- 4. idx_is_template: 模板标记和分类联合索引
-- 5. idx_priority: 优先级索引，支持按优先级排序

-- =============================================
-- 使用示例
-- =============================================
-- 1. 查询所有门禁时间段
-- SELECT * FROM t_access_time_period 
-- WHERE time_period_type = 'ACCESS' AND time_period_status = 1 AND deleted_flag = 0 
-- ORDER BY priority_level DESC;

-- 2. 查询指定时间点所在的时间段
-- SELECT * FROM t_access_time_period 
-- WHERE CURTIME() BETWEEN start_time AND end_time 
-- AND time_period_status = 1 AND deleted_flag = 0;

-- 3. 查询可用作模板的时间段
-- SELECT * FROM t_access_time_period 
-- WHERE is_template = 1 AND deleted_flag = 0 
-- ORDER BY template_category, priority_level DESC;

-- 4. 查询今天生效的时间段（根据星期）
-- SELECT * FROM t_access_time_period 
-- WHERE CASE DAYOFWEEK(CURDATE())
--     WHEN 1 THEN sunday_enabled = 1
--     WHEN 2 THEN monday_enabled = 1
--     WHEN 3 THEN tuesday_enabled = 1
--     WHEN 4 THEN wednesday_enabled = 1
--     WHEN 5 THEN thursday_enabled = 1
--     WHEN 6 THEN friday_enabled = 1
--     WHEN 7 THEN saturday_enabled = 1
-- END AND time_period_status = 1 AND deleted_flag = 0;

-- 5. 更新时间段使用统计
-- UPDATE t_access_time_period 
-- SET usage_count = usage_count + 1, last_used_time = NOW() 
-- WHERE time_period_id = 1;

-- =============================================
-- 维护建议
-- =============================================
-- 1. 定期审查时间段配置的合理性
-- 2. 监控时间段使用频率，优化常用模板
-- 3. 及时更新节假日相关时间段配置
-- 4. 建立时间段变更审批机制
-- 5. 定期清理未使用的时间段配置
-- 6. 优化跨天时间段的处理逻辑
