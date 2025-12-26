-- ================================================================
-- 门禁联动功能表
-- 功能: 实现多门联动控制（触发门A自动开门B）
-- 作者: IOE-DREAM Team
-- 日期: 2025-12-25
-- ================================================================

-- 联动规则表
CREATE TABLE t_access_linkage_rule (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '联动规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) NOT NULL COMMENT '规则编码',
    rule_type VARCHAR(30) NOT NULL COMMENT '联动类型(DOOR_LINKAGE-门联动, AREA_LINKAGE-区域联动)',
    trigger_type VARCHAR(30) NOT NULL COMMENT '触发类型(OPEN_DOOR-开门, CLOSE_DOOR-关门, ALARM-告警)',
    trigger_device_id BIGINT NOT NULL COMMENT '触发设备ID',
    trigger_door_id BIGINT COMMENT '触发门ID',
    action_type VARCHAR(30) NOT NULL COMMENT '执行动作(OPEN_DOOR-开门, CLOSE_DOOR-关门, LOCK-锁定, UNLOCK-解锁)',
    target_device_id BIGINT NOT NULL COMMENT '目标设备ID',
    target_door_id BIGINT COMMENT '目标门ID',
    delay_seconds INT DEFAULT 0 COMMENT '延迟执行秒数',
    condition_expression VARCHAR(500) COMMENT '条件表达式(JSON格式)',
    priority INT DEFAULT 100 COMMENT '优先级(1-100, 数字越小优先级越高)',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态(0-禁用 1-启用)',
    valid_from DATETIME COMMENT '生效开始时间',
    valid_until DATETIME COMMENT '生效结束时间',
    description VARCHAR(500) COMMENT '规则描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    INDEX idx_trigger_device(trigger_device_id),
    INDEX idx_target_device(target_device_id),
    INDEX idx_enabled(enabled),
    INDEX idx_rule_type(rule_type),
    INDEX idx_priority(priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁联动规则表';

-- 联动执行日志表
CREATE TABLE t_access_linkage_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    rule_id BIGINT NOT NULL COMMENT '联动规则ID',
    trigger_device_id BIGINT NOT NULL COMMENT '触发设备ID',
    trigger_door_id BIGINT COMMENT '触发门ID',
    trigger_event VARCHAR(50) NOT NULL COMMENT '触发事件',
    target_device_id BIGINT NOT NULL COMMENT '目标设备ID',
    target_door_id BIGINT COMMENT '目标门ID',
    action_type VARCHAR(30) NOT NULL COMMENT '执行动作',
    execution_status VARCHAR(30) NOT NULL COMMENT '执行状态(SUCCESS-成功, FAILED-失败, PENDING-待执行)',
    execution_time DATETIME COMMENT '执行时间',
    error_message VARCHAR(500) COMMENT '错误信息',
    execution_result VARCHAR(1000) COMMENT '执行结果(JSON格式)',
    trigger_data VARCHAR(1000) COMMENT '触发数据(JSON格式)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_rule_id(rule_id),
    INDEX idx_trigger_device(trigger_device_id),
    INDEX idx_execution_status(execution_status),
    INDEX idx_create_time(create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联动执行日志表';

-- 联动规则与时间段关联表（支持基于时间段启用联动）
CREATE TABLE t_access_linkage_time_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rule_id BIGINT NOT NULL COMMENT '联动规则ID',
    time_rule_name VARCHAR(100) NOT NULL COMMENT '时间段规则名称',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    week_days VARCHAR(20) NOT NULL COMMENT '生效星期(逗号分隔,如:1,2,3,4,5)',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_id(rule_id),
    INDEX idx_enabled(enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联动时间段规则表';

-- 初始化示例数据
INSERT INTO t_access_linkage_rule (
    rule_name, rule_code, rule_type, trigger_type,
    trigger_device_id, trigger_door_id, action_type,
    target_device_id, target_door_id, delay_seconds,
    priority, enabled, description
) VALUES
(
    '主门联动侧门', 'LINKAGE_MAIN_TO_SIDE', 'DOOR_LINKAGE', 'OPEN_DOOR',
    1001, 1001, 'OPEN_DOOR',
    1002, 2001, 3,
    10, 1, '主门打开3秒后自动打开侧门'
),
(
    '大厅联动会议室', 'LINKAGE_LOBBY_TO_MEETING', 'AREA_LINKAGE', 'OPEN_DOOR',
    1001, 1001, 'OPEN_DOOR',
    1003, NULL, 0,
    20, 1, '大厅开门时同时打开会议室所有门'
),
(
    '告警联动锁定', 'LINKAGE_ALARM_LOCK', 'ALARM_LINKAGE', 'ALARM',
    1004, NULL, 'LOCK',
    1005, NULL, 0,
    1, 1, '检测到告警时立即锁定安全门'
);
