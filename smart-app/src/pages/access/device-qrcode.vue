<template>
  <view class="device-qrcode-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">设备二维码</view>
        <view class="navbar-right" @tap="shareQRCode">
          <uni-icons type="paperplane" size="20" color="#ffffff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view class="main-scroll" scroll-y>
      <!-- 设备信息卡片 -->
      <view class="device-info-card">
        <view class="device-header">
          <view class="device-icon" :style="{ background: getDeviceIconGradient() }">
            <uni-icons type="locked" size="32" color="#ffffff"></uni-icons>
          </view>
          <view class="device-detail">
            <text class="device-name">{{ deviceInfo.deviceName }}</text>
            <text class="device-code">{{ deviceInfo.deviceCode }}</text>
          </view>
        </view>
      </view>

      <!-- 二维码展示 -->
      <view class="qrcode-card">
        <view class="qrcode-header">
          <text class="qrcode-title">设备二维码</text>
          <text class="qrcode-desc">扫描二维码查看设备信息</text>
        </view>

        <!-- 二维码图片 -->
        <view class="qrcode-wrapper">
          <image class="qrcode-image" :src="qrcodeUrl" mode="aspectFit" @tap="previewQRCode"></image>
          <view class="qrcode-loading" v-if="loading">
            <uni-icons type="spinner-cycle" size="32" color="#667eea"></uni-icons>
            <text class="loading-text">生成中...</text>
          </view>
        </view>

        <!-- 设备信息摘要 -->
        <view class="device-summary">
          <view class="summary-item">
            <uni-icons type="location" size="14" color="#999"></uni-icons>
            <text class="summary-text">{{ deviceInfo.areaName || '未设置区域' }}</text>
          </view>
          <view class="summary-item">
            <uni-icons type="wifi" size="14" color="#999"></uni-icons>
            <text class="summary-text">{{ deviceInfo.ipAddress || '未配置IP' }}</text>
          </view>
          <view class="summary-item">
            <uni-icons type="settings" size="14" color="#999"></uni-icons>
            <text class="summary-text">{{ getDeviceTypeLabel() }}</text>
          </view>
        </view>

        <!-- 二维码操作 -->
        <view class="qrcode-actions">
          <button class="action-btn primary" @tap="saveQRCode">
            <uni-icons type="download" size="18" color="#ffffff"></uni-icons>
            <text class="btn-text">保存到相册</text>
          </button>
          <button class="action-btn secondary" @tap="shareQRCode">
            <uni-icons type="redo" size="18" color="#667eea"></uni-icons>
            <text class="btn-text">分享</text>
          </button>
        </view>
      </view>

      <!-- 二维码设置 -->
      <view class="settings-card">
        <view class="settings-header">
          <text class="settings-title">二维码设置</text>
        </view>

        <view class="settings-list">
          <!-- 二维码尺寸 -->
          <view class="setting-item">
            <text class="setting-label">二维码尺寸</text>
            <picker mode="selector" :range="qrcodeSizes" @change="onSizeChange">
              <view class="picker-value">
                <text>{{ getQrcodeSizeLabel(qrcodeSize) }}</text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 显示设备信息 -->
          <view class="setting-item switch-setting">
            <text class="setting-label">显示设备信息</text>
            <switch
              :checked="showDeviceInfo"
              @change="onShowInfoChange"
              color="#667eea"
            />
          </view>

          <!-- 显示设备状态 -->
          <view class="setting-item switch-setting">
            <text class="setting-label">显示设备状态</text>
            <switch
              :checked="showDeviceStatus"
              @change="onShowStatusChange"
              color="#667eea"
            />
          </view>

          <!-- 刷新二维码 -->
          <view class="setting-item">
            <text class="setting-label">刷新二维码</text>
            <view class="refresh-btn" @tap="refreshQRCode">
              <uni-icons type="refresh" size="16" color="#667eea"></uni-icons>
              <text class="refresh-text">点击刷新</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 使用说明 -->
      <view class="guide-card">
        <view class="guide-header">
          <text class="guide-title">使用说明</text>
        </view>

        <view class="guide-list">
          <view class="guide-item">
            <view class="guide-number">1</view>
            <text class="guide-text">扫描二维码可快速查看设备详细信息</text>
          </view>
          <view class="guide-item">
            <view class="guide-number">2</view>
            <text class="guide-text">支持手机微信、扫一扫等扫码工具</text>
          </view>
          <view class="guide-item">
            <view class="guide-number">3</view>
            <text class="guide-text">可保存二维码图片用于打印张贴</text>
          </view>
          <view class="guide-item">
            <view class="guide-number">4</view>
            <text class="guide-text">建议将二维码张贴在设备附近便于查看</text>
          </view>
        </view>
      </view>

      <!-- 扫描记录 -->
      <view class="records-card" v-if="scanRecords.length > 0">
        <view class="records-header">
          <text class="records-title">最近扫描</text>
          <text class="view-all" @tap="viewAllRecords">查看全部</text>
        </view>

        <view class="records-list">
          <view
            v-for="record in scanRecords"
            :key="record.id"
            class="record-item"
          >
            <view class="record-time">{{ record.scanTime }}</view>
            <view class="record-user">
              <uni-icons type="person" size="14" color="#999"></uni-icons>
              <text class="user-text">{{ record.userName }}</text>
            </view>
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

// 设备信息
const deviceInfo = reactive({
  deviceName: '1号楼大厅门禁',
  deviceCode: 'ACC001',
  deviceType: 1,
  areaName: 'A栋1楼大厅',
  ipAddress: '192.168.1.100',
  onlineStatus: 1
})

// 二维码
const qrcodeUrl = ref('')
const loading = ref(false)
const qrcodeSize = ref('medium')

// 设置
const showDeviceInfo = ref(true)
const showDeviceStatus = ref(true)

// 选项数据
const qrcodeSizes = [
  { label: '小尺寸', value: 'small' },
  { label: '中尺寸', value: 'medium' },
  { label: '大尺寸', value: 'large' }
]

// 扫描记录
const scanRecords = ref([
  {
    id: 1,
    scanTime: '2025-01-30 14:30',
    userName: '张三'
  },
  {
    id: 2,
    scanTime: '2025-01-30 10:15',
    userName: '李四'
  },
  {
    id: 3,
    scanTime: '2025-01-29 16:45',
    userName: '王五'
  }
])

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  deviceId.value = options.deviceId || ''

  loadDeviceInfo()
  generateQRCode()
})

// 加载设备信息
const loadDeviceInfo = async () => {
  try {
    const res = await deviceApi.getDeviceDetail(deviceId.value)
    if (res.code === 200 && res.data) {
      Object.assign(deviceInfo, res.data)
    }
  } catch (error) {
    console.error('加载设备信息失败:', error)
  }
}

// 生成二维码
const generateQRCode = async () => {
  loading.value = true

  try {
    // 构建二维码数据
    const qrcodeData = {
      deviceId: deviceId.value,
      deviceCode: deviceInfo.deviceCode,
      timestamp: Date.now()
    }

    const dataStr = JSON.stringify(qrcodeData)
    const encodedData = encodeURIComponent(dataStr)

    // 调用后端API生成二维码
    const res = await deviceApi.generateDeviceQRCode(deviceId.value, {
      size: qrcodeSize.value,
      showInfo: showDeviceInfo.value,
      showStatus: showDeviceStatus.value
    })

    if (res.code === 200 && res.data) {
      qrcodeUrl.value = res.data.qrcodeUrl
    } else {
      // 如果后端没有实现，使用占位图
      qrcodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodedData}`
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
    // 使用占位图
    const qrcodeData = {
      deviceId: deviceId.value,
      deviceCode: deviceInfo.deviceCode,
      timestamp: Date.now()
    }
    const dataStr = JSON.stringify(qrcodeData)
    const encodedData = encodeURIComponent(dataStr)
    qrcodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodedData}`
  } finally {
    loading.value = false
  }
}

// 预览二维码
const previewQRCode = () => {
  uni.previewImage({
    urls: [qrcodeUrl.value],
    current: qrcodeUrl.value
  })
}

// 保存二维码
const saveQRCode = () => {
  uni.showLoading({
    title: '保存中...'
  })

  // 下载图片
  uni.downloadFile({
    url: qrcodeUrl.value,
    success: (res) => {
      if (res.statusCode === 200) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading()
            uni.showToast({
              title: '已保存到相册',
              icon: 'success'
            })
          },
          fail: () => {
            uni.hideLoading()
            uni.showToast({
              title: '保存失败',
              icon: 'none'
            })
          }
        })
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({
        title: '下载失败',
        icon: 'none'
      })
    }
  })
}

// 分享二维码
const shareQRCode = () => {
  uni.showActionSheet({
    itemList: ['发送给朋友', '分享到朋友圈', '保存到相册'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 发送给朋友
        uni.showToast({
          title: '分享功能开发中',
          icon: 'none'
        })
      } else if (res.tapIndex === 1) {
        // 分享到朋友圈
        uni.showToast({
          title: '分享功能开发中',
          icon: 'none'
        })
      } else if (res.tapIndex === 2) {
        // 保存到相册
        saveQRCode()
      }
    }
  })
}

// 刷新二维码
const refreshQRCode = () => {
  generateQRCode()
  uni.showToast({
    title: '二维码已刷新',
    icon: 'success'
  })
}

// 获取设备类型标签
const getDeviceTypeLabel = () => {
  const types = {
    1: '门禁控制器',
    2: '门禁一体机',
    3: '考勤机',
    4: '消费机'
  }
  return types[deviceInfo.deviceType] || '未知'
}

// 获取设备图标渐变色
const getDeviceIconGradient = () => {
  return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
}

// 获取二维码尺寸标签
const getQrcodeSizeLabel = (size) => {
  const item = qrcodeSizes.find(s => s.value === size)
  return item ? item.label : '中尺寸'
}

// 尺寸变更
const onSizeChange = (e) => {
  qrcodeSize.value = qrcodeSizes[e.detail.value].value
  generateQRCode()
}

// 显示信息变更
const onShowInfoChange = (e) => {
  showDeviceInfo.value = e.detail.value
  generateQRCode()
}

// 显示状态变更
const onShowStatusChange = (e) => {
  showDeviceStatus.value = e.detail.value
  generateQRCode()
}

// 查看全部记录
const viewAllRecords = () => {
  uni.showToast({
    title: '扫描记录功能开发中',
    icon: 'none'
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-qrcode-page {
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

// 设备信息卡片
.device-info-card {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.device-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.device-icon {
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.device-detail {
  flex: 1;
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

// 二维码卡片
.qrcode-card {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.qrcode-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.qrcode-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12rpx;
}

.qrcode-desc {
  display: block;
  font-size: 24rpx;
  color: #999999;
}

.qrcode-wrapper {
  width: 500rpx;
  height: 500rpx;
  margin: 0 auto 40rpx;
  position: relative;
  background: #f5f7fa;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qrcode-image {
  width: 450rpx;
  height: 450rpx;
}

.qrcode-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20rpx;
}

.loading-text {
  font-size: 24rpx;
  color: #667eea;
}

.device-summary {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 30rpx;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.summary-text {
  font-size: 26rpx;
  color: #666666;
}

.qrcode-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border-radius: 16rpx;
  font-size: 26rpx;
  font-weight: 600;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.action-btn.secondary {
  background: #f5f7fa;
  color: #667eea;
}

.btn-text {
  font-size: 26rpx;
}

// 设置卡片
.settings-card {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.settings-header {
  margin-bottom: 20rpx;
}

.settings-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.setting-label {
  font-size: 28rpx;
  color: #333333;
}

.picker-value {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #667eea;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.refresh-text {
  font-size: 24rpx;
  color: #667eea;
}

.switch-setting {
  .setting-label {
    flex: 1;
  }
}

// 使用说明卡片
.guide-card {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.guide-header {
  margin-bottom: 20rpx;
}

.guide-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.guide-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.guide-item {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.guide-number {
  width: 48rpx;
  height: 48rpx;
  flex-shrink: 0;
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
  font-size: 26rpx;
  color: #666666;
  line-height: 1.5;
}

// 扫描记录卡片
.records-card {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.records-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.view-all {
  font-size: 24rpx;
  color: #667eea;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.record-time {
  font-size: 24rpx;
  color: #999999;
}

.record-user {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.user-text {
  font-size: 26rpx;
  color: #666666;
}
</style>
