<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px', height: (statusBarHeight + 44) + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">
          <text class="title-text">导出记录</text>
        </view>
        <view class="navbar-right">
          <view class="history-btn" @tap="showExportHistory">
            <uni-icons type="list" size="18" color="#fff"></uni-icons>
            <text class="history-text">历史</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 页面内容 -->
    <scroll-view class="page-content" scroll-y enable-back-to-top>
      <!-- 导出类型选择 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="doc" size="18" color="#667eea"></uni-icons>
          <text class="section-title">导出类型</text>
        </view>
        <view class="export-type-grid">
          <view
            class="type-item"
            :class="{ active: selectedExportType === 'excel' }"
            @tap="selectExportType('excel')"
          >
            <view class="type-icon excel">
              <uni-icons type="paperplane" size="32" color="#fff"></uni-icons>
            </view>
            <text class="type-name">Excel表格</text>
            <text class="type-desc">.xlsx格式</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedExportType === 'pdf' }"
            @tap="selectExportType('pdf')"
          >
            <view class="type-icon pdf">
              <uni-icons type="paperplane" size="32" color="#fff"></uni-icons>
            </view>
            <text class="type-name">PDF文档</text>
            <text class="type-desc">.pdf格式</text>
          </view>
          <view
            class="type-item"
            :class="{ active: selectedExportType === 'csv' }"
            @tap="selectExportType('csv')"
          >
            <view class="type-icon csv">
              <uni-icons type="paperplane" size="32" color="#fff"></uni-icons>
            </view>
            <text class="type-name">CSV文件</text>
            <text class="type-desc">.csv格式</text>
          </view>
        </view>
      </view>

      <!-- 时间范围选择 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="calendar" size="18" color="#667eea"></uni-icons>
          <text class="section-title">时间范围</text>
        </view>
        <view class="time-presets">
          <view
            class="preset-chip"
            :class="{ active: selectedTimePreset === 'today' }"
            @tap="selectTimePreset('today')"
          >
            今天
          </view>
          <view
            class="preset-chip"
            :class="{ active: selectedTimePreset === 'yesterday' }"
            @tap="selectTimePreset('yesterday')"
          >
            昨天
          </view>
          <view
            class="preset-chip"
            :class="{ active: selectedTimePreset === 'week' }"
            @tap="selectTimePreset('week')"
          >
            本周
          </view>
          <view
            class="preset-chip"
            :class="{ active: selectedTimePreset === 'month' }"
            @tap="selectTimePreset('month')"
          >
            本月
          </view>
          <view
            class="preset-chip"
            :class="{ active: selectedTimePreset === 'custom' }"
            @tap="selectTimePreset('custom')"
          >
            自定义
          </view>
        </view>
        <view class="custom-time-range" v-if="selectedTimePreset === 'custom'">
          <view class="time-field">
            <text class="field-label">开始时间</text>
            <picker mode="date" :value="startTime" @change="onStartTimeChange">
              <view class="picker-value">
                <text>{{ startTime || '请选择开始日期' }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
          <view class="time-separator">至</view>
          <view class="time-field">
            <text class="field-label">结束时间</text>
            <picker mode="date" :value="endTime" @change="onEndTimeChange">
              <view class="picker-value">
                <text>{{ endTime || '请选择结束日期' }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
        <view class="time-summary" v-if="selectedTimePreset !== 'custom'">
          <text class="summary-label">导出范围：</text>
          <text class="summary-value">{{ getTimeRangeText() }}</text>
        </view>
      </view>

      <!-- 导出字段选择 -->
      <view class="section-card">
        <view class="section-header" @tap="toggleFieldSection">
          <uni-icons type="list" size="18" color="#667eea"></uni-icons>
          <text class="section-title">导出字段</text>
          <view class="field-count">
            <text>{{ selectedFields.length }}/{{ exportFields.length }}</text>
          </view>
          <uni-icons :type="showFieldSection ? 'up' : 'down'" size="16" color="#999"></uni-icons>
        </view>
        <view class="field-options" v-if="showFieldSection">
          <view class="field-group">
            <view class="group-header">
              <checkbox
                :checked="isGroupSelected('basic')"
                @tap="toggleGroup('basic')"
                color="#667eea"
              />
              <text class="group-title">基本信息</text>
            </view>
            <view class="field-list">
              <view
                class="field-item"
                v-for="field in basicFields"
                :key="field.key"
              >
                <checkbox
                  :checked="selectedFields.includes(field.key)"
                  @tap="toggleField(field.key)"
                  color="#667eea"
                />
                <text class="field-label">{{ field.label }}</text>
              </view>
            </view>
          </view>
          <view class="field-group">
            <view class="group-header">
              <checkbox
                :checked="isGroupSelected('pass')"
                @tap="toggleGroup('pass')"
                color="#667eea"
              />
              <text class="group-title">通行信息</text>
            </view>
            <view class="field-list">
              <view
                class="field-item"
                v-for="field in passFields"
                :key="field.key"
              >
                <checkbox
                  :checked="selectedFields.includes(field.key)"
                  @tap="toggleField(field.key)"
                  color="#667eea"
                />
                <text class="field-label">{{ field.label }}</text>
              </view>
            </view>
          </view>
          <view class="field-group">
            <view class="group-header">
              <checkbox
                :checked="isGroupSelected('media')"
                @tap="toggleGroup('media')"
                color="#667eea"
              />
              <text class="group-title">媒体信息</text>
            </view>
            <view class="field-list">
              <view
                class="field-item"
                v-for="field in mediaFields"
                :key="field.key"
              >
                <checkbox
                  :checked="selectedFields.includes(field.key)"
                  @tap="toggleField(field.key)"
                  color="#667eea"
                />
                <text class="field-label">{{ field.label }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 筛选条件 -->
      <view class="section-card">
        <view class="section-header" @tap="toggleFilterSection">
          <uni-icons type="filter" size="18" color="#667eea"></uni-icons>
          <text class="section-title">筛选条件</text>
          <text class="filter-tip" v-if="hasActiveFilters()">已设置</text>
          <uni-icons :type="showFilterSection ? 'up' : 'down'" size="16" color="#999"></uni-icons>
        </view>
        <view class="filter-options" v-if="showFilterSection">
          <!-- 状态筛选 -->
          <view class="filter-row">
            <text class="filter-label">通行状态</text>
            <view class="status-filters">
              <view
                class="status-chip"
                :class="{ active: exportFilters.status.includes('success') }"
                @tap="toggleStatusFilter('success')"
              >
                <view class="chip-dot success"></view>
                <text>成功</text>
              </view>
              <view
                class="status-chip"
                :class="{ active: exportFilters.status.includes('denied') }"
                @tap="toggleStatusFilter('denied')"
              >
                <view class="chip-dot denied"></view>
                <text>拒绝</text>
              </view>
              <view
                class="status-chip"
                :class="{ active: exportFilters.status.includes('alarm') }"
                @tap="toggleStatusFilter('alarm')"
              >
                <view class="chip-dot alarm"></view>
                <text>告警</text>
              </view>
            </view>
          </view>

          <!-- 区域筛选 -->
          <view class="filter-row">
            <text class="filter-label">区域</text>
            <picker mode="selector" :range="areaList" range-key="areaName" @change="onAreaChange">
              <view class="picker-input">
                <text>{{ selectedArea ? selectedArea.areaName : '请选择区域' }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 设备筛选 -->
          <view class="filter-row">
            <text class="filter-label">设备</text>
            <picker mode="selector" :range="deviceList" range-key="deviceName" @change="onDeviceChange">
              <view class="picker-input">
                <text>{{ selectedDevice ? selectedDevice.deviceName : '请选择设备' }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 认证方式 -->
          <view class="filter-row">
            <text class="filter-label">认证方式</text>
            <picker mode="selector" :range="verificationMethods" range-key="label" @change="onVerificationChange">
              <view class="picker-input">
                <text>{{ selectedVerification ? selectedVerification.label : '全部方式' }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 导出模板 -->
      <view class="section-card">
        <view class="section-header">
          <uni-icons type="compose" size="18" color="#667eea"></uni-icons>
          <text class="section-title">导出模板</text>
        </view>
        <view class="template-list">
          <view
            class="template-item"
            :class="{ active: selectedTemplate === 'default' }"
            @tap="selectTemplate('default')"
          >
            <view class="template-info">
              <uni-icons type="checkmarkempty" size="20" color="#667eea" v-if="selectedTemplate === 'default'"></uni-icons>
              <text class="template-name">标准模板</text>
            </view>
            <text class="template-desc">包含所有常用字段</text>
          </view>
          <view
            class="template-item"
            :class="{ active: selectedTemplate === 'simple' }"
            @tap="selectTemplate('simple')"
          >
            <view class="template-info">
              <uni-icons type="checkmarkempty" size="20" color="#667eea" v-if="selectedTemplate === 'simple'"></uni-icons>
              <text class="template-name">简化模板</text>
            </view>
            <text class="template-desc">仅包含核心信息</text>
          </view>
          <view
            class="template-item"
            :class="{ active: selectedTemplate === 'detailed' }"
            @tap="selectTemplate('detailed')"
          >
            <view class="template-info">
              <uni-icons type="checkmarkempty" size="20" color="#667eea" v-if="selectedTemplate === 'detailed'"></uni-icons>
              <text class="template-name">详细模板</text>
            </view>
            <text class="template-desc">包含所有字段和详细信息</text>
          </view>
        </view>
      </view>

      <!-- 数据预览 -->
      <view class="section-card" v-if="previewRecords.length > 0">
        <view class="section-header" @tap="togglePreview">
          <uni-icons type="eye" size="18" color="#667eea"></uni-icons>
          <text class="section-title">数据预览</text>
          <text class="preview-count">{{ previewRecords.length }}条</text>
          <uni-icons :type="showPreview ? 'up' : 'down'" size="16" color="#999"></uni-icons>
        </view>
        <view class="preview-table" v-if="showPreview">
          <view class="table-header">
            <text class="header-cell">姓名</text>
            <text class="header-cell">状态</text>
            <text class="header-cell">时间</text>
          </view>
          <view class="table-row" v-for="(record, index) in previewRecords.slice(0, 5)" :key="index">
            <text class="table-cell">{{ record.personName }}</text>
            <view class="table-cell status-cell">
              <view class="status-badge" :class="record.status">
                <text>{{ getStatusText(record.status) }}</text>
              </view>
            </view>
            <text class="table-cell">{{ formatTime(record.passTime) }}</text>
          </view>
          <view class="preview-more" v-if="previewRecords.length > 5">
            <text>还有 {{ previewRecords.length - 5 }} 条数据...</text>
          </view>
        </view>
      </view>

      <!-- 底部按钮 -->
      <view class="bottom-actions">
        <view class="action-btn secondary" @tap="resetExport">
          <text>重置</text>
        </view>
        <view class="action-btn primary" @tap="startExport">
          <uni-icons type="paperplane" size="18" color="#fff"></uni-icons>
          <text>开始导出</text>
        </view>
      </view>
    </scroll-view>

    <!-- 导出进度弹窗 -->
    <uni-popup ref="progressPopup" type="center" :mask-click="false">
      <view class="progress-popup">
        <view class="progress-header">
          <text class="header-title">正在导出</text>
        </view>
        <view class="progress-content">
          <view class="progress-icon">
            <uni-icons type="spinner-cycle" size="48" color="#667eea"></uni-icons>
          </view>
          <text class="progress-text">{{ exportProgress.text }}</text>
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: exportProgress.percent + '%' }"></view>
          </view>
          <text class="progress-percent">{{ exportProgress.percent }}%</text>
        </view>
      </view>
    </uni-popup>

    <!-- 导出成功弹窗 -->
    <uni-popup ref="successPopup" type="center">
      <view class="success-popup">
        <view class="success-icon">
          <uni-icons type="checkmarkempty" size="60" color="#4CAF50"></uni-icons>
        </view>
        <text class="success-title">导出成功</text>
        <text class="success-desc">共导出 {{ exportResult.totalCount }} 条记录</text>
        <view class="success-actions">
          <view class="success-btn secondary" @tap="closeSuccessPopup">
            <text>关闭</text>
          </view>
          <view class="success-btn primary" @tap="shareFile">
            <uni-icons type="redo" size="16" color="#fff"></uni-icons>
            <text>分享</text>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 导出历史弹窗 -->
    <uni-popup ref="historyPopup" type="right">
      <view class="history-popup">
        <view class="history-header">
          <text class="history-title">导出历史</text>
          <view class="close-btn" @tap="closeHistoryPopup">
            <uni-icons type="close" size="20" color="#fff"></uni-icons>
          </view>
        </view>
        <scroll-view class="history-list" scroll-y>
          <view class="history-item" v-for="(item, index) in exportHistory" :key="index">
            <view class="history-info">
              <view class="history-top">
                <text class="history-type">{{ getExportTypeText(item.type) }}</text>
                <text class="history-time">{{ formatHistoryTime(item.time) }}</text>
              </view>
              <text class="history-desc">{{ item.recordCount }}条记录 · {{ getTimeRangeText(item) }}</text>
            </view>
            <view class="history-actions">
              <view class="history-action" @tap="downloadHistory(item)">
                <uni-icons type="download" size="16" color="#667eea"></uni-icons>
                <text>下载</text>
              </view>
              <view class="history-action" @tap="shareHistory(item)">
                <uni-icons type="redo" size="16" color="#667eea"></uni-icons>
                <text>分享</text>
              </view>
            </view>
          </view>
          <view class="empty-history" v-if="exportHistory.length === 0">
            <text>暂无导出历史</text>
          </view>
        </scroll-view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 导出类型
const selectedExportType = ref('excel')

// 时间范围
const selectedTimePreset = ref('today')
const startTime = ref('')
const endTime = ref('')

// 导出字段
const showFieldSection = ref(true)
const selectedFields = ref(['personName', 'phone', 'passTime', 'status', 'deviceName', 'areaName'])

const exportFields = [
  // 基本信息字段
  { key: 'personName', label: '姓名', group: 'basic' },
  { key: 'phone', label: '手机号', group: 'basic' },
  { key: 'employeeId', label: '工号', group: 'basic' },
  { key: 'department', label: '部门', group: 'basic' },
  { key: 'personType', label: '人员类型', group: 'basic' },

  // 通行信息字段
  { key: 'passTime', label: '通行时间', group: 'pass' },
  { key: 'status', label: '通行状态', group: 'pass' },
  { key: 'direction', label: '方向', group: 'pass' },
  { key: 'verificationMethod', label: '认证方式', group: 'pass' },
  { key: 'deviceName', label: '设备名称', group: 'pass' },
  { key: 'areaName', label: '区域', group: 'pass' },

  // 媒体信息字段
  { key: 'snapshotUrl', label: '抓拍照片', group: 'media' },
  { key: 'videoUrl', label: '视频片段', group: 'media' },
]

const basicFields = computed(() => exportFields.filter(f => f.group === 'basic'))
const passFields = computed(() => exportFields.filter(f => f.group === 'pass'))
const mediaFields = computed(() => exportFields.filter(f => f.group === 'media'))

// 筛选条件
const showFilterSection = ref(true)
const exportFilters = reactive({
  status: ['success', 'denied', 'alarm'],
  areaId: null,
  deviceId: null,
  verificationMethod: null,
})

const areaList = ref([
  { areaId: 1, areaName: 'A栋1楼大厅' },
  { areaId: 2, areaName: 'A栋2楼办公区' },
  { areaId: 3, areaName: 'B栋1楼大厅' },
])

const deviceList = ref([
  { deviceId: 1, deviceName: '主入口门禁-1' },
  { deviceId: 2, deviceName: '主入口门禁-2' },
  { deviceId: 3, deviceName: '办公区门禁-1' },
])

const verificationMethods = [
  { value: 'all', label: '全部方式' },
  { value: 'face', label: '人脸识别' },
  { value: 'fingerprint', label: '指纹识别' },
  { value: 'card', label: '刷卡' },
  { value: 'qrcode', label: '二维码' },
  { value: 'password', label: '密码' },
]

const selectedArea = ref(null)
const selectedDevice = ref(null)
const selectedVerification = ref(null)

// 导出模板
const selectedTemplate = ref('default')

// 数据预览
const showPreview = ref(false)
const previewRecords = ref([])

// 导出进度
const exportProgress = reactive({
  percent: 0,
  text: '准备导出...',
})

// 导出结果
const exportResult = reactive({
  totalCount: 0,
  fileUrl: '',
})

// 导出历史
const exportHistory = ref([
  {
    id: 1,
    type: 'excel',
    recordCount: 156,
    startTime: '2025-01-20',
    endTime: '2025-01-20',
    time: new Date(Date.now() - 3600000),
    fileUrl: '',
  },
  {
    id: 2,
    type: 'pdf',
    recordCount: 342,
    startTime: '2025-01-19',
    endTime: '2025-01-19',
    time: new Date(Date.now() - 86400000),
    fileUrl: '',
  },
])

// 弹窗引用
const progressPopup = ref(null)
const successPopup = ref(null)
const historyPopup = ref(null)

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 选择导出类型
const selectExportType = (type) => {
  selectedExportType.value = type
}

// 选择时间预设
const selectTimePreset = (preset) => {
  selectedTimePreset.value = preset
  if (preset !== 'custom') {
    loadPreviewData()
  }
}

// 获取时间范围文本
const getTimeRangeText = (item) => {
  if (item) {
    return `${item.startTime} 至 ${item.endTime}`
  }

  const now = new Date()
  switch (selectedTimePreset.value) {
    case 'today':
      return formatDate(now)
    case 'yesterday': {
      const yesterday = new Date(now.getTime() - 86400000)
      return formatDate(yesterday)
    }
    case 'week': {
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - now.getDay())
      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 6)
      return `${formatDate(weekStart)} 至 ${formatDate(weekEnd)}`
    }
    case 'month': {
      const monthStart = new Date(now.getFullYear(), now.getMonth(), 1)
      const monthEnd = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      return `${formatDate(monthStart)} 至 ${formatDate(monthEnd)}`
    }
    case 'custom':
      return startTime.value && endTime.value ? `${startTime.value} 至 ${endTime.value}` : '请选择时间范围'
    default:
      return ''
  }
}

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 开始时间变更
const onStartTimeChange = (e) => {
  startTime.value = e.detail.value
  loadPreviewData()
}

// 结束时间变更
const onEndTimeChange = (e) => {
  endTime.value = e.detail.value
  loadPreviewData()
}

// 切换字段区域显示
const toggleFieldSection = () => {
  showFieldSection.value = !showFieldSection.value
}

// 切换筛选区域显示
const toggleFilterSection = () => {
  showFilterSection.value = !showFilterSection.value
}

// 切换预览显示
const togglePreview = () => {
  showPreview.value = !showPreview.value
}

// 切换字段选择
const toggleField = (fieldKey) => {
  const index = selectedFields.value.indexOf(fieldKey)
  if (index > -1) {
    selectedFields.value.splice(index, 1)
  } else {
    selectedFields.value.push(fieldKey)
  }
}

// 判断组是否全部选中
const isGroupSelected = (group) => {
  const groupFields = exportFields.filter(f => f.group === group)
  return groupFields.every(f => selectedFields.value.includes(f.key))
}

// 切换组选择
const toggleGroup = (group) => {
  const groupFields = exportFields.filter(f => f.group === group)
  const allSelected = isGroupSelected(group)

  if (allSelected) {
    // 取消全选
    selectedFields.value = selectedFields.value.filter(f => !groupFields.map(gf => gf.key).includes(f))
  } else {
    // 全选
    groupFields.forEach(f => {
      if (!selectedFields.value.includes(f.key)) {
        selectedFields.value.push(f.key)
      }
    })
  }
}

// 切换状态筛选
const toggleStatusFilter = (status) => {
  const index = exportFilters.status.indexOf(status)
  if (index > -1) {
    exportFilters.status.splice(index, 1)
  } else {
    exportFilters.status.push(status)
  }
  loadPreviewData()
}

// 区域变更
const onAreaChange = (e) => {
  const index = e.detail.value
  selectedArea.value = areaList.value[index]
  exportFilters.areaId = selectedArea.value?.areaId
  loadPreviewData()
}

// 设备变更
const onDeviceChange = (e) => {
  const index = e.detail.value
  selectedDevice.value = deviceList.value[index]
  exportFilters.deviceId = selectedDevice.value?.deviceId
  loadPreviewData()
}

// 认证方式变更
const onVerificationChange = (e) => {
  const index = e.detail.value
  selectedVerification.value = verificationMethods[index]
  exportFilters.verificationMethod = selectedVerification.value?.value
  loadPreviewData()
}

// 判断是否有活动筛选
const hasActiveFilters = () => {
  return exportFilters.status.length < 3 ||
         exportFilters.areaId ||
         exportFilters.deviceId ||
         exportFilters.verificationMethod
}

// 选择模板
const selectTemplate = (template) => {
  selectedTemplate.value = template
  // 根据模板预设字段
  if (template === 'simple') {
    selectedFields.value = ['personName', 'passTime', 'status', 'deviceName', 'areaName']
  } else if (template === 'detailed') {
    selectedFields.value = exportFields.map(f => f.key)
  } else {
    selectedFields.value = ['personName', 'phone', 'passTime', 'status', 'deviceName', 'areaName', 'direction', 'verificationMethod']
  }
}

// 加载预览数据
const loadPreviewData = () => {
  // 模拟加载预览数据
  setTimeout(() => {
    previewRecords.value = [
      {
        personName: '张三',
        passTime: '2025-01-20 08:30:15',
        status: 'success',
      },
      {
        personName: '李四',
        passTime: '2025-01-20 08:35:22',
        status: 'success',
      },
      {
        personName: '王五',
        passTime: '2025-01-20 08:40:33',
        status: 'denied',
      },
      {
        personName: '赵六',
        passTime: '2025-01-20 08:45:44',
        status: 'alarm',
      },
      {
        personName: '钱七',
        passTime: '2025-01-20 08:50:55',
        status: 'success',
      },
    ]
  }, 500)
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.substring(11, 19) // 只显示时分秒
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    success: '成功',
    denied: '拒绝',
    alarm: '告警',
  }
  return statusMap[status] || '未知'
}

// 重置导出
const resetExport = () => {
  selectedExportType.value = 'excel'
  selectedTimePreset.value = 'today'
  startTime.value = ''
  endTime.value = ''
  selectedFields.value = ['personName', 'phone', 'passTime', 'status', 'deviceName', 'areaName']
  exportFilters.status = ['success', 'denied', 'alarm']
  exportFilters.areaId = null
  exportFilters.deviceId = null
  exportFilters.verificationMethod = null
  selectedArea.value = null
  selectedDevice.value = null
  selectedVerification.value = null
  selectedTemplate.value = 'default'
  previewRecords.value = []
}

// 开始导出
const startExport = () => {
  // 显示进度弹窗
  progressPopup.value?.open()

  // 模拟导出进度
  let progress = 0
  const timer = setInterval(() => {
    progress += 10
    exportProgress.percent = progress

    if (progress <= 20) {
      exportProgress.text = '正在收集数据...'
    } else if (progress <= 50) {
      exportProgress.text = '正在生成文件...'
    } else if (progress <= 80) {
      exportProgress.text = '正在优化文件...'
    } else {
      exportProgress.text = '即将完成...'
    }

    if (progress >= 100) {
      clearInterval(timer)
      exportResult.totalCount = Math.floor(Math.random() * 500) + 100

      setTimeout(() => {
        progressPopup.value?.close()
        successPopup.value?.open()

        // 添加到导出历史
        exportHistory.value.unshift({
          id: Date.now(),
          type: selectedExportType.value,
          recordCount: exportResult.totalCount,
          startTime: selectedTimePreset.value === 'custom' ? startTime.value : formatDate(new Date()),
          endTime: selectedTimePreset.value === 'custom' ? endTime.value : formatDate(new Date()),
          time: new Date(),
          fileUrl: '',
        })
      }, 500)
    }
  }, 300)
}

// 关闭成功弹窗
const closeSuccessPopup = () => {
  successPopup.value?.close()
}

// 分享文件
const shareFile = () => {
  uni.showShareMenu({
    withShareTicket: true,
  })
}

// 显示导出历史
const showExportHistory = () => {
  historyPopup.value?.open()
}

// 关闭导出历史
const closeHistoryPopup = () => {
  historyPopup.value?.close()
}

// 获取导出类型文本
const getExportTypeText = (type) => {
  const typeMap = {
    excel: 'Excel表格',
    pdf: 'PDF文档',
    csv: 'CSV文件',
  }
  return typeMap[type] || type
}

// 格式化历史时间
const formatHistoryTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  } else {
    return formatDate(date)
  }
}

// 下载历史文件
const downloadHistory = (item) => {
  uni.showToast({
    title: '开始下载',
    icon: 'success',
  })
}

// 分享历史文件
const shareHistory = (item) => {
  uni.showShareMenu({
    withShareTicket: true,
  })
}

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

onMounted(() => {
  getStatusBarHeight()
  loadPreviewData()
})
</script>

<style lang="scss" scoped>
.page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #e4e8ec 100%);
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
      gap: 8rpx;

      .back-text {
        font-size: 28rpx;
        color: #fff;
      }
    }

    .navbar-title {
      flex: 1;
      text-align: center;

      .title-text {
        font-size: 32rpx;
        font-weight: 600;
        color: #fff;
      }
    }

    .navbar-right {
      width: 100rpx;
      display: flex;
      justify-content: flex-end;

      .history-btn {
        display: flex;
        align-items: center;
        gap: 4rpx;

        .history-text {
          font-size: 24rpx;
          color: #fff;
        }
      }
    }
  }
}

.page-content {
  padding-top: calc(var(--status-bar-height) + 44px);
  padding-bottom: 180rpx;
  min-height: 100vh;
}

.section-card {
  background: #fff;
  margin: 20rpx 30rpx;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  .section-header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 30rpx;

    .section-title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 600;
      color: #333;
    }

    .field-count,
    .preview-count {
      font-size: 24rpx;
      color: #667eea;
      font-weight: 600;
    }

    .filter-tip {
      font-size: 24rpx;
      color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
    }
  }
}

// 导出类型选择
.export-type-grid {
  display: flex;
  gap: 20rpx;

  .type-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    padding: 30rpx 20rpx;
    border-radius: 16rpx;
    border: 2rpx solid #e5e7eb;
    transition: all 0.3s;

    &.active {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
    }

    .type-icon {
      width: 96rpx;
      height: 96rpx;
      border-radius: 20rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.excel {
        background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
      }

      &.pdf {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.csv {
        background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      }
    }

    .type-name {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }

    .type-desc {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 时间预设
.time-presets {
  display: flex;
  gap: 16rpx;
  margin-bottom: 30rpx;
  flex-wrap: wrap;

  .preset-chip {
    padding: 16rpx 32rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    color: #666;
    background: #f5f7fa;
    border: 2rpx solid transparent;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      border-color: #667eea;
    }
  }
}

.custom-time-range {
  display: flex;
  align-items: center;
  gap: 20rpx;

  .time-field {
    flex: 1;

    .field-label {
      display: block;
      font-size: 26rpx;
      color: #999;
      margin-bottom: 16rpx;
    }

    .picker-value {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20rpx 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
    }
  }

  .time-separator {
    font-size: 28rpx;
    color: #999;
    padding-top: 40rpx;
  }
}

.time-summary {
  padding: 20rpx 24rpx;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;

  .summary-label {
    font-size: 28rpx;
    color: #666;
  }

  .summary-value {
    font-size: 28rpx;
    color: #667eea;
    font-weight: 600;
  }
}

// 字段选择
.field-options {
  .field-group {
    margin-bottom: 30rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .group-header {
      display: flex;
      align-items: center;
      gap: 16rpx;
      margin-bottom: 20rpx;

      .group-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .field-list {
      display: flex;
      flex-direction: column;
      gap: 16rpx;
      padding-left: 44rpx;

      .field-item {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .field-label {
          font-size: 28rpx;
          color: #666;
        }
      }
    }
  }
}

// 筛选条件
.filter-options {
  .filter-row {
    margin-bottom: 30rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .filter-label {
      display: block;
      font-size: 28rpx;
      color: #333;
      margin-bottom: 20rpx;
    }

    .status-filters {
      display: flex;
      gap: 16rpx;

      .status-chip {
        display: flex;
        align-items: center;
        gap: 8rpx;
        padding: 16rpx 24rpx;
        border-radius: 40rpx;
        font-size: 26rpx;
        color: #666;
        background: #f5f7fa;
        border: 2rpx solid transparent;
        transition: all 0.3s;

        &.active {
          border-color: #667eea;
        }

        .chip-dot {
          width: 12rpx;
          height: 12rpx;
          border-radius: 50%;

          &.success {
            background: #22c55e;
          }

          &.denied {
            background: #ef4444;
          }

          &.alarm {
            background: #f59e0b;
          }
        }
      }
    }

    .picker-input {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 24rpx;
      background: #f5f7fa;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #333;
    }
  }
}

// 导出模板
.template-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;

  .template-item {
    padding: 30rpx;
    border-radius: 16rpx;
    border: 2rpx solid #e5e7eb;
    transition: all 0.3s;

    &.active {
      border-color: #667eea;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
    }

    .template-info {
      display: flex;
      align-items: center;
      gap: 12rpx;
      margin-bottom: 12rpx;

      .template-name {
        font-size: 30rpx;
        font-weight: 600;
        color: #333;
      }
    }

    .template-desc {
      font-size: 26rpx;
      color: #999;
      padding-left: 32rpx;
    }
  }
}

// 数据预览
.preview-table {
  .table-header {
    display: flex;
    padding: 20rpx 0;
    border-bottom: 2rpx solid #e5e7eb;

    .header-cell {
      flex: 1;
      font-size: 26rpx;
      font-weight: 600;
      color: #666;
      text-align: center;

      &:first-child {
        text-align: left;
        flex: 1.5;
      }
    }
  }

  .table-row {
    display: flex;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    .table-cell {
      flex: 1;
      font-size: 26rpx;
      color: #333;
      text-align: center;

      &:first-child {
        text-align: left;
        flex: 1.5;
      }

      &.status-cell {
        display: flex;
        align-items: center;
        justify-content: center;

        .status-badge {
          padding: 8rpx 16rpx;
          border-radius: 8rpx;
          font-size: 22rpx;

          &.success {
            background: rgba(34, 197, 94, 0.1);
            color: #22c55e;
          }

          &.denied {
            background: rgba(239, 68, 68, 0.1);
            color: #ef4444;
          }

          &.alarm {
            background: rgba(245, 158, 11, 0.1);
            color: #f59e0b;
          }
        }
      }
    }
  }

  .preview-more {
    padding: 20rpx 0;
    text-align: center;
    font-size: 26rpx;
    color: #999;
  }
}

// 底部操作按钮
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);

  .action-btn {
    flex: 1;
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    border-radius: 16rpx;
    font-size: 30rpx;
    font-weight: 600;
    transition: all 0.3s;

    &.secondary {
      background: #f5f7fa;
      color: #666;
    }

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
    }
  }
}

// 进度弹窗
.progress-popup {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;

  .progress-header {
    text-align: center;
    margin-bottom: 40rpx;

    .header-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .progress-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 24rpx;

    .progress-icon {
      animation: rotate 1s linear infinite;
    }

    @keyframes rotate {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
      }
    }

    .progress-text {
      font-size: 28rpx;
      color: #666;
    }

    .progress-bar {
      width: 100%;
      height: 12rpx;
      background: #e5e7eb;
      border-radius: 6rpx;
      overflow: hidden;

      .progress-fill {
        height: 100%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        transition: width 0.3s;
      }
    }

    .progress-percent {
      font-size: 48rpx;
      font-weight: 600;
      color: #667eea;
    }
  }
}

// 成功弹窗
.success-popup {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;

  .success-icon {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: rgba(76, 175, 80, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .success-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }

  .success-desc {
    font-size: 28rpx;
    color: #666;
  }

  .success-actions {
    display: flex;
    gap: 20rpx;
    width: 100%;
    margin-top: 20rpx;

    .success-btn {
      flex: 1;
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      border-radius: 16rpx;
      font-size: 28rpx;
      font-weight: 600;

      &.secondary {
        background: #f5f7fa;
        color: #666;
      }

      &.primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

// 历史弹窗
.history-popup {
  width: 600rpx;
  height: 100vh;
  background: #fff;

  .history-header {
    position: relative;
    padding: 40rpx 30rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;

    .history-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #fff;
    }

    .close-btn {
      position: absolute;
      right: 30rpx;
      top: 50%;
      transform: translateY(-50%);
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 50%;
    }
  }

  .history-list {
    height: calc(100vh - 120rpx);
    padding: 20rpx 30rpx;

    .history-item {
      padding: 30rpx;
      border-radius: 16rpx;
      background: #f5f7fa;
      margin-bottom: 20rpx;

      .history-info {
        margin-bottom: 20rpx;

        .history-top {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12rpx;

          .history-type {
            font-size: 28rpx;
            font-weight: 600;
            color: #333;
          }

          .history-time {
            font-size: 24rpx;
            color: #999;
          }
        }

        .history-desc {
          font-size: 26rpx;
          color: #666;
        }
      }

      .history-actions {
        display: flex;
        gap: 20rpx;

        .history-action {
          flex: 1;
          height: 64rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8rpx;
          border-radius: 12rpx;
          background: #fff;
          font-size: 26rpx;
          color: #667eea;
          border: 2rpx solid #667eea;
        }
      }
    }

    .empty-history {
      padding: 100rpx 0;
      text-align: center;
      font-size: 28rpx;
      color: #999;
    }
  }
}
</style>
