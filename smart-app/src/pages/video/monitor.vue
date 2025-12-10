<template>
  <view class="video-monitor-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="nav-title">实时监控</text>
        <view class="nav-actions">
          <view class="alarm-badge" @click="goToAlarms" v-if="alarmCount > 0">
            <text class="alarm-icon">🔔</text>
            <view class="badge-dot" v-if="alarmCount > 0">{{ alarmCount }}</view>
          </view>
          <text class="refresh-icon" @click="refreshData">🔄</text>
        </view>
      </view>
    </view>

    <!-- 设备统计卡片 -->
    <view class="stats-section">
      <view class="stats-grid">
        <view class="stat-card online">
          <view class="stat-icon">📹</view>
          <view class="stat-info">
            <text class="stat-value">{{ statistics.onlineCount || 0 }}</text>
            <text class="stat-label">在线设备</text>
          </view>
        </view>
        <view class="stat-card total">
          <view class="stat-icon">📊</view>
          <view class="stat-info">
            <text class="stat-value">{{ statistics.totalCount || 0 }}</text>
            <text class="stat-label">总设备数</text>
          </view>
        </view>
        <view class="stat-card recording">
          <view class="stat-icon">🎬</view>
          <view class="stat-info">
            <text class="stat-value">{{ statistics.recordingCount || 0 }}</text>
            <text class="stat-label">录像中</text>
          </view>
        </view>
        <view class="stat-card alarm">
          <view class="stat-icon">⚠️</view>
          <view class="stat-info">
            <text class="stat-value">{{ statistics.alarmCount || 0 }}</text>
            <text class="stat-label">告警数</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 视频预览区域 -->
    <view class="video-preview-section">
      <view class="section-header">
        <text class="section-title">视频预览</text>
        <view class="layout-selector">
          <text
            :class="['layout-btn', { active: currentLayout === '1x1' }]"
            @click="switchLayout('1x1')"
          >单画面</text>
          <text
            :class="['layout-btn', { active: currentLayout === '2x2' }]"
            @click="switchLayout('2x2')"
          >四画面</text>
        </view>
      </view>

      <!-- 单画面布局 -->
      <view v-if="currentLayout === '1x1'" class="video-container single">
        <view class="video-wrapper">
          <video
            v-if="currentMonitor && currentMonitor.streamUrl"
            :src="currentMonitor.streamUrl"
            :controls="true"
            :autoplay="true"
            :show-center-play-btn="false"
            :enable-progress-gesture="false"
            class="video-player"
            @error="handleVideoError"
            @fullscreenchange="handleFullscreenChange"
          ></video>
          <view v-else class="video-placeholder">
            <text class="placeholder-icon">📹</text>
            <text class="placeholder-text">暂无视频</text>
          </view>

          <!-- 视频信息覆盖层 -->
          <view class="video-overlay" v-if="currentMonitor">
            <view class="overlay-top">
              <text class="device-name">{{ currentMonitor.deviceName }}</text>
              <view :class="['status-badge', currentMonitor.deviceStatus]">
                {{ getStatusText(currentMonitor.deviceStatus) }}
              </view>
            </view>
            <view class="overlay-bottom">
              <text class="quality-info">{{ currentMonitor.quality }}</text>
              <text class="latency-info">延迟: {{ currentMonitor.latency }}ms</text>
            </view>
          </view>
        </view>

        <!-- 云台控制按钮 -->
        <view class="ptz-controls" v-if="currentMonitor && currentMonitor.ptzEnabled">
          <view class="ptz-grid">
            <view class="ptz-row">
              <view class="ptz-btn empty"></view>
              <view class="ptz-btn" @touchstart="handlePTZ('UP')" @touchend="handlePTZStop">
                <text>↑</text>
              </view>
              <view class="ptz-btn empty"></view>
            </view>
            <view class="ptz-row">
              <view class="ptz-btn" @touchstart="handlePTZ('LEFT')" @touchend="handlePTZStop">
                <text>←</text>
              </view>
              <view class="ptz-btn center" @click="showPTZMenu">
                <text>⊙</text>
              </view>
              <view class="ptz-btn" @touchstart="handlePTZ('RIGHT')" @touchend="handlePTZStop">
                <text>→</text>
              </view>
            </view>
            <view class="ptz-row">
              <view class="ptz-btn empty"></view>
              <view class="ptz-btn" @touchstart="handlePTZ('DOWN')" @touchend="handlePTZStop">
                <text>↓</text>
              </view>
              <view class="ptz-btn empty"></view>
            </view>
          </view>
          <view class="zoom-controls">
            <view class="zoom-btn" @touchstart="handlePTZ('ZOOM_IN')" @touchend="handlePTZStop">
              <text>+</text>
            </view>
            <view class="zoom-btn" @touchstart="handlePTZ('ZOOM_OUT')" @touchend="handlePTZStop">
              <text>-</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 四画面布局 -->
      <view v-else-if="currentLayout === '2x2'" class="video-container grid">
        <view
          v-for="(monitor, index) in displayMonitors.slice(0, 4)"
          :key="monitor.deviceId"
          class="video-grid-item"
          @click="selectMonitor(monitor)"
        >
          <video
            v-if="monitor.streamUrl"
            :src="monitor.streamUrl"
            :controls="false"
            :autoplay="true"
            :muted="index > 0"
            :show-center-play-btn="false"
            class="video-player-mini"
          ></video>
          <view v-else class="video-placeholder-mini">
            <text>📹</text>
          </view>
          <view class="video-info-mini">
            <text class="device-name-mini">{{ monitor.deviceName }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷操作栏 -->
    <view class="quick-actions">
      <view class="action-row">
        <button class="action-btn" @click="handleQuickAction('SNAPSHOT')">
          <text class="action-icon">📸</text>
          <text class="action-text">截图</text>
        </button>
        <button class="action-btn" @click="handleQuickAction('START_RECORD')">
          <text class="action-icon">🎬</text>
          <text class="action-text">录像</text>
        </button>
        <button class="action-btn" @click="goToPlayback">
          <text class="action-icon">⏮️</text>
          <text class="action-text">回放</text>
        </button>
        <button class="action-btn" @click="goToDeviceList">
          <text class="action-icon">📋</text>
          <text class="action-text">设备</text>
        </button>
      </view>
    </view>

    <!-- 设备列表（下拉选择） -->
    <view class="device-list-section" v-if="showDeviceList">
      <view class="device-list-header">
        <text class="list-title">选择设备</text>
        <text class="close-btn" @click="showDeviceList = false">✕</text>
      </view>
      <scroll-view class="device-scroll" scroll-y>
        <view
          v-for="device in devices"
          :key="device.deviceId"
          :class="['device-item', { selected: currentMonitor && currentMonitor.deviceId === device.deviceId }]"
          @click="switchDevice(device)"
        >
          <view :class="['device-status', device.status]"></view>
          <view class="device-info">
            <text class="device-name">{{ device.deviceName }}</text>
            <text class="device-location">{{ device.location }}</text>
          </view>
          <text class="device-arrow">›</text>
        </view>
      </scroll-view>
    </view>

    <!-- 云台预置位菜单 -->
    <view class="ptz-menu-modal" v-if="showPTZMenuModal" @click="showPTZMenuModal = false">
      <view class="ptz-menu-content" @click.stop>
        <text class="menu-title">预置位</text>
        <view class="preset-grid">
          <button
            v-for="preset in presetList"
            :key="preset.num"
            class="preset-btn"
            @click="gotoPreset(preset.num)"
          >
            {{ preset.name }}
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import videoApi from '@/api/business/video/video-api'
import { useWebSocket } from '@/utils/websocket'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

// 页面状态
const loading = ref(false)
const showDeviceList = ref(false)
const showPTZMenuModal = ref(false)
const currentLayout = ref('1x1')

// 数据
const statistics = reactive({
  onlineCount: 0,
  totalCount: 0,
  recordingCount: 0,
  alarmCount: 0
})
const devices = ref([])
const currentMonitor = ref(null)
const displayMonitors = ref([])
const presetList = ref([])
const alarmCount = ref(0)

// WebSocket
const wsClient = ref(null)

// 页面生命周期
onMounted(() => {
  init()
})

onUnmounted(() => {
  cleanup()
})

onShow(() => {
  // 页面显示时刷新数据
  refreshData()
})

onPullDownRefresh(() => {
  refreshData()
  uni.stopPullDownRefresh()
})

// 方法实现
const init = async () => {
  await loadDevices()
  await loadStatistics()
  await loadAlarmCount()
  initWebSocket()
}

const refreshData = async () => {
  await Promise.all([
    loadDevices(),
    loadStatistics(),
    loadAlarmCount()
  ])
  uni.showToast({ title: '刷新成功', icon: 'success' })
}

    // 加载设备列表
    const loadDevices = async () => {
      try {
        loading.value = true
        const res = await videoApi.getMobileDevices(true)
        if (res.code === 1 && res.data) {
          devices.value = res.data.devices || []
          statistics.onlineCount = res.data.onlineCount || 0
          statistics.totalCount = res.data.totalCount || 0

          // 默认选择第一个设备
          if (devices.value.length > 0 && !currentMonitor.value) {
            await switchDevice(devices.value[0])
          }
        }
      } catch (error) {
        console.error('加载设备列表失败:', error)
        uni.showToast({ title: '加载设备失败', icon: 'none' })
      } finally {
        loading.value = false
      }
    }

    // 加载统计信息
    const loadStatistics = async () => {
      try {
        const res = await videoApi.getDeviceStatistics()
        if (res.code === 1 && res.data) {
          Object.assign(statistics, res.data)
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
      }
    }

    // 加载告警数量
    const loadAlarmCount = async () => {
      try {
        const res = await videoApi.getAlarmOverview()
        if (res.code === 1 && res.data) {
          alarmCount.value = res.data.totalCount || 0
        }
      } catch (error) {
        console.error('加载告警数量失败:', error)
      }
    }

    // 初始化WebSocket
    const initWebSocket = () => {
      const wsUrl = `wss://${import.meta.env.VITE_APP_API_URL.replace('https://', '')}/ws/video/monitor`

      wsClient.value = useWebSocket({
        url: wsUrl,
        heartbeatInterval: 30000,
        reconnectInterval: 3000
      })

      // 订阅设备状态更新
      wsClient.value.subscribe('device_status', handleDeviceStatusUpdate)

      // 订阅告警
      wsClient.value.subscribe('alarm', handleAlarmUpdate)

      // 连接WebSocket
      wsClient.value.connect()
    }

    // 处理设备状态更新
    const handleDeviceStatusUpdate = (message) => {
      const { deviceId, status } = message.data
      const device = devices.value.find(d => d.deviceId === deviceId)
      if (device) {
        device.status = status
      }

      // 更新当前监控设备状态
      if (currentMonitor.value && currentMonitor.value.deviceId === deviceId) {
        currentMonitor.value.deviceStatus = status
      }
    }

    // 处理告警更新
    const handleAlarmUpdate = (message) => {
      alarmCount.value++

      // 震动提醒
      uni.vibrateShort()

      // Toast提示
      uni.showToast({
        title: '新告警',
        icon: 'none',
        duration: 1500
      })
    }

    // 切换设备
    const switchDevice = async (device) => {
      try {
        loading.value = true

        // 获取网络类型
        const networkInfo = await getNetworkType()

        // 获取监控画面
        const res = await videoApi.getMobileMonitor(
          device.deviceId,
          'SUB',
          networkInfo === 'wifi' ? '720p' : '480p'
        )

        if (res.code === 1 && res.data) {
          currentMonitor.value = res.data
          showDeviceList.value = false

          // 加载预置位列表
          if (device.ptzEnabled) {
            await loadPresetList(device.deviceId)
          }
        }
      } catch (error) {
        console.error('切换设备失败:', error)
        uni.showToast({ title: '切换失败', icon: 'none' })
      } finally {
        loading.value = false
      }
    }

    // 切换布局
    const switchLayout = async (layout) => {
      currentLayout.value = layout

      if (layout === '2x2') {
        // 加载4画面
        await load4Monitors()
      }
    }

    // 加载4画面监控
    const load4Monitors = async () => {
      try {
        loading.value = true

        // 取前4个在线设备
        const deviceIds = devices.value
          .filter(d => d.status === 'online')
          .slice(0, 4)
          .map(d => d.deviceId)

        if (deviceIds.length === 0) {
          uni.showToast({ title: '没有在线设备', icon: 'none' })
          return
        }

        const res = await videoApi.getMultiMonitor({
          deviceIds,
          layout: '2x2'
        })

        if (res.code === 1 && res.data) {
          displayMonitors.value = res.data.monitors || []
        }
      } catch (error) {
        console.error('加载多画面失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        loading.value = false
      }
    }

    // 选择监控（从四画面）
    const selectMonitor = (monitor) => {
      currentMonitor.value = monitor
      currentLayout.value = '1x1'
    }

    // 云台控制
    const handlePTZ = async (action) => {
      if (!currentMonitor.value) return

      try {
        await videoApi.mobilePTZControl(currentMonitor.value.deviceId, {
          action,
          speed: 50
        })
      } catch (error) {
        console.error('云台控制失败:', error)
      }
    }

    // 云台停止
    const handlePTZStop = () => {
      // PTZ控制停止
    }

    // 显示云台菜单
    const showPTZMenu = () => {
      showPTZMenuModal.value = true
    }

    // 加载预置位列表
    const loadPresetList = async (deviceId) => {
      try {
        const res = await videoApi.getPresetList(deviceId)
        if (res.code === 1 && res.data) {
          presetList.value = res.data
        }
      } catch (error) {
        console.error('加载预置位失败:', error)
      }
    }

    // 跳转到预置位
    const gotoPreset = async (presetNum) => {
      if (!currentMonitor.value) return

      try {
        await videoApi.gotoPreset(currentMonitor.value.deviceId, presetNum)
        showPTZMenuModal.value = false
        uni.showToast({ title: '预置位调用成功', icon: 'success' })
      } catch (error) {
        console.error('调用预置位失败:', error)
        uni.showToast({ title: '调用失败', icon: 'none' })
      }
    }

    // 快捷操作
    const handleQuickAction = async (action) => {
      if (!currentMonitor.value) {
        uni.showToast({ title: '请先选择设备', icon: 'none' })
        return
      }

      try {
        const res = await videoApi.quickAction(currentMonitor.value.deviceId, action)
        if (res.code === 1) {
          uni.showToast({ title: '操作成功', icon: 'success' })

          // 震动反馈
          uni.vibrateShort()
        }
      } catch (error) {
        console.error('快捷操作失败:', error)
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    }

  
    // 获取网络类型
    const getNetworkType = () => {
      return new Promise((resolve) => {
        uni.getNetworkType({
          success: (res) => {
            resolve(res.networkType)
          },
          fail: () => {
            resolve('unknown')
          }
        })
      })
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        online: '在线',
        offline: '离线',
        fault: '故障',
        recording: '录像中'
      }
      return statusMap[status] || '未知'
    }

    // 视频错误处理
    const handleVideoError = (e) => {
      console.error('视频播放错误:', e)
      uni.showToast({ title: '视频加载失败', icon: 'none' })
    }

    // 全屏变化处理
    const handleFullscreenChange = (e) => {
      console.log('全屏状态变化:', e)
    }

    // 跳转页面
    const goToAlarms = () => {
      uni.navigateTo({ url: '/pages/video/alert' })
    }

    const goToPlayback = () => {
      uni.navigateTo({ url: '/pages/video/playback' })
    }

    const goToDeviceList = () => {
      uni.navigateTo({ url: '/pages/video/device' })
    }

    // 清理资源
    const cleanup = () => {
      if (wsClient.value) {
        wsClient.value.disconnect()
      }
    }

  </script>

<style lang="scss" scoped>
.video-monitor-page {
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

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }

  .nav-actions {
    display: flex;
    align-items: center;
    gap: 32rpx;
  }

  .alarm-badge {
    position: relative;

    .alarm-icon {
      font-size: 40rpx;
    }

    .badge-dot {
      position: absolute;
      top: -8rpx;
      right: -8rpx;
      min-width: 32rpx;
      height: 32rpx;
      padding: 0 8rpx;
      background: #f5222d;
      color: #fff;
      font-size: 20rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .refresh-icon {
    font-size: 40rpx;
  }
}

.stats-section {
  padding: 24rpx 32rpx;

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;
  }

  .stat-card {
    background: #fff;
    border-radius: 16rpx;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .stat-icon {
      font-size: 48rpx;
      margin-bottom: 16rpx;
    }

    .stat-info {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .stat-value {
      font-size: 40rpx;
      font-weight: 600;
      color: #1890ff;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 22rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }
}

.video-preview-section {
  margin: 0 32rpx 24rpx;

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

  .layout-selector {
    display: flex;
    gap: 16rpx;
  }

  .layout-btn {
    padding: 8rpx 24rpx;
    background: #f0f0f0;
    border-radius: 8rpx;
    font-size: 24rpx;
    color: rgba(0, 0, 0, 0.65);

    &.active {
      background: #1890ff;
      color: #fff;
    }
  }
}

.video-container {
  &.single {
    .video-wrapper {
      position: relative;
      width: 100%;
      height: 400rpx;
      background: #000;
      border-radius: 16rpx;
      overflow: hidden;

      .video-player {
        width: 100%;
        height: 100%;
      }

      .video-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        .placeholder-icon {
          font-size: 80rpx;
          margin-bottom: 16rpx;
        }

        .placeholder-text {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.65);
        }
      }

      .video-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        pointer-events: none;

        .overlay-top {
          position: absolute;
          top: 16rpx;
          left: 16rpx;
          right: 16rpx;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }

        .device-name {
          font-size: 28rpx;
          color: #fff;
          text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
        }

        .status-badge {
          padding: 4rpx 16rpx;
          border-radius: 8rpx;
          font-size: 22rpx;
          color: #fff;

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

        .overlay-bottom {
          position: absolute;
          bottom: 16rpx;
          left: 16rpx;
          right: 16rpx;
          display: flex;
          justify-content: space-between;

          .quality-info,
          .latency-info {
            font-size: 22rpx;
            color: #fff;
            text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
          }
        }
      }
    }

    .ptz-controls {
      margin-top: 24rpx;

      .ptz-grid {
        display: flex;
        flex-direction: column;
        gap: 16rpx;
        margin-bottom: 16rpx;
      }

      .ptz-row {
        display: flex;
        justify-content: center;
        gap: 16rpx;
      }

      .ptz-btn {
        width: 120rpx;
        height: 120rpx;
        background: #fff;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 48rpx;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

        &.empty {
          visibility: hidden;
        }

        &.center {
          background: #1890ff;
          color: #fff;
        }

        &:active:not(.empty):not(.center) {
          background: #e6f7ff;
          transform: scale(0.95);
        }

        &.center:active {
          background: #096dd9;
        }
      }

      .zoom-controls {
        display: flex;
        justify-content: center;
        gap: 32rpx;

        .zoom-btn {
          width: 120rpx;
          height: 120rpx;
          background: #fff;
          border-radius: 16rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 64rpx;
          font-weight: 600;
          box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

          &:active {
            background: #e6f7ff;
            transform: scale(0.95);
          }
        }
      }
    }
  }

  &.grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16rpx;

    .video-grid-item {
      position: relative;
      height: 300rpx;
      background: #000;
      border-radius: 16rpx;
      overflow: hidden;

      .video-player-mini {
        width: 100%;
        height: 100%;
      }

      .video-placeholder-mini {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 64rpx;
      }

      .video-info-mini {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 16rpx;
        background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));

        .device-name-mini {
          font-size: 24rpx;
          color: #fff;
          text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
        }
      }
    }
  }
}

.quick-actions {
  padding: 0 32rpx 32rpx;

  .action-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;
  }

  .action-btn {
    background: #fff;
    border: none;
    border-radius: 16rpx;
    padding: 24rpx 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .action-icon {
      font-size: 48rpx;
      margin-bottom: 8rpx;
    }

    .action-text {
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.65);
    }

    &:active {
      background: #f0f0f0;
      transform: scale(0.95);
    }
  }
}

.device-list-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-height: 70vh;
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.1);
  z-index: 100;

  .device-list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx;
    border-bottom: 1px solid #e8e8e8;

    .list-title {
      font-size: 32rpx;
      font-weight: 600;
    }

    .close-btn {
      font-size: 48rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }

  .device-scroll {
    max-height: 60vh;

    .device-item {
      display: flex;
      align-items: center;
      padding: 24rpx 32rpx;
      border-bottom: 1px solid #f0f0f0;

      &.selected {
        background: #e6f7ff;
      }

      &:active {
        background: #f5f5f5;
      }

      .device-status {
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        margin-right: 24rpx;

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

      .device-info {
        flex: 1;
        display: flex;
        flex-direction: column;

        .device-name {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.85);
          margin-bottom: 8rpx;
        }

        .device-location {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .device-arrow {
        font-size: 40rpx;
        color: rgba(0, 0, 0, 0.25);
      }
    }
  }
}

.ptz-menu-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;

  .ptz-menu-content {
    width: 80%;
    max-width: 600rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 48rpx 32rpx;

    .menu-title {
      font-size: 32rpx;
      font-weight: 600;
      text-align: center;
      margin-bottom: 32rpx;
    }

    .preset-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16rpx;

      .preset-btn {
        background: #f0f0f0;
        border: none;
        border-radius: 12rpx;
        padding: 24rpx;
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);

        &:active {
          background: #1890ff;
          color: #fff;
        }
      }
    }
  }
}
</style>

