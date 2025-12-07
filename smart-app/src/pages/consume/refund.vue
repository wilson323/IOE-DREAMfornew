<template>
  <view class="refund-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <text class="back-btn" @click="goBack">‹</text>
      <text class="nav-title">申请退款</text>
      <text class="help-btn" @click="showHelp">帮助</text>
    </view>

    <!-- 账户信息 -->
    <view class="account-info">
      <text class="info-label">当前余额</text>
      <text class="info-value">¥{{ formatAmount(accountBalance) }}</text>
    </view>

    <!-- 退款表单 -->
    <view class="refund-form">
      <view class="form-item">
        <text class="form-label">退款金额</text>
        <view class="form-input-box">
          <text class="input-prefix">¥</text>
          <input
            class="form-input"
            type="digit"
            v-model="refundAmount"
            placeholder="请输入退款金额"
          />
        </view>
        <text class="form-tip">最大可退: ¥{{ formatAmount(accountBalance) }}</text>
      </view>

      <view class="form-item">
        <text class="form-label">退款原因</text>
        <textarea
          class="form-textarea"
          v-model="refundReason"
          placeholder="请输入退款原因（必填）"
          maxlength="200"
        />
        <text class="char-count">{{ refundReason.length }}/200</text>
      </view>

      <view class="form-item">
        <text class="form-label">联系电话</text>
        <input
          class="form-input-plain"
          type="tel"
          v-model="contactPhone"
          placeholder="请输入联系电话"
        />
      </view>
    </view>

    <!-- 退款说明 -->
    <view class="refund-notice">
      <text class="notice-title">⚠️ 退款说明</text>
      <text class="notice-item">• 退款将在1-3个工作日内处理</text>
      <text class="notice-item">• 退款金额将原路返回到支付账户</text>
      <text class="notice-item">• 如有疑问请联系财务部门</text>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button
        :class="['submit-btn', { disabled: !canSubmit || submitting }]"
        :disabled="!canSubmit || submitting"
        @click="submitRefund"
      >
        <text>{{ submitting ? '提交中...' : '提交申请' }}</text>
      </button>
    </view>
  </view>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import consumeApi from '@/api/business/consume/consume-api.js'
import { useUserStore } from '@/store/modules/system/user'

export default {
  name: 'ConsumeRefund',
  
  setup() {
    const systemInfo = uni.getSystemInfoSync()
    const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
    const userStore = useUserStore()

    const submitting = ref(false)
    const accountBalance = ref(0)
    const refundAmount = ref('')
    const refundReason = ref('')
    const contactPhone = ref('')

    const canSubmit = computed(() => {
      const amount = Number(refundAmount.value)
      return amount > 0 &&
             amount <= accountBalance.value &&
             refundReason.value.trim().length >= 5 &&
             /^1[3-9]\d{9}$/.test(contactPhone.value)
    })

    onMounted(() => {
      init()
    })

    const init = async () => {
      const userId = userStore.userId || 1
      await loadAccountInfo(userId)
      
      // 预填联系电话
      contactPhone.value = userStore.phone || ''
    }

    const loadAccountInfo = async (userId) => {
      try {
        const res = await consumeApi.getAccountBalance(userId)
        if (res.code === 1 && res.data) {
          accountBalance.value = res.data.balance || res.data || 0
        }
      } catch (error) {
        console.error('加载账户信息失败:', error)
      }
    }

    const submitRefund = async () => {
      if (!canSubmit.value) {
        if (Number(refundAmount.value) > accountBalance.value) {
          uni.showToast({ title: '退款金额不能大于余额', icon: 'none' })
        } else if (refundReason.value.trim().length < 5) {
          uni.showToast({ title: '请输入退款原因', icon: 'none' })
        } else {
          uni.showToast({ title: '请填写完整信息', icon: 'none' })
        }
        return
      }

      submitting.value = true

      try {
        const res = await consumeApi.accountApi.requestRefund({
          userId: userStore.userId,
          amount: Number(refundAmount.value),
          reason: refundReason.value,
          contactPhone: contactPhone.value
        })

        if (res.code === 1) {
          uni.showModal({
            title: '提交成功',
            content: '退款申请已提交，请等待审核',
            showCancel: false,
            success: () => {
              uni.navigateBack()
            }
          })
        } else {
          uni.showToast({ title: res.message || '提交失败', icon: 'none' })
        }
      } catch (error) {
        console.error('提交退款申请失败:', error)
        uni.showToast({ title: '提交失败', icon: 'none' })
      } finally {
        submitting.value = false
      }
    }

    const showHelp = () => {
      uni.showModal({
        title: '退款帮助',
        content: '退款将在1-3个工作日内审核处理，审核通过后资金将原路返回。如有疑问请联系财务部门。',
        showCancel: false
      })
    }

    const formatAmount = (amount) => {
      if (!amount && amount !== 0) return '0.00'
      return Number(amount).toFixed(2)
    }

    const goBack = () => {
      uni.navigateBack()
    }

    return {
      statusBarHeight,
      submitting,
      accountBalance,
      refundAmount,
      refundReason,
      contactPhone,
      canSubmit,
      submitRefund,
      showHelp,
      formatAmount,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.refund-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  background: #fff;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  
  .back-btn {
    font-size: 48rpx;
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
  }
  
  .help-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.account-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 24rpx 32rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .info-label {
    font-size: 28rpx;
    color: rgba(0, 0, 0, 0.65);
  }
  
  .info-value {
    font-size: 44rpx;
    font-weight: 600;
    color: #1890ff;
  }
}

.refund-form {
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  
  .form-item {
    margin-bottom: 32rpx;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .form-label {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 16rpx;
    }
    
    .form-input-box {
      display: flex;
      align-items: center;
      height: 88rpx;
      padding: 0 24rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      
      .input-prefix {
        font-size: 40rpx;
        font-weight: 600;
        color: #1890ff;
        margin-right: 16rpx;
      }
      
      .form-input {
        flex: 1;
        font-size: 40rpx;
        font-weight: 600;
      }
    }
    
    .form-input-plain {
      height: 88rpx;
      padding: 0 24rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 28rpx;
    }
    
    .form-textarea {
      width: 100%;
      min-height: 200rpx;
      padding: 24rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 28rpx;
      line-height: 1.6;
    }
    
    .form-tip,
    .char-count {
      display: block;
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
      margin-top: 12rpx;
    }
    
    .char-count {
      text-align: right;
    }
  }
}

.refund-notice {
  margin: 0 32rpx 24rpx;
  padding: 24rpx;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 12rpx;
  
  .notice-title {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: #faad14;
    margin-bottom: 16rpx;
  }
  
  .notice-item {
    display: block;
    font-size: 26rpx;
    color: rgba(0, 0, 0, 0.65);
    line-height: 1.6;
    margin-bottom: 8rpx;
  }
}

.submit-section {
  padding: 0 32rpx 32rpx;
  
  .submit-btn {
    width: 100%;
    height: 120rpx;
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    border: none;
    border-radius: 60rpx;
    font-size: 40rpx;
    font-weight: 600;
    color: #fff;
    box-shadow: 0 8rpx 24rpx rgba(82, 196, 26, 0.4);
    
    &:active:not(.disabled) {
      transform: scale(0.98);
    }
    
    &.disabled {
      background: #d9d9d9;
      box-shadow: none;
    }
  }
}
</style>

