<template>
  <view class="attendance-container">
    <!-- 顶部状态卡片 -->
    <view class="status-card">
      <view class="card-header">
        <text class="title">今日考勤</text>
        <text class="date">{{ todayDate }}</text>
      </view>

      <view class="punch-status">
        <view class="status-item">
          <text class="status-label">上班打卡</text>
          <text class="status-time" :class="{ 'punched': morningPunch }">
            {{ morningPunch || '未打卡' }}
          </text>
        </view>

        <view class="status-item">
          <text class="status-label">下班打卡</text>
          <text class="status-time" :class="{ 'punched': eveningPunch }">
            {{ eveningPunch || '未打卡' }}
          </text>
        </view>
      </view>
    </view>

    <!-- 打卡操作区 -->
    <view class="punch-section">
      <view class="section-title">
        <text class="title-text">快速打卡</text>
        <text class="location-text" v-if="currentLocation">{{ currentLocation }}</text>
      </view>

      <view class="punch-buttons">
        <button class="punch-btn上班打卡" :disabled="morningPunch" @click="handlePunch('IN')">
          <text class="btn-icon">🕐</text>
          <text class="btn-text">上班打卡</text>
          <text class="btn-desc" v-if="morningPunch">已打卡</text>
        </button>

        <button class="punch-btn下班打卡" :disabled="eveningPunch" @click="handlePunch('OUT')">
          <text class="btn-icon">🕑</text>
          <text class="btn-text">下班打卡</text>
          <text class="btn-desc" v-if="eveningPunch">已打卡</text>
        </button>
      </view>

      <!-- 位置验证状态 -->
      <view class="location-status" :class="{ 'valid': locationValid }">
        <text class="location-icon">{{ locationValid ? '✓' : '⚠' }}</text>
        <text class="location-text">
          {{ locationValid ? '位置验证通过' : '不在考勤范围内' }}
        </text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="function-menu">
      <view class="menu-title">考勤功能</view>

      <view class="menu-grid">
        <view class="menu-item" @click="navigateTo('/pages/attendance/records')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📋</text>
          </view>
          <text class="menu-text">打卡记录</text>
          <text class="menu-desc">历史记录</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/leave')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📝</text>
          </view>
          <text class="menu-text">请假申请</text>
          <text class="menu-desc">请假出差</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/overtime')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">⏰</text>
          </view>
          <text class="menu-text">加班申请</text>
          <text class="menu-desc">加班登记</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/statistics')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📊</text>
          </view>
          <text class="menu-text">考勤统计</text>
          <text class="menu-desc">月度报表</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/schedule')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📅</text>
          </view>
          <text class="menu-text">我的排班</text>
          <text class="menu-desc">排班查看</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/repair')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">🔧</text>
          </view>
          <text class="menu-text">补卡申请</text>
          <text class="menu-desc">漏打卡补卡</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/trip')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">✈️</text>
          </view>
          <text class="menu-text">出差申请</text>
          <text class="menu-desc">出差登记</text>
        </view>

        <view class="menu-item" @click="navigateTo('/pages/attendance/summary')">
          <view class="menu-icon-wrapper">
            <text class="menu-icon">📈</text>
          </view>
          <text class="menu-text">考勤汇总</text>
          <text class="menu-desc">年度汇总</text>
        </view>
      </view>
    </view>

    <!-- 最近打卡记录 -->
    <view class="recent-records">
      <view class="records-title">最近记录</view>

      <view class="record-list">
        <view class="record-item" v-for="record in recentRecords" :key="record.id">
          <view class="record-info">
            <text class="record-date">{{ record.date }}</text>
            <text class="record-type">{{ record.type === 'IN' ? '上班' : '下班' }}</text>
          </view>
          <text class="record-time">{{ record.time }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

// 响应式数据
const todayDate = ref('')
const morningPunch = ref('')
const eveningPunch = ref('')
const currentLocation = ref('')
const locationValid = ref(false)
const recentRecords = ref([])
const employeeId = ref(1001) // 从用户信息获取

const currentPosition = reactive({
  latitude: 0,
  longitude: 0
})

// 页面生命周期
onMounted(() => {
  initPage()
})

onShow(() => {
  loadTodayStatus()
})

// 方法实现
const initPage = () => {
  todayDate.value = dayjs().format('YYYY年MM月DD日')
  getCurrentLocation()
  loadTodayStatus()
  loadRecentRecords()
}

const getCurrentLocation = () => {
  uni.getLocation({
    type: 'wgs84',
    success: (res) => {
      currentPosition.latitude = res.latitude
      currentPosition.longitude = res.longitude
      validateLocation()
    },
    fail: () => {
      uni.showToast({
        title: '获取位置失败',
        icon: 'none'
      })
    }
  })
}

const validateLocation = async () => {
  try {
    const res = await attendanceApi.locationApi.validateLocation({
      employeeId: employeeId.value,
      latitude: currentPosition.latitude,
      longitude: currentPosition.longitude
    })

    if (res.success) {
      locationValid.value = true
      // 获取位置名称
      getLocationName()
    }
  } catch (error) {
    locationValid.value = false
    console.error('位置验证失败:', error)
  }
}

const getLocationName = () => {
  // 这里可以调用逆地理编码API获取位置名称
  currentLocation.value = '公司办公楼'
}

const loadTodayStatus = async () => {
  try {
    const res = await attendanceApi.punchApi.getTodayPunchStatus(employeeId.value)
    if (res.success && res.data) {
      morningPunch.value = res.data.morningPunch
      eveningPunch.value = res.data.eveningPunch
    }
  } catch (error) {
    console.error('获取今日状态失败:', error)
  }
}

const loadRecentRecords = async () => {
  try {
    const endDate = dayjs().format('YYYY-MM-DD')
    const startDate = dayjs().subtract(7, 'day').format('YYYY-MM-DD')

    const res = await attendanceApi.punchApi.getPunchRecords({
      employeeId: employeeId.value,
      startDate,
      endDate,
      pageSize: 10,
      pageNum: 1
    })

    if (res.success && res.data) {
      recentRecords.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取打卡记录失败:', error)
  }
}

const handlePunch = async (type) => {
  if (!locationValid.value) {
    uni.showToast({
      title: '请在考勤范围内打卡',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '打卡中...'
  })

  try {
    const res = await attendanceApi.punchApi.gpsPunch({
      employeeId: employeeId.value,
      latitude: currentPosition.latitude,
      longitude: currentPosition.longitude,
      photoUrl: '', // 可以拍照后上传
      address: currentLocation.value
    })

    if (res.success) {
      uni.showToast({
        title: '打卡成功',
        icon: 'success'
      })

      // 更新状态
      loadTodayStatus()
      loadRecentRecords()
    } else {
      uni.showToast({
        title: res.message || '打卡失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.showToast({
      title: '打卡失败',
      icon: 'none'
    })
    console.error('打卡失败:', error)
  } finally {
    uni.hideLoading()
  }
}

const navigateTo = (url) => {
  uni.navigateTo({
    url
  })
}
</script>

<style lang="scss" scoped>
.attendance-container {
  padding: 20rpx;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  color: white;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .title {
      font-size: 36rpx;
      font-weight: bold;
    }

    .date {
      font-size: 28rpx;
      opacity: 0.8;
    }
  }

  .punch-status {
    display: flex;
    justify-content: space-around;

    .status-item {
      text-align: center;

      .status-label {
        display: block;
        font-size: 24rpx;
        opacity: 0.8;
        margin-bottom: 10rpx;
      }

      .status-time {
        font-size: 32rpx;
        font-weight: bold;

        &.punched {
          color: #4ade80;
        }
      }
    }
  }
}

.punch-section {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 30rpx;

    .title-text {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }

    .location-text {
      font-size: 24rpx;
      color: #666;
    }
  }

  .punch-buttons {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .punch-btn {
      flex: 0 0 48%;
      height: 120rpx;
      border-radius: 15rpx;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border: none;

      &.上班打卡 {
        background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
        color: white;
      }

      &.下班打卡 {
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        color: white;
      }

      &:disabled {
        opacity: 0.5;
      }

      .btn-icon {
        font-size: 36rpx;
        margin-bottom: 5rpx;
      }

      .btn-text {
        font-size: 28rpx;
        font-weight: bold;
      }

      .btn-desc {
        font-size: 20rpx;
        opacity: 0.8;
        margin-top: 2rpx;
      }
    }
  }

  .location-status {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 15rpx;
    border-radius: 10rpx;
    background-color: #fef2f2;

    &.valid {
      background-color: #f0fdf4;
    }

    .location-icon {
      margin-right: 10rpx;
      font-size: 24rpx;
    }

    .location-text {
      font-size: 24rpx;
      color: #666;
    }
  }
}

.function-menu {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

  .menu-title {
    font-size: 34rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: -8rpx;
      width: 60rpx;
      height: 4rpx;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      border-radius: 2rpx;
    }
  }

  .menu-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;

    .menu-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 24rpx 12rpx;
      border-radius: 16rpx;
      background-color: #f8fafc;
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;

      &:active {
        transform: scale(0.95);
        background-color: #f1f5f9;
      }

      .menu-icon-wrapper {
        width: 80rpx;
        height: 80rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 20rpx;
        margin-bottom: 12rpx;
        position: relative;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

        .menu-icon {
          font-size: 36rpx;
          filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.1));
        }
      }

      .menu-text {
        font-size: 26rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 4rpx;
        text-align: center;
      }

      .menu-desc {
        font-size: 20rpx;
        color: #999;
        text-align: center;
        line-height: 1.2;
      }

      // 为不同的菜单项设置不同的渐变色
      &:nth-child(1) .menu-icon-wrapper {
        background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%);
        box-shadow: 0 4rpx 12rpx rgba(74, 222, 128, 0.3);
      }

      &:nth-child(2) .menu-icon-wrapper {
        background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
        box-shadow: 0 4rpx 12rpx rgba(96, 165, 250, 0.3);
      }

      &:nth-child(3) .menu-icon-wrapper {
        background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
        box-shadow: 0 4rpx 12rpx rgba(251, 191, 36, 0.3);
      }

      &:nth-child(4) .menu-icon-wrapper {
        background: linear-gradient(135deg, #f87171 0%, #ef4444 100%);
        box-shadow: 0 4rpx 12rpx rgba(248, 113, 113, 0.3);
      }

      &:nth-child(5) .menu-icon-wrapper {
        background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
        box-shadow: 0 4rpx 12rpx rgba(167, 139, 250, 0.3);
      }

      &:nth-child(6) .menu-icon-wrapper {
        background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
        box-shadow: 0 4rpx 12rpx rgba(251, 146, 60, 0.3);
      }

      &:nth-child(7) .menu-icon-wrapper {
        background: linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%);
        box-shadow: 0 4rpx 12rpx rgba(56, 189, 248, 0.3);
      }

      &:nth-child(8) .menu-icon-wrapper {
        background: linear-gradient(135deg, #c084fc 0%, #a855f7 100%);
        box-shadow: 0 4rpx 12rpx rgba(192, 132, 252, 0.3);
      }
    }
  }
}

.recent-records {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;

  .records-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 20rpx;
  }

  .record-list {
    .record-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-info {
        .record-date {
          font-size: 28rpx;
          color: #333;
          margin-right: 20rpx;
        }

        .record-type {
          font-size: 24rpx;
          color: #666;
          background-color: #f0f0f0;
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
        }
      }

      .record-time {
        font-size: 28rpx;
        color: #666;
        font-weight: bold;
      }
    }
  }
}
</style>