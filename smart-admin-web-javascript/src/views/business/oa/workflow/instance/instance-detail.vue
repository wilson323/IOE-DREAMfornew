<!--
  * 流程实例详情页
  * 提供工作流流程实例详情查看和管理功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="instance-detail-page">
    <!-- 流程实例基本信息 -->
    <a-card size="small" style="margin-bottom: 16px">
      <a-descriptions :title="instanceDetail.instanceName || '流程实例详情'" :column="4" size="small" bordered>
        <a-descriptions-item label="流程名称">{{ instanceDetail.processName }}</a-descriptions-item>
        <a-descriptions-item label="实例名称">{{ instanceDetail.instanceName }}</a-descriptions-item>
        <a-descriptions-item label="业务Key">{{ instanceDetail.businessKey || '无' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag v-if="instanceDetail.status === 1" color="processing">进行中</a-tag>
          <a-tag v-else-if="instanceDetail.status === 2" color="success">已完成</a-tag>
          <a-tag v-else-if="instanceDetail.status === 3" color="error">已终止</a-tag>
          <a-tag v-else-if="instanceDetail.status === 4" color="warning">已挂起</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="发起人">{{ instanceDetail.startUserName }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ instanceDetail.startTime }}</a-descriptions-item>
        <a-descriptions-item label="结束时间">{{ instanceDetail.endTime || '进行中' }}</a-descriptions-item>
        <a-descriptions-item label="持续时间">
          {{ instanceDetail.duration || '计算中...' }}
        </a-descriptions-item>
      </a-descriptions>
      <template #extra>
        <a-space>
          <a-button
            v-if="instanceDetail.status === 1"
            danger
            @click="handleSuspend"
            v-privilege="'oa:workflow:instance:suspend'"
          >
            挂起
          </a-button>
          <a-button
            v-if="instanceDetail.status === 4"
            @click="handleActivate"
            v-privilege="'oa:workflow:instance:activate'"
          >
            激活
          </a-button>
          <a-button
            v-if="instanceDetail.status === 1"
            danger
            @click="handleTerminate"
            v-privilege="'oa:workflow:instance:terminate'"
          >
            终止
          </a-button>
        </a-space>
      </template>
    </a-card>

    <a-row :gutter="16">
      <!-- 左侧：流程图 -->
      <a-col :span="16">
        <a-card size="small" title="流程图">
          <ProcessDiagram
            v-if="instanceDetail.instanceId"
            :instance-id="instanceDetail.instanceId"
            :highlight-active="true"
          />
          <a-empty v-else description="暂无流程图" />
        </a-card>
      </a-col>

      <!-- 右侧：流程历史 -->
      <a-col :span="8">
        <a-card size="small" title="流程历史">
          <a-timeline v-if="historyList.length > 0">
            <a-timeline-item
              v-for="(item, index) in historyList"
              :key="index"
              :color="getTimelineColor(item.outcome)"
            >
              <p class="timeline-title">{{ item.nodeName }}</p>
              <p class="timeline-content">处理人：{{ item.assigneeName }}</p>
              <p class="timeline-content" v-if="item.comment">处理意见：{{ item.comment }}</p>
              <p class="timeline-time">{{ item.endTime || item.startTime }}</p>
            </a-timeline-item>
          </a-timeline>
          <a-empty v-else description="暂无流程历史" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { message, Modal } from 'ant-design-vue';
  import { CheckCircleOutlined, CloseCircleOutlined, ClockCircleOutlined } from '@ant-design/icons-vue';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { workflowApi } from '/@/api/business/oa/workflow-api';
  import ProcessDiagram from '/@/components/workflow/ProcessDiagram.vue';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { SmartLoading } from '/@/components/framework/smart-loading';

  const route = useRoute();
  const router = useRouter();
  const workflowStore = useWorkflowStore();

  const instanceDetail = ref({});
  const historyList = ref([]);
  const loading = ref(false);

  /**
   * 获取时间线颜色
   * @param {String} outcome - 处理结果
   * @returns {String} 颜色值
   */
  function getTimelineColor(outcome) {
    if (outcome === '1') return 'green';
    if (outcome === '2') return 'red';
    return 'blue';
  }

  /**
   * 查询流程实例详情
   */
  async function queryInstanceDetail() {
    const instanceId = route.query.instanceId;
    if (!instanceId) {
      return;
    }

    try {
      loading.value = true;
      SmartLoading.show();
      await workflowStore.fetchInstanceDetail(instanceId);
      instanceDetail.value = workflowStore.currentInstance || {};

      // 查询流程历史
      await queryProcessHistory(instanceId);
    } catch (err) {
      console.error('查询流程实例详情失败:', err);
      smartSentry.captureError(err);
      message.error('查询流程实例详情失败');
    } finally {
      loading.value = false;
      SmartLoading.hide();
    }
  }

  /**
   * 查询流程历史
   * @param {Number} instanceId - 流程实例ID
   */
  async function queryProcessHistory(instanceId) {
    try {
      await workflowStore.fetchProcessHistory(instanceId);
      historyList.value = workflowStore.processHistory || [];
    } catch (err) {
      console.error('查询流程历史失败:', err);
      smartSentry.captureError(err);
    }
  }

  /**
   * 挂起流程实例
   */
  async function handleSuspend() {
    Modal.confirm({
      title: '提示',
      content: '确认挂起此流程实例吗？',
      onOk: async () => {
        try {
          const response = await workflowApi.suspendInstance(instanceDetail.value.instanceId);
          if (response.code === 1 || response.code === 200) {
            message.success('挂起成功');
            await queryInstanceDetail();
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
   */
  async function handleActivate() {
    try {
      const response = await workflowApi.activateInstance(instanceDetail.value.instanceId);
      if (response.code === 1 || response.code === 200) {
        message.success('激活成功');
        await queryInstanceDetail();
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
   */
  async function handleTerminate() {
    Modal.confirm({
      title: '提示',
      content: '确认终止此流程实例吗？终止后无法恢复。',
      onOk: async () => {
        try {
          const response = await workflowApi.terminateInstance(instanceDetail.value.instanceId);
          if (response.code === 1 || response.code === 200) {
            message.success('终止成功');
            await queryInstanceDetail();
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

  // 初始化
  onMounted(() => {
    queryInstanceDetail();
  });
</script>

<style lang="less" scoped>
  .instance-detail-page {
    padding: 16px;

    .timeline-title {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .timeline-content {
      color: #666;
      margin-bottom: 4px;
      font-size: 12px;
    }

    .timeline-time {
      color: #999;
      font-size: 12px;
    }
  }
</style>

