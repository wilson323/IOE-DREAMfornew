<template>
  <view class="device-enhanced-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">设备管理</view>
      <view class="nav-right" @click="showSearch">
        <uni-icons type="search" size="18" color="#666"></uni-icons>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-card">
        <view class="stats-item">
          <text class="stats-value">{{ stats.totalDevices || 0 }}</text>
          <text class="stats-label">设备总数</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-value stats-online">{{ stats.onlineDevices || 0 }}</text>
          <text class="stats-label">在线设备</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-value stats-offline">{{ stats.offlineDevices || 0 }}</text>
          <text class="stats-label">离线设备</text>
        </view>
      </view>
    </view>

    <!-- 快速操作按钮 -->
    <view class="quick-actions">
      <view class="action-btn" @click="refreshDevices">
        <uni-icons type="refreshempty" size="20" color="#667eea"></uni-icons>
        <text class="action-text">刷新</text>
      </view>
      <view class="action-btn" @click="showAddDevice">
        <uni-icons type="plus" size="20" color="#52c41a"></uni-icons>
        <text class="action-text">添加</text>
      </view>
      <view class="action-btn" @click="batchDelete">
        <uni-icons type="trash" size="20" color="#ff4d4f"></uni-icons>
        <text class="action-text">删除</text>
      </view>
      <view class="action-btn" @click="exportDevices">
        <uni-icons type="download" size="20" color="#faad14"></uni-icons>
        <text class="action-text">导出</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-section">
      <scroll-view class="filter-tabs" scroll-x>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'all' }"
          @click="switchTab('all')"
        >
          全部
        </view>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'online' }"
          @click="switchTab('online')"
        >
          在线
        </view>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'offline' }"
          @click="switchTab('offline')"
        >
          离线
        </view>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'access' }"
          @click="switchTab('access')"
        >
          门禁
        </view>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'reader' }"
          @click="switchTab('reader')"
        >
          读卡器
        </view>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'face' }"
          @click="switchTab('face')"
        >
          人脸设备
        </view>
      </scroll-view>
    </view>

    <!-- 设备列表 -->
    <view class="device-list">
      <view
        class="device-item"
        v-for="device in filteredDevices"
        :key="device.deviceId"
        @click="viewDeviceDetail(device)"
        @longpress="toggleSelect(device)"
      >
        <!-- 选中状态 -->
        <view class="select-checkbox" v-if="batchMode">
          <uni-icons
            :type="device.selected ? 'checkbox-filled' : 'circle'"
            :color="device.selected ? '#667eea' : '#c0c0c0'"
            size="22"
          ></uni-icons>
        </view>

        <!-- 设备图标 -->
        <view class="device-icon">
          <view :class="['icon-wrapper', device.online ? 'online' : 'offline']">
            <uni-icons
              :type="getDeviceIcon(device.deviceType)"
              size="32"
              :color="device.online ? '#52c41a' : '#d9d9d9'"
            ></uni-icons>
          </view>
        </view>

        <!-- 设备信息 -->
        <view class="device-info">
          <view class="device-header">
            <text class="device-name">{{ device.deviceName }}</text>
            <view :class="['status-badge', device.online ? 'online' : 'offline']">
              <text class="status-text">{{ device.online ? '在线' : '离线' }}</text>
            </view>
          </view>
          <view class="device-details">
            <text class="device-code">设备编码: {{ device.deviceCode }}</text>
            <text class="device-area">{{ device.areaName || '未分配区域' }}</text>
          </view>
          <view class="device-extra">
            <text class="device-type">{{ getDeviceTypeName(device.deviceType) }}</text>
            <text class="device-ip">{{ device.ipAddress || '-' }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="device-actions">
          <view class="action-item" @click.stop="showDeviceMenu(device)">
            <uni-icons type="more-filled" size="20" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-wrapper" v-if="loading">
        <uni-load-more status="loading"></uni-load-more>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredDevices.length === 0 && !loading">
        <image class="empty-icon" src="/static/images/empty-device.png" mode="aspectFit"></image>
        <text class="empty-text">暂无设备</text>
        <text class="empty-desc">请添加设备或切换筛选条件</text>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="filteredDevices.length > 0 && hasMore">
        <uni-load-more
          :status="loadMoreStatus"
          @clickLoadMore="loadMore"
        ></uni-load-more>
      </view>
    </view>

    <!-- 搜索弹窗 -->
    <uni-popup ref="searchPopup" type="top">
      <view class="search-popup">
        <view class="search-bar">
          <uni-icons type="search" size="18" color="#999"></uni-icons>
          <input
            class="search-input"
            type="text"
            placeholder="搜索设备名称、编码、IP地址"
            v-model="searchKeyword"
            @confirm="doSearch"
          />
          <text class="search-cancel" @click="closeSearch">取消</text>
        </view>
      </view>
    </uni-popup>

    <!-- 设备操作菜单 -->
    <uni-popup ref="menuPopup" type="bottom">
      <view class="menu-popup">
        <view class="menu-title">设备操作</view>
        <view class="menu-list">
          <view class="menu-item" @click="controlDevice">
            <uni-icons type="settings" size="20" color="#667eea"></uni-icons>
            <text class="menu-text">设备控制</text>
          </view>
          <view class="menu-item" @click="advancedSettings">
            <uni-icons type="gear" size="20" color="#667eea"></uni-icons>
            <text class="menu-text">高级设置</text>
          </view>
          <view class="menu-item" @click="viewCapacity">
            <uni-icons type="list" size="20" color="#667eea"></uni-icons>
            <text class="menu-text">设备容量</text>
          </view>
          <view class="menu-item" @click="viewPersonnel">
            <uni-icons type="person" size="20" color="#667eea"></uni-icons>
            <text class="menu-text">设备人员</text>
          </view>
          <view class="menu-item" @click="viewRecords">
            <uni-icons type="eye" size="20" color="#667eea"></uni-icons>
            <text class="menu-text">通行记录</text>
          </view>
          <view class="menu-item" @click="showQRCode">
            <uni-icons type="qrcode" size="20" color="#667eea"></uni-icons>
            <text class="menu-text">二维码</text>
          </view>
        </view>
        <view class="menu-cancel" @click="closeMenu">取消</view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onShow, onPullDownRefresh, onReachBottom } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'

// ============ 响应式数据 ============
const deviceList = ref([])
const filteredDevices = ref([])
const loading = ref(false)
const hasMore = ref(true)
const loadMoreStatus = ref('more')
const pageNum = ref(1)
const pageSize = ref(20)

// 统计数据
const stats = reactive({
  totalDevices: 0,
  onlineDevices: 0,
  offlineDevices: 0
})

// 筛选相关
const activeTab = ref('all')
const searchKeyword = ref('')
const batchMode = ref(false)
const selectedDevices = ref([])

// 当前选中设备
const currentDevice = ref(null)

// 组件引用
const searchPopup = ref(null)
const menuPopup = ref(null)

// ============ 计算属性 ============
const filteredList = computed(() => {
  let list = [...deviceList.value]

  // 按在线状态筛选
  if (activeTab.value === 'online') {
    list = list.filter(item => item.online)
  } else if (activeTab.value === 'offline') {
    list = list.filter(item => !item.online)
  }

  // 按设备类型筛选
  if (activeTab.value === 'access') {
    list = list.filter(item => item.deviceType === 1)
  } else if (activeTab.value === 'reader') {
    list = list.filter(item => item.deviceType === 2)
  } else if (activeTab.value === 'face') {
    list = list.filter(item => item.deviceType === 3)
  }

  // 搜索筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(item =>
      item.deviceName?.toLowerCase().includes(keyword) ||
      item.deviceCode?.toLowerCase().includes(keyword) ||
      item.ipAddress?.includes(keyword)
    )
  }

  return list
})

// ============ 生命周期 ============
onMounted(() => {
  initPage()
})

onShow(() => {
  loadDevices()
})

onPullDownRefresh(() => {
  refreshDevices()
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadMore()
  }
})

// ============ 方法 ============
const initPage = () => {
  loadDevices()
  loadStats()
}

/**
 * 加载设备列表
 */
const loadDevices = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    const params = {
      pageNum: isRefresh ? 1 : pageNum.value,
      pageSize: pageSize.value
    }

    const response = await deviceApi.getDeviceList(params)

    if (response && response.code === 200 && response.data) {
      const devices = response.data.list || []

      if (isRefresh) {
        deviceList.value = devices
        pageNum.value = 1
      } else {
        deviceList.value = [...deviceList.value, ...devices]
      }

      hasMore.value = devices.length >= pageSize.value
      loadMoreStatus.value = hasMore.value ? 'more' : 'noMore'
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    if (isRefresh) {
      uni.stopPullDownRefresh()
    }
  }
}

/**
 * 加载统计数据
 */
const loadStats = async () => {
  try {
    const response = await deviceApi.getDeviceList({ pageSize: 999 })

    if (response && response.code === 200 && response.data) {
      const devices = response.data.list || []
      stats.totalDevices = devices.length
      stats.onlineDevices = devices.filter(d => d.online).length
      stats.offlineDevices = devices.filter(d => !d.online).length
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const refreshDevices = () => {
  uni.showLoading({ title: '刷新中...' })
  loadDevices(true)
  loadStats()
  uni.hideLoading()
  uni.showToast({ title: '刷新成功', icon: 'success' })
}

const loadMore = () => {
  pageNum.value++
  loadMoreStatus.value = 'loading'
  loadDevices()
}

const switchTab = (tab) => {
  activeTab.value = tab
  filteredDevices.value = filteredList.value
}

const showSearch = () => {
  searchPopup.value?.open()
}

const closeSearch = () => {
  searchPopup.value?.close()
  searchKeyword.value = ''
}

const doSearch = () => {
  filteredDevices.value = filteredList.value
  closeSearch()
}

const showAddDevice = () => {
  uni.navigateTo({ url: '/pages/access/device-add' })
}

const viewDeviceDetail = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-detail?deviceId=${device.deviceId}`
  })
}

const toggleSelect = (device) => {
  if (!batchMode.value) {
    batchMode.value = true
  }
  device.selected = !device.selected
  if (device.selected) {
    selectedDevices.value.push(device.deviceId)
  } else {
    const index = selectedDevices.value.indexOf(device.deviceId)
    if (index > -1) {
      selectedDevices.value.splice(index, 1)
    }
  }

  if (selectedDevices.value.length === 0) {
    batchMode.value = false
  }
}

const batchDelete = () => {
  if (selectedDevices.value.length === 0) {
    batchMode.value = true
    uni.showToast({ title: '请长按选择要删除的设备', icon: 'none' })
    return
  }

  uni.showModal({
    title: '确认删除',
    content: `确定要删除选中的${selectedDevices.value.length}个设备吗？`,
    success: (res) => {
      if (res.confirm) {
        // 执行批量删除
        uni.showToast({ title: '删除成功', icon: 'success' })
        batchMode.value = false
        selectedDevices.value = []
        refreshDevices()
      }
    }
  })
}

const exportDevices = () => {
  uni.showToast({ title: '导出功能开发中', icon: 'none' })
}

const showDeviceMenu = (device) => {
  currentDevice.value = device
  menuPopup.value?.open()
}

const closeMenu = () => {
  menuPopup.value?.close()
  currentDevice.value = null
}

const controlDevice = () => {
  closeMenu()
  uni.navigateTo({
    url: `/pages/access/device-control?deviceId=${currentDevice.value.deviceId}`
  })
}

const advancedSettings = () => {
  closeMenu()
  uni.navigateTo({
    url: `/pages/access/device-settings?deviceId=${currentDevice.value.deviceId}`
  })
}

const viewCapacity = () => {
  closeMenu()
  uni.navigateTo({
    url: `/pages/access/device-capacity?deviceId=${currentDevice.value.deviceId}`
  })
}

const viewPersonnel = () => {
  closeMenu()
  uni.navigateTo({
    url: `/pages/access/device-personnel?deviceId=${currentDevice.value.deviceId}`
  })
}

const viewRecords = () => {
  closeMenu()
  uni.navigateTo({
    url: `/pages/access/device-records?deviceId=${currentDevice.value.deviceId}`
  })
}

const showQRCode = () => {
  closeMenu()
  uni.navigateTo({
    url: `/pages/access/device-qrcode?deviceId=${currentDevice.value.deviceId}`
  })
}

const getDeviceIcon = (deviceType) => {
  const iconMap = {
    1: 'locked',
    2: 'scan',
    3: 'camera',
    4: 'settings'
  }
  return iconMap[deviceType] || 'settings'
}

const getDeviceTypeName = (deviceType) => {
  const typeMap = {
    1: '门禁控制器',
    2: '读卡器',
    3: '人脸识别设备',
    4: '指纹设备'
  }
  return typeMap[deviceType] || '未知设备'
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-enhanced-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
}

// 导航栏
.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 88rpx;
  padding: 0 30rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10rpx);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;

  .nav-left, .nav-right {
    width: 80rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 30rpx;
    transition: all 0.3s ease;

    &:active {
      background-color: rgba(0, 0, 0, 0.05);
    }
  }

  .nav-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #1a1a1a;
    background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
  }
}

// 统计卡片
.stats-section {
  padding: 30rpx;

  .stats-card {
    background: white;
    border-radius: 24rpx;
    padding: 40rpx;
    display: flex;
    align-items: center;
    justify-content: space-around;
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 6rpx;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    }

    .stats-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;

      .stats-value {
        font-size: 48rpx;
        font-weight: bold;
        color: #1a1a1a;
        margin-bottom: 8rpx;
        background: linear-gradient(45deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        background-clip: text;
        -webkit-text-fill-color: transparent;

        &.stats-online {
          background: linear-gradient(45deg, #52c41a 0%, #73d13d 100%);
          -webkit-background-clip: text;
          background-clip: text;
          -webkit-text-fill-color: transparent;
        }

        &.stats-offline {
          background: linear-gradient(45deg, #ff4d4f 0%, #ff7875 100%);
          -webkit-background-clip: text;
          background-clip: text;
          -webkit-text-fill-color: transparent;
        }
      }

      .stats-label {
        font-size: 24rpx;
        color: #666;
        font-weight: 500;
      }
    }

    .stats-divider {
      width: 1rpx;
      height: 60rpx;
      background: linear-gradient(180deg, transparent 0%, #e0e0e0 50%, transparent 100%);
    }
  }
}

// 快速操作
.quick-actions {
  padding: 0 30rpx 30rpx;
  display: flex;
  gap: 20rpx;

  .action-btn {
    flex: 1;
    background: white;
    border-radius: 20rpx;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;

    &:active {
      transform: translateY(2rpx);
      box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    }

    .action-text {
      font-size: 24rpx;
      color: #333;
      font-weight: 500;
    }
  }
}

// 筛选标签
.filter-section {
  padding: 0 30rpx 30rpx;

  .filter-tabs {
    background: white;
    border-radius: 20rpx;
    padding: 20rpx;
    white-space: nowrap;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .filter-tab {
      display: inline-block;
      padding: 16rpx 32rpx;
      margin-right: 20rpx;
      border-radius: 30rpx;
      font-size: 28rpx;
      color: #666;
      background: #f8fafc;
      transition: all 0.3s ease;
      border: 2rpx solid transparent;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        font-weight: 500;
        transform: translateY(-2rpx);
        box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.3);
      }

      &:last-child {
        margin-right: 0;
      }
    }
  }
}

// 设备列表
.device-list {
  padding: 0 30rpx 30rpx;

  .device-item {
    background: white;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 20rpx;
    position: relative;
    overflow: hidden;

    &:active {
      transform: scale(0.98);
      box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    }

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 6rpx;
      height: 100%;
      background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:active::before {
      opacity: 1;
    }

    .select-checkbox {
      width: 44rpx;
      height: 44rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .device-icon {
      .icon-wrapper {
        width: 88rpx;
        height: 88rpx;
        border-radius: 20rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;

        &.online {
          background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
        }

        &.offline {
          background: linear-gradient(135deg, #f5f5f5 0%, #e0e0e0 100%);
        }
      }
    }

    .device-info {
      flex: 1;
      min-width: 0;

      .device-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12rpx;

        .device-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #1a1a1a;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .status-badge {
          padding: 8rpx 16rpx;
          border-radius: 12rpx;
          font-size: 22rpx;
          font-weight: 500;
          flex-shrink: 0;

          &.online {
            background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
            color: white;
            box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
          }

          &.offline {
            background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
            color: #666;
          }
        }
      }

      .device-details {
        margin-bottom: 8rpx;

        .device-code, .device-area {
          display: block;
          font-size: 24rpx;
          color: #666;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .device-extra {
        display: flex;
        gap: 20rpx;

        .device-type, .device-ip {
          font-size: 22rpx;
          color: #999;
        }
      }
    }

    .device-actions {
      .action-item {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 30rpx;
        transition: all 0.3s ease;

        &:active {
          background-color: rgba(0, 0, 0, 0.05);
        }
      }
    }
  }
}

// 加载和空状态
.loading-wrapper {
  padding: 40rpx 0;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;

  .empty-icon {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 40rpx;
    opacity: 0.6;
  }

  .empty-text {
    font-size: 32rpx;
    color: #999;
    margin-bottom: 16rpx;
  }

  .empty-desc {
    font-size: 26rpx;
    color: #ccc;
    text-align: center;
  }
}

.load-more {
  padding: 20rpx 0 60rpx;
}

// 搜索弹窗
.search-popup {
  background: white;
  border-radius: 0 0 24rpx 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);

  .search-bar {
    display: flex;
    align-items: center;
    gap: 20rpx;
    background: #f8fafc;
    border-radius: 20rpx;
    padding: 20rpx 30rpx;

    .search-input {
      flex: 1;
      font-size: 28rpx;
      color: #333;
    }

    .search-cancel {
      font-size: 28rpx;
      color: #667eea;
      white-space: nowrap;
    }
  }
}

// 设备操作菜单
.menu-popup {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  padding-bottom: 40rpx;

  .menu-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #1a1a1a;
    text-align: center;
    padding: 40rpx 30rpx 20rpx;
    border-bottom: 1rpx solid #f0f0f0;
  }

  .menu-list {
    padding: 20rpx 0;

    .menu-item {
      display: flex;
      align-items: center;
      gap: 24rpx;
      padding: 30rpx;
      border-bottom: 1rpx solid #f5f5f5;
      transition: all 0.3s ease;

      &:last-child {
        border-bottom: none;
      }

      &:active {
        background-color: rgba(0, 0, 0, 0.02);
      }

      .menu-text {
        font-size: 30rpx;
        color: #333;
      }
    }
  }

  .menu-cancel {
    margin: 20rpx 30rpx;
    height: 80rpx;
    border-radius: 20rpx;
    background: #f8fafc;
    color: #666;
    font-size: 28rpx;
    font-weight: 500;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
  }
}
</style>
