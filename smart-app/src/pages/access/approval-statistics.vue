<template>
  <view class="statistics-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">审批统计</text>
        </view>
        <view class="navbar-right">
          <view class="date-range-btn" @tap="showDatePicker">
            <uni-icons type="calendar" size="16" color="#fff"></uni-icons>
            <text class="date-text">{{ dateRangeText }}</text>
            <uni-icons type="down" size="12" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 统计概览 -->
      <view class="overview-section">
        <view class="overview-card">
          <view class="overview-item">
            <text class="overview-value">{{ statistics.totalApplications }}</text>
            <text class="overview-label">总申请数</text>
          </view>
          <view class="overview-item">
            <text class="overview-value success">{{ statistics.approvalRate }}%</text>
            <text class="overview-label">审批通过率</text>
          </view>
          <view class="overview-item">
            <text class="overview-value warning">{{ statistics.avgProcessTime }}</text>
            <text class="overview-label">平均处理时间</text>
          </view>
          <view class="overview-item">
            <text class="overview-value danger">{{ statistics.pendingCount }}</text>
            <text class="overview-label">待处理</text>
          </view>
        </view>
      </view>

      <!-- 环比数据 -->
      <view class="comparison-section" v-if="comparisonData">
        <view class="comparison-card">
          <view class="comparison-title">与上期对比</view>
          <view class="comparison-grid">
            <view class="comparison-item">
              <text class="comparison-label">申请数</text>
              <view class="comparison-value" :class="{ up: comparisonData.applicationsUp, down: !comparisonData.applicationsUp }">
                <text>{{ comparisonData.applicationsChange > 0 ? '+' : '' }}{{ comparisonData.applicationsChange }}%</text>
                <uni-icons :type="comparisonData.applicationsUp ? 'up' : 'down'" size="12"></uni-icons>
              </view>
            </view>
            <view class="comparison-item">
              <text class="comparison-label">通过率</text>
              <view class="comparison-value" :class="{ up: comparisonData.rateUp, down: !comparisonData.rateUp }">
                <text>{{ comparisonData.rateChange > 0 ? '+' : '' }}{{ comparisonData.rateChange }}%</text>
                <uni-icons :type="comparisonData.rateUp ? 'up' : 'down'" size="12"></uni-icons>
              </view>
            </view>
            <view class="comparison-item">
              <text class="comparison-label">处理时间</text>
              <view class="comparison-value" :class="{ up: comparisonData.timeUp, down: !comparisonData.timeUp }">
                <text>{{ comparisonData.timeChange > 0 ? '+' : '' }}{{ comparisonData.timeChange }}%</text>
                <uni-icons :type="comparisonData.timeUp ? 'up' : 'down'" size="12"></uni-icons>
              </view>
            </view>
            <view class="comparison-item">
              <text class="comparison-label">待处理</text>
              <view class="comparison-value" :class="{ up: comparisonData.pendingUp, down: !comparisonData.pendingUp }">
                <text>{{ comparisonData.pendingChange > 0 ? '+' : '' }}{{ comparisonData.pendingChange }}%</text>
                <uni-icons :type="comparisonData.pendingUp ? 'up' : 'down'" size="12"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批趋势图 -->
      <view class="chart-section">
        <view class="section-header">
          <text class="section-title">审批趋势</text>
          <view class="chart-tabs">
            <view
              class="chart-tab"
              :class="{ active: trendPeriod === 'week' }"
              @tap="changeTrendPeriod('week')"
            >
              <text>周</text>
            </view>
            <view
              class="chart-tab"
              :class="{ active: trendPeriod === 'month' }"
              @tap="changeTrendPeriod('month')"
            >
              <text>月</text>
            </view>
            <view
              class="chart-tab"
              :class="{ active: trendPeriod === 'quarter' }"
              @tap="changeTrendPeriod('quarter')"
            >
              <text>季</text>
            </view>
          </view>
        </view>
        <view class="chart-container">
          <view class="bar-chart">
            <view class="chart-bars">
              <view
                class="bar-item"
                v-for="(item, index) in trendData"
                :key="index"
              >
                <view class="bar-wrapper">
                  <view
                    class="bar-approve"
                    :style="{ height: item.approvePercent + '%' }"
                  >
                  </view>
                  <view
                    class="bar-reject"
                    :style="{ height: item.rejectPercent + '%' }"
                  >
                  </view>
                </view>
                <text class="bar-label">{{ item.label }}</text>
                <view class="bar-values">
                  <text class="value-approve">{{ item.approveCount }}</text>
                  <text class="value-reject">{{ item.rejectCount }}</text>
                </view>
              </view>
            </view>
          </view>
          <view class="chart-legend">
            <view class="legend-item">
              <view class="legend-dot approve"></view>
              <text class="legend-text">通过</text>
            </view>
            <view class="legend-item">
              <view class="legend-dot reject"></view>
              <text class="legend-text">拒绝</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批类型分布 -->
      <view class="distribution-section">
        <view class="section-header">
          <text class="section-title">审批类型分布</text>
        </view>
        <view class="type-distribution">
          <view
            class="type-item"
            v-for="(item, index) in typeDistribution"
            :key="index"
            :style="{ borderColor: item.color }"
          >
            <view class="type-info">
              <view class="type-icon" :style="{ background: item.color }">
                <uni-icons :type="item.icon" size="20" color="#fff"></uni-icons>
              </view>
              <view class="type-details">
                <text class="type-name">{{ item.name }}</text>
                <text class="type-count">{{ item.count }}次</text>
              </view>
            </view>
            <view class="type-percent">
              <text class="percent-value" :style="{ color: item.color }">{{ item.percent }}%</text>
              <view class="percent-bar">
                <view
                  class="percent-fill"
                  :style="{ width: item.percent + '%', background: item.color }"
                ></view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批状态分布 -->
      <view class="status-section">
        <view class="section-header">
          <text class="section-title">审批状态分布</text>
        </view>
        <view class="status-chart">
          <view class="status-circle">
            <view class="circle-inner">
              <text class="circle-total">{{ statistics.totalApplications }}</text>
              <text class="circle-label">总申请</text>
            </view>
          </view>
          <view class="status-list">
            <view class="status-item" v-for="(item, index) in statusDistribution" :key="index">
              <view class="status-dot" :style="{ background: item.color }"></view>
              <view class="status-info">
                <text class="status-name">{{ item.name }}</text>
                <text class="status-count">{{ item.count }}次</text>
              </view>
              <text class="status-percent" :style="{ color: item.color }">{{ item.percent }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 处理时间分析 -->
      <view class="time-analysis-section">
        <view class="section-header">
          <text class="section-title">处理时间分析</text>
        </view>
        <view class="time-analysis-list">
          <view class="time-item" v-for="(item, index) in timeAnalysis" :key="index">
            <view class="time-header">
              <text class="time-title">{{ item.title }}</text>
              <text class="time-count" :class="item.statusClass">{{ item.count }}次</text>
            </view>
            <view class="time-bar">
              <view
                class="time-fill"
                :style="{ width: item.percent + '%', background: item.color }"
              ></view>
            </view>
            <view class="time-footer">
              <text class="time-range">{{ item.timeRange }}</text>
              <text class="time-percent">{{ item.percent }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批人绩效 -->
      <view class="performance-section">
        <view class="section-header">
          <text class="section-title">审批人绩效</text>
          <text class="section-more" @tap="viewAllPerformance">查看全部</text>
        </view>
        <view class="performance-list">
          <view
            class="performance-item"
            v-for="(item, index) in topPerformers"
            :key="index"
            @tap="viewPerformerDetail(item)"
          >
            <view class="performer-rank" :class="'rank-' + (index + 1)">
              <text class="rank-number">{{ index + 1 }}</text>
            </view>
            <image class="performer-avatar" :src="item.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
            <view class="performer-info">
              <text class="performer-name">{{ item.name }}</text>
              <text class="performer-dept">{{ item.department }}</text>
            </view>
            <view class="performer-stats">
              <text class="performer-count">{{ item.processedCount }}次</text>
              <text class="performer-rate" :class="{ high: item.avgProcessTime < 4, medium: item.avgProcessTime >= 4 && item.avgProcessTime < 8 }">
                平均{{ item.avgProcessTime }}小时
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 导出按钮 -->
      <view class="export-section">
        <view class="export-btn" @tap="exportReport">
          <uni-icons type="download" size="18" color="#fff"></uni-icons>
          <text class="export-text">导出统计报告</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>

    <!-- 日期范围选择器弹窗 -->
    <uni-popup ref="datePopup" type="bottom">
      <view class="date-picker-popup">
        <view class="popup-header">
          <text class="popup-title">选择日期范围</text>
          <view class="popup-close" @tap="closeDatePicker">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>
        <view class="date-options">
          <view
            class="date-option"
            :class="{ active: dateRangeType === 'today' }"
            @tap="selectDateRange('today')"
          >
            <text>今天</text>
          </view>
          <view
            class="date-option"
            :class="{ active: dateRangeType === 'week' }"
            @tap="selectDateRange('week')"
          >
            <text>本周</text>
          </view>
          <view
            class="date-option"
            :class="{ active: dateRangeType === 'month' }"
            @tap="selectDateRange('month')"
          >
            <text>本月</text>
          </view>
          <view
            class="date-option"
            :class="{ active: dateRangeType === 'quarter' }"
            @tap="selectDateRange('quarter')"
          >
            <text>本季度</text>
          </view>
          <view
            class="date-option"
            :class="{ active: dateRangeType === 'year' }"
            @tap="selectDateRange('year')"
          >
            <text>本年度</text>
          </view>
          <view
            class="date-option"
            :class="{ active: dateRangeType === 'custom' }"
            @tap="selectDateRange('custom')"
          >
            <text>自定义</text>
          </view>
        </view>
        <view class="custom-date-range" v-if="dateRangeType === 'custom'">
          <picker mode="date" :value="startDate" @change="onStartDateChange">
            <view class="date-input">
              <text>{{ startDate || '开始日期' }}</text>
              <uni-icons type="calendar" size="16" color="#999"></uni-icons>
            </view>
          </picker>
          <text class="date-separator">至</text>
          <picker mode="date" :value="endDate" @change="onEndDateChange">
            <view class="date-input">
              <text>{{ endDate || '结束日期' }}</text>
              <uni-icons type="calendar" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>
        <view class="popup-actions">
          <view class="action-btn cancel" @tap="closeDatePicker">取消</view>
          <view class="action-btn confirm" @tap="confirmDateRange">确定</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)
const refreshing = ref(false)

// 日期范围
const dateRangeType = ref('month')
const startDate = ref('')
const endDate = ref('')

// 趋势周期
const trendPeriod = ref('week')

// 统计数据
const statistics = reactive({
  totalApplications: 0,
  approvalRate: 0,
  avgProcessTime: '0小时',
  pendingCount: 0
})

// 环比数据
const comparisonData = ref(null)

// 趋势数据
const trendData = ref([])

// 类型分布
const typeDistribution = ref([])

// 状态分布
const statusDistribution = ref([])

// 时间分析
const timeAnalysis = ref([])

// 优秀审批人
const topPerformers = ref([])

// 计算属性 - 日期范围文本
const dateRangeText = computed(() => {
  const typeMap = {
    today: '今天',
    week: '本周',
    month: '本月',
    quarter: '本季度',
    year: '本年度',
    custom: '自定义'
  }
  return typeMap[dateRangeType.value] || '本月'
})

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 显示日期选择器
const showDatePicker = () => {
  // 显示弹窗
  uni.$emit('showDatePopup')
}

// 关闭日期选择器
const closeDatePicker = () => {
  // 关闭弹窗
  uni.$emit('hideDatePopup')
}

// 选择日期范围
const selectDateRange = (type) => {
  dateRangeType.value = type
}

// 开始日期变更
const onStartDateChange = (e) => {
  startDate.value = e.detail.value
}

// 结束日期变更
const onEndDateChange = (e) => {
  endDate.value = e.detail.value
}

// 确认日期范围
const confirmDateRange = () => {
  closeDatePicker()
  loadStatistics()
}

// 切换趋势周期
const changeTrendPeriod = (period) => {
  trendPeriod.value = period
  loadTrendData()
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    // TODO: 调用实际API
    // const res = await approvalApi.getStatistics({
    //   dateRangeType: dateRangeType.value,
    //   startDate: startDate.value,
    //   endDate: endDate.value
    // })

    // 模拟数据
    statistics.totalApplications = 156
    statistics.approvalRate = 85.3
    statistics.avgProcessTime = '4.2小时'
    statistics.pendingCount = 23

    // 加载环比数据
    loadComparisonData()

    // 加载趋势数据
    loadTrendData()

    // 加载类型分布
    loadTypeDistribution()

    // 加载状态分布
    loadStatusDistribution()

    // 加载时间分析
    loadTimeAnalysis()

    // 加载优秀审批人
    loadTopPerformers()
  } catch (error) {
    console.error('加载统计数据失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

// 加载环比数据
const loadComparisonData = () => {
  // 模拟数据
  comparisonData.value = {
    applicationsChange: 12.5,
    applicationsUp: true,
    rateChange: 3.2,
    rateUp: true,
    timeChange: -8.6,
    timeUp: false,
    pendingChange: -15.3,
    pendingUp: false
  }
}

// 加载趋势数据
const loadTrendData = () => {
  // 模拟数据
  if (trendPeriod.value === 'week') {
    trendData.value = [
      { label: '周一', approveCount: 12, rejectCount: 3, approvePercent: 80, rejectPercent: 20 },
      { label: '周二', approveCount: 15, rejectCount: 2, approvePercent: 88, rejectPercent: 12 },
      { label: '周三', approveCount: 18, rejectCount: 4, approvePercent: 82, rejectPercent: 18 },
      { label: '周四', approveCount: 14, rejectCount: 3, approvePercent: 82, rejectPercent: 18 },
      { label: '周五', approveCount: 20, rejectCount: 5, approvePercent: 80, rejectPercent: 20 },
      { label: '周六', approveCount: 8, rejectCount: 1, approvePercent: 89, rejectPercent: 11 },
      { label: '周日', approveCount: 6, rejectCount: 1, approvePercent: 86, rejectPercent: 14 }
    ]
  } else if (trendPeriod.value === 'month') {
    trendData.value = [
      { label: '第1周', approveCount: 45, rejectCount: 10, approvePercent: 82, rejectPercent: 18 },
      { label: '第2周', approveCount: 52, rejectCount: 8, approvePercent: 87, rejectPercent: 13 },
      { label: '第3周', approveCount: 48, rejectCount: 11, approvePercent: 81, rejectPercent: 19 },
      { label: '第4周', approveCount: 55, rejectCount: 9, approvePercent: 86, rejectPercent: 14 }
    ]
  } else {
    trendData.value = [
      { label: '1月', approveCount: 180, rejectCount: 35, approvePercent: 84, rejectPercent: 16 },
      { label: '2月', approveCount: 165, rejectCount: 30, approvePercent: 85, rejectPercent: 15 },
      { label: '3月', approveCount: 200, rejectCount: 38, approvePercent: 84, rejectPercent: 16 }
    ]
  }
}

// 加载类型分布
const loadTypeDistribution = () => {
  const total = statistics.totalApplications
  typeDistribution.value = [
    {
      name: '门禁权限',
      icon: 'locked',
      count: 68,
      percent: ((68 / total) * 100).toFixed(1),
      color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      name: '访客预约',
      icon: 'person',
      count: 42,
      percent: ((42 / total) * 100).toFixed(1),
      color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    },
    {
      name: '加班申请',
      icon: 'clock',
      count: 28,
      percent: ((28 / total) * 100).toFixed(1),
      color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
    },
    {
      name: '请假申请',
      icon: 'calendar',
      count: 18,
      percent: ((18 / total) * 100).toFixed(1),
      color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
    }
  ]
}

// 加载状态分布
const loadStatusDistribution = () => {
  const total = statistics.totalApplications
  statusDistribution.value = [
    {
      name: '已通过',
      count: Math.floor(total * (statistics.approvalRate / 100)),
      percent: statistics.approvalRate,
      color: '#52c41a'
    },
    {
      name: '已拒绝',
      count: Math.floor(total * 0.1),
      percent: 10.2,
      color: '#ff4d4f'
    },
    {
      name: '待审批',
      count: statistics.pendingCount,
      percent: ((statistics.pendingCount / total) * 100).toFixed(1),
      color: '#faad14'
    },
    {
      name: '已撤回',
      count: Math.floor(total * 0.04),
      percent: 4.3,
      color: '#999'
    }
  ]
}

// 加载时间分析
const loadTimeAnalysis = () => {
  const total = statistics.totalApplications
  timeAnalysis.value = [
    {
      title: '快速处理',
      count: 95,
      percent: ((95 / total) * 100).toFixed(1),
      timeRange: '< 2小时',
      color: '#52c41a',
      statusClass: 'success'
    },
    {
      title: '正常处理',
      count: 38,
      percent: ((38 / total) * 100).toFixed(1),
      timeRange: '2-8小时',
      color: '#1890ff',
      statusClass: 'normal'
    },
    {
      title: '较慢处理',
      count: 15,
      percent: ((15 / total) * 100).toFixed(1),
      timeRange: '8-24小时',
      color: '#faad14',
      statusClass: 'warning'
    },
    {
      title: '超时处理',
      count: 8,
      percent: ((8 / total) * 100).toFixed(1),
      timeRange: '> 24小时',
      color: '#ff4d4f',
      statusClass: 'danger'
    }
  ]
}

// 加载优秀审批人
const loadTopPerformers = () => {
  topPerformers.value = [
    {
      userId: 1,
      name: '张三',
      department: '行政部',
      avatar: '/static/avatar1.png',
      processedCount: 45,
      avgProcessTime: 3.2
    },
    {
      userId: 2,
      name: '李四',
      department: '人事部',
      avatar: '/static/avatar2.png',
      processedCount: 42,
      avgProcessTime: 4.5
    },
    {
      userId: 3,
      name: '王五',
      department: '财务部',
      avatar: '/static/avatar3.png',
      processedCount: 38,
      avgProcessTime: 5.1
    },
    {
      userId: 4,
      name: '赵六',
      department: '技术部',
      avatar: '/static/avatar4.png',
      processedCount: 35,
      avgProcessTime: 6.8
    },
    {
      userId: 5,
      name: '孙七',
      department: '市场部',
      avatar: '/static/avatar5.png',
      processedCount: 32,
      avgProcessTime: 7.2
    }
  ]
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadStatistics().finally(() => {
    refreshing.value = false
  })
}

// 加载更多
const loadMore = () => {
  // 如有分页需求
}

// 查看全部审批人绩效
const viewAllPerformance = () => {
  uni.navigateTo({
    url: '/pages/access/approval-performer-list'
  })
}

// 查看审批人详情
const viewPerformerDetail = (performer) => {
  uni.navigateTo({
    url: `/pages/access/approval-performer-detail?userId=${performer.userId}`
  })
}

// 导出报告
const exportReport = () => {
  uni.showLoading({ title: '导出中...' })

  // TODO: 调用实际导出API
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  }, 1500)
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
  loadStatistics()
})
</script>

<style lang="scss" scoped>
.statistics-container {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1000;

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 30rpx;

    .navbar-left {
      display: flex;
      align-items: center;

      .navbar-title {
        margin-left: 20rpx;
        font-size: 18px;
        font-weight: 500;
        color: #fff;
      }
    }

    .navbar-right {
      .date-range-btn {
        display: flex;
        align-items: center;
        padding: 8rpx 20rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20rpx;

        .date-text {
          margin: 0 10rpx;
          font-size: 13px;
          color: #fff;
        }
      }
    }
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 44px);
}

// 统计概览
.overview-section {
  padding: 30rpx;

  .overview-card {
    display: flex;
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .overview-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .overview-value {
        font-size: 32px;
        font-weight: 600;
        color: #333;
        margin-bottom: 10rpx;

        &.success {
          color: #52c41a;
        }

        &.warning {
          color: #faad14;
        }

        &.danger {
          color: #ff4d4f;
        }
      }

      .overview-label {
        font-size: 13px;
        color: #999;
      }
    }
  }
}

// 环比数据
.comparison-section {
  padding: 0 30rpx 30rpx;

  .comparison-card {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .comparison-title {
      font-size: 15px;
      font-weight: 500;
      color: #333;
      margin-bottom: 30rpx;
    }

    .comparison-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20rpx;

      .comparison-item {
        display: flex;
        flex-direction: column;
        align-items: center;

        .comparison-label {
          font-size: 12px;
          color: #999;
          margin-bottom: 10rpx;
        }

        .comparison-value {
          display: flex;
          align-items: center;
          font-size: 14px;
          font-weight: 500;

          &.up {
            color: #52c41a;
          }

          &.down {
            color: #ff4d4f;
          }

          text {
            margin-right: 4rpx;
          }
        }
      }
    }
  }
}

// 图表区块
.chart-section,
.distribution-section,
.status-section,
.time-analysis-section,
.performance-section {
  padding: 0 30rpx 30rpx;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .section-more {
      font-size: 13px;
      color: #667eea;
    }

    .chart-tabs {
      display: flex;
      background: #f5f5f5;
      border-radius: 20rpx;
      padding: 4rpx;

      .chart-tab {
        padding: 8rpx 20rpx;
        border-radius: 16rpx;
        font-size: 12px;
        color: #666;
        transition: all 0.3s;

        &.active {
          background: #fff;
          color: #667eea;
          font-weight: 500;
        }
      }
    }
  }

  .chart-container,
  .type-distribution,
  .status-chart,
  .time-analysis-list,
  .performance-list {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  }
}

// 趋势柱状图
.chart-container {
  .bar-chart {
    .chart-bars {
      display: flex;
      align-items: flex-end;
      justify-content: space-around;
      height: 300rpx;
      padding: 20rpx 0;

      .bar-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex: 1;

        .bar-wrapper {
          display: flex;
          flex-direction: column;
          justify-content: flex-end;
          width: 40rpx;
          height: 240rpx;
          background: #f5f5f5;
          border-radius: 8rpx;
          overflow: hidden;

          .bar-approve {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 8rpx 8rpx 0 0;
            transition: height 0.3s;
          }

          .bar-reject {
            background: #ff4d4f;
            transition: height 0.3s;
          }
        }

        .bar-label {
          margin-top: 10rpx;
          font-size: 11px;
          color: #999;
        }

        .bar-values {
          display: flex;
          flex-direction: column;
          align-items: center;
          margin-top: 8rpx;

          text {
            font-size: 10px;
            color: #666;
          }

          .value-approve {
            color: #667eea;
          }

          .value-reject {
            color: #ff4d4f;
          }
        }
      }
    }

    .chart-legend {
      display: flex;
      justify-content: center;
      margin-top: 30rpx;

      .legend-item {
        display: flex;
        align-items: center;
        margin: 0 30rpx;

        .legend-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 6rpx;
          margin-right: 10rpx;

          &.approve {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.reject {
            background: #ff4d4f;
          }
        }

        .legend-text {
          font-size: 12px;
          color: #666;
        }
      }
    }
  }
}

// 类型分布
.type-distribution {
  .type-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .type-info {
      display: flex;
      align-items: center;
      flex: 1;

      .type-icon {
        width: 56rpx;
        height: 56rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
      }

      .type-details {
        display: flex;
        flex-direction: column;

        .type-name {
          font-size: 14px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .type-count {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .type-percent {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      width: 150rpx;

      .percent-value {
        font-size: 18px;
        font-weight: 600;
        margin-bottom: 10rpx;
      }

      .percent-bar {
        width: 100%;
        height: 8rpx;
        background: #f5f5f5;
        border-radius: 4rpx;
        overflow: hidden;

        .percent-fill {
          height: 100%;
          border-radius: 4rpx;
          transition: width 0.3s;
        }
      }
    }
  }
}

// 状态分布
.status-chart {
  display: flex;
  align-items: center;

  .status-circle {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    margin-right: 40rpx;
    border-radius: 50%;
    background: conic-gradient(
      #52c41a 0deg,
      #52c41a 306deg,
      #ff4d4f 306deg,
      #ff4d4f 343deg,
      #faad14 343deg,
      #faad14 394deg,
      #999 394deg,
      #999 411deg,
      #f5f5f5 411deg,
      #f5f5f5 360deg
    );
    display: flex;
    align-items: center;
    justify-content: center;

    .circle-inner {
      width: 140rpx;
      height: 140rpx;
      border-radius: 50%;
      background: #fff;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      .circle-total {
        font-size: 32px;
        font-weight: 600;
        color: #333;
      }

      .circle-label {
        font-size: 12px;
        color: #999;
        margin-top: 6rpx;
      }
    }
  }

  .status-list {
    flex: 1;

    .status-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;

      .status-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 6rpx;
        margin-right: 20rpx;
      }

      .status-info {
        flex: 1;
        display: flex;
        flex-direction: column;

        .status-name {
          font-size: 14px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .status-count {
          font-size: 12px;
          color: #999;
        }
      }

      .status-percent {
        font-size: 16px;
        font-weight: 500;
      }
    }
  }
}

// 时间分析
.time-analysis-list {
  .time-item {
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .time-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .time-title {
        font-size: 14px;
        color: #333;
      }

      .time-count {
        font-size: 14px;
        font-weight: 500;

        &.success {
          color: #52c41a;
        }

        &.normal {
          color: #1890ff;
        }

        &.warning {
          color: #faad14;
        }

        &.danger {
          color: #ff4d4f;
        }
      }
    }

    .time-bar {
      width: 100%;
      height: 12rpx;
      background: #f5f5f5;
      border-radius: 6rpx;
      overflow: hidden;
      margin-bottom: 10rpx;

      .time-fill {
        height: 100%;
        border-radius: 6rpx;
        transition: width 0.3s;
      }
    }

    .time-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .time-range {
        font-size: 12px;
        color: #999;
      }

      .time-percent {
        font-size: 12px;
        color: #666;
      }
    }
  }
}

// 审批人绩效
.performance-list {
  .performance-item {
    display: flex;
    align-items: center;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .performer-rank {
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
      background: #f5f5f5;

      &.rank-1 {
        background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);

        .rank-number {
          color: #fff;
          font-weight: 600;
        }
      }

      &.rank-2 {
        background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);

        .rank-number {
          color: #fff;
          font-weight: 600;
        }
      }

      &.rank-3 {
        background: linear-gradient(135deg, #cd7f32 0%, #e5a157 100%);

        .rank-number {
          color: #fff;
          font-weight: 600;
        }
      }

      .rank-number {
        font-size: 14px;
        color: #999;
      }
    }

    .performer-avatar {
      width: 64rpx;
      height: 64rpx;
      border-radius: 50%;
      margin-right: 20rpx;
      background: #f5f5f5;
    }

    .performer-info {
      flex: 1;

      .performer-name {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .performer-dept {
        font-size: 12px;
        color: #999;
      }
    }

    .performer-stats {
      display: flex;
      flex-direction: column;
      align-items: flex-end;

      .performer-count {
        font-size: 14px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .performer-rate {
        font-size: 12px;
        color: #999;

        &.high {
          color: #52c41a;
        }

        &.medium {
          color: #faad14;
        }
      }
    }
  }
}

// 导出区域
.export-section {
  padding: 30rpx;

  .export-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 88rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);

    .export-text {
      margin-left: 12rpx;
      font-size: 15px;
      font-weight: 500;
      color: #fff;
    }
  }
}

// 日期选择器弹窗
.date-picker-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .popup-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      padding: 10rpx;
    }
  }

  .date-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;
    margin-bottom: 30rpx;

    .date-option {
      height: 72rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 13px;
      color: #666;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }

  .custom-date-range {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .date-input {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 72rpx;
      padding: 0 20rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 13px;
      color: #333;
    }

    .date-separator {
      margin: 0 20rpx;
      font-size: 13px;
      color: #999;
    }
  }

  .popup-actions {
    display: flex;
    gap: 20rpx;

    .action-btn {
      flex: 1;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12rpx;
      font-size: 15px;
      font-weight: 500;

      &.cancel {
        background: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
