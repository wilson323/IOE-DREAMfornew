<template>
  <view class="device-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">设备监控</text>
        </view>
        <view class="navbar-right">
          <view class="refresh-btn" @tap="refreshDevices">
            <uni-icons type="refreshempty" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 设备统计卡片 -->
      <view class="stats-cards">
        <view class="stat-card">
          <view class="stat-icon">
            <uni-icons type="settings" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ deviceStats.total }}</text>
            <text class="stat-label">设备总数</text>
          </view>
        </view>

        <view class="stat-card online">
          <view class="stat-icon">
            <uni-icons type="checkmarkempty" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ deviceStats.online }}</text>
            <text class="stat-label">在线设备</text>
          </view>
        </view>

        <view class="stat-card offline">
          <view class="stat-icon">
            <uni-icons type="close" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ deviceStats.offline }}</text>
            <text class="stat-label">离线设备</text>
          </view>
        </view>

        <view class="stat-card fault">
          <view class="stat-icon">
            <uni-icons type="notification" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ deviceStats.fault }}</text>
            <text class="stat-label">故障设备</text>
          </view>
        </view>
      </view>

      <!-- 筛选标签 -->
      <view class="filter-tags">
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'all' }"
          @tap="selectFilter('all')"
        >
          全部 ({{ deviceList.length }})
        </view>
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'online' }"
          @tap="selectFilter('online')"
        >
          在线 ({{ onlineCount }})
        </view>
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'offline' }"
          @tap="selectFilter('offline')"
        >
          离线 ({{ offlineCount }})
        </view>
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'fault' }"
          @tap="selectFilter('fault')"
        >
          故障 ({{ faultCount }})
        </view>
      </view>

      <!-- 设备类型筛选 -->
      <view class="type-filter">
        <scroll-view scroll-x class="type-scroll" show-scrollbar>
          <view
            class="type-item"
            :class="{ active: selectedType === 'all' }"
            @tap="selectType('all')"
          >
            全部类型
          </view>
          <view
            class="type-item"
            :class="{ active: selectedType === 'access' }"
            @tap="selectType('access')"
          >
            <uni-icons type="locked" size="16"></uni-icons>
            <text>门禁</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedType === 'attendance' }"
            @tap="selectType('attendance')"
          >
            <uni-icons type="calendar" size="16"></uni-icons>
            <text>考勤</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedType === 'consume' }"
            @tap="selectType('consume')"
          >
            <uni-icons type="shop" size="16"></uni-icons>
            <text>消费</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedType === 'video' }"
            @tap="selectType('video')"
          >
            <uni-icons type="videocam" size="16"></uni-icons>
            <text>视频</text>
          </view>
        </scroll-view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input
          class="search-input"
          type="text"
          placeholder="搜索设备名称、编号"
          v-model="searchKeyword"
          @input="onSearchInput"
          @confirm="onSearchConfirm"
        />
        <view class="search-btn" @tap="onSearchConfirm">搜索</view>
      </view>

      <!-- 设备列表 -->
      <scroll-view
        class="device-list"
        scroll-y
        @scrolltolower="loadMoreDevices"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view
          class="device-item"
          v-for="device in filteredDevices"
          :key="device.deviceId"
          @tap="viewDeviceDetail(device)"
        >
          <!-- 设备头部 -->
          <view class="device-header">
            <view class="device-name">{{ device.deviceName }}</view>
            <view class="device-status" :class="device.status">
              <view class="status-dot"></view>
              <text class="status-text">{{ getStatusText(device.status) }}</text>
            </view>
          </view>

          <!-- 设备信息 -->
          <view class="device-info">
            <view class="info-row">
              <uni-icons type="settings" size="14" color="#666"></uni-icons>
              <text class="info-text">编号: {{ device.deviceCode }}</text>
            </view>
            <view class="info-row">
              <uni-icons type="home" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ device.areaName }}</text>
              <uni-icons type="location" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ device.location }}</text>
            </view>
            <view class="info-row">
              <uni-icons type="phone" size="14" color="#666"></uni-icons>
              <text class="info-text">IP: {{ device.ipAddress }}</text>
            </view>
          </view>

          <!-- 设备性能指标 -->
          <view class="device-performance" v-if="device.status === 'online'">
            <view class="performance-item">
              <view class="performance-label">CPU</view>
              <view class="performance-value" :class="{ warning: device.cpuUsage > 80, danger: device.cpuUsage > 90 }">
                {{ device.cpuUsage }}%
              </view>
            </view>
            <view class="performance-item">
              <view class="performance-label">内存</view>
              <view class="performance-value" :class="{ warning: device.memoryUsage > 80, danger: device.memoryUsage > 90 }">
                {{ device.memoryUsage }}%
              </view>
            </view>
            <view class="performance-item">
              <view class="performance-label">温度</view>
              <view class="performance-value" :class="{ warning: device.temperature > 60, danger: device.temperature > 70 }">
                {{ device.temperature }}°C
              </view>
            </view>
            <view class="performance-item">
              <view class="performance-label">电池</view>
              <view class="performance-value" :class="{ warning: device.battery < 30, danger: device.battery < 20 }">
                {{ device.battery }}%
              </view>
            </view>
          </view>

          <!-- 统计数据 -->
          <view class="device-stats" v-if="device.status === 'online'">
            <view class="stat-item">
              <text class="stat-label">今日通行:</text>
              <text class="stat-value">{{ device.todayPassCount }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-label">通信延迟:</text>
              <text class="stat-value" :class="{ warning: device.latency > 100, danger: device.latency > 200 }">
                {{ device.latency }}ms
              </text>
            </view>
            <view class="stat-item">
              <text class="stat-label">运行时长:</text>
              <text class="stat-value">{{ formatUptime(device.uptime) }}</text>
            </view>
          </view>

          <!-- 设备操作 -->
          <view class="device-actions">
            <view class="action-btn" @tap.stop="restartDevice(device)" v-if="device.status !== 'offline'">
              <uni-icons type="refresh" size="18" color="#667eea"></uni-icons>
              <text class="action-text">重启</text>
            </view>
            <view class="action-btn" @tap.stop="viewDeviceLog(device)">
              <uni-icons type="list" size="18" color="#667eea"></uni-icons>
              <text class="action-text">日志</text>
            </view>
            <view class="action-btn" @tap.stop="configureDevice(device)">
              <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
              <text class="action-text">配置</text>
            </view>
            <view class="action-btn danger" @tap.stop="maintainDevice(device)" v-if="device.status === 'fault'">
              <uni-icons type="notification" size="18" color="#ff6b6b"></uni-icons>
              <text class="action-text">报修</text>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="hasMore">
          <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
          <text class="load-text">加载中...</text>
        </view>

        <!-- 没有更多 -->
        <view class="no-more" v-if="!hasMore && filteredDevices.length > 0">
          <text class="no-more-text">没有更多设备了</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="filteredDevices.length === 0 && !loading">
          <uni-icons type="settings" size="80" color="#ddd"></uni-icons>
          <text class="empty-text">暂无设备</text>
        </view>
      </scroll-view>
    </view>

    <!-- 设备详情弹窗 -->
    <uni-popup ref="detailPopup" type="center">
      <view class="detail-popup">
        <view class="popup-header">
          <text class="popup-title">设备详情</text>
          <view class="close-btn" @tap="closeDetailPopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <view class="detail-section">
            <view class="section-title">基本信息</view>
            <view class="detail-row">
              <text class="detail-label">设备名称:</text>
              <text class="detail-value">{{ currentDevice.deviceName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">设备编号:</text>
              <text class="detail-value">{{ currentDevice.deviceCode }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">设备类型:</text>
              <text class="detail-value">{{ getTypeText(currentDevice.deviceType) }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">所在区域:</text>
              <text class="detail-value">{{ currentDevice.areaName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">安装位置:</text>
              <text class="detail-value">{{ currentDevice.location }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">IP地址:</text>
              <text class="detail-value">{{ currentDevice.ipAddress }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">MAC地址:</text>
              <text class="detail-value">{{ currentDevice.macAddress }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">固件版本:</text>
              <text class="detail-value">{{ currentDevice.firmwareVersion }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">安装时间:</text>
              <text class="detail-value">{{ formatDate(currentDevice.installTime) }}</text>
            </view>
          </view>

          <view class="detail-section" v-if="currentDevice.status === 'online'">
            <view class="section-title">运行状态</view>
            <view class="detail-row">
              <text class="detail-label">CPU使用率:</text>
              <text class="detail-value" :class="{ warning: currentDevice.cpuUsage > 80, danger: currentDevice.cpuUsage > 90 }">
                {{ currentDevice.cpuUsage }}%
              </text>
            </view>
            <view class="detail-row">
              <text class="detail-label">内存使用率:</text>
              <text class="detail-value" :class="{ warning: currentDevice.memoryUsage > 80, danger: currentDevice.memoryUsage > 90 }">
                {{ currentDevice.memoryUsage }}%
              </text>
            </view>
            <view class="detail-row">
              <text class="detail-label">设备温度:</text>
              <text class="detail-value" :class="{ warning: currentDevice.temperature > 60, danger: currentDevice.temperature > 70 }">
                {{ currentDevice.temperature }}°C
              </text>
            </view>
            <view class="detail-row">
              <text class="detail-label">电池电量:</text>
              <text class="detail-value" :class="{ warning: currentDevice.battery < 30, danger: currentDevice.battery < 20 }">
                {{ currentDevice.battery }}%
              </text>
            </view>
            <view class="detail-row">
              <text class="detail-label">通信延迟:</text>
              <text class="detail-value" :class="{ warning: currentDevice.latency > 100, danger: currentDevice.latency > 200 }">
                {{ currentDevice.latency }}ms
              </text>
            </view>
            <view class="detail-row">
              <text class="detail-label">运行时长:</text>
              <text class="detail-value">{{ formatUptime(currentDevice.uptime) }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">今日通行:</text>
              <text class="detail-value">{{ currentDevice.todayPassCount }}次</text>
            </view>
          </view>

          <view class="detail-section">
            <view class="section-title">维护记录</view>
            <view class="maintenance-list" v-if="currentDevice.maintenances && currentDevice.maintenances.length > 0">
              <view class="maintenance-item" v-for="item in currentDevice.maintenances" :key="item.id">
                <view class="maintenance-time">{{ formatDate(item.time) }}</view>
                <view class="maintenance-type">{{ item.type }}</view>
                <view class="maintenance-remark">{{ item.remark }}</view>
              </view>
            </view>
            <view class="empty-maintenance" v-else>
              <text class="empty-text">暂无维护记录</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 设备统计
const deviceStats = reactive({
  total: 48,
  online: 42,
  offline: 4,
  fault: 2
})

// 设备列表
const deviceList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)

// 筛选条件
const selectedFilter = ref('all')
const selectedType = ref('all')
const searchKeyword = ref('')

// 当前设备
const currentDevice = ref({})

// 计算在线数量
const onlineCount = computed(() => {
  return deviceList.value.filter(d => d.status === 'online').length
})

// 计算离线数量
const offlineCount = computed(() => {
  return deviceList.value.filter(d => d.status === 'offline').length
})

// 计算故障数量
const faultCount = computed(() => {
  return deviceList.value.filter(d => d.status === 'fault').length
})

// 筛选后的设备列表
const filteredDevices = computed(() => {
  let result = deviceList.value

  // 状态筛选
  if (selectedFilter.value !== 'all') {
    result = result.filter(d => d.status === selectedFilter.value)
  }

  // 类型筛选
  if (selectedType.value !== 'all') {
    result = result.filter(d => d.deviceType === selectedType.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(d =>
      d.deviceName.toLowerCase().includes(keyword) ||
      d.deviceCode.toLowerCase().includes(keyword) ||
      d.areaName.toLowerCase().includes(keyword)
    )
  }

  return result
})

onMounted(() => {
  // 获取系统信息
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  // 加载数据
  loadDeviceList()
  startRealTimeUpdate()
})

onUnmounted(() => {
  stopRealTimeUpdate()
})

// 实时更新定时器
let updateTimer = null

const startRealTimeUpdate = () => {
  updateTimer = setInterval(() => {
    loadDeviceList(true)
  }, 10000) // 10秒更新一次
}

const stopRealTimeUpdate = () => {
  if (updateTimer) {
    clearInterval(updateTimer)
    updateTimer = null
  }
}

// 加载设备列表
const loadDeviceList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))

    // 模拟数据
    const mockData = generateMockDevices(currentPage.value)

    if (isRefresh) {
      deviceList.value = mockData
      currentPage.value = 1
    } else {
      deviceList.value.push(...mockData)
    }

    hasMore.value = mockData.length >= 20
  } catch (error) {
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 生成模拟数据
const generateMockDevices = (page) => {
  const types = ['access', 'attendance', 'consume', 'video']
  const statuses = ['online', 'offline', 'fault']

  const devices = []
  for (let i = 0; i < 20; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const isOnline = status === 'online'

    devices.push({
      deviceId: `DEV${Date.now()}${i}`,
      deviceCode: `D${String(i + 1).padStart(4, '0')}`,
      deviceName: `${getTypeText(type)}-${String(i + 1).padStart(3, '0')}`,
      deviceType: type,
      status: status,
      areaName: ['A栋1楼大厅', 'A栋2楼办公区', 'B栋会议室', 'C栋餐厅'][Math.floor(Math.random() * 4)],
      location: ['主入口', '侧门', '后门', '大厅', '走廊'][Math.floor(Math.random() * 5)],
      ipAddress: `192.168.1.${100 + i}`,
      macAddress: `00:1A:2B:3C:4D:${String(i).padStart(2, '0').toUpperCase()}`,
      firmwareVersion: 'v2.1.0',
      installTime: Date.now() - Math.random() * 31536000000,
      cpuUsage: isOnline ? Math.floor(Math.random() * 40) + 30 : 0,
      memoryUsage: isOnline ? Math.floor(Math.random() * 40) + 40 : 0,
      temperature: isOnline ? Math.floor(Math.random() * 30) + 35 : 0,
      battery: isOnline ? Math.floor(Math.random() * 60) + 40 : 0,
      latency: isOnline ? Math.floor(Math.random() * 150) + 10 : 0,
      uptime: isOnline ? Math.floor(Math.random() * 86400000) + 3600000 : 0,
      todayPassCount: isOnline ? Math.floor(Math.random() * 200) + 50 : 0,
      maintenances: []
    })
  }

  return devices
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadDeviceList(true)
}

// 加载更多
const loadMoreDevices = () => {
  if (!hasMore.value || loading.value) return
  currentPage.value++
  loadDeviceList()
}

// 搜索
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
}

const onSearchConfirm = () => {
  loadDeviceList(true)
}

// 筛选
const selectFilter = (filter) => {
  selectedFilter.value = filter
}

const selectType = (type) => {
  selectedType.value = type
}

// 刷新设备
const refreshDevices = () => {
  loadDeviceList(true)
}

// 查看设备详情
const viewDeviceDetail = (device) => {
  currentDevice.value = device
  uni.$emit('openDetailPopup')
}

// 关闭详情弹窗
const closeDetailPopup = () => {
  uni.$emit('closeDetailPopup')
}

// 重启设备
const restartDevice = (device) => {
  uni.showModal({
    title: '重启设备',
    content: `确认重启设备 ${device.deviceName}？`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '重启中...'
        })

        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '重启成功',
            icon: 'success'
          })
        }, 2000)
      }
    }
  })
}

// 查看设备日志
const viewDeviceLog = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-log?deviceId=${device.deviceId}`
  })
}

// 配置设备
const configureDevice = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-config?deviceId=${device.deviceId}`
  })
}

// 报修设备
const maintainDevice = (device) => {
  uni.showModal({
    title: '设备报修',
    content: `确认报修设备 ${device.deviceName}？`,
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '报修成功',
          icon: 'success'
        })
      }
    }
  })
}

// 工具方法
const getStatusText = (status) => {
  const texts = {
    online: '在线',
    offline: '离线',
    fault: '故障'
  }
  return texts[status] || '未知'
}

const getTypeText = (type) => {
  const texts = {
    access: '门禁设备',
    attendance: '考勤设备',
    consume: '消费设备',
    video: '视频设备'
  }
  return texts[type] || '未知'
}

const formatUptime = (milliseconds) => {
  const days = Math.floor(milliseconds / 86400000)
  const hours = Math.floor((milliseconds % 86400000) / 3600000)
  const minutes = Math.floor((milliseconds % 3600000) / 60000)

  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

const formatDate = (timestamp) => {
  if (!timestamp) return '--'

  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e8ecf1 100%);
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
      gap: 10rpx;

      .back-text {
        font-size: 28rpx;
        color: #fff;
      }
    }

    .navbar-center {
      .navbar-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .navbar-right {
      .refresh-btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.page-content {
  padding-top: calc(44px + var(--status-bar-height));
  padding-bottom: 40rpx;
}

.stats-cards {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  flex-wrap: wrap;

  .stat-card {
    flex: 1;
    min-width: 160rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    display: flex;
    align-items: center;
    gap: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    &.online {
      background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    &.offline {
      background: linear-gradient(135deg, #8c8c8c 0%, #bfbfbf 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    &.fault {
      background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    .stat-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 20rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .stat-value {
        font-size: 40rpx;
        font-weight: 700;
        color: #333;
      }

      .stat-label {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.filter-tags {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 20rpx;
  overflow-x: auto;

  .filter-tag {
    flex-shrink: 0;
    padding: 12rpx 32rpx;
    background: #fff;
    border-radius: 40rpx;
    font-size: 26rpx;
    color: #666;
    white-space: nowrap;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

.type-filter {
  padding: 0 30rpx 20rpx;

  .type-scroll {
    white-space: nowrap;

    .type-item {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 24rpx;
      margin-right: 20rpx;
      background: #fff;
      border-radius: 40rpx;
      font-size: 26rpx;
      color: #666;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin: 0 30rpx 20rpx;
  padding: 20rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .search-input {
    flex: 1;
    font-size: 28rpx;
  }

  .search-btn {
    padding: 12rpx 32rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 40rpx;
    font-size: 26rpx;
    color: #fff;
  }
}

.device-list {
  height: calc(100vh - 44px - var(--status-bar-height) - 450rpx);
  padding: 0 30rpx;

  .device-item {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .device-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      .device-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
      }

      .device-status {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.online {
          background: #f6ffed;
          color: #52c41a;

          .status-dot {
            background: #52c41a;
          }
        }

        &.offline {
          background: #f5f5f5;
          color: #999;

          .status-dot {
            background: #999;
          }
        }

        &.fault {
          background: #fff2f0;
          color: #ff6b6b;

          .status-dot {
            background: #ff6b6b;
          }
        }

        .status-dot {
          width: 16rpx;
          height: 16rpx;
          border-radius: 50%;
        }

        .status-text {
          font-size: 24rpx;
        }
      }
    }

    .device-info {
      margin-bottom: 20rpx;

      .info-row {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-bottom: 12rpx;
        flex-wrap: wrap;

        &:last-child {
          margin-bottom: 0;
        }

        .info-text {
          font-size: 26rpx;
          color: #666;
        }
      }
    }

    .device-performance {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20rpx;
      padding: 20rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      margin-bottom: 20rpx;

      .performance-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8rpx;

        .performance-label {
          font-size: 22rpx;
          color: #999;
        }

        .performance-value {
          font-size: 28rpx;
          font-weight: 600;
          color: #52c41a;

          &.warning {
            color: #ffa940;
          }

          &.danger {
            color: #ff6b6b;
          }
        }
      }
    }

    .device-stats {
      display: flex;
      flex-wrap: wrap;
      gap: 24rpx;
      margin-bottom: 20rpx;

      .stat-item {
        flex: 1;
        min-width: 150rpx;

        .stat-label {
          font-size: 24rpx;
          color: #999;
          margin-right: 8rpx;
        }

        .stat-value {
          font-size: 26rpx;
          color: #333;
          font-weight: 600;

          &.warning {
            color: #ffa940;
          }

          &.danger {
            color: #ff6b6b;
          }
        }
      }
    }

    .device-actions {
      display: flex;
      gap: 20rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;

      .action-btn {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8rpx;
        padding: 16rpx 0;

        &.danger {
          .action-text {
            color: #ff6b6b;
          }
        }

        .action-text {
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }
}

.load-more,
.no-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 40rpx 0;

  .load-text,
  .no-more-text {
    font-size: 26rpx;
    color: #999;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;

  .empty-text {
    margin-top: 30rpx;
    font-size: 28rpx;
    color: #999;
  }
}

// 弹窗样式
.detail-popup {
  width: 690rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  max-height: 80vh;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .close-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    height: 60vh;
    padding: 30rpx;

    .detail-section {
      margin-bottom: 40rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 20rpx;
        padding-bottom: 12rpx;
        border-bottom: 2rpx solid #f0f0f0;
      }

      .detail-row {
        display: flex;
        margin-bottom: 20rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .detail-label {
          min-width: 180rpx;
          font-size: 26rpx;
          color: #666;
        }

        .detail-value {
          flex: 1;
          font-size: 26rpx;
          color: #333;

          &.warning {
            color: #ffa940;
          }

          &.danger {
            color: #ff6b6b;
          }
        }
      }

      .maintenance-list {
        .maintenance-item {
          padding: 20rpx;
          background: #f5f7fa;
          border-radius: 12rpx;
          margin-bottom: 16rpx;

          &:last-child {
            margin-bottom: 0;
          }

          .maintenance-time {
            font-size: 24rpx;
            color: #999;
            margin-bottom: 8rpx;
          }

          .maintenance-type {
            font-size: 26rpx;
            color: #333;
            font-weight: 600;
            margin-bottom: 8rpx;
          }

          .maintenance-remark {
            font-size: 24rpx;
            color: #666;
            line-height: 1.6;
          }
        }
      }

      .empty-maintenance {
        padding: 60rpx 0;
        text-align: center;

        .empty-text {
          font-size: 26rpx;
          color: #999;
        }
      }
    }
  }
}
</style>
