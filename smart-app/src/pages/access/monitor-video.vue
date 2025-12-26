<template>
  <view class="video-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">视频联动</text>
        </view>
        <view class="navbar-right">
          <view class="refresh-btn" @tap="refreshDevices">
            <uni-icons type="refreshempty" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 视频统计卡片 -->
      <view class="stats-cards">
        <view class="stat-card">
          <view class="stat-icon">
            <uni-icons type="videocam" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ videoStats.total }}</text>
            <text class="stat-label">摄像头总数</text>
          </view>
        </view>

        <view class="stat-card online">
          <view class="stat-icon">
            <uni-icons type="checkmarkempty" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ videoStats.online }}</text>
            <text class="stat-label">在线摄像头</text>
          </view>
        </view>

        <view class="stat-card recording">
          <view class="stat-icon">
            <uni-icons type="circle-filled" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ videoStats.recording }}</text>
            <text class="stat-label">正在录像</text>
          </view>
        </view>

        <view class="stat-card alert">
          <view class="stat-icon">
            <uni-icons type="notification" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ videoStats.alert }}</text>
            <text class="stat-label">告警摄像头</text>
          </view>
        </view>
      </view>

      <!-- 筛选标签 -->
      <view class="filter-tags">
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'all' }"
          @tap="selectFilter('all')"
        >
          全部 ({{ cameraList.length }})
        </view>
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'online' }"
          @tap="selectFilter('online')"
        >
          在线 ({{ onlineCount }})
        </view>
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'recording' }"
          @tap="selectFilter('recording')"
        >
          录像中 ({{ recordingCount }})
        </view>
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'alert' }"
          @tap="selectFilter('alert')"
        >
          告警 ({{ alertCount }})
        </view>
      </view>

      <!-- 区域筛选 -->
      <view class="area-filter">
        <scroll-view scroll-x class="area-scroll" show-scrollbar>
          <view
            class="area-item"
            :class="{ active: selectedArea === 'all' }"
            @tap="selectArea('all')"
          >
            全部区域
          </view>
          <view
            class="area-item"
            :class="{ active: selectedArea === area.areaId }"
            v-for="area in areaList"
            :key="area.areaId"
            @tap="selectArea(area.areaId)"
          >
            {{ area.areaName }}
          </view>
        </scroll-view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input
          class="search-input"
          type="text"
          placeholder="搜索摄像头名称、位置"
          v-model="searchKeyword"
          @input="onSearchInput"
          @confirm="onSearchConfirm"
        />
        <view class="search-btn" @tap="onSearchConfirm">搜索</view>
      </view>

      <!-- 摄像头列表 -->
      <scroll-view
        class="camera-list"
        scroll-y
        @scrolltolower="loadMoreCameras"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view
          class="camera-item"
          v-for="camera in filteredCameras"
          :key="camera.cameraId"
          @tap="viewCameraDetail(camera)"
        >
          <!-- 摄像头头部 -->
          <view class="camera-header">
            <view class="camera-name">{{ camera.cameraName }}</view>
            <view class="camera-status" :class="camera.status">
              <view class="status-dot"></view>
              <text class="status-text">{{ getStatusText(camera.status) }}</text>
            </view>
          </view>

          <!-- 视频预览 -->
          <view class="video-preview" @tap.stop="playVideo(camera)">
            <image
              v-if="camera.snapshot"
              :src="camera.snapshot"
              class="snapshot-image"
              mode="aspectFill"
            ></image>
            <view v-else class="snapshot-placeholder">
              <uni-icons type="videocam" size="60" color="#ddd"></uni-icons>
              <text class="placeholder-text">暂无画面</text>
            </view>

            <!-- 录像指示 -->
            <view class="recording-indicator" v-if="camera.isRecording">
              <view class="recording-dot"></view>
              <text class="recording-text">REC</text>
            </view>

            <!-- 告警标识 -->
            <view class="alert-badge" v-if="camera.hasAlert">
              <uni-icons type="notification" size="16" color="#fff"></uni-icons>
            </view>
          </view>

          <!-- 摄像头信息 -->
          <view class="camera-info">
            <view class="info-row">
              <uni-icons type="home" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ camera.areaName }}</text>
              <uni-icons type="location" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ camera.location }}</text>
            </view>

            <view class="info-row">
              <uni-icons type="settings" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ camera.ipAddress }}</text>
              <text class="info-divider">|</text>
              <text class="info-text">{{ camera.resolution }}</text>
            </view>

            <view class="info-row" v-if="camera.status === 'online' && camera.lastFrameTime">
              <uni-icons type="clock" size="14" color="#666"></uni-icons>
              <text class="info-text">最后更新: {{ formatTime(camera.lastFrameTime) }}</text>
            </view>
          </view>

          <!-- 快捷操作 -->
          <view class="camera-actions">
            <view class="action-btn" @tap.stop="playVideo(camera)">
              <uni-icons type="play-filled" size="20" color="#667eea"></uni-icons>
              <text class="action-text">播放</text>
            </view>
            <view class="action-btn" @tap.stop="captureSnapshot(camera)">
              <uni-icons type="camera" size="20" color="#667eea"></uni-icons>
              <text class="action-text">抓拍</text>
            </view>
            <view class="action-btn" @tap.stop="viewRecordings(camera)">
              <uni-icons type="folder" size="20" color="#667eea"></uni-icons>
              <text class="action-text">回放</text>
            </view>
            <view class="action-btn" @tap.stop="togglePTZ(camera)">
              <uni-icons type="settings" size="20" color="#667eea"></uni-icons>
              <text class="action-text">云台</text>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="hasMore">
          <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
          <text class="load-text">加载中...</text>
        </view>

        <!-- 没有更多 -->
        <view class="no-more" v-if="!hasMore && filteredCameras.length > 0">
          <text class="no-more-text">没有更多摄像头了</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="filteredCameras.length === 0 && !loading">
          <uni-icons type="videocam" size="80" color="#ddd"></uni-icons>
          <text class="empty-text">暂无摄像头</text>
        </view>
      </scroll-view>
    </view>

    <!-- 视频播放弹窗 -->
    <uni-popup ref="videoPopup" type="center" :safe-area="false">
      <view class="video-popup">
        <view class="popup-header">
          <text class="popup-title">{{ currentCamera.cameraName }}</text>
          <view class="close-btn" @tap="closeVideoPopup">
            <uni-icons type="close" size="20" color="#fff"></uni-icons>
          </view>
        </view>

        <view class="video-player">
          <video
            v-if="currentCamera.cameraId"
            :id="'video-' + currentCamera.cameraId"
            :src="currentCamera.streamUrl"
            :autoplay="true"
            :controls="true"
            :show-progress="true"
            :enable-progress-gesture="true"
            :orientation="horizontal"
            @error="onVideoError"
            @fullscreenchange="onFullscreenChange"
            class="video-component"
          ></video>
        </view>

        <view class="video-controls">
          <view class="control-btn" @tap="captureSnapshot">
            <uni-icons type="camera" size="20" color="#667eea"></uni-icons>
            <text class="control-text">抓拍</text>
          </view>
          <view class="control-btn" @tap="startRecording">
            <uni-icons type="circle-filled" size="20" :color="isRecording ? '#ff6b6b' : '#667eea'"></uni-icons>
            <text class="control-text">{{ isRecording ? '停止' : '录像' }}</text>
          </view>
          <view class="control-btn" @tap="toggleFullscreen">
            <uni-icons type="checkbox" size="20" color="#667eea"></uni-icons>
            <text class="control-text">全屏</text>
          </view>
          <view class="control-btn" @tap="openPTZControl">
            <uni-icons type="settings" size="20" color="#667eea"></uni-icons>
            <text class="control-text">云台</text>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 云台控制弹窗 -->
    <uni-popup ref="ptzPopup" type="bottom" :safe-area="false">
      <view class="ptz-popup">
        <view class="popup-header">
          <text class="popup-title">云台控制</text>
          <view class="close-btn" @tap="closePTZPopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="ptz-content">
          <!-- 方向控制 -->
          <view class="ptz-direction">
            <view class="direction-pad">
              <view class="direction-btn top" @tap="ptzControl('up')">
                <uni-icons type="up" size="24" color="#fff"></uni-icons>
              </view>
              <view class="direction-row">
                <view class="direction-btn left" @tap="ptzControl('left')">
                  <uni-icons type="left" size="24" color="#fff"></uni-icons>
                </view>
                <view class="direction-center"></view>
                <view class="direction-btn right" @tap="ptzControl('right')">
                  <uni-icons type="right" size="24" color="#fff"></uni-icons>
                </view>
              </view>
              <view class="direction-btn bottom" @tap="ptzControl('down')">
                <uni-icons type="down" size="24" color="#fff"></uni-icons>
              </view>
            </view>
          </view>

          <!-- 变倍控制 -->
          <view class="ptz-zoom">
            <view class="zoom-label">变倍</view>
            <view class="zoom-controls">
              <view class="zoom-btn" @tap="ptzControl('zoomIn')">
                <uni-icons type="plus" size="20" color="#fff"></uni-icons>
              </view>
              <view class="zoom-label-center">{{ zoomLevel }}x</view>
              <view class="zoom-btn" @tap="ptzControl('zoomOut')">
                <uni-icons type="minus" size="20" color="#fff"></uni-icons>
              </view>
            </view>
          </view>

          <!-- 预置位 -->
          <view class="ptz-preset">
            <view class="preset-label">预置位</view>
            <view class="preset-list">
              <view
                class="preset-item"
                v-for="preset in ptzPresets"
                :key="preset.id"
                @tap="ptzPreset(preset.id)"
              >
                <text class="preset-text">{{ preset.name }}</text>
              </view>
            </view>
          </view>

          <!-- 速度控制 -->
          <view class="ptz-speed">
            <view class="speed-label">云台速度</view>
            <slider
              class="speed-slider"
              :value="ptzSpeed"
              :min="1"
              :max="10"
              :step="1"
              @change="onSpeedChange"
              activeColor="#667eea"
              backgroundColor="#e0e0e0"
            />
            <view class="speed-value">{{ ptzSpeed }}</view>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 抓拍结果弹窗 -->
    <uni-popup ref="snapshotPopup" type="center">
      <view class="snapshot-popup">
        <view class="popup-header">
          <text class="popup-title">抓拍结果</text>
          <view class="close-btn" @tap="closeSnapshotPopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="snapshot-content">
          <image v-if="snapshotImage" :src="snapshotImage" class="snapshot-result" mode="widthFix"></image>
          <view v-else class="snapshot-loading">
            <uni-icons type="spinner-cycle" size="40" color="#667eea"></uni-icons>
            <text class="loading-text">抓拍中...</text>
          </view>
        </view>

        <view class="snapshot-actions">
          <view class="snapshot-btn secondary" @tap="closeSnapshotPopup">
            <text>关闭</text>
          </view>
          <view class="snapshot-btn primary" @tap="saveSnapshot" v-if="snapshotImage">
            <text>保存到相册</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 视频统计
const videoStats = reactive({
  total: 36,
  online: 32,
  recording: 18,
  alert: 2
})

// 摄像头列表
const cameraList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)

// 筛选条件
const selectedFilter = ref('all')
const selectedArea = ref('all')
const searchKeyword = ref('')

// 区域列表
const areaList = ref([])

// 当前摄像头
const currentCamera = ref({})
const isRecording = ref(false)

// 云台控制
const ptzSpeed = ref(5)
const zoomLevel = ref(1)
const ptzPresets = ref([
  { id: 1, name: '预置位1' },
  { id: 2, name: '预置位2' },
  { id: 3, name: '预置位3' },
  { id: 4, name: '预置位4' },
  { id: 5, name: '预置位5' },
  { id: 6, name: '预置位6' }
])

// 抓拍
const snapshotImage = ref('')

// 计算在线数量
const onlineCount = computed(() => {
  return cameraList.value.filter(c => c.status === 'online').length
})

// 计算录像中数量
const recordingCount = computed(() => {
  return cameraList.value.filter(c => c.isRecording).length
})

// 计算告警数量
const alertCount = computed(() => {
  return cameraList.value.filter(c => c.hasAlert).length
})

// 筛选后的摄像头列表
const filteredCameras = computed(() => {
  let result = cameraList.value

  // 状态筛选
  if (selectedFilter.value === 'online') {
    result = result.filter(c => c.status === 'online')
  } else if (selectedFilter.value === 'recording') {
    result = result.filter(c => c.isRecording)
  } else if (selectedFilter.value === 'alert') {
    result = result.filter(c => c.hasAlert)
  }

  // 区域筛选
  if (selectedArea.value !== 'all') {
    result = result.filter(c => c.areaId === selectedArea.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(c =>
      c.cameraName.toLowerCase().includes(keyword) ||
      c.location.toLowerCase().includes(keyword) ||
      c.areaName.toLowerCase().includes(keyword)
    )
  }

  return result
})

onMounted(() => {
  // 获取系统信息
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  // 加载数据
  loadAreaList()
  loadCameraList()
  startRealTimeUpdate()
})

onUnmounted(() => {
  stopRealTimeUpdate()
})

// 实时更新定时器
let updateTimer = null

const startRealTimeUpdate = () => {
  updateTimer = setInterval(() => {
    loadCameraList(true)
  }, 10000) // 10秒更新一次
}

const stopRealTimeUpdate = () => {
  if (updateTimer) {
    clearInterval(updateTimer)
    updateTimer = null
  }
}

// 加载区域列表
const loadAreaList = () => {
  areaList.value = [
    { areaId: '1', areaName: 'A栋1楼大厅' },
    { areaId: '2', areaName: 'A栋2楼办公区' },
    { areaId: '3', areaName: 'B栋会议室' },
    { areaId: '4', areaName: 'C栋餐厅' },
    { areaId: '5', areaName: 'D栋仓库' }
  ]
}

// 加载摄像头列表
const loadCameraList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))

    // 模拟数据
    const mockData = generateMockCameras(currentPage.value)

    if (isRefresh) {
      cameraList.value = mockData
      currentPage.value = 1
    } else {
      cameraList.value.push(...mockData)
    }

    hasMore.value = mockData.length >= 20
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 生成模拟数据
const generateMockCameras = (page) => {
  const statuses = ['online', 'offline', 'error']
  const resolutions = ['1080P', '2K', '4K']

  const cameras = []
  for (let i = 0; i < 20; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const isOnline = status === 'online'

    cameras.push({
      cameraId: `CAM${Date.now()}${i}`,
      cameraName: `摄像头-${String(i + 1).padStart(3, '0')}`,
      status: status,
      areaId: String(Math.floor(Math.random() * 5) + 1),
      areaName: ['A栋1楼大厅', 'A栋2楼办公区', 'B栋会议室', 'C栋餐厅', 'D栋仓库'][Math.floor(Math.random() * 5)],
      location: ['主入口', '侧门', '后门', '大厅', '走廊'][Math.floor(Math.random() * 5)],
      ipAddress: `192.168.1.${100 + i}`,
      resolution: resolutions[Math.floor(Math.random() * resolutions.length)],
      isRecording: isOnline && Math.random() > 0.5,
      hasAlert: isOnline && Math.random() > 0.9,
      snapshot: isOnline ? 'https://picsum.photos/400/300?random=' + i : '',
      streamUrl: isOnline ? 'rtmp://example.com/live/' + i : '',
      lastFrameTime: isOnline ? Date.now() - Math.random() * 60000 : 0
    })
  }

  return cameras
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadCameraList(true)
}

// 加载更多
const loadMoreCameras = () => {
  if (!hasMore.value || loading.value) return
  currentPage.value++
  loadCameraList()
}

// 搜索
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
}

const onSearchConfirm = () => {
  loadCameraList(true)
}

// 筛选
const selectFilter = (filter) => {
  selectedFilter.value = filter
}

const selectArea = (areaId) => {
  selectedArea.value = areaId
}

// 刷新设备
const refreshDevices = () => {
  loadCameraList(true)
}

// 查看摄像头详情
const viewCameraDetail = (camera) => {
  uni.navigateTo({
    url: `/pages/access/camera-detail?cameraId=${camera.cameraId}`
  })
}

// 播放视频
const playVideo = (camera) => {
  if (camera.status !== 'online') {
    uni.showToast({
      title: '摄像头离线',
      icon: 'none'
    })
    return
  }

  currentCamera.value = camera
  uni.$emit('openVideoPopup')
}

// 关闭视频弹窗
const closeVideoPopup = () => {
  uni.$emit('closeVideoPopup')
  isRecording.value = false
}

// 视频错误处理
const onVideoError = (e) => {
  uni.showToast({
    title: '视频加载失败',
    icon: 'none'
  })
}

// 全屏切换
const onFullscreenChange = (e) => {
  // 处理全屏切换
}

const toggleFullscreen = () => {
  const videoContext = uni.createVideoContext('video-' + currentCamera.value.cameraId)
  videoContext.requestFullScreen({
    direction: 90
  })
}

// 抓拍
const captureSnapshot = (camera) => {
  if (camera) {
    currentCamera.value = camera
  }

  snapshotImage.value = ''
  uni.$emit('openSnapshotPopup')

  // 模拟抓拍
  setTimeout(() => {
    snapshotImage.value = currentCamera.value.snapshot
  }, 1000)
}

// 关闭抓拍弹窗
const closeSnapshotPopup = () => {
  uni.$emit('closeSnapshotPopup')
}

// 保存抓拍
const saveSnapshot = () => {
  uni.showToast({
    title: '已保存到相册',
    icon: 'success'
  })
  closeSnapshotPopup()
}

// 开始录像
const startRecording = () => {
  isRecording.value = !isRecording.value

  uni.showToast({
    title: isRecording.value ? '开始录像' : '停止录像',
    icon: 'success'
  })
}

// 查看回放
const viewRecordings = (camera) => {
  uni.navigateTo({
    url: `/pages/access/recording-list?cameraId=${camera.cameraId}`
  })
}

// 打开云台控制
const togglePTZ = (camera) => {
  if (camera.status !== 'online') {
    uni.showToast({
      title: '摄像头离线',
      icon: 'none'
    })
    return
  }

  currentCamera.value = camera
  uni.$emit('openPTZPopup')
}

const openPTZControl = () => {
  uni.$emit('openPTZPopup')
}

// 关闭云台弹窗
const closePTZPopup = () => {
  uni.$emit('closePTZPopup')
}

// 云台控制
const ptzControl = (direction) => {
  const actions = {
    up: '上移',
    down: '下移',
    left: '左移',
    right: '右移',
    zoomIn: '放大',
    zoomOut: '缩小'
  }

  uni.showToast({
    title: `云台${actions[direction]}`,
    icon: 'none'
  })

  // 调用云台控制API
}

// 云台速度
const onSpeedChange = (e) => {
  ptzSpeed.value = e.detail.value
}

// 云台预置位
const ptzPreset = (presetId) => {
  const preset = ptzPresets.value.find(p => p.id === presetId)
  if (preset) {
    uni.showToast({
      title: `切换到${preset.name}`,
      icon: 'none'
    })
  }
}

// 工具方法
const getStatusText = (status) => {
  const texts = {
    online: '在线',
    offline: '离线',
    error: '故障'
  }
  return texts[status] || '未知'
}

const formatTime = (timestamp) => {
  if (!timestamp) return '--'

  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  } else {
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${hour}:${minute}`
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.video-page {
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
      .refresh-btn {
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

.stats-cards {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  flex-wrap: wrap;

  .stat-card {
    flex: 1;
    min-width: 160rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    display: flex;
    align-items: center;
    gap: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    &.online {
      background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    &.recording {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    &.alert {
      background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    .stat-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 20rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .stat-value {
        font-size: 40rpx;
        font-weight: 700;
        color: #333;
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.filter-tags {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 20rpx;
  overflow-x: auto;

  .filter-tag {
    flex-shrink: 0;
    padding: 12rpx 32rpx;
    background: #fff;
    border-radius: 40rpx;
    font-size: 26rpx;
    color: #666;
    white-space: nowrap;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

.area-filter {
  padding: 0 30rpx 20rpx;

  .area-scroll {
    white-space: nowrap;

    .area-item {
      display: inline-block;
      padding: 12rpx 24rpx;
      margin-right: 20rpx;
      background: #fff;
      border-radius: 40rpx;
      font-size: 26rpx;
      color: #666;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin: 0 30rpx 20rpx;
  padding: 20rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .search-input {
    flex: 1;
    font-size: 28rpx;
  }

  .search-btn {
    padding: 12rpx 32rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 40rpx;
    font-size: 26rpx;
    color: #fff;
  }
}

.camera-list {
  height: calc(100vh - 44px - var(--status-bar-height) - 450rpx);
  padding: 0 30rpx;

  .camera-item {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .camera-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      .camera-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
      }

      .camera-status {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.online {
          background: #f6ffed;
          color: #52c41a;

          .status-dot {
            background: #52c41a;
          }
        }

        &.offline {
          background: #f5f5f5;
          color: #999;

          .status-dot {
            background: #999;
          }
        }

        &.error {
          background: #fff2f0;
          color: #ff6b6b;

          .status-dot {
            background: #ff6b6b;
          }
        }

        .status-dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
        }

        .status-text {
          font-size: 24rpx;
        }
      }
    }

    .video-preview {
      position: relative;
      width: 100%;
      height: 400rpx;
      border-radius: 16rpx;
      overflow: hidden;
      margin-bottom: 20rpx;

      .snapshot-image {
        width: 100%;
        height: 100%;
      }

      .snapshot-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: #f5f5f5;

        .placeholder-text {
          margin-top: 20rpx;
          font-size: 26rpx;
          color: #999;
        }
      }

      .recording-indicator {
        position: absolute;
        top: 20rpx;
        left: 20rpx;
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 20rpx;
        background: rgba(255, 107, 107, 0.9);
        border-radius: 40rpx;

        .recording-dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
          background: #fff;
          animation: pulse 1s infinite;
        }

        .recording-text {
          font-size: 24rpx;
          color: #fff;
          font-weight: 600;
        }
      }

      .alert-badge {
        position: absolute;
        top: 20rpx;
        right: 20rpx;
        width: 48rpx;
        height: 48rpx;
        background: rgba(255, 107, 107, 0.9);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .camera-info {
      margin-bottom: 20rpx;

      .info-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-bottom: 12rpx;
        flex-wrap: wrap;

        &:last-child {
          margin-bottom: 0;
        }

        .info-text {
          font-size: 26rpx;
          color: #666;
        }

        .info-divider {
          margin: 0 8rpx;
          color: #ddd;
        }
      }
    }

    .camera-actions {
      display: flex;
      gap: 20rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;

      .action-btn {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8rpx;
        padding: 16rpx 0;

        .action-text {
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }
}

.load-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 40rpx 0;

  .load-text,
  .no-more-text {
    font-size: 26rpx;
    color: #999;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;

  .empty-text {
    margin-top: 30rpx;
    font-size: 28rpx;
    color: #999;
  }
}

// 弹窗样式
.video-popup {
  width: 690rpx;
  background: #000;
  border-radius: 24rpx;
  overflow: hidden;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    background: rgba(0, 0, 0, 0.5);

    .popup-title {
      font-size: 28rpx;
      color: #fff;
    }

    .close-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .video-player {
    width: 100%;
    height: 400rpx;
    background: #000;

    .video-component {
      width: 100%;
      height: 100%;
    }
  }

  .video-controls {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    background: rgba(0, 0, 0, 0.5);

    .control-btn {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;

      .control-text {
        font-size: 24rpx;
        color: #fff;
      }
    }
  }
}

.ptz-popup {
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .ptz-content {
    padding: 30rpx;

    .ptz-direction {
      margin-bottom: 40rpx;

      .direction-pad {
        width: 400rpx;
        height: 400rpx;
        margin: 0 auto;
        position: relative;

        .direction-btn {
          position: absolute;
          width: 100rpx;
          height: 100rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 16rpx;
          display: flex;
          align-items: center;
          justify-content: center;

          &.top {
            top: 0;
            left: 50%;
            transform: translateX(-50%);
          }

          &.bottom {
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
          }

          &.left {
            left: 0;
            top: 50%;
            transform: translateY(-50%);
          }

          &.right {
            right: 0;
            top: 50%;
            transform: translateY(-50%);
          }
        }

        .direction-row {
          position: absolute;
          top: 50%;
          left: 0;
          right: 0;
          height: 100rpx;
          transform: translateY(-50%);
          display: flex;
          justify-content: space-between;
        }

        .direction-center {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 100rpx;
          height: 100rpx;
          background: #f5f5f5;
          border-radius: 50%;
        }
      }
    }

    .ptz-zoom {
      margin-bottom: 40rpx;

      .zoom-label {
        font-size: 28rpx;
        color: #333;
        margin-bottom: 20rpx;
      }

      .zoom-controls {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 60rpx;

        .zoom-btn {
          width: 80rpx;
          height: 80rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 16rpx;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .zoom-label-center {
          font-size: 32rpx;
          color: #333;
          font-weight: 600;
        }
      }
    }

    .ptz-preset {
      margin-bottom: 40rpx;

      .preset-label {
        font-size: 28rpx;
        color: #333;
        margin-bottom: 20rpx;
      }

      .preset-list {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 20rpx;

        .preset-item {
          padding: 24rpx;
          background: #f5f5f5;
          border-radius: 16rpx;
          text-align: center;

          .preset-text {
            font-size: 26rpx;
            color: #333;
          }
        }
      }
    }

    .ptz-speed {
      .speed-label {
        font-size: 28rpx;
        color: #333;
        margin-bottom: 20rpx;
      }

      .speed-slider {
        width: 100%;
        margin-bottom: 20rpx;
      }

      .speed-value {
        text-align: center;
        font-size: 32rpx;
        color: #667eea;
        font-weight: 600;
      }
    }
  }
}

.snapshot-popup {
  width: 690rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .snapshot-content {
    padding: 30rpx;

    .snapshot-result {
      width: 100%;
      border-radius: 16rpx;
    }

    .snapshot-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 80rpx 0;

      .loading-text {
        margin-top: 20rpx;
        font-size: 26rpx;
        color: #999;
      }
    }
  }

  .snapshot-actions {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .snapshot-btn {
      flex: 1;
      height: 80rpx;
      line-height: 80rpx;
      text-align: center;
      border-radius: 16rpx;
      font-size: 28rpx;

      &.secondary {
        background: #f5f5f5;
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}
</style>
