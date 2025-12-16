## ADDED Requirements

### Requirement: Single Source of Anonymous Paths
系统 SHALL 在网关集中维护匿名/白名单接口清单，并以配置驱动方式下发。

#### Scenario: Canonical whitelist from configuration
- **GIVEN** `security.whitelist.paths` 已配置
- **WHEN** 请求命中白名单路径
- **THEN** 网关 SHALL 允许匿名访问且不要求 token

#### Scenario: Non-whitelisted path requires authentication
- **GIVEN** 请求路径不在白名单中
- **WHEN** 客户端未携带 `Authorization: Bearer <token>`
- **THEN** 网关或下游服务 SHALL 返回 401

### Requirement: Authorization via permissions
系统 SHALL 将 JWT 的 `permissions` claim 映射为 Spring Security `GrantedAuthority`，并以 `hasAuthority` 进行授权校验。

#### Scenario: Permission-based access control
- **GIVEN** token 中包含 `permissions=["attendance:offline:sync"]`
- **WHEN** 访问受保护接口并声明 `@PreAuthorize("hasAuthority('attendance:offline:sync')")`
- **THEN** 请求 SHALL 被允许

