<template>
  <view class="upgrade-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">设备固件升级</text>
        </view>
      </view>
    </view>

    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 升级概览 -->
      <view class="overview-section">
        <view class="overview-card">
          <view class="overview-item">
            <text class="overview-value">{{ deviceStats.total }}</text>
            <text class="overview-label">总设备数</text>
          </view>
          <view class="overview-item">
            <text class="overview-value success">{{ deviceStats.latest }}</text>
            <text class="overview-label">最新版本</text>
          </view>
          <view class="overview-item">
            <text class="overview-value warning">{{ deviceStats.upgradeAvailable }}</text>
            <text class="overview-label">可升级</text>
          </view>
          <view class="overview-item">
            <text class="overview-value danger">{{ deviceStats.outdated }}</text>
            <text class="overview-label">版本过低</text>
          </view>
        </view>
      </view>

      <!-- 批量操作 -->
      <view class="batch-section">
        <view class="batch-actions">
          <view class="batch-btn" @tap="selectAll">
            <uni-icons type="checkbox" size="16" color="#667eea"></uni-icons>
            <text class="btn-text">全选</text>
          </view>
          <view class="batch-btn" @tap="batchUpgrade" :class="{ disabled: selectedDevices.length === 0 }">
            <uni-icons type="cloud-upload" size="16" color="#fff"></uni-icons>
            <text class="btn-text">批量升级({{ selectedDevices.length }})</text>
          </view>
        </view>
      </view>

      <!-- 设备列表 -->
      <view class="device-list">
        <view
          class="device-card"
          v-for="(device, index) in deviceList"
          :key="index"
        >
          <view class="device-checkbox" @tap="toggleSelect(device)">
            <view class="checkbox" :class="{ checked: device.selected }">
              <uni-icons v-if="device.selected" type="checkmarkempty" size="14" color="#fff"></uni-icons>
            </view>
          </view>
          <view class="device-info" @tap="viewDeviceDetail(device)">
            <view class="device-header">
              <view class="device-icon" :class="{ online: device.online }">
                <uni-icons type="locked" size="18" color="#fff"></uni-icons>
              </view>
              <view class="device-details">
                <text class="device-name">{{ device.deviceName }}</text>
                <text class="device-location">{{ device.location }}</text>
              </view>
            </view>
            <view class="device-firmware">
              <view class="firmware-info">
                <text class="firmware-label">当前版本</text>
                <text class="firmware-version">{{ device.currentVersion }}</text>
              </view>
              <view class="firmware-status" :class="device.versionStatus">
                <text>{{ getVersionStatusText(device.versionStatus) }}</text>
              </view>
            </view>
            <view class="device-latest" v-if="device.latestVersion">
              <text class="latest-label">最新版本</text>
              <text class="latest-version">{{ device.latestVersion }}</text>
              <text class="release-time">{{ device.releaseTime }}</text>
            </view>
          </view>
          <view class="device-actions">
            <view
              class="action-btn"
              :class="{ disabled: device.versionStatus === 'latest' }"
              @tap="upgradeDevice(device)"
            >
              <uni-icons type="cloud-upload" size="14" color="#fff"></uni-icons>
              <text class="btn-text">升级</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 升级历史 -->
      <view class="history-section">
        <view class="section-header">
          <text class="section-title">升级历史</text>
          <text class="section-more" @tap="viewAllHistory">查看全部</text>
        </view>
        <view class="history-list">
          <view
            class="history-item"
            v-for="(item, index) in upgradeHistory"
            :key="index"
          >
            <view class="history-icon" :class="item.status">
              <uni-icons
                :type="item.status === 'success' ? 'checkmarkempty' : 'closeempty'"
                size="16"
                color="#fff"
              ></uni-icons>
            </view>
            <view class="history-info">
              <text class="history-device">{{ item.deviceName }}</text>
              <text class="history-version">{{ item.oldVersion }} → {{ item.newVersion }}</text>
              <text class="history-time">{{ item.upgradeTime }}</text>
            </view>
            <view class="history-status" :class="item.status">
              <text>{{ getUpgradeStatusText(item.status) }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)
const refreshing = ref(false)

// 设备统计
const deviceStats = reactive({
  total: 24,
  latest: 15,
  upgradeAvailable: 7,
  outdated: 2
})

// 已选设备
const selectedDevices = ref([])

// 设备列表
const deviceList = ref([
  {
    deviceId: 'DEV001',
    deviceName: '主入口门禁',
    location: 'A栋1楼大厅',
    online: true,
    currentVersion: 'v2.1.0',
    latestVersion: 'v2.3.5',
    versionStatus: 'upgrade', // latest, upgrade, outdated
    releaseTime: '2024-12-15',
    selected: false
  },
  {
    deviceId: 'DEV002',
    deviceName: '侧门门禁',
    location: 'A栋2楼楼梯间',
    online: true,
    currentVersion: 'v2.3.5',
    latestVersion: 'v2.3.5',
    versionStatus: 'latest',
    releaseTime: '2024-12-15',
    selected: false
  },
  {
    deviceId: 'DEV003',
    deviceName: '后门门禁',
    location: 'A栋1楼后门',
    online: false,
    currentVersion: 'v2.0.1',
    latestVersion: 'v2.3.5',
    versionStatus: 'outdated',
    releaseTime: '2024-12-15',
    selected: false
  },
  {
    deviceId: 'DEV004',
    deviceName: '办公区门禁',
    location: 'A栋3楼办公区',
    online: true,
    currentVersion: 'v2.2.0',
    latestVersion: 'v2.3.5',
    versionStatus: 'upgrade',
    releaseTime: '2024-12-15',
    selected: false
  },
  {
    deviceId: 'DEV005',
    deviceName: '会议室门禁',
    location: 'B栋2楼会议室',
    online: true,
    currentVersion: 'v2.3.5',
    latestVersion: 'v2.3.5',
    versionStatus: 'latest',
    releaseTime: '2024-12-15',
    selected: false
  }
])

// 升级历史
const upgradeHistory = ref([
  {
    deviceId: 'DEV001',
    deviceName: '主入口门禁',
    oldVersion: 'v2.0.5',
    newVersion: 'v2.1.0',
    status: 'success',
    upgradeTime: '2小时前'
  },
  {
    deviceId: 'DEV004',
    deviceName: '办公区门禁',
    oldVersion: 'v2.1.0',
    newVersion: 'v2.2.0',
    status: 'success',
    upgradeTime: '1天前'
  },
  {
    deviceId: 'DEV002',
    deviceName: '侧门门禁',
    oldVersion: 'v2.2.0',
    newVersion: 'v2.3.0',
    status: 'failed',
    upgradeTime: '3天前'
  },
  {
    deviceId: 'DEV005',
    deviceName: '会议室门禁',
    oldVersion: 'v2.3.0',
    newVersion: 'v2.3.5',
    status: 'success',
    upgradeTime: '5天前'
  }
])

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 获取版本状态文本
const getVersionStatusText = (status) => {
  const statusMap = {
    latest: '最新版本',
    upgrade: '可升级',
    outdated: '版本过低'
  }
  return statusMap[status] || '未知'
}

// 获取升级状态文本
const getUpgradeStatusText = (status) => {
  const statusMap = {
    success: '成功',
    failed: '失败',
    pending: '进行中'
  }
  return statusMap[status] || '未知'
}

// 全选
const selectAll = () => {
  const allSelected = deviceList.value.every(d => d.selected)
  deviceList.value.forEach(device => {
    device.selected = !allSelected
  })
  updateSelectedDevices()
}

// 切换选择
const toggleSelect = (device) => {
  device.selected = !device.selected
  updateSelectedDevices()
}

// 更新已选设备
const updateSelectedDevices = () => {
  selectedDevices.value = deviceList.value.filter(d => d.selected)
}

// 查看设备详情
const viewDeviceDetail = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-detail?deviceId=${device.deviceId}`
  })
}

// 升级设备
const upgradeDevice = (device) => {
  if (device.versionStatus === 'latest') {
    uni.showToast({
      title: '已是最新版本',
      icon: 'none'
    })
    return
  }

  if (!device.online) {
    uni.showToast({
      title: '设备离线无法升级',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认升级',
    content: `确定要将${device.deviceName}升级到${device.latestVersion}吗？\n升级过程中设备将暂时无法使用。`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '升级中...' })
        // TODO: 调用实际升级API
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '升级成功',
            icon: 'success'
          })
          // 刷新设备列表
          onRefresh()
        }, 3000)
      }
    }
  })
}

// 批量升级
const batchUpgrade = () => {
  if (selectedDevices.value.length === 0) {
    uni.showToast({
      title: '请先选择设备',
      icon: 'none'
    })
    return
  }

  const offlineCount = selectedDevices.value.filter(d => !d.online).length
  if (offlineCount > 0) {
    uni.showToast({
      title: `${offlineCount}台设备离线`,
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '批量升级',
    content: `确定要升级选中的${selectedDevices.value.length}台设备吗？`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '准备升级...' })
        // TODO: 调用实际批量升级API
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '批量升级已启动',
            icon: 'success'
          })
          // 清空选择
          deviceList.value.forEach(d => d.selected = false)
          updateSelectedDevices()
        }, 1500)
      }
    }
  })
}

// 查看全部历史
const viewAllHistory = () => {
  uni.navigateTo({
    url: '/pages/access/upgrade-history'
  })
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  // TODO: 重新加载数据
  setTimeout(() => {
    refreshing.value = false
  }, 1000)
}

// 加载更多
const loadMore = () => {
  // TODO: 分页加载
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.upgrade-container {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

// 自定义导航栏
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
    height: 44px;
    padding: 0 30rpx;

    .navbar-left {
      display: flex;
      align-items: center;

      .navbar-title {
        margin-left: 20rpx;
        font-size: 18px;
        font-weight: 500;
        color: #fff;
      }
    }
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 44px);
}

// 概览
.overview-section {
  padding: 30rpx;

  .overview-card {
    display: flex;
    background: #fff;
    border-radius: 24rpx;
    padding: 40rpx 30rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .overview-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;

      .overview-value {
        font-size: 32px;
        font-weight: 600;
        color: #333;
        margin-bottom: 10rpx;

        &.success {
          color: #52c41a;
        }

        &.warning {
          color: #faad14;
        }

        &.danger {
          color: #ff4d4f;
        }
      }

      .overview-label {
        font-size: 13px;
        color: #999;
      }
    }
  }
}

// 批量操作
.batch-section {
  padding: 0 30rpx 30rpx;

  .batch-actions {
    display: flex;
    gap: 20rpx;

    .batch-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 72rpx;
      background: #fff;
      border-radius: 16rpx;
      border: 1rpx solid #e0e0e0;

      &.disabled {
        opacity: 0.5;
      }

      &:not(.disabled):nth-child(2) {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;

        .btn-text {
          color: #fff;
        }

        uni-icons {
          color: #fff !important;
        }
      }

      uni-icons {
        margin-right: 8rpx;
      }

      .btn-text {
        font-size: 13px;
        color: #667eea;
      }
    }
  }
}

// 设备列表
.device-list {
  padding: 0 30rpx 30rpx;

  .device-card {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    &:last-child {
      margin-bottom: 0;
    }

    .device-checkbox {
      margin-right: 20rpx;

      .checkbox {
        width: 36rpx;
        height: 36rpx;
        border: 2rpx solid #e0e0e0;
        border-radius: 8rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        &.checked {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-color: transparent;
        }
      }
    }

    .device-info {
      flex: 1;

      .device-header {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;

        .device-icon {
          width: 48rpx;
          height: 48rpx;
          border-radius: 12rpx;
          background: #e0e0e0;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20rpx;

          &.online {
            background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
          }
        }

        .device-details {
          display: flex;
          flex-direction: column;

          .device-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 6rpx;
          }

          .device-location {
            font-size: 12px;
            color: #999;
          }
        }
      }

      .device-firmware {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16rpx;
        background: #f9f9f9;
        border-radius: 12rpx;
        margin-bottom: 12rpx;

        .firmware-info {
          display: flex;
          flex-direction: column;

          .firmware-label {
            font-size: 11px;
            color: #999;
            margin-bottom: 6rpx;
          }

          .firmware-version {
            font-size: 13px;
            font-weight: 500;
            color: #333;
          }
        }

        .firmware-status {
          padding: 4rpx 16rpx;
          border-radius: 12rpx;
          font-size: 10px;

          &.latest {
            background: #f6ffed;
            color: #52c41a;
          }

          &.upgrade {
            background: #fff7e6;
            color: #faad14;
          }

          &.outdated {
            background: #fff1f0;
            color: #ff4d4f;
          }
        }
      }

      .device-latest {
        display: flex;
        align-items: center;
        font-size: 11px;
        color: #999;

        .latest-label {
          margin-right: 10rpx;
        }

        .latest-version {
          margin-right: 20rpx;
          color: #52c41a;
          font-weight: 500;
        }
      }
    }

    .device-actions {
      margin-left: 20rpx;

      .action-btn {
        display: flex;
        align-items: center;
        padding: 12rpx 20rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12rpx;

        &.disabled {
          background: #e0e0e0;
        }

        uni-icons {
          margin-right: 8rpx;
        }

        .btn-text {
          font-size: 12px;
          color: #fff;
        }
      }
    }
  }
}

// 升级历史
.history-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .section-more {
      font-size: 13px;
      color: #667eea;
    }
  }

  .history-list {
    .history-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .history-icon {
        width: 40rpx;
        height: 40rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;

        &.success {
          background: #52c41a;
        }

        &.failed {
          background: #ff4d4f;
        }
      }

      .history-info {
        flex: 1;

        .history-device {
          display: block;
          font-size: 14px;
          color: #333;
          margin-bottom: 6rpx;
        }

        .history-version {
          display: block;
          font-size: 12px;
          color: #666;
          margin-bottom: 6rpx;
        }

        .history-time {
          font-size: 11px;
          color: #999;
        }
      }

      .history-status {
        padding: 4rpx 16rpx;
        border-radius: 12rpx;
        font-size: 11px;

        &.success {
          background: #f6ffed;
          color: #52c41a;
        }

        &.failed {
          background: #fff1f0;
          color: #ff4d4f;
        }
      }
    }
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
