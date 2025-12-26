<template>
  <view class="tracking-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">人员追踪</text>
        </view>
        <view class="navbar-right">
          <view class="track-btn" @tap="showTrackingHistory">
            <uni-icons type="calendar" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 人员搜索 -->
      <view class="search-section">
        <view class="search-bar">
          <uni-icons type="search" size="18" color="#999"></uni-icons>
          <input
            class="search-input"
            type="text"
            placeholder="输入姓名、工号或手机号"
            v-model="searchKeyword"
            @input="onSearchInput"
            @confirm="onSearchConfirm"
          />
          <view class="search-btn" @tap="onSearchConfirm">搜索</view>
        </view>

        <!-- 最近搜索 -->
        <view class="recent-search" v-if="recentSearches.length > 0 && !searchKeyword">
          <view class="recent-header">
            <text class="recent-title">最近搜索</text>
            <view class="clear-btn" @tap="clearRecentSearches">
              <uni-icons type="clear" size="14" color="#999"></uni-icons>
            </view>
          </view>
          <view class="recent-list">
            <view
              class="recent-item"
              v-for="item in recentSearches"
              :key="item.id"
              @tap="selectRecentSearch(item)"
            >
              <uni-icons type="history" size="14" color="#999"></uni-icons>
              <text class="recent-text">{{ item.keyword }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 在线人员列表 -->
      <view class="online-section" v-if="!searchKeyword || selectedPerson">
        <view class="section-header">
          <view class="section-title">
            <text class="title-text">在线人员</text>
            <text class="title-count">({{ onlinePersons.length }})</text>
          </view>
          <view class="sort-btn" @tap="toggleSort">
            <uni-icons type="refresh" size="16" color="#667eea"></uni-icons>
            <text class="sort-text">{{ sortOrder === 'time' ? '按时间' : '按区域' }}</text>
          </view>
        </view>

        <scroll-view scroll-x class="person-scroll" v-if="!selectedPerson">
          <view
            class="person-card"
            v-for="person in sortedPersons"
            :key="person.personId"
            @tap="selectPerson(person)"
          >
            <image :src="person.avatar" class="person-avatar" mode="aspectFill"></image>
            <view class="person-info">
              <text class="person-name">{{ person.personName }}</text>
              <text class="person-area">{{ person.currentArea }}</text>
              <text class="person-time">{{ formatDuration(person.onlineDuration) }}</text>
            </view>
            <view class="online-indicator"></view>
          </view>
        </scroll-view>

        <!-- 选中人员详情 -->
        <view class="person-detail" v-if="selectedPerson">
          <view class="detail-header">
            <image :src="selectedPerson.avatar" class="detail-avatar" mode="aspectFill"></image>
            <view class="detail-info">
              <text class="detail-name">{{ selectedPerson.personName }}</text>
              <text class="detail-code">工号: {{ selectedPerson.personCode }}</text>
              <text class="detail-dept">{{ selectedPerson.departmentName }}</text>
            </view>
            <view class="close-detail" @tap="closePersonDetail">
              <uni-icons type="close" size="20" color="#999"></uni-icons>
            </view>
          </view>

          <view class="detail-location">
            <view class="location-item">
              <uni-icons type="home" size="18" color="#667eea"></uni-icons>
              <text class="location-text">{{ selectedPerson.currentArea }}</text>
            </view>
            <view class="location-item">
              <uni-icons type="clock" size="18" color="#667eea"></uni-icons>
              <text class="location-text">在线时长: {{ formatDuration(selectedPerson.onlineDuration) }}</text>
            </view>
          </view>

          <!-- 轨迹操作 -->
          <view class="track-actions">
            <view class="track-btn-item" @tap="viewRealTimeTrack">
              <uni-icons type="location" size="20" color="#667eea"></uni-icons>
              <text class="track-btn-text">实时轨迹</text>
            </view>
            <view class="track-btn-item" @tap="viewHistoryTrack">
              <uni-icons type="calendar" size="20" color="#667eea"></uni-icons>
              <text class="track-btn-text">历史轨迹</text>
            </view>
            <view class="track-btn-item" @tap="callPerson">
              <uni-icons type="phone" size="20" color="#667eea"></uni-icons>
              <text class="track-btn-text">联系TA</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 通行记录 -->
      <view class="records-section" v-if="selectedPerson">
        <view class="section-header">
          <text class="section-title">今日通行记录</text>
          <view class="filter-btn" @tap="filterRecords">
            <uni-icons type="settings" size="16" color="#667eea"></uni-icons>
          </view>
        </view>

        <scroll-view class="records-list" scroll-y>
          <view
            class="record-item"
            v-for="record in personRecords"
            :key="record.recordId"
          >
            <view class="record-time">
              <text class="time-text">{{ formatRecordTime(record.passTime) }}</text>
            </view>

            <view class="record-line"></view>

            <view class="record-content">
              <view class="record-header">
                <view class="door-name">{{ record.doorName }}</view>
                <view class="pass-direction" :class="record.direction">
                  <uni-icons :type="record.direction === 'in' ? 'up' : 'down'" size="16" color="#fff"></uni-icons>
                  <text class="direction-text">{{ record.direction === 'in' ? '进入' : '离开' }}</text>
                </view>
              </view>

              <view class="record-info">
                <view class="info-item">
                  <uni-icons type="home" size="14" color="#666"></uni-icons>
                  <text class="info-text">{{ record.areaName }}</text>
                </view>
                <view class="info-item" v-if="record.verifyMethod">
                  <uni-icons type="person" size="14" color="#666"></uni-icons>
                  <text class="info-text">{{ record.verifyMethod }}</text>
                </view>
              </view>

              <view class="record-status" :class="record.status">
                <uni-icons :type="getStatusIcon(record.status)" size="14"></uni-icons>
                <text class="status-text">{{ getStatusText(record.status) }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 轨迹地图 -->
      <view class="map-section" v-if="showTrackMap">
        <view class="map-header">
          <text class="map-title">{{ trackMapTitle }}</text>
          <view class="close-map" @tap="closeTrackMap">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="map-container">
          <!-- 模拟地图 -->
          <view class="mock-map">
            <view class="map-areas">
              <view
                class="map-area"
                v-for="area in mapAreas"
                :key="area.areaId"
                :style="{ left: area.x + '%', top: area.y + '%' }"
              >
                <view class="area-dot" :class="{ active: area.isActive }"></view>
                <text class="area-label">{{ area.areaName }}</text>
              </view>
            </view>

            <!-- 轨迹线 -->
            <view class="track-lines" v-if="trackPoints.length > 0">
              <svg class="track-svg" width="100%" height="100%">
                <polyline
                  :points="trackLinePoints"
                  fill="none"
                  stroke="#667eea"
                  stroke-width="2"
                  stroke-dasharray="5,5"
                />
              </svg>
            </view>
          </view>
        </view>

        <!-- 轨迹点列表 -->
        <view class="track-points-list">
          <scroll-view scroll-x class="points-scroll">
            <view
              class="track-point-item"
              v-for="(point, index) in trackPoints"
              :key="index"
            >
              <view class="point-time">{{ formatPointTime(point.time) }}</view>
              <view class="point-location">{{ point.location }}</view>
              <view class="point-action">{{ point.action }}</view>
            </view>
          </scroll-view>
        </view>
      </view>
    </view>

    <!-- 历史轨迹查询弹窗 -->
    <uni-popup ref="historyPopup" type="bottom" :safe-area="false">
      <view class="history-popup">
        <view class="popup-header">
          <text class="popup-title">历史轨迹查询</text>
          <view class="close-btn" @tap="closeHistoryPopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <view class="form-section">
            <view class="form-label">
              <text class="label-text">选择日期</text>
            </view>
            <picker mode="date" :value="selectedDate" @change="onDateChange">
              <view class="picker-input">
                <text class="picker-text">{{ selectedDate || '请选择日期' }}</text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <view class="form-section">
            <view class="form-label">
              <text class="label-text">时间段</text>
            </view>
            <view class="time-range">
              <picker mode="time" :value="startTime" @change="onStartTimeChange">
                <view class="time-picker">
                  <text class="time-text">{{ startTime }}</text>
                </view>
              </picker>
              <text class="time-separator">至</text>
              <picker mode="time" :value="endTime" @change="onEndTimeChange">
                <view class="time-picker">
                  <text class="time-text">{{ endTime }}</text>
                </view>
              </picker>
            </view>
          </view>

          <view class="form-section">
            <view class="form-label">
              <text class="label-text">显示选项</text>
            </view>
            <checkbox-group @change="onTrackOptionChange">
              <view class="checkbox-item">
                <checkbox value="showTime" :checked="trackOptions.includes('showTime')" />
                <text class="checkbox-text">显示时间节点</text>
              </view>
              <view class="checkbox-item">
                <checkbox value="showArea" :checked="trackOptions.includes('showArea')" />
                <text class="checkbox-text">显示区域名称</text>
              </view>
              <view class="checkbox-item">
                <checkbox value="showDuration" :checked="trackOptions.includes('showDuration')" />
                <text class="checkbox-text">显示停留时长</text>
              </view>
            </checkbox-group>
          </view>
        </view>

        <view class="popup-actions">
          <view class="popup-btn secondary" @tap="closeHistoryPopup">
            <text>取消</text>
          </view>
          <view class="popup-btn primary" @tap="queryHistoryTrack">
            <text>查询轨迹</text>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 轨迹统计弹窗 -->
    <uni-popup ref="statisticsPopup" type="center">
      <view class="statistics-popup">
        <view class="popup-header">
          <text class="popup-title">轨迹统计</text>
          <view class="close-btn" @tap="closeStatisticsPopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="statistics-content">
          <view class="stat-item">
            <text class="stat-label">今日通行次数:</text>
            <text class="stat-value">{{ trackStatistics.todayPassCount }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">访问区域数:</text>
            <text class="stat-value">{{ trackStatistics.areaCount }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">在线时长:</text>
            <text class="stat-value">{{ formatDuration(trackStatistics.onlineDuration) }}</text>
          </view>
          <view class="stat-item">
            <text class="stat-label">平均停留时长:</text>
            <text class="stat-value">{{ formatDuration(trackStatistics.avgStayDuration) }}</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 搜索
const searchKeyword = ref('')
const recentSearches = ref([])

// 在线人员
const onlinePersons = ref([])
const selectedPerson = ref(null)
const sortOrder = ref('time')

// 通行记录
const personRecords = ref([])

// 轨迹地图
const showTrackMap = ref(false)
const trackMapTitle = ref('实时轨迹')
const mapAreas = ref([])
const trackPoints = ref([])

// 历史查询
const selectedDate = ref('')
const startTime = ref('08:00')
const endTime = ref('18:00')
const trackOptions = ref(['showTime', 'showArea'])

// 统计
const trackStatistics = reactive({
  todayPassCount: 0,
  areaCount: 0,
  onlineDuration: 0,
  avgStayDuration: 0
})

// 计算排序后的人员列表
const sortedPersons = computed(() => {
  let result = [...onlinePersons.value]

  if (sortOrder.value === 'time') {
    result.sort((a, b) => b.onlineDuration - a.onlineDuration)
  } else if (sortOrder.value === 'area') {
    result.sort((a, b) => a.currentArea.localeCompare(b.currentArea))
  }

  return result
})

// 计算轨迹线点
const trackLinePoints = computed(() => {
  if (trackPoints.value.length < 2) return ''

  return trackPoints.value.map(point => {
    const area = mapAreas.value.find(a => a.areaName === point.location)
    return area ? `${area.x},${area.y}` : ''
  }).filter(Boolean).join(' ')
})

onMounted(() => {
  // 获取系统信息
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  // 加载数据
  loadRecentSearches()
  loadOnlinePersons()
  loadMapAreas()

  // 实时更新
  startRealTimeUpdate()
})

onUnmounted(() => {
  stopRealTimeUpdate()
})

// 实时更新定时器
let updateTimer = null

const startRealTimeUpdate = () => {
  updateTimer = setInterval(() => {
    if (selectedPerson.value) {
      loadPersonRecords(selectedPerson.value.personId)
    }
    loadOnlinePersons()
  }, 10000) // 10秒更新一次
}

const stopRealTimeUpdate = () => {
  if (updateTimer) {
    clearInterval(updateTimer)
    updateTimer = null
  }
}

// 加载最近搜索
const loadRecentSearches = () => {
  recentSearches.value = [
    { id: 1, keyword: '张三' },
    { id: 2, keyword: 'E001' },
    { id: 3, keyword: '李四' }
  ]
}

// 加载在线人员
const loadOnlinePersons = () => {
  // 模拟数据
  onlinePersons.value = [
    {
      personId: 'P001',
      personName: '张三',
      personCode: 'E001',
      departmentName: '技术部',
      currentArea: 'A栋2楼办公区',
      onlineDuration: 7200000, // 2小时
      avatar: 'https://picsum.photos/100/100?random=1'
    },
    {
      personId: 'P002',
      personName: '李四',
      personCode: 'E002',
      departmentName: '市场部',
      currentArea: 'B栋会议室',
      onlineDuration: 3600000, // 1小时
      avatar: 'https://picsum.photos/100/100?random=2'
    },
    {
      personId: 'P003',
      personName: '王五',
      personCode: 'E003',
      departmentName: '人事部',
      currentArea: 'C栋餐厅',
      onlineDuration: 1800000, // 30分钟
      avatar: 'https://picsum.photos/100/100?random=3'
    },
    {
      personId: 'P004',
      personName: '赵六',
      personCode: 'E004',
      departmentName: '财务部',
      currentArea: 'A栋1楼大厅',
      onlineDuration: 5400000, // 1.5小时
      avatar: 'https://picsum.photos/100/100?random=4'
    }
  ]

  // 更新统计
  trackStatistics.todayPassCount = Math.floor(Math.random() * 20) + 5
  trackStatistics.areaCount = Math.floor(Math.random() * 5) + 1
  trackStatistics.onlineDuration = 7200000
  trackStatistics.avgStayDuration = 1800000
}

// 加载地图区域
const loadMapAreas = () => {
  mapAreas.value = [
    { areaId: '1', areaName: 'A栋1楼大厅', x: 20, y: 30, isActive: false },
    { areaId: '2', areaName: 'A栋2楼办公区', x: 50, y: 20, isActive: false },
    { areaId: '3', areaName: 'B栋会议室', x: 80, y: 30, isActive: false },
    { areaId: '4', areaName: 'C栋餐厅', x: 30, y: 70, isActive: false },
    { areaId: '5', areaName: 'D栋仓库', x: 70, y: 80, isActive: false }
  ]
}

// 加载人员通行记录
const loadPersonRecords = (personId) => {
  // 模拟数据
  personRecords.value = [
    {
      recordId: 'R001',
      doorName: '主入口门禁',
      areaName: 'A栋1楼大厅',
      direction: 'in',
      passTime: Date.now() - 7200000,
      verifyMethod: '人脸识别',
      status: 'success'
    },
    {
      recordId: 'R002',
      doorName: '2楼办公区门禁',
      areaName: 'A栋2楼办公区',
      direction: 'in',
      passTime: Date.now() - 7140000,
      verifyMethod: '人脸识别',
      status: 'success'
    },
    {
      recordId: 'R003',
      doorId: 'R003',
      doorName: '会议室门禁',
      areaName: 'B栋会议室',
      direction: 'in',
      passTime: Date.now() - 3600000,
      verifyMethod: '指纹识别',
      status: 'success'
    },
    {
      recordId: 'R004',
      doorName: '会议室门禁',
      areaName: 'B栋会议室',
      direction: 'out',
      passTime: Date.now() - 1800000,
      verifyMethod: '指纹识别',
      status: 'success'
    },
    {
      recordId: 'R005',
      doorName: '餐厅门禁',
      areaName: 'C栋餐厅',
      direction: 'in',
      passTime: Date.now() - 900000,
      verifyMethod: '刷卡',
      status: 'success'
    }
  ]
}

// 搜索
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
}

const onSearchConfirm = () => {
  if (!searchKeyword.value) return

  // 添加到最近搜索
  const newSearch = {
    id: Date.now(),
    keyword: searchKeyword.value
  }
  recentSearches.value.unshift(newSearch)
  if (recentSearches.value.length > 5) {
    recentSearches.value = recentSearches.value.slice(0, 5)
  }

  // 搜索人员
  const person = onlinePersons.value.find(p =>
    p.personName.includes(searchKeyword.value) ||
    p.personCode.includes(searchKeyword.value)
  )

  if (person) {
    selectPerson(person)
  } else {
    uni.showToast({
      title: '未找到相关人员',
      icon: 'none'
    })
  }
}

const selectRecentSearch = (item) => {
  searchKeyword.value = item.keyword
  onSearchConfirm()
}

const clearRecentSearches = () => {
  recentSearches.value = []
}

// 选择人员
const selectPerson = (person) => {
  selectedPerson.value = person
  loadPersonRecords(person.personId)
}

const closePersonDetail = () => {
  selectedPerson.value = null
}

// 切换排序
const toggleSort = () => {
  sortOrder.value = sortOrder.value === 'time' ? 'area' : 'time'
}

// 查看实时轨迹
const viewRealTimeTrack = () => {
  if (!selectedPerson.value) return

  trackMapTitle.value = '实时轨迹'
  trackPoints.value = generateRealTimeTrack()
  showTrackMap.value = true
}

// 查看历史轨迹
const viewHistoryTrack = () => {
  if (!selectedPerson.value) return
  uni.$emit('openHistoryPopup')
}

// 联系人员
const callPerson = () => {
  if (!selectedPerson.value) return

  uni.showModal({
    title: '联系人员',
    content: `确认联系 ${selectedPerson.value.personName}？`,
    success: (res) => {
      if (res.confirm) {
        uni.makePhoneCall({
          phoneNumber: '13800138000'
        })
      }
    }
  })
}

// 生成实时轨迹
const generateRealTimeTrack = () => {
  return [
    { time: Date.now() - 7200000, location: 'A栋1楼大厅', action: '进入' },
    { time: Date.now() - 7140000, location: 'A栋2楼办公区', action: '进入' },
    { time: Date.now() - 3600000, location: 'B栋会议室', action: '进入' },
    { time: Date.now() - 1800000, location: 'B栋会议室', action: '离开' },
    { time: Date.now() - 900000, location: 'C栋餐厅', action: '进入' }
  ]
}

// 关闭轨迹地图
const closeTrackMap = () => {
  showTrackMap.value = false
}

// 筛选记录
const filterRecords = () => {
  uni.showActionSheet({
    itemList: ['全部记录', '只看进入', '只看离开', '成功记录', '失败记录'],
    success: (res) => {
      console.log('选择了筛选方式:', res.tapIndex)
    }
  })
}

// 显示历史轨迹
const showTrackingHistory = () => {
  if (selectedPerson.value) {
    viewHistoryTrack()
  } else {
    uni.showToast({
      title: '请先选择人员',
      icon: 'none'
    })
  }
}

// 历史查询
const onDateChange = (e) => {
  selectedDate.value = e.detail.value
}

const onStartTimeChange = (e) => {
  startTime.value = e.detail.value
}

const onEndTimeChange = (e) => {
  endTime.value = e.detail.value
}

const onTrackOptionChange = (e) => {
  trackOptions.value = e.detail.value
}

const closeHistoryPopup = () => {
  uni.$emit('closeHistoryPopup')
}

const queryHistoryTrack = () => {
  closeHistoryPopup()

  trackMapTitle.value = '历史轨迹'
  trackPoints.value = generateHistoryTrack()
  showTrackMap.value = true
}

// 生成历史轨迹
const generateHistoryTrack = () => {
  return [
    { time: Date.now() - 86400000, location: 'A栋1楼大厅', action: '进入' },
    { time: Date.now() - 85800000, location: 'A栋2楼办公区', action: '进入' },
    { time: Date.now() - 72000000, location: 'C栋餐厅', action: '进入' },
    { time: Date.now() - 68400000, location: 'C栋餐厅', action: '离开' },
    { time: Date.now() - 36000000, location: 'A栋2楼办公区', action: '离开' }
  ]
}

// 统计弹窗
const closeStatisticsPopup = () => {
  uni.$emit('closeStatisticsPopup')
}

// 工具方法
const formatDuration = (milliseconds) => {
  const hours = Math.floor(milliseconds / 3600000)
  const minutes = Math.floor((milliseconds % 3600000) / 60000)

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

const formatRecordTime = (timestamp) => {
  const date = new Date(timestamp)
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
}

const formatPointTime = (timestamp) => {
  const date = new Date(timestamp)
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${hour}:${minute}:${second}`
}

const getStatusIcon = (status) => {
  const icons = {
    success: 'checkmarkempty',
    denied: 'close',
    alarm: 'notification'
  }
  return icons[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    success: '通行成功',
    denied: '通行拒绝',
    alarm: '告警'
  }
  return texts[status] || '未知'
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.tracking-page {
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
      .track-btn {
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

.search-section {
  padding: 30rpx;

  .search-bar {
    display: flex;
    align-items: center;
    gap: 20rpx;
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

  .recent-search {
    margin-top: 30rpx;

    .recent-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      .recent-title {
        font-size: 26rpx;
        color: #999;
      }

      .clear-btn {
        width: 40rpx;
        height: 40rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .recent-list {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;

      .recent-item {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 12rpx 24rpx;
        background: #fff;
        border-radius: 40rpx;

        .recent-text {
          font-size: 26rpx;
          color: #666;
        }
      }
    }
  }
}

.online-section {
  padding: 0 30rpx 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      display: flex;
      align-items: center;
      gap: 8rpx;

      .title-text {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
      }

      .title-count {
        font-size: 26rpx;
        color: #999;
      }
    }

    .sort-btn {
      display: flex;
      align-items: center;
      gap: 8rpx;

      .sort-text {
        font-size: 26rpx;
        color: #667eea;
      }
    }
  }

  .person-scroll {
    white-space: nowrap;
    padding-bottom: 20rpx;

    .person-card {
      display: inline-block;
      width: 240rpx;
      padding: 24rpx;
      margin-right: 20rpx;
      background: #fff;
      border-radius: 24rpx;
      box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
      position: relative;

      .person-avatar {
        width: 80rpx;
        height: 80rpx;
        border-radius: 50%;
        margin-bottom: 16rpx;
      }

      .person-info {
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .person-name {
          font-size: 28rpx;
          font-weight: 600;
          color: #333;
        }

        .person-area {
          font-size: 24rpx;
          color: #666;
        }

        .person-time {
          font-size: 24rpx;
          color: #999;
        }
      }

      .online-indicator {
        position: absolute;
        top: 24rpx;
        right: 24rpx;
        width: 24rpx;
        height: 24rpx;
        background: #52c41a;
        border: 4rpx solid #fff;
        border-radius: 50%;
      }
    }
  }

  .person-detail {
    padding: 30rpx;
    background: #fff;
    border-radius: 24rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .detail-header {
      display: flex;
      align-items: center;
      gap: 20rpx;
      margin-bottom: 30rpx;

      .detail-avatar {
        width: 100rpx;
        height: 100rpx;
        border-radius: 50%;
      }

      .detail-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .detail-name {
          font-size: 32rpx;
          font-weight: 600;
          color: #333;
        }

        .detail-code {
          font-size: 26rpx;
          color: #666;
        }

        .detail-dept {
          font-size: 26rpx;
          color: #999;
        }
      }

      .close-detail {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .detail-location {
      display: flex;
      flex-wrap: wrap;
      gap: 24rpx;
      margin-bottom: 30rpx;

      .location-item {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .location-text {
          font-size: 28rpx;
          color: #333;
        }
      }
    }

    .track-actions {
      display: flex;
      gap: 20rpx;

      .track-btn-item {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12rpx;
        padding: 20rpx 0;
        background: #f5f7fa;
        border-radius: 16rpx;

        .track-btn-text {
          font-size: 24rpx;
          color: #666;
        }
      }
    }
  }
}

.records-section {
  padding: 0 30rpx 30rpx;

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20rpx;

    .section-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .filter-btn {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .records-list {
    height: 600rpx;

    .record-item {
      display: flex;
      gap: 20rpx;
      padding: 30rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .record-time {
        width: 80rpx;
        text-align: center;

        .time-text {
          font-size: 24rpx;
          color: #999;
        }
      }

      .record-line {
        width: 2rpx;
        background: #e0e0e0;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          top: 10rpx;
          left: -6rpx;
          width: 14rpx;
          height: 14rpx;
          background: #667eea;
          border-radius: 50%;
        }
      }

      .record-content {
        flex: 1;

        .record-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16rpx;

          .door-name {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
          }

          .pass-direction {
            display: flex;
            align-items: center;
            gap: 4rpx;
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
        }

        .record-info {
          display: flex;
          flex-wrap: wrap;
          gap: 24rpx;
          margin-bottom: 16rpx;

          .info-item {
            display: flex;
            align-items: center;
            gap: 8rpx;

            .info-text {
              font-size: 26rpx;
              color: #666;
            }
          }
        }

        .record-status {
          display: flex;
          align-items: center;
          gap: 8rpx;

          &.success {
            color: #52c41a;
          }

          &.denied {
            color: #ff6b6b;
          }

          &.alarm {
            color: #ffa940;
          }

          .status-text {
            font-size: 24rpx;
          }
        }
      }
    }
  }
}

.map-section {
  margin: 0 30rpx 30rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .map-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .map-title {
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .close-map {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .map-container {
    height: 500rpx;
    padding: 30rpx;

    .mock-map {
      width: 100%;
      height: 100%;
      background: #f5f7fa;
      border-radius: 16rpx;
      position: relative;

      .map-areas {
        .map-area {
          position: absolute;
          transform: translate(-50%, -50%);

          .area-dot {
            width: 24rpx;
            height: 24rpx;
            background: #ddd;
            border-radius: 50%;
            margin: 0 auto 8rpx;

            &.active {
              background: #667eea;
              animation: pulse 1s infinite;
            }
          }

          .area-label {
            display: block;
            font-size: 20rpx;
            color: #666;
            white-space: nowrap;
            text-align: center;
          }
        }
      }

      .track-lines {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        pointer-events: none;

        .track-svg {
          width: 100%;
          height: 100%;
        }
      }
    }
  }

  .track-points-list {
    padding: 20rpx 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .points-scroll {
      white-space: nowrap;

      .track-point-item {
        display: inline-block;
        width: 200rpx;
        padding: 20rpx;
        margin-right: 20rpx;
        background: #f5f7fa;
        border-radius: 16rpx;

        .point-time {
          font-size: 24rpx;
          color: #667eea;
          margin-bottom: 8rpx;
        }

        .point-location {
          font-size: 26rpx;
          color: #333;
          margin-bottom: 8rpx;
        }

        .point-action {
          font-size: 24rpx;
          color: #999;
        }
      }
    }
  }
}

// 弹窗样式
.history-popup {
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;

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
    padding: 30rpx;
    max-height: 60vh;
    overflow-y: auto;

    .form-section {
      margin-bottom: 30rpx;

      .form-label {
        margin-bottom: 20rpx;

        .label-text {
          font-size: 28rpx;
          color: #333;
          font-weight: 600;
        }
      }

      .picker-input {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx 30rpx;
        background: #f5f5f5;
        border-radius: 16rpx;

        .picker-text {
          font-size: 28rpx;
        }
      }

      .time-range {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx 30rpx;
        background: #f5f5f5;
        border-radius: 16rpx;

        .time-picker {
          flex: 1;

          .time-text {
            font-size: 28rpx;
            color: #333;
          }
        }

        .time-separator {
          margin: 0 20rpx;
          font-size: 28rpx;
          color: #999;
        }
      }

      .checkbox-item {
        display: flex;
        align-items: center;
        gap: 16rpx;
        margin-bottom: 20rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .checkbox-text {
          font-size: 28rpx;
          color: #333;
        }
      }
    }
  }

  .popup-actions {
    display: flex;
    gap: 20rpx;
    padding: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .popup-btn {
      flex: 1;
      height: 80rpx;
      line-height: 80rpx;
      text-align: center;
      border-radius: 16rpx;
      font-size: 28rpx;

      &.secondary {
        background: #f5f5f5;
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

.statistics-popup {
  width: 690rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;

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

  .statistics-content {
    padding: 30rpx;

    .stat-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 30rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .stat-label {
        font-size: 28rpx;
        color: #666;
      }

      .stat-value {
        font-size: 28rpx;
        color: #333;
        font-weight: 600;
      }
    }
  }
}

@keyframes pulse {
  0% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
