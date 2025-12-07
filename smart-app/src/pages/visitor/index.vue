<template>
  <view class="visitor-management">
    <!-- 头部状态栏 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="status-content">
        <text class="page-title">访客管理</text>
        <view class="sync-status" @click="refreshData">
          <text :class="['sync-icon', syncLoading ? 'rotating' : '']">🔄</text>
          <text class="sync-text">{{ syncLoading ? '同步中...' : '已同步' }}</text>
        </view>
      </view>
    </view>

    <!-- 概览卡片 -->
    <view class="overview-section">
      <view class="overview-grid">
        <view class="overview-card today" @click="navigateTo('/pages/visitor/appointment')">
          <view class="card-icon">📅</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.todayAppointments || 0 }}</text>
            <text class="card-label">今日预约</text>
          </view>
        </view>

        <view class="overview-card active" @click="navigateTo('/pages/visitor/record')">
          <view class="card-icon">👥</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.activeVisitors || 0 }}</text>
            <text class="card-label">在访访客</text>
          </view>
        </view>

        <view class="overview-card pending" @click="navigateTo('/pages/visitor/appointment')">
          <view class="card-icon">⏰</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.pendingApprovals || 0 }}</text>
            <text class="card-label">待审批</text>
          </view>
        </view>

        <view class="overview-card checkin" @click="navigateTo('/pages/visitor/checkin')">
          <view class="card-icon">✅</view>
          <view class="card-info">
            <text class="card-number">{{ overviewStats.todayCheckIns || 0 }}</text>
            <text class="card-label">今日签到</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="section-title">快捷操作</view>
      <view class="actions-grid">
        <view class="action-item" @click="navigateTo('/pages/visitor/appointment')">
          <view class="action-icon">📋</view>
          <text class="action-text">我的预约</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/visitor/checkin')">
          <view class="action-icon">📱</view>
          <text class="action-text">扫码签到</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/visitor/qrcode')">
          <view class="action-icon">🔍</view>
          <text class="action-text">二维码</text>
        </view>
        <view class="action-item" @click="navigateTo('/pages/visitor/record')">
          <view class="action-icon">📖</view>
          <text class="action-text">访问记录</text>
        </view>
      </view>
    </view>

    <!-- 最近预约 -->
    <view class="recent-appointments">
      <view class="section-header">
        <text class="section-title">最近预约</text>
        <text class="view-more" @click="navigateTo('/pages/visitor/appointment')">查看更多</text>
      </view>

      <view class="appointments-list" v-if="recentAppointments.length > 0">
        <view
          class="appointment-item"
          v-for="(appointment, index) in recentAppointments"
          :key="index"
          @click="viewAppointmentDetail(appointment)"
        >
          <view class="appointment-info">
            <view class="info-row">
              <text class="visitor-name">{{ appointment.visitorName }}</text>
              <text :class="['status-tag', `status-${appointment.status}`]">
                {{ getStatusText(appointment.status) }}
              </text>
            </view>
            <view class="info-row">
              <text class="info-label">被访人：</text>
              <text class="info-value">{{ appointment.visiteeName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">预约时间：</text>
              <text class="info-value">{{ formatDateTime(appointment.appointmentTime) }}</text>
            </view>
          </view>
          <view class="appointment-arrow">›</view>
        </view>
      </view>

      <view class="no-data" v-else>
        <text>暂无预约记录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import visitorApi from '@/api/business/visitor/visitor-api.js'

export default {
  name: 'VisitorManagement',
  setup() {
    const userStore = useUserStore()
    // 状态栏高度
    const statusBarHeight = ref(20)
    const syncLoading = ref(false)

    // 概览统计
    const overviewStats = reactive({
      todayAppointments: 0,
      activeVisitors: 0,
      pendingApprovals: 0,
      todayCheckIns: 0
    })

    // 最近预约
    const recentAppointments = ref([])

    // 获取概览统计
    const loadOverviewStats = async () => {
      try {
        // 从用户store获取用户ID
        const userId = userStore.employeeId
        if (!userId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          return
        }
        const result = await visitorApi.getPersonalStatistics(userId)
        if (result.success && result.data) {
          Object.assign(overviewStats, result.data)
        }
      } catch (error) {
        console.error('加载概览统计失败:', error)
      }
    }

    // 加载最近预约
    const loadRecentAppointments = async () => {
      try {
        // 从用户store获取用户ID
        const userId = userStore.employeeId
        if (!userId) {
          return
        }
        const result = await visitorApi.getMyAppointments(userId)
        if (result.success && result.data) {
          recentAppointments.value = result.data.slice(0, 5)
        }
      } catch (error) {
        console.error('加载最近预约失败:', error)
      }
    }

    // 刷新数据
    const refreshData = async () => {
      syncLoading.value = true
      try {
        await Promise.all([loadOverviewStats(), loadRecentAppointments()])
        uni.showToast({ title: '刷新成功', icon: 'success' })
      } catch (error) {
        uni.showToast({ title: '刷新失败', icon: 'none' })
      } finally {
        syncLoading.value = false
      }
    }

    // 页面导航
    const navigateTo = (url) => {
      uni.navigateTo({ url })
    }

    // 查看预约详情
    const viewAppointmentDetail = (appointment) => {
      uni.navigateTo({
        url: `/pages/visitor/appointment?id=${appointment.appointmentId}`
      })
    }

    // 格式化日期时间
    const formatDateTime = (datetime) => {
      if (!datetime) return '-'
      const date = new Date(datetime)
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hours}:${minutes}`
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        'PENDING': '待审批',
        'APPROVED': '已批准',
        'REJECTED': '已拒绝',
        'CANCELLED': '已取消',
        'CHECKED_IN': '已签到',
        'CHECKED_OUT': '已签退'
      }
      return textMap[status] || status
    }

    // 初始化
    onMounted(() => {
      // 获取系统信息
      const systemInfo = uni.getSystemInfoSync()
      statusBarHeight.value = systemInfo.statusBarHeight

      // 加载数据
      loadOverviewStats()
      loadRecentAppointments()
    })

    return {
      statusBarHeight,
      syncLoading,
      overviewStats,
      recentAppointments,
      refreshData,
      navigateTo,
      viewAppointmentDetail,
      formatDateTime,
      getStatusText
    }
  }
}
</script>

<style lang="scss" scoped>
.visitor-management {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.status-bar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 10px 15px;
  padding-bottom: 10px;

  .status-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .page-title {
      font-size: 18px;
      font-weight: bold;
      color: #fff;
    }

    .sync-status {
      display: flex;
      align-items: center;
      color: #fff;
      font-size: 12px;

      .sync-icon {
        margin-right: 4px;
        font-size: 14px;

        &.rotating {
          animation: rotate 1s linear infinite;
        }
      }
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.overview-section {
  padding: 15px;

  .overview-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;

    .overview-card {
      background-color: #fff;
      border-radius: 8px;
      padding: 15px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .card-icon {
        font-size: 32px;
        margin-right: 12px;
      }

      .card-info {
        flex: 1;

        .card-number {
          display: block;
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }

        .card-label {
          display: block;
          font-size: 12px;
          color: #666;
          margin-top: 4px;
        }
      }
    }
  }
}

.quick-actions {
  padding: 0 15px 15px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;

    .action-item {
      background-color: #fff;
      border-radius: 8px;
      padding: 15px 10px;
      display: flex;
      flex-direction: column;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .action-icon {
        font-size: 28px;
        margin-bottom: 8px;
      }

      .action-text {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

.recent-appointments {
  padding: 0 15px 15px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .view-more {
      font-size: 12px;
      color: #1890ff;
    }
  }

  .appointments-list {
    .appointment-item {
      background-color: #fff;
      border-radius: 8px;
      padding: 15px;
      margin-bottom: 10px;
      display: flex;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .appointment-info {
        flex: 1;

        .info-row {
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          &:last-child {
            margin-bottom: 0;
          }

          .visitor-name {
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }

          .status-tag {
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 12px;
            margin-left: auto;

            &.status-PENDING { background-color: #faad14; color: #fff; }
            &.status-APPROVED { background-color: #52c41a; color: #fff; }
            &.status-REJECTED { background-color: #ff4d4f; color: #fff; }
            &.status-CHECKED_IN { background-color: #1890ff; color: #fff; }
          }

          .info-label {
            font-size: 12px;
            color: #666;
            margin-right: 4px;
          }

          .info-value {
            font-size: 12px;
            color: #333;
          }
        }
      }

      .appointment-arrow {
        font-size: 20px;
        color: #d9d9d9;
        margin-left: 10px;
      }
    }
  }

  .no-data {
    text-align: center;
    padding: 40px 0;
    color: #999;
  }
}
</style>

