-- =====================================================
-- IOE-DREAM 数据库回滚脚本
-- 版本: V1.1.0 → V1.0.0
-- 描述: 回滚初始数据，保留表结构
-- 警告: 执行此脚本会删除所有初始数据，请谨慎操作！
-- 创建时间: 2025-12-10
-- =====================================================

-- 设置执行环境
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

USE ioedream;

-- =====================================================
-- ⚠ 警告：执行此脚本会删除所有初始数据
-- =====================================================

-- =====================================================
-- 1. 删除初始数据（保留表结构）
-- =====================================================

-- 删除用户角色关联
DELETE FROM t_common_user_role;

-- 删除角色权限关联
DELETE FROM t_common_role_permission;

-- 删除用户数据（保留admin用户，如需删除请取消注释）
-- DELETE FROM t_common_user WHERE username != 'admin';

-- 删除角色数据
DELETE FROM t_common_role WHERE role_code IN ('SUPER_ADMIN', 'SYSTEM_ADMIN', 'ADMIN', 'USER', 'VISITOR');

-- 删除权限数据
DELETE FROM t_common_permission;

-- 删除字典数据
DELETE FROM t_common_dict_data;
DELETE FROM t_common_dict_type;

-- 删除部门数据
DELETE FROM t_common_department WHERE department_code != 'IOE_DREAM';

-- 删除区域数据
DELETE FROM t_common_area;

-- 删除设备数据
DELETE FROM t_common_device;
DELETE FROM t_video_device;

-- 删除考勤班次数据
DELETE FROM t_attendance_shift;

-- 删除消费账户数据
DELETE FROM t_consume_account;

-- 删除系统配置数据
DELETE FROM t_system_config;

-- =====================================================
-- 2. 记录回滚历史
-- =====================================================

INSERT INTO t_migration_history (
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
) VALUES (
    'V1.1.0-ROLLBACK',
    '回滚V1.1.0初始数据 - 删除所有初始数据，保留表结构',
    'rollback-v1.1.0.sql',
    'SUCCESS',
    NOW(),
    NOW(),
    NOW()
);

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 3. 输出执行完成信息
-- =====================================================

SELECT 'V1.1.0 回滚脚本执行完成！' AS rollback_status,
       '已删除所有初始数据，表结构保留' AS rollback_summary,
       NOW() AS completed_time;

-- =====================================================
-- 脚本结束
-- =====================================================

