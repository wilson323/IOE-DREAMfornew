<template>
  <view class="visitor-checkout-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="iconfont icon-back">←</text>
      </view>
      <view class="nav-title">访客签退</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 访客信息 -->
      <view class="visitor-info-card" v-if="appointmentDetail">
        <view class="info-header">
          <text class="visitor-name">{{ appointmentDetail.visitorName }}</text>
          <text class="status-tag">{{ getStatusText(appointmentDetail.status) }}</text>
        </view>
        <view class="info-content">
          <view class="info-row">
            <text class="label">被访人：</text>
            <text class="value">{{ appointmentDetail.visiteeName }}</text>
          </view>
          <view class="info-row">
            <text class="label">签到时间：</text>
            <text class="value">{{ formatDateTime(appointmentDetail.checkInTime) }}</text>
          </view>
          <view class="info-row">
            <text class="label">停留时长：</text>
            <text class="value">{{ calculateDuration() }}</text>
          </view>
        </view>
      </view>

      <!-- 签退确认 -->
      <view class="checkout-section">
        <view class="section-title">确认签退</view>
        <view class="checkout-tips">
          <text>• 签退后将无法再次访问</text>
          <text>• 请确保已完成所有访问事项</text>
        </view>
        <button class="checkout-button" @click="handleCheckout" :disabled="checkingOut">
          <text class="button-text">{{ checkingOut ? '签退中...' : '确认签退' }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import visitorApi from '@/api/business/visitor/visitor-api.js'

// 响应式数据
const appointmentDetail = ref(null)
const checkingOut = ref(false)

// 页面生命周期
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const appointmentId = currentPage.options.id
  if (appointmentId) {
    loadAppointmentDetail(appointmentId)
  }
})

onShow(() => {
  // 页面显示时可以刷新数据
  if (appointmentDetail.value && appointmentDetail.value.appointmentId) {
    loadAppointmentDetail(appointmentDetail.value.appointmentId)
  }
})

// 方法实现
const loadAppointmentDetail = async (appointmentId) => {
  try {
    const result = await visitorApi.getAppointmentDetail(appointmentId)
    if (result.success && result.data) {
      appointmentDetail.value = result.data
    }
  } catch (error) {
    console.error('加载预约详情失败:', error)
  }
}

const handleCheckout = async () => {
  if (!appointmentDetail.value) {
    uni.showToast({ title: '预约信息不存在', icon: 'none' })
    return
  }

  uni.showModal({
    title: '确认签退',
    content: '确定要签退吗？',
    success: async (res) => {
      if (res.confirm) {
        checkingOut.value = true
        try {
          const result = await visitorApi.checkout(appointmentDetail.value.appointmentId)
          if (result.success) {
            uni.showToast({ title: '签退成功', icon: 'success' })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            uni.showToast({ title: result.msg || '签退失败', icon: 'none' })
          }
        } catch (error) {
          uni.showToast({ title: '签退失败', icon: 'none' })
        } finally {
          checkingOut.value = false
        }
      }
    }
  })
}

const calculateDuration = () => {
  if (!appointmentDetail.value || !appointmentDetail.value.checkInTime) {
    return '-'
  }
  const checkInTime = new Date(appointmentDetail.value.checkInTime)
  const now = new Date()
  const minutes = Math.floor((now - checkInTime) / 1000 / 60)
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
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
    'CHECKED_IN': '已签到',
    'CHECKED_OUT': '已签退'
  }
  return textMap[status] || status
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.visitor-checkout-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
  }

  .icon-back {
    font-size: 20px;
    color: #333;
  }
}

.page-content {
  padding: 15px;
}

.visitor-info-card {
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 15px;

  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;

    .visitor-name {
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }

    .status-tag {
      padding: 4px 12px;
      background-color: #1890ff;
      color: #fff;
      border-radius: 4px;
      font-size: 12px;
    }
  }

  .info-content {
    .info-row {
      display: flex;
      margin-bottom: 12px;
      font-size: 14px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        color: #666;
        width: 80px;
      }

      .value {
        color: #333;
        flex: 1;
      }
    }
  }
}

.checkout-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 15px;
  }

  .checkout-tips {
    margin-bottom: 20px;

    text {
      display: block;
      font-size: 13px;
      color: #666;
      line-height: 1.8;
    }
  }

  .checkout-button {
    width: 100%;
    height: 44px;
    background-color: #1890ff;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 16px;

    &[disabled] {
      opacity: 0.6;
    }
  }
}
</style>

