# Proposal: 完善OA审批工作流引擎

## 变更ID

`add-oa-workflow-engine`

## 优先级

**P2** - 中优先级

## 背景

根据文档`OA工作流/`目录下的设计文档，系统需要完善审批工作流引擎，支持灵活的流程配置和多级审批。

## 变更原因

1. 当前审批流程硬编码，不够灵活
2. 需要支持多级审批、会签、或签等模式
3. 需要支持流程可视化配置
4. 需要支持审批超时自动处理

## 变更内容

### 新增功能

1. **流程定义**
   - 流程模板管理
   - 节点配置
   - 条件分支
   - 并行网关

2. **流程实例**
   - 流程发起
   - 流程审批
   - 流程撤回
   - 流程转办

3. **审批规则**
   - 多级审批
   - 会签/或签
   - 条件审批
   - 超时处理

4. **流程监控**
   - 流程状态查询
   - 审批进度追踪
   - 超时预警

### 涉及服务

- `ioedream-oa-service`

### 涉及文件

- 新增: `WorkflowEngineService.java`
- 新增: `WorkflowEngineServiceImpl.java`
- 新增: `WorkflowDefinitionManager.java`
- 新增: `WorkflowInstanceManager.java`
- 修改: `ApprovalServiceImpl.java`

## 影响范围

- OA审批模块
- 门禁临时权限审批
- 访客预约审批

## 验收标准

- [ ] 流程定义CRUD完整
- [ ] 多级审批流程正常
- [ ] 会签/或签逻辑正确
- [ ] 超时自动处理生效
- [ ] 单元测试覆盖率>80%
