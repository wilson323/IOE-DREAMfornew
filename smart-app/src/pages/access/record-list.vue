<template>
  <view class="record-list-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">通行记录</text>
        </view>
        <view class="navbar-right">
          <view class="search-btn" @tap="openSearch">
            <uni-icons type="search" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 快速筛选标签 -->
      <view class="quick-filters">
        <scroll-view scroll-x class="filter-scroll" show-scrollbar>
          <view
            class="filter-chip"
            :class="{ active: selectedQuickFilter === 'all' }"
            @tap="selectQuickFilter('all')"
          >
            全部 ({{ totalCount }})
          </view>
          <view
            class="filter-chip"
            :class="{ active: selectedQuickFilter === 'success' }"
            @tap="selectQuickFilter('success')"
          >
            <view class="chip-dot success"></view>
            <text>成功 ({{ successCount }})</text>
          </view>
          <view
            class="filter-chip"
            :class="{ active: selectedQuickFilter === 'denied' }"
            @tap="selectQuickFilter('denied')"
          >
            <view class="chip-dot denied"></view>
            <text>拒绝 ({{ deniedCount }})</text>
          </view>
          <view
            class="filter-chip"
            :class="{ active: selectedQuickFilter === 'alarm' }"
            @tap="selectQuickFilter('alarm')"
          >
            <view class="chip-dot alarm"></view>
            <text>告警 ({{ alarmCount }})</text>
          </view>
        </scroll-view>
      </view>

      <!-- 高级筛选 -->
      <view class="advanced-filter" @tap="openAdvancedFilter">
        <view class="filter-left">
          <uni-icons type="settings" size="16" color="#667eea"></uni-icons>
          <text class="filter-text">高级筛选</text>
        </view>
        <view class="filter-right" v-if="hasActiveFilters">
          <text class="active-count">{{ activeFilterCount }}</text>
          <uni-icons type="close" size="14" color="#ff6b6b" @tap.stop="clearFilters"></uni-icons>
        </view>
      </view>

      <!-- 筛选条件展示 -->
      <view class="active-filters" v-if="hasActiveFilters">
        <scroll-view scroll-x class="filters-scroll" show-scrollbar>
          <view class="active-filter-item" v-for="(filter, index) in activeFiltersList" :key="index">
            <text class="filter-label">{{ filter.label }}: {{ filter.value }}</text>
            <uni-icons type="close" size="12" color="#667eea" @tap="removeFilter(index)"></uni-icons>
          </view>
        </scroll-view>
      </view>

      <!-- 记录列表 -->
      <scroll-view
        class="record-list"
        scroll-y
        @scrolltolower="loadMoreRecords"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view
          class="record-item"
          v-for="record in filteredRecords"
          :key="record.recordId"
          @tap="viewRecordDetail(record)"
        >
          <!-- 记录头部 -->
          <view class="record-header">
            <view class="person-info">
              <image :src="record.avatar" class="person-avatar" mode="aspectFill"></image>
              <view class="person-details">
                <text class="person-name">{{ record.personName }}</text>
                <text class="person-code">工号: {{ record.personCode }}</text>
              </view>
            </view>

            <view class="record-status" :class="record.status">
              <view class="status-dot"></view>
              <text class="status-text">{{ getStatusText(record.status) }}</text>
            </view>
          </view>

          <!-- 记录内容 -->
          <view class="record-content">
            <view class="content-row">
              <view class="content-item">
                <uni-icons type="home" size="14" color="#666"></uni-icons>
                <text class="content-text">{{ record.doorName }}</text>
              </view>
              <view class="content-item">
                <uni-icons type="location" size="14" color="#666"></uni-icons>
                <text class="content-text">{{ record.areaName }}</text>
              </view>
            </view>

            <view class="content-row">
              <view class="content-item">
                <uni-icons type="clock" size="14" color="#666"></uni-icons>
                <text class="content-text">{{ formatRecordTime(record.passTime) }}</text>
              </view>
              <view class="content-item">
                <uni-icons type="person" size="14" color="#666"></uni-icons>
                <text class="content-text">{{ record.verifyMethod }}</text>
              </view>
            </view>

            <view class="content-row" v-if="record.status === 'denied' || record.status === 'alarm'">
              <view class="reason-box">
                <uni-icons type="info" size="14" color="#ff6b6b"></uni-icons>
                <text class="reason-text">{{ record.rejectReason || record.alarmType }}</text>
              </view>
            </view>
          </view>

          <!-- 记录底部 -->
          <view class="record-footer">
            <view class="direction-badge" :class="record.direction">
              <uni-icons :type="record.direction === 'in' ? 'up' : 'down'" size="14" color="#fff"></uni-icons>
              <text class="direction-text">{{ record.direction === 'in' ? '进入' : '离开' }}</text>
            </view>

            <view class="footer-actions">
              <view class="action-item" @tap.stop="viewSnapshot(record)">
                <uni-icons type="camera" size="14" color="#667eea"></uni-icons>
                <text class="action-text">抓拍</text>
              </view>
              <view class="action-item" @tap.stop="viewVideo(record)" v-if="record.hasVideo">
                <uni-icons type="videocam" size="14" color="#667eea"></uni-icons>
                <text class="action-text">视频</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="hasMore">
          <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
          <text class="load-text">加载中...</text>
        </view>

        <!-- 没有更多 -->
        <view class="no-more" v-if="!hasMore && filteredRecords.length > 0">
          <text class="no-more-text">没有更多记录了</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="filteredRecords.length === 0 && !loading">
          <image src="/static/images/empty-records.png" class="empty-image" mode="aspectFit"></image>
          <text class="empty-text">暂无通行记录</text>
          <text class="empty-hint">尝试调整筛选条件</text>
        </view>
      </scroll-view>

      <!-- 批量操作栏 -->
      <view class="batch-actions" v-if="selectedRecords.length > 0">
        <view class="selection-info">
          <text class="selection-count">已选择 {{ selectedRecords.length }} 项</text>
        </view>
        <view class="batch-buttons">
          <view class="batch-btn" @tap="exportSelected">
            <uni-icons type="download" size="16" color="#fff"></uni-icons>
            <text class="batch-text">导出</text>
          </view>
          <view class="batch-btn danger" @tap="clearSelection">
            <text class="batch-text">取消</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 高级筛选弹窗 -->
    <uni-popup ref="filterPopup" type="right" :safe-area="false">
      <view class="filter-popup">
        <view class="popup-header">
          <text class="popup-title">高级筛选</text>
          <view class="header-actions">
            <view class="reset-btn" @tap="resetFilters">重置</view>
            <view class="close-btn" @tap="closeFilterPopup">
              <uni-icons type="close" size="20" color="#666"></uni-icons>
            </view>
          </view>
        </view>

        <scroll-view class="popup-content" scroll-y>
          <!-- 时间范围 -->
          <view class="filter-section">
            <view class="section-title">
              <text class="title-text">时间范围</text>
              <text class="required">*</text>
            </view>
            <view class="time-range-group">
              <view
                class="range-item"
                :class="{ active: filterTimeRange === 'today' }"
                @tap="selectTimeRange('today')"
              >
                今日
              </view>
              <view
                class="range-item"
                :class="{ active: filterTimeRange === 'yesterday' }"
                @tap="selectTimeRange('yesterday')"
              >
                昨日
              </view>
              <view
                class="range-item"
                :class="{ active: filterTimeRange === 'week' }"
                @tap="selectTimeRange('week')"
              >
                本周
              </view>
              <view
                class="range-item"
                :class="{ active: filterTimeRange === 'month' }"
                @tap="selectTimeRange('month')"
              >
                本月
              </view>
              <view
                class="range-item"
                :class="{ active: filterTimeRange === 'custom' }"
                @tap="selectTimeRange('custom')"
              >
                自定义
              </view>
            </view>

            <!-- 自定义时间选择 -->
            <view class="custom-time-range" v-if="filterTimeRange === 'custom'">
              <picker mode="datetime" :value="filterStartTime" @change="onStartTimeChange">
                <view class="time-picker">
                  <text class="time-text">{{ filterStartTime || '开始时间' }}</text>
                  <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
                </view>
              </picker>
              <text class="time-separator">至</text>
              <picker mode="datetime" :value="filterEndTime" @change="onEndTimeChange">
                <view class="time-picker">
                  <text class="time-text">{{ filterEndTime || '结束时间' }}</text>
                  <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </view>

          <!-- 区域选择 -->
          <view class="filter-section">
            <view class="section-title">区域</view>
            <picker mode="selector" :range="areaList" range-key="areaName" @change="onAreaChange">
              <view class="selector-input">
                <text class="selector-text" :class="{ placeholder: !selectedArea }">
                  {{ selectedAreaText || '请选择区域' }}
                </text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 设备选择 -->
          <view class="filter-section">
            <view class="section-title">设备</view>
            <picker mode="selector" :range="deviceList" range-key="deviceName" @change="onDeviceChange">
              <view class="selector-input">
                <text class="selector-text" :class="{ placeholder: !selectedDevice }">
                  {{ selectedDeviceText || '请选择设备' }}
                </text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 通行状态 -->
          <view class="filter-section">
            <view class="section-title">通行状态</view>
            <checkbox-group @change="onStatusChange">
              <view class="checkbox-list">
                <label class="checkbox-item" v-for="status in statusOptions" :key="status.value">
                  <checkbox :value="status.value" :checked="filterStatuses.includes(status.value)" />
                  <text class="checkbox-text">{{ status.label }}</text>
                </label>
              </view>
            </checkbox-group>
          </view>

          <!-- 验证方式 -->
          <view class="filter-section">
            <view class="section-title">验证方式</view>
            <checkbox-group @change="onVerifyMethodChange">
              <view class="checkbox-list">
                <label class="checkbox-item" v-for="method in verifyMethods" :key="method.value">
                  <checkbox :value="method.value" :checked="filterVerifyMethods.includes(method.value)" />
                  <text class="checkbox-text">{{ method.label }}</text>
                </label>
              </view>
            </checkbox-group>
          </view>

          <!-- 人员搜索 -->
          <view class="filter-section">
            <view class="section-title">人员搜索</view>
            <input
              class="search-input"
              type="text"
              placeholder="输入姓名或工号"
              v-model="filterPersonKeyword"
            />
          </view>
        </scroll-view>

        <view class="popup-footer">
          <view class="footer-btn primary" @tap="applyFilters">
            <text>应用筛选</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 记录列表
const recordList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)

// 筛选
const selectedQuickFilter = ref('all')
const hasActiveFilters = ref(false)
const activeFilterCount = ref(0)
const activeFiltersList = ref([])

// 选中的记录
const selectedRecords = ref([])

// 高级筛选
const filterTimeRange = ref('today')
const filterStartTime = ref('')
const filterEndTime = ref('')
const selectedArea = ref('')
const selectedDevice = ref('')
const filterStatuses = ref(['success', 'denied', 'alarm'])
const filterVerifyMethods = ref([])
const filterPersonKeyword = ref('')

// 选项数据
const areaList = ref([
  { areaId: '1', areaName: 'A栋1楼大厅' },
  { areaId: '2', areaName: 'A栋2楼办公区' },
  { areaId: '3', areaName: 'B栋会议室' },
  { areaId: '4', areaName: 'C栋餐厅' }
])

const deviceList = ref([
  { deviceId: 'D001', deviceName: '主入口门禁' },
  { deviceId: 'D002', deviceName: '侧门门禁' },
  { deviceId: 'D003', deviceName: '2楼门禁' }
])

const statusOptions = [
  { value: 'success', label: '通行成功' },
  { value: 'denied', label: '通行拒绝' },
  { value: 'alarm', label: '告警' }
]

const verifyMethods = [
  { value: 'face', label: '人脸识别' },
  { value: 'fingerprint', label: '指纹识别' },
  { value: 'card', label: '刷卡' },
  { value: 'password', label: '密码' },
  { value: 'qr', label: '二维码' }
]

// 计算属性
const totalCount = computed(() => recordList.value.length)
const successCount = computed(() => recordList.value.filter(r => r.status === 'success').length)
const deniedCount = computed(() => recordList.value.filter(r => r.status === 'denied').length)
const alarmCount = computed(() => recordList.value.filter(r => r.status === 'alarm').length)

const selectedAreaText = computed(() => {
  const area = areaList.value.find(a => a.areaId === selectedArea.value)
  return area ? area.areaName : ''
})

const selectedDeviceText = computed(() => {
  const device = deviceList.value.find(d => d.deviceId === selectedDevice.value)
  return device ? device.deviceName : ''
})

const filteredRecords = computed(() => {
  let result = recordList.value

  // 快速筛选
  if (selectedQuickFilter.value !== 'all') {
    result = result.filter(r => r.status === selectedQuickFilter.value)
  }

  // 高级筛选
  if (filterStatuses.value.length > 0) {
    result = result.filter(r => filterStatuses.value.includes(r.status))
  }

  if (filterVerifyMethods.value.length > 0) {
    result = result.filter(r => filterVerifyMethods.value.includes(r.verifyMethodCode))
  }

  if (filterPersonKeyword.value) {
    const keyword = filterPersonKeyword.value.toLowerCase()
    result = result.filter(r =>
      r.personName.toLowerCase().includes(keyword) ||
      r.personCode.toLowerCase().includes(keyword)
    )
  }

  return result
})

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  loadRecordList()
})

// 加载记录列表
const loadRecordList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    await new Promise(resolve => setTimeout(resolve, 500))

    const mockData = generateMockRecords(currentPage.value)

    if (isRefresh) {
      recordList.value = mockData
      currentPage.value = 1
    } else {
      recordList.value.push(...mockData)
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
const generateMockRecords = (page) => {
  const statuses = ['success', 'success', 'success', 'denied', 'alarm']
  const directions = ['in', 'out']
  const methods = ['face', 'fingerprint', 'card', 'qr']
  const methodTexts = { face: '人脸识别', fingerprint: '指纹识别', card: '刷卡', qr: '二维码' }

  const records = []
  for (let i = 0; i < 20; i++) {
    const status = statuses[Math.floor(Math.random() * statuses.length)]
    const direction = directions[Math.floor(Math.random() * directions.length)]
    const method = methods[Math.floor(Math.random() * methods.length)]

    records.push({
      recordId: `REC${Date.now()}${i}`,
      personName: ['张三', '李四', '王五', '赵六', '钱七'][Math.floor(Math.random() * 5)],
      personCode: `E00${Math.floor(Math.random() * 100)}`,
      avatar: `https://picsum.photos/100/100?random=${i}`,
      doorName: ['主入口门禁', '侧门门禁', '2楼门禁'][Math.floor(Math.random() * 3)],
      areaName: ['A栋1楼大厅', 'A栋2楼办公区', 'B栋会议室'][Math.floor(Math.random() * 3)],
      passTime: Date.now() - Math.random() * 86400000,
      direction: direction,
      status: status,
      verifyMethod: methodTexts[method],
      verifyMethodCode: method,
      rejectReason: status === 'denied' ? '权限不足' : '',
      alarmType: status === 'alarm' ? '非法闯入' : '',
      hasVideo: Math.random() > 0.7
    })
  }

  return records
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadRecordList(true)
}

// 加载更多
const loadMoreRecords = () => {
  if (!hasMore.value || loading.value) return
  currentPage.value++
  loadRecordList()
}

// 快速筛选
const selectQuickFilter = (filter) => {
  selectedQuickFilter.value = filter
}

// 高级筛选
const openAdvancedFilter = () => {
  uni.$emit('openFilterPopup')
}

const closeFilterPopup = () => {
  uni.$emit('closeFilterPopup')
}

const selectTimeRange = (range) => {
  filterTimeRange.value = range
}

const onStartTimeChange = (e) => {
  filterStartTime.value = e.detail.value
}

const onEndTimeChange = (e) => {
  filterEndTime.value = e.detail.value
}

const onAreaChange = (e) => {
  selectedArea.value = areaList.value[e.detail.value].areaId
}

const onDeviceChange = (e) => {
  selectedDevice.value = deviceList.value[e.detail.value].deviceId
}

const onStatusChange = (e) => {
  filterStatuses.value = e.detail.value
}

const onVerifyMethodChange = (e) => {
  filterVerifyMethods.value = e.detail.value
}

const resetFilters = () => {
  filterTimeRange.value = 'today'
  filterStartTime.value = ''
  filterEndTime.value = ''
  selectedArea.value = ''
  selectedDevice.value = ''
  filterStatuses.value = ['success', 'denied', 'alarm']
  filterVerifyMethods.value = []
  filterPersonKeyword.value = ''
}

const applyFilters = () => {
  // 构建活跃筛选列表
  activeFiltersList.value = []

  if (filterTimeRange.value !== 'today') {
    activeFiltersList.value.push({
      label: '时间',
      value: getTimeRangeText(filterTimeRange.value)
    })
  }

  if (selectedArea.value) {
    activeFiltersList.value.push({
      label: '区域',
      value: selectedAreaText.value
    })
  }

  if (selectedDevice.value) {
    activeFiltersList.value.push({
      label: '设备',
      value: selectedDeviceText.value
    })
  }

  hasActiveFilters.value = activeFiltersList.value.length > 0
  activeFilterCount.value = activeFiltersList.value.length

  closeFilterPopup()
  loadRecordList(true)
}

const removeFilter = (index) => {
  activeFiltersList.value.splice(index, 1)
  hasActiveFilters.value = activeFiltersList.value.length > 0
  activeFilterCount.value = activeFiltersList.value.length
}

const clearFilters = () => {
  resetFilters()
  hasActiveFilters.value = false
  activeFilterCount.value = 0
  activeFiltersList.value = []
}

// 查看详情
const viewRecordDetail = (record) => {
  uni.navigateTo({
    url: `/pages/access/record-detail?recordId=${record.recordId}`
  })
}

// 查看抓拍
const viewSnapshot = (record) => {
  uni.previewImage({
    urls: [record.avatar]
  })
}

// 查看视频
const viewVideo = (record) => {
  uni.navigateTo({
    url: `/pages/access/record-video?recordId=${record.recordId}`
  })
}

// 搜索
const openSearch = () => {
  uni.navigateTo({
    url: '/pages/access/record-search'
  })
}

// 批量操作
const exportSelected = () => {
  uni.showLoading({
    title: '导出中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
    clearSelection()
  }, 1500)
}

const clearSelection = () => {
  selectedRecords.value = []
}

// 工具方法
const getStatusText = (status) => {
  const texts = {
    success: '通行成功',
    denied: '通行拒绝',
    alarm: '告警'
  }
  return texts[status] || '未知'
}

const formatRecordTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 86400000 && date.getDate() === now.getDate()) {
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    const second = String(date.getSeconds()).padStart(2, '0')
    return `${hour}:${minute}:${second}`
  } else {
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${month}-${day} ${hour}:${minute}`
  }
}

const getTimeRangeText = (range) => {
  const texts = {
    yesterday: '昨日',
    week: '本周',
    month: '本月',
    custom: '自定义'
  }
  return texts[range] || range
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.record-list-page {
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
      .search-btn {
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
  padding-bottom: 140rpx;
}

.quick-filters {
  padding: 30rpx;

  .filter-scroll {
    white-space: nowrap;

    .filter-chip {
      display: inline-flex;
      align-items: center;
      gap: 8rpx;
      padding: 12rpx 24rpx;
      margin-right: 16rpx;
      background: #fff;
      border-radius: 40rpx;
      font-size: 26rpx;
      color: #666;
      transition: all 0.3s;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;

        .chip-dot {
          background: #fff;
        }
      }

      .chip-dot {
        width: 12rpx;
        height: 12rpx;
        border-radius: 50%;

        &.success {
          background: #52c41a;
        }

        &.denied {
          background: #ff6b6b;
        }

        &.alarm {
          background: #ffa940;
        }
      }
    }
  }
}

.advanced-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 30rpx 20rpx;
  padding: 20rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .filter-left {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .filter-text {
      font-size: 28rpx;
      color: #333;
    }
  }

  .filter-right {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .active-count {
      padding: 8rpx 16rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 20rpx;
      font-size: 24rpx;
      color: #fff;
    }
  }
}

.active-filters {
  margin: 0 30rpx 20rpx;

  .filters-scroll {
    white-space: nowrap;

    .active-filter-item {
      display: inline-flex;
      align-items: center;
      gap: 12rpx;
      padding: 12rpx 20rpx;
      margin-right: 16rpx;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      border: 1rpx solid #667eea;
      border-radius: 40rpx;

      .filter-label {
        font-size: 24rpx;
        color: #667eea;
      }
    }
  }
}

.record-list {
  height: calc(100vh - 44px - var(--status-bar-height) - 300rpx);
  padding: 0 30rpx;

  .record-item {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .record-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      .person-info {
        display: flex;
        align-items: center;
        gap: 20rpx;

        .person-avatar {
          width: 80rpx;
          height: 80rpx;
          border-radius: 50%;
        }

        .person-details {
          display: flex;
          flex-direction: column;
          gap: 8rpx;

          .person-name {
            font-size: 30rpx;
            font-weight: 600;
            color: #333;
          }

          .person-code {
            font-size: 24rpx;
            color: #999;
          }
        }
      }

      .record-status {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.success {
          background: #f6ffed;
          color: #52c41a;

          .status-dot {
            background: #52c41a;
          }
        }

        &.denied {
          background: #fff2f0;
          color: #ff6b6b;

          .status-dot {
            background: #ff6b6b;
          }
        }

        &.alarm {
          background: #fff7e6;
          color: #ffa940;

          .status-dot {
            background: #ffa940;
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

    .record-content {
      margin-bottom: 20rpx;

      .content-row {
        display: flex;
        flex-wrap: wrap;
        gap: 24rpx;
        margin-bottom: 16rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .content-item {
          display: flex;
          align-items: center;
          gap: 8rpx;

          .content-text {
            font-size: 26rpx;
            color: #666;
          }
        }

        .reason-box {
          display: flex;
          align-items: center;
          gap: 8rpx;
          padding: 12rpx 20rpx;
          background: #fff2f0;
          border-radius: 12rpx;

          .reason-text {
            font-size: 24rpx;
            color: #ff6b6b;
          }
        }
      }
    }

    .record-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;

      .direction-badge {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.in {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
          color: #fff;
        }

        &.out {
          background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
          color: #fff;
        }

        .direction-text {
          font-size: 24rpx;
        }
      }

      .footer-actions {
        display: flex;
        gap: 32rpx;

        .action-item {
          display: flex;
          align-items: center;
          gap: 8rpx;

          .action-text {
            font-size: 24rpx;
            color: #667eea;
          }
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

  .empty-image {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: 30rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
    margin-bottom: 12rpx;
  }

  .empty-hint {
    font-size: 24rpx;
    color: #ccc;
  }
}

.batch-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
  z-index: 999;

  .selection-info {
    .selection-count {
      font-size: 28rpx;
      color: #333;
      font-weight: 600;
    }
  }

  .batch-buttons {
    display: flex;
    gap: 20rpx;

    .batch-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 32rpx;
      border-radius: 40rpx;
      font-size: 26rpx;
      color: #fff;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      &.danger {
        background: #ff6b6b;
      }

      .batch-text {
        font-size: 26rpx;
      }
    }
  }
}

// 筛选弹窗
.filter-popup {
  width: 600rpx;
  height: 100vh;
  background: #fff;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 20rpx;

      .reset-btn {
        font-size: 26rpx;
        color: #667eea;
      }

      .close-btn {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .popup-content {
    height: calc(100vh - 200rpx);
    padding: 30rpx;

    .filter-section {
      margin-bottom: 40rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 20rpx;

        .required {
          color: #ff6b6b;
          margin-left: 4rpx;
        }
      }

      .time-range-group {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16rpx;

        .range-item {
          padding: 20rpx;
          text-align: center;
          background: #f5f5f5;
          border-radius: 12rpx;
          font-size: 26rpx;
          color: #666;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
          }
        }
      }

      .custom-time-range {
        display: flex;
        align-items: center;
        gap: 16rpx;
        margin-top: 20rpx;

        .time-picker {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 20rpx;
          background: #f5f5f5;
          border-radius: 12rpx;

          .time-text {
            font-size: 26rpx;

            &.placeholder {
              color: #999;
            }
          }
        }

        .time-separator {
          font-size: 26rpx;
          color: #999;
        }
      }

      .selector-input {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx 30rpx;
        background: #f5f5f5;
        border-radius: 16rpx;

        .selector-text {
          font-size: 28rpx;

          &.placeholder {
            color: #999;
          }
        }
      }

      .checkbox-list {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 20rpx;

        .checkbox-item {
          display: flex;
          align-items: center;
          gap: 12rpx;

          .checkbox-text {
            font-size: 26rpx;
            color: #333;
          }
        }
      }

      .search-input {
        width: 100%;
        padding: 24rpx 30rpx;
        background: #f5f5f5;
        border-radius: 16rpx;
        font-size: 28rpx;
      }
    }
  }

  .popup-footer {
    padding: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .footer-btn {
      width: 100%;
      height: 80rpx;
      line-height: 80rpx;
      text-align: center;
      border-radius: 16rpx;
      font-size: 28rpx;

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}
</style>
