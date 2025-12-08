-- ==================================================================
-- 考勤服务 - 节假日配置表 (10-t_attendance_holiday.sql)
-- ==================================================================
-- 表名: t_attendance_holiday
-- 说明: 存储和管理公司的节假日配置信息,包括国家法定节假日、公司节假日、调休安排、补班日期等
-- 依赖: 无(独立表)
-- 作者: IOE-DREAM团队
-- 创建时间: 2025-12-08
-- 版本: v1.0.0
-- ==================================================================

USE `ioedream_attendance`;

-- ==================================================================
-- 删除已存在的表(如果存在)
-- ==================================================================
DROP TABLE IF EXISTS `t_attendance_holiday`;

-- ==================================================================
-- 创建表: t_attendance_holiday
-- ==================================================================
CREATE TABLE IF NOT EXISTS `t_attendance_holiday` (
    -- ==============================================================
    -- 主键和唯一标识
    -- ==============================================================
    `holiday_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '节假日ID(主键)',
    `holiday_code` VARCHAR(100) NOT NULL COMMENT '节假日编码(唯一标识,如:HOLIDAY_2025_001)',

    -- ==============================================================
    -- 基本信息
    -- ==============================================================
    `holiday_name` VARCHAR(200) NOT NULL COMMENT '节假日名称(如:元旦、春节、清明节)',
    `holiday_name_en` VARCHAR(200) COMMENT '节假日英文名称(如:New Year Day)',
    `holiday_description` TEXT COMMENT '节假日描述',
    `holiday_type` VARCHAR(50) NOT NULL DEFAULT 'LEGAL' COMMENT '节假日类型(LEGAL-法定节假日、COMPANY-公司节假日、SPECIAL-特殊节假日)',
    `holiday_category` VARCHAR(50) NOT NULL DEFAULT 'NATIONAL' COMMENT '节假日分类(NATIONAL-国家法定、REGIONAL-地方节假日、DEPARTMENT-部门节假日、PROJECT-项目节假日)',

    -- ==============================================================
    -- 时间配置
    -- ==============================================================
    `holiday_year` INT NOT NULL COMMENT '节假日年份(如:2025)',
    `start_date` DATE NOT NULL COMMENT '开始日期(如:2025-01-01)',
    `end_date` DATE NOT NULL COMMENT '结束日期(如:2025-01-03)',
    `total_days` INT NOT NULL DEFAULT 1 COMMENT '总天数(自动计算,end_date - start_date + 1)',
    `actual_holiday_days` INT NOT NULL DEFAULT 0 COMMENT '实际休假天数(排除周末后的天数)',

    -- ==============================================================
    -- 循环配置(支持每年固定日期节假日)
    -- ==============================================================
    `is_recurring` TINYINT NOT NULL DEFAULT 0 COMMENT '是否循环节假日(0-否,1-是,如每年固定的节假日)',
    `recurrence_rule` VARCHAR(200) COMMENT '循环规则(如:YEARLY,农历规则等)',
    `lunar_calendar_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否使用农历(0-否,1-是,如春节)',
    `lunar_month` INT COMMENT '农历月份(1-12)',
    `lunar_day` INT COMMENT '农历日期(1-30)',

    -- ==============================================================
    -- 调休和补班配置
    -- ==============================================================
    `has_makeup_work` TINYINT NOT NULL DEFAULT 0 COMMENT '是否有补班(0-否,1-是)',
    `makeup_work_dates` JSON COMMENT '补班日期集合(JSON数组,如:["2025-01-26","2025-02-08"])',
    `makeup_work_count` INT NOT NULL DEFAULT 0 COMMENT '补班天数统计',

    `has_compensatory_leave` TINYINT NOT NULL DEFAULT 0 COMMENT '是否有调休(0-否,1-是)',
    `compensatory_leave_dates` JSON COMMENT '调休日期集合(JSON数组)',
    `compensatory_leave_count` INT NOT NULL DEFAULT 0 COMMENT '调休天数统计',

    -- ==============================================================
    -- 适用范围
    -- ==============================================================
    `applicable_scope` JSON COMMENT '适用范围(JSON对象,包含部门、职位、员工类型、区域等)',
    `department_ids` JSON COMMENT '适用部门ID集合(JSON数组,为空表示全部)',
    `position_ids` JSON COMMENT '适用职位ID集合(JSON数组)',
    `employee_type_filter` JSON COMMENT '适用员工类型过滤(JSON数组,如:["正式员工","合同工"])',
    `region_filter` JSON COMMENT '适用区域过滤(JSON数组,如:["北京","上海"])',

    -- ==============================================================
    -- 加班配置
    -- ==============================================================
    `overtime_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许加班(0-否,1-是)',
    `overtime_rate` DECIMAL(10,2) NOT NULL DEFAULT 3.00 COMMENT '加班倍率(法定节假日3倍,休息日2倍,补班日1.5倍)',
    `overtime_approval_required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要审批(0-否,1-是)',
    `overtime_rule_id` BIGINT COMMENT '关联的加班规则ID',

    -- ==============================================================
    -- 考勤规则
    -- ==============================================================
    `attendance_rule_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用考勤规则(0-否,1-是)',
    `require_clock_in` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要打卡(0-否,1-是,特殊情况下节假日也需要打卡)',
    `allow_leave_application` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许请假(0-否,1-是)',
    `deduct_from_leave_balance` TINYINT NOT NULL DEFAULT 0 COMMENT '是否扣除假期余额(0-否,1-是)',

    -- ==============================================================
    -- 工作日计算
    -- ==============================================================
    `is_workday` TINYINT NOT NULL DEFAULT 0 COMMENT '是否算作工作日(0-否,1-是,补班日为1)',
    `workday_count` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '工作日计数(0-休假,1-全天工作,0.5-半天工作)',
    `exclude_from_attendance` TINYINT NOT NULL DEFAULT 1 COMMENT '是否排除考勤统计(0-否,1-是)',

    -- ==============================================================
    -- 排班影响
    -- ==============================================================
    `affect_schedule` TINYINT NOT NULL DEFAULT 1 COMMENT '是否影响排班(0-否,1-是)',
    `auto_adjust_schedule` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动调整排班(0-否,1-是)',
    `schedule_adjustment_rule` VARCHAR(200) COMMENT '排班调整规则(如:自动取消节假日排班)',

    -- ==============================================================
    -- 通知配置
    -- ==============================================================
    `notification_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用通知(0-否,1-是)',
    `notification_days_before` INT NOT NULL DEFAULT 7 COMMENT '提前通知天数(默认7天)',
    `notification_channels` JSON COMMENT '通知渠道(JSON数组,["短信","邮件","微信","App推送"])',
    `notification_template` VARCHAR(200) COMMENT '通知模板',
    `notification_time` TIME COMMENT '通知时间',

    -- ==============================================================
    -- 优先级和排序
    -- ==============================================================
    `priority` INT NOT NULL DEFAULT 100 COMMENT '优先级(数值越大优先级越高,用于冲突时的判断)',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',

    -- ==============================================================
    -- 状态和控制
    -- ==============================================================
    `holiday_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '节假日状态(DRAFT-草稿,ACTIVE-生效,EXPIRED-已过期,CANCELLED-已取消)',
    `is_published` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已发布(0-未发布,1-已发布)',
    `publish_time` DATETIME COMMENT '发布时间',
    `effective_date` DATE COMMENT '生效日期',
    `expiry_date` DATE COMMENT '失效日期',

    -- ==============================================================
    -- 审批流程
    -- ==============================================================
    `require_approval` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要审批(0-否,1-是)',
    `approval_workflow_id` BIGINT COMMENT '审批工作流ID',
    `approval_status` VARCHAR(50) NOT NULL DEFAULT 'APPROVED' COMMENT '审批状态(PENDING-待审批,APPROVED-已通过,REJECTED-已拒绝)',
    `approver_id` BIGINT COMMENT '审批人用户ID',
    `approval_time` DATETIME COMMENT '审批时间',
    `approval_comment` TEXT COMMENT '审批意见',

    -- ==============================================================
    -- 关联信息
    -- ==============================================================
    `parent_holiday_id` BIGINT COMMENT '父节假日ID(用于关联长假的子假期)',
    `related_holiday_ids` JSON COMMENT '关联节假日ID集合(JSON数组)',
    `source_type` VARCHAR(50) NOT NULL DEFAULT 'MANUAL' COMMENT '来源类型(MANUAL-手动创建,IMPORT-导入,API-API创建,SYSTEM-系统生成)',
    `source_id` VARCHAR(100) COMMENT '来源ID',

    -- ==============================================================
    -- 统计信息
    -- ==============================================================
    `affected_employee_count` INT NOT NULL DEFAULT 0 COMMENT '影响员工数',
    `affected_department_count` INT NOT NULL DEFAULT 0 COMMENT '影响部门数',
    `schedule_adjustment_count` INT NOT NULL DEFAULT 0 COMMENT '排班调整次数',
    `notification_sent_count` INT NOT NULL DEFAULT 0 COMMENT '通知发送次数',
    `last_notification_time` DATETIME COMMENT '最后通知时间',

    -- ==============================================================
    -- 扩展字段
    -- ==============================================================
    `extended_attributes` JSON COMMENT '扩展属性(JSON对象,存储额外的配置信息)',
    `metadata` JSON COMMENT '元数据(JSON对象)',
    `tags` JSON COMMENT '标签(JSON数组,用于分类和检索)',

    -- ==============================================================
    -- 备注和说明
    -- ==============================================================
    `remark` TEXT COMMENT '备注',
    `admin_note` TEXT COMMENT '管理员备注',
    `important_notice` TEXT COMMENT '重要通知',

    -- ==============================================================
    -- 标准审计字段
    -- ==============================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(用于乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID',
    `update_user_id` BIGINT COMMENT '更新人用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-未删除,1-已删除)',

    -- ==============================================================
    -- 主键约束
    -- ==============================================================
    PRIMARY KEY (`holiday_id`),

    -- ==============================================================
    -- 唯一索引
    -- ==============================================================
    UNIQUE KEY `uk_holiday_code` (`holiday_code`),

    -- ==============================================================
    -- 普通索引
    -- ==============================================================
    INDEX `idx_holiday_name` (`holiday_name`),
    INDEX `idx_holiday_type` (`holiday_type`),
    INDEX `idx_holiday_category` (`holiday_category`),
    INDEX `idx_holiday_year` (`holiday_year`),
    INDEX `idx_start_date` (`start_date`),
    INDEX `idx_end_date` (`end_date`),
    INDEX `idx_date_range` (`start_date`, `end_date`),
    INDEX `idx_holiday_status` (`holiday_status`),
    INDEX `idx_is_published` (`is_published`),
    INDEX `idx_is_recurring` (`is_recurring`),
    INDEX `idx_parent_holiday_id` (`parent_holiday_id`),

    -- ==============================================================
    -- 复合索引(优化常用查询)
    -- ==============================================================
    INDEX `idx_year_type_status` (`holiday_year`, `holiday_type`, `holiday_status`),
    INDEX `idx_date_status` (`start_date`, `holiday_status`),
    INDEX `idx_type_published` (`holiday_type`, `is_published`),

    -- ==============================================================
    -- 时间索引(降序,优化最新数据查询)
    -- ==============================================================
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_update_time` (`update_time` DESC),

    -- ==============================================================
    -- 逻辑删除索引
    -- ==============================================================
    INDEX `idx_deleted_flag` (`deleted_flag`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-节假日配置表';

-- ==================================================================
-- 初始化数据: 2025年中国法定节假日(15条完整数据)
-- ==================================================================

-- 1. 元旦节假日
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_name_en`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `is_recurring`, `recurrence_rule`, `overtime_rate`, `is_workday`, `workday_count`,
    `exclude_from_attendance`, `affect_schedule`, `auto_adjust_schedule`,
    `holiday_status`, `is_published`, `publish_time`, `priority`, `remark`
) VALUES (
    'HOLIDAY_2025_001', '元旦', 'New Year Day', 'LEGAL', 'NATIONAL',
    2025, '2025-01-01', '2025-01-01', 1, 1,
    1, 'YEARLY:01-01', 3.00, 0, 0.00,
    1, 1, 1,
    'ACTIVE', 1, NOW(), 999, '2025年元旦节,法定节假日1天,加班3倍工资'
);

-- 2. 春节假期(共8天)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_name_en`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `is_recurring`, `lunar_calendar_enabled`, `lunar_month`, `lunar_day`, `overtime_rate`,
    `has_makeup_work`, `makeup_work_dates`, `makeup_work_count`,
    `is_workday`, `workday_count`, `exclude_from_attendance`, `affect_schedule`,
    `holiday_status`, `is_published`, `priority`, `remark`
) VALUES (
    'HOLIDAY_2025_002', '春节', 'Spring Festival', 'LEGAL', 'NATIONAL',
    2025, '2025-01-28', '2025-02-04', 8, 7,
    1, 1, 1, 1, 3.00,
    1, '["2025-01-26","2025-02-08"]', 2,
    0, 0.00, 1, 1,
    'ACTIVE', 1, 999, '2025年春节假期8天(含2天补班调休),法定节假日加班3倍工资'
);

-- 3. 春节补班日1(1月26日周日调休)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`,
    `is_workday`, `workday_count`, `overtime_rate`,
    `parent_holiday_id`, `holiday_status`, `priority`, `remark`
) VALUES (
    'MAKEUP_2025_001', '春节补班(1月26日)', 'LEGAL', 'NATIONAL',
    2025, '2025-01-26', '2025-01-26', 1,
    1, 1.00, 1.50,
    (SELECT holiday_id FROM t_attendance_holiday WHERE holiday_code='HOLIDAY_2025_002'),
    'ACTIVE', 500, '春节调休补班日,按工作日计算,加班1.5倍工资'
);

-- 4. 春节补班日2(2月8日周六调休)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`,
    `is_workday`, `workday_count`, `overtime_rate`,
    `parent_holiday_id`, `holiday_status`, `priority`, `remark`
) VALUES (
    'MAKEUP_2025_002', '春节补班(2月8日)', 'LEGAL', 'NATIONAL',
    2025, '2025-02-08', '2025-02-08', 1,
    1, 1.00, 1.50,
    (SELECT holiday_id FROM t_attendance_holiday WHERE holiday_code='HOLIDAY_2025_002'),
    'ACTIVE', 500, '春节调休补班日,按工作日计算,加班1.5倍工资'
);

-- 5. 清明节假期
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_name_en`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `is_recurring`, `recurrence_rule`, `overtime_rate`,
    `is_workday`, `exclude_from_attendance`, `affect_schedule`,
    `holiday_status`, `is_published`, `priority`, `remark`
) VALUES (
    'HOLIDAY_2025_003', '清明节', 'Tomb-sweeping Day', 'LEGAL', 'NATIONAL',
    2025, '2025-04-04', '2025-04-06', 3, 3,
    1, 'YEARLY:04-04~04-06', 3.00,
    0, 1, 1,
    'ACTIVE', 1, 999, '2025年清明节假期3天,法定节假日加班3倍工资'
);

-- 6. 劳动节假期(共5天)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_name_en`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `is_recurring`, `recurrence_rule`, `overtime_rate`,
    `has_makeup_work`, `makeup_work_dates`, `makeup_work_count`,
    `is_workday`, `exclude_from_attendance`, `affect_schedule`,
    `holiday_status`, `is_published`, `priority`, `remark`
) VALUES (
    'HOLIDAY_2025_004', '劳动节', 'Labor Day', 'LEGAL', 'NATIONAL',
    2025, '2025-05-01', '2025-05-05', 5, 5,
    1, 'YEARLY:05-01~05-05', 3.00,
    1, '["2025-04-27"]', 1,
    0, 1, 1,
    'ACTIVE', 1, 999, '2025年劳动节假期5天(含1天补班调休),法定节假日加班3倍工资'
);

-- 7. 劳动节补班日(4月27日周日调休)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`,
    `is_workday`, `workday_count`, `overtime_rate`,
    `parent_holiday_id`, `holiday_status`, `priority`, `remark`
) VALUES (
    'MAKEUP_2025_003', '劳动节补班(4月27日)', 'LEGAL', 'NATIONAL',
    2025, '2025-04-27', '2025-04-27', 1,
    1, 1.00, 1.50,
    (SELECT holiday_id FROM t_attendance_holiday WHERE holiday_code='HOLIDAY_2025_004'),
    'ACTIVE', 500, '劳动节调休补班日,按工作日计算,加班1.5倍工资'
);

-- 8. 端午节假期
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_name_en`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `is_recurring`, `lunar_calendar_enabled`, `lunar_month`, `lunar_day`, `overtime_rate`,
    `is_workday`, `exclude_from_attendance`, `affect_schedule`,
    `holiday_status`, `is_published`, `priority`, `remark`
) VALUES (
    'HOLIDAY_2025_005', '端午节', 'Dragon Boat Festival', 'LEGAL', 'NATIONAL',
    2025, '2025-05-31', '2025-06-02', 3, 3,
    1, 1, 5, 5, 3.00,
    0, 1, 1,
    'ACTIVE', 1, 999, '2025年端午节假期3天,法定节假日加班3倍工资'
);

-- 9. 中秋节假期
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_name_en`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `is_recurring`, `lunar_calendar_enabled`, `lunar_month`, `lunar_day`, `overtime_rate`,
    `is_workday`, `exclude_from_attendance`, `affect_schedule`,
    `holiday_status`, `is_published`, `priority`, `remark`
) VALUES (
    'HOLIDAY_2025_006', '中秋节', 'Mid-Autumn Festival', 'LEGAL', 'NATIONAL',
    2025, '2025-10-06', '2025-10-06', 1, 1,
    1, 1, 8, 15, 3.00,
    0, 1, 1,
    'ACTIVE', 1, 999, '2025年中秋节假期,与国庆节连休,法定节假日加班3倍工资'
);

-- 10. 国庆节假期(共7天,含中秋节)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_name_en`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `is_recurring`, `recurrence_rule`, `overtime_rate`,
    `has_makeup_work`, `makeup_work_dates`, `makeup_work_count`,
    `is_workday`, `exclude_from_attendance`, `affect_schedule`,
    `holiday_status`, `is_published`, `priority`, `remark`
) VALUES (
    'HOLIDAY_2025_007', '国庆节', 'National Day', 'LEGAL', 'NATIONAL',
    2025, '2025-10-01', '2025-10-07', 7, 7,
    1, 'YEARLY:10-01~10-07', 3.00,
    1, '["2025-09-28","2025-10-11"]', 2,
    0, 1, 1,
    'ACTIVE', 1, 999, '2025年国庆节假期7天(含2天补班调休,含中秋节),法定节假日加班3倍工资'
);

-- 11. 国庆节补班日1(9月28日周日调休)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`,
    `is_workday`, `workday_count`, `overtime_rate`,
    `parent_holiday_id`, `holiday_status`, `priority`, `remark`
) VALUES (
    'MAKEUP_2025_004', '国庆节补班(9月28日)', 'LEGAL', 'NATIONAL',
    2025, '2025-09-28', '2025-09-28', 1,
    1, 1.00, 1.50,
    (SELECT holiday_id FROM t_attendance_holiday WHERE holiday_code='HOLIDAY_2025_007'),
    'ACTIVE', 500, '国庆节调休补班日,按工作日计算,加班1.5倍工资'
);

-- 12. 国庆节补班日2(10月11日周六调休)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`,
    `is_workday`, `workday_count`, `overtime_rate`,
    `parent_holiday_id`, `holiday_status`, `priority`, `remark`
) VALUES (
    'MAKEUP_2025_005', '国庆节补班(10月11日)', 'LEGAL', 'NATIONAL',
    2025, '2025-10-11', '2025-10-11', 1,
    1, 1.00, 1.50,
    (SELECT holiday_id FROM t_attendance_holiday WHERE holiday_code='HOLIDAY_2025_007'),
    'ACTIVE', 500, '国庆节调休补班日,按工作日计算,加班1.5倍工资'
);

-- 13. 公司年会日(示例公司节假日)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`, `actual_holiday_days`,
    `overtime_rate`, `is_workday`, `exclude_from_attendance`,
    `holiday_status`, `is_published`, `priority`, `remark`
) VALUES (
    'COMPANY_2025_001', '公司年会日', 'COMPANY', 'COMPANY',
    2025, '2025-01-18', '2025-01-18', 1, 1,
    1.00, 0, 1,
    'ACTIVE', 1, 800, '公司年会日,全员放假1天,不计入工作日'
);

-- 14. 技术部团建日(示例部门节假日)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`,
    `department_ids`, `overtime_rate`, `is_workday`,
    `holiday_status`, `priority`, `remark`
) VALUES (
    'DEPT_2025_001', '技术部团建日', 'COMPANY', 'DEPARTMENT',
    2025, '2025-06-15', '2025-06-15', 1,
    '[101,102,103]', 1.00, 0,
    'ACTIVE', 700, '技术部团建日,技术部门放假1天'
);

-- 15. 项目庆功日(示例项目节假日)
INSERT INTO `t_attendance_holiday` (
    `holiday_code`, `holiday_name`, `holiday_type`, `holiday_category`,
    `holiday_year`, `start_date`, `end_date`, `total_days`,
    `employee_type_filter`, `overtime_rate`, `is_workday`,
    `holiday_status`, `priority`, `remark`
) VALUES (
    'PROJECT_2025_001', '项目庆功日', 'SPECIAL', 'PROJECT',
    2025, '2025-08-20', '2025-08-20', 1,
    '["项目组成员"]', 1.00, 0,
    'ACTIVE', 600, '重点项目完成庆功,项目组成员放假1天'
);

-- ==================================================================
-- 使用示例查询SQL
-- ==================================================================

-- 示例1: 查询2025年所有法定节假日
-- SELECT * FROM t_attendance_holiday
-- WHERE holiday_year = 2025
--   AND holiday_type = 'LEGAL'
--   AND holiday_status = 'ACTIVE'
--   AND deleted_flag = 0
-- ORDER BY start_date ASC;

-- 示例2: 查询指定日期是否为节假日
-- SELECT * FROM t_attendance_holiday
-- WHERE '2025-01-01' BETWEEN start_date AND end_date
--   AND holiday_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 示例3: 查询2025年所有补班日
-- SELECT * FROM t_attendance_holiday
-- WHERE holiday_year = 2025
--   AND is_workday = 1
--   AND holiday_status = 'ACTIVE'
--   AND deleted_flag = 0
-- ORDER BY start_date ASC;

-- 示例4: 查询需要发布通知的节假日(提前7天)
-- SELECT * FROM t_attendance_holiday
-- WHERE start_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
--   AND notification_enabled = 1
--   AND holiday_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 示例5: 查询某部门适用的节假日
-- SELECT * FROM t_attendance_holiday
-- WHERE (department_ids IS NULL OR JSON_CONTAINS(department_ids, '101'))
--   AND holiday_year = 2025
--   AND holiday_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- 示例6: 统计2025年法定节假日总天数
-- SELECT SUM(actual_holiday_days) as total_holiday_days
-- FROM t_attendance_holiday
-- WHERE holiday_year = 2025
--   AND holiday_type = 'LEGAL'
--   AND is_workday = 0
--   AND deleted_flag = 0;

-- 示例7: 查询循环节假日配置
-- SELECT * FROM t_attendance_holiday
-- WHERE is_recurring = 1
--   AND deleted_flag = 0
-- ORDER BY priority DESC;

-- 示例8: 查询节假日及其关联的补班日
-- SELECT h.holiday_name,h.start_date,h.end_date,
--        m.holiday_name as makeup_name, m.start_date as makeup_date
-- FROM t_attendance_holiday h
-- LEFT JOIN t_attendance_holiday m ON m.parent_holiday_id = h.holiday_id
-- WHERE h.holiday_year = 2025
--   AND h.holiday_type = 'LEGAL'
--   AND h.deleted_flag = 0
-- ORDER BY h.start_date ASC;

-- ==================================================================
-- 维护建议
-- ==================================================================
-- 1. 每年年初提前录入下一年的法定节假日安排
-- 2. 及时更新政府发布的节假日调休通知
-- 3. 定期检查循环节假日的准确性
-- 4. 定期清理过期的历史节假日数据(保留3年)
-- 5. 监控节假日对排班系统的影响,及时调整

-- ==================================================================
-- 定时任务建议
-- ==================================================================
-- 1. 每天凌晨检查未来7天的节假日,发送提醒通知
-- 2. 每周一汇总本周节假日安排,推送给管理员
-- 3. 每月统计节假日使用情况,生成报表
-- 4. 每年12月自动生成下一年的循环节假日
-- 5. 每季度检查补班日和调休日的匹配性
-- 6. 实时监控节假日变更,自动调整受影响的排班

-- ==================================================================
-- 注意事项
-- ==================================================================
-- 1. 法定节假日的加班倍率为3倍,休息日为2倍,补班日为1.5倍
-- 2. 节假日的调休和补班日期必须同步更新
-- 3. 循环节假日需要考虑农历和阳历的差异
-- 4. 节假日的适用范围可以按部门、职位、区域等维度配置
-- 5. 节假日变更会影响排班系统,需要同步调整
-- 6. 补班日虽然要上班,但加班倍率不同于正常工作日
-- 7. 节假日期间的请假申请需要特殊处理
-- 8. 跨年的节假日需要分别创建记录(如春节)

-- ==================================================================
-- 版本历史
-- ==================================================================
-- v1.0.0 - 2025-12-08
--   - 初始版本
--   - 支持法定节假日、公司节假日、部门节假日配置
--   - 支持调休和补班日配置
--   - 支持循环节假日(农历/阳历)
--   - 支持多维度适用范围配置
--   - 支持加班倍率和考勤规则配置
--   - 支持通知提醒功能
--   - 初始化2025年中国法定节假日数据(15条完整数据)

-- ==================================================================
-- 文件结束
-- ==================================================================
