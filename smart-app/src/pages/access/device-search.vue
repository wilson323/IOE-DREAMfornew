<template>
  <view class="device-search-page">
    <!-- 搜索头部 -->
    <view class="search-header">
      <view class="search-bar">
        <view class="search-input-wrapper">
          <uni-icons type="search" size="18" color="#999"></uni-icons>
          <input
            class="search-input"
            v-model="searchKeyword"
            placeholder="搜索设备名称、编码、IP地址"
            placeholder-class="input-placeholder"
            confirm-type="search"
            @confirm="performSearch"
            @input="onSearchInput"
          />
          <view v-if="searchKeyword" class="clear-btn" @tap="clearSearch">
            <uni-icons type="clear" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="cancel-btn" @tap="goBack">
          <text class="cancel-text">取消</text>
        </view>
      </view>

      <!-- 快捷筛选标签 -->
      <view class="quick-filters" v-if="!hasSearched">
        <view
          v-for="filter in quickFilters"
          :key="filter.value"
          class="filter-tag"
          :class="{ active: activeQuickFilter === filter.value }"
          @tap="selectQuickFilter(filter.value)"
        >
          <text class="filter-text">{{ filter.label }}</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view class="main-scroll" scroll-y>
      <!-- 搜索历史 -->
      <view class="search-history" v-if="!hasSearched && searchHistory.length > 0">
        <view class="history-header">
          <text class="history-title">搜索历史</text>
          <view class="clear-history" @tap="clearSearchHistory">
            <uni-icons type="trash" size="16" color="#999"></uni-icons>
            <text class="clear-text">清空</text>
          </view>
        </view>

        <view class="history-list">
          <view
            v-for="(item, index) in searchHistory"
            :key="index"
            class="history-item"
            @tap="applyHistorySearch(item)"
          >
            <uni-icons type="clock" size="16" color="#999"></uni-icons>
            <text class="history-text">{{ item }}</text>
            <view class="remove-history" @tap.stop="removeHistoryItem(index)">
              <uni-icons type="close" size="14" color="#CCCCCC"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 高级筛选 -->
      <view class="advanced-filter" v-if="!hasSearched">
        <view class="filter-header" @tap="toggleAdvancedFilter">
          <text class="filter-title">高级筛选</text>
          <uni-icons
            :type="showAdvancedFilter ? 'up' : 'down'"
            size="16"
            color="#999"
          ></uni-icons>
        </view>

        <view class="filter-content" v-if="showAdvancedFilter">
          <!-- 设备类型 -->
          <view class="filter-group">
            <text class="group-label">设备类型</text>
            <view class="filter-options">
              <view
                v-for="type in deviceTypes"
                :key="type.value"
                class="filter-option"
                :class="{ selected: filterParams.deviceTypes.includes(type.value) }"
                @tap="toggleDeviceType(type.value)"
              >
                <text class="option-text">{{ type.label }}</text>
              </view>
            </view>
          </view>

          <!-- 设备状态 -->
          <view class="filter-group">
            <text class="group-label">设备状态</text>
            <view class="filter-options">
              <view
                v-for="status in deviceStatus"
                :key="status.value"
                class="filter-option"
                :class="{ selected: filterParams.status === status.value }"
                @tap="selectStatus(status.value)"
              >
                <text class="option-text">{{ status.label }}</text>
              </view>
            </view>
          </view>

          <!-- 区域选择 -->
          <view class="filter-group">
            <text class="group-label">所在区域</text>
            <picker mode="selector" :range="areas" range-key="label" @change="onAreaChange">
              <view class="picker-value">
                <text :class="{ placeholder: !filterParams.areaId }">
                  {{ filterParams.areaId ? getAreaLabel(filterParams.areaId) : '请选择区域' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 操作按钮 -->
          <view class="filter-actions">
            <button class="btn-reset" @tap="resetFilters">重置</button>
            <button class="btn-apply" @tap="applyFilters">应用筛选</button>
          </view>
        </view>
      </view>

      <!-- 搜索结果 -->
      <view class="search-results" v-if="hasSearched">
        <!-- 结果统计 -->
        <view class="result-stats">
          <text class="stats-text">找到 {{ searchResults.length }} 个设备</text>
          <view class="sort-btn" @tap="toggleSortMenu">
            <text class="sort-text">{{ getSortLabel() }}</text>
            <uni-icons type="arrowdown" size="14" color="#667eea"></uni-icons>
          </view>
        </view>

        <!-- 排序菜单 -->
        <view class="sort-menu" v-if="showSortMenu">
          <view
            v-for="sort in sortOptions"
            :key="sort.value"
            class="sort-option"
            :class="{ active: sortBy === sort.value }"
            @tap="selectSort(sort.value)"
          >
            <text class="sort-option-text">{{ sort.label }}</text>
            <uni-icons
              v-if="sortBy === sort.value"
              type="checkbox-filled"
              size="18"
              color="#667eea"
            ></uni-icons>
          </view>
        </view>

        <!-- 设备列表 -->
        <view class="device-list" v-if="searchResults.length > 0">
          <view
            v-for="device in searchResults"
            :key="device.deviceId"
            class="device-card"
            @tap="viewDeviceDetail(device)"
          >
            <!-- 设备图标 -->
            <view class="device-icon" :style="{ background: getDeviceIconGradient(device.deviceType) }">
              <uni-icons
                :type="getDeviceIcon(device.deviceType)"
                size="24"
                color="#ffffff"
              ></uni-icons>
            </view>

            <!-- 设备信息 -->
            <view class="device-info">
              <view class="device-name-row">
                <text class="device-name">{{ device.deviceName }}</text>
                <view class="device-status" :class="device.onlineStatus === 1 ? 'online' : 'offline'">
                  <text class="status-text">{{ device.onlineStatus === 1 ? '在线' : '离线' }}</text>
                </view>
              </view>

              <view class="device-detail">
                <text class="detail-text">{{ device.deviceCode }}</text>
                <text class="detail-separator">|</text>
                <text class="detail-text">{{ getDeviceTypeLabel(device.deviceType) }}</text>
              </view>

              <view class="device-location" v-if="device.areaName">
                <uni-icons type="location" size="12" color="#999"></uni-icons>
                <text class="location-text">{{ device.areaName }}</text>
              </view>
            </view>

            <!-- 快捷操作 -->
            <view class="device-actions" @tap.stop>
              <view class="action-btn" @tap="quickControl(device)">
                <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <!-- 空结果 -->
        <view class="empty-result" v-else>
          <uni-icons type="search" size="80" color="#CCCCCC"></uni-icons>
          <text class="empty-title">未找到相关设备</text>
          <text class="empty-hint">请尝试其他搜索关键词</text>
          <button class="btn-reset-search" @tap="resetSearch">重新搜索</button>
        </view>
      </div>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'

// 搜索状态
const searchKeyword = ref('')
const hasSearched = ref(false)
const searchResults = ref([])
const searchHistory = ref([])

// 快捷筛选
const activeQuickFilter = ref('')
const quickFilters = [
  { label: '全部', value: '' },
  { label: '在线', value: 'online' },
  { label: '离线', value: 'offline' },
  { label: '门禁', value: 'access' },
  { label: '考勤', value: 'attendance' }
]

// 高级筛选
const showAdvancedFilter = ref(false)
const filterParams = reactive({
  deviceTypes: [],
  status: null,
  areaId: null
})

// 选项数据
const deviceTypes = [
  { label: '门禁控制器', value: 1 },
  { label: '门禁一体机', value: 2 },
  { label: '考勤机', value: 3 },
  { label: '消费机', value: 4 }
]

const deviceStatus = [
  { label: '全部', value: null },
  { label: '在线', value: 1 },
  { label: '离线', value: 0 }
]

const areas = ref([
  { label: 'A栋', value: 1 },
  { label: 'B栋', value: 2 },
  { label: 'C栋', value: 3 }
])

// 排序
const sortBy = ref('default')
const showSortMenu = ref(false)
const sortOptions = [
  { label: '默认排序', value: 'default' },
  { label: '名称排序', value: 'name' },
  { label: '时间排序', value: 'time' },
  { label: '状态排序', value: 'status' }
]

// 页面加载
onMounted(() => {
  loadSearchHistory()
})

// 搜索输入
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

// 执行搜索
const performSearch = async () => {
  if (!searchKeyword.value.trim()) {
    uni.showToast({
      title: '请输入搜索关键词',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '搜索中...'
  })

  try {
    const params = {
      keyword: searchKeyword.value,
      deviceTypes: filterParams.deviceTypes,
      status: filterParams.status,
      areaId: filterParams.areaId
    }

    const res = await deviceApi.searchDevices(params)
    if (res.code === 200 && res.data) {
      searchResults.value = res.data.list || []
      hasSearched.value = true

      // 保存到搜索历史
      saveToSearchHistory(searchKeyword.value)
    }
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 选择快捷筛选
const selectQuickFilter = (value) => {
  activeQuickFilter.value = value

  // 根据快捷筛选设置过滤参数
  resetFilters()

  if (value === 'online') {
    filterParams.status = 1
  } else if (value === 'offline') {
    filterParams.status = 0
  } else if (value === 'access') {
    filterParams.deviceTypes = [1, 2]
  } else if (value === 'attendance') {
    filterParams.deviceTypes = [3]
  }

  applyFilters()
}

// 切换高级筛选
const toggleAdvancedFilter = () => {
  showAdvancedFilter.value = !showAdvancedFilter.value
}

// 切换设备类型
const toggleDeviceType = (value) => {
  const index = filterParams.deviceTypes.indexOf(value)
  if (index > -1) {
    filterParams.deviceTypes.splice(index, 1)
  } else {
    filterParams.deviceTypes.push(value)
  }
}

// 选择状态
const selectStatus = (value) => {
  filterParams.status = value
}

// 区域变更
const onAreaChange = (e) => {
  filterParams.areaId = areas.value[e.detail.value].value
}

// 获取区域标签
const getAreaLabel = (areaId) => {
  const area = areas.value.find(a => a.value === areaId)
  return area ? area.label : ''
}

// 重置筛选
const resetFilters = () => {
  filterParams.deviceTypes = []
  filterParams.status = null
  filterParams.areaId = null
}

// 应用筛选
const applyFilters = async () => {
  uni.showLoading({
    title: '加载中...'
  })

  try {
    const params = {
      keyword: searchKeyword.value,
      deviceTypes: filterParams.deviceTypes,
      status: filterParams.status,
      areaId: filterParams.areaId
    }

    const res = await deviceApi.searchDevices(params)
    if (res.code === 200 && res.data) {
      searchResults.value = res.data.list || []
      hasSearched.value = true
      showAdvancedFilter.value = false
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    uni.hideLoading()
  }
}

// 切换排序菜单
const toggleSortMenu = () => {
  showSortMenu.value = !showSortMenu.value
}

// 选择排序
const selectSort = (value) => {
  sortBy.value = value
  showSortMenu.value = false
  sortResults()
}

// 获取排序标签
const getSortLabel = () => {
  const sort = sortOptions.find(s => s.value === sortBy.value)
  return sort ? sort.label : '排序'
}

// 排序结果
const sortResults = () => {
  if (sortBy.value === 'name') {
    searchResults.value.sort((a, b) => {
      return a.deviceName.localeCompare(b.deviceName)
    })
  } else if (sortBy.value === 'time') {
    searchResults.value.sort((a, b) => {
      return new Date(b.createTime) - new Date(a.createTime)
    })
  } else if (sortBy.value === 'status') {
    searchResults.value.sort((a, b) => {
      return b.onlineStatus - a.onlineStatus
    })
  }
}

// 搜索历史
const loadSearchHistory = () => {
  try {
    const history = uni.getStorageSync('device_search_history')
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (error) {
    console.error('加载搜索历史失败:', error)
  }
}

const saveToSearchHistory = (keyword) => {
  try {
    const index = searchHistory.value.indexOf(keyword)
    if (index > -1) {
      searchHistory.value.splice(index, 1)
    }
    searchHistory.value.unshift(keyword)

    // 只保留最近10条
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10)
    }

    uni.setStorageSync('device_search_history', JSON.stringify(searchHistory.value))
  } catch (error) {
    console.error('保存搜索历史失败:', error)
  }
}

const applyHistorySearch = (keyword) => {
  searchKeyword.value = keyword
  performSearch()
}

const removeHistoryItem = (index) => {
  searchHistory.value.splice(index, 1)
  uni.setStorageSync('device_search_history', JSON.stringify(searchHistory.value))
}

const clearSearchHistory = () => {
  uni.showModal({
    title: '清空历史',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = []
        uni.removeStorageSync('device_search_history')
      }
    }
  })
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  hasSearched.value = false
  searchResults.value = []
  resetFilters()
  activeQuickFilter.value = ''
}

// 查看设备详情
const viewDeviceDetail = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-detail?deviceId=${device.deviceId}`
  })
}

// 快捷控制
const quickControl = (device) => {
  uni.navigateTo({
    url: `/pages/access/device-control?deviceId=${device.deviceId}`
  })
}

// 获取设备图标
const getDeviceIcon = (type) => {
  const icons = {
    1: 'locked',
    2: 'locked',
    3: 'calendar',
    4: 'wallet'
  }
  return icons[type] || 'settings'
}

// 获取设备图标渐变色
const getDeviceIconGradient = (type) => {
  const gradients = {
    1: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    2: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    3: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    4: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
  return gradients[type] || gradients[1]
}

// 获取设备类型标签
const getDeviceTypeLabel = (type) => {
  const deviceType = deviceTypes.find(t => t.value === type)
  return deviceType ? deviceType.label : '未知'
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-search-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
}

// 搜索头部
.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #ffffff;
  padding-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  gap: 20rpx;
}

.search-input-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  height: 72rpx;
  padding: 0 24rpx;
  background: #f5f7fa;
  border-radius: 36rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
}

.input-placeholder {
  color: #CCCCCC;
}

.clear-btn {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cancel-btn {
  padding: 10rpx 10rpx;
}

.cancel-text {
  font-size: 28rpx;
  color: #667eea;
}

// 快捷筛选
.quick-filters {
  display: flex;
  gap: 16rpx;
  padding: 0 30rpx;
  overflow-x: auto;
}

.filter-tag {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  background: #f5f7fa;
  border-radius: 36rpx;
  transition: all 0.3s;
}

.filter-tag.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.filter-text {
  font-size: 24rpx;
  color: #666666;
  white-space: nowrap;
}

.filter-tag.active .filter-text {
  color: #ffffff;
  font-weight: 600;
}

// 主内容区域
.main-scroll {
  height: 100vh;
  padding-top: 200rpx;
  padding-bottom: 30rpx;
}

// 搜索历史
.search-history {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.history-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.clear-history {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.clear-text {
  font-size: 24rpx;
  color: #999999;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.history-text {
  flex: 1;
  font-size: 26rpx;
  color: #666666;
}

.remove-history {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 高级筛选
.advanced-filter {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.filter-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.filter-content {
  padding-top: 20rpx;
}

.filter-group {
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.group-label {
  display: block;
  font-size: 26rpx;
  color: #666666;
  margin-bottom: 16rpx;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.filter-option {
  padding: 12rpx 24rpx;
  background: #f5f7fa;
  border-radius: 8rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.filter-option.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.3);
}

.option-text {
  font-size: 24rpx;
  color: #666666;
}

.filter-option.selected .option-text {
  color: #ffffff;
  font-weight: 600;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72rpx;
  padding: 0 24rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #333333;
}

.picker-value .placeholder {
  color: #CCCCCC;
}

.filter-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.btn-reset,
.btn-apply {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-reset {
  background: #f5f7fa;
  color: #666666;
}

.btn-apply {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

// 搜索结果
.search-results {
  padding: 0 30rpx;
}

.result-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.stats-text {
  font-size: 26rpx;
  color: #666666;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 20rpx;
  background: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.sort-text {
  font-size: 24rpx;
  color: #667eea;
}

.sort-menu {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 12rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.sort-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
}

.sort-option.active {
  background: #f5f7fa;
}

.sort-option-text {
  font-size: 26rpx;
  color: #333333;
}

.sort-option.active .sort-option-text {
  color: #667eea;
  font-weight: 600;
}

// 设备列表
.device-list {
  padding: 0;
}

.device-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  &:active {
    transform: scale(0.98);
  }
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

.device-info {
  flex: 1;
}

.device-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.device-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.device-status {
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
}

.device-status.online {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
}

.device-status.offline {
  background: #f0f0f0;
}

.status-text {
  font-size: 20rpx;
  color: #ffffff;
}

.device-status.offline .status-text {
  color: #999999;
}

.device-detail {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.detail-text {
  font-size: 24rpx;
  color: #999999;
}

.detail-separator {
  font-size: 20rpx;
  color: #CCCCCC;
}

.device-location {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.location-text {
  font-size: 22rpx;
  color: #999999;
}

.device-actions {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 12rpx;
}

// 空结果
.empty-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-title {
  font-size: 28rpx;
  color: #666666;
  margin-top: 30rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #CCCCCC;
  margin-top: 16rpx;
}

.btn-reset-search {
  margin-top: 40rpx;
  width: 240rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 36rpx;
  font-size: 26rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}
</style>
