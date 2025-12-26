<template>
  <view class="statistics-container">
    <!-- 月份选择器 -->
    <view class="month-selector">
      <view class="selector-content">
        <text class="arrow" @click="changeMonth(-1)">‹</text>
        <text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
        <text class="arrow" @click="changeMonth(1)">›</text>
      </view>
    </view>

    <!-- 统计概览卡片 -->
    <view class="overview-cards">
      <view class="overview-item">
        <text class="overview-value">{{ overview.workDays }}</text>
        <text class="overview-label">出勤天数</text>
      </view>
      <view class="overview-item">
        <text class="overview-value">{{ overview.attendanceRate }}%</text>
        <text class="overview-label">出勤率</text>
      </view>
      <view class="overview-item">
        <text class="overview-value">{{ overview.lateCount }}</text>
        <text class="overview-label">迟到次数</text>
      </view>
      <view class="overview-item">
        <text class="overview-value">{{ overview.earlyCount }}</text>
        <text class="overview-label">早退次数</text>
      </view>
    </view>

    <!-- 图表区域 -->
    <view class="charts-section">
      <!-- 出勤趋势图 -->
      <view class="chart-card">
        <view class="chart-title">
          <text class="title-text">出勤趋势</text>
        </view>
        <qiun-ucharts
          type="line"
          :opts="lineChartOpts"
          :chartData="lineChartData"
          :canvas2d="true"
        />
      </view>

      <!-- 考勤分布饼图 -->
      <view class="chart-card">
        <view class="chart-title">
          <text class="title-text">考勤分布</text>
        </view>
        <qiun-ucharts
          type="pie"
          :opts="pieChartOpts"
          :chartData="pieChartData"
          :canvas2d="true"
        />
      </view>
    </view>

    <!-- 详细统计列表 -->
    <view class="detail-list">
      <view class="list-title">每日考勤详情</view>

      <view class="day-item" v-for="day in dailyRecords" :key="day.date">
        <view class="day-header">
          <text class="day-date">{{ formatDate(day.date) }}</text>
          <text class="day-weekday">{{ getWeekday(day.date) }}</text>
        </view>

        <view class="day-punches">
          <view class="punch-info">
            <text class="punch-label">上班:</text>
            <text class="punch-time" :class="{ 'abnormal': day.morningStatus !== 'NORMAL' }">
              {{ day.morningTime || '--:--' }}
            </text>
            <text class="punch-status" :class="getStatusClass(day.morningStatus)">
              {{ getStatusText(day.morningStatus) }}
            </text>
          </view>

          <view class="punch-info">
            <text class="punch-label">下班:</text>
            <text class="punch-time" :class="{ 'abnormal': day.eveningStatus !== 'NORMAL' }">
              {{ day.eveningTime || '--:--' }}
            </text>
            <text class="punch-status" :class="getStatusClass(day.eveningStatus)">
              {{ getStatusText(day.eveningStatus) }}
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

const currentYear = ref(dayjs().format('YYYY'))
const currentMonth = ref(dayjs().format('M'))

const overview = reactive({
  workDays: 0,
  attendanceRate: 0,
  lateCount: 0,
  earlyCount: 0
})

const dailyRecords = ref([])
const employeeId = ref(1001)

// ECharts 配置
const lineChartOpts = {
  color: ['#667eea', '#764ba2'],
  padding: [15, 15, 0, 5],
  enableScroll: false,
  legend: {},
  xAxis: {
    disableGrid: true
  },
  yAxis: {
    data: [{ min: 0 }]
  },
  extra: {
    line: {
      type: 'curve',
      width: 2,
      activeType: 'hollow'
    }
  }
}

const lineChartData = {
  categories: [],
  series: [
    {
      name: '出勤天数',
      data: []
    }
  ]
}

const pieChartOpts = {
  color: ['#22c55e', '#ef4444', '#f59e0b', '#3b82f6'],
  padding: [5, 5, 0, 5],
  enableScroll: false,
  legend: {
    position: 'right',
    itemName: {
      fontSize: 12
    }
  },
  extra: {
    pie: {
      activeOpacity: 0.5,
      activeRadius: 10,
      offsetAngle: 0,
      labelWidth: 15,
      ringWidth: 30,
      border: true,
      borderWidth: 2,
      borderColor: '#FFFFFF'
    }
  }
}

const pieChartData = {
  series: [
    { name: '正常', data: 0 },
    { name: '迟到', data: 0 },
    { name: '早退', data: 0 },
    { name: '缺卡', data: 0 }
  ]
}

onMounted(() => {
  loadStatistics()
})

const changeMonth = (delta) => {
  let month = parseInt(currentMonth.value) + delta
  let year = parseInt(currentYear.value)

  if (month > 12) {
    month = 1
    year++
  } else if (month < 1) {
    month = 12
    year--
  }

  currentMonth.value = month.toString()
  currentYear.value = year.toString()
  loadStatistics()
}

const loadStatistics = async () => {
  try {
    const startDate = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .startOf('month')
      .format('YYYY-MM-DD')

    const endDate = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .endOf('month')
      .format('YYYY-MM-DD')

    const res = await attendanceApi.statisticsApi.getPersonalStatistics({
      employeeId: employeeId.value,
      startDate,
      endDate
    })

    if (res.success && res.data) {
      const stats = res.data

      // 更新概览数据
      overview.workDays = stats.workDays || 0
      overview.attendanceRate = stats.attendanceRate || 0
      overview.lateCount = stats.lateCount || 0
      overview.earlyCount = stats.earlyCount || 0

      // 更新图表数据
      lineChartData.categories = stats.dailyStats?.map(d => d.date) || []
      lineChartData.series[0].data = stats.dailyStats?.map(d => d.workHours) || []

      pieChartData.series[0].data = stats.normalCount || 0
      pieChartData.series[1].data = stats.lateCount || 0
      pieChartData.series[2].data = stats.earlyCount || 0
      pieChartData.series[3].data = stats.absentCount || 0

      // 更新每日记录
      dailyRecords.value = stats.dailyDetails || []
    }
  } catch (error) {
    console.error('[考勤统计] 加载失败:', error)
  }
}

const formatDate = (dateStr) => dayjs(dateStr).format('MM月DD日')
const getWeekday = (dateStr) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[dayjs(dateStr).day()]
}

const getStatusText = (status) => {
  const map = { 'NORMAL': '正常', 'LATE': '迟到', 'EARLY': '早退', 'ABSENT': '缺卡' }
  return map[status] || '--'
}

const getStatusClass = (status) => {
  const map = { 'NORMAL': 'status-normal', 'LATE': 'status-late', 'EARLY': 'status-early', 'ABSENT': 'status-absent' }
  return map[status] || ''
}
</script>

<style lang="scss" scoped>
.statistics-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.month-selector {
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.selector-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.arrow {
  font-size: 40rpx;
  color: #667eea;
  padding: 0 30rpx;
}

.month-text {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin: 0 30rpx;
}

.overview-cards {
  display: flex;
  justify-content: space-around;
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.overview-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8rpx;
}

.overview-label {
  font-size: 22rpx;
  color: #999;
}

.charts-section {
  padding: 30rpx;
}

.chart-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.chart-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.detail-list {
  background: white;
  margin: 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}

.list-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.day-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.day-date {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.day-weekday {
  font-size: 22rpx;
  color: #999;
}

.day-punches {
  display: flex;
  justify-content: space-around;
}

.punch-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.punch-label {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.punch-time {
  font-size: 24rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 5rpx;

  &.abnormal {
    color: #ef4444;
  }
}

.punch-status {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}

.status-normal { background-color: #f0fdf4; color: #22c55e; }
.status-late { background-color: #fef2f2; color: #ef4444; }
.status-early { background-color: #fffbeb; color: #f59e0b; }
.status-absent { background-color: #fef2f2; color: #ef4444; }
</style>
