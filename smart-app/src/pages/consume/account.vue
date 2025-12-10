<template>
  <view class="consume-account-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">我的账户</view>
      <view class="nav-right"></view>
    </view>

    <!-- 账户信息 -->
    <view class="account-info-card">
      <view class="account-balance">
        <text class="balance-label">账户余额</text>
        <view class="balance-amount">
          <text class="currency">¥</text>
          <text class="amount">{{ formatAmount(userInfo.balance) }}</text>
        </view>
      </view>
      <view class="account-details">
        <view class="detail-item">
          <text class="detail-label">账户状态</text>
          <text class="detail-value">{{ userInfo.status === 1 ? '正常' : '冻结' }}</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">账户类型</text>
          <text class="detail-value">{{ userInfo.accountType || '个人账户' }}</text>
        </view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-item" @click="recharge">
        <view class="action-icon">💰</view>
        <text class="action-text">充值</text>
      </view>
      <view class="action-item" @click="viewRecords">
        <view class="action-icon">📋</view>
        <text class="action-text">明细</text>
      </view>
      <view class="action-item" @click="viewStats">
        <view class="action-icon">📊</view>
        <text class="action-text">统计</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import consumeApi from '@/api/business/consume/consume-api.js'

// 响应式数据
const userStore = useUserStore()
const userInfo = reactive({
  balance: 0,
  status: 1,
  accountType: ''
})

// 页面生命周期
onMounted(() => {
  loadUserInfo()
})

onShow(() => {
  // 页面显示时可以刷新数据
})

onPullDownRefresh(() => {
  loadUserInfo()
  uni.stopPullDownRefresh()
})

// 方法实现
const loadUserInfo = async () => {
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
    const result = await consumeApi.getUserInfo(userId)
    if (result.success && result.data) {
      Object.assign(userInfo, result.data)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const recharge = () => {
  uni.navigateTo({ url: '/pages/consume/recharge' })
}

const viewRecords = () => {
  uni.navigateTo({ url: '/pages/consume/record' })
}

const viewStats = () => {
  uni.showToast({ title: '统计功能开发中', icon: 'none' })
}

const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.consume-account-page {
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

.account-info-card {
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 25px 20px;
  color: #fff;

  .account-balance {
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

  .account-details {
    display: flex;
    gap: 20px;

    .detail-item {
      flex: 1;

      .detail-label {
        display: block;
        font-size: 12px;
        opacity: 0.8;
        margin-bottom: 4px;
      }

      .detail-value {
        display: block;
        font-size: 14px;
        font-weight: 600;
      }
    }
  }
}

.quick-actions {
  padding: 0 15px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;

  .action-item {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px 10px;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .action-icon {
      font-size: 32px;
      margin-bottom: 8px;
    }

    .action-text {
      font-size: 13px;
      color: #666;
    }
  }
}
</style>

