<template>
  <view class="consume-record-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">消费记录</view>
      <view class="nav-right"></view>
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
          <text class="merchant-name">{{ record.merchantName || '消费' }}</text>
          <text class="transaction-time">{{ formatDateTime(record.transactionTime) }}</text>
        </view>
        <view class="item-right">
          <text class="amount-value">-¥{{ formatAmount(record.amount) }}</text>
        </view>
      </view>

      <view class="no-data" v-if="recordList.length === 0 && !loading">
        <text>暂无消费记录</text>
      </view>

      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import consumeApi from '@/api/business/consume/consume-api.js'

export default {
  name: 'ConsumeRecord',
  setup() {
    const recordList = ref([])
    const loading = ref(false)

    // 加载记录列表
    const loadRecords = async () => {
      loading.value = true
      try {
        const result = await consumeApi.getTransactionHistory({ limit: 50 })
        if (result.success && result.data) {
          recordList.value = result.data
        }
      } catch (error) {
        console.error('加载记录失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        loading.value = false
      }
    }

    // 查看详情
    const viewDetail = (record) => {
      uni.showToast({ title: '详情功能开发中', icon: 'none' })
    }

    // 格式化金额
    const formatAmount = (amount) => {
      if (!amount) return '0.00'
      return Number(amount).toFixed(2)
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

    // 返回
    const goBack = () => {
      uni.navigateBack()
    }

    // 初始化
    onMounted(() => {
      loadRecords()
    })

    return {
      recordList,
      loading,
      viewDetail,
      formatAmount,
      formatDateTime,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.consume-record-page {
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

.record-list {
  padding: 15px;

  .record-item {
    background-color: #fff;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .item-left {
      flex: 1;

      .merchant-name {
        display: block;
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6px;
      }

      .transaction-time {
        display: block;
        font-size: 12px;
        color: #999;
      }
    }

    .item-right {
      .amount-value {
        font-size: 18px;
        font-weight: 600;
        color: #ff4d4f;
      }
    }
  }

  .no-data, .loading {
    text-align: center;
    padding: 40px 0;
    color: #999;
    font-size: 14px;
  }
}
</style>

