<template>
  <view class="schedule-container">
    <!-- 月份选择器 -->
    <view class="month-selector">
      <view class="selector-content">
        <text class="arrow" @click="changeMonth(-1)">‹</text>
        <text class="month-text">{{ currentYear }}年{{ currentMonth }}月</text>
        <text class="arrow" @click="changeMonth(1)">›</text>
      </view>
    </view>

    <!-- 今日排班概览 -->
    <view class="today-overview" v-if="todayShifts.length > 0">
      <view class="overview-title">今日排班</view>
      <view class="shift-list">
        <view class="shift-item" v-for="shift in todayShifts" :key="shift.shiftId">
          <view class="shift-header">
            <text class="shift-name">{{ shift.shiftName }}</text>
            <view class="shift-type-badge" :class="getShiftTypeClass(shift.shiftType)">
              <text class="type-text">{{ getShiftTypeText(shift.shiftType) }}</text>
            </view>
          </view>
          <view class="shift-time">
            <text class="time-text">{{ formatTimeRange(shift) }}</text>
          </view>
          <view class="shift-actions">
            <button class="action-btn edit-btn" @click="editShift(shift)">
              <text class="btn-icon">✏️</text>
              <text class="btn-text">编辑</text>
            </button>
            <button class="action-btn delete-btn" @click="deleteShift(shift.shiftId)">
              <text class="btn-icon">🗑️</text>
              <text class="btn-text">删除</text>
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 排班统计卡片 -->
    <view class="stats-cards">
      <view class="stat-item">
        <text class="stat-value">{{ monthStats.totalDays || 0 }}</text>
        <text class="stat-label">工作日</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ monthStats.restDays || 0 }}</text>
        <text class="stat-label">休息日</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ monthStats.overtimeDays || 0 }}</text>
        <text class="stat-label">加班日</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ monthStats.conflictCount || 0 }}</text>
        <text class="stat-label">冲突</text>
      </view>
    </view>

    <!-- 日历视图 -->
    <view class="calendar-section">
      <view class="calendar-header">
        <text class="calendar-title">排班日历</text>
        <view class="legend">
          <view class="legend-item">
            <view class="legend-dot normal"></view>
            <text class="legend-text">正常</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot overtime"></view>
            <text class="legend-text">加班</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot night"></view>
            <text class="legend-text">夜班</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot rest"></view>
            <text class="legend-text">休息</text>
          </view>
        </view>
      </view>

      <view class="calendar-weekdays">
        <text class="weekday" v-for="day in weekdays" :key="day">{{ day }}</text>
      </view>

      <scroll-view
        class="calendar-body"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshing"
        @refresherrefresh="refreshCalendar"
      >
        <view class="calendar-days">
          <view
            class="day-cell"
            v-for="day in calendarDays"
            :key="day.date"
            :class="{
              'other-month': day.isOtherMonth,
              'today': day.isToday,
              'has-shift': day.hasShift,
              'conflict': day.hasConflict
            }"
            @click="selectDate(day)"
          >
            <text class="day-number">{{ day.dayNumber }}</text>
            <view class="shift-indicators" v-if="day.shifts && day.shifts.length > 0">
              <view
                class="shift-dot"
                v-for="(shift, index) in day.shifts.slice(0, 3)"
                :key="index"
                :class="getShiftTypeClass(shift.shiftType)"
              ></view>
              <text class="more-shifts" v-if="day.shifts.length > 3">+{{ day.shifts.length - 3 }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <view class="action-title">快捷操作</view>
      <view class="action-grid">
        <view class="action-card" @click="addRegularShift">
          <text class="action-icon">📅</text>
          <text class="action-name">正常排班</text>
        </view>
        <view class="action-card" @click="addOvertimeShift">
          <text class="action-icon">⏰</text>
          <text class="action-name">加班排班</text>
        </view>
        <view class="action-card" @click="batchSchedule">
          <text class="action-icon">📋</text>
          <text class="action-name">批量排班</text>
        </view>
        <view class="action-card" @click="copyPreviousWeek">
          <text class="action-icon">📋</text>
          <text class="action-name">复制上周</text>
        </view>
      </view>
    </view>

    <!-- 冲突提示 -->
    <view class="conflict-alert" v-if="conflicts.length > 0">
      <view class="alert-header">
        <text class="alert-icon">⚠️</text>
        <text class="alert-title">发现 {{ conflicts.length }} 个排班冲突</text>
      </view>
      <view class="conflict-list">
        <view class="conflict-item" v-for="conflict in conflicts" :key="conflict.date">
          <text class="conflict-date">{{ formatDate(conflict.date) }}</text>
          <text class="conflict-desc">{{ conflict.description }}</text>
          <button class="resolve-btn" @click="resolveConflict(conflict)">
            <text class="btn-text">解决</text>
          </button>
        </view>
      </view>
    </view>

    <!-- AI优化建议 -->
    <view class="ai-suggestions" v-if="suggestions.length > 0">
      <view class="suggestion-header">
        <text class="suggestion-icon">🤖</text>
        <text class="suggestion-title">AI优化建议</text>
      </view>
      <view class="suggestion-list">
        <view class="suggestion-item" v-for="suggestion in suggestions" :key="suggestion.id">
          <text class="suggestion-text">{{ suggestion.text }}</text>
          <button class="apply-btn" @click="applySuggestion(suggestion)">
            <text class="btn-text">应用</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 日期详情弹窗 -->
    <uni-popup ref="dateDetailPopup" type="bottom">
      <view class="date-detail-popup">
        <view class="popup-header">
          <text class="popup-title">{{ formatDate(selectedDate) }} 排班详情</text>
          <text class="close-btn" @click="closeDateDetail">✕</text>
        </view>
        <view class="popup-content">
          <view class="shift-detail-list" v-if="selectedDateShifts.length > 0">
            <view class="shift-detail-item" v-for="shift in selectedDateShifts" :key="shift.shiftId">
              <view class="detail-header">
                <text class="detail-name">{{ shift.shiftName }}</text>
                <view class="detail-type" :class="getShiftTypeClass(shift.shiftType)">
                  <text class="type-text">{{ getShiftTypeText(shift.shiftType) }}</text>
                </view>
              </view>
              <view class="detail-info">
                <text class="info-item">⏰ {{ formatTimeRange(shift) }}</text>
                <text class="info-item" v-if="shift.location">📍 {{ shift.location }}</text>
              </view>
              <view class="detail-actions">
                <button class="detail-action-btn edit" @click="editShift(shift)">编辑</button>
                <button class="detail-action-btn delete" @click="deleteShift(shift.shiftId)">删除</button>
              </view>
            </view>
          </view>
          <view class="empty-state" v-else>
            <text class="empty-icon">📅</text>
            <text class="empty-text">该日期暂无排班</text>
            <button class="add-shift-btn" @click="addShiftForDate">
              <text class="btn-text">添加排班</text>
            </button>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

const currentYear = ref(dayjs().format('YYYY'))
const currentMonth = ref(dayjs().format('M'))
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const refreshing = ref(false)
const employeeId = ref(1001)

const todayShifts = ref([])
const calendarDays = ref([])
const conflicts = ref([])
const suggestions = ref([])

const monthStats = reactive({
  totalDays: 0,
  restDays: 0,
  overtimeDays: 0,
  conflictCount: 0
})

const selectedDateShifts = ref([])
const dateDetailPopup = ref(null)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

onMounted(() => {
  loadScheduleData()
})

const changeMonth = (delta) => {
  let month = parseInt(currentMonth.value) + delta
  let year = parseInt(currentYear.value)

  if (month > 12) {
    month = 1
    year++
  } else if (month < 1) {
    month = 12
    year--
  }

  currentMonth.value = month.toString()
  currentYear.value = year.toString()
  loadScheduleData()
}

const loadScheduleData = async () => {
  try {
    const monthStart = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .startOf('month')
      .format('YYYY-MM-DD')

    const monthEnd = dayjs()
      .year(parseInt(currentYear.value))
      .month(parseInt(currentMonth.value) - 1)
      .endOf('month')
      .format('YYYY-MM-DD')

    const [todayRes, statsRes, calendarRes, conflictRes, suggestionRes] = await Promise.all([
      attendanceApi.scheduleApi.getTodayShifts({ employeeId: employeeId.value }),
      attendanceApi.scheduleApi.getMonthScheduleStats({
        employeeId: employeeId.value,
        startDate: monthStart,
        endDate: monthEnd
      }),
      attendanceApi.scheduleApi.getMonthCalendar({
        employeeId: employeeId.value,
        startDate: monthStart,
        endDate: monthEnd
      }),
      attendanceApi.scheduleApi.getScheduleConflicts({
        employeeId: employeeId.value,
        startDate: monthStart,
        endDate: monthEnd
      }),
      attendanceApi.scheduleApi.getOptimizationSuggestions({
        employeeId: employeeId.value,
        startDate: monthStart,
        endDate: monthEnd
      })
    ])

    if (todayRes.success) {
      todayShifts.value = todayRes.data || []
    }

    if (statsRes.success) {
      Object.assign(monthStats, statsRes.data)
    }

    if (calendarRes.success) {
      generateCalendar(calendarRes.data || [])
    }

    if (conflictRes.success) {
      conflicts.value = conflictRes.data || []
    }

    if (suggestionRes.success) {
      suggestions.value = suggestionRes.data || []
    }
  } catch (error) {
    console.error('[排班管理] 加载失败:', error)
    uni.showToast({ title: '数据加载失败', icon: 'none' })
  }
}

const refreshCalendar = () => {
  refreshing.value = true
  loadScheduleData().finally(() => {
    refreshing.value = false
  })
}

const generateCalendar = (scheduleData) => {
  const startOfMonth = dayjs()
    .year(parseInt(currentYear.value))
    .month(parseInt(currentMonth.value) - 1)
    .startOf('month')

  const endOfMonth = dayjs()
    .year(parseInt(currentYear.value))
    .month(parseInt(currentMonth.value) - 1)
    .endOf('month')

  const startWeekday = startOfMonth.day()
  const daysInMonth = endOfMonth.date()

  const prevMonthDays = startWeekday
  const prevMonth = startOfMonth.subtract(1, 'day')
  const daysInPrevMonth = prevMonth.daysInMonth()

  const days = []

  // 上月日期
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    const date = prevMonth.date(daysInPrevMonth - i).format('YYYY-MM-DD')
    days.push({
      date,
      dayNumber: daysInPrevMonth - i,
      isOtherMonth: true,
      isToday: false,
      hasShift: false,
      hasConflict: false,
      shifts: []
    })
  }

  // 当月日期
  const scheduleMap = new Map()
  scheduleData.forEach(item => {
    if (!scheduleMap.has(item.date)) {
      scheduleMap.set(item.date, [])
    }
    scheduleMap.get(item.date).push(item)
  })

  const today = dayjs().format('YYYY-MM-DD')
  for (let i = 1; i <= daysInMonth; i++) {
    const date = startOfMonth.date(i).format('YYYY-MM-DD')
    const shifts = scheduleMap.get(date) || []
    const hasConflict = conflicts.value.some(c => c.date === date)

    days.push({
      date,
      dayNumber: i,
      isOtherMonth: false,
      isToday: date === today,
      hasShift: shifts.length > 0,
      hasConflict,
      shifts
    })
  }

  // 下月日期
  const totalCells = Math.ceil(days.length / 7) * 7
  const remainingCells = totalCells - days.length
  for (let i = 1; i <= remainingCells; i++) {
    const date = endOfMonth.add(i, 'day').format('YYYY-MM-DD')
    days.push({
      date,
      dayNumber: i,
      isOtherMonth: true,
      isToday: false,
      hasShift: false,
      hasConflict: false,
      shifts: []
    })
  }

  calendarDays.value = days
}

const selectDate = (day) => {
  selectedDate.value = day.date
  selectedDateShifts.value = day.shifts || []

  if (dateDetailPopup.value) {
    dateDetailPopup.value.open()
  }
}

const closeDateDetail = () => {
  if (dateDetailPopup.value) {
    dateDetailPopup.value.close()
  }
}

const formatTimeRange = (shift) => {
  return `${shift.startTime} - ${shift.endTime}`
}

const formatDate = (dateStr) => {
  return dayjs(dateStr).format('MM月DD日')
}

const getShiftTypeText = (type) => {
  const map = { 'REGULAR': '正常', 'OVERTIME': '加班', 'NIGHT': '夜班', 'REST': '休息' }
  return map[type] || '未知'
}

const getShiftTypeClass = (type) => {
  const map = { 'REGULAR': 'type-normal', 'OVERTIME': 'type-overtime', 'NIGHT': 'type-night', 'REST': 'type-rest' }
  return map[type] || ''
}

const addRegularShift = () => {
  uni.navigateTo({ url: '/pages/attendance/schedule-edit?mode=regular' })
}

const addOvertimeShift = () => {
  uni.navigateTo({ url: '/pages/attendance/schedule-edit?mode=overtime' })
}

const batchSchedule = () => {
  uni.navigateTo({ url: '/pages/attendance/schedule-batch' })
}

const copyPreviousWeek = async () => {
  try {
    const res = await attendanceApi.scheduleApi.copyPreviousWeek({
      employeeId: employeeId.value,
      targetDate: selectedDate.value
    })

    if (res.success) {
      uni.showToast({ title: '复制成功', icon: 'success' })
      refreshCalendar()
    }
  } catch (error) {
    console.error('[复制上周] 失败:', error)
    uni.showToast({ title: '复制失败', icon: 'none' })
  }
}

const editShift = (shift) => {
  uni.navigateTo({
    url: `/pages/attendance/schedule-edit?shiftId=${shift.shiftId}`
  })
}

const deleteShift = async (shiftId) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除此排班吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await attendanceApi.scheduleApi.deleteShift({ shiftId })
          if (result.success) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            refreshCalendar()
          }
        } catch (error) {
          console.error('[删除排班] 失败:', error)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

const resolveConflict = (conflict) => {
  uni.navigateTo({
    url: `/pages/attendance/conflict-resolve?conflictId=${conflict.id}`
  })
}

const applySuggestion = async (suggestion) => {
  try {
    const res = await attendanceApi.scheduleApi.applyOptimization({
      employeeId: employeeId.value,
      suggestionId: suggestion.id
    })

    if (res.success) {
      uni.showToast({ title: '优化应用成功', icon: 'success' })
      refreshCalendar()
    }
  } catch (error) {
    console.error('[应用优化] 失败:', error)
    uni.showToast({ title: '应用失败', icon: 'none' })
  }
}

const addShiftForDate = () => {
  uni.navigateTo({
    url: `/pages/attendance/schedule-edit?date=${selectedDate.value}`
  })
}
</script>

<style lang="scss" scoped>
.schedule-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.month-selector {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.selector-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.arrow {
  font-size: 40rpx;
  color: white;
  padding: 0 30rpx;
}

.month-text {
  font-size: 32rpx;
  font-weight: bold;
  color: white;
  margin: 0 30rpx;
}

.today-overview {
  background: white;
  margin: 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.overview-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.shift-list {
  .shift-item {
    padding: 25rpx;
    background-color: #f8f9fa;
    border-radius: 12rpx;
    margin-bottom: 15rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.shift-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.shift-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.shift-type-badge {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}

.type-normal { background-color: #f0fdf4; color: #22c55e; }
.type-overtime { background-color: #fef3c7; color: #f59e0b; }
.type-night { background-color: #e0e7ff; color: #6366f1; }
.type-rest { background-color: #f3f4f6; color: #6b7280; }

.shift-time {
  margin-bottom: 15rpx;
}

.time-text {
  font-size: 24rpx;
  color: #666;
}

.shift-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12rpx;
  border-radius: 10rpx;
  border: none;
  font-size: 24rpx;
}

.btn-icon { font-size: 28rpx; margin-right: 8rpx; }
.btn-text { font-size: 24rpx; }

.edit-btn { background-color: #667eea; color: white; }
.delete-btn { background-color: #ef4444; color: white; }

.stats-cards {
  display: flex;
  justify-content: space-around;
  background: white;
  padding: 30rpx;
  margin: 0 30rpx 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

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

.calendar-section {
  background: white;
  margin: 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.calendar-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.legend {
  display: flex;
  gap: 20rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}

.legend-dot.normal { background-color: #22c55e; }
.legend-dot.overtime { background-color: #f59e0b; }
.legend-dot.night { background-color: #6366f1; }
.legend-dot.rest { background-color: #9ca3af; }

.legend-text {
  font-size: 22rpx;
  color: #666;
}

.calendar-weekdays {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.weekday {
  font-size: 24rpx;
  color: #666;
  font-weight: bold;
}

.calendar-body {
  height: 500rpx;
}

.calendar-days {
  display: flex;
  flex-wrap: wrap;
}

.day-cell {
  width: 14.28%;
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 15rpx 0;
  position: relative;
}

.day-cell.other-month .day-number {
  color: #ccc;
}

.day-cell.today {
  background-color: #fef3c7;
  border-radius: 50%;
}

.day-cell.has-shift {
  background-color: #f0fdf4;
}

.day-cell.conflict {
  background-color: #fef2f2;
}

.day-number {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.shift-indicators {
  display: flex;
  gap: 4rpx;
  align-items: center;
}

.shift-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.shift-dot.type-normal { background-color: #22c55e; }
.shift-dot.type-overtime { background-color: #f59e0b; }
.shift-dot.type-night { background-color: #6366f1; }

.more-shifts {
  font-size: 20rpx;
  color: #667eea;
}

.quick-actions {
  background: white;
  margin: 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.action-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 25rpx 10rpx;
  background-color: #f8f9fa;
  border-radius: 12rpx;
}

.action-icon {
  font-size: 40rpx;
  margin-bottom: 10rpx;
}

.action-name {
  font-size: 22rpx;
  color: #666;
  text-align: center;
}

.conflict-alert {
  background: white;
  margin: 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
  border-left: 4rpx solid #ef4444;
  margin-bottom: 20rpx;
}

.alert-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.alert-icon {
  font-size: 28rpx;
  margin-right: 10rpx;
}

.alert-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #ef4444;
}

.conflict-list {
  .conflict-item {
    display: flex;
    align-items: center;
    padding: 15rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }
}

.conflict-date {
  font-size: 24rpx;
  color: #333;
  width: 120rpx;
}

.conflict-desc {
  flex: 1;
  font-size: 24rpx;
  color: #666;
}

.resolve-btn {
  padding: 8rpx 20rpx;
  background-color: #667eea;
  color: white;
  border-radius: 20rpx;
  font-size: 22rpx;
  border: none;
}

.ai-suggestions {
  background: white;
  margin: 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
  border-left: 4rpx solid #8b5cf6;
}

.suggestion-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.suggestion-icon {
  font-size: 28rpx;
  margin-right: 10rpx;
}

.suggestion-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #8b5cf6;
}

.suggestion-list {
  .suggestion-item {
    display: flex;
    align-items: center;
    padding: 15rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }
}

.suggestion-text {
  flex: 1;
  font-size: 24rpx;
  color: #666;
}

.apply-btn {
  padding: 8rpx 20rpx;
  background-color: #8b5cf6;
  color: white;
  border-radius: 20rpx;
  font-size: 22rpx;
  border: none;
}

.date-detail-popup {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.popup-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  font-size: 32rpx;
  color: #999;
  padding: 10rpx;
}

.popup-content {
  padding: 30rpx;
}

.shift-detail-list {
  .shift-detail-item {
    background-color: #f8f9fa;
    border-radius: 12rpx;
    padding: 25rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.detail-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.detail-type {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}

.detail-info {
  margin-bottom: 15rpx;
}

.info-item {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.detail-actions {
  display: flex;
  gap: 15rpx;
}

.detail-action-btn {
  flex: 1;
  padding: 12rpx;
  border-radius: 10rpx;
  font-size: 24rpx;
  border: none;
}

.detail-action-btn.edit {
  background-color: #667eea;
  color: white;
}

.detail-action-btn.delete {
  background-color: #ef4444;
  color: white;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 30rpx;
}

.add-shift-btn {
  width: 200rpx;
  height: 70rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 35rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  font-size: 28rpx;
}
</style>
