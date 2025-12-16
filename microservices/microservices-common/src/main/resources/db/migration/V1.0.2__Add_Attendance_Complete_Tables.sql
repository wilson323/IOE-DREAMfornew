-- IOE-DREAM 考勤管理完整数据库表结构
-- 版本: 1.0.2
-- 创建时间: 2025-12-16
-- 描述: 创建完整的考勤管理相关数据表，支持100%功能实现

-- ====================================================================
-- 1. 智能排班相关表
-- ====================================================================

-- 排班计划表（支持智能排班算法）
CREATE TABLE IF NOT EXISTS `t_attendance_schedule_plan` (
    `plan_id` BIGINT NOT NULL COMMENT '排班计划ID',
    `plan_code` VARCHAR(50) NOT NULL COMMENT '排班计划编码',
    `plan_name` VARCHAR(100) NOT NULL COMMENT '排班计划名称',
    `department_id` BIGINT COMMENT '部门ID',
    `plan_type` TINYINT NOT NULL COMMENT '计划类型: 1-固定排班 2-循环排班 3-弹性排班 4-智能排班',
    `schedule_algorithm` VARCHAR(50) COMMENT '排班算法: GENETIC-遗传算法 GREEDY-贪心算法 BACKTRACK-回溯算法',
    `plan_status` TINYINT DEFAULT 1 COMMENT '计划状态: 1-草稿 2-待审批 3-已审批 4-执行中 5-已完成 6-已取消',
    `effective_start_date` DATE COMMENT '生效开始日期',
    `effective_end_date` DATE COMMENT '生效结束日期',
    `auto_approval` TINYINT DEFAULT 0 COMMENT '自动审批: 0-否 1-是',
    `conflict_resolution` VARCHAR(50) DEFAULT 'MANUAL' COMMENT '冲突解决方式: MANUAL-手动 AUTO-自动 PRIORITY-优先级',
    `schedule_constraints` JSON COMMENT '排班约束条件',
    `optimization_target` VARCHAR(50) COMMENT '优化目标: BALANCE-均衡 COST-成本 PREFERENCE-偏好',
    `description` TEXT COMMENT '排班计划描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`plan_id`),
    UNIQUE KEY `uk_plan_code` (`plan_code`, `deleted_flag`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_plan_status` (`plan_status`),
    KEY `idx_effective_date` (`effective_start_date`, `effective_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班计划表';

-- 个人排班表（记录每个人的具体排班安排）
CREATE TABLE IF NOT EXISTS `t_attendance_personal_schedule` (
    `schedule_id` BIGINT NOT NULL COMMENT '排班记录ID',
    `plan_id` BIGINT NOT NULL COMMENT '排班计划ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `department_id` BIGINT COMMENT '部门ID',
    `shift_id` BIGINT COMMENT '班次ID',
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `work_start_time` TIME COMMENT '上班时间',
    `work_end_time` TIME COMMENT '下班时间',
    `work_location` VARCHAR(100) COMMENT '工作地点',
    `schedule_status` TINYINT DEFAULT 1 COMMENT '排班状态: 1-正常 2-请假 3-调班 4-加班 5-替班',
    `auto_generated` TINYINT DEFAULT 0 COMMENT '自动生成: 0-否 1-是',
    `priority_level` TINYINT DEFAULT 1 COMMENT '优先级: 1-普通 2-重要 3-紧急',
    `schedule_source` VARCHAR(50) COMMENT '排班来源: MANUAL-手动 AUTO-自动 IMPORT-导入 SYNC-同步',
    `conflict_flag` TINYINT DEFAULT 0 COMMENT '冲突标识: 0-无冲突 1-有时间冲突 2-有人员冲突',
    `conflict_resolution` VARCHAR(200) COMMENT '冲突解决方案',
    `extended_attributes` JSON COMMENT '扩展属性',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`schedule_id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `schedule_date`, `deleted_flag`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_shift_id` (`shift_id`),
    KEY `idx_schedule_date` (`schedule_date`),
    KEY `idx_schedule_status` (`schedule_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人排班表';

-- 轮班规则表（支持三班倒、四班三倒等轮班模式）
CREATE TABLE IF NOT EXISTS `t_attendance_shift_rotation` (
    `rotation_id` BIGINT NOT NULL COMMENT '轮班规则ID',
    `rotation_code` VARCHAR(50) NOT NULL COMMENT '轮班规则编码',
    `rotation_name` VARCHAR(100) NOT NULL COMMENT '轮班规则名称',
    `rotation_type` TINYINT NOT NULL COMMENT '轮班类型: 1-三班倒 2-四班三倒 3-五班四倒 4-自定义',
    `cycle_days` INT NOT NULL COMMENT '轮班周期天数',
    `rotation_shifts` JSON NOT NULL COMMENT '轮班班次配置',
    `rotation_groups` JSON COMMENT '轮班分组配置',
    `effective_start_date` DATE NOT NULL COMMENT '生效开始日期',
    `effective_end_date` DATE COMMENT '生效结束日期',
    `auto_rotate` TINYINT DEFAULT 1 COMMENT '自动轮班: 0-否 1-是',
    `rotation_frequency` VARCHAR(50) DEFAULT 'DAILY' COMMENT '轮班频率: DAILY-每日 WEEKLY-每周 MONTHLY-每月',
    `conflict_handling` VARCHAR(50) DEFAULT 'SKIP' COMMENT '冲突处理: SKIP-跳过 OVERRIDE-覆盖 PROMPT-提示',
    `holiday_handling` VARCHAR(50) DEFAULT 'NORMAL' COMMENT '节假日处理: NORMAL-正常 SKIP-跳过 SUBSTITUTE-替代',
    `rotation_status` TINYINT DEFAULT 1 COMMENT '轮班状态: 1-启用 0-禁用',
    `description` TEXT COMMENT '轮班规则描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`rotation_id`),
    UNIQUE KEY `uk_rotation_code` (`rotation_code`, `deleted_flag`),
    KEY `idx_rotation_type` (`rotation_type`),
    KEY `idx_effective_date` (`effective_start_date`, `effective_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮班规则表';

-- ====================================================================
-- 2. 考勤规则引擎相关表
-- ====================================================================

-- 考勤规则配置表
CREATE TABLE IF NOT EXISTS `t_attendance_rule_config` (
    `rule_id` BIGINT NOT NULL COMMENT '规则ID',
    `rule_code` VARCHAR(50) NOT NULL COMMENT '规则编码',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_category` VARCHAR(50) NOT NULL COMMENT '规则分类: TIME-时间规则 LOCATION-地点规则 ABSENCE-缺勤规则 OVERTIME-加班规则',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型',
    `rule_condition` JSON NOT NULL COMMENT '规则条件',
    `rule_action` JSON NOT NULL COMMENT '规则动作',
    `rule_priority` INT DEFAULT 100 COMMENT '规则优先级（数字越小优先级越高）',
    `effective_start_time` TIME COMMENT '生效开始时间',
    `effective_end_time` TIME COMMENT '生效结束时间',
    `effective_days` VARCHAR(20) COMMENT '生效日期: 1,2,3,4,5,6,7',
    `department_ids` JSON COMMENT '适用部门ID列表',
    `user_ids` JSON COMMENT '适用用户ID列表',
    `rule_status` TINYINT DEFAULT 1 COMMENT '规则状态: 1-启用 0-禁用',
    `rule_scope` VARCHAR(50) DEFAULT 'GLOBAL' COMMENT '规则作用域: GLOBAL-全局 DEPARTMENT-部门 USER-个人',
    `execution_order` INT DEFAULT 0 COMMENT '执行顺序',
    `parent_rule_id` BIGINT COMMENT '父规则ID',
    `rule_version` VARCHAR(20) DEFAULT '1.0' COMMENT '规则版本',
    `description` TEXT COMMENT '规则描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`, `deleted_flag`),
    KEY `idx_rule_category` (`rule_category`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_rule_priority` (`rule_priority`),
    KEY `idx_rule_status` (`rule_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤规则配置表';

-- 考勤异常规则表
CREATE TABLE IF NOT EXISTS `t_attendance_abnormal_rule` (
    `abnormal_rule_id` BIGINT NOT NULL COMMENT '异常规则ID',
    `abnormal_type` VARCHAR(50) NOT NULL COMMENT '异常类型: LATE-迟到 EARLY-早退 ABSENCE-缺勤 OVERTIME-异常加班 LOCATION-位置异常',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `detection_condition` JSON NOT NULL COMMENT '检测条件',
    `abnormal_level` TINYINT DEFAULT 1 COMMENT '异常等级: 1-轻微 2-一般 3-严重 4-紧急',
    `auto_approval` TINYINT DEFAULT 0 COMMENT '自动审批: 0-否 1-是',
    `notification_config` JSON COMMENT '通知配置',
    `penalty_rule` JSON COMMENT '处罚规则',
    `resolution_method` VARCHAR(100) COMMENT '解决方法',
    `escalation_rules` JSON COMMENT '升级规则',
    `rule_status` TINYINT DEFAULT 1 COMMENT '规则状态: 1-启用 0-禁用',
    `effective_start_date` DATE COMMENT '生效开始日期',
    `effective_end_date` DATE COMMENT '生效结束日期',
    `description` TEXT COMMENT '规则描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`abnormal_rule_id`),
    KEY `idx_abnormal_type` (`abnormal_type`),
    KEY `idx_abnormal_level` (`abnormal_level`),
    KEY `idx_rule_status` (`rule_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤异常规则表';

-- ====================================================================
-- 3. 销假管理相关表（完全缺失）
-- ====================================================================

-- 请假申请表
CREATE TABLE IF NOT EXISTS `t_attendance_leave_application` (
    `leave_id` BIGINT NOT NULL COMMENT '请假ID',
    `leave_code` VARCHAR(50) NOT NULL COMMENT '请假单号',
    `user_id` BIGINT NOT NULL COMMENT '申请人ID',
    `department_id` BIGINT COMMENT '申请人部门ID',
    `leave_type` VARCHAR(50) NOT NULL COMMENT '请假类型: SICK-病假 PERSONAL-事假 MATERNITY-产假 MARRIAGE-婚假 ANNUAL-年假 COMPENSATORY-调休 OTHER-其他',
    `leave_reason` TEXT NOT NULL COMMENT '请假原因',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `start_time` TIME COMMENT '开始时间',
    `end_time` TIME COMMENT '结束时间',
    `leave_duration` DECIMAL(8,2) NOT NULL COMMENT '请假时长（天）',
    `leave_days` DECIMAL(5,2) NOT NULL COMMENT '请假天数',
    `leave_hours` DECIMAL(5,2) COMMENT '请假小时数',
    `attachment_urls` JSON COMMENT '附件URL列表',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approval_status` TINYINT DEFAULT 1 COMMENT '审批状态: 1-待审批 2-审批中 3-已通过 4-已拒绝 5-已撤销',
    `approval_comments` TEXT COMMENT '审批意见',
    `approval_time` DATETIME COMMENT '审批时间',
    `emergency_contact` VARCHAR(100) COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(20) COMMENT '紧急联系电话',
    `work_handover` TEXT COMMENT '工作交接说明',
    `leave_status` TINYINT DEFAULT 1 COMMENT '请假状态: 1-申请中 2-已批准 3-进行中 4-已完成 5-已取消',
    `cancel_reason` TEXT COMMENT '取消原因',
    `cancel_time` DATETIME COMMENT '取消时间',
    `extended_attributes` JSON COMMENT '扩展属性',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`leave_id`),
    UNIQUE KEY `uk_leave_code` (`leave_code`, `deleted_flag`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_leave_type` (`leave_type`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_leave_date` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 销假申请表
CREATE TABLE IF NOT EXISTS `t_attendance_leave_cancellation` (
    `cancellation_id` BIGINT NOT NULL COMMENT '销假ID',
    `cancellation_code` VARCHAR(50) NOT NULL COMMENT '销假单号',
    `leave_id` BIGINT NOT NULL COMMENT '原请假ID',
    `user_id` BIGINT NOT NULL COMMENT '申请人ID',
    `department_id` BIGINT COMMENT '申请人部门ID',
    `cancellation_reason` TEXT NOT NULL COMMENT '销假原因',
    `cancellation_type` TINYINT NOT NULL COMMENT '销假类型: 1-完全销假 2-部分销假',
    `actual_end_date` DATE COMMENT '实际结束日期',
    `actual_end_time` TIME COMMENT '实际结束时间',
    `actual_return_date` DATE COMMENT '实际返岗日期',
    `actual_return_time` TIME COMMENT '实际返岗时间',
    `recovered_duration` DECIMAL(8,2) COMMENT '恢复时长（天）',
    `recovered_days` DECIMAL(5,2) COMMENT '恢复天数',
    `recovered_hours` DECIMAL(5,2) COMMENT '恢复小时数',
    `approver_id` BIGINT COMMENT '审批人ID',
    `approval_status` TINYINT DEFAULT 1 COMMENT '审批状态: 1-待审批 2-审批中 3-已通过 4-已拒绝 5-已撤销',
    `approval_comments` TEXT COMMENT '审批意见',
    `approval_time` DATETIME COMMENT '审批时间',
    `cancellation_status` TINYINT DEFAULT 1 COMMENT '销假状态: 1-申请中 2-已批准 3-已完成 4-已取消',
    `attachment_urls` JSON COMMENT '附件URL列表',
    `work_resumption_report` TEXT COMMENT '返岗工作报告',
    `extended_attributes` JSON COMMENT '扩展属性',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`cancellation_id`),
    UNIQUE KEY `uk_cancellation_code` (`cancellation_code`, `deleted_flag`),
    KEY `idx_leave_id` (`leave_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_approval_status` (`approval_status`),
    KEY `idx_cancellation_date` (`actual_return_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销假申请表';

-- ====================================================================
-- 4. 实时考勤计算相关表
-- ====================================================================

-- 考勤计算任务表（支持异步计算）
CREATE TABLE IF NOT EXISTS `t_attendance_calculation_task` (
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型: DAILY-日计算 WEEKLY-周计算 MONTHLY-月计算 REALTIME-实时计算 BATCH-批量计算',
    `task_code` VARCHAR(50) NOT NULL COMMENT '任务编码',
    `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
    `calculation_date` DATE COMMENT '计算日期',
    `calculation_start_date` DATE COMMENT '计算开始日期',
    `calculation_end_date` DATE COMMENT '计算结束日期',
    `target_users` JSON COMMENT '目标用户列表',
    `target_departments` JSON COMMENT '目标部门列表',
    `calculation_rules` JSON NOT NULL COMMENT '计算规则配置',
    `task_status` TINYINT DEFAULT 1 COMMENT '任务状态: 1-待执行 2-执行中 3-已完成 4-失败 5-已取消',
    `task_priority` TINYINT DEFAULT 1 COMMENT '任务优先级: 1-低 2-中 3-高 4-紧急',
    `execution_mode` VARCHAR(50) DEFAULT 'AUTO' COMMENT '执行模式: AUTO-自动 MANUAL-手动 SCHEDULED-定时',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` INT DEFAULT 3 COMMENT '最大重试次数',
    `execution_result` JSON COMMENT '执行结果',
    `error_message` TEXT COMMENT '错误信息',
    `execution_start_time` DATETIME COMMENT '执行开始时间',
    `execution_end_time` DATETIME COMMENT '执行结束时间',
    `execution_duration` INT COMMENT '执行耗时（秒）',
    `next_execution_time` DATETIME COMMENT '下次执行时间',
    `cron_expression` VARCHAR(100) COMMENT 'Cron表达式',
    `task_config` JSON COMMENT '任务配置',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`task_id`),
    UNIQUE KEY `uk_task_code` (`task_code`, `deleted_flag`),
    KEY `idx_task_type` (`task_type`),
    KEY `idx_task_status` (`task_status`),
    KEY `idx_calculation_date` (`calculation_date`),
    KEY `idx_next_execution_time` (`next_execution_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤计算任务表';

-- 考勤计算结果表
CREATE TABLE IF NOT EXISTS `t_attendance_calculation_result` (
    `result_id` BIGINT NOT NULL COMMENT '结果ID',
    `task_id` BIGINT COMMENT '关联任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `department_id` BIGINT COMMENT '部门ID',
    `calculation_date` DATE NOT NULL COMMENT '计算日期',
    `shift_id` BIGINT COMMENT '班次ID',
    `schedule_start_time` TIME COMMENT '排班开始时间',
    `schedule_end_time` TIME COMMENT '排班结束时间',
    `actual_start_time` TIME COMMENT '实际开始时间',
    `actual_end_time` TIME COMMENT '实际结束时间',
    `work_duration` DECIMAL(5,2) COMMENT '工作时长（小时）',
    `overtime_duration` DECIMAL(5,2) COMMENT '加班时长（小时）',
    `late_duration` DECIMAL(5,2) COMMENT '迟到时长（分钟）',
    `early_duration` DECIMAL(5,2) COMMENT '早退时长（分钟）',
    `absence_duration` DECIMAL(5,2) COMMENT '缺勤时长（小时）',
    `leave_duration` DECIMAL(5,2) COMMENT '请假时长（小时）',
    `attendance_status` VARCHAR(50) NOT NULL COMMENT '考勤状态: NORMAL-正常 LATE-迟到 EARLY-早退 ABSENCE-缺勤 LEAVE-请假 OVERTIME-加班',
    `abnormal_flags` JSON COMMENT '异常标识',
    `rule_applied` JSON COMMENT '应用的规则',
    `calculation_version` VARCHAR(20) DEFAULT '1.0' COMMENT '计算版本',
    `is_final` TINYINT DEFAULT 0 COMMENT '是否最终结果: 0-否 1-是',
    `needs_review` TINYINT DEFAULT 0 COMMENT '需要人工审核: 0-否 1-是',
    `review_status` TINYINT COMMENT '审核状态: 1-待审核 2-已审核 3-已修改',
    `review_comments` TEXT COMMENT '审核意见',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`result_id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `calculation_date`, `deleted_flag`),
    KEY `idx_task_id` (`task_id`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_shift_id` (`shift_id`),
    KEY `idx_attendance_status` (`attendance_status`),
    KEY `idx_needs_review` (`needs_review`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤计算结果表';

-- ====================================================================
-- 5. 考勤报表统计相关表
-- ====================================================================

-- 考勤统计表（预计算，提高查询性能）
CREATE TABLE IF NOT EXISTS `t_attendance_statistics` (
    `statistics_id` BIGINT NOT NULL COMMENT '统计ID',
    `statistics_type` VARCHAR(50) NOT NULL COMMENT '统计类型: DAILY-日统计 WEEKLY-周统计 MONTHLY-月统计 YEARLY-年统计 DEPARTMENT-部门统计 USER-用户统计',
    `statistics_date` DATE NOT NULL COMMENT '统计日期',
    `statistics_period` VARCHAR(50) COMMENT '统计周期',
    `target_id` BIGINT COMMENT '目标ID（用户ID或部门ID）',
    `target_type` VARCHAR(50) COMMENT '目标类型: USER-用户 DEPARTMENT-部门 COMPANY-公司',
    `total_users` INT DEFAULT 0 COMMENT '总人数',
    `normal_count` INT DEFAULT 0 COMMENT '正常考勤人数',
    `late_count` INT DEFAULT 0 COMMENT '迟到人数',
    `early_count` INT DEFAULT 0 COMMENT '早退人数',
    `absence_count` INT DEFAULT 0 COMMENT '缺勤人数',
    `leave_count` INT DEFAULT 0 COMMENT '请假人数',
    `overtime_count` INT DEFAULT 0 COMMENT '加班人数',
    `total_work_hours` DECIMAL(10,2) DEFAULT 0 COMMENT '总工作时长',
    `total_overtime_hours` DECIMAL(10,2) DEFAULT 0 COMMENT '总加班时长',
    `attendance_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '出勤率',
    `punctuality_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '准点率',
    `overtime_rate` DECIMAL(5,2) DEFAULT 0 COMMENT '加班率',
    `statistics_data` JSON COMMENT '详细统计数据',
    `chart_data` JSON COMMENT '图表数据',
    `report_file_url` VARCHAR(500) COMMENT '报表文件URL',
    `generated_by` VARCHAR(100) COMMENT '生成人',
    `generated_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `is_published` TINYINT DEFAULT 0 COMMENT '是否发布: 0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识: 0-未删除 1-已删除',
    `version` INT DEFAULT 1 COMMENT '版本号',
    PRIMARY KEY (`statistics_id`),
    UNIQUE KEY `uk_statistics` (`statistics_type`, `statistics_date`, `target_id`, `target_type`, `deleted_flag`),
    KEY `idx_statistics_date` (`statistics_date`),
    KEY `idx_target_type` (`target_type`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_generated_time` (`generated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤统计表';

-- ====================================================================
-- 6. 创建索引优化
-- ====================================================================

-- 为现有表添加缺失的索引
ALTER TABLE `t_attendance_record`
ADD INDEX `idx_user_shift_date` (`user_id`, `shift_id`, `attendance_date`),
ADD INDEX `idx_punch_location` (`punch_location`),
ADD INDEX `idx_punch_device` (`punch_device_id`),
ADD INDEX `idx_abnormal_status` (`abnormal_status`);

ALTER TABLE `t_attendance_shift`
ADD INDEX `idx_shift_type_status` (`shift_type`, `shift_status`),
ADD INDEX `idx_work_time_range` (`work_start_time`, `work_end_time`);

-- ====================================================================
-- 7. 插入初始数据
-- ====================================================================

-- 插入默认考勤规则
INSERT INTO `t_attendance_rule_config` (
    `rule_id`, `rule_code`, `rule_name`, `rule_category`, `rule_type`,
    `rule_condition`, `rule_action`, `rule_priority`, `rule_status`,
    `create_user_id`, `create_time`
) VALUES
(1, 'LATE_THRESHOLD_5MIN', '迟到5分钟宽限', 'TIME', 'LATE_THRESHOLD',
    '{"threshold_minutes": 5, "include_lunch_break": false}',
    '{"action": "NORMAL", "notification": false}',
    10, 1, 1, NOW()),
(2, 'EARLY_THRESHOLD_5MIN', '早退5分钟宽限', 'TIME', 'EARLY_THRESHOLD',
    '{"threshold_minutes": 5, "include_lunch_break": false}',
    '{"action": "NORMAL", "notification": false}',
    10, 1, 1, NOW()),
(3, 'ABSENCE_NO_PUNCH', '无打卡记录视为缺勤', 'ABSENCE', 'NO_PUNCH_DETECTION',
    '{"required_punch_count": 2, "check_timeout_minutes": 60}',
    '{"action": "ABSENCE", "notification": true, "escalation_level": 1}',
    5, 1, 1, NOW());

-- 插入默认异常规则
INSERT INTO `t_attendance_abnormal_rule` (
    `abnormal_rule_id`, `abnormal_type`, `rule_name`, `detection_condition`,
    `abnormal_level`, `auto_approval`, `notification_config`, `rule_status`,
    `create_user_id`, `create_time`
) VALUES
(1, 'LATE', '连续3天迟到', '{"consecutive_days": 3, "threshold_minutes": 5}',
    3, 0, '{"channels": ["email", "sms"], "escalation": true}',
    1, 1, NOW()),
(2, 'ABSENCE', '无故缺勤', '{"reason": "UNEXCUSED", "minimum_hours": 4}',
    3, 0, '{"channels": ["email"], "escalation": true}',
    1, 1, NOW());

-- ====================================================================
-- 8. 创建视图
-- ====================================================================

-- 考勤汇总视图
CREATE OR REPLACE VIEW `v_attendance_summary` AS
SELECT
    u.user_id,
    u.actual_name,
    d.department_name,
    DATE(ar.attendance_date) as attendance_date,
    COUNT(CASE WHEN ar.abnormal_status = 'NORMAL' THEN 1 END) as normal_days,
    COUNT(CASE WHEN ar.abnormal_status = 'LATE' THEN 1 END) as late_days,
    COUNT(CASE WHEN ar.abnormal_status = 'EARLY' THEN 1 END) as early_days,
    COUNT(CASE WHEN ar.abnormal_status = 'ABSENCE' THEN 1 END) as absence_days,
    COUNT(CASE WHEN ar.abnormal_status = 'LEAVE' THEN 1 END) as leave_days,
    ROUND(COUNT(CASE WHEN ar.abnormal_status = 'NORMAL' THEN 1 END) * 100.0 /
        COUNT(*), 2) as attendance_rate,
    ROUND((COUNT(*) - COUNT(CASE WHEN ar.abnormal_status IN ('LATE', 'EARLY') THEN 1 END)) * 100.0 /
        COUNT(*), 2) as punctuality_rate
FROM t_smart_user u
LEFT JOIN t_attendance_record ar ON u.user_id = ar.user_id
LEFT JOIN t_common_department d ON u.department_id = d.department_id
WHERE u.deleted_flag = 0
GROUP BY u.user_id, u.actual_name, d.department_name, DATE(ar.attendance_date);

-- ====================================================================
-- 完成数据表创建
-- ====================================================================