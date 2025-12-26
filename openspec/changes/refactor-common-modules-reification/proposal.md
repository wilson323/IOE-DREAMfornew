# Change: 落地并治理细粒度 `microservices-common-*` 模块（消除幽灵依赖，收敛公共库边界）

## Why

当前仓库存在“细粒度模块名在 `pom.xml` 中被大量引用，但工程上未落地为真实 Maven 子模块”的状态，导致构建对本地/CI 缓存高度敏感、团队对公共库边界产生持续误解，属于 P0 级不稳定根因。

本提案以“文档优先、提案先行、审批后实施”为硬门禁，根因级修复该不稳定状态，并为后续公共库拆分与边界治理提供可持续的工程骨架。

## What Changes

- **落地 8 个细粒度公共模块为真实 Maven Reactor 子模块**（目录 + `pom.xml` + `microservices/pom.xml` `<modules>` 声明）：
  - `microservices-common-data`
  - `microservices-common-security`
  - `microservices-common-cache`
  - `microservices-common-monitor`
  - `microservices-common-business`
  - `microservices-common-permission`
  - `microservices-common-export`
  - `microservices-common-workflow`
- **明确每个模块的职责边界与依赖方向**（禁止环依赖，禁止领域实现回流到底层）。
- **逐步迁移/归并现有公共代码**：将当前散落在 `microservices-common` 或错误位置的共享实现，按“能力归属”迁移到对应模块或回归到拥有者微服务。
- **新增强制门禁**（构建/CI/检查脚本）：禁止依赖未纳入 Reactor 的“幽灵模块”，禁止向 `microservices-common-core` 增加 Spring/业务域代码。

## Impact

- **Affected specs**:
  - `openspec/specs/common-boundaries/spec.md`（本变更提供 delta，新增“幽灵模块依赖门禁”与“模块落盘标准”要求）
- **Affected docs**:
  - `CLAUDE.md`
  - `documentation/technical/GLOBAL_MODULE_DEPENDENCY_ROOT_CAUSE_SOLUTION.md`
  - `documentation/architecture/COMMON_LIBRARY_SPLIT.md`
- **Affected code (planned, after approval)**:
  - `microservices/pom.xml`（Reactor modules）
  - `microservices/microservices-common/pom.xml` 与各业务服务 `pom.xml`
  - 新增 `microservices/microservices-common-*/` 模块目录与源码迁移

**BREAKING（可能）**：

- 若某些环境此前依赖本地缓存的旧 jar 通过编译，本提案实施后将以“Reactor 为准”强制对齐；需要一次性修复所有引用路径与依赖声明（提案内提供迁移顺序与回滚策略）。
