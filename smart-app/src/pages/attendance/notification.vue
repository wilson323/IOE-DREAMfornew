<template>
  <view class="notification-container">
    <!-- 顶部标签页 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'all' }"
        @click="switchTab('all')"
      >
        <text class="tab-text">全部</text>
        <view class="badge" v-if="unreadCount > 0">{{ unreadCount }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'approval' }"
        @click="switchTab('approval')"
      >
        <text class="tab-text">审批</text>
        <view class="badge" v-if="approvalUnread > 0">{{ approvalUnread }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'attendance' }"
        @click="switchTab('attendance')"
      >
        <text class="tab-text">考勤</text>
        <view class="badge" v-if="attendanceUnread > 0">{{ attendanceUnread }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'system' }"
        @click="switchTab('system')"
      >
        <text class="tab-text">系统</text>
        <view class="badge" v-if="systemUnread > 0">{{ systemUnread }}</view>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view class="batch-actions" v-if="selectedMessages.length > 0">
      <text class="selected-count">已选择 {{ selectedMessages.length }} 条</text>
      <view class="action-buttons">
        <button class="batch-btn read-btn" @click="markSelectedAsRead">
          <text class="btn-text">标为已读</text>
        </button>
        <button class="batch-btn delete-btn" @click="deleteSelected">
          <text class="btn-text">删除</text>
        </button>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="notification-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="refreshMessages"
      @scrolltolower="loadMoreMessages"
    >
      <!-- 全选 -->
      <view class="select-all-bar" v-if="messageList.length > 0">
        <view class="select-all-left" @click="toggleSelectAll">
          <view class="checkbox" :class="{ 'checked': isAllSelected }">
            <text class="check-icon" v-if="isAllSelected">✓</text>
          </view>
          <text class="select-all-text">全选</text>
        </view>
        <button class="read-all-btn" @click="markAllAsRead" v-if="hasUnread">
          <text class="btn-text">全部已读</text>
        </button>
      </view>

      <!-- 消息列表 -->
      <view class="message-list">
        <view
          class="message-item"
          v-for="message in messageList"
          :key="message.id"
          :class="{ 'unread': !message.isRead, 'selected': message.isSelected }"
          @click="selectMessage(message)"
        >
          <!-- 选择框 -->
          <view class="message-checkbox" @click.stop="toggleSelect(message)">
            <view class="checkbox" :class="{ 'checked': message.isSelected }">
              <text class="check-icon" v-if="message.isSelected">✓</text>
            </view>
          </view>

          <!-- 消息内容 -->
          <view class="message-content" @click="viewMessage(message)">
            <!-- 消息头部 -->
            <view class="message-header">
              <view class="message-left">
                <view class="message-icon" :class="getMessageTypeClass(message.type)">
                  <text class="icon-text">{{ getMessageIcon(message.type) }}</text>
                </view>
                <view class="message-title-row">
                  <text class="message-title">{{ message.title }}</text>
                  <text class="message-time">{{ formatTime(message.createTime) }}</text>
                </view>
              </view>
              <view class="unread-dot" v-if="!message.isRead"></view>
            </view>

            <!-- 消息摘要 -->
            <view class="message-summary">
              <text class="summary-text">{{ message.summary }}</text>
            </view>

            <!-- 消息标签 -->
            <view class="message-tags" v-if="message.tags && message.tags.length > 0">
              <view
                class="tag-item"
                v-for="(tag, index) in message.tags"
                :key="index"
                :class="getTagClass(tag.type)"
              >
                <text class="tag-text">{{ tag.text }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore">
        <text class="load-more-text">{{ loadingMore ? '加载中...' : '上拉加载更多' }}</text>
      </view>

      <!-- 没有更多 -->
      <view class="no-more" v-if="!hasMore && messageList.length > 0">
        <text class="no-more-text">没有更多消息了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="messageList.length === 0 && !loading">
        <text class="empty-icon">📬</text>
        <text class="empty-text">暂无消息通知</text>
      </view>
    </scroll-view>

    <!-- 消息详情弹窗 -->
    <uni-popup ref="messageDetailPopup" type="center">
      <view class="message-detail-popup">
        <view class="popup-header">
          <text class="popup-title">消息详情</text>
          <text class="close-btn" @click="closeMessageDetail">✕</text>
        </view>
        <view class="popup-content" v-if="currentMessage">
          <!-- 消息标题 -->
          <view class="detail-title-row">
            <view class="detail-icon" :class="getMessageTypeClass(currentMessage.type)">
              <text class="icon-text">{{ getMessageIcon(currentMessage.type) }}</text>
            </view>
            <text class="detail-title">{{ currentMessage.title }}</text>
          </view>

          <!-- 消息时间 -->
          <view class="detail-time">
            <text class="time-text">{{ formatTimeFull(currentMessage.createTime) }}</text>
          </view>

          <!-- 消息内容 -->
          <view class="detail-body">
            <text class="body-text">{{ currentMessage.content }}</text>
          </view>

          <!-- 消息标签 -->
          <view class="detail-tags" v-if="currentMessage.tags && currentMessage.tags.length > 0">
            <view
              class="tag-item"
              v-for="(tag, index) in currentMessage.tags"
              :key="index"
              :class="getTagClass(tag.type)"
            >
              <text class="tag-text">{{ tag.text }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="detail-actions" v-if="currentMessage.actionUrl">
            <button class="action-btn primary" @click="handleAction(currentMessage)">
              <text class="btn-text">查看详情</text>
            </button>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const activeTab = ref('all')
const messageList = ref([])
const refreshing = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 20

const unreadCount = ref(0)
const approvalUnread = ref(0)
const attendanceUnread = ref(0)
const systemUnread = ref(0)

const currentMessage = ref(null)
const messageDetailPopup = ref(null)

const selectedMessages = computed(() => {
  return messageList.value.filter(m => m.isSelected)
})

const isAllSelected = computed(() => {
  return messageList.value.length > 0 && messageList.value.every(m => m.isSelected)
})

const hasUnread = computed(() => {
  return messageList.value.some(m => !m.isRead)
})

onMounted(() => {
  loadMessages()
  loadUnreadCount()
})

const switchTab = (tab) => {
  activeTab.value = tab
  currentPage.value = 1
  messageList.value = []
  hasMore.value = true
  loadMessages()
}

const loadMessages = async () => {
  if (loading.value) return

  loading.value = true
  try {
    const params = {
      employeeId: 1001,
      pageNum: currentPage.value,
      pageSize: pageSize
    }

    if (activeTab.value !== 'all') {
      params.type = getTabType(activeTab.value)
    }

    const res = await attendanceApi.notificationApi.getMessageList(params)

    if (res.success && res.data) {
      const list = res.data.list || []

      if (currentPage.value === 1) {
        messageList.value = list
      } else {
        messageList.value = [...messageList.value, ...list]
      }

      hasMore.value = list.length >= pageSize
    }
  } catch (error) {
    console.error('[消息通知] 加载失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadMoreMessages = () => {
  if (!hasMore.value || loadingMore.value) return

  loadingMore.value = true
  currentPage.value++
  loadMessages().finally(() => {
    loadingMore.value = false
  })
}

const refreshMessages = () => {
  refreshing.value = true
  currentPage.value = 1
  loadMessages().finally(() => {
    refreshing.value = false
  })
}

const loadUnreadCount = async () => {
  try {
    const res = await attendanceApi.notificationApi.getUnreadCount({ employeeId: 1001 })

    if (res.success && res.data) {
      unreadCount.value = res.data.total || 0
      approvalUnread.value = res.data.approval || 0
      attendanceUnread.value = res.data.attendance || 0
      systemUnread.value = res.data.system || 0
    }
  } catch (error) {
    console.error('[未读数] 加载失败:', error)
  }
}

const getTabType = (tab) => {
  const map = {
    'approval': 'APPROVAL',
    'attendance': 'ATTENDANCE',
    'system': 'SYSTEM'
  }
  return map[tab]
}

const getMessageIcon = (type) => {
  const map = {
    'APPROVAL': '📝',
    'LEAVE_APPROVED': '✅',
    'LEAVE_REJECTED': '❌',
    'OVERTIME_APPROVED': '✅',
    'OVERTIME_REJECTED': '❌',
    'ATTENDANCE': '⏰',
    'LATE': '⚠️',
    'EARLY': '⚠️',
    'ABSENT': '🚨',
    'SYSTEM': '📢',
    'SCHEDULE_CHANGE': '📅',
    'ANNOUNCEMENT': '📢'
  }
  return map[type] || '📬'
}

const getMessageTypeClass = (type) => {
  const map = {
    'APPROVAL': 'type-approval',
    'LEAVE_APPROVED': 'type-success',
    'LEAVE_REJECTED': 'type-error',
    'OVERTIME_APPROVED': 'type-success',
    'OVERTIME_REJECTED': 'type-error',
    'ATTENDANCE': 'type-attendance',
    'LATE': 'type-warning',
    'EARLY': 'type-warning',
    'ABSENT': 'type-error',
    'SYSTEM': 'type-system',
    'SCHEDULE_CHANGE': 'type-info',
    'ANNOUNCEMENT': 'type-info'
  }
  return map[type] || 'type-default'
}

const getTagClass = (type) => {
  const map = {
    'urgent': 'tag-urgent',
    'important': 'tag-important',
    'info': 'tag-info'
  }
  return map[type] || 'tag-default'
}

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

const formatTimeFull = (time) => {
  return dayjs(time).format('YYYY年MM月DD日 HH:mm')
}

const toggleSelect = (message) => {
  message.isSelected = !message.isSelected
}

const toggleSelectAll = () => {
  const shouldSelect = !isAllSelected.value
  messageList.value.forEach(m => {
    m.isSelected = shouldSelect
  })
}

const selectMessage = (message) => {
  // 点击消息区域不做处理，避免与详情弹窗冲突
}

const viewMessage = async (message) => {
  currentMessage.value = message

  // 标记为已读
  if (!message.isRead) {
    try {
      await attendanceApi.notificationApi.markAsRead({
        messageId: message.id
      })
      message.isRead = true
      loadUnreadCount()
    } catch (error) {
      console.error('[标记已读] 失败:', error)
    }
  }

  if (messageDetailPopup.value) {
    messageDetailPopup.value.open()
  }
}

const closeMessageDetail = () => {
  if (messageDetailPopup.value) {
    messageDetailPopup.value.close()
  }
}

const markSelectedAsRead = async () => {
  const ids = selectedMessages.value.map(m => m.id)
  if (ids.length === 0) return

  try {
    const res = await attendanceApi.notificationApi.batchMarkAsRead({ messageIds: ids })

    if (res.success) {
      messageList.value.forEach(m => {
        if (ids.includes(m.id)) {
          m.isRead = true
          m.isSelected = false
        }
      })
      loadUnreadCount()
      uni.showToast({ title: '标记成功', icon: 'success' })
    }
  } catch (error) {
    console.error('[批量标记已读] 失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const markAllAsRead = async () => {
  try {
    const params = {
      employeeId: 1001
    }

    if (activeTab.value !== 'all') {
      params.type = getTabType(activeTab.value)
    }

    const res = await attendanceApi.notificationApi.markAllAsRead(params)

    if (res.success) {
      messageList.value.forEach(m => {
        m.isRead = true
      })
      loadUnreadCount()
      uni.showToast({ title: '全部已读', icon: 'success' })
    }
  } catch (error) {
    console.error('[全部已读] 失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

const deleteSelected = async () => {
  const ids = selectedMessages.value.map(m => m.id)
  if (ids.length === 0) return

  uni.showModal({
    title: '确认删除',
    content: `确定要删除选中的 ${ids.length} 条消息吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await attendanceApi.notificationApi.batchDelete({ messageIds: ids })

          if (result.success) {
            messageList.value = messageList.value.filter(m => !ids.includes(m.id))
            loadUnreadCount()
            uni.showToast({ title: '删除成功', icon: 'success' })
          }
        } catch (error) {
          console.error('[批量删除] 失败:', error)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const handleAction = (message) => {
  closeMessageDetail()

  if (message.actionUrl) {
    uni.navigateTo({ url: message.actionUrl })
  }
}
</script>

<style lang="scss" scoped>
.notification-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.tabs {
  display: flex;
  background: white;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.tab-item.active .tab-text {
  color: #667eea;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background: #667eea;
  border-radius: 2rpx;
}

.tab-text {
  font-size: 28rpx;
  color: #666;
}

.badge {
  position: absolute;
  top: 5rpx;
  right: 20%;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  padding: 0 8rpx;
  background-color: #ef4444;
  color: white;
  font-size: 20rpx;
  border-radius: 16rpx;
  text-align: center;
}

.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #fef3c7;
}

.selected-count {
  font-size: 26rpx;
  color: #92400e;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 15rpx;
}

.batch-btn {
  padding: 10rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  border: none;
}

.batch-btn.read-btn {
  background-color: #22c55e;
  color: white;
}

.batch-btn.delete-btn {
  background-color: #ef4444;
  color: white;
}

.notification-list {
  height: calc(100vh - 120rpx);
}

.select-all-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: white;
  border-bottom: 1rpx solid #f0f0f0;
}

.select-all-left {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.checkbox {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox.checked {
  background-color: #667eea;
  border-color: #667eea;
}

.check-icon {
  color: white;
  font-size: 24rpx;
  font-weight: bold;
}

.select-all-text {
  font-size: 26rpx;
  color: #666;
}

.read-all-btn {
  padding: 8rpx 20rpx;
  background-color: #667eea;
  color: white;
  border-radius: 20rpx;
  font-size: 22rpx;
  border: none;
}

.message-list {
  padding: 0 30rpx;
}

.message-item {
  display: flex;
  background: white;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;

  &.unread {
    background-color: #f0f9ff;
  }

  &.selected {
    background-color: #fef3c7;
  }
}

.message-checkbox {
  padding: 30rpx 15rpx 30rpx 30rpx;
  display: flex;
  align-items: flex-start;
}

.message-content {
  flex: 1;
  padding: 30rpx 30rpx 30rpx 15rpx;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15rpx;
}

.message-left {
  display: flex;
  align-items: center;
  gap: 15rpx;
  flex: 1;
}

.message-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-approval { background-color: #e0f2fe; }
.type-success { background-color: #f0fdf4; }
.type-error { background-color: #fef2f2; }
.type-attendance { background-color: #fef3c7; }
.type-warning { background-color: #fef3c7; }
.type-system { background-color: #f3f4f6; }
.type-info { background-color: #e0e7ff; }
.type-default { background-color: #f8f9fa; }

.icon-text {
  font-size: 32rpx;
}

.message-title-row {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.message-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.message-time {
  font-size: 22rpx;
  color: #999;
}

.unread-dot {
  width: 16rpx;
  height: 16rpx;
  background-color: #ef4444;
  border-radius: 50%;
}

.message-summary {
  margin-bottom: 15rpx;
}

.summary-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.message-tags {
  display: flex;
  gap: 10rpx;
  flex-wrap: wrap;
}

.tag-item {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}

.tag-urgent { background-color: #fef2f2; color: #ef4444; }
.tag-important { background-color: #fef3c7; color: #f59e0b; }
.tag-info { background-color: #e0f2fe; color: #0ea5e9; }
.tag-default { background-color: #f3f4f6; color: #6b7280; }

.load-more, .no-more {
  padding: 30rpx;
  text-align: center;
}

.load-more-text, .no-more-text {
  font-size: 24rpx;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 20rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.message-detail-popup {
  width: 600rpx;
  background: white;
  border-radius: 16rpx;
  overflow: hidden;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.popup-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 32rpx;
  color: #999;
  padding: 10rpx;
}

.popup-content {
  padding: 30rpx;
  max-height: 70vh;
  overflow-y: auto;
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.detail-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-icon .icon-text {
  font-size: 40rpx;
}

.detail-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.detail-time {
  margin-bottom: 30rpx;
}

.time-text {
  font-size: 24rpx;
  color: #999;
}

.detail-body {
  background-color: #f8f9fa;
  border-radius: 12rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
}

.body-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.8;
}

.detail-tags {
  display: flex;
  gap: 10rpx;
  flex-wrap: wrap;
  margin-bottom: 30rpx;
}

.detail-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  flex: 1;
  padding: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}
</style>
