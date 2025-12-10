<template>
  <view class="smart-schedule-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">智能排班</text>
        <text class="optimize-btn" @click="optimizeSchedule">优化</text>
      </view>
    </view>

    <!-- 排班概览 -->
    <view class="schedule-overview">
      <view class="overview-header">
        <text class="overview-title">本月排班概览</text>
        <text class="overview-date">{{ currentMonth }}</text>
      </view>
      <view class="overview-stats">
        <view class="stat-item">
          <text class="stat-value">{{ scheduleStats.totalDays }}</text>
          <text class="stat-label">工作天数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ scheduleStats.scheduledDays }}</text>
          <text class="stat-label">已排班</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ scheduleStats.pendingDays }}</text>
          <text class="stat-label">待排班</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ scheduleStats.overtimeDays }}</text>
          <text class="stat-label">加班天数</text>
        </view>
      </view>
    </view>

    <!-- 排班日历 -->
    <view class="schedule-calendar">
      <view class="calendar-header">
        <text class="calendar-title">排班日历</text>
        <view class="calendar-nav">
          <text class="nav-btn" @click="prevMonth">‹</text>
          <text class="month-text">{{ currentMonthText }}</text>
          <text class="nav-btn" @click="nextMonth">›</text>
        </view>
      </view>
      <view class="calendar-weekdays">
        <text class="weekday" v-for="day in weekDays" :key="day">{{ day }}</text>
      </view>
      <view class="calendar-days">
        <view
          v-for="day in calendarDays"
          :key="day.date"
          :class="['calendar-day', {
            'other-month': day.isOtherMonth,
            'today': day.isToday,
            'has-shift': day.hasShift
          }]"
          @click="selectDate(day)"
        >
          <text class="day-number">{{ day.day }}</text>
          <view v-if="day.hasShift" class="shift-indicator" :class="day.shiftType"></view>
        </view>
      </view>
    </view>

    <!-- 今日排班 -->
    <view class="today-schedule" v-if="todayShifts.length > 0">
      <text class="section-title">今日排班</text>
      <view class="shifts-list">
        <view
          v-for="shift in todayShifts"
          :key="shift.id"
          class="shift-item"
          @click="viewShiftDetail(shift)"
        >
          <view class="shift-time">
            <text class="time-text">{{ formatTime(shift.startTime) }}-{{ formatTime(shift.endTime) }}</text>
          <text class="duration">{{ calculateDuration(shift.startTime, shift.endTime) }}</text>
          </view>
          <view class="shift-info">
            <text class="shift-name">{{ shift.shiftName }}</text>
            <text class="shift-location">{{ shift.location }}</text>
          </view>
          <view class="shift-actions">
            <text class="action-text" @click.stop="editShift(shift)">编辑</text>
            <text class="action-text" @click.stop="deleteShift(shift)">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 快速排班 -->
    <view class="quick-schedule">
      <text class="section-title">快速排班</text>
      <view class="quick-actions">
        <view class="action-card" @click="createRegularShift">
          <view class="action-icon">📅</view>
          <text class="action-title">常规排班</text>
          <text class="action-desc">标准班次排班</text>
        </view>
        <view class="action-card" @click="createOvertimeShift">
          <view class="action-icon">⏰</view>
          <text class="action-title">加班排班</text>
          <text class="action-desc">加班时间安排</text>
        </view>
        <view class="action-card" @click="batchSchedule">
          <view class="action-icon">📋</view>
          <text class="action-title">批量排班</text>
          <text class="action-desc">多人排班</text>
        </view>
        <view class="action-card" @click="copyPreviousWeek">
          <view class="action-icon">📑</view>
          <text class="action-title">复制上周</text>
          <text class="action-desc">快速复用</text>
        </view>
      </view>
    </view>

    <!-- 排班冲突检测 -->
    <view class="conflict-detection" v-if="conflicts.length > 0">
      <text class="section-title">冲突提醒</text>
      <view class="conflicts-list">
        <view
          v-for="conflict in conflicts"
          :key="conflict.id"
          class="conflict-item"
          @click="resolveConflict(conflict)"
        >
          <view class="conflict-icon">⚠️</view>
          <view class="conflict-content">
            <text class="conflict-title">{{ conflict.title }}</text>
            <text class="conflict-desc">{{ conflict.description }}</text>
            <text class="conflict-time">{{ formatDateTime(conflict.time) }}</text>
          </view>
          <view class="conflict-action">
            <text class="action-text">解决</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 智能建议 -->
    <view class="ai-suggestions">
      <text class="section-title">智能建议</text>
      <view class="suggestions-list">
        <view
          v-for="suggestion in aiSuggestions"
          :key="suggestion.id"
          class="suggestion-item"
          @click="applySuggestion(suggestion)"
        >
          <view class="suggestion-icon">💡</view>
          <view class="suggestion-content">
            <text class="suggestion-title">{{ suggestion.title }}</text>
            <text class="suggestion-desc">{{ suggestion.description }}</text>
            <view class="suggestion-impact">
              <text class="impact-label">预计影响：</text>
              <text class="impact-value">{{ suggestion.impact }}</text>
            </view>
          </view>
          <view class="suggestion-action">
            <text class="action-text">应用</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import attendanceApi from '@/api/business/attendance/attendance-api.js'
import dayjs from 'dayjs'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const userStore = useUserStore()

// 页面状态
const loading = ref(false)
const selectedDate = ref(new Date())
const currentMonth = ref(dayjs().format('YYYY年MM月'))
const currentMonthText = ref(dayjs().format('YYYY年MM月'))

// 排班统计
const scheduleStats = reactive({
  totalDays: 22,
  scheduledDays: 18,
  pendingDays: 4,
  overtimeDays: 3
})

// 日历数据
const weekDays = ['日', '一', '二', '三', '四', '五', '六']
const calendarDays = ref([])

// 排班数据
const todayShifts = ref([])
const conflicts = ref([])
const aiSuggestions = ref([])

// 计算属性
const today = computed(() => {
  return dayjs().format('YYYY-MM-DD')
})

// 页面生命周期
onMounted(() => {
  init()
})

onShow(() => {
  loadScheduleData()
})

// 初始化
const init = async () => {
  await generateCalendarDays()
  await loadScheduleData()
  await loadConflicts()
  await loadAISuggestions()
}

// 生成日历天数
const generateCalendarDays = () => {
  const startOfMonth = dayjs(selectedDate.value).startOf('month')
  const endOfMonth = dayjs(selectedDate.value).endOf('month')
  const startWeekday = startOfMonth.day()

  const days = []

  // 添加上个月的天数
  const prevMonthDays = startWeekday
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    const date = startOfMonth.subtract(i + 1, 'day')
    days.push({
      date: date.format('YYYY-MM-DD'),
      day: date.date(),
      isOtherMonth: true,
      isToday: false,
      hasShift: false,
      shiftType: null
    })
  }

  // 添加当前月的天数
  for (let i = 0; i < endOfMonth.date(); i++) {
    const date = startOfMonth.add(i, 'day')
    days.push({
      date: date.format('YYYY-MM-DD'),
      day: date.date(),
      isOtherMonth: false,
      isToday: date.isSame(dayjs(), 'day'),
      hasShift: false,
      shiftType: null
    })
  }

  // 添加下个月的天数
  const remainingDays = 42 - days.length
  for (let i = 0; i < remainingDays; i++) {
    const date = endOfMonth.add(i + 1, 'day')
    days.push({
      date: date.format('YYYY-MM-DD'),
      day: date.date(),
      isOtherMonth: true,
      isToday: false,
      hasShift: false,
      shiftType: null
    })
  }

  calendarDays.value = days
}

// 加载排班数据
const loadScheduleData = async () => {
  try {
    loading.value = true
    const userId = userStore.userId
    const monthStart = dayjs(selectedDate.value).startOf('month').format('YYYY-MM-DD')
    const monthEnd = dayjs(selectedDate.value).endOf('month').format('YYYY-MM-DD')

    // 并行加载今日排班、排班统计和日历排班
    const [todayRes, statsRes, calendarRes] = await Promise.all([
      attendanceApi.getTodayShifts(userId),
      attendanceApi.getMonthScheduleStats(userId, monthStart, monthEnd),
      attendanceApi.getMonthCalendar(userId, monthStart, monthEnd)
    ])

    if (todayRes.success) {
      todayShifts.value = todayRes.data || []
    }

    if (statsRes.success) {
      Object.assign(scheduleStats, statsRes.data || {})
    }

    if (calendarRes.success) {
      // 更新日历中的排班信息
      updateCalendarShifts(calendarRes.data || [])
    }

  } catch (error) {
    console.error('加载排班数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 加载冲突检测
const loadConflicts = async () => {
  try {
    const userId = userStore.userId
    const res = await attendanceApi.getScheduleConflicts(userId)
    if (res.success) {
      conflicts.value = res.data || []
    }
  } catch (error) {
    console.error('加载冲突失败:', error)
  }
}

// 加载AI建议
const loadAISuggestions = async () => {
  try {
    const userId = userStore.userId
    const res = await attendanceApi.getAISuggestions(userId)
    if (res.success) {
      aiSuggestions.value = res.data || []
    }
  } catch (error) {
    console.error('加载AI建议失败:', error)
  }
}

// 更新日历中的排班信息
const updateCalendarShifts = (calendarData) => {
  calendarData.forEach((item, index) => {
    const calendarDay = calendarDays.value[index]
    if (calendarDay && calendarDay.date === item.date) {
      calendarDay.hasShift = item.hasShift
      calendarDay.shiftType = item.shiftType
    }
  })
}

// 选择日期
const selectDate = (day) => {
  selectedDate.value = dayjs(day.date)
  if (!day.isOtherMonth) {
    // 可以显示该日期的详细排班信息
    showDateDetail(day)
  }
}

// 显示日期详情
const showDateDetail = (day) => {
  uni.navigateTo({
    url: `/pages/attendance/schedule-detail?date=${day.date}`
  })
}

// 上个月
const prevMonth = () => {
  selectedDate.value = dayjs(selectedDate.value).subtract(1, 'month')
  currentMonthText.value = dayjs(selectedDate.value).format('YYYY年MM月')
  generateCalendarDays()
  loadScheduleData()
}

// 下个月
const nextMonth = () => {
  selectedDate.value = dayjs(selectedDate.value).add(1, 'month')
  currentMonthText.value = dayjs(selectedDate.value).format('YYYY年MM月')
  generateCalendarDays()
  loadScheduleData()
}

// 查看班次详情
const viewShiftDetail = (shift) => {
  uni.navigateTo({
    url: `/pages/attendance/shift-detail?shiftId=${shift.id}`
  })
}

// 编辑班次
const editShift = (shift) => {
  uni.navigateTo({
    url: `/pages/attendance/shift-edit?shiftId=${shift.id}`
  })
}

// 删除班次
const deleteShift = (shift) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这个班次吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const deleteRes = await attendanceApi.deleteShift(shift.id)
          if (deleteRes.success) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            loadScheduleData()
          }
        } catch (error) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 创建常规班次
const createRegularShift = () => {
  uni.navigateTo({
    url: '/pages/attendance/shift-create?type=regular'
  })
}

// 创建加班班次
const createOvertimeShift = () => {
  uni.navigateTo({
    url: '/pages/attendance/shift-create?type=overtime'
  })
}

// 批量排班
const batchSchedule = () => {
  uni.navigateTo({
    url: '/pages/attendance/batch-schedule'
  })
}

// 复制上周排班
const copyPreviousWeek = async () => {
  try {
    const userId = userStore.userId
    const res = await attendanceApi.copyPreviousWeekSchedule(userId)
    if (res.success) {
      uni.showToast({ title: '复制成功', icon: 'success' })
      loadScheduleData()
    }
  } catch (error) {
    uni.showToast({ title: '复制失败', icon: 'none' })
  }
}

// 解决冲突
const resolveConflict = (conflict) => {
  uni.navigateTo({
    url: `/pages/attendance/conflict-resolve?conflictId=${conflict.id}`
  })
}

// 应用建议
const applySuggestion = (suggestion) => {
  uni.showModal({
    title: '应用建议',
    content: `确定要应用"${suggestion.title}"建议吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const applyRes = await attendanceApi.applySuggestion(suggestion.id)
          if (applyRes.success) {
            uni.showToast({ title: '应用成功', icon: 'success' })
            loadScheduleData()
          }
        } catch (error) {
          uni.showToast({ title: '应用失败', icon: 'none' })
        }
      }
    }
  })
}

// 优化排班
const optimizeSchedule = async () => {
  try {
    uni.showLoading({ title: '优化中...' })
    const userId = userStore.userId
    const res = await attendanceApi.optimizeSchedule(userId)
    uni.hideLoading()

    if (res.success) {
      uni.showToast({ title: '优化成功', icon: 'success' })
      loadScheduleData()
      loadConflicts()
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '优化失败', icon: 'none' })
  }
}

// 格式化时间
const formatTime = (time) => {
  return dayjs(time).format('HH:mm')
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  return dayjs(datetime).format('YYYY-MM-DD HH:mm')
}

// 计算工作时长
const calculateDuration = (startTime, endTime) => {
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  const duration = end.diff(start, 'minute')

  const hours = Math.floor(duration / 60)
  const minutes = duration % 60
  return `${hours}小时${minutes}分钟`
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.smart-schedule-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  background: #fff;
}

.nav-bar {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 32rpx;
  }

  .back-btn {
    font-size: 48rpx;
    color: rgba(0, 0, 0, 0.85);
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }

  .optimize-btn {
    font-size: 28rpx;
    color: #1890ff;
  }
}

.schedule-overview {
  margin: 24rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 32rpx;
  color: #fff;

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .overview-title {
      font-size: 32rpx;
      font-weight: 600;
    }

    .overview-date {
      font-size: 26rpx;
      opacity: 0.8;
    }
  }

  .overview-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;

    .stat-item {
      text-align: center;

      .stat-value {
        font-size: 40rpx;
        font-weight: bold;
        display: block;
        margin-bottom: 8rpx;
      }

      .stat-label {
        font-size: 22rpx;
        opacity: 0.8;
      }
    }
  }
}

.schedule-calendar,
.today-schedule,
.quick-schedule,
.conflict-detection,
.ai-suggestions {
  margin: 0 32rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .section-title {
    font-size: 28rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    display: block;
    margin-bottom: 24rpx;
  }
}

.schedule-calendar {
  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .calendar-title {
      font-size: 28rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
    }

    .calendar-nav {
      display: flex;
      align-items: center;
      gap: 24rpx;

      .nav-btn {
        font-size: 32rpx;
        color: rgba(0, 0, 0, 0.45);
        padding: 8rpx 16rpx;
      }

      .month-text {
        font-size: 28rpx;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.85);
        min-width: 120rpx;
        text-align: center;
      }
    }
  }

  .calendar-weekdays {
    display: flex;
    margin-bottom: 16rpx;

    .weekday {
      flex: 1;
      text-align: center;
      font-size: 24rpx;
      color: rgba(0, 0, 0, 0.45);
      padding: 16rpx 0;
    }
  }

  .calendar-days {
    display: flex;
    flex-wrap: wrap;

    .calendar-day {
      width: calc(100% / 7);
      height: 120rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      position: relative;
      margin-bottom: 2rpx;

      .day-number {
        font-size: 26rpx;
        color: rgba(0, 0, 0, 0.85);
      }

      .shift-indicator {
        position: absolute;
        bottom: 8rpx;
        width: 12rpx;
        height: 12rpx;
        border-radius: 6rpx;

        &.regular { background: #52c41a; }
        &.overtime { background: #fa8c16; }
        &.night { background: #722ed1; }
        &.rest { background: #f0f0f0; }
      }

      &.other-month {
        opacity: 0.3;
      }

      &.today {
        background: #e6f7ff;
        border-radius: 8rpx;
      }

      &.has-shift {
        .day-number {
          color: #1890ff;
        }
      }
    }
  }
}

.today-schedule {
  .shifts-list {
    .shift-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .shift-time {
        width: 200rpx;
        text-align: center;
        border-right: 1px solid #f0f0f0;
        padding-right: 20rpx;

        .time-text {
          font-size: 26rpx;
          color: rgba(0, 0, 0, 0.85);
          font-weight: 600;
          display: block;
        }

        .duration {
          font-size: 22rpx;
          color: rgba(0, 0, 0, 0.45);
          margin-top: 4rpx;
        }
      }

      .shift-info {
        flex: 1;
        padding-left: 20rpx;

        .shift-name {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.85);
          font-weight: 600;
          display: block;
          margin-bottom: 4rpx;
        }

        .shift-location {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .shift-actions {
        display: flex;
        gap: 24rpx;

        .action-text {
          font-size: 24rpx;
          color: #1890ff;
        }
      }
    }
  }
}

.quick-schedule {
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;

    .action-card {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-radius: 16rpx;
      padding: 32rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      transition: all 0.3s ease;

      &:active {
        transform: scale(0.95);
        background: #e6f7ff;
      }

      .action-icon {
        font-size: 48rpx;
        margin-bottom: 16rpx;
      }

      .action-title {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        font-weight: 600;
        margin-bottom: 8rpx;
        display: block;
      }

      .action-desc {
        font-size: 22rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }
}

.conflict-detection {
  .conflicts-list {
    .conflict-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .conflict-icon {
        font-size: 32rpx;
        color: #fa8c16;
        margin-right: 24rpx;
      }

      .conflict-content {
        flex: 1;

        .conflict-title {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.85);
          font-weight: 600;
          display: block;
          margin-bottom: 8rpx;
        }

        .conflict-desc {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.65);
          margin-bottom: 8rpx;
          display: block;
        }

        .conflict-time {
          font-size: 22rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .conflict-action {
        .action-text {
          font-size: 24rpx;
          color: #1890ff;
        }
      }
    }
  }
}

.ai-suggestions {
  .suggestions-list {
    .suggestion-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .suggestion-icon {
        font-size: 32rpx;
        color: #722ed1;
        margin-right: 24rpx;
      }

      .suggestion-content {
        flex: 1;

        .suggestion-title {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.85);
          font-weight: 600;
          display: block;
          margin-bottom: 8rpx;
        }

        .suggestion-desc {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.65);
          margin-bottom: 8rpx;
          display: block;
        }

        .suggestion-impact {
          display: flex;
          align-items: center;

          .impact-label {
            font-size: 22rpx;
            color: rgba(0, 0, 0, 0.45);
            margin-right: 8rpx;
          }

          .impact-value {
            font-size: 24rpx;
            color: #52c41a;
            font-weight: 600;
          }
        }
      }

      .suggestion-action {
        .action-text {
          font-size: 24rpx;
          color: #1890ff;
        }
      }
    }
  }
}
</style>