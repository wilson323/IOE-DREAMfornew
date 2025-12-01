<template>
  <div class="consume-report-page">
    <!-- 页面标题和操作区域 -->
    <div class="page-header">
      <div class="header-title">
        <h2>消费统计分析</h2>
        <p class="header-description">多维度消费数据分析和可视化报表</p>
      </div>
      <div class="header-actions">
        <a-button type="primary" @click="showExportModal">
          <template #icon><ExportOutlined /></template>
          导出报表
        </a-button>
        <a-button @click="refreshData">
          <template #icon><ReloadOutlined /></template>
          刷新数据
        </a-button>
        <a-button @click="toggleFullscreen" :type="isFullscreen ? 'primary' : 'default'">
          <template #icon><FullscreenOutlined /></template>
          {{ isFullscreen ? '退出全屏' : '全屏显示' }}
        </a-button>
      </div>
    </div>

    <!-- 时间筛选器 -->
    <a-card class="filter-card" :bordered="false">
      <a-form :model="filterForm" layout="inline">
        <a-form-item label="时间维度">
          <a-select
            v-model:value="filterForm.timeDimension"
            style="width: 120px"
            @change="handleTimeDimensionChange"
          >
            <a-select-option value="day">按天</a-select-option>
            <a-select-option value="week">按周</a-select-option>
            <a-select-option value="month">按月</a-select-option>
            <a-select-option value="quarter">按季度</a-select-option>
            <a-select-option value="year">按年</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="时间范围">
          <a-range-picker
            v-model:value="filterForm.dateRange"
            :picker="getDatePickerType()"
            format="YYYY-MM-DD"
            style="width: 280px"
            @change="handleDateRangeChange"
          />
        </a-form-item>

        <a-form-item label="对比周期">
          <a-checkbox v-model:checked="filterForm.enableComparison">启用同期对比</a-checkbox>
        </a-form-item>

        <a-form-item label="数据筛选">
          <a-space>
            <a-select
              v-model:value="filterForm.deviceFilter"
              placeholder="选择设备"
              allow-clear
              style="width: 150px"
            >
              <a-select-option
                v-for="device in deviceOptions"
                :key="device.deviceId"
                :value="device.deviceId"
              >
                {{ device.deviceName }}
              </a-select-option>
            </a-select>

            <a-select
              v-model:value="filterForm.modeFilter"
              placeholder="消费模式"
              allow-clear
              style="width: 120px"
            >
              <a-select-option value="FIXED_AMOUNT">固定金额</a-select-option>
              <a-select-option value="FREE_AMOUNT">自由金额</a-select-option>
              <a-select-option value="METERING">计量计费</a-select-option>
              <a-select-option value="PRODUCT_SCAN">商品扫码</a-select-option>
              <a-select-option value="ORDERING">订餐模式</a-select-option>
              <a-select-option value="SMART">智能模式</a-select-option>
            </a-select>
          </a-space>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch" :loading="loading">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="resetFilter">
              <template #icon><ClearOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 核心指标卡片 -->
    <div class="metrics-cards">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card class="metric-card">
            <a-statistic
              title="总消费笔数"
              :value="summaryData.totalCount"
              :precision="0"
              :value-style="{ color: '#1890ff' }"
            >
              <template #prefix>
                <ShoppingCartOutlined />
              </template>
            </a-statistic>
            <div class="metric-trend">
              <span :class="['trend-value', summaryData.countTrend > 0 ? 'up' : 'down']">
                {{ Math.abs(summaryData.countTrend) }}%
              </span>
              较上期
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="metric-card">
            <a-statistic
              title="总消费金额"
              :value="summaryData.totalAmount"
              :precision="2"
              prefix="¥"
              :value-style="{ color: '#52c41a' }"
            >
              <template #prefix>
                <DollarOutlined />
              </template>
            </a-statistic>
            <div class="metric-trend">
              <span :class="['trend-value', summaryData.amountTrend > 0 ? 'up' : 'down']">
                {{ Math.abs(summaryData.amountTrend) }}%
              </span>
              较上期
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="metric-card">
            <a-statistic
              title="平均消费金额"
              :value="summaryData.avgAmount"
              :precision="2"
              prefix="¥"
              :value-style="{ color: '#faad14' }"
            >
              <template #prefix>
                <BarChartOutlined />
              </template>
            </a-statistic>
            <div class="metric-trend">
              <span :class="['trend-value', summaryData.avgTrend > 0 ? 'up' : 'down']">
                {{ Math.abs(summaryData.avgTrend) }}%
              </span>
              较上期
            </div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="metric-card">
            <a-statistic
              title="活跃用户数"
              :value="summaryData.activeUsers"
              :precision="0"
              :value-style="{ color: '#722ed1' }"
            >
              <template #prefix>
                <UserOutlined />
              </template>
            </a-statistic>
            <div class="metric-trend">
              <span :class="['trend-value', summaryData.userTrend > 0 ? 'up' : 'down']">
                {{ Math.abs(summaryData.userTrend) }}%
              </span>
              较上期
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 图表区域 -->
    <div :class="['charts-container', { 'fullscreen': isFullscreen }]">
      <!-- 消费趋势图 -->
      <a-card class="chart-card" title="消费趋势分析" :bordered="false">
        <template #extra>
          <a-radio-group
            v-model:value="trendChartType"
            size="small"
            @change="handleTrendChartTypeChange"
          >
            <a-radio-button value="amount">金额</a-radio-button>
            <a-radio-button value="count">笔数</a-radio-button>
            <a-radio-button value="users">用户</a-radio-button>
          </a-radio-group>
        </template>
        <div class="chart-wrapper">
          <v-chart
            class="chart"
            :option="trendChartOption"
            :loading="trendLoading"
            @click="handleTrendChartClick"
          />
        </div>
      </a-card>

      <!-- 消费模式分布 -->
      <a-card class="chart-card" title="消费模式分布" :bordered="false">
        <template #extra>
          <a-radio-group
            v-model:value="modeChartType"
            size="small"
            @change="handleModeChartTypeChange"
          >
            <a-radio-button value="pie">饼图</a-radio-button>
            <a-radio-button value="bar">柱状图</a-radio-button>
          </a-radio-group>
        </template>
        <div class="chart-wrapper">
          <v-chart
            class="chart"
            :option="modeChartOption"
            :loading="modeLoading"
          />
        </div>
      </a-card>

      <!-- 设备消费排行 -->
      <a-card class="chart-card" title="设备消费排行" :bordered="false">
        <template #extra>
          <a-radio-group
            v-model:value="deviceChartMetric"
            size="small"
            @change="handleDeviceChartMetricChange"
          >
            <a-radio-button value="amount">金额</a-radio-button>
            <a-radio-button value="count">笔数</a-radio-button>
          </a-radio-group>
        </template>
        <div class="chart-wrapper">
          <v-chart
            class="chart"
            :option="deviceChartOption"
            :loading="deviceLoading"
          />
        </div>
      </a-card>

      <!-- 用户消费排行 -->
      <a-card class="chart-card" title="用户消费排行" :bordered="false">
        <template #extra>
          <a-select
            v-model:value="userRankingType"
            style="width: 100px"
            size="small"
            @change="handleUserRankingTypeChange"
          >
            <a-select-option value="amount">消费金额</a-select-option>
            <a-select-option value="count">消费次数</a-select-option>
          </a-select>
        </template>
        <div class="chart-wrapper">
          <v-chart
            class="chart"
            :option="userRankingChartOption"
            :loading="userRankingLoading"
          />
        </div>
      </a-card>

      <!-- 时段分布分析 -->
      <a-card class="chart-card" title="时段分布分析" :bordered="false">
        <div class="chart-wrapper">
          <v-chart
            class="chart"
            :option="hourDistributionOption"
            :loading="hourLoading"
          />
        </div>
      </a-card>

      <!-- 地区分布热力图 -->
      <a-card class="chart-card" title="地区分布热力图" :bordered="false">
        <template #extra>
          <a-select
            v-model:value="regionMetric"
            style="width: 100px"
            size="small"
            @change="handleRegionMetricChange"
          >
            <a-select-option value="amount">消费金额</a-select-option>
            <a-select-option value="count">消费次数</a-select-option>
          </a-select>
        </template>
        <div class="chart-wrapper">
          <v-chart
            class="chart"
            :option="regionHeatmapOption"
            :loading="regionLoading"
          />
        </div>
      </a-card>
    </div>

    <!-- 数据表格 -->
    <a-card class="table-card" title="详细数据" :bordered="false">
      <template #extra>
        <a-space>
          <a-button size="small" @click="exportTableData">
            <template #icon><ExportOutlined /></template>
            导出表格
          </a-button>
          <a-button size="small" @click="refreshTableData">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="tableColumns"
        :data-source="tableData"
        :loading="tableLoading"
        :pagination="tablePagination"
        :row-key="record => record.id"
        @change="handleTableChange"
        :scroll="{ x: 1000 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'growthRate'">
            <span :class="['growth-rate', record.growthRate > 0 ? 'positive' : 'negative']">
              {{ record.growthRate > 0 ? '+' : '' }}{{ record.growthRate.toFixed(2) }}%
            </span>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 导出模态框 -->
    <ExportModal
      v-model:visible="exportVisible"
      :report-data="reportData"
      @export="handleExport"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  ExportOutlined,
  ReloadOutlined,
  FullscreenOutlined,
  SearchOutlined,
  ClearOutlined,
  ShoppingCartOutlined,
  DollarOutlined,
  BarChartOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import {
  CanvasRenderer
} from 'echarts/renderers'
import {
  LineChart,
  BarChart,
  PieChart,
  MapChart,
  HeatmapChart
} from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
  VisualMapComponent
} from 'echarts/components'
import { consumeRecordApi } from '@/api/business/consume/record-api'
import { consumeReportApi } from '@/api/business/consume/report-api'
import { deviceApi } from '@/api/business/device/device-api'
import ExportModal from './components/ExportModal.vue'
import dayjs from 'dayjs'

// 注册ECharts组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  MapChart,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  DataZoomComponent,
  ToolboxComponent,
  VisualMapComponent
])

// 响应式数据
const loading = ref(false)
const isFullscreen = ref(false)
const exportVisible = ref(false)

// 图表加载状态
const trendLoading = ref(false)
const modeLoading = ref(false)
const deviceLoading = ref(false)
const userRankingLoading = ref(false)
const hourLoading = ref(false)
const regionLoading = ref(false)
const tableLoading = ref(false)

// 数据选项
const deviceOptions = ref([])
const reportData = ref({})

// 筛选表单
const filterForm = reactive({
  timeDimension: 'day',
  dateRange: [],
  enableComparison: false,
  deviceFilter: undefined,
  modeFilter: undefined
})

// 图表配置
const trendChartType = ref('amount')
const modeChartType = ref('pie')
const deviceChartMetric = ref('amount')
const userRankingType = ref('amount')
const regionMetric = ref('amount')

// 核心数据
const summaryData = reactive({
  totalCount: 0,
  totalAmount: 0,
  avgAmount: 0,
  activeUsers: 0,
  countTrend: 0,
  amountTrend: 0,
  avgTrend: 0,
  userTrend: 0
})

// 图表选项
const trendChartOption = ref({})
const modeChartOption = ref({})
const deviceChartOption = ref({})
const userRankingChartOption = ref({})
const hourDistributionOption = ref({})
const regionHeatmapOption = ref({})

// 表格数据
const tableData = ref([])
const tablePagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
})

// 表格列定义
const tableColumns = ref([
  {
    title: '时间',
    dataIndex: 'timeDimension',
    key: 'timeDimension',
    width: 120,
    sorter: true
  },
  {
    title: '消费笔数',
    dataIndex: 'consumeCount',
    key: 'consumeCount',
    width: 100,
    sorter: true
  },
  {
    title: '消费金额',
    dataIndex: 'consumeAmount',
    key: 'consumeAmount',
    width: 120,
    sorter: true,
    customRender: ({ text }) => `¥${Number(text).toFixed(2)}`
  },
  {
    title: '平均金额',
    dataIndex: 'avgAmount',
    key: 'avgAmount',
    width: 120,
    sorter: true,
    customRender: ({ text }) => `¥${Number(text).toFixed(2)}`
  },
  {
    title: '活跃用户',
    dataIndex: 'activeUsers',
    key: 'activeUsers',
    width: 100,
    sorter: true
  },
  {
    title: '增长率',
    key: 'growthRate',
    width: 100,
    sorter: true
  }
])

// 生命周期
onMounted(() => {
  initializeDateRange()
  loadDeviceOptions()
  loadAllData()
})

onUnmounted(() => {
  // 清理全屏状态
  if (isFullscreen.value) {
    document.exitFullscreen?.()
  }
})

// 方法定义
const initializeDateRange = () => {
  const endDate = dayjs()
  const startDate = endDate.subtract(30, 'day')
  filterForm.dateRange = [startDate, endDate]
}

const loadDeviceOptions = async () => {
  try {
    const result = await deviceApi.getDeviceList({ pageSize: 1000 })
    deviceOptions.value = result.data?.records || []
  } catch (error) {
    console.error('加载设备选项失败:', error)
  }
}

const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadSummaryData(),
      loadTrendData(),
      loadModeData(),
      loadDeviceData(),
      loadUserRankingData(),
      loadHourDistributionData(),
      loadRegionData(),
      loadTableData()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const loadSummaryData = async () => {
  try {
    const params = buildApiParams()
    const result = await consumeRecordApi.getStatistics()
    if (result.success) {
      const data = result.data || {}
      Object.assign(summaryData, {
        totalCount: data.todayCount || 0,
        totalAmount: data.todayAmount || 0,
        avgAmount: data.todayAmount ? (data.todayAmount / (data.todayCount || 1)) : 0,
        activeUsers: data.activeUsers || 0,
        countTrend: data.countTrend || 0,
        amountTrend: data.amountTrend || 0,
        avgTrend: data.avgTrend || 0,
        userTrend: data.userTrend || 0
      })
    }
  } catch (error) {
    console.error('加载汇总数据失败:', error)
  }
}

const loadTrendData = async () => {
  trendLoading.value = true
  try {
    const params = buildApiParams()
    const result = await consumeRecordApi.getConsumeTrend(params)
    if (result.success) {
      updateTrendChart(result.data)
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  } finally {
    trendLoading.value = false
  }
}

const loadModeData = async () => {
  modeLoading.value = true
  try {
    const params = buildApiParams()
    const result = await consumeRecordApi.getConsumeModeStats(params)
    if (result.success) {
      updateModeChart(result.data)
    }
  } catch (error) {
    console.error('加载模式数据失败:', error)
  } finally {
    modeLoading.value = false
  }
}

const loadDeviceData = async () => {
  deviceLoading.value = true
  try {
    const params = buildApiParams()
    const result = await consumeRecordApi.getDeviceStats(params)
    if (result.success) {
      updateDeviceChart(result.data)
    }
  } catch (error) {
    console.error('加载设备数据失败:', error)
  } finally {
    deviceLoading.value = false
  }
}

const loadUserRankingData = async () => {
  userRankingLoading.value = true
  try {
    const params = buildApiParams()
    const result = await consumeRecordApi.getUserRanking(params)
    if (result.success) {
      updateUserRankingChart(result.data)
    }
  } catch (error) {
    console.error('加载用户排行数据失败:', error)
  } finally {
    userRankingLoading.value = false
  }
}

const loadHourDistributionData = async () => {
  hourLoading.value = true
  try {
    const params = buildApiParams()
    // TODO: 调用时段分布API
    // const result = await consumeRecordApi.getHourDistribution(params)
    updateHourChart([])
  } catch (error) {
    console.error('加载时段分布数据失败:', error)
  } finally {
    hourLoading.value = false
  }
}

const loadRegionData = async () => {
  regionLoading.value = true
  try {
    const params = buildApiParams()
    // TODO: 调用地区分布API
    // const result = await consumeRecordApi.getRegionDistribution(params)
    updateRegionChart([])
  } catch (error) {
    console.error('加载地区数据失败:', error)
  } finally {
    regionLoading.value = false
  }
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      ...buildApiParams(),
      pageNum: tablePagination.current,
      pageSize: tablePagination.pageSize
    }

    // TODO: 调用详细数据API
    // const result = await consumeRecordApi.getDetailedData(params)
    tableData.value = []
    tablePagination.total = 0

  } catch (error) {
    console.error('加载表格数据失败:', error)
  } finally {
    tableLoading.value = false
  }
}

// 图表更新方法
const updateTrendChart = (data) => {
  const xAxis = data.map(item => item.date)
  const seriesData = data.map(item => {
    switch (trendChartType.value) {
      case 'amount':
        return item.amount
      case 'count':
        return item.count
      case 'users':
        return item.users
      default:
        return item.amount
    }
  })

  trendChartOption.value = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: [getTrendChartLabel()]
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxis
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: getTrendChartLabel(),
        type: 'line',
        smooth: true,
        data: seriesData,
        areaStyle: {
          opacity: 0.3
        }
      }
    ]
  }
}

const updateModeChart = (data) => {
  if (modeChartType.value === 'pie') {
    modeChartOption.value = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '消费模式',
          type: 'pie',
          radius: '50%',
          data: data.map(item => ({
            value: item.count,
            name: getConsumeModeText(item.mode)
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    }
  } else {
    modeChartOption.value = {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: data.map(item => getConsumeModeText(item.mode))
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '消费笔数',
          type: 'bar',
          data: data.map(item => item.count)
        }
      ]
    }
  }
}

const updateDeviceChart = (data) => {
  deviceChartOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.slice(0, 10).map(item => item.deviceName)
    },
    series: [
      {
        name: deviceChartMetric.value === 'amount' ? '消费金额' : '消费笔数',
        type: 'bar',
        data: data.slice(0, 10).map(item =>
          deviceChartMetric.value === 'amount' ? item.amount : item.count
        )
      }
    ]
  }
}

const updateUserRankingChart = (data) => {
  userRankingChartOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: data.slice(0, 10).map(item => item.userName)
    },
    series: [
      {
        name: userRankingType.value === 'amount' ? '消费金额' : '消费次数',
        type: 'bar',
        data: data.slice(0, 10).map(item =>
          userRankingType.value === 'amount' ? item.amount : item.count
        )
      }
    ]
  }
}

const updateHourChart = (data) => {
  const hours = Array.from({ length: 24 }, (_, i) => i)
  const values = Array.from({ length: 24 }, () => Math.floor(Math.random() * 100))

  hourDistributionOption.value = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: hours.map(h => `${h}:00`)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '消费分布',
        type: 'bar',
        data: values
      }
    ]
  }
}

const updateRegionChart = (data) => {
  regionHeatmapOption.value = {
    tooltip: {
      position: 'top'
    },
    grid: {
      height: '50%',
      top: '10%'
    },
    xAxis: {
      type: 'category',
      data: ['北京', '上海', '广州', '深圳', '杭州', '南京', '武汉', '成都'],
      splitArea: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      splitArea: {
        show: true
      }
    },
    visualMap: {
      min: 0,
      max: 10,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '15%'
    },
    series: [
      {
        name: '消费热力图',
        type: 'heatmap',
        data: generateHeatmapData(),
        label: {
          show: true
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
}

const generateHeatmapData = () => {
  const data = []
  for (let i = 0; i < 7; i++) {
    for (let j = 0; j < 8; j++) {
      data.push([j, i, Math.floor(Math.random() * 10)])
    }
  }
  return data
}

// 工具方法
const buildApiParams = () => {
  const params = {}

  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    params.startDate = filterForm.dateRange[0].format('YYYY-MM-DD')
    params.endDate = filterForm.dateRange[1].format('YYYY-MM-DD')
  }

  if (filterForm.deviceFilter) {
    params.deviceId = filterForm.deviceFilter
  }

  if (filterForm.modeFilter) {
    params.consumeMode = filterForm.modeFilter
  }

  if (filterForm.enableComparison) {
    params.comparison = true
  }

  return params
}

const getTrendChartLabel = () => {
  const labelMap = {
    amount: '消费金额',
    count: '消费笔数',
    users: '活跃用户'
  }
  return labelMap[trendChartType.value] || '消费金额'
}

const getConsumeModeText = (mode) => {
  const modeMap = {
    'FIXED_AMOUNT': '固定金额',
    'FREE_AMOUNT': '自由金额',
    'METERING': '计量计费',
    'PRODUCT_SCAN': '商品扫码',
    'ORDERING': '订餐模式',
    'SMART': '智能模式'
  }
  return modeMap[mode] || mode
}

const getDatePickerType = () => {
  const typeMap = {
    day: 'date',
    week: 'week',
    month: 'month',
    quarter: 'quarter',
    year: 'year'
  }
  return typeMap[filterForm.timeDimension] || 'date'
}

// 事件处理
const handleTimeDimensionChange = () => {
  // 根据时间维度调整日期范围选择器类型
  nextTick(() => {
    handleSearch()
  })
}

const handleDateRangeChange = () => {
  handleSearch()
}

const handleSearch = () => {
  loadAllData()
}

const resetFilter = () => {
  Object.assign(filterForm, {
    timeDimension: 'day',
    dateRange: [],
    enableComparison: false,
    deviceFilter: undefined,
    modeFilter: undefined
  })
  initializeDateRange()
  handleSearch()
}

const refreshData = () => {
  loadAllData()
}

const refreshTableData = () => {
  loadTableData()
}

const handleTrendChartTypeChange = () => {
  loadTrendData()
}

const handleModeChartTypeChange = () => {
  loadModeData()
}

const handleDeviceChartMetricChange = () => {
  loadDeviceData()
}

const handleUserRankingTypeChange = () => {
  loadUserRankingData()
}

const handleRegionMetricChange = () => {
  loadRegionData()
}

const handleTrendChartClick = (params) => {
  console.log('趋势图点击:', params)
  // 可以实现点击图表后的钻取功能
}

const handleTableChange = (pagination, filters, sorter) => {
  tablePagination.current = pagination.current
  tablePagination.pageSize = pagination.pageSize
  loadTableData()
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value

  if (isFullscreen.value) {
    document.documentElement.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}

const showExportModal = () => {
  exportVisible.value = true
  reportData.value = {
    summaryData: { ...summaryData },
    filterForm: { ...filterForm },
    charts: {
      trend: trendChartOption.value,
      mode: modeChartOption.value,
      device: deviceChartOption.value,
      userRanking: userRankingChartOption.value,
      hour: hourDistributionOption.value,
      region: regionHeatmapOption.value
    }
  }
}

const handleExport = (exportParams) => {
  console.log('导出参数:', exportParams)
  message.success('报表导出成功')
  exportVisible.value = false
}

const exportTableData = () => {
  // 导出表格数据
  message.success('表格数据导出成功')
}
</script>

<style lang="less" scoped>
.consume-report-page {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    background: white;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .header-title {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        font-weight: 600;
        color: #262626;
      }

      .header-description {
        margin: 0;
        color: #8c8c8c;
        font-size: 14px;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .filter-card {
    margin-bottom: 24px;
  }

  .metrics-cards {
    margin-bottom: 24px;

    .metric-card {
      text-align: center;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      .metric-trend {
        margin-top: 8px;
        font-size: 12px;
        color: #8c8c8c;

        .trend-value {
          font-weight: 600;
          margin-right: 4px;

          &.up {
            color: #52c41a;
          }

          &.down {
            color: #ff4d4f;
          }
        }
      }
    }
  }

  .charts-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;
    margin-bottom: 24px;

    &.fullscreen {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 9999;
      background: white;
      padding: 24px;
      overflow-y: auto;
      grid-template-columns: repeat(3, 1fr);
    }

    .chart-card {
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

      .chart-wrapper {
        height: 400px;

        .chart {
          height: 100%;
          width: 100%;
        }
      }
    }
  }

  .table-card {
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .growth-rate {
      font-weight: 600;

      &.positive {
        color: #52c41a;
      }

      &.negative {
        color: #ff4d4f;
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .consume-report-page {
    .charts-container {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .consume-report-page {
    padding: 16px;

    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;

      .header-actions {
        width: 100%;
        justify-content: flex-end;
      }
    }

    .metrics-cards {
      .ant-col {
        margin-bottom: 16px;
      }
    }

    .charts-container {
      &.fullscreen {
        grid-template-columns: 1fr;
        padding: 16px;
      }

      .chart-card {
        .chart-wrapper {
          height: 300px;
        }
      }
    }
  }
}
</style>