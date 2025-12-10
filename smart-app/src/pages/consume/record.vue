<template>
  <view class="consume-record-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">消费记录</view>
      <view class="nav-right">
        <text class="filter-btn" @click="showFilter = true">筛选</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="stats-card">
        <text class="stats-label">本月消费</text>
        <text class="stats-value">¥{{ formatAmount(monthlyTotal) }}</text>
      </view>
      <view class="stats-card">
        <text class="stats-label">本月笔数</text>
        <text class="stats-value">{{ monthlyCount }}</text>
      </view>
    </view>

    <!-- 筛选条件 -->
    <view class="filter-section" v-if="filterOptions.dateRange || filterOptions.consumeType">
      <view class="filter-tags">
        <text class="filter-tag" v-if="filterOptions.dateRange">
          {{ filterOptions.dateRange }}
          <text class="close-tag" @click="clearDateRange">×</text>
        </text>
        <text class="filter-tag" v-if="filterOptions.consumeType">
          {{ getConsumeTypeText(filterOptions.consumeType) }}
          <text class="close-tag" @click="clearConsumeType">×</text>
        </text>
        <text class="clear-all" @click="clearAllFilters">清除筛选</text>
      </view>
    </view>

    <!-- 记录列表 -->
    <view class="record-list">
      <view
        class="record-item"
        v-for="(record, index) in recordList"
        :key="index"
        @click="viewDetail(record)"
      >
        <view class="item-left">
          <view class="merchant-info">
            <text class="merchant-name">{{ record.merchantName || record.consumeLocation || '消费' }}</text>
            <text class="consume-type" :class="getConsumeTypeClass(record.consumeType)">
              {{ getConsumeTypeText(record.consumeType) }}
            </text>
          </view>
          <view class="transaction-info">
            <text class="transaction-time">{{ formatDateTime(record.transactionTime) }}</text>
            <text class="payment-method" v-if="record.paymentMethod">
              {{ getPaymentMethodText(record.paymentMethod) }}
            </text>
          </view>
        </view>
        <view class="item-right">
          <text class="amount-value" :class="{ refund: record.amount > 0 }">
            {{ record.amount > 0 ? '+' : '-' }}¥{{ formatAmount(Math.abs(record.amount)) }}
          </text>
          <text class="transaction-status" :class="getStatusClass(record.status)">
            {{ getStatusText(record.status) }}
          </text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore && !loading" @click="loadMore">
        <text>加载更多</text>
      </view>

      <view class="no-data" v-if="recordList.length === 0 && !loading">
        <text>暂无消费记录</text>
      </view>

      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
    </view>

    <!-- 筛选弹窗 -->
    <view class="filter-modal" v-if="showFilter" @click="showFilter = false">
      <view class="filter-content" @click.stop>
        <view class="filter-header">
          <text class="filter-title">筛选条件</text>
          <text class="close-btn" @click="showFilter = false">×</text>
        </view>

        <!-- 日期范围 -->
        <view class="filter-group">
          <text class="filter-label">日期范围</text>
          <view class="date-options">
            <text
              class="date-option"
              :class="{ active: filterOptions.dateRange === option.value }"
              v-for="option in dateRangeOptions"
              :key="option.value"
              @click="selectDateRange(option.value)"
            >
              {{ option.label }}
            </text>
          </view>
          <view class="custom-date" v-if="filterOptions.dateRange === 'custom'">
            <picker mode="date" @change="onStartDateChange">
              <view class="date-picker">{{ startDate || '开始日期' }}</view>
            </picker>
            <text>至</text>
            <picker mode="date" @change="onEndDateChange">
              <view class="date-picker">{{ endDate || '结束日期' }}</view>
            </picker>
          </view>
        </view>

        <!-- 消费类型 -->
        <view class="filter-group">
          <text class="filter-label">消费类型</text>
          <view class="type-options">
            <text
              class="type-option"
              :class="{ active: filterOptions.consumeType === option.value }"
              v-for="option in consumeTypeOptions"
              :key="option.value"
              @click="selectConsumeType(option.value)"
            >
              {{ option.label }}
            </text>
          </view>
        </view>

        <!-- 按钮组 -->
        <view class="filter-actions">
          <button class="reset-btn" @click="resetFilters">重置</button>
          <button class="confirm-btn" @click="applyFilters">确认</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import consumeApi from '@/api/business/consume/consume-api.js'

// 响应式数据
const recordList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)
const showFilter = ref(false)

// 统计数据
const monthlyTotal = ref(0)
const monthlyCount = ref(0)

// 筛选选项
const filterOptions = reactive({
  dateRange: null,
  consumeType: null,
})

const tempFilterOptions = reactive({
  dateRange: null,
  consumeType: null,
})

const startDate = ref('')
const endDate = ref('')

// 日期范围选项
const dateRangeOptions = [
  { label: '今天', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '自定义', value: 'custom' }
]

// 消费类型选项
const consumeTypeOptions = [
  { label: '全部', value: null },
  { label: '早餐', value: 'BREAKFAST' },
  { label: '午餐', value: 'LUNCH' },
  { label: '晚餐', value: 'DINNER' },
  { label: '购物', value: 'SHOPPING' },
  { label: '充值', value: 'RECHARGE' },
  { label: '退款', value: 'REFUND' }
]

// 页面生命周期
onMounted(() => {
  loadRecords(true)
  loadStats()
})

onShow(() => {
  // 可以在这里添加页面显示时的逻辑
})

onPullDownRefresh(() => {
  loadRecords(true)
  loadStats()
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadMore()
  }
})

// 方法实现
const loadRecords = async (reset = false) => {
  if (reset) {
    currentPage.value = 1
    hasMore.value = true
    recordList.value = []
  }

  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...filterOptions
    }

    const result = await consumeApi.getTransactionHistory(params)
    if (result.success && result.data) {
      if (reset) {
        recordList.value = result.data.records || []
      } else {
        recordList.value = [...recordList.value, ...(result.data.records || [])]
      }

      hasMore.value = result.data.records?.length === pageSize.value
    }
  } catch (error) {
    console.error('加载记录失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const result = await consumeApi.getMonthlyStats()
    if (result.success && result.data) {
      monthlyTotal.value = result.data.totalAmount || 0
      monthlyCount.value = result.data.totalCount || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadMore = () => {
  currentPage.value++
  loadRecords()
}

const viewDetail = (record) => {
  const recordStr = encodeURIComponent(JSON.stringify(record))
  uni.navigateTo({
    url: `/pages/consume/transaction-detail?id=${record.id}&data=${recordStr}`
  })
}

const selectDateRange = (value) => {
  tempFilterOptions.dateRange = value
  if (value !== 'custom') {
    startDate.value = ''
    endDate.value = ''
  }
}

const selectConsumeType = (value) => {
  tempFilterOptions.consumeType = value
}

const onStartDateChange = (e) => {
  startDate.value = e.detail.value
}

const onEndDateChange = (e) => {
  endDate.value = e.detail.value
}

const applyFilters = () => {
  Object.assign(filterOptions, tempFilterOptions)
  showFilter.value = false
  loadRecords(true)
}

const resetFilters = () => {
  Object.assign(tempFilterOptions, {
    dateRange: null,
    consumeType: null,
  })
  startDate.value = ''
  endDate.value = ''
}

const clearDateRange = () => {
  filterOptions.dateRange = null
  loadRecords(true)
}

const clearConsumeType = () => {
  filterOptions.consumeType = null
  loadRecords(true)
}

const clearAllFilters = () => {
  Object.assign(filterOptions, {
    dateRange: null,
    consumeType: null,
  })
  loadRecords(true)
}

const getConsumeTypeText = (type) => {
  const option = consumeTypeOptions.find(opt => opt.value === type)
  return option ? option.label : type
}

const getConsumeTypeClass = (type) => {
  const classMap = {
    'BREAKFAST': 'breakfast',
    'LUNCH': 'lunch',
    'DINNER': 'dinner',
    'SHOPPING': 'shopping',
    'RECHARGE': 'recharge',
    'REFUND': 'refund'
  }
  return classMap[type] || 'default'
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'BALANCE': '余额支付',
    'CARD': '刷卡支付',
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'NFC': 'NFC支付',
    'FACE': '人脸支付'
  }
  return methodMap[method] || method
}

const getStatusText = (status) => {
  const statusMap = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'PENDING': '处理中',
    'REFUNDED': '已退款'
  }
  return statusMap[status] || status
}

const getStatusClass = (status) => {
  const classMap = {
    'SUCCESS': 'success',
    'FAILED': 'failed',
    'PENDING': 'pending',
    'REFUNDED': 'refunded'
  }
  return classMap[status] || 'default'
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

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.consume-record-page {
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

  .filter-btn {
    font-size: 14px;
    color: #1890ff;
    font-weight: 500;
  }
}

.stats-cards {
  display: flex;
  gap: 12px;
  padding: 15px;

  .stats-card {
    flex: 1;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 20px;
    color: #fff;
    text-align: center;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
    }

    .stats-label {
      font-size: 14px;
      opacity: 0.9;
      margin-bottom: 8px;
    }

    .stats-value {
      font-size: 24px;
      font-weight: 700;
    }
  }
}

.filter-section {
  padding: 0 15px 10px;

  .filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .filter-tag {
      display: inline-flex;
      align-items: center;
      background-color: #fff;
      color: #1890ff;
      border: 1px solid #1890ff;
      border-radius: 16px;
      padding: 6px 12px;
      font-size: 12px;

      .close-tag {
        margin-left: 6px;
        color: #ff4d4f;
        font-weight: bold;
      }
    }

    .clear-all {
      color: #666;
      font-size: 12px;
      padding: 6px 12px;
    }
  }
}

.record-list {
  padding: 0 15px 15px;

  .record-item {
    background-color: #fff;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 12px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
    border-left: 4px solid #1890ff;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }

    &.refund {
      border-left-color: #52c41a;
    }

    .item-left {
      flex: 1;
      padding-right: 15px;

      .merchant-info {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;

        .merchant-name {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          flex: 1;
        }

        .consume-type {
          font-size: 11px;
          padding: 2px 8px;
          border-radius: 10px;
          font-weight: 500;

          &.breakfast {
            background-color: #fff7e6;
            color: #fa8c16;
          }

          &.lunch {
            background-color: #f6ffed;
            color: #52c41a;
          }

          &.dinner {
            background-color: #f0f5ff;
            color: #1890ff;
          }

          &.shopping {
            background-color: #fff2e8;
            color: #fa541c;
          }

          &.recharge {
            background-color: #e6f7ff;
            color: #13c2c2;
          }

          &.refund {
            background-color: #f6ffed;
            color: #52c41a;
          }
        }
      }

      .transaction-info {
        .transaction-time {
          font-size: 13px;
          color: #666;
          margin-bottom: 4px;
        }

        .payment-method {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .item-right {
      text-align: right;

      .amount-value {
        font-size: 18px;
        font-weight: 700;
        color: #ff4d4f;
        margin-bottom: 4px;
        display: block;

        &.refund {
          color: #52c41a;
        }
      }

      .transaction-status {
        font-size: 11px;
        padding: 2px 6px;
        border-radius: 8px;
        font-weight: 500;

        &.success {
          background-color: #f6ffed;
          color: #52c41a;
        }

        &.failed {
          background-color: #fff2f0;
          color: #ff4d4f;
        }

        &.pending {
          background-color: #fffbe6;
          color: #faad14;
        }

        &.refunded {
          background-color: #f6ffed;
          color: #52c41a;
        }
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

  .no-data, .loading {
    text-align: center;
    padding: 60px 0;
    color: #999;
    font-size: 14px;
  }
}

.filter-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;

  .filter-content {
    width: 100%;
    background-color: #fff;
    border-radius: 20px 20px 0 0;
    max-height: 80vh;
    overflow-y: auto;

    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;

      .filter-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }

      .close-btn {
        font-size: 24px;
        color: #999;
        font-weight: 300;
      }
    }

    .filter-group {
      padding: 20px;

      .filter-label {
        font-size: 16px;
        font-weight: 500;
        color: #333;
        margin-bottom: 15px;
      }

      .date-options,
      .type-options {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 10px;

        .date-option,
        .type-option {
          text-align: center;
          padding: 10px;
          border: 1px solid #d9d9d9;
          border-radius: 8px;
          font-size: 14px;
          color: #666;
          transition: all 0.3s ease;

          &.active {
            border-color: #1890ff;
            background-color: #1890ff;
            color: #fff;
          }
        }
      }

      .custom-date {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 10px;

        .date-picker {
          flex: 1;
          text-align: center;
          padding: 8px 12px;
          border: 1px solid #d9d9d9;
          border-radius: 6px;
          font-size: 14px;
          color: #666;
          margin: 0 8px;
        }
      }
    }

    .filter-actions {
      display: flex;
      gap: 10px;
      padding: 20px;
      border-top: 1px solid #f0f0f0;

      .reset-btn,
      .confirm-btn {
        flex: 1;
        height: 44px;
        border-radius: 22px;
        font-size: 16px;
        font-weight: 500;
        border: none;
      }

      .reset-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      .confirm-btn {
        background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
        color: #fff;
      }
    }
  }
}
</style>

