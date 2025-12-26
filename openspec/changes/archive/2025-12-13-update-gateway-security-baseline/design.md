# Design: Update Gateway Security Baseline

## Context

The gateway is the unified entry point for all HTTP traffic. It must provide a consistent security perimeter even if downstream services implement their own fine-grained authorization.

## Goals / Non-Goals

- Goals:
  - Enforce authentication at the gateway for all non-whitelisted paths.
  - Provide coarse-grained authorization (RBAC) as a first layer.
  - Ensure CORS configuration is safe-by-default and configuration-driven.
  - Provide consistent 401/403 JSON semantics.

- Non-Goals:
  - Replace downstream service authorization.
  - Introduce new business APIs.

## Decisions

- Decision: Use `GlobalFilter` for auth/RBAC.
  - Why: Works for Spring Cloud Gateway routing and covers forwarded requests.

- Decision: Keep Spring Security `anyExchange().permitAll()`.
  - Why: Spring Security remains an outer shell; the gateway filter is authoritative for request-level auth.

- Decision: CORS is driven by `cors.*` config with safe defaults.
  - Why: Avoid hard-coded wildcard origins and allow safe-by-default behavior.

## Risks / Trade-offs

- Risk: Coarse RBAC rules misconfigured can deny legitimate traffic.
  - Mitigation: `security.rbac.enabled=false` by default; only configured rules apply.

- Risk: Dual CORS (Security + globalcors) drift.
  - Mitigation: Align defaults and prefer `cors.*`; follow-up to fully unify.

## Migration Plan

- Already implemented in code.
- Future changes should update this change record and the `security-baseline` spec accordingly.

## Open Questions

- Should `globalcors` be removed entirely in the long term?
- Should gateway RBAC integrate with a centralized permission registry rather than static configuration?
