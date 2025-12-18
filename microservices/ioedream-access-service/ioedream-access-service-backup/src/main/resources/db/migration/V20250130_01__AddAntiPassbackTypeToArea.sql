-- 为区域表添加反潜回类型字段
-- 支持四种反潜回算法配置
-- 严格遵循CLAUDE.md数据库设计规范

-- 添加反潜回类型字段
ALTER TABLE t_common_area
ADD COLUMN anti_passback_type VARCHAR(20) DEFAULT 'NONE'
COMMENT '反潜回类型：NONE-无反潜回 HARD-硬反潜回 SOFT-软反潜回 AREA-区域反潜回 GLOBAL-全局反潜回';

-- 为新字段创建索引以提升查询性能
CREATE INDEX idx_area_anti_passback_type ON t_common_area(anti_passback_type);

-- 为反潜回配置创建组合索引
CREATE INDEX idx_area_security_anti_passback ON t_common_area(security_level, anti_passback_type);

-- 添加字段约束确保数据完整性
ALTER TABLE t_common_area
ADD CONSTRAINT chk_anti_passback_type
CHECK (anti_passback_type IN ('NONE', 'HARD', 'SOFT', 'AREA', 'GLOBAL'));

-- 插入默认配置数据
UPDATE t_common_area
SET anti_passback_type = CASE
    WHEN security_level = 4 THEN 'GLOBAL'  -- 机密区域使用全局反潜回
    WHEN security_level = 3 THEN 'HARD'    -- 核心区域使用硬反潜回
    WHEN security_level = 2 THEN 'SOFT'    -- 重要区域使用软反潜回
    ELSE 'NONE'                             -- 普通区域无反潜回
END
WHERE anti_passback_type = 'NONE' AND deleted_flag = 0;

-- 创建反潜回违规记录表
CREATE TABLE IF NOT EXISTS t_anti_passback_violation (
    violation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '违规记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    violation_type VARCHAR(20) NOT NULL COMMENT '违规类型：HARD/SOFT/AREA/GLOBAL',
    violation_reason VARCHAR(500) COMMENT '违规原因',
    violation_time DATETIME NOT NULL COMMENT '违规时间',
    access_data TEXT COMMENT '通行数据(JSON)',
    is_alert_sent TINYINT DEFAULT 0 COMMENT '是否已发送告警：0-未发送 1-已发送',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁反潜回违规记录表';

-- 创建违规记录表索引
CREATE INDEX idx_violation_user_time ON t_anti_passback_violation(user_id, violation_time);
CREATE INDEX idx_violation_device_time ON t_anti_passback_violation(device_id, violation_time);
CREATE INDEX idx_violation_area_time ON t_anti_passback_violation(area_id, violation_time);
CREATE INDEX idx_violation_type_time ON t_anti_passback_violation(violation_type, violation_time);

-- 创建反潜回策略配置表
CREATE TABLE IF NOT EXISTS t_anti_passback_policy (
    policy_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '策略ID',
    policy_name VARCHAR(100) NOT NULL COMMENT '策略名称',
    policy_type VARCHAR(20) NOT NULL COMMENT '策略类型：HARD/SOFT/AREA/GLOBAL',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型：DEVICE/AREA/GLOBAL',
    target_id BIGINT COMMENT '目标ID（设备或区域ID）',
    time_window_minutes INT NOT NULL DEFAULT 5 COMMENT '时间窗口（分钟）',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
    priority INT DEFAULT 1 COMMENT '优先级：1-10，数字越大优先级越高',
    configuration TEXT COMMENT '策略配置(JSON)',
    description VARCHAR(500) COMMENT '策略描述',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁反潜回策略配置表';

-- 创建策略表索引
CREATE UNIQUE INDEX idx_policy_unique ON t_anti_passback_policy(target_type, target_id, deleted_flag);
CREATE INDEX idx_policy_type_enabled ON t_anti_passback_policy(policy_type, is_enabled);
CREATE INDEX idx_policy_priority ON t_anti_passback_policy(priority DESC);

-- 插入默认策略配置
INSERT INTO t_anti_passback_policy (policy_name, policy_type, target_type, configuration, description) VALUES
('默认硬反潜回策略', 'HARD', 'GLOBAL', '{"timeWindowMinutes": 5, "strictMode": true}', '高风险区域硬反潜回策略'),
('默认软反潜回策略', 'SOFT', 'GLOBAL', '{"timeWindowMinutes": 10, "recordExceptions": true}', '普通区域软反潜回策略'),
('默认区域反潜回策略', 'AREA', 'GLOBAL', '{"timeWindowMinutes": 15, "checkEntryExit": true}', '办公楼区域反潜回策略'),
('默认全局反潜回策略', 'GLOBAL', 'GLOBAL', '{"timeWindowMinutes": 20, "crossAreaCheck": true}', '重要区域全局反潜回策略');