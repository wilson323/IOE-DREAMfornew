-- 人员·区域·权限 升级脚本（仅供评审与演练，生产执行需评估窗口与备份）

-- 1) 人员域补强
ALTER TABLE t_person_profile
  ADD COLUMN IF NOT EXISTS person_status TINYINT NOT NULL DEFAULT 1 COMMENT '0黑名单,1在职,2停用,3离职',
  ADD COLUMN IF NOT EXISTS id_masked VARCHAR(128) NULL COMMENT '证件号脱敏',
  ADD COLUMN IF NOT EXISTS ext_json JSON NULL,
  ADD COLUMN IF NOT EXISTS deleted_flag TINYINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_person_status ON t_person_profile(person_status);

CREATE TABLE IF NOT EXISTS t_person_credential (
  credential_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  person_id        BIGINT NOT NULL,
  credential_type  VARCHAR(32) NOT NULL,
  credential_value VARCHAR(256) NOT NULL,
  effective_time   DATETIME NULL,
  expire_time      DATETIME NULL,
  status           TINYINT NOT NULL DEFAULT 1,
  ext_json         JSON NULL,
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_flag     TINYINT NOT NULL DEFAULT 0,
  INDEX idx_person_type(person_id, credential_type),
  UNIQUE KEY uk_person_type_value(person_id, credential_type, credential_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员多凭证';

-- 2) 区域域补强
ALTER TABLE t_area_info
  ADD COLUMN IF NOT EXISTS path VARCHAR(1024) NULL,
  ADD COLUMN IF NOT EXISTS level INT NULL,
  ADD COLUMN IF NOT EXISTS path_hash VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS deleted_flag TINYINT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_area_path_hash ON t_area_info(path_hash);
CREATE INDEX IF NOT EXISTS idx_area_level ON t_area_info(level);

CREATE TABLE IF NOT EXISTS t_area_person (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  area_id       BIGINT NOT NULL,
  person_id     BIGINT NOT NULL,
  data_scope    VARCHAR(32) NOT NULL DEFAULT 'AREA',
  effective_time DATETIME NULL,
  expire_time    DATETIME NULL,
  status        TINYINT NOT NULL DEFAULT 1,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_flag  TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_area_person(area_id, person_id),
  INDEX idx_person(person_id),
  INDEX idx_area(area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员-区域授权';

-- 3) 权限域统一（RBAC/RAC）
CREATE TABLE IF NOT EXISTS t_rbac_resource (
  resource_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  resource_code VARCHAR(128) NOT NULL UNIQUE,
  resource_type VARCHAR(32) NOT NULL,
  description   VARCHAR(256) NULL,
  create_time   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一资源';

CREATE TABLE IF NOT EXISTS t_rbac_role (
  role_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code   VARCHAR(64) NOT NULL UNIQUE,
  role_name   VARCHAR(128) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE IF NOT EXISTS t_rbac_role_resource (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id      BIGINT NOT NULL,
  resource_id  BIGINT NOT NULL,
  action       VARCHAR(64) NULL,
  condition_json JSON NULL,
  UNIQUE KEY uk_role_res(role_id, resource_id, action),
  INDEX idx_role(role_id),
  INDEX idx_res(resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-资源';

-- 4) 初始化基础RBAC数据
-- 初始化角色数据
INSERT IGNORE INTO t_rbac_role (role_code, role_name) VALUES
('SUPER_ADMIN', '超级管理员'),
('AREA_ADMIN', '区域管理员'),
('ACCESS_ADMIN', '门禁管理员'),
('ATTENDANCE_ADMIN', '考勤管理员'),
('CONSUME_ADMIN', '消费管理员'),
('VIDEO_ADMIN', '视频管理员'),
('HR_ADMIN', '人事管理员'),
('FINANCE_ADMIN', '财务管理员'),
('SECURITY_OFFICER', '安保人员'),
('EMPLOYEE', '普通员工');

-- 初始化资源数据
INSERT IGNORE INTO t_rbac_resource (resource_code, resource_type, description) VALUES
-- 门禁模块资源
('access:device:view', 'API', '查看门禁设备'),
('access:device:manage', 'API', '管理门禁设备'),
('access:area:view', 'API', '查看门禁区域'),
('access:area:manage', 'API', '管理门禁区域'),
('access:record:view', 'API', '查看门禁记录'),
('access:record:approve', 'API', '审批门禁记录'),
('access:permission:view', 'API', '查看门禁权限'),
('access:permission:manage', 'API', '管理门禁权限'),
('access:visitor:view', 'API', '查看访客记录'),
('access:visitor:manage', 'API', '管理访客记录'),

-- 考勤模块资源
('attendance:record:view', 'API', '查看考勤记录'),
('attendance:record:manage', 'API', '管理考勤记录'),
('attendance:exception:view', 'API', '查看考勤异常'),
('attendance:exception:approve', 'API', '审批考勤异常'),
('attendance:rule:view', 'API', '查看考勤规则'),
('attendance:rule:manage', 'API', '管理考勤规则'),
('attendance:statistics:view', 'API', '查看考勤统计'),
('attendance:report:export', 'API', '导出考勤报表'),

-- 消费模块资源
('consume:record:view', 'API', '查看消费记录'),
('consume:terminal:view', 'API', '查看消费终端'),
('consume:terminal:manage', 'API', '管理消费终端'),
('consume:account:view', 'API', '查看消费账户'),
('consume:account:manage', 'API', '管理消费账户'),
('consume:recharge:view', 'API', '查看充值记录'),
('consume:recharge:manage', 'API', '管理充值记录'),
('consume:statistics:view', 'API', '查看消费统计'),

-- 视频监控模块资源
('video:device:view', 'API', '查看监控设备'),
('video:device:manage', 'API', '管理监控设备'),
('video:record:view', 'API', '查看监控记录'),
('video:record:download', 'API', '下载监控录像'),
('video:live:view', 'API', '查看实时监控'),
('video:alarm:view', 'API', '查看监控告警'),

-- 系统管理资源
('system:user:view', 'API', '查看用户'),
('system:user:manage', 'API', '管理用户'),
('system:role:view', 'API', '查看角色'),
('system:role:manage', 'API', '管理角色'),
('system:department:view', 'API', '查看部门'),
('system:department:manage', 'API', '管理部门'),
('system:config:view', 'API', '查看系统配置'),
('system:config:manage', 'API', '管理系统配置'),

-- 菜单资源
('menu:access', 'MENU', '门禁管理'),
('menu:attendance', 'MENU', '考勤管理'),
('menu:consume', 'MENU', '消费管理'),
('menu:video', 'MENU', '视频监控'),
('menu:system', 'MENU', '系统管理'),
('menu:dashboard', 'MENU', '控制面板'),
('menu:report', 'MENU', '报表中心');

-- 5) 初始化基础角色-资源映射
-- 超级管理员拥有所有权限
INSERT IGNORE INTO t_rbac_role_resource (role_id, resource_id, action, condition_json)
SELECT r.role_id, res.resource_id, '*', NULL
FROM t_rbac_role r, t_rbac_resource res
WHERE r.role_code = 'SUPER_ADMIN';

-- 普通员工基础权限
INSERT IGNORE INTO t_rbac_role_resource (role_id, resource_id, action, condition_json)
SELECT r.role_id, res.resource_id, 'READ', '{"dataScope": "SELF"}'
FROM t_rbac_role r, t_rbac_resource res
WHERE r.role_code = 'EMPLOYEE' AND res.resource_type = 'API';

-- 6) 验证脚本执行
SELECT '人员域表创建验证' as verification_step,
       COUNT(*) as person_profile_count FROM t_person_profile
UNION ALL
SELECT '人员凭证表创建验证',
       COUNT(*) as count FROM t_person_credential
UNION ALL
SELECT '区域表创建验证',
       COUNT(*) as count FROM t_area_info
UNION ALL
SELECT '人员-区域授权表创建验证',
       COUNT(*) as count FROM t_area_person
UNION ALL
SELECT 'RBAC资源表创建验证',
       COUNT(*) as count FROM t_rbac_resource
UNION ALL
SELECT 'RBAC角色表创建验证',
       COUNT(*) as count FROM t_rbac_role
UNION ALL
SELECT 'RBAC角色-资源表创建验证',
       COUNT(*) as count FROM t_rbac_role_resource;

-- 7) 完成提示
SELECT '人员-区域-权限升级脚本执行完成' as status,
       NOW() as completed_time;

