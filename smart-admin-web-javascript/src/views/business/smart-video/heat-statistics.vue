<!--
  智能视频-过热统计页面

  @Author:    Claude Code
  @Date:      2025-11-06
  @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <div class="smart-video-page">
    <a-card title="热度统计分析" :bordered="false">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="今日总人流量"
              :value="statistics.todayFlow"
              suffix="人次"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <TeamOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="高峰时段"
              :value="statistics.peakTime"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <ClockCircleOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="平均停留时长"
              :value="statistics.avgStayTime"
              suffix="分钟"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <FieldTimeOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card :bordered="false" class="stat-card">
            <a-statistic
              title="热点区域数"
              :value="statistics.hotspotCount"
              suffix="个"
              :value-style="{ color: '#ff4d4f' }"
            >
              <template #prefix>
                <HeatMapOutlined />
              </template>
            </a-statistic>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="12">
          <a-card title="人流量趋势" :bordered="false">
            <template #extra>
              <a-radio-group v-model:value="flowPeriod" button-style="solid" size="small">
                <a-radio-button value="day">今日</a-radio-button>
                <a-radio-button value="week">本周</a-radio-button>
                <a-radio-button value="month">本月</a-radio-button>
              </a-radio-group>
            </template>
            <div ref="flowTrendChartRef" style="height: 300px;"></div>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="区域热度对比" :bordered="false">
            <div ref="areaHeatChartRef" style="height: 300px;"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="12">
          <a-card title="时段分布" :bordered="false">
            <div ref="timeDistributionChartRef" style="height: 300px;"></div>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="停留时长分布" :bordered="false">
            <div ref="stayDurationChartRef" style="height: 300px;"></div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 16px;">
        <a-col :span="24">
          <a-card title="区域详细统计" :bordered="false">
            <template #extra>
              <a-space>
                <a-select v-model:value="filterArea" placeholder="筛选区域" style="width: 150px;">
                  <a-select-option value="">全部区域</a-select-option>
                  <a-select-option value="area1">一号楼</a-select-option>
                  <a-select-option value="area2">停车场</a-select-option>
                </a-select>
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
                <template v-if="column.key === 'trend'">
                  <a-tag :color="record.trend > 0 ? 'red' : 'green'">
                    {{ record.trend > 0 ? '↑' : '↓' }} {{ Math.abs(record.trend) }}%
                  </a-tag>
                </template>

                <template v-if="column.key === 'heatLevel'">
                  <a-progress
                    :percent="record.heatLevel"
                    :stroke-color="getHeatLevelColor(record.heatLevel)"
                    size="small"
                  />
                </template>

                <template v-if="column.key === 'action'">
                  <a-button type="link" size="small" @click="handleViewDetail(record)">
                    查看详情
                  </a-button>
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
import {
  TeamOutlined,
  ClockCircleOutlined,
  FieldTimeOutlined,
  HeatMapOutlined,
  SearchOutlined,
  ExportOutlined,
} from '@ant-design/icons-vue';
import * as echarts from 'echarts';

const loading = ref(false);
const flowPeriod = ref('day');
const filterArea = ref('');

const statistics = reactive({
  todayFlow: 3245,
  peakTime: '14:00-15:00',
  avgStayTime: 4.5,
  hotspotCount: 8,
});

const columns = [
  { title: '区域名称', dataIndex: 'areaName', key: 'areaName' },
  { title: '人流量', dataIndex: 'flowCount', key: 'flowCount' },
  { title: '较昨日', key: 'trend' },
  { title: '热度等级', key: 'heatLevel' },
  { title: '平均停留', dataIndex: 'avgStayTime', key: 'avgStayTime' },
  { title: '峰值时段', dataIndex: 'peakTime', key: 'peakTime' },
  { title: '操作', key: 'action', width: 120 },
];

const dataSource = ref([
  {
    id: 1,
    areaName: '一号楼大厅',
    flowCount: 856,
    trend: 12,
    heatLevel: 85,
    avgStayTime: '3分15秒',
    peakTime: '14:00-15:00',
  },
  {
    id: 2,
    areaName: '停车场入口',
    flowCount: 1234,
    trend: -8,
    heatLevel: 92,
    avgStayTime: '1分42秒',
    peakTime: '08:00-09:00',
  },
  {
    id: 3,
    areaName: '会议室区域',
    flowCount: 456,
    trend: 5,
    heatLevel: 68,
    avgStayTime: '8分20秒',
    peakTime: '10:00-11:00',
  },
]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 3,
  showTotal: (total) => `共 ${total} 条`,
});

const flowTrendChartRef = ref(null);
const areaHeatChartRef = ref(null);
const timeDistributionChartRef = ref(null);
const stayDurationChartRef = ref(null);

let flowTrendChart = null;
let areaHeatChart = null;
let timeDistributionChart = null;
let stayDurationChart = null;

const getHeatLevelColor = (level) => {
  if (level >= 80) return '#ff4d4f';
  if (level >= 60) return '#faad14';
  return '#52c41a';
};

const initCharts = () => {
  if (flowTrendChartRef.value) {
    flowTrendChart = echarts.init(flowTrendChartRef.value);
    flowTrendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00'],
      },
      yAxis: { type: 'value' },
      series: [{
        data: [320, 450, 580, 720, 650, 480],
        type: 'line',
        smooth: true,
        areaStyle: {},
      }],
    });
  }

  if (areaHeatChartRef.value) {
    areaHeatChart = echarts.init(areaHeatChartRef.value);
    areaHeatChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: { type: 'category', data: ['一号楼', '停车场', '会议室', '餐厅', '走廊'] },
      yAxis: { type: 'value' },
      series: [{
        data: [856, 1234, 456, 678, 234],
        type: 'bar',
      }],
    });
  }

  if (timeDistributionChartRef.value) {
    timeDistributionChart = echarts.init(timeDistributionChartRef.value);
    timeDistributionChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 856, name: '8:00-12:00' },
          { value: 1234, name: '12:00-14:00' },
          { value: 720, name: '14:00-18:00' },
          { value: 435, name: '18:00-22:00' },
        ],
      }],
    });
  }

  if (stayDurationChartRef.value) {
    stayDurationChart = echarts.init(stayDurationChartRef.value);
    stayDurationChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: ['<1分钟', '1-3分钟', '3-5分钟', '5-10分钟', '>10分钟'] },
      yAxis: { type: 'value' },
      series: [{
        data: [1200, 1800, 950, 680, 320],
        type: 'bar',
      }],
    });
  }
};

const handleSearch = () => {
  console.log('查询统计数据:', filterArea.value);
};

const handleExport = () => {
  console.log('导出统计数据');
};

const handleViewDetail = (record) => {
  console.log('查看详情:', record);
};

onMounted(() => {
  initCharts();
  window.addEventListener('resize', () => {
    flowTrendChart?.resize();
    areaHeatChart?.resize();
    timeDistributionChart?.resize();
    stayDurationChart?.resize();
  });
});

onUnmounted(() => {
  flowTrendChart?.dispose();
  areaHeatChart?.dispose();
  timeDistributionChart?.dispose();
  stayDurationChart?.dispose();
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
