<template>
  <view class="recharge-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <text class="back-btn" @click="goBack">‹</text>
      <text class="nav-title">账户充值</text>
      <text class="record-btn" @click="goToRechargeRecord">记录</text>
    </view>

    <!-- 账户信息卡片 -->
    <view class="account-card">
      <view class="card-header">
        <text class="card-title">当前余额</text>
      </view>
      <view class="balance-display">
        <text class="balance-amount">¥{{ formatAmount(accountBalance) }}</text>
      </view>
    </view>

    <!-- 充值金额选择 -->
    <view class="recharge-amount-section">
      <text class="section-title">选择充值金额</text>
      <view class="amount-grid">
        <button
          v-for="amount in rechargeAmounts"
          :key="amount"
          :class="['amount-btn', { selected: selectedAmount === amount }]"
          @click="selectAmount(amount)"
        >
          <text class="amount-text">¥{{ amount }}</text>
        </button>
      </view>
    </view>

    <!-- 自定义充值金额 -->
    <view class="custom-amount-section">
      <text class="section-title">自定义金额</text>
      <view class="custom-input-box">
        <text class="input-prefix">¥</text>
        <input
          class="custom-input"
          type="digit"
          v-model="customAmount"
          placeholder="输入充值金额"
          @input="onCustomInput"
        />
      </view>
      <text class="amount-tip">单次充值限额: ¥10 - ¥5000</text>
    </view>

    <!-- 支付方式选择 -->
    <view class="payment-method-section">
      <text class="section-title">支付方式</text>
      <view class="method-list">
        <view
          v-for="method in paymentMethods"
          :key="method.code"
          :class="['method-item', { selected: selectedMethod === method.code }]"
          @click="selectMethod(method.code)"
        >
          <view class="method-icon">{{ method.icon }}</view>
          <text class="method-name">{{ method.name }}</text>
          <view class="method-check">
            <text v-if="selectedMethod === method.code">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 确认充值按钮 -->
    <view class="confirm-section">
      <button
        :class="['confirm-btn', { disabled: !canRecharge || recharging }]"
        :disabled="!canRecharge || recharging"
        @click="confirmRecharge"
      >
        <text>{{ recharging ? '处理中...' : `确认充值 ¥${finalAmount}` }}</text>
      </button>
    </view>
  </view>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import consumeApi from '@/api/business/consume/consume-api.js'
import { useUserStore } from '@/store/modules/system/user'

export default {
  name: 'ConsumeRecharge',
  
  setup() {
    const systemInfo = uni.getSystemInfoSync()
    const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
    const userStore = useUserStore()

    // 页面状态
    const recharging = ref(false)
    const accountBalance = ref(0)

    // 充值金额
    const rechargeAmounts = [50, 100, 200, 500, 1000, 2000]
    const selectedAmount = ref(0)
    const customAmount = ref('')

    // 支付方式
    const paymentMethods = [
      { code: 'wechat', name: '微信支付', icon: '💚' },
      { code: 'alipay', name: '支付宝', icon: '💙' },
      { code: 'bank', name: '银行卡', icon: '💳' }
    ]
    const selectedMethod = ref('wechat')

    // 计算最终金额
    const finalAmount = computed(() => {
      return customAmount.value || selectedAmount.value || 0
    })

    // 是否可以充值
    const canRecharge = computed(() => {
      const amount = Number(finalAmount.value)
      return amount >= 10 && amount <= 5000
    })

    onMounted(() => {
      init()
    })

    const init = async () => {
      const userId = userStore.userId || 1
      await loadAccountInfo(userId)
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

    const selectAmount = (amount) => {
      selectedAmount.value = selectedAmount.value === amount ? 0 : amount
      customAmount.value = ''
      uni.vibrateShort()
    }

    const onCustomInput = () => {
      selectedAmount.value = 0
    }

    const selectMethod = (method) => {
      selectedMethod.value = method
      uni.vibrateShort()
    }

    const confirmRecharge = async () => {
      if (!canRecharge.value) return

      const amount = Number(finalAmount.value)
      
      // 二次确认
      const confirmed = await showConfirm(`确认充值 ¥${formatAmount(amount)}？`)
      if (!confirmed) return

      recharging.value = true

      try {
        // 调用充值API（这里应该集成实际的支付SDK）
        const res = await consumeApi.accountApi.recharge({
          userId: userStore.userId,
          amount,
          paymentMethod: selectedMethod.value
        })

        if (res.code === 1) {
          // 更新余额
          accountBalance.value += amount
          
          // 显示成功
          uni.showToast({ title: '充值成功', icon: 'success' })
          uni.vibrateLong()
          
          // 2秒后返回
          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        } else {
          uni.showToast({ title: res.message || '充值失败', icon: 'none' })
        }
      } catch (error) {
        console.error('充值失败:', error)
        uni.showToast({ title: '充值失败', icon: 'none' })
      } finally {
        recharging.value = false
      }
    }

    const showConfirm = (content) => {
      return new Promise((resolve) => {
        uni.showModal({
          title: '确认充值',
          content,
          success: (res) => resolve(res.confirm)
        })
      })
    }

    const formatAmount = (amount) => {
      if (!amount && amount !== 0) return '0.00'
      return Number(amount).toFixed(2)
    }

    const goToRechargeRecord = () => {
      uni.navigateTo({ url: '/pages/consume/transaction?type=recharge' })
    }

    const goBack = () => {
      uni.navigateBack()
    }

    return {
      statusBarHeight,
      recharging,
      accountBalance,
      rechargeAmounts,
      selectedAmount,
      customAmount,
      paymentMethods,
      selectedMethod,
      finalAmount,
      canRecharge,
      selectAmount,
      onCustomInput,
      selectMethod,
      confirmRecharge,
      formatAmount,
      goToRechargeRecord,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.recharge-page {
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
    color: rgba(0, 0, 0, 0.85);
  }
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }
  
  .record-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.account-card {
  margin: 24rpx 32rpx;
  padding: 48rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
  
  .card-header {
    margin-bottom: 16rpx;
  }
  
  .card-title {
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.85);
  }
  
  .balance-display {
    .balance-amount {
      font-size: 80rpx;
      font-weight: 600;
      color: #fff;
    }
  }
}

.recharge-amount-section,
.custom-amount-section,
.payment-method-section {
  margin: 0 32rpx 32rpx;
  
  .section-title {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    margin-bottom: 24rpx;
  }
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24rpx;
}

.amount-btn {
  height: 140rpx;
  background: #fff;
  border: 3rpx solid #e8e8e8;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.selected {
    border-color: #1890ff;
    background: #e6f7ff;
    
    .amount-text {
      color: #1890ff;
    }
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  .amount-text {
    font-size: 44rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }
}

.custom-input-box {
  display: flex;
  align-items: center;
  height: 120rpx;
  padding: 0 32rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  
  .input-prefix {
    font-size: 48rpx;
    font-weight: 600;
    color: #1890ff;
    margin-right: 16rpx;
  }
  
  .custom-input {
    flex: 1;
    font-size: 56rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }
}

.amount-tip {
  display: block;
  font-size: 24rpx;
  color: rgba(0, 0, 0, 0.45);
  padding-left: 16rpx;
}

.method-list {
  .method-item {
    display: flex;
    align-items: center;
    height: 120rpx;
    padding: 0 32rpx;
    background: #fff;
    border-radius: 16rpx;
    margin-bottom: 16rpx;
    border: 3rpx solid #e8e8e8;
    
    &.selected {
      border-color: #1890ff;
      background: #e6f7ff;
    }
    
    &:active {
      background: #f5f5f5;
    }
    
    .method-icon {
      font-size: 48rpx;
      margin-right: 24rpx;
    }
    
    .method-name {
      flex: 1;
      font-size: 32rpx;
      color: rgba(0, 0, 0, 0.85);
    }
    
    .method-check {
      font-size: 32rpx;
      color: #1890ff;
      font-weight: 600;
    }
  }
}

.confirm-section {
  padding: 48rpx 32rpx 32rpx;
  
  .confirm-btn {
    width: 100%;
    height: 120rpx;
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    border: none;
    border-radius: 60rpx;
    font-size: 40rpx;
    font-weight: 600;
    color: #fff;
    box-shadow: 0 8rpx 24rpx rgba(24, 144, 255, 0.4);
    
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

