# observability-tracing Specification

## Purpose
TBD - created by archiving change update-api-contract-security-tracing. Update Purpose after archive.
## Requirements
### Requirement: TraceId Is Continuous Across Services
系统 SHALL 在入站/出站统一传播 traceId，确保跨服务请求在日志与监控中可关联。

#### Scenario: Inbound traceId propagated to outbound call
- **GIVEN** 入站请求包含 `X-Trace-Id`
- **WHEN** 服务发起服务间调用
- **THEN** 出站请求 SHALL 携带相同 `X-Trace-Id`

### Requirement: TraceId Is Generated When Missing
当入站请求缺少 traceId 时，系统 SHALL 生成新的 traceId 并写入 MDC，同时透传到下游调用。

#### Scenario: Generate traceId on first hop
- **WHEN** 入站请求不包含 `X-Trace-Id`
- **THEN** 服务 SHALL 生成 traceId 并用于该请求生命周期内的所有日志与下游调用

