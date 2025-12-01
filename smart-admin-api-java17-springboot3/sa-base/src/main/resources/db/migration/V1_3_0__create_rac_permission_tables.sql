-- 创建统一RAC权限模型相关表
-- Author: IOE-DREAM Team
-- Date: 2025-01-17
-- Description: 统一人员-区域-权限模型与RAC鉴权中间层数据表

-- 1. 补强人员主档表
ALTER TABLE t_person_profile
  ADD COLUMN person_status TINYINT NOT NULL DEFAULT 1 COMMENT '0-黑名单,1-在职,2-停用,3-离职',
  ADD COLUMN id_masked VARCHAR(128) COMMENT '证件号脱敏',
  ADD COLUMN ext_json JSON NULL COMMENT '扩展信息',
  ADD COLUMN deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '软删除',
  ADD INDEX idx_person_status (person_status),
  ADD INDEX idx_deleted_flag (deleted_flag);

-- 2. 创建人员多凭证表
CREATE TABLE `t_person_credential` (
  `credential_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '凭证ID',
  `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
  `credential_type` VARCHAR(32) NOT NULL COMMENT '凭证类型(CARD|BIOMETRIC|PASSWORD)',
  `credential_value` VARCHAR(256) NOT NULL COMMENT '凭证值',
  `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `device_info` JSON DEFAULT NULL COMMENT '设备信息',
  `ext_json` JSON DEFAULT NULL COMMENT '扩展信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`credential_id`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_person_type` (`person_id`, `credential_type`),
  KEY `idx_status` (`status`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_expire_time` (`expire_time`),
  UNIQUE KEY `uk_person_type_value` (`person_id`, `credential_type`, `credential_value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员多凭证表';

-- 3. 补强区域表（层级/路径/路径哈希）
ALTER TABLE t_area_info
  ADD COLUMN `path` VARCHAR(1024) DEFAULT NULL COMMENT '区域路径(/1/3/9)',
  ADD COLUMN `level` INT(11) DEFAULT NULL COMMENT '层级',
  ADD COLUMN `path_hash` VARCHAR(64) DEFAULT NULL COMMENT '路径哈希',
  ADD COLUMN `deleted_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  ADD INDEX `idx_path_hash` (`path_hash`),
  ADD INDEX `idx_level` (`level`),
  ADD INDEX `idx_parent_path` (`parent_area_id`, `path`);

-- 4. 创建人员-区域授权表
CREATE TABLE `t_area_person` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `area_id` BIGINT(20) NOT NULL COMMENT '区域ID',
  `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
  `data_scope` VARCHAR(32) NOT NULL DEFAULT 'AREA' COMMENT '数据域(AREA|DEPT|SELF|CUSTOM)',
  `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `auth_type` VARCHAR(32) DEFAULT NULL COMMENT '授权类型(MANUAL|AUTO|INHERIT)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_area_id` (`area_id`),
  KEY `idx_person_id` (`person_id`),
  KEY `idx_data_scope` (`data_scope`),
  KEY `idx_status` (`status`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_expire_time` (`expire_time`),
  UNIQUE KEY `uk_area_person` (`area_id`, `person_id`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员-区域授权表';

-- 5. 创建RBAC资源表
CREATE TABLE `t_rbac_resource` (
  `resource_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `resource_code` VARCHAR(128) NOT NULL COMMENT '资源编码',
  `resource_name` VARCHAR(128) NOT NULL COMMENT '资源名称',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '资源类型(API|MENU|BUTTON|DATA)',
  `module_code` VARCHAR(64) DEFAULT NULL COMMENT '模块编码',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '资源描述',
  `parent_id` BIGINT(20) DEFAULT NULL COMMENT '父资源ID',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`resource_id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`, `deleted_flag`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_module_code` (`module_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC资源表';

-- 6. 创建RBAC角色表
CREATE TABLE `t_rbac_role` (
  `role_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(128) NOT NULL COMMENT '角色名称',
  `role_type` VARCHAR(32) DEFAULT NULL COMMENT '角色类型(SYSTEM|BUSINESS|CUSTOM)',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '角色描述',
  `is_system` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否系统角色',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`, `deleted_flag`),
  KEY `idx_role_type` (`role_type`),
  KEY `idx_is_system` (`is_system`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC角色表';

-- 7. 创建RBAC角色资源关联表
CREATE TABLE `t_rbac_role_resource` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `resource_id` BIGINT(20) NOT NULL COMMENT '资源ID',
  `action` VARCHAR(64) DEFAULT NULL COMMENT '操作动作(READ|WRITE|DELETE|APPROVE|*)',
  `condition_json` JSON DEFAULT NULL COMMENT 'RAC条件(JSON格式)',
  `priority` INT(11) DEFAULT 0 COMMENT '优先级',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_res_action` (`role_id`, `resource_id`, `action`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_action` (`action`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RBAC角色资源关联表';

-- 8. 创建用户角色关联表
CREATE TABLE `t_rbac_user_role` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `effective_time` DATETIME DEFAULT NULL COMMENT '生效时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `grant_type` VARCHAR(32) DEFAULT NULL COMMENT '授权类型(MANUAL|AUTO)',
  `grant_source` VARCHAR(128) DEFAULT NULL COMMENT '授权来源',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`, `deleted_flag`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_status` (`status`),
  KEY `idx_effective_time` (`effective_time`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 9. 创建权限审计日志表
CREATE TABLE `t_permission_audit_log` (
  `log_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `username` VARCHAR(64) DEFAULT NULL COMMENT '用户名',
  `resource_code` VARCHAR(128) NOT NULL COMMENT '资源编码',
  `action` VARCHAR(64) DEFAULT NULL COMMENT '操作动作',
  `data_scope` VARCHAR(32) DEFAULT NULL COMMENT '数据域',
  `request_ip` VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` JSON DEFAULT NULL COMMENT '请求参数',
  `decision` VARCHAR(32) NOT NULL COMMENT '决策(ALLOW|DENY)',
  `decision_reason` VARCHAR(500) DEFAULT NULL COMMENT '决策原因',
  `execution_time` BIGINT(20) DEFAULT NULL COMMENT '执行时间(毫秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_code` (`resource_code`),
  `idx_decision` (`decision`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_request_ip` (`request_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限审计日志表';

-- 插入默认系统资源
INSERT INTO `t_rbac_resource` (`resource_code`, `resource_name`, `resource_type`, `module_code`, `description`) VALUES
('system:dashboard', '系统首页', 'MENU', 'system', '系统管理首页'),
('system:user:list', '用户列表', 'API', 'system', '用户列表查询接口'),
('system:user:create', '创建用户', 'BUTTON', 'system', '创建用户按钮'),
('system:user:edit', '编辑用户', 'BUTTON', 'system', '编辑用户按钮'),
('system:user:delete', '删除用户', 'BUTTON', 'system', '删除用户按钮'),
('system:role:list', '角色列表', 'API', 'system', '角色列表查询接口'),
('system:role:create', '创建角色', 'BUTTON', 'system', '创建角色按钮'),
('system:role:edit', '编辑角色', 'BUTTON', 'system', '编辑角色按钮'),
('system:role:delete', '删除角色', 'BUTTON', 'system', '删除角色按钮'),
('area:device:list', '门禁设备列表', 'API', 'area', '门禁设备列表查询接口'),
('area:device:view', '查看门禁设备', 'BUTTON', 'area', '查看门禁设备按钮'),
('area:device:create', '创建门禁设备', 'BUTTON', 'area', '创建门禁设备按钮'),
('area:device:edit', '编辑门禁设备', 'BUTTON', 'area', '编辑门禁设备按钮'),
('area:device:delete', '删除门禁设备', 'BUTTON', 'area', '删除门禁设备按钮'),
('area:record:list', '门禁记录列表', 'API', 'area', '门禁记录查询接口'),
('area:record:view', '查看门禁记录', 'DATA', 'area', '门禁记录数据访问'),
('area:record:export', '导出门禁记录', 'BUTTON', 'area', '导出门禁记录按钮'),
('attendance:record:list', '考勤记录列表', 'API', 'attendance', '考勤记录查询接口'),
('attendance:record:view', '查看考勤记录', 'DATA', 'attendance', '考勤记录数据访问'),
('attendance:record:export', '导出考勤记录', 'BUTTON', 'attendance', '导出考勤记录按钮'),
('consume:record:list', '消费记录列表', 'API', 'consume', '消费记录查询接口'),
('consume:record:view', '查看消费记录', 'DATA', 'consume', '消费记录数据访问'),
('consume:record:export', '导出消费记录', 'BUTTON', 'consume', '导出消费记录按钮');

-- 插入默认系统角色
INSERT INTO `t_rbac_role` (`role_code`, `role_name`, `role_type`, `description`, `is_system`) VALUES
('SUPER_ADMIN', '超级管理员', 'SYSTEM', '系统超级管理员，拥有所有权限', 1),
('ADMIN', '管理员', 'SYSTEM', '系统管理员，拥有大部分管理权限', 1),
('AREA_MANAGER', '区域管理员', 'BUSINESS', '区域管理员，负责特定区域的门禁管理', 0),
('AREA_OPERATOR', '区域操作员', 'BUSINESS', '区域操作员，负责区域的日常操作', 0),
('ATTENDANCE_MANAGER', '考勤管理员', 'BUSINESS', '考勤管理员，负责考勤数据管理', 0),
('CONSUME_MANAGER', '消费管理员', 'BUSINESS', '消费管理员，负责消费数据管理', 0),
('USER', '普通用户', 'BUSINESS', '普通用户，基础权限', 0);

-- 插入默认角色资源授权
INSERT INTO `t_rbac_role_resource` (`role_id`, `resource_id`, `action`, `condition_json`)
SELECT r.role_id, res.resource_id, '*', NULL
FROM t_rbac_role r, t_rbac_resource res
WHERE r.role_code IN ('SUPER_ADMIN') AND res.status = 1;

INSERT INTO `t_rbac_role_resource` (`role_id`, `resource_id`, `action`, `condition_json`)
SELECT r.role_id, res.resource_id, 'READ', NULL
FROM t_rbac_role r, t_rbac_resource res
WHERE r.role_code = 'ADMIN' AND res.status = 1 AND r.role_id NOT IN (
    SELECT DISTINCT rr.role_id FROM t_rbac_role_resource rr
    WHERE rr.resource_id = res.resource_id
);

-- 创建视图：用户权限汇总
CREATE OR REPLACE VIEW `v_user_permission` AS
SELECT
    u.user_id,
    u.username,
    r.role_id,
    r.role_code,
    r.role_name,
    res.resource_id,
    res.resource_code,
    res.resource_name,
    res.resource_type,
    rr.action,
    rr.condition_json,
    ap.area_id,
    ap.data_scope as area_data_scope
FROM t_rbac_user_role ur
JOIN t_rbac_role r ON ur.role_id = r.role_id AND r.status = 1 AND ur.deleted_flag = 0
JOIN t_rbac_role_resource rr ON r.role_id = rr.role_id AND rr.status = 1
JOIN t_rbac_resource res ON rr.resource_id = res.resource_id AND res.deleted_flag = 0
LEFT JOIN t_area_person ap ON u.user_id = ap.person_id AND ap.status = 1 AND ap.deleted_flag = 0
WHERE ur.status = 1 AND ur.deleted_flag = 0;

-- 创建触发器：区域层级路径自动更新
DELIMITER //
CREATE TRIGGER tr_area_path_update BEFORE INSERT ON t_area_info FOR EACH ROW
BEGIN
    DECLARE parent_path VARCHAR(1024);
    DECLARE parent_level INT;

    -- 如果没有设置路径，则根据父区域生成
    IF NEW.path IS NULL OR NEW.path = '' THEN
        IF NEW.parent_area_id IS NULL OR NEW.parent_area_id = 0 THEN
            SET NEW.path = CONCAT('/', NEW.area_id);
            SET NEW.level = 1;
        ELSE
            SELECT path, level INTO parent_path, parent_level
            FROM t_area_info
            WHERE area_id = NEW.parent_area_id AND deleted_flag = 0;

            SET NEW.path = CONCAT(IFNULL(parent_path, ''), '/', NEW.area_id);
            SET NEW.level = IFNULL(parent_level, 0) + 1;
        END IF;

        -- 生成路径哈希
        SET NEW.path_hash = MD5(NEW.path);
    END IF;
END//
DELIMITER ;

-- 创建存储过程：批量更新区域层级路径
DELIMITER //
CREATE PROCEDURE sp_update_area_paths()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE current_id BIGINT;
    DECLARE current_parent BIGINT;
    DECLARE current_level INT;
    DECLARE current_path VARCHAR(1024);

    -- 游标声明
    DECLARE cur CURSOR FOR
        SELECT area_id, parent_area_id
        FROM t_area_info
        WHERE (path IS NULL OR path = '' OR path_hash IS NULL) AND deleted_flag = 0
        ORDER BY parent_area_id NULLS FIRST, area_id;

    -- 打开游标
    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO current_id, current_parent;
        IF done THEN
            LEAVE read_loop;
        END IF;

        -- 计算路径和层级
        SET current_path = '';
        SET current_level = 1;

        -- 递归查找父路径
        WHILE current_parent IS NOT NULL AND current_parent != 0 DO
            SELECT CONCAT('/', current_parent, current_path), level + 1
            INTO current_path, current_level
            FROM t_area_info
            WHERE area_id = current_parent AND deleted_flag = 0;

            SET current_parent = (SELECT parent_area_id FROM t_area_info WHERE area_id = current_parent AND deleted_flag = 0 LIMIT 1);
        END WHILE;

        -- 更新当前区域
        UPDATE t_area_info
        SET
            path = CONCAT('/', current_id, current_path),
            level = current_level,
            path_hash = MD5(CONCAT('/', current_id, current_path)),
            update_time = NOW()
        WHERE area_id = current_id;

    END LOOP;

    -- 关闭游标
    CLOSE cur;
END//
DELIMITER ;

-- 初始化触发器：为现有数据执行路径更新
-- CALL sp_update_area_paths();