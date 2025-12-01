<template>
  <view class="notification-page">
    <view class="header">
      <text class="title">消息通知</text>
      <text class="subtitle">未读消息: {{ unreadCount }}</text>
    </view>

    <view class="content">
      <!-- 通知过滤 -->
      <view class="filter-section">
        <scroll-view scroll-x class="filter-tabs">
          <view
            v-for="(tab, index) in filterTabs"
            :key="index"
            class="filter-tab"
            :class="{ active: activeTab === tab.type }"
            @click="switchTab(tab.type)"
          >
            <text class="tab-text">{{ tab.name }}</text>
            <view v-if="tab.count > 0" class="tab-badge">
              <text class="badge-text">{{ tab.count }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 批量操作 -->
      <view class="batch-section">
        <view class="batch-left">
          <checkbox
            :checked="isAllSelected"
            @change="toggleSelectAll"
            color="#667eea"
          />
          <text class="batch-text">全选</text>
        </view>
        <view class="batch-right">
          <button class="batch-btn mark-read" @click="markSelectedAsRead" :disabled="selectedIds.length === 0">
            <text>标记已读</text>
          </button>
          <button class="batch-btn delete" @click="deleteSelected" :disabled="selectedIds.length === 0">
            <text>删除</text>
          </button>
        </view>
      </view>

      <!-- 通知列表 -->
      <scroll-view
        scroll-y
        class="notification-list"
        @scrolltolower="loadMoreNotifications"
        refresher-enabled
        :refresher-triggered="isRefreshing"
        @refresherrefresh="refreshNotifications"
      >
        <view v-if="filteredNotifications.length === 0" class="empty-state">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无{{ currentTabName }}消息</text>
        </view>

        <view
          v-for="(notification, index) in filteredNotifications"
          :key="notification.id"
          class="notification-item"
          :class="{
            unread: !notification.read,
            selected: selectedIds.includes(notification.id)
          }"
          @click="selectNotification(notification)"
        >
          <view class="item-left">
            <checkbox
              :checked="selectedIds.includes(notification.id)"
              @change="toggleSelection(notification.id)"
              color="#667eea"
              @click.stop
            />
            <view class="notification-icon" :class="notification.type">
              <text class="icon-text">{{ getNotificationIcon(notification.type) }}</text>
            </view>
          </view>

          <view class="item-content" @click="openNotification(notification)">
            <view class="content-header">
              <text class="notification-title">{{ notification.title }}</text>
              <text class="notification-time">{{ formatTime(notification.timestamp) }}</text>
            </view>
            <text class="notification-desc">{{ notification.content }}</text>
            <view v-if="notification.actions && notification.actions.length > 0" class="notification-actions">
              <button
                v-for="(action, actionIndex) in notification.actions"
                :key="actionIndex"
                class="action-btn"
                :class="action.type"
                @click.stop="handleAction(notification, action)"
              >
                <text>{{ action.text }}</text>
              </button>
            </view>
          </view>

          <view class="item-right">
            <view v-if="!notification.read" class="unread-dot"></view>
            <button class="more-btn" @click.stop="showNotificationMenu(notification)">
              <text class="more-icon">⋯</text>
            </button>
          </view>
        </view>
      </scroll-view>

      <!-- 加载更多 -->
      <view v-if="hasMore" class="load-more">
        <text class="load-text">{{ isLoading ? '加载中...' : '上拉加载更多' }}</text>
      </view>
    </view>

    <!-- 通知详情弹窗 -->
    <uni-popup ref="notificationDetail" type="bottom" :mask-click="false">
      <view class="detail-popup">
        <view class="detail-header">
          <text class="detail-title">消息详情</text>
          <button class="close-btn" @click="closeDetail">
            <text class="close-icon">×</text>
          </button>
        </view>
        <view v-if="selectedNotification" class="detail-content">
          <view class="detail-info">
            <view class="info-item">
              <text class="info-label">类型</text>
              <text class="info-value">{{ getNotificationTypeName(selectedNotification.type) }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">时间</text>
              <text class="info-value">{{ formatFullTime(selectedNotification.timestamp) }}</text>
            </view>
            <view class="info-item">
              <text class="info-label">标题</text>
              <text class="info-value">{{ selectedNotification.title }}</text>
            </view>
          </view>
          <view class="detail-body">
            <text class="detail-text">{{ selectedNotification.content }}</text>
          </view>
          <view v-if="selectedNotification.actions" class="detail-actions">
            <button
              v-for="(action, index) in selectedNotification.actions"
              :key="index"
              class="detail-action-btn"
              :class="action.type"
              @click="handleDetailAction(selectedNotification, action)"
            >
              <text>{{ action.text }}</text>
            </button>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 通知设置弹窗 -->
    <uni-popup ref="notificationSettings" type="bottom">
      <view class="settings-popup">
        <view class="settings-header">
          <text class="settings-title">通知设置</text>
          <button class="close-btn" @click="closeSettings">
            <text class="close-icon">×</text>
          </button>
        </view>
        <view class="settings-content">
          <view class="setting-group">
            <text class="group-title">推送设置</text>
            <view class="setting-item">
              <text class="setting-label">推送通知</text>
              <switch
                :checked="settings.pushEnabled"
                @change="togglePushNotification"
                color="#667eea"
              />
            </view>
            <view class="setting-item">
              <text class="setting-label">声音提醒</text>
              <switch
                :checked="settings.soundEnabled"
                @change="toggleSound"
                color="#667eea"
              />
            </view>
            <view class="setting-item">
              <text class="setting-label">震动提醒</text>
              <switch
                :checked="settings.vibrationEnabled"
                @change="toggleVibration"
                color="#667eea"
              />
            </view>
          </view>
          <view class="setting-group">
            <text class="group-title">消息类型</text>
            <view
              v-for="typeConfig in notificationTypes"
              :key="typeConfig.type"
              class="setting-item"
            >
              <text class="setting-label">{{ typeConfig.name }}</text>
              <switch
                :checked="typeConfig.enabled"
                @change="toggleNotificationType(typeConfig.type)"
                color="#667eea"
              />
            </view>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
export default {
  name: 'NotificationManager',
  data() {
    return {
      unreadCount: 0,
      activeTab: 'all',
      notifications: [],
      selectedIds: [],
      isRefreshing: false,
      isLoading: false,
      hasMore: true,
      page: 1,
      pageSize: 20,
      selectedNotification: null,

      filterTabs: [
        { type: 'all', name: '全部', count: 0 },
        { type: 'system', name: '系统', count: 0 },
        { type: 'attendance', name: '考勤', count: 0 },
        { type: 'access', name: '门禁', count: 0 },
        { type: 'consume', name: '消费', count: 0 },
        { type: 'security', name: '安全', count: 0 }
      ],

      notificationTypes: [
        { type: 'system', name: '系统通知', enabled: true },
        { type: 'attendance', name: '考勤提醒', enabled: true },
        { type: 'access', name: '门禁通知', enabled: true },
        { type: 'consume', name: '消费提醒', enabled: true },
        { type: 'security', name: '安全警告', enabled: true }
      ],

      settings: {
        pushEnabled: true,
        soundEnabled: true,
        vibrationEnabled: true
      }
    }
  },

  computed: {
    filteredNotifications() {
      if (this.activeTab === 'all') {
        return this.notifications
      }
      return this.notifications.filter(n => n.type === this.activeTab)
    },

    currentTabName() {
      const tab = this.filterTabs.find(t => t.type === this.activeTab)
      return tab ? tab.name : ''
    },

    isAllSelected() {
      return this.filteredNotifications.length > 0 &&
             this.selectedIds.length === this.filteredNotifications.length
    }
  },

  mounted() {
    this.loadNotifications()
    this.loadSettings()
    this.initializePushService()
    this.updateTabCounts()
  },

  methods: {
    async loadNotifications(refresh = false) {
      if (refresh) {
        this.page = 1
        this.hasMore = true
      }

      try {
        this.isLoading = true

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 800))

        const mockNotifications = this.generateMockNotifications(this.page, this.pageSize)

        if (refresh) {
          this.notifications = mockNotifications
        } else {
          this.notifications = [...this.notifications, ...mockNotifications]
        }

        // 更新未读数量
        this.unreadCount = this.notifications.filter(n => !n.read).length
        this.updateTabCounts()

        // 模拟是否有更多数据
        this.hasMore = this.page < 3

      } catch (error) {
        console.error('加载通知失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        this.isLoading = false
        this.isRefreshing = false
      }
    },

    generateMockNotifications(page, size) {
      const types = ['system', 'attendance', 'access', 'consume', 'security']
      const notifications = []

      for (let i = 0; i < size; i++) {
        const id = (page - 1) * size + i + 1
        const type = types[Math.floor(Math.random() * types.length)]

        notifications.push({
          id: `notification_${id}`,
          type: type,
          title: this.getNotificationTitle(type),
          content: this.getNotificationContent(type),
          timestamp: Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000, // 最近7天
          read: Math.random() > 0.3,
          actions: this.getNotificationActions(type)
        })
      }

      return notifications
    },

    getNotificationTitle(type) {
      const titles = {
        system: '系统更新通知',
        attendance: '考勤异常提醒',
        access: '门禁访问记录',
        consume: '消费余额提醒',
        security: '安全访问警告'
      }
      return titles[type] || '通知消息'
    },

    getNotificationContent(type) {
      const contents = {
        system: '系统将于今晚22:00进行维护升级，预计持续2小时',
        attendance: '您今日有未完成的考勤记录，请及时处理',
        access: '您的门禁卡已成功通过A区域安检',
        consume: '您的账户余额不足，请及时充值',
        security: '检测到异常登录行为，请确认是否为本人操作'
      }
      return contents[type] || '您有一条新的通知消息'
    },

    getNotificationActions(type) {
      const actions = {
        system: [{ text: '查看详情', type: 'primary' }],
        attendance: [{ text: '立即处理', type: 'warning' }, { text: '忽略', type: 'default' }],
        access: [{ text: '查看记录', type: 'primary' }],
        consume: [{ text: '立即充值', type: 'success' }],
        security: [{ text: '查看详情', type: 'danger' }, { text: '修改密码', type: 'warning' }]
      }
      return actions[type] || []
    },

    getNotificationIcon(type) {
      const icons = {
        system: '🔔',
        attendance: '⏰',
        access: '🚪',
        consume: '💳',
        security: '⚠️'
      }
      return icons[type] || '📢'
    },

    getNotificationTypeName(type) {
      const typeNames = {
        system: '系统通知',
        attendance: '考勤提醒',
        access: '门禁通知',
        consume: '消费提醒',
        security: '安全警告'
      }
      return typeNames[type] || '通知'
    },

    updateTabCounts() {
      this.filterTabs.forEach(tab => {
        if (tab.type === 'all') {
          tab.count = this.notifications.filter(n => !n.read).length
        } else {
          tab.count = this.notifications.filter(n => n.type === tab.type && !n.read).length
        }
      })
    },

    switchTab(type) {
      this.activeTab = type
      this.selectedIds = []
    },

    toggleSelectAll() {
      if (this.isAllSelected) {
        this.selectedIds = []
      } else {
        this.selectedIds = this.filteredNotifications.map(n => n.id)
      }
    },

    toggleSelection(notificationId) {
      const index = this.selectedIds.indexOf(notificationId)
      if (index > -1) {
        this.selectedIds.splice(index, 1)
      } else {
        this.selectedIds.push(notificationId)
      }
    },

    selectNotification(notification) {
      const index = this.selectedIds.indexOf(notification.id)
      if (index > -1) {
        this.selectedIds.splice(index, 1)
      } else {
        this.selectedIds.push(notification.id)
      }
    },

    async markSelectedAsRead() {
      try {
        for (const id of this.selectedIds) {
          const notification = this.notifications.find(n => n.id === id)
          if (notification) {
            notification.read = true
          }
        }

        this.selectedIds = []
        this.unreadCount = this.notifications.filter(n => !n.read).length
        this.updateTabCounts()

        uni.showToast({
          title: '标记成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('标记已读失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    },

    async deleteSelected() {
      if (this.selectedIds.length === 0) return

      try {
        uni.showModal({
          title: '确认删除',
          content: `确定要删除${this.selectedIds.length}条消息吗？`,
          success: async (res) => {
            if (res.confirm) {
              this.notifications = this.notifications.filter(n => !this.selectedIds.includes(n.id))
              this.selectedIds = []
              this.unreadCount = this.notifications.filter(n => !n.read).length
              this.updateTabCounts()

              uni.showToast({
                title: '删除成功',
                icon: 'success'
              })
            }
          }
        })
      } catch (error) {
        console.error('删除失败:', error)
        uni.showToast({
          title: '删除失败',
          icon: 'error'
        })
      }
    },

    openNotification(notification) {
      this.selectedNotification = notification
      notification.read = true
      this.unreadCount = this.notifications.filter(n => !n.read).length
      this.updateTabCounts()
      this.$refs.notificationDetail.open()
    },

    closeDetail() {
      this.$refs.notificationDetail.close()
      this.selectedNotification = null
    },

    handleAction(notification, action) {
      console.log('处理通知操作:', notification, action)
      this.handleNotificationAction(notification, action)
    },

    handleDetailAction(notification, action) {
      this.closeDetail()
      this.handleNotificationAction(notification, action)
    },

    handleNotificationAction(notification, action) {
      switch (action.type) {
        case 'primary':
          uni.navigateTo({
            url: `/pages/${notification.type}/detail?id=${notification.id}`
          })
          break
        case 'success':
          uni.navigateTo({
            url: '/pages/consume/recharge'
          })
          break
        case 'warning':
          uni.navigateTo({
            url: '/pages/security/settings'
          })
          break
        case 'danger':
          uni.showModal({
            title: '安全警告',
            content: '检测到异常行为，建议立即修改密码',
            showCancel: false
          })
          break
        default:
          console.log('未知操作类型:', action.type)
      }
    },

    showNotificationMenu(notification) {
      uni.showActionSheet({
        itemList: ['标记已读', '删除', '详情'],
        success: (res) => {
          switch (res.tapIndex) {
            case 0:
              notification.read = true
              this.unreadCount = this.notifications.filter(n => !n.read).length
              this.updateTabCounts()
              break
            case 1:
              this.notifications = this.notifications.filter(n => n.id !== notification.id)
              this.unreadCount = this.notifications.filter(n => !n.read).length
              this.updateTabCounts()
              break
            case 2:
              this.openNotification(notification)
              break
          }
        }
      })
    },

    showSettings() {
      this.$refs.notificationSettings.open()
    },

    closeSettings() {
      this.$refs.notificationSettings.close()
    },

    refreshNotifications() {
      this.isRefreshing = true
      this.loadNotifications(true)
    },

    loadMoreNotifications() {
      if (!this.hasMore || this.isLoading) return
      this.page++
      this.loadNotifications()
    },

    async loadSettings() {
      try {
        const settings = uni.getStorageSync('notification_settings')
        if (settings) {
          this.settings = { ...this.settings, ...settings }
        }

        const typesSettings = uni.getStorageSync('notification_types_settings')
        if (typesSettings) {
          this.notificationTypes = typesSettings
        }
      } catch (error) {
        console.error('加载设置失败:', error)
      }
    },

    async saveSettings() {
      try {
        uni.setStorageSync('notification_settings', this.settings)
        uni.setStorageSync('notification_types_settings', this.notificationTypes)
      } catch (error) {
        console.error('保存设置失败:', error)
      }
    },

    togglePushNotification(e) {
      this.settings.pushEnabled = e.detail.value
      this.saveSettings()
    },

    toggleSound(e) {
      this.settings.soundEnabled = e.detail.value
      this.saveSettings()
    },

    toggleVibration(e) {
      this.settings.vibrationEnabled = e.detail.value
      this.saveSettings()
    },

    toggleNotificationType(type) {
      const typeConfig = this.notificationTypes.find(t => t.type === type)
      if (typeConfig) {
        typeConfig.enabled = !typeConfig.enabled
        this.saveSettings()
      }
    },

    initializePushService() {
      // 初始化推送服务
      if (this.settings.pushEnabled) {
        this.requestPushPermission()
      }
    },

    requestPushPermission() {
      // #ifdef APP-PLUS
      uni.requestPushPermission({
        success: (res) => {
          console.log('推送权限申请结果:', res)
          if (res.accept) {
            this.registerPushService()
          }
        },
        fail: (err) => {
          console.error('推送权限申请失败:', err)
        }
      })
      // #endif
    },

    registerPushService() {
      // #ifdef APP-PLUS
      const pushManager = uni.requireNativePlugin('Push-Manager')
      if (pushManager) {
        pushManager.init({
          success: () => {
            console.log('推送服务初始化成功')
          },
          fail: (err) => {
            console.error('推送服务初始化失败:', err)
          }
        })
      }
      // #endif
    },

    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前'
      } else if (diff < 604800000) {
        return Math.floor(diff / 86400000) + '天前'
      } else {
        return date.toLocaleDateString()
      }
    },

    formatFullTime(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleString()
    }
  }
}
</script>

<style lang="scss" scoped>
.notification-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  text-align: center;
  color: white;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.subtitle {
  font-size: 28rpx;
  opacity: 0.9;
}

.content {
  padding: 20rpx;
}

.filter-section {
  background: white;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.filter-tabs {
  padding: 20rpx;
  white-space: nowrap;
}

.filter-tab {
  display: inline-block;
  padding: 16rpx 24rpx;
  margin-right: 20rpx;
  border-radius: 24rpx;
  background: #f8f9fa;
  position: relative;

  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }
}

.tab-text {
  font-size: 28rpx;
  font-weight: bold;
}

.tab-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  background: #e74c3c;
  border-radius: 20rpx;
  min-width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-text {
  color: white;
  font-size: 20rpx;
  font-weight: bold;
}

.batch-section {
  background: white;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.batch-left {
  display: flex;
  align-items: center;
}

.batch-text {
  margin-left: 16rpx;
  font-size: 28rpx;
  color: #333;
}

.batch-right {
  display: flex;
  gap: 16rpx;
}

.batch-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  border: none;
  color: white;

  &.mark-read {
    background: #3498db;
  }

  &.delete {
    background: #e74c3c;
  }

  &[disabled] {
    background: #ccc;
  }
}

.notification-list {
  height: calc(100vh - 300rpx);
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.empty-state {
  text-align: center;
  padding: 120rpx 40rpx;
}

.empty-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #666;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &.unread {
    background: #f8f9ff;
  }

  &.selected {
    background: #e8f4fd;
  }
}

.item-left {
  display: flex;
  align-items: center;
  margin-right: 20rpx;
}

.notification-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 16rpx;
  font-size: 30rpx;

  &.system {
    background: #3498db;
  }

  &.attendance {
    background: #f39c12;
  }

  &.access {
    background: #27ae60;
  }

  &.consume {
    background: #9b59b6;
  }

  &.security {
    background: #e74c3c;
  }
}

.icon-text {
  color: white;
}

.item-content {
  flex: 1;
  padding-right: 16rpx;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8rpx;
}

.notification-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.notification-time {
  font-size: 22rpx;
  color: #666;
  margin-left: 16rpx;
}

.notification-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.4;
  margin-bottom: 12rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.notification-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  border: none;
  color: white;

  &.primary {
    background: #3498db;
  }

  &.success {
    background: #27ae60;
  }

  &.warning {
    background: #f39c12;
  }

  &.danger {
    background: #e74c3c;
  }

  &.default {
    background: #95a5a6;
  }
}

.item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.unread-dot {
  width: 12rpx;
  height: 12rpx;
  background: #e74c3c;
  border-radius: 50%;
}

.more-btn {
  padding: 8rpx;
  background: transparent;
  border: none;
  color: #666;
}

.more-icon {
  font-size: 24rpx;
}

.load-more {
  text-align: center;
  padding: 40rpx;
}

.load-text {
  font-size: 26rpx;
  color: #666;
}

.detail-popup {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.detail-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f8f9fa;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  font-size: 32rpx;
  color: #666;
}

.detail-content {
  padding: 30rpx;
}

.detail-info {
  margin-bottom: 30rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
}

.info-label {
  font-size: 26rpx;
  color: #666;
}

.info-value {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
}

.detail-body {
  margin-bottom: 30rpx;
}

.detail-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
}

.detail-actions {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
}

.detail-action-btn {
  flex: 1;
  min-width: 200rpx;
  padding: 16rpx 24rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  border: none;
  color: white;

  &.primary {
    background: #3498db;
  }

  &.success {
    background: #27ae60;
  }

  &.warning {
    background: #f39c12;
  }

  &.danger {
    background: #e74c3c;
  }
}

.settings-popup {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
}

.settings-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.settings-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.settings-content {
  padding: 30rpx;
}

.setting-group {
  margin-bottom: 40rpx;
}

.group-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.setting-label {
  font-size: 26rpx;
  color: #333;
}
</style>