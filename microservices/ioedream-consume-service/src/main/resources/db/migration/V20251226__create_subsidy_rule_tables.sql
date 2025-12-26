-- ============================================================
-- 补贴规则引擎数据库表
-- 创建日期: 2025-12-26
-- 说明: 支持灵活的补贴规则配置与自动计算
-- ============================================================

-- ------------------------------------------------------------
-- 1. 补贴规则表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_subsidy_rule (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_code VARCHAR(64) NOT NULL UNIQUE COMMENT '规则编码',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',

    -- 规则类型
    rule_type TINYINT NOT NULL COMMENT '规则类型 1-固定金额 2-比例补贴 3-阶梯补贴 4-限时补贴',
    subsidy_type TINYINT NOT NULL COMMENT '补贴类型 1-餐补 2-交通补 3-其他',

    -- 规则配置
    subsidy_amount DECIMAL(10, 2) COMMENT '补贴金额(固定金额模式)',
    subsidy_rate DECIMAL(5, 4) COMMENT '补贴比例(比例模式 0-1)',
    max_subsidy_amount DECIMAL(10, 2) COMMENT '最高补贴限额',

    -- 阶梯补贴配置(JSON)
    tier_config TEXT COMMENT '阶梯配置: [{"minAmount":0,"rate":0.5},{"minAmount":20,"rate":0.6}]',

    -- 时间条件
    apply_time_type TINYINT DEFAULT 1 COMMENT '适用时间 1-全部 2-工作日 3-周末 4-自定义',
    apply_start_time TIME COMMENT '开始时间',
    apply_end_time TIME COMMENT '结束时间',
    apply_days VARCHAR(32) COMMENT '适用日期(1,2,3,4,5,6,7 代表周一到周日)',

    -- 餐别条件
    apply_meal_types VARCHAR(32) COMMENT '适用餐别(1-早餐 2-午餐 3-晚餐 多个用逗号隔开)',

    -- 优先级和状态
    priority INT DEFAULT 0 COMMENT '优先级(数字越大优先级越高)',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',

    -- 有效期
    effective_date DATETIME NOT NULL COMMENT '生效时间',
    expire_date DATETIME COMMENT '失效时间',

    -- 扩展
    description VARCHAR(500) COMMENT '规则描述',
    extended_attributes TEXT COMMENT '扩展属性(JSON)',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '版本号(乐观锁)',

    -- 索引
    INDEX idx_rule_type (rule_type, status),
    INDEX idx_subsidy_type (subsidy_type),
    INDEX idx_priority (priority DESC, status),
    INDEX idx_effective_date (effective_date, expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补贴规则表';

-- ------------------------------------------------------------
-- 2. 补贴规则条件表（复杂条件组合）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_subsidy_rule_condition (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '条件ID',
    condition_uuid VARCHAR(64) NOT NULL UNIQUE COMMENT '条件UUID',

    -- 规则关联
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_code VARCHAR(64) NOT NULL COMMENT '规则编码',

    -- 条件类型
    condition_type VARCHAR(32) NOT NULL COMMENT '条件类型 user_group/department/area/device/consume_amount',

    -- 条件配置
    condition_operator VARCHAR(16) NOT NULL COMMENT '操作符 eq/in/gt/lt/between',
    condition_value TEXT NOT NULL COMMENT '条件值(JSON数组)',

    -- 逻辑关系
    logic_operator VARCHAR(8) DEFAULT 'AND' COMMENT '逻辑关系 AND/OR/NOT',

    -- 状态
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',

    -- 扩展
    description VARCHAR(500) COMMENT '条件描述',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    -- 索引
    INDEX idx_rule_id (rule_id),
    INDEX idx_rule_code (rule_code),
    INDEX idx_condition_type (condition_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补贴规则条件表';

-- ------------------------------------------------------------
-- 3. 补贴规则执行日志表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_subsidy_rule_log (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    log_uuid VARCHAR(64) NOT NULL UNIQUE COMMENT '日志UUID',

    -- 执行信息
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    rule_code VARCHAR(64) NOT NULL COMMENT '规则编码',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',

    -- 消费信息
    consume_id BIGINT NOT NULL COMMENT '消费记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    consume_amount DECIMAL(10, 2) NOT NULL COMMENT '消费金额',
    consume_time DATETIME NOT NULL COMMENT '消费时间',

    -- 补贴计算
    subsidy_amount DECIMAL(10, 2) NOT NULL COMMENT '补贴金额',
    subsidy_rate DECIMAL(5, 4) COMMENT '补贴比例',
    calculation_detail TEXT COMMENT '计算详情(JSON)',

    -- 执行结果
    execution_status TINYINT DEFAULT 1 COMMENT '执行状态 1-成功 2-失败',
    error_message VARCHAR(500) COMMENT '错误信息',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    -- 索引
    INDEX idx_rule_id (rule_id),
    INDEX idx_consume_id (consume_id),
    INDEX idx_user_id (user_id),
    INDEX idx_consume_time (consume_time),
    INDEX idx_execution_status (execution_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补贴规则执行日志表';

-- ------------------------------------------------------------
-- 4. 用户补贴记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_user_subsidy_record (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    record_uuid VARCHAR(64) NOT NULL UNIQUE COMMENT '记录UUID',

    -- 用户信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(64) COMMENT '用户名',

    -- 补贴信息
    subsidy_type TINYINT NOT NULL COMMENT '补贴类型 1-餐补 2-交通补 3-其他',
    subsidy_amount DECIMAL(10, 2) NOT NULL COMMENT '补贴金额',
    consume_amount DECIMAL(10, 2) NOT NULL COMMENT '原消费金额',

    -- 规则信息
    rule_id BIGINT COMMENT '应用规则ID',
    rule_code VARCHAR(64) COMMENT '应用规则编码',

    -- 消费信息
    consume_id BIGINT NOT NULL COMMENT '消费记录ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    meal_type TINYINT COMMENT '餐别 1-早餐 2-午餐 3-晚餐',

    -- 时间信息
    subsidy_date DATE NOT NULL COMMENT '补贴日期',
    consume_time DATETIME NOT NULL COMMENT '消费时间',

    -- 状态
    status TINYINT DEFAULT 1 COMMENT '状态 0-失效 1-有效 2-已冲回',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',

    -- 索引
    UNIQUE INDEX uk_user_date_type (user_id, subsidy_date, subsidy_type, deleted_flag),
    INDEX idx_user_id (user_id),
    INDEX idx_subsidy_date (subsidy_date),
    INDEX idx_subsidy_type (subsidy_type),
    INDEX idx_consume_id (consume_id),
    INDEX idx_rule_id (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户补贴记录表';

-- ------------------------------------------------------------
-- 初始化默认补贴规则
-- ------------------------------------------------------------
INSERT INTO t_subsidy_rule (rule_code, rule_name, rule_type, subsidy_type, subsidy_amount, subsidy_rate, max_subsidy_amount, apply_time_type, apply_meal_types, priority, status, effective_date, description) VALUES
-- 工作日午餐补贴（固定金额）
('SUBSIDY_WORKDAY_LUNCH', '工作日午餐补贴', 1, 1, 5.00, NULL, 5.00, 2, '2', 100, 1, NOW(), '工作日午餐固定补贴5元'),

-- 周末全天补贴（固定金额）
('SUBSIDY_WEEKEND_ALL', '周末全天补贴', 1, 1, 8.00, NULL, 8.00, 3, '1,2,3', 90, 1, NOW(), '周末全天任意餐补贴8元'),

-- 按比例补贴（70%）
('SUBSIDY_RATIO_70', '70%比例补贴', 2, 1, NULL, 0.7000, 10.00, 1, '1,2,3', 80, 1, NOW(), '任意消费70%补贴，最高10元'),

-- 早餐限时补贴
('SUBSIDY_BREAKFAST_LIMITED', '早餐限时补贴', 4, 1, 3.00, NULL, 3.00, 4, '1', 95, 1, NOW(), '早8点前早餐补贴3元')
ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 初始化规则条件示例
-- ------------------------------------------------------------
INSERT INTO t_subsidy_rule_condition (condition_uuid, rule_id, rule_code, condition_type, condition_operator, condition_value, logic_operator, description) VALUES
-- 工作日午餐补贴条件：工作日 + 午餐
('COND_WORKDAY_LUNCH_001', (SELECT id FROM t_subsidy_rule WHERE rule_code = 'SUBSIDY_WORKDAY_LUNCH'), 'SUBSIDY_WORKDAY_LUNCH', 'consume_amount', 'between', '[0, 999999]', 'AND', '任意消费金额'),
('COND_WORKDAY_LUNCH_002', (SELECT id FROM t_subsidy_rule WHERE rule_code = 'SUBSIDY_WORKDAY_LUNCH'), 'SUBSIDY_WORKDAY_LUNCH', 'meal_type', 'in', '[2]', 'AND', '仅限午餐'),

-- 周末补贴条件：周末 + 全天
('COND_WEEKEND_001', (SELECT id FROM t_subsidy_rule WHERE rule_code = 'SUBSIDY_WEEKEND_ALL'), 'SUBSIDY_WEEKEND_ALL', 'consume_amount', 'between', '[0, 999999]', 'AND', '任意消费金额'),
('COND_WEEKEND_002', (SELECT id FROM t_subsidy_rule WHERE rule_code = 'SUBSIDY_WEEKEND_ALL'), 'SUBSIDY_WEEKEND_ALL', 'meal_type', 'in', '[1,2,3]', 'AND', '全天任意餐别')

ON DUPLICATE KEY UPDATE update_time = CURRENT_TIMESTAMP;
