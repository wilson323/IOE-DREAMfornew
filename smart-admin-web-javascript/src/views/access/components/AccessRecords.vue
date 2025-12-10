<!--
  * Access Records Component
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM
-->
<template>
  <div class="access-records">
    <a-card :bordered="false" title="Recent Access Records">
      <template #extra>
        <a-space>
          <a-select v-model:value="timeRange" style="width: 120px" @change="handleTimeRangeChange">
            <a-select-option value="1h">Last Hour</a-select-option>
            <a-select-option value="24h">Last 24h</a-select-option>
            <a-select-option value="7d">Last 7 days</a-select-option>
          </a-select>
          <a-button @click="$emit('refresh')" :loading="loading">
            <template #icon><ReloadOutlined /></template>
            Refresh
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="records"
        :pagination="{ pageSize: 10, showSizeChanger: false }"
        size="small"
        :loading="loading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userInfo'">
            <div class="user-info">
              <a-avatar
                :src="record.userAvatar"
                :size="24"
                style="margin-right: 8px;"
              >
                {{ record.userName?.charAt(0) }}
              </a-avatar>
              <div class="user-details">
                <div class="user-name">{{ record.userName }}</div>
                <div class="user-code">{{ record.userCode }}</div>
              </div>
            </div>
          </template>

          <template v-else-if="column.key === 'result'">
            <a-tag :color="record.result === 'SUCCESS' ? 'green' : 'red'">
              <template #icon>
                <component :is="record.result === 'SUCCESS' ? 'CheckCircleOutlined' : 'CloseCircleOutlined'" />
              </template>
              {{ record.result === 'SUCCESS' ? 'Success' : 'Failed' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleViewDetails(record)">
              Details
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    ReloadOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
  } from '@ant-design/icons-vue';

  // 定义 props
  const props = defineProps({
    records: {
      type: Array,
      default: () => [],
    },
  });

  // 定义 emits
  const emit = defineEmits(['refresh']);

  const loading = ref(false);
  const timeRange = ref('24h');

  const columns = [
    { title: 'User', key: 'userInfo', width: 180 },
    { title: 'Device', dataIndex: 'deviceName', key: 'deviceName', width: 120 },
    { title: 'Area', dataIndex: 'areaName', key: 'areaName', width: 120 },
    { title: 'Result', key: 'result', width: 100 },
    { title: 'Time', dataIndex: 'accessTime', key: 'accessTime', width: 160 },
    { title: 'Action', key: 'action', width: 100 },
  ];

  const handleTimeRangeChange = (value) => {
    // TODO: 根据时间范围过滤记录
    console.log('Time range changed:', value);
  };

  const handleViewDetails = (record) => {
    message.info(`View details for ${record.userName} - ${record.accessTime}`);
  };

  onMounted(() => {
    // 组件挂载时的初始化逻辑
  });
</script>

<style lang="less" scoped>
  .access-records {
    .user-info {
      display: flex;
      align-items: center;

      .user-details {
        display: flex;
        flex-direction: column;
        line-height: 1.2;
      }
    }
  }
</style>