## MODIFIED Requirements

### Requirement: 服务间调用规范
所有外部（南北向）请求 SHALL 通过 API 网关统一接入与治理。  
内部（东西向）同步调用 SHALL 采用混合策略：
1. 低频或跨域粗粒度同步调用 SHALL 经网关（`GatewayServiceClient`）。
2. 同域高频/低延迟/强一致热路径 MAY 直连，但 MUST 使用统一直连 Client，并满足超时、重试（有上限）、熔断、限流、fallback、指标、Tracing 与服务到服务鉴权；直连调用 MUST 在白名单中声明。

#### Scenario: 高频热路径直连
- **WHEN** 识别到同域高频热路径同步调用
- **THEN** 调用通过白名单直连 Client 执行
- **AND** 统一韧性与观测生效

#### Scenario: 非白名单直连拦截
- **WHEN** 提交包含非白名单直连的代码
- **THEN** 架构扫描与代码审查 SHALL 拒绝该提交
