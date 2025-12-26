<template>
  <view class="device-monitor-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">实时监控</view>
        <view class="navbar-right" @tap="refreshMonitor">
          <uni-icons type="refresh" size="20" color="#ffffff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view class="main-scroll" scroll-y>
      <!-- 设备状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <view class="device-info">
            <text class="device-name">{{ monitorData.deviceName }}</text>
            <text class="device-code">{{ monitorData.deviceCode }}</text>
          </view>
          <view class="online-status" :class="{ online: monitorData.online }">
            <view class="status-dot"></view>
            <text class="status-text">{{ monitorData.online ? '在线' : '离线' }}</text>
          </view>
        </view>

        <view class="status-metrics">
          <view class="metric-item">
            <text class="metric-value">{{ monitorData.todayPassCount }}</text>
            <text class="metric-label">今日通行</text>
          </view>
          <view class="metric-item">
            <text class="metric-value">{{ monitorData.onlineDuration }}</text>
            <text class="metric-label">在线时长</text>
          </view>
          <view class="metric-item">
            <text class="metric-value">{{ monitorData.cpuUsage }}%</text>
            <text class="metric-label">CPU使用率</text>
          </view>
        </view>
      </view>

      <!-- 实时通行记录 -->
      <view class="records-section">
        <view class="section-header">
          <text class="section-title">实时通行记录</text>
          <view class="live-badge">
            <view class="badge-dot"></view>
            <text class="badge-text">实时</text>
          </view>
        </view>

        <view class="records-list">
          <view
            v-for="record in realTimeRecords"
            :key="record.id"
            class="record-item"
            :class="{ success: record.success, failed: !record.success }"
          >
            <view class="record-icon" :style="{ background: record.success ? successGradient : errorGradient }">
              <uni-icons
                :type="record.success ? 'checkbox-filled' : 'close-filled'"
                size="18"
                color="#ffffff"
              ></uni-icons>
            </view>

            <view class="record-info">
              <view class="record-header">
                <text class="record-user">{{ record.userName }}</text>
                <text class="record-time">{{ record.passTime }}</text>
              </view>
              <view class="record-detail">
                <text class="detail-text">{{ record.verifyMethod }}</text>
                <text class="detail-separator">|</text>
                <text class="detail-text">{{ record.areaName }}</text>
              </view>
              <view class="record-fail" v-if="!record.success">
                <text class="fail-text">{{ record.failReason }}</text>
              </view>
            </view>
          </view>

          <view class="empty-records" v-if="realTimeRecords.length === 0">
            <uni-icons type="loop" size="60" color="#CCCCCC"></uni-icons>
            <text class="empty-text">暂无通行记录</text>
          </view>
        </view>
      </view>

      <!-- 性能监控 -->
      <view class="performance-section">
        <view class="section-header">
          <text class="section-title">性能监控</text>
        </view>

        <view class="performance-card">
          <view class="perf-item">
            <view class="perf-header">
              <text class="perf-label">内存使用率</text>
              <text class="perf-value">{{ monitorData.memoryUsage }}%</text>
            </view>
            <view class="perf-bar">
              <view class="perf-fill" :style="{ width: monitorData.memoryUsage + '%' }"></view>
            </view>
          </view>

          <view class="perf-item">
            <view class="perf-header">
              <text class="perf-label">存储空间</text>
              <text class="perf-value">{{ monitorData.storageUsage }}%</text>
            </view>
            <view class="perf-bar">
              <view class="perf-fill" :style="{ width: monitorData.storageUsage + '%' }"></view>
            </view>
          </view>

          <view class="perf-item">
            <view class="perf-header">
              <text class="perf-label">网络延迟</text>
              <text class="perf-value">{{ monitorData.networkDelay }}ms</text>
            </view>
            <view class="perf-bar">
              <view class="perf-fill low" :style="{ width: Math.min(monitorData.networkDelay / 2, 100) + '%' }"></view>
            </view>
          </view>

          <view class="perf-item">
            <view class="perf-header">
              <text class="perf-label">温度</text>
              <text class="perf-value">{{ monitorData.temperature }}°C</text>
            </view>
            <view class="perf-bar">
              <view class="perf-fill" :style="{ width: (monitorData.temperature / 100) + '%' }"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 告警信息 -->
      <view class="alert-section" v-if="alerts.length > 0">
        <view class="section-header">
          <text class="section-title">告警信息</text>
          <view class="alert-count">
            <text class="count-text">{{ alerts.length }} 条</text>
          </view>
        </view>

        <view class="alerts-list">
          <view
            v-for="alert in alerts"
            :key="alert.id"
            class="alert-item"
            :class="alert.level"
          >
            <view class="alert-icon" :style="{ background: getAlertGradient(alert.level) }">
              <uni-icons type="warning-filled" size="20" color="#ffffff"></uni-icons>
            </view>
            <view class="alert-info">
              <text class="alert-title">{{ alert.title }}</text>
              <text class="alert-time">{{ alert.alertTime }}</text>
            </view>
            <view class="alert-action" @tap="handleAlert(alert)">
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 连接信息 -->
      <view class="connection-section">
        <view class="section-header">
          <text class="section-title">连接信息</text>
        </view>

        <view class="connection-card">
          <view class="conn-item">
            <text class="conn-label">连接类型</text>
            <text class="conn-value">{{ monitorData.connectionType }}</text>
          </view>
          <view class="conn-item">
            <text class="conn-label">IP地址</text>
            <text class="conn-value">{{ monitorData.ipAddress }}</text>
          </view>
          <view class="conn-item">
            <text class="conn-label">MAC地址</text>
            <text class="conn-value">{{ monitorData.macAddress }}</text>
          </view>
          <view class="conn-item">
            <text class="conn-label">固件版本</text>
            <text class="conn-value">{{ monitorData.firmwareVersion }}</text>
          </view>
          <view class="conn-item">
            <text class="conn-label">最后通信</text>
            <text class="conn-value">{{ monitorData.lastCommTime }}</text>
          </view>
        </view>
      </view>

      <!-- 快捷操作 -->
      <view class="action-section">
        <view class="action-grid">
          <view class="action-item" @tap="restartDevice">
            <view class="action-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <uni-icons type="refresh" size="24" color="#ffffff"></uni-icons>
            </view>
            <text class="action-text">重启设备</text>
          </view>
          <view class="action-item" @tap="clearCache">
            <view class="action-icon" style="background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);">
              <uni-icons type="trash" size="24" color="#ffffff"></uni-icons>
            </view>
            <text class="action-text">清除缓存</text>
          </view>
          <view class="action-item" @tap="syncTime">
            <view class="action-icon" style="background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);">
              <uni-icons type="clock" size="24" color="#ffffff"></uni-icons>
            </view>
            <text class="action-text">同步时间</text>
          </view>
          <view class="action-item" @tap="viewLogs">
            <view class="action-icon" style="background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);">
              <uni-icons type="list" size="24" color="#ffffff"></uni-icons>
            </view>
            <text class="action-text">查看日志</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'
import dayjs from 'dayjs'

// 页面参数
const deviceId = ref('')

// 监控数据
const monitorData = reactive({
  deviceName: '',
  deviceCode: '',
  online: true,
  todayPassCount: 0,
  onlineDuration: '0小时',
  cpuUsage: 45,
  memoryUsage: 62,
  storageUsage: 38,
  networkDelay: 15,
  temperature: 42,
  connectionType: 'TCP/IP',
  ipAddress: '192.168.1.100',
  macAddress: '00:11:22:33:44:55',
  firmwareVersion: 'v2.1.0',
  lastCommTime: ''
})

// 实时通行记录
const realTimeRecords = ref([])
const successGradient = 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)'
const errorGradient = 'linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%)'

// 告警信息
const alerts = ref([])

// 定时器
let refreshTimer = null

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  deviceId.value = options.deviceId || ''

  loadMonitorData()
  loadRealTimeRecords()
  loadAlerts()

  // 启动定时刷新
  startRefresh()
})

// 页面卸载
onUnmounted(() => {
  stopRefresh()
})

// 加载监控数据
const loadMonitorData = async () => {
  try {
    const res = await deviceApi.getDeviceMonitorData(deviceId.value)
    if (res.code === 200 && res.data) {
      Object.assign(monitorData, res.data)
    }
  } catch (error) {
    console.error('加载监控数据失败:', error)
  }
}

// 加载实时通行记录
const loadRealTimeRecords = async () => {
  try {
    const res = await deviceApi.getDeviceRealTimeRecords(deviceId.value)
    if (res.code === 200 && res.data) {
      realTimeRecords.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载实时记录失败:', error)
  }
}

// 加载告警信息
const loadAlerts = async () => {
  try {
    const res = await deviceApi.getDeviceAlerts(deviceId.value)
    if (res.code === 200 && res.data) {
      alerts.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载告警信息失败:', error)
  }
}

// 启动定时刷新
const startRefresh = () => {
  refreshTimer = setInterval(async () => {
    await loadMonitorData()
    await loadRealTimeRecords()
    await loadAlerts()
  }, 5000) // 每5秒刷新一次
}

// 停止定时刷新
const stopRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 刷新监控
const refreshMonitor = async () => {
  uni.showLoading({
    title: '刷新中...'
  })

  try {
    await loadMonitorData()
    await loadRealTimeRecords()
    await loadAlerts()

    uni.showToast({
      title: '刷新成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('刷新失败:', error)
  } finally {
    uni.hideLoading()
  }
}

// 获取告警渐变色
const getAlertGradient = (level) => {
  const gradients = {
    critical: 'linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%)',
    warning: 'linear-gradient(135deg, #faad14 0%, #ffc53d 100%)',
    info: 'linear-gradient(135deg, #1890ff 0%, #40a9ff 100%)'
  }
  return gradients[level] || gradients.info
}

// 处理告警
const handleAlert = (alert) => {
  uni.showModal({
    title: alert.title,
    content: `${alert.message}\n\n时间：${alert.alertTime}\n级别：${alert.level}`,
    showCancel: false
  })
}

// 重启设备
const restartDevice = () => {
  uni.showModal({
    title: '确认重启',
    content: '确定要重启设备吗？重启期间设备将无法使用。',
    confirmColor: '#667eea',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '重启中...'
        })

        try {
          const res = await deviceApi.restartDevice(deviceId.value)
          if (res.code === 200) {
            uni.showToast({
              title: '重启成功',
              icon: 'success'
            })
          }
        } catch (error) {
          console.error('重启失败:', error)
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 清除缓存
const clearCache = () => {
  uni.showModal({
    title: '确认清除',
    content: '确定要清除设备缓存吗？',
    confirmColor: '#667eea',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '清除中...'
        })

        try {
          const res = await deviceApi.clearDeviceCache(deviceId.value)
          if (res.code === 200) {
            uni.showToast({
              title: '清除成功',
              icon: 'success'
            })
          }
        } catch (error) {
          console.error('清除失败:', error)
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 同步时间
const syncTime = async () => {
  uni.showLoading({
    title: '同步中...'
  })

  try {
    const res = await deviceApi.syncDeviceTime(deviceId.value)
    if (res.code === 200) {
      uni.showToast({
        title: '同步成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('同步失败:', error)
  } finally {
    uni.hideLoading()
  }
}

// 查看日志
const viewLogs = () => {
  uni.navigateTo({
    url: `/pages/access/device-logs?deviceId=${deviceId.value}`
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-monitor-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-top: var(--status-bar-height);
  padding-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  height: 88rpx;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.back-text {
  font-size: 28rpx;
  color: #ffffff;
}

.navbar-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.navbar-right {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 主内容区域
.main-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 108rpx);
  padding-bottom: 30rpx;
}

// 状态卡片
.status-card {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

.device-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.device-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.device-code {
  font-size: 24rpx;
  color: #999999;
}

.online-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #f5f5f5;
  border-radius: 20rpx;
}

.online-status.online {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #ff4d4f;
  animation: pulse 2s infinite;
}

.online-status.online .status-dot {
  background: #ffffff;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.status-text {
  font-size: 22rpx;
  color: #666666;
}

.online-status.online .status-text {
  color: #ffffff;
  font-weight: 600;
}

.status-metrics {
  display: flex;
  gap: 20rpx;
}

.metric-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 0;
  background: #f5f7fa;
  border-radius: 12rpx;
}

.metric-value {
  font-size: 36rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.metric-label {
  font-size: 22rpx;
  color: #999999;
}

// 通用区块样式
.records-section,
.performance-section,
.alert-section,
.connection-section,
.action-section {
  margin: 20rpx 30rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 12rpx;
  background: rgba(255, 77, 79, 0.1);
  border-radius: 16rpx;
}

.badge-dot {
  width: 12rpx;
  height: 12rpx;
  background: #ff4d4f;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.badge-text {
  font-size: 20rpx;
  color: #ff4d4f;
  font-weight: 600;
}

// 实时记录列表
.records-list {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.record-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.record-icon {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.record-user {
  font-size: 26rpx;
  font-weight: 600;
  color: #333333;
}

.record-time {
  font-size: 22rpx;
  color: #999999;
}

.record-detail {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.detail-text {
  font-size: 22rpx;
  color: #666666;
}

.detail-separator {
  font-size: 16rpx;
  color: #CCCCCC;
}

.record-fail {
  margin-top: 4rpx;
}

.fail-text {
  font-size: 22rpx;
  color: #ff4d4f;
}

.empty-records {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 0;
}

.empty-text {
  font-size: 26rpx;
  color: #CCCCCC;
  margin-top: 20rpx;
}

// 性能监控卡片
.performance-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.perf-item {
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.perf-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.perf-label {
  font-size: 26rpx;
  color: #666666;
}

.perf-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #333333;
}

.perf-bar {
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
}

.perf-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;

  &.low {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  }
}

// 告警信息
.alert-count {
  padding: 6rpx 12rpx;
  background: rgba(255, 77, 79, 0.1);
  border-radius: 12rpx;
}

.count-text {
  font-size: 20rpx;
  color: #ff4d4f;
  font-weight: 600;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: #ffffff;
  border-radius: 12rpx;
  border-left: 4rpx solid transparent;

  &.critical {
    border-left-color: #ff4d4f;
    background: rgba(255, 77, 79, 0.05);
  }

  &.warning {
    border-left-color: #faad14;
    background: rgba(250, 173, 20, 0.05);
  }

  &.info {
    border-left-color: #1890ff;
  }
}

.alert-icon {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.alert-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.alert-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333333;
}

.alert-time {
  font-size: 22rpx;
  color: #999999;
}

.alert-action {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 连接信息卡片
.connection-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.conn-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.conn-label {
  font-size: 26rpx;
  color: #666666;
}

.conn-value {
  font-size: 26rpx;
  color: #333333;
  font-weight: 600;
}

// 快捷操作
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
}

.action-item {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  &:active {
    transform: scale(0.95);
  }
}

.action-icon {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.action-text {
  font-size: 22rpx;
  color: #333333;
}
</style>
