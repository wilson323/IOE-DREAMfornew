-- ===================================================
-- 反潜回功能数据库表
-- 创建时间: 2025-01-30
-- 说明: 支持4种反潜回模式（全局/区域/软/硬）
-- ===================================================

-- 反潜回配置表
CREATE TABLE t_access_anti_passback_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',

    -- 模式配置
    mode TINYINT NOT NULL COMMENT '模式: 1-全局 2-区域 3-软 4-硬',
    area_id BIGINT COMMENT '区域ID（区域模式时必填）',

    -- 时间窗口配置
    time_window BIGINT NOT NULL DEFAULT 300000 COMMENT '时间窗口（毫秒），默认5分钟',
    max_pass_count INT DEFAULT 1 COMMENT '最大允许通行次数（时间窗口内）',

    -- 启用状态
    enabled TINYINT DEFAULT 1 COMMENT '启用状态: 0-禁用 1-启用',

    -- 时间范围
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    expire_time DATETIME COMMENT '失效时间（NULL表示永久有效）',

    -- 告警配置
    alert_enabled TINYINT DEFAULT 1 COMMENT '是否启用告警: 0-禁用 1-启用',
    alert_methods VARCHAR(100) COMMENT '告警方式（逗号分隔）: EMAIL, SMS, WEBSOCKET',

    -- 审计字段
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',

    -- 索引
    INDEX idx_mode_enabled (mode, enabled, deleted_flag),
    INDEX idx_area_enabled (area_id, enabled, deleted_flag),
    INDEX idx_effective_time (effective_time, expire_time),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁反潜回配置表';

-- 反潜回检测记录表
CREATE TABLE t_access_anti_passback_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',

    -- 用户信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    user_card_no VARCHAR(50) COMMENT '卡号',

    -- 设备信息
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    device_code VARCHAR(50) COMMENT '设备编码',

    -- 区域信息
    area_id BIGINT COMMENT '区域ID',
    area_name VARCHAR(100) COMMENT '区域名称',

    -- 检测结果
    result TINYINT NOT NULL COMMENT '结果: 1-正常通行 2-软反潜回（告警但允许） 3-硬反潜回（阻止通行）',
    violation_type TINYINT COMMENT '违规类型: 1-时间窗口内重复 2-跨区域异常 3-频次超限',

    -- 时间信息
    pass_time DATETIME NOT NULL COMMENT '通行时间',
    detected_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',

    -- 处理状态
    handled TINYINT DEFAULT 0 COMMENT '处理状态: 0-未处理 1-已处理 2-已忽略',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    handled_by BIGINT COMMENT '处理人ID',
    handled_time DATETIME COMMENT '处理时间',

    -- 详细信息（JSON格式）
    detail_info JSON COMMENT '详细信息（JSON格式）',

    -- 审计字段
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',

    -- 索引
    INDEX idx_user_time (user_id, pass_time, deleted_flag),
    INDEX idx_device_time (device_id, pass_time, deleted_flag),
    INDEX idx_area_time (area_id, pass_time, deleted_flag),
    INDEX idx_result_handled (result, handled, deleted_flag),
    INDEX idx_detected_time (detected_time, deleted_flag),
    INDEX idx_pass_time (pass_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁反潜回检测记录表';

-- 反潜回最近通行记录缓存表（可选，用于Redis缓存失效后恢复）
CREATE TABLE t_access_anti_passback_cache (
    cache_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缓存ID',

    -- 缓存键
    cache_key VARCHAR(200) NOT NULL COMMENT '缓存键: anti_passback:{user_id}:{area_id?}',
    cache_key_hash VARCHAR(64) NOT NULL COMMENT '缓存键哈希值（用于快速查找）',

    -- 用户信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    area_id BIGINT COMMENT '区域ID（全局模式为NULL）',

    -- 最近通行信息（JSON格式）
    recent_passes JSON NOT NULL COMMENT '最近通行记录列表（JSON数组）',

    -- 过期时间
    expire_time DATETIME NOT NULL COMMENT '过期时间',

    -- 审计字段
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    UNIQUE INDEX uk_cache_key (cache_key),
    INDEX idx_user_area (user_id, area_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反潜回缓存表（可选）';

-- 初始化默认配置（全局软反潜回，5分钟时间窗口）
INSERT INTO t_access_anti_passback_config (
    mode, time_window, max_pass_count, enabled, effective_time, alert_enabled, alert_methods
) VALUES (
    1, 300000, 1, 1, NOW(), 1, 'WEBSOCKET'
);

-- 创建性能优化索引
CREATE INDEX idx_user_pass_time ON t_access_anti_passback_record(user_id, pass_time, deleted_flag, result);
CREATE INDEX idx_device_pass_time ON t_access_anti_passback_record(device_id, pass_time, deleted_flag, result);
CREATE INDEX idx_area_pass_time ON t_access_anti_passback_record(area_id, pass_time, deleted_flag, result);
