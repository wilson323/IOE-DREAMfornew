## ADDED Requirements

### Requirement: No Default Secrets In Production
生产环境配置 SHALL 不允许默认管理员口令、默认 JWT secret、默认 Nacos 密码等敏感默认值。

#### Scenario: Startup fails when secret missing
- **GIVEN** 运行环境为 production
- **WHEN** 缺失 JWT secret（或使用默认值）
- **THEN** 服务 SHALL Fail-Fast 并拒绝启动

### Requirement: Anonymous Endpoints Are Whitelisted Explicitly
匿名可访问接口 SHALL 以白名单形式集中配置，并与网关路由/文档保持一致。

#### Scenario: Anonymous access to health endpoint
- **WHEN** 未登录访问 `/actuator/health`
- **THEN** 服务 SHALL 返回健康状态而非 401

