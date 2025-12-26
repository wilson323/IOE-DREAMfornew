-- ================================================================
-- 疏散点管理功能表
-- 功能: 一键全开所有门，用于紧急疏散
-- 作者: IOE-DREAM Team
-- 日期: 2025-12-25
-- ================================================================

CREATE TABLE t_access_evacuation_point (
    point_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '疏散点ID',
    point_name VARCHAR(100) NOT NULL COMMENT '疏散点名称',
    point_code VARCHAR(50) NOT NULL COMMENT '疏散点编码',
    area_ids TEXT NOT NULL COMMENT '关联区域ID列表(JSON数组)',
    door_ids TEXT NOT NULL COMMENT '关联门ID列表(JSON数组)',
    evacuation_mode VARCHAR(30) NOT NULL COMMENT '疏散模式(ALL_OPEN-全开, SPECIFIED-指定门)',
    priority INT DEFAULT 100 COMMENT '优先级(1-100)',
    enabled TINYINT DEFAULT 1 COMMENT '启用状态(0-禁用 1-启用)',
    description VARCHAR(500) COMMENT '疏散点描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_enabled(enabled),
    INDEX idx_priority(priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疏散点管理表';

INSERT INTO t_access_evacuation_point (
    point_name, point_code, area_ids, door_ids, evacuation_mode, priority, enabled, description
) VALUES
(
    '主楼全员疏散', 'EVACUATION_MAIN_BUILDING',
    '[1001, 1002, 1003, 1004, 1005]',
    '[1001, 2001, 2002, 2003, 3001, 3002, 3003]',
    'ALL_OPEN', 1, 1, '紧急情况下一键打开主楼所有门'
),
(
    '实验室区域疏散', 'EVACUATION_LAB_AREA',
    '[2001, 2002, 2003]',
    '[4001, 4002, 4003]',
    'ALL_OPEN', 10, 1, '实验室区域紧急疏散'
);
