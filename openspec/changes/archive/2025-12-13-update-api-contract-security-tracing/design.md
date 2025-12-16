# Design: API Contract, Gateway Routing, Security Baseline, Tracing

## Goals
- 用一条“可执行、可验证、可回滚”的路径把系统拉回企业级基线：
  - 端到端 API 调用可达（前端 → 网关 → 服务）
  - 服务间调用契约一致（经网关/直连）
  - 安全配置无默认值（Fail-Fast）
  - traceId 跨服务连续（入站/出站闭环）

## Non-Goals
- 不引入新业务能力
- 不进行大规模 SRP/模块拆分（另行 change 处理）

## Current Findings (code-based)
### 1) Contract mismatch
- 网关路由谓词以`/access/**`、`/consume/**`等为主，但各服务 Controller 大量使用`/api/v1/...`。
- 前端 API 调用前缀混用（`/api`、`/access`、`/api/v1`），导致与网关/服务的真实路径不一致。

### 2) Security baseline gaps
- 存在默认管理员口令、默认 JWT secret、默认 Nacos 密码等配置，存在上线即暴露的风险。

### 3) Tracing gap
- 已存在 `X-Trace-Id` 出站注入（Gateway/Direct client），但缺少统一入站落 MDC 的 Filter/Interceptor。

## Decision 1: Canonical API prefix
### Option A (recommended): `/api/v1` as canonical
- 优点：服务端已有大量`/api/v1/...`，改动最小；更符合版本化 API 管理。
- 缺点：需要网关与前端全面对齐，并为旧前缀做兼容。

### Option B: `/access/**` etc as canonical
- 优点：网关当前配置相对贴近该模式。
- 缺点：需要大规模修改服务端 Controller 与服务间调用路径；改动更大。

**选择**：Option A。

## Decision 2: Compatibility strategy
- 网关层提供旧前缀兼容：
  - 例如将`/access/**`重写为`/api/v1/access/**`（或反向，取决于 canonical）。
- 兼容窗口明确；到期删除旧入口，并在发布说明中告知。

## Decision 3: Inter-service calls
### GatewayServiceClient
当前实现存在“再拼一层服务前缀”的风险。设计目标：
- 只允许调用“canonical path”（如`/api/v1/...`），避免再拼 `/access` 等二级前缀造成错配。
- 维持低复杂度：在客户端只做 header 注入与通用 error handling，不引入过度抽象。

### DirectServiceClient
保留“热路径直连”能力，但必须：
- 显式开关 + sharedSecret（Fail-Fast）
- 统一签名/trace 头
- 仅允许白名单 serviceId + endpoint（由配置定义）

## Decision 4: Security baseline
### 要求
- 所有敏感配置（管理员口令、JWT secret、Nacos 密码、Jasypt password、第三方密钥）：
  - 生产环境不得有默认值
  - 未配置则启动失败（Fail-Fast）
- 鉴权体系收敛：
  - 需要在实施阶段确认“以 Sa-Token 为主”或“以 Spring Security 为主”，避免双栈并存且无统一认证链。

## Decision 5: Tracing propagation
### Header standard
短期：
- 使用 `X-Trace-Id`（与现有实现一致）
中期：
- 兼容 W3C `traceparent`（可选）

### Inbound/Outbound
- 入站：Filter/Interceptor 读取 `X-Trace-Id`（不存在则生成），写入 MDC（key: `traceId`）
- 出站：统一从 MDC 取 `traceId` 写入 header

## Verification Strategy
- 契约校验（静态）：
  - 从 Java Controller 注解提取路径清单
  - 从网关配置提取 Path predicates / rewrite rules
  - 从前端 api 模块提取调用路径前缀
  - 三者差异应为 0（或仅存在白名单兼容项）
- 回归测试（最小可行）：
  - 网关路由的关键路径 smoke tests
  - 关键服务的鉴权/匿名白名单 tests
  - traceId 传递 tests（入站→出站）

