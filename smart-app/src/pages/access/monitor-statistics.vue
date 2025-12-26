<template>
  <view class="statistics-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">监控统计</text>
        </view>
        <view class="navbar-right">
          <view class="export-btn" @tap="exportStatistics">
            <uni-icons type="download" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 时间筛选 -->
      <view class="time-filter">
        <view
          class="time-item"
          :class="{ active: selectedTimeRange === 'today' }"
          @tap="selectTimeRange('today')"
        >
          今日
        </view>
        <view
          class="time-item"
          :class="{ active: selectedTimeRange === 'week' }"
          @tap="selectTimeRange('week')"
        >
          本周
        </view>
        <view
          class="time-item"
          :class="{ active: selectedTimeRange === 'month' }"
          @tap="selectTimeRange('month')"
        >
          本月
        </view>
        <view
          class="time-item"
          :class="{ active: selectedTimeRange === 'custom' }"
          @tap="selectTimeRange('custom')"
        >
          自定义
        </view>
      </view>

      <!-- 自定义时间选择 -->
      <view class="custom-time" v-if="selectedTimeRange === 'custom'">
        <picker mode="date" :value="startDate" @change="onStartDateChange">
          <view class="date-picker">
            <text class="date-text">{{ startDate || '开始日期' }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
        <text class="date-separator">至</text>
        <picker mode="date" :value="endDate" @change="onEndDateChange">
          <view class="date-picker">
            <text class="date-text">{{ endDate || '结束日期' }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
        <view class="query-btn" @tap="queryCustomData">查询</view>
      </view>

      <!-- 实时统计概览 -->
      <view class="overview-cards">
        <view class="overview-card">
          <view class="card-icon pass">
            <uni-icons type="checkmarkempty" size="24" color="#fff"></uni-icons>
          </view>
          <view class="card-info">
            <text class="card-value">{{ todayStats.totalPass }}</text>
            <text class="card-label">今日通行</text>
          </view>
        </view>

        <view class="overview-card">
          <view class="card-icon person">
            <uni-icons type="person" size="24" color="#fff"></uni-icons>
          </view>
          <view class="card-info">
            <text class="card-value">{{ todayStats.uniquePerson }}</text>
            <text class="card-label">通行人数</text>
          </view>
        </view>

        <view class="overview-card">
          <view class="card-icon alarm">
            <uni-icons type="notification" size="24" color="#fff"></uni-icons>
          </view>
          <view class="card-info">
            <text class="card-value">{{ todayStats.alarmCount }}</text>
            <text class="card-label">告警次数</text>
          </view>
        </view>

        <view class="overview-card">
          <view class="card-icon device">
            <uni-icons type="settings" size="24" color="#fff"></uni-icons>
          </view>
          <view class="card-info">
            <text class="card-value">{{ todayStats.deviceOnline }}%</text>
            <text class="card-label">设备在线率</text>
          </view>
        </view>
      </view>

      <!-- 通行趋势图表 -->
      <view class="chart-card">
        <view class="card-header">
          <text class="card-title">通行趋势</text>
          <view class="chart-legend">
            <view class="legend-item">
              <view class="legend-dot in"></view>
              <text class="legend-text">进入</text>
            </view>
            <view class="legend-item">
              <view class="legend-dot out"></view>
              <text class="legend-text">离开</text>
            </view>
          </view>
        </view>

        <view class="chart-container">
          <view class="chart-bars">
            <view class="chart-bar" v-for="(hour, index) in passTrend" :key="index">
              <view class="bar-label">{{ hour.hour }}:00</view>
              <view class="bar-area">
                <view
                  class="bar-in"
                  :style="{ height: (hour.inCount / maxPassCount * 100) + '%' }"
                ></view>
                <view
                  class="bar-out"
                  :style="{ height: (hour.outCount / maxPassCount * 100) + '%' }"
                ></view>
              </view>
              <view class="bar-value">{{ hour.inCount + hour.outCount }}</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 区域通行统计 -->
      <view class="area-stats">
        <view class="card-header">
          <text class="card-title">区域通行统计</text>
          <view class="view-all" @tap="viewAllAreas">
            <text class="view-text">查看全部</text>
            <uni-icons type="arrowright" size="14" color="#667eea"></uni-icons>
          </view>
        </view>

        <view class="area-list">
          <view class="area-item" v-for="area in areaStatistics" :key="area.areaId">
            <view class="area-header">
              <text class="area-name">{{ area.areaName }}</text>
              <text class="area-count">{{ area.passCount }}次</text>
            </view>

            <view class="area-progress">
              <view class="progress-bar">
                <view
                  class="progress-fill"
                  :style="{ width: (area.passCount / maxAreaPass * 100) + '%' }"
                ></view>
              </view>
              <text class="progress-percent">{{ ((area.passCount / totalPassCount) * 100).toFixed(1) }}%</text>
            </view>

            <view class="area-detail">
              <view class="detail-item">
                <text class="detail-label">进入:</text>
                <text class="detail-value">{{ area.inCount }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-label">离开:</text>
                <text class="detail-value">{{ area.outCount }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-label">当前:</text>
                <text class="detail-value highlight">{{ area.currentCount }}人</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 告警统计 -->
      <view class="alarm-stats">
        <view class="card-header">
          <text class="card-title">告警统计</text>
        </view>

        <view class="alarm-chart">
          <view class="alarm-types">
            <view
              class="alarm-type-item"
              v-for="alarm in alarmStatistics"
              :key="alarm.type"
            >
              <view class="type-header">
                <view class="type-dot" :style="{ backgroundColor: alarm.color }"></view>
                <text class="type-name">{{ alarm.name }}</text>
              </view>
              <view class="type-count">{{ alarm.count }}</view>
              <view class="type-percent">{{ alarm.percent }}%</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 时段分析 -->
      <view class="time-analysis">
        <view class="card-header">
          <text class="card-title">通行时段分析</text>
        </view>

        <view class="time-periods">
          <view
            class="period-item"
            v-for="period in timePeriods"
            :key="period.period"
          >
            <view class="period-name">{{ period.period }}</view>
            <view class="period-time">{{ period.timeRange }}</view>
            <view class="period-stats">
              <view class="stat">
                <text class="stat-label">通行:</text>
                <text class="stat-value">{{ period.passCount }}</text>
              </view>
              <view class="stat">
                <text class="stat-label">占比:</text>
                <text class="stat-value">{{ period.percent }}%</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 设备运行统计 -->
      <view class="device-stats">
        <view class="card-header">
          <text class="card-title">设备运行统计</text>
        </view>

        <view class="device-grid">
          <view class="device-grid-item" v-for="device in deviceRunStats" :key="device.deviceId">
            <view class="device-name">{{ device.deviceName }}</view>
            <view class="device-metrics">
              <view class="metric">
                <text class="metric-label">通行:</text>
                <text class="metric-value">{{ device.passCount }}</text>
              </view>
              <view class="metric">
                <text class="metric-label">在线:</text>
                <text class="metric-value">{{ device.uptime }}%</text>
              </view>
            </view>
            <view class="device-status" :class="device.status">
              <text>{{ getStatusText(device.status) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 对比分析 -->
      <view class="comparison">
        <view class="card-header">
          <text class="card-title">同比环比分析</text>
        </view>

        <view class="comparison-cards">
          <view class="comparison-card">
            <view class="comparison-title">环比昨日</view>
            <view class="comparison-value" :class="{ up: comparisonData.yoy >= 0, down: comparisonData.yoy < 0 }">
              <uni-icons :type="comparisonData.yoy >= 0 ? 'up' : 'down'" size="16"></uni-icons>
              <text>{{ Math.abs(comparisonData.yoy) }}%</text>
            </view>
            <view class="comparison-desc">
              <text v-if="comparisonData.yoy > 0">通行量增加</text>
              <text v-else-if="comparisonData.yoy < 0">通行量减少</text>
              <text v-else>与昨日持平</text>
            </view>
          </view>

          <view class="comparison-card">
            <view class="comparison-title">环比上周</view>
            <view class="comparison-value" :class="{ up: comparisonData.wow >= 0, down: comparisonData.wow < 0 }">
              <uni-icons :type="comparisonData.wow >= 0 ? 'up' : 'down'" size="16"></uni-icons>
              <text>{{ Math.abs(comparisonData.wow) }}%</text>
            </view>
            <view class="comparison-desc">
              <text v-if="comparisonData.wow > 0">通行量增加</text>
              <text v-else-if="comparisonData.wow < 0">通行量减少</text>
              <text v-else>与上周持平</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 时间筛选
const selectedTimeRange = ref('today')
const startDate = ref('')
const endDate = ref('')

// 今日统计
const todayStats = reactive({
  totalPass: 1256,
  uniquePerson: 342,
  alarmCount: 23,
  deviceOnline: 95
})

// 通行趋势
const passTrend = ref([])
const maxPassCount = ref(0)

// 区域统计
const areaStatistics = ref([])

// 告警统计
const alarmStatistics = ref([])

// 时段分析
const timePeriods = ref([])

// 设备运行统计
const deviceRunStats = ref([])

// 对比数据
const comparisonData = reactive({
  yoy: 12.5,
  wow: -5.3
})

// 计算最大区域通行数
const maxAreaPass = computed(() => {
  return Math.max(...areaStatistics.value.map(a => a.passCount))
})

// 计算总通行数
const totalPassCount = computed(() => {
  return areaStatistics.value.reduce((sum, a) => sum + a.passCount, 0)
})

// 加载页面
const loadPage = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  // 加载数据
  loadPassTrend()
  loadAreaStatistics()
  loadAlarmStatistics()
  loadTimePeriods()
  loadDeviceRunStats()
}

// 选择时间范围
const selectTimeRange = (range) => {
  selectedTimeRange.value = range

  // 重新加载数据
  loadStatisticsData()
}

// 自定义时间
const onStartDateChange = (e) => {
  startDate.value = e.detail.value
}

const onEndDateChange = (e) => {
  endDate.value = e.detail.value
}

const queryCustomData = () => {
  if (!startDate.value || !endDate.value) {
    uni.showToast({
      title: '请选择日期',
      icon: 'none'
    })
    return
  }

  loadStatisticsData()
}

// 加载统计数据
const loadStatisticsData = () => {
  // 根据时间范围重新加载所有统计数据
  loadPassTrend()
  loadAreaStatistics()
  loadAlarmStatistics()
}

// 加载通行趋势
const loadPassTrend = () => {
  const trend = []
  let maxCount = 0

  for (let i = 6; i < 20; i++) {
    const inCount = Math.floor(Math.random() * 80) + 20
    const outCount = Math.floor(Math.random() * 80) + 20
    const total = inCount + outCount

    trend.push({
      hour: i,
      inCount: inCount,
      outCount: outCount
    })

    maxCount = Math.max(maxCount, total)
  }

  passTrend.value = trend
  maxPassCount.value = maxCount
}

// 加载区域统计
const loadAreaStatistics = () => {
  areaStatistics.value = [
    {
      areaId: '1',
      areaName: 'A栋1楼大厅',
      passCount: 456,
      inCount: 238,
      outCount: 218,
      currentCount: 20
    },
    {
      areaId: '2',
      areaName: 'A栋2楼办公区',
      passCount: 378,
      inCount: 195,
      outCount: 183,
      currentCount: 12
    },
    {
      areaId: '3',
      areaName: 'B栋会议室',
      passCount: 234,
      inCount: 120,
      outCount: 114,
      currentCount: 6
    },
    {
      areaId: '4',
      areaName: 'C栋餐厅',
      passCount: 188,
      inCount: 98,
      outCount: 90,
      currentCount: 98
    }
  ]
}

// 加载告警统计
const loadAlarmStatistics = () => {
  const total = 23
  alarmStatistics.value = [
    { type: 'intrusion', name: '非法闯入', count: 5, percent: 21.7, color: '#ff6b6b' },
    { type: 'lingering', name: '长时间逗留', count: 8, percent: 34.8, color: '#ffa940' },
    { type: 'offline', name: '设备离线', count: 4, percent: 17.4, color: '#ffec3d' },
    { type: 'damage', name: '暴力破坏', count: 2, percent: 8.7, color: '#ff4d4f' },
    { type: 'tailgating', name: '尾随检测', count: 4, percent: 17.4, color: '#722ed1' }
  ]
}

// 加载时段分析
const loadTimePeriods = () => {
  const totalPass = todayStats.totalPass
  timePeriods.value = [
    {
      period: '早高峰',
      timeRange: '07:00-09:00',
      passCount: 425,
      percent: ((425 / totalPass) * 100).toFixed(1)
    },
    {
      period: '工作时间',
      timeRange: '09:00-12:00',
      passCount: 312,
      percent: ((312 / totalPass) * 100).toFixed(1)
    },
    {
      period: '午休',
      timeRange: '12:00-14:00',
      passCount: 267,
      percent: ((267 / totalPass) * 100).toFixed(1)
    },
    {
      period: '下午',
      timeRange: '14:00-18:00',
      passCount: 186,
      percent: ((186 / totalPass) * 100).toFixed(1)
    },
    {
      period: '晚高峰',
      timeRange: '18:00-20:00',
      passCount: 66,
      percent: ((66 / totalPass) * 100).toFixed(1)
    }
  ]
}

// 加载设备运行统计
const loadDeviceRunStats = () => {
  deviceRunStats.value = [
    {
      deviceId: 'D001',
      deviceName: '主入口门禁',
      passCount: 456,
      uptime: 98,
      status: 'normal'
    },
    {
      deviceId: 'D002',
      deviceName: '侧门门禁',
      passCount: 234,
      uptime: 95,
      status: 'normal'
    },
    {
      deviceId: 'D003',
      deviceName: '2楼门禁',
      passCount: 378,
      uptime: 92,
      status: 'warning'
    },
    {
      deviceId: 'D004',
      deviceName: '会议室门禁',
      passCount: 123,
      uptime: 88,
      status: 'normal'
    }
  ]
}

// 查看全部区域
const viewAllAreas = () => {
  uni.navigateTo({
    url: '/pages/access/area-statistics'
  })
}

// 导出统计
const exportStatistics = () => {
  uni.showActionSheet({
    itemList: ['导出Excel', '导出PDF', '发送到邮箱'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          exportToExcel()
          break
        case 1:
          exportToPDF()
          break
        case 2:
          sendToEmail()
          break
      }
    }
  })
}

// 导出到Excel
const exportToExcel = () => {
  uni.showLoading({
    title: '导出中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  }, 1500)
}

// 导出到PDF
const exportToPDF = () => {
  uni.showLoading({
    title: '导出中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  }, 1500)
}

// 发送到邮箱
const sendToEmail = () => {
  uni.showModal({
    title: '发送到邮箱',
    content: '请输入邮箱地址',
    editable: true,
    placeholderText: '请输入邮箱地址',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '发送中...'
        })

        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '发送成功',
            icon: 'success'
          })
        }, 1500)
      }
    }
  })
}

// 工具方法
const getStatusText = (status) => {
  const texts = {
    normal: '正常',
    warning: '警告',
    error: '故障'
  }
  return texts[status] || '未知'
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面加载时执行
loadPage()
</script>

<style lang="scss" scoped>
.statistics-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e8ecf1 100%);
}

.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1000;

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 30rpx;

    .navbar-left {
      display: flex;
      align-items: center;
      gap: 10rpx;

      .back-text {
        font-size: 28rpx;
        color: #fff;
      }
    }

    .navbar-center {
      .navbar-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .navbar-right {
      .export-btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.page-content {
  padding-top: calc(44px + var(--status-bar-height));
  padding-bottom: 40rpx;
}

.time-filter {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;

  .time-item {
    flex: 1;
    padding: 20rpx 0;
    text-align: center;
    background: #fff;
    border-radius: 16rpx;
    font-size: 28rpx;
    color: #666;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      font-weight: 600;
    }
  }
}

.custom-time {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 0 30rpx 30rpx;

  .date-picker {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 30rpx;
    background: #fff;
    border-radius: 16rpx;

    .date-text {
      font-size: 26rpx;
      color: #333;
    }
  }

  .date-separator {
    font-size: 26rpx;
    color: #999;
  }

  .query-btn {
    padding: 20rpx 40rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16rpx;
    font-size: 26rpx;
    color: #fff;
    text-align: center;
  }
}

.overview-cards {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 30rpx;
  flex-wrap: wrap;

  .overview-card {
    flex: 1;
    min-width: 160rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    display: flex;
    align-items: center;
    gap: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .card-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.pass {
        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
      }

      &.person {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.alarm {
        background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
      }

      &.device {
        background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
      }
    }

    .card-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .card-value {
        font-size: 40rpx;
        font-weight: 700;
        color: #333;
      }

      .card-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.chart-card,
.area-stats,
.alarm-stats,
.time-analysis,
.device-stats,
.comparison {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .card-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .chart-legend {
      display: flex;
      gap: 20rpx;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .legend-dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;

          &.in {
            background: #52c41a;
          }

          &.out {
            background: #ffa940;
          }
        }

        .legend-text {
          font-size: 24rpx;
          color: #666;
        }
      }
    }

    .view-all {
      display: flex;
      align-items: center;
      gap: 4rpx;

      .view-text {
        font-size: 26rpx;
        color: #667eea;
      }
    }
  }
}

.chart-container {
  .chart-bars {
    display: flex;
    align-items: flex-end;
    gap: 12rpx;
    height: 300rpx;

    .chart-bar {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .bar-label {
        font-size: 20rpx;
        color: #999;
        margin-bottom: 12rpx;
      }

      .bar-area {
        flex: 1;
        width: 100%;
        display: flex;
        align-items: flex-end;
        justify-content: center;
        gap: 4rpx;

        .bar-in,
        .bar-out {
          width: 12rpx;
          min-height: 20rpx;
          border-radius: 6rpx 6rpx 0 0;
        }

        .bar-in {
          background: linear-gradient(180deg, #52c41a 0%, #73d13d 100%);
        }

        .bar-out {
          background: linear-gradient(180deg, #ffa940 0%, #ffc069 100%);
        }
      }

      .bar-value {
        margin-top: 12rpx;
        font-size: 20rpx;
        color: #666;
      }
    }
  }
}

.area-list {
  .area-item {
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .area-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .area-name {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }

      .area-count {
        font-size: 26rpx;
        color: #667eea;
        font-weight: 600;
      }
    }

    .area-progress {
      display: flex;
      align-items: center;
      gap: 16rpx;
      margin-bottom: 16rpx;

      .progress-bar {
        flex: 1;
        height: 12rpx;
        background: #f0f0f0;
        border-radius: 6rpx;
        overflow: hidden;

        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
          border-radius: 6rpx;
          transition: width 0.3s;
        }
      }

      .progress-percent {
        font-size: 24rpx;
        color: #999;
        min-width: 60rpx;
        text-align: right;
      }
    }

    .area-detail {
      display: flex;
      gap: 32rpx;

      .detail-item {
        .detail-label {
          font-size: 24rpx;
          color: #999;
          margin-right: 8rpx;
        }

        .detail-value {
          font-size: 26rpx;
          color: #333;

          &.highlight {
            color: #667eea;
            font-weight: 600;
          }
        }
      }
    }
  }
}

.alarm-chart {
  .alarm-types {
    .alarm-type-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .type-header {
        flex: 1;
        display: flex;
        align-items: center;
        gap: 12rpx;

        .type-dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
        }

        .type-name {
          font-size: 28rpx;
          color: #333;
        }
      }

      .type-count {
        font-size: 28rpx;
        color: #333;
        font-weight: 600;
        margin-right: 30rpx;
      }

      .type-percent {
        font-size: 26rpx;
        color: #999;
      }
    }
  }
}

.time-periods {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .period-item {
    padding: 24rpx;
    background: #f5f7fa;
    border-radius: 16rpx;

    .period-name {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 12rpx;
    }

    .period-time {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 16rpx;
    }

    .period-stats {
      display: flex;
      gap: 24rpx;

      .stat {
        .stat-label {
          font-size: 22rpx;
          color: #999;
        }

        .stat-value {
          font-size: 24rpx;
          color: #333;
          font-weight: 600;
          margin-left: 4rpx;
        }
      }
    }
  }
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .device-grid-item {
    padding: 24rpx;
    background: #f5f7fa;
    border-radius: 16rpx;

    .device-name {
      font-size: 26rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .device-metrics {
      display: flex;
      gap: 24rpx;
      margin-bottom: 16rpx;

      .metric {
        .metric-label {
          font-size: 22rpx;
          color: #999;
        }

        .metric-value {
          font-size: 24rpx;
          color: #333;
          font-weight: 600;
          margin-left: 4rpx;
        }
      }
    }

    .device-status {
      padding: 8rpx 20rpx;
      border-radius: 40rpx;
      text-align: center;
      font-size: 24rpx;

      &.normal {
        background: #f6ffed;
        color: #52c41a;
      }

      &.warning {
        background: #fff7e6;
        color: #ffa940;
      }

      &.error {
        background: #fff2f0;
        color: #ff6b6b;
      }
    }
  }
}

.comparison-cards {
  display: flex;
  gap: 20rpx;

  .comparison-card {
    flex: 1;
    padding: 30rpx;
    background: #f5f7fa;
    border-radius: 16rpx;
    text-align: center;

    .comparison-title {
      font-size: 26rpx;
      color: #999;
      margin-bottom: 16rpx;
    }

    .comparison-value {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      font-size: 40rpx;
      font-weight: 700;
      margin-bottom: 12rpx;

      &.up {
        color: #52c41a;
      }

      &.down {
        color: #ff6b6b;
      }
    }

    .comparison-desc {
      font-size: 24rpx;
      color: #666;
    }
  }
}
</style>
