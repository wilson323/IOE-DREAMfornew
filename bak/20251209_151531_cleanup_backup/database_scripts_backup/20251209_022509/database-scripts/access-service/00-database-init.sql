-- ============================================================
-- IOE-DREAM Access Service - 数据库初始化脚本
-- 数据库名: ioedream_access_db
-- 功能: 创建数据库和初始化配置
-- 创建时间: 2025-12-08
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ioedream_access_db` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `ioedream_access_db`;

-- 设置时区
SET time_zone = '+8:00';

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 设置SQL模式
SET SQL_MODE='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 初始化完成提示
SELECT '✅ IOE-DREAM Access Service 数据库初始化完成！' AS message;
