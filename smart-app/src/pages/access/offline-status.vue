<template>
  <view class="offline-status-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-back" @click="goBack">
        <uni-icons type="arrowleft" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">离线同步状态</view>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 网络状态卡片 -->
    <view class="network-card" :class="networkStatus.isConnected ? 'online' : 'offline'">
      <view class="card-icon">
        <uni-icons
          :type="networkStatus.isConnected ? 'cloud-upload' : 'cloud-upload-filled'"
          :size="60"
          :color="networkStatus.isConnected ? '#52c41a' : '#ff4d4f'"
        ></uni-icons>
      </view>
      <view class="card-content">
        <view class="card-title">
          {{ networkStatus.isConnected ? '网络已连接' : '网络未连接' }}
        </view>
        <view class="card-subtitle">
          {{ networkStatus.isConnected ? networkStatus.networkType : '请检查网络设置' }}
        </view>
      </view>
    </view>

    <!-- 同步状态卡片 -->
    <view class="sync-status-card">
      <view class="card-header">
        <view class="header-title">同步状态</view>
        <view class="status-badge" :class="syncStatusClass">
          {{ syncStatusText }}
        </view>
      </view>

      <view class="sync-progress" v-if="syncStatus === 'syncing'">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: syncProgress + '%' }"></view>
        </view>
        <view class="progress-text">正在同步... {{ syncProgress }}%</view>
      </view>

      <view class="sync-info-grid">
        <view class="info-item">
          <view class="info-label">最后同步</view>
          <view class="info-value">{{ lastSyncTimeText }}</view>
        </view>
        <view class="info-item">
          <view class="info-label">同步间隔</view>
          <view class="info-value">{{ syncInterval / 1000 }}秒</view>
        </view>
        <view class="info-item">
          <view class="info-label">待上传记录</view>
          <view class="info-value">{{ pendingRecordsCount }}</view>
        </view>
        <view class="info-item">
          <view class="info-label">同步队列</view>
          <view class="info-value">{{ syncQueueLength }}</view>
        </view>
      </view>

      <!-- 同步按钮 -->
      <view class="sync-actions" v-if="networkStatus.isConnected">
        <button
          class="sync-btn"
          :class="{ 'syncing': syncStatus === 'syncing' }"
          :disabled="syncStatus === 'syncing'"
          @click="handleSync"
        >
          <uni-icons
            :type="syncStatus === 'syncing' ? 'spinner-cycle' : 'refreshempty'"
            size="18"
            color="#fff"
            :class="{ 'rotate': syncStatus === 'syncing' }"
          ></uni-icons>
          <text>{{ syncStatus === 'syncing' ? '同步中...' : '立即同步' }}</text>
        </button>
      </view>
    </view>

    <!-- 数据统计卡片 -->
    <view class="data-stats-card">
      <view class="card-header">
        <view class="header-title">离线数据统计</view>
      </view>

      <view class="stats-grid">
        <view class="stat-item" @click="navigateTo('/pages/access/permission-list')">
          <view class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <uni-icons type="locked" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-content">
            <view class="stat-value">{{ permissionCount }}</view>
            <view class="stat-label">权限数量</view>
          </view>
        </view>

        <view class="stat-item">
          <view class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <uni-icons type="checkmarkempty" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-content">
            <view class="stat-value">{{ todayPassCount }}</view>
            <view class="stat-label">今日通行</view>
          </view>
        </view>

        <view class="stat-item">
          <view class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <uni-icons type="list" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-content">
            <view class="stat-value">{{ storageSize }}</view>
            <view class="stat-label">存储大小</view>
          </view>
        </view>

        <view class="stat-item">
          <view class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
            <uni-icons type="calendar" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-content">
            <view class="stat-value">{{ dataAge }}</view>
            <view class="stat-label">数据时效</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 同步历史 -->
    <view class="sync-history-card">
      <view class="card-header">
        <view class="header-title">同步历史</view>
        <view class="header-action" @click="clearHistory">
          <uni-icons type="trash" size="16" color="#999"></uni-icons>
          <text>清空</text>
        </view>
      </view>

      <view class="history-list" v-if="syncHistory.length > 0">
        <view class="history-item" v-for="(item, index) in syncHistory" :key="index">
          <view class="history-icon" :class="item.success ? 'success' : 'failed'">
            <uni-icons
              :type="item.success ? 'checkmarkempty' : 'closeempty'"
              size="20"
              color="#fff"
            ></uni-icons>
          </view>
          <view class="history-content">
            <view class="history-title">{{ item.title }}</view>
            <view class="history-time">{{ item.time }}</view>
          </view>
          <view class="history-detail" @click="showSyncDetail(item)">
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <view class="empty-history" v-else>
        <uni-icons type="inbox" size="60" color="#ddd"></uni-icons>
        <view class="empty-text">暂无同步记录</view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="bottom-actions">
      <button class="action-btn secondary" @click="navigateTo('/pages/access/offline-settings')">
        <uni-icons type="gear" size="18" color="#666"></uni-icons>
        <text>离线设置</text>
      </button>
      <button class="action-btn danger" @click="handleClearData">
        <uni-icons type="clear" size="18" color="#fff"></uni-icons>
        <text>清空数据</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { offlineSync, SYNC_STATUS } from '@/utils/offline-sync.js'
import { offlinePermission } from '@/utils/offline-permission.js'
import { offlineRecord } from '@/utils/offline-record.js'
import { storage } from '@/utils/offline-storage.js'

// 网络状态
const networkStatus = ref({
  isConnected: false,
  networkType: 'unknown'
})

// 同步状态
const syncStatus = ref(SYNC_STATUS.IDLE)
const syncProgress = ref(0)
const syncInterval = ref(60000)
const lastSyncTime = ref(0)
const syncQueueLength = ref(0)

// 数据统计
const permissionCount = ref(0)
const todayPassCount = ref(0)
const storageSize = ref('0 KB')
const dataAge = ref('未知')
const pendingRecordsCount = ref(0)

// 同步历史
const syncHistory = ref([])

// 计算属性
const syncStatusClass = computed(() => {
  const classMap = {
    [SYNC_STATUS.IDLE]: 'status-idle',
    [SYNC_STATUS.SYNCING]: 'status-syncing',
    [SYNC_STATUS.SUCCESS]: 'status-success',
    [SYNC_STATUS.ERROR]: 'status-error'
  }
  return classMap[syncStatus.value] || 'status-idle'
})

const syncStatusText = computed(() => {
  const textMap = {
    [SYNC_STATUS.IDLE]: '空闲',
    [SYNC_STATUS.SYNCING]: '同步中',
    [SYNC_STATUS.SUCCESS]: '成功',
    [SYNC_STATUS.ERROR]: '失败'
  }
  return textMap[syncStatus.value] || '未知'
})

const lastSyncTimeText = computed(() => {
  if (!lastSyncTime.value) return '从未同步'
  const date = new Date(lastSyncTime.value)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleString()
})

// 初始化
onMounted(async () => {
  console.log('[离线状态页] 页面加载')

  // 初始化离线同步
  await offlineSync.init()
  await offlinePermission.init()
  await offlineRecord.init()

  // 检查网络状态
  checkNetworkStatus()

  // 加载数据统计
  await loadDataStatistics()

  // 加载同步历史
  await loadSyncHistory()

  // 监听同步事件
  offlineSync.on('statusChange', handleStatusChange)
  offlineSync.on('syncComplete', handleSyncComplete)
  offlineSync.on('syncError', handleSyncError)

  // 定时更新
  startUpdateTimer()
})

onUnmounted(() => {
  console.log('[离线状态页] 页面卸载')

  // 移除事件监听
  offlineSync.off('statusChange', handleStatusChange)
  offlineSync.off('syncComplete', handleSyncComplete)
  offlineSync.off('syncError', handleSyncError)

  // 停止定时器
  stopUpdateTimer()
})

// 检查网络状态
const checkNetworkStatus = () => {
  uni.getNetworkType({
    success: (res) => {
      networkStatus.value = {
        isConnected: res.networkType !== 'none',
        networkType: res.networkType
      }
      console.log('[离线状态页] 网络状态:', networkStatus.value)
    }
  })
}

// 加载数据统计
const loadDataStatistics = async () => {
  try {
    // 权限数量
    const stats = await offlinePermission.getStatistics()
    permissionCount.value = stats ? stats.total : 0

    // 今日通行
    const todayRecords = await offlineRecord.getTodayRecords()
    todayPassCount.value = todayRecords.length

    // 待上传记录
    const pendingRecords = await offlineRecord.getPendingRecords()
    pendingRecordsCount.value = pendingRecords.length

    // 存储大小
    const storageInfo = await storage.getInfo()
    if (storageInfo) {
      storageSize.value = `${storageInfo.currentSize} KB`
    }

    // 数据时效
    lastSyncTime.value = await storage.get('offline:sync:last_time', 0)
    if (lastSyncTime.value > 0) {
      const date = new Date(lastSyncTime.value)
      const now = new Date()
      const diff = Math.floor((now - date) / 60000)
      dataAge.value = `${diff}分钟前`
    }

    // 同步队列
    const syncQueue = await storage.get('offline:sync:queue', [])
    syncQueueLength.value = syncQueue.length
  } catch (error) {
    console.error('[离线状态页] 加载统计失败:', error)
  }
}

// 加载同步历史
const loadSyncHistory = async () => {
  // TODO: 从本地存储加载同步历史
  syncHistory.value = [
    {
      success: true,
      title: '自动同步完成',
      time: '10分钟前',
      detail: {
        upload: 15,
        download: 3,
        conflict: 0
      }
    },
    {
      success: true,
      title: '手动同步完成',
      time: '2小时前',
      detail: {
        upload: 8,
        download: 1,
        conflict: 0
      }
    }
  ]
}

// 处理同步状态变化
const handleStatusChange = (status) => {
  console.log('[离线状态页] 同步状态变化:', status)
  syncStatus.value = status

  if (status === SYNC_STATUS.SYNCING) {
    // 模拟同步进度
    simulateSyncProgress()
  }
}

// 模拟同步进度
const simulateSyncProgress = () => {
  let progress = 0
  const interval = setInterval(() => {
    progress += 10
    syncProgress.value = progress

    if (progress >= 100) {
      clearInterval(interval)
    }
  }, 500)
}

// 处理同步完成
const handleSyncComplete = (data) => {
  console.log('[离线状态页] 同步完成:', data)

  // 添加到历史记录
  syncHistory.value.unshift({
    success: true,
    title: '同步成功',
    time: '刚刚',
    detail: data
  })

  // 重新加载数据统计
  loadDataStatistics()
}

// 处理同步错误
const handleSyncError = (error) => {
  console.error('[离线状态页] 同步错误:', error)

  // 添加到历史记录
  syncHistory.value.unshift({
    success: false,
    title: '同步失败: ' + error.message,
    time: '刚刚',
    detail: error
  })
}

// 手动同步
const handleSync = async () => {
  console.log('[离线状态页] 手动同步')

  try {
    const result = await offlineSync.sync(true)

    if (result.success) {
      uni.showToast({
        title: '同步成功',
        icon: 'success'
      })
    } else {
      uni.showToast({
        title: result.message || '同步失败',
        icon: 'none'
      })
    }

    // 重新加载数据
    await loadDataStatistics()
  } catch (error) {
    console.error('[离线状态页] 同步异常:', error)
    uni.showToast({
      title: '同步异常',
      icon: 'none'
    })
  }
}

// 清空数据
const handleClearData = () => {
  uni.showModal({
    title: '确认清空',
    content: '清空后将删除所有离线数据，此操作不可恢复',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        const result = await storage.clear()
        if (result) {
          uni.showToast({
            title: '清空成功',
            icon: 'success'
          })
          await loadDataStatistics()
        } else {
          uni.showToast({
            title: '清空失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 清空历史
const clearHistory = () => {
  syncHistory.value = []
  uni.showToast({
    title: '历史已清空',
    icon: 'success'
  })
}

// 显示同步详情
const showSyncDetail = (item) => {
  uni.showModal({
    title: '同步详情',
    content: JSON.stringify(item.detail, null, 2),
    showCancel: false
  })
}

// 导航
const goBack = () => {
  uni.navigateBack()
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

// 定时更新
let updateTimer = null
const startUpdateTimer = () => {
  updateTimer = setInterval(() => {
    checkNetworkStatus()
    loadDataStatistics()
  }, 10000)  // 每10秒更新一次
}

const stopUpdateTimer = () => {
  if (updateTimer) {
    clearInterval(updateTimer)
    updateTimer = null
  }
}
</script>

<style lang="scss" scoped>
.offline-status-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;

  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
  }

  .nav-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }

  .nav-placeholder {
    width: 60rpx;
  }
}

.card {
  margin: 24rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.network-card {
  display: flex;
  align-items: center;
  padding: 48rpx 32rpx;

  &.online {
    background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
    border: 2rpx solid #91d5ff;
  }

  &.offline {
    background: linear-gradient(135deg, #fff1f0 0%, #fff7f0 100%);
    border: 2rpx solid #ffccc7;
  }

  .card-icon {
    margin-right: 24rpx;
  }

  .card-title {
    font-size: 32rpx;
    font-weight: 600;
    margin-bottom: 8rpx;
  }

  &.online .card-title {
    color: #52c41a;
  }

  &.offline .card-title {
    color: #ff4d4f;
  }

  .card-subtitle {
    font-size: 24rpx;
    color: #999;
  }
}

.sync-status-card {
  @extend .card;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;

    .header-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .status-badge {
      padding: 8rpx 24rpx;
      border-radius: 24rpx;
      font-size: 24rpx;

      &.status-idle {
        background: #f5f5f5;
        color: #999;
      }

      &.status-syncing {
        background: #e6f7ff;
        color: #1890ff;
      }

      &.status-success {
        background: #f6ffed;
        color: #52c41a;
      }

      &.status-error {
        background: #fff1f0;
        color: #ff4d4f;
      }
    }
  }

  .sync-progress {
    margin-bottom: 24rpx;

    .progress-bar {
      height: 16rpx;
      background: #f5f5f5;
      border-radius: 8rpx;
      overflow: hidden;
      margin-bottom: 16rpx;

      .progress-fill {
        height: 100%;
        background: linear-gradient(90deg, #1890ff 0%, #36cfc9 100%);
        border-radius: 8rpx;
        transition: width 0.3s;
      }
    }

    .progress-text {
      text-align: center;
      font-size: 24rpx;
      color: #1890ff;
    }
  }

  .sync-info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24rpx;
    margin-bottom: 24rpx;

    .info-item {
      padding: 24rpx;
      background: #f9f9f9;
      border-radius: 12rpx;

      .info-label {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 8rpx;
      }

      .info-value {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
    }
  }

  .sync-actions {
    .sync-btn {
      width: 100%;
      height: 88rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      color: #fff;
      border: none;

      &.syncing {
        opacity: 0.7;
      }

      ::v-deep .uni-icons {
        margin-right: 8rpx;
      }

      .rotate {
        animation: rotate 1s linear infinite;
      }
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.data-stats-card {
  @extend .card;

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24rpx;

    .stat-item {
      display: flex;
      align-items: center;
      padding: 24rpx;
      background: #f9f9f9;
      border-radius: 12rpx;

      .stat-icon {
        width: 80rpx;
        height: 80rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16rpx;
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 4rpx;
        }

        .stat-label {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
}

.sync-history-card {
  @extend .card;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;

    .header-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .header-action {
      display: flex;
      align-items: center;
      font-size: 24rpx;
      color: #999;

      ::v-deep .uni-icons {
        margin-right: 4rpx;
      }
    }
  }

  .history-list {
    .history-item {
      display: flex;
      align-items: center;
      padding: 24rpx 0;
      border-bottom: 1rpx solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .history-icon {
        width: 56rpx;
        height: 56rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16rpx;

        &.success {
          background: #f6ffed;
        }

        &.failed {
          background: #fff1f0;
        }
      }

      .history-content {
        flex: 1;

        .history-title {
          font-size: 28rpx;
          color: #333;
          margin-bottom: 4rpx;
        }

        .history-time {
          font-size: 24rpx;
          color: #999;
        }
      }

      .history-detail {
        padding: 8rpx;
      }
    }
  }

  .empty-history {
    padding: 120rpx 0;
    text-align: center;

    .empty-text {
      margin-top: 24rpx;
      font-size: 24rpx;
      color: #999;
    }
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 24rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
  display: flex;
  gap: 16rpx;

  .action-btn {
    flex: 1;
    height: 88rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    border: none;

    &.secondary {
      background: #f5f5f5;
      color: #666;
    }

    &.danger {
      background: #ff4d4f;
      color: #fff;
    }

    ::v-deep .uni-icons {
      margin-right: 8rpx;
    }
  }
}
</style>
