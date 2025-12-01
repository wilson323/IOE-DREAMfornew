<!--
  智能视频-行为分析页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="行为分析" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="24">
          <a-card title="行为类型统计" :bordered="false" size="small">
            <a-row :gutter="16">
              <a-col :span="6" v-for="item in behaviorStats" :key="item.type">
                <a-statistic
                  :title="item.name"
                  :value="item.count"
                  suffix="次"
                  :value-style="{ color: item.color }"
                />
              </a-col>
            </a-row>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="24">
          <a-card title="行为检测记录" :bordered="false">
            <template #extra>
              <a-space>
                <a-select v-model:value="behaviorType" placeholder="行为类型" style="width: 150px;">
                  <a-select-option value="">全部</a-select-option>
                  <a-select-option value="fighting">打架斗殴</a-select-option>
                  <a-select-option value="running">快速奔跑</a-select-option>
                  <a-select-option value="falling">跌倒</a-select-option>
                  <a-select-option value="climbing">攀爬</a-select-option>
                </a-select>
                <a-button type="primary" @click="handleSearch">
                  <SearchOutlined />
                  查询
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
                <template v-if="column.key === 'level'">
                  <a-tag :color="getLevelColor(record.level)">{{ record.level }}</a-tag>
                </template>

                <template v-if="column.key === 'status'">
                  <a-badge :status="getStatusBadge(record.status)" :text="record.status" />
                </template>

                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
                    <a-button type="link" size="small" @click="handleProcess(record)">处理</a-button>
                  </a-space>
                </template>
              </template>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { SearchOutlined } from '@ant-design/icons-vue';

const loading = ref(false);
const behaviorType = ref('');

const behaviorStats = ref([
  { type: 'fighting', name: '打架斗殴', count: 5, color: '#ff4d4f' },
  { type: 'running', name: '快速奔跑', count: 23, color: '#faad14' },
  { type: 'falling', name: '跌倒', count: 8, color: '#ff4d4f' },
  { type: 'climbing', name: '攀爬', count: 12, color: '#faad14' },
]);

const columns = [
  { title: '行为类型', dataIndex: 'behaviorType', key: 'behaviorType' },
  { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName' },
  { title: '位置', dataIndex: 'location', key: 'location' },
  { title: '危险等级', key: 'level' },
  { title: '状态', key: 'status' },
  { title: '检测时间', dataIndex: 'detectTime', key: 'detectTime' },
  { title: '操作', key: 'action', width: 180 },
];

const dataSource = ref([
  {
    id: 1,
    behaviorType: '打架斗殴',
    deviceName: '前门摄像头-001',
    location: '一号楼大厅',
    level: '高危',
    status: '待处理',
    detectTime: '2024-11-06 15:32:10',
  },
  {
    id: 2,
    behaviorType: '快速奔跑',
    deviceName: '走廊摄像头-003',
    location: '三楼走廊',
    level: '中危',
    status: '已处理',
    detectTime: '2024-11-06 15:28:35',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 2,
  showTotal: (total) => `共 ${total} 条`,
});

const getLevelColor = (level) => {
  const colorMap = {
    '高危': 'red',
    '中危': 'orange',
    '低危': 'blue',
  };
  return colorMap[level] || 'default';
};

const getStatusBadge = (status) => {
  const statusMap = {
    '待处理': 'warning',
    '处理中': 'processing',
    '已处理': 'success',
  };
  return statusMap[status] || 'default';
};

const handleSearch = () => {
  console.log('查询行为记录:', behaviorType.value);
};

const handleView = (record) => {
  console.log('查看详情:', record);
};

const handleProcess = (record) => {
  console.log('处理记录:', record);
};
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;
}
</style>
