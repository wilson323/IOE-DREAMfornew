<template>
  <view class="remote-control-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <uni-icons type="arrowleft" size="20" color="#333" @tap="goBack"></uni-icons>
        <text class="nav-title">门禁远程控制</text>
        <view class="nav-actions">
          <uni-icons type="refresh" size="20" color="#1890ff" @tap="refreshStatus"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 区域选择 -->
    <view class="area-selector">
      <view class="selector-label">
        <uni-icons type="location" size="16" color="#1890ff"></uni-icons>
        <text>当前区域</text>
      </view>
      <picker mode="selector" :range="areas" range-key="areaName" :value="selectedAreaIndex" @change="onAreaChange">
        <view class="selector-value">
          <text>{{ selectedArea ? selectedArea.areaName : '请选择区域' }}</text>
          <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
        </view>
      </picker>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-container">
      <view class="stat-card online">
        <view class="stat-icon">
          <uni-icons type="checkmarkempty" size="24" color="#52c41a"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.online }}</text>
          <text class="stat-label">在线设备</text>
        </view>
      </view>
      <view class="stat-card offline">
        <view class="stat-icon">
          <uni-icons type="closeempty" size="24" color="#ff4d4f"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.offline }}</text>
          <text class="stat-label">离线设备</text>
        </view>
      </view>
      <view class="stat-card fault">
        <view class="stat-icon">
          <uni-icons type="alert" size="24" color="#fa8c16"></uni-icons>
        </view>
        <view class="stat-info">
          <text class="stat-value">{{ statistics.fault }}</text>
          <text class="stat-label">故障设备</text>
        </view>
      </view>
    </view>

    <!-- 搜索框 -->
    <view class="search-bar">
      <uni-search-bar
        v-model="searchKeyword"
        placeholder="搜索门禁设备"
        :radius="100"
        :clear-button="true"
        @confirm="onSearch"
        @clear="onSearchClear"
      ></uni-search-bar>
    </view>

    <!-- 批量操作 -->
    <view class="batch-actions">
      <button class="action-btn open-all" @tap="openAllDoors">
        <uni-icons type="loop" size="16" color="#fff"></uni-icons>
        <text>一键全开</text>
      </button>
      <button class="action-btn lock-all" @tap="lockAllDoors">
        <uni-icons type="locked" size="16" color="#fff"></uni-icons>
        <text>一键全锁</text>
      </button>
      <button class="action-btn reset-all" @tap="resetAllDoors">
        <uni-icons type="refreshempty" size="16" color="#fff"></uni-icons>
        <text>一键复位</text>
      </button>
    </view>

    <!-- 门禁列表 -->
    <scroll-view
      class="door-list-container"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view v-if="!loading && doorList.length === 0" class="empty-state">
        <image src="/static/images/empty-device.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无门禁设备</text>
        <text class="empty-hint">请切换其他区域查看</text>
      </view>

      <!-- 门禁卡片列表 -->
      <view v-for="door in doorList" :key="door.deviceId" class="door-card" @tap="goToDoorControl(door)">
        <!-- 状态标识 -->
        <view class="door-status" :class="getStatusClass(door)">
          <view class="status-dot"></view>
          <text class="status-text">{{ getStatusText(door) }}</text>
        </view>

        <!-- 设备信息 -->
        <view class="door-info">
          <view class="door-header">
            <view class="door-title-row">
              <uni-icons
                :type="getDoorIcon(door.deviceType)"
                size="20"
                color="#1890ff"
                class="door-icon"
              ></uni-icons>
              <text class="door-name">{{ door.deviceName }}</text>
            </view>
            <view class="door-code">
              <text>编码: {{ door.deviceCode }}</text>
            </view>
          </view>

          <view class="door-details">
            <view class="detail-item">
              <uni-icons type="home" size="14" color="#666"></uni-icons>
              <text>{{ door.areaName || '未分配区域' }}</text>
            </view>
            <view class="detail-item">
              <uni-icons type="eye" size="14" color="#666"></uni-icons>
              <text>今日通行: {{ door.todayPassCount || 0 }}次</text>
            </view>
          </view>
        </view>

        <!-- 控制按钮 -->
        <view class="door-controls">
          <button
            class="control-btn open-btn"
            :disabled="door.onlineStatus !== 1"
            @tap.stop="openDoor(door)"
          >
            <uni-icons type="arrowright" size="16" color="#fff"></uni-icons>
            <text>开门</text>
          </button>
          <button
            class="control-btn lock-btn"
            :disabled="door.onlineStatus !== 1"
            @tap.stop="lockDoor(door)"
          >
            <uni-icons type="locked" size="16" color="#fff"></uni-icons>
            <text>锁定</text>
          </button>
          <button
            class="control-btn detail-btn"
            @tap.stop="goToDoorControl(door)"
          >
            <uni-icons type="more" size="16" color="#1890ff"></uni-icons>
            <text>详情</text>
          </button>
        </view>

        <!-- 故障信息 -->
        <view v-if="door.faultStatus" class="fault-info">
          <uni-icons type="alert" size="14" color="#ff4d4f"></uni-icons>
          <text class="fault-text">{{ door.faultMessage }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <uni-load-more status="loading" :content-text="loadingText"></uni-load-more>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { accessApi } from '@/api/business/access/access-api.js'

/**
 * 门禁远程控制主页面
 * 功能：
 * 1. 区域选择和门禁列表展示
 * 2. 批量控制（一键全开、一键全锁、一键复位）
 * 3. 单门快速控制（开门、锁定）
 * 4. 实时状态刷新
 * 5. 设备统计展示
 */

// ==================== 状态数据 ====================

const loading = ref(false)
const refreshing = ref(false)
const searchKeyword = ref('')

// 区域相关
const areas = ref([])
const selectedAreaIndex = ref(0)
const selectedArea = computed(() => {
  if (selectedAreaIndex.value >= 0 && selectedAreaIndex.value < areas.value.length) {
    return areas.value[selectedAreaIndex.value]
  }
  return null
})

// 门禁列表
const doorList = ref([])

// 统计数据
const statistics = ref({
  online: 0,
  offline: 0,
  fault: 0
})

// 加载文本
const loadingText = ref({
  contentdown: '上拉加载更多',
  contentrefresh: '加载中...',
  contentnomore: '没有更多数据了'
})

// 定时器
let statusTimer = null

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[门禁远程控制] 页面加载')
  loadAreas()
  startStatusTimer()
})

onUnmounted(() => {
  console.log('[门禁远程控制] 页面卸载')
  stopStatusTimer()
})

// ==================== 数据加载 ====================

/**
 * 加载区域列表
 */
const loadAreas = async () => {
  try {
    const result = await accessApi.getAreaList({
      pageNum: 1,
      pageSize: 100
    })

    if (result.success && result.data) {
      areas.value = result.data.list || []

      // 默认选择第一个区域
      if (areas.value.length > 0) {
        selectedAreaIndex.value = 0
        loadDoorList()
      }
    }
  } catch (error) {
    console.error('[门禁远程控制] 加载区域失败:', error)
  }
}

/**
 * 加载门禁列表
 */
const loadDoorList = async () => {
  if (!selectedArea.value) {
    return
  }

  loading.value = true

  try {
    const params = {
      areaId: selectedArea.value.areaId,
      deviceType: 1, // 门禁设备
      keyword: searchKeyword.value
    }

    const result = await accessApi.getDeviceList(params)

    if (result.success && result.data) {
      doorList.value = result.data.list || []
      calculateStatistics()
    }
  } catch (error) {
    console.error('[门禁远程控制] 加载门禁列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 刷新状态
 */
const refreshStatus = async () => {
  console.log('[门禁远程控制] 刷新状态')
  await loadDoorList()
}

/**
 * 计算统计数据
 */
const calculateStatistics = () => {
  let online = 0
  let offline = 0
  let fault = 0

  doorList.value.forEach(door => {
    if (door.onlineStatus === 1) {
      online++
    } else {
      offline++
    }
    if (door.faultStatus === 1) {
      fault++
    }
  })

  statistics.value = { online, offline, fault }
}

// ==================== 交互事件 ====================

/**
 * 区域变更
 */
const onAreaChange = (e) => {
  selectedAreaIndex.value = e.detail.value
  doorList.value = []
  loadDoorList()
}

/**
 * 搜索
 */
const onSearch = () => {
  pageNum.value = 1
  loadDoorList()
}

/**
 * 清除搜索
 */
const onSearchClear = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  loadDoorList()
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadDoorList().finally(() => {
    refreshing.value = false
  })
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 跳转到单门控制
 */
const goToDoorControl = (door) => {
  uni.navigateTo({
    url: `/pages/access/door-control?deviceId=${door.deviceId}`
  })
}

// ==================== 控制操作 ====================

/**
 * 开门
 */
const openDoor = async (door) => {
  if (door.onlineStatus !== 1) {
    uni.showToast({
      title: '设备离线，无法操作',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认开门',
    content: `确定要打开 ${door.deviceName} 吗？`,
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '开门中...',
          mask: true
        })

        try {
          const result = await accessApi.remoteOpenDoor(door.deviceId)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '开门成功',
              icon: 'success'
            })

            // 刷新状态
            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁远程控制] 开门失败:', error)
          uni.showToast({
            title: '开门失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 锁定
 */
const lockDoor = async (door) => {
  if (door.onlineStatus !== 1) {
    uni.showToast({
      title: '设备离线，无法操作',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认锁定',
    content: `确定要锁定 ${door.deviceName} 吗？`,
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '锁定中...',
          mask: true
        })

        try {
          const result = await accessApi.remoteLockDoor(door.deviceId)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '锁定成功',
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁远程控制] 锁定失败:', error)
          uni.showToast({
            title: '锁定失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 一键全开
 */
const openAllDoors = () => {
  const onlineDoors = doorList.value.filter(door => door.onlineStatus === 1)

  if (onlineDoors.length === 0) {
    uni.showToast({
      title: '没有在线设备',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认全开',
    content: `确定要打开全部 ${onlineDoors.length} 个在线门禁吗？`,
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '操作中...',
          mask: true
        })

        try {
          const deviceIds = onlineDoors.map(door => door.deviceId)
          const result = await accessApi.batchOpenDoors(deviceIds)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: `已打开 ${result.data.successCount || 0} 个门禁`,
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁远程控制] 一键全开失败:', error)
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 一键全锁
 */
const lockAllDoors = () => {
  const onlineDoors = doorList.value.filter(door => door.onlineStatus === 1)

  if (onlineDoors.length === 0) {
    uni.showToast({
      title: '没有在线设备',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认全锁',
    content: `确定要锁定全部 ${onlineDoors.length} 个在线门禁吗？`,
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '操作中...',
          mask: true
        })

        try {
          const deviceIds = onlineDoors.map(door => door.deviceId)
          const result = await accessApi.batchLockDoors(deviceIds)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: `已锁定 ${result.data.successCount || 0} 个门禁`,
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁远程控制] 一键全锁失败:', error)
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 一键复位
 */
const resetAllDoors = () => {
  const onlineDoors = doorList.value.filter(door => door.onlineStatus === 1)

  if (onlineDoors.length === 0) {
    uni.showToast({
      title: '没有在线设备',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认复位',
    content: `确定要复位全部 ${onlineDoors.length} 个在线门禁吗？`,
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '复位中...',
          mask: true
        })

        try {
          const deviceIds = onlineDoors.map(door => door.deviceId)
          const result = await accessApi.batchResetDoors(deviceIds)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: `已复位 ${result.data.successCount || 0} 个门禁`,
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁远程控制] 一键复位失败:', error)
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// ==================== 工具方法 ====================

/**
 * 获取状态样式类
 */
const getStatusClass = (door) => {
  if (door.faultStatus === 1) {
    return 'status-fault'
  }
  if (door.onlineStatus === 1) {
    return 'status-online'
  }
  return 'status-offline'
}

/**
 * 获取状态文本
 */
const getStatusText = (door) => {
  if (door.faultStatus === 1) {
    return '故障'
  }
  if (door.onlineStatus === 1) {
    return door.doorStatus === 1 ? '开启' : '关闭'
  }
  return '离线'
}

/**
 * 获取门禁图标
 */
const getDoorIcon = (deviceType) => {
  const iconMap = {
    1: 'locked',
    11: 'locked',
    12: 'arrowright'
  }
  return iconMap[deviceType] || 'locked'
}

/**
 * 启动状态定时器
 */
const startStatusTimer = () => {
  statusTimer = setInterval(() => {
    if (doorList.value.length > 0) {
      refreshStatus()
    }
  }, 30000) // 30秒刷新一次
}

/**
 * 停止状态定时器
 */
const stopStatusTimer = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}
</script>

<style lang="scss" scoped>
.remote-control-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 导航栏
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  padding-bottom: 20rpx;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;

    .nav-title {
      flex: 1;
      text-align: center;
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
    }

    .nav-actions {
      width: 80rpx;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 区域选择器
.area-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 30rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .selector-label {
    display: flex;
    align-items: center;
    gap: 12rpx;
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }

  .selector-value {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 28rpx;
    color: #666;
  }
}

// 统计卡片
.stats-container {
  display: flex;
  gap: 20rpx;
  margin: 0 30rpx 30rpx;

  .stat-card {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 24rpx;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    .stat-icon {
      width: 64rpx;
      height: 64rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      background: #f5f5f5;
    }

    .stat-info {
      display: flex;
      flex-direction: column;
      gap: 4rpx;

      .stat-value {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }

    &.online .stat-icon {
      background: rgba(82, 196, 26, 0.1);
    }

    &.offline .stat-icon {
      background: rgba(255, 77, 79, 0.1);
    }

    &.fault .stat-icon {
      background: rgba(250, 140, 22, 0.1);
    }
  }
}

// 搜索框
.search-bar {
  margin: 0 30rpx 30rpx;
}

// 批量操作
.batch-actions {
  display: flex;
  gap: 20rpx;
  margin: 0 30rpx 30rpx;

  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    height: 72rpx;
    border-radius: 12rpx;
    font-size: 28rpx;
    border: none;

    &.open-all {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }

    &.lock-all {
      background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      color: #fff;
    }

    &.reset-all {
      background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
      color: #fff;
    }
  }
}

// 门禁列表容器
.door-list-container {
  flex: 1;
  padding: 0 30rpx 30rpx;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  .empty-image {
    width: 320rpx;
    height: 320rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 32rpx;
    color: #999;
    margin-bottom: 16rpx;
  }

  .empty-hint {
    font-size: 28rpx;
    color: #bbb;
  }
}

// 门禁卡片
.door-card {
  position: relative;
  margin-bottom: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);

  // 状态标识
  .door-status {
    position: absolute;
    top: 30rpx;
    right: 30rpx;
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    font-size: 24rpx;

    .status-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
    }

    &.status-online {
      background: rgba(82, 196, 26, 0.1);
      color: #52c41a;

      .status-dot {
        background: #52c41a;
      }
    }

    &.status-offline {
      background: rgba(255, 77, 79, 0.1);
      color: #ff4d4f;

      .status-dot {
        background: #ff4d4f;
      }
    }

    &.status-fault {
      background: rgba(250, 140, 22, 0.1);
      color: #fa8c16;

      .status-dot {
        background: #fa8c16;
      }
    }
  }

  // 设备信息
  .door-info {
    margin-bottom: 30rpx;

    .door-header {
      margin-bottom: 20rpx;

      .door-title-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-bottom: 12rpx;

        .door-icon {
          flex-shrink: 0;
        }

        .door-name {
          flex: 1;
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
        }
      }

      .door-code {
        font-size: 24rpx;
        color: #999;
      }
    }

    .door-details {
      display: flex;
      flex-direction: column;
      gap: 12rpx;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
        font-size: 26rpx;
        color: #666;
      }
    }
  }

  // 控制按钮
  .door-controls {
    display: flex;
    gap: 16rpx;

    .control-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      height: 72rpx;
      border-radius: 12rpx;
      font-size: 28rpx;
      border: none;

      &.open-btn {
        background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        color: #fff;
      }

      &.lock-btn {
        background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        color: #fff;
      }

      &.detail-btn {
        background: #f5f5f5;
        color: #1890ff;
      }

      &[disabled] {
        opacity: 0.5;
      }
    }
  }

  // 故障信息
  .fault-info {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-top: 20rpx;
    padding: 16rpx;
    background: rgba(255, 77, 79, 0.05);
    border-radius: 8rpx;

    .fault-text {
      flex: 1;
      font-size: 24rpx;
      color: #ff4d4f;
    }
  }
}

// 加载容器
.loading-container {
  padding: 60rpx 0;
}
</style>
