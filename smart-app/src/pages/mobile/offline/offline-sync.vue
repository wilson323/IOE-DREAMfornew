<template>
  <view class="offline-sync-page">
    <view class="header">
      <text class="title">离线同步</text>
      <text class="subtitle">数据同步状态: {{syncStatus}}</text>
    </view>

    <view class="content">
      <!-- 同步状态卡片 -->
      <view class="status-card">
        <view class="status-icon" :class="syncStatusClass">
          <text class="icon">{{ syncStatusIcon }}</text>
        </view>
        <view class="status-info">
          <text class="status-title">{{ syncTitle }}</text>
          <text class="status-desc">{{ syncDesc }}</text>
        </view>
      </view>

      <!-- 统计信息 -->
      <view class="stats-section">
        <view class="stat-item">
          <text class="stat-number">{{ pendingCount }}</text>
          <text class="stat-label">待同步</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ syncedCount }}</text>
          <text class="stat-label">已同步</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ failedCount }}</text>
          <text class="stat-label">失败</text>
        </view>
      </view>

      <!-- 待同步数据列表 -->
      <view class="pending-section">
        <view class="section-header">
          <text class="section-title">待同步数据</text>
          <text class="section-desc">({{ pendingData.length }}项)</text>
        </view>

        <scroll-view scroll-y class="data-list" @scrolltolower="loadMoreData">
          <view v-for="(item, index) in pendingData" :key="index" class="data-item">
            <view class="item-icon">
              <text :class="itemIconClass(item.type)">{{ itemIcon }}</text>
            </view>
            <view class="item-content">
              <text class="item-title">{{ item.title }}</text>
              <text class="item-desc">{{ item.description }}</text>
              <text class="item-time">{{ item.time }}</text>
            </view>
            <view class="item-status">
              <text class="status-text pending">待同步</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 同步控制按钮 -->
      <view class="control-section">
        <button class="sync-btn" :disabled="isSyncing" @click="startSync">
          <text v-if="!isSyncing">开始同步</text>
          <text v-else>同步中... ({{ syncProgress }}%)</text>
        </button>

        <button class="clear-btn" @click="clearPendingData" :disabled="isSyncing">
          <text>清除待同步数据</text>
        </button>
      </view>

      <!-- 同步历史 -->
      <view class="history-section">
        <view class="section-header">
          <text class="section-title">同步历史</text>
          <text class="section-desc">最近7天</text>
        </view>

        <view class="history-list">
          <view v-for="(history, index) in syncHistory" :key="index" class="history-item">
            <view class="history-time">{{ history.time }}</view>
            <view class="history-content">
              <text class="history-title">{{ history.title }}</text>
              <text class="history-desc">{{ history.result }}</text>
            </view>
            <view class="history-status" :class="history.status">
              <text class="status-text">{{ history.statusText }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'OfflineSync',
  data() {
    return {
      syncStatus: 'IDLE', // IDLE, SYNCING, COMPLETED, FAILED
      isSyncing: false,
      syncProgress: 0,
      pendingCount: 0,
      syncedCount: 0,
      failedCount: 0,
      pendingData: [],
      syncHistory: []
    }
  },
  computed: {
    syncTitle() {
      const statusMap = {
        'IDLE': '等待同步',
        'SYNCING': '正在同步',
        'COMPLETED': '同步完成',
        'FAILED': '同步失败'
      }
      return statusMap[this.syncStatus] || '未知状态'
    },
    syncDesc() {
      const descMap = {
        'IDLE': '点击开始同步按钮进行数据同步',
        'SYNCING': '正在同步离线数据到服务器',
        'COMPLETED': '所有数据已同步成功',
        'FAILED': '部分数据同步失败，请重试'
      }
      return descMap[this.syncStatus] || '未知状态'
    },
    syncStatusIcon() {
      const iconMap = {
        'IDLE': '⏸',
        'SYNCING': '🔄',
        'COMPLETED': '✅',
        'FAILED': '❌'
      }
      return iconMap[this.syncStatus] || '❓'
    },
    syncStatusClass() {
      return `status-${this.syncStatus.toLowerCase()}`
    }
  },
  mounted() {
    this.loadPendingData()
    this.loadSyncHistory()
    this.checkSyncStatus()
  },
  methods: {
    loadPendingData() {
      // 从本地存储加载待同步数据
      const pending = uni.getStorageSync('pending_sync_data') || []
      this.pendingData = pending.map(item => ({
        ...item,
        time: this.formatTime(item.timestamp)
      }))
      this.pendingCount = this.pendingData.length
    },

    loadSyncHistory() {
      // 从本地存储加载同步历史
      this.syncHistory = uni.getStorageSync('sync_history') || []
    },

    checkSyncStatus() {
      // 检查网络状态和同步状态
      const networkStatus = uni.getNetworkType()
      if (networkStatus === 'none') {
        this.syncStatus = 'FAILED'
      }
    },

    startSync() {
      if (this.isSyncing || this.pendingData.length === 0) {
        return
      }

      this.isSyncing = true
      this.syncStatus = 'SYNCING'

      // 模拟同步过程
      const totalItems = this.pendingData.length
      let syncedItems = 0
      let failedItems = 0

      const syncInterval = setInterval(() => {
        if (syncedItems < this.pendingData.length) {
          const item = this.pendingData[syncedItems]

          // 模拟网络请求
          setTimeout(() => {
            const success = Math.random() > 0.2 // 80%成功率
            if (success) {
              // 同步成功，从本地存储移除
              this.removePendingData(item.id)
              syncedItems++
              this.syncedCount++
            } else {
              failedItems++
              this.failedCount++
            }

            this.syncProgress = Math.round((syncedItems + failedItems) / totalItems * 100)
          }, 200 + Math.random() * 300) // 随机延迟
        } else {
          clearInterval(syncInterval)
          this.isSyncing = false
          this.syncStatus = failedItems > 0 ? 'FAILED' : 'COMPLETED'

          // 保存同步历史
          this.saveSyncHistory({
            time: this.formatTime(Date.now()),
            title: `数据同步 (${syncedItems}成功, ${failedItems}失败)`,
            result: this.syncStatus,
            status: this.syncStatus.toLowerCase()
          })

          // 通知同步结果
          uni.showToast({
            title: '同步完成',
            icon: this.syncStatus === 'COMPLETED' ? 'success' : 'error',
            duration: 3000
          })
        }
      }, 500)
    },

    clearPendingData() {
      uni.showModal({
        title: '确认清除',
        content: '确定要清除所有待同步数据吗？',
        success: () => {
          uni.removeStorageSync('pending_sync_data')
          this.pendingData = []
          this.pendingCount = 0
          this.showToast('待同步数据已清除')
        }
      })
    },

    removePendingData(itemId) {
      const pending = uni.getStorageSync('pending_sync_data') || []
      const filtered = pending.filter(item => item.id !== itemId)
      uni.setStorageSync('pending_sync_data', filtered)
    },

    saveSyncHistory(history) {
      const history = this.syncHistory || []
      history.unshift(history)

      // 只保留最近50条记录
      if (history.length > 50) {
        history.splice(50)
      }

      uni.setStorageSync('sync_history', history)
      this.syncHistory = history
    },

    loadMoreData() {
      // 加载更多数据（如果需要）
      console.log('加载更多待同步数据')
    },

    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) { // 1分钟内
        return '刚刚'
      } else if (diff < 3600000) { // 1小时内
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) { // 1天内
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        return date.toLocaleDateString()
      }
    },

    showToast(message) {
      uni.showToast({
        title: '提示',
        icon: 'none',
        duration: 2000,
        content: message
      })
    },

    itemIconClass(type) {
      const iconMap = {
        'attendance': '📅',
        'access': '🚪',
        'consume': '💳',
        'order': '📦',
        'notice': '📢'
      }
      return iconMap[type] || '📄'
    },

    itemIcon(type) {
      const iconMap = {
        'attendance': '📅',
        'access': '🚪',
        'consume': '💳',
        'order': '📦',
        'notice': '📢'
      }
      return iconMap[type] || '📄'
    }
  }
}
</script>

<style lang="scss" scoped>
.offline-sync-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  text-align: center;
  color: white;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.subtitle {
  font-size: 28rpx;
  opacity: 0.9;
}

.content {
  padding: 30rpx;
}

.status-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.status-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 40rpx;
}

.status-icon.status-idle {
  background: #e0e0e0;
}

.status-icon.status-syncing {
  background: #3498db;
  animation: pulse 2s infinite;
}

.status-icon.status-completed {
  background: #27ae60;
}

.status-icon.status-failed {
  background: #e74c3c;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.status-desc {
  font-size: 24rpx;
  color: #666;
}

.stats-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-number {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #666;
}

.pending-section,
.control-section,
.history-section {
  background: white;
  border-radius: 16rpx;
  margin-bottom: 30rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.section-desc {
  font-size: 24rpx;
  color: #666;
}

.data-list {
  max-height: 400rpx;
}

.data-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.item-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 30rpx;
  background: #f8f9fa;
}

.item-content {
  flex: 1;
  padding-right: 20rpx;
}

.item-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 4rpx;
}

.item-desc {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-bottom: 4rpx;
}

.item-time {
  font-size: 22rpx;
  color: #999;
  display: block;
}

.item-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.status-text.pending {
  color: #f39c12;
  font-size: 22rpx;
  font-weight: bold;
  background: #fef2f2;
}

.control-section {
  display: flex;
  gap: 20rpx;
}

.sync-btn,
.clear-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
  color: white;
}

.sync-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.sync-btn[disabled] {
  background: #ccc;
}

.clear-btn {
  background: #e74c3c;
}

.history-list {
  max-height: 300rpx;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.history-time {
  font-size: 22rpx;
  color: #666;
  margin-right: 20rpx;
  width: 120rpx;
}

.history-content {
  flex: 1;
  padding-right: 20rpx;
}

.history-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 4rpx;
}

.history-desc {
  font-size: 22rpx;
  color: #666;
  display: block;
}

.history-status {
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}

.status-text.completed {
  color: #27ae60;
  background: #d4edda;
}

.status-text.failed {
  color: #e74c3c;
  background: #f2dede;
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>