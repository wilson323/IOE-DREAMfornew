-- =====================================================
-- IOE-DREAM 数据库迁移脚本
-- 版本: V2.1.3
-- 描述: RBAC/用户权限表命名体系统一（t_common_* -> t_*）
-- 策略: 方案B - 以代码实体@TableName为准（t_user/t_role/t_user_role/t_role_resource/t_role_menu/t_rbac_resource）
-- 说明:
-- 1) 仅在旧表存在且新表不存在时执行 RENAME TABLE
-- 2) 旧表字段与新实体字段可能不完全一致，后续用 V2.1.4+ 脚本做字段对齐（ALTER TABLE）
-- =====================================================

USE ioedream;

-- ----------- helper: safe rename -----------
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

-- t_common_permission -> t_rbac_resource  (原权限表作为资源表基础，后续字段对齐)
SET @has_old := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_common_permission');
SET @has_new := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_rbac_resource');
SET @sql := IF(@has_old>0 AND @has_new=0, 'RENAME TABLE t_common_permission TO t_rbac_resource', 'SELECT ''SKIP t_common_permission->t_rbac_resource'' AS status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- t_common_role_permission -> t_role_resource (原角色权限关联表作为角色资源关联表基础)
SET @has_old := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_common_role_permission');
SET @has_new := (SELECT COUNT(1) FROM information_schema.tables WHERE table_schema=@db AND table_name='t_role_resource');
SET @sql := IF(@has_old>0 AND @has_new=0, 'RENAME TABLE t_common_role_permission TO t_role_resource', 'SELECT ''SKIP t_common_role_permission->t_role_resource'' AS status');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- t_role_menu: 原脚本中未创建，若新环境需要，后续由专用脚本创建（菜单模块）
-- t_role_menu 通常来自业务模块菜单脚本/迁移脚本

SELECT 'V2.1.3 RBAC命名迁移完成（如显示SKIP代表无需迁移）' AS status;
