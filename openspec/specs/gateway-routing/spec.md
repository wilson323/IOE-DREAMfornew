# gateway-routing Specification

## Purpose
TBD - created by archiving change update-api-contract-security-tracing. Update Purpose after archive.
## Requirements
### Requirement: Gateway Routes Match Canonical API
网关 SHALL 以 canonical API 前缀定义路由谓词，并确保路由与服务端 Controller 路径一致。

#### Scenario: Route access API to access service
- **WHEN** 请求路径匹配 `/api/v1/access/**`
- **THEN** 网关 SHALL 路由到 `lb://ioedream-access-service`

### Requirement: Compatibility Routes Are Explicit
兼容路由 SHALL 显式配置（可审计），并具备可关闭开关与到期清理计划。

#### Scenario: Disable legacy route after migration
- **GIVEN** 兼容窗口结束
- **WHEN** 关闭兼容路由开关
- **THEN** 旧前缀请求 SHALL 返回 404 或明确错误

