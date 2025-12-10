<template>
  <view class="visitor-management">
    <!-- 现代化头部 -->
    <view class="modern-header">
      <view class="header-background"></view>
      <view class="header-content" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="header-top">
          <text class="page-title">访客管理</text>
          <view class="header-actions">
            <view class="sync-button" @click="refreshData">
              <uni-icons
                type="refreshempty"
                :size="16"
                :color="'#fff'"
                :class="{ rotating: syncLoading }"
              ></uni-icons>
            </view>
            <view class="notification-button" @click="goToNotifications">
              <uni-icons type="bell" size="16" color="#fff"></uni-icons>
              <view class="notification-badge" v-if="unreadCount > 0">{{ unreadCount }}</view>
            </view>
          </view>
        </view>

        <!-- 欢迎信息 -->
        <view class="welcome-section">
          <text class="welcome-text">您好，{{ userName }}</text>
          <text class="welcome-subtitle">{{ currentTime }}</text>
        </view>
      </view>
    </view>

    <!-- 统计概览卡片 -->
    <view class="stats-section">
      <view class="stats-grid">
        <view class="stats-card primary" @click="navigateTo('/pages/visitor/appointment')">
          <view class="stats-icon-wrapper">
            <text class="stats-icon">📅</text>
          </view>
          <view class="stats-content">
            <text class="stats-number">{{ overviewStats.todayAppointments }}</text>
            <text class="stats-label">今日预约</text>
          </view>
          <view class="stats-trend">
            <text class="trend-text">{{ overviewStats.appointmentTrend || '+0%' }}</text>
          </view>
        </view>

        <view class="stats-card success" @click="navigateTo('/pages/visitor/record')">
          <view class="stats-icon-wrapper">
            <text class="stats-icon">👥</text>
          </view>
          <view class="stats-content">
            <text class="stats-number">{{ overviewStats.activeVisitors }}</text>
            <text class="stats-label">在访访客</text>
          </view>
          <view class="stats-indicator"></view>
        </view>

        <view class="stats-card warning" @click="navigateTo('/pages/visitor/appointment?filter=pending')">
          <view class="stats-icon-wrapper">
            <text class="stats-icon">⏰</text>
          </view>
          <view class="stats-content">
            <text class="stats-number">{{ overviewStats.pendingApprovals }}</text>
            <text class="stats-label">待审批</text>
          </view>
          <view class="stats-badge" v-if="overviewStats.pendingApprovals > 0">
            <text class="badge-text">{{ overviewStats.pendingApprovals }}</text>
          </view>
        </view>

        <view class="stats-card info" @click="navigateTo('/pages/visitor/checkin')">
          <view class="stats-icon-wrapper">
            <text class="stats-icon">✅</text>
          </view>
          <view class="stats-content">
            <text class="stats-number">{{ overviewStats.todayCheckIns }}</text>
            <text class="stats-label">今日签到</text>
          </view>
          <view class="stats-chart">
            <view class="mini-chart">
              <view class="chart-bar" v-for="i in 7" :key="i" :style="{ height: getChartHeight(i) }"></view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="section-header">
        <text class="section-title">快捷操作</text>
        <text class="section-subtitle">快速访问常用功能</text>
      </view>
      <view class="actions-grid">
        <view class="action-item" @click="navigateTo('/pages/visitor/appointment')">
          <view class="action-icon-wrapper gradient-1">
            <text class="action-icon">📋</text>
          </view>
          <text class="action-text">我的预约</text>
          <text class="action-desc">管理预约</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/visitor/checkin')">
          <view class="action-icon-wrapper gradient-2">
            <text class="action-icon">📱</text>
          </view>
          <text class="action-text">扫码签到</text>
          <text class="action-desc">快速签到</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/visitor/qrcode')">
          <view class="action-icon-wrapper gradient-3">
            <text class="action-icon">🔍</text>
          </view>
          <text class="action-text">二维码</text>
          <text class="action-desc">访客码</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/visitor/record')">
          <view class="action-icon-wrapper gradient-4">
            <text class="action-icon">📖</text>
          </view>
          <text class="action-text">访问记录</text>
          <text class="action-desc">历史记录</text>
        </view>
      </view>
    </view>

    <!-- 最近预约 -->
    <view class="recent-appointments">
      <view class="section-header">
        <view class="header-left">
          <text class="section-title">最近预约</text>
          <text class="section-subtitle">最新预约动态</text>
        </view>
        <text class="view-more" @click="navigateTo('/pages/visitor/appointment')">
          查看更多
          <uni-icons type="right" size="12" color="#667eea"></uni-icons>
        </text>
      </view>

      <view class="appointments-list" v-if="recentAppointments.length > 0">
        <view
          class="appointment-item"
          v-for="(appointment, index) in recentAppointments"
          :key="appointment.id || index"
          @click="viewAppointmentDetail(appointment)"
        >
          <view class="appointment-status">
            <view :class="['status-indicator', `status-${appointment.status}`]">
              <uni-icons
                :type="getStatusIcon(appointment.status)"
                size="14"
                :color="getStatusColor(appointment.status)"
              ></uni-icons>
            </view>
          </view>

          <view class="appointment-content">
            <view class="appointment-header">
              <text class="visitor-name">{{ appointment.visitorName }}</text>
              <view :class="['status-tag', `status-${appointment.status}`]">
                {{ getStatusText(appointment.status) }}
              </view>
            </view>

            <view class="appointment-details">
              <view class="detail-item">
                <uni-icons type="contact" size="12" color="#999"></uni-icons>
                <text class="detail-text">被访人：{{ appointment.visiteeName }}</text>
              </view>
              <view class="detail-item">
                <uni-icons type="calendar" size="12" color="#999"></uni-icons>
                <text class="detail-text">{{ formatDateTime(appointment.appointmentTime) }}</text>
              </view>
              <view class="detail-item" v-if="appointment.visitReason">
                <uni-icons type="flag" size="12" color="#999"></uni-icons>
                <text class="detail-text">{{ appointment.visitReason }}</text>
              </view>
            </view>
          </view>

          <view class="appointment-arrow">
            <uni-icons type="right" size="16" color="#ccc"></uni-icons>
          </view>
        </view>
      </view>

      <view class="empty-state" v-else>
        <image class="empty-icon" src="/static/images/empty-visitor.png" mode="aspectFit"></image>
        <text class="empty-text">暂无预约记录</text>
        <text class="empty-desc">您的访客预约记录将显示在这里</text>
        <button class="empty-action" @click="navigateTo('/pages/visitor/appointment')">
          创建预约
        </button>
      </view>
    </view>

    <!-- 快速预约浮动按钮 -->
    <view class="fab-button" @click="createNewAppointment">
      <uni-icons type="plus" size="24" color="#fff"></uni-icons>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const statusBarHeight = ref(20)
const syncLoading = ref(false)

const userName = ref('张三')
const userId = ref(1001)
const currentTime = ref('')
const unreadCount = ref(3)

const overviewStats = reactive({
  todayAppointments: 8,
  activeVisitors: 3,
  pendingApprovals: 2,
  todayCheckIns: 5,
  appointmentTrend: '+12%'
})

const recentAppointments = ref([])
const chartData = ref([30, 45, 60, 35, 80, 50, 70])

// 页面生命周期
onMounted(() => {
  initPage()
})

onShow(() => {
  loadData()
})

onPullDownRefresh(() => {
  refreshData()
})

// 方法实现
const initPage = () => {
  // 获取系统信息
  try {
    const systemInfo = uni.getSystemInfoSync()
    statusBarHeight.value = systemInfo.statusBarHeight || 20
  } catch (error) {
    console.error('获取系统信息失败:', error)
  }

  // 更新当前时间
  updateCurrentTime()
  setInterval(() => {
    updateCurrentTime()
  }, 60000) // 每分钟更新一次

  // 加载数据
  loadData()
}

const updateCurrentTime = () => {
  const now = dayjs()
  const hour = now.hour()

  if (hour < 6) {
    currentTime.value = '凌晨好'
  } else if (hour < 12) {
    currentTime.value = '上午好'
  } else if (hour < 14) {
    currentTime.value = '中午好'
  } else if (hour < 18) {
    currentTime.value = '下午好'
  } else {
    currentTime.value = '晚上好'
  }
}

const loadData = async () => {
  try {
    await Promise.all([
      loadOverviewStats(),
      loadRecentAppointments()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const loadOverviewStats = async () => {
  try {
    // 这里调用真实API
    // const result = await visitorApi.getPersonalStatistics(userId.value)
    // if (result.success && result.data) {
    //   Object.assign(overviewStats, result.data)
    // }

    // 模拟数据
    Object.assign(overviewStats, {
      todayAppointments: Math.floor(Math.random() * 20) + 1,
      activeVisitors: Math.floor(Math.random() * 10) + 1,
      pendingApprovals: Math.floor(Math.random() * 5),
      todayCheckIns: Math.floor(Math.random() * 15) + 1,
      appointmentTrend: `+${Math.floor(Math.random() * 30)}%`
    })
  } catch (error) {
    console.error('加载概览统计失败:', error)
  }
}

const loadRecentAppointments = async () => {
  try {
    // 这里调用真实API
    // const result = await visitorApi.getMyAppointments(userId.value)
    // if (result.success && result.data) {
    //   recentAppointments.value = result.data.slice(0, 5)
    // }

    // 模拟数据
    recentAppointments.value = generateMockAppointments()
  } catch (error) {
    console.error('加载最近预约失败:', error)
  }
}

const generateMockAppointments = () => {
  const mockData = []
  const visitors = ['李明', '王芳', '张伟', '刘洋', '陈静']
  const visitees = ['张经理', '李总监', '王主管', '赵工程师']
  const reasons = ['商务洽谈', '技术交流', '项目合作', '面试']
  const statuses = ['PENDING', 'APPROVED', 'CHECKED_IN']

  for (let i = 0; i < 5; i++) {
    const date = new Date()
    date.setHours(date.getHours() + i * 2)

    mockData.push({
      id: `appointment_${i}`,
      appointmentId: `APT${Date.now()}_${i}`,
      visitorName: visitors[i],
      visiteeName: visitees[i],
      visitReason: reasons[i],
      appointmentTime: date.toISOString(),
      status: statuses[Math.floor(Math.random() * statuses.length)],
      phoneNumber: '138****5678',
      idNumber: '3301**********1234'
    })
  }

  return mockData
}

const refreshData = async () => {
  syncLoading.value = true
  try {
    await loadData()
    uni.showToast({
      title: '刷新成功',
      icon: 'success'
    })
  } catch (error) {
    uni.showToast({
      title: '刷新失败',
      icon: 'none'
    })
  } finally {
    syncLoading.value = false
    uni.stopPullDownRefresh()
  }
}

// 页面导航
const navigateTo = (url) => {
  uni.navigateTo({ url })
}

const createNewAppointment = () => {
  uni.navigateTo({
    url: '/pages/visitor/appointment?action=create'
  })
}

const viewAppointmentDetail = (appointment) => {
  uni.navigateTo({
    url: `/pages/visitor/appointment?id=${appointment.appointmentId}`
  })
}

const goToNotifications = () => {
  uni.navigateTo({
    url: '/pages/message/list'
  })
}

const getChartHeight = (index) => {
  if (index > chartData.value.length) return '0%'
  return `${chartData.value[index - 1] || 0}%`
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return dayjs(datetime).format('MM-DD HH:mm')
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消',
    'CHECKED_IN': '已签到',
    'CHECKED_OUT': '已签退',
    'EXPIRED': '已过期'
  }
  return textMap[status] || status
}

const getStatusIcon = (status) => {
  const iconMap = {
    'PENDING': 'clock',
    'APPROVED': 'checkmarkempty',
    'REJECTED': 'closeempty',
    'CANCELLED': 'minus',
    'CHECKED_IN': 'circle-filled',
    'CHECKED_OUT': 'checkbox-filled',
    'EXPIRED': 'info'
  }
  return iconMap[status] || 'help'
}

const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': '#faad14',
    'APPROVED': '#52c41a',
    'REJECTED': '#ff4d4f',
    'CANCELLED': '#999',
    'CHECKED_IN': '#1890ff',
    'CHECKED_OUT': '#52c41a',
    'EXPIRED': '#ff4d4f'
  }
  return colorMap[status] || '#666'
}
</script>

<style lang="scss" scoped>
.visitor-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
  position: relative;
}

// 现代化头部
.modern-header {
  position: relative;
  overflow: hidden;

  .header-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    z-index: 1;
  }

  .header-content {
    position: relative;
    z-index: 2;
    padding: 30rpx 30rpx 60rpx;

    .header-top {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 40rpx;

      .page-title {
        font-size: 36rpx;
        font-weight: bold;
        color: white;
      }

      .header-actions {
        display: flex;
        gap: 20rpx;

        .sync-button, .notification-button {
          width: 60rpx;
          height: 60rpx;
          border-radius: 30rpx;
          background: rgba(255, 255, 255, 0.2);
          backdrop-filter: blur(10rpx);
          display: flex;
          align-items: center;
          justify-content: center;
          border: 1rpx solid rgba(255, 255, 255, 0.3);
          transition: all 0.3s ease;

          &:active {
            background: rgba(255, 255, 255, 0.3);
            transform: scale(0.95);
          }

          .rotating {
            animation: rotate 1s linear infinite;
          }
        }

        .notification-button {
          position: relative;

          .notification-badge {
            position: absolute;
            top: -8rpx;
            right: -8rpx;
            background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
            color: white;
            font-size: 20rpx;
            padding: 4rpx 8rpx;
            border-radius: 20rpx;
            min-width: 24rpx;
            text-align: center;
            font-weight: bold;
          }
        }
      }
    }

    .welcome-section {
      .welcome-text {
        font-size: 32rpx;
        color: white;
        font-weight: 500;
        margin-bottom: 8rpx;
        display: block;
      }

      .welcome-subtitle {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.8);
        display: block;
      }
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 统计概览卡片
.stats-section {
  padding: 30rpx;
  margin-top: -30rpx;
  position: relative;
  z-index: 3;

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;

    .stats-card {
      background: white;
      border-radius: 24rpx;
      padding: 30rpx;
      display: flex;
      align-items: center;
      position: relative;
      overflow: hidden;
      box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
      transition: all 0.3s ease;

      &:active {
        transform: scale(0.98);
        box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
      }

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4rpx;
      }

      &.primary::before {
        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      }

      &.success::before {
        background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
      }

      &.warning::before {
        background: linear-gradient(90deg, #faad14 0%, #ffc53d 100%);
      }

      &.info::before {
        background: linear-gradient(90deg, #1890ff 0%, #40a9ff 100%);
      }

      .stats-icon-wrapper {
        width: 80rpx;
        height: 80rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
        position: relative;

        .stats-icon {
          font-size: 40rpx;
          filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
        }
      }

      &.primary .stats-icon-wrapper {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
      }

      &.success .stats-icon-wrapper {
        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
        box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
      }

      &.warning .stats-icon-wrapper {
        background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
        box-shadow: 0 4rpx 12rpx rgba(250, 173, 20, 0.3);
      }

      &.info .stats-icon-wrapper {
        background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
        box-shadow: 0 4rpx 12rpx rgba(24, 144, 255, 0.3);
      }

      .stats-content {
        flex: 1;

        .stats-number {
          font-size: 48rpx;
          font-weight: bold;
          color: #1a1a1a;
          margin-bottom: 4rpx;
          display: block;
        }

        .stats-label {
          font-size: 24rpx;
          color: #666;
          display: block;
        }
      }

      .stats-trend {
        position: absolute;
        top: 30rpx;
        right: 30rpx;

        .trend-text {
          font-size: 24rpx;
          color: #52c41a;
          font-weight: 500;
        }
      }

      .stats-indicator {
        position: absolute;
        top: 30rpx;
        right: 30rpx;
        width: 12rpx;
        height: 12rpx;
        background: #52c41a;
        border-radius: 6rpx;
        animation: pulse 2s infinite;
      }

      .stats-badge {
        position: absolute;
        top: 20rpx;
        right: 20rpx;
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        color: white;
        font-size: 20rpx;
        padding: 4rpx 12rpx;
        border-radius: 20rpx;
        font-weight: bold;
        box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.3);
      }

      .stats-chart {
        position: absolute;
        top: 30rpx;
        right: 30rpx;

        .mini-chart {
          display: flex;
          align-items: flex-end;
          gap: 2rpx;
          width: 60rpx;
          height: 30rpx;

          .chart-bar {
            flex: 1;
            background: rgba(24, 144, 255, 0.3);
            border-radius: 1rpx;
            min-height: 4rpx;
          }
        }
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.2); }
}

// 快捷操作
.quick-actions {
  padding: 0 30rpx 30rpx;

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #1a1a1a;
      margin-bottom: 8rpx;
      display: block;
    }

    .section-subtitle {
      font-size: 24rpx;
      color: #999;
      display: block;
    }
  }

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;

    .action-item {
      background: white;
      border-radius: 20rpx;
      padding: 30rpx 20rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
      transition: all 0.3s ease;

      &:active {
        transform: scale(0.95);
        box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
      }

      .action-icon-wrapper {
        width: 70rpx;
        height: 70rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16rpx;
        position: relative;

        .action-icon {
          font-size: 32rpx;
          filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
        }
      }

      .gradient-1 {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
      }

      .gradient-2 {
        background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
        box-shadow: 0 4rpx 12rpx rgba(74, 222, 128, 0.3);
      }

      .gradient-3 {
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        box-shadow: 0 4rpx 12rpx rgba(96, 165, 250, 0.3);
      }

      .gradient-4 {
        background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
        box-shadow: 0 4rpx 12rpx rgba(251, 191, 36, 0.3);
      }

      .action-text {
        font-size: 26rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 4rpx;
        text-align: center;
      }

      .action-desc {
        font-size: 20rpx;
        color: #999;
        text-align: center;
      }
    }
  }
}

// 最近预约
.recent-appointments {
  padding: 0 30rpx 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 30rpx;

    .header-left {
      .section-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #1a1a1a;
        margin-bottom: 8rpx;
        display: block;
      }

      .section-subtitle {
        font-size: 24rpx;
        color: #999;
        display: block;
      }
    }

    .view-more {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 26rpx;
      color: #667eea;
      padding: 10rpx 16rpx;
      border-radius: 20rpx;
      background: rgba(102, 126, 234, 0.1);
      transition: all 0.3s ease;

      &:active {
        background: rgba(102, 126, 234, 0.2);
        transform: scale(0.95);
      }
    }
  }

  .appointments-list {
    .appointment-item {
      background: white;
      border-radius: 24rpx;
      padding: 30rpx;
      margin-bottom: 20rpx;
      display: flex;
      align-items: center;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;

      &:active {
        transform: scale(0.98);
        box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
      }

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 6rpx;
        height: 100%;
        background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      &:active::before {
        opacity: 1;
      }

      .appointment-status {
        margin-right: 20rpx;

        .status-indicator {
          width: 60rpx;
          height: 60rpx;
          border-radius: 30rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          position: relative;

          &.status-PENDING {
            background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
            box-shadow: 0 4rpx 12rpx rgba(250, 173, 20, 0.3);
          }

          &.status-APPROVED {
            background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
            box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
          }

          &.status-CHECKED_IN {
            background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
            box-shadow: 0 4rpx 12rpx rgba(24, 144, 255, 0.3);
          }
        }
      }

      .appointment-content {
        flex: 1;

        .appointment-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16rpx;

          .visitor-name {
            font-size: 32rpx;
            font-weight: 600;
            color: #1a1a1a;
          }

          .status-tag {
            padding: 8rpx 16rpx;
            border-radius: 16rpx;
            font-size: 22rpx;
            font-weight: 500;

            &.status-PENDING {
              background: rgba(250, 173, 20, 0.1);
              color: #d48806;
            }

            &.status-APPROVED {
              background: rgba(82, 196, 26, 0.1);
              color: #389e0d;
            }

            &.status-CHECKED_IN {
              background: rgba(24, 144, 255, 0.1);
              color: #0958d9;
            }
          }
        }

        .appointment-details {
          .detail-item {
            display: flex;
            align-items: center;
            margin-bottom: 8rpx;

            &:last-child {
              margin-bottom: 0;
            }

            .detail-text {
              font-size: 26rpx;
              color: #666;
              margin-left: 12rpx;
            }
          }
        }
      }

      .appointment-arrow {
        margin-left: 20rpx;
      }
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 40rpx;

    .empty-icon {
      width: 200rpx;
      height: 200rpx;
      margin-bottom: 40rpx;
      opacity: 0.6;
    }

    .empty-text {
      font-size: 32rpx;
      color: #999;
      margin-bottom: 16rpx;
    }

    .empty-desc {
      font-size: 26rpx;
      color: #ccc;
      text-align: center;
      margin-bottom: 40rpx;
    }

    .empty-action {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 24rpx;
      padding: 20rpx 40rpx;
      font-size: 28rpx;
      font-weight: 500;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
      transition: all 0.3s ease;

      &:active {
        transform: scale(0.95);
        box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.4);
      }
    }
  }
}

// 浮动按钮
.fab-button {
  position: fixed;
  bottom: 120rpx;
  right: 30rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
  z-index: 1000;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.5);
  }
}
</style>

