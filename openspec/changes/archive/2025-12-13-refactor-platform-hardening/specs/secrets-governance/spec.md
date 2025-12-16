## ADDED Requirements

### Requirement: No Default Secrets in Deployment Manifests
系统 SHALL 禁止在部署清单（Docker Compose / Kubernetes / scripts）中提供默认口令/默认密钥占位，敏感信息必须显式注入。

#### Scenario: Deployment requires explicit secrets
- **GIVEN** 使用 `docker-compose-all.yml` 或 Kubernetes manifests 部署
- **WHEN** 未提供 `MYSQL_ROOT_PASSWORD`/`REDIS_PASSWORD`/`NACOS_PASSWORD`/`JWT_SECRET`
- **THEN** 部署过程 SHALL 失败或服务启动 Fail-Fast，避免以默认弱口令运行

