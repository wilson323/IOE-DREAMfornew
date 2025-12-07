<template>
  <view class="visitor-appointment-page">
    <!-- 标签页 -->
    <view class="tabs">
      <view
        :class="['tab-item', activeTab === 'all' ? 'active' : '']"
        @click="switchTab('all')"
      >
        <text>全部</text>
      </view>
      <view
        :class="['tab-item', activeTab === 'pending' ? 'active' : '']"
        @click="switchTab('pending')"
      >
        <text>待审批</text>
      </view>
      <view
        :class="['tab-item', activeTab === 'approved' ? 'active' : '']"
        @click="switchTab('approved')"
      >
        <text>已批准</text>
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

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import visitorApi from '@/api/business/visitor/visitor-api.js'

export default {
  name: 'VisitorAppointment',
  setup() {
    const userStore = useUserStore()
    const activeTab = ref('all')
    const appointments = ref([])

    // 过滤后的预约列表
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

    // 切换标签
    const switchTab = (tab) => {
      activeTab.value = tab
    }

    // 加载预约列表
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

    // 查看详情
    const viewDetail = (appointment) => {
      uni.navigateTo({
        url: `/pages/visitor/appointment-detail?id=${appointment.appointmentId}`
      })
    }

    // 创建预约
    const createAppointment = () => {
      uni.navigateTo({ url: '/pages/visitor/create-appointment' })
    }

    // 取消预约
    const cancelAppointment = async (appointment) => {
      uni.showModal({
        title: '提示',
        content: '确定要取消此预约吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              const userId = 1 // TODO: 从本地存储获取
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
      loadAppointments()
    })

    return {
      activeTab,
      appointments,
      filteredAppointments,
      switchTab,
      viewDetail,
      createAppointment,
      cancelAppointment,
      formatDateTime,
      getStatusText
    }
  }
}
</script>

<style lang="scss" scoped>
.visitor-appointment-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.tabs {
  display: flex;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 15px 0;
    font-size: 14px;
    color: #666;
    position: relative;

    &.active {
      color: #1890ff;
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 30px;
        height: 3px;
        background-color: #1890ff;
        border-radius: 2px;
      }
    }
  }
}

.appointment-list {
  padding: 15px;

  .appointment-item {
    background-color: #fff;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .visitor-name {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }

      .status-tag {
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;

        &.status-PENDING { background-color: #faad14; color: #fff; }
        &.status-APPROVED { background-color: #52c41a; color: #fff; }
        &.status-REJECTED { background-color: #ff4d4f; color: #fff; }
        &.status-CHECKED_IN { background-color: #1890ff; color: #fff; }
      }
    }

    .item-content {
      .info-row {
        display: flex;
        margin-bottom: 8px;
        font-size: 13px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #666;
          margin-right: 4px;
        }

        .value {
          color: #333;
          flex: 1;
        }
      }
    }

    .item-actions {
      margin-top: 12px;
      display: flex;
      justify-content: flex-end;

      .cancel-btn {
        background-color: #ff4d4f;
        color: #fff;
        border: none;
        border-radius: 4px;
        padding: 6px 12px;
        font-size: 12px;
      }
    }
  }

  .no-data {
    text-align: center;
    padding: 40px 0;
    color: #999;
    font-size: 14px;
  }
}

.fab-button {
  position: fixed;
  right: 20px;
  bottom: 80px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.5);

  .fab-icon {
    font-size: 32px;
    color: #fff;
    line-height: 1;
  }
}
</style>

