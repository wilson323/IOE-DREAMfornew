<template>
  <view class="attendance-schedule-page">
    <!-- 顶部导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="iconfont icon-back"></text>
      </view>
      <view class="nav-title">排班管理</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 日期选择 -->
      <view class="date-selector">
        <view class="prev-month" @click="prevMonth">
          <text class="iconfont icon-arrow-left"></text>
        </view>
        <view class="current-month">{{ currentYear }}年{{ currentMonth }}月</view>
        <view class="next-month" @click="nextMonth">
          <text class="iconfont icon-arrow-right"></text>
        </view>
      </view>

      <!-- 星期标题 -->
      <view class="week-headers">
        <view class="week-item" v-for="week in weeks" :key="week">{{ week }}</view>
      </view>

      <!-- 日历视图 -->
      <view class="calendar-grid">
        <view
          class="calendar-day"
          v-for="(day, index) in calendarDays"
          :key="index"
          :class="{
            'today': day.isToday,
            'other-month': day.isOtherMonth,
            'has-schedule': day.hasSchedule
          }"
          @click="selectDay(day)"
        >
          <view class="day-number">{{ day.date }}</view>
          <view class="schedule-info" v-if="day.schedule">
            <text class="schedule-type">{{ day.schedule.type }}</text>
          </view>
        </view>
      </view>

      <!-- 选中日期详情 -->
      <view class="selected-day-detail" v-if="selectedDay">
        <view class="detail-header">
          <text class="date">{{ selectedDay.year }}年{{ selectedDay.month }}月{{ selectedDay.date }}日</text>
          <text class="weekday">{{ selectedDay.weekday }}</text>
        </view>

        <view class="schedule-info" v-if="selectedDay.schedule">
          <view class="schedule-item">
            <text class="label">排班类型:</text>
            <text class="value">{{ selectedDay.schedule.type }}</text>
          </view>
          <view class="schedule-item">
            <text class="label">上班时间:</text>
            <text class="value">{{ selectedDay.schedule.startTime }}</text>
          </view>
          <view class="schedule-item">
            <text class="label">下班时间:</text>
            <text class="value">{{ selectedDay.schedule.endTime }}</text>
          </view>
          <view class="schedule-item">
            <text class="label">工作时长:</text>
            <text class="value">{{ selectedDay.schedule.workHours }}</text>
          </view>
        </view>

        <view class="no-schedule" v-else>
          <text>该日期暂无排班信息</text>
        </view>

        <!-- 操作按钮 -->
        <view class="action-buttons">
          <button class="action-btn set-schedule-btn" @click="setSchedule">
            <text class="iconfont icon-edit"></text>
            <text>设置排班</text>
          </button>
          <button class="action-btn copy-schedule-btn" @click="copySchedule">
            <text class="iconfont icon-copy"></text>
            <text>复制排班</text>
          </button>
        </view>
      </view>

      <!-- 排班列表视图 -->
      <view class="schedule-list-section">
        <view class="section-header">
          <text class="title">本月排班列表</text>
          <view class="actions">
            <button class="batch-btn" @click="batchSetSchedule">
              <text class="iconfont icon-batch"></text>
              <text>批量设置</text>
            </button>
          </view>
        </view>

        <view class="schedule-list">
          <view class="schedule-item" v-for="(schedule, index) in scheduleList" :key="index">
            <view class="date">{{ schedule.date }}</view>
            <view class="type">{{ schedule.type }}</view>
            <view class="time">{{ schedule.startTime }} - {{ schedule.endTime }}</view>
            <view class="actions">
              <text class="iconfont icon-edit" @click="editSchedule(schedule)"></text>
            </view>
          </view>
          <view class="no-data" v-if="scheduleList.length === 0">
            <text>暂无排班数据</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 设置排班弹窗 -->
    <view class="schedule-modal" v-if="showScheduleModal">
      <view class="modal-overlay" @click="closeScheduleModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="title">设置排班</text>
          <text class="close-btn" @click="closeScheduleModal">×</text>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <text class="label">排班日期</text>
            <input class="input" :value="modalSchedule.date" disabled />
          </view>
          <view class="form-item">
            <text class="label">排班类型</text>
            <picker mode="selector" :range="scheduleTypes" @change="onScheduleTypeChange">
              <view class="picker">
                {{ modalSchedule.type || '请选择排班类型' }}
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="label">上班时间</text>
            <picker mode="time" :value="modalSchedule.startTime" @change="onStartTimeChange">
              <view class="picker">
                {{ modalSchedule.startTime || '请选择上班时间' }}
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="label">下班时间</text>
            <picker mode="time" :value="modalSchedule.endTime" @change="onEndTimeChange">
              <view class="picker">
                {{ modalSchedule.endTime || '请选择下班时间' }}
              </view>
            </picker>
          </view>
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeScheduleModal">取消</button>
          <button class="confirm-btn" @click="saveSchedule">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { scheduleApi } from '@/api/business/attendance/schedule-api.js'

export default {
  name: 'AttendanceSchedule',
  setup() {
    // 响应式数据
    const currentYear = ref(new Date().getFullYear())
    const currentMonth = ref(new Date().getMonth() + 1)
    const selectedDay = ref(null)
    const showScheduleModal = ref(false)
    const scheduleList = ref([])
    const weeks = ['日', '一', '二', '三', '四', '五', '六']
    const scheduleTypes = ['正常班', '早班', '晚班', '夜班', '休息']

    const modalSchedule = reactive({
      date: '',
      type: '',
      startTime: '09:00',
      endTime: '18:00'
    })

    // 计算属性
    const calendarDays = computed(() => {
      const year = currentYear.value
      const month = currentMonth.value
      const firstDay = new Date(year, month - 1, 1)
      const lastDay = new Date(year, month, 0)
      const startDate = new Date(firstDay)
      startDate.setDate(startDate.getDate() - firstDay.getDay())

      const days = []
      const today = new Date()

      for (let i = 0; i < 42; i++) {
        const date = new Date(startDate)
        date.setDate(startDate.getDate() + i)

        const isToday = date.toDateString() === today.toDateString()
        const isOtherMonth = date.getMonth() !== month - 1
        const dayObj = {
          year: date.getFullYear(),
          month: date.getMonth() + 1,
          date: date.getDate(),
          weekday: weeks[date.getDay()],
          isToday,
          isOtherMonth,
          hasSchedule: false,
          schedule: null
        }

        // 检查是否有排班
        const schedule = scheduleList.value.find(s =>
          s.date === `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
        )

        if (schedule) {
          dayObj.hasSchedule = true
          dayObj.schedule = schedule
        }

        days.push(dayObj)
      }

      return days
    })

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const prevMonth = () => {
      if (currentMonth.value === 1) {
        currentYear.value--
        currentMonth.value = 12
      } else {
        currentMonth.value--
      }
      loadScheduleData()
    }

    const nextMonth = () => {
      if (currentMonth.value === 12) {
        currentYear.value++
        currentMonth.value = 1
      } else {
        currentMonth.value++
      }
      loadScheduleData()
    }

    const selectDay = (day) => {
      if (day.isOtherMonth) return
      selectedDay.value = day
    }

    const setSchedule = () => {
      if (!selectedDay.value) return

      modalSchedule.date = `${selectedDay.value.year}-${String(selectedDay.value.month).padStart(2, '0')}-${String(selectedDay.value.date).padStart(2, '0')}`
      modalSchedule.type = selectedDay.value.schedule?.type || ''
      modalSchedule.startTime = selectedDay.value.schedule?.startTime || '09:00'
      modalSchedule.endTime = selectedDay.value.schedule?.endTime || '18:00'
      showScheduleModal.value = true
    }

    const copySchedule = () => {
      uni.showToast({
        title: '复制排班功能开发中',
        icon: 'none'
      })
    }

    const batchSetSchedule = () => {
      uni.showToast({
        title: '批量设置功能开发中',
        icon: 'none'
      })
    }

    const editSchedule = (schedule) => {
      selectedDay.value = {
        year: parseInt(schedule.date.split('-')[0]),
        month: parseInt(schedule.date.split('-')[1]),
        date: parseInt(schedule.date.split('-')[2]),
        weekday: weeks[new Date(schedule.date).getDay()]
      }
      setSchedule()
    }

    const closeScheduleModal = () => {
      showScheduleModal.value = false
    }

    const onScheduleTypeChange = (e) => {
      modalSchedule.type = scheduleTypes[e.detail.value]
    }

    const onStartTimeChange = (e) => {
      modalSchedule.startTime = e.detail.value
    }

    const onEndTimeChange = (e) => {
      modalSchedule.endTime = e.detail.value
    }

    const saveSchedule = async () => {
      if (!modalSchedule.type) {
        uni.showToast({
          title: '请选择排班类型',
          icon: 'none'
        })
        return
      }

      try {
        const params = {
          date: modalSchedule.date,
          type: modalSchedule.type,
          startTime: modalSchedule.startTime,
          endTime: modalSchedule.endTime
        }

        // 这里应该调用API保存排班信息
        // const result = await scheduleApi.saveSchedule(params)

        uni.showToast({
          title: '排班设置成功',
          icon: 'success'
        })

        closeScheduleModal()
        loadScheduleData()
      } catch (error) {
        uni.showToast({
          title: '排班设置失败',
          icon: 'none'
        })
      }
    }

    const loadScheduleData = async () => {
      try {
        // 模拟数据
        scheduleList.value = [
          {
            date: `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-01`,
            type: '正常班',
            startTime: '09:00',
            endTime: '18:00'
          },
          {
            date: `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-02`,
            type: '早班',
            startTime: '07:00',
            endTime: '16:00'
          },
          {
            date: `${currentYear.value}-${String(currentMonth.value).padStart(2, '0')}-03`,
            type: '晚班',
            startTime: '14:00',
            endTime: '23:00'
          }
        ]
      } catch (error) {
        console.error('加载排班数据失败:', error)
      }
    }

    // 生命周期
    onMounted(() => {
      loadScheduleData()
    })

    // 暴露给模板的数据和方法
    return {
      currentYear,
      currentMonth,
      selectedDay,
      showScheduleModal,
      scheduleList,
      weeks,
      scheduleTypes,
      modalSchedule,
      calendarDays,
      goBack,
      prevMonth,
      nextMonth,
      selectDay,
      setSchedule,
      copySchedule,
      batchSetSchedule,
      editSchedule,
      closeScheduleModal,
      onScheduleTypeChange,
      onStartTimeChange,
      onEndTimeChange,
      saveSchedule
    }
  }
}
</script>

<style lang="scss" scoped>
.attendance-schedule-page {
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
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;

  .nav-left, .nav-right {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
  }
}

.page-content {
  padding-top: 44px;
  padding: 15px;
}

.date-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .prev-month, .next-month {
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: #f0f0f0;

    .iconfont {
      font-size: 16px;
      color: #666;
    }
  }

  .current-month {
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }
}

.week-headers {
  display: flex;
  background-color: #667eea;
  border-radius: 10px 10px 0 0;
  overflow: hidden;
  margin-bottom: 1px;

  .week-item {
    flex: 1;
    text-align: center;
    padding: 10px 0;
    color: #fff;
    font-weight: 500;
  }
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background-color: #ddd;
  border-radius: 0 0 10px 10px;
  overflow: hidden;
  margin-bottom: 20px;

  .calendar-day {
    aspect-ratio: 1;
    background-color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;

    &.today {
      background-color: #e3f2fd;
      border: 1px solid #667eea;
    }

    &.other-month {
      background-color: #f9f9f9;
      color: #ccc;
    }

    &.has-schedule {
      background-color: #e8f5e9;

      .schedule-info {
        .schedule-type {
          font-size: 10px;
          color: #4caf50;
          background-color: rgba(76, 175, 80, 0.1);
          padding: 2px 4px;
          border-radius: 3px;
        }
      }
    }

    .day-number {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 5px;
    }

    .schedule-info {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
  }
}

.selected-day-detail {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eee;

    .date {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .weekday {
      font-size: 14px;
      color: #666;
    }
  }

  .schedule-info {
    .schedule-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid #f0f0f0;

      .label {
        color: #666;
      }

      .value {
        color: #333;
        font-weight: 500;
      }

      &:last-child {
        border-bottom: none;
      }
    }
  }

  .no-schedule {
    text-align: center;
    padding: 20px;
    color: #999;
  }

  .action-buttons {
    display: flex;
    gap: 10px;
    margin-top: 15px;

    .action-btn {
      flex: 1;
      height: 40px;
      border: 1px solid #667eea;
      border-radius: 5px;
      background-color: #fff;
      color: #667eea;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;

      .iconfont {
        font-size: 16px;
        margin-right: 5px;
      }
    }
  }
}

.schedule-list-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .actions {
      .batch-btn {
        height: 30px;
        border: 1px solid #667eea;
        border-radius: 5px;
        background-color: #fff;
        color: #667eea;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        padding: 0 10px;

        .iconfont {
          font-size: 14px;
          margin-right: 3px;
        }
      }
    }
  }

  .schedule-list {
    .schedule-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      .date {
        width: 100px;
        color: #333;
        font-weight: 500;
      }

      .type {
        width: 80px;
        color: #667eea;
      }

      .time {
        flex: 1;
        color: #666;
        font-size: 14px;
      }

      .actions {
        width: 40px;
        text-align: center;

        .iconfont {
          font-size: 18px;
          color: #667eea;
        }
      }

      &:last-child {
        border-bottom: none;
      }
    }

    .no-data {
      text-align: center;
      padding: 20px;
      color: #999;
    }
  }
}

.schedule-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;

  .modal-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
  }

  .modal-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 90%;
    background-color: #fff;
    border-radius: 10px;
    overflow: hidden;

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      background-color: #667eea;
      color: #fff;

      .title {
        font-size: 16px;
        font-weight: 500;
      }

      .close-btn {
        font-size: 24px;
        font-weight: 300;
      }
    }

    .modal-body {
      padding: 20px;

      .form-item {
        margin-bottom: 20px;

        .label {
          display: block;
          margin-bottom: 8px;
          color: #333;
          font-weight: 500;
        }

        .input, .picker {
          width: 100%;
          height: 40px;
          border: 1px solid #ddd;
          border-radius: 5px;
          padding: 0 10px;
          font-size: 14px;
          background-color: #fff;
        }

        .picker {
          display: flex;
          align-items: center;
          justify-content: space-between;
          color: #666;
        }
      }
    }

    .modal-footer {
      display: flex;
      border-top: 1px solid #eee;

      .cancel-btn, .confirm-btn {
        flex: 1;
        height: 45px;
        border: none;
        font-size: 16px;
      }

      .cancel-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      .confirm-btn {
        background-color: #667eea;
        color: #fff;
      }
    }
  }
}
</style>