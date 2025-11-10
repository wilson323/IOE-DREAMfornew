<!--
  申请详情组件

  @Author:    Claude Code
  @Date:      2025-11-05
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="application-detail">
    <template v-if="data">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="申请编号">
          {{ data.id }}
        </a-descriptions-item>
        <a-descriptions-item label="申请类型">
          <a-tag>{{ data.typeName }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="申请人">
          {{ data.applicant }}
        </a-descriptions-item>
        <a-descriptions-item label="申请部门">
          {{ data.department }}
        </a-descriptions-item>
        <a-descriptions-item label="申请时间">
          {{ data.applyTime }}
        </a-descriptions-item>
        <a-descriptions-item label="申请状态">
          <a-tag :color="getStatusColor(data.status)">
            {{ data.statusName }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="时间范围" :span="2">
          {{ data.timeRange }}
        </a-descriptions-item>
        <a-descriptions-item label="时长">
          {{ data.duration }}
        </a-descriptions-item>
        <a-descriptions-item label="当前审批人">
          {{ data.currentApprover }}
        </a-descriptions-item>
        <a-descriptions-item v-if="data.reason" label="申请原因" :span="2">
          {{ data.reason }}
        </a-descriptions-item>
        <a-descriptions-item v-if="data.attachments" label="附件" :span="2">
          <div class="attachments">
            <div v-for="file in data.attachments" :key="file.name" class="attachment-item">
              <FileOutlined />
              <span>{{ file.name }}</span>
              <span class="file-size">({{ formatFileSize(file.size) }})</span>
            </div>
          </div>
        </a-descriptions-item>
      </a-descriptions>

      <!-- 审批流程 -->
      <div v-if="data.approvalFlow" class="approval-flow">
        <h4 class="flow-title">审批流程</h4>
        <div class="flow-steps">
          <a-steps :current="data.approvalFlow.current" direction="vertical">
            <a-step
              v-for="(step, index) in data.approvalFlow.steps"
              :key="index"
              :title="step.title"
              :description="step.description"
              :status="getStepStatus(step.status)"
            />
          </a-steps>
        </div>
      </div>
    </template>
    <template v-else>
      <a-empty description="暂无数据" />
    </template>
  </div>
</template>

<script setup>
import { FileOutlined } from '@ant-design/icons-vue';

const props = defineProps({
  data: {
    type: Object,
    default: () => ({}),
  },
});

const getStatusColor = (status) => {
  const colors = {
    'pending': 'orange',
    'approved': 'green',
    'rejected': 'red',
    'cancelled': 'default',
  };
  return colors[status] || 'default';
};

const getStepStatus = (status) => {
  const statusMap = {
    'completed': 'finish',
    'current': 'process',
    'pending': 'wait',
    'rejected': 'error',
  };
  return statusMap[status] || 'wait';
};

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
};
</script>

<style lang="less" scoped>
.application-detail {
  .attachments {
    .attachment-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px;
      background: #fafafa;
      border-radius: 4px;
      margin-bottom: 4px;

      &:last-child {
        margin-bottom: 0;
      }

      .file-size {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .approval-flow {
    margin-top: 24px;

    .flow-title {
      margin: 0 0 16px;
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .flow-steps {
      :deep(.ant-steps-item-title) {
        font-size: 14px;
      }
    }
  }
}
</style>
