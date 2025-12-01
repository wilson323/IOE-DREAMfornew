<template>
  <view class="notification-manager">
    <!-- 通知列表 -->
    <view class="notification-list" v-if="notifications.length > 0">
      <view
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="{ 'unread': !notification.read }"
        @click="handleNotificationClick(notification)"
      >
        <view class="notification-icon">
          <image :src="getNotificationIcon(notification.type)" class="icon" />
        </view>
        <view class="notification-content">
          <view class="notification-title">{{ notification.title }}</view>
          <view class="notification-message">{{ notification.content }}</view>
          <view class="notification-time">{{ formatTime(notification.timestamp) }}</view>
        </view>
        <view class="notification-actions">
          <view
            v-if="!notification.read"
            class="unread-dot"
            @click.stop="markAsRead(notification.id)"
          ></view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <image src="/static/images/empty-notification.png" class="empty-image" />
      <text class="empty-text">暂无通知</text>
    </view>

    <!-- 通知详情弹窗 -->
    <uni-popup ref="detailPopup" type="center">
      <view class="notification-detail">
        <view class="detail-header">
          <text class="detail-title">{{ selectedNotification?.title }}</text>
          <text class="detail-close" @click="closeDetail">✕</text>
        </view>
        <view class="detail-content">
          <text class="detail-message">{{ selectedNotification?.content }}</text>
          <text class="detail-time">{{ formatTime(selectedNotification?.timestamp) }}</text>
        </view>
        <view class="detail-actions">
          <button class="action-button" @click="markAsRead(selectedNotification?.id)">
            标记已读
          </button>
          <button class="action-button" @click="deleteNotification(selectedNotification?.id)">
            删除
          </button>
        </view>
      </view>
    </uni-popup>

    <!-- 批量操作栏 -->
    <view class="batch-actions" v-if="hasUnreadNotifications">
      <button class="batch-button" @click="markAllAsRead">
        全部已读 ({{ unreadCount }})
      </button>
      <button class="batch-button" @click="clearAllNotifications">
        清空通知
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRealtimeStore } from '@/store/realtime'

// Props
const props = defineProps({
  // 最大显示数量
  maxDisplay: {
    type: Number,
    default: 10
  },
  // 是否显示时间
  showTime: {
    type: Boolean,
    default: true
  },
  // 自动刷新间隔
  refreshInterval: {
    type: Number,
    default: 5000
  }
})

// Emits
const emit = defineEmits([
  'notification-click',
  'notification-read',
  'notification-delete',
  'batch-read',
  'batch-clear'
])

// Store
const realtimeStore = useRealtimeStore()

// 弹窗引用
const detailPopup = ref(null)

// 响应式数据
const selectedNotification = ref(null)
const refreshTimer = ref(null)

// 计算属性
const notifications = computed(() => {
  return realtimeStore.notifications.slice(0, props.maxDisplay)
})

const unreadCount = computed(() => {
  return realtimeStore.notifications.filter(n => !n.read).length
})

const hasUnreadNotifications = computed(() => {
  return unreadCount.value > 0
})

// 方法
/**
 * 获取通知图标
 */
const getNotificationIcon = (type) => {
  const iconMap = {
    info: '/static/icons/notification-info.png',
    success: '/static/icons/notification-success.png',
    warning: '/static/icons/notification-warning.png',
    error: '/static/icons/notification-error.png',
    alert: '/static/icons/notification-alert.png',
    device: '/static/icons/notification-device.png',
    permission: '/static/icons/notification-permission.png'
  }
  return iconMap[type] || iconMap.info
}

/**
 * 格式化时间
 */
const formatTime = (timestamp) => {
  if (!timestamp) return ''

  const now = Date.now()
  const diff = now - timestamp

  if (diff < 60000) { // 小于1分钟
    return '刚刚'
  } else if (diff < 3600000) { // 小于1小时
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) { // 小于1天
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    const date = new Date(timestamp)
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  }
}

/**
 * 处理通知点击
 */
const handleNotificationClick = (notification) => {
  selectedNotification.value = notification

  if (detailPopup.value) {
    detailPopup.value.open()
  }

  emit('notification-click', notification)

  // 自动标记为已读
  if (!notification.read) {
    markAsRead(notification.id)
  }
}

/**
 * 关闭详情弹窗
 */
const closeDetail = () => {
  if (detailPopup.value) {
    detailPopup.value.close()
  }
  selectedNotification.value = null
}

/**
 * 标记为已读
 */
const markAsRead = (notificationId) => {
  if (!notificationId) return

  realtimeStore.markNotificationAsRead(notificationId)
  emit('notification-read', notificationId)

  uni.showToast({
    title: '已标记为已读',
    icon: 'success',
    duration: 1500
  })
}

/**
 * 删除通知
 */
const deleteNotification = (notificationId) => {
  if (!notificationId) return

  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条通知吗？',
    success: (res) => {
      if (res.confirm) {
        const index = realtimeStore.notifications.findIndex(n => n.id === notificationId)
        if (index > -1) {
          realtimeStore.notifications.splice(index, 1)
          emit('notification-delete', notificationId)

          uni.showToast({
            title: '删除成功',
            icon: 'success',
            duration: 1500
          })

          closeDetail()
        }
      }
    }
  })
}

/**
 * 全部标记为已读
 */
const markAllAsRead = () => {
  const unreadIds = realtimeStore.notifications
    .filter(n => !n.read)
    .map(n => n.id)

  if (unreadIds.length === 0) return

  unreadIds.forEach(id => {
    realtimeStore.markNotificationAsRead(id)
  })

  emit('batch-read', unreadIds)

  uni.showToast({
    title: `已标记 ${unreadIds.length} 条为已读`,
    icon: 'success',
    duration: 2000
  })
}

/**
 * 清空所有通知
 */
const clearAllNotifications = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有通知吗？此操作不可恢复。',
    success: (res) => {
      if (res.confirm) {
        const count = realtimeStore.notifications.length
        realtimeStore.notifications.splice(0)

        emit('batch-clear', count)

        uni.showToast({
          title: `已清空 ${count} 条通知`,
          icon: 'success',
          duration: 2000
        })
      }
    }
  })
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
      // 刷新逻辑
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
 * 设置推送通知
 */
const setupPushNotifications = () => {
  // #ifdef APP-PLUS
  try {
    // 请求推送权限
    plus.push.createMessage("实时通知已启用", "SmartAdmin", {}, {
      cover: false,
      sound: "system"
    })

    // 监听推送消息
    plus.push.addEventListener('click', (message) => {
      console.log('收到推送消息点击:', message)
      // 处理推送消息点击
    })

  } catch (error) {
    console.warn('推送通知设置失败:', error)
  }
  // #endif
}

/**
 * 设置本地通知权限
 */
const requestNotificationPermission = () => {
  // #ifdef APP-PLUS
  try {
    const NotificationManager = plus.android.importClass("android.app.NotificationManager")
    const main = plus.android.runtimeMainActivity()
    const notificationManager = main.getSystemService("notification")

    if (!notificationManager.areNotificationsEnabled()) {
      uni.showModal({
        title: '开启通知权限',
        content: '为了及时接收重要消息，请开启通知权限',
        confirmText: '去设置',
        success: (res) => {
          if (res.confirm) {
            // 跳转到系统设置
            const Intent = plus.android.importClass("android.content.Intent")
            const Settings = plus.android.importClass("android.provider.Settings")
            const intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            main.startActivity(intent)
          }
        }
      })
    }
  } catch (error) {
    console.warn('通知权限检查失败:', error)
  }
  // #endif
}

// 生命周期
onMounted(() => {
  setupPushNotifications()
  requestNotificationPermission()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

// 暴露方法
defineExpose({
  markAsRead,
  deleteNotification,
  markAllAsRead,
  clearAllNotifications,
  refresh: () => {
    // 手动刷新逻辑
  }
})
</script>

<style lang="scss" scoped>
.notification-manager {
  background: #f8f9fa;
  min-height: 100vh;

  .notification-list {
    padding: 16rpx;

    .notification-item {
      display: flex;
      align-items: flex-start;
      padding: 24rpx;
      margin-bottom: 16rpx;
      background: white;
      border-radius: 16rpx;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;

      &.unread {
        border-left: 8rpx solid #1890ff;
        background: linear-gradient(to right, rgba(24, 144, 255, 0.05), white);
      }

      &:active {
        transform: scale(0.98);
      }

      .notification-icon {
        margin-right: 24rpx;

        .icon {
          width: 48rpx;
          height: 48rpx;
        }
      }

      .notification-content {
        flex: 1;

        .notification-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
          margin-bottom: 8rpx;
          line-height: 1.4;
        }

        .notification-message {
          font-size: 28rpx;
          color: #666;
          line-height: 1.5;
          margin-bottom: 12rpx;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .notification-time {
          font-size: 24rpx;
          color: #999;
        }
      }

      .notification-actions {
        display: flex;
        align-items: center;
        margin-left: 16rpx;

        .unread-dot {
          width: 16rpx;
          height: 16rpx;
          background: #ff4d4f;
          border-radius: 50%;
          animation: pulse 2s infinite;
        }
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 40rpx;

    .empty-image {
      width: 200rpx;
      height: 200rpx;
      margin-bottom: 32rpx;
      opacity: 0.6;
    }

    .empty-text {
      font-size: 28rpx;
      color: #999;
    }
  }

  .batch-actions {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 24rpx;
    background: white;
    border-top: 1rpx solid #f0f0f0;
    display: flex;
    gap: 16rpx;

    .batch-button {
      flex: 1;
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

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}

.notification-detail {
  background: white;
  border-radius: 16rpx;
  padding: 32rpx;
  margin: 32rpx;
  max-height: 80vh;
  overflow-y: auto;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .detail-title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;
      flex: 1;
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
    margin-bottom: 32rpx;

    .detail-message {
      font-size: 30rpx;
      color: #333;
      line-height: 1.6;
      margin-bottom: 16rpx;
    }

    .detail-time {
      font-size: 26rpx;
      color: #999;
    }
  }

  .detail-actions {
    display: flex;
    gap: 16rpx;

    .action-button {
      flex: 1;
      padding: 24rpx;
      border-radius: 12rpx;
      font-size: 28rpx;
      border: none;
      background: #1890ff;
      color: white;

      &:active {
        transform: scale(0.95);
      }
    }
  }
}
</style>