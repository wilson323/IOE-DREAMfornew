<template>
  <view class="consume-qrcode-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">扫码支付</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 扫码区域 -->
      <view class="scan-section">
        <view class="scan-box">
          <view class="scan-icon">📱</view>
          <text class="scan-text">请扫描商家二维码</text>
          <button class="scan-button" @click="scanQRCode">
            <text>开始扫码</text>
          </button>
        </view>
      </view>

      <!-- 最近扫码记录 -->
      <view class="recent-scans">
        <view class="section-title">最近扫码</view>
        <view class="scan-list">
          <view
            class="scan-item"
            v-for="(scan, index) in recentScans"
            :key="index"
          >
            <text class="merchant-name">{{ scan.merchantName }}</text>
            <text class="scan-time">{{ formatDateTime(scan.scanTime) }}</text>
          </view>

          <view class="no-data" v-if="recentScans.length === 0">
            <text>暂无扫码记录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import consumeApi from '@/api/business/consume/consume-api.js'

export default {
  name: 'ConsumeQRCode',
  setup() {
    const recentScans = ref([])

    // 扫描二维码
    const scanQRCode = () => {
      uni.scanCode({
        success: async (res) => {
          uni.showLoading({ title: '处理中...' })
          try {
            const result = await consumeApi.scanConsume({
              qrCode: res.result,
              deviceId: 'MOBILE_001'
            })
            uni.hideLoading()
            if (result.success) {
              uni.showToast({ title: '支付成功', icon: 'success' })
              loadRecentScans()
            } else {
              uni.showToast({ title: result.msg || '支付失败', icon: 'none' })
            }
          } catch (error) {
            uni.hideLoading()
            uni.showToast({ title: '支付失败', icon: 'none' })
          }
        },
        fail: () => {
          uni.showToast({ title: '扫码失败', icon: 'none' })
        }
      })
    }

    // 加载最近扫码记录
    const loadRecentScans = async () => {
      try {
        const result = await consumeApi.getRecentHistory({ limit: 10 })
        if (result.success && result.data) {
          recentScans.value = result.data
        }
      } catch (error) {
        console.error('加载扫码记录失败:', error)
      }
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

    // 返回
    const goBack = () => {
      uni.navigateBack()
    }

    // 初始化
    onMounted(() => {
      loadRecentScans()
    })

    return {
      recentScans,
      scanQRCode,
      formatDateTime,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.consume-qrcode-page {
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

.scan-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 40px 20px;
  text-align: center;
  margin-bottom: 15px;

  .scan-box {
    .scan-icon {
      font-size: 64px;
      margin-bottom: 15px;
    }

    .scan-text {
      display: block;
      font-size: 16px;
      color: #666;
      margin-bottom: 20px;
    }

    .scan-button {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border: none;
      border-radius: 25px;
      padding: 12px 40px;
      font-size: 16px;
    }
  }
}

.recent-scans {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }

  .scan-list {
    .scan-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .merchant-name {
        font-size: 14px;
        color: #333;
      }

      .scan-time {
        font-size: 12px;
        color: #999;
      }
    }

    .no-data {
      text-align: center;
      padding: 30px 0;
      color: #999;
      font-size: 14px;
    }
  }
}
</style>

