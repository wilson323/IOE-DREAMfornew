<template>
  <view class="attendance-punch-page">
    <!-- 顶部导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="iconfont icon-back"></text>
      </view>
      <view class="nav-title">考勤打卡</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 当前时间显示 -->
      <view class="time-section">
        <view class="current-time">{{ currentTime }}</view>
        <view class="current-date">{{ currentDate }}</view>
      </view>

      <!-- 打卡状态显示 -->
      <view class="punch-status-section" v-if="todayRecord">
        <view class="status-item">
          <text class="label">上班时间:</text>
          <text class="value">{{ todayRecord.punchInTime || '未打卡' }}</text>
        </view>
        <view class="status-item">
          <text class="label">下班时间:</text>
          <text class="value">{{ todayRecord.punchOutTime || '未打卡' }}</text>
        </view>
        <view class="status-item">
          <text class="label">工作时长:</text>
          <text class="value">{{ todayRecord.workHours || '0小时' }}</text>
        </view>
      </view>

      <!-- 打卡按钮 -->
      <view class="punch-button-section">
        <button
          class="punch-button"
          :class="{ 'punch-in': !hasPunchedIn, 'punch-out': hasPunchedIn && !hasPunchedOut, 'disabled': isPunching }"
          @click="handlePunch"
          :disabled="isPunching"
        >
          <view class="button-content">
            <text class="iconfont" :class="punchIcon"></text>
            <text class="button-text">{{ punchButtonText }}</text>
          </view>
        </button>
      </view>

      <!-- 位置信息 -->
      <view class="location-section">
        <view class="location-header">
          <text class="iconfont icon-location"></text>
          <text class="title">当前位置</text>
        </view>
        <view class="location-info">
          <text v-if="location.address">{{ location.address }}</text>
          <text v-else>正在获取位置信息...</text>
        </view>
        <button class="refresh-location-btn" @click="refreshLocation">
          <text class="iconfont icon-refresh"></text>
          <text>刷新位置</text>
        </button>
      </view>

      <!-- 历史打卡记录 -->
      <view class="history-section">
        <view class="section-header">
          <text class="title">今日打卡记录</text>
        </view>
        <view class="history-list">
          <view class="history-item" v-for="(record, index) in todayRecords" :key="index">
            <view class="time">{{ record.punchTime }}</view>
            <view class="type">{{ record.punchType }}</view>
            <view class="location">{{ record.location }}</view>
          </view>
          <view class="no-data" v-if="todayRecords.length === 0">
            <text>暂无打卡记录</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 打卡成功弹窗 -->
    <view class="success-modal" v-if="showSuccessModal">
      <view class="modal-content">
        <view class="success-icon">
          <text class="iconfont icon-success"></text>
        </view>
        <view class="success-message">{{ successMessage }}</view>
        <button class="confirm-btn" @click="closeSuccessModal">确定</button>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { attendanceApi } from '@/api/business/attendance/attendance-api.js'

export default {
  name: 'AttendancePunch',
  setup() {
    // 响应式数据
    const currentTime = ref('')
    const currentDate = ref('')
    const isPunching = ref(false)
    const showSuccessModal = ref(false)
    const successMessage = ref('')
    const todayRecord = ref(null)
    const todayRecords = ref([])
    const location = reactive({
      latitude: '',
      longitude: '',
      address: ''
    })

    // 计算属性
    const hasPunchedIn = computed(() => {
      return todayRecord.value && todayRecord.value.punchInTime
    })

    const hasPunchedOut = computed(() => {
      return todayRecord.value && todayRecord.value.punchOutTime
    })

    const punchButtonText = computed(() => {
      if (!hasPunchedIn.value) return '上班打卡'
      if (hasPunchedIn.value && !hasPunchedOut.value) return '下班打卡'
      return '已打卡完成'
    })

    const punchIcon = computed(() => {
      if (!hasPunchedIn.value) return 'icon-clock-in'
      if (hasPunchedIn.value && !hasPunchedOut.value) return 'icon-clock-out'
      return 'icon-check'
    })

    // 方法
    const updateTime = () => {
      const now = new Date()
      currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
      currentDate.value = now.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long'
      })
    }

    const goBack = () => {
      uni.navigateBack()
    }

    const getLocation = () => {
      // 获取地理位置
      uni.getLocation({
        type: 'wgs84',
        success: (res) => {
          location.latitude = res.latitude
          location.longitude = res.longitude

          // 获取地址信息
          uni.request({
            url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${res.latitude},${res.longitude}&key=YOUR_API_KEY&get_poi=1`,
            success: (addressRes) => {
              if (addressRes.data.status === 0) {
                location.address = addressRes.data.result.address
              }
            }
          })
        },
        fail: (err) => {
          console.error('获取位置失败:', err)
          location.address = '位置获取失败'
        }
      })
    }

    const refreshLocation = () => {
      getLocation()
    }

    const handlePunch = async () => {
      if (isPunching.value) return

      isPunching.value = true

      try {
        const punchType = !hasPunchedIn.value ? '上班' : '下班'
        const params = {
          punchTime: new Date().toISOString(),
          location: location.address || '未知位置',
          latitude: location.latitude,
          longitude: location.longitude
        }

        let result
        if (punchType === '上班') {
          result = await attendanceApi.punchIn(params)
        } else {
          result = await attendanceApi.punchOut(params)
        }

        if (result.success) {
          successMessage.value = `${punchType}打卡成功！`
          showSuccessModal.value = true
          await getTodayPunchRecords()
        } else {
          uni.showToast({
            title: result.msg || '打卡失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.showToast({
          title: '打卡失败，请重试',
          icon: 'none'
        })
      } finally {
        isPunching.value = false
      }
    }

    const getTodayPunchRecords = async () => {
      try {
        const result = await attendanceApi.getTodayPunchRecord()
        if (result.success) {
          todayRecord.value = result.data
          // 如果有记录，更新历史记录
          if (result.data && result.data.punchRecords) {
            todayRecords.value = result.data.punchRecords
          }
        }
      } catch (error) {
        console.error('获取今日打卡记录失败:', error)
      }
    }

    const closeSuccessModal = () => {
      showSuccessModal.value = false
    }

    // 生命周期
    onMounted(() => {
      // 更新时间
      updateTime()
      const timer = setInterval(updateTime, 1000)

      // 获取位置信息
      getLocation()

      // 获取今日打卡记录
      getTodayPunchRecords()

      // 组件卸载时清除定时器
      onUnmounted(() => {
        clearInterval(timer)
      })
    })

    // 暴露给模板的数据和方法
    return {
      currentTime,
      currentDate,
      isPunching,
      showSuccessModal,
      successMessage,
      todayRecord,
      todayRecords,
      location,
      hasPunchedIn,
      hasPunchedOut,
      punchButtonText,
      punchIcon,
      goBack,
      refreshLocation,
      handlePunch,
      closeSuccessModal
    }
  }
}
</script>

<style lang="scss" scoped>
.attendance-punch-page {
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

.time-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  color: #fff;
  margin-bottom: 15px;

  .current-time {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 5px;
  }

  .current-date {
    font-size: 14px;
    opacity: 0.9;
  }
}

.punch-status-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .status-item {
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

.punch-button-section {
  text-align: center;
  margin-bottom: 20px;

  .punch-button {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    border: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 500;
    color: #fff;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;

    &.punch-in {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.punch-out {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }

    &.disabled {
      opacity: 0.6;
    }

    .button-content {
      display: flex;
      flex-direction: column;
      align-items: center;

      .iconfont {
        font-size: 32px;
        margin-bottom: 5px;
      }
    }
  }
}

.location-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .location-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .iconfont {
      font-size: 18px;
      color: #667eea;
      margin-right: 5px;
    }

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .location-info {
    padding: 10px;
    background-color: #f9f9f9;
    border-radius: 5px;
    margin-bottom: 10px;
    min-height: 40px;
    display: flex;
    align-items: center;
  }

  .refresh-location-btn {
    width: 100%;
    height: 40px;
    background-color: #667eea;
    color: #fff;
    border: none;
    border-radius: 5px;
    display: flex;
    align-items: center;
    justify-content: center;

    .iconfont {
      font-size: 16px;
      margin-right: 5px;
    }
  }
}

.history-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .section-header {
    margin-bottom: 15px;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .history-list {
    .history-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;

      .time {
        color: #666;
        font-size: 14px;
      }

      .type {
        color: #333;
        font-weight: 500;
      }

      .location {
        color: #999;
        font-size: 12px;
        flex: 1;
        text-align: right;
        margin-left: 10px;
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

.success-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;

  .modal-content {
    background-color: #fff;
    border-radius: 10px;
    padding: 30px;
    width: 80%;
    text-align: center;

    .success-icon {
      .iconfont {
        font-size: 48px;
        color: #4caf50;
        margin-bottom: 15px;
      }
    }

    .success-message {
      font-size: 16px;
      color: #333;
      margin-bottom: 20px;
    }

    .confirm-btn {
      width: 100%;
      height: 40px;
      background-color: #667eea;
      color: #fff;
      border: none;
      border-radius: 5px;
      font-size: 16px;
    }
  }
}
</style>