<template>
  <view class="device-communication-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">通信设置</view>
        <view class="navbar-right" @tap="saveSettings">
          <text class="save-text">保存</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view class="main-scroll" scroll-y>
      <!-- 连接类型 -->
      <view class="settings-section">
        <view class="section-header">
          <text class="section-title">连接类型</text>
          <text class="section-desc">选择设备的通信方式</text>
        </view>

        <view class="connection-type-selector">
          <view
            v-for="type in connectionTypes"
            :key="type.value"
            class="connection-type-item"
            :class="{ active: commType === type.value }"
            @tap="selectConnectionType(type.value)"
          >
            <view class="type-icon">
              <uni-icons :type="type.icon" size="32" :color="commType === type.value ? '#667eea' : '#999'"></uni-icons>
            </view>
            <text class="type-name">{{ type.label }}</text>
            <view v-if="commType === type.value" class="type-check">
              <uni-icons type="checkbox-filled" size="18" color="#667eea"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- TCP/IP 配置 -->
      <view class="settings-section" v-if="commType === 1">
        <view class="section-header">
          <text class="section-title">TCP/IP 网络配置</text>
          <text class="section-desc">以太网通信参数设置</text>
        </view>

        <view class="settings-card">
          <!-- IP地址 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="wifi" size="18" color="#667eea"></uni-icons>
              <text class="label-text">IP地址</text>
            </view>
            <input
              class="setting-input"
              v-model="tcpConfig.ipAddress"
              placeholder="192.168.1.100"
              placeholder-class="input-placeholder"
            />
          </view>

          <!-- 端口 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
              <text class="label-text">端口</text>
            </view>
            <input
              class="setting-input"
              v-model="tcpConfig.port"
              type="number"
              placeholder="8000"
              placeholder-class="input-placeholder"
            />
          </view>

          <!-- 子网掩码 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="eye" size="18" color="#667eea"></uni-icons>
              <text class="label-text">子网掩码</text>
            </view>
            <input
              class="setting-input"
              v-model="tcpConfig.subnetMask"
              placeholder="255.255.255.0"
              placeholder-class="input-placeholder"
            />
          </view>

          <!-- 网关 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="home" size="18" color="#667eea"></uni-icons>
              <text class="label-text">默认网关</text>
            </view>
            <input
              class="setting-input"
              v-model="tcpConfig.gateway"
              placeholder="192.168.1.1"
              placeholder-class="input-placeholder"
            />
          </view>

          <!-- MAC地址 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="chip" size="18" color="#667eea"></uni-icons>
              <text class="label-text">MAC地址</text>
            </view>
            <input
              class="setting-input"
              v-model="tcpConfig.macAddress"
              placeholder="00:11:22:33:44:55"
              placeholder-class="input-placeholder"
            />
          </view>

          <!-- DNS -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="cloud" size="18" color="#667eea"></uni-icons>
              <text class="label-text">DNS服务器</text>
            </view>
            <input
              class="setting-input"
              v-model="tcpConfig.dnsServer"
              placeholder="8.8.8.8"
              placeholder-class="input-placeholder"
            />
          </view>
        </view>
      </view>

      <!-- RS485 配置 -->
      <view class="settings-section" v-if="commType === 2">
        <view class="section-header">
          <text class="section-title">RS485 串口配置</text>
          <text class="section-desc">串口通信参数设置</text>
        </view>

        <view class="settings-card">
          <!-- 串口号 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
              <text class="label-text">串口号</text>
            </view>
            <picker mode="selector" :range="serialPorts" @change="onSerialPortChange">
              <view class="picker-value">
                <text :class="{ placeholder: !rs485Config.serialPort }">
                  {{ rs485Config.serialPort || '请选择串口号' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 波特率 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="speed" size="18" color="#667eea"></uni-icons>
              <text class="label-text">波特率</text>
            </view>
            <picker mode="selector" :range="baudRates" @change="onBaudRateChange">
              <view class="picker-value">
                <text :class="{ placeholder: !rs485Config.baudRate }">
                  {{ rs485Config.baudRate || '请选择波特率' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 数据位 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="list" size="18" color="#667eea"></uni-icons>
              <text class="label-text">数据位</text>
            </view>
            <picker mode="selector" :range="dataBits" @change="onDataBitChange">
              <view class="picker-value">
                <text :class="{ placeholder: !rs485Config.dataBit }">
                  {{ rs485Config.dataBit || '请选择数据位' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 停止位 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="pause" size="18" color="#667eea"></uni-icons>
              <text class="label-text">停止位</text>
            </view>
            <picker mode="selector" :range="stopBits" @change="onStopBitChange">
              <view class="picker-value">
                <text :class="{ placeholder: !rs485Config.stopBit }">
                  {{ rs485Config.stopBit || '请选择停止位' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 校验位 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="checkmarkempty" size="18" color="#667eea"></uni-icons>
              <text class="label-text">校验位</text>
            </view>
            <picker mode="selector" :range="parityBits" @change="onParityBitChange">
              <view class="picker-value">
                <text :class="{ placeholder: !rs485Config.parityBit }">
                  {{ rs485Config.parityBit || '请选择校验位' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 设备地址 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="location" size="18" color="#667eea"></uni-icons>
              <text class="label-text">设备地址</text>
            </view>
            <input
              class="setting-input"
              v-model="rs485Config.deviceAddress"
              type="number"
              placeholder="1-255"
              placeholder-class="input-placeholder"
            />
          </view>
        </view>
      </view>

      <!-- 心跳设置 -->
      <view class="settings-section">
        <view class="section-header">
          <text class="section-title">心跳设置</text>
          <text class="section-desc">设备连接保活参数</text>
        </view>

        <view class="settings-card">
          <!-- 心跳间隔 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="loop" size="18" color="#667eea"></uni-icons>
              <text class="label-text">心跳间隔</text>
            </view>
            <view class="input-with-unit">
              <input
                class="setting-input"
                v-model="heartbeatConfig.interval"
                type="number"
                placeholder="30"
                placeholder-class="input-placeholder"
              />
              <text class="input-unit">秒</text>
            </view>
          </view>

          <!-- 心跳超时 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="clock" size="18" color="#667eea"></uni-icons>
              <text class="label-text">心跳超时</text>
            </view>
            <view class="input-with-unit">
              <input
                class="setting-input"
                v-model="heartbeatConfig.timeout"
                type="number"
                placeholder="90"
                placeholder-class="input-placeholder"
              />
              <text class="input-unit">秒</text>
            </view>
          </view>

          <!-- 心跳重试次数 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="refresh" size="18" color="#667eea"></uni-icons>
              <text class="label-text">重试次数</text>
            </view>
            <view class="input-with-unit">
              <input
                class="setting-input"
                v-model="heartbeatConfig.retryCount"
                type="number"
                placeholder="3"
                placeholder-class="input-placeholder"
              />
              <text class="input-unit">次</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 高级设置 -->
      <view class="settings-section">
        <view class="section-header">
          <text class="section-title">高级设置</text>
          <text class="section-desc">连接超时和重连策略</text>
        </view>

        <view class="settings-card">
          <!-- 连接超时 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="clock" size="18" color="#667eea"></uni-icons>
              <text class="label-text">连接超时</text>
            </view>
            <view class="input-with-unit">
              <input
                class="setting-input"
                v-model="advancedConfig.connectionTimeout"
                type="number"
                placeholder="10"
                placeholder-class="input-placeholder"
              />
              <text class="input-unit">秒</text>
            </view>
          </view>

          <!-- 读写超时 -->
          <view class="setting-item">
            <view class="setting-label">
              <uni-icons type="eye" size="18" color="#667eea"></uni-icons>
              <text class="label-text">读写超时</text>
            </view>
            <view class="input-with-unit">
              <input
                class="setting-input"
                v-model="advancedConfig.readWriteTimeout"
                type="number"
                placeholder="5"
                placeholder-class="input-placeholder"
              />
              <text class="input-unit">秒</text>
            </view>
          </view>

          <!-- 自动重连 -->
          <view class="setting-item switch-setting">
            <view class="setting-label">
              <uni-icons type="refresh" size="18" color="#667eea"></uni-icons>
              <text class="label-text">自动重连</text>
            </view>
            <switch
              :checked="advancedConfig.autoReconnect"
              @change="onAutoReconnectChange"
              color="#667eea"
            />
          </view>

          <!-- 重连间隔 -->
          <view class="setting-item" v-if="advancedConfig.autoReconnect">
            <view class="setting-label">
              <uni-icons type="time" size="18" color="#667eea"></uni-icons>
              <text class="label-text">重连间隔</text>
            </view>
            <view class="input-with-unit">
              <input
                class="setting-input"
                v-model="advancedConfig.reconnectInterval"
                type="number"
                placeholder="5"
                placeholder-class="input-placeholder"
              />
              <text class="input-unit">秒</text>
            </view>
          </view>

          <!-- 最大重连次数 -->
          <view class="setting-item" v-if="advancedConfig.autoReconnect">
            <view class="setting-label">
              <uni-icons type="numbered-list" size="18" color="#667eea"></uni-icons>
              <text class="label-text">最大重连次数</text>
            </view>
            <view class="input-with-unit">
              <input
                class="setting-input"
                v-model="advancedConfig.maxReconnectAttempts"
                type="number"
                placeholder="10"
                placeholder-class="input-placeholder"
              />
              <text class="input-unit">次</text>
            </view>
          </view>
        </view>
      </view>

      <!-- SSL/TLS 配置 -->
      <view class="settings-section" v-if="commType === 1">
        <view class="section-header">
          <text class="section-title">SSL/TLS 配置</text>
          <text class="section-desc">安全通信参数</text>
        </view>

        <view class="settings-card">
          <!-- 启用SSL -->
          <view class="setting-item switch-setting">
            <view class="setting-label">
              <uni-icons type="locked" size="18" color="#667eea"></uni-icons>
              <text class="label-text">启用SSL</text>
            </view>
            <switch
              :checked="sslConfig.enabled"
              @change="onSslEnabledChange"
              color="#667eea"
            />
          </view>

          <!-- SSL版本 -->
          <view class="setting-item" v-if="sslConfig.enabled">
            <view class="setting-label">
              <uni-icons type="settings" size="18" color="#667eea"></uni-icons>
              <text class="label-text">SSL版本</text>
            </view>
            <picker mode="selector" :range="sslVersions" @change="onSslVersionChange">
              <view class="picker-value">
                <text :class="{ placeholder: !sslConfig.version }">
                  {{ sslConfig.version || '请选择SSL版本' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 证书验证 -->
          <view class="setting-item switch-setting" v-if="sslConfig.enabled">
            <view class="setting-label">
              <uni-icons type="checkmarkempty" size="18" color="#667eea"></uni-icons>
              <text class="label-text">验证证书</text>
            </view>
            <switch
              :checked="sslConfig.verifyCert"
              @change="onVerifyCertChange"
              color="#667eea"
            />
          </view>
        </view>
      </view>

      <!-- 连接测试 -->
      <view class="settings-section">
        <view class="test-connection-card">
          <view class="test-header">
            <uni-icons type="wifi" size="24" color="#667eea"></uni-icons>
            <text class="test-title">连接测试</text>
          </view>
          <text class="test-desc">测试设备通信连接是否正常</text>
          <button class="btn-test" @tap="testConnection" :disabled="testing">
            {{ testing ? '测试中...' : '开始测试' }}
          </button>

          <!-- 测试结果 -->
          <view class="test-result" v-if="testResult">
            <view class="result-item" :class="testResult.success ? 'success' : 'failed'">
              <uni-icons
                :type="testResult.success ? 'checkbox-filled' : 'close-filled'"
                size="20"
                :color="testResult.success ? '#52c41a' : '#ff4d4f'"
              ></uni-icons>
              <text class="result-text">{{ testResult.message }}</text>
            </view>
            <view class="result-detail" v-if="testResult.detail">
              <text class="detail-text">{{ testResult.detail }}</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'

// 页面参数
const deviceId = ref('')

// 连接类型
const commType = ref(1) // 1-TCP/IP 2-RS485

const connectionTypes = [
  { label: 'TCP/IP', value: 1, icon: 'wifi' },
  { label: 'RS485', value: 2, icon: 'settings' }
]

// TCP/IP 配置
const tcpConfig = reactive({
  ipAddress: '',
  port: '8000',
  subnetMask: '255.255.255.0',
  gateway: '',
  macAddress: '',
  dnsServer: ''
})

// RS485 配置
const rs485Config = reactive({
  serialPort: '',
  baudRate: '',
  dataBit: '',
  stopBit: '',
  parityBit: '',
  deviceAddress: ''
})

// 选项数据
const serialPorts = ['COM1', 'COM2', 'COM3', 'COM4', 'COM5', 'COM6', 'COM7', 'COM8']
const baudRates = ['1200', '2400', '4800', '9600', '14400', '19200', '38400', '57600', '115200']
const dataBits = ['5', '6', '7', '8']
const stopBits = ['1', '1.5', '2']
const parityBits = ['无', '奇校验', '偶校验']
const sslVersions = ['TLS 1.0', 'TLS 1.1', 'TLS 1.2', 'TLS 1.3']

// 心跳配置
const heartbeatConfig = reactive({
  interval: 30,
  timeout: 90,
  retryCount: 3
})

// 高级配置
const advancedConfig = reactive({
  connectionTimeout: 10,
  readWriteTimeout: 5,
  autoReconnect: true,
  reconnectInterval: 5,
  maxReconnectAttempts: 10
})

// SSL配置
const sslConfig = reactive({
  enabled: false,
  version: 'TLS 1.2',
  verifyCert: true
})

// 测试状态
const testing = ref(false)
const testResult = ref(null)

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  deviceId.value = options.deviceId || ''

  loadCommunicationSettings()
})

// 加载通信设置
const loadCommunicationSettings = async () => {
  try {
    const res = await deviceApi.getDeviceCommunication(deviceId.value)
    if (res.code === 200 && res.data) {
      commType.value = res.data.commType || 1
      Object.assign(tcpConfig, res.data.tcpConfig || {})
      Object.assign(rs485Config, res.data.rs485Config || {})
      Object.assign(heartbeatConfig, res.data.heartbeatConfig || {})
      Object.assign(advancedConfig, res.data.advancedConfig || {})
      Object.assign(sslConfig, res.data.sslConfig || {})
    }
  } catch (error) {
    console.error('加载通信设置失败:', error)
  }
}

// 选择连接类型
const selectConnectionType = (type) => {
  commType.value = type
}

// RS485 配置选择器变更
const onSerialPortChange = (e) => {
  rs485Config.serialPort = serialPorts[e.detail.value]
}

const onBaudRateChange = (e) => {
  rs485Config.baudRate = baudRates[e.detail.value]
}

const onDataBitChange = (e) => {
  rs485Config.dataBit = dataBits[e.detail.value]
}

const onStopBitChange = (e) => {
  rs485Config.stopBit = stopBits[e.detail.value]
}

const onParityBitChange = (e) => {
  rs485Config.parityBit = parityBits[e.detail.value]
}

// 高级配置变更
const onAutoReconnectChange = (e) => {
  advancedConfig.autoReconnect = e.detail.value
}

// SSL配置变更
const onSslEnabledChange = (e) => {
  sslConfig.enabled = e.detail.value
}

const onSslVersionChange = (e) => {
  sslConfig.version = sslVersions[e.detail.value]
}

const onVerifyCertChange = (e) => {
  sslConfig.verifyCert = e.detail.value
}

// 保存设置
const saveSettings = async () => {
  // 验证TCP/IP配置
  if (commType.value === 1) {
    if (!tcpConfig.ipAddress) {
      uni.showToast({
        title: '请输入IP地址',
        icon: 'none'
      })
      return
    }
    if (!tcpConfig.port) {
      uni.showToast({
        title: '请输入端口',
        icon: 'none'
      })
      return
    }
  }

  // 验证RS485配置
  if (commType.value === 2) {
    if (!rs485Config.serialPort) {
      uni.showToast({
        title: '请选择串口号',
        icon: 'none'
      })
      return
    }
    if (!rs485Config.baudRate) {
      uni.showToast({
        title: '请选择波特率',
        icon: 'none'
      })
      return
    }
  }

  try {
    const params = {
      commType: commType.value,
      tcpConfig: commType.value === 1 ? tcpConfig : {},
      rs485Config: commType.value === 2 ? rs485Config : {},
      heartbeatConfig,
      advancedConfig,
      sslConfig
    }

    uni.showLoading({
      title: '保存中...'
    })

    const res = await deviceApi.updateDeviceCommunication(deviceId.value, params)
    if (res.code === 200) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('保存通信设置失败:', error)
    uni.showToast({
      title: '保存失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 测试连接
const testConnection = async () => {
  testing.value = true
  testResult.value = null

  try {
    const res = await deviceApi.testDeviceConnection(deviceId.value, {
      commType: commType.value,
      tcpConfig: commType.value === 1 ? tcpConfig : {},
      rs485Config: commType.value === 2 ? rs485Config : {}
    })

    testResult.value = {
      success: res.code === 200,
      message: res.code === 200 ? '连接成功' : '连接失败',
      detail: res.data?.detail || res.message || ''
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    testResult.value = {
      success: false,
      message: '连接失败',
      detail: error.message || '网络连接异常'
    }
  } finally {
    setTimeout(() => {
      testing.value = false
    }, 1000)
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-communication-page {
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 600;
}

// 主内容区域
.main-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 108rpx);
  padding-bottom: 30rpx;
}

// 设置区块
.settings-section {
  margin: 20rpx 30rpx;
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
  margin-bottom: 8rpx;
}

.section-desc {
  display: block;
  font-size: 24rpx;
  color: #999999;
}

.settings-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.label-text {
  font-size: 28rpx;
  color: #333333;
}

.setting-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  color: #333333;
}

.input-placeholder {
  color: #CCCCCC;
}

.input-with-unit {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.input-with-unit .setting-input {
  flex: 1;
  text-align: right;
}

.input-unit {
  font-size: 24rpx;
  color: #999999;
  flex-shrink: 0;
}

.picker-value {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: #333333;
}

.picker-value .placeholder {
  color: #CCCCCC;
}

.switch-setting {
  .setting-label {
    flex: 1;
  }
}

// 连接类型选择器
.connection-type-selector {
  display: flex;
  gap: 20rpx;
}

.connection-type-item {
  flex: 1;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  position: relative;

  &.active {
    border-color: #667eea;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  }
}

.type-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 50%;
}

.type-name {
  font-size: 28rpx;
  color: #333333;
  font-weight: 600;
}

.type-check {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
}

// 测试连接卡片
.test-connection-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
}

.test-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.test-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.test-desc {
  display: block;
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 30rpx;
}

.btn-test {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  background: #ffffff;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #667eea;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  &[disabled] {
    opacity: 0.6;
  }
}

.test-result {
  margin-top: 30rpx;
  padding: 20rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.result-text {
  font-size: 26rpx;
  color: #ffffff;
  font-weight: 600;
}

.result-item.success .result-text {
  color: #52c41a;
}

.result-item.failed .result-text {
  color: #ff4d4f;
}

.result-detail {
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.detail-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
}
</style>
