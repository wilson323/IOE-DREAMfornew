# 人员·区域·权限 差距清单与DDL建议（第一版）

> 目的：统一三大基础域的数据模型与权限契约，支撑后续统一鉴权与数据域控制。

## 1. 差距摘要

- 人员域缺口：
  - 人员状态机（在职/离职/停用/黑名单）未固化；
  - 多凭证（卡/生物模板/密码）缺统一映射表；
  - 证件脱敏策略与审计字段不完整。
- 区域域缺口：
  - 层级/path 约束、祖先路径索引缺失；
  - 区域-设备、区域-人员一致性缺巡检与回填。
- 权限域缺口：
  - 资源-动作-条件（RAC）模型未统一；
  - 数据域（区域/部门/本人/自定义）策略分散在各 Controller。

## 2. 数据模型与DDL（建议）

### 2.1 人员域
```sql
-- 人员主档补强
ALTER TABLE t_person_profile
  ADD COLUMN person_status TINYINT NOT NULL DEFAULT 1 COMMENT '0-黑名单,1-在职,2-停用,3-离职',
  ADD COLUMN id_masked VARCHAR(128) COMMENT '证件号脱敏',
  ADD COLUMN ext_json JSON NULL COMMENT '扩展信息',
  ADD COLUMN deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '软删除';

CREATE INDEX idx_person_status ON t_person_profile(person_status);

-- 人员多凭证
CREATE TABLE IF NOT EXISTS t_person_credential (
  credential_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
  person_id        BIGINT NOT NULL,
  credential_type  VARCHAR(32) NOT NULL COMMENT 'CARD|BIOMETRIC|PASSWORD',
  credential_value VARCHAR(256) NOT NULL,
  effective_time   DATETIME NULL,
  expire_time      DATETIME NULL,
  status           TINYINT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
  ext_json         JSON NULL,
  create_time      DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_flag     TINYINT NOT NULL DEFAULT 0,
  INDEX idx_person_type(person_id, credential_type),
  UNIQUE KEY uk_person_type_value(person_id, credential_type, credential_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员多凭证';
```

### 2.2 区域域
```sql
-- 区域表补强：层级/路径/路径哈希
ALTER TABLE t_area_info
  ADD COLUMN path VARCHAR(1024) NULL COMMENT '/1/3/9',
  ADD COLUMN level INT NULL,
  ADD COLUMN path_hash VARCHAR(64) NULL,
  ADD COLUMN deleted_flag TINYINT NOT NULL DEFAULT 0;

CREATE INDEX idx_area_path_hash ON t_area_info(path_hash);
CREATE INDEX idx_area_level ON t_area_info(level);

-- 人员-区域授权（数据域维度）
CREATE TABLE IF NOT EXISTS t_area_person (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  area_id       BIGINT NOT NULL,
  person_id     BIGINT NOT NULL,
  data_scope    VARCHAR(32) NOT NULL DEFAULT 'AREA' COMMENT 'AREA|DEPT|SELF|CUSTOM',
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
```

### 2.3 权限域（RBAC/RAC）
```sql
-- 统一资源与角色关系（示例）
CREATE TABLE IF NOT EXISTS t_rbac_resource (
  resource_id   BIGINT PRIMARY KEY AUTO_INCREMENT,
  resource_code VARCHAR(128) NOT NULL UNIQUE,
  resource_type VARCHAR(32) NOT NULL COMMENT 'API|MENU|BUTTON|DATA',
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
  action       VARCHAR(64) NULL COMMENT 'READ|WRITE|DELETE|APPROVE|*',
  condition_json JSON NULL COMMENT 'RAC条件',
  UNIQUE KEY uk_role_res(role_id, resource_id, action),
  INDEX idx_role(role_id),
  INDEX idx_res(resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-资源';
```

## 3. 巡检与回填建议
- 区域：定期计算/回填 `path`、`level`、`path_hash`；扫描环路与孤立节点；
- 人员：离职/停用联动吊销角色/数据域/门禁授权；
- 权限：RAC策略抽样回归，保底 READ/WRITE 黑白名单校验。

## 4. 风险与回滚
- 所有 DDL 需灰度执行，提供回滚脚本（见 mysql/people_area_permission_rollback.sql）；
- 生产前对大表新增列需评估锁/时间窗口，优先在线DDL方案。

