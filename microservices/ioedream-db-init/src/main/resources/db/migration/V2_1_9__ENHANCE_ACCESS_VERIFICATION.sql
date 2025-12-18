-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.1.9
-- 描述: 门禁验证架构优化 - 添加验证模式字段和反潜记录表
-- 创建日期: 2025-01-30
-- =====================================================

-- =====================================================
-- 1. 优化 t_access_area_ext 表 - 添加验证模式字段
-- =====================================================

-- 检查字段是否存在，如果不存在则添加
SET @dbname = DATABASE();
SET @tablename = 't_access_area_ext';
SET @columnname = 'verification_mode';
SET @preparedStatement = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(20) DEFAULT ''edge'' COMMENT ''验证方式: edge=设备端验证, backend=后台验证, hybrid=混合验证''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 如果字段已存在，则修改字段定义
ALTER TABLE t_access_area_ext
MODIFY COLUMN verification_mode VARCHAR(20) DEFAULT 'edge'
COMMENT '验证方式: edge=设备端验证, backend=后台验证, hybrid=混合验证';

-- 添加索引以优化查询性能
CREATE INDEX IF NOT EXISTS idx_verification_mode ON t_access_area_ext(verification_mode);

-- 添加 access_mode 字段（如果不存在）
SET @columnname2 = 'access_mode';
SET @preparedStatement2 = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname2)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname2, ' VARCHAR(50) COMMENT ''门禁模式''')
));
PREPARE alterIfNotExists2 FROM @preparedStatement2;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

-- 添加 device_count 字段（如果不存在）
SET @columnname3 = 'device_count';
SET @preparedStatement3 = (SELECT IF(
    (
        SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE
            (TABLE_SCHEMA = @dbname)
            AND (TABLE_NAME = @tablename)
            AND (COLUMN_NAME = @columnname3)
    ) > 0,
    'SELECT 1',
    CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname3, ' INT DEFAULT 0 COMMENT ''设备数量''')
));
PREPARE alterIfNotExists3 FROM @preparedStatement3;
EXECUTE alterIfNotExists3;
DEALLOCATE PREPARE alterIfNotExists3;

-- =====================================================
-- 2. 创建反潜记录表
-- =====================================================

CREATE TABLE IF NOT EXISTS t_access_anti_passback_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    area_id BIGINT COMMENT '区域ID',
    in_out_status TINYINT NOT NULL COMMENT '进出状态: 1=进, 2=出',
    record_time DATETIME NOT NULL COMMENT '记录时间',
    access_type VARCHAR(20) COMMENT '通行类型: IN=进入, OUT=离开',
    verify_type INT COMMENT '验证方式: 0=密码, 1=指纹, 2=卡, 11=面部等',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',

    -- 索引优化
    INDEX idx_user_device (user_id, device_id, record_time),
    INDEX idx_user_area (user_id, area_id, record_time),
    INDEX idx_device_time (device_id, record_time),
    INDEX idx_record_time (record_time),
    INDEX idx_in_out_status (in_out_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反潜记录表';

-- =====================================================
-- 3. 创建互锁记录表（可选，用于互锁验证）
-- =====================================================

CREATE TABLE IF NOT EXISTS t_access_interlock_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    interlock_group_id BIGINT NOT NULL COMMENT '互锁组ID',
    lock_status TINYINT NOT NULL DEFAULT 0 COMMENT '锁定状态: 0=未锁定, 1=已锁定',
    lock_time DATETIME COMMENT '锁定时间',
    unlock_time DATETIME COMMENT '解锁时间',
    lock_duration INT COMMENT '锁定时长(秒)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',

    -- 索引优化
    INDEX idx_device_group (device_id, interlock_group_id),
    INDEX idx_lock_status (lock_status, lock_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互锁记录表';

-- =====================================================
-- 4. 创建多人验证记录表（可选，用于多人验证）
-- =====================================================

CREATE TABLE IF NOT EXISTS t_access_multi_person_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    verification_session_id VARCHAR(100) NOT NULL COMMENT '验证会话ID',
    area_id BIGINT NOT NULL COMMENT '区域ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    required_count INT NOT NULL DEFAULT 2 COMMENT '需要验证的人数',
    current_count INT NOT NULL DEFAULT 0 COMMENT '当前已验证人数',
    user_ids TEXT COMMENT '已验证用户ID列表(JSON格式)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=等待中, 1=已完成, 2=已超时',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    complete_time DATETIME COMMENT '完成时间',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',

    -- 索引优化
    UNIQUE INDEX uk_session_id (verification_session_id, deleted_flag),
    INDEX idx_area_device (area_id, device_id, status),
    INDEX idx_status_time (status, start_time),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多人验证记录表';

-- =====================================================
-- 5. 数据迁移：为现有记录设置默认验证模式
-- =====================================================

-- 为现有记录设置默认值（如果verification_mode为NULL）
UPDATE t_access_area_ext
SET verification_mode = 'edge'
WHERE verification_mode IS NULL OR verification_mode = '';

-- =====================================================
-- 完成标记
-- =====================================================

SELECT 'V2.1.9 门禁验证架构优化完成' AS status;
