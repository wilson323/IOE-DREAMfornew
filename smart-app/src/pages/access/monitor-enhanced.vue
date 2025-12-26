<template>
  <view class="monitor-enhanced-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#ffffff"></uni-icons>
      </view>
      <view class="nav-title">实时监控</view>
      <view class="nav-right" @click="showMoreMenu">
        <uni-icons type="more" size="20" color="#ffffff"></uni-icons>
      </view>
    </view>

    <!-- 刷新状态栏 -->
    <view class="refresh-status">
      <view class="status-left">
        <view class="live-dot"></view>
        <text class="live-text">实时监控中</text>
      </view>
      <view class="status-right">
        <text class="refresh-time">{{ lastRefreshTime }}</text>
        <uni-icons type="refreshempty" size="14" color="#667eea" @click="manualRefresh"></uni-icons>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content-container" scroll-y>
      <!-- 全局统计卡片 -->
      <view class="stats-cards">
        <view class="stat-card primary">
          <view class="stat-icon">
            <uni-icons type="door" size="20" color="#ffffff"></uni-icons>
          </view>
          <view class="stat-info">
            <view class="stat-value">{{ globalStats.totalDevices }}</view>
            <view class="stat-label">设备总数</view>
          </view>
          <view class="stat-trend" :class="{ up: globalStats.deviceTrend > 0 }">
            <uni-icons :type="globalStats.deviceTrend > 0 ? 'up' : 'down'" size="12" color="#52c41a"></uni-icons>
            <text class="trend-text">{{ Math.abs(globalStats.deviceTrend) }}%</text>
          </view>
        </view>

        <view class="stat-card success">
          <view class="stat-icon">
            <uni-icons type="checkmarkempty" size="20" color="#ffffff"></uni-icons>
          </view>
          <view class="stat-info">
            <view class="stat-value">{{ globalStats.onlineDevices }}</view>
            <view class="stat-label">在线设备</view>
          </view>
          <view class="stat-rate">
            <text class="rate-text">{{ onlineRate }}%</text>
          </view>
        </view>

        <view class="stat-card warning">
          <view class="stat-icon">
            <uni-icons type="person" size="20" color="#ffffff"></uni-icons>
          </view>
          <view class="stat-info">
            <view class="stat-value">{{ globalStats.todayPasses }}</view>
            <view class="stat-label">今日通行</view>
          </view>
          <view class="stat-trend up">
            <uni-icons type="up" size="12" color="#52c41a"></uni-icons>
            <text class="trend-text">+{{ globalStats.passTrend }}%</text>
          </view>
        </view>

        <view class="stat-card danger">
          <view class="stat-icon">
            <uni-icons type="bell" size="20" color="#ffffff"></uni-icons>
          </view>
          <view class="stat-info">
            <view class="stat-value">{{ globalStats.alertCount }}</view>
            <view class="stat-label">告警数</view>
          </view>
          <view class="stat-action" @click="goToAlarmList">
            <text class="action-text">处理</text>
          </view>
        </view>
      </view>

      <!-- 区域筛选标签 -->
      <view class="filter-tags">
        <view
          v-for="(tag, index) in areaTags"
          :key="index"
          class="filter-tag"
          :class="{ active: selectedAreaIndex === index }"
          @click="selectArea(index)"
        >
          {{ tag.label }}
          <view v-if="tag.count > 0" class="tag-count">{{ tag.count }}</view>
        </view>
      </view>

      <!-- 设备状态网格 -->
      <view class="section-container">
        <view class="section-header">
          <view class="section-title">设备状态</view>
          <view class="section-actions">
            <text class="action-text" @click="goToDeviceMonitor">全部</text>
          </view>
        </view>

        <scroll-view class="device-grid-scroll" scroll-x>
          <view class="device-grid">
            <view
              v-for="device in deviceList"
              :key="device.deviceId"
              class="device-grid-item"
              :class="{ offline: !device.online, alert: device.hasAlert }"
              @click="showDeviceDetail(device)"
            >
              <view class="device-icon">
                <uni-icons type="door" size="20" :color="device.online ? '#52c41a' : '#d9d9d9'"></uni-icons>
              </view>
              <view class="device-info">
                <view class="device-name">{{ device.deviceName }}</view>
                <view class="device-location">{{ device.location }}</view>
              </view>
              <view class="device-status">
                <view class="status-dot" :class="{ online: device.online }"></view>
                <text class="status-text">{{ device.online ? '在线' : '离线' }}</text>
              </view>
              <view class="device-passes" v-if="device.online">
                <text class="passes-count">{{ device.todayPasses || 0 }}</text>
                <text class="passes-label">次</text>
              </view>
              <view class="alert-badge" v-if="device.hasAlert">
                <uni-icons type="bell" size="12" color="#ff4d4f"></uni-icons>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 实时通行记录 -->
      <view class="section-container">
        <view class="section-header">
          <view class="section-title">实时通行</view>
          <view class="section-actions">
            <text class="action-text" @click="goToRecordList">更多</text>
          </view>
        </view>

        <view class="pass-record-list">
          <view v-if="passRecords.length === 0" class="empty-state">
            <uni-icons type="norecorded" size="60" color="#d9d9d9"></uni-icons>
            <text class="empty-text">暂无通行记录</text>
          </view>

          <view
            v-for="record in passRecords"
            :key="record.recordId"
            class="pass-record-item"
            @click="showRecordDetail(record)"
          >
            <view class="record-time">
              <text class="time-text">{{ formatTime(record.passTime) }}</text>
            </view>

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

              <view class="pass-info">
                <uni-icons type="door" size="14" color="#667eea"></uni-icons>
                <text class="door-name">{{ record.doorName }}</text>
              </view>

              <view class="pass-direction" :class="record.direction">
                <uni-icons :type="record.direction === 'in' ? 'up' : 'down'" size="14" :color="record.direction === 'in' ? '#52c41a' : '#ff4d4f'"></uni-icons>
              </view>
            </view>

            <view class="record-status" :class="getStatusClass(record.status)">
              {{ getStatusLabel(record.status) }}
            </view>
          </view>
        </view>
      </view>

      <!-- 告警信息 -->
      <view class="section-container" v-if="alertList.length > 0">
        <view class="section-header">
          <view class="section-title">告警信息</view>
          <view class="section-actions">
            <text class="action-text" @click="goToAlarmList">全部({{ alertList.length }})</text>
          </view>
        </view>

        <view class="alert-list">
          <view
            v-for="alert in alertList"
            :key="alert.alertId"
            class="alert-item"
            :class="getAlertClass(alert.level)"
            @click="showAlertDetail(alert)"
          >
            <view class="alert-icon">
              <uni-icons :type="getAlertIcon(alert.level)" size="16" color="#ffffff"></uni-icons>
            </view>

            <view class="alert-content">
              <view class="alert-title">{{ alert.title }}</view>
              <view class="alert-message">{{ alert.message }}</view>
              <view class="alert-time">{{ formatTime(alert.alertTime) }}</view>
            </view>

            <view class="alert-actions">
              <button class="handle-btn" @click.stop="handleAlert(alert)">处理</button>
            </view>
          </view>
        </view>
      </view>

      <!-- 在线人员 -->
      <view class="section-container">
        <view class="section-header">
          <view class="section-title">在线人员</view>
          <view class="section-actions">
            <text class="action-text" @click="goToOnlineList">全部({{ onlineCount }})</text>
          </view>
        </view>

        <scroll-view class="personnel-scroll" scroll-x>
          <view class="personnel-scroll-list">
            <view
              v-for="person in onlinePersonnel"
              :key="person.personId"
              class="personnel-scroll-item"
              @click="showPersonnelDetail(person)"
            >
              <view class="personnel-avatar-wrapper">
                <image
                  class="personnel-avatar"
                  :src="person.avatar || '/static/images/default-avatar.png'"
                ></image>
                <view class="online-dot"></view>
              </view>
              <view class="personnel-info">
                <view class="personnel-name">{{ person.personName }}</view>
                <view class="personnel-code">{{ person.personCode }}</view>
                <view class="personnel-area">{{ person.areaName }}</view>
              </view>
              <view class="personnel-duration">
                <text class="duration-text">{{ formatDuration(person.onlineDuration) }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 底部留白 -->
      <view class="bottom-spacer"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// 状态栏高度
const statusBarHeight = ref(0)

// 全局统计
const globalStats = reactive({
  totalDevices: 48,
  onlineDevices: 45,
  todayPasses: 1256,
  alertCount: 3,
  deviceTrend: 2.5,
  passTrend: 12.3
})

// 在线率计算
const onlineRate = computed(() => {
  return globalStats.totalDevices > 0
    ? Math.round((globalStats.onlineDevices / globalStats.totalDevices) * 100)
    : 0
})

// 最后刷新时间
const lastRefreshTime = ref('')

// 区域筛选标签
const areaTags = ref([
  { label: '全部', value: '', count: 0 },
  { label: 'A栋', value: 'building-a', count: 12 },
  { label: 'B栋', value: 'building-b', count: 18 },
  { label: 'C栋', value: 'building-c', count: 15 }
])
const selectedAreaIndex = ref(0)

// 设备列表
const deviceList = ref([])
const passRecords = ref([])
const alertList = ref([])
const onlinePersonnel = ref([])

// 在线人数
const onlineCount = ref(0)

// 定时器
let refreshTimer = null

// 页面加载
onLoad((options) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

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
  }, 3000) // 3秒刷新一次
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
  await loadGlobalStats()
  await loadDeviceList()
  await loadPassRecords()
  await loadAlertList()
  await loadOnlinePersonnel()

  // 更新刷新时间
  const now = new Date()
  lastRefreshTime.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
}

/**
 * 加载全局统计
 */
const loadGlobalStats = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await monitorApi.getGlobalStats()

    // 模拟数据更新
    if (Math.random() > 0.7) {
      globalStats.todayPasses += Math.floor(Math.random() * 5)
    }
  } catch (error) {
    console.error('加载全局统计失败:', error)
  }
}

/**
 * 加载设备列表
 */
const loadDeviceList = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await monitorApi.getDeviceList()

    // 模拟数据
    if (deviceList.value.length === 0) {
      deviceList.value = Array.from({ length: 12 }, (_, i) => ({
        deviceId: i + 1,
        deviceName: `门禁${String.fromCharCode(65 + i)}-${Math.floor(i / 4) + 1}楼`,
        location: `${['A栋', 'B栋', 'C栋'][i % 3]}${Math.floor(i / 4) + 1}楼`,
        online: Math.random() > 0.1,
        todayPasses: Math.floor(Math.random() * 100),
        hasAlert: Math.random() > 0.9
      }))
    } else {
      // 更新在线状态和通行次数
      deviceList.value.forEach(device => {
        if (Math.random() > 0.8) {
          device.todayPasses += Math.floor(Math.random() * 3)
        }
        device.hasAlert = Math.random() > 0.95
      })
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
  }
}

/**
 * 加载通行记录
 */
const loadPassRecords = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await monitorApi.getPassRecords({ limit: 5 })

    // 模拟数据 - 只保留最新的5条
    const newRecords = Array.from({ length: 3 }, (_, i) => ({
      recordId: Date.now() - i,
      personName: ['张三', '李四', '王五'][i % 3],
      personCode: `EMP00${i + 1}`,
      avatar: `/static/images/avatar-${(i % 3) + 1}.png`,
      doorName: `门禁${String.fromCharCode(65 + (i % 8))}`,
      passTime: new Date(Date.now() - i * 30000),
      status: 'success',
      direction: i % 2 === 0 ? 'in' : 'out'
    }))

    passRecords.value = [...newRecords, ...passRecords.value].slice(0, 5)
  } catch (error) {
    console.error('加载通行记录失败:', error)
  }
}

/**
 * 加载告警列表
 */
const loadAlertList = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await monitorApi.getAlertList({ limit: 3 })

    // 模拟数据
    if (alertList.value.length === 0) {
      alertList.value = [
        {
          alertId: 1,
          title: '非法闯入',
          message: 'A栋1楼东门检测到非法闯入',
          alertTime: new Date(Date.now() - 60000),
          level: 'high',
          status: 'pending'
        },
        {
          alertId: 2,
          title: '设备离线',
          message: 'B栋2楼南门设备离线',
          alertTime: new Date(Date.now() - 120000),
          level: 'medium',
          status: 'pending'
        },
        {
          alertId: 3,
          title: '长时间逗留',
          message: 'C栋3楼会议室有人长时间逗留',
          alertTime: new Date(Date.now() - 180000),
          level: 'low',
          status: 'pending'
        }
      ]
    }
  } catch (error) {
    console.error('加载告警列表失败:', error)
  }
}

/**
 * 加载在线人员
 */
const loadOnlinePersonnel = async () => {
  try {
    // TODO: 调用实际的API
    // const res = await monitorApi.getOnlinePersonnel({ limit: 10 })

    // 模拟数据
    if (onlinePersonnel.value.length === 0) {
      onlinePersonnel.value = Array.from({ length: 8 }, (_, i) => ({
        personId: i + 1,
        personName: `员工${i + 1}`,
        personCode: `EMP00${i + 1}`,
        avatar: `/static/images/avatar-${(i % 3) + 1}.png`,
        areaName: ['A栋', 'B栋', 'C栋'][i % 3],
        onlineDuration: Math.floor(Math.random() * 7200000) // 毫秒
      }))
    }
    onlineCount.value = onlinePersonnel.value.length
  } catch (error) {
    console.error('加载在线人员失败:', error)
  }
}

/**
 * 选择区域
 */
const selectArea = (index) => {
  selectedAreaIndex.value = index
  loadData()
}

/**
 * 手动刷新
 */
const manualRefresh = () => {
  loadData()
  uni.showToast({
    title: '刷新成功',
    icon: 'success',
    duration: 1000
  })
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
  } else {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
}

/**
 * 格式化在线时长
 */
const formatDuration = (duration) => {
  const hours = Math.floor(duration / 3600000)
  const minutes = Math.floor((duration % 3600000) / 60000)

  if (hours > 0) {
    return `${hours}小时${minutes}分`
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
    success: '成功',
    denied: '拒绝',
    alarm: '异常'
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
 * 显示设备详情
 */
const showDeviceDetail = (device) => {
  uni.navigateTo({
    url: `/pages/access/door-detail?deviceId=${device.deviceId}`
  })
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
 * 显示人员详情
 */
const showPersonnelDetail = (person) => {
  uni.navigateTo({
    url: `/pages/personnel/detail?personId=${person.personId}`
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
 * 跳转到设备监控
 */
const goToDeviceMonitor = () => {
  uni.navigateTo({
    url: '/pages/access/monitor-device'
  })
}

/**
 * 跳转到记录列表
 */
const goToRecordList = () => {
  uni.navigateTo({
    url: '/pages/access/record-enhanced'
  })
}

/**
 * 跳转到告警列表
 */
const goToAlarmList = () => {
  uni.navigateTo({
    url: '/pages/access/monitor-alarm'
  })
}

/**
 * 跳转到在线人员列表
 */
const goToOnlineList = () => {
  uni.navigateTo({
    url: '/pages/access/monitor-tracking'
  })
}

/**
 * 显示更多菜单
 */
const showMoreMenu = () => {
  uni.showActionSheet({
    itemList: ['刷新数据', '设备监控', '通行记录', '告警处理', '人员追踪', '监控统计'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          manualRefresh()
          break
        case 1:
          goToDeviceMonitor()
          break
        case 2:
          goToRecordList()
          break
        case 3:
          goToAlarmList()
          break
        case 4:
          goToOnlineList()
          break
        case 5:
          uni.navigateTo({ url: '/pages/access/monitor-statistics' })
          break
      }
    }
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
.monitor-enhanced-page {
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

// 刷新状态栏
.refresh-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 30rpx;
  background-color: #ffffff;
  border-bottom: 1px solid #f0f0f0;

  .status-left {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .live-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
      background-color: #52c41a;
      animation: pulse 2s infinite;
    }

    .live-text {
      font-size: 13px;
      color: #333;
      font-weight: 500;
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

  .status-right {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .refresh-time {
      font-size: 12px;
      color: #999;
    }
  }
}

// 内容区域
.content-container {
  padding: 30rpx;
}

// 统计卡片
.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  margin-bottom: 30rpx;

  .stat-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 20rpx;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    position: relative;
    box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);

  &.success {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    box-shadow: 0 4rpx 20rpx rgba(82, 196, 26, 0.3);
  }

  &.warning {
    background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
    box-shadow: 0 4rpx 20rpx rgba(250, 173, 20, 0.3);
  }

  &.danger {
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
    box-shadow: 0 4rpx 20rpx rgba(255, 77, 79, 0.3);
  }

  .stat-icon {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background-color: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    align-self: flex-start;
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #ffffff;
      line-height: 1;
      margin-bottom: 6rpx;
    }

    .stat-label {
      font-size: 12px;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .stat-trend {
    position: absolute;
    top: 24rpx;
    right: 24rpx;
    display: flex;
    align-items: center;
    gap: 6rpx;

    .trend-text {
      font-size: 12px;
      color: #ffffff;
      font-weight: 500;
    }

    &.up .trend-text {
      color: #52c41a;
    }
  }

  .stat-rate {
    position: absolute;
    bottom: 24rpx;
    right: 24rpx;

    .rate-text {
      font-size: 20px;
      font-weight: 600;
      color: #ffffff;
    }
  }

  .stat-action {
    position: absolute;
    bottom: 24rpx;
    right: 24rpx;

    .action-text {
      font-size: 14px;
      color: #ffffff;
      font-weight: 500;
    }
  }
}

// 筛选标签
.filter-tags {
  display: flex;
  gap: 16rpx;
  margin-bottom: 30rpx;
  overflow-x: auto;

  .filter-tag {
    flex-shrink: 0;
    padding: 12rpx 24rpx;
    background-color: #ffffff;
    border-radius: 20rpx;
    font-size: 13px;
    color: #666;
    display: flex;
    align-items: center;
    gap: 8rpx;
    transition: all 0.3s;

    .tag-count {
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
      color: #ffffff;
      font-weight: 500;

      .tag-count {
        background-color: rgba(255, 255, 255, 0.9);
        color: #667eea;
      }
    }
  }
}

// 区块容器
.section-container {
  background-color: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 30rpx;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .section-actions {
      .action-text {
        font-size: 13px;
        color: #667eea;
      }
    }
  }
}

// 设备状态网格
.device-grid-scroll {
  white-space: nowrap;

  .device-grid {
    display: inline-flex;
    gap: 16rpx;

    .device-grid-item {
      width: 200rpx;
      background-color: #f5f5f5;
      border-radius: 16rpx;
      padding: 20rpx;
      position: relative;

      &.offline {
        opacity: 0.6;
      }

      &.alert {
        border: 2px solid #ff4d4f;
      }

      .device-icon {
        margin-bottom: 16rpx;
      }

      .device-info {
        margin-bottom: 16rpx;

        .device-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 6rpx;
        }

        .device-location {
          font-size: 11px;
          color: #999;
        }
      }

      .device-status {
        display: flex;
        align-items: center;
        gap: 8rpx;
        margin-bottom: 12rpx;

        .status-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 50%;
          background-color: #d9d9d9;

          &.online {
            background-color: #52c41a;
          }
        }

        .status-text {
          font-size: 11px;
          color: #666;
        }
      }

      .device-passes {
        display: flex;
        align-items: baseline;
        gap: 4rpx;

        .passes-count {
          font-size: 18px;
          font-weight: 600;
          color: #667eea;
        }

        .passes-label {
          font-size: 11px;
          color: #999;
        }
      }

      .alert-badge {
        position: absolute;
        top: 12rpx;
        right: 12rpx;
      }
    }
  }
}

// 通行记录列表
.pass-record-list {
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60rpx 0;

    .empty-text {
      font-size: 13px;
      color: #999;
      margin-top: 20rpx;
    }
  }

  .pass-record-item {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 16rpx;
    background-color: #f5f5f5;
    border-radius: 12rpx;
    margin-bottom: 12rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .record-time {
      min-width: 80rpx;

      .time-text {
        font-size: 12px;
        color: #999;
      }
    }

    .record-content {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 12rpx;

      .person-info {
        display: flex;
        align-items: center;
        gap: 12rpx;

        .person-avatar {
          width: 48rpx;
          height: 48rpx;
          border-radius: 50%;
          background-color: #eaeaea;
        }

        .person-details {
          .person-name {
            font-size: 14px;
            font-weight: 500;
            color: #333;
            margin-bottom: 4rpx;
          }

          .person-code {
            font-size: 11px;
            color: #999;
          }
        }
      }

      .pass-info {
        display: flex;
        align-items: center;
        gap: 6rpx;
        margin: 0 12rpx;

        .door-name {
          font-size: 13px;
          color: #666;
        }
      }

      .pass-direction {
        width: 32rpx;
        height: 32rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        &.in {
          background-color: rgba(82, 196, 26, 0.1);
        }

        &.out {
          background-color: rgba(255, 77, 79, 0.1);
        }
      }
    }

    .record-status {
      padding: 6rpx 12rpx;
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
}

// 告警列表
.alert-list {
  .alert-item {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 16rpx;
    background-color: #f5f5f5;
    border-radius: 12rpx;
    margin-bottom: 12rpx;
    border-left: 4rpx solid transparent;

    &:last-child {
      margin-bottom: 0;
    }

    &.alert-high {
      border-left-color: #ff4d4f;
      background-color: rgba(255, 77, 79, 0.05);
    }

    &.alert-medium {
      border-left-color: #faad14;
      background-color: rgba(250, 173, 20, 0.05);
    }

    &.alert-low {
      border-left-color: #667eea;
      background-color: rgba(102, 126, 234, 0.05);
    }

    .alert-icon {
      width: 40rpx;
      height: 40rpx;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
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

    .alert-content {
      flex: 1;
      overflow: hidden;

      .alert-title {
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6rpx;
      }

      .alert-message {
        font-size: 12px;
        color: #666;
        margin-bottom: 6rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .alert-time {
        font-size: 11px;
        color: #999;
      }
    }

    .alert-actions {
      flex-shrink: 0;

      .handle-btn {
        padding: 8rpx 20rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        border-radius: 8rpx;
        font-size: 12px;
        color: #ffffff;
        font-weight: 500;
      }
    }
  }
}

// 在线人员滚动列表
.personnel-scroll {
  white-space: nowrap;

  .personnel-scroll-list {
    display: inline-flex;
    gap: 16rpx;

    .personnel-scroll-item {
      width: 180rpx;
      background-color: #f5f5f5;
      border-radius: 16rpx;
      padding: 20rpx;
      text-align: center;

      .personnel-avatar-wrapper {
        position: relative;
        display: inline-block;
        margin-bottom: 16rpx;

        .personnel-avatar {
          width: 64rpx;
          height: 64rpx;
          border-radius: 50%;
          background-color: #eaeaea;
        }

        .online-dot {
          position: absolute;
          bottom: 4rpx;
          right: 4rpx;
          width: 14rpx;
          height: 14rpx;
          border-radius: 50%;
          border: 2px solid #ffffff;
          background-color: #52c41a;
        }
      }

      .personnel-info {
        .personnel-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 6rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .personnel-code {
          font-size: 11px;
          color: #999;
          margin-bottom: 6rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .personnel-area {
          font-size: 11px;
          color: #667eea;
          margin-bottom: 8rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .personnel-duration {
        .duration-text {
          font-size: 11px;
          color: #52c41a;
        }
      }
    }
  }
}

// 底部留白
.bottom-spacer {
  height: 60rpx;
}
</style>
