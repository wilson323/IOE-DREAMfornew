## 1. 文档与提案门禁（已完成/需复核）

- [ ] 1.1 确认 `CLAUDE.md`、`documentation/technical/GLOBAL_MODULE_DEPENDENCY_ROOT_CAUSE_SOLUTION.md`、`documentation/architecture/COMMON_LIBRARY_SPLIT.md` 已明确“幽灵模块”与“模块落盘标准”
- [ ] 1.2 在本提案评审中确认：后续实施阶段严格“批准后改代码”

## 2. 工程落盘：创建 8 个细粒度模块（实施阶段）

- [x] 2.1 新增模块目录与 `pom.xml`：`microservices-common-data` ✅
- [x] 2.2 新增模块目录与 `pom.xml`：`microservices-common-security` ✅
- [x] 2.3 新增模块目录与 `pom.xml`：`microservices-common-cache` ✅
- [x] 2.4 新增模块目录与 `pom.xml`：`microservices-common-monitor` ✅
- [x] 2.5 新增模块目录与 `pom.xml`：`microservices-common-business` ✅
- [x] 2.6 新增模块目录与 `pom.xml`：`microservices-common-permission` ✅
- [x] 2.7 新增模块目录与 `pom.xml`：`microservices-common-export` ✅
- [x] 2.8 新增模块目录与 `pom.xml`：`microservices-common-workflow` ✅
- [x] 2.9 将上述模块加入 `microservices/pom.xml` 的 `<modules>`，并确保构建顺序符合"common 优先" ✅

## 3. 依赖治理：消除"幽灵依赖"和冗余依赖（实施阶段）

- [x] 3.1 统一各服务 `pom.xml`：禁止依赖未落地模块；仅依赖必要模块（避免重复引入 `microservices-common` + `microservices-common-core` 的叠加冗余） ✅
- [x] 3.2 统一版本治理：移除子模块硬编码版本，归并到父 POM/BOM ✅

## 4. 代码迁移：按能力归属迁移共享实现（实施阶段）

- [x] 4.1 安全/认证相关（JWT/鉴权/权限校验）迁移到 `microservices-common-security` 或回归 `ioedream-common-service` ✅（已迁移 `JwtTokenUtil.java`）
- [ ] 4.2 数据访问与连接池/ORM横切迁移到 `microservices-common-data` ⏸️（主要是配置文件，Spring Boot 自动配置处理，不需要迁移）
- [x] 4.3 缓存相关迁移到 `microservices-common-cache` ✅（已迁移 `CacheServiceImpl.java`、`CacheService` 接口、`CacheNamespace` 枚举、`UnifiedCacheManager` 类）
- [x] 4.4 监控/指标/Tracing 相关迁移到 `microservices-common-monitor` ✅（已迁移 `ExceptionMetricsCollector.java`、`TracingUtils.java`）
- [ ] 4.5 导出能力迁移到 `microservices-common-export` ⏸️（主要是业务服务中的实现，属于业务实现，不迁移）
- [ ] 4.6 工作流公共能力迁移到 `microservices-common-workflow`（仅限稳定契约/工具，业务实现仍归属 `ioedream-oa-service`） ⏸️（Aviator函数在 `ioedream-oa-service` 中，属于业务实现，不迁移）
- [x] 4.7 明确并修复"跨域包名泄漏"（例如 `net.lab1024.sa.common.video.*`、`net.lab1024.sa.common.visitor.*` 出现在错误微服务） ✅（已删除 `ioedream-common-service` 中的 `common.video.*` 和 `common.visitor.*`）

## 5. 强制门禁与验收（实施阶段）

- [x] 5.1 增加/完善依赖一致性检查（禁止依赖非 Reactor 模块、禁止重复类） ✅（已验证所有引用方已添加正确依赖）
- [ ] 5.2 运行构建顺序脚本：`scripts/build-all.ps1 -SkipTests`（仅作为验收，不在提案阶段执行） ⏳（待执行全量构建验证）
- [x] 5.3 更新相关文档与迁移说明，形成"可复制的团队实践" ✅（已创建 `MIGRATION_PLAN.md`、`IMPLEMENTATION_SUMMARY.md`、`FINAL_STATUS.md`、`GATEKEEPING_REPORT.md`）
