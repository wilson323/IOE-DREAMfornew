-- ============================================================
-- 智能排班计划表结构升级脚本
-- 版本: v1.1.0
-- 日期: 2025-01-30
-- 说明: 添加converged和errorMessage字段以支持完整的优化执行状态追踪
-- ============================================================

-- ============================================================
-- 1. 为t_smart_schedule_plan表添加新字段
-- ============================================================

ALTER TABLE `t_smart_schedule_plan`
ADD COLUMN `converged` TINYINT(1) DEFAULT 0 COMMENT '是否收敛（算法是否找到稳定解）: 0-未收敛 1-已收敛' AFTER `unsatisfied_constraint_count`,
ADD COLUMN `error_message` VARCHAR(500) DEFAULT NULL COMMENT '执行错误信息（执行失败时记录）' AFTER `converged`;

-- ============================================================
-- 2. 为新字段添加索引（提升查询性能）
-- ============================================================

-- 按收敛状态索引（用于筛选已收敛的排班计划）
CREATE INDEX `idx_converged` ON `t_smart_schedule_plan`(`converged`);

-- 按执行状态和收敛状态联合索引（用于统计查询）
CREATE INDEX `idx_status_converged` ON `t_smart_schedule_plan`(`execution_status`, `converged`);

-- ============================================================
-- 3. 数据迁移说明
-- ============================================================

-- 3.1 初始化现有数据
-- 对于已完成的计划（execution_status=2），默认设置为已收敛
UPDATE `t_smart_schedule_plan`
SET `converged` = 1
WHERE `execution_status` = 2 AND `converged` IS NULL;

-- 对于执行中的计划（execution_status=1），默认设置为未收敛
UPDATE `t_smart_schedule_plan`
SET `converged` = 0
WHERE `execution_status` = 1 AND `converged` IS NULL;

-- 对于失败的计划（execution_status=3），保持error_message中的错误信息
-- （此步骤由应用程序自动处理）

-- ============================================================
-- 4. 字段注释说明
-- ============================================================

-- converged字段:
--   0 - 未收敛（算法未找到稳定解或未达到收敛条件）
--   1 - 已收敛（算法成功找到稳定解）
--   NULL - 待执行或执行中（默认值）

-- error_message字段:
--   记录优化执行失败的详细错误信息
--   仅在execution_status=3（执行失败）时有值
--   其他状态为NULL

-- ============================================================
-- 5. 验证脚本
-- ============================================================

-- 验证字段是否添加成功
SELECT
    COUNT(*) AS total_plans,
    SUM(CASE WHEN converged IS NOT NULL THEN 1 ELSE 0 END) AS has_converged,
    SUM(CASE WHEN error_message IS NOT NULL THEN 1 ELSE 0 END) AS has_error_message
FROM `t_smart_schedule_plan`;

-- 验证数据迁移结果
SELECT
    execution_status,
    converged,
    COUNT(*) AS count
FROM `t_smart_schedule_plan`
GROUP BY execution_status, converged;

-- ============================================================
-- 6. 回滚脚本（仅在需要时使用）
-- ============================================================

-- DROP INDEX `idx_status_converged` ON `t_smart_schedule_plan`;
-- DROP INDEX `idx_converged` ON `t_smart_schedule_plan`;
-- ALTER TABLE `t_smart_schedule_plan` DROP COLUMN `error_message`;
-- ALTER TABLE `t_smart_schedule_plan` DROP COLUMN `converged`;

-- ============================================================
-- 脚本结束
-- ============================================================
