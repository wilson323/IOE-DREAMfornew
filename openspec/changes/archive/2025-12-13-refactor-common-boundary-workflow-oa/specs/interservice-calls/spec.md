# Spec Delta: interservice-calls

## MODIFIED Requirements

### Requirement: Workflow Start Path Is Consistent

服务间工作流启动调用 SHALL 使用与 OA 工作流引擎真实入口一致的路径；对于历史路径，系统 SHALL 提供兼容入口并可配置兼容窗口。

#### Scenario: Business service starts workflow using legacy path

- **WHEN** 业务服务通过 `POST /api/v1/workflow/start` 发起审批
- **THEN** OA 服务 SHALL 接收请求并成功启动流程实例

#### Scenario: Business service starts workflow using canonical engine path

- **WHEN** 业务服务通过 `POST /api/v1/workflow/engine/instance/start` 发起审批
- **THEN** OA 服务 SHALL 成功启动流程实例并返回实例 ID
