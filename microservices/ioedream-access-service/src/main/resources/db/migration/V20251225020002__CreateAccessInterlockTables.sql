-- ================================================================
-- 门禁互锁功能表
-- 功能: 实现互斥区域控制（A开门时B自动锁定）
-- 作者: IOE-DREAM Team
-- 日期: 2025-12-25
-- ================================================================

-- 互锁规则表
CREATE TABLE t_access_interlock_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '互锁规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
    rule_type VARCHAR(30) NOT NULL COMMENT '互锁类型(AREA_INTERLOCK-区域互锁, DOOR_INTERLOCK-门互锁)',
    area_a_id BIGINT NOT NULL COMMENT '区域A ID',
    area_a_name VARCHAR(100) COMMENT '区域A名称',
    door_a_id BIGINT COMMENT '门A ID',
    area_b_id BIGINT NOT NULL COMMENT '区域B ID',
    area_b_name VARCHAR(100) COMMENT '区域B名称',
    door_b_id BIGINT COMMENT '门B ID',
    interlock_mode VARCHAR(30) NOT NULL COMMENT '互锁模式(BIDIRECTIONAL-双向, UNIDIRECTIONAL-单向)',
    unlock_condition VARCHAR(30) NOT NULL COMMENT '解锁条件(MANUAL-手动, TIMER-定时, AUTO-自动)',
    unlock_delay_seconds INT DEFAULT 0 COMMENT '自动解锁延迟秒数',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态(0-禁用 1-启用)',
    priority INT DEFAULT 100 COMMENT '优先级(1-100, 数字越小优先级越高)',
    description VARCHAR(500) COMMENT '规则描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    INDEX idx_area_a(area_a_id),
    INDEX idx_area_b(area_b_id),
    INDEX idx_enabled(enabled),
    INDEX idx_rule_type(rule_type),
    INDEX idx_priority(priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁互锁规则表';

-- 互锁执行日志表
CREATE TABLE t_access_interlock_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    rule_id BIGINT NOT NULL COMMENT '互锁规则ID',
    trigger_area_id BIGINT COMMENT '触发区域ID',
    trigger_area_name VARCHAR(100) COMMENT '触发区域名称',
    trigger_door_id BIGINT COMMENT '触发门ID',
    trigger_action VARCHAR(30) NOT NULL COMMENT '触发动作(OPEN_DOOR-开门)',
    target_area_id BIGINT COMMENT '目标区域ID',
    target_area_name VARCHAR(100) COMMENT '目标区域名称',
    target_door_id BIGINT COMMENT '目标门ID',
    target_action VARCHAR(30) NOT NULL COMMENT '执行动作(LOCK-锁定)',
    execution_status VARCHAR(30) NOT NULL COMMENT '执行状态(SUCCESS-成功, FAILED-失败)',
    execution_time DATETIME COMMENT '执行时间',
    error_message VARCHAR(500) COMMENT '错误信息',
    unlock_time DATETIME COMMENT '解锁时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_rule_id(rule_id),
    INDEX idx_trigger_area(trigger_area_id),
    INDEX idx_execution_status(execution_status),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互锁执行日志表';

-- 初始化示例数据
INSERT INTO t_access_interlock_rule (
    rule_name, rule_code, rule_type,
    area_a_id, area_a_name, door_a_id,
    area_b_id, area_b_name, door_b_id,
    interlock_mode, unlock_condition, unlock_delay_seconds,
    priority, enabled, description
) VALUES
(
    '实验室A与实验室B互锁', 'INTERLOCK_LAB_A_B', 'AREA_INTERLOCK',
    1001, '实验室A', 1001,
    1002, '实验室B', 1002,
    'BIDIRECTIONAL', 'AUTO', 30,
    10, 1, '实验室A和实验室B互锁，任一区域开门时另一区域自动锁定，30秒后自动解锁'
),
(
    '数据中心与服务器机房互锁', 'INTERLOCK_DATACENTER_SERVER', 'AREA_INTERLOCK',
    1003, '数据中心', 1003,
    1004, '服务器机房', 1004,
    'UNIDIRECTIONAL', 'MANUAL', 0,
    20, 1, '数据中心开门时服务器机房自动锁定，需手动解锁'
),
(
    '危险品存储区互锁', 'INTERLOCK_DANGEROUS_GOODS', 'AREA_INTERLOCK',
    1005, '危险品存储区A', 1005,
    1006, '危险品存储区B', 1006,
    'BIDIRECTIONAL', 'TIMER', 60,
    1, 1, '危险品存储区互锁，60秒后自动解锁'
);
