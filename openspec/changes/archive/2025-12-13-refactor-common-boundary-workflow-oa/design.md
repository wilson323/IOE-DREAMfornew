# Design

## Context

- 网关路由已将 `/api/v1/workflow/**` 归口到 `ioedream-oa-service`。
- `WorkflowApprovalManager` 仍调用 `POST /api/v1/workflow/start`，与 OA 现有标准入口不一致。
- 多个微服务启动类存在无效 `@MapperScan` 扫描项（`hr/document/file/security.dao`），反映出 common 边界与配置漂移。

## Goals / Non-Goals

- Goals:
  - workflow 启动链路在不改业务服务代码的前提下恢复可用（兼容旧路径）
  - 清理无效 `@MapperScan` 项，保证扫描包与真实代码一致
  - 为后续将 workflow 实现迁入 OA 的 P1 工作打基础（contract 下沉、实现迁移）

- Non-Goals:
  - 本变更不进行大规模业务重构，不新增新业务能力
  - 本变更不立即完成 workflow 全量实现迁移（列入 P1）

## Decisions

- Decision: workflow 归口 OA（D1-1）
  - Why: 网关路由已将 `/api/v1/workflow/**` 归口 OA；审批能力天然属于 OA 业务域。

- Decision: 采用 OA 侧“兼容入口”策略
  - What: 在 OA 增加 `POST /api/v1/workflow/start`，内部转调工作流引擎标准入口。
  - Why: 让所有业务服务无需改动即可恢复调用链路，降低回归风险。

## Migration Plan

1. P0：在 OA 提供旧路径兼容入口（并补齐参数校验）。
2. P0：清理各服务启动类无效 `@MapperScan` 扫描项（替换为 `common.rbac.dao`）。
3. P1：将 workflow 契约（枚举/常量/DTO）下沉到 `microservices-common-core`。
4. P1：将 workflow 实现迁入 `ioedream-oa-service` 并从 `microservices-common` 移除实现代码。

## Risks / Trade-offs

- Risk: 兼容入口可能导致“长期不清理旧路径”。
  - Mitigation: 在 `tasks.md` 中明确兼容窗口与后续删除计划。

- Risk: `@MapperScan` 清理可能影响少量模块的 MyBatis 扫描。
  - Mitigation: 仅移除确认不存在的包；对安全域扫描统一改为已存在的 `common.rbac.dao`.
