<template>
  <view class="device-advanced-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">高级设置</view>
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
        <text class="meta-text">{{ deviceInfo.ipAddress || '-' }}</text>
        <text class="meta-divider">|</text>
        <text class="meta-text">{{ getDeviceTypeLabel(deviceInfo.deviceType) }}</text>
      </view>
    </view>

    <!-- 验证参数设置 -->
    <view class="settings-section">
      <view class="section-title">验证参数</view>
      <view class="setting-item" @click="openVerifyParamsModal">
        <view class="setting-left">
          <view class="setting-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <uni-icons type="settings" size="20" color="#fff"></uni-icons>
          </view>
          <view class="setting-info">
            <text class="setting-title">下发验证参数</text>
            <text class="setting-desc">配置设备验证方式和参数</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
      </view>
    </view>

    <!-- 网络设置 -->
    <view class="settings-section">
      <view class="section-title">网络设置</view>
      <view class="setting-item" @click="openTimeZoneModal">
        <view class="setting-left">
          <view class="setting-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <uni-icons type="calendar" size="20" color="#fff"></uni-icons>
          </view>
          <view class="setting-info">
            <text class="setting-title">时区设置</text>
            <text class="setting-value">{{ deviceInfo.timeZone || 'GMT+8' }}</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
      </view>
      <view class="setting-item" @click="openIpModifyModal">
        <view class="setting-left">
          <view class="setting-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <uni-icons type="link" size="20" color="#fff"></uni-icons>
          </view>
          <view class="setting-info">
            <text class="setting-title">修改IP地址</text>
            <text class="setting-value">{{ deviceInfo.ipAddress || '-' }}</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
      </view>
      <view class="setting-item" @click="openNtpModal">
        <view class="setting-left">
          <view class="setting-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <uni-icons type="cloud-download" size="20" color="#fff"></uni-icons>
          </view>
          <view class="setting-info">
            <text class="setting-title">NTP服务器</text>
            <text class="setting-value">{{ deviceInfo.ntpServer || 'pool.ntp.org' }}</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
      </view>
    </view>

    <!-- 安全设置 -->
    <view class="settings-section">
      <view class="section-title">安全设置</view>
      <view class="setting-item" @click="openPasswordModifyModal">
        <view class="setting-left">
          <view class="setting-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
            <uni-icons type="locked" size="20" color="#fff"></uni-icons>
          </view>
          <view class="setting-info">
            <text class="setting-title">修改通讯密码</text>
            <text class="setting-desc">修改设备通讯密码</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
      </view>
    </view>

    <!-- 设备维护 -->
    <view class="settings-section">
      <view class="section-title">设备维护</view>
      <view class="setting-item" @click="replaceDevice">
        <view class="setting-left">
          <view class="setting-icon" style="background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);">
            <uni-icons type="loop" size="20" color="#fff"></uni-icons>
          </view>
          <view class="setting-info">
            <text class="setting-title">设备替换</text>
            <text class="setting-desc">更换设备并保留配置</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
      </view>
      <view class="setting-item" @click="upgradeFirmware">
        <view class="setting-left">
          <view class="setting-icon" style="background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);">
            <uni-icons type="upload" size="20" color="#fff"></uni-icons>
          </view>
          <view class="setting-info">
            <text class="setting-title">固件升级</text>
            <text class="setting-value">{{ deviceInfo.firmwareVersion || '-' }}</text>
          </view>
        </view>
        <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
      </view>
    </view>

    <!-- 时区设置弹窗 -->
    <uni-popup ref="timeZonePopup" type="bottom">
      <view class="popup-container">
        <view class="popup-header">
          <text class="popup-title">选择时区</text>
          <view class="popup-close" @click="closeTimeZoneModal">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <picker-view class="picker-view" :value="timeZoneValue" @change="onTimeZoneChange">
          <picker-view-column>
            <view v-for="(zone, index) in timeZoneList" :key="index" class="picker-item">
              {{ zone }}
            </view>
          </picker-view-column>
        </picker-view>
        <view class="popup-actions">
          <button class="popup-btn cancel" @click="closeTimeZoneModal">取消</button>
          <button class="popup-btn confirm" @click="saveTimeZone">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- IP地址修改弹窗 -->
    <uni-popup ref="ipModifyPopup" type="center">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">修改IP地址</text>
          <view class="modal-close" @click="closeIpModifyModal">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <view class="form-label">新IP地址</view>
            <input
              class="form-input"
              type="text"
              placeholder="请输入新IP地址"
              v-model="newIpAddress"
            />
          </view>
          <view class="form-item">
            <view class="form-label">端口号</view>
            <input
              class="form-input"
              type="number"
              placeholder="请输入端口号"
              v-model="newPort"
            />
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeIpModifyModal">取消</button>
          <button class="modal-btn confirm" @click="saveIpAddress">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- NTP服务器设置弹窗 -->
    <uni-popup ref="ntpPopup" type="center">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">NTP服务器设置</text>
          <view class="modal-close" @click="closeNtpModal">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <view class="form-label">NTP服务器地址</view>
            <input
              class="form-input"
              type="text"
              placeholder="请输入NTP服务器地址"
              v-model="ntpServer"
            />
          </view>
          <view class="form-item">
            <view class="form-label">同步间隔（分钟）</view>
            <input
              class="form-input"
              type="number"
              placeholder="请输入同步间隔"
              v-model="ntpInterval"
            />
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closeNtpModal">取消</button>
          <button class="modal-btn confirm" @click="saveNtpSettings">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- 密码修改弹窗 -->
    <uni-popup ref="passwordPopup" type="center">
      <view class="modal-container">
        <view class="modal-header">
          <text class="modal-title">修改通讯密码</text>
          <view class="modal-close" @click="closePasswordModal">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <view class="modal-body">
          <view class="form-item">
            <view class="form-label">当前密码</view>
            <input
              class="form-input"
              type="password"
              placeholder="请输入当前密码"
              v-model="oldPassword"
            />
          </view>
          <view class="form-item">
            <view class="form-label">新密码</view>
            <input
              class="form-input"
              type="password"
              placeholder="请输入新密码"
              v-model="newPassword"
            />
          </view>
          <view class="form-item">
            <view class="form-label">确认密码</view>
            <input
              class="form-input"
              type="password"
              placeholder="请再次输入新密码"
              v-model="confirmPassword"
            />
          </view>
        </view>
        <view class="modal-footer">
          <button class="modal-btn cancel" @click="closePasswordModal">取消</button>
          <button class="modal-btn confirm" @click="savePassword">确定</button>
        </view>
      </view>
    </uni-popup>

    <!-- 验证参数设置弹窗 -->
    <uni-popup ref="verifyParamsPopup" type="bottom">
      <view class="popup-full-container">
        <view class="popup-header">
          <text class="popup-title">验证参数设置</text>
          <view class="popup-close" @click="closeVerifyParamsModal">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>
        <scroll-view class="popup-content" scroll-y>
          <view class="form-section">
            <view class="form-item">
              <view class="form-label">验证方式</view>
              <picker mode="selector" :range="verifyModes" range-key="label" :value="verifyModeIndex" @change="onVerifyModeChange">
                <view class="form-picker">
                  <text :class="['picker-text', { placeholder: !verifyParams.verifyMode }]">
                    {{ getVerifyModeLabel() }}
                  </text>
                  <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <view class="form-label">卡号验证模式</view>
              <picker mode="selector" :range="cardModes" range-key="label" :value="cardModeIndex" @change="onCardModeChange">
                <view class="form-picker">
                  <text :class="['picker-text', { placeholder: !verifyParams.cardMode }]">
                    {{ getCardModeLabel() }}
                  </text>
                  <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <view class="form-label">密码验证模式</view>
              <picker mode="selector" :range="passwordModes" range-key="label" :value="passwordModeIndex" @change="onPasswordModeChange">
                <view class="form-picker">
                  <text :class="['picker-text', { placeholder: !verifyParams.passwordMode }]">
                    {{ getPasswordModeLabel() }}
                  </text>
                  <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item switch-item">
              <view class="switch-left">
                <text class="form-label">反潜回功能</text>
                <text class="form-desc">启用后将检测非法通行</text>
              </view>
              <switch :checked="verifyParams.antiPassback" color="#1890ff" @change="onAntiPassbackChange" />
            </view>
            <view class="form-item switch-item">
              <view class="switch-left">
                <text class="form-label">多卡验证</text>
                <text class="form-desc">需要多张卡同时验证</text>
              </view>
              <switch :checked="verifyParams.multiCard" color="#1890ff" @change="onMultiCardChange" />
            </view>
            <view class="form-item" v-if="verifyParams.multiCard">
              <view class="form-label">验证卡数量</view>
              <input
                class="form-input"
                type="number"
                placeholder="请输入验证卡数量"
                v-model="verifyParams.cardCount"
              />
            </view>
            <view class="form-item switch-item">
              <view class="switch-left">
                <text class="form-label">首卡开门</text>
                <text class="form-desc">第一张卡验证后开门</text>
              </view>
              <switch :checked="verifyParams.firstCardOpen" color="#1890ff" @change="onFirstCardOpenChange" />
            </view>
          </view>
        </scroll-view>
        <view class="popup-actions">
          <button class="popup-btn cancel" @click="closeVerifyParamsModal">取消</button>
          <button class="popup-btn confirm" @click="saveVerifyParams">保存</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 设备信息
const deviceInfo = ref({})
const deviceId = ref(null)

// 时区相关
const timeZonePopup = ref(null)
const timeZoneValue = ref([7]) // GMT+8
const timeZoneList = [
  'GMT-12', 'GMT-11', 'GMT-10', 'GMT-9', 'GMT-8', 'GMT-7',
  'GMT-6', 'GMT-5', 'GMT-4', 'GMT-3', 'GMT-2', 'GMT-1',
  'GMT+0', 'GMT+1', 'GMT+2', 'GMT+3', 'GMT+4', 'GMT+5',
  'GMT+6', 'GMT+7', 'GMT+8', 'GMT+9', 'GMT+10', 'GMT+11', 'GMT+12'
]
const selectedTimeZone = ref('GMT+8')

// IP修改相关
const ipModifyPopup = ref(null)
const newIpAddress = ref('')
const newPort = ref('8080')

// NTP设置相关
const ntpPopup = ref(null)
const ntpServer = ref('pool.ntp.org')
const ntpInterval = ref('60')

// 密码修改相关
const passwordPopup = ref(null)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')

// 验证参数相关
const verifyParamsPopup = ref(null)
const verifyParams = ref({
  verifyMode: 1, // 1-卡 2-密码 3-卡+密码 4-生物识别
  cardMode: 1, // 1-只读卡号 2-读卡号+密码
  passwordMode: 1, // 1-固定密码 2-个人密码
  antiPassback: false,
  multiCard: false,
  cardCount: 2,
  firstCardOpen: false
})

const verifyModes = [
  { value: 1, label: '刷卡' },
  { value: 2, label: '密码' },
  { value: 3, label: '卡+密码' },
  { value: 4, label: '生物识别' }
]
const verifyModeIndex = ref(0)

const cardModes = [
  { value: 1, label: '只读卡号' },
  { value: 2, label: '读卡号+密码' },
  { value: 3, label: '读卡号+生物识别' }
]
const cardModeIndex = ref(0)

const passwordModes = [
  { value: 1, label: '固定密码' },
  { value: 2, label: '个人密码' },
  { value: 3, label: '卡+个人密码' }
]
const passwordModeIndex = ref(0)

// 页面生命周期
onLoad((options) => {
  if (options.id) {
    deviceId.value = parseInt(options.id)
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

      // 设置当前值
      if (deviceInfo.value.timeZone) {
        const index = timeZoneList.indexOf(deviceInfo.value.timeZone)
        if (index !== -1) {
          timeZoneValue.value = [index]
          selectedTimeZone.value = deviceInfo.value.timeZone
        }
      }

      if (deviceInfo.value.ipAddress) {
        newIpAddress.value = deviceInfo.value.ipAddress
      }

      if (deviceInfo.value.port) {
        newPort.value = deviceInfo.value.port.toString()
      }

      if (deviceInfo.value.ntpServer) {
        ntpServer.value = deviceInfo.value.ntpServer
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
 * 获取设备类型标签
 */
const getDeviceTypeLabel = (type) => {
  const types = {
    1: '门禁控制器',
    2: '门禁一体机'
  }
  return types[type] || '未知'
}

/**
 * 打开时区弹窗
 */
const openTimeZoneModal = () => {
  timeZonePopup.value.open()
}

/**
 * 关闭时区弹窗
 */
const closeTimeZoneModal = () => {
  timeZonePopup.value.close()
}

/**
 * 时区变更
 */
const onTimeZoneChange = (e) => {
  timeZoneValue.value = e.detail.value
}

/**
 * 保存时区
 */
const saveTimeZone = async () => {
  const index = timeZoneValue.value[0]
  const timeZone = timeZoneList[index]

  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    const result = await accessApi.updateDevice(deviceId.value, {
      timeZone: timeZone
    })

    uni.hideLoading()

    if (result.success) {
      deviceInfo.value.timeZone = timeZone
      selectedTimeZone.value = timeZone
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
      closeTimeZoneModal()
    } else {
      uni.showToast({
        title: result.message || '保存失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('保存时区失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

/**
 * 打开IP修改弹窗
 */
const openIpModifyModal = () => {
  ipModifyPopup.value.open()
}

/**
 * 关闭IP修改弹窗
 */
const closeIpModifyModal = () => {
  ipModifyPopup.value.close()
}

/**
 * 保存IP地址
 */
const saveIpAddress = async () => {
  // 验证IP地址
  if (!newIpAddress.value || newIpAddress.value.trim().length === 0) {
    uni.showToast({
      title: '请输入IP地址',
      icon: 'none'
    })
    return
  }

  const ipRegex = /^(\d{1,3}\.){3}\d{1,3}$/
  if (!ipRegex.test(newIpAddress.value)) {
    uni.showToast({
      title: 'IP地址格式不正确',
      icon: 'none'
    })
    return
  }

  const parts = newIpAddress.value.split('.')
  for (let i = 0; i < parts.length; i++) {
    const num = parseInt(parts[i])
    if (num < 0 || num > 255) {
      uni.showToast({
        title: 'IP地址每段必须在0-255之间',
        icon: 'none'
      })
      return
    }
  }

  // 验证端口号
  if (!newPort.value || newPort.value.trim().length === 0) {
    uni.showToast({
      title: '请输入端口号',
      icon: 'none'
    })
    return
  }

  const port = parseInt(newPort.value)
  if (port < 1 || port > 65535) {
    uni.showToast({
      title: '端口号必须在1-65535之间',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认修改',
    content: '修改IP地址后，需要使用新地址访问设备。确定要修改吗？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({
          title: '修改中...',
          mask: true
        })

        try {
          const result = await accessApi.updateDevice(deviceId.value, {
            ipAddress: newIpAddress.value.trim(),
            port: port
          })

          uni.hideLoading()

          if (result.success) {
            deviceInfo.value.ipAddress = newIpAddress.value.trim()
            deviceInfo.value.port = port
            uni.showToast({
              title: '修改成功',
              icon: 'success'
            })
            closeIpModifyModal()
          } else {
            uni.showToast({
              title: result.message || '修改失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('修改IP地址失败:', error)
          uni.showToast({
            title: '修改失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 打开NTP设置弹窗
 */
const openNtpModal = () => {
  ntpPopup.value.open()
}

/**
 * 关闭NTP设置弹窗
 */
const closeNtpModal = () => {
  ntpPopup.value.close()
}

/**
 * 保存NTP设置
 */
const saveNtpSettings = async () => {
  if (!ntpServer.value || ntpServer.value.trim().length === 0) {
    uni.showToast({
      title: '请输入NTP服务器地址',
      icon: 'none'
    })
    return
  }

  const interval = parseInt(ntpInterval.value)
  if (isNaN(interval) || interval < 1 || interval > 1440) {
    uni.showToast({
      title: '同步间隔必须在1-1440分钟之间',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    const result = await accessApi.updateDevice(deviceId.value, {
      ntpServer: ntpServer.value.trim(),
      ntpInterval: interval
    })

    uni.hideLoading()

    if (result.success) {
      deviceInfo.value.ntpServer = ntpServer.value.trim()
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
      closeNtpModal()
    } else {
      uni.showToast({
        title: result.message || '保存失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('保存NTP设置失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

/**
 * 打开密码修改弹窗
 */
const openPasswordModifyModal = () => {
  passwordPopup.value.open()
}

/**
 * 关闭密码修改弹窗
 */
const closePasswordModal = () => {
  passwordPopup.value.close()
  // 清空表单
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
}

/**
 * 保存密码
 */
const savePassword = async () => {
  // 验证表单
  if (!oldPassword.value || oldPassword.value.trim().length === 0) {
    uni.showToast({
      title: '请输入当前密码',
      icon: 'none'
    })
    return
  }

  if (!newPassword.value || newPassword.value.trim().length === 0) {
    uni.showToast({
      title: '请输入新密码',
      icon: 'none'
    })
    return
  }

  if (newPassword.value.length < 6) {
    uni.showToast({
      title: '新密码长度不能少于6位',
      icon: 'none'
    })
    return
  }

  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '修改中...',
    mask: true
  })

  try {
    const result = await accessApi.updateDevicePassword(deviceId.value, {
      oldPassword: oldPassword.value,
      newPassword: newPassword.value
    })

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '修改成功',
        icon: 'success'
      })
      closePasswordModal()
    } else {
      uni.showToast({
        title: result.message || '修改失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('修改密码失败:', error)
    uni.showToast({
      title: '修改失败',
      icon: 'none'
    })
  }
}

/**
 * 打开验证参数弹窗
 */
const openVerifyParamsModal = () => {
  verifyParamsPopup.value.open()
}

/**
 * 关闭验证参数弹窗
 */
const closeVerifyParamsModal = () => {
  verifyParamsPopup.value.close()
}

/**
 * 获取验证方式标签
 */
const getVerifyModeLabel = () => {
  if (!verifyParams.value.verifyMode) {
    return '请选择验证方式'
  }
  const mode = verifyModes.find(m => m.value === verifyParams.value.verifyMode)
  return mode ? mode.label : '请选择验证方式'
}

/**
 * 验证方式变更
 */
const onVerifyModeChange = (e) => {
  const index = e.detail.value
  verifyModeIndex.value = index
  verifyParams.value.verifyMode = verifyModes[index].value
}

/**
 * 获取卡验证模式标签
 */
const getCardModeLabel = () => {
  if (!verifyParams.value.cardMode) {
    return '请选择卡验证模式'
  }
  const mode = cardModes.find(m => m.value === verifyParams.value.cardMode)
  return mode ? mode.label : '请选择卡验证模式'
}

/**
 * 卡验证模式变更
 */
const onCardModeChange = (e) => {
  const index = e.detail.value
  cardModeIndex.value = index
  verifyParams.value.cardMode = cardModes[index].value
}

/**
 * 获取密码验证模式标签
 */
const getPasswordModeLabel = () => {
  if (!verifyParams.value.passwordMode) {
    return '请选择密码验证模式'
  }
  const mode = passwordModes.find(m => m.value === verifyParams.value.passwordMode)
  return mode ? mode.label : '请选择密码验证模式'
}

/**
 * 密码验证模式变更
 */
const onPasswordModeChange = (e) => {
  const index = e.detail.value
  passwordModeIndex.value = index
  verifyParams.value.passwordMode = passwordModes[index].value
}

/**
 * 反潜回功能变更
 */
const onAntiPassbackChange = (e) => {
  verifyParams.value.antiPassback = e.detail.value
}

/**
 * 多卡验证变更
 */
const onMultiCardChange = (e) => {
  verifyParams.value.multiCard = e.detail.value
}

/**
 * 首卡开门变更
 */
const onFirstCardOpenChange = (e) => {
  verifyParams.value.firstCardOpen = e.detail.value
}

/**
 * 保存验证参数
 */
const saveVerifyParams = async () => {
  uni.showLoading({
    title: '保存中...',
    mask: true
  })

  try {
    const result = await accessApi.updateDeviceVerifyParams(deviceId.value, verifyParams.value)

    uni.hideLoading()

    if (result.success) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
      closeVerifyParamsModal()
    } else {
      uni.showToast({
        title: result.message || '保存失败',
        icon: 'none'
      })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('保存验证参数失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  }
}

/**
 * 设备替换
 */
const replaceDevice = () => {
  uni.showModal({
    title: '设备替换',
    content: '此操作将替换当前设备，并保留原有配置。确定要继续吗？',
    confirmText: '确定替换',
    confirmColor: '#ff4d4f',
    success: (res) => {
      if (res.confirm) {
        // 跳转到设备替换页面
        uni.navigateTo({
          url: `/pages/access/device-replace?id=${deviceId.value}`
        })
      }
    }
  })
}

/**
 * 固件升级
 */
const upgradeFirmware = () => {
  if (!deviceInfo.value.online) {
    uni.showToast({
      title: '设备离线，无法升级',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '固件升级',
    content: '升级过程中设备将无法正常工作，请确保设备电量充足。确定要升级吗？',
    confirmText: '确定升级',
    success: (res) => {
      if (res.confirm) {
        // 执行固件升级
        uni.showLoading({
          title: '检查更新...',
          mask: true
        })

        // 模拟检查更新
        setTimeout(() => {
          uni.hideLoading()
          uni.showModal({
            title: '发现新版本',
            content: '当前版本: v1.0.0\n最新版本: v1.2.0\n\n是否立即升级？',
            confirmText: '立即升级',
            success: (res) => {
              if (res.confirm) {
                uni.showLoading({
                  title: '升级中...',
                  mask: true
                })

                // 模拟升级过程
                setTimeout(() => {
                  uni.hideLoading()
                  uni.showToast({
                    title: '升级成功',
                    icon: 'success'
                  })

                  // 刷新设备信息
                  loadDeviceInfo()
                }, 3000)
              }
            }
          })
        }, 1000)
      }
    }
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-advanced-page {
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
    display: flex;
    align-items: center;

    .meta-text {
      font-size: 14px;
      color: rgba(255, 255, 255, 0.9);
    }

    .meta-divider {
      margin: 0 8px;
      color: rgba(255, 255, 255, 0.5);
    }
  }
}

// 设置区块
.settings-section {
  margin: 0 15px 15px;
  background-color: #fff;
  border-radius: 12px;
  padding: 5px 0;

  .section-title {
    padding: 15px 15px 10px;
    font-size: 16px;
    font-weight: 500;
    color: #333;
  }

  .setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
    transition: background-color 0.3s;

    &:last-child {
      border-bottom: none;
    }

    &:active {
      background-color: #f5f5f5;
    }

    .setting-left {
      flex: 1;
      display: flex;
      align-items: center;

      .setting-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
      }

      .setting-info {
        flex: 1;

        .setting-title {
          font-size: 15px;
          font-weight: 500;
          color: #333;
          display: block;
          margin-bottom: 4px;
        }

        .setting-desc {
          font-size: 13px;
          color: #999;
          display: block;
        }

        .setting-value {
          font-size: 13px;
          color: #1890ff;
          display: block;
        }
      }
    }
  }
}

// 底部弹窗容器
.popup-container {
  background-color: #fff;
  border-radius: 20px 20px 0 0;
  padding: 20px;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .popup-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 16px;
    }
  }

  .picker-view {
    height: 200px;
    margin-bottom: 20px;

    .picker-item {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 40px;
      font-size: 15px;
      color: #333;
    }
  }

  .popup-actions {
    display: flex;
    gap: 12px;

    .popup-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      border: none;

      &.cancel {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

// 全屏弹窗容器
.popup-full-container {
  height: 80vh;
  background-color: #fff;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .popup-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 16px;
    }
  }

  .popup-content {
    flex: 1;
    padding: 20px;

    .form-section {
      .form-item {
        margin-bottom: 20px;

        .form-label {
          font-size: 15px;
          color: #333;
          margin-bottom: 12px;
          font-weight: 500;
        }

        .form-picker {
          display: flex;
          justify-content: space-between;
          align-items: center;
          height: 48px;
          padding: 0 12px;
          background-color: #f5f5f5;
          border-radius: 8px;

          .picker-text {
            font-size: 15px;
            color: #333;

            &.placeholder {
              color: #999;
            }
          }
        }

        .form-input {
          width: 100%;
          height: 48px;
          padding: 0 12px;
          font-size: 15px;
          color: #333;
          background-color: #f5f5f5;
          border-radius: 8px;
          box-sizing: border-box;

          &::placeholder {
            color: #999;
          }
        }

        &.switch-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 15px 0;

          .switch-left {
            flex: 1;

            .form-label {
              margin-bottom: 6px;
            }

            .form-desc {
              font-size: 13px;
              color: #999;
            }
          }
        }
      }
    }
  }

  .popup-actions {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #f0f0f0;

    .popup-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      border: none;

      &.cancel {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}

// 中心弹窗容器
.modal-container {
  width: 85%;
  background-color: #fff;
  border-radius: 16px;
  overflow: hidden;

  .modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .modal-title {
      font-size: 17px;
      font-weight: 500;
      color: #333;
    }

    .modal-close {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 16px;
    }
  }

  .modal-body {
    padding: 20px;

    .form-item {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .form-label {
        font-size: 15px;
        color: #333;
        margin-bottom: 12px;
        font-weight: 500;
      }

      .form-input {
        width: 100%;
        height: 48px;
        padding: 0 12px;
        font-size: 15px;
        color: #333;
        background-color: #f5f5f5;
        border-radius: 8px;
        box-sizing: border-box;

        &::placeholder {
          color: #999;
        }
      }
    }
  }

  .modal-footer {
    display: flex;
    gap: 12px;
    padding: 20px;
    border-top: 1px solid #f0f0f0;

    .modal-btn {
      flex: 1;
      height: 48px;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 500;
      border: none;

      &.cancel {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: #fff;
      }
    }
  }
}
</style>
