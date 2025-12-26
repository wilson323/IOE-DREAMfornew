# Spec Delta: architecture-compliance

## ADDED Requirements

### Requirement: MapperScan Packages Must Exist

微服务启动类中的 `@MapperScan` 配置 SHALL 仅包含在代码库中真实存在的 DAO 包，禁止保留已迁移/不存在的包路径。

#### Scenario: Startup scans only existing DAO packages

 
- **WHEN** 应用启动并进行 MyBatis Mapper 扫描
- **THEN** 扫描项 SHALL 不包含不存在的包（例如 `net.lab1024.sa.common.hr.dao`）

### Requirement: Security DAO Scan Uses RBAC Package

当需要扫描权限相关 DAO 时，系统 SHALL 使用已存在的 `net.lab1024.sa.common.rbac.dao` 包而不是不存在的 `net.lab1024.sa.common.security.dao`。

#### Scenario: Service scans RBAC DAOs

 
- **WHEN** 服务启用 MyBatis Mapper 扫描
- **THEN** 扫描项 SHALL 包含 `net.lab1024.sa.common.rbac.dao`
