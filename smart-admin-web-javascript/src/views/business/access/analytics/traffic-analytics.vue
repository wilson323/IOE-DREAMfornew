<!--
 * 通行数据分析报表页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-13
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="traffic-analytics-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">通行数据分析</h1>
        <p class="page-description">全面分析园区通行数据，掌握人流动态趋势</p>
      </div>
      <div class="header-right">
        <a-space>
          <a-button
            type="primary"
            :loading="store.state.loading.traffic"
            @click="handleRefresh"
          >
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新数据
          </a-button>
          <a-button
            type="default"
            @click="showExportModal = true"
          >
            <template #icon>
              <ExportOutlined />
            </template>
            导出报表
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 查询条件 -->
    <a-card class="query-card">
      <a-form
        :model="queryForm"
        layout="inline"
        @finish="handleQuery"
      >
        <a-form-item label="时间范围" name="timeRange">
          <a-range-picker
            v-model:value="queryForm.timeRange"
            :shortcuts="dateShortcuts"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item label="区域" name="areaIds">
          <a-select
            v-model:value="queryForm.areaIds"
            mode="multiple"
            placeholder="请选择区域"
            style="width: 200px"
            :options="areaOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="人员类型" name="personTypes">
          <a-select
            v-model:value="queryForm.personTypes"
            mode="multiple"
            placeholder="请选择人员类型"
            style="width: 200px"
            :options="personTypeOptions"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="store.state.loading.traffic">
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- KPI指标卡片 -->
    <div class="kpi-cards">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12" :md="6" v-for="(kpi, index) in kpiMetrics" :key="index">
          <a-card class="kpi-card">
            <a-statistic
              :title="kpi.title"
              :value="kpi.value"
              :precision="kpi.precision || 0"
              :suffix="kpi.suffix"
              :value-style="{ color: kpi.color }"
            >
              <template #prefix>
                <component :is="kpi.icon" />
              </template>
            </a-statistic>
            <div class="kpi-trend" v-if="kpi.trend !== undefined">
              <span :class="getTrendClass(kpi.trend)">
                {{ getTrendIcon(kpi.trend) }} {{ Math.abs(kpi.trend) }}%
              </span>
              <span class="trend-text">较昨日</span>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 24小时通行热力图 -->
      <a-card title="24小时通行热力图" class="chart-card">
        <template #extra>
          <a-button type="link" size="small" @click="refreshHeatmapData">
            <ReloadOutlined />
          </a-button>
        </template>
        <HeatmapChart
          :data="heatMapData"
          :x-axis="heatMapXAxis"
          :y-axis="heatMapYAxis"
          height="300px"
          :loading="heatmapLoading"
        />
      </a-card>

      <!-- 通行流量趋势图 -->
      <a-card title="通行流量趋势" class="chart-card">
        <template #extra>
          <a-button type="link" size="small" @click="refreshTrafficTrendData">
            <ReloadOutlined />
          </a-button>
        </template>
        <LineChart
          :data="trafficTrendData"
          :x-axis="trafficTrendXAxis"
          height="300px"
          :loading="trendLoading"
          :show-area="true"
          title=""
        />
      </a-card>

      <!-- 区域通行统计 -->
      <a-row :gutter="16">
        <a-col :xs="24" :lg="12">
          <a-card title="区域通行统计" class="chart-card">
            <template #extra>
              <a-button type="link" size="small" @click="refreshAreaStatsData">
                <ReloadOutlined />
              </a-button>
            </template>
            <BarChart
              :data="areaStatsData"
              :x-axis="areaStatsXAxis"
              height="300px"
              :loading="areaStatsLoading"
              title=""
              color-scheme="business"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a-card title="人员类型分布" class="chart-card">
            <template #extra>
              <a-button type="link" size="small" @click="refreshPersonTypeStatsData">
                <ReloadOutlined />
              </a-button>
            </template>
            <PieChart
              :data="personTypeStatsData"
              height="300px"
              :loading="personTypeStatsLoading"
              title=""
              :show-legend="true"
            />
          </a-card>
        </a-col>
      </a-row>

      <!-- 异常通行趋势 -->
      <a-row :gutter="16">
        <a-col :xs="24" :lg="16">
          <a-card title="异常通行趋势" class="chart-card">
            <template #extra>
              <a-button type="link" size="small" @click="refreshAbnormalTrendData">
                <ReloadOutlined />
              </a-button>
            </template>
            <LineChart
              :data="abnormalTrendData"
              :x-axis="abnormalTrendXAxis"
              height="300px"
              :loading="abnormalTrendLoading"
              title=""
              color-scheme="security"
            />
          </a-card>
        </a-col>
        <a-col :xs="24" :lg="8">
          <a-card title="通行高峰时段" class="chart-card">
            <template #extra>
              <a-button type="link" size="small" @click="refreshPeakHourData">
                <ReloadOutlined />
              </a-button>
            </template>
            <BarChart
              :data="peakHourData"
              :x-axis="peakHourXAxis"
              height="300px"
              :loading="peakHourLoading"
              title=""
              :bar-width="20"
              color-scheme="traffic"
            />
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 数据表格 -->
    <a-card title="详细数据" class="data-table-card">
      <template #extra>
        <a-space>
          <a-button type="link" size="small" @click="handleExportTableData">
            <ExportOutlined />
            导出Excel
          </a-button>
        </a-space>
      </template>
      <a-table
        :columns="tableColumns"
        :data-source="tableData"
        :loading="tableLoading"
        :pagination="tablePagination"
        :scroll="{ x: 1200 }"
        @change="handleTableChange"
      />
    </a-card>

    <!-- 导出模态框 -->
    <ExportModal
      v-model:visible="showExportModal"
      :export-type="'TRAFFIC'"
      :query-params="queryForm"
      @confirm="handleExport"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  ReloadOutlined,
  ExportOutlined,
  UserOutlined,
  TeamOutlined,
  SafetyOutlined,
  ClockCircleOutlined
} from '@ant-design/icons-vue';
import { useAccessAnalyticsStore } from '/@/store/modules/business/access-analytics';
import {
  LineChart,
  BarChart,
  PieChart,
  HeatmapChart
} from '/@/components/charts';
import ExportModal from './components/ExportModal.vue';

const store = useAccessAnalyticsStore();

// 响应式数据
const showExportModal = ref(false);
const queryForm = reactive({
  timeRange: [
    dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
    dayjs().format('YYYY-MM-DD')
  ],
  areaIds: [],
  personTypes: []
});

// 加载状态
const heatmapLoading = ref(false);
const trendLoading = ref(false);
const areaStatsLoading = ref(false);
const personTypeStatsLoading = ref(false);
const abnormalTrendLoading = ref(false);
const peakHourLoading = ref(false);
const tableLoading = ref(false);

// 日期快捷选项
const dateShortcuts = [
  {
    label: '今天',
    value: () => [dayjs().format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')]
  },
  {
    label: '昨天',
    value: () => [
      dayjs().subtract(1, 'day').format('YYYY-MM-DD'),
      dayjs().subtract(1, 'day').format('YYYY-MM-DD')
    ]
  },
  {
    label: '最近7天',
    value: () => [
      dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
      dayjs().format('YYYY-MM-DD')
    ]
  },
  {
    label: '最近30天',
    value: () => [
      dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
      dayjs().format('YYYY-MM-DD')
    ]
  }
];

// 区域选项
const areaOptions = ref([
  { label: '主楼A区', value: 'AREA_A' },
  { label: '主楼B区', value: 'AREA_B' },
  { label: '科研楼', value: 'AREA_RESEARCH' },
  { label: '生产车间', value: 'AREA_WORKSHOP' },
  { label: '食堂', value: 'AREA_CAFETERIA' },
  { label: '宿舍区', value: 'AREA_DORMITORY' }
]);

// 人员类型选项
const personTypeOptions = ref([
  { label: '员工', value: 'EMPLOYEE' },
  { label: '访客', value: 'VISITOR' },
  { label: '承包商', value: 'CONTRACTOR' },
  { label: '临时人员', value: 'TEMPORARY' }
]);

// KPI指标数据
const kpiMetrics = computed(() => [
  {
    title: '今日通行总量',
    value: 5680,
    icon: UserOutlined,
    color: '#1890ff',
    suffix: '次',
    trend: 12.5
  },
  {
    title: '活跃用户数',
    value: 1234,
    icon: TeamOutlined,
    color: '#52c41a',
    suffix: '人',
    trend: 8.2
  },
  {
    title: '异常通行次数',
    value: 23,
    icon: SafetyOutlined,
    color: '#f5222d',
    suffix: '次',
    trend: -15.3
  },
  {
    title: '平均通行时间',
    value: 2.8,
    icon: ClockCircleOutlined,
    color: '#faad14',
    suffix: '秒',
    precision: 1,
    trend: -5.1
  }
]);

// 热力图数据
const heatMapData = computed(() => store.state.trafficData.heatmapData);
const heatMapXAxis = computed(() => {
  const hours = [];
  for (let i = 0; i < 24; i++) {
    hours.push(`${i}:00`);
  }
  return hours;
});
const heatMapYAxis = computed(() => ['周一', '周二', '周三', '周四', '周五', '周六', '周日']);

// 通行流量趋势数据
const trafficTrendData = computed(() => [
  {
    name: '进入人数',
    data: [320, 302, 301, 334, 390, 330, 320, 345, 360, 380, 375, 365]
  },
  {
    name: '离开人数',
    data: [280, 288, 290, 320, 350, 310, 300, 325, 340, 360, 355, 345]
  },
  {
    name: '总计',
    data: [600, 590, 591, 654, 740, 640, 620, 670, 700, 740, 730, 710]
  }
]);
const trafficTrendXAxis = computed(() => {
  const days = [];
  for (let i = 6; i >= 0; i--) {
    days.push(dayjs().subtract(i, 'day').format('MM-DD'));
  }
  return days;
});

// 区域通行统计数据
const areaStatsData = computed(() => [
  {
    name: '通行次数',
    data: [1250, 980, 860, 720, 650, 580]
  }
]);
const areaStatsXAxis = computed(() => ['主楼A区', '主楼B区', '科研楼', '生产车间', '食堂', '宿舍区']);

// 人员类型统计数据
const personTypeStatsData = computed(() => [
  { name: '员工', value: 3420, color: '#1890ff' },
  { name: '访客', value: 1260, color: '#52c41a' },
  { name: '承包商', value: 680, color: '#faad14' },
  { name: '临时人员', value: 320, color: '#f5222d' }
]);

// 异常通行趋势数据
const abnormalTrendData = computed(() => [
  {
    name: '异常次数',
    data: [12, 15, 8, 22, 18, 25, 20]
  },
  {
    name: '异常率(%)',
    data: [2.1, 2.5, 1.4, 3.4, 2.9, 3.8, 3.1]
  }
]);
const abnormalTrendXAxis = computed(() => trafficTrendXAxis.value);

// 高峰时段数据
const peakHourData = computed(() => [
  {
    name: '通行次数',
    data: [120, 180, 450, 680, 890, 750, 560, 480, 520, 680, 750, 620]
  }
]);
const peakHourXAxis = computed(() => {
  const hours = [];
  for (let i = 7; i <= 18; i++) {
    hours.push(`${i}:00`);
  }
  return hours;
});

// 表格配置
const tableColumns = [
  {
    title: '时间',
    dataIndex: 'time',
    width: 150,
    fixed: 'left'
  },
  {
    title: '区域',
    dataIndex: 'areaName',
    width: 120
  },
  {
    title: '人员类型',
    dataIndex: 'personType',
    width: 100
  },
  {
    title: '进入次数',
    dataIndex: 'inCount',
    width: 100,
    sorter: true
  },
  {
    title: '离开次数',
    dataIndex: 'outCount',
    width: 100,
    sorter: true
  },
  {
    title: '总计',
    dataIndex: 'totalCount',
    width: 100,
    sorter: true
  },
  {
    title: '异常次数',
    dataIndex: 'abnormalCount',
    width: 100,
    sorter: true
  },
  {
    title: '异常率',
    dataIndex: 'abnormalRate',
    width: 100,
    render: (text) => `${(text * 100).toFixed(2)}%`
  }
];

// 表格数据
const tableData = ref([]);
const tablePagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
});

// 方法
const handleQuery = async () => {
  await Promise.all([
    refreshAllChartData(),
    loadTableData()
  ]);
};

const handleReset = () => {
  queryForm.timeRange = [
    dayjs().subtract(7, 'day').format('YYYY-MM-DD'),
    dayjs().format('YYYY-MM-DD')
  ];
  queryForm.areaIds = [];
  queryForm.personTypes = [];
  handleQuery();
};

const handleRefresh = () => {
  handleQuery();
  message.success('数据已刷新');
};

const refreshAllChartData = async () => {
  await Promise.all([
    refreshHeatmapData(),
    refreshTrafficTrendData(),
    refreshAreaStatsData(),
    refreshPersonTypeStatsData(),
    refreshAbnormalTrendData(),
    refreshPeakHourData()
  ]);
};

const refreshHeatmapData = async () => {
  heatmapLoading.value = true;
  try {
    await store.get24HourHeatmapData({
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      areaIds: queryForm.areaIds,
      personTypes: queryForm.personTypes
    });
  } catch (error) {
    console.error('刷新热力图数据失败:', error);
  } finally {
    heatmapLoading.value = false;
  }
};

const refreshTrafficTrendData = async () => {
  trendLoading.value = true;
  try {
    await store.getTrafficTrendData({
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      areaIds: queryForm.areaIds,
      personTypes: queryForm.personTypes
    });
  } catch (error) {
    console.error('刷新通行流量趋势数据失败:', error);
  } finally {
    trendLoading.value = false;
  }
};

const refreshAreaStatsData = async () => {
  areaStatsLoading.value = true;
  try {
    await store.getAreaTrafficStats({
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      personTypes: queryForm.personTypes
    });
  } catch (error) {
    console.error('刷新区域通行统计数据失败:', error);
  } finally {
    areaStatsLoading.value = false;
  }
};

const refreshPersonTypeStatsData = async () => {
  personTypeStatsLoading.value = true;
  try {
    await store.getPersonTypeStats({
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      areaIds: queryForm.areaIds
    });
  } catch (error) {
    console.error('刷新人员类型统计数据失败:', error);
  } finally {
    personTypeStatsLoading.value = false;
  }
};

const refreshAbnormalTrendData = async () => {
  abnormalTrendLoading.value = true;
  try {
    await store.getAbnormalTrendData({
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      areaIds: queryForm.areaIds,
      personTypes: queryForm.personTypes
    });
  } catch (error) {
    console.error('刷新异常通行趋势数据失败:', error);
  } finally {
    abnormalTrendLoading.value = false;
  }
};

const refreshPeakHourData = async () => {
  peakHourLoading.value = true;
  try {
    await store.getPeakHourAnalysis({
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      areaIds: queryForm.areaIds,
      personTypes: queryForm.personTypes
    });
  } catch (error) {
    console.error('刷新通行高峰时段数据失败:', error);
  } finally {
    peakHourLoading.value = false;
  }
};

const loadTableData = async () => {
  tableLoading.value = true;
  try {
    // 模拟表格数据
    const mockData = [];
    for (let i = 0; i < 50; i++) {
      mockData.push({
        key: i,
        time: dayjs().subtract(i, 'hour').format('YYYY-MM-DD HH:mm'),
        areaName: areaOptions.value[Math.floor(Math.random() * areaOptions.value.length)].label,
        personType: personTypeOptions.value[Math.floor(Math.random() * personTypeOptions.value.length)].label,
        inCount: Math.floor(Math.random() * 100) + 10,
        outCount: Math.floor(Math.random() * 100) + 10,
        totalCount: Math.floor(Math.random() * 200) + 20,
        abnormalCount: Math.floor(Math.random() * 10),
        abnormalRate: (Math.random() * 0.05).toFixed(3)
      });
    }
    tableData.value = mockData;
    tablePagination.total = mockData.length;
  } catch (error) {
    console.error('加载表格数据失败:', error);
  } finally {
    tableLoading.value = false;
  }
};

const handleTableChange = (pagination, filters, sorter) => {
  tablePagination.current = pagination.current;
  tablePagination.pageSize = pagination.pageSize;
  loadTableData();
};

const handleExport = async (exportParams) => {
  try {
    await store.exportTrafficAnalytics({
      ...exportParams,
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      areaIds: queryForm.areaIds,
      personTypes: queryForm.personTypes
    });
    showExportModal.value = false;
    message.success('导出任务已创建，请在导出管理中查看进度');
  } catch (error) {
    console.error('导出失败:', error);
  }
};

const handleExportTableData = async () => {
  try {
    await store.exportTrafficAnalytics({
      format: 'EXCEL',
      includeCharts: false,
      startDate: queryForm.timeRange[0],
      endDate: queryForm.timeRange[1],
      areaIds: queryForm.areaIds,
      personTypes: queryForm.personTypes
    });
    message.success('表格数据导出任务已创建');
  } catch (error) {
    console.error('导出表格数据失败:', error);
  }
};

const getTrendIcon = (trend) => {
  if (trend > 0) return '↑';
  if (trend < 0) return '↓';
  return '→';
};

const getTrendClass = (trend) => {
  if (trend > 0) return 'trend-up';
  if (trend < 0) return 'trend-down';
  return 'trend-stable';
};

// 监听查询条件变化
watch(
  () => [queryForm.timeRange, queryForm.areaIds, queryForm.personTypes],
  () => {
    // 防抖处理，避免频繁查询
    const timer = setTimeout(() => {
      handleQuery();
    }, 500);
    return () => clearTimeout(timer);
  },
  { deep: true }
);

// 生命周期
onMounted(() => {
  handleQuery();
});
</script>

<style scoped>
.traffic-analytics-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-left .page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.header-left .page-description {
  margin: 8px 0 0 0;
  color: #8c8c8c;
  font-size: 14px;
}

.query-card {
  margin-bottom: 24px;
}

.kpi-cards {
  margin-bottom: 24px;
}

.kpi-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.kpi-card :deep(.ant-statistic-content) {
  font-size: 24px;
}

.kpi-trend {
  margin-top: 8px;
  font-size: 12px;
  color: #8c8c8c;
}

.trend-up {
  color: #52c41a;
}

.trend-down {
  color: #f5222d;
}

.trend-stable {
  color: #8c8c8c;
}

.trend-text {
  margin-left: 4px;
}

.charts-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.data-table-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .traffic-analytics-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-right {
    width: 100%;
  }

  .header-right :deep(.ant-space) {
    width: 100%;
  }

  .kpi-cards .ant-col {
    margin-bottom: 16px;
  }

  .charts-container .ant-row {
    margin-bottom: 16px;
  }
}
</style>