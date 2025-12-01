<!--
 * 考勤统计报表页面
 *
 * @Author:    SmartAdmin Team
 * @Date:      2025-11-17
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->

<template>
  <div class="attendance-statistics-container">
    <!-- 页面标题和操作按钮 -->
    <div class="page-header">
      <div class="page-title">
        <h2>考勤统计报表</h2>
        <p class="page-description">多维度考勤数据分析与统计报表</p>
      </div>
      <div class="page-actions">
        <a-space>
          <a-button @click="showExportModal">
            <template #icon><ExportOutlined /></template>
            导出报表
          </a-button>
          <a-button @click="showAlertConfigModal">
            <template #icon><BellOutlined /></template>
            预警配置
          </a-button>
          <a-button @click="refreshRealtimeData" :loading="realtimeLoading">
            <template #icon><SyncOutlined /></template>
            实时刷新
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 查询表单 -->
    <a-card class="mb-4" :bordered="false">
      <a-form class="smart-query-form" v-privilege="'attendance:statistics:query'">
        <a-row class="smart-query-form-row">
          <a-form-item label="统计维度" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.statisticsType"
              style="width: 150px"
              @change="onStatisticsTypeChange"
              placeholder="选择统计维度"
            >
              <a-select-option value="personal">个人统计</a-select-option>
              <a-select-option value="department">部门统计</a-select-option>
              <a-select-option value="company">公司统计</a-select-option>
              <a-select-option value="daily">按日统计</a-select-option>
              <a-select-option value="weekly">按周统计</a-select-option>
              <a-select-option value="monthly">按月统计</a-select-option>
              <a-select-option value="yearly">按年统计</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="员工" class="smart-query-form-item" v-if="queryForm.statisticsType === 'personal'">
            <a-select
              v-model:value="queryForm.employeeId"
              style="width: 200px"
              :showSearch="true"
              :allowClear="true"
              placeholder="选择员工"
              :filterOption="filterEmployeeOption"
            >
              <a-select-option v-for="item in employeeList" :key="item.employeeId" :value="item.employeeId">
                {{ item.employeeName }} - {{ item.departmentName }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="部门" class="smart-query-form-item" v-if="queryForm.statisticsType === 'department'">
            <a-tree-select
              v-model:value="queryForm.departmentIds"
              style="width: 250px"
              :tree-data="departmentTreeData"
              placeholder="选择部门（可多选）"
              allow-clear
              multiple
              tree-checkable
            />
          </a-form-item>

          <a-form-item label="时间范围" class="smart-query-form-item">
            <a-range-picker
              v-model:value="dateRange"
              :presets="dateRangePresets"
              @change="onDateRangeChange"
              style="width: 300px"
            />
          </a-form-item>

          <a-form-item label="对比分析" class="smart-query-form-item" v-if="queryForm.statisticsType === 'personal' || queryForm.statisticsType === 'department'">
            <a-switch
              v-model:checked="queryForm.enableComparison"
              checked-children="启用"
              un-checked-children="禁用"
              @change="onComparisonToggle"
            />
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-button-group>
              <a-button type="primary" @click="onSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                查询
              </a-button>
              <a-button @click="onReload">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu @click="handleExport">
                    <a-menu-item key="excel">
                      <FileExcelOutlined />
                      导出Excel
                    </a-menu-item>
                    <a-menu-item key="pdf">
                      <FilePdfOutlined />
                      导出PDF
                    </a-menu-item>
                    <a-menu-item key="csv">
                      <FileTextOutlined />
                      导出CSV
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button v-privilege="'attendance:statistics:export'">
                  <template #icon>
                    <ExportOutlined />
                  </template>
                  导出 <DownOutlined />
                </a-button>
              </a-dropdown>
              <a-button @click="showAdvancedQuery">
                <template #icon><FilterOutlined /></template>
                高级筛选
              </a-button>
            </a-button-group>
          </a-form-item>
        </a-row>

        <!-- 快速时间选择 -->
        <a-row class="smart-query-form-row" v-if="!queryForm.enableComparison">
          <a-form-item label="快速选择" class="smart-query-form-item">
            <a-space>
              <a-button size="small" @click="selectTimeRange('today')">今日</a-button>
              <a-button size="small" @click="selectTimeRange('yesterday')">昨日</a-button>
              <a-button size="small" @click="selectTimeRange('thisWeek')">本周</a-button>
              <a-button size="small" @click="selectTimeRange('thisMonth')">本月</a-button>
              <a-button size="small" @click="selectTimeRange('thisQuarter')">本季度</a-button>
              <a-button size="small" @click="selectTimeRange('thisYear')">今年</a-button>
            </a-space>
          </a-form-item>
        </a-row>

        <!-- 对比分析时间选择 -->
        <a-row class="smart-query-form-row" v-if="queryForm.enableComparison">
          <a-form-item label="对比期间" class="smart-query-form-item">
            <a-range-picker
              v-model:value="comparisonDateRange"
              :presets="dateRangePresets"
              @change="onComparisonDateChange"
              style="width: 300px"
            />
          </a-form-item>
          <a-form-item label="对比类型" class="smart-query-form-item">
            <a-select
              v-model:value="comparisonType"
              style="width: 120px"
              @change="onComparisonTypeChange"
            >
              <a-select-option value="period">期间对比</a-select-option>
              <a-select-option value="yoy">同比</a-select-option>
              <a-select-option value="mom">环比</a-select-option>
            </a-select>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 统计概览卡片 -->
    <a-row :gutter="16" class="mb-4">
      <a-col :span="6" v-for="(card, index) in overviewCards" :key="index">
        <a-card :bordered="false" class="overview-card">
          <a-statistic
            :title="card.title"
            :value="card.value"
            :precision="card.precision || 0"
            :suffix="card.suffix"
            :value-style="{ color: card.color }"
          >
            <template #prefix>
              <component :is="card.icon" />
            </template>
          </a-statistic>
          <div class="card-trend" v-if="card.trend !== undefined">
            <span :class="['trend-value', card.trend >= 0 ? 'up' : 'down']">
              <ArrowUpOutlined v-if="card.trend > 0" />
              <ArrowDownOutlined v-else-if="card.trend < 0" />
              <MinusOutlined v-else />
              {{ card.trend >= 0 ? '+' : '' }}{{ card.trend }}%
            </span>
            <span class="trend-text">较上期</span>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域（使用现有组件） -->
    <a-tabs v-model:activeKey="activeChartTab" @change="onChartTabChange" class="mb-4">
      <a-tab-pane key="trend" tab="趋势分析">
        <a-card :bordered="false">
          <div class="chart-controls mb-4">
            <a-space>
              <span>图表类型：</span>
              <a-radio-group v-model:value="trendChartType" @change="onTrendChartTypeChange">
                <a-radio-button value="line">折线图</a-radio-button>
                <a-radio-button value="bar">柱状图</a-radio-button>
                <a-radio-button value="area">面积图</a-radio-button>
              </a-radio-group>
            </a-space>
          </div>
          <LineChart
            v-if="trendChartType === 'line' || trendChartType === 'area'"
            :data="trendChartData"
            :x-axis="trendXAxis"
            :title="趋势分析图"
            :show-area="trendChartType === 'area'"
            :loading="chartLoading"
            height="400px"
          />
          <BarChart
            v-else
            :data="trendChartData"
            :x-axis="trendXAxis"
            :title="趋势分析图"
            :loading="chartLoading"
            height="400px"
          />
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="distribution" tab="分布分析">
        <a-card :bordered="false">
          <a-row :gutter="16">
            <a-col :span="12">
              <BarChart
                :data="statusDistributionData"
                title="考勤状态分布"
                :loading="chartLoading"
                height="300px"
                color-scheme="business"
              />
            </a-col>
            <a-col :span="12">
              <BarChart
                :data="departmentDistributionData"
                title="部门考勤分布"
                :loading="chartLoading"
                height="300px"
                color-scheme="traffic"
              />
            </a-col>
          </a-row>
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="comparison" tab="对比分析" v-if="queryForm.enableComparison">
        <a-card :bordered="false">
          <LineChart
            :data="comparisonChartData"
            :x-axis="comparisonXAxis"
            title="同期对比分析"
            :loading="chartLoading"
            height="400px"
          />
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="heatmap" tab="热力分析">
        <a-card :bordered="false">
          <div class="heatmap-controls mb-4">
            <a-space>
              <span>分析维度：</span>
              <a-select
                v-model:value="heatmapDimension"
                style="width: 150px"
                @change="onHeatmapDimensionChange"
              >
                <a-select-option value="weekday">星期分析</a-select-option>
                <a-select-option value="hour">小时分析</a-select-option>
                <a-select-option value="department">部门分析</a-select-option>
              </a-select>
            </a-space>
          </div>
          <HeatmapChart
            :data="heatmapData"
            :title="`${heatmapDimension === 'weekday' ? '星期' : heatmapDimension === 'hour' ? '小时' : '部门'}热力分析`"
            :loading="chartLoading"
            height="400px"
          />
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="prediction" tab="预测分析">
        <a-card :bordered="false">
          <LineChart
            :data="predictionChartData"
            :x-axis="predictionXAxis"
            title="考勤趋势预测"
            :loading="chartLoading"
            height="400px"
            smooth
            color-scheme="business"
          />
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <!-- 详细数据表格 -->
    <a-card :bordered="false">
      <div class="table-header">
        <h3>详细统计数据</h3>
      </div>

      <a-table
        rowKey="id"
        :columns="visibleColumns"
        :dataSource="tableData"
        :scroll="{ x: 1200 }"
        :pagination="tablePagination"
        :loading="tableLoading"
        size="small"
        bordered
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'employeeInfo'">
            <div>
              <div class="employee-name">{{ record.employeeName }}</div>
              <div class="employee-dept">{{ record.departmentName }}</div>
            </div>
          </template>
          <template v-if="column.dataIndex === 'attendanceRate'">
            <a-progress
              :percent="record.attendanceRate"
              size="small"
              :status="getAttendanceRateStatus(record.attendanceRate)"
            />
          </template>
          <template v-if="column.dataIndex === 'lateCount'">
            <a-tag :color="getLateCountColor(record.lateCount)">
              {{ record.lateCount }}次
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'earlyLeaveCount'">
            <a-tag :color="getEarlyLeaveCountColor(record.earlyLeaveCount)">
              {{ record.earlyLeaveCount }}次
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'absenceCount'">
            <a-tag :color="record.absenceCount > 0 ? 'red' : 'green'">
              {{ record.absenceCount }}天
            </a-tag>
          </template>
          <template v-if="column.dataIndex === 'workHours'">
            <span :class="getWorkHoursClass(record.workHours, record.standardWorkHours)">
              {{ record.workHours }}h / {{ record.standardWorkHours }}h
            </span>
          </template>
          <template v-if="column.dataIndex === 'trend'">
            <span v-if="record.trend > 0" style="color: #f5222d;">
              <ArrowUpOutlined /> {{ record.trend }}%
            </span>
            <span v-else-if="record.trend < 0" style="color: #52c41a;">
              <ArrowDownOutlined /> {{ Math.abs(record.trend) }}%
            </span>
            <span v-else style="color: #8c8c8c;">
              <MinusOutlined /> 0%
            </span>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
              <a-button type="link" size="small" @click="viewDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="viewEmployeeProfile(record.employeeId)">画像</a-button>
            </div>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情模态框 -->
    <StatisticsDetailModal
      v-model:visible="showDetailModal"
      :detail-data="selectedRecord"
      :query-form="queryForm"
    />

    <!-- 员工画像模态框 -->
    <EmployeeProfileModal
      v-model:visible="showEmployeeProfileModal"
      :employee-id="selectedEmployeeId"
    />

    <!-- 数据导出模态框（复用现有组件） -->
    <ExportModal
      v-model:visible="showExportModal"
      export-type="ATTENDANCE"
      :query-params="queryForm"
      @confirm="handleExportConfirm"
    />

    <!-- 预警配置模态框 -->
    <AlertConfigModal
      v-model:visible="showAlertConfigModal"
      @success="onAlertConfigSuccess"
    />

    </div>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick, computed } from 'vue';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import {
  SearchOutlined,
  ReloadOutlined,
  ExportOutlined,
  UserOutlined,
  CalendarOutlined,
  TeamOutlined,
  ClockCircleOutlined,
  BellOutlined,
  SyncOutlined,
  FileExcelOutlined,
  FilePdfOutlined,
  DownOutlined,
  FilterOutlined,
  SettingOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  MinusOutlined
} from '@ant-design/icons-vue';
import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { attendanceStatisticsApi } from '/@/api/business/attendance/attendance-statistics-api';
import { smartSentry } from '/@/lib/smart-sentry';

// 基于现有组件的导入
import BaseChart from '/@/components/charts/BaseChart.vue';
import LineChart from '/@/components/charts/LineChart.vue';
import BarChart from '/@/components/charts/BarChart.vue';
import HeatmapChart from '/@/components/charts/HeatmapChart.vue';
import ExportModal from '/@/views/business/access/analytics/components/ExportModal.vue';

// 考勤专用组件（保留必要组件）
import StatisticsDetailModal from './components/StatisticsDetailModal.vue';
import EmployeeProfileModal from './components/EmployeeProfileModal.vue';
import AlertConfigModal from './components/AlertConfigModal.vue';

// 响应式数据
const queryFormState = {
  statisticsType: 'personal',
  employeeId: undefined,
  departmentIds: [],
  startDate: null,
  endDate: null,
  pageNum: 1,
  pageSize: PAGE_SIZE,
  enableComparison: false,
  groupBy: 'employee'
};
const queryForm = reactive({ ...queryFormState });

// 图表和界面状态
const dateRange = ref([]);
const comparisonDateRange = ref([]);
const comparisonType = ref('period');
const activeChartTab = ref('trend');
const trendChartType = ref('line');
const heatmapDimension = ref('weekday');
const realtimeLoading = ref(false);

// 数据状态
const tableData = ref([]);
const tableLoading = ref(false);
const total = ref(0);
const employeeList = ref([]);
const departmentList = ref([]);
const departmentTreeData = ref([]);

// 模态框状态
const showDetailModal = ref(false);
const showEmployeeProfileModal = ref(false);
const showReportGenerateModal = ref(false);
const showAlertConfigModal = ref(false);
const showAdvancedQueryModal = ref(false);
const showColumnConfigModal = ref(false);

// 选中数据
const selectedRecord = ref(null);
const selectedEmployeeId = ref(null);

// 概览卡片数据
const overviewCards = ref([
  {
    title: '总出勤率',
    value: 0,
    suffix: '%',
    precision: 1,
    color: '#52c41a',
    icon: 'CalendarOutlined',
    trend: 0
  },
  {
    title: '平均工作时长',
    value: 0,
    suffix: 'h',
    precision: 1,
    color: '#1890ff',
    icon: 'ClockCircleOutlined',
    trend: 0
  },
  {
    title: '迟到人数',
    value: 0,
    suffix: '人',
    color: '#fa8c16',
    icon: 'UserOutlined',
    trend: 0
  },
  {
    title: '异常次数',
    value: 0,
    suffix: '次',
    color: '#f5222d',
    icon: 'TeamOutlined',
    trend: 0
  }
]);

// 图表引用
const trendChartRef = ref();
const statusChartRef = ref();
const departmentChartRef = ref();
const personalChartRef = ref();
const abnormalChartRef = ref();
const comparisonChartRef = ref();
const heatmapChartRef = ref();
const predictionChartRef = ref();

// 列配置
const allColumns = ref([
  {
    title: '员工信息',
    dataIndex: 'employeeInfo',
    width: 150,
    fixed: 'left',
    visible: true
  },
  {
    title: '应出勤天数',
    dataIndex: 'shouldWorkDays',
    width: 100,
    align: 'center',
    visible: true
  },
  {
    title: '实际出勤',
    dataIndex: 'actualWorkDays',
    width: 100,
    align: 'center',
    visible: true
  },
  {
    title: '出勤率',
    dataIndex: 'attendanceRate',
    width: 120,
    align: 'center',
    visible: true
  },
  {
    title: '迟到',
    dataIndex: 'lateCount',
    width: 80,
    align: 'center',
    visible: true
  },
  {
    title: '早退',
    dataIndex: 'earlyLeaveCount',
    width: 80,
    align: 'center',
    visible: true
  },
  {
    title: '缺勤',
    dataIndex: 'absenceCount',
    width: 80,
    align: 'center',
    visible: true
  },
  {
    title: '工作时长',
    dataIndex: 'workHours',
    width: 120,
    align: 'center',
    visible: true
  },
  {
    title: '加班时长',
    dataIndex: 'overtimeHours',
    width: 100,
    align: 'center',
    visible: false
  },
  {
    title: '趋势',
    dataIndex: 'trend',
    width: 80,
    align: 'center',
    visible: false
  },
  {
    title: '操作',
    dataIndex: 'action',
    fixed: 'right',
    width: 120,
    visible: true
  }
]);

// 可见列计算属性
const visibleColumns = computed(() => {
  return allColumns.value.filter(column => column.visible);
});

// 日期范围预设
const dateRangePresets = [
  { label: '本周', value: () => [dayjs().startOf('week'), dayjs().endOf('week')] },
  { label: '本月', value: () => [dayjs().startOf('month'), dayjs().endOf('month')] },
  { label: '上月', value: () => [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')] },
  { label: '本季度', value: () => [dayjs().startOf('quarter'), dayjs().endOf('quarter')] },
  { label: '本年', value: () => [dayjs().startOf('year'), dayjs().endOf('year')] },
];

// 表格列定义（使用 allColumns）

// 分页配置
const tablePagination = reactive({
  current: 1,
  pageSize: PAGE_SIZE,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showLessItems: true,
  pageSizeOptions: PAGE_SIZE_OPTIONS,
  showTotal: (total) => `共${total}条`,
});

// ------------------ 数据初始化 ------------------

onMounted(() => {
  initializeDateRange();
  queryEmployeeList();
  queryDepartmentList();
  queryStatistics();
});

// ------------------ 数据查询 ------------------

// 初始化日期范围
function initializeDateRange() {
  const startOfMonth = dayjs().startOf('month');
  const endOfMonth = dayjs().endOf('month');
  dateRange.value = [startOfMonth, endOfMonth];
  queryForm.startDate = startOfMonth.format('YYYY-MM-DD');
  queryForm.endDate = endOfMonth.format('YYYY-MM-DD');
}

// 查询员工列表
async function queryEmployeeList() {
  try {
    const result = await attendanceApi.getEmployeeSchedule({ pageNum: 1, pageSize: 1000 });
    employeeList.value = result.data.list || [];
  } catch (err) {
    smartSentry.captureError(err);
  }
}

// 查询部门列表
async function queryDepartmentList() {
  try {
    // 这里应该调用部门查询API，暂时使用模拟数据
    departmentList.value = [
      { departmentId: 1, departmentName: '技术部' },
      { departmentId: 2, departmentName: '人事部' },
      { departmentId: 3, departmentName: '财务部' },
      { departmentId: 4, departmentName: '市场部' }
    ];
  } catch (err) {
    smartSentry.captureError(err);
  }
}

// 查询统计数据
async function queryStatistics() {
  try {
    tableLoading.value = true;

    let result;
    if (queryForm.statisticsType === 'personal') {
      result = await attendanceStatisticsApi.getPersonalStatistics(queryForm);
    } else if (queryForm.statisticsType === 'department') {
      result = await attendanceStatisticsApi.getDepartmentStatistics(queryForm);
    } else if (queryForm.statisticsType === 'company') {
      result = await attendanceStatisticsApi.getCompanyStatistics(queryForm);
    } else {
      result = await attendanceStatisticsApi.getAttendanceStatistics(queryForm);
    }

    tableData.value = result.data.list || [];
    total.value = result.data.total || 0;
    tablePagination.total = total.value;

    // 更新概览卡片
    updateOverviewCards(result.data.overview);

    // 等待DOM更新后绘制图表
    await nextTick();
    renderCharts(result.data.charts);
  } catch (err) {
    smartSentry.captureError(err);
    message.error('查询统计数据失败');
  } finally {
    tableLoading.value = false;
  }
}

// 更新概览卡片
function updateOverviewCards(overview) {
  if (overview) {
    overviewCards.value[0].value = overview.attendanceRate || 0;
    overviewCards.value[1].value = overview.avgWorkHours || 0;
    overviewCards.value[2].value = overview.lateCount || 0;
    overviewCards.value[3].value = overview.abnormalCount || 0;

    // 设置趋势数据
    overviewCards.value.forEach((card, index) => {
      card.trend = overview.trends?.[index] || 0;
    });
  }
}

// 绘制图表
function renderCharts(chartData) {
  if (!chartData) return;

  // 趋势图
  if (trendChartRef.value && chartData.trendChart) {
    const trendChart = echarts.init(trendChartRef.value);
    trendChart.setOption(chartData.trendChart);
  }

  // 状态分布图
  if (statusChartRef.value && chartData.statusChart) {
    const statusChart = echarts.init(statusChartRef.value);
    statusChart.setOption(chartData.statusChart);
  }

  // 部门对比图
  if (departmentChartRef.value && chartData.departmentChart) {
    const departmentChart = echarts.init(departmentChartRef.value);
    departmentChart.setOption(chartData.departmentChart);
  }

  // 个人详情图
  if (personalChartRef.value && chartData.personalChart) {
    const personalChart = echarts.init(personalChartRef.value);
    personalChart.setOption(chartData.personalChart);
  }

  // 异常统计图
  if (abnormalChartRef.value && chartData.abnormalChart) {
    const abnormalChart = echarts.init(abnormalChartRef.value);
    abnormalChart.setOption(chartData.abnormalChart);
  }
}

// ------------------ 事件处理 ------------------

// 统计类型变化
function onStatisticsTypeChange() {
  // 清空相关查询条件
  queryForm.employeeId = undefined;
  queryForm.departmentId = undefined;

  // 重新查询
  queryStatistics();
}

// 日期范围变化
function onDateRangeChange(dates, dateStrings) {
  queryForm.startDate = dateStrings[0];
  queryForm.endDate = dateStrings[1];
}

// 搜索
function onSearch() {
  queryForm.pageNum = 1;
  tablePagination.current = 1;
  queryStatistics();
}

// 重置
function onReload() {
  Object.assign(queryForm, queryFormState);
  initializeDateRange();
  tablePagination.current = 1;
  queryStatistics();
}

// 表格变化
function onTableChange(pagination) {
  queryForm.pageNum = pagination.current;
  queryForm.pageSize = pagination.pageSize;
  tablePagination.current = pagination.current;
  tablePagination.pageSize = pagination.pageSize;
  queryStatistics();
}

// 导出报表
async function exportReport() {
  try {
    const params = {
      ...queryForm,
      exportType: queryForm.statisticsType
    };

    await attendanceApi.exportAttendanceReport(params);
    message.success('报表导出成功');
  } catch (err) {
    smartSentry.captureError(err);
    message.error('报表导出失败');
  }
}

// 查看详情
function viewDetail(record) {
  selectedRecord.value = record;
  showDetailModal.value = true;
}

// 查看员工画像
function viewEmployeeProfile(employeeId) {
  selectedEmployeeId.value = employeeId;
  showEmployeeProfileModal.value = true;
}

// 实时刷新数据
async function refreshRealtimeData() {
  try {
    realtimeLoading.value = true;
    const result = await attendanceStatisticsApi.getTodayRealtimeStatistics();

    // 更新概览卡片数据
    if (result.data) {
      updateOverviewCards(result.data.overview);
    }

    message.success('实时数据刷新成功');
  } catch (err) {
    smartSentry.captureError(err);
    message.error('实时数据刷新失败');
  } finally {
    realtimeLoading.value = false;
  }
}

// 选择时间范围
function selectTimeRange(type) {
  let start, end;
  const now = dayjs();

  switch (type) {
    case 'today':
      start = now.startOf('day');
      end = now.endOf('day');
      break;
    case 'yesterday':
      start = now.subtract(1, 'day').startOf('day');
      end = now.subtract(1, 'day').endOf('day');
      break;
    case 'thisWeek':
      start = now.startOf('week');
      end = now.endOf('week');
      break;
    case 'thisMonth':
      start = now.startOf('month');
      end = now.endOf('month');
      break;
    case 'thisQuarter':
      start = now.startOf('quarter');
      end = now.endOf('quarter');
      break;
    case 'thisYear':
      start = now.startOf('year');
      end = now.endOf('year');
      break;
  }

  dateRange.value = [start, end];
  queryForm.startDate = start.format('YYYY-MM-DD');
  queryForm.endDate = end.format('YYYY-MM-DD');
}

// 对比分析切换
function onComparisonToggle(checked) {
  if (checked) {
    // 设置默认对比期间为上一个周期
    const start = dayjs(queryForm.startDate);
    const end = dayjs(queryForm.endDate);
    const duration = end.diff(start, 'day');

    const comparisonStart = start.subtract(duration + 1, 'day');
    const comparisonEnd = end.subtract(duration + 1, 'day');

    comparisonDateRange.value = [comparisonStart, comparisonEnd];
  }
}

// 对比日期变化
function onComparisonDateChange(dates, dateStrings) {
  // 这里可以处理对比日期变化的逻辑
}

// 对比类型变化
function onComparisonTypeChange(value) {
  // 这里可以处理对比类型变化的逻辑
}

// 图表标签页切换
function onChartTabChange(activeKey) {
  // 根据不同标签页加载对应图表数据
  loadChartData(activeKey);
}

// 趋势图类型变化
function onTrendChartTypeChange(e) {
  trendChartType.value = e.target.value;
  // 重新渲染趋势图
  renderTrendChart();
}

// 热力图维度变化
function onHeatmapDimensionChange(value) {
  heatmapDimension.value = value;
  // 重新渲染热力图
  renderHeatmapChart();
}

// 加载图表数据
async function loadChartData(chartType) {
  try {
    let chartData;

    switch (chartType) {
      case 'trend':
        chartData = await attendanceStatisticsApi.getTrendChartData(queryForm);
        break;
      case 'distribution':
        chartData = await attendanceStatisticsApi.getStatusDistributionChartData(queryForm);
        break;
      case 'comparison':
        if (queryForm.enableComparison) {
          chartData = await attendanceStatisticsApi.getComparisonChartData({
            ...queryForm,
            comparisonStartDate: comparisonDateRange.value[0].format('YYYY-MM-DD'),
            comparisonEndDate: comparisonDateRange.value[1].format('YYYY-MM-DD'),
            comparisonType: comparisonType.value
          });
        }
        break;
      case 'heatmap':
        chartData = await attendanceStatisticsApi.getHeatmapChartData({
          ...queryForm,
          dimension: heatmapDimension.value
        });
        break;
      case 'prediction':
        chartData = await attendanceStatisticsApi.getAttendancePrediction(queryForm);
        break;
    }

    if (chartData && chartData.success) {
      await nextTick();
      renderSpecificChart(chartType, chartData.data);
    }
  } catch (err) {
    smartSentry.captureError(err);
    console.error('加载图表数据失败:', err);
  }
}

// 渲染特定图表
function renderSpecificChart(chartType, data) {
  switch (chartType) {
    case 'trend':
      renderTrendChartWithData(data);
      break;
    case 'distribution':
      renderDistributionCharts(data);
      break;
    case 'comparison':
      renderComparisonChart(data);
      break;
    case 'heatmap':
      renderHeatmapChartWithData(data);
      break;
    case 'prediction':
      renderPredictionChart(data);
      break;
  }
}

// 处理导出
async function handleExport({ key }) {
  try {
    const exportType = key; // excel, pdf, csv
    const params = {
      ...queryForm,
      exportType: exportType,
      statisticsType: queryForm.statisticsType
    };

    switch (exportType) {
      case 'excel':
        await attendanceStatisticsApi.exportStatisticsReport(params);
        break;
      case 'pdf':
        await attendanceStatisticsApi.exportStatisticsReport({ ...params, format: 'pdf' });
        break;
      case 'csv':
        await attendanceStatisticsApi.exportStatisticsReport({ ...params, format: 'csv' });
        break;
    }

    message.success(`${exportType.toUpperCase()}报表导出成功`);
  } catch (err) {
    smartSentry.captureError(err);
    message.error('报表导出失败');
  }
}

// 显示报表生成模态框
function showReportGenerateModal() {
  showReportGenerateModal.value = true;
}

// 显示预警配置模态框
function showAlertConfigModal() {
  showAlertConfigModal.value = true;
}

// 显示高级查询模态框
function showAdvancedQuery() {
  showAdvancedQueryModal.value = true;
}

// 显示列配置模态框
function showColumnConfig() {
  showColumnConfigModal.value = true;
}

// 报表生成成功回调
function onReportGenerateSuccess() {
  message.success('报表生成任务已提交');
  // 可以刷新报表历史记录
}

// 预警配置成功回调
function onAlertConfigSuccess() {
  message.success('预警配置已保存');
}

// 高级查询成功回调
function onAdvancedQuerySuccess(advancedQuery) {
  // 合并高级查询条件到主查询表单
  Object.assign(queryForm, advancedQuery);
  onSearch();
}

// 列配置成功回调
function onColumnConfigSuccess(columns) {
  allColumns.value = columns;
}

// 获取出勤率状态
function getAttendanceRateStatus(rate) {
  if (rate >= 95) return 'success';
  if (rate >= 80) return 'normal';
  return 'exception';
}

// 获取迟到次数颜色
function getLateCountColor(count) {
  if (count > 3) return 'red';
  if (count > 0) return 'orange';
  return 'green';
}

// 获取早退次数颜色
function getEarlyLeaveCountColor(count) {
  if (count > 3) return 'red';
  if (count > 0) return 'orange';
  return 'green';
}

// 获取工作时长样式类
function getWorkHoursClass(actual, standard) {
  if (actual >= standard) return 'text-success';
  if (actual >= standard * 0.9) return 'text-warning';
  return 'text-danger';
}

// 员工过滤
function filterEmployeeOption(input, option) {
  return option.children.toLowerCase().includes(input.toLowerCase());
}

// 图表渲染方法占位符（这些方法将在实际使用时实现）
function renderTrendChart() {
  // 趋势图渲染逻辑
}

function renderTrendChartWithData(data) {
  if (trendChartRef.value && data) {
    const chart = echarts.init(trendChartRef.value);
    chart.setOption(data);
  }
}

function renderDistributionCharts(data) {
  if (statusChartRef.value && data.statusChart) {
    const statusChart = echarts.init(statusChartRef.value);
    statusChart.setOption(data.statusChart);
  }
  if (departmentChartRef.value && data.departmentChart) {
    const departmentChart = echarts.init(departmentChartRef.value);
    departmentChart.setOption(data.departmentChart);
  }
}

function renderComparisonChart(data) {
  if (comparisonChartRef.value && data) {
    const chart = echarts.init(comparisonChartRef.value);
    chart.setOption(data);
  }
}

function renderHeatmapChart() {
  // 热力图渲染逻辑
}

function renderHeatmapChartWithData(data) {
  if (heatmapChartRef.value && data) {
    const chart = echarts.init(heatmapChartRef.value);
    chart.setOption(data);
  }
}

function renderPredictionChart(data) {
  if (predictionChartRef.value && data) {
    const chart = echarts.init(predictionChartRef.value);
    chart.setOption(data);
  }
}
</script>

<style lang="less" scoped>
.attendance-statistics-container {
  padding: 16px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.mb-4 {
  margin-bottom: 16px;
}

.overview-card {
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .card-trend {
    margin-top: 8px;
    font-size: 12px;

    .trend-value {
      margin-right: 4px;

      &.up {
        color: #52c41a;
      }

      &.down {
        color: #f5222d;
      }
    }

    .trend-text {
      color: #8c8c8c;
    }
  }
}

.chart-container {
  height: 300px;
  width: 100%;
}

.employee-name {
  font-weight: 500;
  color: #262626;
}

.employee-dept {
  font-size: 12px;
  color: #8c8c8c;
}

.text-success {
  color: #52c41a;
  font-weight: 500;
}

.text-warning {
  color: #fa8c16;
  font-weight: 500;
}

.text-danger {
  color: #f5222d;
  font-weight: 500;
}

.smart-table-operate {
  display: flex;
  gap: 8px;
}

:deep(.ant-card-head-title) {
  font-weight: 600;
  color: #262626;
}

:deep(.ant-statistic-title) {
  font-size: 14px;
  color: #8c8c8c;
}

:deep(.ant-statistic-content) {
  font-size: 24px;
  font-weight: 600;
}

:deep(.ant-table-small) {
  .ant-table-thead > tr > th {
    background: #fafafa;
    font-weight: 500;
  }
}

:deep(.ant-progress-line) {
  .ant-progress-text {
    font-size: 12px;
  }
}

:deep(.ant-modal-body) {
  .ant-descriptions {
    margin-bottom: 16px;
  }
}
</style>