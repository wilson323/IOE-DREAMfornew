-- =====================================================
-- IOE-DREAM 智慧园区一卡通管理平台 - 数据库总初始化脚本
-- 版本: 1.0.0
-- 创建时间: 2025-12-08
-- 说明: 统一数据库初始化入口，按顺序执行所有初始化脚本
-- =====================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET TIME_ZONE = '+08:00';

-- 记录开始时间
SELECT '=== IOE-DREAM 数据库初始化开始 ===' as message,
       NOW() as start_time;

-- 1. 创建所有数据库
SELECT '1. 创建数据库...' as step;
SOURCE sql/01-create-databases.sql;

-- 2. 创建公共数据库表结构
SELECT '2. 创建公共数据库表结构...' as step;
SOURCE sql/02-common-schema.sql;

-- 3. 创建业务数据库表结构
SELECT '3. 创建业务数据库表结构...' as step;
SOURCE sql/03-business-schema.sql;

-- 4. 创建Flyway版本管理表
SELECT '4. 创建Flyway版本管理表...' as step;
SOURCE sql/99-flyway-schema.sql;

-- 5. 初始化公共数据
SELECT '5. 初始化公共数据...' as step;
SOURCE data/common-data.sql;

-- 6. 初始化业务数据
SELECT '6. 初始化业务数据...' as step;
SOURCE data/business-data.sql;

-- 7. 创建数据库版本记录
SELECT '7. 创建数据库版本记录...' as step;

USE ioedream_database;
INSERT INTO database_version (db_name, version, status, description, last_sync_time) VALUES
('ioedream_database', '1.0.0', 'SUCCESS', '数据库管理服务初始化完成', NOW()),
('ioedream_common_db', '1.0.0', 'SUCCESS', '公共数据库初始化完成', NOW()),
('ioedream_access_db', '1.0.0', 'SUCCESS', '门禁管理数据库初始化完成', NOW()),
('ioedream_attendance_db', '1.0.0', 'SUCCESS', '考勤管理数据库初始化完成', NOW()),
('ioedream_consume_db', '1.0.0', 'SUCCESS', '消费管理数据库初始化完成', NOW()),
('ioedream_visitor_db', '1.0.0', 'SUCCESS', '访客管理数据库初始化完成', NOW()),
('ioedream_video_db', '1.0.0', 'SUCCESS', '视频监控数据库初始化完成', NOW()),
('ioedream_device_db', '1.0.0', 'SUCCESS', '设备管理数据库初始化完成', NOW())
ON DUPLICATE KEY UPDATE
    version = '1.0.0',
    status = 'SUCCESS',
    description = '数据库初始化完成',
    last_sync_time = NOW();

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 记录完成时间并显示结果
SELECT '=== IOE-DREAM 数据库初始化完成 ===' as message,
       NOW() as end_time;

-- 显示所有已创建的数据库
SELECT '已创建的数据库:' as info;
SHOW DATABASES LIKE 'ioedream_%';

-- 显示数据库统计信息
SELECT
    COUNT(*) as total_databases,
    GROUP_CONCAT(db_name ORDER BY db_name) as database_list
FROM (
    SELECT SCHEMA_NAME as db_name
    FROM INFORMATION_SCHEMA.SCHEMATA
    WHERE SCHEMA_NAME LIKE 'ioedream_%'
) as db_list;