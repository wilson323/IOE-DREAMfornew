<template>
  <view class="video-playback-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">录像回放</text>
        <text class="download-btn" @click="downloadRecord" v-if="currentRecord">下载</text>
      </view>
    </view>

    <!-- 视频播放器 -->
    <view class="video-player-section">
      <video
        v-if="playbackUrl"
        :src="playbackUrl"
        :controls="true"
        :autoplay="false"
        :show-center-play-btn="true"
        class="video-player"
        @error="handleVideoError"
        @timeupdate="handleTimeUpdate"
      ></video>
      <view v-else class="video-placeholder">
        <text class="placeholder-icon">🎬</text>
        <text class="placeholder-text">请选择录像</text>
      </view>
    </view>

    <!-- 时间轴控制 -->
    <view class="timeline-section" v-if="currentRecord">
      <view class="timeline-header">
        <text class="current-time">{{ formatPlayTime(currentTime) }}</text>
        <text class="duration">{{ formatPlayTime(duration) }}</text>
      </view>

      <view class="timeline-bar">
        <slider
          :value="timelineProgress"
          :min="0"
          :max="100"
          :step="1"
          @changing="handleTimelineChanging"
          @change="handleTimelineChange"
          active-color="#1890ff"
          background-color="#e8e8e8"
        />
      </view>

      <view class="playback-controls">
        <button class="control-btn" @click="changeSpeed(-1)">
          <text>0.5x</text>
        </button>
        <button class="control-btn" @click="changeSpeed(1)">
          <text>1x</text>
        </button>
        <button class="control-btn" @click="changeSpeed(2)">
          <text>2x</text>
        </button>
        <button class="control-btn" @click="changeSpeed(4)">
          <text>4x</text>
        </button>
      </view>
    </view>

    <!-- 设备选择 -->
    <view class="device-selector">
      <text class="selector-label">选择设备</text>
      <picker
        mode="selector"
        :range="devices"
        range-key="deviceName"
        @change="onDeviceChange"
      >
        <view class="picker-value">
          {{ selectedDevice ? selectedDevice.deviceName : '请选择设备' }}
        </view>
      </picker>
    </view>

    <!-- 日期时间选择 -->
    <view class="datetime-selector">
      <view class="datetime-row">
        <text class="selector-label">开始时间</text>
        <picker
          mode="date"
          :value="startDate"
          @change="onStartDateChange"
        >
          <view class="picker-value">{{ startDate }}</view>
        </picker>
        <picker
          mode="time"
          :value="startTime"
          @change="onStartTimeChange"
        >
          <view class="picker-value">{{ startTime }}</view>
        </picker>
      </view>

      <view class="datetime-row">
        <text class="selector-label">结束时间</text>
        <picker
          mode="date"
          :value="endDate"
          @change="onEndDateChange"
        >
          <view class="picker-value">{{ endDate }}</view>
        </picker>
        <picker
          mode="time"
          :value="endTime"
          @change="onEndTimeChange"
        >
          <view class="picker-value">{{ endTime }}</view>
        </picker>
      </view>
    </view>

    <!-- 查询按钮 -->
    <view class="query-section">
      <button class="query-btn" @click="queryPlayback">
        查询录像
      </button>
    </view>

    <!-- 录像列表 -->
    <view class="record-list-section" v-if="recordList.length > 0">
      <view class="section-header">
        <text class="section-title">录像列表</text>
        <text class="record-count">共 {{ recordList.length }} 条</text>
      </view>

      <scroll-view class="record-scroll" scroll-y>
        <view
          v-for="record in recordList"
          :key="record.recordId"
          :class="['record-card', { selected: currentRecord && currentRecord.recordId === record.recordId }]"
          @click="playRecord(record)"
        >
          <view class="record-info">
            <text class="record-time">{{ formatRecordTime(record.startTime) }}</text>
            <text class="record-duration">时长: {{ record.duration }}s</text>
          </view>
          <view class="record-actions">
            <text class="play-icon">▶️</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import videoApi from '@/api/business/video/video-api'

// 页面状态
const loading = ref(false)

// 数据
const devices = ref([])
const selectedDevice = ref(null)
const recordList = ref([])
const currentRecord = ref(null)
const playbackUrl = ref('')
const currentTime = ref(0)
const duration = ref(0)
const playSpeed = ref(1)

// 日期时间
const now = new Date()
const startDate = ref(formatDate(now))
const startTime = ref('00:00')
const endDate = ref(formatDate(now))
const endTime = ref('23:59')

// 计算时间轴进度
const timelineProgress = computed(() => {
  if (duration.value === 0) return 0
  return (currentTime.value / duration.value) * 100
})

// 页面生命周期
onMounted(() => {
  init()
})

// 初始化
const init = async () => {
  await loadDevices()
}

// 加载设备列表
const loadDevices = async () => {
  try {
    const res = await videoApi.getMobileDevices(true)
    if (res.code === 1 && res.data) {
      devices.value = res.data.devices || []
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

// 查询录像
const queryPlayback = async () => {
  if (!selectedDevice.value) {
    uni.showToast({ title: '请选择设备', icon: 'none' })
    return
  }

  try {
    loading.value = true

    const params = {
      deviceId: selectedDevice.value.deviceId,
      startTime: `${startDate.value} ${startTime.value}:00`,
      endTime: `${endDate.value} ${endTime.value}:00`
    }

    const res = await videoApi.queryPlayback(params)

    if (res.code === 1 && res.data) {
      recordList.value = res.data

      if (recordList.value.length === 0) {
        uni.showToast({ title: '该时段无录像', icon: 'none' })
      }
    }
  } catch (error) {
    console.error('查询录像失败:', error)
    uni.showToast({ title: '查询失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 播放录像
const playRecord = async (record) => {
  try {
    currentRecord.value = record

    const res = await videoApi.getPlaybackUrl(
      selectedDevice.value.deviceId,
      record.recordId
    )

    if (res.code === 1 && res.data) {
      playbackUrl.value = res.data.streamUrl || res.data
      duration.value = record.duration || 0
    }
  } catch (error) {
    console.error('获取回放地址失败:', error)
    uni.showToast({ title: '播放失败', icon: 'none' })
  }
}

// 下载录像
const downloadRecord = async () => {
  if (!currentRecord.value) return

  try {
    const res = await videoApi.downloadRecord(currentRecord.value.recordId)

    if (res.code === 1 && res.data) {
      uni.showToast({ title: '下载已开始', icon: 'success' })
    }
  } catch (error) {
    console.error('下载录像失败:', error)
    uni.showToast({ title: '下载失败', icon: 'none' })
  }
}

// 设备选择change
const onDeviceChange = (e) => {
  const index = e.detail.value
  selectedDevice.value = devices.value[index]
  recordList.value = []
  currentRecord.value = null
  playbackUrl.value = ''
}

// 日期时间选择
const onStartDateChange = (e) => {
  startDate.value = e.detail.value
}

const onStartTimeChange = (e) => {
  startTime.value = e.detail.value
}

const onEndDateChange = (e) => {
  endDate.value = e.detail.value
}

const onEndTimeChange = (e) => {
  endTime.value = e.detail.value
}

// 时间更新
const handleTimeUpdate = (e) => {
  currentTime.value = e.detail.currentTime
  duration.value = e.detail.duration
}

// 时间轴拖动
const handleTimelineChanging = (e) => {
  // 拖动中
}

const handleTimelineChange = (e) => {
  const newTime = (duration.value * e.detail.value) / 100
  // 跳转到指定时间（需要video组件支持）
}

// 改变播放速度
const changeSpeed = (speed) => {
  playSpeed.value = speed
  uni.showToast({ title: `${speed}x 倍速`, icon: 'none' })
  // 设置播放速度（需要video组件支持）
}

// 视频错误处理
const handleVideoError = (e) => {
  console.error('视频播放错误:', e)
  uni.showToast({ title: '播放失败', icon: 'none' })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 格式化日期
function formatDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化播放时间
const formatPlayTime = (seconds) => {
  if (!seconds) return '00:00'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)

  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  } else {
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
}

// 格式化录像时间
const formatRecordTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style lang="scss" scoped>
.video-playback-page {
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

  .download-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.video-player-section {
  margin: 24rpx 32rpx;
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
      color: rgba(255, 255, 255, 0.65);
    }

    .placeholder-text {
      font-size: 28rpx;
      color: rgba(255, 255, 255, 0.65);
    }
  }
}

.timeline-section {
  margin: 0 32rpx 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;

  .timeline-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16rpx;

    .current-time,
    .duration {
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }

  .timeline-bar {
    margin-bottom: 24rpx;
  }

  .playback-controls {
    display: flex;
    gap: 16rpx;

    .control-btn {
      flex: 1;
      height: 64rpx;
      background: #f0f0f0;
      border: none;
      border-radius: 8rpx;
      font-size: 26rpx;
      color: rgba(0, 0, 0, 0.65);

      &:active {
        background: #1890ff;
        color: #fff;
      }
    }
  }
}

.device-selector,
.datetime-selector {
  margin: 0 32rpx 16rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
}

.device-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .selector-label {
    font-size: 28rpx;
    color: rgba(0, 0, 0, 0.85);
  }

  .picker-value {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.datetime-selector {
  .datetime-row {
    display: flex;
    align-items: center;
    height: 72rpx;

    &:not(:last-child) {
      border-bottom: 1px solid #f0f0f0;
      margin-bottom: 16rpx;
      padding-bottom: 16rpx;
    }

    .selector-label {
      width: 140rpx;
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.85);
    }

    .picker-value {
      flex: 1;
      font-size: 28rpx;
      color: #1890ff;
      text-align: center;
      padding: 8rpx 16rpx;
      background: #f0f0f0;
      border-radius: 8rpx;
      margin: 0 8rpx;
    }
  }
}

.query-section {
  padding: 0 32rpx 24rpx;

  .query-btn {
    width: 100%;
    height: 88rpx;
    background: #1890ff;
    border: none;
    border-radius: 8rpx;
    font-size: 32rpx;
    font-weight: 600;
    color: #fff;

    &:active {
      background: #096dd9;
    }
  }
}

.record-list-section {
  margin: 0 32rpx 32rpx;

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

  .record-count {
    font-size: 24rpx;
    color: rgba(0, 0, 0, 0.45);
  }
}

.record-scroll {
  max-height: 400rpx;
}

.record-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 12rpx;

  &.selected {
    background: #e6f7ff;
    border: 2rpx solid #1890ff;
  }

  &:active {
    background: #f5f5f5;
  }

  .record-info {
    display: flex;
    flex-direction: column;

    .record-time {
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 8rpx;
    }

    .record-duration {
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }

  .record-actions {
    .play-icon {
      font-size: 40rpx;
    }
  }
}
</style>

