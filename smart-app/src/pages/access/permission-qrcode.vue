<template>
  <view class="permission-qrcode-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <uni-icons type="arrowleft" size="20" color="#333" @tap="goBack"></uni-icons>
        <text class="nav-title">通行二维码</text>
        <view class="nav-actions">
          <uni-icons type="refresh" size="20" color="#1890ff" @tap="refreshQRCode"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <uni-load-more status="loading" :content-text="loadingText"></uni-load-more>
    </view>

    <template v-else>
      <!-- 权限信息卡片 -->
      <view class="permission-card">
        <view class="card-header">
          <view class="area-info">
            <uni-icons type="home" size="24" color="#1890ff"></uni-icons>
            <text class="area-name">{{ permissionInfo.areaName }}</text>
          </view>
          <view class="permission-status" :class="getStatusClass()">
            {{ getStatusText() }}
          </view>
        </view>
        <view class="card-details">
          <view class="detail-item">
            <text class="detail-label">权限类型</text>
            <text class="detail-value">{{ getPermissionTypeText() }}</text>
          </view>
          <view v-if="permissionInfo.expireTime" class="detail-item">
            <text class="detail-label">有效期至</text>
            <text class="detail-value">{{ formatDate(permissionInfo.expireTime) }}</text>
          </view>
        </view>
      </view>

      <!-- 二维码展示区域 -->
      <view class="qrcode-section">
        <view class="qrcode-wrapper">
          <!-- 二维码图片 -->
          <view v-if="qrcodeData" class="qrcode-container">
            <image :src="qrcodeData" mode="aspectFit" class="qrcode-image"></image>
            <!-- 倒计时进度条 -->
            <view v-if="showCountdown" class="countdown-bar">
              <view class="countdown-progress" :style="{ width: countdownProgress + '%' }"></view>
            </view>
          </view>

          <!-- 加载状态 -->
          <view v-else class="qrcode-loading">
            <uni-load-more status="loading"></uni-load-more>
          </view>

          <!-- 自动刷新倒计时 -->
          <view v-if="autoRefreshEnabled" class="refresh-countdown">
            <text>{{ refreshCountdown }}秒后自动刷新</text>
          </view>
        </view>

        <!-- 使用提示 -->
        <view class="usage-tips">
          <view class="tip-item">
            <uni-icons type="info" size="16" color="#1890ff"></uni-icons>
            <text>请在门禁设备出示二维码</text>
          </view>
          <view class="tip-item">
            <uni-icons type="loop" size="16" color="#52c41a"></uni-icons>
            <text>二维码每60秒自动刷新</text>
          </view>
        </view>
      </view>

      <!-- 快速操作 -->
      <view class="quick-actions">
        <button class="action-btn refresh-btn" @tap="refreshQRCode">
          <uni-icons type="refreshempty" size="18" color="#1890ff"></uni-icons>
          <text>刷新二维码</text>
        </button>
        <button class="action-btn save-btn" @tap="saveQRCode">
          <uni-icons type="download" size="18" color="#52c41a"></uni-icons>
          <text>保存到相册</text>
        </button>
        <button class="action-btn share-btn" @tap="shareQRCode">
          <uni-icons type="redo" size="18" color="#722ed1"></uni-icons>
          <text>分享权限</text>
        </button>
      </view>

      <!-- 权限详情 -->
      <view class="detail-section">
        <view class="section-title">权限详情</view>
        <view class="detail-list">
          <view class="detail-row">
            <text class="row-label">权限编号</text>
            <text class="row-value">{{ permissionInfo.permissionCode || '-' }}</text>
          </view>
          <view class="detail-row">
            <text class="row-label">生效时间</text>
            <text class="row-value">{{ formatDateTime(permissionInfo.effectiveTime) }}</text>
          </view>
          <view class="detail-row">
            <text class="row-label">失效时间</text>
            <text class="row-value">{{ formatDateTime(permissionInfo.expireTime) }}</text>
          </view>
          <view v-if="permissionInfo.startTime" class="detail-row">
            <text class="row-label">允许时段</text>
            <text class="row-value">{{ permissionInfo.startTime }} - {{ permissionInfo.endTime }}</text>
          </view>
          <view class="detail-row">
            <text class="row-label">通行方式</text>
            <text class="row-value">{{ getPassTypeText() }}</text>
          </view>
        </view>
      </view>

      <!-- 最近通行 -->
      <view v-if="recentPasses.length > 0" class="recent-section">
        <view class="section-title">最近通行</view>
        <view class="pass-list">
          <view v-for="pass in recentPasses" :key="pass.recordId" class="pass-item">
            <view class="pass-time">{{ formatPassTime(pass.passTime) }}</view>
            <view class="pass-info">
              <text class="pass-device">{{ pass.deviceName }}</text>
              <view class="pass-result" :class="pass.passResult === 1 ? 'success' : 'fail'">
                {{ pass.passResult === 1 ? '通过' : '拒绝' }}
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 异常提示 -->
      <view v-if="permissionInfo.permissionStatus !== 1" class="alert-section">
        <view class="alert-card" :class="getAlertClass()">
          <uni-icons :type="getAlertIcon()" size="24" color="#fff"></uni-icons>
          <view class="alert-content">
            <text class="alert-title">{{ getAlertTitle() }}</text>
            <text class="alert-desc">{{ getAlertDesc() }}</text>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { accessApi } from '@/api/business/access/access-api.js'

/**
 * 权限二维码页面
 * 功能：
 * 1. 展示权限通行二维码
 * 2. 自动刷新二维码（每60秒）
 * 3. 刷新倒计时显示
 * 4. 快速操作（刷新、保存、分享）
 * 5. 权限详情展示
 * 6. 最近通行记录
 */

// ==================== 状态数据 ====================

const loading = ref(true)
const permissionId = ref('')

// 权限信息
const permissionInfo = ref({})

// 二维码数据
const qrcodeData = ref('')

// 自动刷新
const autoRefreshEnabled = ref(true)
const refreshCountdown = ref(60)
const refreshTimer = ref(null)

// 二维码倒计时进度
const showCountdown = ref(true)
const countdownProgress = ref(100)
const countdownTimer = ref(null)

// 最近通行
const recentPasses = ref([])

// 加载文本
const loadingText = ref({
  contentdown: '上拉加载更多',
  contentrefresh: '加载中...',
  contentnomore: '没有更多数据了'
})

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[权限二维码] 页面加载')

  // 获取权限ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.permissionId) {
    permissionId.value = options.permissionId
    loadPermissionInfo()
    loadRecentPasses()
  }
})

onUnmounted(() => {
  console.log('[权限二维码] 页面卸载')
  stopRefreshTimer()
  stopCountdownTimer()
})

// ==================== 数据加载 ====================

/**
 * 加载权限信息
 */
const loadPermissionInfo = async () => {
  loading.value = true

  try {
    const result = await accessApi.getPermissionDetail(permissionId.value)

    if (result.success && result.data) {
      permissionInfo.value = result.data
      await loadQRCode()
    }
  } catch (error) {
    console.error('[权限二维码] 加载权限信息失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 加载二维码
 */
const loadQRCode = async () => {
  try {
    const result = await accessApi.getPermissionQRCode(permissionId.value)

    if (result.success && result.data) {
      qrcodeData.value = result.data.qrCode

      // 启动自动刷新
      if (autoRefreshEnabled.value) {
        startRefreshTimer()
        startCountdownTimer()
      }
    }
  } catch (error) {
    console.error('[权限二维码] 加载二维码失败:', error)
    uni.showToast({
      title: '加载二维码失败',
      icon: 'none'
    })
  }
}

/**
 * 加载最近通行
 */
const loadRecentPasses = async () => {
  try {
    const result = await accessApi.getPermissionRecords(permissionId.value, {
      pageNum: 1,
      pageSize: 5
    })

    if (result.success && result.data) {
      recentPasses.value = result.data.list || []
    }
  } catch (error) {
    console.error('[权限二维码] 加载通行记录失败:', error)
  }
}

/**
 * 刷新二维码
 */
const refreshQRCode = async () => {
  stopRefreshTimer()
  stopCountdownTimer()

  qrcodeData.value = ''
  refreshCountdown.value = 60
  countdownProgress.value = 100

  uni.showLoading({
    title: '刷新中...',
    mask: true
  })

  try {
    await loadQRCode()
    uni.hideLoading()
    uni.showToast({
      title: '刷新成功',
      icon: 'success',
      duration: 1500
    })
  } catch (error) {
    uni.hideLoading()
    console.error('[权限二维码] 刷新失败:', error)
  }
}

// ==================== 定时器管理 ====================

/**
 * 启动刷新定时器
 */
const startRefreshTimer = () => {
  refreshCountdown.value = 60
  refreshTimer.value = setInterval(() => {
    refreshCountdown.value--
    if (refreshCountdown.value <= 0) {
      loadQRCode()
    }
  }, 1000)
}

/**
 * 停止刷新定时器
 */
const stopRefreshTimer = () => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
}

/**
 * 启动倒计时定时器
 */
const startCountdownTimer = () => {
  countdownProgress.value = 100
  const duration = 60000 // 60秒
  const interval = 100 // 100ms更新一次
  const steps = duration / interval

  countdownTimer.value = setInterval(() => {
    countdownProgress.value -= (100 / steps)
    if (countdownProgress.value <= 0) {
      countdownProgress.value = 100
    }
  }, interval)
}

/**
 * 停止倒计时定时器
 */
const stopCountdownTimer = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

// ==================== 交互操作 ====================

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 保存二维码
 */
const saveQRCode = () => {
  if (!qrcodeData.value) {
    uni.showToast({
      title: '二维码未加载完成',
      icon: 'none'
    })
    return
  }

  uni.saveImageToPhotosAlbum({
    filePath: qrcodeData.value,
    success: () => {
      uni.showToast({
        title: '已保存到相册',
        icon: 'success'
      })
    },
    fail: () => {
      uni.showToast({
        title: '保存失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 分享二维码
 */
const shareQRCode = () => {
  if (!qrcodeData.value) {
    uni.showToast({
      title: '二维码未加载完成',
      icon: 'none'
    })
    return
  }

  // TODO: 实现分享功能
  uni.showToast({
    title: '分享功能开发中',
    icon: 'none'
  })
}

// ==================== 工具方法 ====================

/**
 * 获取状态类
 */
const getStatusClass = () => {
  const status = permissionInfo.value.permissionStatus
  if (status === 1) return 'status-valid'
  if (status === 2) return 'status-expiring'
  if (status === 3) return 'status-expired'
  if (status === 4) return 'status-frozen'
  return ''
}

/**
 * 获取状态文本
 */
const getStatusText = () => {
  const status = permissionInfo.value.permissionStatus
  const statusMap = {
    1: '有效',
    2: '即将过期',
    3: '已过期',
    4: '已冻结'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取权限类型文本
 */
const getPermissionTypeText = () => {
  const type = permissionInfo.value.permissionType
  const typeMap = {
    1: '永久权限',
    2: '临时权限',
    3: '定时权限'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取通行方式文本
 */
const getPassTypeText = () => {
  const passTypeMap = {
    1: '人脸',
    2: '指纹',
    3: 'IC卡',
    4: '密码',
    5: '二维码'
  }

  if (!permissionInfo.value.passType || permissionInfo.value.passType.length === 0) {
    return '不限'
  }

  return permissionInfo.value.passType
    .map(type => passTypeMap[type])
    .join('、')
}

/**
 * 格式化日期
 */
const formatDate = (timestamp) => {
  if (!timestamp) return '-'

  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

/**
 * 格式化日期时间
 */
const formatDateTime = (timestamp) => {
  if (!timestamp) return '-'

  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/**
 * 格式化通行时间
 */
const formatPassTime = (timestamp) => {
  if (!timestamp) return '-'

  const date = new Date(timestamp)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return `今天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } else if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } else {
    return formatDateTime(timestamp)
  }
}

/**
 * 获取警告类
 */
const getAlertClass = () => {
  const status = permissionInfo.value.permissionStatus
  if (status === 2) return 'alert-warning'
  if (status === 3 || status === 4) return 'alert-error'
  return 'alert-info'
}

/**
 * 获取警告图标
 */
const getAlertIcon = () => {
  const status = permissionInfo.value.permissionStatus
  if (status === 2) return 'alert'
  if (status === 3) return 'close'
  if (status === 4) return 'snow'
  return 'info'
}

/**
 * 获取警告标题
 */
const getAlertTitle = () => {
  const status = permissionInfo.value.permissionStatus
  const titleMap = {
    2: '即将过期',
    3: '已过期',
    4: '已冻结'
  }
  return titleMap[status] || '提示'
}

/**
 * 获取警告描述
 */
const getAlertDesc = () => {
  const status = permissionInfo.value.permissionStatus
  const descMap = {
    2: '权限即将到期，请及时续期',
    3: '权限已过期，无法使用',
    4: '权限已冻结，暂时无法使用'
  }
  return descMap[status] || ''
}
</script>

<style lang="scss" scoped>
.permission-qrcode-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 导航栏
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  padding-bottom: 20rpx;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;

    .nav-title {
      flex: 1;
      text-align: center;
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
    }

    .nav-actions {
      width: 80rpx;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 加载容器
.loading-container {
  padding: 120rpx 0;
}

// 权限信息卡片
.permission-card {
  margin: 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .area-info {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .area-name {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .permission-status {
      padding: 8rpx 20rpx;
      border-radius: 20rpx;
      font-size: 24rpx;

      &.status-valid {
        background: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }

      &.status-expiring {
        background: rgba(250, 140, 22, 0.1);
        color: #fa8c16;
      }

      &.status-expired,
      &.status-frozen {
        background: rgba(255, 77, 79, 0.1);
        color: #ff4d4f;
      }
    }
  }

  .card-details {
    display: flex;
    flex-direction: column;
    gap: 12rpx;

    .detail-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 26rpx;

      .detail-label {
        color: #999;
      }

      .detail-value {
        color: #333;
      }
    }
  }
}

// 二维码区域
.qrcode-section {
  margin: 0 30rpx 30rpx;

  .qrcode-wrapper {
    position: relative;
    padding: 60rpx;
    background: #fff;
    border-radius: 24rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 30rpx;

    .qrcode-container {
      position: relative;
      width: 480rpx;
      height: 480rpx;

      .qrcode-image {
        width: 100%;
        height: 100%;
      }

      .countdown-bar {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 6rpx;
        background: #f0f0f0;
        border-radius: 0 0 8rpx 8rpx;
        overflow: hidden;

        .countdown-progress {
          height: 100%;
          background: linear-gradient(90deg, #1890ff 0%, #096dd9 100%);
          transition: width 0.1s linear;
        }
      }
    }

    .qrcode-loading {
      width: 480rpx;
      height: 480rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .refresh-countdown {
      padding: 12rpx 24rpx;
      background: #fffbe6;
      border-radius: 20rpx;
      font-size: 24rpx;
      color: #fa8c16;
    }
  }

  .usage-tips {
    display: flex;
    flex-direction: column;
    gap: 12rpx;

    .tip-item {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      font-size: 26rpx;
      color: #666;
    }
  }
}

// 快速操作
.quick-actions {
  display: flex;
  gap: 20rpx;
  margin: 0 30rpx 30rpx;

  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    height: 88rpx;
    background: #fff;
    border-radius: 16rpx;
    font-size: 28rpx;
    color: #666;
    border: none;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

    &.refresh-btn {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }

    &.save-btn {
      background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      color: #fff;
    }

    &.share-btn {
      background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
      color: #fff;
    }
  }
}

// 详情区域
.detail-section,
.recent-section,
.alert-section {
  margin: 0 30rpx 30rpx;

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }
}

// 详情列表
.detail-list {
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .detail-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .row-label {
      font-size: 28rpx;
      color: #666;
    }

    .row-value {
      font-size: 28rpx;
      color: #333;
      text-align: right;
      max-width: 400rpx;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

// 通行列表
.pass-list {
  padding: 20rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .pass-item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .pass-time {
      width: 140rpx;
      font-size: 24rpx;
      color: #999;
      flex-shrink: 0;
    }

    .pass-info {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .pass-device {
        font-size: 28rpx;
        color: #333;
      }

      .pass-result {
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 24rpx;
        font-weight: 500;

        &.success {
          background: rgba(82, 196, 26, 0.1);
          color: #52c41a;
        }

        &.fail {
          background: rgba(255, 77, 79, 0.1);
          color: #ff4d4f;
        }
      }
    }
  }
}

// 警告卡片
.alert-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 30rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);

  &.alert-warning {
    background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
  }

  &.alert-error {
    background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
  }

  &.alert-info {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  }

  .alert-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;

    .alert-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }

    .alert-desc {
      font-size: 26rpx;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}
</style>
