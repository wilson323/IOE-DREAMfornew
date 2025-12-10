-- ============================================================
-- IOE-DREAM 考勤服务 - 数据库初始化脚本
-- 数据库名: ioedream_attendance_db
-- 功能: 创建数据库和初始化配置
-- 创建时间: 2025-12-08
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ioedream_attendance_db` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `ioedream_attendance_db`;

-- 设置时区
SET time_zone = '+8:00';

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 设置SQL模式
SET SQL_MODE='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ============================================================
-- 执行顺序说明
-- ============================================================
-- 1. 00-database-init.sql (本文件)
-- 2. 01-t_attendance_record.sql (考勤记录表)
-- 3. 02-t_attendance_shift.sql (班次配置表)
-- 4. 03-t_attendance_schedule.sql (排班计划表)
-- 5. 04-t_attendance_rule.sql (考勤规则表)
-- 6. 05-t_attendance_exception.sql (异常申请表)
-- 7. 06-t_attendance_statistics.sql (考勤统计表)
-- 8. 07-t_attendance_leave_type.sql (请假类型表)
-- 9. 08-t_attendance_leave_balance.sql (年假余额表)
-- 10. 09-t_attendance_overtime_rule.sql (加班规则表)
-- 11. 10-t_attendance_holiday.sql (节假日配置表)
-- 12. 11-t_attendance_shift_group.sql (班次组配置表)
-- 13. 12-t_attendance_rotation_rule.sql (轮班规则表)
-- 14. 13-t_attendance_makeup_card.sql (补卡记录表)
-- 15. 14-t_attendance_device.sql (考勤设备表)

-- ============================================================
-- 初始化完成提示
-- ============================================================
SELECT '✅ IOE-DREAM 考勤服务 数据库初始化完成！' AS message;
SELECT '📊 请按顺序执行01-14号SQL脚本创建表' AS next_step;