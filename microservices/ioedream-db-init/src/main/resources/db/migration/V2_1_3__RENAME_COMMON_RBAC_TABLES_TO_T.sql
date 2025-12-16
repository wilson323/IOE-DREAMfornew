-- =====================================================
-- IOE-DREAM Flyway 迁移脚本
-- 版本: V2.1.3
-- 描述: RBAC/用户权限表命名体系统一（t_common_* -> t_*）
-- 策略: 方案B - 以代码实体@TableName为准
-- 目标表:
--   t_user / t_role / t_user_role / t_rbac_resource / t_role_resource / t_role_menu
-- 说明:
-- 1) 仅在旧表存在且新表不存在时执行 RENAME TABLE
-- 2) 字段差异在后续版本用 ALTER TABLE 对齐
-- =====================================================

SET @db := DATABASE();

-- t_common_user -> t_user
SET @has_old := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_common_user');
SET @has_new := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_user');
SET @sql := IF(@has_old>0 AND @has_new=0, 'RENAME TABLE t_common_user TO t_user', 'SELECT ''SKIP t_common_user->t_user'' AS status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- t_common_role -> t_role
SET @has_old := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_common_role');
SET @has_new := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_role');
SET @sql := IF(@has_old>0 AND @has_new=0, 'RENAME TABLE t_common_role TO t_role', 'SELECT ''SKIP t_common_role->t_role'' AS status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- t_common_user_role -> t_user_role
SET @has_old := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_common_user_role');
SET @has_new := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_user_role');
SET @sql := IF(@has_old>0 AND @has_new=0, 'RENAME TABLE t_common_user_role TO t_user_role', 'SELECT ''SKIP t_common_user_role->t_user_role'' AS status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- t_common_permission -> t_rbac_resource
SET @has_old := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_common_permission');
SET @has_new := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_rbac_resource');
SET @sql := IF(@has_old>0 AND @has_new=0, 'RENAME TABLE t_common_permission TO t_rbac_resource', 'SELECT ''SKIP t_common_permission->t_rbac_resource'' AS status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- t_common_role_permission -> t_role_resource
SET @has_old := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_common_role_permission');
SET @has_new := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_role_resource');
SET @sql := IF(@has_old>0 AND @has_new=0, 'RENAME TABLE t_common_role_permission TO t_role_resource', 'SELECT ''SKIP t_common_role_permission->t_role_resource'' AS status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- t_role_menu: 若不存在需由菜单模块迁移脚本创建（后续补齐）

SELECT 'V2.1.3 RBAC命名迁移完成（如显示SKIP代表无需迁移）' AS status;
