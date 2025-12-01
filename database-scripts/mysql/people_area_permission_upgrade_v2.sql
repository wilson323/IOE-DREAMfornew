-- ====================================================================
-- 人员·区域·权限 统一升级脚本
-- 版本: 1.0.0
-- 创建时间: 2025-11-16
-- 描述: 基于现有表结构进行统一人员区域权限模型的升级
-- 依赖: 需先执行 V1_2_0__create_access_control_tables.sql
-- ====================================================================

-- 1) 人员域统一：将 t_hr_employee 升级为 t_person_profile
-- 注意：实际生产环境中，如果已有 t_person_profile 则跳过此步骤

-- 创建统一人员档案表（如果不存在）
CREATE TABLE IF NOT EXISTS t_person_profile (
  person_id      BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '人员ID（主键）',
  person_code    VARCHAR(32) NOT NULL COMMENT '人员工号（系统唯一）',
  person_name    VARCHAR(64) NOT NULL COMMENT '姓名',
  gender         TINYINT NULL COMMENT '性别 1男 2女',
  email          VARCHAR(128) NULL,
  phone          VARCHAR(32) NULL,
  department_id  BIGINT NULL COMMENT '部门ID',
  position       VARCHAR(64) NULL COMMENT '职位',
  person_status  TINYINT NOT NULL DEFAULT 1 COMMENT '人员状态 0黑名单 1在职 2停用 3离职',
  id_card        VARCHAR(32) NULL COMMENT '身份证号',
  id_masked      VARCHAR(128) NULL COMMENT '证件号脱敏显示',
  salary         DECIMAL(12,2) NULL COMMENT '薪资',
  join_date      DATE NULL COMMENT '入职日期',
  address        VARCHAR(256) NULL COMMENT '地址',
  ext_json       JSON NULL COMMENT '扩展信息JSON',
  -- BaseEntity 审计字段
  create_time    DATETIME NULL,
  update_time    DATETIME NULL,
  create_user_id BIGINT NULL,
  update_user_id BIGINT NULL,
  deleted_flag   TINYINT NOT NULL DEFAULT 0,
  version        INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_person_code (person_code),
  KEY idx_department_id (department_id),
  KEY idx_person_name (person_name),
  KEY idx_person_status (person_status),
  KEY idx_id_card (id_card)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一人员档案表';

-- 创建人员多凭证表
CREATE TABLE IF NOT EXISTS t_person_credential (
  credential_id    BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '凭证ID（主键）',
  person_id        BIGINT NOT NULL COMMENT '人员ID',
  credential_type  VARCHAR(32) NOT NULL COMMENT '凭证类型 CARD|BIOMETRIC|PASSWORD|QR_CODE',
  credential_value VARCHAR(256) NOT NULL COMMENT '凭证值',
  credential_name  VARCHAR(100) NULL COMMENT '凭证名称（便于识别）',
  effective_time   DATETIME NULL COMMENT '生效时间',
  expire_time      DATETIME NULL COMMENT '失效时间',
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  ext_json         JSON NULL COMMENT '扩展信息JSON',
  -- BaseEntity 审计字段
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user_id   BIGINT NULL,
  update_user_id   BIGINT NULL,
  deleted_flag     TINYINT NOT NULL DEFAULT 0,
  version          INT NOT NULL DEFAULT 0,
  INDEX idx_person_type (person_id, credential_type),
  INDEX idx_person_id (person_id),
  INDEX idx_credential_type (credential_type),
  UNIQUE KEY uk_person_type_value (person_id, credential_type, credential_value),
  CONSTRAINT fk_credential_person FOREIGN KEY (person_id) REFERENCES t_person_profile (person_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员多凭证管理表';

-- 2) 区域域统一：创建统一的区域信息表（基于门禁区域扩展）
CREATE TABLE IF NOT EXISTS t_area_info (
  area_id       BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID（主键）',
  area_code     VARCHAR(32) NOT NULL COMMENT '区域编码（系统唯一）',
  area_name     VARCHAR(100) NOT NULL COMMENT '区域名称',
  area_type     TINYINT NOT NULL DEFAULT 1 COMMENT '区域类型 1园区 2建筑 3楼层 4房间 5区域 6其他',
  parent_id     BIGINT DEFAULT 0 COMMENT '上级区域ID（0表示根区域）',
  path          VARCHAR(1024) NULL COMMENT '层级路径 /1/3/9',
  level         INT NULL COMMENT '层级深度（根区域为0）',
  path_hash     VARCHAR(64) NULL COMMENT '路径哈希（快速层级查询）',
  sort_order    INT DEFAULT 0 COMMENT '排序号（同层级排序）',
  description   VARCHAR(500) DEFAULT NULL COMMENT '区域描述',
  building_id   BIGINT DEFAULT NULL COMMENT '所在建筑ID',
  floor_id      BIGINT DEFAULT NULL COMMENT '所在楼层ID',
  area          DECIMAL(10,2) DEFAULT NULL COMMENT '区域面积（平方米）',
  capacity      INT DEFAULT NULL COMMENT '容纳人数',
  access_enabled TINYINT DEFAULT 1 COMMENT '是否启用门禁 0禁用 1启用',
  access_level  TINYINT DEFAULT 1 COMMENT '访问权限级别',
  longitude     DECIMAL(10,7) DEFAULT NULL COMMENT '经度坐标',
  latitude      DECIMAL(10,7) DEFAULT NULL COMMENT '纬度坐标',
  status        TINYINT DEFAULT 1 COMMENT '区域状态 0停用 1正常 2维护中',
  -- BaseEntity 审计字段
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user_id BIGINT NULL,
  update_user_id BIGINT NULL,
  deleted_flag  TINYINT NOT NULL DEFAULT 0,
  version       INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_area_code (area_code),
  KEY idx_parent_id (parent_id),
  KEY idx_path (path(255)),
  KEY idx_level (level),
  KEY idx_path_hash (path_hash),
  KEY idx_area_type (area_type),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一区域信息表';

-- 创建人员-区域授权表
CREATE TABLE IF NOT EXISTS t_area_person (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  area_id       BIGINT NOT NULL COMMENT '区域ID',
  person_id     BIGINT NOT NULL COMMENT '人员ID',
  data_scope    VARCHAR(32) NOT NULL DEFAULT 'AREA' COMMENT '数据域 AREA|DEPT|SELF|CUSTOM',
  permission_level TINYINT DEFAULT 1 COMMENT '权限级别',
  effective_time DATETIME NULL COMMENT '生效时间',
  expire_time   DATETIME NULL COMMENT '失效时间',
  status        TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  remark        VARCHAR(500) NULL COMMENT '备注',
  -- BaseEntity 审计字段
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user_id BIGINT NULL,
  update_user_id BIGINT NULL,
  deleted_flag  TINYINT NOT NULL DEFAULT 0,
  version       INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_area_person (area_id, person_id),
  INDEX idx_person_id (person_id),
  INDEX idx_area_id (area_id),
  INDEX idx_data_scope (data_scope),
  INDEX idx_status (status),
  CONSTRAINT fk_area_person_area FOREIGN KEY (area_id) REFERENCES t_area_info (area_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_area_person_person FOREIGN KEY (person_id) REFERENCES t_person_profile (person_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员-区域授权表';

-- 3) 统一RBAC权限模型
CREATE TABLE IF NOT EXISTS t_rbac_resource (
  resource_id   BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资源ID（主键）',
  resource_code VARCHAR(128) NOT NULL UNIQUE COMMENT '资源编码',
  resource_name VARCHAR(128) NOT NULL COMMENT '资源名称',
  resource_type VARCHAR(32) NOT NULL COMMENT '资源类型 API|MENU|BUTTON|DATA',
  module        VARCHAR(32) NOT NULL COMMENT '所属模块 ACCESS|ATTENDANCE|CONSUME|VIDEO|SYSTEM',
  description   VARCHAR(256) NULL COMMENT '资源描述',
  parent_id     BIGINT DEFAULT 0 COMMENT '父级资源ID',
  sort_order    INT DEFAULT 0 COMMENT '排序',
  status        TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user_id BIGINT NULL,
  update_user_id BIGINT NULL,
  deleted_flag  TINYINT NOT NULL DEFAULT 0,
  version       INT NOT NULL DEFAULT 0,
  KEY idx_parent_id (parent_id),
  KEY idx_resource_type (resource_type),
  KEY idx_module (module),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一资源表';

CREATE TABLE IF NOT EXISTS t_rbac_role (
  role_id     BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID（主键）',
  role_code   VARCHAR(64) NOT NULL UNIQUE COMMENT '角色编码',
  role_name   VARCHAR(128) NOT NULL COMMENT '角色名称',
  description VARCHAR(256) NULL COMMENT '角色描述',
  role_level  TINYINT DEFAULT 1 COMMENT '角色级别',
  status      TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user_id BIGINT NULL,
  update_user_id BIGINT NULL,
  deleted_flag  TINYINT NOT NULL DEFAULT 0,
  version      INT NOT NULL DEFAULT 0,
  KEY idx_role_level (role_level),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一角色表';

CREATE TABLE IF NOT EXISTS t_rbac_role_resource (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  role_id      BIGINT NOT NULL COMMENT '角色ID',
  resource_id  BIGINT NOT NULL COMMENT '资源ID',
  action       VARCHAR(64) NULL COMMENT '动作 READ|WRITE|DELETE|APPROVE|*',
  condition_json JSON NULL COMMENT 'RAC条件JSON',
  data_scope   VARCHAR(32) NULL COMMENT '数据域限制',
  status       TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  remark       VARCHAR(500) NULL COMMENT '备注',
  create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_user_id BIGINT NULL,
  update_user_id BIGINT NULL,
  deleted_flag TINYINT NOT NULL DEFAULT 0,
  version     INT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_res (role_id, resource_id, action),
  INDEX idx_role_id (role_id),
  INDEX idx_resource_id (resource_id),
  INDEX idx_action (action),
  INDEX idx_data_scope (data_scope),
  CONSTRAINT fk_rr_role FOREIGN KEY (role_id) REFERENCES t_rbac_role (role_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_rr_resource FOREIGN KEY (resource_id) REFERENCES t_rbac_resource (resource_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-资源权限表';

-- 4) 用户角色关联表
CREATE TABLE IF NOT EXISTS t_rbac_user_role (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id     BIGINT NOT NULL COMMENT '用户ID',
  role_id     BIGINT NOT NULL COMMENT '角色ID',
  effective_time DATETIME NULL COMMENT '生效时间',
  expire_time DATETIME NULL COMMENT '失效时间',
  status      TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_user_id BIGINT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  INDEX idx_user_id (user_id),
  INDEX idx_role_id (role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES t_person_profile (person_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES t_rbac_role (role_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- 5) 创建必要的索引
CREATE INDEX IF NOT EXISTS idx_person_credential_type ON t_person_credential(credential_type, status);
CREATE INDEX IF NOT EXISTS idx_area_person_composite ON t_area_person(area_id, data_scope, status);
CREATE INDEX IF NOT EXISTS idx_rbac_resource_composite ON t_rbac_resource(module, resource_type, status);
CREATE INDEX IF NOT EXISTS idx_rbac_role_resource_composite ON t_rbac_role_resource(role_id, action, status);

-- 6) 插入基础数据

-- 插入默认角色
INSERT INTO t_rbac_role (role_code, role_name, description, role_level, create_user_id) VALUES
('SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 99, 1),
('ADMIN', '管理员', '系统管理员，拥有大部分权限', 90, 1),
('ACCESS_ADMIN', '门禁管理员', '门禁系统管理员，负责门禁设备和区域管理', 50, 1),
('ATTENDANCE_ADMIN', '考勤管理员', '考勤系统管理员，负责考勤规则和数据处理', 50, 1),
('CONSUME_ADMIN', '消费管理员', '消费系统管理员，负责消费终端和财务管理', 50, 1),
('USER', '普通用户', '普通用户，拥有基本查看权限', 10, 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), description = VALUES(description);

-- 插入基础资源
INSERT INTO t_rbac_resource (resource_code, resource_name, resource_type, module, description, create_user_id) VALUES
-- 门禁模块
('access:area:view', '区域查看', 'DATA', 'ACCESS', '查看门禁区域信息', 1),
('access:area:manage', '区域管理', 'DATA', 'ACCESS', '管理门禁区域（增删改）', 1),
('access:device:view', '设备查看', 'DATA', 'ACCESS', '查看门禁设备信息', 1),
('access:device:control', '设备控制', 'API', 'ACCESS', '控制门禁设备（开门等操作）', 1),
('access:event:view', '事件查看', 'DATA', 'ACCESS', '查看门禁通行事件', 1),
('access:record:export', '记录导出', 'API', 'ACCESS', '导出门禁记录数据', 1),
-- 考勤模块
('attendance:record:view', '考勤查看', 'DATA', 'ATTENDANCE', '查看个人考勤记录', 1),
('attendance:record:manage', '考勤管理', 'DATA', 'ATTENDANCE', '管理考勤记录（增删改查）', 1),
('attendance:rule:manage', '规则管理', 'DATA', 'ATTENDANCE', '管理考勤规则和排班', 1),
('attendance:statistics:view', '统计查看', 'DATA', 'ATTENDANCE', '查看考勤统计报表', 1),
-- 消费模块
('consume:record:view', '消费记录', 'DATA', 'CONSUME', '查看消费记录', 1),
('consume:terminal:manage', '终端管理', 'DATA', 'CONSUME', '管理消费终端', 1),
('consume:account:manage', '账户管理', 'DATA', 'CONSUME', '管理用户账户和余额', 1),
('consume:statistics:view', '消费统计', 'DATA', 'CONSUME', '查看消费统计报表', 1),
-- 系统模块
('system:user:manage', '用户管理', 'DATA', 'SYSTEM', '管理系统用户', 1),
('system:role:manage', '角色管理', 'DATA', 'SYSTEM', '管理系统角色', 1),
('system:permission:manage', '权限管理', 'DATA', 'SYSTEM', '管理系统权限', 1)
ON DUPLICATE KEY UPDATE resource_name = VALUES(resource_name), description = VALUES(description);

-- 7) 创建用于路径哈希计算的函数
DELIMITER //
CREATE FUNCTION IF NOT EXISTS fn_area_path_hash(p_path VARCHAR(1024)) RETURNS VARCHAR(64)
READS SQL DATA
DETERMINISTIC
BEGIN
    RETURN SHA2(CONCAT('area_path_', p_path, COALESCE(p_path, '')), 256);
END //
DELIMITER ;

-- 8) 创建区域路径计算和更新的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS sp_update_area_path_and_hash(IN p_area_id BIGINT)
BEGIN
    DECLARE v_parent_id BIGINT DEFAULT 0;
    DECLARE v_parent_path VARCHAR(1024);
    DECLARE v_current_path VARCHAR(1024);
    DECLARE v_level INT DEFAULT 0;

    -- 获取父级信息
    SELECT parent_id INTO v_parent_id FROM t_area_info WHERE area_id = p_area_id AND deleted_flag = 0;

    -- 如果是根区域
    IF v_parent_id = 0 THEN
        SET v_current_path = CONCAT('/', p_area_id);
        SET v_level = 0;
    ELSE
        -- 获取父级路径
        SELECT path, level INTO v_parent_path, v_level FROM t_area_info WHERE area_id = v_parent_id AND deleted_flag = 0;

        IF v_parent_path IS NOT NULL THEN
            SET v_current_path = CONCAT(v_parent_path, '/', p_area_id);
            SET v_level = v_level + 1;
        ELSE
            SET v_current_path = CONCAT('/0/', p_area_id);
            SET v_level = 1;
        END IF;
    END IF;

    -- 更新当前区域
    UPDATE t_area_info
    SET path = v_current_path,
        level = v_level,
        path_hash = fn_area_path_hash(v_current_path)
    WHERE area_id = p_area_id;
END //
DELIMITER ;

-- 9) 批量更新现有区域的路径信息
-- 注意：如果已有区域数据，执行此存储过程更新路径
-- UPDATE t_area_info SET path = NULL, level = NULL, path_hash = NULL WHERE deleted_flag = 0;
-- 然后按层级顺序调用 sp_update_area_path_and_hash 更新每个区域

-- ====================================================================
-- 升级完成
-- ====================================================================

-- 输出升级完成信息
SELECT
    '人员区域权限升级脚本执行完成' AS message,
    NOW() AS execute_time,
    COUNT(*) AS total_tables_created
FROM information_schema.tables
WHERE table_schema = DATABASE()
AND table_name IN ('t_person_profile', 't_person_credential', 't_area_info', 't_area_person', 't_rbac_resource', 't_rbac_role', 't_rbac_role_resource', 't_rbac_user_role');