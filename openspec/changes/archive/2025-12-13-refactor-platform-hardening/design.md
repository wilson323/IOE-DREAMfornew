# Design: Platform Hardening

## 1. 目标与非目标

### 目标（按优先级）
1. **白名单/匿名接口单一真源**：以网关统一维护，产出可审计清单。
2. **鉴权/授权语义统一**：Spring Security + JWT Bearer；授权以 `permissions` 为主映射到 `GrantedAuthority`。
3. **配置治理**：禁止默认口令/默认密钥；生产环境 Fail-Fast 全覆盖。
4. **common 边界拆分**：降低跨服务耦合、提升可维护性与可替换性。
5. **前端 TODO 闭环**：以业务域为单位，完成端到端可验收能力。

### 非目标
- 一次性完成所有业务页面的 API 对接与 UI/交互重构。
- 在无可运行环境（缺 JDK）时进行大规模包迁移并保证编译通过。

## 2. 鉴权与权限模型（Spring Security）

### JWT Claims 约定
- `roles`: 作为 `ROLE_` 前缀 Authority（可选）
- `permissions`: 直接映射为 `GrantedAuthority`（核心授权基线）

### 资源服务（Servlet）
- 从 `Authorization: Bearer <token>` 解析并校验 token。
- 校验成功后写入 `SecurityContext`。
- Controller/Service 层使用 `@PreAuthorize("hasAuthority('x:y:z')")`。

### 网关（WebFlux）
- 白名单由配置驱动（Nacos/环境）。
- 非白名单流量统一走 token 校验（后续可升级为 Reactive Resource Server + JWT decoder）。

## 3. 白名单单一真源

### 原则
- **仅网关维护**对外暴露 API 的匿名/白名单。
- 各业务服务只保留：`/actuator/**`、Swagger/OpenAPI 等必要运维入口（如有）。

### 配置载体
- Nacos 公共配置 `common-security.yaml` 提供：
  - `security.whitelist.paths`（网关使用）
  - `security.jwt.*`（各服务使用）

## 4. `microservices-common` 边界拆分（分阶段）

### 现状问题
- `microservices-common` 包含多业务域（access/attendance/consume/oa/...）的 entity/dao/manager。
- 任何变更都可能引发跨服务编译/运行时影响。

### 目标结构（阶段性）
- Phase 1（先止血）：明确“纯公共能力”清单并冻结新增业务域代码进入 common。
- Phase 2（拆分）：将纯公共能力拆到独立模块（如 common-core/common-web/common-auth），业务域代码下沉到对应微服务。
- Phase 3（收敛）：`microservices-common` 仅作为聚合/过渡层或被移除。

## 5. 前端 TODO 闭环（分域推进）
- 以“门禁/考勤/消费/访客/视频”五域建立对接清单（页面 → API → DTO → 权限点）。
- 每个域形成可验收的最小闭环（列表/详情/创建/状态变更/导出）。

