<template>
  <view class="device-capacity-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="nav-back" @click="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">设备容量</text>
        <view class="nav-action" @click="refreshCapacity">
          <uni-icons type="refreshempty" size="20" color="#fff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="content-scroll" :style="{ paddingTop: navbarHeight + 'px' }">
      <!-- 总体容量卡片 -->
      <view class="capacity-overview">
        <view class="overview-header">
          <text class="overview-title">容量概览</text>
          <text class="overview-subtitle">总容量使用情况</text>
        </view>

        <view class="overview-stats">
          <view class="stat-item">
            <text class="stat-value">{{ capacitySummary.totalCapacity || 0 }}</text>
            <text class="stat-label">总容量</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value used">{{ capacitySummary.usedCapacity || 0 }}</text>
            <text class="stat-label">已使用</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value available">{{ capacitySummary.availableCapacity || 0 }}</text>
            <text class="stat-label">剩余</text>
          </view>
        </view>

        <!-- 总体使用率 -->
        <view class="overall-usage">
          <view class="usage-header">
            <text class="usage-label">总体使用率</text>
            <text :class="['usage-percent', getUsageLevel(capacitySummary.usagePercent)]">
              {{ capacitySummary.usagePercent || 0 }}%
            </text>
          </view>
          <view class="usage-bar-bg">
            <view
              class="usage-bar-fill"
              :style="{
                width: (capacitySummary.usagePercent || 0) + '%',
                background: getUsageGradient(capacitySummary.usagePercent)
              }"
            ></view>
          </view>
        </view>
      </view>

      <!-- 分类容量统计 -->
      <view class="capacity-section">
        <view class="section-title">分类容量</view>

        <!-- 人脸容量 -->
        <view class="capacity-card">
          <view class="card-header">
            <view class="header-left">
              <view class="icon-wrap face">
                <uni-icons type="person" size="20" color="#fff"></uni-icons>
              </view>
              <text class="card-title">人脸容量</text>
            </view>
            <view :class="['capacity-badge', getUsageLevel(capacity.faceUsagePercent)]">
              {{ capacity.faceUsagePercent || 0 }}%
            </view>
          </view>

          <view class="capacity-details">
            <view class="detail-row">
              <text class="detail-label">已存储</text>
              <text class="detail-value">{{ capacity.faceUsed || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">总容量</text>
              <text class="detail-value">{{ capacity.faceCapacity || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">剩余</text>
              <text class="detail-value available">{{ capacity.faceAvailable || 0 }}</text>
            </view>
          </view>

          <view class="capacity-bar">
            <view
              class="bar-fill"
              :style="{
                width: (capacity.faceUsagePercent || 0) + '%',
                background: getUsageGradient(capacity.faceUsagePercent)
              }"
            ></view>
          </view>
        </view>

        <!-- 指纹容量 -->
        <view class="capacity-card">
          <view class="card-header">
            <view class="header-left">
              <view class="icon-wrap fingerprint">
                <uni-icons type="scan" size="20" color="#fff"></uni-icons>
              </view>
              <text class="card-title">指纹容量</text>
            </view>
            <view :class="['capacity-badge', getUsageLevel(capacity.fingerprintUsagePercent)]">
              {{ capacity.fingerprintUsagePercent || 0 }}%
            </view>
          </view>

          <view class="capacity-details">
            <view class="detail-row">
              <text class="detail-label">已存储</text>
              <text class="detail-value">{{ capacity.fingerprintUsed || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">总容量</text>
              <text class="detail-value">{{ capacity.fingerprintCapacity || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">剩余</text>
              <text class="detail-value available">{{ capacity.fingerprintAvailable || 0 }}</text>
            </view>
          </view>

          <view class="capacity-bar">
            <view
              class="bar-fill"
              :style="{
                width: (capacity.fingerprintUsagePercent || 0) + '%',
                background: getUsageGradient(capacity.fingerprintUsagePercent)
              }"
            ></view>
          </view>
        </view>

        <!-- 卡片容量 -->
        <view class="capacity-card">
          <view class="card-header">
            <view class="header-left">
              <view class="icon-wrap card">
                <uni-icons type="contact" size="20" color="#fff"></uni-icons>
              </view>
              <text class="card-title">卡片容量</text>
            </view>
            <view :class="['capacity-badge', getUsageLevel(capacity.cardUsagePercent)]">
              {{ capacity.cardUsagePercent || 0 }}%
            </view>
          </view>

          <view class="capacity-details">
            <view class="detail-row">
              <text class="detail-label">已存储</text>
              <text class="detail-value">{{ capacity.cardUsed || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">总容量</text>
              <text class="detail-value">{{ capacity.cardCapacity || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">剩余</text>
              <text class="detail-value available">{{ capacity.cardAvailable || 0 }}</text>
            </view>
          </view>

          <view class="capacity-bar">
            <view
              class="bar-fill"
              :style="{
                width: (capacity.cardUsagePercent || 0) + '%',
                background: getUsageGradient(capacity.cardUsagePercent)
              }"
            ></view>
          </view>
        </view>

        <!-- 密码容量 -->
        <view class="capacity-card">
          <view class="card-header">
            <view class="header-left">
              <view class="icon-wrap password">
                <uni-icons type="locked" size="20" color="#fff"></uni-icons>
              </view>
              <text class="card-title">密码容量</text>
            </view>
            <view :class="['capacity-badge', getUsageLevel(capacity.passwordUsagePercent)]">
              {{ capacity.passwordUsagePercent || 0 }}%
            </view>
          </view>

          <view class="capacity-details">
            <view class="detail-row">
              <text class="detail-label">已存储</text>
              <text class="detail-value">{{ capacity.passwordUsed || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">总容量</text>
              <text class="detail-value">{{ capacity.passwordCapacity || 0 }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">剩余</text>
              <text class="detail-value available">{{ capacity.passwordAvailable || 0 }}</text>
            </view>
          </view>

          <view class="capacity-bar">
            <view
              class="bar-fill"
              :style="{
                width: (capacity.passwordUsagePercent || 0) + '%',
                background: getUsageGradient(capacity.passwordUsagePercent)
              }"
            ></view>
          </view>
        </view>
      </view>

      <!-- 容量预警 -->
      <view v-if="hasWarning" class="warning-section">
        <view class="warning-header">
          <uni-icons type="warn" size="20" color="#ff4d4f"></uni-icons>
          <text class="warning-title">容量预警</text>
        </view>

        <view v-for="warning in warnings" :key="warning.type" class="warning-item">
          <view class="warning-icon">
            <uni-icons type="info" size="16" color="#ff4d4f"></uni-icons>
          </view>
          <text class="warning-text">{{ warning.message }}</text>
        </view>
      </view>

      <!-- 操作建议 -->
      <view class="tips-section">
        <view class="tips-header">
          <uni-icons type="help" size="20" color="#1890ff"></uni-icons>
          <text class="tips-title">操作建议</text>
        </view>

        <view class="tip-item">
          <text class="tip-dot">•</text>
          <text class="tip-text">当使用率超过80%时，建议及时清理无效数据</text>
        </view>
        <view class="tip-item">
          <text class="tip-dot">•</text>
          <text class="tip-text">定期备份重要数据，防止数据丢失</text>
        </view>
        <view class="tip-item">
          <text class="tip-dot">•</text>
          <text class="tip-text">建议保留10%的剩余空间用于新用户录入</text>
        </view>
        <view class="tip-item">
          <text class="tip-dot">•</text>
          <text class="tip-text">容量不足时，可以升级设备或添加新设备</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view style="height: 40rpx;"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'

// 导航栏高度
const navbarHeight = ref(44)

// 设备ID
const deviceId = ref('')

// 容量汇总
const capacitySummary = reactive({
  totalCapacity: 0,
  usedCapacity: 0,
  availableCapacity: 0,
  usagePercent: 0
})

// 分类容量
const capacity = reactive({
  // 人脸
  faceCapacity: 0,
  faceUsed: 0,
  faceAvailable: 0,
  faceUsagePercent: 0,

  // 指纹
  fingerprintCapacity: 0,
  fingerprintUsed: 0,
  fingerprintAvailable: 0,
  fingerprintUsagePercent: 0,

  // 卡片
  cardCapacity: 0,
  cardUsed: 0,
  cardAvailable: 0,
  cardUsagePercent: 0,

  // 密码
  passwordCapacity: 0,
  passwordUsed: 0,
  passwordAvailable: 0,
  passwordUsagePercent: 0
})

// 预警列表
const warnings = ref([])

// 是否有预警
const hasWarning = computed(() => warnings.value.length > 0)

// 获取使用率等级
const getUsageLevel = (percent) => {
  if (percent >= 90) return 'danger'
  if (percent >= 80) return 'warning'
  if (percent >= 50) return 'normal'
  return 'good'
}

// 获取使用率渐变色
const getUsageGradient = (percent) => {
  if (percent >= 90) return 'linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%)'
  if (percent >= 80) return 'linear-gradient(135deg, #faad14 0%, #ffc53d 100%)'
  if (percent >= 50) return 'linear-gradient(135deg, #1890ff 0%, #40a9ff 100%)'
  return 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)'
}

// 加载容量数据
const loadCapacity = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    const res = await deviceApi.getDeviceCapacity(deviceId.value)

    uni.hideLoading()

    if (res.code === 200 && res.data) {
      const data = res.data

      // 更新分类容量
      Object.assign(capacity, {
        faceCapacity: data.faceCapacity || 0,
        faceUsed: data.faceUsed || 0,
        faceAvailable: Math.max(0, (data.faceCapacity || 0) - (data.faceUsed || 0)),
        faceUsagePercent: data.faceCapacity ? Math.round((data.faceUsed / data.faceCapacity) * 100) : 0,

        fingerprintCapacity: data.fingerprintCapacity || 0,
        fingerprintUsed: data.fingerprintUsed || 0,
        fingerprintAvailable: Math.max(0, (data.fingerprintCapacity || 0) - (data.fingerprintUsed || 0)),
        fingerprintUsagePercent: data.fingerprintCapacity ? Math.round((data.fingerprintUsed / data.fingerprintCapacity) * 100) : 0,

        cardCapacity: data.cardCapacity || 0,
        cardUsed: data.cardUsed || 0,
        cardAvailable: Math.max(0, (data.cardCapacity || 0) - (data.cardUsed || 0)),
        cardUsagePercent: data.cardCapacity ? Math.round((data.cardUsed / data.cardCapacity) * 100) : 0,

        passwordCapacity: data.passwordCapacity || 0,
        passwordUsed: data.passwordUsed || 0,
        passwordAvailable: Math.max(0, (data.passwordCapacity || 0) - (data.passwordUsed || 0)),
        passwordUsagePercent: data.passwordCapacity ? Math.round((data.passwordUsed / data.passwordCapacity) * 100) : 0
      })

      // 更新总体容量
      const totalCap = (data.faceCapacity || 0) + (data.fingerprintCapacity || 0) +
                       (data.cardCapacity || 0) + (data.passwordCapacity || 0)
      const totalUsed = (data.faceUsed || 0) + (data.fingerprintUsed || 0) +
                       (data.cardUsed || 0) + (data.passwordUsed || 0)

      capacitySummary.totalCapacity = totalCap
      capacitySummary.usedCapacity = totalUsed
      capacitySummary.availableCapacity = Math.max(0, totalCap - totalUsed)
      capacitySummary.usagePercent = totalCap ? Math.round((totalUsed / totalCap) * 100) : 0

      // 检查预警
      checkWarnings()
    }
  } catch (error) {
    uni.hideLoading()
    console.error('加载容量数据失败:', error)
    uni.showToast({ title: '加载失败，请重试', icon: 'none' })
  }
}

// 检查预警
const checkWarnings = () => {
  const list = []

  if (capacity.faceUsagePercent >= 90) {
    list.push({ type: 'face', message: '人脸容量使用率超过90%，请及时清理' })
  }
  if (capacity.fingerprintUsagePercent >= 90) {
    list.push({ type: 'fingerprint', message: '指纹容量使用率超过90%，请及时清理' })
  }
  if (capacity.cardUsagePercent >= 90) {
    list.push({ type: 'card', message: '卡片容量使用率超过90%，请及时清理' })
  }
  if (capacity.passwordUsagePercent >= 90) {
    list.push({ type: 'password', message: '密码容量使用率超过90%，请及时清理' })
  }

  warnings.value = list
}

// 刷新容量
const refreshCapacity = () => {
  loadCapacity()
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  navbarHeight.value = systemInfo.statusBarHeight + 44

  // 获取设备ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  if (options.deviceId) {
    deviceId.value = options.deviceId
    loadCapacity()
  }
})
</script>

<style lang="scss" scoped>
.device-capacity-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 30rpx;

    .nav-back {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .nav-title {
      font-size: 36rpx;
      font-weight: 600;
      color: white;
    }

    .nav-action {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding: 20rpx 30rpx;
  box-sizing: border-box;
}

// 总体容量卡片
.capacity-overview {
  background: white;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  &::before {
    content: '';
    display: block;
    height: 6rpx;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx 24rpx 0 0;
    margin: -40rpx -40rpx 30rpx -40rpx;
  }

  .overview-header {
    margin-bottom: 30rpx;

    .overview-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      display: block;
      margin-bottom: 8rpx;
    }

    .overview-subtitle {
      font-size: 24rpx;
      color: #999;
    }
  }

  .overview-stats {
    display: flex;
    align-items: center;
    justify-content: space-around;
    margin-bottom: 30rpx;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .stat-value {
        font-size: 48rpx;
        font-weight: 700;
        color: #333;
        line-height: 1.2;
        margin-bottom: 8rpx;

        &.used {
          color: #1890ff;
        }

        &.available {
          color: #52c41a;
        }
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }

    .stat-divider {
      width: 2rpx;
      height: 60rpx;
      background: #f0f0f0;
    }
  }

  .overall-usage {
    .usage-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16rpx;

      .usage-label {
        font-size: 28rpx;
        color: #666;
      }

      .usage-percent {
        font-size: 36rpx;
        font-weight: 700;

        &.danger {
          color: #ff4d4f;
        }

        &.warning {
          color: #faad14;
        }

        &.normal {
          color: #1890ff;
        }

        &.good {
          color: #52c41a;
        }
      }
    }

    .usage-bar-bg {
      height: 24rpx;
      background: #f0f0f0;
      border-radius: 12rpx;
      overflow: hidden;

      .usage-bar-fill {
        height: 100%;
        border-radius: 12rpx;
        transition: width 0.3s;
      }
    }
  }
}

// 容量区块
.capacity-section {
  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
    padding-left: 10rpx;
  }
}

// 容量卡片
.capacity-card {
  background: white;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24rpx;

    .header-left {
      display: flex;
      align-items: center;

      .icon-wrap {
        width: 56rpx;
        height: 56rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16rpx;

        &.face {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        &.fingerprint {
          background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
        }

        &.card {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
        }

        &.password {
          background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
        }
      }

      .card-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .capacity-badge {
      padding: 8rpx 16rpx;
      border-radius: 12rpx;
      font-size: 24rpx;
      font-weight: 600;
      color: white;

      &.danger {
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
      }

      &.warning {
        background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
      }

      &.normal {
        background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
      }

      &.good {
        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
      }
    }
  }

  .capacity-details {
    display: flex;
    margin-bottom: 20rpx;

    .detail-row {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .detail-label {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 8rpx;
      }

      .detail-value {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;

        &.available {
          color: #52c41a;
        }
      }
    }
  }

  .capacity-bar {
    height: 16rpx;
    background: #f0f0f0;
    border-radius: 8rpx;
    overflow: hidden;

    .bar-fill {
      height: 100%;
      border-radius: 8rpx;
      transition: width 0.3s;
    }
  }
}

// 预警区块
.warning-section {
  background: #fff2f0;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  border: 2rpx solid #ffccc7;

  .warning-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .warning-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #ff4d4f;
      margin-left: 12rpx;
    }
  }

  .warning-item {
    display: flex;
    align-items: flex-start;
    margin-bottom: 16rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .warning-icon {
      margin-right: 12rpx;
      margin-top: 4rpx;
    }

    .warning-text {
      flex: 1;
      font-size: 26rpx;
      color: #ff4d4f;
      line-height: 1.6;
    }
  }
}

// 操作建议区块
.tips-section {
  background: white;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .tips-header {
    display: flex;
    align-items: center;
    margin-bottom: 20rpx;

    .tips-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
      margin-left: 12rpx;
    }
  }

  .tip-item {
    display: flex;
    margin-bottom: 16rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .tip-dot {
      font-size: 28rpx;
      color: #1890ff;
      margin-right: 12rpx;
    }

    .tip-text {
      flex: 1;
      font-size: 26rpx;
      color: #666;
      line-height: 1.6;
    }
  }
}
</style>
