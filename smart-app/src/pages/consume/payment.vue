<template>
  <view class="quick-consume-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 简化导航栏 -->
    <view class="nav-bar">
      <text class="back-btn" @click="goBack">✕</text>
      <text class="nav-title">快速消费</text>
      <text class="history-btn" @click="goToHistory">记录</text>
    </view>

    <!-- 用户信息卡片（醒目显示余额） -->
    <view class="user-card">
      <view class="user-avatar">
        <image v-if="userInfo.avatar" :src="userInfo.avatar" mode="aspectFill" />
        <text v-else class="avatar-placeholder">{{ userInfo.name ? userInfo.name[0] : 'U' }}</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userInfo.name || '未登录' }}</text>
        <view class="user-balance">
          <text class="balance-label">余额</text>
          <text class="balance-amount">¥{{ formatAmount(userInfo.balance) }}</text>
        </view>
      </view>
      <view class="reload-btn" @click="refreshBalance">
        <text :class="['reload-icon', { rotating: refreshing }]">🔄</text>
      </view>
    </view>

    <!-- 金额显示区域 -->
    <view class="amount-display">
      <text class="currency-symbol">¥</text>
      <text class="amount-value">{{ displayAmount }}</text>
    </view>

    <!-- 快捷金额按钮（大按钮设计） -->
    <view class="quick-amount-section">
      <text class="section-title">快捷金额</text>
      <view class="amount-grid">
        <button
          v-for="amount in quickAmounts"
          :key="amount"
          :class="['amount-btn', { selected: selectedAmount === amount }]"
          @click="selectAmount(amount)"
        >
          <text class="amount-text">¥{{ amount }}</text>
        </button>
      </view>
    </view>

    <!-- 自定义金额输入 -->
    <view class="custom-amount-section">
      <text class="section-title">自定义金额</text>
      <view class="custom-input-box">
        <text class="input-prefix">¥</text>
        <input
          class="custom-input"
          type="digit"
          v-model="customAmount"
          placeholder="输入金额"
          @input="onCustomAmountInput"
          @confirm="confirmCustomAmount"
        />
        <button v-if="customAmount" class="clear-btn" @click="clearCustomAmount">
          <text>✕</text>
        </button>
      </view>
    </view>

    <!-- 确认消费按钮（超大按钮） -->
    <view class="confirm-section">
      <button
        :class="['confirm-btn', { disabled: !canConsume || consuming }]"
        :disabled="!canConsume || consuming"
        @click="confirmConsume"
      >
        <text class="confirm-text">{{ consuming ? '支付中...' : `确认消费 ¥${displayAmount}` }}</text>
      </button>

      <!-- 余额不足提示 -->
      <text v-if="selectedAmount > 0 && userInfo.balance < selectedAmount" class="insufficient-tip">
        余额不足，请先充值
      </text>
    </view>

    <!-- 最近消费记录（快速查看） -->
    <view class="recent-records-section">
      <view class="section-header">
        <text class="section-title">最近消费</text>
        <text class="view-more" @click="goToHistory">查看更多 ›</text>
      </view>
      <scroll-view class="records-scroll" scroll-y>
        <view
          v-for="record in recentRecords"
          :key="record.id"
          class="record-item"
        >
          <view class="record-info">
            <text class="record-title">{{ record.areaName || '消费' }}</text>
            <text class="record-time">{{ formatTime(record.createTime) }}</text>
          </view>
          <text class="record-amount">-¥{{ formatAmount(record.amount) }}</text>
        </view>

        <view v-if="recentRecords.length === 0" class="records-empty">
          <text>暂无消费记录</text>
        </view>
      </scroll-view>
    </view>

    <!-- 消费成功动画 -->
    <view v-if="showSuccessAnimation" class="success-animation">
      <view class="success-content">
        <text class="success-icon">✅</text>
        <text class="success-text">消费成功</text>
        <text class="success-amount">¥{{ formatAmount(lastConsumeAmount) }}</text>
        <text class="success-balance">余额: ¥{{ formatAmount(userInfo.balance) }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import consumeApi from '@/api/business/consume/consume-api.js'
import { useUserStore } from '@/store/modules/system/user'

export default {
  name: 'QuickConsumePayment',

  setup() {
    // 系统信息
    const systemInfo = uni.getSystemInfoSync()
    const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

    // 用户store
    const userStore = useUserStore()

    // 页面状态
    const consuming = ref(false)
    const refreshing = ref(false)
    const showSuccessAnimation = ref(false)

    // 用户信息
    const userInfo = reactive({
      userId: null,
      name: '',
      avatar: '',
      balance: 0
    })

    // 快捷金额
    const quickAmounts = [5, 10, 15, 20, 30, 50]

    // 选中的金额
    const selectedAmount = ref(0)

    // 自定义金额
    const customAmount = ref('')

    // 最后消费金额
    const lastConsumeAmount = ref(0)

    // 最近消费记录
    const recentRecords = ref([])

    // 计算显示金额
    const displayAmount = computed(() => {
      if (customAmount.value) {
        return formatAmount(customAmount.value)
      }
      if (selectedAmount.value > 0) {
        return formatAmount(selectedAmount.value)
      }
      return '0.00'
    })

    // 是否可以消费
    const canConsume = computed(() => {
      const amount = customAmount.value || selectedAmount.value
      return amount > 0 && amount <= userInfo.balance
    })

    // 页面生命周期
    onMounted(() => {
      init()
    })

    // 初始化
    const init = async () => {
      // 从用户store获取用户ID
      const userId = userStore.employeeId
      if (!userId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }
      userInfo.userId = userId

      await Promise.all([
        loadUserInfo(userId),
        loadRecentRecords(userId)
      ])
    }

    // 加载用户信息
    const loadUserInfo = async (userId) => {
      try {
        const res = await consumeApi.getUserConsumeInfo(userId)
        if (res.code === 1 && res.data) {
          userInfo.name = res.data.userName || res.data.name
          userInfo.avatar = res.data.avatar
          userInfo.balance = res.data.balance || 0
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)

        // 尝试从本地缓存加载
        const cachedInfo = uni.getStorageSync('USER_CONSUME_INFO')
        if (cachedInfo) {
          Object.assign(userInfo, cachedInfo)
        }
      }
    }

    // 加载最近消费记录
    const loadRecentRecords = async (userId) => {
      try {
        const res = await consumeApi.getRecentHistory({
          userId,
          pageNum: 1,
          pageSize: 5
        })
        if (res.code === 1 && res.data) {
          recentRecords.value = res.data.list || res.data || []
        }
      } catch (error) {
        console.error('加载消费记录失败:', error)
      }
    }

    // 选择金额
    const selectAmount = (amount) => {
      if (selectedAmount.value === amount) {
        selectedAmount.value = 0  // 取消选择
      } else {
        selectedAmount.value = amount
        customAmount.value = ''  // 清除自定义金额
      }

      // 震动反馈
      uni.vibrateShort({ type: 'light' })
    }

    // 自定义金额输入
    const onCustomAmountInput = (e) => {
      selectedAmount.value = 0  // 清除快捷金额选择
    }

    // 确认自定义金额
    const confirmCustomAmount = () => {
      if (customAmount.value) {
        selectedAmount.value = 0
        uni.vibrateShort({ type: 'light' })
      }
    }

    // 清除自定义金额
    const clearCustomAmount = () => {
      customAmount.value = ''
      uni.vibrateShort({ type: 'light' })
    }

    // 刷新余额
    const refreshBalance = async () => {
      refreshing.value = true
      await loadUserInfo(userInfo.userId)
      refreshing.value = false
      uni.showToast({ title: '余额已刷新', icon: 'none', duration: 1000 })
    }

    // 确认消费
    const confirmConsume = async () => {
      if (!canConsume.value) {
        if (userInfo.balance < (customAmount.value || selectedAmount.value)) {
          uni.showToast({ title: '余额不足', icon: 'none' })
        } else {
          uni.showToast({ title: '请选择金额', icon: 'none' })
        }
        return
      }

      // 二次确认
      const amount = Number(customAmount.value || selectedAmount.value)
      const confirmed = await showConfirmDialog(`确认消费 ¥${formatAmount(amount)}？`)
      if (!confirmed) {
        return
      }

      consuming.value = true

      try {
        const res = await consumeApi.quickConsume({
          userId: userInfo.userId,
          amount,
          deviceId: 'MOBILE_POS_001',
          paymentMethod: 'balance'
        })

        if (res.code === 1) {
          lastConsumeAmount.value = amount

          // 更新余额（乐观更新）
          userInfo.balance -= amount

          // 缓存用户信息
          uni.setStorageSync('USER_CONSUME_INFO', userInfo)

          // 显示成功动画
          showSuccessAnimation.value = true

          // 震动反馈（长震动）
          uni.vibrateLong()

          // 播放成功音效（可选）
          // playSuccessSound()

          // 3秒后关闭动画并返回
          setTimeout(() => {
            showSuccessAnimation.value = false

            // 重置选择
            selectedAmount.value = 0
            customAmount.value = ''

            // 刷新数据
            loadUserInfo(userInfo.userId)
            loadRecentRecords(userInfo.userId)
          }, 2000)
        } else {
          uni.showToast({
            title: res.message || '消费失败',
            icon: 'none',
            duration: 2000
          })
        }
      } catch (error) {
        console.error('消费失败:', error)

        // 检查是否是网络错误
        if (!error.response) {
          // 网络错误，尝试离线消费
          const offlineSaved = await saveOfflineConsume(amount)
          if (offlineSaved) {
            uni.showToast({
              title: '网络不佳，已保存离线记录',
              icon: 'none',
              duration: 2000
            })
          } else {
            uni.showToast({ title: '消费失败', icon: 'none' })
          }
        } else {
          uni.showToast({
            title: error.message || '消费失败',
            icon: 'none'
          })
        }
      } finally {
        consuming.value = false
      }
    }

    // 保存离线消费记录
    const saveOfflineConsume = async (amount) => {
      try {
        const offlineQueue = uni.getStorageSync('OFFLINE_CONSUME_QUEUE') || []
        offlineQueue.push({
          userId: userInfo.userId,
          amount,
          deviceId: 'MOBILE_POS_001',
          timestamp: Date.now(),
          status: 'pending'
        })
        uni.setStorageSync('OFFLINE_CONSUME_QUEUE', offlineQueue)
        return true
      } catch (error) {
        console.error('保存离线记录失败:', error)
        return false
      }
    }

    // 显示确认对话框
    const showConfirmDialog = (content) => {
      return new Promise((resolve) => {
        uni.showModal({
          title: '确认消费',
          content,
          success: (res) => {
            resolve(res.confirm)
          }
        })
      })
    }

    // 跳转到历史记录
    const goToHistory = () => {
      uni.navigateTo({ url: '/pages/consume/record' })
    }

    // 返回
    const goBack = () => {
      uni.navigateBack()
    }

    // 格式化金额
    const formatAmount = (amount) => {
      if (!amount && amount !== 0) return '0.00'
      return Number(amount).toFixed(2)
    }

    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return `${Math.floor(diff / 60000)}分钟前`
      } else if (diff < 86400000) {
        return `${Math.floor(diff / 3600000)}小时前`
      } else {
        const month = date.getMonth() + 1
        const day = date.getDate()
        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${month}-${day} ${hours}:${minutes}`
      }
    }

    return {
      statusBarHeight,
      consuming,
      refreshing,
      showSuccessAnimation,
      userInfo,
      quickAmounts,
      selectedAmount,
      customAmount,
      lastConsumeAmount,
      recentRecords,
      displayAmount,
      canConsume,
      selectAmount,
      onCustomAmountInput,
      confirmCustomAmount,
      clearCustomAmount,
      refreshBalance,
      confirmConsume,
      goToHistory,
      goBack,
      formatAmount,
      formatTime
    }
  }
}
</script>

<style lang="scss" scoped>
.quick-consume-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1890ff 0%, #096dd9 40%, #f5f5f5 40%);
}

.status-bar {
  background: transparent;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;

  .back-btn,
  .history-btn {
    font-size: 32rpx;
    color: #fff;
    font-weight: 600;
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
  }
}

.user-card {
  display: flex;
  align-items: center;
  margin: 0 32rpx 48rpx;
  padding: 32rpx;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 24rpx;

  .user-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    overflow: hidden;
    margin-right: 24rpx;
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;

    image {
      width: 100%;
      height: 100%;
    }

    .avatar-placeholder {
      font-size: 48rpx;
      font-weight: 600;
      color: #1890ff;
    }
  }

  .user-info {
    flex: 1;

    .user-name {
      display: block;
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
      margin-bottom: 12rpx;
    }

    .user-balance {
      display: flex;
      align-items: baseline;
      gap: 12rpx;

      .balance-label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.85);
      }

      .balance-amount {
        font-size: 40rpx;
        font-weight: 600;
        color: #fff;
      }
    }
  }

  .reload-btn {
    .reload-icon {
      font-size: 48rpx;
      color: #fff;

      &.rotating {
        animation: rotate 1s linear infinite;
      }
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.amount-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 48rpx;

  .currency-symbol {
    font-size: 48rpx;
    font-weight: 600;
    color: #fff;
    margin-right: 16rpx;
  }

  .amount-value {
    font-size: 96rpx;
    font-weight: 600;
    color: #fff;
  }
}

.quick-amount-section {
  margin: 0 32rpx 32rpx;

  .section-title {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    margin-bottom: 24rpx;
  }

  .amount-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24rpx;
  }

  .amount-btn {
    height: 160rpx;
    background: #fff;
    border: 3rpx solid transparent;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    transition: all 0.2s;

    &.selected {
      border-color: #1890ff;
      background: #e6f7ff;
      transform: scale(1.05);

      .amount-text {
        color: #1890ff;
      }
    }

    &:active {
      transform: scale(0.95);
    }

    .amount-text {
      font-size: 48rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }
  }
}

.custom-amount-section {
  margin: 0 32rpx 48rpx;

  .section-title {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    margin-bottom: 24rpx;
  }

  .custom-input-box {
    display: flex;
    align-items: center;
    height: 120rpx;
    padding: 0 32rpx;
    background: #fff;
    border-radius: 20rpx;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);

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

    .clear-btn {
      width: 56rpx;
      height: 56rpx;
      background: #f0f0f0;
      border: none;
      border-radius: 28rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0;
      font-size: 32rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }
}

.confirm-section {
  padding: 0 32rpx 24rpx;

  .confirm-btn {
    width: 100%;
    height: 120rpx;
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    border: none;
    border-radius: 60rpx;
    box-shadow: 0 8rpx 24rpx rgba(82, 196, 26, 0.4);

    &:active:not(.disabled) {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
    }

    &.disabled {
      background: #d9d9d9;
      box-shadow: none;
    }

    .confirm-text {
      font-size: 40rpx;
      font-weight: 600;
      color: #fff;
    }
  }

  .insufficient-tip {
    display: block;
    text-align: center;
    font-size: 26rpx;
    color: #f5222d;
    margin-top: 16rpx;
  }
}

.recent-records-section {
  margin: 0 32rpx 32rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;
  }

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }

  .view-more {
    font-size: 26rpx;
    color: #1890ff;
  }
}

.records-scroll {
  max-height: 400rpx;

  .record-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .record-info {
      flex: 1;

      .record-title {
        display: block;
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        margin-bottom: 8rpx;
      }

      .record-time {
        display: block;
        font-size: 24rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }

    .record-amount {
      font-size: 32rpx;
      font-weight: 600;
      color: #f5222d;
    }
  }

  .records-empty {
    text-align: center;
    padding: 48rpx 0;
    font-size: 28rpx;
    color: rgba(0, 0, 0, 0.25);
  }
}

.success-animation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-in;

  .success-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 80rpx 120rpx;
    background: #fff;
    border-radius: 32rpx;
    animation: zoomIn 0.3s ease-out;

    .success-icon {
      font-size: 120rpx;
      margin-bottom: 24rpx;
      animation: bounceIn 0.5s ease-out;
    }

    .success-text {
      font-size: 36rpx;
      font-weight: 600;
      color: #52c41a;
      margin-bottom: 16rpx;
    }

    .success-amount {
      font-size: 56rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      margin-bottom: 16rpx;
    }

    .success-balance {
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes zoomIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

@keyframes bounceIn {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}
</style>
