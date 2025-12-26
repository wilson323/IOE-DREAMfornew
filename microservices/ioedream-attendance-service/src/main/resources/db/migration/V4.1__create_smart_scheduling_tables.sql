-- ============================================================
-- P0-4: OptaPlanner智能排班功能 - 数据库表结构
-- 版本: V4.1
-- 创建时间: 2025-12-26
-- 说明: 智能排班优化、班次分配、约束规则、历史记录
-- ============================================================

-- ============================================================
-- 表1: 智能排班方案表 (t_smart_scheduling_plan)
-- 说明: 存储OptaPlanner生成的排班方案
-- ============================================================
CREATE TABLE IF NOT EXISTS t_smart_scheduling_plan (
    plan_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排班方案ID',
    plan_name VARCHAR(200) NOT NULL COMMENT '方案名称',
    plan_description TEXT COMMENT '方案描述',

    -- 优化目标配置
    objective_type VARCHAR(50) NOT NULL DEFAULT 'FAIRNESS' COMMENT '优化目标类型: FAIRNESS-公平性, COST-成本, COMBINED-综合',
    fairness_weight DECIMAL(5,2) DEFAULT 0.40 COMMENT '公平性权重 (0-1)',
    compliance_weight DECIMAL(5,2) DEFAULT 0.35 COMMENT '合规性权重 (0-1)',
    cost_weight DECIMAL(5,2) DEFAULT 0.25 COMMENT '成本权重 (0-1)',

    -- 求解配置
    solver_duration_seconds INT DEFAULT 300 COMMENT '求解最长时长(秒)',
    solver_algorithm VARCHAR(50) DEFAULT 'TABU_SEARCH' COMMENT '求解算法: TABU_SEARCH, SIMULATED_ANNEALING, LATE_ACCEPTANCE',

    -- 求解结果
    solution_score BIGINT COMMENT '方案评分(HardSoftScore)',
    hard_score BIGINT COMMENT '硬约束得分',
    soft_score BIGINT COMMENT '软约束得分',
    solution_status VARCHAR(50) DEFAULT 'SOLVING' COMMENT '方案状态: SOLVING-求解中, SUCCESS-成功, FAILED-失败, CANCELLED-已取消',

    -- 方案元数据
    planning_horizon_start DATE NOT NULL COMMENT '排班计划开始日期',
    planning_horizon_end DATE NOT NULL COMMENT '排班计划结束日期',
    total_shifts INT DEFAULT 0 COMMENT '总班次数',
    total_assignments INT DEFAULT 0 COMMENT '总分配数',
    assigned_employees INT DEFAULT 0 COMMENT '已分配员工数',
    utilization_rate DECIMAL(5,2) COMMENT '员工利用率(%)',

    -- 统计信息
    fair_distribution_score DECIMAL(5,2) COMMENT '公平性得分(0-100)',
    compliance_score DECIMAL(5,2) COMMENT '合规性得分(0-100)',
    cost_optimization_score DECIMAL(5,2) COMMENT '成本优化得分(0-100)',
    constraint_violations INT DEFAULT 0 COMMENT '约束违规次数',

    -- 审计字段
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',

    INDEX idx_plan_status (solution_status),
    INDEX idx_plan_dates (planning_horizon_start, planning_horizon_end),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能排班方案表';

-- ============================================================
-- 表2: 班次分配表 (t_shift_assignment)
-- 说明: 存储排班方案中的具体班次分配结果
-- ============================================================
CREATE TABLE IF NOT EXISTS t_shift_assignment (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分配ID',
    plan_id BIGINT NOT NULL COMMENT '排班方案ID (外键)',
    shift_id BIGINT NOT NULL COMMENT '班次ID (外键到t_work_shift)',
    employee_id BIGINT NOT NULL COMMENT '员工ID (外键到t_common_user)',

    -- 时间信息
    assignment_date DATE NOT NULL COMMENT '分配日期',
    start_time DATETIME NOT NULL COMMENT '班次开始时间',
    end_time DATETIME NOT NULL COMMENT '班次结束时间',
    work_hours DECIMAL(4,2) COMMENT '工作时长(小时)',

    -- 班次类型
    shift_type VARCHAR(50) COMMENT '班次类型: MORNING-早班, AFTERNOON-午班, NIGHT-夜班',
    shift_role VARCHAR(100) COMMENT '班次角色: 普通员工、组长、主管等',

    -- 技能要求
    required_skills JSON COMMENT '所需技能列表 [{"skillId":1,"skillName":"门禁操作"}]',
    employee_skills JSON COMMENT '员工技能列表',

    -- 分配状态
    assignment_status VARCHAR(50) DEFAULT 'ASSIGNED' COMMENT '分配状态: ASSIGNED-已分配, CANCELLED-已取消, SWAPPED-已调换',
    pinned BOOLEAN DEFAULT FALSE COMMENT '是否固定(固定后OptaPlanner不会修改)',

    -- 质量指标
    preference_score INT COMMENT '偏好得分(0-100,员工对该班次的偏好程度)',
    fairness_score INT COMMENT '公平性得分(0-100,该分配的公平程度)',
    consecutive_days INT COMMENT '连续工作天数',

    -- 审计字段
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    FOREIGN KEY (plan_id) REFERENCES t_smart_scheduling_plan(plan_id) ON DELETE CASCADE,
    INDEX idx_plan_shift (plan_id, shift_id),
    INDEX idx_employee (employee_id),
    INDEX idx_date (assignment_date),
    INDEX idx_status (assignment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班次分配表';

-- ============================================================
-- 表3: 约束规则表 (t_scheduling_constraint)
-- 说明: 存储排班约束规则配置
-- ============================================================
CREATE TABLE IF NOT EXISTS t_scheduling_constraint (
    constraint_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '约束ID',
    constraint_name VARCHAR(200) NOT NULL COMMENT '约束名称',
    constraint_code VARCHAR(100) NOT NULL UNIQUE COMMENT '约束编码(唯一标识)',

    -- 约束分类
    constraint_type VARCHAR(50) NOT NULL COMMENT '约束类型: HARD-硬约束, SOFT-软约束',
    constraint_category VARCHAR(100) NOT NULL COMMENT '约束分类: SKILL-技能, TIME-时间, FAIRNESS-公平性, COST-成本',

    -- 约束描述
    constraint_description TEXT COMMENT '约束描述',
    constraint_expression TEXT COMMENT '约束表达式(Aviator表达式)',

    -- 权重配置
    weight_level INT DEFAULT 5 COMMENT '权重等级(1-10, 软约束使用)',
    penalty_value BIGINT DEFAULT -1 COMMENT '惩罚值(负数表示惩罚)',

    -- 状态控制
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用 0-禁用 1-启用',
    is_mandatory TINYINT DEFAULT 0 COMMENT '是否必须(1表示不可调整)',

    -- 适用范围
    applies_to_all TINYINT DEFAULT 1 COMMENT '是否适用于所有员工',
    applicable_roles JSON COMMENT '适用角色列表',
    applicable_shifts JSON COMMENT '适用班次列表',

    -- 审计字段
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    INDEX idx_type_enabled (constraint_type, is_enabled),
    INDEX idx_category (constraint_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班约束规则表';

-- ============================================================
-- 表4: 排班历史表 (t_scheduling_history)
-- 说明: 记录排班方案的调整历史和操作日志
-- ============================================================
CREATE TABLE IF NOT EXISTS t_scheduling_history (
    history_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史记录ID',
    plan_id BIGINT NOT NULL COMMENT '排班方案ID',
    assignment_id BIGINT COMMENT '班次分配ID(可选,空表示方案级别操作)',

    -- 操作信息
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE-创建, UPDATE-更新, DELETE-删除, CONFIRM-确认, CANCEL-取消',
    operation_description TEXT COMMENT '操作描述',

    -- 变更内容
    before_data JSON COMMENT '变更前数据',
    after_data JSON COMMENT '变更后数据',
    changed_fields JSON COMMENT '变更字段列表',

    -- 操作人员
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    -- 审计字段
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    FOREIGN KEY (plan_id) REFERENCES t_smart_scheduling_plan(plan_id) ON DELETE CASCADE,
    INDEX idx_plan (plan_id),
    INDEX idx_operator (operator_id),
    INDEX idx_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班历史表';

-- ============================================================
-- 初始化数据: 插入标准约束规则
-- ============================================================

-- 硬约束: 员工可用性
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value, is_mandatory) VALUES
('员工可用性检查', 'EMPLOYEE_AVAILABLE', 'HARD', 'TIME', '不可用员工不能排班', 10, -1000, 1);

-- 硬约束: 技能匹配
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value, is_mandatory) VALUES
('技能匹配检查', 'SKILL_MATCHING', 'HARD', 'SKILL', '员工必须具备班次所需技能', 10, -1000, 1);

-- 硬约束: 时间冲突
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value, is_mandatory) VALUES
('时间冲突检查', 'TIME_CONFLICT', 'HARD', 'TIME', '员工同一时间只能排一个班次', 10, -1000, 1);

-- 硬约束: 班次时长限制
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value, is_mandatory) VALUES
('每日工作时长限制', 'DAILY_WORK_HOURS_LIMIT', 'HARD', 'TIME', '每天工作时长不超过法定限制(12小时)', 10, -1000, 1);

-- 硬约束: 休息时间要求
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value, is_mandatory) VALUES
('班次间休息时间', 'REST_TIME_BETWEEN_SHIFTS', 'HARD', 'TIME', '两个班次之间必须有足够休息(至少11小时)', 10, -1000, 1);

-- 软约束: 公平性
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value) VALUES
('排班次数均衡', 'FAIR_DISTRIBUTION', 'SOFT', 'FAIRNESS', '员工排班次数尽量均衡(标准差最小化)', 8, -10);

-- 软约束: 偏好匹配
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value) VALUES
('员工班次偏好', 'PREFERENCE_MATCHING', 'SOFT', 'FAIRNESS', '优先满足员工班次偏好', 7, -8);

-- 软约束: 避免过度连续排班
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value) VALUES
('连续工作天数限制', 'CONSECUTIVE_SHIFTS_LIMIT', 'SOFT', 'TIME', '避免过度连续排班(不超过6天)', 6, -6);

-- 软约束: 技能利用率
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value) VALUES
('最大化技能利用率', 'SKILL_UTILIZATION', 'SOFT', 'SKILL', '优先使用具备专业技能的员工', 5, -5);

-- 软约束: 成本优化
INSERT INTO t_scheduling_constraint (constraint_name, constraint_code, constraint_type, constraint_category, constraint_description, weight_level, penalty_value) VALUES
('人力成本最小化', 'COST_OPTIMIZATION', 'SOFT', 'COST', '优先使用成本较低的员工', 9, -9);
