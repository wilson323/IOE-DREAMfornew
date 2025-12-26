<template>
  <view class="device-export-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">导出设备</view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view class="main-scroll" scroll-y>
      <!-- 导出格式选择 -->
      <view class="export-section">
        <view class="section-header">
          <text class="section-title">导出格式</text>
          <text class="section-desc">选择文件导出格式</text>
        </view>

        <view class="format-selector">
          <view
            v-for="format in exportFormats"
            :key="format.value"
            class="format-item"
            :class="{ active: exportFormat === format.value }"
            @tap="selectFormat(format.value)"
          >
            <view class="format-icon" :style="{ background: format.gradient }">
              <uni-icons :type="format.icon" size="24" color="#ffffff"></uni-icons>
            </view>
            <text class="format-name">{{ format.label }}</text>
            <view v-if="exportFormat === format.value" class="format-check">
              <uni-icons type="checkbox-filled" size="18" color="#667eea"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 导出范围选择 -->
      <view class="export-section">
        <view class="section-header">
          <text class="section-title">导出范围</text>
          <text class="section-desc">选择要导出的设备</text>
        </view>

        <view class="range-selector">
          <view
            v-for="range in exportRanges"
            :key="range.value"
            class="range-item"
            :class="{ active: exportRange === range.value }"
            @tap="selectRange(range.value)"
          >
            <view class="range-radio">
              <view v-if="exportRange === range.value" class="radio-dot"></view>
            </view>
            <text class="range-name">{{ range.label }}</text>
          </view>
        </view>

        <!-- 设备列表（当选中"选中设备"时显示） -->
        <view class="selected-devices" v-if="exportRange === 'selected' && selectedDevices.length > 0">
          <view class="devices-count">
            <text class="count-text">已选择 {{ selectedDevices.length }} 台设备</text>
          </view>
          <view class="devices-list">
            <view
              v-for="device in selectedDevices.slice(0, 3)"
              :key="device.deviceId"
              class="device-chip"
            >
              <text class="chip-text">{{ device.deviceName }}</text>
            </view>
            <view v-if="selectedDevices.length > 3" class="device-chip">
              <text class="chip-text">+{{ selectedDevices.length - 3 }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 导出字段选择 -->
      <view class="export-section">
        <view class="section-header">
          <view class="header-left">
            <text class="section-title">导出字段</text>
            <text class="section-desc">选择要包含的字段</text>
          </view>
          <view class="select-all-btn" @tap="toggleSelectAll">
            <text class="select-text">{{ isAllFieldsSelected ? '取消全选' : '全选' }}</text>
          </view>
        </view>

        <view class="fields-grid">
          <view
            v-for="field in exportFields"
            :key="field.value"
            class="field-item"
            :class="{ selected: selectedFields.includes(field.value) }"
            @tap="toggleField(field.value)"
          >
            <view class="field-checkbox">
              <uni-icons
                v-if="selectedFields.includes(field.value)"
                type="checkbox-filled"
                size="18"
                color="#667eea"
              ></uni-icons>
              <uni-icons v-else type="circle" size="18" color="#CCCCCC"></uni-icons>
            </view>
            <text class="field-name">{{ field.label }}</text>
          </view>
        </view>
      </view>

      <!-- 导出预览 -->
      <view class="export-section">
        <view class="section-header">
          <text class="section-title">导出预览</text>
        </view>

        <view class="preview-card">
          <view class="preview-info">
            <text class="preview-title">预计导出</text>
            <text class="preview-count">{{ previewCount }} 条记录</text>
          </view>
          <view class="preview-detail">
            <text class="detail-item">格式：{{ getFormatLabel(exportFormat) }}</text>
            <text class="detail-item">字段：{{ selectedFields.length }} 个</text>
          </view>
        </view>
      </view>

      <!-- 导出操作 -->
      <view class="export-action">
        <button class="btn-export" @tap="startExport" :disabled="exporting">
          {{ exporting ? '导出中...' : '开始导出' }}
        </button>
      </view>

      <!-- 导出进度 -->
      <view class="export-progress" v-if="exporting">
        <view class="progress-info">
          <text class="progress-text">{{ exportStatus.text }}</text>
          <text class="progress-percent">{{ exportStatus.percent }}%</text>
        </view>
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: exportStatus.percent + '%' }"></view>
        </view>
      </view>

      <!-- 导出历史 -->
      <view class="export-section" v-if="exportHistory.length > 0">
        <view class="section-header">
          <text class="section-title">导出历史</text>
          <text class="view-all" @tap="viewAllHistory">查看全部</text>
        </view>

        <view class="history-list">
          <view
            v-for="history in exportHistory.slice(0, 3)"
            :key="history.id"
            class="history-item"
            @tap="openExportedFile(history)"
          >
            <view class="history-icon" :style="{ background: history.gradient }">
              <uni-icons :type="history.icon" size="20" color="#ffffff"></uni-icons>
            </view>
            <view class="history-info">
              <text class="history-filename">{{ history.fileName }}</text>
              <text class="history-time">{{ history.exportTime }}</text>
            </view>
            <view class="history-size">
              <text class="size-text">{{ history.fileSize }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'
import dayjs from 'dayjs'

// 页面参数
const deviceId = ref('')
const selectedDevices = ref([])

// 导出格式
const exportFormat = ref('excel')
const exportFormats = [
  { label: 'Excel', value: 'excel', icon: 'paperplane', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { label: 'CSV', value: 'csv', icon: 'list', gradient: 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)' },
  { label: 'PDF', value: 'pdf', icon: 'email', gradient: 'linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%)' }
]

// 导出范围
const exportRange = ref('all')
const exportRanges = [
  { label: '全部设备', value: 'all' },
  { label: '选中设备', value: 'selected' },
  { label: '筛选结果', value: 'filtered' }
]

// 导出字段
const selectedFields = ref(['deviceName', 'deviceCode', 'deviceType', 'areaName', 'ipAddress', 'onlineStatus'])
const exportFields = [
  { label: '设备名称', value: 'deviceName' },
  { label: '设备编码', value: 'deviceCode' },
  { label: '设备类型', value: 'deviceType' },
  { label: '所在区域', value: 'areaName' },
  { label: 'IP地址', value: 'ipAddress' },
  { label: 'MAC地址', value: 'macAddress' },
  { label: '固件版本', value: 'firmwareVersion' },
  { label: '在线状态', value: 'onlineStatus' },
  { label: '安装位置', value: 'installLocation' },
  { label: '创建时间', value: 'createTime' }
]

// 导出状态
const exporting = ref(false)
const exportStatus = reactive({
  text: '准备中...',
  percent: 0
})

// 导出历史
const exportHistory = ref([
  {
    id: 1,
    fileName: '设备列表_20250130.xlsx',
    format: 'excel',
    exportTime: '2025-01-30 14:30',
    fileSize: '125 KB',
    icon: 'paperplane',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    id: 2,
    fileName: '设备列表_20250129.csv',
    format: 'csv',
    exportTime: '2025-01-29 10:15',
    fileSize: '98 KB',
    icon: 'list',
    gradient: 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)'
  }
])

// 计算属性
const previewCount = computed(() => {
  if (exportRange.value === 'all') {
    return 156 // 假设总数
  } else if (exportRange.value === 'selected') {
    return selectedDevices.value.length
  } else {
    return 42 // 筛选结果
  }
})

const isAllFieldsSelected = computed(() => {
  return selectedFields.value.length === exportFields.length
})

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  deviceId.value = options.deviceId || ''

  // 如果有选中设备
  if (options.selectedDevices) {
    try {
      selectedDevices.value = JSON.parse(decodeURIComponent(options.selectedDevices))
      exportRange.value = 'selected'
    } catch (e) {
      console.error('解析选中设备失败:', e)
    }
  }
})

// 选择导出格式
const selectFormat = (format) => {
  exportFormat.value = format
}

// 选择导出范围
const selectRange = (range) => {
  exportRange.value = range
}

// 切换字段选择
const toggleField = (field) => {
  const index = selectedFields.value.indexOf(field)
  if (index > -1) {
    if (selectedFields.value.length > 1) {
      selectedFields.value.splice(index, 1)
    } else {
      uni.showToast({
        title: '至少选择一个字段',
        icon: 'none'
      })
    }
  } else {
    selectedFields.value.push(field)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (isAllFieldsSelected.value) {
    selectedFields.value = ['deviceName', 'deviceCode']
  } else {
    selectedFields.value = exportFields.map(f => f.value)
  }
}

// 获取格式标签
const getFormatLabel = (format) => {
  const item = exportFormats.find(f => f.value === format)
  return item ? item.label.toUpperCase() : 'EXCEL'
}

// 开始导出
const startExport = async () => {
  if (selectedFields.value.length === 0) {
    uni.showToast({
      title: '请至少选择一个字段',
      icon: 'none'
    })
    return
  }

  exporting.value = true
  exportStatus.percent = 0
  exportStatus.text = '准备中...'

  try {
    // 模拟导出进度
    const steps = [
      { text: '收集数据...', percent: 20 },
      { text: '处理数据...', percent: 40 },
      { text: '生成文件...', percent: 70 },
      { text: '完成导出...', percent: 100 }
    ]

    for (const step of steps) {
      await new Promise(resolve => setTimeout(resolve, 800))
      exportStatus.text = step.text
      exportStatus.percent = step.percent
    }

    // 添加到导出历史
    const formatLabel = getFormatLabel(exportFormat.value)
    const fileName = `设备列表_${dayjs().format('YYYYMMDD_HHmm')}.${exportFormat.value}`

    exportHistory.value.unshift({
      id: Date.now(),
      fileName,
      format: exportFormat.value,
      exportTime: dayjs().format('YYYY-MM-DD HH:mm'),
      fileSize: `${Math.floor(Math.random() * 200 + 50)} KB`,
      icon: exportFormats.find(f => f.value === exportFormat.value).icon,
      gradient: exportFormats.find(f => f.value === exportFormat.value).gradient
    })

    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('导出失败:', error)
    uni.showToast({
      title: '导出失败',
      icon: 'none'
    })
  } finally {
    setTimeout(() => {
      exporting.value = false
    }, 1000)
  }
}

// 打开已导出的文件
const openExportedFile = (history) => {
  uni.showModal({
    title: '打开文件',
    content: `文件名：${history.fileName}\n导出时间：${history.exportTime}\n文件大小：${history.fileSize}\n\n是否打开该文件？`,
    confirmText: '打开',
    confirmColor: '#667eea',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '打开文件功能开发中',
          icon: 'none'
        })
      }
    }
  })
}

// 查看全部历史
const viewAllHistory = () => {
  uni.showToast({
    title: '导出历史功能开发中',
    icon: 'none'
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-export-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-top: var(--status-bar-height);
  padding-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  height: 88rpx;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.back-text {
  font-size: 28rpx;
  color: #ffffff;
}

.navbar-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.navbar-right {
  width: 80rpx;
  height: 60rpx;
}

// 主内容区域
.main-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 108rpx);
  padding-bottom: 30rpx;
}

// 导出区块
.export-section {
  margin: 20rpx 30rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.section-desc {
  font-size: 24rpx;
  color: #999999;
}

.select-all-btn {
  padding: 8rpx 16rpx;
  background: #f5f7fa;
  border-radius: 16rpx;
}

.select-text {
  font-size: 22rpx;
  color: #667eea;
}

// 格式选择器
.format-selector {
  display: flex;
  gap: 20rpx;
}

.format-item {
  flex: 1;
  background: #f5f7fa;
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  border: 2rpx solid transparent;
  position: relative;
  transition: all 0.3s;
}

.format-item.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.format-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.format-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333333;
}

.format-check {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
}

// 范围选择器
.range-selector {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.range-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.range-item.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.range-radio {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border-radius: 50%;
  border: 2rpx solid #e0e0e0;
}

.radio-dot {
  width: 20rpx;
  height: 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
}

.range-name {
  font-size: 28rpx;
  color: #333333;
}

.range-item.active .range-name {
  color: #667eea;
  font-weight: 600;
}

// 选中设备列表
.selected-devices {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
}

.devices-count {
  margin-bottom: 16rpx;
}

.count-text {
  font-size: 24rpx;
  color: #666666;
}

.devices-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.device-chip {
  padding: 8rpx 16rpx;
  background: #ffffff;
  border-radius: 16rpx;
  border: 1rpx solid #e0e0e0;
}

.chip-text {
  font-size: 22rpx;
  color: #667eea;
}

// 字段网格
.fields-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.field-item.selected {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.field-checkbox {
  flex-shrink: 0;
}

.field-name {
  font-size: 26rpx;
  color: #333333;
}

.field-item.selected .field-name {
  color: #667eea;
  font-weight: 600;
}

// 导出预览
.preview-card {
  padding: 24rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
}

.preview-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.preview-title {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.preview-count {
  font-size: 32rpx;
  font-weight: 700;
  color: #ffffff;
}

.preview-detail {
  display: flex;
  gap: 24rpx;
}

.detail-item {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}

// 导出操作
.export-action {
  margin: 30rpx;
}

.btn-export {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.4);

  &[disabled] {
    opacity: 0.6;
  }
}

// 导出进度
.export-progress {
  margin: 0 30rpx 20rpx;
  padding: 30rpx;
  background: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.progress-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.progress-text {
  font-size: 26rpx;
  color: #333333;
}

.progress-percent {
  font-size: 32rpx;
  font-weight: 700;
  color: #667eea;
}

.progress-bar {
  height: 12rpx;
  background: #f0f0f0;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.3);
}

// 导出历史
.view-all {
  font-size: 24rpx;
  color: #667eea;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #f5f7fa;
  border-radius: 12rpx;

  &:active {
    transform: scale(0.98);
  }
}

.history-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.history-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.history-filename {
  font-size: 26rpx;
  color: #333333;
  font-weight: 600;
}

.history-time {
  font-size: 22rpx;
  color: #999999;
}

.history-size {
  flex-shrink: 0;
}

.size-text {
  font-size: 22rpx;
  color: #999999;
}
</style>
