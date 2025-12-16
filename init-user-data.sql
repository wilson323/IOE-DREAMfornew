-- ============================================================
-- IOE-DREAM 用户数据初始化脚本
-- 解决 Spring Security 403 "user not found!" 错误
-- ============================================================

-- 1. 检查用户表结构
DESCRIBE t_common_user;

-- 2. 插入默认管理员用户（密码: admin123）
-- 密码使用BCrypt加密: $2a$10$YJZa8vE6hWQrzBgdZo9lv.OvE8rF.VGN9V.g9Q3I5Q4Q4rQjQ3Q3W
INSERT INTO t_common_user (
    username,
    password,
    real_name,
    email,
    phone,
    status,
    create_time,
    update_time,
    deleted_flag
) VALUES (
    'admin',
    '$2a$10$YJZa8vE6hWQrzBgdZo9lv.OvE8rF.VGN9V.g9Q3I5Q4Q4rQjQ3Q3W',
    '系统管理员',
    'admin@ioedream.com',
    '13800138000',
    1,
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE
    password = '$2a$10$YJZa8vE6hWQrzBgdZo9lv.OvE8rF.VGN9V.g9Q3I5Q4Q4rQjQ3Q3W',
    status = 1,
    update_time = NOW();

-- 3. 插入默认角色
INSERT INTO t_common_role (
    role_code,
    role_name,
    description,
    status,
    create_time,
    update_time,
    deleted_flag
) VALUES (
    'ADMIN',
    '管理员',
    '系统管理员角色',
    1,
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE
    status = 1,
    update_time = NOW();

-- 4. 关联用户角色
INSERT INTO t_common_user_role (
    user_id,
    role_id,
    create_time
)
SELECT u.id, r.id, NOW()
FROM t_common_user u, t_common_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE
    create_time = NOW();

-- 5. 插入基础权限
INSERT INTO t_common_permission (
    permission_code,
    permission_name,
    description,
    status,
    create_time,
    update_time,
    deleted_flag
) VALUES (
    'ALL',
    '所有权限',
    '系统所有操作权限',
    1,
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE
    status = 1,
    update_time = NOW();

-- 6. 关联角色权限
INSERT INTO t_common_role_permission (
    role_id,
    permission_id,
    create_time
)
SELECT r.id, p.id, NOW()
FROM t_common_role r, t_common_permission p
WHERE r.role_code = 'ADMIN' AND p.permission_code = 'ALL'
ON DUPLICATE KEY UPDATE
    create_time = NOW();

-- 7. 验证数据插入结果
SELECT
    u.id as user_id,
    u.username,
    u.real_name,
    u.status as user_status,
    r.role_code,
    r.role_name
FROM t_common_user u
LEFT JOIN t_common_user_role ur ON u.id = ur.user_id
LEFT JOIN t_common_role r ON ur.role_id = r.id
WHERE u.username = 'admin';

-- ============================================================
-- 使用说明:
-- 1. 执行此脚本前确保数据库连接正常
-- 2. 默认管理员账号: admin / admin123
-- 3. 脚本使用了 ON DUPLICATE KEY UPDATE 避免重复插入
-- 4. 执行完成后重新启动服务即可登录
-- ============================================================