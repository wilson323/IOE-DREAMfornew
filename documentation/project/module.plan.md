# 模块工作计划（去重版）

## 范围与目标

对齐 OpenSpec 变更 add-people-area-permission-rac（Phase A），聚焦统一人员-区域-权限模型与 RAC 鉴权中间层，确保升级到 Spring Boot 3.x 的 Jakarta 规范全局一致。

## 验收标准

- 代码可编译运行（Java 17 + Spring Boot 3.x）
- EE 命名空间 javax.* 为 0（JDK 标准库白名单除外）
- 统一鉴权中间层设计文档与接入示例完成
- 差距清单与 DDL 草案产物齐备

## To-dos

### Phase A（本周期）

- [x] 抽取人员/区域/权限当前模型与文档差距清单（列 DDL）
- [x] 编制 DDL 迁移脚本与回滚方案（仅产物）
- [x] 设计统一鉴权中间层与注解（RAC + DataScope）
- [x] 前端权限指令/常量统一，补充用法文档
- [x] 选 3 个关键接口接入统一鉴权（门禁/考勤/消费各 1）

### Phase B（下周期）

- [ ] 区域层级一致性维护与巡检方案（path/level/path_hash）
- [ ] 人员状态机与联动撤销策略（在职/离职/停用/黑名单）
- [ ] 资源模型清理与迁移映射清单（API/菜单/按钮统一编码）

### 跨模块梳理（一次性）

- [x] 梳理门禁/考勤/消费模块规范与代码差距，输出缺失清单
- [x] 汇总交叉注意事项与统一改造项

## 产物定位

- DDL 草案：数据库SQL脚本/mysql/people_area_permission_upgrade.sql
- 差距清单：docs/IMPLEMENTATION_PLAN/people-area-permission-gap.md
- OpenSpec：openspec/changes/add-people-area-permission-rac/
- 迁移规范：CLAUDE.md（全局 Jakarta 规则）
