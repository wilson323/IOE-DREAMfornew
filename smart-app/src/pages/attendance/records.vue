<template>
  <view class="records-container">
    <!-- 顶部日期选择器 -->
    <view class="date-selector">
      <view class="selector-header">
        <text class="current-year">{{ currentYear }}</text>
        <text class="current-month">{{ currentMonth }}月</text>
      </view>

      <view class="month-list">
        <scroll-view scroll-x class="month-scroll">
          <view
            class="month-item"
            :class="{ 'active': month === parseInt(currentMonth) }"
            v-for="month in monthList"
            :key="month"
            @click="selectMonth(month)"
          >
            <text class="month-text">{{ month }}月</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 筛选器 -->
    <view class="filter-bar">
      <view class="filter-tabs">
        <view
          class="filter-tab"
          :class="{ 'active': activeFilter === 'all' }"
          @click="setFilter('all')"
        >
          <text class="tab-text">全部</text>
          <text class="tab-count" v-if="recordCount.all > 0">{{ recordCount.all }}</text>
        </view>
        <view
          class="filter-tab"
          :class="{ 'active': activeFilter === 'normal' }"
          @click="setFilter('normal')"
        >
          <text class="tab-text">正常</text>
          <text class="tab-count" v-if="recordCount.normal > 0">{{ recordCount.normal }}</text>
        </view>
        <view
          class="filter-tab"
          :class="{ 'active': activeFilter === 'abnormal' }"
          @click="setFilter('abnormal')"
        >
          <text class="tab-text">异常</text>
          <text class="tab-count" v-if="recordCount.abnormal > 0">{{ recordCount.abnormal }}</text>
        </view>
        <view
          class="filter-tab"
          :class="{ 'active': activeFilter === 'late' }"
          @click="setFilter('late')"
        >
          <text class="tab-text">迟到</text>
          <text class="tab-count" v-if="recordCount.late > 0">{{ recordCount.late }}</text>
        </view>
        <view
          class="filter-tab"
          :class="{ 'active': activeFilter === 'early' }"
          @click="setFilter('early')"
        >
          <text class="tab-text">早退</text>
          <text class="tab-count" v-if="recordCount.early > 0">{{ recordCount.early }}</text>
        </view>
      </view>
    </view>

    <!-- 统计摘要 -->
    <view class="summary-cards">
      <view class="summary-item">
        <text class="summary-value">{{ summaryStats.workDays }}</text>
        <text class="summary-label">出勤天数</text>
      </view>
      <view class="summary-item">
        <text class="summary-value">{{ summaryStats.lateCount }}</text>
        <text class="summary-label">迟到次数</text>
      </view>
      <view class="summary-item">
        <text class="summary-value">{{ summaryStats.earlyCount }}</text>
        <text class="summary-label">早退次数</text>
      </view>
      <view class="summary-item">
        <text class="summary-value">{{ summaryStats.absentCount }}</text>
        <text class="summary-label">缺卡次数</text>
      </view>
    </view>

    <!-- 打卡记录列表 -->
    <scroll-view
      class="records-list"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 按日期分组的记录 -->
      <view class="date-group" v-for="group in groupedRecords" :key="group.date">
        <view class="date-header">
          <text class="date-text">{{ group.dateText }}</text>
          <text class="date-weekday">{{ group.weekday }}</text>
        </view>

        <view class="record-card">
          <!-- 上班打卡 -->
          <view class="punch-item" v-if="group.morning">
            <view class="punch-info">
              <text class="punch-label">上班</text>
              <text class="punch-time" :class="getPunchTimeClass(group.morning.status)">
                {{ group.morning.time || '--:--' }}
              </text>
              <text class="punch-status" :class="getStatusClass(group.morning.status)">
                {{ getStatusText(group.morning.status) }}
              </text>
            </view>
            <view class="punch-location" v-if="group.morning.location">
              <text class="location-icon">📍</text>
              <text class="location-text">{{ group.morning.location }}</text>
            </view>
          </view>

          <!-- 下班打卡 -->
          <view class="punch-item" v-if="group.evening">
            <view class="punch-info">
              <text class="punch-label">下班</text>
              <text class="punch-time" :class="getPunchTimeClass(group.evening.status)">
                {{ group.evening.time || '--:--' }}
              </text>
              <text class="punch-status" :class="getStatusClass(group.evening.status)">
                {{ getStatusText(group.evening.status) }}
              </text>
            </view>
            <view class="punch-location" v-if="group.evening.location">
              <text class="location-icon">📍</text>
              <text class="location-text">{{ group.evening.location }}</text>
            </view>
          </view>

          <!-- 缺卡提示 -->
          <view class="absent-tip" v-if="!group.morning || !group.evening">
            <text class="tip-icon">⚠️</text>
            <text class="tip-text">{{ !group.morning ? '缺上班打卡' : '缺下班打卡' }}</text>
            <button class="repair-btn" @click="goToRepair(group.date)">补卡</button>
          </view>
        </view>
      </view>

      <!-- 加载更多提示 -->
      <view class="load-more" v-if="hasMore">
        <text class="load-text">{{ loading ? '加载中...' : '上拉加载更多' }}</text>
      </view>

      <!-- 没有更多数据 -->
      <view class="no-more" v-if="!hasMore && recordList.length > 0">
        <text class="no-more-text">没有更多数据了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="recordList.length === 0 && !loading">
        <text class="empty-icon">📭</text>
        <text class="empty-text">本月暂无打卡记录</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <button class="action-btn export-btn" @click="exportRecords">
        <text class="btn-icon">📥</text>
        <text class="btn-text">导出记录</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

// ==================== 响应式数据 ====================
const currentYear = ref(dayjs().format('YYYY'))
const currentMonth = ref(dayjs().format('M'))
const monthList = ref([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12])

const activeFilter = ref('all')
const recordList = ref([])
const summaryStats = reactive({
  workDays: 0,
  lateCount: 0,
  earlyCount: 0,
  absentCount: 0
})

const recordCount = reactive({
  all: 0,
  normal: 0,
  abnormal: 0,
  late: 0,
  early: 0
})

const refreshing = ref(false)
const loading = ref(false)
const hasMore = ref(true)

const pagination = reactive({
  pageNum: 1,
  pageSize: 20
})

const employeeId = ref(1001) // TODO: 从用户信息获取

// ==================== 计算属性 ====================
/**
 * 按日期分组的记录
 */
const groupedRecords = computed(() => {
  const groups = []
  const map = new Map()

  recordList.value.forEach(record => {
    const dateKey = record.date
    if (!map.has(dateKey)) {
      map.set(dateKey, {
        date: dateKey,
        dateText: formatDate(dateKey),
        weekday: getWeekday(dateKey),
        morning: null,
        evening: null
      })
    }

    const group = map.get(dateKey)
    if (record.punchType === 'IN') {
      group.morning = record
    } else if (record.punchType === 'OUT') {
      group.evening = record
    }
  })

  return Array.from(map.values()).sort((a, b) => b.date.localeCompare(a.date))
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadRecords()
  loadSummaryStats()
})

// ==================== 数据加载 ====================
/**
 * 加载打卡记录
 */
const loadRecords = async (refresh = false) => {
  if (refresh) {
    pagination.pageNum = 1
    hasMore.value = true
  }

  if (loading.value) return
  if (!hasMore.value && !refresh) return

  loading.value = true

  try {
    const startDate = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .startOf('month')
      .format('YYYY-MM-DD')

    const endDate = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .endOf('month')
      .format('YYYY-MM-DD')

    const res = await attendanceApi.punchApi.getPunchRecords({
      employeeId: employeeId.value,
      startDate,
      endDate,
      pageSize: pagination.pageSize,
      pageNum: pagination.pageNum,
      status: activeFilter.value === 'all' ? undefined : activeFilter.value
    })

    if (res.success && res.data) {
      const newRecords = res.data.list || []

      if (refresh) {
        recordList.value = newRecords
      } else {
        recordList.value = [...recordList.value, ...newRecords]
      }

      // 更新分页状态
      hasMore.value = newRecords.length >= pagination.pageSize
      pagination.pageNum++

      // 更新记录计数
      updateRecordCount(res.data.total || 0)
    }
  } catch (error) {
    console.error('[打卡记录] 加载失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 加载统计数据
 */
const loadSummaryStats = async () => {
  try {
    const startDate = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .startOf('month')
      .format('YYYY-MM-DD')

    const endDate = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .endOf('month')
      .format('YYYY-MM-DD')

    const res = await attendanceApi.statisticsApi.getPersonalStatistics({
      employeeId: employeeId.value,
      startDate,
      endDate
    })

    if (res.success && res.data) {
      const stats = res.data
      summaryStats.workDays = stats.workDays || 0
      summaryStats.lateCount = stats.lateCount || 0
      summaryStats.earlyCount = stats.earlyCount || 0
      summaryStats.absentCount = stats.absentCount || 0

      // 更新筛选计数
      recordCount.normal = stats.normalCount || 0
      recordCount.abnormal = stats.abnormalCount || 0
      recordCount.late = stats.lateCount || 0
      recordCount.early = stats.earlyCount || 0
      recordCount.all = stats.totalCount || 0
    }
  } catch (error) {
    console.error('[打卡记录] 统计数据加载失败:', error)
  }
}

/**
 * 更新记录计数
 */
const updateRecordCount = (total) => {
  recordCount.all = total
  // TODO: 根据筛选条件更新其他计数
}

// ==================== 交互操作 ====================
/**
 * 选择月份
 */
const selectMonth = (month) => {
  currentMonth.value = month.toString()
  loadRecords(true)
  loadSummaryStats()
}

/**
 * 设置筛选条件
 */
const setFilter = (filter) => {
  activeFilter.value = filter
  loadRecords(true)
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadRecords(true)
  loadSummaryStats()
}

/**
 * 上拉加载更多
 */
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    loadRecords()
  }
}

/**
 * 跳转到补卡页面
 */
const goToRepair = (date) => {
  uni.navigateTo({
    url: `/pages/attendance/repair?date=${date}`
  })
}

/**
 * 导出记录
 */
const exportRecords = () => {
  uni.showToast({
    title: '导出功能开发中',
    icon: 'none'
  })

  // TODO: 实现导出功能
  // 1. 调用后端导出接口
  // 2. 下载Excel文件
  // 3. 打开文件
}

// ==================== 工具函数 ====================
/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  return dayjs(dateStr).format('MM月DD日')
}

/**
 * 获取星期几
 */
const getWeekday = (dateStr) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[dayjs(dateStr).day()]
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    'NORMAL': '正常',
    'LATE': '迟到',
    'EARLY': '早退',
    'ABSENT': '缺卡',
    'LEAVE': '请假',
    'OVERTIME': '加班'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    'NORMAL': 'status-normal',
    'LATE': 'status-late',
    'EARLY': 'status-early',
    'ABSENT': 'status-absent',
    'LEAVE': 'status-leave',
    'OVERTIME': 'status-overtime'
  }
  return classMap[status] || ''
}

/**
 * 获取打卡时间样式类
 */
const getPunchTimeClass = (status) => {
  if (status === 'LATE' || status === 'EARLY') {
    return 'time-abnormal'
  }
  return ''
}
</script>

<style lang="scss" scoped>
.records-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

// 日期选择器
.date-selector {
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .selector-header {
    display: flex;
    align-items: baseline;
    margin-bottom: 20rpx;

    .current-year {
      font-size: 28rpx;
      color: #666;
      margin-right: 10rpx;
    }

    .current-month {
      font-size: 40rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .month-list {
    .month-scroll {
      white-space: nowrap;

      .month-item {
        display: inline-block;
        padding: 12rpx 24rpx;
        margin-right: 12rpx;
        border-radius: 20rpx;
        background-color: #f5f5f5;
        transition: all 0.3s ease;

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        .month-text {
          font-size: 26rpx;
        }
      }
    }
  }
}

// 筛选器
.filter-bar {
  background: white;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;

  .filter-tabs {
    display: flex;

    .filter-tab {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16rpx 0;
      position: relative;

      &.active {
        .tab-text {
          color: #667eea;
          font-weight: bold;
        }

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 40rpx;
          height: 4rpx;
          background: #667eea;
          border-radius: 2rpx;
        }
      }

      .tab-text {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 4rpx;
      }

      .tab-count {
        font-size: 20rpx;
        color: #999;
      }
    }
  }
}

// 统计摘要
.summary-cards {
  display: flex;
  justify-content: space-around;
  background: white;
  padding: 30rpx;
  margin-bottom: 20rpx;

  .summary-item {
    display: flex;
    flex-direction: column;
    align-items: center;

    .summary-value {
      font-size: 36rpx;
      font-weight: bold;
      color: #667eea;
      margin-bottom: 8rpx;
    }

    .summary-label {
      font-size: 22rpx;
      color: #999;
    }
  }
}

// 记录列表
.records-list {
  height: calc(100vh - 500rpx);

  .date-group {
    margin-bottom: 20rpx;

    .date-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 30rpx;
      background-color: #f5f5f5;

      .date-text {
        font-size: 28rpx;
        color: #333;
        font-weight: bold;
      }

      .date-weekday {
        font-size: 24rpx;
        color: #999;
      }
    }

    .record-card {
      background: white;
      padding: 20rpx 30rpx;

      .punch-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20rpx 0;
        border-bottom: 1rpx solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .punch-info {
          flex: 1;
          display: flex;
          align-items: center;

          .punch-label {
            font-size: 26rpx;
            color: #666;
            width: 80rpx;
          }

          .punch-time {
            font-size: 32rpx;
            font-weight: bold;
            color: #333;
            margin-right: 20rpx;

            &.time-abnormal {
              color: #ef4444;
            }
          }

          .punch-status {
            font-size: 22rpx;
            padding: 4rpx 12rpx;
            border-radius: 8rpx;

            &.status-normal {
              background-color: #f0fdf4;
              color: #22c55e;
            }

            &.status-late {
              background-color: #fef2f2;
              color: #ef4444;
            }

            &.status-early {
              background-color: #fffbeb;
              color: #f59e0b;
            }

            &.status-absent {
              background-color: #fef2f2;
              color: #ef4444;
            }

            &.status-leave {
              background-color: #eff6ff;
              color: #3b82f6;
            }

            &.status-overtime {
              background-color: #f3e8ff;
              color: #a855f7;
            }
          }
        }

        .punch-location {
          display: flex;
          align-items: center;
          font-size: 22rpx;
          color: #999;

          .location-icon {
            margin-right: 6rpx;
          }

          .location-text {
            max-width: 200rpx;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }

      .absent-tip {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20rpx 0;

        .tip-icon {
          font-size: 24rpx;
          margin-right: 10rpx;
        }

        .tip-text {
          flex: 1;
          font-size: 24rpx;
          color: #ef4444;
        }

        .repair-btn {
          padding: 8rpx 20rpx;
          background-color: #667eea;
          color: white;
          border-radius: 8rpx;
          font-size: 22rpx;
          border: none;
        }
      }
    }
  }

  .load-more {
    padding: 30rpx;
    text-align: center;

    .load-text {
      font-size: 24rpx;
      color: #999;
    }
  }

  .no-more {
    padding: 30rpx;
    text-align: center;

    .no-more-text {
      font-size: 24rpx;
      color: #ccc;
    }
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 120rpx 0;

    .empty-icon {
      font-size: 80rpx;
      margin-bottom: 20rpx;
      opacity: 0.5;
    }

    .empty-text {
      font-size: 26rpx;
      color: #999;
    }
  }
}

// 底部操作栏
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  padding: 20rpx;
  background-color: white;
  border-top: 1rpx solid #f0f0f0;

  .action-btn {
    flex: 1;
    max-width: 400rpx;
    height: 80rpx;
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;

    &.export-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;

      .btn-icon {
        font-size: 28rpx;
        margin-right: 8rpx;
      }

      .btn-text {
        font-size: 28rpx;
        font-weight: bold;
      }
    }
  }
}
</style>
