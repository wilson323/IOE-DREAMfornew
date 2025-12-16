# Change: Refactor for SRP and Global Consistency

## Why
当前代码与配置整体已达到“可运行+规范化”的基线，但存在影响企业级长期演进的系统性技术债：

1. **超大类/多职责聚合**：`PaymentService`、`DeviceSyncService`、`AlertManager`、`UnifiedAuthenticationManager` 等类体量极大（1k–4k+ 行），职责混杂（编排/协议/SDK/DAO/缓存/签名/通知），违反单一职责原则，导致：
   - 变更半径大、回归风险高
   - 单测困难、可读性/可维护性下降
   - 质量工具（PMD/Sonar）噪声与误报上升

2. **公共库“共享单体化”趋势**：`microservices-common` 承载大量跨域逻辑与依赖，服务间耦合被放大，任何公共变更都会波及全局。

3. **依赖与构建一致性漂移**：
   - 子模块存在硬编码版本覆盖父 POM（如 MySQL 版本不一致）
   - `build.gradle` 与 Maven 父 POM 并存，形成第二真相源
   - Gateway（WebFlux）引入阻塞型 DB 依赖，潜在运行时风险

4. **内部调用的容量/韧性压力**：全量经网关同步调用是当前架构约束，需要在不破坏约束的前提下补齐超时/熔断/降级与观测要求。

本变更旨在在不改变现有业务能力与外部 API 的前提下，重塑内部结构，使项目严格遵循 `.cursorrules`/`CLAUDE.md`，实现全局一致、低冗余、企业级高质量。

## What Changes
- **SRP 重构**：按“编排层（Service/Manager）+ 适配层（Adapter/Strategy/Client）+ 领域组件（Domain）”拆解超大类，收敛职责边界。
- **公共库边界收敛**：先按有界上下文重整包结构，再逐步拆分为多 JAR 子模块（阶段性落地）。
- **依赖治理**：消除子模块版本覆盖与冗余依赖；Gateway 依赖瘦身为纯反应式网关定位。
- **构建单一真相源**：明确 Maven 为唯一构建入口，冻结或移除 Gradle 配置并在父 POM 引入强制版本/依赖校验。
- **内部调用混合治理**：采用“网关守边界 + 热路径直连”的混合模式：
  - 南北向（外部→服务）与跨域低频调用继续经网关（`GatewayServiceClient`）。
  - 同域高频/低延迟热路径允许直连，但必须使用统一直连 Client 并内置超时、重试、熔断、限流、观测与服务到服务鉴权；直连必须进入白名单审计。
  - 同步更新 `.cursorrules`/相关规格以保持治理一致性。

## Impact
- Affected specs:
  - `code-srp-refactor`
  - `dependency-build-consistency`
  - `communication-architecture`
  - `microservice-consolidation`
  - `architecture-compliance`
- Affected code (implementation stage):
  - `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/service/PaymentService.java`
  - `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/devicecomm/service/DeviceSyncService.java`
  - `microservices/microservices-common/src/main/java/net/lab1024/sa/common/monitoring/AlertManager.java`
  - `microservices/microservices-common/src/main/java/net/lab1024/sa/common/auth/manager/UnifiedAuthenticationManager.java`
  - `microservices/microservices-common/pom.xml`
  - `microservices/ioedream-gateway-service/pom.xml`
  - `microservices/build.gradle`
  - 相关 ManagerConfiguration / 调用封装与测试
- Breaking changes:
  - 外部 API **无破坏性变更**；内部包/类路径与调用模式会调整，但通过 Spring Bean/接口/网关路由保持兼容。
  - 内部“服务间调用规范”从“全经网关”演进为“混合模式”，属于架构规则变更，需要平台护栏与白名单治理。

## Relationship to Existing Changes
- `refactor-optimal-seven-microservice-architecture` 与 `fix-critical-architecture-violations` 已覆盖微服务整合/架构违规修复。本 change **不重复其目标**，聚焦 SRP/低冗余/一致性收敛，并作为其后续质量演进步骤。
