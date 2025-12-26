# OA工作流模块使用说明

## 📋 模块概述

OA工作流模块提供了完整的工作流流程定义、实例、任务管理功能，支持Web端和移动端。

## 🗂️ 文件结构

```
workflow/
├── task/                    # 任务相关页面
│   ├── pending-task-list.vue    # 待办任务列表
│   ├── completed-task-list.vue  # 已办任务列表
│   └── task-detail.vue          # 任务详情
├── instance/                # 流程实例相关页面
│   ├── instance-list.vue        # 流程实例列表
│   ├── instance-detail.vue      # 流程实例详情
│   └── my-process-list.vue      # 我发起的流程
├── definition/              # 流程定义相关页面
│   └── definition-list.vue      # 流程定义管理
└── monitor/                 # 流程监控
    └── process-monitor.vue       # 流程监控页
```

## 🚀 快速开始

### 1. 路由配置

路由通过后端菜单数据动态加载，需要在数据库中添加以下菜单配置：

#### 待办任务列表
- **路径**: `/oa/workflow/task/pending-task-list`
- **组件**: `/business/oa/workflow/task/pending-task-list.vue`
- **权限**: `oa:workflow:task:query`

#### 已办任务列表
- **路径**: `/oa/workflow/task/completed-task-list`
- **组件**: `/business/oa/workflow/task/completed-task-list.vue`
- **权限**: `oa:workflow:task:query`

#### 任务详情
- **路径**: `/oa/workflow/task/task-detail`
- **组件**: `/business/oa/workflow/task/task-detail.vue`
- **权限**: `oa:workflow:task:query`

#### 流程实例列表
- **路径**: `/oa/workflow/instance/instance-list`
- **组件**: `/business/oa/workflow/instance/instance-list.vue`
- **权限**: `oa:workflow:instance:query`

#### 流程实例详情
- **路径**: `/oa/workflow/instance/instance-detail`
- **组件**: `/business/oa/workflow/instance/instance-detail.vue`
- **权限**: `oa:workflow:instance:query`

#### 我发起的流程
- **路径**: `/oa/workflow/instance/my-process-list`
- **组件**: `/business/oa/workflow/instance/my-process-list.vue`
- **权限**: `oa:workflow:instance:query`

#### 流程定义管理
- **路径**: `/oa/workflow/definition/definition-list`
- **组件**: `/business/oa/workflow/definition/definition-list.vue`
- **权限**: `oa:workflow:definition:query`

#### 流程监控
- **路径**: `/oa/workflow/monitor/process-monitor`
- **组件**: `/business/oa/workflow/monitor/process-monitor.vue`
- **权限**: `oa:workflow:monitor:query`

### 2. 依赖安装

#### Web端流程图组件

流程图组件需要安装 `bpmn-js` 库：

```bash
npm install bpmn-js
```

然后在 `ProcessDiagram.vue` 中取消注释相关代码：

```javascript
import BpmnViewer from 'bpmn-js/lib/NavigatedViewer';
viewer = new BpmnViewer({
  container: diagramContainer.value
});
await viewer.importXML(bpmnXml);
```

### 3. WebSocket配置

在应用启动时初始化WebSocket连接：

```javascript
import { initWorkflowWebSocket } from '/@/utils/workflow-websocket';

// 在用户登录后初始化
const wsUrl = 'ws://your-websocket-server/workflow';
const token = userStore.getToken();
initWorkflowWebSocket(wsUrl, token);

// 监听新任务通知
const ws = getWorkflowWebSocket();
ws.on('NEW_TASK', (data) => {
  // 处理新任务通知
  console.log('新任务:', data);
});
```

## 📦 公共组件

### ProcessSteps - 流程进度组件

显示流程节点的执行进度。

```vue
<ProcessSteps :history-list="historyList" />
```

**Props:**
- `historyList` (Array): 流程历史记录列表

### ProcessDiagram - 流程图组件

显示BPMN流程图，支持节点高亮。

```vue
<ProcessDiagram
  :instance-id="instanceId"
  :highlight-node="nodeId"
/>
```

**Props:**
- `instanceId` (Number): 流程实例ID
- `bpmnXml` (String, 可选): BPMN XML字符串
- `highlightNode` (String, 可选): 要高亮的节点ID
- `highlightActive` (Boolean, 可选): 是否高亮活动节点

### ApprovalForm - 审批表单组件

提供任务审批、驳回、转办、委派功能。

```vue
<ApprovalForm
  :task-id="taskId"
  :task-detail="taskDetail"
  @submit-success="handleSuccess"
/>
```

**Props:**
- `taskId` (Number): 任务ID
- `taskDetail` (Object): 任务详情
- `showVariables` (Boolean): 是否显示流程变量
- `showFormData` (Boolean): 是否显示表单数据

**Events:**
- `submit-success`: 提交成功时触发

## 🔧 Store使用

### 获取待办任务列表

```javascript
import { useWorkflowStore } from '/@/store/modules/business/workflow';

const workflowStore = useWorkflowStore();

// 查询待办任务
await workflowStore.fetchPendingTaskList({
  pageNum: 1,
  pageSize: 20,
  category: 'LEAVE',
  priority: 3,
  dueStatus: 'OVERDUE'
});

// 获取任务列表
const taskList = workflowStore.pendingTaskList;
const total = workflowStore.pendingTaskTotal;
```

### 受理任务

```javascript
await workflowStore.claimTask(taskId);
```

### 完成任务

```javascript
await workflowStore.completeTask(taskId, {
  outcome: '1', // 1-同意, 2-驳回
  comment: '审批意见',
  variables: {},
  formData: {}
});
```

### 驳回任务

```javascript
await workflowStore.rejectTask(taskId, {
  comment: '驳回原因',
  variables: {}
});
```

## 📱 移动端使用

移动端页面已配置在 `smart-app/src/pages.json` 中，可直接使用：

```javascript
// 跳转到待办任务列表
uni.navigateTo({
  url: '/pages/workflow/pending-task-list'
});

// 跳转到任务详情
uni.navigateTo({
  url: `/pages/workflow/task-detail?taskId=${taskId}`
});
```

## ⚠️ 注意事项

1. **流程图组件**: 需要安装 `bpmn-js` 库才能完整显示流程图
2. **用户选择**: 转办/委派功能需要接入用户管理API获取用户列表
3. **WebSocket**: 需要配置WebSocket服务器地址和认证方式
4. **权限控制**: 所有页面都使用 `v-privilege` 指令进行权限控制
5. **API路径**: 确保后端API路径与前端调用路径一致

## 🐛 常见问题

### Q: 流程图不显示？
A: 需要安装 `bpmn-js` 库，并在 `ProcessDiagram.vue` 中启用相关代码。

### Q: WebSocket连接失败？
A: 检查WebSocket服务器地址和认证token是否正确。

### Q: 转办/委派时找不到用户？
A: 需要在 `ApprovalForm.vue` 中接入用户管理API。

## 📚 相关文档

- [后端API文档](../api/business/oa/workflow-api.js)
- [Store文档](../../../store/modules/business/workflow.js)
- [WebSocket工具文档](../../../utils/workflow-websocket.js)

