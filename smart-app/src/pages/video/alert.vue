<template>
  <view class="video-alert-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">告警管理</text>
        <text class="filter-btn" @click="showFilterModal = true">筛选</text>
      </view>
    </view>

    <!-- 告警统计概览 -->
    <view class="alert-overview">
      <view class="overview-grid">
        <view class="overview-card total">
          <text class="card-value">{{ overview.totalCount || 0 }}</text>
          <text class="card-label">今日告警</text>
        </view>
        <view class="overview-card critical">
          <text class="card-value">{{ overview.criticalCount || 0 }}</text>
          <text class="card-label">紧急</text>
        </view>
        <view class="overview-card high">
          <text class="card-value">{{ overview.highCount || 0 }}</text>
          <text class="card-label">重要</text>
        </view>
        <view class="overview-card active">
          <text class="card-value">{{ overview.activeCount || 0 }}</text>
          <text class="card-label">活跃</text>
        </view>
      </view>
    </view>

    <!-- 告警列表 -->
    <view class="alert-list-section">
      <view class="section-header">
        <text class="section-title">告警列表</text>
        <text class="refresh-btn" @click="refreshAlarms">刷新</text>
      </view>

      <scroll-view
        class="alert-scroll"
        scroll-y
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view
          v-for="alarm in alarmList"
          :key="alarm.alarmId"
          :class="['alert-card', alarm.alarmLevel]"
          @click="viewAlarmDetail(alarm)"
        >
          <!-- 告警级别标识 -->
          <view :class="['level-indicator', alarm.alarmLevel]"></view>

          <!-- 告警内容 -->
          <view class="alert-content">
            <view class="alert-header">
              <text class="device-name">{{ alarm.deviceName }}</text>
              <view :class="['urgency-badge', alarm.urgency]">
                {{ getUrgencyText(alarm.urgency) }}
              </view>
            </view>

            <text class="alert-message">{{ alarm.alarmMessage }}</text>

            <view class="alert-footer">
              <text class="alert-time">{{ formatTime(alarm.createTime) }}</text>
              <view :class="['alert-status', alarm.status]">
                {{ getStatusText(alarm.status) }}
              </view>
            </view>
          </view>

          <!-- 快速操作按钮 -->
          <view class="alert-actions" @click.stop>
            <button class="action-btn confirm" @click="handleAlarm(alarm, 'CONFIRM')">
              确认
            </button>
            <button class="action-btn ignore" @click="handleAlarm(alarm, 'IGNORE')">
              忽略
            </button>
            <button class="action-btn forward" @click="handleAlarm(alarm, 'FORWARD')">
              转发
            </button>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="alarmList.length === 0 && !loading" class="empty-state">
          <text class="empty-icon">✅</text>
          <text class="empty-text">暂无告警</text>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="loading-more">
          <text>加载更多...</text>
        </view>
        <view v-else-if="alarmList.length > 0" class="no-more">
          <text>没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- 筛选弹窗 -->
    <view class="filter-modal" v-if="showFilterModal" @click="showFilterModal = false">
      <view class="filter-content" @click.stop>
        <text class="filter-title">筛选告警</text>

        <view class="filter-section">
          <text class="filter-label">告警级别</text>
          <view class="filter-options">
            <text
              v-for="level in levelOptions"
              :key="level.value"
              :class="['filter-option', { selected: filterLevel === level.value }]"
              @click="filterLevel = level.value"
            >
              {{ level.label }}
            </text>
          </view>
        </view>

        <view class="filter-section">
          <text class="filter-label">告警状态</text>
          <view class="filter-options">
            <text
              v-for="status in statusOptions"
              :key="status.value"
              :class="['filter-option', { selected: filterStatus === status.value }]"
              @click="filterStatus = status.value"
            >
              {{ status.label }}
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

<script>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import videoApi from '@/api/business/video/video-api'
import { useWebSocket } from '@/utils/websocket'

export default {
  name: 'VideoAlert',

  setup() {
    // 系统信息
    const systemInfo = uni.getSystemInfoSync()
    const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

    // 页面状态
    const loading = ref(false)
    const refreshing = ref(false)
    const hasMore = ref(true)
    const showFilterModal = ref(false)

    // 筛选条件
    const filterLevel = ref('all')
    const filterStatus = ref('all')

    // 数据
    const overview = reactive({
      totalCount: 0,
      criticalCount: 0,
      highCount: 0,
      activeCount: 0
    })
    const alarmList = ref([])
    const pageNum = ref(1)
    const pageSize = ref(20)

    // 选项
    const levelOptions = [
      { value: 'all', label: '全部' },
      { value: 'CRITICAL', label: '紧急' },
      { value: 'HIGH', label: '重要' },
      { value: 'MEDIUM', label: '一般' },
      { value: 'LOW', label: '低' }
    ]

    const statusOptions = [
      { value: 'all', label: '全部' },
      { value: 'ACTIVE', label: '活跃' },
      { value: 'CONFIRMED', label: '已确认' },
      { value: 'RESOLVED', label: '已解决' }
    ]

    // WebSocket
    const wsClient = ref(null)

    // 页面生命周期
    onMounted(() => {
      init()
    })

    onUnmounted(() => {
      cleanup()
    })

    // 初始化
    const init = async () => {
      await loadOverview()
      await loadAlarms()
      initWebSocket()
    }

    // 加载告警概览
    const loadOverview = async () => {
      try {
        const res = await videoApi.getAlarmOverview()
        if (res.code === 1 && res.data) {
          Object.assign(overview, res.data)
        }
      } catch (error) {
        console.error('加载告警概览失败:', error)
      }
    }

    // 加载告警列表
    const loadAlarms = async (append = false) => {
      try {
        loading.value = true

        const res = await videoApi.getActiveAlarms(pageSize.value)

        if (res.code === 1 && res.data) {
          const newAlarms = res.data || []

          if (append) {
            alarmList.value = [...alarmList.value, ...newAlarms]
          } else {
            alarmList.value = newAlarms
          }

          hasMore.value = newAlarms.length >= pageSize.value
        }
      } catch (error) {
        console.error('加载告警列表失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }

    // 初始化WebSocket
    const initWebSocket = () => {
      const wsUrl = `wss://${import.meta.env.VITE_APP_API_URL.replace('https://', '')}/ws/video/alarm`

      wsClient.value = useWebSocket({
        url: wsUrl,
        heartbeatInterval: 30000
      })

      // 订阅新告警
      wsClient.value.subscribe('new_alarm', handleNewAlarm)

      // 订阅告警状态更新
      wsClient.value.subscribe('alarm_status_update', handleAlarmStatusUpdate)

      wsClient.value.connect()
    }

    // 处理新告警
    const handleNewAlarm = (message) => {
      const newAlarm = message.data

      // 添加到列表顶部
      alarmList.value.unshift(newAlarm)

      // 更新统计
      overview.totalCount++
      if (newAlarm.alarmLevel === 'CRITICAL') {
        overview.criticalCount++
      } else if (newAlarm.alarmLevel === 'HIGH') {
        overview.highCount++
      }

      // 震动提醒
      uni.vibrateShort()

      // 声音提醒（如果是紧急告警）
      if (newAlarm.alarmLevel === 'CRITICAL') {
        uni.showToast({
          title: '紧急告警！',
          icon: 'none',
          duration: 2000
        })
      }
    }

    // 处理告警状态更新
    const handleAlarmStatusUpdate = (message) => {
      const { alarmId, status } = message.data
      const alarm = alarmList.value.find(a => a.alarmId === alarmId)
      if (alarm) {
        alarm.status = status
      }
    }

    // 处理告警
    const handleAlarm = async (alarm, action) => {
      try {
        const userStore = useUserStore()
        const userId = userStore.userId

        const res = await videoApi.processMobileAlarm(alarm.alarmId, action, userId)

        if (res.code === 1) {
          uni.showToast({ title: '处理成功', icon: 'success' })

          // 更新告警状态
          alarm.status = action === 'CONFIRM' ? 'CONFIRMED' : 'IGNORED'

          // 震动反馈
          uni.vibrateShort()
        }
      } catch (error) {
        console.error('处理告警失败:', error)
        uni.showToast({ title: '处理失败', icon: 'none' })
      }
    }

    // 查看告警详情
    const viewAlarmDetail = (alarm) => {
      uni.navigateTo({
        url: `/pages/video/alert-detail?alarmId=${alarm.alarmId}`
      })
    }

    // 刷新告警
    const refreshAlarms = async () => {
      pageNum.value = 1
      await Promise.all([
        loadOverview(),
        loadAlarms(false)
      ])
      uni.showToast({ title: '刷新成功', icon: 'success' })
    }

    // 下拉刷新
    const onRefresh = async () => {
      refreshing.value = true
      pageNum.value = 1
      await loadAlarms(false)
    }

    // 加载更多
    const loadMore = () => {
      if (hasMore.value && !loading.value) {
        pageNum.value++
        loadAlarms(true)
      }
    }

    // 应用筛选
    const applyFilter = async () => {
      showFilterModal.value = false
      pageNum.value = 1
      await loadAlarms(false)
    }

    // 重置筛选
    const resetFilter = () => {
      filterLevel.value = 'all'
      filterStatus.value = 'all'
    }

    // 返回
    const goBack = () => {
      uni.navigateBack()
    }

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) {
        return `${Math.floor(diff / 3600000)}小时前`
      } else {
        return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
      }
    }

    // 获取紧急程度文本
    const getUrgencyText = (urgency) => {
      const map = {
        URGENT: '紧急',
        HIGH: '重要',
        MEDIUM: '一般',
        LOW: '低',
        INFO: '信息'
      }
      return map[urgency] || '未知'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const map = {
        ACTIVE: '活跃',
        CONFIRMED: '已确认',
        RESOLVED: '已解决',
        IGNORED: '已忽略'
      }
      return map[status] || '未知'
    }

    // 清理资源
    const cleanup = () => {
      if (wsClient.value) {
        wsClient.value.disconnect()
      }
    }

    return {
      statusBarHeight,
      loading,
      refreshing,
      hasMore,
      showFilterModal,
      filterLevel,
      filterStatus,
      overview,
      alarmList,
      levelOptions,
      statusOptions,
      handleAlarm,
      viewAlarmDetail,
      refreshAlarms,
      onRefresh,
      loadMore,
      applyFilter,
      resetFilter,
      goBack,
      formatTime,
      getUrgencyText,
      getStatusText
    }
  }
}
</script>

<style lang="scss" scoped>
.video-alert-page {
  min-height: 100vh;
  background: #f5f5f5;
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

.alert-overview {
  padding: 24rpx 32rpx;

  .overview-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;
  }

  .overview-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx 16rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .card-value {
      font-size: 40rpx;
      font-weight: 600;
      margin-bottom: 8rpx;
    }

    .card-label {
      font-size: 22rpx;
      color: rgba(0, 0, 0, 0.45);
    }

    &.total .card-value {
      color: #1890ff;
    }

    &.critical .card-value {
      color: #f5222d;
    }

    &.high .card-value {
      color: #fa8c16;
    }

    &.active .card-value {
      color: #52c41a;
    }
  }
}

.alert-list-section {
  padding: 0 32rpx 32rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
  }

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }

  .refresh-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.alert-scroll {
  height: calc(100vh - 400rpx);
}

.alert-card {
  position: relative;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  overflow: hidden;

  .level-indicator {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 8rpx;

    &.CRITICAL {
      background: #f5222d;
    }

    &.HIGH {
      background: #fa8c16;
    }

    &.MEDIUM {
      background: #faad14;
    }

    &.LOW {
      background: #52c41a;
    }
  }

  .alert-content {
    padding-left: 16rpx;

    .alert-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;
    }

    .device-name {
      font-size: 32rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }

    .urgency-badge {
      padding: 4rpx 16rpx;
      border-radius: 8rpx;
      font-size: 22rpx;
      color: #fff;

      &.URGENT {
        background: #f5222d;
      }

      &.HIGH {
        background: #fa8c16;
      }

      &.MEDIUM {
        background: #faad14;
      }

      &.LOW {
        background: #52c41a;
      }
    }

    .alert-message {
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.65);
      line-height: 1.6;
      margin-bottom: 16rpx;
      display: block;
    }

    .alert-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .alert-time {
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
    }

    .alert-status {
      padding: 4rpx 12rpx;
      border-radius: 6rpx;
      font-size: 22rpx;

      &.ACTIVE {
        background: #e6f7ff;
        color: #1890ff;
      }

      &.CONFIRMED {
        background: #f6ffed;
        color: #52c41a;
      }

      &.RESOLVED {
        background: #f0f0f0;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }

  .alert-actions {
    display: flex;
    gap: 16rpx;
    margin-top: 24rpx;
    padding-top: 24rpx;
    border-top: 1px solid #f0f0f0;

    .action-btn {
      flex: 1;
      height: 64rpx;
      border: none;
      border-radius: 8rpx;
      font-size: 28rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.confirm {
        background: #e6f7ff;
        color: #1890ff;

        &:active {
          background: #1890ff;
          color: #fff;
        }
      }

      &.ignore {
        background: #f0f0f0;
        color: rgba(0, 0, 0, 0.65);

        &:active {
          background: #d9d9d9;
        }
      }

      &.forward {
        background: #fff7e6;
        color: #fa8c16;

        &:active {
          background: #fa8c16;
          color: #fff;
        }
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 24rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: rgba(0, 0, 0, 0.45);
  }
}

.loading-more,
.no-more {
  text-align: center;
  padding: 32rpx;
  font-size: 28rpx;
  color: rgba(0, 0, 0, 0.45);
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
  z-index: 200;

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
    }

    .filter-section {
      margin-bottom: 32rpx;

      .filter-label {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        display: block;
        margin-bottom: 16rpx;
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

