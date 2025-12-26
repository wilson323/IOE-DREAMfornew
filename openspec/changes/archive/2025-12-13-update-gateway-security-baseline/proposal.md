# Change: Update Gateway Security Baseline (Auth/RBAC/CORS)

## Why

当前 `ioedream-gateway-service` 是所有请求的统一入口。

在未显式实施“网关统一鉴权 + 粗粒度 RBAC + CORS 单一真源”前，存在以下 P0/P1 风险：

- **P0：网关入口默认放行**
  - Spring Security 的 `anyExchange().permitAll()` 仅能作为路由网关的“安全外壳”，如果缺少网关侧统一鉴权过滤器，可能导致业务 API 在网关层不被拦截。

- **P0：鉴权失败缺少统一返回与身份透传**
  - 缺少统一的 401/403 语义与 JSON 返回，会导致前端/移动端处理不一致。
  - 缺少身份信息透传（例如 `X-User-Id`）会导致下游需要重复解析 token 或无法关联审计。

- **P0/P1：CORS 双源配置与高风险组合**
  - 同时存在 Spring Security CORS 与 Spring Cloud Gateway `globalcors` 配置，容易发生冲突与漂移。
  - `allowCredentials=true` 与 `*` 组合属于高风险配置，容易放大 CSRF/跨域滥用风险。

本变更的目标是**将已经落地的实现**在 OpenSpec 中形成可审计的安全基线记录与验证清单，确保后续演进不回退。

## What Changes

- **网关统一鉴权（401）**
  - 新增 `GlobalFilter` 强制校验 `Authorization: Bearer <JWT>`（兼容 `token` 头）。
  - 只允许 `ACCESS` token 访问业务接口，刷新令牌访问业务接口返回 401。
  - 鉴权失败返回统一 JSON（`ResponseDTO`）。

- **网关粗粒度 RBAC（403）**
  - 新增 `security.rbac` 配置模型（默认 `enabled=false`），仅对配置的路径生效。
  - 当命中规则且用户缺少 required roles/permissions 时返回 403。

- **身份透传**
  - 将 `userId/username/roles/permissions` 透传到下游 Header（`X-User-Id` / `X-User-Name` / `X-User-Roles` / `X-User-Permissions`）。

- **CORS 单一真源与安全兜底**
  - Spring Security CORS 改为配置驱动（`cors.*`），并将 `CorsConfigurationSource` 以单例 Bean 注入到 `SecurityWebFilterChain`。
  - 兼容 `cors.*` 以逗号字符串形式配置。
  - 当 `allowCredentials=true` 且允许 `*` 时，自动降级关闭 credentials 并输出 warn。
  - 生产/性能配置中 `spring.cloud.gateway.globalcors` 的默认 `allowedOriginPatterns` 不再为 `*`。

## Impact

- Affected capabilities (delta specs in this change):
  - `security-baseline`

- Affected code:
  - `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/filter/JwtAuthenticationGlobalFilter.java`
  - `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/config/RbacProperties.java`
  - `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/config/WebFluxSecurityConfig.java`
  - `microservices/ioedream-gateway-service/src/main/resources/application-prod.yml`
  - `microservices/ioedream-gateway-service/src/main/resources/application-performance.yml`

- Breaking changes:
  - 对外 API 路径不变。
  - 未登录访问受保护路径将返回 401（属于安全行为变更，预期）。

## Verification

- `mvn -f microservices/pom.xml -pl ioedream-gateway-service -am test` 通过

## Follow-ups

- 进一步收敛 Spring Cloud Gateway `globalcors` 与 Spring Security CORS 的配置来源（长期目标：以 `cors.*` 为单一真源）。
- 下游服务与审计系统基于透传 Header 统一落库与追踪。
