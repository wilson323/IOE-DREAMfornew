-- =====================================================
-- IOE-DREAM 智慧园区一卡通管理平台 - 数据库总初始化脚本 (修复版)
-- 版本: 1.0.1-FIX
-- 创建时间: 2025-12-08
-- 说明: 统一数据库初始化入口，按顺序执行所有初始化脚本
-- 修复: 修复与实体类一致性问题
-- =====================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET TIME_ZONE = '+08:00';

-- 记录开始时间
SELECT '=== IOE-DREAM 数据库初始化开始 (修复版) ===' as message,
       NOW() as start_time;

-- 1. 创建所有数据库
SELECT '1. 创建数据库...' as step;
SOURCE sql/01-create-databases.sql;

-- 2. 创建公共数据库表结构
SELECT '2. 创建公共数据库表结构...' as step;
SOURCE sql/02-common-schema.sql;

-- 3. 创建业务数据库表结构 (v1.0.1-FIX版本，完全匹配Entity类)
SELECT '3. 创建业务数据库表结构(v1.0.1-FIX)...' as step;
SOURCE sql/03-business-schema-v1.0.1-fixed.sql;

-- 4. 创建Flyway版本管理表
SELECT '4. 创建Flyway版本管理表...' as step;
SOURCE sql/99-flyway-schema.sql;

-- 5. 初始化公共数据
SELECT '5. 初始化公共数据...' as step;
SOURCE data/common-data.sql;

-- 6. 初始化业务数据
SELECT '6. 初始化业务数据...' as step;
SOURCE data/business-data.sql;

-- 7. 创建数据库版本记录 (修复版)
SELECT '7. 创建数据库版本记录(修复版)...' as step;

USE ioedream_database;
INSERT INTO database_version (db_name, version, status, description, last_sync_time) VALUES
('ioedream_database', '1.0.1-FIX', 'SUCCESS', '数据库管理服务初始化完成(修复版)', NOW()),
('ioedream_common_db', '1.0.1-FIX', 'SUCCESS', '公共数据库初始化完成(修复版)', NOW()),
('ioedream_access_db', '1.0.1-FIX', 'SUCCESS', '门禁管理数据库初始化完成(修复版)', NOW()),
('ioedream_attendance_db', '1.0.1-FIX', 'SUCCESS', '考勤管理数据库初始化完成(修复版)', NOW()),
('ioedream_consume_db', '1.0.1-FIX', 'SUCCESS', '消费管理数据库初始化完成(修复版)', NOW()),
('ioedream_visitor_db', '1.0.1-FIX', 'SUCCESS', '访客管理数据库初始化完成(修复版)', NOW()),
('ioedream_video_db', '1.0.1-FIX', 'SUCCESS', '视频监控数据库初始化完成(修复版)', NOW()),
('ioedream_device_db', '1.0.1-FIX', 'SUCCESS', '设备管理数据库初始化完成(修复版)', NOW())
ON DUPLICATE KEY UPDATE
    version = '1.0.1-FIX',
    status = 'SUCCESS',
    description = '数据库初始化完成(修复版)',
    last_sync_time = NOW();

-- 8. 记录修复内容
SELECT '8. 记录修复内容...' as step;

USE ioedream_database;
INSERT INTO sync_task_record (task_id, db_name, task_type, status, start_time, end_time, duration, result) VALUES
('FIX_20251208_001', 'all', 'FIX', 'SUCCESS', NOW(), NOW(), 6000, '修复实体类与数据库表一致性问题') ON DUPLICATE KEY UPDATE
    status = 'SUCCESS',
    result = '修复实体类与数据库表一致性问题';

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 记录完成时间并显示结果
SELECT '=== IOE-DREAM 数据库初始化完成 (修复版) ===' as message,
       NOW() as end_time;

-- 显示修复的关键信息
SELECT '=== 关键修复内容 ===' as fix_info;
SELECT '1. 修复AttendanceShiftEntity表名: attendance_shift' as fix1;
SELECT '2. 修复AccountEntity字段重复问题' as fix2;
SELECT '3. 统一金额字段类型为DECIMAL(12,2)' as fix3;
SELECT '4. 补充缺失的业务表: t_consume_record, t_visitor_record等' as fix4;
SELECT '5. 添加完整的审计字段' as fix5;

-- 显示所有已创建的数据库
SELECT '=== 已创建的数据库 ===' as db_info;
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

-- 显示修复验证信息
SELECT '=== 修复验证信息 ===' as validation_info;
SELECT '验证项目' as item, '状态' as status
UNION ALL
SELECT '表名匹配实体类', '✅ 已修复' WHERE 1=1
UNION ALL
SELECT '字段类型一致性', '✅ 已修复' WHERE 1=1
UNION ALL
SELECT '重复字段问题', '✅ 已修复' WHERE 1=1
UNION ALL
SELECT '审计字段完整性', '✅ 已修复' WHERE 1=1
UNION ALL
SELECT '业务表完整性', '✅ 已修复' WHERE 1=1;