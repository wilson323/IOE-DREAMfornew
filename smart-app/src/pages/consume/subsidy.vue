<template>
  <view class="subsidy-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">我的补贴</view>
      <view class="nav-right"></view>
    </view>

    <!-- 补贴余额卡片 -->
    <view class="balance-card">
      <view class="total-balance">
        <text class="balance-label">补贴总额</text>
        <view class="balance-amount">
          <text class="currency">¥</text>
          <text class="amount">{{ formatAmount(balanceInfo.totalBalance || 0) }}</text>
        </view>
      </view>

      <view class="balance-breakdown">
        <view class="breakdown-item">
          <view class="item-icon">🍽️</view>
          <view class="item-info">
            <text class="item-label">餐补</text>
            <text class="item-value">¥{{ formatAmount(balanceInfo.mealSubsidyBalance || 0) }}</text>
          </view>
        </view>
        <view class="breakdown-item">
          <view class="item-icon">🚗</view>
          <view class="item-info">
            <text class="item-label">交通补</text>
            <text class="item-value">¥{{ formatAmount(balanceInfo.trafficSubsidyBalance || 0) }}</text>
          </view>
        </view>
        <view class="breakdown-item">
          <view class="item-icon">💰</view>
          <view class="item-info">
            <text class="item-label">其他补贴</text>
            <text class="item-value">¥{{ formatAmount(balanceInfo.generalSubsidyBalance || 0) }}</text>
          </view>
        </view>
      </view>

      <view class="monthly-stats">
        <view class="stat-item">
          <text class="stat-label">本月发放</text>
          <text class="stat-value grant">+¥{{ formatAmount(balanceInfo.monthlyGranted || 0) }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">本月使用</text>
          <text class="stat-value used">¥{{ formatAmount(balanceInfo.monthlyUsed || 0) }}</text>
        </view>
      </view>
    </view>

    <!-- 即将过期提醒 -->
    <view class="expiring-warning" v-if="expiringList.length > 0">
      <view class="warning-header">
        <text class="warning-icon">⚠️</text>
        <text class="warning-title">即将过期补贴</text>
      </view>
      <scroll-view class="expiring-scroll" scroll-x>
        <view
          class="expiring-item"
          v-for="(item, index) in expiringList"
          :key="index"
        >
          <text class="expiring-type">{{ getSubsidyTypeText(item.subsidyType) }}</text>
          <text class="expiring-amount">¥{{ formatAmount(item.remainingAmount) }}</text>
          <text class="expiring-days">{{ item.remainingDays }}天后过期</text>
        </view>
      </scroll-view>
    </view>

    <!-- 标签页切换 -->
    <view class="tab-container">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'records' }"
        @click="switchTab('records')"
      >
        <text class="tab-text">发放记录</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'usage' }"
        @click="switchTab('usage')"
      >
        <text class="tab-text">使用明细</text>
      </view>
    </view>

    <!-- 发放记录列表 -->
    <view class="record-list" v-if="activeTab === 'records'">
      <view
        class="record-item"
        v-for="(record, index) in recordsList"
        :key="index"
      >
        <view class="record-header">
          <text class="record-type">{{ getSubsidyTypeText(record.subsidyType) }}</text>
          <text class="record-amount grant">+¥{{ formatAmount(record.subsidyAmount) }}</text>
        </view>
        <view class="record-info">
          <text class="info-label">发放时间</text>
          <text class="info-value">{{ formatDate(record.grantTime) }}</text>
        </view>
        <view class="record-info">
          <text class="info-label">有效期</text>
          <text class="info-value">{{ formatDate(record.validFrom) }} 至 {{ formatDate(record.validUntil) }}</text>
        </view>
        <view class="progress-section">
          <view class="progress-info">
            <text class="progress-label">已使用 {{ formatAmount(record.usedAmount) }} / {{ formatAmount(record.subsidyAmount) }}</text>
            <text class="progress-percent">{{ record.usagePercentage }}%</text>
          </view>
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: record.usagePercentage + '%' }"
              :class="getProgressClass(record.usagePercentage)"
            ></view>
          </view>
        </view>
        <view class="record-status">
          <text class="status-badge" :class="getStatusClass(record)">
            {{ getStatusText(record) }}
          </text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMoreRecords && !loadingRecords" @click="loadMoreRecords">
        <text>加载更多</text>
      </view>

      <view class="no-data" v-if="recordsList.length === 0 && !loadingRecords">
        <text>暂无发放记录</text>
      </view>

      <view class="loading" v-if="loadingRecords">
        <text>加载中...</text>
      </view>
    </view>

    <!-- 使用明细列表 -->
    <view class="usage-list" v-if="activeTab === 'usage'">
      <view
        class="usage-item"
        v-for="(usage, index) in usageList"
        :key="index"
      >
        <view class="usage-header">
          <text class="usage-merchant">{{ usage.merchantName || usage.consumeLocation || '消费' }}</text>
          <text class="usage-amount">-¥{{ formatAmount(usage.consumeAmount) }}</text>
        </view>
        <view class="usage-info">
          <text class="info-label">消费时间</text>
          <text class="info-value">{{ formatDateTime(usage.transactionTime) }}</text>
        </view>
        <view class="usage-info">
          <text class="info-label">补贴类型</text>
          <text class="info-value">{{ getSubsidyTypeText(usage.subsidyType) }}</text>
        </view>
        <view class="usage-info" v-if="usage.remark">
          <text class="info-label">备注</text>
          <text class="info-value">{{ usage.remark }}</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMoreUsage && !loadingUsage" @click="loadMoreUsage">
        <text>加载更多</text>
      </view>

      <view class="no-data" v-if="usageList.length === 0 && !loadingUsage">
        <text>暂无使用记录</text>
      </view>

      <view class="loading" v-if="loadingUsage">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { subsidyApi } from '@/api/business/consume/subsidy-api.js'

// 响应式数据
const userStore = useUserStore()
const balanceInfo = reactive({
  totalBalance: 0,
  mealSubsidyBalance: 0,
  trafficSubsidyBalance: 0,
  generalSubsidyBalance: 0,
  monthlyGranted: 0,
  monthlyUsed: 0
})

const expiringList = ref([])
const activeTab = ref('records')
const recordsList = ref([])
const usageList = ref([])
const loadingRecords = ref(false)
const loadingUsage = ref(false)
const hasMoreRecords = ref(true)
const hasMoreUsage = ref(true)
const currentRecordsPage = ref(1)
const currentUsagePage = ref(1)
const pageSize = ref(20)

// 补贴类型映射
const getSubsidyTypeText = (type) => {
  const typeMap = {
    'MEAL': '餐补',
    'TRAFFIC': '交通补',
    'GENERAL': '其他补贴',
    'ANNUAL_BONUS': '年终补贴',
    'FESTIVAL': '节日补贴',
    'SPECIAL': '特殊补贴'
  }
  return typeMap[type] || type || '补贴'
}

// 进度条样式
const getProgressClass = (percentage) => {
  if (percentage >= 80) return 'warning'
  if (percentage >= 50) return 'normal'
  return 'good'
}

// 状态文本
const getStatusText = (record) => {
  if (record.deletedFlag === 1) return '已删除'
  if (record.expired) return '已过期'
  if (record.usagePercentage >= 100) return '已用完'
  if (record.expiringSoon) return '即将过期'
  return '使用中'
}

// 状态样式
const getStatusClass = (record) => {
  if (record.deletedFlag === 1) return 'deleted'
  if (record.expired) return 'expired'
  if (record.usagePercentage >= 100) return 'used-up'
  if (record.expiringSoon) return 'expiring'
  return 'active'
}

// 格式化金额
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化日期
const formatDate = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
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

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'records' && recordsList.value.length === 0) {
    loadRecords()
  } else if (tab === 'usage' && usageList.value.length === 0) {
    loadUsage()
  }
}

// 加载余额信息
const loadBalance = async () => {
  try {
    const userId = userStore.employeeId
    if (!userId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    const result = await subsidyApi.getSubsidyBalance(userId)
    if (result.success && result.data) {
      Object.assign(balanceInfo, result.data)
    }
  } catch (error) {
    console.error('加载补贴余额失败:', error)
  }
}

// 加载即将过期的补贴
const loadExpiring = async () => {
  try {
    const userId = userStore.employeeId
    if (!userId) return

    const result = await subsidyApi.getExpiringSubsidies(userId, 7)
    if (result.success && result.data) {
      expiringList.value = result.data
    }
  } catch (error) {
    console.error('加载即将过期补贴失败:', error)
  }
}

// 加载发放记录
const loadRecords = async (reset = false) => {
  if (reset) {
    currentRecordsPage.value = 1
    hasMoreRecords.value = true
    recordsList.value = []
  }

  loadingRecords.value = true
  try {
    const userId = userStore.employeeId
    if (!userId) return

    const params = {
      pageNum: currentRecordsPage.value,
      pageSize: pageSize.value
    }

    const result = await subsidyApi.getSubsidyRecords(userId, params)
    if (result.success && result.data) {
      const newRecords = result.data.list || []

      if (reset) {
        recordsList.value = newRecords
      } else {
        recordsList.value = [...recordsList.value, ...newRecords]
      }

      hasMoreRecords.value = newRecords.length === pageSize.value
    }
  } catch (error) {
    console.error('加载发放记录失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loadingRecords.value = false
  }
}

// 加载使用明细
const loadUsage = async (reset = false) => {
  if (reset) {
    currentUsagePage.value = 1
    hasMoreUsage.value = true
    usageList.value = []
  }

  loadingUsage.value = true
  try {
    const userId = userStore.employeeId
    if (!userId) return

    const params = {
      pageNum: currentUsagePage.value,
      pageSize: pageSize.value
    }

    const result = await subsidyApi.getSubsidyUsage(userId, params)
    if (result.success && result.data) {
      const newUsage = result.data.list || []

      if (reset) {
        usageList.value = newUsage
      } else {
        usageList.value = [...usageList.value, ...newUsage]
      }

      hasMoreUsage.value = newUsage.length === pageSize.value
    }
  } catch (error) {
    console.error('加载使用明细失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loadingUsage.value = false
  }
}

// 加载更多发放记录
const loadMoreRecords = () => {
  currentRecordsPage.value++
  loadRecords()
}

// 加载更多使用明细
const loadMoreUsage = () => {
  currentUsagePage.value++
  loadUsage()
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面生命周期
onMounted(() => {
  loadBalance()
  loadExpiring()
  loadRecords(true)
})

// 下拉刷新
onPullDownRefresh(() => {
  loadBalance()
  loadExpiring()
  if (activeTab.value === 'records') {
    loadRecords(true)
  } else {
    loadUsage(true)
  }
  uni.stopPullDownRefresh()
})

// 触底加载更多
onReachBottom(() => {
  if (activeTab.value === 'records') {
    if (hasMoreRecords.value && !loadingRecords.value) {
      loadMoreRecords()
    }
  } else {
    if (hasMoreUsage.value && !loadingUsage.value) {
      loadMoreUsage()
    }
  }
})
</script>

<style lang="scss" scoped>
.subsidy-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 600;
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
  border-radius: 16px;
  padding: 25px 20px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.35);

  .total-balance {
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

  .balance-breakdown {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;

    .breakdown-item {
      flex: 1;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 10px;
      padding: 12px;
      display: flex;
      align-items: center;
      gap: 8px;

      .item-icon {
        font-size: 24px;
      }

      .item-info {
        flex: 1;

        .item-label {
          display: block;
          font-size: 11px;
          opacity: 0.8;
          margin-bottom: 4px;
        }

        .item-value {
          display: block;
          font-size: 14px;
          font-weight: 600;
        }
      }
    }
  }

  .monthly-stats {
    display: flex;
    gap: 15px;
    padding-top: 15px;
    border-top: 1px solid rgba(255, 255, 255, 0.3);

    .stat-item {
      flex: 1;

      .stat-label {
        display: block;
        font-size: 12px;
        opacity: 0.8;
        margin-bottom: 4px;
      }

      .stat-value {
        display: block;
        font-size: 16px;
        font-weight: 600;

        &.grant {
          color: #a7ffc7;
        }

        &.used {
          color: #ffd6e7;
        }
      }
    }
  }
}

.expiring-warning {
  margin: 0 15px 15px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 4px 12px rgba(255, 154, 158, 0.3);

  .warning-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .warning-icon {
      font-size: 18px;
    }

    .warning-title {
      font-size: 15px;
      font-weight: 600;
      color: #d32f2f;
    }
  }

  .expiring-scroll {
    white-space: nowrap;

    .expiring-item {
      display: inline-block;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 10px;
      padding: 12px 16px;
      margin-right: 10px;
      text-align: center;
      min-width: 120px;

      .expiring-type {
        display: block;
        font-size: 13px;
        font-weight: 600;
        color: #666;
        margin-bottom: 4px;
      }

      .expiring-amount {
        display: block;
        font-size: 16px;
        font-weight: 700;
        color: #d32f2f;
        margin-bottom: 4px;
      }

      .expiring-days {
        display: block;
        font-size: 11px;
        color: #ff5722;
      }
    }
  }
}

.tab-container {
  display: flex;
  background-color: #fff;
  margin: 0 15px 15px;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 10px;
    border-radius: 10px;
    transition: all 0.3s ease;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

      .tab-text {
        color: #fff;
        font-weight: 600;
      }
    }

    .tab-text {
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }
  }
}

.record-list,
.usage-list {
  padding: 0 15px 15px;

  .record-item,
  .usage-item {
    background-color: #fff;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.record-item {
  .record-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;

    .record-type {
      font-size: 16px;
      font-weight: 600;
      color: #333;
    }

    .record-amount {
      font-size: 18px;
      font-weight: 700;
      color: #52c41a;
    }
  }

  .record-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;

    .info-label {
      font-size: 13px;
      color: #999;
    }

    .info-value {
      font-size: 13px;
      color: #666;
      font-weight: 500;
    }
  }

  .progress-section {
    margin: 12px 0;

    .progress-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;

      .progress-label {
        font-size: 12px;
        color: #666;
      }

      .progress-percent {
        font-size: 12px;
        font-weight: 600;
        color: #666;
      }
    }

    .progress-bar {
      height: 8px;
      background-color: #f0f0f0;
      border-radius: 4px;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        border-radius: 4px;
        transition: width 0.3s ease;

        &.good {
          background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
        }

        &.normal {
          background: linear-gradient(90deg, #faad14 0%, #ffc53d 100%);
        }

        &.warning {
          background: linear-gradient(90deg, #ff4d4f 0%, #ff7875 100%);
        }
      }
    }
  }

  .record-status {
    margin-top: 12px;

    .status-badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 11px;
      font-weight: 500;

      &.active {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.expiring {
        background-color: #fff7e6;
        color: #fa8c16;
      }

      &.used-up {
        background-color: #f0f0f0;
        color: #999;
      }

      &.expired {
        background-color: #fff2f0;
        color: #ff4d4f;
      }

      &.deleted {
        background-color: #f0f0f0;
        color: #999;
      }
    }
  }
}

.usage-item {
  .usage-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .usage-merchant {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      flex: 1;
    }

    .usage-amount {
      font-size: 18px;
      font-weight: 700;
      color: #ff4d4f;
    }
  }

  .usage-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;

    .info-label {
      font-size: 13px;
      color: #999;
    }

    .info-value {
      font-size: 13px;
      color: #666;
      font-weight: 500;
      max-width: 60%;
      text-align: right;
    }
  }
}

.load-more {
  text-align: center;
  padding: 15px;
  color: #1890ff;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.no-data,
.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}
</style>
