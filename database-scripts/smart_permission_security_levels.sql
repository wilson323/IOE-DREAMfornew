-- =============================================
-- SmartAdmin 5级安全级别权限管理模块数据库表结构
-- 创建时间: 2025-11-13
-- 版本: v1.0.0
-- 描述: 实现5级安全级别的精细化权限控制系统
-- =============================================

-- 1. 安全级别定义表
CREATE TABLE `t_security_level` (
    `level_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '级别ID',
    `level_code` VARCHAR(32) NOT NULL COMMENT '级别代码：LEVEL_1-LEVEL_5',
    `level_name` VARCHAR(128) NOT NULL COMMENT '级别名称',
    `level_value` INT NOT NULL COMMENT '级别值：1-5，数字越大权限越高',
    `level_desc` TEXT COMMENT '级别描述',
    `level_color` VARCHAR(32) COMMENT '级别颜色（用于UI显示）',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统级别：0-用户级别，1-系统级别',
    `data_access_rule` JSON COMMENT '数据访问规则配置',
    `operation_permission` JSON COMMENT '操作权限配置',
    `time_restriction` JSON COMMENT '时间限制配置',
    `ip_restriction` JSON COMMENT 'IP访问限制配置',
    `device_restriction` JSON COMMENT '设备访问限制配置',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`level_id`),
    UNIQUE KEY `uk_level_code` (`level_code`),
    UNIQUE KEY `uk_level_value` (`level_value`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全级别定义表';

-- 2. 用户安全级别表
CREATE TABLE `t_user_security_level` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) NOT NULL COMMENT '用户姓名',
    `user_type` VARCHAR(32) NOT NULL COMMENT '用户类型：EMPLOYEE-员工，VISITOR-访客，CONTRACTOR-承包商，SYSTEM-系统',
    `security_level_id` BIGINT NOT NULL COMMENT '安全级别ID',
    `security_level_code` VARCHAR(32) NOT NULL COMMENT '安全级别代码',
    `effective_time` DATETIME NOT NULL COMMENT '生效时间',
    `expire_time` DATETIME COMMENT '失效时间',
    `grant_reason` TEXT COMMENT '授权原因',
    `grant_user_id` BIGINT COMMENT '授权人ID',
    `grant_user_name` VARCHAR(128) COMMENT '授权人姓名',
    `approve_status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '审批状态：PENDING-待审批，ACTIVE-生效，EXPIRED-已过期，REVOKED-已撤销',
    `approve_time` DATETIME COMMENT '审批时间',
    `approve_user_id` BIGINT COMMENT '审批人ID',
    `approve_user_name` VARCHAR(128) COMMENT '审批人姓名',
    `approve_remark` TEXT COMMENT '审批备注',
    `custom_permissions` JSON COMMENT '自定义权限配置',
    `restriction_rules` JSON COMMENT '限制规则配置',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_security_level_id` (`security_level_id`),
    KEY `idx_security_level_code` (`security_level_code`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_approve_status` (`approve_status`),
    KEY `idx_effective_expire_time` (`effective_time`, `expire_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户安全级别表';

-- 3. 权限操作表
CREATE TABLE `t_permission_operation` (
    `operation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '操作ID',
    `operation_code` VARCHAR(128) NOT NULL COMMENT '操作代码',
    `operation_name` VARCHAR(256) NOT NULL COMMENT '操作名称',
    `operation_group` VARCHAR(128) NOT NULL COMMENT '操作分组',
    `module_name` VARCHAR(128) NOT NULL COMMENT '模块名称',
    `resource_type` VARCHAR(64) NOT NULL COMMENT '资源类型：MENU-菜单，BUTTON-按钮，API-接口，DATA-数据',
    `resource_path` VARCHAR(512) COMMENT '资源路径',
    `http_method` VARCHAR(32) COMMENT 'HTTP方法：GET，POST，PUT，DELETE等',
    `operation_desc` TEXT COMMENT '操作描述',
    `risk_level` VARCHAR(32) NOT NULL DEFAULT 'LOW' COMMENT '风险级别：LOW-低，MEDIUM-中，HIGH-高，CRITICAL-严重',
    `required_security_level` INT NOT NULL DEFAULT 1 COMMENT '所需最低安全级别',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统操作：0-用户操作，1-系统操作',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`operation_id`),
    UNIQUE KEY `uk_operation_code` (`operation_code`),
    KEY `idx_operation_group` (`operation_group`),
    KEY `idx_module_name` (`module_name`),
    KEY `idx_resource_type` (`resource_type`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_required_security_level` (`required_security_level`),
    KEY `idx_is_system` (`is_system`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限操作表';

-- 4. 用户操作权限表
CREATE TABLE `t_user_operation_permission` (
    `permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) NOT NULL COMMENT '用户姓名',
    `operation_id` BIGINT NOT NULL COMMENT '操作ID',
    `operation_code` VARCHAR(128) NOT NULL COMMENT '操作代码',
    `operation_name` VARCHAR(256) NOT NULL COMMENT '操作名称',
    `permission_type` VARCHAR(32) NOT NULL COMMENT '权限类型：GRANT-授权，DENY-拒绝，TEMPORARY-临时',
    `effective_time` DATETIME NOT NULL COMMENT '生效时间',
    `expire_time` DATETIME COMMENT '失效时间',
    `access_count_limit` INT COMMENT '访问次数限制',
    `access_count_used` INT DEFAULT 0 COMMENT '已使用访问次数',
    `time_quota_minutes` INT COMMENT '时间配额（分钟）',
    `time_quota_used` INT DEFAULT 0 COMMENT '已使用时间（分钟）',
    `grant_reason` TEXT COMMENT '授权原因',
    `grant_user_id` BIGINT COMMENT '授权人ID',
    `grant_user_name` VARCHAR(128) COMMENT '授权人姓名',
    `approve_status` VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '审批状态',
    `approve_time` DATETIME COMMENT '审批时间',
    `approve_user_id` BIGINT COMMENT '审批人ID',
    `conditions` JSON COMMENT '权限条件配置',
    `restrictions` JSON COMMENT '权限限制配置',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_user_operation` (`user_id`, `operation_id`),
    KEY `idx_operation_code` (`operation_code`),
    KEY `idx_permission_type` (`permission_type`),
    KEY `idx_approve_status` (`approve_status`),
    KEY `idx_effective_expire_time` (`effective_time`, `expire_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户操作权限表';

-- 5. 数据权限规则表
CREATE TABLE `t_data_permission_rule` (
    `rule_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `rule_code` VARCHAR(128) NOT NULL COMMENT '规则代码',
    `rule_name` VARCHAR(256) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(64) NOT NULL COMMENT '规则类型：DEPARTMENT-部门，AREA-区域，CUSTOM-自定义',
    `module_name` VARCHAR(128) NOT NULL COMMENT '模块名称',
    `table_name` VARCHAR(128) COMMENT '数据表名',
    `field_name` VARCHAR(128) COMMENT '字段名',
    `filter_expression` TEXT COMMENT '过滤表达式',
    `rule_config` JSON COMMENT '规则配置',
    `is_enabled` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序序号',
    `description` TEXT COMMENT '规则描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`rule_id`),
    UNIQUE KEY `uk_rule_code` (`rule_code`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_module_name` (`module_name`),
    KEY `idx_table_name` (`table_name`),
    KEY `idx_is_enabled` (`is_enabled`),
    KEY `idx_sort_order` (`sort_order`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限规则表';

-- 6. 用户数据权限表
CREATE TABLE `t_user_data_permission` (
    `permission_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) NOT NULL COMMENT '用户姓名',
    `rule_id` BIGINT NOT NULL COMMENT '规则ID',
    `rule_code` VARCHAR(128) NOT NULL COMMENT '规则代码',
    `permission_scope` VARCHAR(512) COMMENT '权限范围（JSON格式的部门、区域ID列表）',
    `filter_value` TEXT COMMENT '过滤值',
    `permission_level` VARCHAR(32) NOT NULL DEFAULT 'READ' COMMENT '权限级别：READ-只读，WRITE-读写，DELETE-删除，ALL-全部',
    `effective_time` DATETIME NOT NULL COMMENT '生效时间',
    `expire_time` DATETIME COMMENT '失效时间',
    `grant_reason` TEXT COMMENT '授权原因',
    `grant_user_id` BIGINT COMMENT '授权人ID',
    `grant_user_name` VARCHAR(128) COMMENT '授权人姓名',
    `conditions` JSON COMMENT '权限条件',
    `restrictions` JSON COMMENT '权限限制',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-正常，1-删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '版本号',
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_user_rule` (`user_id`, `rule_id`),
    KEY `idx_rule_code` (`rule_code`),
    KEY `idx_permission_level` (`idx_permission_level`),
    KEY `idx_effective_expire_time` (`effective_time`, `expire_time`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_deleted_flag` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户数据权限表';

-- 7. 权限审计日志表
CREATE TABLE `t_permission_audit_log` (
    `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `log_type` VARCHAR(64) NOT NULL COMMENT '日志类型：LOGIN-登录，OPERATION-操作，PERMISSION_CHANGE-权限变更',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(128) NOT NULL COMMENT '用户姓名',
    `operation_id` BIGINT COMMENT '操作ID',
    `operation_code` VARCHAR(128) COMMENT '操作代码',
    `operation_name` VARCHAR(256) COMMENT '操作名称',
    `resource_type` VARCHAR(64) COMMENT '资源类型',
    `resource_id` VARCHAR(128) COMMENT '资源ID',
    `resource_name` VARCHAR(256) COMMENT '资源名称',
    `security_level_before` INT COMMENT '操作前安全级别',
    `security_level_after` INT COMMENT '操作后安全级别',
    `permission_result` VARCHAR(64) NOT NULL COMMENT '权限结果：GRANTED-授权，DENIED-拒绝，EXPIRED-过期，VIOLATION-违规',
    `failure_reason` TEXT COMMENT '失败原因',
    `client_ip` VARCHAR(64) COMMENT '客户端IP',
    `device_info` VARCHAR(512) COMMENT '设备信息',
    `user_agent` VARCHAR(1024) COMMENT '用户代理',
    `session_id` VARCHAR(128) COMMENT '会话ID',
    `request_data` JSON COMMENT '请求数据',
    `response_data` JSON COMMENT '响应数据',
    `execution_time_ms` INT COMMENT '执行时间（毫秒）',
    `risk_score` INT COMMENT '风险评分',
    `violation_type` VARCHAR(128) COMMENT '违规类型',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_code` (`operation_code`),
    KEY `idx_permission_result` (`permission_result`),
    KEY `idx_security_level_before` (`security_level_before`),
    KEY `idx_security_level_after` (`security_level_after`),
    KEY `idx_risk_score` (`risk_score`),
    KEY `idx_violation_type` (`violation_type`),
    KEY `idx_client_ip` (`client_ip`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限审计日志表';

-- 初始化5级安全级别基础数据
INSERT INTO `t_security_level` (`level_code`, `level_name`, `level_value`, `level_desc`, `level_color`, `is_system`) VALUES
('LEVEL_1', '基础级别', 1, '基础访问权限，仅能查看公开信息', '#52c41a', 1),
('LEVEL_2', '普通级别', 2, '普通业务权限，能操作基础业务数据', '#1890ff', 1),
('LEVEL_3', '重要级别', 3, '重要业务权限，能操作敏感业务数据', '#faad14', 1),
('LEVEL_4', '机密级别', 4, '机密数据权限，能操作核心机密数据', '#f5222d', 1),
('LEVEL_5', '绝密级别', 5, '绝密数据权限，最高级别访问权限', '#722ed1', 1);

-- 初始化基础操作权限数据
INSERT INTO `t_permission_operation` (`operation_code`, `operation_name`, `operation_group`, `module_name`, `resource_type`, `resource_path`, `http_method`, `operation_desc`, `risk_level`, `required_security_level`) VALUES
('SYSTEM_LOGIN', '系统登录', 'SYSTEM', '系统管理', 'API', '/api/auth/login', 'POST', '用户登录系统', 'LOW', 1),
('SYSTEM_LOGOUT', '系统登出', 'SYSTEM', '系统管理', 'API', '/api/auth/logout', 'POST', '用户登出系统', 'LOW', 1),
('USER_VIEW', '查看用户', 'USER', '用户管理', 'BUTTON', 'user:view', 'GET', '查看用户列表和详情', 'LOW', 2),
('USER_ADD', '新增用户', 'USER', '用户管理', 'BUTTON', 'user:add', 'POST', '新增用户信息', 'MEDIUM', 3),
('USER_EDIT', '编辑用户', 'USER', '用户管理', 'BUTTON', 'user:edit', 'PUT', '编辑用户信息', 'MEDIUM', 3),
('USER_DELETE', '删除用户', 'USER', '用户管理', 'BUTTON', 'user:delete', 'DELETE', '删除用户信息', 'HIGH', 4),
('DATA_EXPORT', '数据导出', 'DATA', '数据管理', 'BUTTON', 'data:export', 'POST', '导出业务数据', 'HIGH', 4),
('DATA_DELETE', '数据删除', 'DATA', '数据管理', 'BUTTON', 'data:delete', 'DELETE', '删除业务数据', 'CRITICAL', 5),
('CONFIG_SYSTEM', '系统配置', 'SYSTEM', '系统管理', 'API', '/api/system/config', 'PUT', '修改系统配置', 'CRITICAL', 5);

-- 初始化数据权限规则
INSERT INTO `t_data_permission_rule` (`rule_code`, `rule_name`, `rule_type`, `module_name`, `table_name`, `field_name`, `filter_expression`, `rule_config`, `description`) VALUES
('DEPARTMENT_SELF', '本部门数据', 'DEPARTMENT', '通用', 't_user', 'department_id', 'department_id = ${user.department_id}', '{"allowRead": true, "allowWrite": true}', '用户只能查看和操作本部门数据'),
('DEPARTMENT_SUB', '本部门及下级数据', 'DEPARTMENT', '通用', 't_user', 'department_id', 'department_id IN (SELECT id FROM t_department WHERE path LIKE CONCAT(${user.department_path}, \'%\'))', '{"allowRead": true, "allowWrite": true}', '用户可以查看和操作本部门及下级部门数据'),
('AREA_SELF', '本区域数据', 'AREA', '通用', 't_device', 'area_id', 'area_id = ${user.area_id}', '{"allowRead": true, "allowWrite": false}', '用户只能查看本区域设备数据'),
('AREA_SUB', '本区域及下级数据', 'AREA', '通用', 't_device', 'area_id', 'area_id IN (SELECT id FROM t_area WHERE path LIKE CONCAT(${user.area_path}, \'%\'))', '{"allowRead": true, "allowWrite": false}', '用户可以查看本区域及下级区域设备数据');

-- 创建索引优化查询性能
CREATE INDEX idx_permission_audit_user_time ON t_permission_audit_log(user_id, create_time);
CREATE INDEX idx_permission_audit_result ON t_permission_audit_log(permission_result, create_time);
CREATE INDEX idx_user_security_level_user_type ON t_user_security_level(user_id, user_type);
CREATE INDEX idx_user_operation_permission_user_op ON t_user_operation_permission(user_id, operation_id);