-- ============================================================
-- IOE-DREAM Common Service - 数据库初始化脚本
-- 数据库名: ioedream_common_db
-- 功能: 创建数据库和初始化配置
-- 创建时间: 2025-12-02
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ioedream_common_db` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `ioedream_common_db`;

-- 设置时区
SET time_zone = '+8:00';

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- 执行顺序说明
-- ============================================================
-- 1. 00-database-init.sql (本文件)
-- 2. 01-t_user_session.sql (Auth模块)
-- 3. 02-t_user.sql (Identity模块)
-- 4. 03-t_role.sql (Identity模块)
-- 5. 04-t_permission.sql (Identity模块)
-- 6. 05-t_user_role.sql (Identity模块)
-- 7. 06-t_role_permission.sql (Identity模块)
-- 8. 07-t_notification_message.sql (Notification模块)
-- 9. 08-t_notification_template.sql (Notification模块)
-- 10. 09-t_notification_config.sql (Notification模块)
-- 11. 10-t_audit_log.sql (Audit模块)
-- 12. 11-t_alert.sql (Monitor模块)
-- 13. 12-t_alert_rule.sql (Monitor模块)
-- 14. 13-t_system_monitor.sql (Monitor模块)
-- 15. 14-t_scheduled_job.sql (Scheduler模块)
-- 16. 15-t_job_execution_log.sql (Scheduler模块)
-- 17. 16-t_system_config.sql (System模块)
-- 18. 17-t_system_dict.sql (System模块)
-- 19. 18-t_employee.sql (System模块)

-- ============================================================
-- 初始化完成提示
-- ============================================================
SELECT '✅ IOE-DREAM Common Service 数据库初始化完成！' AS message;
SELECT '📊 请按顺序执行01-18号SQL脚本创建表' AS next_step;

