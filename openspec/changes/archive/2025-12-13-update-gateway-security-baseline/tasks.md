# Tasks

## 1. Scope confirmation

- [x] 1.1 Confirm gateway security baseline scope (Auth/RBAC/CORS)

## 2. Implementation record

- [x] 2.1 Gateway auth filter enforces JWT for non-whitelisted routes
- [x] 2.2 Only ACCESS token is accepted for business routes
- [x] 2.3 Coarse RBAC enabled via `security.rbac.enabled` and path-based rules
- [x] 2.4 Identity headers are forwarded downstream

## 3. CORS baseline

- [x] 3.1 Spring Security CORS is configuration-driven (`cors.*`) and uses singleton `CorsConfigurationSource`
- [x] 3.2 Guardrail: `allowCredentials=true` with wildcard origins is downgraded
- [x] 3.3 Production/performance `globalcors` default no longer uses `*`

## 4. Verification

- [x] 4.1 `mvn -f microservices/pom.xml -pl ioedream-gateway-service -am test` succeeded

## 5. Follow-ups

- [x] 5.1 Decide long-term single-source for CORS (prefer `cors.*`, minimize `globalcors`)
-    决策：使用 `cors.*` 作为单一来源，`globalcors` 仅用于兜底
- [x] 5.2 Add minimal integration tests for 401/403 behaviors (gateway webtestclient)
-    产物：`microservices/ioedream-gateway-service/src/test/java/net/lab1024/sa/gateway/filter/GatewaySecurityIntegrationTest.java`
- [x] 5.3 Document recommended `security.rbac.rules` examples for prod
-    产物：`documentation/security/RBAC-Rules-Examples.md`

## 6. Execution Record (for archive)

- [x] 6.1 Gateway auth filter enforces JWT for non-whitelisted routes
- [x] 6.2 Coarse RBAC enabled via path-based rules
- [x] 6.3 CORS baseline hardened (no wildcard with credentials)
- [x] 6.4 401/403 integration tests created
- [x] 6.5 RBAC rules examples documented
