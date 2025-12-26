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
          v-for="(amount, index) in rechargeAmounts"
          :key="amount"
          :class="['amount-btn', { selected: selectedAmount === amount, pressed: pressedAmountIndex === index }]"
          @touchstart="pressedAmountIndex = index"
          @touchend="pressedAmountIndex = -1"
          @touchcancel="pressedAmountIndex = -1"
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
          v-for="(method, index) in paymentMethods"
          :key="method.code"
          :class="['method-item', { selected: selectedMethod === method.code, pressed: pressedMethodIndex === index }]"
          @touchstart="pressedMethodIndex = index"
          @touchend="pressedMethodIndex = -1"
          @touchcancel="pressedMethodIndex = -1"
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

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { rechargeApi } from '@/api/business/consume/recharge-api.js'
import consumeApi from '@/api/business/consume/consume-api.js'
import cacheManager from '@/utils/cache-manager.js'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const userStore = useUserStore()

// 页面状态
const recharging = ref(false)
const accountBalance = ref(0)
const currentOrderId = ref(null)
const pollingTimer = ref(null)
const loadingBalance = ref(false) // 余额加载状态
const pressedAmountIndex = ref(-1) // 金额按钮触摸反馈
const pressedMethodIndex = ref(-1) // 支付方式触摸反馈

// 充值金额
const rechargeAmounts = [50, 100, 200, 500, 1000, 2000]
const selectedAmount = ref(0)
const customAmount = ref('')

// 支付方式
const paymentMethods = [
  { code: 'WECHAT', name: '微信支付', icon: '💚' },
  { code: 'ALIPAY', name: '支付宝', icon: '💙' }
]
const selectedMethod = ref('WECHAT')

// 计算最终金额
const finalAmount = computed(() => {
  return customAmount.value || selectedAmount.value || 0
})

// 是否可以充值
const canRecharge = computed(() => {
  const amount = Number(finalAmount.value)
  return amount >= 10 && amount <= 5000
})

// 页面生命周期
onMounted(() => {
  init()
})

onShow(() => {
  // 页面显示时可以刷新余额
  const userId = userStore.employeeId
  if (userId) {
    loadAccountInfo(userId)
  }
})

onUnmounted(() => {
  // 清理轮询定时器
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
})

// 方法实现
const init = async () => {
  const userId = userStore.userId || userStore.employeeId || 1
  await loadAccountInfo(userId)
}

const loadAccountInfo = async (userId) => {
  try {
    // 生成缓存key
    const cacheKey = `account_balance_${userId}`

    // 先尝试从缓存获取（余额数据有效期较短，2分钟）
    const cachedData = cacheManager.getCache(cacheKey)
    if (cachedData !== null) {
      console.log('[充值] 使用缓存余额数据')
      accountBalance.value = cachedData
      return
    }

    // 缓存未命中，请求API
    const res = await consumeApi.getAccountBalance(userId)
    if (res.code === 1 && res.data) {
      const balance = res.data.balance || res.data || 0
      accountBalance.value = balance
      // 缓存余额数据，有效期2分钟（120000ms）
      cacheManager.setCache(cacheKey, balance, 120000)
      console.log('[充值] 已缓存余额数据')
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
  const userId = userStore.employeeId

  if (!userId) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  // 二次确认
  const confirmed = await showConfirm(`确认充值 ¥${formatAmount(amount)}？`)
  if (!confirmed) return

  recharging.value = true

  try {
    // 步骤1: 创建充值订单
    const createResult = await rechargeApi.createOrder({
      userId: userId,
      rechargeAmount: amount,
      paymentMethod: selectedMethod.value
    })

    if (!createResult.success || !createResult.data) {
      throw new Error(createResult.message || '创建订单失败')
    }

    const orderId = createResult.data.orderId
    currentOrderId.value = orderId

    // 步骤2: 发起支付
    if (selectedMethod.value === 'WECHAT') {
      await handleWechatPay(orderId, createResult.data)
    } else if (selectedMethod.value === 'ALIPAY') {
      await handleAlipay(orderId, createResult.data)
    }

  } catch (error) {
    console.error('充值失败:', error)
    uni.showToast({
      title: error.message || '充值失败',
      icon: 'none'
    })
  } finally {
    recharging.value = false
  }
}

// 处理微信支付
const handleWechatPay = async (orderId, payData) => {
  try {
    // 调用uni-app的微信支付
    const payResult = await uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payData.timeStamp,
      nonceStr: payData.nonceStr,
      package: payData.package,
      signType: payData.signType,
      paySign: payData.paySign
    })

    // 支付成功，轮询查询结果
    uni.showLoading({ title: '确认支付结果...' })
    await pollPaymentResult(orderId)
    uni.hideLoading()

    // 充值成功
    uni.showToast({ title: '充值成功', icon: 'success' })
    uni.vibrateLong()

    // 刷新余额
    await loadAccountInfo(userStore.employeeId)

    // 跳转到充值结果页
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/consume/recharge-result?orderId=${orderId}&status=success&amount=${finalAmount.value}`
      })
    }, 1500)

  } catch (error) {
    // 支付取消或失败
    if (error.errMsg && error.errMsg.includes('cancel')) {
      uni.showToast({ title: '支付已取消', icon: 'none' })
    } else {
      throw new Error('微信支付失败')
    }
  }
}

// 处理支付宝支付
const handleAlipay = async (orderId, payData) => {
  try {
    // 调用uni-app的支付宝支付
    const payResult = await uni.requestPayment({
      provider: 'alipay',
      orderInfo: payData.orderInfo
    })

    // 支付成功，轮询查询结果
    uni.showLoading({ title: '确认支付结果...' })
    await pollPaymentResult(orderId)
    uni.hideLoading()

    // 充值成功
    uni.showToast({ title: '充值成功', icon: 'success' })
    uni.vibrateLong()

    // 刷新余额
    await loadAccountInfo(userStore.employeeId)

    // 跳转到充值结果页
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/consume/recharge-result?orderId=${orderId}&status=success&amount=${finalAmount.value}`
      })
    }, 1500)

  } catch (error) {
    // 支付取消或失败
    if (error.errMsg && error.errMsg.includes('cancel')) {
      uni.showToast({ title: '支付已取消', icon: 'none' })
    } else {
      throw new Error('支付宝支付失败')
    }
  }
}

// 轮询支付结果
const pollPaymentResult = async (orderId) => {
  const maxAttempts = 20 // 最多轮询20次（每次3秒，共60秒）
  let attempts = 0

  return new Promise((resolve, reject) => {
    pollingTimer.value = setInterval(async () => {
      attempts++

      try {
        const result = await rechargeApi.getPaymentResult(orderId)

        if (result.success && result.data) {
          const status = result.data.status

          // 支付成功
          if (status === 'SUCCESS' || status === 'success') {
            clearInterval(pollingTimer.value)
            resolve(result.data)
          }
          // 支付失败
          else if (status === 'FAILED' || status === 'failed') {
            clearInterval(pollingTimer.value)
            reject(new Error('支付失败'))
          }
          // 超时
          else if (attempts >= maxAttempts) {
            clearInterval(pollingTimer.value)
            reject(new Error('支付超时，请稍后查询订单状态'))
          }
        }
      } catch (error) {
        clearInterval(pollingTimer.value)
        reject(error)
      }
    }, 3000) // 每3秒查询一次
  })
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
  transition: all 0.2s ease;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  &.selected {
    border-color: #667eea;
    background: linear-gradient(135deg, #e6f0ff 0%, #f0e6ff 100%);
    box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.25);

    .amount-text {
      color: #667eea;
    }
  }

  &.pressed {
    transform: scale(0.95);
    box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.1);
  }

  .amount-text {
    font-size: 44rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    transition: color 0.2s ease;
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
    transition: all 0.2s ease;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, #e6f0ff 0%, #f0e6ff 100%);
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.25);

      .method-check {
        color: #667eea;
      }
    }

    &.pressed {
      transform: scale(0.98);
      box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.1);
    }

    .method-icon {
      font-size: 48rpx;
      margin-right: 24rpx;
      transition: transform 0.2s ease;
    }

    .method-name {
      flex: 1;
      font-size: 32rpx;
      color: rgba(0, 0, 0, 0.85);
      font-weight: 500;
    }

    .method-check {
      font-size: 32rpx;
      color: #667eea;
      font-weight: 600;
    }
  }
}

.confirm-section {
  padding: 48rpx 32rpx 32rpx;

  .confirm-btn {
    width: 100%;
    height: 120rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 60rpx;
    font-size: 40rpx;
    font-weight: 600;
    color: #fff;
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);
    transition: all 0.3s ease;

    &:active:not(.disabled) {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
    }

    &.disabled {
      background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
      box-shadow: none;
    }
  }
}
</style>

