# Change: Update API Contract, Security Baseline, and Tracing

## Why
当前 IOE-DREAM 在“代码层面”的企业级落地存在 P0 级风险，主要集中在 **API 契约断裂**、**安全默认值**、**可观测性断链** 三类问题，已导致：

- **可用性风险（P0）**：网关路由前缀（`/access/**`等）与各业务服务 Controller 实际前缀（大量`/api/v1/...`）以及前端调用前缀（混用`/api`、`/access`）不一致，导致请求无法正确路由/频繁 404。
- **安全风险（P0）**：存在默认口令/默认 JWT secret/默认 Nacos 密码等上线即踩雷配置，无法满足企业级与合规要求。
- **排障效率风险（P1）**：链路追踪仅有出站 `X-Trace-Id` 注入，缺少入站统一落 MDC 的闭环；跨服务 traceId 断裂，影响故障定位与审计追踪。

本变更目标是 **在不引入无谓复杂度的前提下（KISS/YAGNI）**，用一套明确的“契约 + 兼容策略 + 验证门禁”把系统拉回可运维、可演进的企业级基线。

## What Changes
- **API 契约统一**：定义并落地唯一外部/内部 API 前缀规范（建议：`/api/v1/...`），并提供有限期兼容入口（旧前缀在网关层做重写/转发）。
- **网关路由对齐**：网关路由谓词、重写规则、Swagger/安全白名单与“真实 Controller 路径”一致。
- **服务间调用对齐**：`GatewayServiceClient`/`DirectServiceClient` 的路径拼接与 API 契约一致，避免“二次前缀拼接”。
- **安全基线收敛**：移除所有“默认口令/默认 secret/默认 Nacos 密码”；生产环境缺失敏感配置应显式失败（Fail-Fast）。
- **链路追踪闭环**：统一 `X-Trace-Id` 作为短期头（可选兼容 W3C traceparent），入站统一注入 MDC，出站统一透传，确保跨服务一致。
- **回归验证**：补齐“API 契约/网关路由/鉴权”关键回归用例，避免整改反复。

## Scope
### In scope (P0/P1)
- `ioedream-gateway-service`：路由谓词、重写规则、与安全配置的白名单对齐
- 全部业务微服务：Controller 路径与网关/前端契约对齐（含历史`/api/...`路径的清退与兼容策略）
- `microservices-common`：服务间调用客户端（Gateway/Direct）、traceId 传播工具、（如需）统一入站 Filter/Interceptor 的公共实现
- 前端（管理端 + 移动端）：API 调用前缀统一、移除硬编码示例 baseURL

### Out of scope (deferred)
- 大规模 SRP 拆分与 `microservices-common` 多模块拆分（已在 `refactor-srp-and-global-consistency` 覆盖，作为后续阶段）
- 业务功能新增/改动（本变更只做契约与基线治理，不做新业务）

## Assumptions / Decisions Needed
1. **唯一 API 前缀**：以`/api/v1`作为唯一对外契约；旧前缀在网关做兼容（默认建议）。
2. **兼容窗口**：旧前缀兼容保留周期（建议 2 个迭代或 30 天），到期删除。
3. **鉴权体系收敛方向**：Sa-Token vs Spring Security（建议明确一套主体系；其余仅做边缘适配或移除）。

若以上任一项需调整，本提案将以“配置优先 + 可回滚”的方式落地。

## Impact
- Affected capabilities:
  - `api-contract`
  - `gateway-routing`
  - `interservice-calls`
  - `security-baseline`
  - `observability-tracing`
- Breaking changes:
  - 若客户端直接调用旧前缀且不走网关兼容，将产生破坏性影响；因此需要兼容窗口与灰度发布策略。

## Risks & Mitigations
- **风险**：路径重写/兼容导致路由冲突
  - **缓解**：制定“路由优先级与谓词互斥”规则，提供自动化契约校验脚本/测试。
- **风险**：安全配置 Fail-Fast 影响本地开发
  - **缓解**：开发环境允许显式设置本地 `.env`/启动参数，生产环境不允许默认值。

## Relationship to Existing Changes
- 依赖/关联：
  - `fix-critical-architecture-violations`（安全风险与架构违规治理方向一致）
  - `refactor-srp-and-global-consistency`（内部调用治理/直连白名单与观测要求一致）
- 本变更不重复 SRP 大拆分，仅聚焦“契约对齐 + 安全底线 + tracing 闭环”。

