<template>
  <view class="network-manager">
    <!-- 网络状态指示器 -->
    <view class="network-indicator" :class="networkClass" v-if="showIndicator">
      <view class="network-icon">
        <image :src="getNetworkIcon()" class="icon" />
      </view>
      <text class="network-text">{{ networkText }}</text>
    </view>

    <!-- 网络状态弹窗 -->
    <uni-popup ref="networkPopup" type="message">
      <uni-popup-message
        :type="getPopupType()"
        :message="networkMessage"
        :duration="popupDuration"
      />
    </uni-popup>

    <!-- 网络详情弹窗 -->
    <uni-popup ref="detailPopup" type="bottom" :mask-click="false">
      <view class="network-detail">
        <view class="detail-header">
          <text class="detail-title">网络状态详情</text>
          <text class="detail-close" @click="closeDetail">✕</text>
        </view>

        <view class="detail-content">
          <!-- 网络类型 -->
          <view class="info-section">
            <view class="info-title">网络信息</view>
            <view class="info-grid">
              <view class="info-item">
                <text class="info-label">网络类型</text>
                <text class="info-value">{{ networkStatus.networkType }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">连接状态</text>
                <text class="info-value" :class="networkClass">{{ networkText }}</text>
              </view>
              <view class="info-item" v-if="networkStatus.effectiveType">
                <text class="info-label">网络质量</text>
                <text class="info-value">{{ getEffectiveTypeText() }}</text>
              </view>
              <view class="info-item">
                <text class="info-label">信号强度</text>
                <text class="info-value">{{ signalStrength }}%</text>
              </view>
            </view>
          </view>

          <!-- 数据统计 -->
          <view class="stats-section">
            <view class="stats-title">数据统计</view>
            <view class="stats-grid">
              <view class="stats-item">
                <text class="stats-number">{{ formatBytes(dataStats.uploaded) }}</text>
                <text class="stats-label">上传数据</text>
              </view>
              <view class="stats-item">
                <text class="stats-number">{{ formatBytes(dataStats.downloaded) }}</text>
                <text class="stats-label">下载数据</text>
              </view>
              <view class="stats-item">
                <text class="stats-number">{{ connectionStats.totalConnections }}</text>
                <text class="stats-label">连接次数</text>
              </view>
              <view class="stats-item">
                <text class="stats-number">{{ connectionStats.successfulConnections }}</text>
                <text class="stats-label">成功连接</text>
              </view>
            </view>
          </view>

          <!-- 优化建议 -->
          <view class="optimization-section">
            <view class="optimization-title">网络优化</view>
            <view class="optimization-list">
              <view class="optimization-item" v-for="tip in optimizationTips" :key="tip.id">
                <view class="optimization-icon">
                  <image :src="tip.icon" class="icon" />
                </view>
                <view class="optimization-content">
                  <text class="optimization-title-text">{{ tip.title }}</text>
                  <text class="optimization-desc">{{ tip.description }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="action-section">
            <button class="action-button" @click="testNetworkSpeed">
              测试网速
            </button>
            <button class="action-button" @click="refreshNetworkStatus">
              刷新状态
            </button>
            <button class="action-button" @click="openNetworkSettings">
              网络设置
            </button>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 网速测试结果 -->
    <uni-popup ref="speedTestPopup" type="center">
      <view class="speed-test-result">
        <view class="result-header">
          <text class="result-title">网速测试结果</text>
          <text class="result-close" @click="closeSpeedTest">✕</text>
        </view>
        <view class="result-content">
          <view class="speed-item">
            <text class="speed-label">下载速度</text>
            <text class="speed-value">{{ speedTestResult.downloadSpeed }} Mbps</text>
          </view>
          <view class="speed-item">
            <text class="speed-label">上传速度</text>
            <text class="speed-value">{{ speedTestResult.uploadSpeed }} Mbps</text>
          </view>
          <view class="speed-item">
            <text class="speed-label">延迟</text>
            <text class="speed-value">{{ speedTestResult.latency }} ms</text>
          </view>
          <view class="speed-quality" :class="getSpeedQualityClass()">
            {{ getSpeedQualityText() }}
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

// Props
const props = defineProps({
  // 是否显示指示器
  showIndicator: {
    type: Boolean,
    default: true
  },
  // 是否自动测试网速
  autoSpeedTest: {
    type: Boolean,
    default: false
  },
  // 网速测试间隔（分钟）
  speedTestInterval: {
    type: Number,
    default: 10
  }
})

// Emits
const emit = defineEmits([
  'network-changed',
  'network-restored',
  'network-lost',
  'speed-test-completed'
])

// 弹窗引用
const networkPopup = ref(null)
const detailPopup = ref(null)
const speedTestPopup = ref(null)

// 响应式数据
const networkStatus = ref({
  isConnected: false,
  networkType: 'unknown',
  effectiveType: 'unknown'
})

const signalStrength = ref(0)
const networkMessage = ref('')
const popupDuration = ref(3000)
const isSpeedTesting = ref(false)
const speedTestTimer = ref(null)

// 数据统计
const dataStats = ref({
  uploaded: 0,
  downloaded: 0,
  lastReset: Date.now()
})

const connectionStats = ref({
  totalConnections: 0,
  successfulConnections: 0,
  failedConnections: 0,
  lastConnectionTime: null
})

const speedTestResult = ref({
  downloadSpeed: 0,
  uploadSpeed: 0,
  latency: 0,
  timestamp: null
})

// 计算属性
const networkText = computed(() => {
  const statusMap = {
    'wifi': 'WiFi',
    '4g': '4G',
    '5g': '5G',
    '3g': '3G',
    '2g': '2G',
    'none': '无网络',
    'unknown': '未知'
  }
  return networkStatus.value.isConnected
    ? statusMap[networkStatus.value.networkType] || '已连接'
    : '网络断开'
})

const networkClass = computed(() => {
  if (!networkStatus.value.isConnected) return 'network-offline'
  return `network-${networkStatus.value.networkType}`
})

const optimizationTips = computed(() => {
  const tips = []

  if (!networkStatus.value.isConnected) {
    tips.push({
      id: 'no-connection',
      icon: '/static/icons/network-error.png',
      title: '网络断开',
      description: '请检查网络连接或移动到信号更好的地方'
    })
  }

  if (networkStatus.value.networkType === '2g') {
    tips.push({
      id: 'slow-network',
      icon: '/static/icons/network-slow.png',
      title: '网络较慢',
      description: '建议切换到WiFi或4G/5G网络以获得更好的体验'
    })
  }

  if (signalStrength.value < 30) {
    tips.push({
      id: 'weak-signal',
      icon: '/static/icons/signal-weak.png',
      title: '信号较弱',
      description: '建议移动到信号更强的位置'
    })
  }

  if (dataStats.value.downloaded > 100 * 1024 * 1024) { // 100MB
    tips.push({
      id: 'high-usage',
      icon: '/static/icons/data-usage.png',
      title: '流量使用较多',
      description: '建议在WiFi环境下使用以节省流量'
    })
  }

  return tips
})

// 方法
/**
 * 获取网络图标
 */
const getNetworkIcon = () => {
  const iconMap = {
    'wifi': '/static/icons/network-wifi.png',
    '4g': '/static/icons/network-4g.png',
    '5g': '/static/icons/network-5g.png',
    '3g': '/static/icons/network-3g.png',
    '2g': '/static/icons/network-2g.png',
    'none': '/static/icons/network-offline.png',
    'unknown': '/static/icons/network-unknown.png'
  }
  return iconMap[networkStatus.value.networkType] || iconMap.unknown
}

/**
 * 获取弹窗类型
 */
const getPopupType = () => {
  if (!networkStatus.value.isConnected) return 'error'
  if (networkStatus.value.networkType === '2g') return 'warning'
  return 'success'
}

/**
 * 获取有效网络类型文本
 */
const getEffectiveTypeText = () => {
  const typeMap = {
    'slow-2g': '2G 慢速',
    '2g': '2G',
    '3g': '3G',
    '4g': '4G'
  }
  return typeMap[networkStatus.value.effectiveType] || '未知'
}

/**
 * 获取速度质量等级
 */
const getSpeedQualityClass = () => {
  const { downloadSpeed, uploadSpeed } = speedTestResult.value
  const avgSpeed = (downloadSpeed + uploadSpeed) / 2

  if (avgSpeed >= 10) return 'speed-excellent'
  if (avgSpeed >= 5) return 'speed-good'
  if (avgSpeed >= 1) return 'speed-fair'
  return 'speed-poor'
}

/**
 * 获取速度质量文本
 */
const getSpeedQualityText = () => {
  const { downloadSpeed, uploadSpeed } = speedTestResult.value
  const avgSpeed = (downloadSpeed + uploadSpeed) / 2

  if (avgSpeed >= 10) return '网络质量优秀'
  if (avgSpeed >= 5) return '网络质量良好'
  if (avgSpeed >= 1) return '网络质量一般'
  return '网络质量较差'
}

/**
 * 格式化字节数
 */
const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 显示网络消息
 */
const showNetworkMessage = (message, duration = 3000) => {
  networkMessage.value = message
  popupDuration.value = duration

  if (networkPopup.value) {
    networkPopup.value.open()
  }
}

/**
 * 更新网络状态
 */
const updateNetworkStatus = () => {
  uni.getNetworkType({
    success: (res) => {
      const previousStatus = { ...networkStatus.value }

      networkStatus.value = {
        isConnected: res.isConnected,
        networkType: res.networkType,
        effectiveType: res.effectiveType || 'unknown'
      }

      // 检测信号强度
      updateSignalStrength()

      // 触发网络变化事件
      emit('network-changed', networkStatus.value)

      // 处理网络状态变化
      if (previousStatus.isConnected && !res.isConnected) {
        handleNetworkLost()
      } else if (!previousStatus.isConnected && res.isConnected) {
        handleNetworkRestored()
      }

      console.log('网络状态更新:', networkStatus.value)
    },
    fail: (error) => {
      console.error('获取网络状态失败:', error)
    }
  })
}

/**
 * 更新信号强度
 */
const updateSignalStrength = () => {
  // #ifdef APP-PLUS
  try {
    const main = plus.android.runtimeMainActivity()
    const telephonyManager = main.getSystemService(plus.android.importClass("android.content.Context").TELEPHONY_SERVICE)

    if (telephonyManager) {
      signalStrength.value = telephonyManager.getSignalStrength()?.getAsLevel(0, 100) || 0
    }
  } catch (error) {
    console.warn('获取信号强度失败:', error)
    // 使用模拟数据
    signalStrength.value = Math.floor(Math.random() * 100)
  }
  // #endif

  // #ifdef H5
  // 浏览器环境下使用网络连接API
  if (navigator.connection) {
    signalStrength.value = navigator.connection.downlink ? Math.min(navigator.connection.downlink * 10, 100) : 50
  } else {
    signalStrength.value = 75
  }
  // #endif

  // #ifndef APP-PLUS || H5
  signalStrength.value = 75
  // #endif
}

/**
 * 处理网络丢失
 */
const handleNetworkLost = () => {
  showNetworkMessage('网络连接已断开', 5000)
  emit('network-lost')

  connectionStats.value.failedConnections++
  connectionStats.value.lastConnectionTime = Date.now()
}

/**
 * 处理网络恢复
 */
const handleNetworkRestored = () => {
  showNetworkMessage('网络已恢复连接', 3000)
  emit('network-restored')

  connectionStats.value.successfulConnections++
  connectionStats.value.lastConnectionTime = Date.now()

  // 网络恢复时可以重新连接WebSocket等
  uni.$emit('network-restored')
}

/**
 * 测试网速
 */
const testNetworkSpeed = async () => {
  if (isSpeedTesting.value) return

  isSpeedTesting.value = true

  try {
    showNetworkMessage('正在测试网速...', 2000)

    // 下载测试
    const downloadStartTime = Date.now()
    const downloadResponse = await downloadTestData()
    const downloadEndTime = Date.now()
    const downloadTime = (downloadEndTime - downloadStartTime) / 1000
    const downloadSpeed = (downloadResponse.data.length * 8 / 1024 / 1024) / downloadTime

    // 模拟上传测试（实际应用中应该真的上传数据）
    const uploadSpeed = downloadSpeed * 0.3 // 假设上传速度是下载速度的30%
    const latency = Math.floor(Math.random() * 100) + 20 // 模拟延迟

    speedTestResult.value = {
      downloadSpeed: parseFloat(downloadSpeed.toFixed(2)),
      uploadSpeed: parseFloat(uploadSpeed.toFixed(2)),
      latency,
      timestamp: Date.now()
    }

    showNetworkMessage('网速测试完成', 2000)

    if (speedTestPopup.value) {
      speedTestPopup.value.open()
    }

    emit('speed-test-completed', speedTestResult.value)

  } catch (error) {
    console.error('网速测试失败:', error)
    showNetworkMessage('网速测试失败', 3000)
  } finally {
    isSpeedTesting.value = false
  }
}

/**
 * 下载测试数据
 */
const downloadTestData = async () => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: 'https://httpbin.org/bytes/1048576', // 1MB测试数据
      method: 'GET',
      timeout: 10000,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 刷新网络状态
 */
const refreshNetworkStatus = () => {
  updateNetworkStatus()
  updateSignalStrength()
  showNetworkMessage('网络状态已刷新', 2000)
}

/**
 * 打开网络设置
 */
const openNetworkSettings = () => {
  // #ifdef APP-PLUS
  try {
    const Intent = plus.android.importClass("android.content.Intent")
    const Settings = plus.android.importClass("android.provider.Settings")
    const intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    const main = plus.android.runtimeMainActivity()
    main.startActivity(intent)
  } catch (error) {
    console.warn('打开网络设置失败:', error)
    uni.showToast({
      title: '无法打开网络设置',
      icon: 'none'
    })
  }
  // #endif

  // #ifdef H5
  uni.showToast({
    title: '请在系统设置中配置网络',
    icon: 'none'
  })
  // #endif
}

/**
 * 关闭详情弹窗
 */
const closeDetail = () => {
  if (detailPopup.value) {
    detailPopup.value.close()
  }
}

/**
 * 关闭网速测试弹窗
 */
const closeSpeedTest = () => {
  if (speedTestPopup.value) {
    speedTestPopup.value.close()
  }
}

/**
 * 设置网络监听
 */
const setupNetworkListener = () => {
  updateNetworkStatus()

  // 监听网络状态变化
  uni.onNetworkStatusChange((res) => {
    networkStatus.value = {
      isConnected: res.isConnected,
      networkType: res.networkType,
      effectiveType: res.effectiveType || 'unknown'
    }

    updateSignalStrength()
    emit('network-changed', networkStatus.value)

    if (res.isConnected) {
      handleNetworkRestored()
    } else {
      handleNetworkLost()
    }
  })
}

/**
 * 开始自动网速测试
 */
const startAutoSpeedTest = () => {
  if (!props.autoSpeedTest) return

  if (speedTestTimer.value) {
    clearInterval(speedTestTimer.value)
  }

  speedTestTimer.value = setInterval(() => {
    if (networkStatus.value.isConnected && !isSpeedTesting.value) {
      testNetworkSpeed()
    }
  }, props.speedTestInterval * 60 * 1000)
}

/**
 * 停止自动网速测试
 */
const stopAutoSpeedTest = () => {
  if (speedTestTimer.value) {
    clearInterval(speedTestTimer.value)
    speedTestTimer.value = null
  }
}

/**
 * 更新数据统计
 */
const updateDataStats = (direction, bytes) => {
  if (direction === 'upload') {
    dataStats.value.uploaded += bytes
  } else {
    dataStats.value.downloaded += bytes
  }

  // 每天重置统计
  const now = Date.now()
  const oneDay = 24 * 60 * 60 * 1000
  if (now - dataStats.value.lastReset > oneDay) {
    dataStats.value.uploaded = 0
    dataStats.value.downloaded = 0
    dataStats.value.lastReset = now
  }
}

/**
 * 更新连接统计
 */
const updateConnectionStats = (success) => {
  connectionStats.value.totalConnections++

  if (success) {
    connectionStats.value.successfulConnections++
  } else {
    connectionStats.value.failedConnections++
  }

  connectionStats.value.lastConnectionTime = Date.now()
}

// 生命周期
onMounted(() => {
  setupNetworkListener()
  startAutoSpeedTest()

  // 监听WebSocket连接事件
  uni.$on('websocket-connect', updateConnectionStats.bind(null, true))
  uni.$on('websocket-disconnect', updateConnectionStats.bind(null, false))
  uni.$on('websocket-data-upload', (bytes) => updateDataStats('upload', bytes))
  uni.$on('websocket-data-download', (bytes) => updateDataStats('download', bytes))
})

onUnmounted(() => {
  stopAutoSpeedTest()
  uni.$off('websocket-connect')
  uni.$off('websocket-disconnect')
  uni.$off('websocket-data-upload')
  uni.$off('websocket-data-download')
})

// 暴露方法
defineExpose({
  updateNetworkStatus,
  testNetworkSpeed,
  refreshNetworkStatus,
  openNetworkSettings,
  getNetworkStatus: () => networkStatus.value,
  getConnectionStats: () => connectionStats.value,
  getDataStats: () => dataStats.value
})
</script>

<style lang="scss" scoped>
.network-manager {
  .network-indicator {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

    .network-icon {
      .icon {
        width: 24rpx;
        height: 24rpx;
      }
    }

    .network-text {
      font-size: 24rpx;
      color: #666;
    }

    &.network-wifi {
      .network-text {
        color: #52c41a;
      }
    }

    &.network-5g,
    &.network-4g {
      .network-text {
        color: #1890ff;
      }
    }

    &.network-3g {
      .network-text {
        color: #faad14;
      }
    }

    &.network-2g,
    &.network-offline {
      .network-text {
        color: #ff4d4f;
      }
    }
  }
}

.network-detail {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
  overflow-y: auto;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 32rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .detail-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
    }

    .detail-close {
      font-size: 32rpx;
      color: #999;
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .detail-content {
    padding: 32rpx;

    .info-section,
    .stats-section,
    .optimization-section {
      margin-bottom: 40rpx;

      .info-title,
      .stats-title,
      .optimization-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 24rpx;
      }

      .info-grid,
      .stats-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 24rpx;

        .info-item,
        .stats-item {
          background: #f8f9fa;
          padding: 24rpx;
          border-radius: 12rpx;

          .info-label,
          .stats-label {
            display: block;
            font-size: 24rpx;
            color: #666;
            margin-bottom: 8rpx;
          }

          .info-value,
          .stats-number {
            font-size: 28rpx;
            font-weight: bold;
            color: #333;

            &.network-offline {
              color: #ff4d4f;
            }
          }
        }
      }

      .optimization-list {
        .optimization-item {
          display: flex;
          align-items: flex-start;
          padding: 24rpx;
          margin-bottom: 16rpx;
          background: #f8f9fa;
          border-radius: 12rpx;

          .optimization-icon {
            margin-right: 16rpx;

            .icon {
              width: 32rpx;
              height: 32rpx;
            }
          }

          .optimization-content {
            flex: 1;

            .optimization-title-text {
              display: block;
              font-size: 28rpx;
              font-weight: bold;
              color: #333;
              margin-bottom: 8rpx;
            }

            .optimization-desc {
              font-size: 24rpx;
              color: #666;
              line-height: 1.5;
            }
          }
        }
      }
    }

    .action-section {
      display: flex;
      flex-direction: column;
      gap: 16rpx;

      .action-button {
        padding: 24rpx;
        border-radius: 12rpx;
        font-size: 28rpx;
        border: none;
        background: #f5f5f5;
        color: #333;

        &:active {
          transform: scale(0.95);
        }
      }
    }
  }
}

.speed-test-result {
  background: white;
  border-radius: 16rpx;
  padding: 32rpx;
  margin: 32rpx;
  text-align: center;

  .result-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;

    .result-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
    }

    .result-close {
      font-size: 32rpx;
      color: #999;
      width: 48rpx;
      height: 48rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .result-content {
    .speed-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f5f5f5;

      .speed-label {
        font-size: 28rpx;
        color: #666;
      }

      .speed-value {
        font-size: 32rpx;
        font-weight: bold;
        color: #1890ff;
      }
    }

    .speed-quality {
      margin-top: 32rpx;
      padding: 24rpx;
      border-radius: 12rpx;
      font-size: 28rpx;
      font-weight: bold;

      &.speed-excellent {
        background: #f6ffed;
        color: #52c41a;
        border: 1rpx solid #b7eb8f;
      }

      &.speed-good {
        background: #e6f7ff;
        color: #1890ff;
        border: 1rpx solid #91d5ff;
      }

      &.speed-fair {
        background: #fffbe6;
        color: #faad14;
        border: 1rpx solid #ffe58f;
      }

      &.speed-poor {
        background: #fff2f0;
        color: #ff4d4f;
        border: 1rpx solid #ffccc7;
      }
    }
  }
}
</style>