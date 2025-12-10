-- =====================================================
-- IOE-DREAM 数据库初始数据脚本 - 生产环境
-- 版本: V1.1.0-PROD
-- 描述: 生产环境初始数据，最小化数据，不包含测试数据
-- 执行条件: ENVIRONMENT=prod
-- 数据库名: ioedream
-- 幂等性: 使用INSERT IGNORE确保可重复执行
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- 生产环境：最小化数据，不包含测试数据
-- =====================================================

-- 注意：生产环境只包含基础数据，不包含测试用户
-- 所有测试数据已在 02-ioedream-data.sql 中排除

-- 生产环境只记录迁移历史，不添加额外数据
INSERT INTO t_migration_history (
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
) VALUES (
    'V1.1.0-PROD',
    '生产环境初始数据 - 最小化数据，不包含测试数据',
    '02-ioedream-data-prod.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    script_name = VALUES(script_name),
    status = 'SUCCESS',
    end_time = NOW();

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'V1.1.0-PROD 生产环境数据脚本执行完成！' AS migration_status,
       '生产环境使用最小化数据，仅包含基础配置' AS migration_summary,
       NOW() AS completed_time;

