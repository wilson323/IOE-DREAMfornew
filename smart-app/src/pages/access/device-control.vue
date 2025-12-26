<template>
  <view class="device-control-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">远程控制</view>
      <view class="nav-right"></view>
    </view>

    <!-- 设备信息 -->
    <view class="device-info-card">
      <view class="device-header">
        <view class="device-name">{{ deviceInfo.deviceName }}</view>
        <view :class="['status-badge', deviceInfo.online ? 'online' : 'offline']">
          <text class="status-text">{{ deviceInfo.online ? '在线' : '离线' }}</text>
        </view>
      </view>
      <view class="device-meta">
        <text class="meta-text">{{ deviceInfo.areaName || '未分配区域' }}</text>
      </view>
    </view>

    <!-- 控制模式选择 -->
    <view class="control-mode-section" v-if="controlAction === 'select'">
      <view class="section-title">选择控制模式</view>
      <view class="mode-grid">
        <view
          class="mode-item"
          :class="{ active: selectedMode === 'single' }"
          @click="selectMode('single')"
        >
          <view class="mode-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <uni-icons type="locked" size="28" color="#fff"></uni-icons>
          </view>
          <text class="mode-title">单次开门</text>
          <text class="mode-desc">远程开门一次</text>
        </view>
        <view
          class="mode-item"
          :class="{ active: selectedMode === 'keep-open' }"
          @click="selectMode('keep-open')"
        >
          <view class="mode-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <uni-icons type="unlock" size="28" color="#fff"></uni-icons>
          </view>
          <text class="mode-title">常开</text>
          <text class="mode-desc">保持常开状态</text>
        </view>
        <view
          class="mode-item"
          :class="{ active: selectedMode === 'keep-closed' }"
          @click="selectMode('keep-closed')"
        >
          <view class="mode-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <uni-icons type="close" size="28" color="#fff"></uni-icons>
          </view>
          <text class="mode-title">常闭</text>
          <text class="mode-desc">保持常闭状态</text>
        </view>
      </view>
    </view>

    <!-- 门选择 -->
    <view class="door-select-section" v-if="selectedMode && controlAction === 'select'">
      <view class="section-title">选择门</view>
      <view class="door-list">
        <view
          class="door-item"
          v-for="door in doors"
          :key="door.doorId"
          :class="{ selected: selectedDoorId === door.doorId }"
          @click="selectDoor(door)"
        >
          <view class="door-info">
            <view class="door-name-row">
              <text class="door-name">{{ door.doorName }}</text>
              <uni-icons
                v-if="selectedDoorId === door.doorId"
                type="checkbox-filled"
                size="20"
                color="#1890ff"
              ></uni-icons>
              <uni-icons
                v-else
                type="circle"
                size="20"
                color="#d9d9d9"
              ></uni-icons>
            </view>
            <text class="door-location">{{ door.location || '未设置位置' }}</text>
          </view>
          <view :class="['door-status', door.open ? 'open' : 'closed']">
            <text class="door-status-text">{{ door.open ? '开启' : '关闭' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 原因输入 -->
    <view class="reason-section" v-if="selectedDoorId">
      <view class="section-title required">操作原因</view>
      <view class="reason-input">
        <textarea
          class="textarea"
          placeholder="请输入操作原因（必填）"
          v-model="reason"
          maxlength="200"
        />
        <view class="char-count">{{ reason.length }}/200</view>
      </view>
    </view>

    <!-- 执行按钮 -->
    <view class="action-section" v-if="selectedDoorId && reason">
      <button class="action-btn" @click="executeControl" :disabled="executing">
        <uni-icons v-if="executing" type="spinner-cycle" size="18" color="#fff"></uni-icons>
        <text class="btn-text">{{ executing ? '执行中...' : getButtonText() }}</text>
      </button>
    </view>

    <!-- 常用操作 -->
    <view class="common-actions-section">
      <view class="section-title">常用操作</view>
      <view class="action-list">
        <view class="action-row" @click="rebootDevice">
          <view class="action-row-left">
            <uni-icons type="refreshempty" size="20" color="#1890ff"></uni-icons>
            <text class="action-row-text">重启设备</text>
          </view>
          <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
        </view>
        <view class="action-row" @click="syncTime">
          <view class="action-row-left">
            <uni-icons type="calendar" size="20" color="#1890ff"></uni-icons>
            <text class="action-row-text">同步时间</text>
          </view>
          <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
        </view>
        <view class="action-row" @click="clearCommands">
          <view class="action-row-left">
            <uni-icons type="trash" size="20" color="#1890ff"></uni-icons>
            <text class="action-row-text">清除命令</text>
          </view>
          <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
        </view>
        <view class="action-row" @click="toggleDeviceStatus">
          <view class="action-row-left">
            <uni-icons :type="deviceInfo.enabled ? 'eye-slash' : 'eye'" size="20" color="#1890ff"></uni-icons>
            <text class="action-row-text">{{ deviceInfo.enabled ? '禁用设备' : '启用设备' }}</text>
          </view>
          <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 操作日志 -->
    <view class="log-section" v-if="operationLogs.length > 0">
      <view class="section-title">
        <text>操作日志</text>
        <text class="clear-log" @click="clearLogs">清空</text>
      </view>
      <view class="log-list">
        <view class="log-item" v-for="(log, index) in operationLogs" :key="index">
          <view class="log-header">
            <text class="log-action">{{ getActionLabel(log.action) }}</text>
            <text class="log-time">{{ formatTime(log.timestamp) }}</text>
          </view>
          <view class="log-result" :class="log.success ? 'success' : 'fail'">
            <text>{{ log.result }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 设备信息
const deviceInfo = ref({})
const deviceId = ref(null)

// 控制动作
const controlAction = ref('select') // select, single, keep-open, keep-closed

// 控制模式
const selectedMode = ref(null) // single, keep-open, keep-closed

// 门列表
const doors = ref([])
const selectedDoorId = ref(null)

// 原因
const reason = ref('')

// 执行状态
const executing = ref(false)

// 操作日志
const operationLogs = ref([])

// 页面生命周期
onLoad((options) => {
  if (options.id) {
    deviceId.value = parseInt(options.id)
    if (options.action) {
      controlAction.value = options.action
      if (options.action !== 'select') {
        selectedMode.value = options.action
      }
    }
    loadDeviceInfo()
  }
})

/**
 * 加载设备信息
 */
const loadDeviceInfo = async () => {
  uni.showLoading({
    title: '加载中...',
    mask: true
  })

  try {
    const result = await accessApi.getDeviceDetail(deviceId.value)

    uni.hideLoading()

    if (result.success && result.data) {
      deviceInfo.value = result.data.device || {}
      doors.value = result.data.doors || []

      // 如果只有一门，自动选中
      if (doors.value.length === 1) {
        selectedDoorId.value = doors.value[0].doorId
      }
    } else {
      uni.showToast({
        title: result.message || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('加载设备信息失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 选择控制模式
 */
const selectMode = (mode) => {
  selectedMode.value = mode
  selectedDoorId.value = null
}

/**
 * 选择门
 */
const selectDoor = (door) => {
  selectedDoorId.value = door.doorId
}

/**
 * 获取按钮文本
 */
const getButtonText = () => {
  const modeTexts = {
    'single': '远程开门',
    'keep-open': '设为常开',
    'keep-closed': '设为常闭'
  }
  return modeTexts[selectedMode.value] || '执行'
}

/**
 * 获取操作标签
 */
const getActionLabel = (action) => {
  const labels = {
    'single': '远程开门',
    'keep-open': '设为常开',
    'keep-closed': '设为常闭',
    'reboot': '重启设备',
    'sync-time': '同步时间',
    'clear': '清除命令',
    'enable': '启用设备',
    'disable': '禁用设备'
  }
  return labels[action] || action
}

/**
 * 执行控制
 */
const executeControl = async () => {
  // 验证原因
  if (!reason.value || reason.value.trim().length === 0) {
    uni.showToast({
      title: '请输入操作原因',
      icon: 'none'
    })
    return
  }

  executing.value = true

  try {
    let result
    const doorId = selectedDoorId.value

    // 根据模式执行不同的控制操作
    switch (selectedMode.value) {
      case 'single':
        result = await accessApi.remoteOpenDoor(doorId, reason.value)
        break
      case 'keep-open':
        result = await accessApi.remoteKeepOpen(doorId, reason.value)
        break
      case 'keep-closed':
        result = await accessApi.remoteKeepClosed(doorId, reason.value)
        break
      default:
        throw new Error('未知的控制模式')
    }

    if (result.success) {
      uni.showToast({
        title: '操作成功',
        icon: 'success'
      })

      // 记录日志
      operationLogs.value.unshift({
        action: selectedMode.value,
        doorId: doorId,
        reason: reason.value,
        result: '操作成功',
        success: true,
        timestamp: new Date().getTime()
      })

      // 刷新设备信息
      setTimeout(() => {
        loadDeviceInfo()
      }, 1000)
    } else {
      uni.showToast({
        title: result.message || '操作失败',
        icon: 'none'
      })

      // 记录失败日志
      operationLogs.value.unshift({
        action: selectedMode.value,
        doorId: doorId,
        reason: reason.value,
        result: result.message || '操作失败',
        success: false,
        timestamp: new Date().getTime()
      })
    }
  } catch (error) {
    console.error('执行控制失败:', error)
    uni.showToast({
      title: '操作失败，请稍后重试',
      icon: 'none'
    })

    // 记录失败日志
    operationLogs.value.unshift({
      action: selectedMode.value,
      doorId: selectedDoorId.value,
      reason: reason.value,
      result: '操作失败',
      success: false,
      timestamp: new Date().getTime()
    })
  } finally {
    executing.value = false
  }
}

/**
 * 重启设备
 */
const rebootDevice = () => {
  uni.showModal({
    title: '确认重启',
    content: '确定要重启此设备吗？重启过程中设备将无法正常工作。',
    success: async (res) => {
      if (res.confirm) {
        executing.value = true

        try {
          const result = await accessApi.deviceControl(deviceId.value, 'reboot')

          if (result.success) {
            uni.showToast({
              title: '重启成功',
              icon: 'success'
            })

            operationLogs.value.unshift({
              action: 'reboot',
              result: '重启成功',
              success: true,
              timestamp: new Date().getTime()
            })
          } else {
            uni.showToast({
              title: result.message || '重启失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('重启设备失败:', error)
          uni.showToast({
            title: '重启失败',
            icon: 'none'
          })
        } finally {
          executing.value = false
        }
      }
    }
  })
}

/**
 * 同步时间
 */
const syncTime = () => {
  uni.showModal({
    title: '确认同步',
    content: '确定要同步设备时间吗？',
    success: async (res) => {
      if (res.confirm) {
        executing.value = true

        try {
          const result = await accessApi.deviceControl(deviceId.value, 'sync-time')

          if (result.success) {
            uni.showToast({
              title: '同步成功',
              icon: 'success'
            })

            operationLogs.value.unshift({
              action: 'sync-time',
              result: '同步成功',
              success: true,
              timestamp: new Date().getTime()
            })
          } else {
            uni.showToast({
              title: result.message || '同步失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('同步时间失败:', error)
          uni.showToast({
            title: '同步失败',
            icon: 'none'
          })
        } finally {
          executing.value = false
        }
      }
    }
  })
}

/**
 * 清除命令
 */
const clearCommands = () => {
  uni.showModal({
    title: '确认清除',
    content: '确定要清除设备命令缓存吗？',
    success: async (res) => {
      if (res.confirm) {
        executing.value = true

        try {
          const result = await accessApi.deviceControl(deviceId.value, 'clear-commands')

          if (result.success) {
            uni.showToast({
              title: '清除成功',
              icon: 'success'
            })

            operationLogs.value.unshift({
              action: 'clear',
              result: '清除成功',
              success: true,
              timestamp: new Date().getTime()
            })
          } else {
            uni.showToast({
              title: result.message || '清除失败',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('清除命令失败:', error)
          uni.showToast({
            title: '清除失败',
            icon: 'none'
          })
        } finally {
          executing.value = false
        }
      }
    }
  })
}

/**
 * 切换设备状态
 */
const toggleDeviceStatus = () => {
  const action = deviceInfo.value.enabled ? 'disable' : 'enable'
  const actionText = deviceInfo.value.enabled ? '禁用' : '启用'

  uni.showModal({
    title: `确认${actionText}`,
    content: `确定要${actionText}此设备吗？`,
    success: async (res) => {
      if (res.confirm) {
        executing.value = true

        try {
          const result = await accessApi.deviceControl(
            deviceId.value,
            deviceInfo.value.enabled ? 'disable' : 'enable'
          )

          if (result.success) {
            uni.showToast({
              title: `${actionText}成功`,
              icon: 'success'
            })

            operationLogs.value.unshift({
              action: action,
              result: `${actionText}成功`,
              success: true,
              timestamp: new Date().getTime()
            })

            // 刷新设备信息
            setTimeout(() => {
              loadDeviceInfo()
            }, 500)
          } else {
            uni.showToast({
              title: result.message || `${actionText}失败`,
              icon: 'none'
            })
          }
        } catch (error) {
          console.error(`${actionText}设备失败:`, error)
          uni.showToast({
            title: `${actionText}失败`,
            icon: 'none'
          })
        } finally {
          executing.value = false
        }
      }
    }
  })
}

/**
 * 格式化时间
 */
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }

  // 小于1小时
  if (diff < 3600000) {
    return Math.floor(diff / 60000) + '分钟前'
  }

  // 小于24小时
  if (diff < 86400000) {
    return Math.floor(diff / 3600000) + '小时前'
  }

  // 格式化为日期时间
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')

  return `${month}-${day} ${hour}:${minute}`
}

/**
 * 清空日志
 */
const clearLogs = () => {
  operationLogs.value = []
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-control-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 30px;
}

// 导航栏
.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;

  .nav-left,
  .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
    flex: 1;
    text-align: center;
  }
}

// 设备信息卡片
.device-info-card {
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .device-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .device-name {
      font-size: 20px;
      font-weight: bold;
      color: #fff;
    }

    .status-badge {
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;

      &.online {
        background-color: rgba(82, 196, 26, 0.2);
        color: #52c41a;
      }

      &.offline {
        background-color: rgba(255, 77, 79, 0.2);
        color: #ff4d4f;
      }
    }
  }

  .device-meta {
    .meta-text {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

// 控制模式选择
.control-mode-section {
  margin: 0 15px 15px;
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;
  }

  .mode-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;

    .mode-item {
      padding: 20px 10px;
      border-radius: 12px;
      background-color: #f5f5f5;
      display: flex;
      flex-direction: column;
      align-items: center;
      transition: all 0.3s;
      border: 2px solid transparent;

      &:active {
        transform: scale(0.98);
      }

      &.active {
        border-color: #1890ff;
        background-color: #e6f7ff;
      }

      .mode-icon {
        width: 56px;
        height: 56px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
      }

      .mode-title {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6px;
      }

      .mode-desc {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

// 门选择
.door-select-section {
  margin: 0 15px 15px;
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;

    &.required::before {
      content: '*';
      color: #ff4d4f;
      margin-right: 4px;
    }
  }

  .door-list {
    .door-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      border-radius: 8px;
      background-color: #f5f5f5;
      margin-bottom: 10px;
      transition: all 0.3s;

      &:active {
        transform: scale(0.98);
      }

      &.selected {
        background-color: #e6f7ff;
        border: 1px solid #1890ff;
      }

      .door-info {
        flex: 1;

        .door-name-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 6px;

          .door-name {
            font-size: 15px;
            font-weight: 500;
            color: #333;
          }
        }

        .door-location {
          font-size: 13px;
          color: #999;
        }
      }

      .door-status {
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 12px;

        &.open {
          background-color: #f6ffed;
          color: #52c41a;
        }

        &.closed {
          background-color: #fff1f0;
          color: #ff4d4f;
        }
      }
    }
  }
}

// 原因输入
.reason-section {
  margin: 0 15px 15px;
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;

    &.required::before {
      content: '*';
      color: #ff4d4f;
      margin-right: 4px;
    }
  }

  .reason-input {
    position: relative;

    .textarea {
      width: 100%;
      min-height: 100px;
      padding: 12px;
      font-size: 15px;
      color: #333;
      background-color: #f5f5f5;
      border-radius: 8px;
      box-sizing: border-box;

      &::placeholder {
        color: #999;
      }
    }

    .char-count {
      position: absolute;
      bottom: 10px;
      right: 12px;
      font-size: 12px;
      color: #999;
    }
  }
}

// 执行按钮
.action-section {
  padding: 0 15px 15px;

  .action-btn {
    width: 100%;
    height: 50px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    color: #fff;
    font-size: 16px;
    font-weight: 500;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;

    &:disabled {
      opacity: 0.6;
    }

    .btn-text {
      margin-left: 8px;
    }
  }
}

// 常用操作
.common-actions-section {
  margin: 0 15px 15px;
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;
  }

  .action-list {
    .action-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &:active {
        background-color: #f5f5f5;
      }

      .action-row-left {
        display: flex;
        align-items: center;

        .action-row-text {
          margin-left: 12px;
          font-size: 15px;
          color: #333;
        }
      }
    }
  }
}

// 操作日志
.log-section {
  margin: 0 15px;
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;

  .section-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;

    .clear-log {
      font-size: 14px;
      color: #999;
    }
  }

  .log-list {
    .log-item {
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .log-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;

        .log-action {
          font-size: 15px;
          font-weight: 500;
          color: #333;
        }

        .log-time {
          font-size: 12px;
          color: #999;
        }
      }

      .log-result {
        font-size: 13px;

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
</style>
