## 1. 数据模型与DDL（高优先级）
- [x] 1.1 复核现状与差距清单对齐 `docs/IMPLEMENTATION_PLAN/people-area-permission-gap.md`
- [x] 1.2 完成并校验迁移脚本 `数据库SQL脚本/mysql/people_area_permission_upgrade.sql`
- [x] 1.3 更新数据字典 `docs/DATA_DICTIONARY.md`

## 2. 鉴权中间层（高优先级）
- [x] 2.1 设计 `AuthorizationContext/PolicyEvaluator/DataScopeResolver`
- [x] 2.2 定义注解 `@RequireResource(code, scope)` 与拦截器
- [x] 2.3 接入门禁/考勤/消费各1个核心接口

## 3. 前端契约（中优先级）
- [x] 3.1 扩展 `v-permission` 支持数据域校验
- [x] 3.2 统一资源码常量与路由守卫
- [x] 3.3 文档与用法示例

## 4. 验证与文档
- [x] 4.1 单元与集成测试（RAC策略覆盖率≥85%）
- [x] 4.2 更新 `docs/ARCHITECTURE_STANDARDS.md`（RAC+DataScope约定）
- [x] 4.3 OpenSpec 验证与归档