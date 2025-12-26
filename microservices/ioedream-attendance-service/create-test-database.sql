-- ============================================================
-- IOE-DREAM Attendance Service 测试数据库初始化脚本
--
-- 用途: 创建集成测试所需的测试数据库
-- 数据库名: ioedream_test
-- 字符集: utf8mb4 (支持Emoji和特殊字符)
-- ============================================================

-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS ioedream_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 使用测试数据库
USE ioedream_test;

-- 显示创建结果
SELECT 'Database ioedream_test created successfully!' AS Status;

-- ============================================================
-- 使用说明:
--
-- 1. MySQL命令行执行:
--    mysql -u root -p < create-test-database.sql
--
-- 2. MySQL客户端直接执行:
--    SOURCE D:\IOE-DREAM\microservices\ioedream-attendance-service\create-test-database.sql;
--
-- 3. 验证数据库创建:
--    SHOW DATABASES LIKE 'ioedream_test';
-- ============================================================
