## ADDED Requirements

### Requirement: No Ghost Common Modules
系统 SHALL 禁止出现“依赖声明引用了未纳入 Maven Reactor 的 common 子模块（幽灵模块）”的状态；任何依赖 `microservices-common-*` 的模块，必须满足“目录 + pom.xml + Reactor modules”三要素。

#### Scenario: Reject dependency on non-reactor common module
- **GIVEN** 开发者在任意服务的 `pom.xml` 中新增依赖 `microservices-common-xxx`
- **WHEN** 该 `microservices-common-xxx` 未在仓库中落地为 Maven Reactor 子模块
- **THEN** 构建/CI SHALL 失败并提示“幽灵模块依赖，必须先落盘模块或移除依赖”

### Requirement: Common-Core Purity Guardrail
系统 SHALL 保护 `microservices-common-core` 的“最小稳定内核”属性；新增代码不得引入 Spring/框架/业务域实现，且历史偏差必须以提案方式逐步剥离。

#### Scenario: Prevent new Spring dependencies in common-core
- **GIVEN** 开发者试图在 `microservices-common-core` 中新增 Spring/Starter 依赖或 Spring 组件类
- **WHEN** 代码进入评审/CI
- **THEN** CI/静态检查 SHALL 阻止合入并提示“core 仅允许最小稳定基元”


