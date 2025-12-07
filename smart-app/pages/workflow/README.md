# OA工作流模块 - 移动端使用说明

## 📋 模块概述

移动端工作流模块提供了完整的工作流任务管理和流程查看功能，支持待办任务、已办任务、任务审批等核心功能。

## 🗂️ 文件结构

```
pages/workflow/
├── pending-task-list.vue    # 待办任务列表
├── completed-task-list.vue   # 已办任务列表
├── task-detail.vue           # 任务详情
├── task-approval.vue         # 任务审批
├── instance-list.vue         # 流程实例列表
└── instance-detail.vue       # 流程实例详情

components/workflow/
└── TaskCard.vue              # 任务卡片组件
```

## 🚀 快速开始

### 1. 路由配置

路由已配置在 `src/pages.json` 中，无需额外配置。

### 2. Store使用

```javascript
import { useWorkflowStore } from '@/store/workflow';

const workflowStore = useWorkflowStore();

// 查询待办任务
await workflowStore.fetchPendingTaskList({
  pageNum: 1,
  pageSize: 20
});
```

### 3. 页面跳转

```javascript
// 跳转到待办任务列表
uni.navigateTo({
  url: '/pages/workflow/pending-task-list'
});

// 跳转到任务详情
uni.navigateTo({
  url: `/pages/workflow/task-detail?taskId=${taskId}`
});

// 跳转到任务审批
uni.navigateTo({
  url: `/pages/workflow/task-approval?taskId=${taskId}&action=approve`
});
```

## 📦 组件使用

### TaskCard - 任务卡片组件

```vue
<TaskCard
  :task="task"
  @click="handleViewDetail"
  @quick-approve="handleQuickApprove"
/>
```

**Props:**
- `task` (Object): 任务对象

**Events:**
- `click`: 点击卡片时触发
- `quick-approve`: 快速审批时触发

## 🔧 API调用

所有API方法都在 `api/workflow.js` 中定义：

```javascript
import { workflowApi } from '@/api/workflow';

// 查询待办任务
const response = await workflowApi.pageMyTasks({
  pageNum: 1,
  pageSize: 20
});

// 完成任务
await workflowApi.completeTask(taskId, {
  outcome: '1',
  comment: '审批意见'
});
```

## 📱 功能说明

### 待办任务列表

- ✅ 统计卡片显示（紧急、高、已过期、全部）
- ✅ 筛选功能（分类、优先级、到期状态）
- ✅ 下拉刷新、上拉加载
- ✅ 快速审批功能
- ✅ 任务卡片展示

### 已办任务列表

- ✅ 筛选功能（处理结果）
- ✅ 下拉刷新、上拉加载
- ✅ 查看详情

### 任务详情

- ✅ 任务基本信息展示
- ✅ 流程进度可视化
- ✅ 申请内容展示
- ✅ 审批历史记录
- ✅ 快速操作（同意、驳回、转办、委派）

### 任务审批

- ✅ 快速审批按钮
- ✅ 审批意见输入
- ✅ 附件上传
- ✅ 转办、委派功能

### 流程实例

- ✅ 流程实例列表
- ✅ 流程实例详情
- ✅ 流程历史记录
- ✅ 流程操作（挂起、激活、终止）

## ⚠️ 注意事项

1. **下拉刷新**: 所有列表页都支持下拉刷新和上拉加载
2. **权限控制**: 部分操作需要相应权限
3. **错误处理**: 所有API调用都有错误处理和用户提示
4. **数据缓存**: Store会自动缓存数据，页面切换时保持状态

## 🐛 常见问题

### Q: 列表数据不刷新？
A: 检查 `onShow` 生命周期中是否调用了查询方法。

### Q: 审批提交失败？
A: 检查网络连接和API返回的错误信息。

### Q: 附件上传失败？
A: 检查文件大小和格式是否符合要求。

## 📚 相关文档

- [API文档](../../api/workflow.js)
- [Store文档](../../src/store/workflow.js)

