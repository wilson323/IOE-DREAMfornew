<template>
  <div class="payment-container">
    <!-- 支付头部 -->
    <div class="payment-header">
      <van-nav-bar
        title="确认支付"
        left-arrow
        @click-left="goBack"
      />
    </div>

    <!-- 订单信息 -->
    <div class="order-info">
      <div class="merchant-info">
        <van-image
          :src="merchantInfo.logo"
          width="40"
          height="40"
          round
        />
        <div class="merchant-details">
          <div class="merchant-name">{{ merchantInfo.name }}</div>
          <div class="order-desc">{{ orderInfo.description }}</div>
        </div>
      </div>

      <div class="amount-section">
        <div class="amount-label">支付金额</div>
        <div class="amount-value">
          <span class="currency">¥</span>
          <span class="amount">{{ formatAmount(orderInfo.amount) }}</span>
        </div>
      </div>
    </div>

    <!-- 支付方式 -->
    <div class="payment-methods">
      <div class="section-title">选择支付方式</div>

      <van-radio-group v-model="selectedPayment">
        <!-- 微信支付 -->
        <van-cell-group>
          <van-cell
            title="微信支付"
            label="推荐使用微信支付"
            clickable
            @click="selectPayment('WECHAT')"
          >
            <template #icon>
              <div class="payment-icon wechat">
                <van-icon name="wechat" size="24" />
              </div>
            </template>
            <template #right-icon>
              <van-radio name="WECHAT" />
            </template>
          </van-cell>
        </van-cell-group>

        <!-- 支付宝 -->
        <van-cell-group>
          <van-cell
            title="支付宝"
            label="花呗、余额宝、银行卡支付"
            clickable
            @click="selectPayment('ALIPAY')"
          >
            <template #icon>
              <div class="payment-icon alipay">
                <van-icon name="alipay" size="24" />
              </div>
            </template>
            <template #right-icon>
              <van-radio name="ALIPAY" />
            </template>
          </van-cell>
        </van-cell-group>

        <!-- 余额支付 -->
        <van-cell-group>
          <van-cell
            title="余额支付"
            :label="`可用余额 ¥${formatAmount(userBalance)}`"
            clickable
            @click="selectPayment('BALANCE')"
            :disabled="userBalance < orderInfo.amount"
          >
            <template #icon>
              <div class="payment-icon balance">
                <van-icon name="balance-o" size="24" />
              </div>
            </template>
            <template #right-icon>
              <van-radio name="BALANCE" />
            </template>
            <template #extra>
              <span v-if="userBalance < orderInfo.amount" class="insufficient">
                余额不足
              </span>
            </template>
          </van-cell>
        </van-cell-group>

        <!-- 优惠券 -->
        <van-cell-group v-if="availableCoupons.length > 0">
          <van-cell
            title="优惠券"
            :label="selectedCoupon ? `已选择 ${selectedCoupon.name}` : '选择优惠券'"
            is-link
            @click="showCouponSelector = true"
          >
            <template #icon>
              <div class="payment-icon coupon">
                <van-icon name="coupon-o" size="24" />
              </div>
            </template>
            <template #extra>
              <span v-if="selectedCoupon" class="discount">
                -¥{{ formatAmount(selectedCoupon.discountAmount) }}
              </span>
            </template>
          </van-cell>
        </van-cell-group>
      </van-radio-group>
    </div>

    <!-- 支付按钮 -->
    <div class="payment-footer">
      <div class="total-amount">
        <span class="label">实付金额：</span>
        <span class="value">¥{{ formatAmount(finalAmount) }}</span>
      </div>
      <van-button
        type="primary"
        block
        size="large"
        :loading="paying"
        :disabled="!canPay"
        @click="confirmPayment"
      >
        {{ getPayButtonText() }}
      </van-button>
    </div>

    <!-- 优惠券选择器 -->
    <van-popup
      v-model:show="showCouponSelector"
      position="bottom"
      :style="{ height: '60%' }"
    >
      <div class="coupon-selector">
        <div class="selector-header">
          <span>选择优惠券</span>
          <van-button
            size="small"
            @click="clearCoupon"
            v-if="selectedCoupon"
          >
            不使用优惠券
          </van-button>
        </div>

        <div class="coupon-list">
          <van-radio-group v-model="tempSelectedCoupon">
            <van-cell
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              clickable
              @click="tempSelectedCoupon = coupon.id"
            >
              <template #title>
                <div class="coupon-info">
                  <div class="coupon-name">{{ coupon.name }}</div>
                  <div class="coupon-desc">{{ coupon.description }}</div>
                  <div class="coupon-expire">有效期至 {{ formatDate(coupon.expireTime) }}</div>
                </div>
              </template>
              <template #right-icon>
                <van-radio :name="coupon.id" />
              </template>
            </van-cell>
          </van-radio-group>
        </div>

        <div class="selector-footer">
          <van-button block type="primary" @click="confirmCouponSelection">
            确定
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- 支付结果弹窗 -->
    <van-dialog
      v-model:show="showPaymentResult"
      :title="paymentResult.success ? '支付成功' : '支付失败'"
      :show-cancel-button="false"
      :confirm-button-text="paymentResult.success ? '查看订单' : '重新支付'"
      @confirm="handlePaymentResultConfirm"
    >
      <div class="payment-result">
        <van-icon
          :name="paymentResult.success ? 'success' : 'fail'"
          size="48"
          :color="paymentResult.success ? '#07c160' : '#ee0a24'"
        />
        <div class="result-message">{{ paymentResult.message }}</div>
        <div class="result-amount" v-if="paymentResult.success">
          支付金额：¥{{ formatAmount(orderInfo.amount) }}
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showLoadingToast, closeToast, showDialog } from 'vant'
import { consumeApi } from '@/api/business/consume'
import { userApi } from '@/api/business/user'

const router = useRouter()
const route = useRoute()

// 响应式数据
const selectedPayment = ref('WECHAT')
const paying = ref(false)
const showCouponSelector = ref(false)
const tempSelectedCoupon = ref(null)
const selectedCoupon = ref(null)
const showPaymentResult = ref(false)

// 订单信息
const orderInfo = ref({
  id: '',
  amount: 0,
  description: '消费支付',
  paymentId: ''
})

// 商户信息
const merchantInfo = ref({
  name: '智慧消费平台',
  logo: '/images/logo.png'
})

// 用户信息
const userBalance = ref(0)
const availableCoupons = ref([])

// 支付结果
const paymentResult = ref({
  success: false,
  message: ''
})

// 计算属性
const finalAmount = computed(() => {
  let amount = orderInfo.value.amount || 0
  if (selectedCoupon.value) {
    amount -= selectedCoupon.value.discountAmount
  }
  return Math.max(0, amount)
})

const canPay = computed(() => {
  if (!selectedPayment.value) return false
  if (selectedPayment.value === 'BALANCE' && userBalance.value < finalAmount.value) {
    return false
  }
  return true
})

// 生命周期
onMounted(() => {
  const { paymentId, amount, description } = route.query

  if (paymentId) {
    orderInfo.value.paymentId = paymentId
    orderInfo.value.amount = parseFloat(amount) || 0
    orderInfo.value.description = description || '消费支付'
  } else {
    showToast('订单信息不完整')
    router.back()
  }

  loadUserInfo()
  loadAvailableCoupons()
})

// 方法
const goBack = () => {
  router.back()
}

const selectPayment = (method) => {
  selectedPayment.value = method
}

const formatAmount = (amount) => {
  return (amount || 0).toFixed(2)
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString()
}

const getPayButtonText = () => {
  if (paying.value) return '支付中...'

  switch (selectedPayment.value) {
    case 'WECHAT':
      return '微信支付'
    case 'ALIPAY':
      return '支付宝支付'
    case 'BALANCE':
      return '余额支付'
    default:
      return '确认支付'
  }
}

const loadUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()
    if (response.data) {
      userBalance.value = response.data.balance || 0
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

const loadAvailableCoupons = async () => {
  try {
    const response = await userApi.getAvailableCoupons({
      amount: orderInfo.value.amount
    })
    if (response.data) {
      availableCoupons.value = response.data
    }
  } catch (error) {
    console.error('加载优惠券失败', error)
  }
}

const clearCoupon = () => {
  selectedCoupon.value = null
  tempSelectedCoupon.value = null
  showCouponSelector.value = false
}

const confirmCouponSelection = () => {
  if (tempSelectedCoupon.value) {
    selectedCoupon.value = availableCoupons.value.find(
      coupon => coupon.id === tempSelectedCoupon.value
    )
  } else {
    selectedCoupon.value = null
  }
  showCouponSelector.value = false
}

const confirmPayment = async () => {
  if (!canPay.value) return

  paying.value = true
  showLoadingToast({ message: '支付中...', forbidClick: true })

  try {
    const paymentData = {
      paymentId: orderInfo.value.paymentId,
      paymentMethod: selectedPayment.value,
      amount: finalAmount.value,
      couponId: selectedCoupon.value?.id
    }

    let response

    if (selectedPayment.value === 'BALANCE') {
      // 余额支付
      response = await consumeApi.payWithBalance(paymentData)
    } else {
      // 第三方支付
      response = await consumeApi.createOnlinePayment(paymentData)
    }

    closeToast()

    if (response.success) {
      if (selectedPayment.value === 'BALANCE') {
        // 余额支付直接成功
        handlePaymentSuccess(response.data)
      } else {
        // 调用第三方支付
        await invokeThirdPartyPayment(response.data)
      }
    } else {
      handlePaymentError(response.message || '支付失败')
    }
  } catch (error) {
    closeToast()
    handlePaymentError('支付异常，请重试')
  } finally {
    paying.value = false
  }
}

const invokeThirdPartyPayment = async (paymentData) => {
  try {
    switch (selectedPayment.value) {
      case 'WECHAT':
        await invokeWechatPay(paymentData)
        break
      case 'ALIPAY':
        await invokeAlipay(paymentData)
        break
      default:
        throw new Error('不支持的支付方式')
    }
  } catch (error) {
    handlePaymentError('支付调用失败')
  }
}

const invokeWechatPay = async (paymentData) => {
  // 调用微信支付JSAPI
  if (window.wx && window.wx.miniProgram) {
    // 小程序环境
    const payParams = paymentData.payParams
    await window.wx.miniProgram.navigateTo({
      url: `/pages/payment/payment?${new URLSearchParams(payParams).toString()}`
    })
  } else {
    // H5环境
    const payForm = document.createElement('form')
    payForm.method = 'POST'
    payForm.action = paymentData.paymentUrl

    Object.keys(paymentData.formData).forEach(key => {
      const input = document.createElement('input')
      input.type = 'hidden'
      input.name = key
      input.value = paymentData.formData[key]
      payForm.appendChild(input)
    })

    document.body.appendChild(payForm)
    payForm.submit()
  }

  // 监听支付结果
  startPaymentResultPolling(paymentData.paymentId)
}

const invokeAlipay = async (paymentData) => {
  // 调用支付宝支付
  const payForm = document.createElement('form')
  payForm.method = 'POST'
  payForm.action = paymentData.gatewayUrl

  Object.keys(paymentData.formData).forEach(key => {
    const input = document.createElement('input')
    input.type = 'hidden'
    input.name = key
    input.value = paymentData.formData[key]
    payForm.appendChild(input)
  })

  document.body.appendChild(payForm)
  payForm.submit()

  // 监听支付结果
  startPaymentResultPolling(paymentData.paymentId)
}

const startPaymentResultPolling = (paymentId) => {
  const pollInterval = setInterval(async () => {
    try {
      const response = await consumeApi.getPaymentStatus(paymentId)

      if (response.success && response.data.status === 'SUCCESS') {
        clearInterval(pollInterval)
        handlePaymentSuccess(response.data)
      } else if (response.data.status === 'FAILED' || response.data.status === 'CLOSED') {
        clearInterval(pollInterval)
        handlePaymentError('支付失败或已取消')
      }
    } catch (error) {
      console.error('轮询支付状态失败', error)
    }
  }, 2000)

  // 30秒后停止轮询
  setTimeout(() => {
    clearInterval(pollInterval)
  }, 30000)
}

const handlePaymentSuccess = (data) => {
  paymentResult.value = {
    success: true,
    message: '支付成功',
    data
  }
  showPaymentResult.value = true
}

const handlePaymentError = (message) => {
  paymentResult.value = {
    success: false,
    message
  }
  showPaymentResult.value = true
}

const handlePaymentResultConfirm = () => {
  showPaymentResult.value = false

  if (paymentResult.value.success) {
    // 支付成功，跳转到订单详情
    router.replace({
      path: '/consume/detail',
      query: {
        orderId: paymentResult.value.data.orderId
      }
    })
  } else {
    // 支付失败，重新尝试
    // 可以选择重新支付或返回
  }
}
</script>

<style lang="less" scoped>
.payment-container {
  min-height: 100vh;
  background-color: #f7f8fa;
  display: flex;
  flex-direction: column;

  .payment-header {
    background: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .order-info {
    background: white;
    padding: 20px;
    margin: 12px;

    .merchant-info {
      display: flex;
      align-items: center;
      margin-bottom: 20px;

      .merchant-details {
        margin-left: 12px;

        .merchant-name {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .order-desc {
          font-size: 14px;
          color: #969799;
        }
      }
    }

    .amount-section {
      text-align: center;

      .amount-label {
        font-size: 14px;
        color: #969799;
        margin-bottom: 8px;
      }

      .amount-value {
        .currency {
          font-size: 24px;
          color: #323233;
        }

        .amount {
          font-size: 36px;
          font-weight: 600;
          color: #323233;
        }
      }
    }
  }

  .payment-methods {
    flex: 1;
    background: white;
    margin: 0 12px 12px;
    padding: 20px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .payment-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;

      &.wechat {
        background: #07c160;
        color: white;
      }

      &.alipay {
        background: #1677ff;
        color: white;
      }

      &.balance {
        background: #ff976a;
        color: white;
      }

      &.coupon {
        background: #ff6034;
        color: white;
      }
    }

    .insufficient {
      color: #ee0a24;
      font-size: 12px;
    }

    .discount {
      color: #07c160;
      font-weight: 600;
    }
  }

  .payment-footer {
    background: white;
    padding: 16px;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);

    .total-amount {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .label {
        font-size: 14px;
        color: #969799;
      }

      .value {
        font-size: 20px;
        font-weight: 600;
        color: #ee0a24;
      }
    }
  }

  .coupon-selector {
    display: flex;
    flex-direction: column;
    height: 100%;

    .selector-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid #ebedf0;

      span {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .coupon-list {
      flex: 1;
      overflow-y: auto;
      padding: 0 20px;

      .coupon-info {
        .coupon-name {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .coupon-desc {
          font-size: 14px;
          color: #969799;
          margin-bottom: 4px;
        }

        .coupon-expire {
          font-size: 12px;
          color: #c8c9cc;
        }
      }
    }

    .selector-footer {
      padding: 16px 20px;
      border-top: 1px solid #ebedf0;
    }
  }

  .payment-result {
    text-align: center;
    padding: 20px;

    .result-message {
      font-size: 16px;
      margin: 16px 0;
    }

    .result-amount {
      font-size: 14px;
      color: #969799;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .payment-container {
    .order-info {
      margin: 8px;
      padding: 16px;
    }

    .payment-methods {
      margin: 0 8px 8px;
      padding: 16px;
    }

    .payment-footer {
      padding: 12px;
    }
  }
}
</style>