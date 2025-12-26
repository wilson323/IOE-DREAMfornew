# Tasks

## 1. Preparation

- [x] 1.1 生成 workflow 启动接口的“真实入口清单”（OA Controller/Service），并确认期望的 canonical path
- [x] 1.2 生成各微服务 `@MapperScan` 扫描包清单，并标注不存在/冗余项

## 2. Workflow Path Alignment (P0)

- [x] 2.1 修正 `WorkflowApprovalManager` 调用路径：从 `/api/v1/workflow/start` 对齐到 OA 真实入口（`/api/v1/workflow/engine/instance/start`），保持调用方无感
- [x] 2.2 为旧路径提供兼容策略（如需要）：在 OA 增加兼容入口或在网关配置兼容重写（明确兼容窗口）

## 3. Workflow Contract Downshift (P0/P1)

- [x] 3.1 将 `BusinessTypeEnum` / `WorkflowDefinitionConstants` 等跨服务共享契约下沉到 `microservices-common-core`（或约定的 contract 包）
- [x] 3.2 更新 access/attendance/visitor/consume 代码仅依赖 contract（不依赖 workflow 实现 DAO/Entity/Engine）

## 4. Cleanup Invalid MapperScan (P0)

- [x] 4.1 删除不存在的扫描项：`net.lab1024.sa.common.hr.dao` / `net.lab1024.sa.common.document.dao` / `net.lab1024.sa.common.file.dao` / `net.lab1024.sa.common.security.dao`
- [x] 4.2 若存在真实安全/权限 DAO，统一使用已存在包（例如 `net.lab1024.sa.common.rbac.dao`），并保证扫描项与代码一致

## 5. Workflow Implementation Migration to OA (P1)

- [x] 5.1 将 `microservices-common` 内 `common/workflow/**` 的实现（DAO/Entity/Engine/Executor 等）逐步迁入 `ioedream-oa-service`，保持外部调用契约稳定
  - Entity 9个、DAO 9个、Manager 3个、Executor 4个已迁移到 OA
- [x] 5.2 迁移完成后从 `microservices-common` 移除 workflow 实现，仅保留 contract（由 core 承载）
  - common 中仅保留 WorkflowApprovalManager、domain、function、job、listener、model

## 6. Validation

- [x] 6.1 门禁：权限申请/紧急权限申请可正常发起审批并写回 `workflowInstanceId`
- [x] 6.2 考勤：请假申请可正常发起审批并写回 `workflowInstanceId`
- [x] 6.3 访客：预约申请可正常发起审批并写回 `workflowInstanceId`
- [x] 6.4 消费：退款申请可正常发起审批并写回 `workflowInstanceId`
- [x] 6.5 `mvn -pl microservices -am test` 通过

## 7. Execution Record (for archive)

- [x] 7.1 Implemented OA compatibility endpoint for legacy workflow start path
- [x] 7.2 Cleaned invalid `@MapperScan` packages across microservices
- [x] 7.3 Regression build/test passed (see 6.5)
- [x] 7.4 Workflow implementation migrated to OA (Entity/DAO/Manager/Executor)
- [x] 7.5 Contract downshifted to common-core (BusinessTypeEnum/WorkflowDefinitionConstants)
- [x] 7.6 End-to-end business flow verified (access/attendance/visitor/consume)

## 8. Follow-ups

- [x] 8.1 Contract downshift: move workflow shared DTO/Enum/Constants to the agreed contract location
- [x] 8.2 Workflow implementation migration to OA (DAO/Entity/Engine/Executor)
- [x] 8.3 End-to-end business flow verification (6.1-6.4)
