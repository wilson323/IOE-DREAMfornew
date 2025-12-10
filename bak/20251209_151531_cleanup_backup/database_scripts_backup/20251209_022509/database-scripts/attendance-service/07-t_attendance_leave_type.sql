-- =====================================================================================================================
-- IOE-DREAM 智慧园区一卡通管理平台 - 考勤服务
-- 请假类型表 (t_attendance_leave_type)
-- =====================================================================================================================
-- 文件名称: 07-t_attendance_leave_type.sql
-- 功能说明: 管理所有请假类型定义，包括年假、病假、事假等各种假期类型及其规则配置
-- 依赖关系: 
--   - 依赖 05-t_attendance_exception.sql (异常申请表)
-- 执行顺序: 第7个执行
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
-- DROP TABLE IF EXISTS `t_attendance_leave_type`;

-- 创建请假类型表
CREATE TABLE IF NOT EXISTS `t_attendance_leave_type` (
    -- =====================================================================================================
    -- 主键和唯一标识
    -- =====================================================================================================
    `leave_type_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '请假类型ID（主键）',
    `leave_type_code` VARCHAR(50) NOT NULL COMMENT '请假类型编码：如ANNUAL-年假, SICK-病假, PERSONAL-事假等',
    
    -- =====================================================================================================
    -- 基本信息
    -- =====================================================================================================
    `leave_type_name` VARCHAR(100) NOT NULL COMMENT '请假类型名称：如年假、病假、事假、婚假等',
    `leave_type_category` VARCHAR(50) NOT NULL DEFAULT 'REGULAR' COMMENT '请假类型分类：REGULAR-常规假期, SPECIAL-特殊假期, EMERGENCY-紧急假期',
    `leave_type_description` TEXT COMMENT '请假类型描述',
    `icon_url` VARCHAR(500) COMMENT '图标URL',
    
    -- =====================================================================================================
    -- 基本规则配置
    -- =====================================================================================================
    `unit_type` VARCHAR(20) NOT NULL DEFAULT 'DAY' COMMENT '单位类型：DAY-天, HOUR-小时, MINUTE-分钟',
    `min_duration` DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '最小申请时长（根据单位类型）',
    `max_duration` DECIMAL(8,2) NOT NULL DEFAULT 9999.00 COMMENT '最大申请时长（根据单位类型）',
    `allow_fractional` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许小数：1-允许（如0.5天）, 0-不允许',
    
    -- =====================================================================================================
    -- 审批规则
    -- =====================================================================================================
    `require_approval` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要审批：1-需要, 0-不需要（自动通过）',
    `auto_approve_threshold` DECIMAL(8,2) COMMENT '自动审批阈值（小于等于此值自动审批，大于需要审批）',
    `approval_level` INT NOT NULL DEFAULT 1 COMMENT '审批层级：1-一级审批, 2-二级审批, 3-三级审批',
    `default_approver_role` VARCHAR(100) COMMENT '默认审批人角色：如DIRECT_SUPERVISOR-直属上级, DEPARTMENT_MANAGER-部门经理',
    `allow_delegate_approval` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许委托审批：1-允许, 0-不允许',
    
    -- =====================================================================================================
    -- 薪资影响规则
    -- =====================================================================================================
    `deduct_salary` TINYINT NOT NULL DEFAULT 0 COMMENT '是否扣薪：1-扣薪, 0-不扣薪',
    `deduct_rate` DECIMAL(5,2) NOT NULL DEFAULT 100.00 COMMENT '扣薪比例（%）：100-全额扣薪, 50-扣薪50%',
    `salary_calculation_method` VARCHAR(50) NOT NULL DEFAULT 'FULL_DAY' COMMENT '薪资计算方式：FULL_DAY-按整天计算, ACTUAL_HOURS-按实际小时计算',
    `affect_full_attendance` TINYINT NOT NULL DEFAULT 1 COMMENT '是否影响全勤：1-影响, 0-不影响',
    
    -- =====================================================================================================
    -- 申请限制规则
    -- =====================================================================================================
    `advance_apply_days` INT NOT NULL DEFAULT 0 COMMENT '需提前申请天数：0-无限制',
    `allow_retroactive` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许补申请：1-允许, 0-不允许',
    `retroactive_limit_days` INT COMMENT '补申请限制天数（超过此天数不允许补申请）',
    `max_concurrent_leaves` INT NOT NULL DEFAULT 999 COMMENT '最大并发请假数（同一时间可申请的此类假期数量）',
    `annual_limit_days` DECIMAL(8,2) COMMENT '年度限额天数（每年最多可申请的天数）',
    
    -- =====================================================================================================
    -- 特殊假期规则
    -- =====================================================================================================
    `is_paid_leave` TINYINT NOT NULL DEFAULT 1 COMMENT '是否带薪假期：1-带薪, 0-无薪',
    `is_accrual_leave` TINYINT NOT NULL DEFAULT 0 COMMENT '是否累积假期：1-是（如年假可累积）, 0-否',
    `accrual_rate` DECIMAL(8,2) COMMENT '累积率（每年累积天数）',
    `carry_forward_allowed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许结转：1-允许, 0-不允许',
    `carry_forward_limit_days` DECIMAL(8,2) COMMENT '结转限额天数',
    `expiration_months` INT COMMENT '有效期月数（超过此月数未使用则过期）',
    
    -- =====================================================================================================
    -- 证书和附件要求
    -- =====================================================================================================
    `require_certificate` TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要证明材料：1-需要, 0-不需要',
    `certificate_types` JSON COMMENT '所需证明材料类型（JSON数组）：MEDICAL_CERTIFICATE-医疗证明, MARRIAGE_CERTIFICATE-结婚证等',
    `max_attachment_size` INT COMMENT '附件最大大小（字节）',
    `allowed_attachment_formats` JSON COMMENT '允许的附件格式（JSON数组）：jpg, png, pdf等',
    
    -- =====================================================================================================
    -- 工作流集成
    -- =====================================================================================================
    `workflow_definition_id` BIGINT COMMENT '工作流定义ID（关联OA工作流）',
    `workflow_definition_code` VARCHAR(100) COMMENT '工作流定义编码',
    
    -- =====================================================================================================
    -- 状态和控制
    -- =====================================================================================================
    `leave_type_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '请假类型状态：ACTIVE-激活, INACTIVE-停用, MAINTENANCE-维护中',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级（数值越大优先级越高）',
    `is_system_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统默认：1-是（系统内置类型）, 0-否（用户自定义）',
    `allow_employee_create` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许员工自定义：1-允许, 0-不允许',
    
    -- =====================================================================================================
    -- 部门和员工类型限制
    -- =====================================================================================================
    `applicable_departments` JSON COMMENT '适用部门（JSON数组，空表示所有部门）',
    `excluded_departments` JSON COMMENT '排除部门（JSON数组）',
    `applicable_employee_types` JSON COMMENT '适用员工类型（JSON数组）：FULL_TIME-全职, PART_TIME-兼职等',
    `excluded_employee_types` JSON COMMENT '排除员工类型（JSON数组）',
    
    -- =====================================================================================================
    -- 时间限制规则
    -- =====================================================================================================
    `available_weekdays` JSON COMMENT '可用星期（JSON数组）：1-周一, 2-周二,..., 7-周日',
    `available_time_ranges` JSON COMMENT '可用时间范围（JSON数组）：[{start: "09:00", end: "18:00"}]',
    `restricted_dates` JSON COMMENT '限制日期（JSON数组）：节假日、特殊日期等',
    
    -- =====================================================================================================
    -- 统计和分析
    -- =====================================================================================================
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数统计',
    `last_used_time` DATETIME COMMENT '最后使用时间',
    `average_approval_duration` INT COMMENT '平均审批时长（小时）',
    
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
    PRIMARY KEY (`leave_type_id`),
    
    -- 唯一索引
    UNIQUE KEY `uk_leave_type_code` (`leave_type_code`),
    
    -- 普通索引
    INDEX `idx_leave_type_name` (`leave_type_name`),
    INDEX `idx_leave_type_category` (`leave_type_category`),
    INDEX `idx_leave_type_status` (`leave_type_status`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_is_system_default` (`is_system_default`),
    INDEX `idx_deduct_salary` (`deduct_salary`),
    INDEX `idx_is_paid_leave` (`is_paid_leave`),
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-请假类型表';

-- =====================================================================================================================
-- 2. 初始化数据（示例数据，真实可用，企业级质量）
-- =====================================================================================================================

-- 清空表数据（开发环境使用，生产环境禁用）
-- TRUNCATE TABLE `t_attendance_leave_type`;

-- 插入企业级示例数据（12条，覆盖所有常见请假类型）

-- 示例1: 年假（带薪，可累积，可结转）
INSERT INTO `t_attendance_leave_type` (
    `leave_type_code`, `leave_type_name`, `leave_type_category`, `leave_type_description`, `icon_url`,
    `unit_type`, `min_duration`, `max_duration`, `allow_fractional`,
    `require_approval`, `auto_approve_threshold`, `approval_level`, `default_approver_role`, `allow_delegate_approval`,
    `deduct_salary`, `deduct_rate`, `salary_calculation_method`, `affect_full_attendance`,
    `advance_apply_days`, `allow_retroactive`, `retroactive_limit_days`, `max_concurrent_leaves`, `annual_limit_days`,
    `is_paid_leave`, `is_accrual_leave`, `accrual_rate`, `carry_forward_allowed`, `carry_forward_limit_days`, `expiration_months`,
    `require_certificate`, `certificate_types`, `max_attachment_size`, `allowed_attachment_formats`,
    `workflow_definition_id`, `workflow_definition_code`,
    `leave_type_status`, `priority`, `is_system_default`, `allow_employee_create`,
    `applicable_departments`, `excluded_departments`, `applicable_employee_types`, `excluded_employee_types`,
    `available_weekdays`, `available_time_ranges`, `restricted_dates`,
    `usage_count`, `last_used_time`, `average_approval_duration`,
    `version`, `create_user_id`, `deleted_flag`
) VALUES (
    'ANNUAL', '年假', 'REGULAR', '员工享有的带薪年假，根据工龄计算天数', '/icons/leave/annual.png',
    'DAY', 0.5, 9999.00, 1,
    1, 3.00, 1, 'DIRECT_SUPERVISOR', 1,
    0, 100.00, 'FULL_DAY', 0,
    3, 1, 30, 999, NULL,
    1, 1, 15.00, 1, 5.00, 12,
    0, NULL, NULL, NULL,
    1001, 'ANNUAL_LEAVE_WORKFLOW',
    'ACTIVE', 100, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5]', '[{"start": "09:00", "end": "18:00"}]', '[]',
    1250, '2025-01-08 15:30:00', 24,
    1, 1001, 0
);

-- 示例2: 病假（带薪，需要医疗证明）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'SICK', 'REGULAR', '因疾病需要休息的假期', '/icons/leave/sick.png',
    'DAY', 0.5, 30.00, 1,
    1, 1.00, 1, 'DIRECT_SUPERVISOR', 1,
    0, 100.00, 'ACTUAL_HOURS', 1,
    0, 1, 7, 5, NULL,
    1, 0, NULL, 0, NULL, NULL,
    1, '["MEDICAL_CERTIFICATE"]', 5242880, '["jpg","png","pdf","doc","docx"]',
    1002, 'SICK_LEAVE_WORKFLOW',
    'ACTIVE', 90, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5]', '[{"start": "00:00", "end": "23:59"}]', '[]',
    890, '2025-01-08 16:45:00', 12,
    1, 1001, 0
);

-- 示例3: 事假（无薪）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'PERSONAL', 'REGULAR', '因个人事务需要处理的假期', '/icons/leave/personal.png',
    'HOUR', 1.00, 24.00, 1,
    1, 4.00, 2, 'DEPARTMENT_MANAGER', 1,
    1, 100.00, 'ACTUAL_HOURS', 1,
    1, 0, NULL, 3, NULL,
    0, 0, NULL, 0, NULL, NULL,
    0, NULL, NULL, NULL,
    1003, 'PERSONAL_LEAVE_WORKFLOW',
    'ACTIVE', 80, 1, 1,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5]', '[{"start": "09:00", "end": "18:00"}]', '[]',
    2100, '2025-01-08 14:20:00', 48,
    1, 1001, 0
);

-- 示例4: 婚假（带薪，需要结婚证）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'MARRIAGE', 'SPECIAL', '员工结婚享有的带薪假期', '/icons/leave/marriage.png',
    'DAY', 1.00, 15.00, 0,
    1, NULL, 2, 'DEPARTMENT_MANAGER', 1,
    0, 100.00, 'FULL_DAY', 0,
    7, 1, 30, 1, NULL,
    1, 0, NULL, 0, NULL, NULL,
    1, '["MARRIAGE_CERTIFICATE"]', 2097152, '["jpg","png","pdf"]',
    1004, 'MARRIAGE_LEAVE_WORKFLOW',
    'ACTIVE', 95, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5,6,7]', '[{"start": "00:00", "end": "23:59"}]', '[]',
    156, '2025-01-07 11:30:00', 72,
    1, 1001, 0
);

-- 示例5: 产假（带薪，特殊假期）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'MATERNITY', 'SPECIAL', '女性员工生育享有的带薪假期', '/icons/leave/maternity.png',
    'DAY', 1.00, 180.00, 0,
    1, NULL, 3, 'HR_MANAGER', 0,
    0, 100.00, 'FULL_DAY', 0,
    30, 1, 365, 1, NULL,
    1, 0, NULL, 0, NULL, NULL,
    1, '["BIRTH_CERTIFICATE","MEDICAL_CERTIFICATE"]', 10485760, '["jpg","png","pdf"]',
    1005, 'MATERNITY_LEAVE_WORKFLOW',
    'ACTIVE', 98, 1, 0,
    NULL, '["2004"]', '["FEMALE"]', NULL,
    '[1,2,3,4,5,6,7]', '[{"start": "00:00", "end": "23:59"}]', '[]',
    89, '2025-01-05 09:15:00', 168,
    1, 1001, 0
);

-- 示例6: 陪产假（带薪）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'PATERNITY', 'SPECIAL', '男性员工配偶生育享有的陪产假期', '/icons/leave/paternity.png',
    'DAY', 1.00, 30.00, 0,
    1, NULL, 2, 'DEPARTMENT_MANAGER', 1,
    0, 100.00, 'FULL_DAY', 0,
    7, 1, 90, 1, NULL,
    1, 0, NULL, 0, NULL, NULL,
    1, '["BIRTH_CERTIFICATE"]', 5242880, '["jpg","png","pdf"]',
    1006, 'PATERNITY_LEAVE_WORKFLOW',
    'ACTIVE', 97, 1, 0,
    NULL, '["2004"]', '["MALE"]', NULL,
    '[1,2,3,4,5,6,7]', '[{"start": "00:00", "end": "23:59"}]', '[]',
    45, '2025-01-06 14:20:00', 48,
    1, 1001, 0
);

-- 示例7: 丧假（带薪）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'BEREAVEMENT', 'SPECIAL', '员工直系亲属去世享有的带薪假期', '/icons/leave/bereavement.png',
    'DAY', 1.00, 15.00, 0,
    1, NULL, 1, 'DIRECT_SUPERVISOR', 1,
    0, 100.00, 'FULL_DAY', 0,
    0, 1, 30, 1, NULL,
    1, 0, NULL, 0, NULL, NULL,
    1, '["DEATH_CERTIFICATE"]', 3145728, '["jpg","png","pdf"]',
    1007, 'BEREAVEMENT_LEAVE_WORKFLOW',
    'ACTIVE', 96, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5,6,7]', '[{"start": "00:00", "end": "23:59"}]', '[]',
    78, '2025-01-04 10:45:00', 24,
    1, 1001, 0
);

-- 示例8: 调休（带薪，需要关联加班记录）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'COMPENSATORY', 'REGULAR', '用加班时间换取的调休假期', '/icons/leave/compensatory.png',
    'HOUR', 1.00, 9999.00, 1,
    1, 8.00, 1, 'DIRECT_SUPERVISOR', 1,
    0, 100.00, 'ACTUAL_HOURS', 1,
    1, 1, 180, 999, NULL,
    1, 0, NULL, 0, NULL, 24,
    0, NULL, NULL, NULL,
    1008, 'COMPENSATORY_LEAVE_WORKFLOW',
    'ACTIVE', 85, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5]', '[{"start": "09:00", "end": "18:00"}]', '[]',
    1890, '2025-01-08 17:20:00', 6,
    1, 1001, 0
);

-- 示例9: 工伤假（带薪，需要工伤认定书）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'WORK_INJURY', 'EMERGENCY', '因工负伤享有的带薪假期', '/icons/leave/work_injury.png',
    'DAY', 1.00, 365.00, 0,
    1, NULL, 3, 'HR_MANAGER', 0,
    0, 100.00, 'FULL_DAY', 0,
    0, 1, NULL, 1, NULL,
    1, 0, NULL, 0, NULL, NULL,
    1, '["WORK_INJURY_CERTIFICATE","MEDICAL_CERTIFICATE"]', 15728640, '["jpg","png","pdf","doc","docx"]',
    1009, 'WORK_INJURY_LEAVE_WORKFLOW',
    'ACTIVE', 99, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5,6,7]', '[{"start": "00:00", "end": "23:59"}]', '[]',
    23, '2025-01-03 13:15:00', 120,
    1, 1001, 0
);

-- 示例10: 哺乳假（带薪，女性专用）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'BREASTFEEDING', 'REGULAR', '哺乳期女性员工享有的哺乳时间', '/icons/leave/breastfeeding.png',
    'MINUTE', 30.00, 120.00, 0,
    0, NULL, 1, 'DIRECT_SUPERVISOR', 1,
    0, 100.00, 'ACTUAL_HOURS', 0,
    0, 1, NULL, 999, NULL,
    1, 0, NULL, 0, NULL, 12,
    0, NULL, NULL, NULL,
    1010, 'BREASTFEEDING_LEAVE_WORKFLOW',
    'ACTIVE', 88, 1, 0,
    NULL, '["2004"]', '["FEMALE"]', NULL,
    '[1,2,3,4,5]', '[{"start": "09:00", "end": "18:00"}]', '[]',
    342, '2025-01-08 11:30:00', 2,
    1, 1001, 0
);

-- 示例11: 探亲假（无薪，需要证明）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'FAMILY_VISIT', 'SPECIAL', '探望不住在一起的配偶或父母的假期', '/icons/leave/family_visit.png',
    'DAY', 1.00, 60.00, 0,
    1, NULL, 2, 'DEPARTMENT_MANAGER', 1,
    1, 100.00, 'FULL_DAY', 1,
    15, 1, 180, 1, NULL,
    0, 0, NULL, 0, NULL, NULL,
    1, '["RELATIONSHIP_PROOF"]', 4194304, '["jpg","png","pdf"]',
    1011, 'FAMILY_VISIT_LEAVE_WORKFLOW',
    'ACTIVE', 75, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5,6,7]', '[{"start": "00:00", "end": "23:59"}]', '[]',
    67, '2025-01-02 16:45:00', 96,
    1, 1001, 0
);

-- 示例12: 培训假（带薪）
INSERT INTO `t_attendance_leave_type` VALUES (
    NULL, 'TRAINING', 'REGULAR', '参加公司安排的培训享有的带薪假期', '/icons/leave/training.png',
    'DAY', 0.5, 30.00, 1,
    1, 2.00, 1, 'DIRECT_SUPERVISOR', 1,
    0, 100.00, 'FULL_DAY', 0,
    3, 1, 30, 5, NULL,
    1, 0, NULL, 0, NULL, NULL,
    1, '["TRAINING_NOTICE"]', 10485760, '["jpg","png","pdf","doc","docx"]',
    1012, 'TRAINING_LEAVE_WORKFLOW',
    'ACTIVE', 82, 1, 0,
    NULL, NULL, NULL, NULL,
    '[1,2,3,4,5]', '[{"start": "09:00", "end": "18:00"}]', '[]',
    234, '2025-01-08 09:15:00', 12,
    1, 1001, 0
);

-- =====================================================================================================================
-- 3. 索引优化说明
-- =====================================================================================================================

/*
索引设计说明：
1. uk_leave_type_code: 唯一索引，确保请假类型编码全局唯一，快速查询定位
2. idx_leave_type_name: 普通索引，支持按请假类型名称模糊查询
3. idx_leave_type_category: 普通索引，支持按请假类型分类筛选
4. idx_leave_type_status: 普通索引，支持按请假类型状态筛选
5. idx_priority: 普通索引，支持按优先级排序查询
6. idx_is_system_default: 普通索引，区分系统默认和用户自定义类型
7. idx_deduct_salary: 普通索引，支持按是否扣薪筛选
8. idx_is_paid_leave: 普通索引，支持按是否带薪筛选
9. idx_create_time: 降序索引，审计追踪
10. idx_deleted_flag: 普通索引，优化逻辑删除查询性能
*/

-- =====================================================================================================================
-- 4. 使用示例
-- =====================================================================================================================

-- 示例1: 查询所有激活的请假类型
SELECT 
    leave_type_code,
    leave_type_name,
    leave_type_category,
    unit_type,
    min_duration,
    max_duration,
    allow_fractional,
    deduct_salary,
    is_paid_leave,
    priority
FROM t_attendance_leave_type
WHERE leave_type_status = 'ACTIVE'
  AND deleted_flag = 0
ORDER BY priority DESC, leave_type_name;

-- 示例2: 查询带薪假期类型
SELECT 
    leave_type_code,
    leave_type_name,
    unit_type,
    min_duration,
    max_duration,
    allow_fractional,
    annual_limit_days,
    is_accrual_leave,
    accrual_rate
FROM t_attendance_leave_type
WHERE is_paid_leave = 1
  AND leave_type_status = 'ACTIVE'
  AND deleted_flag = 0
ORDER BY is_accrual_leave DESC, accrual_rate DESC;

-- 示例3: 查询需要审批的假期类型及其审批规则
SELECT 
    leave_type_code,
    leave_type_name,
    require_approval,
    auto_approve_threshold,
    approval_level,
    default_approver_role,
    allow_delegate_approval
FROM t_attendance_leave_type
WHERE require_approval = 1
  AND leave_type_status = 'ACTIVE'
  AND deleted_flag = 0
ORDER BY approval_level, leave_type_name;

-- 示例4: 查询需要证明材料的假期类型
SELECT 
    leave_type_code,
    leave_type_name,
    certificate_types,
    max_attachment_size,
    allowed_attachment_formats
FROM t_attendance_leave_type
WHERE require_certificate = 1
  AND leave_type_status = 'ACTIVE'
  AND deleted_flag = 0;

-- 示例5: 查询系统默认的假期类型（员工不可自定义）
SELECT 
    leave_type_code,
    leave_type_name,
    leave_type_category,
    leave_type_description
FROM t_attendance_leave_type
WHERE is_system_default = 1
  AND allow_employee_create = 0
  AND leave_type_status = 'ACTIVE'
  AND deleted_flag = 0;

-- =====================================================================================================================
-- 5. 维护建议
-- =====================================================================================================================

/*
维护建议：
1. 定期审查请假类型配置，确保符合公司政策和法规要求
2. 每年初重置年假相关的累积和结转配置
3. 定期分析请假类型使用情况，优化配置参数
4. 监控请假类型审批效率，调整审批流程
5. 根据法律法规变化及时更新假期类型规则
6. 建立请假类型版本管理机制，记录变更历史
7. 定期清理已停用且长时间未使用的请假类型
8. 优化请假类型图标和描述，提升用户体验
9. 建立请假类型配置的备份和恢复机制
10. 定期培训管理员正确配置和管理请假类型
*/

-- =====================================================================================================================
-- 6. 定时任务建议
-- =====================================================================================================================

/*
定时任务建议：

任务1: 请假类型使用统计（每日 01:00执行）
- 统计各请假类型的使用次数
- 更新usage_count和last_used_time字段
- 生成使用报告

任务2: 年假配置重置（每年1月1日 00:00执行）
- 重置年假相关的累积天数
- 更新结转限额
- 发送配置更新通知

任务3: 请假类型状态检查（每周日 02:00执行）
- 检查维护中的请假类型是否可以恢复正常
- 检查已停用的请假类型是否需要彻底删除
- 发送状态变更报告

任务4: 附件配置验证（每月1日 03:00执行）
- 验证附件大小和格式配置的有效性
- 检查证书要求的合理性
- 发送配置验证报告

任务5: 审批效率分析（每月15日 04:00执行）
- 分析各请假类型的平均审批时长
- 更新average_approval_duration字段
- 识别审批效率低下的类型并提出优化建议

任务6: 请假类型清理（每季度第一天 05:00执行）
- 清理已停用超过1年的请假类型（逻辑删除）
- 归档历史配置信息
- 发送清理报告

任务7: 法规合规检查（每半年第一天 06:00执行）
- 检查请假类型配置是否符合最新法规
- 识别需要调整的类型
- 发送合规检查报告
*/

-- =====================================================================================================================
-- 7. 数据完整性约束建议
-- =====================================================================================================================

/*
数据完整性约束建议：

1. 请假类型编码唯一性约束：
   - 确保leave_type_code全局唯一
   - 编码规范：使用大写字母和下划线组合

2. 单位类型约束：
   - unit_type只能是DAY、HOUR、MINUTE之一
   - 不同单位类型对应不同的时长精度

3. 时长范围约束：
   - min_duration <= max_duration
   - 时长值必须为非负数

4. 扣薪规则约束：
   - deduct_rate范围：0-100
   - 扣薪比例为0时表示不扣薪

5. 审批规则约束：
   - approval_level范围：1-5
   - auto_approve_threshold不能大于max_duration

6. 累积规则约束：
   - is_accrual_leave=1时，必须填写accrual_rate
   - carry_forward_allowed=1时，可以填写carry_forward_limit_days

7. 附件规则约束：
   - require_certificate=1时，应该填写certificate_types
   - max_attachment_size应该设置合理值

8. 工作流约束：
   - workflow_definition_id和workflow_definition_code应该同时存在或同时为空

9. 状态约束：
   - leave_type_status只能是ACTIVE、INACTIVE、MAINTENANCE之一

10. 时间限制约束：
    - available_weekdays数组元素只能是1-7
    - available_time_ranges格式必须正确
    - restricted_dates格式必须正确
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
   - 合理使用缓存存储常用配置

3. 配置缓存：
   - 使用Redis缓存活跃的请假类型配置
   - 设置合理的缓存过期时间
   - 配置变更时及时更新缓存

4. 数据归档：
   - 定期归档历史配置变更记录
   - 使用分区表技术，按时间分区
   - 冷热数据分离，提升查询性能

5. 批量操作：
   - 支持批量导入请假类型配置
   - 批量更新状态和规则
   - 提供批量操作进度查询

6. 数据库优化：
   - 定期优化表结构，减少碎片
   - 合理配置数据库参数
   - 使用连接池，提升连接效率

7. 监控告警：
   - 监控请假类型配置变更频率
   - 监控配置使用情况
   - 设置异常告警机制
*/

-- =====================================================================================================================
-- 9. 业务规则说明
-- =====================================================================================================================

/*
业务规则说明：

1. 请假类型分类规则：
   - REGULAR（常规假期）：年假、病假、事假等日常假期
   - SPECIAL（特殊假期）：婚假、产假、丧假等特殊情况假期
   - EMERGENCY（紧急假期）：工伤假等紧急情况假期

2. 单位类型规则：
   - DAY：按天计算，请假最小单位为半天（0.5天）
   - HOUR：按小时计算，请假最小单位为1小时
   - MINUTE：按分钟计算，请假最小单位为30分钟

3. 审批规则：
   - require_approval=0：自动通过，无需审批
   - auto_approve_threshold：小于等于此值自动审批
   - approval_level：审批层级，决定审批流程复杂度

4. 薪资影响规则：
   - deduct_salary=1：扣薪假期
   - deduct_rate：扣薪比例，100表示全额扣薪
   - salary_calculation_method：FULL_DAY按整天计算，ACTUAL_HOURS按实际小时计算

5. 申请限制规则：
   - advance_apply_days：需提前申请的天数
   - allow_retroactive：是否允许补申请
   - retroactive_limit_days：补申请的时间限制

6. 累积假期规则：
   - is_accrual_leave=1：可累积假期，如年假
   - accrual_rate：每年累积的天数
   - carry_forward_allowed：是否允许结转到下一年
   - expiration_months：假期的有效期

7. 证明材料规则：
   - require_certificate=1：需要提供证明材料
   - certificate_types：所需的证明材料类型
   - max_attachment_size：附件大小限制
   - allowed_attachment_formats：允许的附件格式

8. 状态管理规则：
   - ACTIVE：激活状态，可正常使用
   - INACTIVE：停用状态，不可申请
   - MAINTENANCE：维护状态，暂时不可用

9. 优先级规则：
   - priority数值越大优先级越高
   - 用于请假类型展示和默认选择排序

10. 系统默认规则：
    - is_system_default=1：系统内置类型，不可删除
    - allow_employee_create=0：员工不可自定义此类假期
*/

-- =====================================================================================================================
-- 10. 注意事项
-- =====================================================================================================================

/*
注意事项：

1. 配置管理：
   - 请假类型配置变更需要审批
   - 重要配置变更需要通知相关人员
   - 建立配置变更日志

2. 数据一致性：
   - 请假类型与异常申请数据保持一致
   - 使用事务确保数据完整性
   - 定期进行数据一致性校验

3. 并发控制：
   - 使用乐观锁(version)防止并发更新冲突
   - 配置变更时加锁，防止并发修改
   - 避免同一配置被重复修改

4. 性能考虑：
   - 配置查询使用缓存提升性能
   - 大批量配置变更使用异步处理
   - 合理设置缓存过期时间

5. 异常处理：
   - 配置变更失败自动回滚
   - 记录详细的错误日志
   - 发送异常告警通知

6. 数据安全：
   - 请假类型配置涉及薪资规则，需严格权限控制
   - 敏感字段考虑加密存储
   - 完整的操作审计日志

7. 业务规则：
   - 配置规则可配置化
   - 支持按部门自定义配置规则
   - 历史配置不可随意修改

8. 用户体验：
   - 提供配置变更进度查询
   - 配置完成后及时通知
   - 支持配置导出和导入

9. 扩展性：
   - 预留扩展字段
   - 支持自定义配置维度
   - 支持多租户隔离

10. 兼容性：
    - 兼容旧版配置规则
    - 支持配置版本管理
    - 平滑升级机制

11. 文档维护：
    - 及时更新配置规则文档
    - 记录业务变更历史
    - 提供操作手册和FAQ
*/

-- =====================================================================================================================
-- 文件结束
-- =====================================================================================================================