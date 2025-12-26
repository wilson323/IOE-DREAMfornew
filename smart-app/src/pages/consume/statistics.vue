<template>
  <view class="consume-statistics-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <view class="header-title">消费统计</view>
      <view class="header-subtitle">个人消费数据分析</view>
    </view>

    <!-- 时间选择器 -->
    <view class="time-selector">
      <view
        v-for="(item, index) in timeOptions"
        :key="index"
        class="time-option"
        :class="{ active: selectedTime === item.value }"
        @click="selectTime(item.value)"
      >
        {{ item.label }}
      </view>
    </view>

    <!-- 消费概览卡片 -->
    <view class="overview-cards">
      <view class="card-item">
        <view class="card-label">总消费</view>
        <view class="card-value amount">¥{{ overview.totalAmount || '0.00' }}</view>
      </view>
      <view class="card-item">
        <view class="card-label">消费次数</view>
        <view class="card-value">{{ overview.totalCount || 0 }}次</view>
      </view>
      <view class="card-item">
        <view class="card-label">平均消费</view>
        <view class="card-value">¥{{ overview.avgAmount || '0.00' }}</view>
      </view>
      <view class="card-item">
        <view class="card-label">当前余额</view>
        <view class="card-value balance">¥{{ overview.balance || '0.00' }}</view>
      </view>
    </view>

    <!-- 消费趋势图表 -->
    <view class="chart-section">
      <view class="section-title">消费趋势</view>
      <qiun-ucharts type="line" :opts="lineOpts" :chartData="trendData" />
    </view>

    <!-- 消费分类统计 -->
    <view class="category-section">
      <view class="section-title">消费分类</view>
      <qiun-ucharts type="pie" :opts="pieOpts" :chartData="categoryData" />
    </view>

    <!-- 消费时段分析 -->
    <view class="time-period-section">
      <view class="section-title">时段分析</view>
      <view class="period-list">
        <view v-for="(item, index) in periodData" :key="index" class="period-item">
          <view class="period-label">{{ item.period }}</view>
          <view class="period-bar-container">
            <view class="period-bar" :style="{ width: item.percent + '%' }"></view>
          </view>
          <view class="period-value">¥{{ item.amount }}</view>
        </view>
      </view>
    </view>

    <!-- 消费排行榜 -->
    <view class="ranking-section">
      <view class="section-title">我的消费排名</view>
      <view class="ranking-card">
        <view class="ranking-item">
          <view class="ranking-label">本月排名</view>
          <view class="ranking-value highlight">{{ ranking.monthRank || '-' }}</view>
        </view>
        <view class="ranking-item">
          <view class="ranking-label">超越用户</view>
          <view class="ranking-value">{{ ranking.surpassPercent || '0%' }}</view>
        </view>
      </view>
    </view>

    <!-- 消费明细 -->
    <view class="detail-section">
      <view class="section-title">最近消费</view>
      <view v-for="(item, index) in recentList" :key="index" class="detail-item">
        <view class="detail-icon">
          <text class="iconfont">{{ getMealIcon(item.consumeType) }}</text>
        </view>
        <view class="detail-info">
          <view class="detail-title">{{ item.consumeTypeName }}</view>
          <view class="detail-time">{{ formatTime(item.consumeTime) }}</view>
        </view>
        <view class="detail-amount">-¥{{ item.amount }}</view>
      </view>
      <view v-if="!recentList.length" class="empty-tip">暂无消费记录</view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <uni-load-more status="loading" :content-text="{ contentdown: '加载中...' }"></uni-load-more>
    </view>
  </view>
</template>

<script>
import { getRequest } from '@/lib/smart-request'

export default {
  name: 'ConsumeStatistics',
  data() {
    return {
      loading: false,
      selectedTime: 'month',
      timeOptions: [
        { label: '本周', value: 'week' },
        { label: '本月', value: 'month' },
        { label: '本年', value: 'year' }
      ],
      overview: {
        totalAmount: '0.00',
        totalCount: 0,
        avgAmount: '0.00',
        balance: '0.00'
      },
      trendData: {},
      categoryData: {},
      periodData: [],
      ranking: {
        monthRank: '-',
        surpassPercent: '0%'
      },
      recentList: [],
      lineOpts: {
        color: ['#1890FF', '#91CB74', '#FAC858', '#EE6666'],
        padding: [15, 15, 0, 5],
        enableScroll: false,
        legend: {},
        xAxis: {
          disableGrid: true
        },
        yAxis: {
          data: [{ min: 0 }],
          gridType: 'dash',
          dashLength: 2
        },
        extra: {
          line: {
            type: 'curve',
            width: 2,
            activeType: 'hollow'
          }
        }
      },
      pieOpts: {
        color: ['#1890FF', '#91CB74', '#FAC858', '#EE6666', '#73C0DE', '#3BA272'],
        padding: [5, 5, 5, 5],
        enableScroll: false,
        extra: {
          pie: {
            activeOpacity: 0.5,
            activeRadius: 10,
            offsetAngle: 0,
            labelWidth: 15,
            ringWidth: 30,
            border: true,
            borderWidth: 2,
            borderColor: '#FFFFFF'
          }
        }
      }
    }
  },
  onLoad() {
    this.loadStatisticsData()
  },
  onPullDownRefresh() {
    this.loadStatisticsData()
    setTimeout(() => {
      uni.stopPullDownRefresh()
    }, 1000)
  },
  methods: {
    /**
     * 选择时间范围
     */
    selectTime(value) {
      this.selectedTime = value
      this.loadStatisticsData()
    },

    /**
     * 加载统计数据
     */
    async loadStatisticsData() {
      this.loading = true

      try {
        const userId = uni.getStorageSync('userId')

        // 并行加载所有数据
        const [overview, trend, category, period, ranking, recent] = await Promise.all([
          this.getOverview(userId),
          this.getTrendData(userId),
          this.getCategoryData(userId),
          this.getPeriodData(userId),
          this.getRankingData(userId),
          this.getRecentData(userId)
        ])

        this.overview = overview
        this.trendData = trend
        this.categoryData = category
        this.periodData = period
        this.ranking = ranking
        this.recentList = recent
      } catch (error) {
        console.error('[消费统计] 加载失败:', error)
        uni.showToast({
          title: '数据加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取消费概览
     */
    async getOverview(userId) {
      const params = {
        userId,
        timeType: this.selectedTime
      }
      const response = await getRequest('/api/v1/consume/mobile/stats/overview', params)

      return response.data || {
        totalAmount: '0.00',
        totalCount: 0,
        avgAmount: '0.00',
        balance: '0.00'
      }
    },

    /**
     * 获取趋势数据
     */
    async getTrendData(userId) {
      const days = this.selectedTime === 'week' ? 7 : this.selectedTime === 'month' ? 30 : 365
      const params = { userId, days }
      const response = await getRequest('/api/v1/consume/mobile/stats/trend', params)

      return response.data || { categories: [], series: [] }
    },

    /**
     * 获取分类数据
     */
    async getCategoryData(userId) {
      const params = {
        userId,
        timeType: this.selectedTime
      }
      const response = await getRequest('/api/v1/consume/mobile/stats/category', params)

      return response.data || { series: [] }
    },

    /**
     * 获取时段数据
     */
    async getPeriodData(userId) {
      const params = {
        userId,
        timeType: this.selectedTime
      }
      const response = await getRequest('/api/v1/consume/mobile/stats/period', params)

      return response.data || []
    },

    /**
     * 获取排名数据
     */
    async getRankingData(userId) {
      const params = {
        userId,
        timeType: this.selectedTime
      }
      const response = await getRequest('/api/v1/consume/mobile/stats/ranking', params)

      return response.data || {
        monthRank: '-',
        surpassPercent: '0%'
      }
    },

    /**
     * 获取最近消费记录
     */
    async getRecentData(userId) {
      const params = {
        userId,
        pageSize: 5
      }
      const response = await getRequest('/api/v1/consume/mobile/history/recent', params)

      return response.data || []
    },

    /**
     * 获取餐别图标
     */
    getMealIcon(consumeType) {
      const iconMap = {
        BREAKFAST: '\ue100',
        LUNCH: '\ue101',
        DINNER: '\ue102',
        SNACK: '\ue103',
        DEFAULT: '\ue104'
      }
      return iconMap[consumeType] || iconMap.DEFAULT
    },

    /**
     * 格式化时间
     */
    formatTime(time) {
      if (!time) return ''

      const date = new Date(time)
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')

      return `${month}-${day} ${hour}:${minute}`
    }
  }
}
</script>

<style lang="scss" scoped>
.consume-statistics-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 0 30rpx 30rpx;
}

.page-header {
  padding: 40rpx 0 30rpx;
  text-align: center;

  .header-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }

  .header-subtitle {
    font-size: 24rpx;
    color: #999;
    margin-top: 10rpx;
  }
}

.time-selector {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 10rpx;
  margin-bottom: 30rpx;

  .time-option {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    font-size: 28rpx;
    color: #666;
    border-radius: 12rpx;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
      font-weight: bold;
    }
  }
}

.overview-cards {
  display: flex;
  flex-wrap: wrap;
  margin: 0 -10rpx 30rpx;

  .card-item {
    width: calc(50% - 20rpx);
    margin: 10rpx;
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);

    .card-label {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 10rpx;
    }

    .card-value {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;

      &.amount {
        color: #ff4d4f;
      }

      &.balance {
        color: #52c41a;
      }
    }
  }
}

.chart-section,
.category-section,
.time-period-section,
.ranking-section,
.detail-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  padding-left: 20rpx;
  border-left: 6rpx solid #1890ff;
}

.period-list {
  .period-item {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .period-label {
      width: 150rpx;
      font-size: 26rpx;
      color: #666;
    }

    .period-bar-container {
      flex: 1;
      height: 24rpx;
      background: #f0f0f0;
      border-radius: 12rpx;
      overflow: hidden;
      margin: 0 20rpx;
    }

    .period-bar {
      height: 100%;
      background: linear-gradient(90deg, #1890ff 0%, #40a9ff 100%);
      border-radius: 12rpx;
      transition: width 0.5s;
    }

    .period-value {
      width: 120rpx;
      text-align: right;
      font-size: 26rpx;
      font-weight: bold;
      color: #ff4d4f;
    }
  }
}

.ranking-card {
  display: flex;
  justify-content: space-around;

  .ranking-item {
    text-align: center;

    .ranking-label {
      font-size: 24rpx;
      color: #999;
      margin-bottom: 10rpx;
    }

    .ranking-value {
      font-size: 36rpx;
      font-weight: bold;
      color: #333;

      &.highlight {
        color: #1890ff;
      }
    }
  }
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .detail-icon {
    width: 80rpx;
    height: 80rpx;
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20rpx;

    .iconfont {
      font-size: 40rpx;
      color: #fff;
    }
  }

  .detail-info {
    flex: 1;

    .detail-title {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 8rpx;
    }

    .detail-time {
      font-size: 24rpx;
      color: #999;
    }
  }

  .detail-amount {
    font-size: 32rpx;
    font-weight: bold;
    color: #ff4d4f;
  }
}

.empty-tip {
  text-align: center;
  padding: 60rpx 0;
  font-size: 28rpx;
  color: #999;
}

.loading-container {
  padding: 60rpx 0;
  text-align: center;
}
</style>
