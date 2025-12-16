## ADDED Requirements

### Requirement: Canonical API Prefix
系统 SHALL 以单一 canonical API 前缀对外提供所有业务接口，默认采用 `/api/v1`。

#### Scenario: Client calls canonical endpoint
- **WHEN** 前端调用 `/api/v1/access/device/query`
- **THEN** 请求 SHALL 通过网关路由到门禁服务并返回成功响应

### Requirement: Legacy Prefix Compatibility
系统 SHALL 在兼容窗口内支持旧前缀调用，并在网关层完成重写/转发，不要求业务服务同时维护两套 Controller 路径。

#### Scenario: Client calls legacy endpoint during compatibility window
- **WHEN** 客户端调用旧前缀（例如 `/access/**`）
- **THEN** 网关 SHALL 将其重写/转发到 canonical `/api/v1/...` 并返回等价响应

## MODIFIED Requirements
（无）

## REMOVED Requirements
（无）

