<template>
  <view class="visitor-record-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="iconfont icon-back">←</text>
      </view>
      <view class="nav-title">访问记录</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 记录列表 -->
      <view class="record-list">
        <view
          class="record-item"
          v-for="(record, index) in recordList"
          :key="index"
          @click="viewDetail(record)"
        >
          <view class="item-header">
            <text class="visitor-name">{{ record.visitorName }}</text>
            <text class="record-date">{{ formatDate(record.checkInTime) }}</text>
          </view>
          <view class="item-content">
            <view class="info-row">
              <text class="label">被访人：</text>
              <text class="value">{{ record.visiteeName }}</text>
            </view>
            <view class="info-row">
              <text class="label">签到时间：</text>
              <text class="value">{{ formatTime(record.checkInTime) }}</text>
            </view>
            <view class="info-row">
              <text class="label">签退时间：</text>
              <text class="value">{{ formatTime(record.checkOutTime) }}</text>
            </view>
            <view class="info-row">
              <text class="label">停留时长：</text>
              <text class="value">{{ formatDuration(record.actualDuration) }}</text>
            </view>
          </view>
          <view class="item-arrow">›</view>
        </view>

        <view class="no-data" v-if="recordList.length === 0 && !loading">
          <text>暂无访问记录</text>
        </view>

        <view class="loading" v-if="loading">
          <text>加载中...</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'
import visitorApi from '@/api/business/visitor/visitor-api.js'

export default {
  name: 'VisitorRecord',
  setup() {
    const recordList = ref([])
    const loading = ref(false)

    // 加载记录列表
    const loadRecords = async () => {
      loading.value = true
      try {
        const userId = 1 // TODO: 从本地存储获取
        const result = await visitorApi.getMyAppointments(userId)
        if (result.success && result.data) {
          recordList.value = result.data
        }
      } catch (error) {
        console.error('加载记录列表失败:', error)
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        loading.value = false
      }
    }

    // 查看详情
    const viewDetail = (record) => {
      uni.navigateTo({
        url: `/pages/visitor/record-detail?id=${record.appointmentId}`
      })
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

    // 格式化时间
    const formatTime = (datetime) => {
      if (!datetime) return '-'
      const date = new Date(datetime)
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    }

    // 格式化时长
    const formatDuration = (minutes) => {
      if (!minutes) return '-'
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      return `${hours}小时${mins}分钟`
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
      formatDate,
      formatTime,
      formatDuration,
      goBack
    }
  }
}
</script>

<style lang="scss" scoped>
.visitor-record-page {
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

.page-content {
  padding: 15px;
}

.record-list {
  .record-item {
    background-color: #fff;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .item-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .visitor-name {
        font-size: 16px;
        font-weight: 600;
        color: #333;
      }

      .record-date {
        font-size: 12px;
        color: #999;
      }
    }

    .item-content {
      flex: 1;

      .info-row {
        display: flex;
        margin-bottom: 8px;
        font-size: 13px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #666;
          width: 80px;
        }

        .value {
          color: #333;
          flex: 1;
        }
      }
    }

    .item-arrow {
      font-size: 20px;
      color: #d9d9d9;
      margin-left: 10px;
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

