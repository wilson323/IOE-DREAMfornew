<template>
  <view class="device-firmware-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">固件升级</view>
        <view class="navbar-right" @tap="checkUpdate">
          <uni-icons type="refresh" size="20" color="#ffffff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view class="main-scroll" scroll-y>
      <!-- 当前固件信息卡片 -->
      <view class="firmware-card current-firmware">
        <view class="card-header">
          <text class="header-title">当前固件</text>
          <view class="firmware-badge" :style="{ background: getVersionBadgeGradient() }">
            <text class="badge-text">{{ currentFirmware.versionType }}</text>
          </view>
        </view>

        <view class="version-info">
          <view class="version-number">
            <text class="version-label">版本号</text>
            <text class="version-value">{{ currentFirmware.version }}</text>
          </view>

          <view class="firmware-details">
            <view class="detail-item">
              <uni-icons type="calendar" size="14" color="#999"></uni-icons>
              <text class="detail-label">发布日期</text>
              <text class="detail-value">{{ currentFirmware.releaseDate }}</text>
            </view>
            <view class="detail-item">
              <uni-icons type="info" size="14" color="#999"></uni-icons>
              <text class="detail-label">文件大小</text>
              <text class="detail-value">{{ currentFirmware.fileSize }}</text>
            </view>
            <view class="detail-item">
              <uni-icons type="checkmarkempty" size="14" color="#999"></uni-icons>
              <text class="detail-label">MD5</text>
              <text class="detail-value">{{ currentFirmware.md5 }}</text>
            </view>
          </view>
        </view>

        <!-- 更新状态 -->
        <view class="update-status" v-if="updateStatus">
          <view class="status-item" v-if="updateStatus.hasUpdate">
            <uni-icons type="info" size="16" color="#ff4d4f"></uni-icons>
            <text class="status-text">发现新版本 {{ updateStatus.latestVersion }}</text>
          </view>
          <view class="status-item" v-else>
            <uni-icons type="checkbox-filled" size="16" color="#52c41a"></uni-icons>
            <text class="status-text">当前已是最新版本</text>
          </view>
        </view>
      </view>

      <!-- 升级进度卡片 -->
      <view class="firmware-card" v-if="upgrading">
        <view class="card-header">
          <text class="header-title">升级进度</text>
        </view>

        <view class="upgrade-progress">
          <view class="progress-info">
            <text class="progress-text">{{ upgradeProgress.statusText }}</text>
            <text class="progress-percent">{{ upgradeProgress.percent }}%</text>
          </view>

          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: upgradeProgress.percent + '%' }"></view>
          </view>

          <view class="progress-detail" v-if="upgradeProgress.detail">
            <text class="detail-text">{{ upgradeProgress.detail }}</text>
          </view>

          <!-- 升级步骤 -->
          <view class="upgrade-steps">
            <view
              v-for="(step, index) in upgradeSteps"
              :key="index"
              class="step-item"
              :class="{ active: step.active, completed: step.completed }"
            >
              <view class="step-icon">
                <uni-icons
                  v-if="step.completed"
                  type="checkbox-filled"
                  size="16"
                  color="#52c41a"
                ></uni-icons>
                <uni-icons
                  v-else-if="step.active"
                  type="spinner-cycle"
                  size="16"
                  color="#667eea"
                ></uni-icons>
                <uni-icons
                  v-else
                  type="circle"
                  size="16"
                  color="#CCCCCC"
                ></uni-icons>
              </view>
              <text class="step-text">{{ step.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 可用更新列表 -->
      <view class="firmware-card available-updates" v-if="availableUpdates.length > 0">
        <view class="card-header">
          <text class="header-title">可用更新</text>
          <text class="update-count">{{ availableUpdates.length }}个</text>
        </view>

        <view class="updates-list">
          <view
            v-for="update in availableUpdates"
            :key="update.version"
            class="update-item"
            :class="{ recommended: update.recommended }"
          >
            <view class="update-header">
              <view class="version-info">
                <text class="update-version">{{ update.version }}</text>
                <view v-if="update.recommended" class="recommended-badge">
                  <text class="badge-text">推荐</text>
                </view>
              </view>
              <view class="update-type">
                <text class="type-text">{{ update.updateType }}</text>
              </view>
            </view>

            <view class="update-details">
              <text class="release-date">{{ update.releaseDate }}</text>
              <text class="file-size">{{ update.fileSize }}</text>
            </view>

            <view class="update-description">
              <text class="description-text">{{ update.description }}</text>
            </view>

            <view class="update-changelog" v-if="update.changelog && update.changelog.length > 0">
              <text class="changelog-title">更新内容：</text>
              <view class="changelog-list">
                <view v-for="(log, index) in update.changelog" :key="index" class="changelog-item">
                  <text class="changelog-text">• {{ log }}</text>
                </view>
              </view>
            </view>

            <view class="update-actions">
              <button class="btn-view-detail" @tap="viewUpdateDetail(update)">查看详情</button>
              <button class="btn-upgrade" @tap="startUpgrade(update)" :disabled="upgrading">
                {{ upgrading ? '升级中...' : '立即升级' }}
              </button>
            </view>
          </view>
        </view>
      </view>

      <!-- 升级历史 -->
      <view class="firmware-card upgrade-history">
        <view class="card-header">
          <text class="header-title">升级历史</text>
          <text class="view-all" @tap="viewAllHistory">查看全部</text>
        </view>

        <view class="history-list">
          <view
            v-for="history in upgradeHistory"
            :key="history.id"
            class="history-item"
            @tap="viewHistoryDetail(history)"
          >
            <view class="history-header">
              <text class="history-version">{{ history.version }}</text>
              <view class="history-status" :class="history.status">
                <text class="status-text">{{ getHistoryStatusLabel(history.status) }}</text>
              </view>
            </view>

            <view class="history-info">
              <text class="upgrade-time">{{ history.upgradeTime }}</text>
              <text class="upgrade-duration">{{ history.duration }}</text>
            </view>

            <view class="history-result" v-if="history.status === 'failed'">
              <text class="error-message">{{ history.errorMessage }}</text>
            </view>
          </view>
        </view>

        <view v-if="upgradeHistory.length === 0" class="empty-history">
          <uni-icons type="folder" size="60" color="#CCCCCC"></uni-icons>
          <text class="empty-text">暂无升级记录</text>
        </view>
      </view>

      <!-- 升级说明 -->
      <view class="firmware-card upgrade-guide">
        <view class="card-header">
          <text class="header-title">升级说明</text>
        </view>

        <view class="guide-content">
          <view class="guide-item">
            <view class="guide-number">1</view>
            <text class="guide-text">升级前请备份重要数据</text>
          </view>
          <view class="guide-item">
            <view class="guide-number">2</view>
            <text class="guide-text">升级过程中请保持设备供电</text>
          </view>
          <view class="guide-item">
            <view class="guide-number">3</view>
            <text class="guide-text">升级期间请勿操作设备</text>
          </view>
          <view class="guide-item">
            <view class="guide-number">4</view>
            <text class="guide-text">升级完成后设备会自动重启</text>
          </view>
          <view class="guide-item">
            <view class="guide-number">5</view>
            <text class="guide-text">升级失败可尝试重新升级</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'
import dayjs from 'dayjs'

// 页面参数
const deviceId = ref('')

// 当前固件信息
const currentFirmware = reactive({
  version: 'v2.1.0',
  versionType: '正式版',
  releaseDate: '2024-01-15',
  fileSize: '5.2 MB',
  md5: 'a1b2c3d4e5f6g7h8i9j0'
})

// 更新状态
const updateStatus = ref(null)

// 升级状态
const upgrading = ref(false)
const upgradeProgress = reactive({
  percent: 0,
  statusText: '准备中...',
  detail: ''
})

const upgradeSteps = ref([
  { label: '下载固件', active: false, completed: false },
  { label: '验证固件', active: false, completed: false },
  { label: '写入固件', active: false, completed: false },
  { label: '重启设备', active: false, completed: false }
])

// 可用更新列表
const availableUpdates = ref([])

// 升级历史
const upgradeHistory = ref([
  {
    id: 1,
    version: 'v2.1.0',
    status: 'success',
    upgradeTime: '2024-01-15 14:30',
    duration: '5分钟'
  },
  {
    id: 2,
    version: 'v2.0.5',
    status: 'success',
    upgradeTime: '2023-12-10 10:20',
    duration: '4分钟'
  },
  {
    id: 3,
    version: 'v2.0.4',
    status: 'failed',
    upgradeTime: '2023-12-05 16:45',
    duration: '3分钟',
    errorMessage: '网络连接中断'
  }
])

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  deviceId.value = options.deviceId || ''

  loadCurrentFirmware()
  checkUpdate()
  loadUpgradeHistory()
})

// 加载当前固件信息
const loadCurrentFirmware = async () => {
  try {
    const res = await deviceApi.getDeviceFirmware(deviceId.value)
    if (res.code === 200 && res.data) {
      Object.assign(currentFirmware, res.data)
    }
  } catch (error) {
    console.error('加载固件信息失败:', error)
  }
}

// 检查更新
const checkUpdate = async () => {
  uni.showLoading({
    title: '检查中...'
  })

  try {
    const res = await deviceApi.checkFirmwareUpdate(deviceId.value)
    if (res.code === 200 && res.data) {
      updateStatus.value = res.data
      if (res.data.updates && res.data.updates.length > 0) {
        availableUpdates.value = res.data.updates
      } else {
        availableUpdates.value = []
      }

      uni.showToast({
        title: res.data.hasUpdate ? '发现新版本' : '已是最新版本',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('检查更新失败:', error)
    uni.showToast({
      title: '检查失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 开始升级
const startUpgrade = (update) => {
  uni.showModal({
    title: '确认升级',
    content: `确定要升级到 ${update.version} 吗？\n\n升级过程中请勿操作设备，升级完成后设备会自动重启。`,
    confirmColor: '#667eea',
    success: (res) => {
      if (res.confirm) {
        performUpgrade(update)
      }
    }
  })
}

// 执行升级
const performUpgrade = async (update) => {
  upgrading.value = true
  upgradeProgress.percent = 0
  upgradeProgress.statusText = '准备中...'

  // 重置升级步骤
  upgradeSteps.value = upgradeSteps.value.map(step => ({
    ...step,
    active: false,
    completed: false
  }))

  try {
    // 步骤1: 下载固件
    upgradeSteps.value[0].active = true
    upgradeProgress.statusText = '下载固件中...'
    upgradeProgress.detail = '0 / 5.2 MB'

    await simulateProgress(0, 30, 2000, (percent) => {
      upgradeProgress.percent = percent
      upgradeProgress.detail = `${(percent * 0.052).toFixed(1)} / 5.2 MB`
    })

    upgradeSteps.value[0].active = false
    upgradeSteps.value[0].completed = true

    // 步骤2: 验证固件
    upgradeSteps.value[1].active = true
    upgradeProgress.statusText = '验证固件...'
    upgradeProgress.detail = '计算MD5校验和'

    await simulateProgress(30, 40, 1000)

    upgradeSteps.value[1].active = false
    upgradeSteps.value[1].completed = true

    // 步骤3: 写入固件
    upgradeSteps.value[2].active = true
    upgradeProgress.statusText = '写入固件...'
    upgradeProgress.detail = '请勿关闭设备'

    await simulateProgress(40, 90, 3000)

    upgradeSteps.value[2].active = false
    upgradeSteps.value[2].completed = true

    // 步骤4: 重启设备
    upgradeSteps.value[3].active = true
    upgradeProgress.statusText = '重启设备...'
    upgradeProgress.detail = '设备将自动重启'

    await simulateProgress(90, 100, 2000)

    upgradeSteps.value[3].active = false
    upgradeSteps.value[3].completed = true

    upgradeProgress.statusText = '升级完成'
    upgradeProgress.detail = '设备已重启'

    uni.showToast({
      title: '升级成功',
      icon: 'success'
    })

    // 更新当前固件版本
    currentFirmware.version = update.version
    currentFirmware.releaseDate = dayjs().format('YYYY-MM-DD')
    currentFirmware.fileSize = update.fileSize

    // 添加到升级历史
    upgradeHistory.value.unshift({
      id: Date.now(),
      version: update.version,
      status: 'success',
      upgradeTime: dayjs().format('YYYY-MM-DD HH:mm'),
      duration: '5分钟'
    })

    // 刷新可用更新列表
    setTimeout(() => {
      checkUpdate()
    }, 2000)

  } catch (error) {
    console.error('升级失败:', error)
    upgradeProgress.statusText = '升级失败'
    upgradeProgress.detail = error.message || '未知错误'

    // 添加失败记录
    upgradeHistory.value.unshift({
      id: Date.now(),
      version: update.version,
      status: 'failed',
      upgradeTime: dayjs().format('YYYY-MM-DD HH:mm'),
      duration: '1分钟',
      errorMessage: error.message || '未知错误'
    })

    uni.showToast({
      title: '升级失败',
      icon: 'none'
    })
  } finally {
    setTimeout(() => {
      upgrading.value = false
    }, 2000)
  }
}

// 模拟进度
const simulateProgress = (start, end, duration, callback) => {
  return new Promise((resolve) => {
    const steps = 20
    const increment = (end - start) / steps
    const interval = duration / steps

    let current = start
    const timer = setInterval(() => {
      current += increment
      if (current >= end) {
        current = end
        clearInterval(timer)
        resolve()
      }
      upgradeProgress.percent = Math.floor(current)
      if (callback) {
        callback(Math.floor(current))
      }
    }, interval)
  })
}

// 查看更新详情
const viewUpdateDetail = (update) => {
  uni.showModal({
    title: `版本 ${update.version} 详情`,
    content: `${update.description}\n\n文件大小: ${update.fileSize}\n发布日期: ${update.releaseDate}\nMD5: ${update.md5}`,
    showCancel: false
  })
}

// 查看升级历史详情
const viewHistoryDetail = (history) => {
  uni.showModal({
    title: `升级记录 - ${history.version}`,
    content: `升级时间: ${history.upgradeTime}\n升级耗时: ${history.duration}\n升级状态: ${getHistoryStatusLabel(history.status)}${history.status === 'failed' ? '\n失败原因: ' + history.errorMessage : ''}`,
    showCancel: false
  })
}

// 查看全部历史
const viewAllHistory = () => {
  uni.showToast({
    title: '查看全部历史功能开发中',
    icon: 'none'
  })
}

// 获取历史状态标签
const getHistoryStatusLabel = (status) => {
  const statusMap = {
    success: '成功',
    failed: '失败',
    pending: '进行中'
  }
  return statusMap[status] || '未知'
}

// 获取版本徽章渐变色
const getVersionBadgeGradient = () => {
  if (currentFirmware.versionType === '正式版') {
    return 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)'
  } else if (currentFirmware.versionType === '测试版') {
    return 'linear-gradient(135deg, #faad14 0%, #ffc53d 100%)'
  } else {
    return 'linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%)'
  }
}

// 加载升级历史
const loadUpgradeHistory = async () => {
  try {
    const res = await deviceApi.getUpgradeHistory(deviceId.value)
    if (res.code === 200 && res.data) {
      upgradeHistory.value = res.data.list || []
    }
  } catch (error) {
    console.error('加载升级历史失败:', error)
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-firmware-page {
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

// 固件卡片
.firmware-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin: 20rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.firmware-badge {
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
}

.badge-text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 600;
}

.update-count {
  font-size: 24rpx;
  color: #667eea;
}

.view-all {
  font-size: 24rpx;
  color: #667eea;
}

// 当前固件信息
.version-info {
  padding: 20rpx 0;
}

.version-number {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.version-label {
  font-size: 26rpx;
  color: #999999;
}

.version-value {
  font-size: 36rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.firmware-details {
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.detail-label {
  font-size: 26rpx;
  color: #999999;
  margin-left: 8rpx;
}

.detail-value {
  flex: 1;
  font-size: 26rpx;
  color: #333333;
  margin-left: 16rpx;
  text-align: right;
  font-family: monospace;
}

// 更新状态
.update-status {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.status-text {
  font-size: 26rpx;
  color: #666666;
}

// 升级进度
.upgrade-progress {
  padding: 20rpx 0;
}

.progress-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.progress-text {
  font-size: 28rpx;
  color: #333333;
}

.progress-percent {
  font-size: 32rpx;
  font-weight: 700;
  color: #667eea;
}

.progress-bar {
  height: 16rpx;
  background: #f0f0f0;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 16rpx;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.3);
}

.progress-detail {
  text-align: center;
  margin-bottom: 24rpx;
}

.detail-text {
  font-size: 24rpx;
  color: #999999;
}

.upgrade-steps {
  margin-top: 30rpx;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 20rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.step-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-text {
  font-size: 26rpx;
  color: #666666;
}

.step-item.active .step-text {
  color: #667eea;
  font-weight: 600;
}

.step-item.completed .step-text {
  color: #52c41a;
}

// 可用更新列表
.updates-list {
  padding: 0;
}

.update-item {
  padding: 24rpx;
  background: #f5f7fa;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  border: 2rpx solid transparent;

  &:last-child {
    margin-bottom: 0;
  }

  &.recommended {
    border-color: #667eea;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  }
}

.update-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.update-version {
  font-size: 32rpx;
  font-weight: 700;
  color: #333333;
}

.recommended-badge {
  padding: 6rpx 12rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.3);
}

.recommended-badge .badge-text {
  font-size: 20rpx;
  color: #ffffff;
}

.update-type {
  padding: 6rpx 12rpx;
  background: #fff4e5;
  border-radius: 8rpx;
}

.type-text {
  font-size: 20rpx;
  color: #faad14;
}

.update-details {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.release-date,
.file-size {
  font-size: 24rpx;
  color: #999999;
}

.update-description {
  margin-bottom: 16rpx;
}

.description-text {
  font-size: 26rpx;
  color: #666666;
  line-height: 1.6;
}

.update-changelog {
  margin-bottom: 20rpx;
}

.changelog-title {
  font-size: 26rpx;
  color: #333333;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.changelog-list {
  padding-left: 12rpx;
}

.changelog-item {
  margin-bottom: 8rpx;
}

.changelog-text {
  font-size: 24rpx;
  color: #666666;
  line-height: 1.5;
}

.update-actions {
  display: flex;
  gap: 16rpx;
}

.btn-view-detail,
.btn-upgrade {
  flex: 1;
  height: 68rpx;
  line-height: 68rpx;
  text-align: center;
  border-radius: 12rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.btn-view-detail {
  background: #ffffff;
  color: #667eea;
  border: 1rpx solid #667eea;
}

.btn-upgrade {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

  &[disabled] {
    opacity: 0.6;
  }
}

// 升级历史
.history-list {
  padding: 0;
}

.history-item {
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }

  &:active {
    transform: scale(0.98);
  }
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.history-version {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.history-status {
  padding: 6rpx 12rpx;
  border-radius: 8rpx;

  &.success {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  }

  &.failed {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  }

  &.pending {
    background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
  }
}

.history-status .status-text {
  font-size: 20rpx;
  color: #ffffff;
}

.history-info {
  display: flex;
  gap: 24rpx;
}

.upgrade-time,
.upgrade-duration {
  font-size: 24rpx;
  color: #999999;
}

.history-result {
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #e0e0e0;
}

.error-message {
  font-size: 24rpx;
  color: #ff4d4f;
}

.empty-history {
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

// 升级说明
.guide-content {
  padding: 0;
}

.guide-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.guide-number {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 700;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.guide-text {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
  line-height: 1.5;
}
</style>
