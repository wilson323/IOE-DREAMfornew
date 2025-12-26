<template>
  <view class="attendance-container">
    <!-- 顶部状态卡片 -->
    <view class="status-card">
      <view class="card-header">
        <text class="title">今日考勤</text>
        <text class="date">{{ todayDate }}</text>
      </view>

      <view class="punch-status">
        <view class="status-item">
          <text class="status-label">上班打卡</text>
          <text class="status-time" :class="{ 'punched': morningPunch }">
            {{ morningPunch || '未打卡' }}
          </text>
        </view>

        <view class="status-item">
          <text class="status-label">下班打卡</text>
          <text class="status-time" :class="{ 'punched': eveningPunch }">
            {{ eveningPunch || '未打卡' }}
          </text>
        </view>
      </view>

      <!-- 今日排班信息 -->
      <view class="schedule-info" v-if="todaySchedule">
        <text class="schedule-text">班次: {{ todaySchedule.shiftName }}</text>
        <text class="schedule-time">{{ todaySchedule.workStartTime }} - {{ todaySchedule.workEndTime }}</text>
      </view>
    </view>

    <!-- 打卡操作区 -->
    <view class="punch-section">
      <view class="section-title">
        <text class="title-text">快速打卡</text>
        <text class="location-text" v-if="currentLocation">{{ currentLocation }}</text>
      </view>

      <!-- 定位状态指示器 -->
      <view class="location-indicator" v-if="locationStatus.loading">
        <text class="loading-icon">📍</text>
        <text class="loading-text">正在定位...</text>
      </view>

      <view class="punch-buttons">
        <!-- 上班打卡按钮 -->
        <button
          class="punch-btn上班打卡"
          :disabled="morningPunch || locationStatus.loading || !locationValid || punching"
          @click="handlePunch('IN')"
        >
          <text class="btn-icon" v-if="!punching">🕐</text>
          <text class="btn-icon spinning" v-else>⏳</text>
          <text class="btn-text">{{ punching ? '打卡中...' : (morningPunch ? '已打卡' : '上班打卡') }}</text>
          <text class="btn-desc" v-if="morningPunch">{{ morningPunch }}</text>
        </button>

        <!-- 下班打卡按钮 -->
        <button
          class="punch-btn下班打卡"
          :disabled="eveningPunch || locationStatus.loading || !locationValid || punching"
          @click="handlePunch('OUT')"
        >
          <text class="btn-icon" v-if="!punching">🕑</text>
          <text class="btn-icon spinning" v-else>⏳</text>
          <text class="btn-text">{{ punching ? '打卡中...' : (eveningPunch ? '已打卡' : '下班打卡') }}</text>
          <text class="btn-desc" v-if="eveningPunch">{{ eveningPunch }}</text>
        </button>
      </view>

      <!-- 位置验证状态 -->
      <view class="location-status" :class="locationStatusClass">
        <text class="location-icon">{{ locationStatusIcon }}</text>
        <text class="location-text">{{ locationStatusText }}</text>
        <text class="location-accuracy" v-if="locationStatus.accuracy">
          精度: {{ locationStatus.accuracy }}m
        </text>
      </view>

      <!-- 离线缓存提示 -->
      <view class="offline-cache-tip" v-if="offlineCacheCount > 0">
        <text class="tip-icon">💾</text>
        <text class="tip-text">有 {{ offlineCacheCount }} 条离线打卡待同步</text>
        <button class="sync-btn" @click="syncOfflinePunches">立即同步</button>
      </view>
    </view>

    <!-- 多模式打卡选择 -->
    <view class="punch-modes" v-if="!locationValid && !locationStatus.loading">
      <view class="mode-title">其他打卡方式</view>
      <view class="mode-grid">
        <view class="mode-item" @click="handleWiFiPunch">
          <text class="mode-icon">📶</text>
          <text class="mode-text">WiFi打卡</text>
        </view>
        <view class="mode-item" @click="handleFacePunch">
          <text class="mode-icon">👤</text>
          <text class="mode-text">人脸打卡</text>
        </view>
        <view class="mode-item" @click="handleBluetoothPunch">
          <text class="mode-icon">🔵</text>
          <text class="mode-text">蓝牙打卡</text>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="function-menu">
      <view class="menu-title">考勤功能</view>

      <view class="menu-grid">
        <view class="menu-item" @click="navigateTo('/pages/attendance/records')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📋</text>
          </view>
          <text class="menu-text">打卡记录</text>
          <text class="menu-desc">历史记录</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/leave')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📝</text>
          </view>
          <text class="menu-text">请假申请</text>
          <text class="menu-desc">请假出差</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/overtime')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">⏰</text>
          </view>
          <text class="menu-text">加班申请</text>
          <text class="menu-desc">加班登记</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/statistics')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📊</text>
          </view>
          <text class="menu-text">考勤统计</text>
          <text class="menu-desc">月度报表</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/schedule')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📅</text>
          </view>
          <text class="menu-text">我的排班</text>
          <text class="menu-desc">排班查看</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/repair')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">🔧</text>
          </view>
          <text class="menu-text">补卡申请</text>
          <text class="menu-desc">漏打卡补卡</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/trip')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">✈️</text>
          </view>
          <text class="menu-text">出差申请</text>
          <text class="menu-desc">出差登记</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/summary')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📈</text>
          </view>
          <text class="menu-text">考勤汇总</text>
          <text class="menu-desc">年度汇总</text>
        </view>
      </view>
    </view>

    <!-- 最近打卡记录 -->
    <view class="recent-records">
      <view class="records-title">最近记录</view>

      <view class="record-list">
        <view class="record-item" v-for="record in recentRecords" :key="record.id">
          <view class="record-info">
            <text class="record-date">{{ record.date }}</text>
            <text class="record-type">{{ record.type === 'IN' ? '上班' : '下班' }}</text>
          </view>
          <text class="record-time">{{ record.time }}</text>
          <text class="record-location" v-if="record.location">{{ record.location }}</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="recentRecords.length === 0">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无打卡记录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

// ==================== 响应式数据 ====================
const todayDate = ref('')
const morningPunch = ref('')
const eveningPunch = ref('')
const currentLocation = ref('')
const locationValid = ref(false)
const recentRecords = ref([])
const employeeId = ref(1001) // TODO: 从用户信息获取
const todaySchedule = ref(null)
const punching = ref(false)
const offlineCacheCount = ref(0)

// 定位状态
const locationStatus = reactive({
  loading: false,
  success: false,
  error: null,
  accuracy: null,
  timestamp: null
})

// 当前位置
const currentPosition = reactive({
  latitude: 0,
  longitude: 0,
  accuracy: 0
})

// 定位配置
const LOCATION_CONFIG = {
  timeout: 10000, // 定位超时时间 10秒
  maxAccuracy: 100, // 最大精度误差 100米
  retryTimes: 3, // 重试次数
  retryDelay: 2000 // 重试延迟 2秒
}

// 打卡防抖（防止重复打卡）
const punchDebounceTimer = ref(null)
const PUNCH_DEBOUNCE_TIME = 3000 // 3秒内不允许重复打卡

// 定位定时器
let locationWatchId = null
let locationRetryTimer = null

// ==================== 计算属性 ====================
const locationStatusClass = computed(() => {
  if (locationStatus.loading) return 'loading'
  if (locationValid.value) return 'valid'
  return 'invalid'
})

const locationStatusIcon = computed(() => {
  if (locationStatus.loading) return '⌛'
  if (locationValid.value) return '✓'
  return '⚠'
})

const locationStatusText = computed(() => {
  if (locationStatus.loading) return '正在定位...'
  if (locationValid.value) return '位置验证通过'
  if (locationStatus.error) return locationStatus.error
  return '不在考勤范围内'
})

// ==================== 生命周期 ====================
onMounted(() => {
  initPage()
  startLocationWatch()
  checkOfflineCache()
  loadTodaySchedule()
})

onUnmounted(() => {
  stopLocationWatch()
  if (locationRetryTimer) {
    clearTimeout(locationRetryTimer)
  }
  if (punchDebounceTimer.value) {
    clearTimeout(punchDebounceTimer.value)
  }
})

// ==================== 页面初始化 ====================
const initPage = () => {
  console.log('[考勤打卡] 页面初始化')
  todayDate.value = dayjs().format('YYYY年MM月DD日')
  getCurrentLocation()
  loadTodayStatus()
  loadRecentRecords()
}

// ==================== 定位相关 ====================
/**
 * 启动定位监听
 */
const startLocationWatch = () => {
  // 持续监听位置变化
  locationWatchId = uni.onLocationChange((res) => {
    currentPosition.latitude = res.latitude
    currentPosition.longitude = res.longitude
    currentPosition.accuracy = res.accuracy || 0
    locationStatus.accuracy = Math.round(res.accuracy || 0)
    locationStatus.timestamp = dayjs().format('HH:mm:ss')

    validateLocation()
  })
}

/**
 * 停止定位监听
 */
const stopLocationWatch = () => {
  if (locationWatchId) {
    uni.offLocationChange(locationWatchId)
    locationWatchId = null
  }
}

/**
 * 获取当前位置
 * 增强版：支持超时、重试、精度检查
 */
const getCurrentLocation = (retryCount = 0) => {
  locationStatus.loading = true
  locationStatus.error = null

  uni.getLocation({
    type: 'gcj02', // 使用国测局坐标
    altitude: true,
    accuracy: 'best',
    timeout: LOCATION_CONFIG.timeout,
    success: (res) => {
      console.log('[考勤打卡] 定位成功:', res)

      currentPosition.latitude = res.latitude
      currentPosition.longitude = res.longitude
      currentPosition.accuracy = res.accuracy || 0

      locationStatus.accuracy = Math.round(res.accuracy || 0)
      locationStatus.timestamp = dayjs().format('HH:mm:ss')
      locationStatus.loading = false
      locationStatus.success = true

      // 精度检查
      if (res.accuracy && res.accuracy > LOCATION_CONFIG.maxAccuracy) {
        console.warn('[考勤打卡] 定位精度不足:', res.accuracy)
        locationStatus.error = `定位精度不足 (${Math.round(res.accuracy)}m)`
      }

      // 验证位置
      validateLocation()
    },
    fail: (error) => {
      console.error('[考勤打卡] 定位失败:', error)

      locationStatus.loading = false
      locationStatus.error = '定位失败'

      // 重试机制
      if (retryCount < LOCATION_CONFIG.retryTimes) {
        console.log(`[考勤打卡] 定位重试 ${retryCount + 1}/${LOCATION_CONFIG.retryTimes}`)
        locationRetryTimer = setTimeout(() => {
          getCurrentLocation(retryCount + 1)
        }, LOCATION_CONFIG.retryDelay)
      } else {
        locationStatus.error = '定位失败，请检查GPS权限'
      }
    }
  })
}

/**
 * 验证位置是否在考勤范围内
 * 增强版：支持距离计算、多地点验证
 */
const validateLocation = async () => {
  if (currentPosition.latitude === 0 || currentPosition.longitude === 0) {
    locationValid.value = false
    return
  }

  try {
    const res = await attendanceApi.locationApi.validateLocation({
      employeeId: employeeId.value,
      latitude: currentPosition.latitude,
      longitude: currentPosition.longitude
    })

    if (res.success && res.data) {
      locationValid.value = res.data.valid || true

      if (locationValid.value) {
        // 获取位置名称
        getLocationName()

        // 震动反馈
        uni.vibrateShort({
          success: () => {
            console.log('[考勤打卡] 位置验证通过，震动反馈')
          }
        })
      } else {
        locationStatus.error = res.data.message || '不在考勤范围内'
      }
    }
  } catch (error) {
    console.error('[考勤打卡] 位置验证失败:', error)
    locationValid.value = false
    locationStatus.error = '位置验证失败'
  }
}

/**
 * 获取位置名称（逆地理编码）
 */
const getLocationName = () => {
  // TODO: 调用逆地理编码API获取详细地址
  // 这里可以集成腾讯地图、高德地图等逆地理编码服务
  currentLocation.value = '公司办公楼' // 临时占位
}

// ==================== 打卡相关 ====================
/**
 * 处理打卡
 * 增强版：支持防抖、离线缓存、错误处理
 */
const handlePunch = async (type) => {
  // 防抖检查
  if (punchDebounceTimer.value) {
    uni.showToast({
      title: '打卡操作太频繁',
      icon: 'none'
    })
    return
  }

  // 位置验证
  if (!locationValid.value) {
    uni.showToast({
      title: '请在考勤范围内打卡',
      icon: 'none'
    })
    return
  }

  // 检查网络状态
  const networkType = await getNetworkType()
  if (networkType === 'none') {
    // 离线打卡
    handleOfflinePunch(type)
    return
  }

  // 在线打卡
  punching.value = true

  try {
    const res = await attendanceApi.punchApi.gpsPunch({
      employeeId: employeeId.value,
      punchType: type,
      latitude: currentPosition.latitude,
      longitude: currentPosition.longitude,
      accuracy: currentPosition.accuracy,
      photoUrl: '', // 可选：拍照后上传
      address: currentLocation.value
    })

    if (res.success) {
      // 打卡成功
      handlePunchSuccess(type)

      // 设置防抖
      setPunchDebounce()
    } else {
      uni.showToast({
        title: res.message || '打卡失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('[考勤打卡] 打卡失败:', error)

    // 网络错误时使用离线打卡
    if (error.message && error.message.includes('网络')) {
      handleOfflinePunch(type)
    } else {
      uni.showToast({
        title: '打卡失败，请稍后重试',
        icon: 'none'
      })
    }
  } finally {
    punching.value = false
  }
}

/**
 * 打卡成功处理
 */
const handlePunchSuccess = (type) => {
  const time = dayjs().format('HH:mm:ss')

  if (type === 'IN') {
    morningPunch.value = time
  } else {
    eveningPunch.value = time
  }

  // 震动反馈
  uni.vibrateShort()

  // 成功提示
  uni.showToast({
    title: '打卡成功',
    icon: 'success'
  })

  // 刷新数据
  loadTodayStatus()
  loadRecentRecords()
}

/**
 * 设置打卡防抖
 */
const setPunchDebounce = () => {
  punchDebounceTimer.value = setTimeout(() => {
    punchDebounceTimer.value = null
  }, PUNCH_DEBOUNCE_TIME)
}

/**
 * 离线打卡处理
 */
const handleOfflinePunch = (type) => {
  const offlinePunch = {
    id: Date.now(),
    employeeId: employeeId.value,
    punchType: type,
    latitude: currentPosition.latitude,
    longitude: currentPosition.longitude,
    accuracy: currentPosition.accuracy,
    address: currentLocation.value,
    punchTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    synced: false
  }

  // 保存到本地存储
  const offlinePunches = uni.getStorageSync('offlinePunches') || []
  offlinePunches.push(offlinePunch)
  uni.setStorageSync('offlinePunches', offlinePunches)

  // 更新缓存计数
  offlineCacheCount.value = offlinePunches.length

  uni.showToast({
    title: '离线打卡已缓存',
    icon: 'success'
  })

  console.log('[考勤打卡] 离线打卡已缓存:', offlinePunch)
}

/**
 * 同步离线打卡数据
 */
const syncOfflinePunches = async () => {
  const offlinePunches = uni.getStorageSync('offlinePunches') || []

  if (offlinePunches.length === 0) {
    uni.showToast({
      title: '无离线数据需同步',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '同步中...'
  })

  try {
    const res = await attendanceApi.offlineApi.syncOfflinePunches(employeeId.value)

    if (res.success) {
      // 清空本地缓存
      uni.removeStorageSync('offlinePunches')
      offlineCacheCount.value = 0

      uni.showToast({
        title: '同步成功',
        icon: 'success'
      })

      // 刷新数据
      loadTodayStatus()
      loadRecentRecords()
    }
  } catch (error) {
    console.error('[考勤打卡] 同步离线数据失败:', error)
    uni.showToast({
      title: '同步失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

/**
 * 检查离线缓存
 */
const checkOfflineCache = () => {
  const offlinePunches = uni.getStorageSync('offlinePunches') || []
  offlineCacheCount.value = offlinePunches.length

  if (offlineCacheCount.value > 0) {
    console.log(`[考勤打卡] 发现 ${offlineCacheCount.value} 条离线打卡待同步`)

    // 自动同步（仅在有网络时）
    getNetworkType().then(networkType => {
      if (networkType !== 'none') {
        syncOfflinePunches()
      }
    })
  }
}

// ==================== 其他打卡方式 ====================
/**
 * WiFi打卡
 */
const handleWiFiPunch = async () => {
  uni.showToast({
    title: 'WiFi打卡功能开发中',
    icon: 'none'
  })

  // TODO: 实现WiFi打卡逻辑
  // 1. 获取当前连接的WiFi SSID
  // 2. 验证是否在公司WiFi列表
  // 3. 执行打卡
}

/**
 * 人脸识别打卡
 */
const handleFacePunch = async () => {
  uni.showToast({
    title: '人脸打卡功能开发中',
    icon: 'none'
  })

  // TODO: 实现人脸识别打卡逻辑
  // 1. 调用摄像头拍照
  // 2. 上传照片到后端进行人脸识别
  // 3. 验证通过后执行打卡
}

/**
 * 蓝牙打卡
 */
const handleBluetoothPunch = async () => {
  uni.showToast({
    title: '蓝牙打卡功能开发中',
    icon: 'none'
  })

  // TODO: 实现蓝牙打卡逻辑
  // 1. 搜索蓝牙设备
  // 2. 验证是否在公司蓝牙信标范围内
  // 3. 执行打卡
}

// ==================== 数据加载 ====================
/**
 * 获取网络类型
 */
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

/**
 * 加载今日考勤状态
 */
const loadTodayStatus = async () => {
  try {
    const res = await attendanceApi.punchApi.getTodayPunchStatus(employeeId.value)

    if (res.success && res.data) {
      morningPunch.value = res.data.morningPunch || ''
      eveningPunch.value = res.data.eveningPunch || ''
    }
  } catch (error) {
    console.error('[考勤打卡] 获取今日状态失败:', error)
  }
}

/**
 * 加载今日排班
 */
const loadTodaySchedule = async () => {
  try {
    const res = await attendanceApi.scheduleApi.getTodaySchedule(employeeId.value)

    if (res.success && res.data) {
      todaySchedule.value = res.data
    }
  } catch (error) {
    console.error('[考勤打卡] 获取今日排班失败:', error)
  }
}

/**
 * 加载最近打卡记录
 */
const loadRecentRecords = async () => {
  try {
    const endDate = dayjs().format('YYYY-MM-DD')
    const startDate = dayjs().subtract(7, 'day').format('YYYY-MM-DD')

    const res = await attendanceApi.punchApi.getPunchRecords({
      employeeId: employeeId.value,
      startDate,
      endDate,
      pageSize: 10,
      pageNum: 1
    })

    if (res.success && res.data) {
      recentRecords.value = res.data.list || []
    }
  } catch (error) {
    console.error('[考勤打卡] 获取打卡记录失败:', error)
  }
}

/**
 * 页面跳转
 */
const navigateTo = (url) => {
  uni.navigateTo({
    url
  })
}
</script>

<style lang="scss" scoped>
.attendance-container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

// 状态卡片
.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  color: white;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .title {
      font-size: 36rpx;
      font-weight: bold;
    }

    .date {
      font-size: 28rpx;
      opacity: 0.8;
    }
  }

  .punch-status {
    display: flex;
    justify-content: space-around;

    .status-item {
      text-align: center;

      .status-label {
        display: block;
        font-size: 24rpx;
        opacity: 0.8;
        margin-bottom: 10rpx;
      }

      .status-time {
        font-size: 32rpx;
        font-weight: bold;

        &.punched {
          color: #4ade80;
        }
      }
    }
  }

  .schedule-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20rpx;
    padding-top: 20rpx;
    border-top: 1rpx solid rgba(255, 255, 255, 0.2);

    .schedule-text {
      font-size: 26rpx;
    }

    .schedule-time {
      font-size: 24rpx;
      opacity: 0.8;
    }
  }
}

// 打卡操作区
.punch-section {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .title-text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .location-text {
      font-size: 24rpx;
      color: #666;
    }
  }

  .location-indicator {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20rpx;
    margin-bottom: 20rpx;
    background-color: #f0f9ff;
    border-radius: 10rpx;

    .loading-icon {
      font-size: 28rpx;
      margin-right: 10rpx;
      animation: pulse 1.5s ease-in-out infinite;
    }

    .loading-text {
      font-size: 26rpx;
      color: #0284c7;
    }
  }

  .punch-buttons {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .punch-btn {
      flex: 0 0 48%;
      height: 120rpx;
      border-radius: 15rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border: none;
      position: relative;
      overflow: hidden;

      &.上班打卡 {
        background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
        color: white;
      }

      &.下班打卡 {
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        color: white;
      }

      &:disabled {
        opacity: 0.5;
      }

      .btn-icon {
        font-size: 36rpx;
        margin-bottom: 5rpx;

        &.spinning {
          animation: spin 1s linear infinite;
        }
      }

      .btn-text {
        font-size: 28rpx;
        font-weight: bold;
      }

      .btn-desc {
        font-size: 20rpx;
        opacity: 0.8;
        margin-top: 2rpx;
      }
    }
  }

  .location-status {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 15rpx;
    border-radius: 10rpx;
    background-color: #fef2f2;

    &.valid {
      background-color: #f0fdf4;
    }

    &.loading {
      background-color: #eff6ff;
    }

    .location-icon {
      margin-right: 10rpx;
      font-size: 24rpx;
    }

    .location-text {
      font-size: 24rpx;
      color: #666;
    }

    .location-accuracy {
      margin-left: 10rpx;
      font-size: 20rpx;
      color: #999;
    }
  }

  .offline-cache-tip {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 15rpx;
    margin-top: 20rpx;
    background-color: #fffbeb;
    border-radius: 10rpx;

    .tip-icon {
      font-size: 24rpx;
      margin-right: 10rpx;
    }

    .tip-text {
      flex: 1;
      font-size: 24rpx;
      color: #92400e;
    }

    .sync-btn {
      padding: 8rpx 20rpx;
      background-color: #f59e0b;
      color: white;
      border-radius: 8rpx;
      font-size: 22rpx;
      border: none;
    }
  }
}

// 多模式打卡
.punch-modes {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .mode-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #666;
    margin-bottom: 20rpx;
  }

  .mode-grid {
    display: flex;
    justify-content: space-around;

    .mode-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20rpx;

      .mode-icon {
        font-size: 40rpx;
        margin-bottom: 10rpx;
      }

      .mode-text {
        font-size: 24rpx;
        color: #666;
      }
    }
  }
}

// 功能菜单
.function-menu {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .menu-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: -8rpx;
      width: 60rpx;
      height: 4rpx;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      border-radius: 2rpx;
    }
  }

  .menu-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;

    .menu-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24rpx 12rpx;
      border-radius: 16rpx;
      background-color: #f8fafc;
      transition: all 0.3s ease;

      &:active {
        transform: scale(0.95);
        background-color: #f1f5f9;
      }

      .menu-icon-wrapper {
        width: 80rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 20rpx;
        margin-bottom: 12rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

        .menu-icon {
          font-size: 36rpx;
        }
      }

      .menu-text {
        font-size: 26rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 4rpx;
      }

      .menu-desc {
        font-size: 20rpx;
        color: #999;
      }
    }
  }
}

// 最近记录
.recent-records {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;

  .records-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .record-list {
    .record-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-info {
        flex: 1;

        .record-date {
          font-size: 28rpx;
          color: #333;
          margin-right: 20rpx;
        }

        .record-type {
          font-size: 24rpx;
          color: #666;
          background-color: #f0f0f0;
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
        }
      }

      .record-time {
        font-size: 28rpx;
        color: #666;
        font-weight: bold;
        margin-right: 10rpx;
      }

      .record-location {
        font-size: 20rpx;
        color: #999;
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60rpx 0;

      .empty-icon {
        font-size: 60rpx;
        margin-bottom: 20rpx;
        opacity: 0.5;
      }

      .empty-text {
        font-size: 26rpx;
        color: #999;
      }
    }
  }
}

// 动画
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
