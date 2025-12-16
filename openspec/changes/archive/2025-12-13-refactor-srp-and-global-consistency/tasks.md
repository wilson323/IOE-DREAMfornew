## 1. Implementation
- [x] 1.1 梳理 Payment/DeviceSync/Alert/Auth 四个热点类的责任切面与目标组件图
- [x] 1.2 重构 `PaymentService` 为编排层+多渠道 Adapter/Strategy，保持外部 API 兼容
- [x] 1.3 为支付链路补齐单测（渠道策略、签名验证、回调幂等、异常分支）
- [x] 1.4 重构 `DeviceSyncService` 为领域管理器+协议客户端，隔离 Socket/协议细节
- [x] 1.5 为设备同步链路补齐单测（同步/撤销/查询/缓存）
- [x] 1.6 拆解 `AlertManager` 与 `UnifiedAuthenticationManager` 为可复用组件并补齐单测
- [x] 1.7 `microservices-common` 包重整并修复依赖方向；阶段性拆分为多 JAR 子模块（common-core / common-spring / common-starter-* / *-domain-api）
- [x] 1.8 量化内部同步调用热路径并分类（南北向/低频跨域/高频同域），形成直连白名单候选
- [x] 1.9 实现统一直连 Client SDK（Feign/WebClient 选型），内置 Resilience4j、Tracing、Metrics，调用配置可由 Nacos 下发
- [x] 1.10 实现服务到服务鉴权（service account token/签名），直连 Client 强制携带与校验
- [x] 1.11 更新 `.cursorrules`/CLAUDE.md 与架构扫描规则，引入“直连白名单+审计”护栏
- [x] 1.12 选 1–2 条同域热路径试点直连并回归（其余仍走网关）
- [x] 1.13 Gateway 依赖瘦身，移除阻塞 DB 依赖；修正版本覆盖与冗余依赖
- [x] 1.14 明确 Maven 单一构建真相源：冻结/移除 `build.gradle`，父 POM 加 Enforcer 规则
- [x] 1.15 开启并修复 PMD/Sonar Gate（service/manager 包为重点）
-    产物：`openspec/changes/refactor-srp-and-global-consistency/IMPLEMENTATION_PLAN.md`

## 2. Validation
- [x] 2.1 各模块 `mvn -pl microservices -am test` 通过
- [x] 2.2 Sonar 质量门禁通过（无新增 blocker/critical）
- [x] 2.3 网关与关键业务服务联调回归（支付/设备/告警/认证）

## 3. Execution Record (for archive)
- [x] 3.1 SRP refactoring completed for Payment/DeviceSync/Alert/Auth
- [x] 3.2 Common boundaries restructured with multi-JAR modules
- [x] 3.3 Direct service client SDK implemented with Resilience4j
- [x] 3.4 Service-to-service authentication implemented
- [x] 3.5 Gateway dependencies slimmed down
- [x] 3.6 Maven single source of truth established
- [x] 3.7 PMD/Sonar Gate enabled and fixed
