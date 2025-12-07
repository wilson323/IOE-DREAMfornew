<!--
  * 流程实例列表页
  * 提供工作流流程实例的查询和管理功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="instance-list-page">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" v-privilege="'oa:workflow:instance:query'">
        <a-row class="smart-query-form-row">
          <a-form-item label="流程定义" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.definitionId"
              style="width: 200px"
              :show-search="true"
              :allow-clear="true"
              placeholder="请选择流程定义"
            >
              <a-select-option v-for="item in definitionList" :key="item.definitionId" :value="item.definitionId">
                {{ item.processName }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择状态"
            >
              <a-select-option :value="1">进行中</a-select-option>
              <a-select-option :value="2">已完成</a-select-option>
              <a-select-option :value="3">已终止</a-select-option>
              <a-select-option :value="4">已挂起</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="发起人" class="smart-query-form-item">
            <a-input style="width: 120px" v-model:value="queryForm.startUserName" placeholder="发起人" />
          </a-form-item>

          <a-form-item label="开始时间" class="smart-query-form-item">
            <a-range-picker
              v-model:value="startDate"
              :presets="defaultTimeRanges"
              @change="startDateChange"
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

    <!-- 流程实例列表区域 -->
    <a-card size="small" :bordered="false" class="instance-list-card">
      <a-table
        row-key="instanceId"
        :columns="tableColumns"
        :data-source="instanceList"
        :scroll="{ x: 1400 }"
        :pagination="false"
        :loading="loading"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'instanceName'">
            <a @click="handleViewDetail(record.instanceId)">{{ text }}</a>
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-tag v-if="text === 1" color="processing">进行中</a-tag>
            <a-tag v-else-if="text === 2" color="success">已完成</a-tag>
            <a-tag v-else-if="text === 3" color="error">已终止</a-tag>
            <a-tag v-else-if="text === 4" color="warning">已挂起</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
              <a-button type="link" size="small" @click="handleViewDetail(record.instanceId)">详情</a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                danger
                @click="handleSuspend(record.instanceId)"
                v-privilege="'oa:workflow:instance:suspend'"
              >
                挂起
              </a-button>
              <a-button
                v-if="record.status === 4"
                type="link"
                size="small"
                @click="handleActivate(record.instanceId)"
                v-privilege="'oa:workflow:instance:activate'"
              >
                激活
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                danger
                @click="handleTerminate(record.instanceId)"
                v-privilege="'oa:workflow:instance:terminate'"
              >
                终止
              </a-button>
              <a-button
                v-if="record.status === 1"
                type="link"
                size="small"
                danger
                @click="handleRevoke(record.instanceId)"
                v-privilege="'oa:workflow:instance:revoke'"
              >
                撤销
              </a-button>
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
          @change="queryInstanceList"
          :show-total="(total) => `共${total}条`"
        />
      </div>
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { message, Modal } from 'ant-design-vue';
  import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { workflowApi } from '/@/api/business/oa/workflow-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { defaultTimeRanges } from '/@/lib/default-time-ranges';

  const router = useRouter();
  const workflowStore = useWorkflowStore();

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    definitionId: null,
    status: null,
    startUserId: null,
    startUserName: '',
    startDate: null,
    endDate: null,
  };
  const queryForm = reactive({ ...queryFormState });
  const startDate = ref([]);

  // 表格数据
  const instanceList = ref([]);
  const total = ref(0);
  const loading = ref(false);
  const definitionList = ref([]);

  // 表格列定义
  const tableColumns = ref([
    {
      title: '实例名称',
      dataIndex: 'instanceName',
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
      title: '状态',
      dataIndex: 'status',
      width: 100,
    },
    {
      title: '发起人',
      dataIndex: 'startUserName',
      width: 100,
    },
    {
      title: '开始时间',
      dataIndex: 'startTime',
      width: 180,
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      width: 180,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 300,
    },
  ]);

  /**
   * 开始时间选择变化
   * @param {Array} dates - 日期数组
   * @param {Array} dateStrings - 日期字符串数组
   */
  function startDateChange(dates, dateStrings) {
    queryForm.startDate = dateStrings[0];
    queryForm.endDate = dateStrings[1];
  }

  /**
   * 查询流程实例列表
   */
  async function queryInstanceList() {
    try {
      loading.value = true;
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        definitionId: queryForm.definitionId,
        status: queryForm.status,
        startUserId: queryForm.startUserId,
        startDate: queryForm.startDate,
        endDate: queryForm.endDate,
      };
      await workflowStore.fetchInstanceList(params);
      instanceList.value = workflowStore.instanceList;
      total.value = workflowStore.instanceTotal;
    } catch (err) {
      console.error('查询流程实例列表失败:', err);
      smartSentry.captureError(err);
      message.error('查询流程实例列表失败');
    } finally {
      loading.value = false;
    }
  }

  /**
   * 加载流程定义列表
   */
  async function loadDefinitionList() {
    try {
      const response = await workflowApi.pageDefinitions({ pageNum: 1, pageSize: 1000 });
      if (response.code === 1 || response.code === 200) {
        definitionList.value = response.data?.list || [];
      }
    } catch (err) {
      console.error('加载流程定义列表失败:', err);
      smartSentry.captureError(err);
    }
  }

  /**
   * 点击查询
   */
  function onSearch() {
    queryForm.pageNum = 1;
    queryInstanceList();
  }

  /**
   * 点击重置
   */
  function onReload() {
    Object.assign(queryForm, queryFormState);
    startDate.value = [];
    queryInstanceList();
  }

  /**
   * 查看详情
   * @param {Number} instanceId - 流程实例ID
   */
  function handleViewDetail(instanceId) {
    router.push({
      path: '/oa/workflow/instance/instance-detail',
      query: { instanceId },
    });
  }

  /**
   * 挂起流程实例
   * @param {Number} instanceId - 流程实例ID
   */
  async function handleSuspend(instanceId) {
    Modal.confirm({
      title: '提示',
      content: '确认挂起此流程实例吗？',
      onOk: async () => {
        try {
          const response = await workflowApi.suspendInstance(instanceId);
          if (response.code === 1 || response.code === 200) {
            message.success('挂起成功');
            await queryInstanceList();
          } else {
            message.error(response.message || '挂起失败');
          }
        } catch (err) {
          console.error('挂起流程实例失败:', err);
          smartSentry.captureError(err);
          message.error('挂起流程实例失败');
        }
      },
    });
  }

  /**
   * 激活流程实例
   * @param {Number} instanceId - 流程实例ID
   */
  async function handleActivate(instanceId) {
    try {
      const response = await workflowApi.activateInstance(instanceId);
      if (response.code === 1 || response.code === 200) {
        message.success('激活成功');
        await queryInstanceList();
      } else {
        message.error(response.message || '激活失败');
      }
    } catch (err) {
      console.error('激活流程实例失败:', err);
      smartSentry.captureError(err);
      message.error('激活流程实例失败');
    }
  }

  /**
   * 终止流程实例
   * @param {Number} instanceId - 流程实例ID
   */
  async function handleTerminate(instanceId) {
    Modal.confirm({
      title: '提示',
      content: '确认终止此流程实例吗？终止后无法恢复。',
      onOk: async () => {
        try {
          const response = await workflowApi.terminateInstance(instanceId);
          if (response.code === 1 || response.code === 200) {
            message.success('终止成功');
            await queryInstanceList();
          } else {
            message.error(response.message || '终止失败');
          }
        } catch (err) {
          console.error('终止流程实例失败:', err);
          smartSentry.captureError(err);
          message.error('终止流程实例失败');
        }
      },
    });
  }

  /**
   * 撤销流程实例
   * @param {Number} instanceId - 流程实例ID
   */
  async function handleRevoke(instanceId) {
    Modal.confirm({
      title: '提示',
      content: '确认撤销此流程实例吗？',
      onOk: async () => {
        try {
          const response = await workflowApi.revokeInstance(instanceId);
          if (response.code === 1 || response.code === 200) {
            message.success('撤销成功');
            await queryInstanceList();
          } else {
            message.error(response.message || '撤销失败');
          }
        } catch (err) {
          console.error('撤销流程实例失败:', err);
          smartSentry.captureError(err);
          message.error('撤销流程实例失败');
        }
      },
    });
  }

  // 初始化
  onMounted(() => {
    loadDefinitionList();
    queryInstanceList();
  });
</script>

<style lang="less" scoped>
  .instance-list-page {
    padding: 16px;

    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

