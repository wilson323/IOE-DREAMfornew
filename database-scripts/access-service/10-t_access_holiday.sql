-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 节假日配置表 (t_access_holiday)
-- =============================================
-- 功能说明: 节假日配置表，管理系统节假日和特殊日期
-- 业务场景: 
--   1. 国家法定节假日管理
--   2. 企业自定义假期
--   3. 调休工作日配置
--   4. 特殊活动日期管理
-- 企业级特性:
--   - 支持多种假期类型
--   - 支持假期联动规则
--   - 支持假期自动导入
--   - 支持假期批量管理
-- =============================================

USE `ioedream_access_db`;

-- =============================================
-- 表结构定义
-- =============================================
CREATE TABLE IF NOT EXISTS `t_access_holiday` (
    -- 主键ID
    `holiday_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节假日ID',
    
    -- 节假日基本信息
    `holiday_code` VARCHAR(100) NOT NULL COMMENT '节假日编码（唯一标识）',
    `holiday_name` VARCHAR(200) NOT NULL COMMENT '节假日名称',
    `holiday_type` VARCHAR(50) NOT NULL COMMENT '节假日类型：NATIONAL-法定节假日, COMPANY-企业假期, WEEKEND-周末, SPECIAL-特殊日期, WORKDAY-调休工作日',
    `holiday_desc` VARCHAR(500) COMMENT '节假日描述',
    
    -- 日期信息
    `holiday_date` DATE NOT NULL COMMENT '节假日日期',
    `start_date` DATE NOT NULL COMMENT '开始日期（用于连续假期）',
    `end_date` DATE NOT NULL COMMENT '结束日期（用于连续假期）',
    `duration_days` INT DEFAULT 1 COMMENT '假期天数',
    
    -- 年度信息
    `holiday_year` INT NOT NULL COMMENT '所属年份',
    `is_recurring` TINYINT DEFAULT 0 COMMENT '是否年度重复：0-不重复, 1-每年重复',
    `recurrence_rule` VARCHAR(200) COMMENT '重复规则（如：每年农历初一）',
    
    -- 假期属性
    `is_paid` TINYINT DEFAULT 1 COMMENT '是否带薪：0-无薪, 1-带薪',
    `is_statutory` TINYINT DEFAULT 0 COMMENT '是否法定：0-非法定, 1-法定',
    `overtime_rate` DECIMAL(5,2) DEFAULT 1.00 COMMENT '加班倍率（如节假日加班3倍工资）',
    
    -- 门禁规则
    `access_rule` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '门禁规则：NORMAL-正常, STRICT-严格, LOOSE-宽松, DISABLE-禁用',
    `time_period_id` BIGINT COMMENT '节假日专用时间段ID',
    `attendance_required` TINYINT DEFAULT 0 COMMENT '是否需要考勤：0-不需要, 1-需要',
    
    -- 联动配置
    `auto_approve_leave` TINYINT DEFAULT 1 COMMENT '自动审批请假：0-不自动, 1-自动审批',
    `auto_unlock_areas` TEXT COMMENT '自动解锁区域ID列表（JSON数组）：["1", "2", "3"]',
    `notification_enabled` TINYINT DEFAULT 1 COMMENT '通知启用：0-禁用, 1-启用（节假日前发送提醒）',
    `notification_days_before` INT DEFAULT 3 COMMENT '提前通知天数',
    
    -- 状态信息
    `holiday_status` TINYINT DEFAULT 1 COMMENT '节假日状态：0-禁用, 1-启用',
    `enabled_flag` TINYINT DEFAULT 1 COMMENT '启用标记：0-停用, 1-启用',
    `is_past` TINYINT DEFAULT 0 COMMENT '是否已过期：0-未过期, 1-已过期',
    
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
    PRIMARY KEY (`holiday_id`),
    
    -- 唯一键约束
    UNIQUE KEY `uk_holiday_code` (`holiday_code`) USING BTREE,
    UNIQUE KEY `uk_holiday_date` (`holiday_date`, `holiday_type`) USING BTREE,
    
    -- 普通索引
    INDEX `idx_holiday_type` (`holiday_type`) USING BTREE,
    INDEX `idx_holiday_year` (`holiday_year`, `holiday_type`) USING BTREE,
    INDEX `idx_date_range` (`start_date`, `end_date`) USING BTREE,
    INDEX `idx_holiday_date` (`holiday_date`) USING BTREE,
    INDEX `idx_status` (`holiday_status`, `enabled_flag`) USING BTREE,
    INDEX `idx_create_time` (`create_time` DESC) USING BTREE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节假日配置表';

-- =============================================
-- 初始化数据（2025年法定节假日）
-- =============================================

-- 1. 元旦假期（2025-01-01）
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`, `recurrence_rule`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES (
    'HOLIDAY_2025_NEW_YEAR', '2025年元旦', 'NATIONAL', '2025年元旦假期',
    '2025-01-01', '2025-01-01', '2025-01-01', 1,
    2025, 1, '每年1月1日',
    1, 1, 3.00,
    'LOOSE', 0, 1,
    1, 3,
    1, 1, '元旦法定节假日'
);

-- 2. 春节假期（2025-01-28 至 2025-02-04，共8天）
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`, `recurrence_rule`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES 
('HOLIDAY_2025_SPRING_1', '2025年春节（除夕）', 'NATIONAL', '2025年春节假期第1天',
 '2025-01-28', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历除夕至正月初七',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节法定节假日（除夕）'),
('HOLIDAY_2025_SPRING_2', '2025年春节（初一）', 'NATIONAL', '2025年春节假期第2天',
 '2025-01-29', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历正月初一',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节法定节假日（初一）'),
('HOLIDAY_2025_SPRING_3', '2025年春节（初二）', 'NATIONAL', '2025年春节假期第3天',
 '2025-01-30', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历正月初二',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节法定节假日（初二）'),
('HOLIDAY_2025_SPRING_4', '2025年春节（初三）', 'NATIONAL', '2025年春节假期第4天',
 '2025-01-31', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历正月初三',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节法定节假日（初三）'),
('HOLIDAY_2025_SPRING_5', '2025年春节（初四）', 'NATIONAL', '2025年春节假期第5天',
 '2025-02-01', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历正月初四',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节假期（初四）'),
('HOLIDAY_2025_SPRING_6', '2025年春节（初五）', 'NATIONAL', '2025年春节假期第6天',
 '2025-02-02', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历正月初五',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节假期（初五）'),
('HOLIDAY_2025_SPRING_7', '2025年春节（初六）', 'NATIONAL', '2025年春节假期第7天',
 '2025-02-03', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历正月初六',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节假期（初六）'),
('HOLIDAY_2025_SPRING_8', '2025年春节（初七）', 'NATIONAL', '2025年春节假期第8天',
 '2025-02-04', '2025-01-28', '2025-02-04', 8,
 2025, 1, '农历正月初七',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '春节假期（初七）');

-- 3. 清明节假期（2025-04-04 至 2025-04-06，共3天）
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`, `recurrence_rule`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES 
('HOLIDAY_2025_QINGMING_1', '2025年清明节', 'NATIONAL', '2025年清明节假期第1天',
 '2025-04-04', '2025-04-04', '2025-04-06', 3,
 2025, 1, '每年清明节',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '清明节法定节假日'),
('HOLIDAY_2025_QINGMING_2', '2025年清明节', 'NATIONAL', '2025年清明节假期第2天',
 '2025-04-05', '2025-04-04', '2025-04-06', 3,
 2025, 1, '每年清明节',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '清明节假期第2天'),
('HOLIDAY_2025_QINGMING_3', '2025年清明节', 'NATIONAL', '2025年清明节假期第3天',
 '2025-04-06', '2025-04-04', '2025-04-06', 3,
 2025, 1, '每年清明节',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '清明节假期第3天');

-- 4. 劳动节假期（2025-05-01 至 2025-05-05，共5天）
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`, `recurrence_rule`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES 
('HOLIDAY_2025_LABOR_1', '2025年劳动节', 'NATIONAL', '2025年劳动节假期第1天',
 '2025-05-01', '2025-05-01', '2025-05-05', 5,
 2025, 1, '每年5月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '劳动节法定节假日'),
('HOLIDAY_2025_LABOR_2', '2025年劳动节', 'NATIONAL', '2025年劳动节假期第2天',
 '2025-05-02', '2025-05-01', '2025-05-05', 5,
 2025, 1, '每年5月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '劳动节假期第2天'),
('HOLIDAY_2025_LABOR_3', '2025年劳动节', 'NATIONAL', '2025年劳动节假期第3天',
 '2025-05-03', '2025-05-01', '2025-05-05', 5,
 2025, 1, '每年5月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '劳动节假期第3天'),
('HOLIDAY_2025_LABOR_4', '2025年劳动节', 'NATIONAL', '2025年劳动节假期第4天',
 '2025-05-04', '2025-05-01', '2025-05-05', 5,
 2025, 1, '每年5月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '劳动节假期第4天'),
('HOLIDAY_2025_LABOR_5', '2025年劳动节', 'NATIONAL', '2025年劳动节假期第5天',
 '2025-05-05', '2025-05-01', '2025-05-05', 5,
 2025, 1, '每年5月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '劳动节假期第5天');

-- 5. 端午节假期（2025-05-31 至 2025-06-02，共3天）
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`, `recurrence_rule`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES 
('HOLIDAY_2025_DRAGON_1', '2025年端午节', 'NATIONAL', '2025年端午节假期第1天',
 '2025-05-31', '2025-05-31', '2025-06-02', 3,
 2025, 1, '农历五月初五',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '端午节法定节假日'),
('HOLIDAY_2025_DRAGON_2', '2025年端午节', 'NATIONAL', '2025年端午节假期第2天',
 '2025-06-01', '2025-05-31', '2025-06-02', 3,
 2025, 1, '农历五月初五',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '端午节假期第2天'),
('HOLIDAY_2025_DRAGON_3', '2025年端午节', 'NATIONAL', '2025年端午节假期第3天',
 '2025-06-02', '2025-05-31', '2025-06-02', 3,
 2025, 1, '农历五月初五',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '端午节假期第3天');

-- 6. 中秋节假期（2025-10-06 至 2025-10-08，共3天）
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`, `recurrence_rule`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES 
('HOLIDAY_2025_MID_AUTUMN_1', '2025年中秋节', 'NATIONAL', '2025年中秋节假期第1天',
 '2025-10-06', '2025-10-06', '2025-10-08', 3,
 2025, 1, '农历八月十五',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '中秋节法定节假日'),
('HOLIDAY_2025_MID_AUTUMN_2', '2025年中秋节', 'NATIONAL', '2025年中秋节假期第2天',
 '2025-10-07', '2025-10-06', '2025-10-08', 3,
 2025, 1, '农历八月十五',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '中秋节假期第2天'),
('HOLIDAY_2025_MID_AUTUMN_3', '2025年中秋节', 'NATIONAL', '2025年中秋节假期第3天',
 '2025-10-08', '2025-10-06', '2025-10-08', 3,
 2025, 1, '农历八月十五',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 3, 1, 1, '中秋节假期第3天');

-- 7. 国庆节假期（2025-10-01 至 2025-10-07，共7天）
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`, `recurrence_rule`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES 
('HOLIDAY_2025_NATIONAL_1', '2025年国庆节', 'NATIONAL', '2025年国庆节假期第1天',
 '2025-10-01', '2025-10-01', '2025-10-07', 7,
 2025, 1, '每年10月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '国庆节法定节假日'),
('HOLIDAY_2025_NATIONAL_2', '2025年国庆节', 'NATIONAL', '2025年国庆节假期第2天',
 '2025-10-02', '2025-10-01', '2025-10-07', 7,
 2025, 1, '每年10月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '国庆节假期第2天'),
('HOLIDAY_2025_NATIONAL_3', '2025年国庆节', 'NATIONAL', '2025年国庆节假期第3天',
 '2025-10-03', '2025-10-01', '2025-10-07', 7,
 2025, 1, '每年10月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '国庆节假期第3天'),
('HOLIDAY_2025_NATIONAL_4', '2025年国庆节', 'NATIONAL', '2025年国庆节假期第4天',
 '2025-10-04', '2025-10-01', '2025-10-07', 7,
 2025, 1, '每年10月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '国庆节假期第4天'),
('HOLIDAY_2025_NATIONAL_5', '2025年国庆节', 'NATIONAL', '2025年国庆节假期第5天',
 '2025-10-05', '2025-10-01', '2025-10-07', 7,
 2025, 1, '每年10月1日',
 1, 1, 3.00, 'LOOSE', 0, 1, 1, 7, 1, 1, '国庆节假期第5天');

-- 8. 企业自定义假期示例
INSERT INTO `t_access_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_desc`,
    `holiday_date`, `start_date`, `end_date`, `duration_days`,
    `holiday_year`, `is_recurring`,
    `is_paid`, `is_statutory`, `overtime_rate`,
    `access_rule`, `attendance_required`, `auto_approve_leave`,
    `notification_enabled`, `notification_days_before`,
    `holiday_status`, `enabled_flag`, `remark`
) VALUES (
    'HOLIDAY_2025_COMPANY_ANNIVERSARY', '2025年公司周年庆', 'COMPANY', '公司成立周年纪念日',
    '2025-06-15', '2025-06-15', '2025-06-15', 1,
    2025, 1,
    1, 0, 2.00,
    'NORMAL', 0, 1,
    1, 7,
    1, 1, '公司周年庆，员工放假一天'
);

-- =============================================
-- 索引优化说明
-- =============================================
-- 1. uk_holiday_code: 节假日编码唯一索引
-- 2. uk_holiday_date: 节假日日期和类型联合唯一索引，防止重复配置
-- 3. idx_holiday_type: 节假日类型索引，支持按类型查询
-- 4. idx_holiday_year: 年份和类型联合索引，支持年度查询
-- 5. idx_date_range: 日期范围索引，支持区间查询
-- 6. idx_holiday_date: 日期索引，支持日期查询

-- =============================================
-- 使用示例
-- =============================================
-- 1. 查询2025年所有法定节假日
-- SELECT * FROM t_access_holiday 
-- WHERE holiday_year = 2025 AND holiday_type = 'NATIONAL' AND deleted_flag = 0 
-- ORDER BY holiday_date ASC;

-- 2. 查询指定日期是否为节假日
-- SELECT * FROM t_access_holiday 
-- WHERE holiday_date = '2025-10-01' AND holiday_status = 1 AND deleted_flag = 0;

-- 3. 查询指定日期范围内的所有节假日
-- SELECT * FROM t_access_holiday 
-- WHERE start_date >= '2025-05-01' AND end_date <= '2025-05-31' AND deleted_flag = 0;

-- 4. 统计2025年节假日总天数
-- SELECT SUM(duration_days) as total_holiday_days 
-- FROM t_access_holiday 
-- WHERE holiday_year = 2025 AND holiday_type = 'NATIONAL' AND deleted_flag = 0;

-- =============================================
-- 维护建议
-- =============================================
-- 1. 每年年底导入下一年度的法定节假日数据
-- 2. 及时更新政府发布的调休安排
-- 3. 定期清理过期的历史节假日数据
-- 4. 建立节假日变更通知机制
-- 5. 监控节假日对考勤、门禁的影响
-- 6. 定期审查企业自定义假期的合理性
