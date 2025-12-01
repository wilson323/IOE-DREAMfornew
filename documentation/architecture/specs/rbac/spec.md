# rbac Specification

## Purpose
RBAC capability负责统一资源码注册、角色授权及基于资源-动作-条件（RAC）的策略评估，是所有业务模块复用的鉴权中间层。
## Requirements
### Requirement: 统一资源（RAC：资源-动作-条件）
系统 SHALL 以资源码为核心，支持 API/MENU/BUTTON/DATA 等类型统一注册，并以动作与条件表达策略。

#### Scenario: 基于资源码与动作鉴权
- WHEN 调用标注 `@RequireResource(code, scope)` 的接口
- THEN 鉴权中间层根据资源码与数据域解析策略并做出允许/拒绝判定
