-- ================================================================
-- IOE-DREAM 智慧园区一卡通管理平台 - 考勤服务
-- 表名: t_attendance_shift_group - 班次组配置表
-- 版本: v1.0.0
-- 创建日期: 2025-12-08
-- 最后修改: 2025-12-08
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4_unicode_ci
-- 引擎: InnoDB
-- 时区: Asia/Shanghai
-- ================================================================
-- 
-- 📋 表说明:
-- 本表用于存储和管理班次组配置信息,是考勤系统的核心配置表之一。
-- 班次组是对具有相同或相似班次安排的员工群体进行分组管理的功能,
-- 便于批量设置排班规则、考勤规则、加班规则等。
-- 
-- 🎯 核心功能:
-- 1. 班次组基础信息管理: 组名、描述、状态等
-- 2. 班次组成员管理: 员工、部门、职位等成员归属
-- 3. 班次组规则配置: 关联班次、考勤规则、加班规则等
-- 4. 班次组权限控制: 访问权限、操作权限等
-- 5. 班次组统计分析: 成员统计、使用情况等
-- 6. 班次组模板管理: 支持班次组模板复制和快速创建
-- 7. 班次组继承关系: 支持父子班次组的继承关系
-- 8. 班次组多维度查询: 按名称、状态、类型等多维度查询
-- 
-- 🏢 业务场景:
-- 1. 部门班次组: 按部门划分的班次组(如研发部、销售部等)
-- 2. 职位班次组: 按职位划分的班次组(如经理、主管、员工等)
-- 3. 项目班次组: 按项目划分的班次组(如项目A组、项目B组等)
-- 4. 轮班组: 按轮班制度划分的班次组(如早班组、中班组、晚班组等)
-- 5. 特殊班次组: 特殊工作安排的班次组(如弹性工作组、远程工作组等)
-- 6. 临时班次组: 临时项目或活动的班次组
-- 7. 模板班次组: 用于快速创建新班次组的模板
-- 8. 默认班次组: 系统默认的班次组配置
-- 
-- 📊 数据特点:
-- - 数据量级: 较少(通常几十到几百个班次组)
-- - 更新频率: 低频(班次组配置相对稳定)
-- - 查询频率: 中频(排班管理、考勤规则查询)
-- - 数据时效: 长期有效(需保留历史数据)
-- 
-- 🔗 关联关系:
-- - 关联班次表(t_attendance_shift): 班次组包含的班次
-- - 关联考勤规则表(t_attendance_rule): 班次组的考勤规则
-- - 关联加班规则表(t_attendance_overtime_rule): 班次组的加班规则
-- - 关联员工表(t_common_employee): 班次组成员
-- - 关联部门表(t_common_department): 班次组适用部门
-- - 关联职位表(t_common_position): 班次组适用职位
-- 
-- ================================================================

-- 如果表已存在,先删除(开发环境)
-- DROP TABLE IF EXISTS `t_attendance_shift_group`;

-- 创建班次组配置表
CREATE TABLE IF NOT EXISTS `t_attendance_shift_group` (
    
    -- ================================================================
    -- 主键和唯一标识
    -- ================================================================
    `group_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '班次组ID(主键)',
    `group_code` VARCHAR(100) NOT NULL COMMENT '班次组编码(唯一标识):如DEVELOPMENT_TEAM_A',
    
    -- ================================================================
    -- 基本信息
    -- ================================================================
    `group_name` VARCHAR(200) NOT NULL COMMENT '班次组名称:如"研发A组"',
    `group_name_en` VARCHAR(200) COMMENT '班次组英文名称:如"Development Team A"',
    `group_description` TEXT COMMENT '班次组描述:详细说明班次组的用途和特点',
    `group_type` VARCHAR(50) NOT NULL DEFAULT 'NORMAL' COMMENT '班次组类型:NORMAL-普通组,TEMPLATE-模板组,DEFAULT-默认组,SPECIAL-特殊组',
    `group_category` VARCHAR(50) NOT NULL DEFAULT 'DEPARTMENT' COMMENT '班次组分类:DEPARTMENT-部门组,POSITION-职位组,PROJECT-项目组,SHIFT-班次组,SPECIAL-特殊组',
    
    -- ================================================================
    -- 班次配置
    -- ================================================================
    `primary_shift_id` BIGINT COMMENT '主要班次ID:班次组的主要班次,用于默认排班',
    `secondary_shift_id` BIGINT COMMENT '次要班次ID:班次组的次要班次,用于替补排班',
    `shift_ids` JSON COMMENT '班次ID集合(JSON数组):班次组包含的所有班次ID',
    `shift_rotation_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用轮班:1-启用轮班,0-不启用轮班',
    `rotation_rule` VARCHAR(200) COMMENT '轮班规则:轮班的具体规则说明',
    `default_schedule_type` VARCHAR(50) NOT NULL DEFAULT 'AUTO' COMMENT '默认排班类型:AUTO-自动排班,MANUAL-手动排班,IMPORT-导入排班',
    
    -- ================================================================
    -- 考勤规则
    -- ================================================================
    `attendance_rule_id` BIGINT COMMENT '考勤规则ID:班次组适用的考勤规则',
    `overtime_rule_id` BIGINT COMMENT '加班规则ID:班次组适用的加班规则',
    `leave_rule_id` BIGINT COMMENT '请假规则ID:班次组适用的请假规则',
    `exception_rule_id` BIGINT COMMENT '异常规则ID:班次组适用的异常处理规则',
    
    -- ================================================================
    -- 成员管理
    -- ================================================================
    `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数量:班次组当前成员总数',
    `employee_ids` JSON COMMENT '员工ID集合(JSON数组):班次组包含的所有员工ID',
    `department_ids` JSON COMMENT '部门ID集合(JSON数组):班次组适用的所有部门ID',
    `position_ids` JSON COMMENT '职位ID集合(JSON数组):班次组适用的所有职位ID',
    `employee_type_filter` JSON COMMENT '员工类型过滤(JSON数组):FULL_TIME-全职,PART_TIME-兼职,INTERN-实习生等',
    
    -- ================================================================
    -- 继承关系
    -- ================================================================
    `parent_group_id` BIGINT COMMENT '父班次组ID:继承父班次组的配置',
    `inherit_settings` JSON COMMENT '继承设置(JSON对象):指定哪些配置需要继承',
    `child_group_count` INT NOT NULL DEFAULT 0 COMMENT '子班次组数量:直接子班次组的数量',
    `inheritance_level` INT NOT NULL DEFAULT 1 COMMENT '继承层级:1-顶级,2-二级,以此类推',
    
    -- ================================================================
    -- 排班设置
    -- ================================================================
    `schedule_generation_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用排班生成:1-启用,0-禁用',
    `auto_assign_new_employees` TINYINT NOT NULL DEFAULT 1 COMMENT '是否自动分配新员工:1-自动分配,0-手动分配',
    `schedule_preview_days` INT NOT NULL DEFAULT 30 COMMENT '排班预览天数:默认预览未来30天的排班',
    
    -- ================================================================
    -- 工作日和假期设置
    -- ================================================================
    `workday_schedule_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用工作日排班:1-启用,0-禁用',
    `holiday_schedule_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用节假日排班:1-启用,0-禁用',
    `weekend_schedule_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用周末排班:1-启用,0-禁用',
    `custom_date_schedules` JSON COMMENT '自定义日期排班(JSON对象):特定日期的排班安排',
    
    -- ================================================================
    -- 通知和提醒
    -- ================================================================
    `notification_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用通知:1-启用,0-禁用',
    `schedule_reminder_days` INT NOT NULL DEFAULT 7 COMMENT '排班提醒天数:默认提前7天提醒',
    `notification_channels` JSON COMMENT '通知渠道(JSON数组):SMS-短信,EMAIL-邮件,WECHAT-微信,APP-App推送',
    `reminder_template` VARCHAR(200) COMMENT '提醒模板:排班提醒的通知模板',
    
    -- ================================================================
    -- 权限和访问控制
    -- ================================================================
    `access_control_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用访问控制:1-启用,0-禁用',
    `view_permission_roles` JSON COMMENT '查看权限角色(JSON数组):允许查看此班次组的角色',
    `manage_permission_roles` JSON COMMENT '管理权限角色(JSON数组):允许管理此班次组的角色',
    `member_manage_permission` VARCHAR(50) NOT NULL DEFAULT 'MANAGER_ONLY' COMMENT '成员管理权限:MANAGER_ONLY-仅管理者,LEADER_APPROVAL-组长审批,SELF_SERVICE-自助服务',
    
    -- ================================================================
    -- 状态和控制
    -- ================================================================
    `group_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '班次组状态:ACTIVE-激活,INACTIVE-停用,DRAFT-草稿,ARCHIVED-归档',
    `is_template` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为模板:1-是模板,0-不是模板',
    `template_source_group_id` BIGINT COMMENT '模板源班次组ID:如果是从模板创建的,记录源模板ID',
    `effective_date` DATE COMMENT '生效日期:班次组配置的生效日期',
    `expiry_date` DATE COMMENT '失效日期:班次组配置的失效日期',
    
    -- ================================================================
    -- 审批流程
    -- ================================================================
    `require_approval` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要审批:1-需要审批,0-不需要审批',
    `approval_workflow_id` BIGINT COMMENT '审批工作流ID:关联的审批工作流',
    `approval_level` INT NOT NULL DEFAULT 1 COMMENT '审批层级:1-一级审批,2-二级审批,3-三级审批',
    
    -- ================================================================
    -- 统计信息
    -- ================================================================
    `total_scheduled_days` INT NOT NULL DEFAULT 0 COMMENT '总排班天数:累计排班的总天数',
    `total_work_hours` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总工作时长:累计的工作时长(小时)',
    `average_member_count` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平均成员数:平均每天的成员数量',
    `schedule_conflict_count` INT NOT NULL DEFAULT 0 COMMENT '排班冲突数:检测到的排班冲突次数',
    `last_schedule_generated_time` DATETIME COMMENT '最后排班生成时间:最后一次生成排班的时间',
    `last_member_assigned_time` DATETIME COMMENT '最后成员分配时间:最后一次分配成员的时间',
    
    -- ================================================================
    -- 备注和扩展
    -- ================================================================
    `remark` VARCHAR(500) COMMENT '备注信息:班次组配置的额外说明',
    `extended_attributes` JSON COMMENT '扩展属性(JSON格式):存储业务特定的额外字段',
    `attachment_urls` JSON COMMENT '附件URL(JSON数组):班次组相关的附件地址',
    
    -- ================================================================
    -- 标准审计字段
    -- ================================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(用于乐观锁):防止并发修改冲突',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间:记录首次创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间:记录最后修改时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID:记录谁创建了此班次组配置',
    `update_user_id` BIGINT COMMENT '更新人用户ID:记录谁最后修改了此班次组配置',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除(逻辑删除)',
    
    -- ================================================================
    -- 主键和索引定义
    -- ================================================================
    PRIMARY KEY (`group_id`),
    
    -- 唯一索引:确保班次组编码全局唯一
    UNIQUE KEY `uk_group_code` (`group_code`),
    
    -- 普通索引:优化常用查询条件
    INDEX `idx_group_name` (`group_name`),
    INDEX `idx_group_type` (`group_type`),
    INDEX `idx_group_category` (`group_category`),
    INDEX `idx_primary_shift_id` (`primary_shift_id`),
    INDEX `idx_attendance_rule_id` (`attendance_rule_id`),
    INDEX `idx_group_status` (`group_status`),
    INDEX `idx_is_template` (`is_template`),
    INDEX `idx_parent_group_id` (`parent_group_id`),
    
    -- 复合索引:优化复杂查询
    INDEX `idx_type_status` (`group_type`, `group_status`),
    INDEX `idx_category_status` (`group_category`, `group_status`),
    INDEX `idx_status_template` (`group_status`, `is_template`),
    
    -- 时间索引:审计追踪和时间范围查询优化(降序索引)
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_update_time` (`update_time` DESC),
    
    -- 逻辑删除索引:优化未删除数据查询
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-班次组配置表';

-- ================================================================
-- 初始化数据
-- ================================================================
-- 插入企业级标准班次组配置数据

INSERT INTO `t_attendance_shift_group` (
    `group_code`, `group_name`, `group_name_en`, `group_description`,
    `group_type`, `group_category`,
    `primary_shift_id`, `secondary_shift_id`, `shift_ids`, `shift_rotation_enabled`, `rotation_rule`, `default_schedule_type`,
    `attendance_rule_id`, `overtime_rule_id`, `leave_rule_id`, `exception_rule_id`,
    `member_count`, `employee_ids`, `department_ids`, `position_ids`, `employee_type_filter`,
    `parent_group_id`, `inherit_settings`, `child_group_count`, `inheritance_level`,
    `schedule_generation_enabled`, `auto_assign_new_employees`, `schedule_preview_days`,
    `workday_schedule_enabled`, `holiday_schedule_enabled`, `weekend_schedule_enabled`, `custom_date_schedules`,
    `notification_enabled`, `schedule_reminder_days`, `notification_channels`, `reminder_template`,
    `access_control_enabled`, `view_permission_roles`, `manage_permission_roles`, `member_manage_permission`,
    `group_status`, `is_template`, `template_source_group_id`, `effective_date`, `expiry_date`,
    `require_approval`, `approval_workflow_id`, `approval_level`,
    `total_scheduled_days`, `total_work_hours`, `average_member_count`, `schedule_conflict_count`, `last_schedule_generated_time`, `last_member_assigned_time`,
    `remark`, `extended_attributes`, `attachment_urls`,
    `create_user_id`, `update_user_id`
) VALUES 

-- 1. 研发部门默认班次组
(
    'DEVELOPMENT_DEFAULT', '研发部门默认班次组', 'Development Default Group', '研发部门的默认班次组配置,包含标准工作班次',
    'DEFAULT', 'DEPARTMENT',
    1, NULL, '[1,2,3]', 0, NULL, 'AUTO',
    1, 1, 1, 1,
    50, '[1001,1002,1003,1004,1005]', '[101]', '[201,202,203]', '["FULL_TIME"]',
    NULL, '{"inherit_shifts": true, "inherit_rules": true}', 3, 1,
    1, 1, 30,
    1, 0, 0, NULL,
    1, 7, '["EMAIL","APP"]', 'SCHEDULE_REMINDER_TEMPLATE',
    1, '["ADMIN","MANAGER"]', '["ADMIN","DEPARTMENT_MANAGER"]', 'MANAGER_ONLY',
    'ACTIVE', 0, NULL, '2025-01-01', NULL,
    1, 101, 2,
    0, 0.00, 0.00, 0, NULL, NULL,
    '研发部门默认班次组配置', NULL, NULL,
    1, 1
),

-- 2. 销售部门班次组
(
    'SALES_TEAM_A', '销售A组', 'Sales Team A', '销售部门A组的班次配置,包含弹性工作时间',
    'NORMAL', 'DEPARTMENT',
    4, 5, '[4,5]', 0, NULL, 'AUTO',
    2, 2, 1, 1,
    20, '[2001,2002,2003,2004,2005]', '[102]', '[204]', '["FULL_TIME"]',
    NULL, NULL, 0, 1,
    1, 1, 30,
    1, 1, 1, NULL,
    1, 5, '["SMS","APP"]', 'SALES_SCHEDULE_REMINDER',
    1, '["ADMIN","MANAGER","TEAM_LEADER"]', '["ADMIN","DEPARTMENT_MANAGER"]', 'LEADER_APPROVAL',
    'ACTIVE', 0, NULL, '2025-01-01', NULL,
    0, NULL, 1,
    0, 0.00, 0.00, 0, NULL, NULL,
    '销售A组班次配置', NULL, NULL,
    1, 1
),

-- 3. 客服部门轮班组
(
    'CUSTOMER_SERVICE_ROTATION', '客服轮班组', 'Customer Service Rotation Group', '客服部门的轮班制班次组,包含早班、中班、晚班',
    'NORMAL', 'SHIFT',
    6, 7, '[6,7,8]', 1, '三班两运转', 'AUTO',
    3, 3, 1, 1,
    30, '[3001,3002,3003,3004,3005]', '[103]', '[205]', '["FULL_TIME"]',
    NULL, NULL, 0, 1,
    1, 1, 30,
    1, 1, 1, NULL,
    1, 3, '["SMS","APP","WECHAT"]', 'ROTATION_REMINDER',
    1, '["ADMIN","MANAGER","TEAM_LEADER"]', '["ADMIN","DEPARTMENT_MANAGER"]', 'SELF_SERVICE',
    'ACTIVE', 0, NULL, '2025-01-01', NULL,
    1, 102, 2,
    0, 0.00, 0.00, 0, NULL, NULL,
    '客服部门轮班制配置', '{"rotation_cycle": "3 days"}', NULL,
    1, 1
),

-- 4. 研发部门弹性工作组
(
    'DEVELOPMENT_FLEXIBLE', '研发弹性工作组', 'Development Flexible Group', '研发部门的弹性工作制班次组',
    'NORMAL', 'SPECIAL',
    9, NULL, '[9]', 0, NULL, 'MANUAL',
    4, 4, 1, 1,
    15, '[4001,4002,4003,4004,4005]', '[101]', '[206]', '["FULL_TIME","PART_TIME"]',
    1, '{"inherit_rules": true}', 0, 2,
    1, 0, 15,
    1, 0, 0, NULL,
    1, 2, '["EMAIL","APP"]', 'FLEXIBLE_REMINDER',
    1, '["ADMIN","MANAGER"]', '["ADMIN","DEPARTMENT_MANAGER"]', 'MANAGER_ONLY',
    'ACTIVE', 0, NULL, '2025-06-01', NULL,
    1, 103, 1,
    0, 0.00, 0.00, 0, NULL, NULL,
    '研发部门弹性工作制', '{"flexible_hours": "9:00-18:00 with flexible start/end"}', NULL,
    1, 1
),

-- 5. 管理层班次组
(
    'MANAGEMENT_TEAM', '管理层班次组', 'Management Team Group', '公司管理层的班次组配置',
    'NORMAL', 'POSITION',
    10, NULL, '[10]', 0, NULL, 'MANUAL',
    5, 5, 1, 1,
    10, '[5001,5002,5003,5004,5005]', '[101,102,103]', '[207]', '["FULL_TIME"]',
    NULL, NULL, 2, 1,
    1, 0, 60,
    1, 1, 1, NULL,
    1, 1, '["EMAIL","APP","WECHAT"]', 'MANAGEMENT_REMINDER',
    1, '["ADMIN","CEO","CTO"]', '["ADMIN","CEO"]', 'MANAGER_ONLY',
    'ACTIVE', 0, NULL, '2025-01-01', NULL,
    1, 104, 3,
    0, 0.00, 0.00, 0, NULL, NULL,
    '管理层班次组配置', '{"executive_level": "senior"}', NULL,
    1, 1
),

-- 6. 项目专项组模板
(
    'PROJECT_TEAM_TEMPLATE', '项目专项组模板', 'Project Team Template', '用于创建项目专项组的模板配置',
    'TEMPLATE', 'PROJECT',
    1, NULL, '[1]', 0, NULL, 'AUTO',
    1, 1, 1, 1,
    0, NULL, NULL, NULL, '["FULL_TIME"]',
    NULL, NULL, 0, 1,
    1, 1, 30,
    1, 0, 0, NULL,
    1, 7, '["EMAIL","APP"]', 'PROJECT_REMINDER',
    1, '["ADMIN","PROJECT_MANAGER"]', '["ADMIN","PROJECT_MANAGER"]', 'MANAGER_ONLY',
    'ACTIVE', 1, NULL, '2025-01-01', NULL,
    0, NULL, 1,
    0, 0.00, 0.00, 0, NULL, NULL,
    '项目专项组模板', '{"template_for": "short_term_projects"}', NULL,
    1, 1
),

-- 7. 实习生班次组
(
    'INTERN_GROUP', '实习生班次组', 'Intern Group', '实习生的班次组配置',
    'NORMAL', 'DEPARTMENT',
    11, NULL, '[11]', 0, NULL, 'AUTO',
    6, 6, 1, 1,
    25, '[6001,6002,6003,6004,6005]', '[101,102,103]', '[208]', '["INTERN"]',
    NULL, '{"inherit_rules": true}', 0, 1,
    1, 1, 15,
    1, 0, 0, NULL,
    1, 1, '["EMAIL","APP"]', 'INTERN_REMINDER',
    1, '["ADMIN","MANAGER","MENTOR"]', '["ADMIN","DEPARTMENT_MANAGER"]', 'LEADER_APPROVAL',
    'ACTIVE', 0, NULL, '2025-01-01', NULL,
    0, NULL, 1,
    0, 0.00, 0.00, 0, NULL, NULL,
    '实习生班次组配置', '{"internship_period": "3 months"}', NULL,
    1, 1
),

-- 8. 夜班安保组
(
    'NIGHT_SECURITY', '夜班安保组', 'Night Security Group', '夜间安保人员的班次组配置',
    'NORMAL', 'SPECIAL',
    12, NULL, '[12]', 0, NULL, 'AUTO',
    7, 7, 1, 1,
    8, '[7001,7002,7003,7004,7005]', '[104]', '[209]', '["FULL_TIME"]',
    NULL, NULL, 0, 1,
    1, 1, 30,
    1, 1, 1, '{"2025-12-24": 13}',
    1, 1, '["SMS","APP"]', 'SECURITY_REMINDER',
    1, '["ADMIN","SECURITY_MANAGER"]', '["ADMIN","SECURITY_MANAGER"]', 'MANAGER_ONLY',
    'ACTIVE', 0, NULL, '2025-01-01', NULL,
    1, 105, 2,
    0, 0.00, 0.00, 0, NULL, NULL,
    '夜班安保人员班次组', '{"security_level": "high"}', NULL,
    1, 1
);

-- ================================================================
-- 使用示例
-- ================================================================

-- 示例1: 查询所有激活的班次组
-- SELECT * FROM t_attendance_shift_group 
-- WHERE group_status = 'ACTIVE' 
--   AND deleted_flag = 0
-- ORDER BY group_name;

-- 示例2: 查询指定类型的班次组
-- SELECT * FROM t_attendance_shift_group 
-- WHERE group_type = 'NORMAL' 
--   AND group_category = 'DEPARTMENT'
--   AND deleted_flag = 0
-- ORDER BY create_time DESC;

-- 示例3: 查询包含特定班次的班次组
-- SELECT * FROM t_attendance_shift_group 
-- WHERE JSON_CONTAINS(shift_ids, '1')
--   AND deleted_flag = 0;

-- 示例4: 查询指定部门的班次组
-- SELECT * FROM t_attendance_shift_group 
-- WHERE JSON_CONTAINS(department_ids, '101')
--   AND deleted_flag = 0;

-- 示例5: 统计各类型班次组的数量
-- SELECT 
--     group_type,
--     group_category,
--     COUNT(*) as group_count,
--     SUM(member_count) as total_members
-- FROM t_attendance_shift_group 
-- WHERE deleted_flag = 0
-- GROUP BY group_type, group_category;

-- 示例6: 查询模板班次组
-- SELECT * FROM t_attendance_shift_group 
-- WHERE is_template = 1 
--   AND deleted_flag = 0
-- ORDER BY create_time DESC;

-- 示例7: 查询父子班次组关系
-- SELECT 
--     parent.group_name as parent_group,
--     child.group_name as child_group,
--     child.inheritance_level
-- FROM t_attendance_shift_group parent
-- JOIN t_attendance_shift_group child ON parent.group_id = child.parent_group_id
-- WHERE parent.deleted_flag = 0 AND child.deleted_flag = 0;

-- 示例8: 查询需要审批的班次组变更
-- SELECT * FROM t_attendance_shift_group 
-- WHERE require_approval = 1 
--   AND group_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- ================================================================
-- 索引优化说明
-- ================================================================
-- 
-- 1. uk_group_code: 唯一索引,确保班次组编码全局唯一
--    - 用途: 班次组编码查询、数据去重
-- 
-- 2. idx_group_name: 普通索引,按班次组名称查询优化
--    - 用途: 班次组名称搜索、模糊查询
-- 
-- 3. idx_group_type: 普通索引,按班次组类型查询优化
--    - 用途: 按类型筛选班次组(普通、模板、默认等)
-- 
-- 4. idx_group_category: 普通索引,按班次组分类查询优化
--    - 用途: 按分类筛选班次组(部门、职位、项目等)
-- 
-- 5. idx_primary_shift_id: 普通索引,按主要班次ID查询优化
--    - 用途: 查询使用特定班次的班次组
-- 
-- 6. idx_attendance_rule_id: 普通索引,按考勤规则ID查询优化
--    - 用途: 查询使用特定考勤规则的班次组
-- 
-- 7. idx_group_status: 普通索引,按班次组状态查询优化
--    - 用途: 按状态筛选班次组(激活、停用、草稿等)
-- 
-- 8. idx_is_template: 普通索引,按模板标识查询优化
--    - 用途: 区分模板和普通班次组
-- 
-- 9. idx_parent_group_id: 普通索引,按父班次组ID查询优化
--    - 用途: 查询父子班次组关系、继承关系
-- 
-- 10. idx_type_status: 复合索引,优化类型和状态组合查询
--     - 用途: 按类型和状态筛选班次组
-- 
-- 11. idx_category_status: 复合索引,优化分类和状态组合查询
--     - 用途: 按分类和状态筛选班次组
-- 
-- 12. idx_status_template: 复合索引,优化状态和模板组合查询
--     - 用途: 查询特定状态的模板或普通班次组
-- 
-- 13. idx_create_time: 降序索引,优化创建时间查询和排序
--     - 用途: 最新班次组查询、审计追踪
-- 
-- 14. idx_update_time: 降序索引,优化更新时间查询和排序
--     - 用途: 最近修改班次组查询、变更追踪
-- 
-- 15. idx_deleted_flag: 普通索引,优化未删除数据查询
--     - 用途: 逻辑删除优化、有效数据过滤
-- 
-- ================================================================
-- 维护建议
-- ================================================================
-- 
-- 1. 数据维护:
--    - 定期清理已停用的班次组数据
--    - 更新班次组成员数量统计
--    - 审核班次组配置的合理性
-- 
-- 2. 性能优化:
--    - 定期分析慢查询,优化索引策略
--    - 监控班次组查询的性能指标
--    - 对高频查询的班次组建立缓存
-- 
-- 3. 数据一致性:
--    - 确保班次组与班次的关联关系正确
--    - 验证班次组成员的有效性
--    - 检查父子班次组的继承关系
-- 
-- 4. 业务规则:
--    - 定期审查班次组配置,确保符合业务需求
--    - 根据组织架构变化调整班次组
--    - 监控班次组对排班的影响
-- 
-- 5. 安全管理:
--    - 班次组配置需要审批流程控制
--    - 敏感班次组修改需要记录审计日志
--    - 定期备份班次组配置数据
-- 
-- ================================================================
-- 定时任务建议
-- ================================================================
-- 
-- 1. 班次组成员统计(每日执行):
--    - 统计各班次组的实际成员数量
--    - 更新member_count字段
--    - 生成成员统计报表
-- 
-- 2. 班次组状态检查(每日执行):
--    - 检查班次组的有效期
--    - 自动更新过期班次组状态
--    - 发送即将过期提醒
-- 
-- 3. 班次组继承关系验证(每周执行):
--    - 验证父子班次组的继承关系
--    - 检查继承设置的合理性
--    - 修复不一致的继承关系
-- 
-- 4. 班次组使用情况分析(每月执行):
--    - 分析各班次组的使用频率
--    - 统计排班冲突情况
--    - 生成班次组使用报告
-- 
-- 5. 班次组配置审计(每季度执行):
--    - 审计班次组配置的合规性
--    - 检查班次组与实际业务的匹配度
--    - 提出优化建议
-- 
-- 6. 模板班次组更新(每年执行):
--    - 根据业务变化更新模板配置
--    - 发布新版模板班次组
--    - 通知相关部门使用新版模板
-- 
-- ================================================================
-- 注意事项
-- ================================================================
-- 
-- 1. JSON字段使用:
--    - shift_ids: 存储班次ID数组
--    - employee_ids: 存储员工ID数组
--    - department_ids: 存储部门ID数组
--    - position_ids: 存储职位ID数组
--    - employee_type_filter: 存储员工类型过滤条件
--    - notification_channels: 存储通知渠道
--    - view_permission_roles: 存储查看权限角色
--    - manage_permission_roles: 存储管理权限角色
--    - custom_date_schedules: 存储自定义日期排班
--    - inherit_settings: 存储继承设置
--    - extended_attributes: 存储扩展属性
--    - attachment_urls: 存储附件URL
-- 
-- 2. 继承关系:
--    - parent_group_id指向父班次组
--    - inherit_settings指定继承哪些配置
--    - inheritance_level表示继承层级
--    - child_group_count记录子班次组数量
-- 
-- 3. 权限控制:
--    - access_control_enabled启用访问控制
--    - view_permission_roles指定查看权限
--    - manage_permission_roles指定管理权限
--    - member_manage_permission设置成员管理权限
-- 
-- 4. 通知提醒:
--    - notification_enabled启用通知
--    - schedule_reminder_days设置提醒天数
--    - notification_channels指定通知渠道
--    - reminder_template指定提醒模板
-- 
-- 5. 审批流程:
--    - require_approval是否需要审批
--    - approval_workflow_id关联审批工作流
--    - approval_level审批层级
-- 
-- 6. 统计信息:
--    - total_scheduled_days总排班天数
--    - total_work_hours总工作时长
--    - average_member_count平均成员数
--    - schedule_conflict_count排班冲突数
-- 
-- 7. 数据一致性:
--    - 确保班次组编码全局唯一
--    - 验证班次ID的有效性
--    - 检查部门和职位ID的有效性
--    - 确保父子关系的合理性
-- 
-- 8. 性能考虑:
--    - 班次组查询频率较高,需要合理使用索引
--    - 对常用查询条件建立缓存
--    - 定期归档历史班次组数据
-- 
-- ================================================================
-- 版本历史
-- ================================================================
-- v1.0.0 - 2025-12-08
-- - 初始版本创建
-- - 支持班次组基础信息管理
-- - 支持班次组成员管理
-- - 支持班次组规则配置
-- - 支持班次组权限控制
-- - 支持班次组继承关系
-- - 支持班次组模板管理
-- - 提供8条示例数据
-- - 包含完整的索引优化方案
-- - 提供详细的使用示例和维护建议
-- 
-- ================================================================
-- END OF FILE
-- ================================================================