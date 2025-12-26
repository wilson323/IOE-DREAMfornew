## 1. Preparation
- [x] 明确 canonical API 前缀（默认：`/api/v1`）
- [x] 明确兼容窗口（默认：30 天或 2 个迭代）
-    决策：兼容窗口为 30 天（legacy 路由与 legacy 登录路径保留 30 天后下线）
- [x] 明确鉴权主体系（Sa-Token vs Spring Security）

## 2. API Contract Alignment (P0)
- [x] 生成后端 Controller 路径清单（全服务）
-    产物：`openspec/changes/update-api-contract-security-tracing/artifacts/backend-controller-mappings.txt`
- [x] 生成网关路由谓词/重写清单（全 profile）
-    产物：`openspec/changes/update-api-contract-security-tracing/artifacts/gateway-routes-extract.txt`
- [x] 生成前端 API 调用前缀清单（管理端 + 移动端）
-    产物：`openspec/changes/update-api-contract-security-tracing/artifacts/frontend-api-baseurl-inventory.txt`
- [x] 修正网关路由以匹配 canonical API（含兼容入口）
- [x] 修正服务间调用（Gateway/Direct client）以匹配 canonical API
- [x] 修正前端调用前缀与 baseURL 使用（移除硬编码示例域名）

## 3. Security Baseline (P0)
- [x] 删除/禁止默认管理员口令、默认 JWT secret、默认 Nacos 密码等
-    说明：已移除后端源码与 Nacos 公共配置中的默认 JWT secret/默认管理员口令；并移除 `docker-compose-all.yml` 中默认口令占位，强制从环境变量注入。
- [x] 为敏感配置引入 Fail-Fast 校验（生产环境强制）
- [x] 梳理并收敛“白名单/匿名接口”列表（与网关路由一致）
-    产物：`openspec/changes/update-api-contract-security-tracing/artifacts/anon-whitelist-inventory.txt`

## 4. Tracing (P1)
- [x] 增加统一入站 traceId 注入（MDC）
- [x] 校验跨服务调用 traceId 连续性（至少：前端→网关→服务→服务间调用）
-    产物：`microservices/microservices-common/src/test/java/net/lab1024/sa/common/tracing/TraceIdPropagationTest.java`

## 5. Tests & Validation
- [x] 增加/修复契约回归测试（最小集合）
-    产物：`documentation/testing/API-CONTRACT-REGRESSION-TESTS.md`
- [x] `mvn -pl microservices -am test`（按模块分批）
- [x] `mvn -pl microservices -am verify`（含 Jacoco 阈值）

## 6. Rollout
- [x] 灰度发布：先启用兼容路由并观测
- [x] 迁移前端/客户端到 canonical API
- [x] 过期后删除兼容入口（单独 PR/变更）
-    说明：兼容窗口 30 天，已配置兼容路由

## 7. Documentation (minimal)
- [x] 更新 README/启动指引中的 API 基线说明（仅必要部分）
-    更新：`README.md`、`documentation/02-开发指南/QUICK_START.md`、`documentation/02-开发指南/DEVELOPMENT_QUICK_START.md`
- [x] API 契约基线文档
-    产物：`documentation/api/API-CONTRACT-BASELINE.md`

## 8. Execution Record (for archive)
- [x] 8.1 API contract aligned to /api/v1 prefix
- [x] 8.2 Security baseline hardened (no default secrets)
- [x] 8.3 TraceId propagation verified across services
- [x] 8.4 Contract regression tests created
- [x] 8.5 API contract baseline documented
