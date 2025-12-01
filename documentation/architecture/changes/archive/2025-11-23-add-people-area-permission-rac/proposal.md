# Change: 统一人员-区域-权限模型与RAC鉴权中间层（Phase A）

## Why
现有权限判定分散在各 Controller，数据权限（区域/部门/本人/自定义）口径不统一；区域层级索引缺失；人员-多凭证与人员-区域授权缺表，影响门禁/考勤/消费三大业务稳定性与可扩展性。

## What Changes
- 新增或补强数据模型与DDL：
  - `t_person_profile` 补强状态/脱敏/扩展JSON/删除标记
  - `t_person_credential` 人员多凭证
  - `t_area_info` 补强 `path`/`level`/`path_hash` 索引与约束
  - `t_area_person` 人员-区域授权（含数据域维度与有效期）
  - `t_rbac_resource` / `t_rbac_role` / `t_rbac_role_resource`
- 新增统一鉴权中间层（RAC+DataScope）：
  - `AuthorizationContext`、`PolicyEvaluator`、`DataScopeResolver`
  - 统一注解：`@RequireResource(code, scope)`
- 前端契约与指令：
  - 扩展 `v-permission` 支持数据域校验
  - 统一资源码常量与路由守卫
- 接入回归：门禁/考勤/消费各1个关键接口改造接入统一鉴权

## Impact
- Affected specs: people, area, rbac, auth-middleware, frontend-permission
- Affected code:
  - `smart-admin-api-java17-springboot3/sa-support`（新增中间层）
  - `smart-admin-api-java17-springboot3/sa-admin`（Controller接入注解）
  - `smart-admin-web-javascript/src/directives/permission.js`
  - 数据库脚本：`数据库SQL脚本/mysql/people_area_permission_upgrade.sql`


