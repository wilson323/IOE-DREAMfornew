<template>
  <view class="area-monitor-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#ffffff"></uni-icons>
      </view>
      <view class="nav-title">区域监控</view>
      <view class="nav-right" @click="toggleFullscreen">
        <uni-icons type="scan" size="20" color="#ffffff"></uni-icons>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-container">
      <!-- 区域信息卡片 -->
      <view class="area-info-card">
        <view class="area-header">
          <view class="area-icon">
            <uni-icons type="location" size="20" color="#667eea"></uni-icons>
          </view>
          <view class="area-info">
            <view class="area-name">{{ areaInfo.areaName }}</view>
            <view class="area-code">{{ areaInfo.areaCode }}</view>
          </view>
          <view class="live-status">
            <view class="live-dot"></view>
            <text class="live-text">实时</text>
          </view>
        </view>
      </view>

      <!-- 实时统计卡片 -->
      <view class="stats-cards">
        <view class="stat-card">
          <view class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <uni-icons type="person" size="20" color="#ffffff"></uni-icons>
          </view>
          <view class="stat-info">
            <view class="stat-value">{{ areaInfo.onlineCount }}</view>
            <view class="stat-label">在线人数</view>
          </view>
        </view>

        <view class="stat-card">
          <view class="stat-icon" style="background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);">
            <uni-icons type="checkmarkempty" size="20" color="#ffffff"></uni-icons>
          </view>
          <view class="stat-info">
            <view class="stat-value">{{ areaInfo.todayPasses }}</view>
            <view class="stat-label">今日通行</view>
          </view>
        </view>

        <view class="stat-card">
          <view class="stat-icon" style="background: linear-gradient(135deg, #faad14 0%, #d48806 100%);">
            <uni-icons type="bell" size="20" color="#ffffff"></uni-icons>
          </view>
          <view class="stat-info">
            <view class="stat-value">{{ areaInfo.alertCount }}</view>
            <view class="stat-label">告警数</view>
          </view>
        </view>
      </view>

      <!-- 标签页切换 -->
      <view class="tab-container">
        <view
          v-for="(tab, index) in tabs"
          :key="index"
          class="tab-item"
          :class="{ active: activeTab === index }"
          @click="switchTab(index)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view v-if="tab.count > 0" class="tab-badge">{{ tab.count }}</view>
        </view>
      </view>

      <!-- 实时通行记录 -->
      <scroll-view
        class="tab-content"
        scroll-y
        v-if="activeTab === 0"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view v-if="passRecords.length === 0 && !loading" class="empty-state">
          <uni-icons type="norecorded" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无通行记录</text>
        </view>

        <view
          v-for="record in passRecords"
          :key="record.recordId"
          class="pass-record-card"
          @click="showRecordDetail(record)"
        >
          <!-- 记录头部 -->
          <view class="record-header">
            <view class="record-time">{{ formatTime(record.passTime) }}</view>
            <view class="record-status" :class="getStatusClass(record.passStatus)">
              {{ getStatusLabel(record.passStatus) }}
            </view>
          </view>

          <!-- 记录内容 -->
          <view class="record-content">
            <view class="person-info">
              <image
                class="person-avatar"
                :src="record.avatar || '/static/images/default-avatar.png'"
              ></image>
              <view class="person-details">
                <view class="person-name">{{ record.personName }}</view>
                <view class="person-code">{{ record.personCode }}</view>
              </view>
            </view>

            <view class="door-info">
              <uni-icons type="door" size="14" color="#999"></uni-icons>
              <text class="door-name">{{ record.doorName }}</text>
            </view>

            <view class="pass-direction" :class="record.direction">
              <uni-icons :type="record.direction === 'in' ? 'up' : 'down'" size="14" :color="record.direction === 'in' ? '#52c41a' : '#ff4d4f'"></uni-icons>
              <text class="direction-text">{{ record.direction === 'in' ? '进入' : '离开' }}</text>
            </view>
          </view>

          <!-- 记录底部 -->
          <view class="record-footer" v-if="record.imagePath">
            <image class="capture-image" :src="record.imagePath" mode="aspectFill"></image>
            <text class="capture-time">抓拍时间 {{ record.captureTime }}</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="passRecords.length > 0">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="noMore" class="no-more-text">没有更多了</text>
          <text v-else class="load-text" @click="loadMore">加载更多</text>
        </view>
      </scroll-view>

      <!-- 告警信息 -->
      <scroll-view
        class="tab-content"
        scroll-y
        v-if="activeTab === 1"
      >
        <view v-if="alertList.length === 0 && !loading" class="empty-state">
          <uni-icons type="bell" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无告警信息</text>
        </view>

        <view
          v-for="alert in alertList"
          :key="alert.alertId"
          class="alert-card"
          :class="getAlertClass(alert.level)"
          @click="showAlertDetail(alert)"
        >
          <view class="alert-header">
            <view class="alert-icon">
              <uni-icons :type="getAlertIcon(alert.level)" size="18" color="#ffffff"></uni-icons>
            </view>
            <view class="alert-info">
              <view class="alert-title">{{ alert.title }}</view>
              <view class="alert-time">{{ formatTime(alert.alertTime) }}</view>
            </view>
            <view class="alert-level">{{ getLevelLabel(alert.level) }}</view>
          </view>

          <view class="alert-content">
            <text class="alert-message">{{ alert.message }}</text>
          </view>

          <view class="alert-footer" v-if="alert.imagePath">
            <image class="alert-image" :src="alert.imagePath" mode="aspectFill"></image>
            <view class="alert-actions">
              <button class="action-btn handle" @click.stop="handleAlert(alert)">处理</button>
              <button class="action-btn ignore" @click.stop="ignoreAlert(alert)">忽略</button>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="alertList.length > 0">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="noMore" class="no-more-text">没有更多了</text>
          <text v-else class="load-text" @click="loadMore">加载更多</text>
        </view>
      </scroll-view>

      <!-- 在线人员 -->
      <scroll-view
        class="tab-content"
        scroll-y
        v-if="activeTab === 2"
      >
        <view v-if="onlinePersonnel.length === 0 && !loading" class="empty-state">
          <uni-icons type="person" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无在线人员</text>
        </view>

        <view class="personnel-grid">
          <view
            v-for="person in onlinePersonnel"
            :key="person.personId"
            class="personnel-grid-item"
            @click="showPersonnelDetail(person)"
          >
            <view class="grid-avatar-wrapper">
              <image
                class="grid-avatar"
                :src="person.avatar || '/static/images/default-avatar.png'"
              ></image>
              <view class="online-indicator"></view>
            </view>
            <view class="grid-info">
              <view class="grid-name">{{ person.personName }}</view>
              <view class="grid-meta">{{ person.personCode }}</view>
              <view class="grid-time">{{ formatDuration(person.onlineDuration) }}</view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="onlinePersonnel.length > 0">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="noMore" class="no-more-text">没有更多了</text>
          <text v-else class="load-text" @click="loadMore">加载更多</text>
        </view>
      </scroll-view>

      <!-- 设备状态 -->
      <scroll-view
        class="tab-content"
        scroll-y
        v-if="activeTab === 3"
      >
        <view v-if="deviceList.length === 0 && !loading" class="empty-state">
          <uni-icons type="door" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无设备数据</text>
        </view>

        <view
          v-for="device in deviceList"
          :key="device.deviceId"
          class="device-card"
          @click="showDeviceDetail(device)"
        >
          <view class="device-header">
            <view class="device-info">
              <view class="device-icon">
                <uni-icons type="door" size="18" color="#667eea"></uni-icons>
              </view>
              <view class="device-details">
                <view class="device-name">{{ device.deviceName }}</view>
                <view class="device-code">{{ device.deviceCode }}</view>
              </view>
            </view>
            <view class="device-status" :class="{ online: device.online }">
              {{ device.online ? '在线' : '离线' }}
            </view>
          </view>

          <view class="device-stats">
            <view class="stat-row">
              <text class="stat-label">今日通行</text>
              <text class="stat-value">{{ device.todayPasses || 0 }}</text>
            </view>
            <view class="stat-row">
              <text class="stat-label">最后通行</text>
              <text class="stat-value">{{ device.lastPassTime || '-' }}</text>
            </view>
            <view class="stat-row">
              <text class="stat-label">电池电量</text>
              <text class="stat-value" :class="{ low: device.battery < 20 }">
                {{ device.battery }}%
              </text>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="deviceList.length > 0">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="noMore" class="no-more-text">没有更多了</text>
          <text v-else class="load-text" @click="loadMore">加载更多</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// 状态栏高度
const statusBarHeight = ref(0)
const areaId = ref('')

// 区域信息
const areaInfo = reactive({
  areaName: 'A栋办公楼',
  areaCode: 'AREA-A-001',
  onlineCount: 42,
  todayPasses: 568,
  alertCount: 3
})

// 标签页
const tabs = ref([
  { label: '通行记录', count: 0 },
  { label: '告警信息', count: 3 },
  { label: '在线人员', count: 42 },
  { label: '设备状态', count: 12 }
])
const activeTab = ref(0)

// 数据列表
const passRecords = ref([])
const alertList = ref([])
const onlinePersonnel = ref([])
const deviceList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNo = ref(1)
const pageSize = 20
const noMore = ref(false)

// 定时器
let refreshTimer = null

// 页面加载
onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  if (options.areaId) {
    areaId.value = options.areaId
  }

  loadData()
  startAutoRefresh()
})

// 页面卸载
onUnmounted(() => {
  stopAutoRefresh()
})

/**
 * 开始自动刷新
 */
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadData(true)
  }, 30000) // 30秒刷新一次
}

/**
 * 停止自动刷新
 */
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

/**
 * 加载数据
 */
const loadData = async (isRefresh = false) => {
  switch (activeTab.value) {
    case 0:
      await loadPassRecords(isRefresh)
      break
    case 1:
      await loadAlertList(isRefresh)
      break
    case 2:
      await loadOnlinePersonnel(isRefresh)
      break
    case 3:
      await loadDeviceList(isRefresh)
      break
  }

  // 更新标签页计数
  await updateTabCounts()
}

/**
 * 加载通行记录
 */
const loadPassRecords = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    pageNo.value = 1
    noMore.value = false
  }

  loading.value = true

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getPassRecords({
    //   areaId: areaId.value,
    //   pageNo: pageNo.value,
    //   pageSize: pageSize
    // })

    // 模拟数据
    const mockData = Array.from({ length: 10 }, (_, i) => ({
      recordId: i + 1,
      personName: `张${i + 1}`,
      personCode: `EMP00${i + 1}`,
      avatar: `/static/images/avatar-${(i % 3) + 1}.png`,
      doorName: `门禁${String.fromCharCode(65 + (i % 8))}`,
      passTime: new Date(Date.now() - i * 60000),
      passStatus: ['success', 'denied', 'alarm'][i % 3],
      direction: i % 2 === 0 ? 'in' : 'out',
      imagePath: i % 3 === 0 ? '/static/images/capture-1.jpg' : '',
      captureTime: i % 3 === 0 ? new Date(Date.now() - i * 60000) : null
    }))

    if (isRefresh) {
      passRecords.value = mockData
    } else {
      passRecords.value.push(...mockData)
    }

    if (pageNo.value >= 3) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载通行记录失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 加载告警列表
 */
const loadAlertList = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    pageNo.value = 1
    noMore.value = false
  }

  loading.value = true

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getAlertList({
    //   areaId: areaId.value,
    //   pageNo: pageNo.value,
    //   pageSize: pageSize
    // })

    // 模拟数据
    const mockData = Array.from({ length: 5 }, (_, i) => ({
      alertId: i + 1,
      title: ['非法闯入', '长时间逗留', '设备离线', '暴力破坏', '尾随检测'][i % 5],
      message: `检测到异常行为，请及时处理`,
      alertTime: new Date(Date.now() - i * 300000),
      level: ['high', 'medium', 'low'][i % 3],
      imagePath: i % 2 === 0 ? '/static/images/alert-1.jpg' : '',
      status: 'pending'
    }))

    if (isRefresh) {
      alertList.value = mockData
    } else {
      alertList.value.push(...mockData)
    }

    if (pageNo.value >= 2) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载告警列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 加载在线人员
 */
const loadOnlinePersonnel = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    pageNo.value = 1
    noMore.value = false
  }

  loading.value = true

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getOnlinePersonnel({
    //   areaId: areaId.value,
    //   pageNo: pageNo.value,
    //   pageSize: pageSize
    // })

    // 模拟数据
    const mockData = Array.from({ length: 12 }, (_, i) => ({
      personId: i + 1,
      personName: `张${i + 1}`,
      personCode: `EMP00${i + 1}`,
      avatar: `/static/images/avatar-${(i % 3) + 1}.png`,
      onlineDuration: Math.floor(Math.random() * 3600000) // 毫秒
    }))

    if (isRefresh) {
      onlinePersonnel.value = mockData
    } else {
      onlinePersonnel.value.push(...mockData)
    }

    if (pageNo.value >= 2) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载在线人员失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 加载设备列表
 */
const loadDeviceList = async (isRefresh = false) => {
  if (loading.value) return

  if (isRefresh) {
    pageNo.value = 1
    noMore.value = false
  }

  loading.value = true

  try {
    // TODO: 调用实际的API
    // const res = await areaApi.getDeviceList({
    //   areaId: areaId.value,
    //   pageNo: pageNo.value,
    //   pageSize: pageSize
    // })

    // 模拟数据
    const mockData = Array.from({ length: 8 }, (_, i) => ({
      deviceId: i + 1,
      deviceName: `门禁${String.fromCharCode(65 + i)}`,
      deviceCode: `DOOR-${String(i + 1).padStart(3, '0')}`,
      online: Math.random() > 0.2,
      todayPasses: Math.floor(Math.random() * 100),
      lastPassTime: new Date(Date.now() - Math.floor(Math.random() * 3600000)),
      battery: Math.floor(Math.random() * 100)
    }))

    if (isRefresh) {
      deviceList.value = mockData
    } else {
      deviceList.value.push(...mockData)
    }

    if (pageNo.value >= 1) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 更新标签页计数
 */
const updateTabCounts = async () => {
  try {
    // TODO: 调用实际的API获取统计数字
    // const res = await areaApi.getMonitorStats(areaId.value)

    // 模拟数据
    tabs.value[1].count = 3 // 告警数
    tabs.value[2].count = 42 // 在线人员
    tabs.value[3].count = 12 // 设备数
  } catch (error) {
    console.error('更新统计失败:', error)
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadData(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    pageNo.value++
    loadData()
  }
}

/**
 * 切换标签页
 */
const switchTab = (index) => {
  activeTab.value = index
  pageNo.value = 1
  noMore.value = false
  loadData()
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  }
}

/**
 * 格式化在线时长
 */
const formatDuration = (duration) => {
  const hours = Math.floor(duration / 3600000)
  const minutes = Math.floor((duration % 3600000) / 60000)

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    success: 'status-success',
    denied: 'status-denied',
    alarm: 'status-alarm'
  }
  return classMap[status] || ''
}

/**
 * 获取状态标签
 */
const getStatusLabel = (status) => {
  const labelMap = {
    success: '通行成功',
    denied: '拒绝通行',
    alarm: '异常告警'
  }
  return labelMap[status] || status
}

/**
 * 获取告警样式类
 */
const getAlertClass = (level) => {
  const classMap = {
    high: 'alert-high',
    medium: 'alert-medium',
    low: 'alert-low'
  }
  return classMap[level] || ''
}

/**
 * 获取告警图标
 */
const getAlertIcon = (level) => {
  const iconMap = {
    high: 'closeempty',
    medium: 'bell',
    low: 'info'
  }
  return iconMap[level] || 'info'
}

/**
 * 获取等级标签
 */
const getLevelLabel = (level) => {
  const labelMap = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return labelMap[level] || level
}

/**
 * 显示记录详情
 */
const showRecordDetail = (record) => {
  uni.navigateTo({
    url: `/pages/access/record-detail?recordId=${record.recordId}`
  })
}

/**
 * 显示告警详情
 */
const showAlertDetail = (alert) => {
  uni.navigateTo({
    url: `/pages/access/alert-detail?alertId=${alert.alertId}`
  })
}

/**
 * 处理告警
 */
const handleAlert = (alert) => {
  uni.navigateTo({
    url: `/pages/access/alert-handle?alertId=${alert.alertId}`
  })
}

/**
 * 忽略告警
 */
const ignoreAlert = (alert) => {
  uni.showModal({
    title: '确认忽略',
    content: '确定要忽略此告警吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // TODO: 调用实际的API
          uni.showToast({
            title: '已忽略',
            icon: 'success'
          })
          loadData(true)
        } catch (error) {
          uni.showToast({
            title: '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 显示人员详情
 */
const showPersonnelDetail = (person) => {
  uni.navigateTo({
    url: `/pages/personnel/detail?personId=${person.personId}`
  })
}

/**
 * 显示设备详情
 */
const showDeviceDetail = (device) => {
  uni.navigateTo({
    url: `/pages/access/door-detail?deviceId=${device.deviceId}`
  })
}

/**
 * 切换全屏
 */
const toggleFullscreen = () => {
  uni.showToast({
    title: '全屏模式功能开发中',
    icon: 'none'
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.area-monitor-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

// 自定义导航栏
.custom-navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30rpx;
  height: 44px;

  .nav-left,
  .nav-right {
    width: 40px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #ffffff;
    flex: 1;
    text-align: center;
  }
}

// 内容区域
.content-container {
  padding: 30rpx;
}

// 区域信息卡片
.area-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);

  .area-header {
    display: flex;
    align-items: center;

    .area-icon {
      width: 64rpx;
      height: 64rpx;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
    }

    .area-info {
      flex: 1;

      .area-name {
        font-size: 18px;
        font-weight: 600;
        color: #ffffff;
        margin-bottom: 8rpx;
      }

      .area-code {
        font-size: 13px;
        color: rgba(255, 255, 255, 0.8);
      }
    }

    .live-status {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 8rpx 16rpx;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: 20rpx;

      .live-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;
        background-color: #52c41a;
        animation: pulse 2s infinite;
      }

      .live-text {
        font-size: 12px;
        color: #ffffff;
      }

      @keyframes pulse {
        0%, 100% {
          opacity: 1;
        }
        50% {
          opacity: 0.5;
        }
      }
    }
  }
}

// 统计卡片
.stats-cards {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;

  .stat-card {
    flex: 1;
    background-color: #ffffff;
    border-radius: 16rpx;
    padding: 24rpx;
    display: flex;
    align-items: center;
    gap: 16rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);

    .stat-icon {
      width: 64rpx;
      height: 64rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-info {
      flex: 1;

      .stat-value {
        font-size: 20px;
        font-weight: 600;
        color: #333;
        margin-bottom: 6rpx;
      }

      .stat-label {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

// 标签页
.tab-container {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 12rpx;

  .tab-item {
    flex: 1;
    height: 64rpx;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    transition: all 0.3s;

    .tab-text {
      font-size: 14px;
      color: #666;
    }

    .tab-badge {
      position: absolute;
      top: 8rpx;
      right: 8rpx;
      min-width: 32rpx;
      height: 32rpx;
      padding: 0 8rpx;
      background-color: #ff4d4f;
      border-radius: 16rpx;
      font-size: 11px;
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      .tab-text {
        color: #ffffff;
        font-weight: 500;
      }

      .tab-badge {
        background-color: rgba(255, 255, 255, 0.9);
        color: #ff4d4f;
      }
    }
  }
}

// 标签页内容
.tab-content {
  height: calc(100vh - 560rpx);
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 20rpx;

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 150rpx;

    .empty-text {
      font-size: 14px;
      color: #999;
      margin-top: 30rpx;
    }
  }

  .load-more {
    padding: 30rpx 0;
    text-align: center;

    .loading-text,
    .load-text,
    .no-more-text {
      font-size: 13px;
      color: #999;
    }
  }
}

// 通行记录卡片
.pass-record-card {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .record-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16rpx;

    .record-time {
      font-size: 13px;
      color: #999;
    }

    .record-status {
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
      font-size: 11px;
      font-weight: 500;

      &.status-success {
        background-color: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }

      &.status-denied {
        background-color: rgba(255, 77, 79, 0.1);
        color: #ff4d4f;
      }

      &.status-alarm {
        background-color: rgba(250, 173, 20, 0.1);
        color: #faad14;
      }
    }
  }

  .record-content {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;

    .person-info {
      display: flex;
      align-items: center;
      gap: 16rpx;
      flex: 1;

      .person-avatar {
        width: 64rpx;
        height: 64rpx;
        border-radius: 50%;
        background-color: #eaeaea;
      }

      .person-details {
        .person-name {
          font-size: 15px;
          font-weight: 500;
          color: #333;
          margin-bottom: 6rpx;
        }

        .person-code {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .door-info {
      display: flex;
      align-items: center;
      gap: 8rpx;
      margin: 0 20rpx;

      .door-name {
        font-size: 13px;
        color: #666;
      }
    }

    .pass-direction {
      display: flex;
      align-items: center;
      gap: 6rpx;
      padding: 8rpx 16rpx;
      border-radius: 8rpx;

      &.in {
        background-color: rgba(82, 196, 26, 0.1);
      }

      &.out {
        background-color: rgba(255, 77, 79, 0.1);
      }

      .direction-text {
        font-size: 12px;
        font-weight: 500;
      }
    }
  }

  .record-footer {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .capture-image {
      width: 80rpx;
      height: 80rpx;
      border-radius: 8rpx;
      background-color: #eaeaea;
    }

    .capture-time {
      flex: 1;
      font-size: 12px;
      color: #999;
    }
  }
}

// 告警卡片
.alert-card {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  border-left: 4rpx solid transparent;

  &:last-child {
    margin-bottom: 0;
  }

  &.alert-high {
    border-left-color: #ff4d4f;
  }

  &.alert-medium {
    border-left-color: #faad14;
  }

  &.alert-low {
    border-left-color: #667eea;
  }

  .alert-header {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 16rpx;

    .alert-icon {
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .alert-high & .alert-icon {
      background-color: #ff4d4f;
    }

    .alert-medium & .alert-icon {
      background-color: #faad14;
    }

    .alert-low & .alert-icon {
      background-color: #667eea;
    }

    .alert-info {
      flex: 1;

      .alert-title {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6rpx;
      }

      .alert-time {
        font-size: 12px;
        color: #999;
      }
    }

    .alert-level {
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
      font-size: 11px;
      font-weight: 500;

      .alert-high & {
        background-color: rgba(255, 77, 79, 0.1);
        color: #ff4d4f;
      }

      .alert-medium & {
        background-color: rgba(250, 173, 20, 0.1);
        color: #faad14;
      }

      .alert-low & {
        background-color: rgba(102, 126, 234, 0.1);
        color: #667eea;
      }
    }
  }

  .alert-content {
    margin-bottom: 16rpx;

    .alert-message {
      font-size: 14px;
      color: #666;
      line-height: 1.5;
    }
  }

  .alert-footer {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .alert-image {
      width: 80rpx;
      height: 80rpx;
      border-radius: 8rpx;
      background-color: #eaeaea;
    }

    .alert-actions {
      flex: 1;
      display: flex;
      gap: 12rpx;

      .action-btn {
        flex: 1;
        height: 56rpx;
        border-radius: 8rpx;
        font-size: 13px;
        font-weight: 500;
        border: none;

        &.handle {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #ffffff;
        }

        &.ignore {
          background-color: #ffffff;
          color: #666;
          border: 1px solid #d9d9d9;
        }
      }
    }
  }
}

// 在线人员网格
.personnel-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;

  .personnel-grid-item {
    background-color: #f5f5f5;
    border-radius: 12rpx;
    padding: 20rpx;
    text-align: center;

    .grid-avatar-wrapper {
      position: relative;
      display: inline-block;
      margin-bottom: 16rpx;

      .grid-avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        background-color: #eaeaea;
      }

      .online-indicator {
        position: absolute;
        bottom: 4rpx;
        right: 4rpx;
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;
        border: 2px solid #ffffff;
        background-color: #52c41a;
      }
    }

    .grid-info {
      .grid-name {
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .grid-meta {
        font-size: 11px;
        color: #999;
        margin-bottom: 6rpx;
      }

      .grid-time {
        font-size: 11px;
        color: #667eea;
      }
    }
  }
}

// 设备卡片
.device-card {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .device-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .device-info {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .device-icon {
        width: 48rpx;
        height: 48rpx;
        border-radius: 12rpx;
        background-color: rgba(102, 126, 234, 0.1);
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .device-details {
        .device-name {
          font-size: 15px;
          font-weight: 500;
          color: #333;
          margin-bottom: 6rpx;
        }

        .device-code {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .device-status {
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
      font-size: 11px;
      font-weight: 500;
      background-color: #f5f5f5;
      color: #999;

      &.online {
        background-color: rgba(82, 196, 26, 0.1);
        color: #52c41a;
      }
    }
  }

  .device-stats {
    .stat-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12rpx 0;
      border-bottom: 1px solid #eaeaea;

      &:last-child {
        border-bottom: none;
      }

      .stat-label {
        font-size: 13px;
        color: #666;
      }

      .stat-value {
        font-size: 13px;
        color: #333;
        font-weight: 500;

        &.low {
          color: #ff4d4f;
        }
      }
    }
  }
}
</style>
