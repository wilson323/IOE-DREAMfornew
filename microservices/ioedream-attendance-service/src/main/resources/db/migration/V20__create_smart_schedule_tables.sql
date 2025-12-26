-- 智能排班算法引擎数据库表
-- 作者: IOE-DREAM Team
-- 日期: 2025-01-30
-- 描述: 支持智能排班算法、约束配置、优化结果存储

-- 排班计划表
CREATE TABLE IF NOT EXISTS `t_smart_schedule_plan` (
    `plan_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班计划ID',
    `plan_name` VARCHAR(200) NOT NULL COMMENT '计划名称',
    `plan_code` VARCHAR(100) COMMENT '计划编码',
    `plan_type` TINYINT NOT NULL DEFAULT 1 COMMENT '计划类型: 1-自动生成 2-手动优化 3-混合模式',

    -- 排班周期
    `start_date` DATE NOT NULL COMMENT '计划开始日期',
    `end_date` DATE NOT NULL COMMENT '计划结束日期',
    `period_days` INT NOT NULL COMMENT '排班周期（天）',

    -- 排班范围
    `department_id` BIGINT COMMENT '部门ID',
    `department_name` VARCHAR(200) COMMENT '部门名称',
    `employee_ids` TEXT COMMENT '员工ID列表（JSON数组）',
    `employee_count` INT NOT NULL DEFAULT 0 COMMENT '员工数量',

    -- 优化目标配置
    `optimization_goal` TINYINT NOT NULL DEFAULT 5 COMMENT '优化目标: 1-公平性优先 2-成本优先 3-效率优先 4-满意度优先 5-综合优化',
    `optimization_weights` TEXT COMMENT '优化目标权重（JSON格式）',

    -- 约束条件配置
    `constraints` TEXT COMMENT '约束条件（JSON格式）',
    `min_consecutive_work_days` INT NOT NULL DEFAULT 3 COMMENT '最小连续工作天数',
    `max_consecutive_work_days` INT NOT NULL DEFAULT 6 COMMENT '最大连续工作天数',
    `min_rest_days` INT NOT NULL DEFAULT 1 COMMENT '最小休息天数',
    `min_daily_staff` INT NOT NULL DEFAULT 5 COMMENT '每日最少在岗人数',
    `max_daily_staff` INT NOT NULL DEFAULT 20 COMMENT '每日最多在岗人数',

    -- 算法配置
    `algorithm_type` TINYINT NOT NULL DEFAULT 1 COMMENT '优化算法: 1-遗传算法 2-模拟退火 3-贪心算法 4-整数规划',
    `algorithm_params` TEXT COMMENT '算法参数（JSON格式）',
    `max_iterations` INT NOT NULL DEFAULT 1000 COMMENT '最大迭代次数',
    `convergence_threshold` DECIMAL(10,6) NOT NULL DEFAULT 0.001 COMMENT '收敛阈值',

    -- 执行状态
    `execution_status` TINYINT NOT NULL DEFAULT 0 COMMENT '执行状态: 0-待执行 1-执行中 2-已完成 3-执行失败',
    `execute_start_time` DATETIME COMMENT '执行开始时间',
    `execute_end_time` DATETIME COMMENT '执行结束时间',
    `execution_duration_ms` BIGINT COMMENT '执行耗时（毫秒）',
    `execution_progress` DECIMAL(5,2) COMMENT '执行进度（%）',

    -- 优化结果统计
    `objective_value` DECIMAL(10,6) COMMENT '目标函数值（最优解）',
    `fitness_score` DECIMAL(5,4) COMMENT '适应度值（0-1之间）',
    `iteration_count` INT COMMENT '迭代次数',
    `fairness_score` DECIMAL(5,4) COMMENT '公平性得分（0-1）',
    `cost_score` DECIMAL(5,4) COMMENT '成本得分（0-1）',
    `efficiency_score` DECIMAL(5,4) COMMENT '效率得分（0-1）',
    `satisfaction_score` DECIMAL(5,4) COMMENT '满意度得分（0-1）',
    `conflict_count` INT NOT NULL DEFAULT 0 COMMENT '冲突数量',
    `unsatisfied_constraint_count` INT NOT NULL DEFAULT 0 COMMENT '未满足约束数量',

    -- 操作信息
    `create_user_id` BIGINT COMMENT '创建人ID',
    `create_user_name` VARCHAR(100) COMMENT '创建人姓名',
    `remark` TEXT COMMENT '备注',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',

    PRIMARY KEY (`plan_id`),
    UNIQUE KEY `uk_plan_code` (`plan_code`, `deleted_flag`),
    KEY `idx_department` (`department_id`),
    KEY `idx_execution_status` (`execution_status`),
    KEY `idx_date_range` (`start_date`, `end_date`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能排班计划表';

-- 排班结果明细表
CREATE TABLE IF NOT EXISTS `t_smart_schedule_result` (
    `result_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班结果ID',
    `plan_id` BIGINT NOT NULL COMMENT '排班计划ID',

    -- 员工信息
    `employee_id` BIGINT NOT NULL COMMENT '员工ID',
    `employee_name` VARCHAR(100) NOT NULL COMMENT '员工姓名',
    `employee_no` VARCHAR(50) COMMENT '员工工号',
    `department_id` BIGINT COMMENT '部门ID',
    `department_name` VARCHAR(200) COMMENT '部门名称',

    -- 排班日期
    `schedule_date` DATE NOT NULL COMMENT '排班日期',
    `day_of_week` TINYINT NOT NULL COMMENT '星期: 1-周一 2-周二 ... 7-周日',
    `is_work_day` TINYINT NOT NULL DEFAULT 1 COMMENT '是否工作日: 0-否 1-是',

    -- 班次信息
    `shift_id` BIGINT NOT NULL COMMENT '班次ID',
    `shift_code` VARCHAR(50) COMMENT '班次编码',
    `shift_name` VARCHAR(100) COMMENT '班次名称',
    `shift_type` TINYINT COMMENT '班次类型: 1-固定 2-弹性 3-轮班 4-临时',

    -- 工作时间
    `work_start_time` TIME NOT NULL COMMENT '上班时间',
    `work_end_time` TIME NOT NULL COMMENT '下班时间',
    `work_duration` INT NOT NULL COMMENT '工作时长（分钟）',
    `lunch_start_time` TIME COMMENT '午休开始时间',
    `lunch_end_time` TIME COMMENT '午休结束时间',
    `lunch_duration` INT COMMENT '午休时长（分钟）',

    -- 统计信息
    `monthly_work_days` INT COMMENT '当月工作天数',
    `monthly_work_hours` DECIMAL(10,2) COMMENT '当月工作时长（小时）',
    `consecutive_work_days` INT COMMENT '连续工作天数',
    `consecutive_rest_days` INT COMMENT '连续休息天数',

    -- 优化信息
    `fitness_score` DECIMAL(5,4) COMMENT '适应度得分（0-1）',
    `has_conflict` TINYINT NOT NULL DEFAULT 0 COMMENT '是否存在冲突: 0-否 1-是',
    `conflict_types` VARCHAR(500) COMMENT '冲突类型（多个用逗号分隔）',
    `conflict_description` TEXT COMMENT '冲突描述',

    -- 状态信息
    `schedule_status` TINYINT NOT NULL DEFAULT 1 COMMENT '排班状态: 1-草稿 2-已确认 3-已执行 4-已取消',
    `confirm_time` DATETIME COMMENT '确认时间',
    `confirm_user_id` BIGINT COMMENT '确认人ID',
    `confirm_user_name` VARCHAR(100) COMMENT '确认人姓名',
    `remark` TEXT COMMENT '备注',

    -- 审计字段
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',

    PRIMARY KEY (`result_id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_employee_id` (`employee_id`),
    KEY `idx_schedule_date` (`schedule_date`),
    KEY `idx_shift_id` (`shift_id`),
    KEY `idx_department` (`department_id`),
    CONSTRAINT `fk_schedule_result_plan` FOREIGN KEY (`plan_id`)
        REFERENCES `t_smart_schedule_plan` (`plan_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能排班结果明细表';

-- 插入测试数据（可选）
INSERT INTO `t_smart_schedule_plan`
    (`plan_name`, `plan_code`, `plan_type`, `start_date`, `end_date`, `period_days`,
     `employee_count`, `optimization_goal`, `algorithm_type`, `max_iterations`)
VALUES
    ('2025年1月排班计划', 'PLAN_202501', 1, '2025-01-01', '2025-01-31', 31, 50, 5, 1, 1000);
