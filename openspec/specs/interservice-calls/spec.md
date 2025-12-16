# interservice-calls Specification

## Purpose
TBD - created by archiving change update-api-contract-security-tracing. Update Purpose after archive.
## Requirements
### Requirement: Inter-service Calls Use Canonical Paths
服务间调用客户端（Gateway/Direct）SHALL 使用 canonical API 路径，不得在客户端层重复拼接业务前缀导致路径错配。

#### Scenario: Device service calls consume API through gateway
- **WHEN** 设备通讯服务调用消费服务的 `/api/v1/consume/...` 接口
- **THEN** 请求路径 SHALL 与网关/消费服务的真实 Controller 路径一致

### Requirement: Direct Calls Are Guarded
直连调用 SHALL 受配置开关与 sharedSecret 保护，并仅允许白名单 serviceId 与 endpoint。

#### Scenario: Direct call disabled by default
- **WHEN** 未配置 sharedSecret 或 disabled
- **THEN** 直连调用 SHALL 被拒绝并返回明确错误码

