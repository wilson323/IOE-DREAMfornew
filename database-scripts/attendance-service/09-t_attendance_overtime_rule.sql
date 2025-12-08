-- ==================================================================
-- 考勤服务 - 加班规则表初始化脚本
-- ==================================================================
-- 功能说明: 定义和管理员工加班的各种规则配置
-- 表名: t_attendance_overtime_rule
-- 依赖表: t_common_department (部门表)
-- 作者: IOE-DREAM团队
-- 创建时间: 2025-01-09
-- 版本: v1.0.0
-- ==================================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET TIME_ZONE = '+08:00';

-- 如果表存在则删除(开发环境)
-- 生产环境请注释此行
DROP TABLE IF EXISTS `t_attendance_overtime_rule`;

-- ==================================================================
-- 表结构定义
-- ==================================================================

CREATE TABLE IF NOT EXISTS `t_attendance_overtime_rule` (
    -- ============================================================
    -- 主键和唯一标识
    -- ============================================================
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '加班规则ID(主键)',
    `rule_code` VARCHAR(100) NOT NULL COMMENT '加班规则编码(唯一标识)',
    
    -- ============================================================
    -- 基本信息
    -- ============================================================
    `rule_name` VARCHAR(200) NOT NULL COMMENT '加班规则名称',
    `rule_description` TEXT COMMENT '加班规则描述',
    `rule_category` VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT '规则分类:GENERAL-通用规则,SPECIAL-特殊规则,EMERGENCY-紧急规则,HOLIDAY-节假日规则',
    `rule_type` VARCHAR(50) NOT NULL DEFAULT 'TIME_BASED' COMMENT '规则类型:TIME_BASED-基于时间,WORKLOAD_BASED-基于工作量,TASK_BASED-基于任务',
    
    -- ============================================================
    -- 适用范围
    -- ============================================================
    `applicable_departments` JSON COMMENT '适用部门(JSON数组):空表示所有部门',
    `excluded_departments` JSON COMMENT '排除部门(JSON数组)',
    `applicable_positions` JSON COMMENT '适用职位(JSON数组):空表示所有职位',
    `excluded_positions` JSON COMMENT '排除职位(JSON数组)',
    `applicable_employee_types` JSON COMMENT '适用员工类型(JSON数组):FULL_TIME-全职,PART_TIME-兼职,INTERN-实习生,TEMPORARY-临时工',
    `excluded_employee_types` JSON COMMENT '排除员工类型(JSON数组)',
    
    -- ============================================================
    -- 时间规则
    -- ============================================================
    `min_overtime_duration` INT NOT NULL DEFAULT 30 COMMENT '最小加班时长(分钟):小于该时长不计入加班',
    `max_daily_overtime` INT NOT NULL DEFAULT 240 COMMENT '每日最大加班时长(分钟):默认4小时',
    `max_weekly_overtime` INT NOT NULL DEFAULT 1200 COMMENT '每周最大加班时长(分钟):默认20小时',
    `max_monthly_overtime` INT NOT NULL DEFAULT 4800 COMMENT '每月最大加班时长(分钟):默认80小时',
    `continuous_overtime_limit` INT NOT NULL DEFAULT 7 COMMENT '连续加班天数限制:默认7天',
    
    -- ============================================================
    -- 计算规则
    -- ============================================================
    `calculation_method` VARCHAR(50) NOT NULL DEFAULT 'FIXED_RATE' COMMENT '计算方式:FIXED_RATE-固定倍率,TIERED_RATE-阶梯倍率,SLIDING_SCALE-滑动比例',
    `fixed_rate` DECIMAL(5,2) NOT NULL DEFAULT 1.50 COMMENT '固定倍率:如1.5表示1.5倍工资',
    `tiered_rates` JSON COMMENT '阶梯倍率配置(JSON数组):[{hours: 2, rate: 1.5}, {hours: 4, rate: 2.0}]',
    `sliding_scale_formula` VARCHAR(500) COMMENT '滑动比例公式:如"1.5 + (hours-2)*0.1"',
    
    -- ============================================================
    -- 休息规则
    -- ============================================================
    `rest_required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要休息:1-需要,0-不需要',
    `rest_duration` INT NOT NULL DEFAULT 60 COMMENT '休息时长(分钟):默认1小时',
    `rest_interval` INT NOT NULL DEFAULT 180 COMMENT '休息间隔(分钟):默认3小时',
    
    -- ============================================================
    -- 申请和审批
    -- ============================================================
    `require_approval` TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要审批:1-需要,0-不需要',
    `auto_approve_threshold` INT COMMENT '自动审批阈值(分钟):小于等于此值自动审批',
    `approval_level` INT NOT NULL DEFAULT 1 COMMENT '审批层级:1-一级审批,2-二级审批,3-三级审批',
    `default_approver_role` VARCHAR(100) COMMENT '默认审批人角色:DIRECT_SUPERVISOR-直属上级,DEPARTMENT_MANAGER-部门经理,HR_SPECIALIST-HR专员',
    `allow_delegate_approval` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许委托审批:1-允许,0-不允许',
    
    -- ============================================================
    -- 申报规则
    -- ============================================================
    `advance_apply_hours` INT NOT NULL DEFAULT 0 COMMENT '需提前申请小时数:0-无限制',
    `allow_retroactive` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许补申请:1-允许,0-不允许',
    `retroactive_limit_days` INT COMMENT '补申请限制天数:超过此天数不允许补申请',
    `max_concurrent_overtimes` INT NOT NULL DEFAULT 999 COMMENT '最大并发加班数:同一时间可申请的加班数量',
    
    -- ============================================================
    -- 工作流集成
    -- ============================================================
    `workflow_definition_id` BIGINT COMMENT '工作流定义ID(关联OA工作流)',
    `workflow_definition_code` VARCHAR(100) COMMENT '工作流定义编码',
    
    -- ============================================================
    -- 时间限制
    -- ============================================================
    `available_weekdays` JSON COMMENT '可用星期(JSON数组):1-周一,2-周二,...,7-周日',
    `available_time_ranges` JSON COMMENT '可用时间范围(JSON数组):[{start: "18:00", end: "22:00"}]',
    `restricted_dates` JSON COMMENT '限制日期(JSON数组):节假日、特殊日期等',
    
    -- ============================================================
    -- 状态和控制
    -- ============================================================
    `rule_status` VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '规则状态:ACTIVE-激活,INACTIVE-停用,MAINTENANCE-维护中',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级(数值越大优先级越高)',
    `is_system_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统默认:1-是(系统内置规则),0-否(用户自定义)',
    `allow_employee_customize` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许员工自定义:1-允许,0-不允许',
    
    -- ============================================================
    -- 统计和分析
    -- ============================================================
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数统计',
    `last_used_time` DATETIME COMMENT '最后使用时间',
    `average_approval_duration` INT COMMENT '平均审批时长(小时)',
    
    -- ============================================================
    -- 备注和扩展
    -- ============================================================
    `remark` VARCHAR(500) COMMENT '备注信息',
    `extended_attributes` JSON COMMENT '扩展属性(JSON格式):存储业务特定的额外字段',
    
    -- ============================================================
    -- 标准审计字段
    -- ============================================================
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号(用于乐观锁)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人用户ID',
    `update_user_id` BIGINT COMMENT '更新人用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记:0-未删除,1-已删除',
    
    -- ============================================================
    -- 主键和索引定义
    -- ============================================================
    PRIMARY KEY (`rule_id`),
    
    -- 唯一索引
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    
    -- 普通索引
    INDEX `idx_rule_name` (`rule_name`),
    INDEX `idx_rule_category` (`rule_category`),
    INDEX `idx_rule_type` (`rule_type`),
    INDEX `idx_rule_status` (`rule_status`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_is_system_default` (`is_system_default`),
    INDEX `idx_require_approval` (`require_approval`),
    INDEX `idx_create_time` (`create_time` DESC),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考勤服务-加班规则表';

-- ==================================================================
-- 初始化数据
-- ==================================================================

INSERT INTO `t_attendance_overtime_rule` (
    `rule_code`,
    `rule_name`,
    `rule_description`,
    `rule_category`,
    `rule_type`,
    `applicable_departments`,
    `excluded_departments`,
    `applicable_positions`,
    `excluded_positions`,
    `applicable_employee_types`,
    `excluded_employee_types`,
    `min_overtime_duration`,
    `max_daily_overtime`,
    `max_weekly_overtime`,
    `max_monthly_overtime`,
    `continuous_overtime_limit`,
    `calculation_method`,
    `fixed_rate`,
    `tiered_rates`,
    `sliding_scale_formula`,
    `rest_required`,
    `rest_duration`,
    `rest_interval`,
    `require_approval`,
    `auto_approve_threshold`,
    `approval_level`,
    `default_approver_role`,
    `allow_delegate_approval`,
    `advance_apply_hours`,
    `allow_retroactive`,
    `retroactive_limit_days`,
    `max_concurrent_overtimes`,
    `workflow_definition_id`,
    `workflow_definition_code`,
    `available_weekdays`,
    `available_time_ranges`,
    `restricted_dates`,
    `rule_status`,
    `priority`,
    `is_system_default`,
    `allow_employee_customize`,
    `usage_count`,
    `last_used_time`,
    `average_approval_duration`,
    `remark`
) VALUES
-- 示例1: 标准加班规则(通用规则,固定1.5倍率)
('OT_STD_001', '标准加班规则', '适用于大多数部门的标准加班规则,1.5倍工资计算', 'GENERAL', 'TIME_BASED',
 NULL, NULL, NULL, NULL, '["FULL_TIME"]', NULL,
 30, 240, 1200, 4800, 7,
 'FIXED_RATE', 1.50, NULL, NULL,
 1, 60, 180,
 1, 120, 1, 'DIRECT_SUPERVISOR', 1,
 0, 1, 3, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5]', '[{"start": "18:00", "end": "22:00"}]', NULL,
 'ACTIVE', 100, 1, 1,
 125, '2025-01-09 10:30:00', 2,
 '标准加班规则,适用于全职员工'),

-- 示例2: 高管加班规则(特殊规则,阶梯倍率)
('OT_EXEC_001', '高管加班规则', '适用于高管的特殊加班规则,采用阶梯倍率计算', 'SPECIAL', 'TIME_BASED',
 '["1","2"]', NULL, '["MANAGER","DIRECTOR"]', NULL, '["FULL_TIME"]', NULL,
 60, 300, 1500, 6000, 5,
 'TIERED_RATE', 1.00, '[{"hours": 2, "rate": 1.5}, {"hours": 4, "rate": 2.0}, {"hours": 8, "rate": 3.0}]', NULL,
 1, 60, 180,
 1, NULL, 2, 'DEPARTMENT_MANAGER', 1,
 24, 1, 7, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5]', '[{"start": "19:00", "end": "23:00"}]', NULL,
 'ACTIVE', 200, 1, 0,
 45, '2025-01-09 11:15:00', 4,
 '高管加班规则,采用阶梯倍率计算'),

-- 示例3: 研发部门加班规则(部门专属规则)
('OT_RD_001', '研发部门加班规则', '研发部门专用加班规则,周末加班2倍工资', 'GENERAL', 'TIME_BASED',
 '["3"]', NULL, NULL, NULL, '["FULL_TIME"]', NULL,
 30, 300, 1800, 7200, 7,
 'FIXED_RATE', 2.00, NULL, NULL,
 1, 60, 180,
 1, 60, 1, 'DEPARTMENT_MANAGER', 1,
 0, 1, 3, 999,
 4, 'OVERTIME_APPLICATION',
 '[6,7]', '[{"start": "09:00", "end": "24:00"}]', NULL,
 'ACTIVE', 150, 0, 1,
 87, '2025-01-09 14:20:00', 3,
 '研发部门加班规则,周末加班2倍工资'),

-- 示例4: 节假日加班规则(节假日规则,3倍工资)
('OT_HOL_001', '节假日加班规则', '法定节假日加班规则,3倍工资计算', 'HOLIDAY', 'TIME_BASED',
 NULL, NULL, NULL, NULL, '["FULL_TIME"]', NULL,
 30, 480, 2400, 9600, 3,
 'FIXED_RATE', 3.00, NULL, NULL,
 1, 120, 180,
 1, NULL, 2, 'DEPARTMENT_MANAGER', 1,
 0, 1, 7, 999,
 4, 'OVERTIME_APPLICATION',
 NULL, '[{"start": "00:00", "end": "24:00"}]', '["2025-01-01","2025-02-10","2025-05-01","2025-10-01"]',
 'ACTIVE', 300, 1, 0,
 23, '2025-01-01 20:00:00', 6,
 '节假日加班规则,3倍工资计算'),

-- 示例5: 实习生加班规则(实习生专用规则)
('OT_INT_001', '实习生加班规则', '实习生加班规则,最多每天2小时,1.2倍工资', 'GENERAL', 'TIME_BASED',
 NULL, NULL, NULL, NULL, '["INTERN"]', NULL,
 30, 120, 600, 2400, 7,
 'FIXED_RATE', 1.20, NULL, NULL,
 1, 30, 120,
 1, 60, 1, 'DIRECT_SUPERVISOR', 1,
 0, 1, 3, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5]', '[{"start": "18:00", "end": "21:00"}]', NULL,
 'ACTIVE', 50, 1, 1,
 12, '2025-01-08 17:45:00', 1,
 '实习生加班规则,最多每天2小时,1.2倍工资'),

-- 示例6: 紧急项目加班规则(紧急规则,无需审批)
('OT_EMG_001', '紧急项目加班规则', '紧急项目加班规则,无需审批,自动通过', 'EMERGENCY', 'TASK_BASED',
 '["4"]', NULL, NULL, NULL, '["FULL_TIME"]', NULL,
 15, 480, 2400, 9600, 14,
 'FIXED_RATE', 2.00, NULL, NULL,
 0, 0, 0,
 0, NULL, 1, NULL, 0,
 0, 1, 1, 999,
 4, 'OVERTIME_APPLICATION',
 NULL, '[{"start": "00:00", "end": "24:00"}]', NULL,
 'ACTIVE', 250, 0, 0,
 67, '2025-01-09 22:30:00', 0,
 '紧急项目加班规则,无需审批,自动通过'),

-- 示例7: 夜班加班规则(基于时间的特殊规则)
('OT_NIGHT_001', '夜班加班规则', '夜班时段加班规则,22:00-06:00,2倍工资并有额外补贴', 'GENERAL', 'TIME_BASED',
 NULL, NULL, NULL, NULL, '["FULL_TIME"]', NULL,
 30, 480, 2400, 9600, 7,
 'FIXED_RATE', 2.00, NULL, NULL,
 1, 60, 180,
 1, 120, 1, 'DIRECT_SUPERVISOR', 1,
 0, 1, 3, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5,6,7]', '[{"start": "22:00", "end": "06:00"}]', NULL,
 'ACTIVE', 180, 0, 1,
 34, '2025-01-09 02:15:00', 2,
 '夜班加班规则,22:00-06:00,2倍工资并有额外补贴'),

-- 示例8: 兼职员工加班规则(兼职专用规则)
('OT_PART_001', '兼职员工加班规则', '兼职员工加班规则,最多每周10小时', 'GENERAL', 'TIME_BASED',
 NULL, NULL, NULL, NULL, '["PART_TIME"]', NULL,
 30, 180, 600, 2400, 7,
 'FIXED_RATE', 1.50, NULL, NULL,
 1, 30, 120,
 1, 60, 1, 'DIRECT_SUPERVISOR', 1,
 0, 1, 3, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5,6,7]', '[{"start": "18:00", "end": "22:00"}]', NULL,
 'ACTIVE', 80, 1, 1,
 19, '2025-01-07 19:30:00', 2,
 '兼职员工加班规则,最多每周10小时'),

-- 示例9: 滑动比例加班规则(高级计算规则)
('OT_SLIDE_001', '滑动比例加班规则', '滑动比例计算加班费,加班时间越长倍率越高', 'SPECIAL', 'TIME_BASED',
 '["5"]', NULL, '["SENIOR_ENGINEER"]', NULL, '["FULL_TIME"]', NULL,
 60, 300, 1800, 7200, 7,
 'SLIDING_SCALE', 1.00, NULL, '1.5 + (hours-2)*0.2',
 1, 60, 180,
 1, NULL, 2, 'DEPARTMENT_MANAGER', 1,
 24, 1, 7, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5]', '[{"start": "19:00", "end": "23:00"}]', NULL,
 'ACTIVE', 220, 0, 0,
 28, '2025-01-09 20:45:00', 5,
 '滑动比例加班规则,加班时间越长倍率越高'),

-- 示例10: 临时工加班规则(临时工专用规则)
('OT_TEMP_001', '临时工加班规则', '临时工加班规则,按小时计费,无需倍率计算', 'GENERAL', 'TIME_BASED',
 NULL, NULL, NULL, NULL, '["TEMPORARY"]', NULL,
 30, 240, 1200, 4800, 7,
 'FIXED_RATE', 1.00, NULL, NULL,
 1, 30, 120,
 1, 60, 1, 'DIRECT_SUPERVISOR', 1,
 0, 1, 2, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5]', '[{"start": "18:00", "end": "22:00"}]', NULL,
 'ACTIVE', 60, 1, 1,
 8, '2025-01-06 20:15:00', 1,
 '临时工加班规则,按小时计费,无需倍率计算'),

-- 示例11: 无需休息的加班规则(特殊场景)
('OT_NOREST_001', '无需休息加班规则', '特殊场景下的加班规则,无需强制休息', 'SPECIAL', 'TIME_BASED',
 '["6"]', NULL, NULL, NULL, '["FULL_TIME"]', NULL,
 15, 120, 600, 2400, 3,
 'FIXED_RATE', 1.50, NULL, NULL,
 0, 0, 0,
 1, 30, 1, 'DIRECT_SUPERVISOR', 1,
 0, 1, 1, 999,
 4, 'OVERTIME_APPLICATION',
 NULL, '[{"start": "00:00", "end": "24:00"}]', NULL,
 'ACTIVE', 120, 0, 0,
 5, '2025-01-02 03:30:00', 1,
 '无需休息的加班规则,特殊场景使用'),

-- 示例12: 自动审批加班规则(高效审批)
('OT_AUTO_001', '自动审批加班规则', '小于2小时的加班自动审批通过', 'GENERAL', 'TIME_BASED',
 NULL, NULL, NULL, NULL, '["FULL_TIME"]', NULL,
 30, 240, 1200, 4800, 7,
 'FIXED_RATE', 1.50, NULL, NULL,
 1, 60, 180,
 1, 120, 1, 'DIRECT_SUPERVISOR', 1,
 0, 1, 3, 999,
 4, 'OVERTIME_APPLICATION',
 '[1,2,3,4,5]', '[{"start": "18:00", "end": "22:00"}]', NULL,
 'ACTIVE', 90, 1, 1,
 203, '2025-01-09 18:45:00', 0,
 '自动审批加班规则,小于2小时的加班自动审批通过');

-- ==================================================================
-- 索引优化说明
-- ==================================================================

-- 1. uk_rule_code: 唯一索引,确保规则编码全局唯一
--    - 防止规则编码重复
--    - 支持快速通过编码查找规则

-- 2. idx_rule_name: 按规则名称查询
--    - 支持模糊查询规则名称
--    - 规则名称搜索优化

-- 3. idx_rule_category: 按规则分类筛选
--    - 筛选不同类型的规则
--    - 规则分类统计分析

-- 4. idx_rule_type: 按规则类型筛选
--    - 筛选不同计算方式的规则
--    - 规则类型维度分析

-- 5. idx_rule_status: 按状态筛选
--    - 筛选激活/停用的规则
--    - 规则状态管理

-- 6. idx_priority: 按优先级排序
--    - 查询高优先级规则
--    - 规则优先级排序

-- 7. idx_is_system_default: 区分系统默认和自定义规则
--    - 筛选系统内置规则
--    - 规则类型区分

-- 8. idx_require_approval: 按是否需要审批筛选
--    - 筛选需要审批的规则
--    - 审批流程优化

-- 9. idx_create_time: 降序索引,按创建时间倒序查询
--    - 查询最新创建的规则
--    - 审计追踪

-- 10. idx_deleted_flag: 逻辑删除索引
--     - 过滤已删除记录
--     - 提升查询性能

-- ==================================================================
-- 使用示例
-- ==================================================================

-- 示例1: 查询所有激活的加班规则
-- SELECT * FROM t_attendance_overtime_rule
-- WHERE rule_status = 'ACTIVE'
--   AND deleted_flag = 0
-- ORDER BY priority DESC;

-- 示例2: 查询适用于特定部门的规则
-- SELECT * FROM t_attendance_overtime_rule
-- WHERE rule_status = 'ACTIVE'
--   AND (applicable_departments IS NULL OR JSON_CONTAINS(applicable_departments, '"3"'))
--   AND (excluded_departments IS NULL OR NOT JSON_CONTAINS(excluded_departments, '"3"'))
--   AND deleted_flag = 0;

-- 示例3: 查询需要审批的规则及其审批层级
-- SELECT rule_code, rule_name, approval_level, default_approver_role
-- FROM t_attendance_overtime_rule
-- WHERE require_approval = 1
--   AND rule_status = 'ACTIVE'
--   AND deleted_flag = 0
-- ORDER BY approval_level;

-- 示例4: 统计各类规则的使用情况
-- SELECT 
--     rule_category,
--     COUNT(*) AS rule_count,
--     SUM(usage_count) AS total_usage,
--     AVG(priority) AS avg_priority
-- FROM t_attendance_overtime_rule
-- WHERE deleted_flag = 0
-- GROUP BY rule_category;

-- 示例5: 查询系统默认规则
-- SELECT rule_code, rule_name, calculation_method, fixed_rate
-- FROM t_attendance_overtime_rule
-- WHERE is_system_default = 1
--   AND rule_status = 'ACTIVE'
--   AND deleted_flag = 0;

-- ==================================================================
-- 维护建议
-- ==================================================================

-- 1. 定期审查加班规则配置,确保符合公司政策和法规要求
-- 2. 根据业务变化及时调整规则参数(如最大加班时长、倍率等)
-- 3. 定期分析规则使用情况,优化配置参数
-- 4. 监控加班规则审批效率,调整审批流程
-- 5. 根据法律法规变化及时更新加班规则

-- ==================================================================
-- 定时任务建议
-- ==================================================================

-- 1. 加班规则使用统计(每日)
--    - 统计各规则的使用次数
--    - 更新usage_count和last_used_time字段
--    - 生成使用报告

-- 2. 加班规则状态检查(每周)
--    - 检查规则状态是否合理
--    - 自动将过期规则设置为停用状态
--    - 发送状态异常告警

-- 3. 加班规则审批效率分析(每月)
--    - 分析各规则的平均审批时长
--    - 更新average_approval_duration字段
--    - 优化审批流程

-- 4. 加班规则配置验证(每季度)
--    - 验证规则配置的合理性
--    - 检查必填字段是否完整
--    - 发现配置问题及时告警

-- 5. 加班规则清理(每半年)
--    - 清理长期未使用的自定义规则
--    - 归档历史规则配置
--    - 优化数据库性能

-- ==================================================================
-- 数据完整性约束建议
-- ==================================================================

-- 1. rule_code必须唯一且不为空
-- 2. rule_name不能为空
-- 3. calculation_method必须在预定义范围内
-- 4. fixed_rate >= 1.0 (至少1倍工资)
-- 5. max_daily_overtime >= min_overtime_duration
-- 6. max_weekly_overtime >= max_daily_overtime
-- 7. max_monthly_overtime >= max_weekly_overtime
-- 8. priority >= 0
-- 9. approval_level在1-3范围内
-- 10. JSON字段格式必须正确

-- ==================================================================
-- 性能优化建议
-- ==================================================================

-- 1. 对于高频查询字段,考虑使用覆盖索引减少回表
--    CREATE INDEX idx_cover_status_priority ON t_attendance_overtime_rule
--    (rule_status, priority, rule_code, rule_name);

-- 2. 定期分析表和索引使用情况,优化索引
--    ANALYZE TABLE t_attendance_overtime_rule;

-- 3. 对于历史数据(3年前),考虑归档到历史表
--    CREATE TABLE t_attendance_overtime_rule_history LIKE t_attendance_overtime_rule;

-- 4. 使用Redis缓存热点数据(常用的加班规则)
--    缓存key: overtime_rule:code:{rule_code}
--    缓存过期时间: 1小时

-- ==================================================================
-- 脚本结束
-- ==================================================================
