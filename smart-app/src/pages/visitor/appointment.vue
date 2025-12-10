<template>
  <view class="visitor-appointment-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <text class="nav-title">我的预约</text>
      <text class="nav-subtitle">管理访客预约记录</text>
    </view>

    <!-- 标签页 -->
    <view class="tabs-container">
      <view class="tabs">
        <view
          :class="['tab-item', activeTab === 'all' ? 'active' : '']"
          @click="switchTab('all')"
        >
          <text class="tab-text">全部</text>
          <view class="tab-indicator"></view>
        </view>
        <view
          :class="['tab-item', activeTab === 'pending' ? 'active' : '']"
          @click="switchTab('pending')"
        >
          <text class="tab-text">待审批</text>
          <view class="tab-indicator"></view>
        </view>
        <view
          :class="['tab-item', activeTab === 'approved' ? 'active' : '']"
          @click="switchTab('approved')"
        >
          <text class="tab-text">已批准</text>
          <view class="tab-indicator"></view>
        </view>
      </view>
    </view>

    <!-- 预约列表 -->
    <view class="appointment-list">
      <view
        class="appointment-item"
        v-for="(appointment, index) in filteredAppointments"
        :key="index"
        @click="viewDetail(appointment)"
      >
        <view class="item-header">
          <text class="visitor-name">{{ appointment.visitorName }}</text>
          <text :class="['status-tag', `status-${appointment.status}`]">
            {{ getStatusText(appointment.status) }}
          </text>
        </view>
        <view class="item-content">
          <view class="info-row">
            <text class="label">被访人：</text>
            <text class="value">{{ appointment.visiteeName }}</text>
          </view>
          <view class="info-row">
            <text class="label">预约时间：</text>
            <text class="value">{{ formatDateTime(appointment.appointmentTime) }}</text>
          </view>
          <view class="info-row">
            <text class="label">来访事由：</text>
            <text class="value">{{ appointment.purpose }}</text>
          </view>
        </view>
        <view class="item-actions" v-if="appointment.status === 'PENDING'">
          <button class="cancel-btn" size="mini" @click.stop="cancelAppointment(appointment)">
            取消预约
          </button>
        </view>
      </view>

      <view class="no-data" v-if="filteredAppointments.length === 0">
        <text>暂无预约记录</text>
      </view>
    </view>

    <!-- 创建预约按钮 -->
    <view class="fab-button" @click="createAppointment">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import visitorApi from '@/api/business/visitor/visitor-api.js'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const userStore = useUserStore()

// 响应式数据
const activeTab = ref('all')
const appointments = ref([])

// 计算属性
const filteredAppointments = computed(() => {
  if (activeTab.value === 'all') {
    return appointments.value
  }
  const statusMap = {
    'pending': 'PENDING',
    'approved': 'APPROVED'
  }
  return appointments.value.filter(a => a.status === statusMap[activeTab.value])
})

// 页面生命周期
onMounted(() => {
  loadAppointments()
})

onShow(() => {
  // 页面显示时刷新数据
  loadAppointments()
})

onPullDownRefresh(() => {
  loadAppointments()
  uni.stopPullDownRefresh()
})

// 方法实现
const switchTab = (tab) => {
  activeTab.value = tab
}

const loadAppointments = async () => {
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
    const result = await visitorApi.getMyAppointments(userId)
    if (result.success && result.data) {
      appointments.value = result.data
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
  }
}

const viewDetail = (appointment) => {
  uni.navigateTo({
    url: `/pages/visitor/appointment-detail?id=${appointment.appointmentId}`
  })
}

const createAppointment = () => {
  uni.navigateTo({ url: '/pages/visitor/create-appointment' })
}

const cancelAppointment = async (appointment) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消此预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const userId = userStore.employeeId || 1
          const result = await visitorApi.cancelAppointment(appointment.appointmentId, userId)
          if (result.success) {
            uni.showToast({ title: '取消成功', icon: 'success' })
            loadAppointments()
          } else {
            uni.showToast({ title: result.msg || '取消失败', icon: 'none' })
          }
        } catch (error) {
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

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
</script>

<style lang="scss" scoped>
.visitor-appointment-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
}

.status-bar {
  background: #fff;
}

.nav-bar {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 24rpx 32rpx 32rpx;
  text-align: center;

  .nav-title {
    font-size: 44rpx;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8rpx;
  }

  .nav-subtitle {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.8);
  }
}

.tabs-container {
  background: #fff;
  border-radius: 0 0 32rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.tabs {
  display: flex;
  padding: 0 32rpx;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 32rpx 0 24rpx;
    position: relative;
    transition: all 0.3s ease;

    .tab-text {
      font-size: 32rpx;
      color: rgba(0, 0, 0, 0.65);
      font-weight: 500;
      transition: all 0.3s ease;
    }

    .tab-indicator {
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 48rpx;
      height: 6rpx;
      background: transparent;
      border-radius: 3rpx;
      transition: all 0.3s ease;
    }

    &.active {
      .tab-text {
        color: #1890ff;
        font-weight: 700;
      }

      .tab-indicator {
        background: linear-gradient(90deg, #1890ff 0%, #096dd9 100%);
        width: 64rpx;
      }
    }
  }
}

.appointment-list {
  padding: 0 32rpx 32rpx;

  .appointment-item {
    background: #fff;
    border-radius: 24rpx;
    padding: 32rpx;
    margin-bottom: 24rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
    border: 2rpx solid transparent;
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.98);
      border-color: #1890ff;
      box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.15);
    }

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24rpx;
      padding-bottom: 24rpx;
      border-bottom: 2rpx solid #f0f0f0;

      .visitor-name {
        font-size: 36rpx;
        font-weight: 700;
        color: rgba(0, 0, 0, 0.85);
      }

      .status-tag {
        padding: 8rpx 24rpx;
        border-radius: 24rpx;
        font-size: 24rpx;
        font-weight: 600;

        &.status-PENDING {
          background: linear-gradient(135deg, #faad14 0%, #d48806 100%);
          color: #fff;
        }
        &.status-APPROVED {
          background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
          color: #fff;
        }
        &.status-REJECTED {
          background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
          color: #fff;
        }
        &.status-CHECKED_IN {
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
          color: #fff;
        }
        &.status-CANCELLED {
          background: linear-gradient(135deg, #8c8c8c 0%, #595959 100%);
          color: #fff;
        }
      }
    }

    .item-content {
      .info-row {
        display: flex;
        margin-bottom: 16rpx;
        font-size: 28rpx;
        align-items: flex-start;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: rgba(0, 0, 0, 0.45);
          margin-right: 16rpx;
          min-width: 120rpx;
          font-weight: 500;
        }

        .value {
          color: rgba(0, 0, 0, 0.85);
          flex: 1;
          line-height: 1.6;
        }
      }
    }

    .item-actions {
      margin-top: 24rpx;
      display: flex;
      justify-content: flex-end;
      padding-top: 24rpx;
      border-top: 2rpx solid #f0f0f0;

      .cancel-btn {
        background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
        color: #fff;
        border: none;
        border-radius: 32rpx;
        padding: 16rpx 32rpx;
        font-size: 28rpx;
        font-weight: 600;
        box-shadow: 0 4rpx 12rpx rgba(255, 77, 79, 0.3);
        transition: all 0.3s ease;

        &:active {
          transform: scale(0.95);
          box-shadow: 0 2rpx 6rpx rgba(255, 77, 79, 0.3);
        }
      }
    }
  }

  .no-data {
    text-align: center;
    padding: 120rpx 0;

    &::before {
      content: '📋';
      font-size: 120rpx;
      display: block;
      margin-bottom: 24rpx;
    }

    color: rgba(0, 0, 0, 0.25);
    font-size: 32rpx;
    font-weight: 500;
  }
}

.fab-button {
  position: fixed;
  right: 32rpx;
  bottom: 160rpx;
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.4);
  border: 4rpx solid #fff;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.9) rotate(90deg);
    box-shadow: 0 4rpx 12rpx rgba(24, 144, 255, 0.4);
  }

  .fab-icon {
    font-size: 64rpx;
    color: #fff;
    line-height: 1;
    font-weight: 300;
  }
}
</style>

