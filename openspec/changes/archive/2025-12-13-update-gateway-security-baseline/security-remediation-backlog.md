# Security Remediation Backlog (C-stage Findings)

> Scope: derived from cross-service security comparison and baseline extraction.
> This file is **documentation only** (no implementation).

## References

- Comparison template: `openspec/changes/update-gateway-security-baseline/security-baseline-comparison-template.md`
- Servlet baseline: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/auth/config/SecurityConfig.java`
- Gateway baseline: `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/filter/JwtAuthenticationGlobalFilter.java`

---

## P0 (must address first)

### P0-1: Actuator sensitive endpoints exposed while `/actuator/**` is permitAll

- **Problem**
  - Multiple services set `management.endpoints.web.exposure.include` to include `env,configprops` (OA additionally exposes `activiti`).
  - Servlet security baseline permits `/actuator/**` without authentication.
  - This combination can leak sensitive runtime configuration.

- **Affected services**
  - `ioedream-consume-service`
  - `ioedream-access-service`
  - `ioedream-attendance-service`
  - `ioedream-device-comm-service`
  - `ioedream-video-service`
  - `ioedream-visitor-service`
  - `ioedream-oa-service`

- **Recommended remediation (choose one strategy and apply consistently)**
  - **Strategy A (config-side minimization)**
    - Remove `env,configprops` (and `activiti`) from prod exposure; keep a minimal set such as `health,info,prometheus,metrics`.
  - **Strategy B (security-side restriction)**
    - Narrow `/actuator/**` whitelist in `microservices-common` to only `health` (and optionally liveness/readiness endpoints).
    - Require auth (or internal-only access) for other actuator endpoints.

- **Verification**
  - Unauthenticated request to `/actuator/env` and `/actuator/configprops` returns 401/403.
  - `/actuator/health` continues to work as intended.

---

## P1 (important, schedule next)

### P1-1: Missing unified global exception advice in most business services

- **Problem**
  - Only `ioedream-common-service` has a known `GlobalExceptionHandler`.
  - Other services do not show `@RestControllerAdvice/@ControllerAdvice` in code scan.

- **Affected services**
  - `ioedream-consume-service`, `ioedream-access-service`, `ioedream-attendance-service`, `ioedream-device-comm-service`, `ioedream-video-service`, `ioedream-visitor-service`, `ioedream-oa-service`

- **Recommended remediation**
  - Provide a shared, minimal exception advice (either duplicated per service or moved to a common module) to normalize:
    - validation errors
    - business exceptions
    - unknown exceptions
  - Keep 401/403 semantics aligned with gateway and Servlet security.

- **Verification**
  - A forced runtime exception returns a consistent `ResponseDTO` envelope across services.

### P1-2: Gateway monitoring profile may expose all actuator endpoints

- **Problem**
  - `ioedream-gateway-service` has `application-monitoring.yml` with `management.endpoints.web.exposure.include: '*'`.
  - If that profile is enabled in production, exposure expands significantly.

- **Recommended remediation**
  - Ensure monitoring profile is not enabled in production, or scope it to a safe allowlist.

- **Verification**
  - Production effective config does not expose `*`.

### P1-3: Identity propagation is forwarded but no downstream consumption found

- **Problem**
  - Gateway forwards `X-User-*` headers.
  - No downstream code was found explicitly consuming these headers.

- **Recommended remediation**
  - Decide whether downstream should:
    - rely exclusively on JWT parsing in each service, or
    - use forwarded headers as a trusted context (requires trust boundary definition + tamper resistance).

- **Verification**
  - Documented decision and at least one service demonstrates the intended consumption path.

### P1-4: Lint/compat debt (deferred by policy, track here)

- **Problem**
  - Gateway code has null-type-safety warnings.
  - Gateway `application-prod.yml` has deprecated/unknown properties warnings.

- **Recommended remediation**
  - Address warnings in a dedicated cleanup change after security remediation scope is agreed.

---

## Notes

- This backlog is intentionally implementation-agnostic.
- Apply OpenSpec for any remediation that changes runtime security behavior.
