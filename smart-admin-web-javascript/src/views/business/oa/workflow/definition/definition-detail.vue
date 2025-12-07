<!--
  * 流程定义详情页
  * 提供工作流流程定义的详细信息查看和编辑功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="definition-detail-page">
    <a-spin :spinning="loading">
      <a-card size="small" style="margin-bottom: 16px">
        <a-descriptions
          :title="definitionDetail.processName || '流程定义详情'"
          :column="4"
          size="small"
          bordered
        >
          <template #extra>
            <a-space>
              <a-button
                v-if="definitionDetail.status === 'DISABLED'"
                @click="handleActivate"
                v-privilege="'oa:workflow:definition:activate'"
              >
                激活
              </a-button>
              <a-button
                v-if="definitionDetail.status === 'PUBLISHED'"
                danger
                @click="handleDisable"
                v-privilege="'oa:workflow:definition:disable'"
              >
                禁用
              </a-button>
            </a-space>
          </template>
          <a-descriptions-item label="流程Key">{{ definitionDetail.processKey }}</a-descriptions-item>
          <a-descriptions-item label="流程名称">{{ definitionDetail.processName }}</a-descriptions-item>
          <a-descriptions-item label="版本">{{ definitionDetail.version }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag v-if="definitionDetail.status === 'PUBLISHED'" color="success">已发布</a-tag>
            <a-tag v-else-if="definitionDetail.status === 'DRAFT'" color="default">草稿</a-tag>
            <a-tag v-else-if="definitionDetail.status === 'DISABLED'" color="error">已禁用</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="分类">{{ definitionDetail.category || '未分类' }}</a-descriptions-item>
          <a-descriptions-item label="描述" :span="3">
            {{ definitionDetail.description || '无描述' }}
          </a-descriptions-item>
          <a-descriptions-item label="部署时间">{{ definitionDetail.deployTime }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ definitionDetail.createTime }}</a-descriptions-item>
          <a-descriptions-item label="实例数量">{{ definitionDetail.instanceCount || 0 }}</a-descriptions-item>
        </a-descriptions>
      </a-card>

      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="diagram" tab="流程图">
          <a-card size="small">
            <ProcessDiagram
              v-if="definitionDetail.bpmnXml"
              :bpmn-xml="definitionDetail.bpmnXml"
            />
            <a-empty v-else description="暂无流程图数据" />
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="form" tab="表单定义">
          <a-card size="small">
            <a-empty v-if="!definitionDetail.formDefinition" description="暂无表单定义" />
            <pre v-else style="max-height: 600px; overflow: auto; padding: 16px; background: #f5f5f5; border-radius: 4px;">{{ formatJson(definitionDetail.formDefinition) }}</pre>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="variables" tab="变量定义">
          <a-card size="small">
            <a-empty v-if="!definitionDetail.variableDefinition" description="暂无变量定义" />
            <pre v-else style="max-height: 600px; overflow: auto; padding: 16px; background: #f5f5f5; border-radius: 4px;">{{ formatJson(definitionDetail.variableDefinition) }}</pre>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="instances" tab="流程实例">
          <a-card size="small">
            <a-table
              :columns="instanceColumns"
              :data-source="instanceList"
              :pagination="instancePagination"
              size="small"
              @change="handleInstanceTableChange"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'instanceName'">
                  <a @click="handleViewInstance(record.instanceId)">{{ record.instanceName }}</a>
                </template>
                <template v-else-if="column.dataIndex === 'status'">
                  <a-tag v-if="record.status === 1" color="processing">进行中</a-tag>
                  <a-tag v-else-if="record.status === 2" color="success">已完成</a-tag>
                  <a-tag v-else-if="record.status === 3" color="error">已终止</a-tag>
                  <a-tag v-else-if="record.status === 4" color="warning">已挂起</a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-tab-pane>
      </a-tabs>
    </a-spin>
  </div>
</template>

<script setup>
  import { ref, reactive, onMounted, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { workflowApi } from '/@/api/business/oa/workflow-api';
  import ProcessDiagram from '/@/components/workflow/ProcessDiagram.vue';
  import { smartSentry } from '/@/lib/smart-sentry';

  const route = useRoute();
  const router = useRouter();
  const workflowStore = useWorkflowStore();

  const definitionDetail = ref({});
  const loading = ref(false);
  const activeTab = ref('diagram');
  const instanceList = ref([]);
  const instancePagination = reactive({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const instanceColumns = ref([
    {
      title: '实例名称',
      dataIndex: 'instanceName',
      width: 200,
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
  ]);

  /**
   * 查询流程定义详情
   */
  async function queryDefinitionDetail() {
    const definitionId = route.query.definitionId;
    if (!definitionId) {
      return;
    }

    try {
      loading.value = true;
      await workflowStore.fetchDefinitionDetail(definitionId);
      definitionDetail.value = workflowStore.currentDefinition || {};

      // 如果当前在实例标签页，加载实例列表
      if (activeTab.value === 'instances') {
        await queryInstanceList();
      }
    } catch (err) {
      console.error('查询流程定义详情失败:', err);
      smartSentry.captureError(err);
      message.error('查询流程定义详情失败');
    } finally {
      loading.value = false;
    }
  }

  /**
   * 查询流程实例列表
   */
  async function queryInstanceList() {
    try {
      const response = await workflowApi.pageInstances({
        pageNum: instancePagination.current,
        pageSize: instancePagination.pageSize,
        definitionId: definitionDetail.value.definitionId,
      });

      if (response.code === 1 || response.code === 200) {
        instanceList.value = response.data?.list || [];
        instancePagination.total = response.data?.total || 0;
      }
    } catch (err) {
      console.error('查询流程实例列表失败:', err);
      smartSentry.captureError(err);
    }
  }

  /**
   * 格式化JSON
   * @param {String|Object} data - JSON数据
   * @returns {String}
   */
  function formatJson(data) {
    if (!data) return '';
    try {
      const obj = typeof data === 'string' ? JSON.parse(data) : data;
      return JSON.stringify(obj, null, 2);
    } catch (e) {
      return String(data);
    }
  }

  /**
   * 激活流程定义
   */
  async function handleActivate() {
    try {
      const response = await workflowApi.activateDefinition(definitionDetail.value.definitionId);
      if (response.code === 1 || response.code === 200) {
        message.success('激活成功');
        await queryDefinitionDetail();
      } else {
        message.error(response.message || '激活失败');
      }
    } catch (err) {
      console.error('激活流程定义失败:', err);
      smartSentry.captureError(err);
      message.error('激活流程定义失败');
    }
  }

  /**
   * 禁用流程定义
   */
  async function handleDisable() {
    try {
      const response = await workflowApi.disableDefinition(definitionDetail.value.definitionId);
      if (response.code === 1 || response.code === 200) {
        message.success('禁用成功');
        await queryDefinitionDetail();
      } else {
        message.error(response.message || '禁用失败');
      }
    } catch (err) {
      console.error('禁用流程定义失败:', err);
      smartSentry.captureError(err);
      message.error('禁用流程定义失败');
    }
  }

  /**
   * 查看流程实例
   * @param {Number} instanceId - 流程实例ID
   */
  function handleViewInstance(instanceId) {
    router.push({
      path: '/oa/workflow/instance/instance-detail',
      query: { instanceId },
    });
  }

  /**
   * 流程实例表格变化
   * @param {Object} pagination - 分页信息
   */
  function handleInstanceTableChange(pagination) {
    instancePagination.current = pagination.current;
    instancePagination.pageSize = pagination.pageSize;
    queryInstanceList();
  }

  /**
   * 标签页切换
   * @param {String} key - 标签页key
   */
  function handleTabChange(key) {
    activeTab.value = key;
    if (key === 'instances') {
      queryInstanceList();
    }
  }

  // 监听标签页切换
  watch(activeTab, (newKey) => {
    if (newKey === 'instances' && definitionDetail.value.definitionId) {
      queryInstanceList();
    }
  });

  onMounted(() => {
    queryDefinitionDetail();
  });
</script>

<style lang="less" scoped>
  .definition-detail-page {
    padding: 16px;
  }
</style>

