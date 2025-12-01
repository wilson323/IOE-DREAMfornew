<!-- fd138286-36ae-4ada-bb22-80b9c817d7ac df7873c6-d627-4408-b542-a29a5b5a6d15 -->
# 人员·区域·权限 全局差距梳理与落地计划

## 范围与目标

- 模块范围：
- 人员域：`t_person_profile`、人员属性/状态、关联生物识别模板/卡片/部门岗位
- 区域域：`t_area_info`、`t_area_device`、层级/路径、边界与跨域策略
- 权限域：RBAC（角色/资源）、数据权限（区域/部门/人员）、业务权限（门禁/考勤/消费的授权契约）
- 目标：形成“差距清单 → 高优先级修复 → 接口/DDL/文档/测试”闭环，统一规范、避免冗余。

## 现状快速结论（基于已阅文档与代码结构）

- 人员域：
- 缺口：人员扩展档案、证件脱敏策略、状态机（在职/离职/黑名单）与联动清理；人员与多凭证（卡/生物模板/密码）统一映射；人员-区域授权快照。
- 区域域：
- 缺口：区域层级校验、祖先路径冗余存储与索引、跨区域数据权限边界；区域-设备、区域-人员一致性校验与清理任务。
- 权限域：
- 缺口：资源模型统一（API、菜单、数据域），资源-动作-条件（RAC）规范；数据权限粒度（区域/部门/本人/自定义）未统一；统一鉴权中间层缺失（目前分散在各 Controller）。

## 待实现清单（差距 → 交付物）

1) 统一数据模型与DDL补强（高优先级）

- 新/修表：
- `t_person_profile` 增加状态、证件脱敏、扩展JSON、删除标记
- `t_person_credential`（多凭证表：CARD/BIOMETRIC/PASSWORD）
- `t_area_info` 增加 `path`、`level`、`path_hash` 索引及约束
- `t_area_person` 建立人员-区域授权表（含有效期、数据权限维度）
- `t_rbac_role`、`t_rbac_resource`、`t_rbac_role_resource` 标准化字段与唯一约束
- 文档：`docs/DATA_DICTIONARY.md` 更新；新增迁移脚本说明

2) 统一权限契约与鉴权中间层（高优先级）

- 在 `sa-support` 增加：
- `AuthorizationContext`（登录用户、区域/部门/角色/数据域）
- `PolicyEvaluator`（RAC：资源-动作-条件）
- `DataScopeResolver`（区域/部门/本人/自定义）
- 在 `sa-admin` 提供统一注解：`@RequireResource(code="area:view", scope=DEPT | AREA | SELF)`
- 文档：`docs/ARCHITECTURE_STANDARDS.md` 增补RAC、数据权限约定

3) 区域层级一致性与索引（中优先级）

- 维护触发器/应用层服务：保持 `path`、`level`、`path_hash` 一致并回填
- 异常修复工具：扫描环路、悬挂节点、越级绑定

4) 人员域状态机与联动（中优先级）

- 状态：在职/离职/停用/黑名单；联动：自动吊销角色/数据权限/门禁授权
- 审计与追踪：记录联动变更

5) 资源模型清理与统一（中优先级）

- 统一 API/菜单/按钮 的资源编码命名与层级；冗余清理与迁移表

6) 前端契约与指令（中优先级）

- 指令：`v-permission` 扩展支持数据域校验；
- 统一资源码常量枚举与路由守卫；
- 文档：前端 `src/constants/permission.ts` 与用法指南

## 分阶段落地（两周节奏）

- Phase A（本周期）：
- 完成 1) DDL补强、2) 鉴权中间层 与 6) 前端契约
- 回归门禁/考勤/消费 3条关键接口，替换为统一鉴权
- Phase B（下周期）：
- 完成 3) 区域一致性、4) 人员状态机、5) 资源清理

## 关键文件（涉及修改）

- 后端：
- `sa-base`：公共实体/注解/工具（不改动业务）
- `sa-support`：新增鉴权中间层（AuthorizationContext/PolicyEvaluator/DataScopeResolver）
- `sa-admin`：人员/区域/权限 Controller 与 Service 接入统一鉴权注解
- 前端：
- `smart-admin-web-javascript/src/directives/permission.js`
- `smart-admin-web-javascript/src/constants/permission.js`
- 文档：
- `docs/DATA_DICTIONARY.md`、`docs/ARCHITECTURE_STANDARDS.md`、`docs/COMMON_MODULES/*`

## 验收标准

- 架构：Controller不直接做权限判断；统一注解与中间层覆盖≥90%接口
- 数据：区域层级正确率100%，新增/更新自动维护；人员状态联动验证通过
- 安全：RAC策略测试覆盖率≥80%，关键路径无越权
- 文档：数据字典与架构标准同步更新

### To-dos

- [ ] 抽取人员/区域/权限当前模型与文档差距清单（列DDL）
- [ ] 编制DDL迁移脚本与回滚方案（不执行，仅产物）
- [ ] 设计统一鉴权中间层与注解（RAC+DataScope）
- [ ] 前端权限指令/常量统一，补充用法文档
- [ ] 选3个关键接口接入统一鉴权（门禁/考勤/消费各1）
- [ ] 区域层级一致性维护与巡检方案
- [ ] 人员状态机与联动撤销策略
- [ ] 资源模型清理与迁移映射清单