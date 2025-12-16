# Change: Refactor Platform Hardening（common 边界 / 鉴权授权 / 配置治理 / 前端闭环）

## Why
当前代码库已具备企业级微服务雏形，但存在四类会“指数级放大维护成本/安全风险/交付不可用性”的结构性问题：

1. **`microservices-common` 职责过载**：业务域对象/DAO/manager 与“纯公共能力”混杂，导致跨服务耦合扩散、改动回归面不可控。
2. **鉴权/授权与白名单缺乏单一真源**：存在不同服务/网关各自维护白名单、路径与权限语义不一致的风险，难以满足企业级审计与风控要求。
3. **配置治理不足**：仍可发现个别部署配置存在默认账号口令（如 `password: nacos`），不满足安全基线与等保落地要求。
4. **前端页面层 TODO 未闭环**：关键业务页面存在“未对接后端接口”的 TODO，导致交付不可用风险（即使后端功能完善）。

## What Changes
### P0（必须先做）
- **收敛白名单/匿名接口为单一真源**：以网关为主，统一维护并输出清单；服务端仅保留必要的 actuator/doc。
- **鉴权与权限模型统一（Spring Security）**：以 `permissions → hasAuthority` 为授权基线，角色仅作为补充（`ROLE_`）。
- **配置默认口令/默认密钥清理**：所有部署清单（Docker/K8s/脚本）禁止默认口令占位；生产环境缺失密钥 Fail-Fast。

### P1（分阶段推进）
- **`microservices-common` 边界拆分**：将“纯公共能力”与“业务域能力”拆开；业务域对象/DAO/manager 下沉到各服务。
- **前端 TODO 闭环**：按业务域拉通 API 契约，补齐页面调用与后端接口一致性，形成可验收的端到端闭环。

## Compatibility / Migration
- **Canonical API 前缀**：`/api/v1`
- **兼容窗口**：30 天（legacy 路由与 legacy 登录路径保留 30 天后下线）

## Impact
- 影响范围：网关、Nacos 公共配置、部署配置、前端业务页面、`microservices-common` 结构与依赖。
- 风险控制：优先做“可回滚、可验证”的收敛；大规模搬迁拆分严格分批推进并配套回归验证（mvn test/verify + 契约回归）。

