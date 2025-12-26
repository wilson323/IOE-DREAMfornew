<template>
  <view class="record-search-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">高级搜索</text>
        </view>
        <view class="navbar-right">
          <view class="reset-btn" @tap="resetSearch">重置</view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 快速搜索 -->
      <view class="quick-search">
        <view class="search-title">快速搜索</view>
        <view class="search-bar-main">
          <uni-icons type="search" size="20" color="#999"></uni-icons>
          <input
            class="search-input-main"
            type="text"
            placeholder="搜索姓名、工号"
            v-model="quickKeyword"
            @confirm="onQuickSearch"
          />
          <view class="search-btn-main" @tap="onQuickSearch">搜索</view>
        </view>
      </view>

      <!-- 时间范围 -->
      <view class="search-section">
        <view class="section-header">
          <uni-icons type="calendar" size="18" color="#667eea"></uni-icons>
          <text class="section-title">时间范围</text>
        </view>

        <view class="time-presets">
          <view
            class="preset-item"
            :class="{ active: selectedTimePreset === 'today' }"
            @tap="selectTimePreset('today')"
          >
            今日
          </view>
          <view
            class="preset-item"
            :class="{ active: selectedTimePreset === 'yesterday' }"
            @tap="selectTimePreset('yesterday')"
          >
            昨日
          </view>
          <view
            class="preset-item"
            :class="{ active: selectedTimePreset === 'week' }"
            @tap="selectTimePreset('week')"
          >
            本周
          </view>
          <view
            class="preset-item"
            :class="{ active: selectedTimePreset === 'month' }"
            @tap="selectTimePreset('month')"
          >
            本月
          </view>
          <view
            class="preset-item"
            :class="{ active: selectedTimePreset === 'custom' }"
            @tap="selectTimePreset('custom')"
          >
            自定义
          </view>
        </view>

        <view class="custom-time" v-if="selectedTimePreset === 'custom'">
          <picker mode="date" :value="customStartDate" @change="onStartDateChange">
            <view class="date-picker-input">
              <text class="date-text">{{ customStartDate || '开始日期' }}</text>
              <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
            </view>
          </picker>
          <text class="date-separator">至</text>
          <picker mode="date" :value="customEndDate" @change="onEndDateChange">
            <view class="date-picker-input">
              <text class="date-text">{{ customEndDate || '结束日期' }}</text>
              <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <view class="time-range" v-if="selectedTimePreset !== 'custom'">
          <picker mode="time" :value="startTime" @change="onStartTimeChange">
            <view class="time-picker-input">
              <text class="time-text">{{ startTime }}</text>
            </view>
          </picker>
          <text class="time-separator">至</text>
          <picker mode="time" :value="endTime" @change="onEndTimeChange">
            <view class="time-picker-input">
              <text class="time-text">{{ endTime }}</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 通行状态 -->
      <view class="search-section">
        <view class="section-header">
          <uni-icons type="checkbox" size="18" color="#667eea"></uni-icons>
          <text class="section-title">通行状态</text>
        </view>

        <view class="checkbox-grid">
          <label
            class="status-checkbox"
            :class="{ active: searchStatuses.includes('success') }"
            @tap="toggleStatus('success')"
          >
            <view class="checkbox-icon success"></view>
            <text class="checkbox-label">通行成功</text>
          </label>
          <label
            class="status-checkbox"
            :class="{ active: searchStatuses.includes('denied') }"
            @tap="toggleStatus('denied')"
          >
            <view class="checkbox-icon denied"></view>
            <text class="checkbox-label">通行拒绝</text>
          </label>
          <label
            class="status-checkbox"
            :class="{ active: searchStatuses.includes('alarm') }"
            @tap="toggleStatus('alarm')"
          >
            <view class="checkbox-icon alarm"></view>
            <text class="checkbox-label">告警记录</text>
          </label>
        </view>
      </view>

      <!-- 区域选择 -->
      <view class="search-section">
        <view class="section-header">
          <uni-icons type="home" size="18" color="#667eea"></uni-icons>
          <text class="section-title">区域选择</text>
        </view>

        <view class="area-grid">
          <view
            class="area-item"
            :class="{ active: selectedAreas.includes('all') }"
            @tap="selectArea('all')"
          >
            全部区域
          </view>
          <view
            class="area-item"
            :class="{ active: selectedAreas.includes(area.areaId) }"
            v-for="area in areaList"
            :key="area.areaId"
            @tap="toggleArea(area.areaId)"
          >
            {{ area.areaName }}
          </view>
        </view>
      </view>

      <!-- 设备选择 -->
      <view class="search-section">
        <view class="section-header">
          <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
          <text class="section-title">设备选择</text>
        </view>

        <picker mode="selector" :range="deviceList" range-key="deviceName" @change="onDeviceChange">
          <view class="selector-input">
            <text class="selector-text" :class="{ placeholder: !selectedDevice }">
              {{ selectedDeviceText || '请选择设备' }}
            </text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 验证方式 -->
      <view class="search-section">
        <view class="section-header">
          <uni-icons type="person" size="18" color="#667eea"></uni-icons>
          <text class="section-title">验证方式</text>
        </view>

        <view class="method-grid">
          <label
            class="method-item"
            :class="{ active: searchMethods.includes('face') }"
            @tap="toggleMethod('face')"
          >
            <view class="method-icon">
              <uni-icons type="person-filled" size="20"></uni-icons>
            </view>
            <text class="method-label">人脸识别</text>
          </label>
          <label
            class="method-item"
            :class="{ active: searchMethods.includes('fingerprint') }"
            @tap="toggleMethod('fingerprint')"
          >
            <view class="method-icon">
              <uni-icons type="hand-thumbsup" size="20"></uni-icons>
            </view>
            <text class="method-label">指纹识别</text>
          </label>
          <label
            class="method-item"
            :class="{ active: searchMethods.includes('card') }"
            @tap="toggleMethod('card')"
          >
            <view class="method-icon">
              <uni-icons type="wallet" size="20"></uni-icons>
            </view>
            <text class="method-label">刷卡</text>
          </label>
          <label
            class="method-item"
            :class="{ active: searchMethods.includes('qr') }"
            @tap="toggleMethod('qr')"
          >
            <view class="method-icon">
              <uni-icons type="qrcode" size="20"></uni-icons>
            </view>
            <text class="method-label">二维码</text>
          </label>
          <label
            class="method-item"
            :class="{ active: searchMethods.includes('password') }"
            @tap="toggleMethod('password')"
          >
            <view class="method-icon">
              <uni-icons type="locked" size="20"></uni-icons>
            </view>
            <text class="method-label">密码</text>
          </label>
        </view>
      </view>

      <!-- 通行方向 -->
      <view class="search-section">
        <view class="section-header">
          <uni-icons type="redo" size="18" color="#667eea"></uni-icons>
          <text class="section-title">通行方向</text>
        </view>

        <view class="direction-tabs">
          <view
            class="direction-tab"
            :class="{ active: selectedDirection === 'all' }"
            @tap="selectDirection('all')"
          >
            全部
          </view>
          <view
            class="direction-tab"
            :class="{ active: selectedDirection === 'in' }"
            @tap="selectDirection('in')"
          >
            <uni-icons type="up" size="16" color="#667eea"></uni-icons>
            <text>进入</text>
          </view>
          <view
            class="direction-tab"
            :class="{ active: selectedDirection === 'out' }"
            @tap="selectDirection('out')"
          >
            <uni-icons type="down" size="16" color="#667eea"></uni-icons>
            <text>离开</text>
          </view>
        </view>
      </view>

      <!-- 高级选项 -->
      <view class="search-section">
        <view class="section-header" @tap="toggleAdvanced">
          <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
          <text class="section-title">高级选项</text>
          <uni-icons
            :type="showAdvanced ? 'up' : 'down'"
            size="16"
            color="#999"
          ></uni-icons>
        </view>

        <view class="advanced-options" v-if="showAdvanced">
          <!-- 人员类型 -->
          <view class="option-item">
            <text class="option-label">人员类型</text>
            <picker mode="selector" :range="personTypes" range-key="label" @change="onPersonTypeChange">
              <view class="option-picker">
                <text class="picker-text" :class="{ placeholder: !selectedPersonType }">
                  {{ selectedPersonType || '请选择' }}
                </text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 部门选择 -->
          <view class="option-item">
            <text class="option-label">部门选择</text>
            <picker mode="selector" :range="departmentList" range-key="label" @change="onDepartmentChange">
              <view class="option-picker">
                <text class="picker-text" :class="{ placeholder: !selectedDepartment }">
                  {{ selectedDepartment || '请选择' }}
                </text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 告警等级 -->
          <view class="option-item">
            <text class="option-label">告警等级</text>
            <picker mode="selector" :range="alarmLevels" range-key="label" @change="onAlarmLevelChange">
              <view class="option-picker">
                <text class="picker-text" :class="{ placeholder: !selectedAlarmLevel }">
                  {{ selectedAlarmLevel || '请选择' }}
                </text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 结果排序 -->
          <view class="option-item">
            <text class="option-label">结果排序</text>
            <picker mode="selector" :range="sortOptions" range-key="label" @change="onSortChange">
              <view class="option-picker">
                <text class="picker-text">{{ getSortLabel(selectedSort) }}</text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 每页数量 -->
          <view class="option-item">
            <text class="option-label">每页显示</text>
            <picker mode="selector" :range="pageSizeOptions" @change="onPageSizeChange">
              <view class="option-picker">
                <text class="picker-text">{{ selectedPageSize }}条/页</text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 搜索按钮 -->
      <view class="search-actions">
        <view class="action-btn secondary" @tap="resetSearch">
          <text>重置</text>
        </view>
        <view class="action-btn primary" @tap="performSearch">
          <uni-icons type="search" size="18" color="#fff"></uni-icons>
          <text>搜索</text>
        </view>
      </view>

      <!-- 搜索历史 -->
      <view class="search-history" v-if="searchHistory.length > 0">
        <view class="history-header">
          <text class="history-title">搜索历史</text>
          <view class="clear-btn" @tap="clearHistory">
            <uni-icons type="clear" size="14" color="#999"></uni-icons>
            <text class="clear-text">清空</text>
          </view>
        </view>

        <view class="history-list">
          <view
            class="history-item"
            v-for="(item, index) in searchHistory"
            :key="index"
            @tap="applyHistory(item)"
          >
            <view class="history-content">
              <text class="history-keyword">{{ item.keyword }}</text>
              <text class="history-time">{{ formatHistoryTime(item.time) }}</text>
            </view>
            <view class="history-delete" @tap.stop="deleteHistory(index)">
              <uni-icons type="close" size="14" color="#999"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 热门搜索 -->
      <view class="hot-search" v-if="hotSearches.length > 0">
        <view class="hot-header">
          <uni-icons type="fire" size="16" color="#ff6b6b"></uni-icons>
          <text class="hot-title">热门搜索</text>
        </view>

        <view class="hot-list">
          <view
            class="hot-item"
            v-for="(item, index) in hotSearches"
            :key="index"
            @tap="applyHotSearch(item)"
          >
            <text class="hot-rank">{{ index + 1 }}</text>
            <text class="hot-keyword">{{ item.keyword }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 快速搜索
const quickKeyword = ref('')

// 时间范围
const selectedTimePreset = ref('today')
const customStartDate = ref('')
const customEndDate = ref('')
const startTime = ref('00:00')
const endTime = ref('23:59')

// 筛选条件
const searchStatuses = ref(['success', 'denied', 'alarm'])
const selectedAreas = ref(['all'])
const selectedDevice = ref('')
const searchMethods = ref([])
const selectedDirection = ref('all')

// 高级选项
const showAdvanced = ref(false)
const selectedPersonType = ref('')
const selectedDepartment = ref('')
const selectedAlarmLevel = ref('')
const selectedSort = ref('time_desc')
const selectedPageSize = ref(20)

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

const personTypes = [
  { value: 'employee', label: '员工' },
  { value: 'visitor', label: '访客' },
  { value: 'contractor', label: '承包商' }
]

const departmentList = [
  { value: 'tech', label: '技术部' },
  { value: 'market', label: '市场部' },
  { value: 'hr', label: '人事部' }
]

const alarmLevels = [
  { value: 'high', label: '高危' },
  { value: 'medium', label: '中等' },
  { value: 'low', label: '低级' }
]

const sortOptions = [
  { value: 'time_desc', label: '时间倒序' },
  { value: 'time_asc', label: '时间正序' },
  { value: 'name_asc', label: '姓名A-Z' }
]

const pageSizeOptions = [10, 20, 50, 100]

// 搜索历史
const searchHistory = ref([])

// 热门搜索
const hotSearches = ref([
  { keyword: '今日通行成功', count: 1256 },
  { keyword: '访客通行记录', count: 89 },
  { keyword: '告警记录', count: 45 },
  { keyword: '人脸识别', count: 234 }
])

const selectedDeviceText = computed(() => {
  const device = deviceList.value.find(d => d.deviceId === selectedDevice.value)
  return device ? device.deviceName : ''
})

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  loadSearchHistory()
})

// 加载搜索历史
const loadSearchHistory = () => {
  searchHistory.value = [
    { keyword: '今日通行成功', time: Date.now() - 3600000 },
    { keyword: '访客通行记录', time: Date.now() - 7200000 },
    { keyword: '人脸识别', time: Date.now() - 86400000 }
  ]
}

// 快速搜索
const onQuickSearch = () => {
  if (!quickKeyword.value) {
    uni.showToast({
      title: '请输入搜索关键词',
      icon: 'none'
    })
    return
  }

  saveSearchHistory(quickKeyword.value)
  performSearch()
}

// 时间选择
const selectTimePreset = (preset) => {
  selectedTimePreset.value = preset
}

const onStartDateChange = (e) => {
  customStartDate.value = e.detail.value
}

const onEndDateChange = (e) => {
  customEndDate.value = e.detail.value
}

const onStartTimeChange = (e) => {
  startTime.value = e.detail.value
}

const onEndTimeChange = (e) => {
  endTime.value = e.detail.value
}

// 筛选操作
const toggleStatus = (status) => {
  const index = searchStatuses.value.indexOf(status)
  if (index > -1) {
    searchStatuses.value.splice(index, 1)
  } else {
    searchStatuses.value.push(status)
  }
}

const selectArea = (areaId) => {
  if (areaId === 'all') {
    selectedAreas.value = ['all']
  } else {
    const allIndex = selectedAreas.value.indexOf('all')
    if (allIndex > -1) {
      selectedAreas.value.splice(allIndex, 1)
    }
    toggleArea(areaId)
  }
}

const toggleArea = (areaId) => {
  const index = selectedAreas.value.indexOf(areaId)
  if (index > -1) {
    selectedAreas.value.splice(index, 1)
    if (selectedAreas.value.length === 0) {
      selectedAreas.value = ['all']
    }
  } else {
    selectedAreas.value.push(areaId)
  }
}

const onDeviceChange = (e) => {
  selectedDevice.value = deviceList.value[e.detail.value].deviceId
}

const toggleMethod = (method) => {
  const index = searchMethods.value.indexOf(method)
  if (index > -1) {
    searchMethods.value.splice(index, 1)
  } else {
    searchMethods.value.push(method)
  }
}

const selectDirection = (direction) => {
  selectedDirection.value = direction
}

// 高级选项
const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value
}

const onPersonTypeChange = (e) => {
  selectedPersonType.value = personTypes.value[e.detail.value].value
}

const onDepartmentChange = (e) => {
  selectedDepartment.value = departmentList.value[e.detail.value].value
}

const onAlarmLevelChange = (e) => {
  selectedAlarmLevel.value = alarmLevels.value[e.detail.value].value
}

const onSortChange = (e) => {
  selectedSort.value = sortOptions.value[e.detail.value].value
}

const onPageSizeChange = (e) => {
  selectedPageSize.value = pageSizeOptions[e.detail.value]
}

const getSortLabel = (value) => {
  const option = sortOptions.find(o => o.value === value)
  return option ? option.label : '时间倒序'
}

// 搜索历史
const formatHistoryTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${month}-${day}`
  }
}

const applyHistory = (item) => {
  quickKeyword.value = item.keyword
  performSearch()
}

const deleteHistory = (index) => {
  searchHistory.value.splice(index, 1)
}

const clearHistory = () => {
  searchHistory.value = []
}

const saveSearchHistory = (keyword) => {
  const newItem = {
    keyword: keyword,
    time: Date.now()
  }

  searchHistory.value.unshift(newItem)
  if (searchHistory.value.length > 10) {
    searchHistory.value = searchHistory.value.slice(0, 10)
  }
}

// 热门搜索
const applyHotSearch = (item) => {
  quickKeyword.value = item.keyword
  performSearch()
}

// 重置搜索
const resetSearch = () => {
  quickKeyword.value = ''
  selectedTimePreset.value = 'today'
  customStartDate.value = ''
  customEndDate.value = ''
  startTime.value = '00:00'
  endTime.value = '23:59'
  searchStatuses.value = ['success', 'denied', 'alarm']
  selectedAreas.value = ['all']
  selectedDevice.value = ''
  searchMethods.value = []
  selectedDirection.value = 'all'
  showAdvanced.value = false
  selectedPersonType.value = ''
  selectedDepartment.value = ''
  selectedAlarmLevel.value = ''
  selectedSort.value = 'time_desc'
  selectedPageSize.value = 20
}

// 执行搜索
const performSearch = () => {
  // 构建搜索参数
  const searchParams = {
    keyword: quickKeyword.value,
    timeRange: selectedTimePreset.value,
    startTime: customStartDate.value,
    endTime: customEndDate.value,
    startHM: startTime.value,
    endHM: endTime.value,
    statuses: searchStatuses.value,
    areas: selectedAreas.value,
    device: selectedDevice.value,
    methods: searchMethods.value,
    direction: selectedDirection.value,
    personType: selectedPersonType.value,
    department: selectedDepartment.value,
    alarmLevel: selectedAlarmLevel.value,
    sort: selectedSort.value,
    pageSize: selectedPageSize.value
  }

  // 保存搜索历史
  if (quickKeyword.value) {
    saveSearchHistory(quickKeyword.value)
  }

  // 跳转到结果页面
  uni.navigateTo({
    url: '/pages/access/record-list',
    success: () => {
      // 通过事件总线传递搜索参数
      uni.$emit('searchRecords', searchParams)
    }
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.record-search-page {
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
      .reset-btn {
        font-size: 26rpx;
        color: #fff;
      }
    }
  }
}

.page-content {
  padding-top: calc(44px + var(--status-bar-height));
  padding-bottom: 40rpx;
}

.quick-search {
  padding: 30rpx;

  .search-title {
    font-size: 28rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 20rpx;
  }

  .search-bar-main {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 20rpx 30rpx;
    background: #fff;
    border-radius: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .search-input-main {
      flex: 1;
      font-size: 28rpx;
    }

    .search-btn-main {
      padding: 16rpx 40rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 40rpx;
      font-size: 26rpx;
      color: #fff;
    }
  }
}

.search-section {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .section-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .time-presets {
    display: flex;
    gap: 16rpx;
    margin-bottom: 24rpx;

    .preset-item {
      flex: 1;
      padding: 16rpx 0;
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

  .custom-time,
  .time-range {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .date-picker-input,
    .time-picker-input {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 30rpx;
      background: #f5f5f5;
      border-radius: 12rpx;

      .date-text,
      .time-text {
        font-size: 26rpx;
        color: #333;
      }
    }

    .date-separator,
    .time-separator {
      font-size: 26rpx;
      color: #999;
    }
  }

  .checkbox-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;

    .status-checkbox {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      padding: 24rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      &.active {
        border-color: #667eea;
        background: rgba(102, 126, 234, 0.05);
      }

      .checkbox-icon {
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

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

      .checkbox-label {
        font-size: 24rpx;
        color: #666;
      }
    }
  }

  .area-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;

    .area-item {
      padding: 20rpx;
      text-align: center;
      background: #f5f7fa;
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

  .method-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16rpx;

    .method-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      padding: 20rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      border: 2rpx solid transparent;
      transition: all 0.3s;

      &.active {
        border-color: #667eea;
        background: rgba(102, 126, 234, 0.05);
      }

      .method-icon {
        width: 60rpx;
        height: 60rpx;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      .method-label {
        font-size: 24rpx;
        color: #666;
      }
    }
  }

  .direction-tabs {
    display: flex;
    gap: 16rpx;

    .direction-tab {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      padding: 20rpx;
      background: #f5f7fa;
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

  .advanced-options {
    .option-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .option-label {
        font-size: 26rpx;
        color: #666;
      }

      .option-picker {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20rpx 30rpx;
        background: #f5f5f5;
        border-radius: 12rpx;
        min-width: 400rpx;

        .picker-text {
          font-size: 26rpx;
          color: #333;

          &.placeholder {
            color: #999;
          }
        }
      }
    }
  }
}

.search-actions {
  display: flex;
  gap: 20rpx;
  padding: 0 30rpx 30rpx;

  .action-btn {
    flex: 1;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    border-radius: 16rpx;
    font-size: 28rpx;

    &.secondary {
      background: #fff;
      color: #666;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
    }

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
    }
  }
}

.search-history {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .history-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }

    .clear-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;

      .clear-text {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .history-list {
    .history-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .history-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .history-keyword {
          font-size: 28rpx;
          color: #333;
        }

        .history-time {
          font-size: 24rpx;
          color: #999;
        }
      }

      .history-delete {
        width: 40rpx;
        height: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.hot-search {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .hot-header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 20rpx;

    .hot-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .hot-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16rpx;

    .hot-item {
      display: flex;
      align-items: center;
      gap: 16rpx;
      padding: 20rpx;
      background: #fff7e6;
      border-radius: 12rpx;

      .hot-rank {
        min-width: 32rpx;
        width: 32rpx;
        height: 32rpx;
        line-height: 32rpx;
        text-align: center;
        background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
        border-radius: 50%;
        font-size: 20rpx;
        font-weight: 700;
        color: #fff;
      }

      .hot-keyword {
        font-size: 26rpx;
        color: #333;
      }
    }
  }
}
</style>
