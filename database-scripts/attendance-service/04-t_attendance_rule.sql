-- =============================================
-- IOE-DREAM 智慧园区一卡通管理平台
-- 考勤服务 - 考勤规则表 SQL脚本
-- =============================================
-- 功能说明：
--   考勤规则表定义各种考勤计算规则，包括迟到、早退、旷工、加班等规则。
--   支持按部门、按员工类型、按时间段等维度配置不同的考勤规则。
--
-- 核心业务场景：
--   1. 迟到规则：定义迟到的时间阈值和处罚标准
--   2. 早退规则：定义早退的时间阈值和处罚标准
--   3. 旷工规则：定义旷工的判定标准和处理方式
--   4. 加班规则：定义加班的计算方式和倍率
--   5. 缺卡规则：定义缺卡的处理方式和补卡流程
--   6. 外勤规则：定义外勤打卡的验证方式和审批流程
--
-- 企业级特性：
--   - 支持多维度规则配置（全局、部门、员工类型）
--   - 支持规则优先级和冲突处理
--   - 支持规则生效时间和失效时间
--   - 支持规则版本管理和历史追溯
--   - 支持规则变更审批流程
--   - 完整的规则执行日志记录
--
-- 作者: IOE-DREAM 开发团队
-- 创建时间: 2025-01-08
-- 版本: v1.0.0
-- =============================================

USE `ioedream_attendance_db`;

-- =============================================
-- 1. 删除已存在的表（开发环境）
-- =============================================
DROP TABLE IF EXISTS `t_attendance_rule`;

-- =============================================
-- 2. 创建考勤规则表
-- =============================================
CREATE TABLE IF NOT EXISTS `t_attendance_rule` (
    -- ========== 主键与业务标识 ==========
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '考勤规则ID，主键，自增',
    `rule_code` VARCHAR(100) NOT NULL COMMENT '考勤规则编码，格式：RULE_{TYPE}_{序号}，唯一标识',
    `rule_name` VARCHAR(200) NOT NULL COMMENT '考勤规则名称，如"迟到规则"、"加班规则"等',
    
    -- ========== 规则类型与配置 ==========
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型：LATE-迟到规则, EARLY-早退规则, ABSENT-旷工规则, OVERTIME-加班规则, MISSING-缺卡规则, BUSINESS_TRIP-外勤规则, EXCEPTION-异常规则',
    `rule_category` VARCHAR(50) COMMENT '规则分类：GENERAL-通用规则, SPECIAL-特殊规则, CUSTOM-自定义规则',
    `rule_priority` INT NOT NULL DEFAULT 100 COMMENT '规则优先级，数值越小优先级越高，用于规则冲突时的选择',
    `rule_config` JSON NOT NULL COMMENT '规则配置（JSON格式），存储具体的规则参数',
    
    -- ========== 适用范围 ==========
    `apply_scope` VARCHAR(50) NOT NULL DEFAULT 'GLOBAL' COMMENT '适用范围：GLOBAL-全局规则, DEPARTMENT-部门规则, EMPLOYEE_TYPE-员工类型规则, SPECIFIC-特定人员',
    `department_id` BIGINT COMMENT '适用部门ID，apply_scope为DEPARTMENT时必填',
    `department_name` VARCHAR(200) COMMENT '适用部门名称，冗余字段',
    `employee_types` JSON COMMENT '适用员工类型列表（JSON数组），apply_scope为EMPLOYEE_TYPE时填写',
    `specific_users` JSON COMMENT '特定人员列表（JSON数组），apply_scope为SPECIFIC时填写',
    
    -- ========== 时间范围 ==========
    `effective_date` DATE COMMENT '生效日期，NULL表示立即生效',
    `expire_date` DATE COMMENT '失效日期，NULL表示永不失效',
    `valid_weekdays` VARCHAR(20) DEFAULT '1,2,3,4,5,6,7' COMMENT '有效星期几，逗号分隔，1-星期一, 7-星期日',
    `valid_time_ranges` JSON COMMENT '有效时间段列表（JSON数组），如[{"start":"09:00","end":"18:00"}]',
    
    -- ========== 规则状态 ==========
    `rule_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '规则状态：DRAFT-草稿, ACTIVE-激活, INACTIVE-停用, EXPIRED-已过期',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认规则：0-否, 1-是（每个规则类型只能有一个默认规则）',
    `version` INT NOT NULL DEFAULT 1 COMMENT '规则版本号，用于版本管理和历史追溯',
    
    -- ========== 审批信息 ==========
    `approval_status` VARCHAR(50) DEFAULT 'APPROVED' COMMENT '审批状态：DRAFT-草稿, PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝',
    `approval_user_id` BIGINT COMMENT '审批人ID',
    `approval_user_name` VARCHAR(100) COMMENT '审批人姓名',
    `approval_time` DATETIME COMMENT '审批时间',
    `approval_remark` VARCHAR(500) COMMENT '审批备注',
    
    -- ========== 统计信息 ==========
    `execution_count` INT NOT NULL DEFAULT 0 COMMENT '规则执行次数，用于统计分析',
    `last_execute_time` DATETIME COMMENT '最后执行时间',
    `violation_count` INT NOT NULL DEFAULT 0 COMMENT '违规次数，用于统计分析',
    
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
    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`, `version`),
    UNIQUE KEY `uk_rule_type_default` (`rule_type`, `is_default`, `deleted_flag`),
    INDEX `idx_rule_type` (`rule_type`),
    INDEX `idx_rule_status` (`rule_status`),
    INDEX `idx_department_id` (`department_id`),
    INDEX `idx_apply_scope` (`apply_scope`),
    INDEX `idx_effective_date` (`effective_date`),
    INDEX `idx_expire_date` (`expire_date`),
    INDEX `idx_create_time` (`create_time`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-考勤规则表';

-- =============================================
-- 3. 初始化示例数据（企业级生产环境标准）
-- =============================================

-- 说明：
--   以下数据为常见的考勤规则示例，涵盖各种规则类型：
--   - 迟到规则（不同阈值）
--   - 早退规则
--   - 加班规则
--   - 旷工规则
--   - 缺卡规则
--   - 外勤规则

-- 3.1 全局默认迟到规则
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_LATE_001', 
    '默认迟到规则', 
    'LATE', 
    'GENERAL', 
    10, 
    '{"threshold_minutes": 30, "penalty_type": "NONE", "warning_only": true, "accumulate_mode": "DAILY", "accumulate_limit": 3}',
    'GLOBAL', 
    '2025-01-01', 
    'ACTIVE', 
    1, 
    1,
    'APPROVED', 
    1, 
    0
);

-- 3.2 严格迟到规则（适用于管理层）
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `employee_types`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_LATE_002', 
    '管理层迟到规则', 
    'LATE', 
    'SPECIAL', 
    5, 
    '{"threshold_minutes": 10, "penalty_type": "DEDUCT_SALARY", "deduct_amount": 50.00, "accumulate_mode": "MONTHLY", "accumulate_limit": 0}',
    'EMPLOYEE_TYPE', 
    '["MANAGER", "DIRECTOR"]', 
    '2025-01-01', 
    'ACTIVE', 
    0, 
    1,
    'APPROVED', 
    1, 
    0
);

-- 3.3 早退规则
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_EARLY_001', 
    '默认早退规则', 
    'EARLY', 
    'GENERAL', 
    10, 
    '{"threshold_minutes": 30, "penalty_type": "WARNING", "early_departure_detection": true}',
    'GLOBAL', 
    '2025-01-01', 
    'ACTIVE', 
    1, 
    1,
    'APPROVED', 
    1, 
    0
);

-- 3.4 加班规则
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_OVERTIME_001', 
    '默认加班规则', 
    'OVERTIME', 
    'GENERAL', 
    10, 
    '{"min_duration_minutes": 60, "overtime_rates": {"weekday": 1.5, "weekend": 2.0, "holiday": 3.0}, "rounding_mode": "HALF_UP", "rounding_unit": 30}',
    'GLOBAL', 
    '2025-01-01', 
    'ACTIVE', 
    1, 
    1,
    'APPROVED', 
    1, 
    0
);

-- 3.5 旷工规则
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_ABSENT_001', 
    '默认旷工规则', 
    'ABSENT', 
    'GENERAL', 
    5, 
    '{"absent_types": ["FULL_DAY", "HALF_DAY"], "notification_required": true, "auto_notify_managers": true, "escalation_levels": 3}',
    'GLOBAL', 
    '2025-01-01', 
    'ACTIVE', 
    1, 
    1,
    'APPROVED', 
    1, 
    0
);

-- 3.6 缺卡规则
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_MISSING_001', 
    '默认缺卡规则', 
    'MISSING', 
    'GENERAL', 
    15, 
    '{"allow_makeup_days": 3, "require_attachment": false, "approval_required": true, "auto_remind": true}',
    'GLOBAL', 
    '2025-01-01', 
    'ACTIVE', 
    1, 
    1,
    'APPROVED', 
    1, 
    0
);

-- 3.7 外勤规则
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_BUSINESS_TRIP_001', 
    '默认外勤规则', 
    'BUSINESS_TRIP', 
    'GENERAL', 
    20, 
    '{"require_gps": true, "require_photo": true, "require_approval": true, "max_distance_meters": 1000, "check_in_frequency": 180}',
    'GLOBAL', 
    '2025-01-01', 
    'ACTIVE', 
    1, 
    1,
    'APPROVED', 
    1, 
    0
);

-- 3.8 技术部特殊规则（迟到15分钟内免罚）
INSERT INTO `t_attendance_rule` (
    `rule_code`, `rule_name`, `rule_type`, `rule_category`, `rule_priority`, `rule_config`,
    `apply_scope`, `department_id`, `department_name`, `effective_date`, `rule_status`, `is_default`, `version`,
    `approval_status`, `create_user_id`, `deleted_flag`
) VALUES (
    'RULE_LATE_003', 
    '技术部宽松迟到规则', 
    'LATE', 
    'CUSTOM', 
    8, 
    '{"threshold_minutes": 15, "penalty_type": "NONE", "warning_only": true}',
    'DEPARTMENT', 
    2003, 
    '技术部', 
    '2025-01-01', 
    'ACTIVE', 
    0, 
    1,
    'APPROVED', 
    1, 
    0
);

-- =============================================
-- 4. 索引优化说明
-- =============================================
-- 索引设计原则：
--   1. uk_rule_code：规则编码+版本号联合唯一索引，保证规则版本唯一性
--   2. uk_rule_type_default：规则类型+默认标志联合唯一索引，确保每种类型只有一个默认规则
--   3. idx_rule_type：规则类型索引，优化按类型查询
--   4. idx_rule_status：规则状态索引，优化按状态过滤
--   5. idx_department_id：部门ID索引，优化部门规则查询
--   6. idx_apply_scope：适用范围索引，优化按范围查询
--   7. idx_effective_date：生效日期索引，优化时间范围查询
--   8. idx_expire_date：失效日期索引，优化过期规则查询
--   9. idx_create_time：创建时间索引，优化时间排序查询

-- =============================================
-- 5. 使用示例
-- =============================================

-- 示例1: 查询所有激活的全局规则
-- SELECT * FROM t_attendance_rule
-- WHERE apply_scope = 'GLOBAL'
--   AND rule_status = 'ACTIVE'
--   AND deleted_flag = 0
-- ORDER BY rule_type, rule_priority ASC;

-- 示例2: 查询技术部的考勤规则
-- SELECT * FROM t_attendance_rule
-- WHERE (apply_scope = 'GLOBAL' OR (apply_scope = 'DEPARTMENT' AND department_id = 2003))
--   AND rule_status = 'ACTIVE'
--   AND deleted_flag = 0
--   AND (effective_date IS NULL OR effective_date <= CURDATE())
--   AND (expire_date IS NULL OR expire_date >= CURDATE())
-- ORDER BY rule_priority ASC;

-- 示例3: 查询默认迟到规则
-- SELECT * FROM t_attendance_rule
-- WHERE rule_type = 'LATE'
--   AND is_default = 1
--   AND deleted_flag = 0;

-- 示例4: 统计各类型规则数量
-- SELECT rule_type, COUNT(*) as rule_count
-- FROM t_attendance_rule
-- WHERE deleted_flag = 0
-- GROUP BY rule_type;

-- 示例5: 查询需要审批的规则
-- SELECT * FROM t_attendance_rule
-- WHERE approval_status = 'PENDING'
--   AND deleted_flag = 0
-- ORDER BY create_time ASC;

-- =============================================
-- 6. 维护建议
-- =============================================
-- 1. 定期清理已过期且长时间未使用的规则
-- 2. 定期检查规则冲突和优先级设置
-- 3. 定期统计规则执行情况，优化规则配置
-- 4. 定期备份规则配置，防止误删
-- 5. 定期审查规则审批流程的及时性
-- 6. 定期同步规则变更到相关系统

-- =============================================
-- 7. 定时任务建议
-- =============================================
-- 1. 每日凌晨01:00：检查并更新过期规则状态
-- 2. 每周一凌晨02:00：统计上周规则执行情况
-- 3. 每月1日00:00：生成上月规则执行报告
-- 4. 每日22:00：检查待审批规则并发送提醒
-- 5. 每季度第一个周一：审查规则配置合理性

-- =============================================
-- SQL脚本结束
-- =============================================