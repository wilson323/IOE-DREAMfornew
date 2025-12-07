<template>
  <view class="access-record-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">通行记录</view>
      <view class="nav-right"></view>
    </view>

    <!-- 记录列表 -->
    <view class="record-list">
      <view
        class="record-item"
        v-for="(record, index) in recordList"
        :key="index"
      >
        <view class="record-info">
          <text class="user-name">{{ record.userName }}</text>
          <text class="access-time">{{ formatDateTime(record.accessTime) }}</text>
        </view>
        <view class="record-detail">
          <text class="area-name">{{ record.areaName }}</text>
          <text :class="['access-result', record.success ? 'success' : 'fail']">
            {{ record.success ? '成功' : '失败' }}
          </text>
        </view>
      </view>

      <view class="no-data" v-if="recordList.length === 0 && !loading">
        <text>暂无通行记录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import accessApi from '@/api/access.js'

export default {
  name: 'AccessRecord',
  setup() {
    const recordList = ref([])
    const loading = ref(false)

    // 加载记录列表
    const loadRecords = async () => {
      loading.value = true
      try {
        const result = await accessApi.getRecentRecords({ limit: 50 })
        if (result.success && result.data) {
          recordList.value = result.data
        }
      } catch (error) {
        console.error('加载记录失败:', error)
      } finally {
        loading.value = false
      }
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
      formatDateTime,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.access-record-page {
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
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .record-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .user-name {
        font-size: 15px;
        font-weight: 500;
        color: #333;
      }

      .access-time {
        font-size: 12px;
        color: #999;
      }
    }

    .record-detail {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .area-name {
        font-size: 13px;
        color: #666;
      }

      .access-result {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 4px;

        &.success {
          background-color: #f6ffed;
          color: #52c41a;
        }

        &.fail {
          background-color: #fff2f0;
          color: #ff4d4f;
        }
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
</style>

