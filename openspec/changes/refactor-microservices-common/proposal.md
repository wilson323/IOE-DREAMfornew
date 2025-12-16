# Change: Refactor microservices-common Module

## Why

`microservices-common` 模块当前包含325个Java文件，职责过于庞大，混合了安全、监控、业务公共等多种功能。这导致：
- 模块间依赖不清晰
- 各微服务被迫引入不需要的依赖
- 维护和演进困难

## What Changes

- **BREAKING**: 将 `microservices-common` 拆分为4个专门模块
  - `microservices-common-security`: 安全认证相关（62个文件）
  - `microservices-common-monitor`: 监控告警相关（49个文件）
  - `microservices-common-business`: 业务公共相关（74个文件）
  - `microservices-common-core`: 核心公共类（保留现有）
- 将业务专属代码迁移到对应微服务
  - `workflow` → `ioedream-oa-service`
  - `consume` → `ioedream-consume-service`
  - `visitor` → `ioedream-visitor-service`
- 更新所有微服务的依赖配置

## Impact

- Affected specs: microservices-architecture
- Affected code:
  - `microservices/microservices-common/` (325个文件)
  - `microservices/ioedream-*/pom.xml` (所有微服务POM)
  - `microservices/pom.xml` (父POM)
