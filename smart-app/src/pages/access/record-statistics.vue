<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px', height: (statusBarHeight + 44) + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">
          <text class="title-text">通行统计</text>
        </view>
        <view class="navbar-right">
          <view class="date-btn" @tap="showDateRangePicker">
            <uni-icons type="calendar" size="18" color="#fff"></uni-icons>
            <text class="date-text">{{ currentDateRange }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <scroll-view class="page-content" scroll-y enable-back-to-top @scrolltolower="loadMoreData">
      <!-- 统计概览卡片 -->
      <view class="overview-section">
        <view class="overview-card primary">
          <view class="card-header">
            <view class="header-icon">
              <uni-icons type="person" size="24" color="#fff"></uni-icons>
            </view>
            <text class="header-title">总通行人次</text>
          </view>
          <view class="card-content">
            <text class="card-value">{{ statisticsData.totalPasses }}</text>
            <view class="card-trend" :class="{ up: statisticsData.passTrend > 0 }">
              <uni-icons :type="statisticsData.passTrend > 0 ? 'up' : 'down'" size="14" :color="statisticsData.passTrend > 0 ? '#22c55e' : '#ef4444'"></uni-icons>
              <text>{{ Math.abs(statisticsData.passTrend) }}%</text>
            </view>
          </view>
        </view>

        <view class="overview-grid">
          <view class="overview-item success">
            <view class="item-icon">
              <uni-icons type="checkmarkempty" size="20" color="#fff"></uni-icons>
            </view>
            <view class="item-content">
              <text class="item-label">成功通行</text>
              <text class="item-value">{{ statisticsData.successCount }}</text>
            </view>
          </view>

          <view class="overview-item denied">
            <view class="item-icon">
              <uni-icons type="closeempty" size="20" color="#fff"></uni-icons>
            </view>
            <view class="item-content">
              <text class="item-label">拒绝通行</text>
              <text class="item-value">{{ statisticsData.deniedCount }}</text>
            </view>
          </view>

          <view class="overview-item alarm">
            <view class="item-icon">
              <uni-icons type="notification" size="20" color="#fff"></uni-icons>
            </view>
            <view class="item-content">
              <text class="item-label">告警次数</text>
              <text class="item-value">{{ statisticsData.alarmCount }}</text>
            </view>
          </view>

          <view class="overview-item info">
            <view class="item-icon">
              <uni-icons type="person" size="20" color="#fff"></uni-icons>
            </view>
            <view class="item-content">
              <text class="item-label">通行人数</text>
              <text class="item-value">{{ statisticsData.uniquePersons }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 时间段分布 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="clock" size="18" color="#667eea"></uni-icons>
          <text class="section-title">通行时段分布</text>
          <view class="chart-type-switch">
            <view
              class="type-btn"
              :class="{ active: timeChartType === 'line' }"
              @tap="switchTimeChartType('line')"
            >
              <text>折线图</text>
            </view>
            <view
              class="type-btn"
              :class="{ active: timeChartType === 'bar' }"
              @tap="switchTimeChartType('bar')"
            >
              <text>柱状图</text>
            </view>
          </view>
        </view>
        <view class="chart-container">
          <view class="chart-placeholder">
            <text class="placeholder-text">{{ timeChartType === 'line' ? '折线图区域' : '柱状图区域' }}</text>
            <text class="placeholder-desc">显示24小时通行分布</text>
          </view>
        </view>
        <view class="time-stats">
          <view class="stat-item">
            <text class="stat-label">高峰时段</text>
            <text class="stat-value primary">{{ statisticsData.peakHours }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">平均每小时</text>
            <text class="stat-value">{{ statisticsData.avgPerHour }}</text>
          </view>
        </view>
      </view>

      <!-- 通行方式分布 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="scan" size="18" color="#667eea"></uni-icons>
          <text class="section-title">通行方式分布</text>
        </view>
        <view class="verification-grid">
          <view
            class="verification-item"
            v-for="(item, index) in verificationData"
            :key="index"
          >
            <view class="verification-icon" :class="item.type">
              <uni-icons :type="getVerificationIcon(item.type)" size="28" color="#fff"></uni-icons>
            </view>
            <view class="verification-info">
              <text class="verification-name">{{ item.name }}</text>
              <text class="verification-count">{{ item.count }}次</text>
              <view class="verification-bar">
                <view
                  class="bar-fill"
                  :class="item.type"
                  :style="{ width: item.percent + '%' }"
                ></view>
              </view>
              <text class="verification-percent">{{ item.percent }}%</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 区域通行排行 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="location" size="18" color="#667eea"></uni-icons>
          <text class="section-title">区域通行排行</text>
          <view class="rank-type-selector" @tap="showRankTypePopup">
            <text>{{ selectedRankType }}</text>
            <uni-icons type="down" size="12" color="#999"></uni-icons>
          </view>
        </view>
        <view class="rank-list">
          <view
            class="rank-item"
            v-for="(area, index) in areaRankings"
            :key="area.areaId"
          >
            <view class="rank-number" :class="getRankClass(index)">
              <text>{{ index + 1 }}</text>
            </view>
            <view class="rank-info">
              <text class="area-name">{{ area.areaName }}</text>
              <text class="area-count">{{ area.count }}次通行</text>
            </view>
            <view class="rank-chart">
              <view
                class="chart-bar"
                :style="{ width: (area.count / areaRankings[0].count * 100) + '%' }"
              ></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 设备通行排行 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="gear" size="18" color="#667eea"></uni-icons>
          <text class="section-title">设备通行排行</text>
        </view>
        <view class="device-rank-list">
          <view
            class="device-rank-item"
            v-for="(device, index) in deviceRankings"
            :key="device.deviceId"
          >
            <view class="device-rank">
              <text class="rank-text" :class="getRankClass(index)">{{ index + 1 }}</text>
            </view>
            <view class="device-info">
              <view class="device-top">
                <text class="device-name">{{ device.deviceName }}</text>
                <text class="device-count">{{ device.count }}次</text>
              </view>
              <text class="device-location">{{ device.location }}</text>
            </view>
            <view class="device-status">
              <view class="status-dot" :class="device.online ? 'online' : 'offline'"></view>
              <text class="status-text" :class="device.online ? 'online' : 'offline'">
                {{ device.online ? '在线' : '离线' }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 人员通行排行 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="person-filled" size="18" color="#667eea"></uni-icons>
          <text class="section-title">人员通行排行</text>
          <view class="tab-buttons">
            <view
              class="tab-btn"
              :class="{ active: personRankTab === 'top' }"
              @tap="switchPersonRankTab('top')"
            >
              <text>通行最多</text>
            </view>
            <view
              class="tab-btn"
              :class="{ active: personRankTab === 'frequent' }"
              @tap="switchPersonRankTab('frequent')"
            >
              <text>最频繁</text>
            </view>
          </view>
        </view>
        <view class="person-rank-list">
          <view
            class="person-rank-item"
            v-for="(person, index) in currentPersonRankings"
            :key="person.personId"
          >
            <view class="person-avatar">
              <image :src="person.avatar || '/static/default-avatar.png'" mode="aspectFill"></image>
            </view>
            <view class="person-info">
              <view class="person-top">
                <text class="person-name">{{ person.personName }}</text>
                <text class="person-count">{{ person.count }}次</text>
              </view>
              <text class="person-dept">{{ person.department }}</text>
            </view>
            <view class="person-trend" :class="{ up: person.trend > 0 }">
              <uni-icons :type="person.trend > 0 ? 'up' : 'down'" size="14" :color="person.trend > 0 ? '#22c55e' : '#ef4444'"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 告警统计 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="notification" size="18" color="#667eea"></uni-icons>
          <text class="section-title">告警统计</text>
        </view>
        <view class="alarm-grid">
          <view
            class="alarm-item"
            v-for="(alarm, index) in alarmStatistics"
            :key="index"
          >
            <view class="alarm-icon" :class="alarm.level">
              <uni-icons :type="getAlarmIcon(alarm.type)" size="24" color="#fff"></uni-icons>
            </view>
            <text class="alarm-name">{{ alarm.name }}</text>
            <text class="alarm-count">{{ alarm.count }}次</text>
          </view>
        </view>
        <view class="alarm-trend" @tap="viewAlarmDetail">
          <text class="trend-text">查看告警详情</text>
          <uni-icons type="right" size="14" color="#999"></uni-icons>
        </view>
      </view>

      <!-- 对比分析 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="loop" size="18" color="#667eea"></uni-icons>
          <text class="section-title">环比分析</text>
        </view>
        <view class="comparison-list">
          <view class="comparison-item">
            <text class="comparison-label">较昨日</text>
            <view class="comparison-value" :class="{ up: statisticsData.vsYesterday > 0 }">
              <uni-icons :type="statisticsData.vsYesterday > 0 ? 'up' : 'down'" size="16" :color="statisticsData.vsYesterday > 0 ? '#22c55e' : '#ef4444'"></uni-icons>
              <text>{{ Math.abs(statisticsData.vsYesterday) }}%</text>
            </view>
          </view>
          <view class="comparison-item">
            <text class="comparison-label">较上周</text>
            <view class="comparison-value" :class="{ up: statisticsData.vsLastWeek > 0 }">
              <uni-icons :type="statisticsData.vsLastWeek > 0 ? 'up' : 'down'" size="16" :color="statisticsData.vsLastWeek > 0 ? '#22c55e' : '#ef4444'"></uni-icons>
              <text>{{ Math.abs(statisticsData.vsLastWeek) }}%</text>
            </view>
          </view>
          <view class="comparison-item">
            <text class="comparison-label">较上月</text>
            <view class="comparison-value" :class="{ up: statisticsData.vsLastMonth > 0 }">
              <uni-icons :type="statisticsData.vsLastMonth > 0 ? 'up' : 'down'" size="16" :color="statisticsData.vsLastMonth > 0 ? '#22c55e' : '#ef4444'"></uni-icons>
              <text>{{ Math.abs(statisticsData.vsLastMonth) }}%</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 日期范围选择弹窗 -->
    <uni-popup ref="dateRangePopup" type="bottom">
      <view class="date-range-popup">
        <view class="popup-header">
          <text class="popup-title">选择日期范围</text>
          <view class="close-btn" @tap="closeDateRangePopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <view class="date-presets">
          <view
            class="preset-btn"
            :class="{ active: selectedDatePreset === 'today' }"
            @tap="selectDatePreset('today')"
          >
            <text>今天</text>
          </view>
          <view
            class="preset-btn"
            :class="{ active: selectedDatePreset === 'yesterday' }"
            @tap="selectDatePreset('yesterday')"
          >
            <text>昨天</text>
          </view>
          <view
            class="preset-btn"
            :class="{ active: selectedDatePreset === 'week' }"
            @tap="selectDatePreset('week')"
          >
            <text>本周</text>
          </view>
          <view
            class="preset-btn"
            :class="{ active: selectedDatePreset === 'month' }"
            @tap="selectDatePreset('month')"
          >
            <text>本月</text>
          </view>
          <view
            class="preset-btn"
            :class="{ active: selectedDatePreset === 'custom' }"
            @tap="selectDatePreset('custom')"
          >
            <text>自定义</text>
          </view>
        </view>
        <view class="custom-date-range" v-if="selectedDatePreset === 'custom'">
          <view class="date-field">
            <text class="field-label">开始日期</text>
            <picker mode="date" :value="customStartDate" @change="onStartDateChange">
              <view class="picker-input">
                <text>{{ customStartDate || '请选择' }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
          <view class="date-field">
            <text class="field-label">结束日期</text>
            <picker mode="date" :value="customEndDate" @change="onEndDateChange">
              <view class="picker-input">
                <text>{{ customEndDate || '请选择' }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
        <view class="popup-footer">
          <view class="footer-btn secondary" @tap="resetDateRange">
            <text>重置</text>
          </view>
          <view class="footer-btn primary" @tap="applyDateRange">
            <text>确定</text>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 排行类型选择弹窗 -->
    <uni-popup ref="rankTypePopup" type="bottom">
      <view class="rank-type-popup">
        <view class="popup-header">
          <text class="popup-title">选择排行类型</text>
          <view class="close-btn" @tap="closeRankTypePopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <view class="rank-type-options">
          <view
            class="type-option"
            :class="{ active: selectedRankType === item }"
            v-for="item in rankTypes"
            :key="item"
            @tap="selectRankType(item)"
          >
            <text>{{ item }}</text>
            <uni-icons type="checkmarkempty" size="18" color="#667eea" v-if="selectedRankType === item"></uni-icons>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 当前日期范围
const currentDateRange = ref('今天')

// 统计数据
const statisticsData = reactive({
  totalPasses: 3582,
  passTrend: 12.5,
  successCount: 3421,
  deniedCount: 128,
  alarmCount: 33,
  uniquePersons: 486,
  peakHours: '08:00-09:00',
  avgPerHour: 149,
  vsYesterday: 8.3,
  vsLastWeek: 15.2,
  vsLastMonth: -3.5,
})

// 时间图表类型
const timeChartType = ref('line')

// 通行方式数据
const verificationData = ref([
  { type: 'face', name: '人脸识别', count: 1823, percent: 51 },
  { type: 'card', name: '刷卡', count: 956, percent: 27 },
  { type: 'fingerprint', name: '指纹', count: 482, percent: 13 },
  { type: 'qrcode', name: '二维码', count: 256, percent: 7 },
  { type: 'password', name: '密码', count: 65, percent: 2 },
])

// 区域通行排行
const areaRankings = ref([
  { areaId: 1, areaName: 'A栋1楼大厅', count: 856 },
  { areaId: 2, areaName: 'A栋2楼办公区', count: 642 },
  { areaId: 3, areaName: 'B栋1楼大厅', count: 534 },
  { areaId: 4, areaName: 'B栋2楼会议室', count: 428 },
  { areaId: 5, areaName: 'C栋1楼大厅', count: 315 },
])

// 设备通行排行
const deviceRankings = ref([
  { deviceId: 1, deviceName: '主入口门禁-1', location: 'A栋1楼大厅', count: 456, online: true },
  { deviceId: 2, deviceName: '主入口门禁-2', location: 'A栋1楼大厅', count: 398, online: true },
  { deviceId: 3, deviceName: '办公区门禁-1', location: 'A栋2楼办公区', count: 342, online: false },
  { deviceId: 4, deviceName: '会议室门禁', location: 'B栋2楼会议室', count: 256, online: true },
  { deviceId: 5, deviceName: '侧门门禁', location: 'C栋1楼大厅', count: 198, online: false },
])

// 人员通行排行
const personRankTop = ref([
  { personId: 1, personName: '张三', department: '技术部', count: 28, avatar: '', trend: 12 },
  { personId: 2, personName: '李四', department: '市场部', count: 25, avatar: '', trend: 8 },
  { personId: 3, personName: '王五', department: '行政部', count: 22, avatar: '', trend: -3 },
  { personId: 4, personName: '赵六', department: '技术部', count: 20, avatar: '', trend: 15 },
  { personId: 5, personName: '钱七', department: '财务部', count: 18, avatar: '', trend: 5 },
])

const personRankFrequent = ref([
  { personId: 6, personName: '孙八', department: '运营部', count: 15, avatar: '', trend: 20 },
  { personId: 7, personName: '周九', department: '市场部', count: 14, avatar: '', trend: 10 },
  { personId: 8, personName: '吴十', department: '技术部', count: 13, avatar: '', trend: -5 },
  { personId: 9, personName: '郑十一', department: '人事部', count: 12, avatar: '', trend: 8 },
  { personId: 10, personName: '王十二', department: '行政部', count: 11, avatar: '', trend: -2 },
])

const personRankTab = ref('top')
const currentPersonRankings = computed(() => {
  return personRankTab.value === 'top' ? personRankTop.value : personRankFrequent.value
})

// 告警统计
const alarmStatistics = ref([
  { type: 'illegal', name: '非法通行', count: 15, level: 'high' },
  { type: 'timeout', name: '超时未归', count: 8, level: 'medium' },
  { type: 'tailgating', name: '尾随', count: 5, level: 'high' },
  { type: 'forced', name: '强行闯入', count: 3, level: 'high' },
  { type: 'device', name: '设备异常', count: 2, level: 'low' },
])

// 日期范围选择
const selectedDatePreset = ref('today')
const customStartDate = ref('')
const customEndDate = ref('')

// 排行类型
const rankTypes = ['通行次数', '进入次数', '离开次数']
const selectedRankType = ref('通行次数')

// 弹窗引用
const dateRangePopup = ref(null)
const rankTypePopup = ref(null)

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 切换时间图表类型
const switchTimeChartType = (type) => {
  timeChartType.value = type
}

// 获取认证方式图标
const getVerificationIcon = (type) => {
  const iconMap = {
    face: 'person',
    card: 'bankcard',
    fingerprint: 'fingerprint',
    qrcode: 'qrcode',
    password: 'locked',
  }
  return iconMap[type] || 'help'
}

// 获取排行样式类
const getRankClass = (index) => {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return ''
}

// 获取告警图标
const getAlarmIcon = (type) => {
  const iconMap = {
    illegal: 'no',
    timeout: 'clock',
    tailgating: 'person',
    forced: 'locked',
    device: 'gear',
  }
  return iconMap[type] || 'notification'
}

// 切换人员排行标签
const switchPersonRankTab = (tab) => {
  personRankTab.value = tab
}

// 查看告警详情
const viewAlarmDetail = () => {
  uni.navigateTo({
    url: '/pages/access/record-list?status=alarm',
  })
}

// 显示日期范围选择器
const showDateRangePicker = () => {
  dateRangePopup.value?.open()
}

// 关闭日期范围弹窗
const closeDateRangePopup = () => {
  dateRangePopup.value?.close()
}

// 选择日期预设
const selectDatePreset = (preset) => {
  selectedDatePreset.value = preset
  if (preset !== 'custom') {
    currentDateRange.value = getDateRangeText(preset)
  }
}

// 获取日期范围文本
const getDateRangeText = (preset) => {
  const presetMap = {
    today: '今天',
    yesterday: '昨天',
    week: '本周',
    month: '本月',
    custom: '自定义',
  }
  return presetMap[preset] || preset
}

// 开始日期变更
const onStartDateChange = (e) => {
  customStartDate.value = e.detail.value
}

// 结束日期变更
const onEndDateChange = (e) => {
  customEndDate.value = e.detail.value
}

// 重置日期范围
const resetDateRange = () => {
  selectedDatePreset.value = 'today'
  customStartDate.value = ''
  customEndDate.value = ''
  currentDateRange.value = '今天'
}

// 应用日期范围
const applyDateRange = () => {
  if (selectedDatePreset.value === 'custom') {
    if (!customStartDate.value || !customEndDate.value) {
      uni.showToast({
        title: '请选择完整的日期范围',
        icon: 'none',
      })
      return
    }
    currentDateRange.value = `${customStartDate.value} 至 ${customEndDate.value}`
  } else {
    currentDateRange.value = getDateRangeText(selectedDatePreset.value)
  }
  closeDateRangePopup()
  // 重新加载统计数据
  loadStatisticsData()
}

// 显示排行类型弹窗
const showRankTypePopup = () => {
  rankTypePopup.value?.open()
}

// 关闭排行类型弹窗
const closeRankTypePopup = () => {
  rankTypePopup.value?.close()
}

// 选择排行类型
const selectRankType = (type) => {
  selectedRankType.value = type
  closeRankTypePopup()
  // 重新加载排行数据
  loadRankingData()
}

// 加载统计数据
const loadStatisticsData = () => {
  // TODO: 调用API加载统计数据
  uni.showLoading({ title: '加载中...' })
  setTimeout(() => {
    uni.hideLoading()
  }, 500)
}

// 加载排行数据
const loadRankingData = () => {
  // TODO: 调用API加载排行数据
  uni.showLoading({ title: '加载中...' })
  setTimeout(() => {
    uni.hideLoading()
  }, 500)
}

// 加载更多数据
const loadMoreData = () => {
  // TODO: 实现分页加载
}

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

onMounted(() => {
  getStatusBarHeight()
  loadStatisticsData()
})
</script>

<style lang="scss" scoped>
.page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e4e8ec 100%);
}

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
      gap: 8rpx;

      .back-text {
        font-size: 28rpx;
        color: #fff;
      }
    }

    .navbar-title {
      flex: 1;
      text-align: center;

      .title-text {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .navbar-right {
      width: 100rpx;
      display: flex;
      justify-content: flex-end;

      .date-btn {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .date-text {
          font-size: 24rpx;
          color: #fff;
        }
      }
    }
  }
}

.page-content {
  padding-top: calc(var(--status-bar-height) + 44px);
  padding-bottom: 40rpx;
  min-height: 100vh;
}

// 统计概览
.overview-section {
  padding: 30rpx;

  .overview-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);

    &.primary {
      .card-header {
        display: flex;
        align-items: center;
        gap: 16rpx;
        margin-bottom: 30rpx;

        .header-icon {
          width: 64rpx;
          height: 64rpx;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 16rpx;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .header-title {
          font-size: 30rpx;
          color: rgba(255, 255, 255, 0.9);
        }
      }

      .card-content {
        display: flex;
        align-items: flex-end;
        justify-content: space-between;

        .card-value {
          font-size: 72rpx;
          font-weight: 700;
          color: #fff;
          line-height: 1;
        }

        .card-trend {
          display: flex;
          align-items: center;
          gap: 8rpx;
          padding: 12rpx 20rpx;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 40rpx;
          font-size: 26rpx;
          color: #fff;

          &.up {
            background: rgba(34, 197, 94, 0.2);
          }
        }
      }
    }
  }

  .overview-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20rpx;

    .overview-item {
      padding: 30rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      gap: 20rpx;

      &.success {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
      }

      &.denied {
        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
      }

      &.alarm {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.info {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      }

      .item-icon {
        width: 56rpx;
        height: 56rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .item-content {
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .item-label {
          font-size: 24rpx;
          color: rgba(255, 255, 255, 0.8);
        }

        .item-value {
          font-size: 36rpx;
          font-weight: 700;
          color: #fff;
          line-height: 1;
        }
      }
    }
  }
}

.section-card {
  background: #fff;
  margin: 20rpx 30rpx;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 30rpx;

    .section-title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .chart-type-switch {
      display: flex;
      gap: 8rpx;

      .type-btn {
        padding: 8rpx 20rpx;
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #666;
        background: #f5f7fa;
        transition: all 0.3s;

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
        }
      }
    }

    .rank-type-selector {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 8rpx 20rpx;
      background: #f5f7fa;
      border-radius: 8rpx;
      font-size: 24rpx;
      color: #666;
    }

    .tab-buttons {
      display: flex;
      gap: 8rpx;

      .tab-btn {
        padding: 8rpx 20rpx;
        border-radius: 8rpx;
        font-size: 24rpx;
        color: #666;
        background: #f5f7fa;
        transition: all 0.3s;

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
        }
      }
    }
  }
}

// 图表容器
.chart-container {
  height: 400rpx;
  background: #f5f7fa;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;

  .chart-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;

    .placeholder-text {
      font-size: 28rpx;
      color: #999;
    }

    .placeholder-desc {
      font-size: 24rpx;
      color: #bbb;
    }
  }
}

.time-stats {
  display: flex;
  gap: 30rpx;

  .stat-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    padding: 20rpx;
    background: #f5f7fa;
    border-radius: 12rpx;

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }

    .stat-value {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;

      &.primary {
        color: #667eea;
      }
    }
  }
}

// 通行方式分布
.verification-grid {
  display: flex;
  flex-direction: column;
  gap: 30rpx;

  .verification-item {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .verification-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.face {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.card {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      }

      &.fingerprint {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
      }

      &.qrcode {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.password {
        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
      }
    }

    .verification-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 12rpx;

      .verification-name {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }

      .verification-count {
        font-size: 24rpx;
        color: #999;
      }

      .verification-bar {
        width: 100%;
        height: 12rpx;
        background: #f5f7fa;
        border-radius: 6rpx;
        overflow: hidden;

        .bar-fill {
          height: 100%;
          border-radius: 6rpx;
          transition: width 0.5s;

          &.face {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          }

          &.card {
            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
          }

          &.fingerprint {
            background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
          }

          &.qrcode {
            background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
          }

          &.password {
            background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
          }
        }
      }

      .verification-percent {
        font-size: 22rpx;
        color: #999;
      }
    }
  }
}

// 区域通行排行
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  .rank-item {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .rank-number {
      width: 56rpx;
      height: 56rpx;
      border-radius: 12rpx;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      font-weight: 600;
      color: #999;

      &.rank-first {
        background: linear-gradient(135deg, #ffd700 0%, #ffb300 100%);
        color: #fff;
      }

      &.rank-second {
        background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
        color: #fff;
      }

      &.rank-third {
        background: linear-gradient(135deg, #cd7f32 0%, #b8722b 100%);
        color: #fff;
      }
    }

    .rank-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .area-name {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }

      .area-count {
        font-size: 24rpx;
        color: #999;
      }
    }

    .rank-chart {
      width: 200rpx;

      .chart-bar {
        height: 12rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 6rpx;
        transition: width 0.5s;
      }
    }
  }
}

// 设备通行排行
.device-rank-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  .device-rank-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 24rpx;
    background: #f5f7fa;
    border-radius: 16rpx;

    .device-rank {
      .rank-text {
        font-size: 32rpx;
        font-weight: 700;
        color: #999;

        &.rank-first {
          color: #ffd700;
        }

        &.rank-second {
          color: #c0c0c0;
        }

        &.rank-third {
          color: #cd7f32;
        }
      }
    }

    .device-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .device-top {
        display: flex;
        justify-content: space-between;

        .device-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }

        .device-count {
          font-size: 26rpx;
          color: #667eea;
          font-weight: 600;
        }
      }

      .device-location {
        font-size: 24rpx;
        color: #999;
      }
    }

    .device-status {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;

      .status-dot {
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;

        &.online {
          background: #22c55e;
        }

        &.offline {
          background: #ef4444;
        }
      }

      .status-text {
        font-size: 22rpx;

        &.online {
          color: #22c55e;
        }

        &.offline {
          color: #ef4444;
        }
      }
    }
  }
}

// 人员通行排行
.person-rank-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  .person-rank-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 24rpx;
    background: #f5f7fa;
    border-radius: 16rpx;

    .person-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 50%;
      overflow: hidden;

      image {
        width: 100%;
        height: 100%;
      }
    }

    .person-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .person-top {
        display: flex;
        justify-content: space-between;

        .person-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }

        .person-count {
          font-size: 26rpx;
          color: #667eea;
          font-weight: 600;
        }
      }

      .person-dept {
        font-size: 24rpx;
        color: #999;
      }
    }

    .person-trend {
      &.up {
        color: #22c55e;
      }
    }
  }
}

// 告警统计
.alarm-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;

  .alarm-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    padding: 30rpx 20rpx;
    background: #f5f7fa;
    border-radius: 16rpx;

    .alarm-icon {
      width: 72rpx;
      height: 72rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.high {
        background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
      }

      &.medium {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.low {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      }
    }

    .alarm-name {
      font-size: 26rpx;
      color: #666;
    }

    .alarm-count {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }
}

.alarm-trend {
  margin-top: 30rpx;
  padding: 24rpx;
  background: rgba(239, 68, 68, 0.05);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;

  .trend-text {
    font-size: 28rpx;
    color: #ef4444;
  }
}

// 对比分析
.comparison-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  .comparison-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx;
    background: #f5f7fa;
    border-radius: 12rpx;

    .comparison-label {
      font-size: 28rpx;
      color: #666;
    }

    .comparison-value {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 32rpx;
      font-weight: 600;

      &.up {
        color: #22c55e;
      }

      &:not(.up) {
        color: #ef4444;
      }
    }
  }
}

// 日期范围弹窗
.date-range-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;
  padding-bottom: calc(40rpx + env(safe-area-inset-bottom));

  .popup-header {
    position: relative;
    text-align: center;
    margin-bottom: 40rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .date-presets {
    display: flex;
    gap: 16rpx;
    margin-bottom: 30rpx;
    flex-wrap: wrap;

    .preset-btn {
      padding: 16rpx 32rpx;
      border-radius: 40rpx;
      font-size: 28rpx;
      color: #666;
      background: #f5f7fa;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
        border-color: #667eea;
      }
    }
  }

  .custom-date-range {
    display: flex;
    gap: 20rpx;
    margin-bottom: 40rpx;

    .date-field {
      flex: 1;

      .field-label {
        display: block;
        font-size: 26rpx;
        color: #999;
        margin-bottom: 16rpx;
      }

      .picker-input {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20rpx 24rpx;
        background: #f5f7fa;
        border-radius: 12rpx;
        font-size: 28rpx;
        color: #333;
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 20rpx;

    .footer-btn {
      flex: 1;
      height: 88rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 16rpx;
      font-size: 30rpx;
      font-weight: 600;

      &.secondary {
        background: #f5f7fa;
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

// 排行类型弹窗
.rank-type-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;

  .popup-header {
    position: relative;
    text-align: center;
    margin-bottom: 40rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .rank-type-options {
    display: flex;
    flex-direction: column;
    gap: 20rpx;

    .type-option {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 30rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      font-size: 28rpx;
      color: #333;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      &.active {
        border-color: #667eea;
        background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
      }
    }
  }
}
</style>
