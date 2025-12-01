-- smart-permission 权限管理模块数据库脚本
-- 创建时间: 2025-11-14
-- 版本: v1.0

USE smart_admin_v3;

-- 1. 安全级别表
CREATE TABLE IF NOT EXISTS t_smart_security_level (
    level_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '安全级别ID',
    level_code VARCHAR(20) NOT NULL UNIQUE COMMENT '级别代码',
    level_name VARCHAR(50) NOT NULL COMMENT '级别名称',
    level_description VARCHAR(200) COMMENT '级别描述',
    level_value INT NOT NULL COMMENT '级别值(1-5)',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统级别',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT NOT NULL COMMENT '创建用户ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '版本号',
    INDEX idx_level_code (level_code),
    INDEX idx_level_value (level_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全级别表';

-- 2. 数据权限表
CREATE TABLE IF NOT EXISTS t_smart_data_permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '数据权限ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型(area,device,department)',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    permission_type TINYINT NOT NULL COMMENT '权限类型(1:查看,2:编辑,3:删除,4:全部)',
    security_level_id BIGINT NOT NULL COMMENT '安全级别ID',
    permission_scope VARCHAR(100) COMMENT '权限范围描述',
    is_temporary TINYINT DEFAULT 0 COMMENT '是否临时权限',
    start_time DATETIME COMMENT '权限开始时间',
    end_time DATETIME COMMENT '权限结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT NOT NULL COMMENT '创建用户ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '版本号',
    INDEX idx_user_id (user_id),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_security_level (security_level_id),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (security_level_id) REFERENCES t_smart_security_level(level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限表';

-- 3. 临时权限表
CREATE TABLE IF NOT EXISTS t_smart_temporary_permission (
    temp_permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '临时权限ID',
    user_id BIGINT NOT NULL COMMENT '申请人用户ID',
    approver_id BIGINT COMMENT '审批人用户ID',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    permission_type TINYINT NOT NULL COMMENT '权限类型',
    security_level_id BIGINT NOT NULL COMMENT '安全级别ID',
    apply_reason TEXT COMMENT '申请原因',
    approval_status TINYINT DEFAULT 1 COMMENT '审批状态(1:待审批,2:已通过,3:已拒绝)',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    start_time DATETIME NOT NULL COMMENT '权限开始时间',
    end_time DATETIME NOT NULL COMMENT '权限结束时间',
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    approval_time DATETIME COMMENT '审批时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    version INT DEFAULT 0 COMMENT '版本号',
    INDEX idx_user_id (user_id),
    INDEX idx_approver_id (approver_id),
    INDEX idx_approval_status (approval_status),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_time_range (start_time, end_time),
    FOREIGN KEY (security_level_id) REFERENCES t_smart_security_level(level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='临时权限表';

-- 4. 权限审计表
CREATE TABLE IF NOT EXISTS t_smart_permission_audit (
    audit_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审计ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    operation_type TINYINT NOT NULL COMMENT '操作类型(1:登录,2:权限申请,3:权限验证,4:权限变更)',
    resource_type VARCHAR(50) COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    permission_result TINYINT COMMENT '权限验证结果(1:通过,2:拒绝)',
    security_level_id BIGINT COMMENT '安全级别ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    operation_detail TEXT COMMENT '操作详情',
    error_message VARCHAR(1000) COMMENT '错误信息',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_time (operation_time),
    INDEX idx_resource (resource_type, resource_id),
    FOREIGN KEY (security_level_id) REFERENCES t_smart_security_level(level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限审计表';

-- 插入初始安全级别数据
INSERT INTO t_smart_security_level (level_code, level_name, level_description, level_value, is_system, create_user_id) VALUES
('TOP_SECRET', '绝密级', '最高安全级别，仅限核心人员访问', 5, 1, 1),
('SECRET', '机密级', '高度机密信息，限制访问', 4, 1, 1),
('CONFIDENTIAL', '秘密级', '机密信息，需授权访问', 3, 1, 1),
('INTERNAL', '内部级', '内部信息，员工可访问', 2, 1, 1),
('PUBLIC', '公开级', '公开信息，所有人可访问', 1, 1, 1);

-- 创建权限验证视图
CREATE OR REPLACE VIEW v_user_data_permission AS
SELECT
    dp.user_id,
    dp.resource_type,
    dp.resource_id,
    dp.permission_type,
    sl.level_code as security_level_code,
    sl.level_name as security_level_name,
    dp.permission_scope,
    dp.start_time,
    dp.end_time,
    CASE
        WHEN dp.is_temporary = 1 THEN '临时权限'
        ELSE '正式权限'
    END as permission_type_desc
FROM t_smart_data_permission dp
LEFT JOIN t_smart_security_level sl ON dp.security_level_id = sl.level_id
WHERE dp.deleted_flag = 0
AND (dp.end_time IS NULL OR dp.end_time > NOW())
AND (dp.start_time IS NULL OR dp.start_time <= NOW());

-- 创建临时权限待审批视图
CREATE OR REPLACE VIEW v_temporary_permission_pending AS
SELECT
    tp.temp_permission_id,
    tp.user_id,
    tp.resource_type,
    tp.resource_id,
    tp.permission_type,
    tp.apply_reason,
    tp.approval_status,
    tp.start_time,
    tp.end_time,
    tp.apply_time,
    sl.level_code as security_level_code,
    sl.level_name as security_level_name
FROM t_smart_temporary_permission tp
LEFT JOIN t_smart_security_level sl ON tp.security_level_id = sl.level_id
WHERE tp.deleted_flag = 0
AND tp.approval_status = 1; -- 待审批

-- 权限验证函数
DELIMITER $$
CREATE FUNCTION fn_check_permission(
    p_user_id BIGINT,
    p_resource_type VARCHAR(50),
    p_resource_id BIGINT,
    p_permission_type TINYINT
) RETURNS BOOLEAN
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_permission_count INT DEFAULT 0;
    DECLARE v_security_level_value INT DEFAULT 1;
    DECLARE v_resource_security_level INT DEFAULT 1;

    -- 获取用户的最高安全级别
    SELECT COALESCE(MAX(sl.level_value), 1) INTO v_security_level_value
    FROM t_smart_data_permission dp
    INNER JOIN t_smart_security_level sl ON dp.security_level_id = sl.level_id
    WHERE dp.user_id = p_user_id
    AND dp.deleted_flag = 0
    AND (dp.end_time IS NULL OR dp.end_time > NOW())
    AND (dp.start_time IS NULL OR dp.start_time <= NOW());

    -- 检查是否有对应的权限
    SELECT COUNT(*) INTO v_permission_count
    FROM v_user_data_permission
    WHERE user_id = p_user_id
    AND resource_type = p_resource_type
    AND resource_id = p_resource_id
    AND permission_type >= p_permission_type;

    RETURN v_permission_count > 0;
END$$
DELIMITER ;

-- 创建索引优化性能
CREATE INDEX idx_permission_audit_composite ON t_smart_permission_audit(user_id, operation_type, operation_time);
CREATE INDEX idx_data_permission_composite ON t_smart_data_permission(user_id, resource_type, resource_id, end_time);
CREATE INDEX idx_temp_permission_composite ON t_smart_temporary_permission(user_id, approval_status, apply_time);

-- 创建分区表（用于大数据量时）
-- ALTER TABLE t_smart_permission_audit PARTITION BY RANGE (TO_DAYS(operation_time)) (
--     PARTITION p_202411 VALUES LESS THAN (TO_DAYS('2024-12-01')),
--     PARTITION p_202412 VALUES LESS THAN (TO_DAYS('2025-01-01')),
--     PARTITION p_202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );

-- 备注：数据库脚本已创建完成，包含完整的表结构、初始数据、视图和函数