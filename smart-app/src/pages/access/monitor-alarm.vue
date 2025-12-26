<template>
  <view class="alarm-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-center">
          <text class="navbar-title">告警处理</text>
        </view>
        <view class="navbar-right">
          <view class="alarm-badge" @tap="showAlarmStatistics">
            <uni-icons type="notification" size="20" color="#fff"></uni-icons>
            <view class="badge-count" v-if="unhandledCount > 0">{{ unhandledCount > 99 ? '99+' : unhandledCount }}</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 告警统计卡片 -->
      <view class="stats-cards">
        <view class="stat-card high-priority">
          <view class="stat-icon">
            <uni-icons type="info-filled" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ alarmStats.high }}</text>
            <text class="stat-label">高危告警</text>
          </view>
        </view>

        <view class="stat-card medium-priority">
          <view class="stat-icon">
            <uni-icons type="help-filled" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ alarmStats.medium }}</text>
            <text class="stat-label">中等告警</text>
          </view>
        </view>

        <view class="stat-card low-priority">
          <view class="stat-icon">
            <uni-icons type="help" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ alarmStats.low }}</text>
            <text class="stat-label">低级告警</text>
          </view>
        </view>

        <view class="stat-card">
          <view class="stat-icon">
            <uni-icons type="checkmarkempty" size="24" color="#fff"></uni-icons>
          </view>
          <view class="stat-info">
            <text class="stat-value">{{ alarmStats.handled }}</text>
            <text class="stat-label">已处理</text>
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
          全部 ({{ alarmList.length }})
        </view>
        <view
          class="filter-tag"
          :class="{ active: selectedFilter === 'unhandled' }"
          @tap="selectFilter('unhandled')"
        >
          未处理 ({{ unhandledCount }})
        </view>
        <view
          class="filter-tag high"
          :class="{ active: selectedFilter === 'high' }"
          @tap="selectFilter('high')"
        >
          高危
        </view>
        <view
          class="filter-tag medium"
          :class="{ active: selectedFilter === 'medium' }"
          @tap="selectFilter('medium')"
        >
          中等
        </view>
        <view
          class="filter-tag low"
          :class="{ active: selectedFilter === 'low' }"
          @tap="selectFilter('low')"
        >
          低级
        </view>
      </view>

      <!-- 告警类型筛选 -->
      <view class="type-filter-row">
        <scroll-view scroll-x class="type-filter-scroll" show-scrollbar>
          <view
            class="type-filter-item"
            :class="{ active: selectedType === 'all' }"
            @tap="selectType('all')"
          >
            全部类型
          </view>
          <view
            class="type-filter-item"
            :class="{ active: selectedType === 'intrusion' }"
            @tap="selectType('intrusion')"
          >
            <uni-icons type="locked" size="16"></uni-icons>
            <text>非法闯入</text>
          </view>
          <view
            class="type-filter-item"
            :class="{ active: selectedType === 'lingering' }"
            @tap="selectType('lingering')"
          >
            <uni-icons type="clock" size="16"></uni-icons>
            <text>长时间逗留</text>
          </view>
          <view
            class="type-filter-item"
            :class="{ active: selectedType === 'offline' }"
            @tap="selectType('offline')"
          >
            <uni-icons type="cloud-download" size="16"></uni-icons>
            <text>设备离线</text>
          </view>
          <view
            class="type-filter-item"
            :class="{ active: selectedType === 'damage' }"
            @tap="selectType('damage')"
          >
            <uni-icons type="settings" size="16"></uni-icons>
            <text>暴力破坏</text>
          </view>
          <view
            class="type-filter-item"
            :class="{ active: selectedType === 'tailgating' }"
            @tap="selectType('tailgating')"
          >
            <uni-icons type="person" size="16"></uni-icons>
            <text>尾随检测</text>
          </view>
        </scroll-view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <input
          class="search-input"
          type="text"
          placeholder="搜索告警ID、设备名称、区域"
          v-model="searchKeyword"
          @input="onSearchInput"
          @confirm="onSearchConfirm"
        />
        <view class="search-btn" @tap="onSearchConfirm">搜索</view>
      </view>

      <!-- 响应时间提示 -->
      <view class="response-tip" v-if="unhandledCount > 0">
        <uni-icons type="info" size="16" color="#ff6b6b"></uni-icons>
        <text class="tip-text">请注意：未处理告警需在30秒内响应</text>
      </view>

      <!-- 告警列表 -->
      <scroll-view
        class="alarm-list"
        scroll-y
        @scrolltolower="loadMoreAlarms"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <view
          class="alarm-item"
          v-for="alarm in filteredAlarms"
          :key="alarm.alarmId"
          @tap="viewAlarmDetail(alarm)"
        >
          <!-- 告警头部 -->
          <view class="alarm-header">
            <view class="alarm-type-badge" :class="alarm.type">
              <uni-icons :type="getTypeIcon(alarm.type)" size="16" color="#fff"></uni-icons>
              <text class="type-text">{{ getTypeText(alarm.type) }}</text>
            </view>

            <view class="priority-badge" :class="alarm.priority">
              <text>{{ getPriorityText(alarm.priority) }}</text>
            </view>

            <view class="alarm-status" :class="alarm.status">
              <text>{{ getStatusText(alarm.status) }}</text>
            </view>
          </view>

          <!-- 告警内容 -->
          <view class="alarm-content">
            <view class="alarm-title">{{ alarm.title }}</view>
            <view class="alarm-desc">{{ alarm.description }}</view>

            <view class="alarm-info-row">
              <view class="info-item">
                <uni-icons type="home" size="14" color="#666"></uni-icons>
                <text class="info-text">{{ alarm.areaName }}</text>
              </view>
              <view class="info-item">
                <uni-icons type="settings" size="14" color="#666"></uni-icons>
                <text class="info-text">{{ alarm.deviceName }}</text>
              </view>
            </view>

            <view class="alarm-info-row" v-if="alarm.personName">
              <view class="info-item">
                <uni-icons type="person" size="14" color="#666"></uni-icons>
                <text class="info-text">{{ alarm.personName }}</text>
              </view>
              <view class="info-item">
                <uni-icons type="phone" size="14" color="#666"></uni-icons>
                <text class="info-text">{{ alarm.personPhone }}</text>
              </view>
            </view>

            <view class="alarm-time-row">
              <view class="time-item">
                <uni-icons type="clock" size="14" color="#666"></uni-icons>
                <text class="time-text">发生时间: {{ formatTime(alarm.occurTime) }}</text>
              </view>
              <view class="response-time" v-if="alarm.status === 'unhandled' && alarm.responseTime">
                <text class="time-text" :class="{ overtime: alarm.responseTime > 30 }">
                  响应时间: {{ alarm.responseTime }}秒
                  <text v-if="alarm.responseTime > 30"> (超时)</text>
                </text>
              </view>
            </view>
          </view>

          <!-- 告警操作 -->
          <view class="alarm-actions" v-if="alarm.status === 'unhandled'">
            <view class="action-btn secondary" @tap.stop="ignoreAlarm(alarm)">
              <text>忽略</text>
            </view>
            <view class="action-btn primary" @tap.stop="handleAlarm(alarm)">
              <text>立即处理</text>
            </view>
          </view>

          <!-- 处理结果 -->
          <view class="alarm-result" v-if="alarm.status === 'handled'">
            <view class="result-info">
              <uni-icons type="checkmarkempty" size="16" color="#52c41a"></uni-icons>
              <text class="result-text">已由 {{ alarm.handlerName }} 于 {{ formatTime(alarm.handleTime) }} 处理</text>
            </view>
            <text class="result-remark" v-if="alarm.handleRemark">备注: {{ alarm.handleRemark }}</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="hasMore">
          <uni-icons type="spinner-cycle" size="20" color="#999"></uni-icons>
          <text class="load-text">加载中...</text>
        </view>

        <!-- 没有更多 -->
        <view class="no-more" v-if="!hasMore && filteredAlarms.length > 0">
          <text class="no-more-text">没有更多告警了</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="filteredAlarms.length === 0 && !loading">
          <uni-icons type="inbox" size="80" color="#ddd"></uni-icons>
          <text class="empty-text">暂无告警信息</text>
        </view>
      </scroll-view>

      <!-- 底部批量操作栏 -->
      <view class="bottom-actions" v-if="selectedAlarms.length > 0">
        <view class="selection-info">
          <text class="selection-count">已选择 {{ selectedAlarms.length }} 项</text>
        </view>
        <view class="batch-actions">
          <view class="batch-btn secondary" @tap="clearSelection">
            <text>取消选择</text>
          </view>
          <view class="batch-btn danger" @tap="batchHandleAlarms">
            <text>批量处理</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 告警处理弹窗 -->
    <uni-popup ref="handlePopup" type="bottom" :safe-area="false">
      <view class="handle-popup">
        <view class="popup-header">
          <text class="popup-title">处理告警</text>
          <view class="close-btn" @tap="closeHandlePopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <view class="alarm-detail-card">
            <view class="detail-row">
              <text class="detail-label">告警类型:</text>
              <text class="detail-value">{{ currentAlarm.typeText }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">发生位置:</text>
              <text class="detail-value">{{ currentAlarm.areaName }} - {{ currentAlarm.deviceName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">发生时间:</text>
              <text class="detail-value">{{ formatTime(currentAlarm.occurTime) }}</text>
            </view>
            <view class="detail-row" v-if="currentAlarm.personName">
              <text class="detail-label">相关人员:</text>
              <text class="detail-value">{{ currentAlarm.personName }} ({{ currentAlarm.personPhone }})</text>
            </view>
          </view>

          <view class="form-section">
            <view class="form-label">
              <text class="label-text">处理方式</text>
              <text class="required">*</text>
            </view>
            <picker mode="selector" :range="handleMethods" @change="onHandleMethodChange">
              <view class="picker-input">
                <text class="picker-text" :class="{ placeholder: !selectedHandleMethod }">
                  {{ selectedHandleMethod || '请选择处理方式' }}
                </text>
                <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <view class="form-section">
            <view class="form-label">
              <text class="label-text">处理说明</text>
              <text class="required">*</text>
            </view>
            <textarea
              class="textarea-input"
              v-model="handleRemark"
              placeholder="请详细说明处理情况和建议措施"
              maxlength="500"
            ></textarea>
            <view class="char-count">{{ handleRemark.length }}/500</view>
          </view>

          <view class="form-section">
            <view class="form-label">
              <text class="label-text">后续措施</text>
            </view>
            <checkbox-group @change="onFollowUpChange">
              <view class="checkbox-item">
                <checkbox value="patrol" :checked="followUpActions.includes('patrol')" />
                <text class="checkbox-text">加强巡逻</text>
              </view>
              <view class="checkbox-item">
                <checkbox value="maintain" :checked="followUpActions.includes('maintain')" />
                <text class="checkbox-text">设备维护</text>
              </view>
              <view class="checkbox-item">
                <checkbox value="security" :checked="followUpActions.includes('security')" />
                <text class="checkbox-text">加强安保</text>
              </view>
              <view class="checkbox-item">
                <checkbox value="report" :checked="followUpActions.includes('report')" />
                <text class="checkbox-text">上报主管</text>
              </view>
            </checkbox-group>
          </view>
        </view>

        <view class="popup-actions">
          <view class="popup-btn secondary" @tap="closeHandlePopup">
            <text>取消</text>
          </view>
          <view class="popup-btn primary" @tap="confirmHandleAlarm">
            <text>确认处理</text>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 告警统计弹窗 -->
    <uni-popup ref="statisticsPopup" type="center">
      <view class="statistics-popup">
        <view class="popup-header">
          <text class="popup-title">告警统计</text>
          <view class="close-btn" @tap="closeStatisticsPopup">
            <uni-icons type="close" size="20" color="#666"></uni-icons>
          </view>
        </view>

        <view class="statistics-content">
          <view class="stat-chart">
            <view class="chart-title">今日告警趋势</view>
            <view class="chart-bars">
              <view class="chart-bar" v-for="(hour, index) in alarmTrend" :key="index">
                <view
                  class="bar-fill"
                  :style="{ height: (hour.count / maxTrendCount * 100) + '%' }"
                ></view>
                <text class="bar-label">{{ hour.hour }}:00</text>
                <text class="bar-value">{{ hour.count }}</text>
              </view>
            </view>
          </view>

          <view class="stat-summary">
            <view class="summary-item">
              <text class="summary-label">今日告警总数:</text>
              <text class="summary-value">{{ alarmStats.total }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">已处理:</text>
              <text class="summary-value success">{{ alarmStats.handled }}</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">处理率:</text>
              <text class="summary-value">{{ ((alarmStats.handled / alarmStats.total) * 100).toFixed(1) }}%</text>
            </view>
            <view class="summary-item">
              <text class="summary-label">平均响应时间:</text>
              <text class="summary-value">{{ avgResponseTime }}秒</text>
            </view>
          </view>

          <view class="type-distribution">
            <view class="distribution-title">告警类型分布</view>
            <view class="distribution-list">
              <view class="distribution-item" v-for="type in typeDistribution" :key="type.code">
                <view class="type-info">
                  <view class="type-dot" :style="{ backgroundColor: type.color }"></view>
                  <text class="type-name">{{ type.name }}</text>
                </view>
                <view class="type-stats">
                  <text class="type-count">{{ type.count }}</text>
                  <text class="type-percent">{{ type.percent }}%</text>
                </view>
              </view>
            </view>
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

// 告警统计数据
const alarmStats = reactive({
  total: 45,
  high: 3,
  medium: 12,
  low: 18,
  handled: 12
})

// 告警列表
const alarmList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)

// 筛选条件
const selectedFilter = ref('unhandled')
const selectedType = ref('all')
const searchKeyword = ref('')

// 选中的告警
const selectedAlarms = ref([])

// 告警处理
const currentAlarm = ref({})
const selectedHandleMethod = ref('')
const handleRemark = ref('')
const followUpActions = ref([])
const handleMethods = ['现场查看', '电话核实', '视频确认', '联系安保', '已解决误报']

// 告警趋势
const alarmTrend = ref([])
const maxTrendCount = ref(0)
const avgResponseTime = ref(15)

// 类型分布
const typeDistribution = ref([])

// 计算未处理数量
const unhandledCount = computed(() => {
  return alarmList.value.filter(a => a.status === 'unhandled').length
})

// 筛选后的告警列表
const filteredAlarms = computed(() => {
  let result = alarmList.value

  // 状态筛选
  if (selectedFilter.value === 'unhandled') {
    result = result.filter(a => a.status === 'unhandled')
  } else if (selectedFilter.value !== 'all') {
    result = result.filter(a => a.priority === selectedFilter.value)
  }

  // 类型筛选
  if (selectedType.value !== 'all') {
    result = result.filter(a => a.type === selectedType.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(a =>
      a.alarmId.toLowerCase().includes(keyword) ||
      a.deviceName.toLowerCase().includes(keyword) ||
      a.areaName.toLowerCase().includes(keyword) ||
      (a.personName && a.personName.toLowerCase().includes(keyword))
    )
  }

  return result
})

onMounted(() => {
  // 获取系统信息
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight

  // 加载数据
  loadAlarmList()
  loadAlarmStatistics()
  startRealTimeUpdate()
})

onUnmounted(() => {
  stopRealTimeUpdate()
})

// 实时更新定时器
let updateTimer = null

const startRealTimeUpdate = () => {
  updateTimer = setInterval(() => {
    loadAlarmList(true)
    updateResponseTime()
  }, 5000) // 5秒更新一次
}

const stopRealTimeUpdate = () => {
  if (updateTimer) {
    clearInterval(updateTimer)
    updateTimer = null
  }
}

// 更新响应时间
const updateResponseTime = () => {
  const now = Date.now()
  alarmList.value.forEach(alarm => {
    if (alarm.status === 'unhandled') {
      const elapsed = Math.floor((now - alarm.occurTime) / 1000)
      alarm.responseTime = elapsed
    }
  })
}

// 加载告警列表
const loadAlarmList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))

    // 模拟数据
    const mockData = generateMockAlarms(currentPage.value)

    if (isRefresh) {
      alarmList.value = mockData
      currentPage.value = 1
    } else {
      alarmList.value.push(...mockData)
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
const generateMockAlarms = (page) => {
  const types = ['intrusion', 'lingering', 'offline', 'damage', 'tailgating']
  const priorities = ['high', 'medium', 'low']
  const statuses = ['unhandled', 'handled', 'ignored']

  const alarms = []
  for (let i = 0; i < 20; i++) {
    const type = types[Math.floor(Math.random() * types.length)]
    const priority = priorities[Math.floor(Math.random() * priorities.length)]
    const status = statuses[Math.floor(Math.random() * statuses.length)]

    alarms.push({
      alarmId: `ALM${Date.now()}${i}`,
      type: type,
      priority: priority,
      status: status,
      title: generateAlarmTitle(type),
      description: generateAlarmDesc(type),
      areaName: ['A栋1楼大厅', 'B栋办公区', 'C栋会议室', '餐厅入口'][Math.floor(Math.random() * 4)],
      deviceName: `门禁设备-${String(i + 1).padStart(3, '0')}`,
      personName: status === 'handled' ? '张三' : '李四',
      personPhone: status === 'handled' ? '138****1234' : '139****5678',
      occurTime: Date.now() - Math.random() * 3600000,
      responseTime: status === 'unhandled' ? Math.floor(Math.random() * 60) : 0,
      handlerName: status === 'handled' ? '王安保' : '',
      handleTime: status === 'handled' ? Date.now() - 1800000 : 0,
      handleRemark: status === 'handled' ? '已现场核实，属误报' : ''
    })
  }

  return alarms
}

const generateAlarmTitle = (type) => {
  const titles = {
    intrusion: '检测到非法闯入',
    lingering: '检测到长时间逗留',
    offline: '设备离线告警',
    damage: '检测到暴力破坏',
    tailgating: '检测到尾随行为'
  }
  return titles[type] || '未知告警'
}

const generateAlarmDesc = (type) => {
  const descs = {
    intrusion: '检测到非授权人员尝试通过门禁设备',
    lingering: '人员在区域内逗留时间超过30分钟',
    offline: '门禁设备失去连接，可能存在故障',
    damage: '设备检测到暴力破坏行为',
    tailgating: '检测到紧随他人通过门禁的行为'
  }
  return descs[type] || '异常告警'
}

// 加载告警统计
const loadAlarmStatistics = () => {
  // 告警趋势
  const trend = []
  let maxCount = 0
  for (let i = 8; i < 18; i++) {
    const count = Math.floor(Math.random() * 10)
    trend.push({ hour: i, count })
    maxCount = Math.max(maxCount, count)
  }
  alarmTrend.value = trend
  maxTrendCount.value = maxCount

  // 类型分布
  typeDistribution.value = [
    { code: 'intrusion', name: '非法闯入', count: 8, percent: 17.8, color: '#ff6b6b' },
    { code: 'lingering', name: '长时间逗留', count: 12, percent: 26.7, color: '#ffa940' },
    { code: 'offline', name: '设备离线', count: 6, percent: 13.3, color: '#ffec3d' },
    { code: 'damage', name: '暴力破坏', count: 3, percent: 6.7, color: '#ff4d4f' },
    { code: 'tailgating', name: '尾随检测', count: 4, percent: 8.9, color: '#722ed1' }
  ]
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadAlarmList(true)
}

// 加载更多
const loadMoreAlarms = () => {
  if (!hasMore.value || loading.value) return
  currentPage.value++
  loadAlarmList()
}

// 搜索
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
}

const onSearchConfirm = () => {
  loadAlarmList(true)
}

// 筛选
const selectFilter = (filter) => {
  selectedFilter.value = filter
}

const selectType = (type) => {
  selectedType.value = type
}

// 查看告警详情
const viewAlarmDetail = (alarm) => {
  uni.navigateTo({
    url: `/pages/access/alarm-detail?alarmId=${alarm.alarmId}`
  })
}

// 忽略告警
const ignoreAlarm = (alarm) => {
  uni.showModal({
    title: '确认忽略',
    content: '确认忽略此告警？',
    success: (res) => {
      if (res.confirm) {
        alarm.status = 'ignored'
        uni.showToast({
          title: '已忽略',
          icon: 'success'
        })
      }
    }
  })
}

// 处理告警
const handleAlarm = (alarm) => {
  currentAlarm.value = {
    ...alarm,
    typeText: getTypeText(alarm.type)
  }
  selectedHandleMethod.value = ''
  handleRemark.value = ''
  followUpActions.value = []

  // 打开处理弹窗
  uni.$emit('openHandlePopup')
}

// 关闭处理弹窗
const closeHandlePopup = () => {
  uni.$emit('closeHandlePopup')
}

// 处理方式选择
const onHandleMethodChange = (e) => {
  selectedHandleMethod.value = handleMethods[e.detail.value]
}

// 后续措施选择
const onFollowUpChange = (e) => {
  followUpActions.value = e.detail.value
}

// 确认处理告警
const confirmHandleAlarm = () => {
  if (!selectedHandleMethod.value) {
    uni.showToast({
      title: '请选择处理方式',
      icon: 'none'
    })
    return
  }

  if (!handleRemark.value) {
    uni.showToast({
      title: '请填写处理说明',
      icon: 'none'
    })
    return
  }

  // 模拟API调用
  uni.showLoading({
    title: '处理中...'
  })

  setTimeout(() => {
    uni.hideLoading()

    // 更新告警状态
    const alarm = alarmList.value.find(a => a.alarmId === currentAlarm.value.alarmId)
    if (alarm) {
      alarm.status = 'handled'
      alarm.handlerName = '当前用户'
      alarm.handleTime = Date.now()
      alarm.handleRemark = handleRemark.value
    }

    closeHandlePopup()

    uni.showToast({
      title: '处理成功',
      icon: 'success'
    })

    // 刷新列表
    loadAlarmList(true)
  }, 1000)
}

// 批量处理告警
const batchHandleAlarms = () => {
  uni.showModal({
    title: '批量处理',
    content: `确认处理选中的 ${selectedAlarms.value.length} 条告警？`,
    success: (res) => {
      if (res.confirm) {
        // 模拟批量处理
        uni.showLoading({
          title: '处理中...'
        })

        setTimeout(() => {
          uni.hideLoading()

          // 更新告警状态
          selectedAlarms.value.forEach(alarmId => {
            const alarm = alarmList.value.find(a => a.alarmId === alarmId)
            if (alarm) {
              alarm.status = 'handled'
              alarm.handlerName = '当前用户'
              alarm.handleTime = Date.now()
              alarm.handleRemark = '批量处理'
            }
          })

          clearSelection()

          uni.showToast({
            title: '批量处理成功',
            icon: 'success'
          })

          loadAlarmList(true)
        }, 1000)
      }
    }
  })
}

// 清除选择
const clearSelection = () => {
  selectedAlarms.value = []
}

// 显示告警统计
const showAlarmStatistics = () => {
  uni.$emit('openStatisticsPopup')
}

// 关闭统计弹窗
const closeStatisticsPopup = () => {
  uni.$emit('closeStatisticsPopup')
}

// 工具方法
const getTypeIcon = (type) => {
  const icons = {
    intrusion: 'locked',
    lingering: 'clock',
    offline: 'cloud-download',
    damage: 'settings',
    tailgating: 'person'
  }
  return icons[type] || 'info'
}

const getTypeText = (type) => {
  const texts = {
    intrusion: '非法闯入',
    lingering: '长时间逗留',
    offline: '设备离线',
    damage: '暴力破坏',
    tailgating: '尾随检测'
  }
  return texts[type] || '未知'
}

const getPriorityText = (priority) => {
  const texts = {
    high: '高危',
    medium: '中等',
    low: '低级'
  }
  return texts[priority] || '未知'
}

const getStatusText = (status) => {
  const texts = {
    unhandled: '未处理',
    handled: '已处理',
    ignored: '已忽略'
  }
  return texts[status] || '未知'
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  } else if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  } else {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.alarm-page {
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
      .alarm-badge {
        position: relative;

        .badge-count {
          position: absolute;
          top: -8rpx;
          right: -8rpx;
          min-width: 32rpx;
          height: 32rpx;
          line-height: 32rpx;
          text-align: center;
          background: #ff4d4f;
          border-radius: 16rpx;
          font-size: 20rpx;
          color: #fff;
          padding: 0 8rpx;
        }
      }
    }
  }
}

.page-content {
  padding-top: calc(44px + var(--status-bar-height));
  padding-bottom: 120rpx;
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

    &.high-priority {
      background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    &.medium-priority {
      background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);

      .stat-icon {
        background: rgba(255, 255, 255, 0.3);
      }

      .stat-value,
      .stat-label {
        color: #fff;
      }
    }

    &.low-priority {
      background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);

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

    &.high.active {
      background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
    }

    &.medium.active {
      background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
    }

    &.low.active {
      background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    }
  }
}

.type-filter-row {
  padding: 0 30rpx 20rpx;

  .type-filter-scroll {
    white-space: nowrap;

    .type-filter-item {
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

.response-tip {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 0 30rpx 20rpx;
  padding: 20rpx 30rpx;
  background: #fff2f0;
  border-radius: 16rpx;
  border-left: 4rpx solid #ff6b6b;

  .tip-text {
    font-size: 26rpx;
    color: #ff6b6b;
  }
}

.alarm-list {
  height: calc(100vh - 44px - var(--status-bar-height) - 400rpx);
  padding: 0 30rpx;

  .alarm-item {
    background: #fff;
    border-radius: 24rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

    .alarm-header {
      display: flex;
      align-items: center;
      gap: 16rpx;
      margin-bottom: 20rpx;

      .alarm-type-badge {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;
        color: #fff;

        &.intrusion {
          background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
        }

        &.lingering {
          background: linear-gradient(135deg, #ffa940 0%, #ffc069 100%);
        }

        &.offline {
          background: linear-gradient(135deg, #ffec3d 0%, #ffe066 100%);
        }

        &.damage {
          background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        }

        &.tailgating {
          background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
        }

        .type-text {
          font-size: 24rpx;
        }
      }

      .priority-badge {
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;
        font-weight: 600;

        &.high {
          background: #fff2f0;
          color: #ff6b6b;
        }

        &.medium {
          background: #fff7e6;
          color: #ffa940;
        }

        &.low {
          background: #f6ffed;
          color: #52c41a;
        }
      }

      .alarm-status {
        margin-left: auto;
        padding: 8rpx 20rpx;
        border-radius: 40rpx;
        font-size: 24rpx;

        &.unhandled {
          background: #fff2f0;
          color: #ff6b6b;
        }

        &.handled {
          background: #f6ffed;
          color: #52c41a;
        }

        &.ignored {
          background: #f5f5f5;
          color: #999;
        }
      }
    }

    .alarm-content {
      .alarm-title {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 16rpx;
      }

      .alarm-desc {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
        margin-bottom: 20rpx;
      }

      .alarm-info-row {
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

      .alarm-time-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 20rpx;
        padding-top: 20rpx;
        border-top: 1rpx solid #f0f0f0;

        .time-item {
          display: flex;
          align-items: center;
          gap: 8rpx;

          .time-text {
            font-size: 24rpx;
            color: #999;

            &.overtime {
              color: #ff6b6b;
              font-weight: 600;
            }
          }
        }

        .response-time {
          .time-text {
            font-size: 24rpx;
            color: #999;
          }
        }
      }
    }

    .alarm-actions {
      display: flex;
      gap: 20rpx;
      margin-top: 20rpx;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;

      .action-btn {
        flex: 1;
        height: 70rpx;
        line-height: 70rpx;
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

    .alarm-result {
      margin-top: 20rpx;
      padding: 20rpx;
      background: #f6ffed;
      border-radius: 16rpx;

      .result-info {
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-bottom: 12rpx;

        .result-text {
          font-size: 26rpx;
          color: #52c41a;
        }
      }

      .result-remark {
        font-size: 26rpx;
        color: #666;
        line-height: 1.6;
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

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
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

  .batch-actions {
    display: flex;
    gap: 20rpx;

    .batch-btn {
      padding: 16rpx 40rpx;
      border-radius: 40rpx;
      font-size: 28rpx;

      &.secondary {
        background: #f5f5f5;
        color: #666;
      }

      &.danger {
        background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
        color: #fff;
      }
    }
  }
}

// 弹窗样式
.handle-popup,
.statistics-popup {
  background: #fff;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 80vh;
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

  .popup-content {
    padding: 30rpx;
    max-height: 60vh;
    overflow-y: auto;

    .alarm-detail-card {
      padding: 30rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      margin-bottom: 30rpx;

      .detail-row {
        display: flex;
        margin-bottom: 20rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .detail-label {
          min-width: 180rpx;
          font-size: 28rpx;
          color: #666;
        }

        .detail-value {
          flex: 1;
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }
      }
    }

    .form-section {
      margin-bottom: 30rpx;

      .form-label {
        margin-bottom: 20rpx;

        .label-text {
          font-size: 28rpx;
          color: #333;
          font-weight: 600;
        }

        .required {
          color: #ff6b6b;
          margin-left: 8rpx;
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

          &.placeholder {
            color: #999;
          }
        }
      }

      .textarea-input {
        width: 100%;
        min-height: 200rpx;
        padding: 24rpx 30rpx;
        background: #f5f5f5;
        border-radius: 16rpx;
        font-size: 28rpx;
        line-height: 1.6;
      }

      .char-count {
        text-align: right;
        margin-top: 12rpx;
        font-size: 24rpx;
        color: #999;
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
  border-radius: 32rpx;
  margin: 30rpx;
  max-height: 70vh;

  .statistics-content {
    padding: 30rpx;

    .stat-chart {
      margin-bottom: 40rpx;

      .chart-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 30rpx;
      }

      .chart-bars {
        display: flex;
        align-items: flex-end;
        gap: 12rpx;
        height: 300rpx;
        padding: 0 20rpx;

        .chart-bar {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          position: relative;

          .bar-fill {
            width: 100%;
            background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
            border-radius: 8rpx 8rpx 0 0;
            min-height: 20rpx;
          }

          .bar-label {
            margin-top: 12rpx;
            font-size: 20rpx;
            color: #999;
          }

          .bar-value {
            position: absolute;
            top: -30rpx;
            font-size: 20rpx;
            color: #666;
          }
        }
      }
    }

    .stat-summary {
      padding: 30rpx;
      background: #f5f7fa;
      border-radius: 16rpx;
      margin-bottom: 40rpx;

      .summary-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .summary-label {
          font-size: 28rpx;
          color: #666;
        }

        .summary-value {
          font-size: 28rpx;
          color: #333;
          font-weight: 600;

          &.success {
            color: #52c41a;
          }
        }
      }
    }

    .type-distribution {
      .distribution-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 30rpx;
      }

      .distribution-list {
        .distribution-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 24rpx;

          &:last-child {
            margin-bottom: 0;
          }

          .type-info {
            display: flex;
            align-items: center;
            gap: 16rpx;

            .type-dot {
              width: 24rpx;
              height: 24rpx;
              border-radius: 50%;
            }

            .type-name {
              font-size: 28rpx;
              color: #333;
            }
          }

          .type-stats {
            display: flex;
            gap: 24rpx;

            .type-count {
              font-size: 28rpx;
              color: #333;
              font-weight: 600;
            }

            .type-percent {
              font-size: 26rpx;
              color: #999;
            }
          }
        }
      }
    }
  }
}
</style>
