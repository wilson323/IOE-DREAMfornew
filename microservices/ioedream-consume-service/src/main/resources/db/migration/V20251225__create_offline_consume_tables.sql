-- =====================================================
-- 离线消费同步机制 - 数据库表创建
-- 版本: V20251225
-- 作者: IOE-DREAM Team
-- 描述: 支持移动端离线消费记录存储和同步
-- =====================================================

-- 1. 离线消费记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_offline_consume_record (
    id VARCHAR(64) PRIMARY KEY COMMENT '记录ID（UUID）',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT NOT NULL COMMENT '消费设备ID',
    device_code VARCHAR(50) COMMENT '消费设备编码',
    amount DECIMAL(10, 2) NOT NULL COMMENT '消费金额',
    consume_type TINYINT DEFAULT 1 COMMENT '消费类型 1-普通消费 2-补贴消费 3-离线白名单消费',
    consume_time DATETIME NOT NULL COMMENT '消费时间',

    -- 同步状态管理
    sync_status TINYINT DEFAULT 0 COMMENT '同步状态 0-待同步 1-同步中 2-已同步 3-冲突',
    sync_time DATETIME COMMENT '同步时间',
    sync_error_message VARCHAR(500) COMMENT '同步错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',

    -- 冲突解决
    conflict_type TINYINT COMMENT '冲突类型 1-时间戳冲突 2-余额冲突 3-重复交易',
    conflict_reason VARCHAR(255) COMMENT '冲突原因',
    resolved TINYINT DEFAULT 0 COMMENT '是否已解决 0-未解决 1-已自动解决 2-已人工处理',
    resolved_time DATETIME COMMENT '解决时间',
    resolved_remark VARCHAR(500) COMMENT '解决备注',

    -- 数据完整性
    signature VARCHAR(128) COMMENT '数字签名（防篡改）',
    check_sum VARCHAR(64) COMMENT '校验和',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引优化
    INDEX idx_user_sync (user_id, sync_status) COMMENT '用户同步状态查询',
    INDEX idx_device_time (device_id, consume_time) COMMENT '设备时间查询',
    INDEX idx_sync_status_created (sync_status, created_at) COMMENT '同步任务查询',
    INDEX idx_conflict_unresolved (conflict_type, resolved) COMMENT '未解决冲突查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线消费记录表';

-- 2. 离线同步日志表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_offline_sync_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    sync_batch_no VARCHAR(64) NOT NULL COMMENT '同步批次号',
    user_id BIGINT COMMENT '用户ID（null表示批量同步）',
    sync_type TINYINT DEFAULT 1 COMMENT '同步类型 1-自动同步 2-手动同步 3-批量同步',

    -- 同步统计
    total_count INT DEFAULT 0 COMMENT '总记录数',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    conflict_count INT DEFAULT 0 COMMENT '冲突数量',

    -- 同步结果
    sync_status TINYINT DEFAULT 0 COMMENT '同步状态 0-进行中 1-成功 2-部分失败 3-全部失败',
    error_summary TEXT COMMENT '错误汇总',

    -- 性能指标
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT COMMENT '耗时（毫秒）',

    -- 审计信息
    created_by VARCHAR(100) COMMENT '创建人',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_batch_no (sync_batch_no) COMMENT '批次号查询',
    INDEX idx_user_created (user_id, created_at) COMMENT '用户同步历史',
    INDEX idx_status_time (sync_status, start_time) COMMENT '同步状态查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线同步日志表';

-- 3. 离线消费白名单表（支持离线白名单模式）
-- =====================================================
CREATE TABLE IF NOT EXISTS t_offline_whitelist (
    whitelist_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '白名单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT COMMENT '设备ID（null表示全部设备）',
    area_id BIGINT COMMENT '区域ID（null表示全部区域）',

    -- 白名单配置
    whitelist_type TINYINT DEFAULT 1 COMMENT '白名单类型 1-用户白名单 2-设备白名单 3-区域白名单',
    max_single_amount DECIMAL(10, 2) DEFAULT 100.00 COMMENT '单笔最大金额',
    max_daily_amount DECIMAL(10, 2) DEFAULT 500.00 COMMENT '每日最大金额',
    max_daily_count INT DEFAULT 10 COMMENT '每日最大次数',

    -- 有效期
    effective_date DATETIME NOT NULL COMMENT '生效日期',
    expire_date DATETIME COMMENT '失效日期（null表示永久有效）',

    -- 状态管理
    enabled TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注',

    -- 审计信息
    created_by BIGINT COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_user_device (user_id, device_id) COMMENT '用户设备白名单',
    INDEX idx_user_area (user_id, area_id) COMMENT '用户区域白名单',
    INDEX idx_enabled_effective (enabled, effective_date, expire_date) COMMENT '有效白名单查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线消费白名单表';

-- 4. 离线消费配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS t_offline_consume_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(50) DEFAULT 'STRING' COMMENT '配置类型 STRING/NUMBER/BOOLEAN/JSON',
    description VARCHAR(500) COMMENT '配置描述',

    -- 分组管理
    group_name VARCHAR(100) DEFAULT 'DEFAULT' COMMENT '配置分组',

    -- 状态管理
    enabled TINYINT DEFAULT 1 COMMENT '启用状态 0-禁用 1-启用',

    -- 审计信息
    created_by VARCHAR(100) COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(100) COMMENT '更新人',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_config_key (config_key) COMMENT '配置键唯一索引',
    INDEX idx_group_enabled (group_name, enabled) COMMENT '配置分组查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='离线消费配置表';

-- 5. 初始化默认配置
-- =====================================================
INSERT INTO t_offline_consume_config (config_key, config_value, config_type, description, group_name) VALUES
-- 离线消费开关
('offline.consume.enabled', 'true', 'BOOLEAN', '是否启用离线消费功能', 'FEATURE'),
('offline.consume.whitelist.enabled', 'true', 'BOOLEAN', '是否启用白名单模式', 'FEATURE'),
('offline.consume.auto.sync.enabled', 'true', 'BOOLEAN', '是否启用自动同步', 'FEATURE'),

-- 同步配置
('offline.consume.sync.batch.size', '100', 'NUMBER', '批量同步大小', 'SYNC'),
('offline.consume.sync.interval.seconds', '60', 'NUMBER', '自动同步间隔（秒）', 'SYNC'),
('offline.consume.sync.retry.max', '3', 'NUMBER', '最大重试次数', 'SYNC'),
('offline.consume.sync.timeout.seconds', '30', 'NUMBER', '同步超时时间（秒）', 'SYNC'),

-- 冲突解决策略
('offline.consume.conflict.strategy', 'LAST_WRITE_WINS', 'STRING', '冲突解决策略 LAST_WRITE_WINS/MANUAL_REVIEW', 'CONFLICT'),
('offline.consume.conflict.auto.resolve', 'true', 'BOOLEAN', '是否自动解决简单冲突', 'CONFLICT'),

-- 安全配置
('offline.consume.signature.enabled', 'true', 'BOOLEAN', '是否启用数字签名', 'SECURITY'),
('offline.consume.signature.algorithm', 'HmacSHA256', 'STRING', '签名算法', 'SECURITY'),
('offline.consume.validate.balance', 'true', 'BOOLEAN', '是否验证余额', 'SECURITY'),
('offline.consume.validate.signature', 'true', 'BOOLEAN', '是否验证签名', 'SECURITY'),

-- 性能配置
('offline.consume.cache.enabled', 'true', 'BOOLEAN', '是否启用缓存', 'PERFORMANCE'),
('offline.consume.cache.ttl.seconds', '300', 'NUMBER', '缓存过期时间（秒）', 'PERFORMANCE'),
('offline.consume.async.processing.enabled', 'true', 'BOOLEAN', '是否异步处理', 'PERFORMANCE')

ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    updated_time = CURRENT_TIMESTAMP;

-- =====================================================
-- 数据库升级完成
-- =====================================================
