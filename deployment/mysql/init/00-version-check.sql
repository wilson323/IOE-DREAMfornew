-- =====================================================
-- IOE-DREAM 数据库版本检查脚本
-- 版本: V1.0.0
-- 描述: 检查当前数据库版本，为增量更新做准备
-- 执行顺序: 在所有初始化脚本之前执行
-- 数据库名: ioedream
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS ioedream
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE ioedream;

-- =====================================================
-- 1. 创建迁移历史表（如果不存在）
-- =====================================================

CREATE TABLE IF NOT EXISTS t_migration_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    version VARCHAR(50) NOT NULL COMMENT '版本号',
    description TEXT COMMENT '版本描述',
    script_name VARCHAR(200) COMMENT '脚本文件名',
    status VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '执行状态',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_version (version),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库迁移历史表';

-- =====================================================
-- 2. 创建版本检查函数（用于获取当前版本）
-- =====================================================

DELIMITER $$

DROP FUNCTION IF EXISTS get_current_version$$
CREATE FUNCTION get_current_version() RETURNS VARCHAR(50)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE current_version VARCHAR(50) DEFAULT 'V0.0.0';

    SELECT version INTO current_version
    FROM t_migration_history
    WHERE status = 'SUCCESS'
    ORDER BY create_time DESC
    LIMIT 1;

    IF current_version IS NULL THEN
        SET current_version = 'V0.0.0';
    END IF;

    RETURN current_version;
END$$

DELIMITER ;

-- =====================================================
-- 3. 创建版本检查存储过程（用于检查版本是否需要执行）
-- =====================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS check_version_executed$$
CREATE PROCEDURE check_version_executed(
    IN p_version VARCHAR(50),
    OUT p_executed BOOLEAN
)
BEGIN
    DECLARE v_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_count
    FROM t_migration_history
    WHERE version = p_version AND status = 'SUCCESS';

    SET p_executed = (v_count > 0);
END$$

DELIMITER ;

-- =====================================================
-- 4. 输出当前版本信息
-- =====================================================

SELECT
    get_current_version() AS current_version,
    (SELECT COUNT(*) FROM t_migration_history WHERE status = 'SUCCESS') AS executed_migrations,
    (SELECT MAX(create_time) FROM t_migration_history WHERE status = 'SUCCESS') AS last_migration_time;

-- =====================================================
-- 脚本结束
-- =====================================================

