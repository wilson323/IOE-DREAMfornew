-- ================================================================
-- 人数控制功能表
-- 功能: 区域人数限制，超员禁止进入
-- 作者: IOE-DREAM Team
-- 日期: 2025-12-25
-- ================================================================

CREATE TABLE t_access_capacity_control (
    rule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    area_name VARCHAR(100) COMMENT '区域名称',
    max_capacity INT NOT NULL COMMENT '最大容量',
    current_count INT DEFAULT 0 COMMENT '当前人数',
    control_mode VARCHAR(30) NOT NULL COMMENT '控制模式(STRICT-严格, WARNING-警告)',
    check_out_mode VARCHAR(30) NOT NULL COMMENT '出门模式(ALLOW-允许, CHECK-检查)',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态(0-禁用 1-启用)',
    description VARCHAR(500) COMMENT '规则描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_area_id(area_id),
    INDEX idx_enabled(enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人数控制规则表';

INSERT INTO t_access_capacity_control (
    rule_name, area_id, area_name, max_capacity, current_count, control_mode, check_out_mode, enabled, description
) VALUES
(
    '会议室A容量控制', 1001, '会议室A', 20, 0, 'STRICT', 'ALLOW', 1, '会议室A最多容纳20人，超员禁止进入'
),
(
    '数据中心容量控制', 1002, '数据中心', 5, 0, 'STRICT', 'CHECK', 1, '数据中心最多5人，进出都需要检查'
);
