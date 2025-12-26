<template>
  <view class="overtime-container">
    <!-- 顶部标签页 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'apply' }"
        @click="switchTab('apply')"
      >
        <text class="tab-text">加班申请</text>
      </view>
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'records' }"
        @click="switchTab('records')"
      >
        <text class="tab-text">加班记录</text>
      </view>
    </view>

    <!-- 申请表单 -->
    <view class="apply-form" v-if="activeTab === 'apply'">
      <!-- 加班日期 -->
      <view class="form-item">
        <text class="item-label">加班日期</text>
        <picker
          mode="date"
          :value="overtimeForm.overtimeDate"
          :start="minDate"
          :end="maxDate"
          @change="onDateChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !overtimeForm.overtimeDate }">
              {{ overtimeForm.overtimeDate || '请选择加班日期' }}
            </text>
            <text class="calendar-icon">📅</text>
          </view>
        </picker>
      </view>

      <!-- 加班类型 -->
      <view class="form-item">
        <text class="item-label">加班类型</text>
        <view class="type-selector">
          <view
            class="type-option"
            :class="{ 'active': overtimeForm.overtimeType === 'workday' }"
            @click="selectOvertimeType('workday')"
          >
            <text class="type-icon">💼</text>
            <text class="type-text">工作日</text>
          </view>
          <view
            class="type-option"
            :class="{ 'active': overtimeForm.overtimeType === 'weekend' }"
            @click="selectOvertimeType('weekend')"
          >
            <text class="type-icon">🌟</text>
            <text class="type-text">周末</text>
          </view>
          <view
            class="type-option"
            :class="{ 'active': overtimeForm.overtimeType === 'holiday' }"
            @click="selectOvertimeType('holiday')"
          >
            <text class="type-icon">🎉</text>
            <text class="type-text">节假日</text>
          </view>
        </view>
      </view>

      <!-- 加班时长显示 -->
      <view class="form-item">
        <text class="item-label">加班时长</text>
        <view class="duration-display">
          <text class="duration-value">{{ calculatedDuration }}</text>
          <text class="duration-unit">小时</text>
        </view>
      </view>

      <!-- 开始时间 -->
      <view class="form-item">
        <text class="item-label">开始时间</text>
        <picker
          mode="time"
          :value="overtimeForm.startTime"
          @change="onStartTimeChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !overtimeForm.startTime }">
              {{ overtimeForm.startTime || '请选择开始时间' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 结束时间 -->
      <view class="form-item">
        <text class="item-label">结束时间</text>
        <picker
          mode="time"
          :value="overtimeForm.endTime"
          @change="onEndTimeChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !overtimeForm.endTime }">
              {{ overtimeForm.endTime || '请选择结束时间' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 加班原因 -->
      <view class="form-item reason-item">
        <text class="item-label">加班原因</text>
        <textarea
          class="reason-input"
          v-model="overtimeForm.reason"
          placeholder="请简要说明加班原因（如：项目紧急、客户需求等）"
          maxlength="200"
          :show-confirm-bar="false"
        ></textarea>
        <text class="char-count">{{ overtimeForm.reason.length }}/200</text>
      </view>

      <!-- 加班福利 -->
      <view class="form-item benefits-item">
        <text class="item-label">加班福利</text>
        <view class="benefits-list">
          <view
            class="benefit-option"
            :class="{ 'active': overtimeForm.provideMeal }"
            @click="toggleMeal"
          >
            <text class="benefit-icon">🍱️</text>
            <text class="benefit-text">加班餐</text>
            <text class="check-icon" v-if="overtimeForm.provideMeal">✓</text>
          </view>
          <view
            class="benefit-option"
            :class="{ 'active': overtimeForm.provideTransport }"
            @click="toggleTransport"
          >
            <text class="benefit-icon">🚗</text>
            <text class="benefit-text">加班车</text>
            <text class="check-icon" v-if="overtimeForm.provideTransport">✓</text>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-item remark-item">
        <text class="item-label">备注</text>
        <textarea
          class="remark-input"
          v-model="overtimeForm.remark"
          placeholder="其他需要说明的事项（选填）"
          maxlength="100"
          :show-confirm-bar="false"
        ></textarea>
        <text class="char-count">{{ overtimeForm.remark.length }}/100</text>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button
          class="submit-btn"
          :disabled="!canSubmit"
          @click="submitOvertime"
        >
          <text class="btn-text">{{ submitting ? '提交中...' : '提交申请' }}</text>
        </button>
      </view>

      <!-- 加班须知 -->
      <view class="notice-section">
        <view class="notice-title">
          <text class="title-icon">📌</text>
          <text class="title-text">加班须知</text>
        </view>
        <view class="notice-content">
          <text class="notice-item">1. 加班申请需提前1天提交，特殊情况可当天申请</text>
          <text class="notice-item">2. 工作日加班按1.5倍计算，周末按2倍，节假日按3倍</text>
          <text class="notice-item">3. 加班时长需≥2小时才能申请调休</text>
          <text class="notice-item">4. 加班餐和加班车需提前申请</text>
        </view>
      </view>
    </view>

    <!-- 加班记录 -->
    <view class="records-list" v-if="activeTab === 'records'">
      <!-- 统计卡片 -->
      <view class="stats-cards">
        <view class="stat-item">
          <text class="stat-value">{{ stats.totalHours }}</text>
          <text class="stat-label">总加班(小时)</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ stats.thisMonth }}</text>
          <text class="stat-label">本月(小时)</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ stats.pendingCount }}</text>
          <text class="stat-label">待审批</text>
        </view>
      </view>

      <!-- 筛选器 -->
      <view class="filter-bar">
        <view class="filter-tabs">
          <view
            class="filter-tab"
            :class="{ 'active': recordFilter === 'all' }"
            @click="setRecordFilter('all')"
          >
            <text class="tab-text">全部</text>
          </view>
          <view
            class="filter-tab"
            :class="{ 'active': recordFilter === 'pending' }"
            @click="setRecordFilter('pending')"
          >
            <text class="tab-text">待审批</text>
          </view>
          <view
            class="filter-tab"
            :class="{ 'active': recordFilter === 'approved' }"
            @click="setRecordFilter('approved')"
          >
            <text class="tab-text">已通过</text>
          </view>
        </view>
      </view>

      <!-- 记录列表 -->
      <scroll-view
        class="list-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshingRecords"
        @refresherrefresh="refreshRecords"
        @scrolltolower="loadMoreRecords"
      >
        <view class="record-card" v-for="record in overtimeRecords" :key="record.id">
          <!-- 卡片头部 -->
          <view class="card-header">
            <view class="header-left">
              <text class="overtime-date">{{ formatDate(record.overtimeDate) }}</text>
              <text class="overtime-type" :class="getTypeClass(record.overtimeType)">
                {{ getTypeText(record.overtimeType) }}
              </text>
            </view>
            <text class="record-status" :class="getStatusClass(record.status)">
              {{ getStatusText(record.status) }}
            </text>
          </view>

          <!-- 时间信息 -->
          <view class="time-info">
            <view class="time-row">
              <text class="time-label">加班时间:</text>
              <text class="time-value">{{ record.startTime }} - {{ record.endTime }}</text>
            </view>
            <view class="time-row">
              <text class="time-label">加班时长:</text>
              <text class="time-value">{{ record.duration }}小时</text>
            </view>
          </view>

          <!-- 加班原因 -->
          <view class="reason-info" v-if="record.reason">
            <text class="reason-label">加班原因:</text>
            <text class="reason-text">{{ record.reason }}</text>
          </view>

          <!-- 福利信息 -->
          <view class="benefits-info" v-if="record.provideMeal || record.provideTransport">
            <text class="benefits-label">福利:</text>
            <text class="benefit-tag" v-if="record.provideMeal">加班餐</text>
            <text class="benefit-tag" v-if="record.provideTransport">加班车</text>
          </view>

          <!-- 操作按钮 -->
          <view class="card-actions" v-if="record.status === 'PENDING'">
            <button class="action-btn cancel-btn" @click="cancelOvertime(record.id)">
              <text class="btn-text">撤销申请</text>
            </button>
          </view>
        </view>

        <!-- 加载提示 -->
        <view class="load-more" v-if="hasMoreRecords">
          <text class="load-text">{{ loadingRecords ? '加载中...' : '上拉加载更多' }}</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="overtimeRecords.length === 0 && !loadingRecords">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无加班记录</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

// ==================== 响应式数据 ====================
const activeTab = ref('apply')

// 加班表单
const overtimeForm = reactive({
  overtimeDate: '',
  overtimeType: 'workday',
  startTime: '',
  endTime: '',
  reason: '',
  provideMeal: false,
  provideTransport: false,
  remark: ''
})

// 日期范围
const minDate = dayjs().format('YYYY-MM-DD')
const maxDate = dayjs().add(30, 'day').format('YYYY-MM-DD')

// 提交状态
const submitting = ref(false)

// 加班记录
const overtimeRecords = ref([])
const recordFilter = ref('all')
const refreshingRecords = ref(false)
const loadingRecords = ref(false)
const hasMoreRecords = ref(true)

// 统计数据
const stats = reactive({
  totalHours: 0,
  thisMonth: 0,
  pendingCount: 0
})

const recordPagination = reactive({
  pageNum: 1,
  pageSize: 20
})

const employeeId = ref(1001) // TODO: 从用户信息获取

// ==================== 计算属性 ====================
/**
 * 计算加班时长
 */
const calculatedDuration = computed(() => {
  if (!overtimeForm.startTime || !overtimeForm.endTime) {
    return 0
  }

  const start = dayjs(`${overtimeForm.overtimeDate} ${overtimeForm.startTime}`)
  const end = dayjs(`${overtimeForm.overtimeDate} ${overtimeForm.endTime}`)

  if (end.isBefore(start)) {
    return 0
  }

  const hours = end.diff(start, 'hour', true)
  return hours.toFixed(1)
})

/**
 * 是否可以提交
 */
const canSubmit = computed(() => {
  return (
    overtimeForm.overtimeDate &&
    overtimeForm.overtimeType &&
    overtimeForm.startTime &&
    overtimeForm.endTime &&
    overtimeForm.reason &&
    calculatedDuration.value >= 2 // 至少2小时
  )
})

// ==================== 生命周期 ====================
onMounted(() => {
  loadOvertimeRecords()
  loadStats()
})

// ==================== 事件处理 ====================
/**
 * 切换标签页
 */
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'records') {
    loadOvertimeRecords()
    loadStats()
  }
}

/**
 * 日期变更
 */
const onDateChange = (e) => {
  overtimeForm.overtimeDate = e.detail.value
}

/**
 * 选择加班类型
 */
const selectOvertimeType = (type) => {
  overtimeForm.overtimeType = type
}

/**
 * 开始时间变更
 */
const onStartTimeChange = (e) => {
  overtimeForm.startTime = e.detail.value
}

/**
 * 结束时间变更
 */
const onEndTimeChange = (e) => {
  overtimeForm.endTime = e.detail.value
}

/**
 * 切换加班餐
 */
const toggleMeal = () => {
  overtimeForm.provideMeal = !overtimeForm.provideMeal
}

/**
 * 切换加班车
 */
const toggleTransport = () => {
  overtimeForm.provideTransport = !overtimeForm.provideTransport
}

/**
 * 提交加班申请
 */
const submitOvertime = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请完善加班信息',
      icon: 'none'
    })
    return
  }

  submitting.value = true

  try {
    const res = await attendanceApi.overtimeApi.applyOvertime({
      employeeId: employeeId.value,
      overtimeDate: overtimeForm.overtimeDate,
      overtimeType: overtimeForm.overtimeType,
      startTime: overtimeForm.startTime,
      endTime: overtimeForm.endTime,
      duration: calculatedDuration.value,
      reason: overtimeForm.reason,
      provideMeal: overtimeForm.provideMeal,
      provideTransport: overtimeForm.provideTransport,
      remark: overtimeForm.remark
    })

    if (res.success) {
      uni.showToast({
        title: '申请提交成功',
        icon: 'success'
      })

      // 重置表单
      resetForm()

      // 切换到记录标签页
      setTimeout(() => {
        switchTab('records')
      }, 1500)
    } else {
      uni.showToast({
        title: res.message || '提交失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('[加班申请] 提交失败:', error)
    uni.showToast({
      title: '提交失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  overtimeForm.overtimeDate = ''
  overtimeForm.overtimeType = 'workday'
  overtimeForm.startTime = ''
  overtimeForm.endTime = ''
  overtimeForm.reason = ''
  overtimeForm.provideMeal = false
  overtimeForm.provideTransport = false
  overtimeForm.remark = ''
}

// ==================== 记录列表 ====================
/**
 * 加载加班记录
 */
const loadOvertimeRecords = async (refresh = false) => {
  if (refresh) {
    recordPagination.pageNum = 1
    hasMoreRecords.value = true
  }

  if (loadingRecords.value) return
  if (!hasMoreRecords.value && !refresh) return

  loadingRecords.value = true

  try {
    const res = await attendanceApi.overtimeApi.getOvertimeRecords({
      employeeId: employeeId.value,
      status: recordFilter.value === 'all' ? undefined : recordFilter.value,
      pageSize: recordPagination.pageSize,
      pageNum: recordPagination.pageNum
    })

    if (res.success && res.data) {
      const newRecords = res.data.list || []

      if (refresh) {
        overtimeRecords.value = newRecords
      } else {
        overtimeRecords.value = [...overtimeRecords.value, ...newRecords]
      }

      hasMoreRecords.value = newRecords.length >= recordPagination.pageSize
      recordPagination.pageNum++
    }
  } catch (error) {
    console.error('[加班记录] 加载失败:', error)
  } finally {
    loadingRecords.value = false
    refreshingRecords.value = false
  }
}

/**
 * 加载统计数据
 */
const loadStats = async () => {
  try {
    const startDate = dayjs().startOf('month').format('YYYY-MM-DD')
    const endDate = dayjs().endOf('month').format('YYYY-MM-DD')

    const res = await attendanceApi.statisticsApi.getPersonalStatistics({
      employeeId: employeeId.value,
      startDate,
      endDate
    })

    if (res.success && res.data) {
      stats.totalHours = res.data.overtimeHours || 0
      stats.thisMonth = res.data.thisMonthOvertime || 0
      stats.pendingCount = res.data.pendingOvertime || 0
    }
  } catch (error) {
    console.error('[加班统计] 加载失败:', error)
  }
}

/**
 * 刷新记录
 */
const refreshRecords = () => {
  refreshingRecords.value = true
  loadOvertimeRecords(true)
  loadStats()
}

/**
 * 加载更多记录
 */
const loadMoreRecords = () => {
  if (hasMoreRecords.value && !loadingRecords.value) {
    loadOvertimeRecords()
  }
}

/**
 * 设置记录筛选
 */
const setRecordFilter = (filter) => {
  recordFilter.value = filter
  loadOvertimeRecords(true)
}

/**
 * 撤销加班申请
 */
const cancelOvertime = async (overtimeId) => {
  uni.showModal({
    title: '确认撤销',
    content: '确定要撤销此加班申请吗？',
    success: async (res) => {
      if (res.confirm) {
        // TODO: 调用撤销接口
        uni.showToast({
          title: '撤销成功',
          icon: 'success'
        })
        loadOvertimeRecords(true)
      }
    }
  })
}

// ==================== 工具函数 ====================
/**
 * 格式化日期
 */
const formatDate = (dateStr) => {
  return dayjs(dateStr).format('MM月DD日')
}

/**
 * 获取加班类型文本
 */
const getTypeText = (type) => {
  const typeMap = {
    'workday': '工作日',
    'weekend': '周末',
    'holiday': '节假日'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取加班类型样式类
 */
const getTypeClass = (type) => {
  const classMap = {
    'workday': 'type-workday',
    'weekend': 'type-weekend',
    'holiday': 'type-holiday'
  }
  return classMap[type] || ''
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    'PENDING': 'status-pending',
    'APPROVED': 'status-approved',
    'REJECTED': 'status-rejected'
  }
  return classMap[status] || ''
}
</script>

<style lang="scss" scoped>
.overtime-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

// 标签页
.tabs {
  display: flex;
  background: white;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .tab-item {
    flex: 1;
    text-align: center;
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
        width: 60rpx;
        height: 4rpx;
        background: #667eea;
        border-radius: 2rpx;
      }
    }

    .tab-text {
      font-size: 28rpx;
      color: #666;
    }
  }
}

// 申请表单
.apply-form {
  padding: 30rpx;

  .form-item {
    background: white;
    padding: 30rpx;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    .item-label {
      display: block;
      font-size: 28rpx;
      color: #333;
      font-weight: bold;
      margin-bottom: 20rpx;
    }

    .picker-value {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .value-text {
        font-size: 28rpx;
        color: #333;

        &.placeholder {
          color: #999;
        }
      }

      .arrow, .calendar-icon {
        font-size: 32rpx;
        color: #999;
      }
    }

    .type-selector {
      display: flex;
      justify-content: space-around;

      .type-option {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 20rpx;
        border: 2rpx solid #f0f0f0;
        border-radius: 10rpx;
        margin: 0 10rpx;

        &:first-child {
          margin-left: 0;
        }

        &:last-child {
          margin-right: 0;
        }

        &.active {
          border-color: #667eea;
          background-color: #f0f9ff;

          .type-icon {
            transform: scale(1.1);
          }
        }

        .type-icon {
          font-size: 36rpx;
          margin-bottom: 10rpx;
        }

        .type-text {
          font-size: 24rpx;
          color: #666;
        }
      }
    }

    .duration-display {
      display: flex;
      align-items: baseline;
      justify-content: center;
      padding: 30rpx 0;

      .duration-value {
        font-size: 60rpx;
        font-weight: bold;
        color: #667eea;
      }

      .duration-unit {
        font-size: 28rpx;
        color: #666;
        margin-left: 10rpx;
      }
    }

    &.reason-item, &.remark-item {
      .reason-input, .remark-input {
        width: 100%;
        min-height: 120rpx;
        padding: 20rpx;
        background-color: #f5f5f5;
        border-radius: 10rpx;
        font-size: 28rpx;
        color: #333;
      }

      .char-count {
        display: block;
        text-align: right;
        font-size: 22rpx;
        color: #999;
        margin-top: 10rpx;
      }
    }

    &.benefits-item {
      .benefits-list {
        display: flex;
        justify-content: space-around;

        .benefit-option {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          padding: 20rpx;
          border: 2rpx solid #f0f0f0;
          border-radius: 10rpx;
          margin: 0 10rpx;
          position: relative;

          &:first-child {
            margin-left: 0;
          }

          &:last-child {
            margin-right: 0;
          }

          &.active {
            border-color: #22c55e;
            background-color: #f0fdf4;
          }

          .benefit-icon {
            font-size: 36rpx;
            margin-bottom: 10rpx;
          }

          .benefit-text {
            font-size: 24rpx;
            color: #666;
          }

          .check-icon {
            position: absolute;
            top: 5rpx;
            right: 5rpx;
            font-size: 20rpx;
            color: #22c55e;
          }
        }
      }
    }
  }

  .submit-section {
    margin-top: 40rpx;

    .submit-btn {
      width: 100%;
      height: 90rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 45rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border: none;
      font-size: 32rpx;
      font-weight: bold;

      &:disabled {
        opacity: 0.5;
      }

      .btn-text {
        color: white;
      }
    }
  }

  .notice-section {
    background: white;
    padding: 30rpx;
    border-radius: 16rpx;
    margin-top: 30rpx;

    .notice-title {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;

      .title-icon {
        font-size: 24rpx;
        margin-right: 10rpx;
      }

      .title-text {
        font-size: 26rpx;
        font-weight: bold;
        color: #333;
      }
    }

    .notice-content {
      .notice-item {
        display: block;
        font-size: 24rpx;
        color: #666;
        line-height: 1.8;
        margin-bottom: 10rpx;
      }
    }
  }
}

// 记录列表
.records-list {
  height: calc(100vh - 120rpx);

  .stats-cards {
    display: flex;
    justify-content: space-around;
    background: white;
    padding: 30rpx;
    margin-bottom: 20rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 36rpx;
        font-weight: bold;
        color: #667eea;
        margin-bottom: 8rpx;
      }

      .stat-label {
        font-size: 22rpx;
        color: #999;
      }
    }
  }

  .filter-bar {
    background: white;
    padding: 20rpx 30rpx;
    margin-bottom: 20rpx;

    .filter-tabs {
      display: flex;

      .filter-tab {
        flex: 1;
        text-align: center;
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
        }
      }
    }
  }

  .list-scroll {
    height: calc(100% - 350rpx);
    padding: 30rpx;

    .record-card {
      background: white;
      padding: 30rpx;
      border-radius: 16rpx;
      margin-bottom: 20rpx;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;

        .header-left {
          display: flex;
          align-items: center;

          .overtime-date {
            font-size: 28rpx;
            font-weight: bold;
            color: #333;
            margin-right: 15rpx;
          }

          .overtime-type {
            font-size: 20rpx;
            padding: 4rpx 12rpx;
            border-radius: 10rpx;

            &.type-workday {
              background-color: #eff6ff;
              color: #3b82f6;
            }

            &.type-weekend {
              background-color: #fef3c7;
              color: #f59e0b;
            }

            &.type-holiday {
              background-color: #fce7f3;
              color: #ec4899;
            }
          }
        }

        .record-status {
          font-size: 22rpx;
          padding: 6rpx 16rpx;
          border-radius: 20rpx;

          &.status-pending {
            background-color: #fffbeb;
            color: #f59e0b;
          }

          &.status-approved {
            background-color: #f0fdf4;
            color: #22c55e;
          }

          &.status-rejected {
            background-color: #fef2f2;
            color: #ef4444;
          }
        }
      }

      .time-info {
        margin-bottom: 20rpx;

        .time-row {
          display: flex;
          margin-bottom: 10rpx;

          .time-label {
            font-size: 24rpx;
            color: #999;
            width: 140rpx;
          }

          .time-value {
            font-size: 24rpx;
            color: #333;
          }
        }
      }

      .reason-info {
        margin-bottom: 20rpx;

        .reason-label {
          font-size: 24rpx;
          color: #999;
        }

        .reason-text {
          font-size: 24rpx;
          color: #333;
        }
      }

      .benefits-info {
        margin-bottom: 20rpx;

        .benefits-label {
          font-size: 24rpx;
          color: #999;
          margin-right: 10rpx;
        }

        .benefit-tag {
          display: inline-block;
          font-size: 20rpx;
          padding: 4rpx 12rpx;
          background-color: #f0fdf4;
          color: #22c55e;
          border-radius: 8rpx;
          margin-right: 10rpx;
        }
      }

      .card-actions {
        display: flex;
        justify-content: flex-end;

        .action-btn {
          padding: 10rpx 30rpx;
          border-radius: 20rpx;
          font-size: 24rpx;
          border: none;

          &.cancel-btn {
            background-color: #f3f4f6;
            color: #666;
          }

          .btn-text {
            color: inherit;
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
}
</style>
