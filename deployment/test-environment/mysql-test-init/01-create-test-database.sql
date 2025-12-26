-- ============================================================
-- IOE-DREAM 测试数据库初始化脚本
-- 步骤1: 创建测试数据库
-- ============================================================

-- 创建测试数据库
CREATE DATABASE IF NOT EXISTS ioedream_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 使用测试数据库
USE ioedream_test;

-- 设置时区
SET time_zone = '+8:00';

-- 显示创建结果
SELECT 'Database ioedream_test created successfully' AS status;
