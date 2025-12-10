<template>
  <view class="attendance-analytics-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">智能分析</text>
        <text class="filter-btn" @click="showFilterModal = true">筛选</text>
      </view>
    </view>

    <!-- 分析时间范围选择 -->
    <view class="time-range-selector">
      <view class="range-tabs">
        <view
          v-for="range in timeRanges"
          :key="range.value"
          :class="['range-tab', { active: selectedRange === range.value }]"
          @click="selectTimeRange(range.value)"
        >
          <text class="range-text">{{ range.label }}</text>
        </view>
      </view>
      <view class="custom-range" v-if="selectedRange === 'custom'">
        <text class="custom-text">{{ customRangeText }}</text>
        <text class="custom-arrow" @click="showDatePicker">›</text>
      </view>
    </view>

    <!-- 考勤统计概览 -->
    <view class="statistics-overview">
      <view class="overview-grid">
        <view class="stat-card">
          <text class="stat-value">{{ statistics.workDays }}</text>
          <text class="stat-label">工作天数</text>
          <text class="stat-trend" :class="statistics.workDaysTrend">
            {{ formatTrend(statistics.workDaysTrend) }}
          </text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ statistics.attendanceRate }}%</text>
          <text class="stat-label">出勤率</text>
          <text class="stat-trend" :class="statistics.attendanceRateTrend">
            {{ formatTrend(statistics.attendanceRateTrend) }}
          </text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ statistics.lateDays }}</text>
          <text class="stat-label">迟到天数</text>
          <text class="stat-trend" :class="statistics.lateDaysTrend">
            {{ formatTrend(statistics.lateDaysTrend) }}
          </text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ statistics.earlyDays }}</text>
          <text class="stat-label">早退天数</text>
          <text class="stat-trend" :class="statistics.earlyDaysTrend">
            {{ formatTrend(statistics.earlyDaysTrend) }}
          </text>
        </view>
      </view>
    </view>

    <!-- 出勤图表 -->
    <view class="chart-section">
      <view class="chart-header">
        <text class="chart-title">出勤趋势</text>
        <text class="chart-type" @click="switchChartType">{{ getChartTypeText() }}</text>
      </view>
      <view class="chart-container">
        <!-- 这里可以集成图表库，如ECharts -->
        <view class="chart-placeholder">
          <text class="chart-icon">📊</text>
          <text class="chart-text">{{ getChartTypeText() }}图表</text>
        </view>
      </view>
    </view>

    <!-- 异常提醒 -->
    <view class="abnormal-alerts" v-if="abnormalAlerts.length > 0">
      <text class="section-title">异常提醒</text>
      <view class="alerts-list">
        <view
          v-for="alert in abnormalAlerts"
          :key="alert.id"
          class="alert-item"
          :class="alert.type"
          @click="viewAlertDetail(alert)"
        >
          <view class="alert-icon">
            {{ getAlertIcon(alert.type) }}
          </view>
          <view class="alert-content">
            <text class="alert-title">{{ alert.title }}</text>
            <text class="alert-desc">{{ alert.description }}</text>
            <text class="alert-time">{{ formatTime(alert.createTime) }}</text>
          </view>
          <view class="alert-action">
            <text class="action-text">查看</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 考勤评分 -->
    <view class="score-section">
      <view class="score-header">
        <text class="section-title">考勤评分</text>
        <text class="score-help">?</text>
      </view>
      <view class="score-card">
        <view class="score-circle">
          <view class="score-progress" :style="{ transform: `rotate(${(1 - score/100) * 360}deg)` }"></view>
          <view class="score-center">
            <text class="score-value">{{ score }}</text>
            <text class="score-label">分</text>
          </view>
        </view>
        <view class="score-details">
          <view class="score-item">
            <text class="item-label">出勤分</text>
            <text class="item-value">{{ scoreDetails.attendance }}</text>
          </view>
          <view class="score-item">
            <text class="item-label">守时分</text>
            <text class="item-value">{{ scoreDetails.punctuality }}</text>
          </view>
          <view class="score-item">
            <text class="item-label">加班分</text>
            <text class="item-value">{{ scoreDetails.overtime }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 考勤建议 -->
    <view class="suggestions-section">
      <text class="section-title">改进建议</text>
      <view class="suggestions-list">
        <view
          v-for="suggestion in suggestions"
          :key="suggestion.id"
          class="suggestion-item"
          @click="viewSuggestionDetail(suggestion)"
        >
          <view class="suggestion-icon" :class="suggestion.priority">
            {{ getSuggestionIcon(suggestion.priority) }}
          </view>
          <view class="suggestion-content">
            <text class="suggestion-title">{{ suggestion.title }}</text>
            <text class="suggestion-desc">{{ suggestion.description }}</text>
          </view>
          <view class="suggestion-arrow">›</view>
        </view>
      </view>
    </view>

    <!-- 筛选弹窗 -->
    <view class="filter-modal" v-if="showFilterModal" @click="showFilterModal = false">
      <view class="filter-content" @click.stop>
        <text class="filter-title">数据筛选</text>

        <view class="filter-section">
          <text class="filter-label">时间范围</text>
          <view class="filter-options">
            <text
              v-for="range in timeRanges"
              :key="range.value"
              :class="['filter-option', { selected: selectedRange === range.value }]"
              @click="selectedRange = range.value"
            >
              {{ range.label }}
            </text>
          </view>
        </view>

        <view class="filter-section">
          <text class="filter-label">部门</text>
          <input
            class="filter-input"
            v-model="filterForm.department"
            placeholder="请输入部门名称"
          />
        </view>

        <view class="filter-section">
          <text class="filter-label">员工类型</text>
          <view class="filter-options">
            <text
              v-for="type in employeeTypes"
              :key="type.value"
              :class="['filter-option', { selected: filterForm.employeeType === type.value }]"
              @click="filterForm.employeeType = type.value"
            >
              {{ type.label }}
            </text>
          </view>
        </view>

        <view class="filter-buttons">
          <button class="filter-btn reset" @click="resetFilter">重置</button>
          <button class="filter-btn confirm" @click="applyFilter">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import attendanceApi from '@/api/business/attendance/attendance-api.js'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const userStore = useUserStore()

// 页面状态
const loading = ref(false)
const showFilterModal = ref(false)
const selectedRange = ref('month')
const customRangeText = ref('')
const chartType = ref('attendance') // attendance, punctuality, overtime

// 数据
const statistics = reactive({
  workDays: 22,
  workDaysTrend: 'stable',
  attendanceRate: 96.5,
  attendanceRateTrend: 'up',
  lateDays: 3,
  lateDaysTrend: 'down',
  earlyDays: 1,
  earlyDaysTrend: 'stable'
})

const score = ref(92)
const scoreDetails = reactive({
  attendance: 48,
  punctuality: 38,
  overtime: 6
})

const abnormalAlerts = ref([
  {
    id: 1,
    type: 'warning',
    title: '迟到频繁',
    description: '本月有3次迟到记录',
    createTime: '2024-01-10 09:15:00'
  },
  {
    id: 2,
    type: 'error',
    title: '缺勤异常',
    description: '1月8日有缺勤记录',
    createTime: '2024-01-09 08:30:00'
  }
])

const suggestions = ref([
  {
    id: 1,
    priority: 'high',
    title: '优化通勤路线',
    description: '建议提前10分钟出门，避免交通拥堵'
  },
  {
    id: 2,
    priority: 'medium',
    title: '加强时间管理',
    description: '合理规划工作时间，提高效率'
  },
  {
    id: 3,
    priority: 'low',
    title: '学习加班申请流程',
    description: '熟悉加班申请和审批流程'
  }
])

const timeRanges = ref([
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本季', value: 'quarter' },
  { label: '本年', value: 'year' },
  { label: '自定义', value: 'custom' }
])

const employeeTypes = ref([
  { label: '全部', value: 'all' },
  { label: '正式员工', value: 'formal' },
  { label: '实习员工', value: 'intern' },
  { label: '外包员工', value: 'contractor' }
])

const filterForm = reactive({
  department: '',
  employeeType: 'all'
})

// 计算属性
const getChartTypeText = () => {
  const types = {
    attendance: '出勤',
    punctuality: '守时',
    overtime: '加班'
  }
  return types[chartType.value] || '出勤'
}

// 页面生命周期
onMounted(() => {
  init()
})

onShow(() => {
  loadData()
})

// 初始化
const init = async () => {
  await loadData()
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    const userId = userStore.userId

    // 并行加载多个数据
    const [statisticsRes, alertsRes, suggestionsRes, scoreRes] = await Promise.all([
      attendanceApi.getAttendanceStatistics(userId, selectedRange.value),
      attendanceApi.getAbnormalAlerts(userId),
      attendanceApi.getImprovementSuggestions(userId),
      attendanceApi.getAttendanceScore(userId)
    ])

    if (statisticsRes.success) {
      Object.assign(statistics, statisticsRes.data)
    }

    if (alertsRes.success) {
      abnormalAlerts.value = alertsRes.data || []
    }

    if (suggestionsRes.success) {
      suggestions.value = suggestionsRes.data || []
    }

    if (scoreRes.success) {
      score.value = scoreRes.data.totalScore || 0
      Object.assign(scoreDetails, scoreRes.data.scores || {})
    }

  } catch (error) {
    console.error('加载数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 选择时间范围
const selectTimeRange = (range) => {
  selectedRange.value = range
  if (range !== 'custom') {
    loadData()
  }
}

// 显示日期选择器
const showDatePicker = () => {
  uni.showModal({
    title: '选择时间范围',
    content: '请选择开始和结束日期',
    success: (res) => {
      if (res.confirm) {
        // 这里可以调用日期选择器
        customRangeText.value = '2024-01-01 至 2024-01-31'
        loadData()
      }
    }
  })
}

// 切换图表类型
const switchChartType = () => {
  const types = ['attendance', 'punctuality', 'overtime']
  const currentIndex = types.indexOf(chartType.value)
  chartType.value = types[(currentIndex + 1) % types.length]
}

// 查看异常详情
const viewAlertDetail = (alert) => {
  uni.navigateTo({
    url: `/pages/attendance/alert-detail?alertId=${alert.id}`
  })
}

// 查看建议详情
const viewSuggestionDetail = (suggestion) => {
  uni.navigateTo({
    url: `/pages/attendance/suggestion-detail?suggestionId=${suggestion.id}`
  })
}

// 应用筛选
const applyFilter = () => {
  showFilterModal.value = false
  loadData()
}

// 重置筛选
const resetFilter = () => {
  filterForm.department = ''
  filterForm.employeeType = 'all'
  selectedRange.value = 'month'
  loadData()
}

// 格式化趋势
const formatTrend = (trend) => {
  const icons = {
    up: '↑',
    down: '↓',
    stable: '→'
  }
  return icons[trend] || '→'
}

// 获取异常图标
const getAlertIcon = (type) => {
  const icons = {
    warning: '⚠️',
    error: '❌',
    info: 'ℹ️'
  }
  return icons[type] || '⚠️'
}

// 获取建议图标
const getSuggestionIcon = (priority) => {
  const icons = {
    high: '🔴',
    medium: '🟡',
    low: '🟢'
  }
  return icons[priority] || '🟡'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 86400000) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.attendance-analytics-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.status-bar {
  background: #fff;
}

.nav-bar {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 32rpx;
  }

  .back-btn {
    font-size: 48rpx;
    color: rgba(0, 0, 0, 0.85);
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }

  .filter-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.time-range-selector {
  padding: 24rpx 32rpx;

  .range-tabs {
    display: flex;
    gap: 16rpx;
    margin-bottom: 16rpx;

    .range-tab {
      flex: 1;
      text-align: center;
      padding: 16rpx 0;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 24rpx;
      color: rgba(255, 255, 255, 0.7);
      font-size: 26rpx;
      border: 1px solid rgba(255, 255, 255, 0.2);

      &.active {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
        border-color: rgba(255, 255, 255, 0.4);
      }
    }
  }

  .custom-range {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 24rpx;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 16rpx;
    border: 1px solid rgba(255, 255, 255, 0.2);

    .custom-text {
      color: #fff;
      font-size: 26rpx;
    }

    .custom-arrow {
      color: #fff;
      font-size: 32rpx;
    }
  }
}

.statistics-overview {
  padding: 0 32rpx 24rpx;

  .overview-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16rpx;

    .stat-card {
      background: #fff;
      border-radius: 16rpx;
      padding: 24rpx;
      text-align: center;
      box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);

      .stat-value {
        font-size: 48rpx;
        font-weight: bold;
        color: #1890ff;
        display: block;
        margin-bottom: 8rpx;
      }

      .stat-label {
        font-size: 24rpx;
        color: rgba(0, 0, 0, 0.45);
        display: block;
        margin-bottom: 8rpx;
      }

      .stat-trend {
        font-size: 24rpx;
        display: block;

        &.up { color: #52c41a; }
        &.down { color: #ff4d4f; }
        &.stable { color: rgba(0, 0, 0, 0.45); }
      }
    }
  }
}

.chart-section,
.abnormal-alerts,
.score-section,
.suggestions-section {
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    display: block;
    margin-bottom: 24rpx;
  }

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .chart-title {
      font-size: 32rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }

    .chart-type {
      font-size: 28rpx;
      color: #1890ff;
      padding: 8rpx 16rpx;
      border: 1px solid #e8e8e8;
      border-radius: 8rpx;
    }
  }

  .chart-container {
    height: 400rpx;
    background: #fafafa;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .chart-placeholder {
      text-align: center;

      .chart-icon {
        font-size: 64rpx;
        margin-bottom: 16rpx;
        display: block;
      }

      .chart-text {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }
}

.abnormal-alerts {
  .alerts-list {
    .alert-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .alert-icon {
        font-size: 32rpx;
        margin-right: 24rpx;
      }

      .alert-content {
        flex: 1;

        .alert-title {
          font-size: 28rpx;
          font-weight: 600;
          color: rgba(0, 0, 0, 0.85);
          display: block;
          margin-bottom: 8rpx;
        }

        .alert-desc {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
          margin-bottom: 8rpx;
          display: block;
        }

        .alert-time {
          font-size: 22rpx;
          color: rgba(0, 0, 0, 0.25);
        }
      }

      .alert-action {
        .action-text {
          font-size: 26rpx;
          color: #1890ff;
        }
      }
    }
  }
}

.score-section {
  .score-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;

    .score-help {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      background: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }

  .score-card {
    .score-circle {
      position: relative;
      width: 200rpx;
      height: 200rpx;
      border-radius: 50%;
      margin: 0 auto 32rpx;
      background: #f0f0f0;

      .score-progress {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border-radius: 50%;
        background: conic-gradient(#1890ff 0deg, #1890ff calc(3.6deg * 92), #f0f0f0 0deg);
        transition: transform 0.3s ease;
      }

      .score-center {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        text-align: center;

        .score-value {
          font-size: 48rpx;
          font-weight: bold;
          color: #1890ff;
          display: block;
        }

        .score-label {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }
    }

    .score-details {
      display: flex;
      justify-content: space-around;

      .score-item {
        text-align: center;

        .item-label {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
          display: block;
          margin-bottom: 8rpx;
        }

        .item-value {
          font-size: 32rpx;
          font-weight: 600;
          color: rgba(0, 0, 0, 0.85);
        }
      }
    }
  }
}

.suggestions-section {
  .suggestions-list {
    .suggestion-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .suggestion-icon {
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20rpx;
        margin-right: 24rpx;

        &.high {
          background: #fff1f0;
          color: #ff4d4f;
        }

        &.medium {
          background: #fff7e6;
          color: #fa8c16;
        }

        &.low {
          background: #f6ffed;
          color: #52c41a;
        }
      }

      .suggestion-content {
        flex: 1;

        .suggestion-title {
          font-size: 28rpx;
          font-weight: 600;
          color: rgba(0, 0, 0, 0.85);
          display: block;
          margin-bottom: 8rpx;
        }

        .suggestion-desc {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .suggestion-arrow {
        font-size: 32rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }
}

.filter-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
  z-index: 300;

  .filter-content {
    width: 100%;
    max-height: 80vh;
    background: #fff;
    border-radius: 32rpx 32rpx 0 0;
    padding: 48rpx 32rpx;

    .filter-title {
      font-size: 36rpx;
      font-weight: 600;
      display: block;
      margin-bottom: 32rpx;
      text-align: center;
    }

    .filter-section {
      margin-bottom: 32rpx;

      .filter-label {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        display: block;
        margin-bottom: 16rpx;
      }

      .filter-input {
        width: 100%;
        height: 88rpx;
        border: 1px solid #d9d9d9;
        border-radius: 8rpx;
        padding: 0 24rpx;
        font-size: 28rpx;
      }

      .filter-options {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;

        .filter-option {
          padding: 12rpx 32rpx;
          background: #f0f0f0;
          border-radius: 8rpx;
          font-size: 26rpx;
          color: rgba(0, 0, 0, 0.65);

          &.selected {
            background: #1890ff;
            color: #fff;
          }
        }
      }
    }

    .filter-buttons {
      display: flex;
      gap: 16rpx;
      margin-top: 48rpx;

      .filter-btn {
        flex: 1;
        height: 88rpx;
        border: none;
        border-radius: 8rpx;
        font-size: 32rpx;

        &.reset {
          background: #f0f0f0;
          color: rgba(0, 0, 0, 0.65);
        }

        &.confirm {
          background: #1890ff;
          color: #fff;
        }
      }
    }
  }
}
</style>