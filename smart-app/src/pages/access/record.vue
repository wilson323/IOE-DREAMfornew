<template>
  <view class="access-record-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">通行记录</view>
      <view class="nav-right" @click="showFilterModal">
        <uni-icons type="settings" size="18" color="#666"></uni-icons>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-card">
        <view class="stats-item">
          <text class="stats-value">{{ todayCount }}</text>
          <text class="stats-label">今日通行</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-value">{{ weekCount }}</text>
          <text class="stats-label">本周通行</text>
        </view>
        <view class="stats-divider"></view>
        <view class="stats-item">
          <text class="stats-value">{{ successRate }}%</text>
          <text class="stats-label">成功率</text>
        </view>
      </view>
    </view>

    <!-- 快速筛选 -->
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
          :class="{ active: activeTab === 'success' }"
          @click="switchTab('success')"
        >
          成功通行
        </view>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'fail' }"
          @click="switchTab('fail')"
        >
          失败记录
        </view>
        <view
          class="filter-tab"
          :class="{ active: activeTab === 'today' }"
          @click="switchTab('today')"
        >
          今天
        </view>
      </scroll-view>
    </view>

    <!-- 记录列表 -->
    <view class="record-list">
      <view
        class="record-item"
        v-for="(record, index) in filteredRecords"
        :key="record.id || index"
        @click="viewRecordDetail(record)"
      >
        <view class="record-header">
          <view class="user-info">
            <view class="user-avatar">
              <text class="avatar-text">{{ record.userName ? record.userName.charAt(0) : '用' }}</text>
            </view>
            <view class="user-details">
              <text class="user-name">{{ record.userName || '未知用户' }}</text>
              <text class="user-dept">{{ record.deptName || '未知部门' }}</text>
            </view>
          </view>
          <view :class="['status-badge', record.success ? 'success' : 'fail']">
            <text class="status-text">{{ record.success ? '成功' : '失败' }}</text>
          </view>
        </view>

        <view class="record-content">
          <view class="record-info">
            <view class="info-item">
              <uni-icons type="location" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ record.areaName || '未知区域' }}</text>
            </view>
            <view class="info-item">
              <uni-icons type="calendar" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ formatDateTime(record.accessTime) }}</text>
            </view>
          </view>

          <view class="record-extra" v-if="record.deviceName">
            <text class="device-info">{{ record.deviceName }}</text>
            <text class="access-method">{{ getAccessMethod(record.accessMethod) }}</text>
          </view>
        </view>

        <view class="record-fail-reason" v-if="!record.success && record.failReason">
          <uni-icons type="info" size="14" color="#ff4d4f"></uni-icons>
          <text class="fail-reason">{{ record.failReason }}</text>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-wrapper" v-if="loading">
        <uni-load-more status="loading"></uni-load-more>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="filteredRecords.length === 0 && !loading">
        <image class="empty-icon" src="/static/images/empty-record.png" mode="aspectFit"></image>
        <text class="empty-text">暂无通行记录</text>
        <text class="empty-desc">您的门禁通行记录将显示在这里</text>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="filteredRecords.length > 0 && hasMore">
        <uni-load-more
          :status="loadMoreStatus"
          @clickLoadMore="loadMore"
        ></uni-load-more>
      </view>
    </view>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom">
      <view class="filter-popup">
        <view class="popup-header">
          <text class="popup-title">筛选条件</text>
          <text class="popup-close" @click="closeFilterModal">关闭</text>
        </view>

        <view class="filter-content">
          <view class="filter-group">
            <text class="filter-label">时间范围</text>
            <view class="date-options">
              <view
                class="date-option"
                :class="{ active: dateRange === 'today' }"
                @click="selectDateRange('today')"
              >
                今天
              </view>
              <view
                class="date-option"
                :class="{ active: dateRange === 'week' }"
                @click="selectDateRange('week')"
              >
                本周
              </view>
              <view
                class="date-option"
                :class="{ active: dateRange === 'month' }"
                @click="selectDateRange('month')"
              >
                本月
              </view>
            </view>
          </view>

          <view class="filter-group">
            <text class="filter-label">通行状态</text>
            <view class="status-options">
              <view
                class="status-option"
                :class="{ active: statusFilter === 'all' }"
                @click="selectStatusFilter('all')"
              >
                全部
              </view>
              <view
                class="status-option"
                :class="{ active: statusFilter === 'success' }"
                @click="selectStatusFilter('success')"
              >
                成功
              </view>
              <view
                class="status-option"
                :class="{ active: statusFilter === 'fail' }"
                @click="selectStatusFilter('fail')"
              >
                失败
              </view>
            </view>
          </view>
        </view>

        <view class="filter-actions">
          <button class="reset-btn" @click="resetFilter">重置</button>
          <button class="confirm-btn" @click="applyFilter">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import dayjs from 'dayjs'

// 响应式数据
const recordList = ref([])
const filteredRecords = ref([])
const loading = ref(false)
const hasMore = ref(true)
const loadMoreStatus = ref('more')

// 分页
const pageNum = ref(1)
const pageSize = ref(20)

// 统计数据
const todayCount = ref(0)
const weekCount = ref(0)
const successRate = ref(0)

// 筛选相关
const activeTab = ref('all')
const dateRange = ref('today')
const statusFilter = ref('all')

// 筛选弹窗
const showFilter = ref(false)

// 用户信息
const userId = ref(1001) // 从全局状态获取
const userName = ref('张三')
const deptName = ref('技术部')

// 组件引用
const filterPopup = ref(null)

// 计算属性 - 筛选后的记录
const filteredList = computed(() => {
  let list = [...recordList.value]

  // 按状态筛选
  if (statusFilter.value === 'success') {
    list = list.filter(item => item.success)
  } else if (statusFilter.value === 'fail') {
    list = list.filter(item => !item.success)
  }

  // 按时间筛选
  const now = dayjs()
  if (dateRange.value === 'today') {
    list = list.filter(item => dayjs(item.accessTime).isSame(now, 'day'))
  } else if (dateRange.value === 'week') {
    list = list.filter(item => dayjs(item.accessTime).isAfter(now.subtract(7, 'day')))
  } else if (dateRange.value === 'month') {
    list = list.filter(item => dayjs(item.accessTime).isAfter(now.subtract(1, 'month')))
  }

  return list
})

// 监听筛选条件变化
watch(filteredList, (newList) => {
  filteredRecords.value = newList
}, { immediate: true })

// 监听Tab切换
watch(activeTab, (newTab) => {
  updateFilterByTab(newTab)
})

// 页面生命周期
onMounted(() => {
  initPage()
})

onShow(() => {
  loadRecords()
})

onPullDownRefresh(() => {
  refreshData()
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadMore()
  }
})

// 方法实现
const initPage = () => {
  loadRecords()
  loadStatistics()
}

const loadRecords = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    const params = {
      userId: userId.value,
      pageNum: isRefresh ? 1 : pageNum.value,
      pageSize: pageSize.value
    }

    // 这里调用真实的API
    const mockData = generateMockData()

    if (isRefresh) {
      recordList.value = mockData
      pageNum.value = 1
    } else {
      recordList.value = [...recordList.value, ...mockData]
    }

    // 更新加载状态
    hasMore.value = mockData.length >= pageSize.value
    loadMoreStatus.value = hasMore.value ? 'more' : 'noMore'

  } catch (error) {
    console.error('加载记录失败:', error)
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

const generateMockData = () => {
  const mockRecords = []
  const now = new Date()

  for (let i = 0; i < pageSize.value; i++) {
    const recordTime = new Date(now.getTime() - i * 3600000)
    mockRecords.push({
      id: `record_${pageNum.value}_${i}`,
      userName: userName.value,
      deptName: deptName.value,
      areaName: ['主门', '侧门', '车库入口', '办公区', '会议室'][Math.floor(Math.random() * 5)],
      deviceName: ['门禁终端A1', '门禁终端B2', '门禁终端C3'][Math.floor(Math.random() * 3)],
      accessTime: recordTime.toISOString(),
      success: Math.random() > 0.2,
      accessMethod: ['人脸识别', '刷卡', '指纹', '二维码'][Math.floor(Math.random() * 4)],
      failReason: Math.random() > 0.2 ? null : ['权限不足', '卡片过期', '识别失败'][Math.floor(Math.random() * 3)]
    })
  }

  return mockRecords
}

const loadStatistics = async () => {
  try {
    // 模拟统计数据
    todayCount.value = Math.floor(Math.random() * 10) + 1
    weekCount.value = Math.floor(Math.random() * 50) + 10
    successRate.value = Math.floor(Math.random() * 20) + 80
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const refreshData = () => {
  loadRecords(true)
  loadStatistics()
}

const loadMore = () => {
  pageNum.value++
  loadMoreStatus.value = 'loading'
  loadRecords()
}

const switchTab = (tab) => {
  activeTab.value = tab
}

const updateFilterByTab = (tab) => {
  switch (tab) {
    case 'success':
      statusFilter.value = 'success'
      break
    case 'fail':
      statusFilter.value = 'fail'
      break
    case 'today':
      dateRange.value = 'today'
      break
    default:
      statusFilter.value = 'all'
      dateRange.value = 'month'
  }
}

const showFilterModal = () => {
  filterPopup.value?.open()
}

const closeFilterModal = () => {
  filterPopup.value?.close()
}

const selectDateRange = (range) => {
  dateRange.value = range
}

const selectStatusFilter = (status) => {
  statusFilter.value = status
}

const resetFilter = () => {
  dateRange.value = 'today'
  statusFilter.value = 'all'
  activeTab.value = 'all'
}

const applyFilter = () => {
  closeFilterModal()
  refreshData()
}

const viewRecordDetail = (record) => {
  uni.navigateTo({
    url: `/pages/access/record-detail?id=${record.id}`
  })
}

const getAccessMethod = (method) => {
  const methodMap = {
    'face': '人脸识别',
    'card': '刷卡',
    'finger': '指纹',
    'qr': '二维码'
  }
  return methodMap[method] || method || '未知'
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return dayjs(datetime).format('MM-DD HH:mm')
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.access-record-page {
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

// 记录列表
.record-list {
  padding: 0 30rpx 30rpx;

  .record-item {
    background: white;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
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

    .record-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      .user-info {
        display: flex;
        align-items: center;
        flex: 1;

        .user-avatar {
          width: 80rpx;
          height: 80rpx;
          border-radius: 40rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20rpx;
          box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

          .avatar-text {
            color: white;
            font-size: 32rpx;
            font-weight: bold;
          }
        }

        .user-details {
          flex: 1;

          .user-name {
            font-size: 32rpx;
            font-weight: 600;
            color: #1a1a1a;
            margin-bottom: 6rpx;
          }

          .user-dept {
            font-size: 24rpx;
            color: #666;
          }
        }
      }

      .status-badge {
        padding: 12rpx 20rpx;
        border-radius: 20rpx;
        font-size: 24rpx;
        font-weight: 500;

        &.success {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
          color: white;
          box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
        }

        &.fail {
          background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
          color: white;
          box-shadow: 0 4rpx 12rpx rgba(255, 77, 79, 0.3);
        }
      }
    }

    .record-content {
      .record-info {
        margin-bottom: 16rpx;

        .info-item {
          display: flex;
          align-items: center;
          margin-bottom: 8rpx;

          .info-text {
            font-size: 26rpx;
            color: #666;
            margin-left: 12rpx;
          }
        }
      }

      .record-extra {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 16rpx;
        border-top: 1rpx solid #f0f0f0;

        .device-info {
          font-size: 24rpx;
          color: #666;
        }

        .access-method {
          font-size: 24rpx;
          color: #667eea;
          background: rgba(102, 126, 234, 0.1);
          padding: 4rpx 12rpx;
          border-radius: 12rpx;
        }
      }
    }

    .record-fail-reason {
      display: flex;
      align-items: center;
      margin-top: 16rpx;
      padding: 12rpx 16rpx;
      background: rgba(255, 77, 79, 0.1);
      border-radius: 12rpx;

      .fail-reason {
        font-size: 24rpx;
        color: #ff4d4f;
        margin-left: 8rpx;
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

// 筛选弹窗
.filter-popup {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
  overflow: hidden;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 40rpx 30rpx 20rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .popup-title {
      font-size: 34rpx;
      font-weight: 600;
      color: #1a1a1a;
    }

    .popup-close {
      font-size: 28rpx;
      color: #667eea;
      padding: 10rpx 20rpx;
      border-radius: 20rpx;
      background: rgba(102, 126, 234, 0.1);
    }
  }

  .filter-content {
    padding: 40rpx 30rpx;

    .filter-group {
      margin-bottom: 40rpx;

      .filter-label {
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
        margin-bottom: 20rpx;
      }

      .date-options, .status-options {
        display: flex;
        flex-wrap: wrap;
        gap: 20rpx;

        .date-option, .status-option {
          padding: 20rpx 30rpx;
          border-radius: 20rpx;
          font-size: 28rpx;
          color: #666;
          background: #f8fafc;
          border: 2rpx solid transparent;
          transition: all 0.3s ease;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            transform: translateY(-2rpx);
            box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
          }
        }
      }
    }
  }

  .filter-actions {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .reset-btn, .confirm-btn {
      flex: 1;
      height: 80rpx;
      border-radius: 20rpx;
      font-size: 28rpx;
      font-weight: 500;
      border: none;
    }

    .reset-btn {
      background: #f8fafc;
      color: #666;
    }

    .confirm-btn {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
    }
  }
}
</style>

