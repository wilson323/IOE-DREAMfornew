<template>
  <view class="door-control-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <uni-icons type="arrowleft" size="20" color="#333" @tap="goBack"></uni-icons>
        <text class="nav-title">门禁控制</text>
        <view class="nav-actions">
          <uni-icons type="more" size="20" color="#1890ff" @tap="showSettings"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 门禁状态卡片 -->
    <view class="door-status-card" :class="getDoorStatusClass()">
      <view class="status-header">
        <view class="door-icon-wrapper">
          <uni-icons
            :type="getDoorIcon(doorInfo.deviceType)"
            size="48"
            :color="getStatusColor()"
          ></uni-icons>
        </view>
        <view class="door-title-section">
          <text class="door-name">{{ doorInfo.deviceName }}</text>
          <text class="door-code">{{ doorInfo.deviceCode }}</text>
        </view>
      </view>

      <view class="status-main">
        <view class="status-indicator">
          <view class="indicator-dot" :class="getStatusClass()"></view>
          <text class="status-text">{{ getStatusText() }}</text>
        </view>
        <view class="location-info">
          <uni-icons type="home" size="16" color="#666"></uni-icons>
          <text>{{ doorInfo.areaName || '未分配区域' }}</text>
        </view>
      </view>

      <!-- 状态信息 -->
      <view class="status-details">
        <view class="detail-item">
          <text class="detail-label">当前状态</text>
          <text class="detail-value">{{ getDoorStateText() }}</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">今日通行</text>
          <text class="detail-value">{{ doorInfo.todayPassCount || 0 }}次</text>
        </view>
        <view class="detail-item">
          <text class="detail-label">在线状态</text>
          <text class="detail-value" :class="doorInfo.onlineStatus === 1 ? 'online' : 'offline'">
            {{ doorInfo.onlineStatus === 1 ? '在线' : '离线' }}
          </text>
        </view>
      </view>

      <!-- 故障提示 -->
      <view v-if="doorInfo.faultStatus === 1" class="fault-alert">
        <uni-icons type="alert" size="16" color="#ff4d4f"></uni-icons>
        <text class="fault-text">{{ doorInfo.faultMessage || '设备故障，请检查' }}</text>
      </view>
    </view>

    <!-- 快速控制 -->
    <view class="control-section">
      <view class="section-title">快速控制</view>
      <view class="control-buttons">
        <button
          class="control-btn open-btn"
          :disabled="doorInfo.onlineStatus !== 1"
          @tap="openDoor"
        >
          <view class="btn-icon">
            <uni-icons type="arrowright" size="28" color="#fff"></uni-icons>
          </view>
          <text class="btn-label">开门</text>
        </button>
        <button
          class="control-btn lock-btn"
          :disabled="doorInfo.onlineStatus !== 1"
          @tap="lockDoor"
        >
          <view class="btn-icon">
            <uni-icons type="locked" size="28" color="#fff"></uni-icons>
          </view>
          <text class="btn-label">锁定</text>
        </button>
        <button
          class="control-btn reset-btn"
          :disabled="doorInfo.onlineStatus !== 1"
          @tap="resetDoor"
        >
          <view class="btn-icon">
            <uni-icons type="refreshempty" size="28" color="#fff"></uni-icons>
          </view>
          <text class="btn-label">复位</text>
        </button>
        <button
          class="control-btn unlock-btn"
          :disabled="doorInfo.onlineStatus !== 1"
          @tap="unlockDoor"
        >
          <view class="btn-icon">
            <uni-icons type="unlock" size="28" color="#fff"></uni-icons>
          </view>
          <text class="btn-label">解锁</text>
        </button>
      </view>
    </view>

    <!-- 门禁设置 -->
    <view class="settings-section">
      <view class="section-title">
        <text>门禁设置</text>
        <uni-icons type="arrowright" size="16" color="#999" @tap="showSettings"></uni-icons>
      </view>
      <view class="settings-list">
        <view class="setting-item" @tap="toggleSetting('autoLock')">
          <view class="setting-left">
            <uni-icons type="loop" size="20" color="#1890ff"></uni-icons>
            <text class="setting-label">自动锁门</text>
          </view>
          <view class="setting-right">
            <text class="setting-value">{{ doorSettings.autoLock ? '已开启' : '已关闭' }}</text>
            <switch
              :checked="doorSettings.autoLock"
              color="#1890ff"
              @change="onSettingChange"
              data-setting="autoLock"
            />
          </view>
        </view>
        <view class="setting-item" @tap="toggleSetting('keepOpen')">
          <view class="setting-left">
            <uni-icons type="eye" size="20" color="#1890ff"></uni-icons>
            <text class="setting-label">常开模式</text>
          </view>
          <view class="setting-right">
            <text class="setting-value">{{ doorSettings.keepOpen ? '已开启' : '已关闭' }}</text>
            <switch
              :checked="doorSettings.keepOpen"
              color="#1890ff"
              @change="onSettingChange"
              data-setting="keepOpen"
            />
          </view>
        </view>
        <view class="setting-item" @tap="openDurationSetting">
          <view class="setting-left">
            <uni-icons type="calendar" size="20" color="#1890ff"></uni-icons>
            <text class="setting-label">开门时长</text>
          </view>
          <view class="setting-right">
            <text class="setting-value">{{ doorSettings.openDuration }}秒</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 今日通行记录 -->
    <view class="records-section">
      <view class="section-title">
        <text>今日通行</text>
        <text class="more-btn" @tap="viewAllRecords">查看全部</text>
      </view>
      <scroll-view class="records-list" scroll-y>
        <!-- 空状态 -->
        <view v-if="passRecords.length === 0" class="empty-records">
          <text>今日暂无通行记录</text>
        </view>

        <!-- 通行记录列表 -->
        <view v-for="record in passRecords" :key="record.recordId" class="record-item">
          <view class="record-avatar">
            <uni-icons type="person" size="20" color="#1890ff"></uni-icons>
          </view>
          <view class="record-info">
            <view class="record-header">
              <text class="record-name">{{ record.userName }}</text>
              <text class="record-time">{{ formatTime(record.passTime) }}</text>
            </view>
            <view class="record-details">
              <text class="record-type">{{ getPassTypeText(record.passType) }}</text>
              <text class="record-result" :class="record.passResult === 1 ? 'success' : 'fail'">
                {{ getPassResultText(record.passResult) }}
              </text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 事件日志 -->
    <view class="events-section">
      <view class="section-title">
        <text>事件日志</text>
        <text class="more-btn" @tap="viewAllEvents">查看全部</text>
      </view>
      <scroll-view class="events-list" scroll-y>
        <!-- 空状态 -->
        <view v-if="eventLogs.length === 0" class="empty-events">
          <text>暂无事件日志</text>
        </view>

        <!-- 事件日志列表 -->
        <view v-for="event in eventLogs" :key="event.eventId" class="event-item">
          <view class="event-time">{{ formatEventTime(event.eventTime) }}</view>
          <view class="event-content">
            <view class="event-icon" :class="getEventClass(event.eventType)">
              <uni-icons :type="getEventIcon(event.eventType)" size="16" color="#fff"></uni-icons>
            </view>
            <view class="event-info">
              <text class="event-text">{{ getEventText(event) }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 设置弹窗 -->
    <uni-popup ref="settingsPopup" type="bottom">
      <view class="settings-popup">
        <view class="popup-header">
          <text class="popup-title">门禁设置</text>
          <uni-icons type="close" size="20" color="#999" @tap="closeSettings"></uni-icons>
        </view>
        <view class="popup-content">
          <view class="setting-group">
            <view class="group-title">基本设置</view>
            <view class="setting-item">
              <text class="setting-label">开门时长</text>
              <picker
                mode="selector"
                :range="durationOptions"
                @change="onDurationChange"
              >
                <view class="picker-value">
                  <text>{{ doorSettings.openDuration }}秒</text>
                  <uni-icons type="arrowright" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="setting-item">
              <text class="setting-label">自动锁门</text>
              <switch
                :checked="doorSettings.autoLock"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="autoLock"
              />
            </view>
            <view class="setting-item">
              <text class="setting-label">常开模式</text>
              <switch
                :checked="doorSettings.keepOpen"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="keepOpen"
              />
            </view>
          </view>
          <view class="setting-group">
            <view class="group-title">安全设置</view>
            <view class="setting-item">
              <text class="setting-label">反潜回</text>
              <switch
                :checked="doorSettings.antiPassback"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="antiPassback"
              />
            </view>
            <view class="setting-item">
              <text class="setting-label">胁迫码报警</text>
              <switch
                :checked="doorSettings.duressCode"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="duressCode"
              />
            </view>
            <view class="setting-item">
              <text class="setting-label">异常开门报警</text>
              <switch
                :checked="doorSettings.abnormalAlarm"
                color="#1890ff"
                @change="onSettingChange"
                data-setting="abnormalAlarm"
              />
            </view>
          </view>
        </view>
        <view class="popup-footer">
          <button class="save-btn" @tap="saveSettings">保存设置</button>
        </view>
      </view>
    </uni-popup>

    <!-- 开门时长选择弹窗 -->
    <uni-popup ref="durationPopup" type="bottom">
      <view class="duration-popup">
        <view class="popup-header">
          <text class="popup-title">选择开门时长</text>
          <uni-icons type="close" size="20" color="#999" @tap="closeDurationSetting"></uni-icons>
        </view>
        <view class="duration-options">
          <view
            v-for="option in durationOptions"
            :key="option"
            class="duration-option"
            :class="{ active: doorSettings.openDuration === option }"
            @tap="selectDuration(option)"
          >
            <text>{{ option }}秒</text>
          </view>
        </view>
      </view>
    </uni-popup>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <uni-load-more status="loading" :content-text="loadingText"></uni-load-more>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { accessApi } from '@/api/business/access/access-api.js'

/**
 * 门禁单门控制详情页面
 * 功能：
 * 1. 门禁状态实时展示
 * 2. 快速控制（开门、锁定、复位、解锁）
 * 3. 门禁设置（自动锁门、常开模式、开门时长等）
 * 4. 今日通行记录展示
 * 5. 事件日志展示
 */

// ==================== 状态数据 ====================

const loading = ref(false)
const deviceId = ref('')

// 门禁信息
const doorInfo = ref({})

// 门禁设置
const doorSettings = ref({
  openDuration: 3,
  autoLock: true,
  keepOpen: false,
  antiPassback: true,
  duressCode: true,
  abnormalAlarm: true
})

// 通行记录
const passRecords = ref([])

// 事件日志
const eventLogs = ref([])

// 开门时长选项
const durationOptions = [3, 5, 10, 15, 30, 60]

// 弹窗引用
const settingsPopup = ref(null)
const durationPopup = ref(null)

// 加载文本
const loadingText = ref({
  contentdown: '上拉加载更多',
  contentrefresh: '加载中...',
  contentnomore: '没有更多数据了'
})

// 定时器
let statusTimer = null

// ==================== 生命周期 ====================

onMounted(() => {
  console.log('[门禁控制] 页面加载')

  // 获取设备ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.deviceId) {
    deviceId.value = options.deviceId
    loadDoorInfo()
    loadPassRecords()
    loadEventLogs()
    startStatusTimer()
  }
})

onUnmounted(() => {
  console.log('[门禁控制] 页面卸载')
  stopStatusTimer()
})

// ==================== 数据加载 ====================

/**
 * 加载门禁信息
 */
const loadDoorInfo = async () => {
  loading.value = true

  try {
    const result = await accessApi.getDeviceDetail(deviceId.value)

    if (result.success && result.data) {
      doorInfo.value = result.data
      loadDoorSettings()
    }
  } catch (error) {
    console.error('[门禁控制] 加载门禁信息失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 加载门禁设置
 */
const loadDoorSettings = async () => {
  try {
    const result = await accessApi.getDoorSettings(deviceId.value)

    if (result.success && result.data) {
      doorSettings.value = {
        ...doorSettings.value,
        ...result.data
      }
    }
  } catch (error) {
    console.error('[门禁控制] 加载门禁设置失败:', error)
  }
}

/**
 * 加载通行记录
 */
const loadPassRecords = async () => {
  try {
    const today = new Date()
    const startTime = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime()
    const endTime = today.getTime()

    const result = await accessApi.getPassRecords({
      deviceId: deviceId.value,
      startTime,
      endTime,
      pageNum: 1,
      pageSize: 10
    })

    if (result.success && result.data) {
      passRecords.value = result.data.list || []
    }
  } catch (error) {
    console.error('[门禁控制] 加载通行记录失败:', error)
  }
}

/**
 * 加载事件日志
 */
const loadEventLogs = async () => {
  try {
    const result = await accessApi.getDoorEvents({
      deviceId: deviceId.value,
      pageNum: 1,
      pageSize: 10
    })

    if (result.success && result.data) {
      eventLogs.value = result.data.list || []
    }
  } catch (error) {
    console.error('[门禁控制] 加载事件日志失败:', error)
  }
}

/**
 * 刷新状态
 */
const refreshStatus = () => {
  loadDoorInfo()
}

// ==================== 控制操作 ====================

/**
 * 开门
 */
const openDoor = async () => {
  if (doorInfo.value.onlineStatus !== 1) {
    uni.showToast({
      title: '设备离线，无法操作',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认开门',
    content: '确定要打开门禁吗？',
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '开门中...',
          mask: true
        })

        try {
          const result = await accessApi.remoteOpenDoor(deviceId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '开门成功',
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁控制] 开门失败:', error)
          uni.showToast({
            title: '开门失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 锁定
 */
const lockDoor = async () => {
  if (doorInfo.value.onlineStatus !== 1) {
    uni.showToast({
      title: '设备离线，无法操作',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认锁定',
    content: '确定要锁定门禁吗？',
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '锁定中...',
          mask: true
        })

        try {
          const result = await accessApi.remoteLockDoor(deviceId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '锁定成功',
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁控制] 锁定失败:', error)
          uni.showToast({
            title: '锁定失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 复位
 */
const resetDoor = async () => {
  if (doorInfo.value.onlineStatus !== 1) {
    uni.showToast({
      title: '设备离线，无法操作',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认复位',
    content: '确定要复位门禁吗？',
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '复位中...',
          mask: true
        })

        try {
          const result = await accessApi.resetDoor(deviceId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '复位成功',
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁控制] 复位失败:', error)
          uni.showToast({
            title: '复位失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 解锁
 */
const unlockDoor = async () => {
  if (doorInfo.value.onlineStatus !== 1) {
    uni.showToast({
      title: '设备离线，无法操作',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认解锁',
    content: '确定要解锁门禁吗？',
    confirmColor: '#1890ff',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '解锁中...',
          mask: true
        })

        try {
          const result = await accessApi.unlockDoor(deviceId.value)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({
              title: '解锁成功',
              icon: 'success'
            })

            setTimeout(() => {
              refreshStatus()
            }, 1000)
          }
        } catch (error) {
          uni.hideLoading()
          console.error('[门禁控制] 解锁失败:', error)
          uni.showToast({
            title: '解锁失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

// ==================== 设置操作 ====================

/**
 * 显示设置
 */
const showSettings = () => {
  settingsPopup.value?.open()
}

/**
 * 关闭设置
 */
const closeSettings = () => {
  settingsPopup.value?.close()
}

/**
 * 切换设置
 */
const toggleSetting = (setting) => {
  // 单击整个项也触发切换
  doorSettings.value[setting] = !doorSettings.value[setting]
  saveSettings()
}

/**
 * 设置变更
 */
const onSettingChange = (e) => {
  const setting = e.currentTarget.dataset.setting
  doorSettings.value[setting] = e.detail.value
}

/**
 * 开门时长设置
 */
const openDurationSetting = () => {
  durationPopup.value?.open()
}

/**
 * 关闭开门时长设置
 */
const closeDurationSetting = () => {
  durationPopup.value?.close()
}

/**
 * 开门时长变更
 */
const onDurationChange = (e) => {
  const index = e.detail.value
  doorSettings.value.openDuration = durationOptions[index]
}

/**
 * 选择开门时长
 */
const selectDuration = (duration) => {
  doorSettings.value.openDuration = duration
  closeDurationSetting()
  saveSettings()
}

/**
 * 保存设置
 */
const saveSettings = async () => {
  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    const result = await accessApi.updateDoorSettings(deviceId.value, doorSettings.value)

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
      closeSettings()
    }
  } catch (error) {
    uni.hideLoading()
    console.error('[门禁控制] 保存设置失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

// ==================== 导航操作 ====================

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 查看全部记录
 */
const viewAllRecords = () => {
  uni.navigateTo({
    url: `/pages/access/access-record?deviceId=${deviceId.value}`
  })
}

/**
 * 查看全部事件
 */
const viewAllEvents = () => {
  uni.navigateTo({
    url: `/pages/access/door-events?deviceId=${deviceId.value}`
  })
}

// ==================== 工具方法 ====================

/**
 * 获取状态样式类
 */
const getStatusClass = () => {
  if (doorInfo.value.faultStatus === 1) {
    return 'fault'
  }
  if (doorInfo.value.onlineStatus === 1) {
    return 'online'
  }
  return 'offline'
}

/**
 * 获取状态文本
 */
const getStatusText = () => {
  if (doorInfo.value.faultStatus === 1) {
    return '故障'
  }
  if (doorInfo.value.onlineStatus === 1) {
    return '正常'
  }
  return '离线'
}

/**
 * 获取门禁状态类
 */
const getDoorStatusClass = () => {
  if (doorInfo.value.faultStatus === 1) {
    return 'status-fault'
  }
  if (doorInfo.value.onlineStatus === 1) {
    return 'status-online'
  }
  return 'status-offline'
}

/**
 * 获取状态颜色
 */
const getStatusColor = () => {
  if (doorInfo.value.faultStatus === 1) {
    return '#ff4d4f'
  }
  if (doorInfo.value.onlineStatus === 1) {
    return '#52c41a'
  }
  return '#999'
}

/**
 * 获取门禁图标
 */
const getDoorIcon = (deviceType) => {
  const iconMap = {
    1: 'locked',
    11: 'locked',
    12: 'arrowright'
  }
  return iconMap[deviceType] || 'locked'
}

/**
 * 获取门状态文本
 */
const getDoorStateText = () => {
  if (doorInfo.value.doorStatus === 1) {
    return '开启'
  }
  return '关闭'
}

/**
 * 获取通行类型文本
 */
const getPassTypeText = (passType) => {
  const typeMap = {
    1: '人脸识别',
    2: '指纹',
    3: 'IC卡',
    4: '密码',
    5: '二维码',
    6: '远程'
  }
  return typeMap[passType] || '其他'
}

/**
 * 获取通行结果文本
 */
const getPassResultText = (passResult) => {
  if (passResult === 1) {
    return '通过'
  }
  return '拒绝'
}

/**
 * 格式化时间
 */
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * 获取事件类
 */
const getEventClass = (eventType) => {
  const classMap = {
    1: 'event-info',
    2: 'event-warning',
    3: 'event-error',
    4: 'event-success'
  }
  return classMap[eventType] || 'event-info'
}

/**
 * 获取事件图标
 */
const getEventIcon = (eventType) => {
  const iconMap = {
    1: 'info',
    2: 'alert',
    3: 'close',
    4: 'checkmarkempty'
  }
  return iconMap[eventType] || 'info'
}

/**
 * 获取事件文本
 */
const getEventText = (event) => {
  const textMap = {
    1: `门禁${event.eventAction}`,
    2: `异常告警：${event.eventMessage}`,
    3: `故障：${event.eventMessage}`,
    4: `操作成功：${event.eventAction}`
  }
  return textMap[event.eventType] || event.eventMessage || '未知事件'
}

/**
 * 格式化事件时间
 */
const formatEventTime = (timestamp) => {
  const date = new Date(timestamp)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

/**
 * 启动状态定时器
 */
const startStatusTimer = () => {
  statusTimer = setInterval(() => {
    refreshStatus()
  }, 10000) // 10秒刷新一次
}

/**
 * 停止状态定时器
 */
const stopStatusTimer = () => {
  if (statusTimer) {
    clearInterval(statusTimer)
    statusTimer = null
  }
}
</script>

<style lang="scss" scoped>
.door-control-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// 导航栏
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
  padding-bottom: 20rpx;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;

    .nav-title {
      flex: 1;
      text-align: center;
      font-size: 36rpx;
      font-weight: 600;
      color: #fff;
    }

    .nav-actions {
      width: 80rpx;
      display: flex;
      justify-content: flex-end;
    }
  }
}

// 门禁状态卡片
.door-status-card {
  margin: 30rpx;
  padding: 40rpx;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);

  &.status-online {
    border-top: 4rpx solid #52c41a;
  }

  &.status-offline {
    border-top: 4rpx solid #ff4d4f;
  }

  &.status-fault {
    border-top: 4rpx solid #fa8c16;
  }

  .status-header {
    display: flex;
    align-items: center;
    gap: 24rpx;
    margin-bottom: 30rpx;

    .door-icon-wrapper {
      width: 96rpx;
      height: 96rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 20rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .door-title-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8rpx;

      .door-name {
        font-size: 36rpx;
        font-weight: 600;
        color: #333;
      }

      .door-code {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .status-main {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .status-indicator {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .indicator-dot {
        width: 16rpx;
        height: 16rpx;
        border-radius: 50%;

        &.online {
          background: #52c41a;
          box-shadow: 0 0 12rpx rgba(82, 196, 26, 0.4);
        }

        &.offline {
          background: #ff4d4f;
        }

        &.fault {
          background: #fa8c16;
        }
      }

      .status-text {
        font-size: 28rpx;
        font-weight: 500;
        color: #333;
      }
    }

    .location-info {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 24rpx;
      color: #666;
    }
  }

  .status-details {
    display: flex;
    justify-content: space-around;
    padding: 24rpx 0;
    background: #f5f5f5;
    border-radius: 12rpx;

    .detail-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;

      .detail-label {
        font-size: 24rpx;
        color: #999;
      }

      .detail-value {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;

        &.online {
          color: #52c41a;
        }

        &.offline {
          color: #ff4d4f;
        }
      }
    }
  }

  .fault-alert {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 20rpx;
    padding: 16rpx;
    background: rgba(255, 77, 79, 0.08);
    border-radius: 8rpx;

    .fault-text {
      flex: 1;
      font-size: 24rpx;
      color: #ff4d4f;
    }
  }
}

// 控制区域
.control-section {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .section-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;
  }

  .control-buttons {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;

    .control-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      padding: 24rpx 0;
      background: #f5f5f5;
      border-radius: 16rpx;
      border: none;

      .btn-icon {
        width: 56rpx;
        height: 56rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
      }

      .btn-label {
        font-size: 24rpx;
        color: #666;
      }

      &.open-btn {
        .btn-icon {
          background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
        }
      }

      &.lock-btn {
        .btn-icon {
          background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
        }
      }

      &.reset-btn {
        .btn-icon {
          background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
        }
      }

      &.unlock-btn {
        .btn-icon {
          background: linear-gradient(135deg, #722ed1 0%, #531dab 100%);
        }
      }

      &[disabled] {
        opacity: 0.4;
      }
    }
  }
}

// 设置区域
.settings-section {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;

    .more-btn {
      font-size: 26rpx;
      color: #1890ff;
      font-weight: 400;
    }
  }

  .settings-list {
    .setting-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 24rpx 0;
      border-bottom: 1rpx solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .setting-left {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .setting-label {
          font-size: 28rpx;
          color: #333;
        }
      }

      .setting-right {
        display: flex;
        align-items: center;
        gap: 16rpx;

        .setting-value {
          font-size: 26rpx;
          color: #999;
        }
      }
    }
  }
}

// 通行记录区域
.records-section {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;

    .more-btn {
      font-size: 26rpx;
      color: #1890ff;
      font-weight: 400;
    }
  }

  .records-list {
    max-height: 500rpx;

    .empty-records {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 80rpx 0;
      font-size: 28rpx;
      color: #999;
    }

    .record-item {
      display: flex;
      align-items: center;
      gap: 20rpx;
      padding: 24rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      margin-bottom: 16rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .record-avatar {
        width: 64rpx;
        height: 64rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        background: #e6f7ff;
      }

      .record-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 8rpx;

        .record-header {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .record-name {
            font-size: 28rpx;
            font-weight: 500;
            color: #333;
          }

          .record-time {
            font-size: 24rpx;
            color: #999;
          }
        }

        .record-details {
          display: flex;
          align-items: center;
          gap: 16rpx;

          .record-type {
            font-size: 24rpx;
            color: #666;
          }

          .record-result {
            font-size: 24rpx;
            font-weight: 500;

            &.success {
              color: #52c41a;
            }

            &.fail {
              color: #ff4d4f;
            }
          }
        }
      }
    }
  }
}

// 事件日志区域
.events-section {
  margin: 0 30rpx 30rpx;
  padding: 30rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 24rpx;

    .more-btn {
      font-size: 26rpx;
      color: #1890ff;
      font-weight: 400;
    }
  }

  .events-list {
    max-height: 400rpx;

    .empty-events {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 80rpx 0;
      font-size: 28rpx;
      color: #999;
    }

    .event-item {
      display: flex;
      flex-direction: column;
      gap: 12rpx;

      .event-time {
        font-size: 24rpx;
        color: #999;
        align-self: flex-start;
      }

      .event-content {
        display: flex;
        align-items: flex-start;
        gap: 16rpx;

        .event-icon {
          width: 40rpx;
          height: 40rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          flex-shrink: 0;

          &.event-info {
            background: #1890ff;
          }

          &.event-warning {
            background: #fa8c16;
          }

          &.event-error {
            background: #ff4d4f;
          }

          &.event-success {
            background: #52c41a;
          }
        }

        .event-info {
          flex: 1;

          .event-text {
            font-size: 26rpx;
            color: #333;
            line-height: 1.5;
          }
        }
      }
    }
  }
}

// 设置弹窗
.settings-popup {
  background: #fff;
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
  }

  .popup-content {
    padding: 30rpx;
    max-height: 60vh;
    overflow-y: auto;

    .setting-group {
      margin-bottom: 40rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .group-title {
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 20rpx;
      }

      .setting-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 24rpx 0;
        border-bottom: 1rpx solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .setting-label {
          font-size: 28rpx;
          color: #333;
        }

        .picker-value {
          display: flex;
          align-items: center;
          gap: 8rpx;
          font-size: 28rpx;
          color: #666;
        }
      }
    }
  }

  .popup-footer {
    padding: 30rpx;
    border-top: 1rpx solid #f0f0f0;

    .save-btn {
      width: 100%;
      height: 88rpx;
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
      font-size: 32rpx;
      font-weight: 600;
      border-radius: 12rpx;
      border: none;
    }
  }
}

// 开门时长选择弹窗
.duration-popup {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;

  .popup-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30rpx;

    .popup-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }
  }

  .duration-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20rpx;

    .duration-option {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 88rpx;
      background: #f5f5f5;
      border-radius: 12rpx;
      font-size: 28rpx;
      color: #666;
      border: 2rpx solid transparent;

      &.active {
        background: #e6f7ff;
        color: #1890ff;
        border-color: #1890ff;
      }
    }
  }
}

// 加载容器
.loading-container {
  padding: 60rpx 0;
}
</style>
