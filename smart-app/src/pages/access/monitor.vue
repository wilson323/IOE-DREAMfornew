<template>
  <view class="access-monitor-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">实时监控</view>
      <view class="nav-right" @click="refreshData">
        <text class="refresh-icon">🔄</text>
      </view>
    </view>

    <!-- 实时统计 -->
    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-value">{{ stats.onlineDevices || 0 }}</text>
        <text class="stat-label">在线设备</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.todayAccess || 0 }}</text>
        <text class="stat-label">今日通行</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.activeAlerts || 0 }}</text>
        <text class="stat-label">活跃告警</text>
      </view>
    </view>

    <!-- 实时事件 -->
    <view class="events-section">
      <view class="section-title">实时事件</view>
      <view class="event-list">
        <view
          class="event-item"
          v-for="(event, index) in eventList"
          :key="index"
        >
          <view class="event-info">
            <text class="event-type">{{ event.eventType }}</text>
            <text class="event-time">{{ formatDateTime(event.eventTime) }}</text>
          </view>
          <view class="event-detail">
            <text class="event-desc">{{ event.description }}</text>
          </view>
        </view>

        <view class="no-data" v-if="eventList.length === 0">
          <text>暂无事件</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import accessApi from '@/api/access.js'

// 响应式数据
const stats = reactive({
  onlineDevices: 0,
  todayAccess: 0,
  activeAlerts: 0
})
const eventList = ref([])

// 页面生命周期
onMounted(() => {
  loadStats()
  loadEvents()
})

onShow(() => {
  // 页面显示时可以刷新数据
  refreshData()
})

onPullDownRefresh(() => {
  refreshData()
  uni.stopPullDownRefresh()
})

// 方法实现
const loadStats = async () => {
  try {
    const result = await accessApi.getAccessStatistics()
    if (result.success && result.data) {
      Object.assign(stats, result.data)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadEvents = async () => {
  try {
    const result = await accessApi.getRecentEvents({ limit: 20 })
    if (result.success && result.data) {
      eventList.value = result.data
    }
  } catch (error) {
    console.error('加载事件列表失败:', error)
  }
}

const refreshData = async () => {
  uni.showLoading({ title: '刷新中...' })
  try {
    await Promise.all([loadStats(), loadEvents()])
    uni.hideLoading()
    uni.showToast({ title: '刷新成功', icon: 'success' })
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '刷新失败', icon: 'none' })
  }
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.access-monitor-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
  }

  .icon-back, .refresh-icon {
    font-size: 20px;
    color: #333;
  }
}

.stats-section {
  padding: 15px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;

  .stat-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 15px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .stat-value {
      display: block;
      font-size: 24px;
      font-weight: bold;
      color: #1890ff;
      margin-bottom: 6px;
    }

    .stat-label {
      display: block;
      font-size: 12px;
      color: #666;
    }
  }
}

.events-section {
  padding: 0 15px 15px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }

  .event-list {
    background-color: #fff;
    border-radius: 8px;
    padding: 10px;

    .event-item {
      padding: 12px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .event-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;

        .event-type {
          font-size: 14px;
          font-weight: 500;
          color: #333;
        }

        .event-time {
          font-size: 12px;
          color: #999;
        }
      }

      .event-detail {
        .event-desc {
          font-size: 13px;
          color: #666;
        }
      }
    }

    .no-data {
      text-align: center;
      padding: 30px 0;
      color: #999;
      font-size: 14px;
    }
  }
}
</style>

