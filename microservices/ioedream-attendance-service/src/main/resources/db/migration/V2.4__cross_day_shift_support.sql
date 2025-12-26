-- =============================================
-- IOE-DREAM 考勤跨天班次支持 - 数据库迁移脚本
-- =============================================
-- 功能：为 t_attendance_work_shift 表添加跨天班次支持字段
-- 版本：1.0.0
-- 日期：2025-01-30
-- =============================================

USE `ioedream_attendance`;

-- 1. 添加跨天标识字段
ALTER TABLE `t_attendance_work_shift`
ADD COLUMN `is_cross_day` TINYINT(1) DEFAULT 0 COMMENT '是否跨天班次：0-否 1-是' AFTER `sort_order`;

-- 2. 添加跨天归属规则字段
ALTER TABLE `t_attendance_work_shift`
ADD COLUMN `cross_day_rule` VARCHAR(20) DEFAULT 'START_DATE' COMMENT '跨天归属规则：START_DATE-以开始日期为准 END_DATE-以结束日期为准 SPLIT-分别归属' AFTER `is_cross_day`;

-- 3. 为现有数据自动判断并设置跨天标识
UPDATE `t_attendance_work_shift`
SET `is_cross_day` = CASE
    WHEN `work_end_time` < `work_start_time` THEN 1
    ELSE 0
END,
`cross_day_rule` = 'START_DATE';

-- 4. 添加索引以优化查询性能
ALTER TABLE `t_attendance_work_shift`
ADD INDEX `idx_is_cross_day` (`is_cross_day`);

-- 5. 添加注释
ALTER TABLE `t_attendance_work_shift`
MODIFY COLUMN `is_cross_day` TINYINT(1) DEFAULT 0 COMMENT '是否跨天班次：0-否 1-是（如夜班22:00-06:00）';

-- =============================================
-- 迁移验证SQL
-- =============================================

-- 验证跨天班次数据
SELECT
    shift_id,
    shift_code,
    shift_name,
    work_start_time,
    work_end_time,
    is_cross_day,
    cross_day_rule
FROM t_attendance_work_shift
WHERE is_cross_day = 1;

-- 预期结果：应该看到所有 work_end_time < work_start_time 的班次被标记为跨天班次
