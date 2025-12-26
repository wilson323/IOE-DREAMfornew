<template>
  <view class="device-records-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="nav-back" @click="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">通行记录</text>
        <view class="nav-action" @click="showFilterPopup">
          <uni-icons type="settings" size="20" color="#fff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-cards">
      <view class="stat-card">
        <text class="stat-value">{{ todayCount || 0 }}</text>
        <text class="stat-label">今日通行</text>
      </view>
      <view class="stat-card success">
        <text class="stat-value">{{ successCount || 0 }}</text>
        <text class="stat-label">成功</text>
      </view>
      <view class="stat-card fail">
        <text class="stat-value">{{ failCount || 0 }}</text>
        <text class="stat-label">失败</text>
      </view>
    </view>

    <!-- 筛选标签 -->
    <view class="filter-tabs">
      <view
        v-for="tab in filterTabs"
        :key="tab.value"
        :class="['filter-tab', { active: activeTab === tab.value }]"
        @click="switchTab(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 记录列表 -->
    <scroll-view
      scroll-y
      class="records-list"
      :style="{ paddingTop: navbarHeight + 200 + 'rpx' }"
      @scrolltolower="loadMore"
    >
      <view v-if="loading && recordsList.length === 0" class="loading-state">
        <uni-load-more status="loading"></uni-load-more>
      </view>

      <view v-else-if="recordsList.length === 0" class="empty-state">
        <image src="/static/images/empty.png" mode="aspectFit" class="empty-image"></image>
        <text class="empty-text">暂无通行记录</text>
      </view>

      <view v-else>
        <!-- 按日期分组 -->
        <view v-for="(group, date) in groupedRecords" :key="date">
          <view class="date-header">
            <text class="date-text">{{ formatDateGroup(date) }}</text>
            <text class="date-count">{{ group.length }}条</text>
          </view>

          <view
            v-for="record in group"
            :key="record.recordId"
            class="record-item"
            @click="viewRecordDetail(record)"
          >
            <view class="record-header">
              <view class="record-time">
                <text class="time-text">{{ formatTime(record.accessTime) }}</text>
              </view>
              <view :class="['record-status', record.success ? 'success' : 'fail']">
                <uni-icons
                  :type="record.success ? 'checkmarkempty' : 'closeempty'"
                  size="14"
                  :color="record.success ? '#52c41a' : '#ff4d4f'"
                ></uni-icons>
                <text class="status-text">{{ record.success ? '成功' : '失败' }}</text>
              </view>
            </view>

            <view class="record-content">
              <view class="content-row">
                <text class="content-label">人员</text>
                <text class="content-value">{{ record.userName || '-' }}</text>
              </view>
              <view class="content-row">
                <text class="content-label">工号</text>
                <text class="content-value">{{ record.userCode || '-' }}</text>
              </view>
              <view class="content-row">
                <text class="content-label">方式</text>
                <view class="method-tags">
                  <view v-if="record.verifyMethod" class="method-tag">
                    {{ getVerifyMethodName(record.verifyMethod) }}
                  </view>
                </view>
              </view>
              <view v-if="!record.success && record.failReason" class="content-row fail-reason">
                <text class="content-label">原因</text>
                <text class="content-value fail">{{ record.failReason }}</text>
              </view>
            </view>

            <view v-if="record.deviceName" class="record-footer">
              <uni-icons type="location" size="12" color="#999"></uni-icons>
              <text class="footer-text">{{ record.deviceName }}</text>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="load-more">
          <uni-load-more :status="loadingMore ? 'loading' : 'more'"></uni-load-more>
        </view>

        <view v-else-if="recordsList.length > 0" class="no-more">
          <text class="no-more-text">没有更多了</text>
        </view>
      </view>
    </scroll-view>

    <!-- 筛选弹窗 -->
    <uni-popup ref="filterPopup" type="bottom" :safe-area="false">
      <view class="filter-popup">
        <view class="popup-header">
          <text class="popup-title">筛选条件</text>
          <view class="popup-close" @click="hideFilterPopup">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>

        <scroll-view scroll-y class="popup-content">
          <!-- 日期范围 -->
          <view class="filter-section">
            <view class="section-title">日期范围</view>
            <view class="date-range-selector">
              <view
                :class="['date-range-item', { active: dateRangeType === 'today' }]"
                @click="selectDateRange('today')"
              >
                <text class="range-text">今天</text>
              </view>
              <view
                :class="['date-range-item', { active: dateRangeType === 'week' }]"
                @click="selectDateRange('week')"
              >
                <text class="range-text">本周</text>
              </view>
              <view
                :class="['date-range-item', { active: dateRangeType === 'month' }]"
                @click="selectDateRange('month')"
              >
                <text class="range-text">本月</text>
              </view>
              <view
                :class="['date-range-item', { active: dateRangeType === 'custom' }]"
                @click="selectDateRange('custom')"
              >
                <text class="range-text">自定义</text>
              </view>
            </view>

            <view v-if="dateRangeType === 'custom'" class="custom-date-range">
              <view class="date-input" @click="showStartDatePicker">
                <text :class="['date-text', { placeholder: !startDate }]">
                  {{ startDate || '开始日期' }}
                </text>
              </view>
              <text class="date-separator">至</text>
              <view class="date-input" @click="showEndDatePicker">
                <text :class="['date-text', { placeholder: !endDate }]">
                  {{ endDate || '结束日期' }}
                </text>
              </view>
            </view>
          </view>

          <!-- 验证方式 -->
          <view class="filter-section">
            <view class="section-title">验证方式</view>
            <view class="method-selector">
              <view
                v-for="method in verifyMethods"
                :key="method.value"
                :class="['method-chip', { active: selectedMethods.includes(method.value) }]"
                @click="toggleMethod(method.value)"
              >
                <text class="chip-text">{{ method.label }}</text>
              </view>
            </view>
          </view>

          <!-- 结果筛选 -->
          <view class="filter-section">
            <view class="section-title">通行结果</view>
            <view class="result-selector">
              <view
                :class="['result-option', { active: resultFilter === 'all' }]"
                @click="resultFilter = 'all'"
              >
                <text class="option-text">全部</text>
              </view>
              <view
                :class="['result-option', { active: resultFilter === 'success' }]"
                @click="resultFilter = 'success'"
              >
                <text class="option-text">成功</text>
              </view>
              <view
                :class="['result-option', { active: resultFilter === 'fail' }]"
                @click="resultFilter = 'fail'"
              >
                <text class="option-text">失败</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view class="popup-footer">
          <button class="footer-btn secondary" @click="resetFilter">重置</button>
          <button class="footer-btn primary" @click="applyFilter">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- 日期选择器 -->
    <uni-datetime-picker
      v-model="startDatePicker"
      type="date"
      :clear-icon="false"
      @confirm="onStartDateConfirm"
    />
    <uni-datetime-picker
      v-model="endDatePicker"
      type="date"
      :clear-icon="false"
      @confirm="onEndDateConfirm"
    />
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'
import dayjs from 'dayjs'

// 导航栏高度
const navbarHeight = ref(44)

// 设备ID
const deviceId = ref('')

// 记录列表
const recordsList = ref([])

// 当前激活的标签
const activeTab = ref('all')

// 筛选标签
const filterTabs = ref([
  { label: '全部', value: 'all' },
  { label: '成功', value: 'success' },
  { label: '失败', value: 'fail' }
])

// 统计数据
const todayCount = ref(0)
const successCount = ref(0)
const failCount = ref(0)

// 加载状态
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(false)

// 分页参数
const pageNum = ref(1)
const pageSize = ref(20)

// 筛选条件
const dateRangeType = ref('today')
const startDate = ref('')
const endDate = ref('')
const startDatePicker = ref('')
const endDatePicker = ref('')
const selectedMethods = ref([])
const resultFilter = ref('all')

// 验证方式列表
const verifyMethods = [
  { label: '人脸', value: 'face' },
  { label: '指纹', value: 'fingerprint' },
  { label: '卡片', value: 'card' },
  { label: '密码', value: 'password' }
]

// 按日期分组
const groupedRecords = computed(() => {
  const groups = {}
  recordsList.value.forEach(record => {
    const date = dayjs(record.accessTime).format('YYYY-MM-DD')
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(record)
  })
  return groups
})

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'success') {
    resultFilter.value = 'success'
  } else if (tab === 'fail') {
    resultFilter.value = 'fail'
  } else {
    resultFilter.value = 'all'
  }
  loadRecords(true)
}

// 选择日期范围
const selectDateRange = (type) => {
  dateRangeType.value = type
  if (type !== 'custom') {
    startDate.value = ''
    endDate.value = ''
  }
}

// 显示开始日期选择器
const showStartDatePicker = () => {
  // 触发日期选择器
}

// 显示结束日期选择器
const showEndDatePicker = () => {
  // 触发日期选择器
}

// 开始日期确认
const onStartDateConfirm = (e) => {
  startDate.value = dayjs(e).format('YYYY-MM-DD')
}

// 结束日期确认
const onEndDateConfirm = (e) => {
  endDate.value = dayjs(e).format('YYYY-MM-DD')
}

// 切换验证方式
const toggleMethod = (method) => {
  const index = selectedMethods.value.indexOf(method)
  if (index > -1) {
    selectedMethods.value.splice(index, 1)
  } else {
    selectedMethods.value.push(method)
  }
}

// 显示筛选弹窗
const showFilterPopup = () => {
  // 显示弹窗
}

// 隐藏筛选弹窗
const hideFilterPopup = () => {
  // 隐藏弹窗
}

// 应用筛选
const applyFilter = () => {
  hideFilterPopup()
  loadRecords(true)
}

// 重置筛选
const resetFilter = () => {
  dateRangeType.value = 'today'
  startDate.value = ''
  endDate.value = ''
  selectedMethods.value = []
  resultFilter.value = 'all'
  activeTab.value = 'all'
  hideFilterPopup()
  loadRecords(true)
}

// 获取验证方式名称
const getVerifyMethodName = (method) => {
  const map = {
    face: '人脸',
    fingerprint: '指纹',
    card: '刷卡',
    password: '密码',
    qr: '二维码',
    nfc: 'NFC'
  }
  return map[method] || method
}

// 格式化日期分组
const formatDateGroup = (dateStr) => {
  const date = dayjs(dateStr)
  const today = dayjs()
  const yesterday = today.subtract(1, 'day')

  if (date.isSame(today, 'day')) {
    return '今天'
  } else if (date.isSame(yesterday, 'day')) {
    return '昨天'
  } else {
    return date.format('MM月DD日')
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  return dayjs(timeStr).format('HH:mm:ss')
}

// 查看记录详情
const viewRecordDetail = (record) => {
  // 跳转到详情页
  uni.navigateTo({
    url: `/pages/access/record-detail?recordId=${record.recordId}`
  })
}

// 加载记录
const loadRecords = async (refresh = false) => {
  if (refresh) {
    pageNum.value = 1
    recordsList.value = []
  }

  if (loading.value) return
  loading.value = true

  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      resultFilter: resultFilter.value
    }

    // 日期范围
    if (dateRangeType.value === 'today') {
      params.startDate = dayjs().format('YYYY-MM-DD')
      params.endDate = dayjs().format('YYYY-MM-DD')
    } else if (dateRangeType.value === 'week') {
      params.startDate = dayjs().startOf('week').format('YYYY-MM-DD')
      params.endDate = dayjs().endOf('week').format('YYYY-MM-DD')
    } else if (dateRangeType.value === 'month') {
      params.startDate = dayjs().startOf('month').format('YYYY-MM-DD')
      params.endDate = dayjs().endOf('month').format('YYYY-MM-DD')
    } else if (dateRangeType.value === 'custom') {
      if (startDate.value) params.startDate = startDate.value
      if (endDate.value) params.endDate = endDate.value
    }

    // 验证方式
    if (selectedMethods.value.length > 0) {
      params.verifyMethods = selectedMethods.value.join(',')
    }

    const res = await deviceApi.getDeviceRecords(deviceId.value, params)

    if (res.code === 200 && res.data) {
      const list = res.data.list || []

      if (refresh) {
        recordsList.value = list
      } else {
        recordsList.value.push(...list)
      }

      hasMore.value = list.length >= pageSize.value

      // 更新统计
      if (refresh) {
        todayCount.value = res.data.todayCount || 0
        successCount.value = res.data.successCount || 0
        failCount.value = res.data.failCount || 0
      }
    }
  } catch (error) {
    console.error('加载记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return

  loadingMore.value = true
  pageNum.value++

  loadRecords().finally(() => {
    loadingMore.value = false
  })
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
    loadRecords(true)
  }
})
</script>

<style lang="scss" scoped>
.device-records-page {
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

// 统计卡片
.stats-cards {
  position: fixed;
  top: calc(44px + var(--status-bar-height));
  left: 0;
  right: 0;
  z-index: 999;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  background: white;

  .stat-card {
    flex: 1;
    background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
    border-radius: 16rpx;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;

    &.success {
      background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);

      .stat-value {
        color: #52c41a;
      }
    }

    &.fail {
      background: linear-gradient(135deg, #fff2f0 0%, #ffccc7 100%);

      .stat-value {
        color: #ff4d4f;
      }
    }

    .stat-value {
      font-size: 32rpx;
      font-weight: 700;
      color: #667eea;
      margin-bottom: 8rpx;
    }

    .stat-label {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 筛选标签
.filter-tabs {
  position: fixed;
  top: calc(44px + var(--status-bar-height) + 110rpx);
  left: 0;
  right: 0;
  z-index: 998;
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 20rpx;
  background: white;

  .filter-tab {
    padding: 12rpx 32rpx;
    background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
    border-radius: 40rpx;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

      .tab-text {
        color: white;
      }
    }

    .tab-text {
      font-size: 28rpx;
      color: #666;
    }
  }
}

// 记录列表
.records-list {
  height: 100vh;
  padding: 0 30rpx 20rpx;
}

// 日期分组标题
.date-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;

  .date-text {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
  }

  .date-count {
    font-size: 24rpx;
    color: #999;
  }
}

// 记录项
.record-item {
  background: white;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .record-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20rpx;

    .record-time {
      .time-text {
        font-size: 36rpx;
        font-weight: 700;
        color: #333;
      }
    }

    .record-status {
      display: flex;
      align-items: center;
      padding: 8rpx 16rpx;
      border-radius: 40rpx;

      &.success {
        background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);

        .status-text {
          color: #52c41a;
        }
      }

      &.fail {
        background: linear-gradient(135deg, #fff2f0 0%, #ffccc7 100%);

        .status-text {
          color: #ff4d4f;
        }
      }

      .status-text {
        font-size: 24rpx;
        font-weight: 600;
        margin-left: 8rpx;
      }
    }
  }

  .record-content {
    margin-bottom: 16rpx;

    .content-row {
      display: flex;
      align-items: center;
      margin-bottom: 12rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .content-label {
        font-size: 26rpx;
        color: #999;
        min-width: 100rpx;
      }

      .content-value {
        flex: 1;
        font-size: 26rpx;
        color: #333;

        &.fail {
          color: #ff4d4f;
        }
      }

      .method-tags {
        display: flex;
        gap: 12rpx;

        .method-tag {
          padding: 6rpx 12rpx;
          background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
          border-radius: 8rpx;
          font-size: 24rpx;
          color: #667eea;
        }
      }

      &.fail-reason {
        .content-label {
          color: #ff4d4f;
        }
      }
    }
  }

  .record-footer {
    display: flex;
    align-items: center;
    padding-top: 16rpx;
    border-top: 1rpx solid #f0f0f0;

    .footer-text {
      font-size: 24rpx;
      color: #999;
      margin-left: 8rpx;
    }
  }
}

// 筛选弹窗
.filter-popup {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
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

    .popup-close {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .popup-content {
    padding: 30rpx;
    max-height: calc(80vh - 200rpx);

    .filter-section {
      margin-bottom: 30rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 20rpx;
      }

      .date-range-selector {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16rpx;

        .date-range-item {
          padding: 16rpx;
          background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
          border-radius: 12rpx;
          text-align: center;
          border: 2rpx solid transparent;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
            border-color: #667eea;
          }

          .range-text {
            font-size: 26rpx;
            color: #666;
          }
        }
      }

      .custom-date-range {
        display: flex;
        align-items: center;
        gap: 16rpx;
        margin-top: 20rpx;

        .date-input {
          flex: 1;
          padding: 16rpx;
          background: #f6f8fb;
          border-radius: 12rpx;
          text-align: center;

          .date-text {
            font-size: 26rpx;
            color: #333;

            &.placeholder {
              color: #999;
            }
          }
        }

        .date-separator {
          font-size: 26rpx;
          color: #999;
        }
      }

      .method-selector {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;

        .method-chip {
          padding: 12rpx 24rpx;
          background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
          border-radius: 40rpx;
          border: 2rpx solid transparent;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: #667eea;

            .chip-text {
              color: white;
            }
          }

          .chip-text {
            font-size: 26rpx;
            color: #666;
          }
        }
      }

      .result-selector {
        display: flex;
        gap: 16rpx;

        .result-option {
          flex: 1;
          padding: 16rpx;
          background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
          border-radius: 12rpx;
          text-align: center;
          border: 2rpx solid transparent;
          transition: all 0.3s;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: #667eea;

            .option-text {
              color: white;
            }
          }

          .option-text {
            font-size: 26rpx;
            color: #666;
          }
        }
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 20rpx;
    padding: 20rpx 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .footer-btn {
      flex: 1;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 20rpx;
      font-size: 28rpx;
      font-weight: 600;
      border: none;

      &.secondary {
        background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }
    }
  }
}

// 空状态
.empty-state,
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .empty-image {
    width: 300rpx;
    height: 300rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

// 加载更多/没有更多
.load-more,
.no-more {
  padding: 40rpx 0;
  text-align: center;

  .no-more-text {
    font-size: 24rpx;
    color: #999;
  }
}
</style>
