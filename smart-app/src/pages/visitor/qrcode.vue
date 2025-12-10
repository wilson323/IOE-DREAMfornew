<template>
  <view class="visitor-qrcode-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <text class="nav-title">访客二维码</text>
      <text class="nav-subtitle">用于访客签到验证</text>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 二维码显示区域 -->
      <view class="qrcode-section">
        <view class="qrcode-box">
          <view class="qrcode-placeholder">
            <text class="qrcode-icon">📱</text>
            <text class="qrcode-text">访客二维码</text>
          </view>
          <view class="qrcode-info">
            <text class="info-text">预约编号：{{ appointmentCode }}</text>
            <text class="info-text">有效期至：{{ formatDateTime(expireTime) }}</text>
          </view>
        </view>

        <view class="tips">
          <text class="tip-item">• 请在预约时间内使用</text>
          <text class="tip-item">• 扫码后将自动签到</text>
          <text class="tip-item">• 二维码仅限本人使用</text>
        </view>
      </view>

      <!-- 扫码按钮 -->
      <view class="scan-section">
        <button class="scan-button" @click="scanQRCode">
          <text class="button-text">扫描其他访客二维码</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import visitorApi from '@/api/business/visitor/visitor-api.js'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

// 响应式数据
const appointmentCode = ref('')
const expireTime = ref(null)

// 页面生命周期
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const appointmentId = currentPage.options.id
  if (appointmentId) {
    loadQRCodeInfo(appointmentId)
  }
})

onShow(() => {
  // 页面显示时可以刷新二维码信息
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const appointmentId = currentPage.options.id
  if (appointmentId) {
    loadQRCodeInfo(appointmentId)
  }
})

onPullDownRefresh(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const appointmentId = currentPage.options.id
  if (appointmentId) {
    loadQRCodeInfo(appointmentId)
  }
  uni.stopPullDownRefresh()
})

// 方法实现
const loadQRCodeInfo = async (appointmentId) => {
  try {
    const result = await visitorApi.getAppointmentDetail(appointmentId)
    if (result.success && result.data) {
      appointmentCode.value = result.data.appointmentCode
      expireTime.value = result.data.appointmentTime
    }
  } catch (error) {
    console.error('加载二维码信息失败:', error)
  }
}

const scanQRCode = () => {
  uni.scanCode({
    success: async (res) => {
      try {
        const result = await visitorApi.checkInByQRCode(res.result)
        if (result.success) {
          uni.showToast({ title: '签到成功', icon: 'success' })
        } else {
          uni.showToast({ title: result.msg || '签到失败', icon: 'none' })
        }
      } catch (error) {
        uni.showToast({ title: '签到失败', icon: 'none' })
      }
    },
    fail: (error) => {
      uni.showToast({ title: '扫码失败', icon: 'none' })
    }
  })
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.visitor-qrcode-page {
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

.qrcode-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 30px 20px;
  text-align: center;
  margin-bottom: 15px;

  .qrcode-box {
    .qrcode-placeholder {
      width: 200px;
      height: 200px;
      margin: 0 auto 20px;
      border: 2px dashed #d9d9d9;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: #fafafa;

      .qrcode-icon {
        font-size: 64px;
        margin-bottom: 10px;
      }

      .qrcode-text {
        font-size: 14px;
        color: #666;
      }
    }

    .qrcode-info {
      margin-top: 20px;

      .info-text {
        display: block;
        font-size: 13px;
        color: #666;
        line-height: 1.8;
      }
    }
  }

  .tips {
    margin-top: 30px;
    text-align: left;

    .tip-item {
      display: block;
      font-size: 12px;
      color: #999;
      line-height: 2;
    }
  }
}

.scan-section {
  .scan-button {
    width: 100%;
    height: 44px;
    background-color: #1890ff;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 16px;
  }
}
</style>

