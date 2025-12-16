# common-boundaries Specification

## Purpose
TBD - created by archiving change refactor-platform-hardening. Update Purpose after archive.
## Requirements
### Requirement: Common Module Boundary
系统 SHALL 将 `microservices-common` 的“纯公共能力”与“业务域能力”在模块边界上隔离，避免业务域代码在公共库中持续膨胀。

#### Scenario: Prevent business-domain leakage
- **GIVEN** 开发者新增门禁/考勤/消费等业务域 DAO/Manager/Entity
- **WHEN** 代码试图提交到 `microservices-common`
- **THEN** CI/静态检查 SHALL 阻止合入并提示迁移到对应微服务

