# Spec Delta: common-library-boundaries

## ADDED Requirements

### Requirement: Workflow Ownership Is OA
工作流能力（流程定义/实例/任务管理）SHALL 由 `ioedream-oa-service` 作为唯一 Provider，对外通过网关路由 `/api/v1/workflow/**` 提供服务。

#### Scenario: Gateway routes workflow requests to OA
- **WHEN** 网关收到 `/api/v1/workflow/**` 请求
- **THEN** 请求 SHALL 路由到 `ioedream-oa-service`

## MODIFIED Requirements

### Requirement: Common Library Avoids Non-Existent Domain Modules
`microservices-common` SHALL 不包含已废弃或不存在的业务域模块引用；公共配置（如 `@MapperScan`）不得引用不存在的包路径。

#### Scenario: Common-related configuration does not reference removed domains
- **WHEN** 开发者维护公共扫描/引用配置
- **THEN** 配置 SHALL 不包含 `hr/document/file/security.dao` 等不存在模块
