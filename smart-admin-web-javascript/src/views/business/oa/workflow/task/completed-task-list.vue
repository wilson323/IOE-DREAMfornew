<!--
  * 已办任务列表页
  * 提供工作流已办任务的查询和查看功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="completed-task-list-page">
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

          <a-form-item label="处理结果" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.outcome"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择结果"
            >
              <a-select-option value="1">同意</a-select-option>
              <a-select-option value="2">驳回</a-select-option>
              <a-select-option value="3">转办</a-select-option>
              <a-select-option value="4">委派</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="处理时间" class="smart-query-form-item">
            <a-range-picker
              v-model:value="handleDate"
              :presets="defaultTimeRanges"
              @change="handleDateChange"
              style="width: 220px"
            />
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
      <a-table
        row-key="taskId"
        :columns="tableColumns"
        :data-source="taskList"
        :scroll="{ x: 1400 }"
        :pagination="false"
        :loading="loading"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'taskName'">
            <a @click="handleViewDetail(record.taskId)">{{ text }}</a>
          </template>
          <template v-else-if="column.dataIndex === 'outcome'">
            <a-tag v-if="text === '1'" color="green">同意</a-tag>
            <a-tag v-else-if="text === '2'" color="red">驳回</a-tag>
            <a-tag v-else-if="text === '3'" color="blue">转办</a-tag>
            <a-tag v-else-if="text === '4'" color="orange">委派</a-tag>
            <a-tag v-else color="default">未知</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'comment'">
            <span v-if="text">{{ text }}</span>
            <span v-else style="color: #999">无</span>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
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
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';

  const router = useRouter();
  const workflowStore = useWorkflowStore();

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    category: null,
    outcome: null,
    startDate: null,
    endDate: null,
  };
  const queryForm = reactive({ ...queryFormState });
  const handleDate = ref([]);

  // 表格数据
  const taskList = ref([]);
  const total = ref(0);
  const loading = ref(false);

  // 流程分类列表
  const categoryList = ref([
    { label: '请假申请', value: 'LEAVE' },
    { label: '报销申请', value: 'EXPENSE' },
    { label: '采购申请', value: 'PURCHASE' },
    { label: '合同审批', value: 'CONTRACT' },
    { label: '其他', value: 'OTHER' },
  ]);

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
      title: '处理结果',
      dataIndex: 'outcome',
      width: 100,
    },
    {
      title: '处理意见',
      dataIndex: 'comment',
      width: 200,
      ellipsis: true,
    },
    {
      title: '处理时间',
      dataIndex: 'endTime',
      width: 180,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 100,
    },
  ]);

  /**
   * 处理时间选择变化
   * @param {Array} dates - 日期数组
   * @param {Array} dateStrings - 日期字符串数组
   */
  function handleDateChange(dates, dateStrings) {
    queryForm.startDate = dateStrings[0];
    queryForm.endDate = dateStrings[1];
  }

  /**
   * 查询任务列表
   */
  async function queryTaskList() {
    try {
      loading.value = true;
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        category: queryForm.category,
        outcome: queryForm.outcome,
        startDate: queryForm.startDate,
        endDate: queryForm.endDate,
      };
      await workflowStore.fetchCompletedTaskList(params);
      taskList.value = workflowStore.completedTaskList;
      total.value = workflowStore.completedTaskTotal;
    } catch (err) {
      console.error('查询已办任务列表失败:', err);
      smartSentry.captureError(err);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 点击查询
   */
  function onSearch() {
    queryForm.pageNum = 1;
    queryTaskList();
  }

  /**
   * 点击重置
   */
  function onReload() {
    Object.assign(queryForm, queryFormState);
    handleDate.value = [];
    queryTaskList();
  }

  /**
   * 查看详情
   * @param {Number} taskId - 任务ID
   */
  function handleViewDetail(taskId) {
    router.push({
      path: '/oa/workflow/task/task-detail',
      query: { taskId },
    });
  }

  // 初始化
  onMounted(() => {
    queryTaskList();
  });
</script>

<style lang="less" scoped>
  .completed-task-list-page {
    padding: 16px;

    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

