<template>
  <div class="queue-status-panel">
    <a-spin :spinning="loading">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="队列名称">
          {{ queueStatus?.name || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="消息总数">
          <a-statistic
            :value="queueStatus?.messageCount || 0"
            :value-style="{ fontSize: '16px' }"
          />
        </a-descriptions-item>
        <a-descriptions-item label="消费者数量">
          <a-badge
            :count="queueStatus?.consumerCount || 0"
            :number-style="{ backgroundColor: '#52c41a' }"
          />
        </a-descriptions-item>
        <a-descriptions-item label="队列状态">
          <a-tag :color="getStatusColor(queueStatus?.status)">
            {{ getStatusText(queueStatus?.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ queueStatus?.updateTime || '-' }}
        </a-descriptions-item>
      </a-descriptions>

      <div class="panel-actions">
        <a-button type="primary" size="small" @click="handleRefresh">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue';
import { ReloadOutlined } from '@ant-design/icons-vue';

const props = defineProps({
  queueStatus: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['refresh']);

const loading = ref(false);

const getStatusColor = (status) => {
  const colorMap = {
    running: 'green',
    stopped: 'red',
    idle: 'orange'
  };
  return colorMap[status] || 'default';
};

const getStatusText = (status) => {
  const textMap = {
    running: '运行中',
    stopped: '已停止',
    idle: '空闲'
  };
  return textMap[status] || '未知';
};

const handleRefresh = async () => {
  loading.value = true;
  try {
    await emit('refresh');
  } finally {
    setTimeout(() => {
      loading.value = false;
    }, 500);
  }
};
</script>

<style scoped lang="scss">
.queue-status-panel {
  .panel-actions {
    margin-top: 16px;
    text-align: right;
  }
}
</style>

