<template>
  <view class="transaction-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <text class="back-btn" @click="goBack">‹</text>
      <text class="nav-title">{{ pageTitle }}</text>
      <text class="filter-btn" @click="showFilter = true">筛选</text>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-label">总笔数</text>
        <text class="stat-value">{{ statistics.totalCount || 0 }}</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-label">总金额</text>
        <text class="stat-value">¥{{ formatAmount(statistics.totalAmount) }}</text>
      </view>
    </view>

    <!-- 交易列表 -->
    <scroll-view
      class="transaction-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        v-for="item in transactionList"
        :key="item.id"
        class="transaction-card"
        @click="viewDetail(item)"
      >
        <view class="card-header">
          <view :class="['status-badge', item.status]">
            {{ getStatusText(item.status) }}
          </view>
          <text class="transaction-time">{{ formatDateTime(item.createTime) }}</text>
        </view>
        
        <view class="card-body">
          <view class="transaction-info">
            <text class="transaction-type">{{ getTypeText(item.type) }}</text>
            <text class="transaction-id">订单号: {{ item.orderNo }}</text>
          </view>
          <text :class="['transaction-amount', item.type]">
            {{ item.type === 'recharge' ? '+' : '-' }}¥{{ formatAmount(item.amount) }}
          </text>
        </view>
      </view>

      <view v-if="transactionList.length === 0 && !loading" class="empty-state">
        <text class="empty-icon">📝</text>
        <text class="empty-text">暂无交易记录</text>
      </view>

      <view v-if="hasMore" class="loading-more">
        <text>加载更多...</text>
      </view>
      <view v-else-if="transactionList.length > 0" class="no-more">
        <text>没有更多了</text>
      </view>
    </scroll-view>

    <!-- 筛选弹窗 -->
    <view v-if="showFilter" class="filter-modal" @click="showFilter = false">
      <view class="filter-content" @click.stop>
        <text class="filter-title">筛选交易</text>
        
        <view class="filter-section">
          <text class="filter-label">交易类型</text>
          <view class="filter-options">
            <text
              v-for="type in typeOptions"
              :key="type.value"
              :class="['filter-option', { selected: filterType === type.value }]"
              @click="filterType = type.value"
            >
              {{ type.label }}
            </text>
          </view>
        </view>

        <view class="filter-section">
          <text class="filter-label">交易状态</text>
          <view class="filter-options">
            <text
              v-for="status in statusOptions"
              :key="status.value"
              :class="['filter-option', { selected: filterStatus === status.value }]"
              @click="filterStatus = status.value"
            >
              {{ status.label }}
            </text>
          </view>
        </view>

        <view class="filter-buttons">
          <button class="filter-btn reset" @click="resetFilter">重置</button>
          <button class="filter-btn confirm" @click="applyFilter">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import consumeApi from '@/api/business/consume/consume-api.js'
import { useUserStore } from '@/store/modules/system/user'

export default {
  name: 'ConsumeTransaction',
  
  setup() {
    const systemInfo = uni.getSystemInfoSync()
    const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
    const userStore = useUserStore()

    const loading = ref(false)
    const refreshing = ref(false)
    const hasMore = ref(true)
    const showFilter = ref(false)
    const transactionType = ref('all')  // all/recharge/refund

    const statistics = reactive({
      totalCount: 0,
      totalAmount: 0
    })

    const transactionList = ref([])
    const pageNum = ref(1)
    const pageSize = ref(20)

    const filterType = ref('all')
    const filterStatus = ref('all')

    const typeOptions = [
      { value: 'all', label: '全部' },
      { value: 'recharge', label: '充值' },
      { value: 'refund', label: '退款' }
    ]

    const statusOptions = [
      { value: 'all', label: '全部' },
      { value: 'pending', label: '处理中' },
      { value: 'success', label: '成功' },
      { value: 'failed', label: '失败' }
    ]

    const pageTitle = computed(() => {
      if (transactionType.value === 'recharge') return '充值记录'
      if (transactionType.value === 'refund') return '退款记录'
      return '交易记录'
    })

    onMounted(() => {
      init()
    })

    const init = async () => {
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      if (currentPage.options.type) {
        transactionType.value = currentPage.options.type
      }

      await loadTransactions()
      await loadStatistics()
    }

    const loadTransactions = async (append = false) => {
      try {
        loading.value = true
        
        const params = {
          userId: userStore.userId,
          pageNum: pageNum.value,
          pageSize: pageSize.value,
          type: filterType.value !== 'all' ? filterType.value : undefined,
          status: filterStatus.value !== 'all' ? filterStatus.value : undefined
        }

        const res = await consumeApi.getTransactionHistory(params)
        
        if (res.code === 1 && res.data) {
          const newList = res.data.list || res.data || []
          
          if (append) {
            transactionList.value = [...transactionList.value, ...newList]
          } else {
            transactionList.value = newList
          }
          
          hasMore.value = newList.length >= pageSize.value
        }
      } catch (error) {
        console.error('加载交易记录失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }

    const loadStatistics = async () => {
      try {
        // 从用户store获取用户ID
        const userId = userStore.employeeId
        if (!userId) {
          return
        }
        
        // 尝试调用统计API（如果后端已实现）
        try {
          const result = await consumeApi.statsApi.getUserStats(userId)
          if (result.success && result.data) {
            statistics.totalCount = result.data.totalCount || 0
            statistics.totalAmount = result.data.totalAmount || 0
            return
          }
        } catch (apiError) {
          // API未实现或调用失败，使用降级方案
          console.warn('统计API调用失败，使用降级方案:', apiError)
        }
        
        // 降级方案：从交易列表计算统计信息
        statistics.totalCount = transactionList.value.length
        statistics.totalAmount = transactionList.value.reduce((sum, item) => {
          return sum + Number(item.amount || 0)
        }, 0)
      } catch (error) {
        console.error('加载统计信息失败:', error)
      }
    }

    const viewDetail = (item) => {
      uni.navigateTo({
        url: `/pages/consume/transaction-detail?id=${item.id}`
      })
    }

    const onRefresh = async () => {
      refreshing.value = true
      pageNum.value = 1
      await loadTransactions(false)
    }

    const loadMore = () => {
      if (hasMore.value && !loading.value) {
        pageNum.value++
        loadTransactions(true)
      }
    }

    const applyFilter = async () => {
      showFilter.value = false
      pageNum.value = 1
      await loadTransactions(false)
    }

    const resetFilter = () => {
      filterType.value = 'all'
      filterStatus.value = 'all'
    }

    const getStatusText = (status) => {
      const map = {
        pending: '处理中',
        success: '成功',
        failed: '失败'
      }
      return map[status] || '未知'
    }

    const getTypeText = (type) => {
      const map = {
        recharge: '充值',
        refund: '退款',
        consume: '消费'
      }
      return map[type] || '交易'
    }

    const formatAmount = (amount) => {
      if (!amount && amount !== 0) return '0.00'
      return Number(amount).toFixed(2)
    }

    const formatDateTime = (datetime) => {
      if (!datetime) return ''
      const date = new Date(datetime)
      const month = date.getMonth() + 1
      const day = date.getDate()
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${month}-${day} ${hours}:${minutes}`
    }

    const goBack = () => {
      uni.navigateBack()
    }

    return {
      statusBarHeight,
      loading,
      refreshing,
      hasMore,
      showFilter,
      pageTitle,
      statistics,
      transactionList,
      filterType,
      filterStatus,
      typeOptions,
      statusOptions,
      viewDetail,
      onRefresh,
      loadMore,
      applyFilter,
      resetFilter,
      getStatusText,
      getTypeText,
      formatAmount,
      formatDateTime,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.transaction-page {
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
  
  .filter-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.stats-card {
  display: flex;
  align-items: center;
  margin: 24rpx 32rpx;
  padding: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  
  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .stat-label {
      font-size: 26rpx;
      color: rgba(0, 0, 0, 0.45);
      margin-bottom: 12rpx;
    }
    
    .stat-value {
      font-size: 40rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }
  }
  
  .stat-divider {
    width: 1px;
    height: 80rpx;
    background: #e8e8e8;
  }
}

.transaction-scroll {
  height: calc(100vh - 400rpx);
  padding: 0 32rpx;
}

.transaction-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16rpx;
    
    .status-badge {
      padding: 4rpx 16rpx;
      border-radius: 8rpx;
      font-size: 22rpx;
      
      &.pending {
        background: #fff7e6;
        color: #fa8c16;
      }
      
      &.success {
        background: #f6ffed;
        color: #52c41a;
      }
      
      &.failed {
        background: #fff1f0;
        color: #f5222d;
      }
    }
    
    .transaction-time {
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
    }
  }
  
  .card-body {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .transaction-info {
      flex: 1;
      
      .transaction-type {
        display: block;
        font-size: 32rpx;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
        margin-bottom: 8rpx;
      }
      
      .transaction-id {
        display: block;
        font-size: 24rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }
    
    .transaction-amount {
      font-size: 40rpx;
      font-weight: 600;
      
      &.recharge {
        color: #52c41a;
      }
      
      &.refund {
        color: #1890ff;
      }
      
      &.consume {
        color: #f5222d;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  
  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 24rpx;
  }
  
  .empty-text {
    font-size: 28rpx;
    color: rgba(0, 0, 0, 0.45);
  }
}

.loading-more,
.no-more {
  text-align: center;
  padding: 32rpx;
  font-size: 28rpx;
  color: rgba(0, 0, 0, 0.45);
}

.filter-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: flex-end;
  z-index: 200;
  
  .filter-content {
    width: 100%;
    max-height: 80vh;
    background: #fff;
    border-radius: 32rpx 32rpx 0 0;
    padding: 48rpx 32rpx;
    
    .filter-title {
      font-size: 36rpx;
      font-weight: 600;
      display: block;
      margin-bottom: 32rpx;
    }
    
    .filter-section {
      margin-bottom: 32rpx;
      
      .filter-label {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        display: block;
        margin-bottom: 16rpx;
      }
      
      .filter-options {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;
        
        .filter-option {
          padding: 12rpx 32rpx;
          background: #f0f0f0;
          border-radius: 8rpx;
          font-size: 26rpx;
          color: rgba(0, 0, 0, 0.65);
          
          &.selected {
            background: #1890ff;
            color: #fff;
          }
        }
      }
    }
    
    .filter-buttons {
      display: flex;
      gap: 16rpx;
      margin-top: 48rpx;
      
      .filter-btn {
        flex: 1;
        height: 88rpx;
        border: none;
        border-radius: 8rpx;
        font-size: 32rpx;
        
        &.reset {
          background: #f0f0f0;
          color: rgba(0, 0, 0, 0.65);
        }
        
        &.confirm {
          background: #1890ff;
          color: #fff;
        }
      }
    }
  }
}
</style>

