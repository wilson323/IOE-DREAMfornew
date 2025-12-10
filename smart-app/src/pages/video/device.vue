<template>
  <view class="video-device-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">设备管理</text>
        <text class="sync-btn" @click="syncDevices">同步</text>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-section">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索设备名称或位置"
          @confirm="handleSearch"
        />
        <text v-if="searchKeyword" class="clear-icon" @click="clearSearch">✕</text>
      </view>
    </view>

    <!-- 设备统计 -->
    <view class="device-stats">
      <view class="stat-item">
        <text class="stat-label">总数</text>
        <text class="stat-value">{{ statistics.totalCount || 0 }}</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-label">在线</text>
        <text class="stat-value online">{{ statistics.onlineCount || 0 }}</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-label">离线</text>
        <text class="stat-value offline">{{ statistics.offlineCount || 0 }}</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-label">故障</text>
        <text class="stat-value fault">{{ statistics.faultCount || 0 }}</text>
      </view>
    </view>

    <!-- 设备列表 -->
    <scroll-view
      class="device-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        v-for="device in deviceList"
        :key="device.deviceId"
        class="device-card"
        @click="viewDeviceDetail(device)"
      >
        <!-- 设备图标和状态 -->
        <view class="device-icon-wrapper">
          <view :class="['device-icon', device.status]">
            <text>📹</text>
          </view>
          <view :class="['status-dot', device.status]"></view>
        </view>

        <!-- 设备信息 -->
        <view class="device-info">
          <view class="device-header">
            <text class="device-name">{{ device.deviceName }}</text>
            <view :class="['device-status-badge', device.status]">
              {{ getStatusText(device.status) }}
            </view>
          </view>

          <text class="device-location">📍 {{ device.location || '未设置' }}</text>

          <view class="device-meta">
            <text class="meta-item">IP: {{ device.ipAddress }}</text>
            <text class="meta-item" v-if="device.ptzEnabled">支持云台</text>
            <text class="meta-item" v-if="device.recordingEnabled">录像中</text>
          </view>
        </view>

        <!-- 快速操作 -->
        <view class="device-actions" @click.stop>
          <button class="quick-btn" @click="quickPreview(device)">
            <text>👁️</text>
          </button>
          <button class="quick-btn" @click="quickControl(device)">
            <text>🎮</text>
          </button>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="deviceList.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">📹</text>
        <text class="empty-text">暂无设备</text>
      </view>

      <!-- 加载更多 -->
      <view v-if="hasMore" class="loading-more">
        <text>加载更多...</text>
      </view>
      <view v-else-if="deviceList.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import videoApi from '@/api/business/video/video-api'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

// 页面状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const searchKeyword = ref('')

// 数据
const statistics = reactive({
  totalCount: 0,
  onlineCount: 0,
  offlineCount: 0,
  faultCount: 0
})
const deviceList = ref([])
const pageNum = ref(1)
const pageSize = ref(20)

// 页面生命周期
onMounted(() => {
  init()
})

// 初始化
const init = async () => {
  await loadDevices()
  await loadStatistics()
}

// 加载设备列表
const loadDevices = async (append = false) => {
  try {
    loading.value = true

    const res = await videoApi.getMobileDevices(false)  // 显示所有设备

    if (res.code === 1 && res.data) {
      const newDevices = res.data.devices || []

      if (append) {
        deviceList.value = [...deviceList.value, ...newDevices]
      } else {
        deviceList.value = newDevices
      }

      hasMore.value = newDevices.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const res = await videoApi.getDeviceStatistics()
    if (res.code === 1 && res.data) {
      statistics.totalCount = res.data.totalCount || 0
      statistics.onlineCount = res.data.onlineCount || 0
      statistics.offlineCount = res.data.offlineCount || 0
      statistics.faultCount = res.data.faultCount || 0
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 查看设备详情
const viewDeviceDetail = (device) => {
  uni.navigateTo({
    url: `/pages/video/device-detail?deviceId=${device.deviceId}`
  })
}

// 快速预览
const quickPreview = (device) => {
  uni.navigateTo({
    url: `/pages/video/monitor?deviceId=${device.deviceId}`
  })
}

// 快速控制
const quickControl = (device) => {
  if (!device.ptzEnabled) {
    uni.showToast({ title: '该设备不支持云台', icon: 'none' })
    return
  }

  uni.navigateTo({
    url: `/pages/video/ptz?deviceId=${device.deviceId}`
  })
}

// 同步设备
const syncDevices = async () => {
  await loadDevices()
  await loadStatistics()
  uni.showToast({ title: '同步成功', icon: 'success' })
}

// 搜索
const handleSearch = () => {
  // 实现搜索逻辑
  const keyword = searchKeyword.value.toLowerCase()
  if (!keyword) {
    loadDevices()
    return
  }

  deviceList.value = deviceList.value.filter(device =>
    device.deviceName.toLowerCase().includes(keyword) ||
    device.location?.toLowerCase().includes(keyword)
  )
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  loadDevices()
}

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  pageNum.value = 1
  await loadDevices(false)
}

// 加载更多
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    pageNum.value++
    loadDevices(true)
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    online: '在线',
    offline: '离线',
    fault: '故障',
    recording: '录像中'
  }
  return map[status] || '未知'
}
</script>

<style lang="scss" scoped>
.video-device-page {
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

  .sync-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.search-section {
  padding: 24rpx 32rpx;
  background: #fff;

  .search-box {
    display: flex;
    align-items: center;
    height: 72rpx;
    padding: 0 24rpx;
    background: #f5f5f5;
    border-radius: 36rpx;

    .search-icon {
      font-size: 32rpx;
      margin-right: 16rpx;
    }

    .search-input {
      flex: 1;
      font-size: 28rpx;
    }

    .clear-icon {
      font-size: 32rpx;
      color: rgba(0, 0, 0, 0.25);
      margin-left: 16rpx;
    }
  }
}

.device-stats {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  margin-bottom: 16rpx;

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;

    .stat-label {
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 8rpx;
    }

    .stat-value {
      font-size: 40rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);

      &.online {
        color: #52c41a;
      }

      &.offline {
        color: #d9d9d9;
      }

      &.fault {
        color: #f5222d;
      }
    }
  }

  .stat-divider {
    width: 1px;
    height: 60rpx;
    background: #e8e8e8;
  }
}

.device-scroll {
  height: calc(100vh - 450rpx);
  padding: 0 32rpx;
}

.device-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  &:active {
    background: #f5f5f5;
  }

  .device-icon-wrapper {
    position: relative;
    margin-right: 24rpx;

    .device-icon {
      width: 96rpx;
      height: 96rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 48rpx;

      &.online {
        background: #e6fffb;
      }

      &.offline {
        background: #f5f5f5;
      }

      &.fault {
        background: #fff1f0;
      }
    }

    .status-dot {
      position: absolute;
      top: 0;
      right: 0;
      width: 20rpx;
      height: 20rpx;
      border-radius: 50%;
      border: 3rpx solid #fff;

      &.online {
        background: #52c41a;
      }

      &.offline {
        background: #d9d9d9;
      }

      &.fault {
        background: #f5222d;
      }
    }
  }

  .device-info {
    flex: 1;

    .device-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12rpx;
    }

    .device-name {
      font-size: 32rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }

    .device-status-badge {
      padding: 4rpx 12rpx;
      border-radius: 6rpx;
      font-size: 22rpx;

      &.online {
        background: #f6ffed;
        color: #52c41a;
      }

      &.offline {
        background: #f5f5f5;
        color: rgba(0, 0, 0, 0.45);
      }

      &.fault {
        background: #fff1f0;
        color: #f5222d;
      }
    }

    .device-location {
      font-size: 26rpx;
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 12rpx;
      display: block;
    }

    .device-meta {
      display: flex;
      gap: 24rpx;

      .meta-item {
        font-size: 24rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }

  .device-actions {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
    margin-left: 16rpx;

    .quick-btn {
      width: 72rpx;
      height: 72rpx;
      background: #f0f0f0;
      border: none;
      border-radius: 12rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36rpx;
      padding: 0;

      &:active {
        background: #1890ff;
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
</style>

