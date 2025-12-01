# 人员·区域·权限 差距清单（代码 vs 文档）

本清单用于快速校验当前实现与规范文档的对齐情况，并给出对应交付物指引。

## 1. 人员域
- [x] `t_person_profile` 含 `person_status/extra_json/deleted_flag` 与索引（见 `mysql/person_state_machine.sql`）
- [x] 人员离职/停用联动撤销参考语句（角色/数据域/门禁授权），含巡检 SQL（同上）
- [ ] 人员多凭证表 `t_person_credential`（CARD/BIOMETRIC/PASSWORD）建设与落地

## 2. 区域域
- [x] `t_area_info` 补强 `path/level/path_hash/deleted_flag` 与索引（见 `docs/IMPLEMENTATION_PLAN/people-area-permission-gap.md` 与 `mysql/area_hierarchy_maintenance.sql`）
- [x] 区域层级回填与巡检（孤立/环路/重复哈希/残缺数据）
- [ ] 区域-设备/区域-人员一致性巡检与修复脚本

## 3. 权限域（RBAC/RAC）
- [x] 统一资源与角色关系表（`t_rbac_resource/t_rbac_role/t_rbac_role_resource`），预留 `action/condition_json`（见 `mysql/rbac_resource_unify.sql`）
- [x] 统一鉴权注解与切面：`@RequireResource` + `RequireResourceAspect` + `PolicyEvaluator`（`sa-support` 模块）
- [x] 关键接口接入统一鉴权：门禁/生物识别/设备各 1（`access:verify` / `biometric:verify` / `device:control`）
- [ ] 历史资源/角色迁移映射（INSERT...SELECT），按生产数据表落地

## 4. 前端契约
- [x] 指令路径修正：`v-permission` 使用 `'/@/store/modules/permission-store'`
- [ ] 统一资源码常量与用法文档（`src/constants/permission.js` 与 README）

## 5. 交付物索引
- SQL：
  - `数据库SQL脚本/mysql/person_state_machine.sql`
  - `数据库SQL脚本/mysql/area_hierarchy_maintenance.sql`
  - `数据库SQL脚本/mysql/rbac_resource_unify.sql`
- 文档：
  - `docs/IMPLEMENTATION_PLAN/people-area-permission-gap.md`
  - 本文件（差距清单）

## 6. 下一步建议
1) 落地 `t_person_credential` 与区域-设备/区域-人员一致性辅助脚本；
2) 编写历史资源迁移映射 SQL（按生产历史表）；
3) 前端资源码常量与路由守卫统一，并补充用法说明。


