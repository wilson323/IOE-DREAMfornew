# OpenSpec 未实现项汇总 + 归档计划（2025-12-13）

> 目标：
>
> 1) 将 `openspec/changes/` 下各 change 的 **未实现/未验证/不符合 OpenSpec 结构** 的事项汇总到同一个 MD。
> 2) 给出 **归档清单与操作计划**（归档到 `openspec/changes/archive/YYYY-MM-DD-<change-id>/`）。
>
> 说明：本文件仅做“梳理与归档计划”，不包含任何业务代码整改。

---

## 1. 范围与扫描结果

当前执行归档策略为：**选项2 + B（真归档）**。

当前 `openspec/changes/` 目录下（未在 `changes/archive/` 内）保留（今日创建，未归档）的 change：

- `refactor-common-boundary-workflow-oa`
- `refactor-platform-hardening`
- `refactor-srp-and-global-consistency`
- `update-api-contract-security-tracing`
- `update-gateway-security-baseline`

已归档到 `openspec/changes/archive/`（非今日创建，已移动归档）的 change：

- `archive/2025-12-13-complete-smart-campus-implementation`
- `archive/2025-12-13-fix-critical-architecture-violations`
- `archive/2025-12-13-refactor-optimal-seven-microservice-architecture`
- `archive/2025-12-13-seven-microservice-architecture-integration`

---

## 2. 未实现 / 未验证 / 不符合结构项（按 change 维度）

### 2.1 `complete-smart-campus-implementation`

- **结构不符合 OpenSpec 标准**
  - 仅存在：`smart-campus-implementation-proposal.md`
  - 缺失：`proposal.md`、`design.md`、`tasks.md`、`specs/**/spec.md`（标准结构不完整）
- **状态**
  - 当前更像“业务实施路线图”，未按 OpenSpec 的“可验证任务清单 + spec delta”拆分。

### 2.2 `fix-critical-architecture-violations`

- **未完成事项（tasks.md）**
  - Phase 1（P0）全部未勾选：
    - `@Repository` → `@Mapper`
    - `@Autowired` → `@Resource`
    - 明文密码治理（116 risks）
  - Phase 2/3 也均为未勾选。
- **代码侧证据（现状）**
  - 全仓仍可检索到 `@Repository` 与 `@Autowired` 的使用（需要后续集中整改）。

### 2.3 `refactor-common-boundary-workflow-oa`

- **未完成事项（tasks.md 未勾选）**
  - `2.1` 调整 `WorkflowApprovalManager` 调用路径（对齐 canonical engine path）
  - `3.1/3.2` workflow contract 下沉（common-core/contract 包）
  - `5.1/5.2` workflow 实现迁移到 OA（DAO/Entity/Engine/Executor）
  - `6.1-6.4` 端到端业务流验证（门禁/考勤/访客/消费写回 workflowInstanceId）
  - `8.1-8.3` follow-ups（contract downshift、实现迁移、E2E 验证）
- **已完成项（已勾选）**
  - OA 兼容入口策略、MapperScan 清理、回归构建通过。

### 2.4 `refactor-optimal-seven-microservice-architecture`

- **未完成事项（tasks.md）**
  - 大量迁移/验证/归档任务未勾选（功能扫描矩阵、迁移执行、测试基线、归档清理等）。
- **风险提示**
  - 该 change 目标与当前仓库“已存在的 8+ 服务形态”存在潜在历史差异（需要确认该 change 是否为旧提案/已由其它 change 覆盖）。

### 2.5 `refactor-platform-hardening`

- **未完成事项（tasks.md 未勾选）**
  - `4.2` 制定 common 拆分迁移顺序与模块划分
  - `4.3` 定义“禁止新增业务域代码进入 common”的准入规则与检查项（CI/静态检查）
  - `6.1/6.2/6.3` Maven `test/verify` 与契约回归验证
- **已存在的产物/证据（已落地）**
  - `microservices/common-config/nacos/common-security.yaml` ✅
  - `artifacts/anon-whitelist-inventory.txt` ✅
  - `artifacts/microservices-common-package-inventory.txt` ✅
  - `artifacts/frontend-todo-inventory.txt` ✅

### 2.6 `refactor-srp-and-global-consistency`

- **未完成事项（tasks.md 未勾选）**
  - `1.15` 开启并修复 PMD/Sonar Gate
  - `2.2` Sonar 质量门禁通过
  - `2.3` 网关与关键业务服务联调回归（支付/设备/告警/认证）
- **说明**
  - 其它 Implementation/Validation 多数已勾选，说明“代码可能已落地，但质量门禁/联调验证未闭环”。

### 2.7 `seven-microservice-architecture-integration`

- **整体状态**
  - `tasks.md` 全部未勾选（更偏向“长周期实施计划”，并非已完成变更记录）。
- **风险提示**
  - 与 `openspec/project.md` 的当前服务清单（8+）与历史提案可能存在冲突/重复（需确认是否应直接归档为历史方案）。

### 2.8 `update-api-contract-security-tracing`

- **未完成事项（tasks.md 未勾选）**
  - `4` Tracing：
    - `4.2` 跨服务 traceId 连续性验证
  - `5` Tests & Validation：
    - `5.1` 契约回归测试最小集合
    - `5.2` `mvn ... test`
    - `5.3` `mvn ... verify`
  - `6` Rollout：
    - 灰度发布、迁移客户端到 canonical、到期删除兼容入口
- **已存在的产物/证据（已落地）**
  - `artifacts/backend-controller-mappings.txt` ✅
  - `artifacts/gateway-routes-extract.txt` ✅
  - `artifacts/frontend-api-baseurl-inventory.txt` ✅
  - `artifacts/anon-whitelist-inventory.txt` ✅

### 2.9 `update-gateway-security-baseline`

- **未完成事项（tasks.md 未勾选）**
  - `5.1` CORS 长期单一真源决策（减少 `globalcors`）
  - `5.2` 增加 401/403 最小集成测试（WebTestClient）
  - `5.3` 提供 `security.rbac.rules` 生产示例
- **本次补充产物（本次会话新增）**
  - `security-baseline-comparison-template.md`（对比表模板 + 基线条目）
  - `security-remediation-backlog.md`（P0/P1 统一整改 Backlog）

---

## 3. 统一“未实现项”总清单（按优先级）

### P0（优先处理）

- **P0-1**：Actuator 敏感端点暴露风险（env/configprops/activiti）与 `/actuator/** permitAll` 叠加（跨服务）
- **P0-2**：工作流 contract 下沉 + workflow 实现迁移到 OA（保证边界与调用一致）
- **P0-3**：`fix-critical-architecture-violations` 中的注解规范与明文密码治理（若尚未完成）

### P1（重要但可排期）

- **P1-1**：全局异常处理统一（多数业务服务缺少 controller advice）
- **P1-2**：Tracing 连续性验证与最小回归测试集补齐
- **P1-3**：Gateway 安全/鉴权 401/403 行为的最小集成测试
- **P1-4**：PMD/Sonar Gate 启用并通过

---

## 4. 归档执行结果（已完成）

本次按 **选项2 + B（真归档）** 执行归档：

- **保留未归档（今日创建）**：
  - `refactor-common-boundary-workflow-oa/`
  - `refactor-platform-hardening/`
  - `refactor-srp-and-global-consistency/`
  - `update-api-contract-security-tracing/`
  - `update-gateway-security-baseline/`
- **已移动归档（非今日创建）**：
  - `archive/2025-12-13-complete-smart-campus-implementation/`
  - `archive/2025-12-13-fix-critical-architecture-violations/`
  - `archive/2025-12-13-refactor-optimal-seven-microservice-architecture/`
  - `archive/2025-12-13-seven-microservice-architecture-integration/`

备注：后续如果需要将“今日创建”的 5 个 change 也归档，可继续沿用同一命名规则：`archive/YYYY-MM-DD-<change-id>/`.

---

## 5. 归档执行结果总结

已完成 **选项2 + B（真归档）** 归档，相关 change 目录已移动到 `openspec/changes/archive/` 下。请确认归档结果并继续后续工作。
