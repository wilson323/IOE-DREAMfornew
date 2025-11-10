<!--
  智能视频-人群态势页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="人群态势分析" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="当前在场人数"
              :value="crowdStats.currentCount"
              suffix="人"
              :value-style="{ color: '#1890ff' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="今日峰值人数"
              :value="crowdStats.peakCount"
              suffix="人"
              :value-style="{ color: '#52c41a' }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="人群密度"
              :value="crowdStats.density"
              suffix="人/m²"
              :value-style="{ color: getDensityColor() }"
            />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="拥挤告警"
              :value="crowdStats.alarmCount"
              suffix="次"
              :value-style="{ color: '#ff4d4f' }"
            />
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="12">
          <a-card title="人流量趋势" :bordered="false">
            <div ref="flowTrendChartRef" style="height: 300px;"></div>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="区域人数分布" :bordered="false">
            <div ref="areaDistributionChartRef" style="height: 300px;"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="24">
          <a-card title="实时监控区域" :bordered="false">
            <a-table
              :columns="columns"
              :data-source="dataSource"
              :pagination="pagination"
              :loading="loading"
              size="small"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'densityLevel'">
                  <a-tag :color="getDensityLevelColor(record.densityLevel)">
                    {{ record.densityLevel }}
                  </a-tag>
                </template>

                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" size="small" @click="handleViewVideo(record)">查看视频</a-button>
                    <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
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
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import * as echarts from 'echarts';

const loading = ref(false);

const crowdStats = reactive({
  currentCount: 342,
  peakCount: 568,
  density: 0.52,
  alarmCount: 3,
});

const columns = [
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName' },
  { title: '当前人数', dataIndex: 'currentCount', key: 'currentCount' },
  { title: '容量', dataIndex: 'capacity', key: 'capacity' },
  { title: '密度等级', key: 'densityLevel' },
  { title: '设备数量', dataIndex: 'deviceCount', key: 'deviceCount' },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime' },
  { title: '操作', key: 'action', width: 180 },
];

const dataSource = ref([
  {
    id: 1,
    areaName: '一号楼大厅',
    currentCount: 85,
    capacity: 200,
    densityLevel: '正常',
    deviceCount: 4,
    updateTime: '2024-11-06 15:32:10',
  },
  {
    id: 2,
    areaName: '停车场入口',
    currentCount: 156,
    capacity: 150,
    densityLevel: '拥挤',
    deviceCount: 3,
    updateTime: '2024-11-06 15:32:08',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 2,
  showTotal: (total) => `共 ${total} 条`,
});

const flowTrendChartRef = ref(null);
const areaDistributionChartRef = ref(null);
let flowTrendChart = null;
let areaDistributionChart = null;

const getDensityColor = () => {
  if (crowdStats.density >= 0.8) return '#ff4d4f';
  if (crowdStats.density >= 0.5) return '#faad14';
  return '#52c41a';
};

const getDensityLevelColor = (level) => {
  const colorMap = {
    '拥挤': 'red',
    '较密集': 'orange',
    '正常': 'green',
  };
  return colorMap[level] || 'default';
};

const initFlowTrendChart = () => {
  if (!flowTrendChartRef.value) return;
  flowTrendChart = echarts.init(flowTrendChartRef.value);

  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00'],
    },
    yAxis: { type: 'value' },
    series: [{
      data: [120, 280, 450, 380, 420, 350],
      type: 'line',
      smooth: true,
      areaStyle: {},
    }],
  };

  flowTrendChart.setOption(option);
};

const initAreaDistributionChart = () => {
  if (!areaDistributionChartRef.value) return;
  areaDistributionChart = echarts.init(areaDistributionChartRef.value);

  const option = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: [
        { value: 85, name: '一号楼大厅' },
        { value: 156, name: '停车场入口' },
        { value: 67, name: '会议室区域' },
        { value: 34, name: '餐厅' },
      ],
    }],
  };

  areaDistributionChart.setOption(option);
};

const handleViewVideo = (record) => {
  console.log('查看视频:', record);
};

const handleViewDetail = (record) => {
  console.log('查看详情:', record);
};

onMounted(() => {
  initFlowTrendChart();
  initAreaDistributionChart();
});

onUnmounted(() => {
  flowTrendChart?.dispose();
  areaDistributionChart?.dispose();
});
</script>

<style scoped lang="less">
.smart-video-page {
  padding: 24px;
  background-color: #f0f2f5;

  .stat-card {
    :deep(.ant-card-body) {
      padding: 20px;
    }
  }
}
</style>
