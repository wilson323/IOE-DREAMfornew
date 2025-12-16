-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.1.1
-- 描述: 表命名规范统一（t_common_area -> t_area）
-- 策略: 方案1 - 直接重命名表
-- 说明: 仅在 t_common_area 存在且 t_area 不存在时执行
-- =====================================================

-- 仅在老表存在且新表不存在时执行重命名
SET @has_old := (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 't_common_area'
);

SET @has_new := (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 't_area'
);

SET @sql := IF(@has_old > 0 AND @has_new = 0,
    'RENAME TABLE t_common_area TO t_area',
    'SELECT ''SKIP: t_common_area not exists OR t_area already exists'' AS status'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
