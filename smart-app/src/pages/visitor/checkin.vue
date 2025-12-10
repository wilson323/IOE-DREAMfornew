<template>
  <view class="visitor-checkin-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="iconfont icon-back">←</text>
      </view>
      <view class="nav-title">访客签到</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 扫码区域 -->
      <view class="scan-section">
        <view class="scan-box">
          <view class="scan-icon">📱</view>
          <text class="scan-text">请扫描访客二维码</text>
          <button class="scan-button" @click="scanQRCode">
            <text class="button-text">开始扫码</text>
          </button>
        </view>

        <view class="or-divider">
          <text>或</text>
        </view>

        <!-- 手动输入 -->
        <view class="manual-input">
          <input
            class="input-field"
            v-model="qrCodeValue"
            placeholder="请输入二维码内容"
            placeholder-style="color: #999;"
          />
          <button class="verify-button" @click="verifyQRCode" :disabled="!qrCodeValue || verifying">
            <text class="button-text">{{ verifying ? '验证中...' : '验证' }}</text>
          </button>
        </view>
      </view>

      <!-- 身份验证区域 -->
      <view class="idcard-section">
        <view class="section-title">身份证验证</view>
        <view class="form-group">
          <view class="form-label">身份证号</view>
          <input
            class="form-input"
            v-model="idCardForm.idCardNumber"
            placeholder="请输入身份证号"
            placeholder-style="color: #999;"
          />
        </view>
        <view class="form-group">
          <view class="form-label">手机号</view>
          <input
            class="form-input"
            v-model="idCardForm.phoneNumber"
            placeholder="请输入手机号"
            placeholder-style="color: #999;"
          />
        </view>
        <button class="submit-button" @click="verifyIdCard" :disabled="verifying">
          <text class="button-text">{{ verifying ? '验证中...' : '身份证验证' }}</text>
        </button>
      </view>
    </view>

    <!-- 验证结果弹窗 -->
    <view class="result-modal" v-if="showResultModal" @click="closeResultModal">
      <view class="modal-content" @click.stop>
        <view :class="['result-icon', verifyResult.success ? 'success' : 'error']">
          {{ verifyResult.success ? '✓' : '✗' }}
        </view>
        <view class="result-message">{{ verifyResult.message }}</view>
        <view class="visitor-info" v-if="verifyResult.success && verifyResult.data">
          <view class="info-item">
            <text class="info-label">访客姓名：</text>
            <text class="info-value">{{ verifyResult.data.visitorName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">签到状态：</text>
            <text class="info-value">{{ verifyResult.data.isCheckedIn ? '已签到' : '未签到' }}</text>
          </view>
        </view>
        <button class="confirm-button" @click="closeResultModal">
          <text class="button-text">确定</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import visitorApi from '@/api/business/visitor/visitor-api.js'

// 响应式数据
const qrCodeValue = ref('')
const verifying = ref(false)
const showResultModal = ref(false)

const idCardForm = reactive({
  idCardNumber: '',
  phoneNumber: ''
})

const verifyResult = reactive({
  success: false,
  message: '',
  data: null
})

// 页面生命周期
onMounted(() => {
  // 初始化页面
})

onShow(() => {
  // 页面显示时重置状态
  qrCodeValue.value = ''
  idCardForm.idCardNumber = ''
  idCardForm.phoneNumber = ''
})

// 方法实现
const scanQRCode = () => {
  uni.scanCode({
    success: async (res) => {
      qrCodeValue.value = res.result
      await verifyQRCode()
    },
    fail: (error) => {
      uni.showToast({
        title: '扫码失败',
        icon: 'none'
      })
    }
  })
}

const verifyQRCode = async () => {
  if (!qrCodeValue.value) {
    uni.showToast({ title: '请输入二维码内容', icon: 'none' })
    return
  }

  verifying.value = true
  try {
    const result = await visitorApi.checkInByQRCode(qrCodeValue.value)
    if (result.success) {
      verifyResult.success = true
      verifyResult.message = '验证成功，访客已签到'
      verifyResult.data = result.data
      showResultModal.value = true
      qrCodeValue.value = ''
    } else {
      verifyResult.success = false
      verifyResult.message = result.msg || '验证失败'
      verifyResult.data = null
      showResultModal.value = true
    }
  } catch (error) {
    uni.showToast({ title: '验证失败', icon: 'none' })
  } finally {
    verifying.value = false
  }
}

const verifyIdCard = async () => {
  if (!idCardForm.idCardNumber || !idCardForm.phoneNumber) {
    uni.showToast({ title: '请输入身份证号和手机号', icon: 'none' })
    return
  }

  verifying.value = true
  try {
    const result = await visitorApi.validateVisitorInfo(
      idCardForm.idCardNumber,
      idCardForm.phoneNumber
    )
    if (result.success && result.data) {
      verifyResult.success = result.data.isValid
      verifyResult.message = result.data.isValid ? '验证成功' : '访客信息不存在'
      verifyResult.data = result.data
      showResultModal.value = true
      if (result.data.isValid) {
        idCardForm.idCardNumber = ''
        idCardForm.phoneNumber = ''
      }
    } else {
      uni.showToast({ title: result.msg || '验证失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: '验证失败', icon: 'none' })
  } finally {
    verifying.value = false
  }
}

const closeResultModal = () => {
  showResultModal.value = false
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.visitor-checkin-page {
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
    height: 40px;
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
  padding: 30px 20px;
  margin-bottom: 15px;
  text-align: center;

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

  .or-divider {
    margin: 30px 0;
    position: relative;
    text-align: center;

    &::before, &::after {
      content: '';
      position: absolute;
      top: 50%;
      width: 40%;
      height: 1px;
      background-color: #e0e0e0;
    }

    &::before { left: 0; }
    &::after { right: 0; }

    text {
      padding: 0 10px;
      background-color: #fff;
      color: #999;
      font-size: 14px;
    }
  }

  .manual-input {
    display: flex;
    gap: 10px;

    .input-field {
      flex: 1;
      height: 44px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      padding: 0 15px;
      font-size: 14px;
    }

    .verify-button {
      width: 80px;
      height: 44px;
      background-color: #1890ff;
      color: #fff;
      border: none;
      border-radius: 4px;
      font-size: 14px;

      &[disabled] {
        opacity: 0.6;
      }
    }
  }
}

.idcard-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 15px;
  }

  .form-group {
    margin-bottom: 15px;

    .form-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }

    .form-input {
      width: 100%;
      height: 44px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      padding: 0 15px;
      font-size: 14px;
    }
  }

  .submit-button {
    width: 100%;
    height: 44px;
    background-color: #1890ff;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    margin-top: 10px;

    &[disabled] {
      opacity: 0.6;
    }
  }
}

.result-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;

  .modal-content {
    width: 80%;
    background-color: #fff;
    border-radius: 10px;
    padding: 30px 20px;
    text-align: center;

    .result-icon {
      width: 60px;
      height: 60px;
      line-height: 60px;
      border-radius: 50%;
      margin: 0 auto 15px;
      font-size: 32px;
      color: #fff;

      &.success { background-color: #52c41a; }
      &.error { background-color: #ff4d4f; }
    }

    .result-message {
      font-size: 16px;
      color: #333;
      margin-bottom: 15px;
    }

    .visitor-info {
      text-align: left;
      margin: 20px 0;

      .info-item {
        display: flex;
        margin-bottom: 10px;
        font-size: 14px;

        .info-label {
          color: #666;
        }

        .info-value {
          color: #333;
          font-weight: 500;
        }
      }
    }

    .confirm-button {
      width: 100%;
      height: 44px;
      background-color: #1890ff;
      color: #fff;
      border: none;
      border-radius: 4px;
      font-size: 16px;
      margin-top: 10px;
    }
  }
}
</style>

