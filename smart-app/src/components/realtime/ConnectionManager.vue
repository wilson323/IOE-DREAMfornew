<template>
  <view class="connection-manager">
    <!-- 连接状态指示器 -->
    <view class="status-indicator" :class="statusClass">
      <view class="status-dot"></view>
      <text class="status-text">{{ statusText }}</text>
    </view>

    <!-- 连接详情弹窗 -->
    <uni-popup ref="detailPopup" type="bottom" :mask-click="false">
      <view class="connection-detail">
        <view class="detail-header">
          <text class="detail-title">连接详情</text>
          <text class="detail-close" @click="closeDetail">✕</text>
        </view>

        <view class="detail-content">
          <!-- 基本状态信息 -->
          <view class="info-section">
            <view class="info-item">
              <text class="info-label">连接状态</text>
              <text class="info-value" :class="statusClass">{{ statusText }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">连接时长</text>
              <text class="info-value">{{ formatDuration(connectionDuration) }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">网络类型</text>
              <text class="info-value">{{ networkStatus.networkType }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">重连次数</text>
              <text class="info-value">{{ connectionInfo.reconnectCount }}</text>
            </view>
          </view>

          <!-- 统计信息 -->
          <view class="stats-section">
            <view class="stats-title">消息统计</view>
            <view class="stats-grid">
              <view class="stats-item">
                <text class="stats-number">{{ statistics.totalMessages }}</text>
                <text class="stats-label">总消息</text>
              </view>
              <view class="stats-item">
                <text class="stats-number">{{ statistics.sentMessages }}</text>
                <text class="stats-label">已发送</text>
              </view>
              <view class="stats-item">
                <text class="stats-number">{{ statistics.receivedMessages }}</text>
                <text class="stats-label">已接收</text>
              </view>
              <view class="stats-item">
                <text class="stats-number">{{ formatBytes(statistics.totalBytes) }}</text>
                <text class="stats-label">数据量</text>
              </view>
            </view>
          </view>

          <!-- 网络质量 -->
          <view class="quality-section">
            <view class="quality-item">
              <text class="quality-label">连接质量</text>
              <text class="quality-value" :class="qualityClass">{{ connectionQualityText }}</text>
            </view>
            <view class="quality-item">
              <text class="quality-label">平均延迟</text>
              <text class="quality-value">{{ statistics.averageLatency }}ms</text>
            </view>
            <view class="quality-item" v-if="appState.batteryLevel !== undefined">
              <text class="quality-label">电池电量</text>
              <text class="quality-value">{{ appState.batteryLevel }}%</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="action-section">
            <button
              v-if="!isConnected"
              class="action-button primary"
              @click="handleConnect"
              :disabled="isConnecting"
            >
              {{ isConnecting ? '连接中...' : '重新连接' }}
            </button>
            <button
              v-else
              class="action-button"
              @click="handleDisconnect"
            >
              断开连接
            </button>
            <button class="action-button" @click="handleTest">
              测试连接
            </button>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 网络状态提示 -->
    <uni-popup ref="networkPopup" type="message">
      <uni-popup-message type="error" :message="networkMessage" :duration="3000" />
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRealtimeStore } from '@/store/realtime'

// Props
const props = defineProps({
  // 显示模式: minimal, compact, full
  mode: {
    type: String,
    default: 'compact',
    validator: (value) => ['minimal', 'compact', 'full'].includes(value)
  },
  // 是否显示详情按钮
  showDetail: {
    type: Boolean,
    default: true
  },
  // 自动刷新间隔
  refreshInterval: {
    type: Number,
    default: 1000
  }
})

// Emits
const emit = defineEmits([
  'status-changed',
  'connect',
  'disconnect',
  'test'
])

// Store
const realtimeStore = useRealtimeStore()

// 弹窗引用
const detailPopup = ref(null)
const networkPopup = ref(null)

// 响应式数据
const networkMessage = ref('')
const refreshTimer = ref(null)

// 计算属性
const status = computed(() => realtimeStore.status)
const connectionInfo = computed(() => realtimeStore.connectionInfo)
const statistics = computed(() => realtimeStore.statistics)
const networkStatus = computed(() => realtimeStore.networkStatus)
const appState = computed(() => realtimeStore.appState)
const isConnected = computed(() => realtimeStore.isConnected)
const isConnecting = computed(() => realtimeStore.isConnecting)
const connectionQuality = computed(() => realtimeStore.getConnectionQuality)

const statusText = computed(() => {
  const statusMap = {
    connected: '已连接',
    connecting: '连接中',
    disconnecting: '断开中',
    disconnected: '未连接',
    reconnecting: '重连中',
    error: '连接错误'
  }
  return statusMap[status.value] || status.value
})

const statusClass = computed(() => {
  return `status-${status.value}`
})

const connectionDuration = computed(() => {
  if (!connectionInfo.value.connectedAt) return 0
  return Math.floor((Date.now() - connectionInfo.value.connectedAt) / 1000)
})

const connectionQualityText = computed(() => {
  const qualityMap = {
    excellent: '优秀',
    good: '良好',
    fair: '一般',
    poor: '较差'
  }
  return qualityMap[connectionQuality.value] || connectionQuality.value
})

const qualityClass = computed(() => {
  return `quality-${connectionQuality.value}`
})

// 方法
/**
 * 格式化时长
 */
const formatDuration = (seconds) => {
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${hours}小时${minutes}分钟`
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
 * 显示连接详情
 */
const showDetail = () => {
  if (detailPopup.value) {
    detailPopup.value.open()
  }
}

/**
 * 关闭连接详情
 */
const closeDetail = () => {
  if (detailPopup.value) {
    detailPopup.value.close()
  }
}

/**
 * 显示网络提示
 */
const showNetworkMessage = (message, type = 'error') => {
  networkMessage.value = message
  if (networkPopup.value) {
    networkPopup.value.open()
    setTimeout(() => {
      networkPopup.value.close()
    }, 3000)
  }
}

/**
 * 处理连接
 */
const handleConnect = async () => {
  try {
    emit('connect')
    await realtimeStore.connect()
    uni.showToast({
      title: '连接成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('连接失败:', error)
    uni.showToast({
      title: '连接失败',
      icon: 'none'
    })
  }
}

/**
 * 处理断开连接
 */
const handleDisconnect = async () => {
  try {
    emit('disconnect')
    await realtimeStore.disconnect()
    uni.showToast({
      title: '已断开连接',
      icon: 'success'
    })
  } catch (error) {
    console.error('断开连接失败:', error)
  }
}

/**
 * 处理测试连接
 */
const handleTest = async () => {
  try {
    emit('test')
    await realtimeStore.sendMessage({
      type: 'test',
      data: { timestamp: Date.now() }
    })
    uni.showToast({
      title: '测试消息已发送',
      icon: 'success'
    })
  } catch (error) {
    console.error('测试连接失败:', error)
    uni.showToast({
      title: '测试失败',
      icon: 'none'
    })
  }
}

/**
 * 开始自动刷新
 */
const startAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
  }

  if (props.refreshInterval > 0) {
    refreshTimer.value = setInterval(() => {
      // 自动更新逻辑可以在这里实现
    }, props.refreshInterval)
  }
}

/**
 * 停止自动刷新
 */
const stopAutoRefresh = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

/**
 * 监听网络状态变化
 */
const setupNetworkListener = () => {
  uni.onNetworkStatusChange((res) => {
    if (!res.isConnected) {
      showNetworkMessage('网络连接已断开')
    } else if (status.value === 'disconnected') {
      showNetworkMessage('网络已恢复，正在重新连接...')
      handleConnect()
    }
  })
}

// 监听状态变化
const setupStatusListener = () => {
  // 监听连接状态变化
  const checkStatus = () => {
    emit('status-changed', {
      status: status.value,
      connectionInfo: connectionInfo.value,
      statistics: statistics.value
    })
  }

  // 定期检查状态
  const statusTimer = setInterval(checkStatus, 1000)

  onUnmounted(() => {
    clearInterval(statusTimer)
  })
}

// 生命周期
onMounted(() => {
  setupNetworkListener()
  setupStatusListener()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

// 暴露方法
defineExpose({
  showDetail,
  closeDetail,
  handleConnect,
  handleDisconnect,
  handleTest
})
</script>

<style lang="scss" scoped>
.connection-manager {
  .status-indicator {
    display: flex;
    align-items: center;
    gap: 8rpx;
    padding: 8rpx 16rpx;
    border-radius: 20rpx;
    background: rgba(255, 255, 255, 0.9);

    .status-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
      background: #999;
    }

    .status-text {
      font-size: 24rpx;
      color: #666;
    }

    &.status-connected {
      .status-dot {
        background: #52c41a;
        box-shadow: 0 0 8rpx rgba(82, 196, 26, 0.6);
      }
      .status-text {
        color: #52c41a;
      }
    }

    &.status-connecting {
      .status-dot {
        background: #1890ff;
        animation: pulse 1.5s infinite;
      }
      .status-text {
        color: #1890ff;
      }
    }

    &.status-reconnecting {
      .status-dot {
        background: #faad14;
        animation: pulse 1s infinite;
      }
      .status-text {
        color: #faad14;
      }
    }

    &.status-error {
      .status-dot {
        background: #ff4d4f;
      }
      .status-text {
        color: #ff4d4f;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.connection-detail {
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

    .info-section {
      margin-bottom: 40rpx;

      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 0;
        border-bottom: 1rpx solid #f5f5f5;

        .info-label {
          font-size: 28rpx;
          color: #666;
        }

        .info-value {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;

          &.status-connected {
            color: #52c41a;
          }

          &.status-error {
            color: #ff4d4f;
          }

          &.status-connecting,
          &.status-reconnecting {
            color: #1890ff;
          }
        }
      }
    }

    .stats-section {
      margin-bottom: 40rpx;

      .stats-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 24rpx;
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 24rpx;

        .stats-item {
          text-align: center;
          padding: 24rpx;
          background: #f8f9fa;
          border-radius: 16rpx;

          .stats-number {
            display: block;
            font-size: 36rpx;
            font-weight: bold;
            color: #1890ff;
            margin-bottom: 8rpx;
          }

          .stats-label {
            font-size: 24rpx;
            color: #666;
          }
        }
      }
    }

    .quality-section {
      margin-bottom: 40rpx;

      .quality-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 0;
        border-bottom: 1rpx solid #f5f5f5;

        .quality-label {
          font-size: 28rpx;
          color: #666;
        }

        .quality-value {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;

          &.quality-excellent {
            color: #52c41a;
          }

          &.quality-good {
            color: #1890ff;
          }

          &.quality-fair {
            color: #faad14;
          }

          &.quality-poor {
            color: #ff4d4f;
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

        &.primary {
          background: #1890ff;
          color: white;
        }

        &:disabled {
          opacity: 0.6;
        }
      }
    }
  }
}
</style>