<template>
  <view class="summary-container">
    <!-- 年度选择器 -->
    <view class="year-selector">
      <view class="selector-content">
        <text class="arrow" @click="changeYear(-1)">‹</text>
        <text class="year-text">{{ currentYear }}年度汇总</text>
        <text class="arrow" @click="changeYear(1)">›</text>
      </view>
    </view>

    <!-- 年度概览卡片 -->
    <view class="year-overview">
      <view class="overview-card">
        <text class="card-icon">📊</text>
        <text class="card-value">{{ yearData.totalWorkDays || 0 }}</text>
        <text class="card-label">总出勤天数</text>
      </view>

      <view class="overview-card">
        <text class="card-icon">✅</text>
        <text class="card-value">{{ yearData.attendanceRate || 0 }}%</text>
        <text class="card-label">年度出勤率</text>
      </view>

      <view class="overview-card">
        <text class="card-icon">⏰</text>
        <text class="card-value">{{ yearData.overtimeHours || 0 }}</text>
        <text class="card-label">加班时长(小时)</text>
      </view>

      <view class="overview-card">
        <text class="card-icon">🏖️</text>
        <text class="card-value">{{ yearData.leaveDays || 0 }}</text>
        <text class="card-label">请假天数</text>
      </view>
    </view>

    <!-- 月度统计图表 -->
    <view class="monthly-chart">
      <view class="chart-title">月度出勤趋势</view>
      <qiun-ucharts
        type="column"
        :opts="columnChartOpts"
        :chartData="columnChartData"
        :canvas2d="true"
      />
    </view>

    <!-- 月度详情列表 -->
    <view class="monthly-list">
      <view class="list-title">月度明细</view>

      <view class="month-item" v-for="month in monthlyData" :key="month.month">
        <view class="month-header">
          <text class="month-name">{{ month.month }}月</text>
          <text class="month-rate">出勤率: {{ month.attendanceRate }}%</text>
        </view>

        <view class="month-stats">
          <view class="stat-row">
            <text class="stat-label">出勤:</text>
            <text class="stat-value">{{ month.workDays }}天</text>
          </view>
          <view class="stat-row">
            <text class="stat-label">迟到:</text>
            <text class="stat-value late">{{ month.lateCount }}次</text>
          </view>
          <view class="stat-row">
            <text class="stat-label">早退:</text>
            <text class="stat-value early">{{ month.earlyCount }}次</text>
          </view>
          <view class="stat-row">
            <text class="stat-label">请假:</text>
            <text class="stat-value">{{ month.leaveDays }}天</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 排行榜 -->
    <view class="leaderboard" v-if="leaderboardData.length > 0">
      <view class="leaderboard-title">出勤排行榜</view>

      <view class="leaderboard-list">
        <view class="leaderboard-item" v-for="(item, index) in leaderboardData" :key="item.userId">
          <text class="rank rank-1" v-if="index === 0">🥇</text>
          <text class="rank rank-2" v-else-if="index === 1">🥈</text>
          <text class="rank rank-3" v-else-if="index === 2">🥉</text>
          <text class="rank" v-else>{{ index + 1 }}</text>

          <image class="avatar" :src="item.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
          <text class="name">{{ item.name }}</text>
          <text class="score">{{ item.attendanceRate }}%</text>
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

const yearData = reactive({
  totalWorkDays: 0,
  attendanceRate: 0,
  overtimeHours: 0,
  leaveDays: 0
})

const monthlyData = ref([])
const leaderboardData = ref([])
const employeeId = ref(1001)

const columnChartOpts = {
  color: ['#667eea'],
  padding: [15, 15, 0, 5],
  enableScroll: false,
  xAxis: {
    disableGrid: true
  },
  yAxis: {
    data: [{ min: 0 }]
  },
  extra: {
    column: {
      type: 'group',
      width: 20,
      activeType: 'hollow',
      linearType: 'custom',
      stop: true
    }
  }
}

const columnChartData = {
  categories: [],
  series: [
    {
      name: '出勤天数',
      data: []
    }
  ]
}

onMounted(() => {
  loadSummary()
})

const changeYear = (delta) => {
  const year = parseInt(currentYear.value) + delta
  currentYear.value = year.toString()
  loadSummary()
}

const loadSummary = async () => {
  try {
    const startDate = dayjs()
      .year(parseInt(currentYear.value))
      .startOf('year')
      .format('YYYY-MM-DD')

    const endDate = dayjs()
      .year(parseInt(currentYear.value))
      .endOf('year')
      .format('YYYY-MM-DD')

    const res = await attendanceApi.statisticsApi.getMonthlySummary({
      employeeId: employeeId.value,
      year: currentYear.value
    })

    if (res.success && res.data) {
      const data = res.data

      // 更新年度数据
      yearData.totalWorkDays = data.totalWorkDays || 0
      yearData.attendanceRate = data.attendanceRate || 0
      yearData.overtimeHours = data.overtimeHours || 0
      yearData.leaveDays = data.leaveDays || 0

      // 更新月度数据
      monthlyData.value = data.monthlyData || []

      // 更新图表数据
      columnChartData.categories = monthlyData.value.map(m => m.month + '月')
      columnChartData.series[0].data = monthlyData.value.map(m => m.workDays)

      // 更新排行榜
      leaderboardData.value = data.leaderboard || []
    }
  } catch (error) {
    console.error('[考勤汇总] 加载失败:', error)
  }
}
</script>

<style lang="scss" scoped>
.summary-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.year-selector {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  color: white;
  padding: 0 30rpx;
}

.year-text {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
  margin: 0 30rpx;
}

.year-overview {
  display: flex;
  justify-content: space-around;
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.overview-card {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-icon {
  font-size: 36rpx;
  margin-bottom: 10rpx;
}

.card-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8rpx;
}

.card-label {
  font-size: 22rpx;
  color: #999;
}

.monthly-chart {
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.chart-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.monthly-list {
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

.month-item {
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.month-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.month-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.month-rate {
  font-size: 22rpx;
  color: #667eea;
}

.month-stats {
  display: flex;
  justify-content: space-around;
}

.stat-row {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 22rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.stat-value {
  font-size: 24rpx;
  color: #333;
  font-weight: bold;

  &.late, &.early {
    color: #ef4444;
  }
}

.leaderboard {
  background: white;
  margin: 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}

.leaderboard-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.leaderboard-list {
  .leaderboard-item {
    display: flex;
    align-items: center;
    padding: 15rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }
}

.rank {
  width: 50rpx;
  height: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: bold;
  color: #999;
  margin-right: 20rpx;
}

.rank-1, .rank-2, .rank-3 {
  font-size: 32rpx;
}

.avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.name {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.score {
  font-size: 26rpx;
  font-weight: bold;
  color: #667eea;
}
</style>
