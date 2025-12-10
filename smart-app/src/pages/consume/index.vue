<template>
  <view class="consume-index-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">智能消费</view>
      <view class="nav-right"></view>
    </view>

    <!-- 账户余额卡片 -->
    <view class="balance-card">
      <view class="balance-info">
        <text class="balance-label">账户余额</text>
        <view class="balance-amount">
          <text class="currency">¥</text>
          <text class="amount">{{ formatAmount(accountBalance) }}</text>
        </view>
      </view>
      <view class="balance-actions">
        <button class="action-btn recharge" @click="navigateTo('/pages/consume/account')">
          <text>充值</text>
        </button>
        <button class="action-btn detail" @click="navigateTo('/pages/consume/record')">
          <text>明细</text>
        </button>
      </view>
    </view>

    <!-- 快捷消费 -->
    <view class="quick-consume">
      <view class="section-title">快捷消费</view>
      <view class="consume-methods">
        <view class="method-item" @click="navigateTo('/pages/consume/qrcode')">
          <view class="method-icon">📱</view>
          <text class="method-text">扫码支付</text>
        </view>
        <view class="method-item" @click="navigateTo('/pages/consume/payment?type=nfc')">
          <view class="method-icon">💳</view>
          <text class="method-text">NFC支付</text>
        </view>
        <view class="method-item" @click="navigateTo('/pages/consume/payment?type=face')">
          <view class="method-icon">👤</view>
          <text class="method-text">人脸支付</text>
        </view>
        <view class="method-item" @click="navigateTo('/pages/consume/payment?type=quick')">
          <view class="method-icon">⚡</view>
          <text class="method-text">快速支付</text>
        </view>
      </view>
    </view>

    <!-- 最近交易 -->
    <view class="recent-transactions">
      <view class="section-header">
        <text class="section-title">最近交易</text>
        <text class="view-more" @click="navigateTo('/pages/consume/record')">查看全部</text>
      </view>
      <view class="transaction-list">
        <view
          class="transaction-item"
          v-for="(transaction, index) in recentTransactions"
          :key="index"
        >
          <view class="transaction-info">
            <text class="merchant-name">{{ transaction.merchantName }}</text>
            <text class="transaction-time">{{ formatDateTime(transaction.transactionTime) }}</text>
          </view>
          <view class="transaction-amount">
            <text class="amount-value">-¥{{ formatAmount(transaction.amount) }}</text>
          </view>
        </view>

        <view class="no-data" v-if="recentTransactions.length === 0">
          <text>暂无交易记录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import consumeApi from '@/api/business/consume/consume-api.js'

// 响应式数据
const userStore = useUserStore()
const accountBalance = ref(0)
const recentTransactions = ref([])

// 页面生命周期
onMounted(() => {
  loadData()
})

onShow(() => {
  // 页面显示时可以刷新数据
})

onPullDownRefresh(() => {
  loadData()
  uni.stopPullDownRefresh()
})

// 方法实现
const loadData = async () => {
  await Promise.all([
    loadAccountBalance(),
    loadRecentTransactions()
  ])
}

const loadAccountBalance = async () => {
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
    const result = await consumeApi.getAccountBalance(userId)
    if (result.success && result.data) {
      accountBalance.value = result.data.balance || 0
    }
  } catch (error) {
    console.error('加载账户余额失败:', error)
  }
}

const loadRecentTransactions = async () => {
  try {
    const result = await consumeApi.getRecentHistory({ limit: 5 })
    if (result.success && result.data) {
      recentTransactions.value = result.data
    }
  } catch (error) {
    console.error('加载最近交易失败:', error)
  }
}

const navigateTo = (url) => {
  uni.navigateTo({ url })
}

const goBack = () => {
  uni.navigateBack()
}

const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
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
</script>

<style lang="scss" scoped>
.consume-index-page {
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

.balance-card {
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 25px 20px;
  color: #fff;

  .balance-info {
    margin-bottom: 20px;

    .balance-label {
      font-size: 14px;
      opacity: 0.9;
    }

    .balance-amount {
      margin-top: 8px;

      .currency {
        font-size: 20px;
        margin-right: 4px;
      }

      .amount {
        font-size: 36px;
        font-weight: bold;
      }
    }
  }

  .balance-actions {
    display: flex;
    gap: 10px;

    .action-btn {
      flex: 1;
      height: 36px;
      border-radius: 18px;
      border: 1px solid rgba(255, 255, 255, 0.6);
      background-color: rgba(255, 255, 255, 0.2);
      color: #fff;
      font-size: 14px;
    }
  }
}

.quick-consume {
  padding: 0 15px 15px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }

  .consume-methods {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;

    .method-item {
      background-color: #fff;
      border-radius: 8px;
      padding: 15px 10px;
      display: flex;
      flex-direction: column;
      align-items: center;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      .method-icon {
        font-size: 28px;
        margin-bottom: 8px;
      }

      .method-text {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

.recent-transactions {
  padding: 0 15px 15px;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .view-more {
      font-size: 12px;
      color: #1890ff;
    }
  }

  .transaction-list {
    background-color: #fff;
    border-radius: 8px;
    overflow: hidden;

    .transaction-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .transaction-info {
        flex: 1;

        .merchant-name {
          display: block;
          font-size: 14px;
          color: #333;
          margin-bottom: 4px;
        }

        .transaction-time {
          display: block;
          font-size: 12px;
          color: #999;
        }
      }

      .transaction-amount {
        .amount-value {
          font-size: 16px;
          font-weight: 600;
          color: #ff4d4f;
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
}
</style>

