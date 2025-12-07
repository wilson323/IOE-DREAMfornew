<!--
  * 待办任务列表页
  * 提供工作流待办任务的查询、统计、操作功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="pending-task-list-page">
    <!-- 统计卡片区域 -->
    <a-row :gutter="16" class="statistics-row">
      <a-col :span="6">
        <a-card size="small" class="stat-card urgent">
          <a-statistic title="紧急任务" :value="taskStats.urgent" :value-style="{ color: '#cf1322' }">
            <template #prefix>
              <WarningOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card size="small" class="stat-card high">
          <a-statistic title="高优先级" :value="taskStats.high" :value-style="{ color: '#fa8c16' }">
            <template #prefix>
              <UpOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card size="small" class="stat-card overdue">
          <a-statistic title="已过期" :value="taskStats.overdue" :value-style="{ color: '#ff4d4f' }">
            <template #prefix>
              <ClockCircleOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card size="small" class="stat-card total">
          <a-statistic title="全部任务" :value="taskStats.total" :value-style="{ color: '#1890ff' }">
            <template #prefix>
              <FileTextOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" v-privilege="'oa:workflow:task:query'">
        <a-row class="smart-query-form-row">
          <a-form-item label="流程分类" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.category"
              style="width: 150px"
              :show-search="true"
              :allow-clear="true"
              placeholder="请选择分类"
            >
              <a-select-option v-for="item in categoryList" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="优先级" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.priority"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择优先级"
            >
              <a-select-option :value="1">低</a-select-option>
              <a-select-option :value="2">普通</a-select-option>
              <a-select-option :value="3">高</a-select-option>
              <a-select-option :value="4">紧急</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="到期状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.dueStatus"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择状态"
            >
              <a-select-option value="OVERDUE">已过期</a-select-option>
              <a-select-option value="DUE_SOON">即将到期</a-select-option>
              <a-select-option value="NORMAL">正常</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-button-group>
              <a-button type="primary" @click="onSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                查询
              </a-button>
              <a-button @click="onReload">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </a-button-group>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 任务列表区域 -->
    <a-card size="small" :bordered="false" class="task-list-card">
      <a-row class="smart-table-btn-block">
        <div class="smart-table-operate-block">
          <a-button
            type="primary"
            :disabled="selectedTaskIds.length === 0"
            @click="handleBatchClaim"
            v-privilege="'oa:workflow:task:claim'"
          >
            <template #icon>
              <CheckOutlined />
            </template>
            批量受理
          </a-button>
          <a-button
            :disabled="selectedTaskIds.length === 0"
            @click="handleBatchTransfer"
            v-privilege="'oa:workflow:task:transfer'"
          >
            <template #icon>
              <SwapOutlined />
            </template>
            批量转办
          </a-button>
        </div>
      </a-row>

      <a-table
        row-key="taskId"
        :columns="tableColumns"
        :data-source="taskList"
        :scroll="{ x: 1400 }"
        :pagination="false"
        :loading="loading"
        :row-selection="rowSelection"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'taskName'">
            <a @click="handleViewDetail(record.taskId)">{{ text }}</a>
          </template>
          <template v-else-if="column.dataIndex === 'priority'">
            <a-tag v-if="text === 4" color="red">紧急</a-tag>
            <a-tag v-else-if="text === 3" color="orange">高</a-tag>
            <a-tag v-else-if="text === 2" color="blue">普通</a-tag>
            <a-tag v-else color="default">低</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'dueDate'">
            <span :class="{ 'overdue-text': record.isOverdue }">{{ text || '无期限' }}</span>
            <a-tag v-if="record.isOverdue" color="red" style="margin-left: 8px">已过期</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
              <a-button
                v-if="!record.claimed"
                type="link"
                size="small"
                @click="handleClaim(record.taskId)"
                v-privilege="'oa:workflow:task:claim'"
              >
                受理
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleTransfer(record.taskId)"
                v-privilege="'oa:workflow:task:transfer'"
              >
                转办
              </a-button>
              <a-button
                type="link"
                size="small"
                @click="handleDelegate(record.taskId)"
                v-privilege="'oa:workflow:task:delegate'"
              >
                委派
              </a-button>
              <a-button type="link" size="small" @click="handleViewDetail(record.taskId)">详情</a-button>
            </div>
          </template>
        </template>
      </a-table>

      <div class="smart-query-table-page">
        <a-pagination
          show-size-changer
          show-quick-jumper
          show-less-items
          :page-size-options="PAGE_SIZE_OPTIONS"
          :default-page-size="queryForm.pageSize"
          v-model:current="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          @change="queryTaskList"
          :show-total="(total) => `共${total}条`"
        />
      </div>
    </a-card>

    <!-- 转办/委派对话框 -->
    <a-modal
      v-model:open="transferModalVisible"
      title="转办任务"
      @ok="handleTransferConfirm"
      @cancel="handleTransferCancel"
    >
      <a-form :model="transferForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="目标用户" required>
          <a-select
            v-model:value="transferForm.targetUserId"
            placeholder="请选择目标用户"
            :show-search="true"
            :filter-option="filterUserOption"
          >
            <a-select-option v-for="user in userList" :key="user.userId" :value="user.userId">
              {{ user.userName }} ({{ user.employeeName }})
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
  import { reactive, ref, computed, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { useRouter } from 'vue-router';
  import {
    SearchOutlined,
    ReloadOutlined,
    CheckOutlined,
    SwapOutlined,
    WarningOutlined,
    UpOutlined,
    ClockCircleOutlined,
    FileTextOutlined,
  } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { employeeApi } from '/@/api/system/employee-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const router = useRouter();
  const workflowStore = useWorkflowStore();

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    category: null,
    priority: null,
    dueStatus: null,
  };
  const queryForm = reactive({ ...queryFormState });

  // 表格数据
  const taskList = ref([]);
  const total = ref(0);
  const loading = ref(false);
  const selectedTaskIds = ref([]);

  // 转办/委派相关
  const transferModalVisible = ref(false);
  const transferForm = reactive({
    taskId: null,
    targetUserId: null,
    type: 'transfer', // transfer 转办, delegate 委派
  });
  const userList = ref([]);

  // 流程分类列表
  const categoryList = ref([
    { label: '请假申请', value: 'LEAVE' },
    { label: '报销申请', value: 'EXPENSE' },
    { label: '采购申请', value: 'PURCHASE' },
    { label: '合同审批', value: 'CONTRACT' },
    { label: '其他', value: 'OTHER' },
  ]);

  // 统计信息
  const taskStats = computed(() => {
    return workflowStore.pendingTaskStats;
  });

  // 表格列定义
  const tableColumns = ref([
    {
      title: '任务名称',
      dataIndex: 'taskName',
      width: 250,
      ellipsis: true,
    },
    {
      title: '流程名称',
      dataIndex: 'processName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '发起人',
      dataIndex: 'startUserName',
      width: 100,
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      width: 100,
    },
    {
      title: '到期时间',
      dataIndex: 'dueDate',
      width: 180,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 200,
    },
  ]);

  // 行选择配置
  const rowSelection = {
    selectedRowKeys: selectedTaskIds,
    onChange: (selectedKeys) => {
      selectedTaskIds.value = selectedKeys;
    },
  };

  // 查询任务列表
  async function queryTaskList() {
    try {
      loading.value = true;
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        category: queryForm.category,
        priority: queryForm.priority,
        dueStatus: queryForm.dueStatus,
      };
      await workflowStore.fetchPendingTaskList(params);
      taskList.value = workflowStore.pendingTaskList;
      total.value = workflowStore.pendingTaskTotal;
    } catch (err) {
      console.error('查询待办任务列表失败:', err);
      smartSentry.captureError(err);
      message.error('查询待办任务列表失败');
    } finally {
      loading.value = false;
    }
  }

  // 点击查询
  function onSearch() {
    queryForm.pageNum = 1;
    queryTaskList();
  }

  // 点击重置
  function onReload() {
    Object.assign(queryForm, queryFormState);
    queryTaskList();
  }

  // 查看详情
  function handleViewDetail(taskId) {
    router.push({
      path: '/oa/workflow/task/task-detail',
      query: { taskId },
    });
  }

  // 受理任务
  async function handleClaim(taskId) {
    try {
      const success = await workflowStore.claimTask(taskId);
      if (success) {
        await queryTaskList();
      }
    } catch (err) {
      console.error('受理任务失败:', err);
      smartSentry.captureError(err);
    }
  }

  // 转办任务
  async function handleTransfer(taskId) {
    transferForm.taskId = taskId;
    transferForm.type = 'transfer';
    transferModalVisible.value = true;
    await loadUserList();
  }

  // 委派任务
  async function handleDelegate(taskId) {
    transferForm.taskId = taskId;
    transferForm.type = 'delegate';
    transferModalVisible.value = true;
    await loadUserList();
  }

  // 批量受理
  async function handleBatchClaim() {
    if (selectedTaskIds.value.length === 0) {
      message.warning('请选择要受理的任务');
      return;
    }
    Modal.confirm({
      title: '提示',
      content: `确认受理选中的 ${selectedTaskIds.value.length} 个任务吗？`,
      onOk: async () => {
        try {
          let successCount = 0;
          for (const taskId of selectedTaskIds.value) {
            const success = await workflowStore.claimTask(taskId);
            if (success) successCount++;
          }
          message.success(`成功受理 ${successCount} 个任务`);
          selectedTaskIds.value = [];
          await queryTaskList();
        } catch (err) {
          console.error('批量受理失败:', err);
          smartSentry.captureError(err);
        }
      },
    });
  }

  // 批量转办
  function handleBatchTransfer() {
    if (selectedTaskIds.value.length === 0) {
      message.warning('请选择要转办的任务');
      return;
    }
    message.info('批量转办功能开发中');
  }

  // 加载用户列表
  async function loadUserList() {
    try {
      const result = await employeeApi.queryAll();
      if (result.code === 1 || result.code === 200) {
        // 将员工数据转换为用户列表格式
        userList.value = (result.data || []).map((employee) => ({
          userId: employee.employeeId,
          userName: employee.loginName || employee.actualName,
          employeeName: employee.actualName,
        }));
      }
    } catch (err) {
      console.error('加载用户列表失败:', err);
      smartSentry.captureError(err);
      message.error('加载用户列表失败');
    }
  }

  // 用户选择过滤
  function filterUserOption(input, option) {
    return option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  }

  // 转办/委派确认
  async function handleTransferConfirm() {
    if (!transferForm.targetUserId) {
      message.warning('请选择目标用户');
      return;
    }
    try {
      let success = false;
      if (transferForm.type === 'transfer') {
        success = await workflowStore.transferTask(transferForm.taskId, transferForm.targetUserId);
      } else {
        success = await workflowStore.delegateTask(transferForm.taskId, transferForm.targetUserId);
      }
      if (success) {
        handleTransferCancel();
        await queryTaskList();
      }
    } catch (err) {
      console.error('转办/委派任务失败:', err);
      smartSentry.captureError(err);
    }
  }

  // 转办/委派取消
  function handleTransferCancel() {
    transferModalVisible.value = false;
    transferForm.taskId = null;
    transferForm.targetUserId = null;
    transferForm.type = 'transfer';
  }

  // 初始化
  onMounted(() => {
    queryTaskList();
  });
</script>

<style lang="less" scoped>
  .pending-task-list-page {
    padding: 16px;

    .statistics-row {
      margin-bottom: 16px;

      .stat-card {
        text-align: center;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        }
      }
    }

    .query-card {
      margin-bottom: 16px;
    }

    .task-list-card {
      .overdue-text {
        color: #ff4d4f;
      }
    }
  }
</style>

