# Change: Refactor microservices-common Boundary and Workflow Ownership (OA)

## Why

当前 `microservices-common` 在工程边界上出现了典型的“共享单体化”趋势，带来以下 P0/P1 风险：

- **P0：服务间工作流调用路径不一致**
  - 业务服务通过 `WorkflowApprovalManager` 发起审批时，当前使用 `POST /api/v1/workflow/start`。
  - 网关路由已将 `/api/v1/workflow/**` 归口到 `ioedream-oa-service`，且 OA 实际存在的“启动流程”入口为 `POST /api/v1/workflow/engine/instance/start`。
  - 若不对齐，审批链路会在运行期出现 404/路径错配，影响门禁/考勤/访客/消费等核心流程。

- **P0：微服务启动类存在无效 @MapperScan 包**
  - 多个微服务启动类包含不存在的 DAO 包扫描项（如 `net.lab1024.sa.common.hr.dao` / `net.lab1024.sa.common.document.dao` / `net.lab1024.sa.common.file.dao` / `net.lab1024.sa.common.security.dao`），会造成“配置漂移 + 误导边界”的长期隐患。

- **P1：common 的职责边界不清晰**
  - `microservices-common` 同时承载：业务域共享（workflow/auth/organization/system 等）、横切能力（tracing/cache/response）、以及部分 Web/安全运行时语义（Servlet Filter/Security Config）。
  - 这种混合会放大耦合半径，使网关（WebFlux）被迫做大量依赖排除，后续拆分与治理成本上升。

本变更目标是在不引入新业务能力的前提下，明确“**workflow 归口 OA**、**common 边界收敛**、**启动配置清理**”，为后续拆分迁移与安全审计提供稳定基线。

## What Changes

- **Workflow 归口 OA（D1-1）**
  - 规范：工作流能力（流程定义/实例/任务管理）由 `ioedream-oa-service` 作为唯一 Provider。
  - 对齐：`WorkflowApprovalManager` 的远程调用路径与 OA Controller 真实路径对齐。

- **工作流契约下沉（Contract）**
  - 将跨服务共享的工作流枚举/常量/DTO（仅契约级）下沉到 `microservices-common-core`（或其既有约定的 contract 包），避免业务服务依赖 workflow 的实现细节（DAO/Entity/Engine）。

- **清理无效 @MapperScan**
  - 删除或替换微服务启动类中不存在的 DAO 包扫描项，确保扫描项与真实代码一致。

- **common 边界治理（阶段性）**
  - 短期：先完成“调用路径对齐 + contract 下沉 + MapperScan 清理”。
  - 中期：逐步将 `common/workflow` 的实现（DAO/Entity/Engine/Executor）迁入 OA，使 `microservices-common` 只保留可复用的通用能力与契约。

## Impact

- Affected capabilities (delta specs in this change):
  - `interservice-calls`
  - `common-library-boundaries`
  - `architecture-compliance`

- Affected code (implementation stage):
  - `microservices/microservices-common/src/main/java/net/lab1024/sa/common/workflow/manager/WorkflowApprovalManager.java`
  - `microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/controller/WorkflowEngineController.java`
  - 业务服务：access/attendance/visitor/consume 中引用 workflow 常量/manager 的 Service 实现类
  - 各微服务启动类：`*ServiceApplication.java` 的 `@MapperScan` 配置

- Breaking changes:
  - 对外 API **不应有破坏性变更**（通过网关路由保持一致）。
  - 内部实现与包结构将逐步调整；本变更通过“契约下沉 + 路径对齐 + 分阶段迁移”保证可回滚。

## Relationship to Existing Changes

- 与 `update-api-contract-security-tracing` 的目标一致：确保服务间调用路径与网关/Controller 真实路径一致。
- 与 `refactor-srp-and-global-consistency` 的方向一致：收敛 `microservices-common` 职责边界，降低共享单体化.
