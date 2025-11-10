<!--
  智能视频-联动记录页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="联动记录" :bordered="false">
      <template #extra>
        <a-space>
          <a-select v-model:value="filterStatus" placeholder="执行状态" style="width: 120px;" @change="handleSearch">
            <a-select-option value="">全部</a-select-option>
            <a-select-option value="success">成功</a-select-option>
            <a-select-option value="failed">失败</a-select-option>
            <a-select-option value="pending">待执行</a-select-option>
          </a-select>
          <a-range-picker v-model:value="dateRange" @change="handleSearch" />
          <a-button type="primary" @click="handleSearch">
            <SearchOutlined />
            查询
          </a-button>
          <a-button @click="handleExport">
            <ExportOutlined />
            导出
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge :status="getStatusBadge(record.status)" :text="record.statusText" />
          </template>

          <template v-if="column.key === 'executeTime'">
            <span>{{ record.executeTime || '-' }}</span>
          </template>

          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
              <a-button type="link" size="small" v-if="record.status === 'failed'" @click="handleRetry(record)">
                重试
              </a-button>
            </a-space>
          </template>
        </template>

        <template #expandedRowRender="{ record }">
          <a-descriptions :column="2" size="small" bordered>
            <a-descriptions-item label="触发条件">
              {{ record.triggerCondition }}
            </a-descriptions-item>
            <a-descriptions-item label="执行动作">
              {{ record.executeAction }}
            </a-descriptions-item>
            <a-descriptions-item label="执行时长">
              {{ record.executeDuration }}ms
            </a-descriptions-item>
            <a-descriptions-item label="重试次数">
              {{ record.retryCount }}次
            </a-descriptions-item>
            <a-descriptions-item label="失败原因" :span="2" v-if="record.status === 'failed'">
              <a-tag color="red">{{ record.failReason }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { SearchOutlined, ExportOutlined } from '@ant-design/icons-vue';

const loading = ref(false);
const filterStatus = ref('');
const dateRange = ref([]);

const columns = [
  { title: '规则名称', dataIndex: 'ruleName', key: 'ruleName' },
  { title: '触发设备', dataIndex: 'triggerDevice', key: 'triggerDevice' },
  { title: '联动设备', dataIndex: 'linkageDevice', key: 'linkageDevice' },
  { title: '触发时间', dataIndex: 'triggerTime', key: 'triggerTime' },
  { title: '执行时间', key: 'executeTime' },
  { title: '状态', key: 'status' },
  { title: '操作', key: 'action', width: 150 },
];

const dataSource = ref([
  {
    id: 1,
    ruleName: '入侵检测联动告警',
    triggerDevice: '前门摄像头-001',
    linkageDevice: '报警器-01, 闪光灯-01',
    triggerTime: '2024-11-06 15:32:10',
    executeTime: '2024-11-06 15:32:11',
    status: 'success',
    statusText: '执行成功',
    triggerCondition: '检测到可疑人员',
    executeAction: '触发报警器，开启闪光灯',
    executeDuration: 850,
    retryCount: 0,
  },
  {
    id: 2,
    ruleName: '火灾检测联动疏散',
    triggerDevice: '烟雾探测器-01',
    linkageDevice: '疏散广播, 应急照明',
    triggerTime: '2024-11-06 14:28:35',
    executeTime: '2024-11-06 14:28:36',
    status: 'success',
    statusText: '执行成功',
    triggerCondition: '检测到烟雾',
    executeAction: '启动疏散广播，开启应急照明',
    executeDuration: 1200,
    retryCount: 0,
  },
  {
    id: 3,
    ruleName: '车辆违停联动通知',
    triggerDevice: '停车场摄像头-01',
    linkageDevice: '车主通知系统',
    triggerTime: '2024-11-06 13:15:20',
    executeTime: null,
    status: 'failed',
    statusText: '执行失败',
    triggerCondition: '检测到车辆违停',
    executeAction: '发送通知给车主',
    executeDuration: 0,
    retryCount: 2,
    failReason: '通知系统连接超时',
  },
  {
    id: 4,
    ruleName: '入侵检测联动告警',
    triggerDevice: '前门摄像头-001',
    linkageDevice: '报警器-01, 闪光灯-01',
    triggerTime: '2024-11-06 12:45:08',
    executeTime: null,
    status: 'pending',
    statusText: '待执行',
    triggerCondition: '检测到可疑人员',
    executeAction: '触发报警器，开启闪光灯',
    executeDuration: 0,
    retryCount: 0,
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 4,
  showTotal: (total) => `共 ${total} 条`,
});

const getStatusBadge = (status) => {
  const statusMap = {
    'success': 'success',
    'failed': 'error',
    'pending': 'processing',
  };
  return statusMap[status] || 'default';
};

const handleSearch = () => {
  console.log('查询记录:', filterStatus.value, dateRange.value);
};

const handleExport = () => {
  console.log('导出记录');
};

const handleViewDetail = (record) => {
  console.log('查看详情:', record);
};

const handleRetry = (record) => {
  console.log('重试执行:', record);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;
}
</style>
