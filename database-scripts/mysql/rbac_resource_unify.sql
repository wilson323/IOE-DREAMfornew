-- 资源模型清理与统一（RBAC/RAC最小可用建表与迁移映射）
-- 说明：
-- 1) 统一资源表、角色表、角色-资源关系表
-- 2) 预留 action 与 condition_json 字段支持 RAC
-- 3) 可在此基础上编写从历史表的迁移映射 INSERT...SELECT

CREATE TABLE IF NOT EXISTS t_rbac_resource (
  resource_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  resource_code  VARCHAR(128) NOT NULL UNIQUE,
  resource_type  VARCHAR(32)  NOT NULL COMMENT 'API|MENU|BUTTON|DATA',
  description    VARCHAR(256) NULL,
  create_time    DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一资源';

CREATE TABLE IF NOT EXISTS t_rbac_role (
  role_id     BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code   VARCHAR(64)  NOT NULL UNIQUE,
  role_name   VARCHAR(128) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE IF NOT EXISTS t_rbac_role_resource (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id       BIGINT NOT NULL,
  resource_id   BIGINT NOT NULL,
  action        VARCHAR(64) NULL COMMENT 'READ|WRITE|DELETE|APPROVE|*',
  condition_json JSON NULL COMMENT 'RAC条件（区域/部门/本人/自定义）',
  UNIQUE KEY uk_role_res(role_id, resource_id, action),
  INDEX idx_role(role_id),
  INDEX idx_res(resource_id),
  CONSTRAINT fk_rr_role  FOREIGN KEY (role_id) REFERENCES t_rbac_role(role_id),
  CONSTRAINT fk_rr_res   FOREIGN KEY (resource_id) REFERENCES t_rbac_resource(resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-资源';

-- 示例：历史映射（按实际历史表替换）
-- INSERT INTO t_rbac_resource(resource_code, resource_type, description)
-- SELECT DISTINCT api_perm AS resource_code, 'API' AS resource_type, NULL
-- FROM legacy_api_perm;

-- INSERT INTO t_rbac_role(role_code, role_name)
-- SELECT DISTINCT role_code, role_name FROM legacy_role;

-- INSERT INTO t_rbac_role_resource(role_id, resource_id, action)
-- SELECT r.role_id, res.resource_id, lp.action
-- FROM legacy_perm lp
-- JOIN t_rbac_role r ON r.role_code = lp.role_code
-- JOIN t_rbac_resource res ON res.resource_code = lp.resource_code;


