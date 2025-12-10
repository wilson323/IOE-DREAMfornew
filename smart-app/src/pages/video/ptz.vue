<template>
  <view class="video-ptz-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">云台控制</text>
        <text class="device-name">{{ deviceInfo.deviceName }}</text>
      </view>
    </view>

    <!-- 视频预览（小窗） -->
    <view class="video-preview-mini">
      <video
        v-if="streamUrl"
        :src="streamUrl"
        :controls="false"
        :autoplay="true"
        :muted="false"
        class="video-player-mini"
      ></video>
      <view v-else class="video-placeholder-mini">
        <text>📹</text>
      </view>
    </view>

    <!-- 云台控制面板 -->
    <view class="ptz-control-panel">
      <text class="panel-title">方向控制</text>

      <!-- 方向控制盘 -->
      <view class="direction-pad">
        <view class="pad-row">
          <view class="pad-btn empty"></view>
          <view
            class="pad-btn"
            @touchstart="handlePTZStart('UP')"
            @touchend="handlePTZStop"
          >
            <text class="pad-icon">⬆️</text>
          </view>
          <view class="pad-btn empty"></view>
        </view>

        <view class="pad-row">
          <view
            class="pad-btn"
            @touchstart="handlePTZStart('LEFT')"
            @touchend="handlePTZStop"
          >
            <text class="pad-icon">⬅️</text>
          </view>
          <view class="pad-btn center" @click="stopAllMovement">
            <text class="pad-icon">⏹️</text>
          </view>
          <view
            class="pad-btn"
            @touchstart="handlePTZStart('RIGHT')"
            @touchend="handlePTZStop"
          >
            <text class="pad-icon">➡️</text>
          </view>
        </view>

        <view class="pad-row">
          <view class="pad-btn empty"></view>
          <view
            class="pad-btn"
            @touchstart="handlePTZStart('DOWN')"
            @touchend="handlePTZStop"
          >
            <text class="pad-icon">⬇️</text>
          </view>
          <view class="pad-btn empty"></view>
        </view>
      </view>

      <!-- 速度控制 -->
      <view class="speed-control">
        <text class="control-label">速度</text>
        <slider
          :value="ptzSpeed"
          :min="1"
          :max="100"
          :step="1"
          @change="handleSpeedChange"
          active-color="#1890ff"
          background-color="#e8e8e8"
        />
        <text class="control-value">{{ ptzSpeed }}</text>
      </view>
    </view>

    <!-- 变焦控制 -->
    <view class="zoom-control-panel">
      <text class="panel-title">变焦控制</text>

      <view class="zoom-buttons">
        <button
          class="zoom-btn zoom-in"
          @touchstart="handlePTZStart('ZOOM_IN')"
          @touchend="handlePTZStop"
        >
          <text class="zoom-icon">🔍+</text>
          <text class="zoom-text">放大</text>
        </button>
        <button
          class="zoom-btn zoom-out"
          @touchstart="handlePTZStart('ZOOM_OUT')"
          @touchend="handlePTZStop"
        >
          <text class="zoom-icon">🔍-</text>
          <text class="zoom-text">缩小</text>
        </button>
      </view>
    </view>

    <!-- 预置位控制 -->
    <view class="preset-control-panel">
      <text class="panel-title">预置位</text>

      <view class="preset-grid">
        <button
          v-for="preset in presetList"
          :key="preset.num"
          class="preset-btn"
          @click="gotoPreset(preset)"
        >
          <text class="preset-num">{{ preset.num }}</text>
          <text class="preset-name">{{ preset.name }}</text>
        </button>

        <!-- 空状态 -->
        <view v-if="presetList.length === 0" class="preset-empty">
          <text>暂无预置位</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions-panel">
      <button class="action-btn snapshot" @click="captureSnapshot">
        <text class="action-icon">📸</text>
        <text class="action-text">截图</text>
      </button>
      <button class="action-btn record" @click="toggleRecord">
        <text class="action-icon">{{ isRecording ? '⏹️' : '🎬' }}</text>
        <text class="action-text">{{ isRecording ? '停止录像' : '开始录像' }}</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import videoApi from '@/api/business/video/video-api'

// 页面参数
const deviceId = ref(0)

// 页面状态
const loading = ref(false)
const isRecording = ref(false)
const recordId = ref('')

// 设备信息
const deviceInfo = reactive({
  deviceId: 0,
  deviceName: '',
  ptzEnabled: false
})

// 云台控制
const ptzSpeed = ref(50)
const currentAction = ref('')
const ptzTimer = ref(null)

// 视频流
const streamUrl = ref('')

// 预置位列表
const presetList = ref([])

// 页面生命周期
onMounted(() => {
  init()
})

onUnmounted(() => {
  cleanup()
})

// 初始化
const init = async () => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  deviceId.value = currentPage.options.deviceId || 0

  if (deviceId.value) {
    await loadDeviceInfo()
    await loadPresetList()
    await loadVideoStream()
  }
}

// 加载设备信息
const loadDeviceInfo = async () => {
  try {
    const res = await videoApi.getMobileDeviceDetail(deviceId.value)
    if (res.code === 1 && res.data) {
      Object.assign(deviceInfo, res.data)
    }
  } catch (error) {
    console.error('加载设备信息失败:', error)
  }
}

// 加载预置位列表
const loadPresetList = async () => {
  try {
    const res = await videoApi.getPresetList(deviceId.value)
    if (res.code === 1 && res.data) {
      presetList.value = res.data
    }
  } catch (error) {
    console.error('加载预置位列表失败:', error)
  }
}

// 加载视频流
const loadVideoStream = async () => {
  try {
    const res = await videoApi.getMobileMonitor(deviceId.value, 'SUB', '480p')
    if (res.code === 1 && res.data) {
      streamUrl.value = res.data.streamUrl
    }
  } catch (error) {
    console.error('加载视频流失败:', error)
  }
}

// 云台控制开始
const handlePTZStart = async (action) => {
  currentAction.value = action

  try {
    await videoApi.mobilePTZControl(deviceId.value, {
      action,
      speed: ptzSpeed.value
    })

    // 持续发送控制命令（每500ms）
    ptzTimer.value = setInterval(async () => {
      await videoApi.mobilePTZControl(deviceId.value, {
        action,
        speed: ptzSpeed.value
      })
    }, 500)

  } catch (error) {
    console.error('云台控制失败:', error)
  }
}

// 云台控制停止
const handlePTZStop = () => {
  if (ptzTimer.value) {
    clearInterval(ptzTimer.value)
    ptzTimer.value = null
  }
  currentAction.value = ''
}

// 停止所有移动
const stopAllMovement = () => {
  handlePTZStop()
  uni.vibrateShort()
}

// 速度改变
const handleSpeedChange = (e) => {
  ptzSpeed.value = e.detail.value
}

// 跳转预置位
const gotoPreset = async (preset) => {
  try {
    await videoApi.gotoPreset(deviceId.value, preset.num)
    uni.showToast({ title: `跳转到${preset.name}`, icon: 'success' })
    uni.vibrateShort()
  } catch (error) {
    console.error('跳转预置位失败:', error)
    uni.showToast({ title: '跳转失败', icon: 'none' })
  }
}

// 截图
const captureSnapshot = async () => {
  try {
    const res = await videoApi.captureSnapshot(deviceId.value)
    if (res.code === 1) {
      uni.showToast({ title: '截图成功', icon: 'success' })
      uni.vibrateShort()
    }
  } catch (error) {
    console.error('截图失败:', error)
    uni.showToast({ title: '截图失败', icon: 'none' })
  }
}

// 切换录像
const toggleRecord = async () => {
  try {
    if (isRecording.value) {
      // 停止录像
      await videoApi.stopRecord(deviceId.value, recordId.value)
      isRecording.value = false
      recordId.value = ''
      uni.showToast({ title: '录像已停止', icon: 'success' })
    } else {
      // 开始录像
      const res = await videoApi.startRecord(deviceId.value)
      if (res.code === 1 && res.data) {
        isRecording.value = true
        recordId.value = res.data
        uni.showToast({ title: '录像已开始', icon: 'success' })
      }
    }

    uni.vibrateShort()
  } catch (error) {
    console.error('录像操作失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 清理资源
const cleanup = () => {
  if (ptzTimer.value) {
    clearInterval(ptzTimer.value)
  }
}
</script>

<style lang="scss" scoped>
.video-ptz-page {
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
    flex: 1;
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    text-align: center;
  }

  .device-name {
    font-size: 24rpx;
    color: rgba(0, 0, 0, 0.45);
  }
}

.video-preview-mini {
  margin: 24rpx 32rpx;
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
    font-size: 80rpx;
  }
}

.ptz-control-panel {
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;

  .panel-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    display: block;
    margin-bottom: 24rpx;
  }

  .direction-pad {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    margin-bottom: 32rpx;
  }

  .pad-row {
    display: flex;
    justify-content: center;
    gap: 20rpx;
  }

  .pad-btn {
    width: 140rpx;
    height: 140rpx;
    background: #f0f0f0;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

    &.empty {
      visibility: hidden;
    }

    &.center {
      background: #ff4d4f;

      &:active {
        background: #cf1322;
      }
    }

    &:not(.empty):not(.center):active {
      background: #1890ff;
      transform: scale(0.95);
    }

    .pad-icon {
      font-size: 56rpx;
    }
  }

  .speed-control {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .control-label {
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.65);
      width: 80rpx;
    }

    slider {
      flex: 1;
    }

    .control-value {
      font-size: 28rpx;
      color: #1890ff;
      font-weight: 600;
      width: 80rpx;
      text-align: right;
    }
  }
}

.zoom-control-panel {
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;

  .panel-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    display: block;
    margin-bottom: 24rpx;
  }

  .zoom-buttons {
    display: flex;
    gap: 24rpx;

    .zoom-btn {
      flex: 1;
      height: 120rpx;
      background: #f0f0f0;
      border: none;
      border-radius: 16rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

      &:active {
        background: #1890ff;

        .zoom-icon,
        .zoom-text {
          color: #fff;
        }
      }

      .zoom-icon {
        font-size: 48rpx;
        margin-bottom: 8rpx;
      }

      .zoom-text {
        font-size: 26rpx;
        color: rgba(0, 0, 0, 0.65);
      }
    }
  }
}

.preset-control-panel {
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;

  .panel-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    display: block;
    margin-bottom: 24rpx;
  }

  .preset-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;

    .preset-btn {
      height: 120rpx;
      background: #f0f0f0;
      border: none;
      border-radius: 12rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 0;

      &:active {
        background: #1890ff;

        .preset-num,
        .preset-name {
          color: #fff;
        }
      }

      .preset-num {
        font-size: 36rpx;
        font-weight: 600;
        color: #1890ff;
        margin-bottom: 8rpx;
      }

      .preset-name {
        font-size: 24rpx;
        color: rgba(0, 0, 0, 0.65);
      }
    }

    .preset-empty {
      grid-column: 1 / -1;
      text-align: center;
      padding: 48rpx 0;
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }
}

.quick-actions-panel {
  display: flex;
  gap: 24rpx;
  padding: 0 32rpx 32rpx;

  .action-btn {
    flex: 1;
    height: 120rpx;
    background: #fff;
    border: none;
    border-radius: 16rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    &.snapshot:active {
      background: #e6f7ff;
    }

    &.record:active {
      background: #fff1f0;
    }

    .action-icon {
      font-size: 48rpx;
      margin-bottom: 8rpx;
    }

    .action-text {
      font-size: 26rpx;
      color: rgba(0, 0, 0, 0.65);
    }
  }
}
</style>

