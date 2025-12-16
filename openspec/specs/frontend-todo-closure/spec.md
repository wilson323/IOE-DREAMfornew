# frontend-todo-closure Specification

## Purpose
TBD - created by archiving change refactor-platform-hardening. Update Purpose after archive.
## Requirements
### Requirement: Frontend TODO Must Be Closed for Release
系统 SHALL 在发布前关闭核心业务页面中“未对接后端接口”的 TODO（按业务域分批验收）。

#### Scenario: Consume domain minimal closure
- **GIVEN** 消费域页面存在 TODO 标记
- **WHEN** 进入发布候选（RC）
- **THEN** 消费域最小闭环（列表/详情/创建/状态变更/导出） SHALL 可端到端验证通过

