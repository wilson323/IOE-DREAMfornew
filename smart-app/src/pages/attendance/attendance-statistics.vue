<template>
  <view class="attendance-statistics-page">
    <!-- 顶部导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="iconfont icon-back"></text>
      </view>
      <view class="nav-title">考勤统计</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 统计类型选择 -->
      <view class="stat-type-selector">
        <view
          class="stat-type-item"
          :class="{ active: currentStatType === 'personal' }"
          @click="switchStatType('personal')"
        >
          <text class="iconfont icon-user"></text>
          <text>个人统计</text>
        </view>
        <view
          class="stat-type-item"
          :class="{ active: currentStatType === 'department' }"
          @click="switchStatType('department')"
        >
          <text class="iconfont icon-team"></text>
          <text>部门统计</text>
        </view>
        <view
          class="stat-type-item"
          :class="{ active: currentStatType === 'company' }"
          @click="switchStatType('company')"
        >
          <text class="iconfont icon-company"></text>
          <text>公司统计</text>
        </view>
      </view>

      <!-- 日期筛选 -->
      <view class="date-filter">
        <view class="date-range">
          <view class="date-item" @click="showStartDatePicker">
            <text class="label">开始日期</text>
            <text class="value">{{ startDate }}</text>
          </view>
          <text class="separator">至</text>
          <view class="date-item" @click="showEndDatePicker">
            <text class="label">结束日期</text>
            <text class="value">{{ endDate }}</text>
          </view>
        </view>
        <button class="search-btn" @click="loadStatistics">
          <text class="iconfont icon-search"></text>
          <text>查询</text>
        </button>
      </view>

      <!-- 统计概览 -->
      <view class="stat-overview">
        <view class="overview-item">
          <view class="item-header">
            <text class="title">应出勤</text>
          </view>
          <view class="item-value">{{ statistics.shouldAttend || 0 }}天</view>
        </view>
        <view class="overview-item">
          <view class="item-header">
            <text class="title">实际出勤</text>
          </view>
          <view class="item-value">{{ statistics.actualAttend || 0 }}天</view>
        </view>
        <view class="overview-item">
          <view class="item-header">
            <text class="title">迟到</text>
          </view>
          <view class="item-value" :class="{ warning: (statistics.lateCount || 0) > 0 }">
            {{ statistics.lateCount || 0 }}次
          </view>
        </view>
        <view class="overview-item">
          <view class="item-header">
            <text class="title">早退</text>
          </view>
          <view class="item-value" :class="{ warning: (statistics.earlyLeaveCount || 0) > 0 }">
            {{ statistics.earlyLeaveCount || 0 }}次
          </view>
        </view>
        <view class="overview-item">
          <view class="item-header">
            <text class="title">缺勤</text>
          </view>
          <view class="item-value" :class="{ danger: (statistics.absenceCount || 0) > 0 }">
            {{ statistics.absenceCount || 0 }}天
          </view>
        </view>
        <view class="overview-item">
          <view class="item-header">
            <text class="title">加班</text>
          </view>
          <view class="item-value">{{ statistics.overtimeHours || 0 }}小时</view>
        </view>
      </view>

      <!-- 图表展示 -->
      <view class="chart-section">
        <view class="section-header">
          <text class="title">考勤趋势图</text>
        </view>
        <view class="chart-container">
          <!-- 这里应该使用图表库，如 uCharts -->
          <view class="chart-placeholder">
            <text>考勤趋势图表</text>
          </view>
        </view>
      </view>

      <!-- 详细统计列表 -->
      <view class="stat-details">
        <view class="section-header">
          <text class="title">详细统计</text>
          <button class="export-btn" @click="exportStatistics">
            <text class="iconfont icon-export"></text>
            <text>导出</text>
          </button>
        </view>

        <view class="details-list">
          <view class="detail-item" v-for="(item, index) in detailList" :key="index">
            <view class="item-header">
              <text class="date">{{ item.date }}</text>
              <text class="status" :class="item.statusClass">{{ item.status }}</text>
            </view>
            <view class="item-content">
              <view class="time-info">
                <text v-if="item.punchInTime">上班: {{ item.punchInTime }}</text>
                <text v-if="item.punchOutTime">下班: {{ item.punchOutTime }}</text>
              </view>
              <view class="work-info" v-if="item.workHours">
                <text>工时: {{ item.workHours }}小时</text>
              </view>
            </view>
          </view>
          <view class="no-data" v-if="detailList.length === 0">
            <text>暂无统计数据</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 日期选择器 -->
    <view class="date-picker-modal" v-if="showDatePicker">
      <view class="modal-overlay" @click="closeDatePicker"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="title">选择日期</text>
          <text class="close-btn" @click="closeDatePicker">×</text>
        </view>
        <view class="modal-body">
          <picker mode="date" :value="tempDate" @change="onDateChange">
            <view class="date-display">{{ tempDate }}</view>
          </picker>
        </view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeDatePicker">取消</button>
          <button class="confirm-btn" @click="confirmDate">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { statisticsApi } from '@/api/business/attendance/statistics-api.js'

export default {
  name: 'AttendanceStatistics',
  setup() {
    // 响应式数据
    const currentStatType = ref('personal')
    const startDate = ref('')
    const endDate = ref('')
    const showDatePicker = ref(false)
    const datePickerType = ref('') // 'start' 或 'end'
    const tempDate = ref('')
    const statistics = ref({})
    const detailList = ref([])

    // 初始化日期
    const initDates = () => {
      const now = new Date()
      const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
      startDate.value = formatDate(firstDay)
      endDate.value = formatDate(now)
    }

    const formatDate = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const switchStatType = (type) => {
      currentStatType.value = type
      loadStatistics()
    }

    const showStartDatePicker = () => {
      datePickerType.value = 'start'
      tempDate.value = startDate.value
      showDatePicker.value = true
    }

    const showEndDatePicker = () => {
      datePickerType.value = 'end'
      tempDate.value = endDate.value
      showDatePicker.value = true
    }

    const closeDatePicker = () => {
      showDatePicker.value = false
    }

    const onDateChange = (e) => {
      tempDate.value = e.detail.value
    }

    const confirmDate = () => {
      if (datePickerType.value === 'start') {
        startDate.value = tempDate.value
      } else {
        endDate.value = tempDate.value
      }
      closeDatePicker()
    }

    const loadStatistics = async () => {
      try {
        const params = {
          startDate: startDate.value,
          endDate: endDate.value
        }

        let result
        switch (currentStatType.value) {
          case 'personal':
            result = await statisticsApi.getPersonalStatistics(params)
            break
          case 'department':
            result = await statisticsApi.getDepartmentStatistics(params)
            break
          case 'company':
            result = await statisticsApi.getCompanyStatistics(params)
            break
        }

        if (result.success) {
          statistics.value = result.data.summary || {}
          detailList.value = result.data.details || []
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        uni.showToast({
          title: '加载统计数据失败',
          icon: 'none'
        })
      }
    }

    const exportStatistics = async () => {
      try {
        const params = {
          startDate: startDate.value,
          endDate: endDate.value,
          type: currentStatType.value
        }

        // 这里应该调用导出API
        // const result = await statisticsApi.exportStatistics(params)

        uni.showToast({
          title: '导出功能开发中',
          icon: 'none'
        })
      } catch (error) {
        uni.showToast({
          title: '导出失败',
          icon: 'none'
        })
      }
    }

    // 生命周期
    onMounted(() => {
      initDates()
      loadStatistics()
    })

    // 暴露给模板的数据和方法
    return {
      currentStatType,
      startDate,
      endDate,
      showDatePicker,
      datePickerType,
      tempDate,
      statistics,
      detailList,
      goBack,
      switchStatType,
      showStartDatePicker,
      showEndDatePicker,
      closeDatePicker,
      onDateChange,
      confirmDate,
      loadStatistics,
      exportStatistics
    }
  }
}
</script>

<style lang="scss" scoped>
.attendance-statistics-page {
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

.stat-type-selector {
  display: flex;
  background-color: #fff;
  border-radius: 10px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  .stat-type-item {
    flex: 1;
    text-align: center;
    padding: 15px 0;
    color: #666;
    font-size: 14px;

    .iconfont {
      font-size: 20px;
      margin-bottom: 5px;
      display: block;
    }

    &.active {
      background-color: #667eea;
      color: #fff;
    }
  }
}

.date-filter {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .date-range {
    flex: 1;
    display: flex;
    align-items: center;

    .date-item {
      flex: 1;
      text-align: center;

      .label {
        display: block;
        font-size: 12px;
        color: #999;
        margin-bottom: 3px;
      }

      .value {
        display: block;
        font-size: 14px;
        color: #333;
        font-weight: 500;
      }
    }

    .separator {
      margin: 0 10px;
      color: #999;
    }
  }

  .search-btn {
    height: 40px;
    background-color: #667eea;
    color: #fff;
    border: none;
    border-radius: 5px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 15px;
    font-size: 14px;

    .iconfont {
      font-size: 16px;
      margin-right: 5px;
    }
  }
}

.stat-overview {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;

  .overview-item {
    background-color: #fff;
    border-radius: 10px;
    padding: 15px 10px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .item-header {
      margin-bottom: 8px;

      .title {
        font-size: 14px;
        color: #666;
      }
    }

    .item-value {
      font-size: 18px;
      font-weight: 500;
      color: #333;

      &.warning {
        color: #ff9800;
      }

      &.danger {
        color: #f44336;
      }
    }
  }
}

.chart-section {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .section-header {
    margin-bottom: 15px;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .chart-container {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;

    .chart-placeholder {
      text-align: center;
      color: #999;
    }
  }
}

.stat-details {
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

    .export-btn {
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

  .details-list {
    .detail-item {
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;

      .item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;

        .date {
          font-size: 16px;
          font-weight: 500;
          color: #333;
        }

        .status {
          font-size: 12px;
          padding: 3px 8px;
          border-radius: 3px;
          background-color: #e0e0e0;
          color: #666;

          &.normal {
            background-color: #e8f5e9;
            color: #4caf50;
          }

          &.late {
            background-color: #fff3e0;
            color: #ff9800;
          }

          &.early {
            background-color: #fce4ec;
            color: #f44336;
          }

          &.absent {
            background-color: #ffebee;
            color: #f44336;
          }
        }
      }

      .item-content {
        .time-info {
          display: flex;
          justify-content: space-between;
          font-size: 14px;
          color: #666;
          margin-bottom: 5px;
        }

        .work-info {
          font-size: 14px;
          color: #666;
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

.date-picker-modal {
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
    width: 80%;
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
      padding: 30px 20px;
      text-align: center;

      .date-display {
        font-size: 18px;
        color: #333;
        padding: 10px;
        border: 1px solid #ddd;
        border-radius: 5px;
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