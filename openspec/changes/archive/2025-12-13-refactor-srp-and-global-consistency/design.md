## Context
项目已完成七微服务架构与基础规范落地，但核心服务/公共库存在过度聚合与一致性漂移风险。本设计描述 SRP 重构与全局一致性治理的技术路径与权衡。

## Goals / Non-Goals
- Goals:
  - 关键超大类降维为可读、可测、可演进的组件集合（单类目标 300–600 行）。
  - `microservices-common` 收敛到“共享基础能力”，降低跨域耦合。
  - 依赖/构建/网关定位保持单一真相源与一致性。
  - 内部调用在“混合模式”下具备企业级韧性与观测。
- Non-Goals:
  - 不做业务功能新增/删减，不改数据库结构。
  - 不做外部 API 路径/协议变更。
  - 不在本 change 中拆分/合并微服务数量（除非另行提案）。

## Decisions
1. **超大类拆解模式**
   - 采用 “Service 编排 + Manager 流程 + Adapter/Strategy 适配外部 + Domain 领域组件” 的结构。
   - Service 仅负责事务边界与跨组件编排；外部 SDK/协议细节下沉到 Adapter；DAO 仅数据访问。

2. **Payment 领域重构**
   - 拆分为 `PaymentOrchestratorService`（编排）、`WechatPayAdapter`、`AlipayAdapter`、`BankPayAdapter`、`RefundAdapter`、`NotifyHandler`、`SignatureVerifier` 等。
   - 以策略模式+工厂选择支付渠道，避免条件分支爆炸。

3. **DeviceSync 领域重构**
   - 拆分为 `DeviceUserSyncManager`、`DevicePermissionManager`、`DeviceHealthManager`、`ProtocolClient`、`ProtocolHandler` 等。
   - Socket/协议层完全隔离，业务侧只看到领域模型与结果 DTO。

4. **公共库边界治理**
   - 阶段 1：按有界上下文重整包（auth/monitoring/organization/consume/...），清理循环依赖。
   - 阶段 2：逐步拆为多 JAR（`common-core`/`common-auth`/`common-monitoring`…），各服务按需依赖。

5. **内部调用架构**
   - 采用分层混合模式：
     1) 南北向（外部/前端→服务）必须经网关统一治理。
     2) 东西向低频/粗粒度跨域同步调用继续经网关，保持治理集中与路由能力。
     3) 东西向高频/低延迟/强一致同域热路径允许直连。
   - 直连必须配套平台护栏：统一直连 Client（超时/重试/熔断/限流/fallback/指标/Tracing）、服务到服务鉴权（service token/签名，必要时引入 mTLS/网格）、契约/版本治理、直连白名单与 CI 审计扫描。
   - 更新现有“全经网关”相关规范与 OpenSpec 能力（`microservice-consolidation`、`architecture-compliance`）为混合模式。

6. **网关与构建一致性**
   - Gateway 仅保留 WebFlux/安全/路由/限流/观测依赖；移除阻塞 DB 依赖。
   - Maven 作为唯一构建真相源；Gradle 配置冻结/移除；父 POM 加 Enforcer 约束版本与禁止覆盖。

## Risks / Trade-offs
- 拆解过程中可能出现 Bean 冲突/循环依赖 → 通过构造器注入、分层接口、逐类迁移控制风险。
- 公共库拆分会引入跨模块发布与依赖管理成本 → 采用分阶段、先包后模块的渐进策略。
- 网关调用链加固可能增加少量延迟 → 以 SLA/SLO 为约束进行参数调优。

## Migration Plan
1. 先拆解 Payment/DeviceSync 两个最大热点类并补齐单测。
2. 拆解 monitoring/auth 两个公共热点（Alert/UnifiedAuthentication）。
3. 完成公共库包重整，随后按模块拆分 JAR。
4. 依赖治理与网关瘦身并行推进。
5. 开启静态门禁（PMD/Sonar Gate）并固化 CI。

## Open Questions
- 直连试点的 Top 热路径名单与优先级（建议先选 1–2 条同域链路）。
- 服务到服务鉴权的具体落地形态（service token/签名 vs mTLS/服务网格），以及上线节奏。
- `microservices-common` 拆分为多 JAR 的发布节奏与版本策略（渐进式优先）。
