# Security Baseline Comparison Template

## Purpose

This template standardizes the C-stage cross-service security audit and comparison.

- Use it to document **what is actually enforced** at the gateway and each downstream service.
- Mark risks as **P0/P1/P2**.
- Prefer linking to authoritative code/config locations (file paths + class names).

## How to Fill

- Add one row per microservice.
- For services that rely on `microservices-common` (Servlet), mark "inherits" and only note deviations.
- Keep the table short; attach detailed evidence below the table as bullet points.

## Field Definitions

- **Web stack**: `WebFlux` (gateway) or `Servlet`.
- **Auth enforcement**: where authentication is actually enforced (gateway filter vs service security chain).
- **Auth token type**: whether **ACCESS token only** is enforced for business routes.
- **RBAC/Authorization**: coarse gateway RBAC, method-level security, path rules, etc.
- **Identity propagation**: whether `X-User-*` headers are forwarded and consumed.
- **CORS**: single source of truth and safe defaults.
- **Actuator exposure**: prod `management.endpoints.web.exposure.include` and whether `/actuator/**` is public.
- **Global exception handling**: `@RestControllerAdvice` coverage and 401/403 JSON behavior.
- **Tracing / Response normalization**: `traceId` injection and response formatting filters.
- **S2S direct call**: client/server enablement and shared secret requirements.

---

## Baseline: Gateway (WebFlux) — `ioedream-gateway-service`

### Evidence anchors (authoritative)

- **Auth/RBAC filter**: `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/filter/JwtAuthenticationGlobalFilter.java`
- **WebFlux security shell**: `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/config/WebFluxSecurityConfig.java`
- **RBAC config model**: `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/config/RbacProperties.java`
- **Prod/Perf config**: `microservices/ioedream-gateway-service/src/main/resources/application-prod.yml`, `application-performance.yml`

### Behavior summary

- **Auth enforcement**: Gateway `GlobalFilter` is authoritative for request-level auth on non-whitelisted paths.
- **401/403 semantics**: Unified JSON response via `ResponseDTO`.
- **ACCESS token only**: Business routes require ACCESS token; refresh token used as access returns 401.
- **Coarse RBAC**: `security.rbac.enabled` + path-based rules; default disabled.
- **Identity propagation**: forwards `X-User-Id`, `X-User-Name`, `X-User-Roles`, `X-User-Permissions`.
- **CORS**: configuration-driven via `cors.*` with guardrails for `allowCredentials=true` + wildcard origins.

---

## Baseline: Servlet (Downstream services) — `microservices-common`

> Applies to services that include `microservices-common` and run as Servlet apps (most non-gateway microservices).

### Evidence anchors (Servlet, authoritative)

- **Security chain**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/auth/config/SecurityConfig.java`
- **JWT prod fail-fast**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/auth/config/JwtSecretFailFastValidator.java`
- **TraceId filter**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/tracing/TraceIdMdcFilter.java`
- **Response normalization**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/filter/ResponseFormatFilter.java`

### Behavior summary (Servlet SecurityConfig)

- **Method-level security**: enabled (`@EnableMethodSecurity(prePostEnabled=true, securedEnabled=true, jsr250Enabled=true)`).
- **Auth mechanism**: JWT Bearer via `Authorization: Bearer <token>`.
- **ACCESS token only**: only tokens passing `validateToken(token)` and `isAccessToken(token)` are accepted.
- **Default protection**: `anyRequest().authenticated()`.
- **Whitelist (permitAll)**:
  - `/actuator/**`
  - `/doc.html`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`
  - `/webjars/**`
  - `/favicon.ico`
- **401 JSON**: `ResponseDTO.error(401, "未登录或令牌无效")`.
- **403 JSON**: `ResponseDTO.error(403, "无权限访问")`.
- **CSRF**: disabled; **session**: stateless; **form/basic**: disabled.
- **CORS**: `http.cors(Customizer.withDefaults())` (depends on MVC CORS configuration availability).

### Production hardening (JwtSecretFailFastValidator)

- On `prod` profile, service startup fails if `security.jwt.secret` is missing or too short (< 32 bytes).

### Tracing & response normalization

- `TraceIdMdcFilter` injects `traceId` from `X-Trace-Id` header (Servlet).
- `ResponseFormatFilter` normalizes JSON content-type/charset behavior.

---

## Comparison Table (fill per microservice)

| Service | Web stack | Inherits common SecurityConfig | Auth enforcement | Auth token type | RBAC/Authorization | Identity propagation | CORS source | Actuator exposure (prod) | Exception handling | Trace/Response filters | S2S direct call | Test baseline | Risks (P0/P1) |
|---|---|---|---|---|---|---|---|---|---|---|---|---:|---|
| ioedream-gateway-service | WebFlux | N/A | `JwtAuthenticationGlobalFilter` | ACCESS only | coarse RBAC (`security.rbac.*`) | `X-User-*` forwarded | `cors.*` | `health,info,metrics,prometheus,gateway` | 401/403 JSON via `JwtAuthenticationGlobalFilter` | (not checked in this pass) | N/A | 3 | P1: `application-monitoring.yml` exposes `include: '*'` (risk if profile enabled) |
| ioedream-common-service | Servlet | Yes | `microservices-common` SecurityConfig (+ DirectCallAuthFilter if enabled) | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | (not checked in this pass) | `GlobalExceptionHandler` | TraceId/ResponseFormat | server-side (conditional `ioedream.direct-call.enabled=true`) | 17 | P1: downstream services lack unified controller advice |
| ioedream-consume-service | Servlet | Yes | inherits | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | `health,info,metrics,prometheus,env,configprops` | (none found) | TraceId/ResponseFormat | client (`DirectCallClientConfiguration`) | 32 | P0: actuator env/configprops exposed + `/actuator/**` permitAll (common); P1: no global exception advice |
| ioedream-access-service | Servlet | Yes | inherits | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | `health,info,metrics,prometheus,env,configprops` | (none found) | TraceId/ResponseFormat | none found | 10 | P0: actuator env/configprops exposed + `/actuator/**` permitAll (common); P1: no global exception advice |
| ioedream-attendance-service | Servlet | Yes | inherits | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | `health,info,metrics,prometheus,env,configprops` | (none found) | TraceId/ResponseFormat | none found | 15 | P0: actuator env/configprops exposed + `/actuator/**` permitAll (common); P1: no global exception advice |
| ioedream-device-comm-service | Servlet | Yes | inherits | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | `health,info,metrics,prometheus,env,configprops` | (none found) | TraceId/ResponseFormat | client (`DirectCallClientConfiguration`) | 11 | P0: actuator env/configprops exposed + `/actuator/**` permitAll (common); P1: no global exception advice |
| ioedream-video-service | Servlet | Yes | inherits | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | `health,info,metrics,prometheus,env,configprops` | (none found) | TraceId/ResponseFormat | none found | 5 | P0: actuator env/configprops exposed + `/actuator/**` permitAll (common); P1: no global exception advice |
| ioedream-visitor-service | Servlet | Yes | inherits | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | `health,info,metrics,prometheus,env,configprops` | (none found) | TraceId/ResponseFormat | none found | 8 | P0: actuator env/configprops exposed + `/actuator/**` permitAll (common); P1: no global exception advice |
| ioedream-oa-service | Servlet | Yes | inherits | ACCESS only | method security + app rules | no explicit `X-User-*` consumption found | MVC + Security default | `health,info,metrics,prometheus,env,configprops,activiti` | (none found) | TraceId/ResponseFormat | none found | 7 | P0: actuator env/configprops/activiti exposed + `/actuator/**` permitAll (common); P1: no global exception advice |

---

## Known cross-service risk patterns (to mark in the table)

- **P0**: `management.endpoints.web.exposure.include` contains `env,configprops` while `/actuator/**` is `permitAll`.
- **P1**: Missing `@RestControllerAdvice` in most business services (response/error semantics drift).
- **P1**: CORS configuration is not a single source of truth for downstream services (Security defaults depend on MVC CORS).
