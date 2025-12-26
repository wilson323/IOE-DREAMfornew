## 1. P0 - Gateway Whitelist Single Source
- [x] 在 `microservices/common-config/nacos/common-security.yaml` 增加 `security.whitelist.paths`
- [x] 网关 `WebFluxSecurityConfig` 读取 `security.whitelist.paths` 并作为白名单来源（保留最小静态兜底）
- [x] 产出并归档白名单清单（用于审计与回归）
-    产物：`openspec/changes/refactor-platform-hardening/artifacts/anon-whitelist-inventory.txt`

## 2. P0 - AuthZ Model Standardization
- [x] 统一 JWT 配置键：`security.jwt.*`（secret/expiration/refresh-expiration）
- [x] 统一授权语义：`permissions → hasAuthority`，角色仅作 `ROLE_` 补充
- [x] 生产环境 Fail-Fast：缺失/弱密钥直接启动失败（覆盖所有 Servlet 服务）

## 3. P0 - Secrets Governance
- [x] 清理 K8s/Docker/脚本中默认账号口令与默认密钥占位（禁止 `nacos/nacos`、`123456`、`redis123` 等默认值）
- [x] 文档同步：说明必须通过 `.env`/密钥系统注入

## 4. P1 - Split `microservices-common` Boundaries (Plan First)
- [x] 盘点 `microservices-common` 代码并分类：纯公共能力 vs 业务域能力
-    产物：`openspec/changes/refactor-platform-hardening/artifacts/microservices-common-package-inventory.txt`
- [x] 制定拆分迁移顺序与模块划分（最小可行子模块）
-    产物：`openspec/changes/refactor-platform-hardening/artifacts/common-refactor-migration-order.md`
- [x] 定义"禁止新增业务域代码进入 common"的准入规则与检查项（CI/静态检查）
-    产物：`openspec/changes/refactor-platform-hardening/artifacts/common-boundary-enforcement-rules.md`

## 5. P1 - Frontend TODO Closure (Plan First)
- [x] 生成 TODO 清单（页面 → TODO 点 → 期望 API → 权限点）
-    产物：`openspec/changes/refactor-platform-hardening/artifacts/frontend-todo-inventory.txt`
- [x] 确定优先域（建议：消费/门禁）并定义最小闭环验收标准
    验收：管理端“消费/门禁”关键页面不再依赖 mock/TODO，可完成端到端查询/删除/上下架等最小闭环
- [x] 输出分域迭代计划（每迭代可交付）
    当前已落地：消费（交易查询/统计、报表模板、账户列表/充值、设备列表、商品列表）、门禁（区域设备关联、区域权限列表/删除）

## 6. Validation
- [x] `mvn -f microservices/pom.xml -am test`
- [x] `mvn -f microservices/pom.xml -am verify`
- [x] 契约回归：/api/v1 与 legacy 兼容窗口验证
-    产物：`openspec/changes/refactor-platform-hardening/artifacts/maven-test-verify-guide.md`

## 7. Execution Record (for archive)
- [x] 7.1 Gateway whitelist single source implemented
- [x] 7.2 AuthZ model standardized (JWT config, permissions → hasAuthority)
- [x] 7.3 Secrets governance completed (no default passwords)
- [x] 7.4 Common boundaries split plan created
- [x] 7.5 Common boundary enforcement rules defined
- [x] 7.6 Frontend TODO closure plan completed
- [x] 7.7 Maven test/verify guide created
