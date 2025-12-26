<template>
  <view class="device-add-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="nav-back" @click="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">添加设备</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="content-scroll" :style="{ paddingTop: navbarHeight + 'px' }">
      <!-- 设备类型选择 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">设备类型</text>
          <text class="required">*</text>
        </view>

        <view class="type-cards">
          <view
            v-for="type in deviceTypes"
            :key="type.value"
            :class="['type-card', { active: formData.deviceType === type.value }]"
            @click="selectDeviceType(type.value)"
          >
            <view class="type-icon">
              <uni-icons :type="type.icon" size="32" :color="formData.deviceType === type.value ? '#667eea' : '#999'"></uni-icons>
            </view>
            <text class="type-name">{{ type.label }}</text>
            <text class="type-desc">{{ type.desc }}</text>
          </view>
        </view>
      </view>

      <!-- 连接方式选择 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">连接方式</text>
          <text class="required">*</text>
        </view>

        <view class="comm-type-selector">
          <view
            :class="['comm-type-item', { active: formData.commType === 1 }]"
            @click="formData.commType = 1"
          >
            <uni-icons type="wifi" size="20" color="#667eea"></uni-icons>
            <text class="comm-type-label">TCP/IP</text>
          </view>
          <view
            :class="['comm-type-item', { active: formData.commType === 2 }]"
            @click="formData.commType = 2"
          >
            <uni-icons type="link" size="20" color="#667eea"></uni-icons>
            <text class="comm-type-label">RS485</text>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">基本信息</text>
        </view>

        <view class="form-item">
          <text class="form-label">设备名称</text>
          <text class="required">*</text>
          <input
            v-model="formData.deviceName"
            class="form-input"
            placeholder="请输入设备名称"
            maxlength="50"
          />
        </view>

        <view class="form-item">
          <text class="form-label">设备编码</text>
          <text class="required">*</text>
          <input
            v-model="formData.deviceCode"
            class="form-input"
            placeholder="请输入设备编码"
            maxlength="50"
          />
        </view>

        <view class="form-item">
          <text class="form-label">设备型号</text>
          <input
            v-model="formData.deviceModel"
            class="form-input"
            placeholder="请输入设备型号"
            maxlength="50"
          />
        </view>

        <view class="form-item" @click="showAreaPicker">
          <text class="form-label">所属区域</text>
          <text class="required">*</text>
          <view class="form-value">
            <text :class="['value-text', { placeholder: !selectedArea }]">
              {{ selectedArea ? selectedArea.areaName : '请选择区域' }}
            </text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>

        <view class="form-item">
          <text class="form-label">位置描述</text>
          <textarea
            v-model="formData.locationDesc"
            class="form-textarea"
            placeholder="请输入设备安装位置"
            maxlength="200"
          />
        </view>
      </view>

      <!-- TCP/IP配置 -->
      <view v-if="formData.commType === 1" class="form-section">
        <view class="section-header">
          <text class="section-title">网络配置</text>
        </view>

        <view class="form-item">
          <text class="form-label">IP地址</text>
          <text class="required">*</text>
          <input
            v-model="formData.ipAddress"
            class="form-input"
            placeholder="例: 192.168.1.100"
          />
        </view>

        <view class="form-item">
          <text class="form-label">端口号</text>
          <text class="required">*</text>
          <input
            v-model="formData.port"
            class="form-input"
            placeholder="例: 8000"
            type="number"
          />
        </view>

        <view class="form-item">
          <text class="form-label">设备MAC</text>
          <input
            v-model="formData.macAddress"
            class="form-input"
            placeholder="例: AA:BB:CC:DD:EE:FF"
          />
        </view>
      </view>

      <!-- RS485配置 -->
      <view v-if="formData.commType === 2" class="form-section">
        <view class="section-header">
          <text class="section-title">串口配置</text>
        </view>

        <view class="form-item">
          <text class="form-label">串口号</text>
          <text class="required">*</text>
          <picker mode="selector" :range="serialPorts" @change="onSerialPortChange">
            <view class="form-value">
              <text :class="['value-text', { placeholder: !formData.serialPort }]">
                {{ formData.serialPort || '请选择串口号' }}
              </text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">波特率</text>
          <picker mode="selector" :range="baudRates" @change="onBaudRateChange">
            <view class="form-value">
              <text :class="['value-text', { placeholder: !formData.baudRate }]">
                {{ formData.baudRate || '请选择波特率' }}
              </text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="form-label">设备地址</text>
          <text class="required">*</text>
          <input
            v-model="formData.deviceAddress"
            class="form-input"
            placeholder="请输入设备地址(1-255)"
            type="number"
          />
        </view>
      </view>

      <!-- 自动推送配置 -->
      <view v-if="isPushDevice" class="form-section">
        <view class="section-header">
          <text class="section-title">自动推送配置</text>
          <text class="section-desc">Push设备会主动连接服务器</text>
        </view>

        <view class="switch-item">
          <text class="switch-label">启用自动推送</text>
          <switch
            :checked="formData.autoPush"
            color="#667eea"
            @change="onAutoPushChange"
          />
        </view>

        <view v-if="formData.autoPush" class="info-box">
          <uni-icons type="info" size="16" color="#1890ff"></uni-icons>
          <text class="info-text">启用后，设备将主动连接服务器进行数据同步</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view style="height: 120rpx;"></view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <button class="action-btn secondary" @click="goBack">取消</button>
      <button class="action-btn primary" :disabled="submitting" @click="handleSubmit">
        {{ submitting ? '提交中...' : '确定添加' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'

// 导航栏高度
const navbarHeight = ref(44)

// 表单数据
const formData = reactive({
  deviceType: 1,           // 设备类型 1-门禁控制器 2-门禁一体机
  commType: 1,             // 连接方式 1-TCP/IP 2-RS485
  deviceName: '',
  deviceCode: '',
  deviceModel: '',
  areaId: null,
  locationDesc: '',
  ipAddress: '',
  port: '8000',
  macAddress: '',
  serialPort: '',
  baudRate: '',
  deviceAddress: '',
  autoPush: false          // 自动推送
})

// 选中的区域
const selectedArea = ref(null)

// 提交状态
const submitting = ref(false)

// 设备类型列表
const deviceTypes = [
  {
    value: 1,
    label: '门禁控制器',
    desc: '控制门的开关',
    icon: 'locked'
  },
  {
    value: 2,
    label: '门禁一体机',
    desc: '集成识别和控制',
    icon: 'checkmarkempty'
  }
]

// 串口号列表
const serialPorts = ['COM1', 'COM2', 'COM3', 'COM4', 'COM5', 'COM6', 'COM7', 'COM8']

// 波特率列表
const baudRates = ['1200', '2400', '4800', '9600', '19200', '38400', '57600', '115200']

// 是否是Push设备
const isPushDevice = computed(() => {
  // 门禁一体机和读卡器通常是Push设备
  return formData.deviceType === 2
})

// 选择设备类型
const selectDeviceType = (type) => {
  formData.deviceType = type
  // 根据设备类型设置默认连接方式
  if (type === 2) {
    formData.autoPush = true  // 一体机默认启用自动推送
  }
}

// 显示区域选择器
const showAreaPicker = () => {
  uni.navigateTo({
    url: '/pages/access/area-select?mode=single'
  })
}

// 串口号选择
const onSerialPortChange = (e) => {
  formData.serialPort = serialPorts[e.detail.value]
}

// 波特率选择
const onBaudRateChange = (e) => {
  formData.baudRate = baudRates[e.detail.value]
}

// 自动推送开关
const onAutoPushChange = (e) => {
  formData.autoPush = e.detail.value
}

// 表单验证
const validateForm = () => {
  if (!formData.deviceName) {
    uni.showToast({ title: '请输入设备名称', icon: 'none' })
    return false
  }
  if (!formData.deviceCode) {
    uni.showToast({ title: '请输入设备编码', icon: 'none' })
    return false
  }
  if (!formData.areaId) {
    uni.showToast({ title: '请选择区域', icon: 'none' })
    return false
  }

  if (formData.commType === 1) {
    // TCP/IP验证
    if (!formData.ipAddress) {
      uni.showToast({ title: '请输入IP地址', icon: 'none' })
      return false
    }
    if (!formData.port) {
      uni.showToast({ title: '请输入端口号', icon: 'none' })
      return false
    }
    // IP格式验证
    const ipRegex = /^(\d{1,3}\.){3}\d{1,3}$/
    if (!ipRegex.test(formData.ipAddress)) {
      uni.showToast({ title: 'IP地址格式不正确', icon: 'none' })
      return false
    }
  } else {
    // RS485验证
    if (!formData.serialPort) {
      uni.showToast({ title: '请选择串口号', icon: 'none' })
      return false
    }
    if (!formData.baudRate) {
      uni.showToast({ title: '请选择波特率', icon: 'none' })
      return false
    }
    if (!formData.deviceAddress) {
      uni.showToast({ title: '请输入设备地址', icon: 'none' })
      return false
    }
    const addr = parseInt(formData.deviceAddress)
    if (addr < 1 || addr > 255) {
      uni.showToast({ title: '设备地址范围1-255', icon: 'none' })
      return false
    }
  }

  return true
}

// 提交表单
const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  submitting.value = true

  try {
    const params = {
      deviceName: formData.deviceName,
      deviceCode: formData.deviceCode,
      deviceModel: formData.deviceModel,
      deviceType: formData.deviceType,
      commType: formData.commType,
      areaId: formData.areaId,
      locationDesc: formData.locationDesc,
      autoPush: formData.autoPush
    }

    if (formData.commType === 1) {
      // TCP/IP参数
      params.ipAddress = formData.ipAddress
      params.port = parseInt(formData.port)
      params.macAddress = formData.macAddress
    } else {
      // RS485参数
      params.serialPort = formData.serialPort
      params.baudRate = parseInt(formData.baudRate)
      params.deviceAddress = parseInt(formData.deviceAddress)
    }

    const res = await deviceApi.addDevice(params)

    if (res.code === 200) {
      uni.showToast({ title: '添加成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '添加失败', icon: 'none' })
    }
  } catch (error) {
    console.error('添加设备失败:', error)
    uni.showToast({ title: '网络错误，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 监听区域选择结果
onMounted(() => {
  // 获取系统信息
  const systemInfo = uni.getSystemInfoSync()
  navbarHeight.value = systemInfo.statusBarHeight + 44
})

// 页面显示时检查是否有选中的区域（从区域选择页面返回）
// 在实际项目中需要通过事件总线或全局状态管理
</script>

<style lang="scss" scoped>
.device-add-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 30rpx;

    .nav-back {
      width: 60rpx;
      height: 60rpx;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .nav-title {
      font-size: 36rpx;
      font-weight: 600;
      color: white;
    }

    .nav-placeholder {
      width: 60rpx;
    }
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding: 20rpx 30rpx;
  box-sizing: border-box;
}

// 表单区块
.form-section {
  background: white;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);

  &::before {
    content: '';
    display: block;
    height: 6rpx;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
    border-radius: 24rpx 24rpx 0 0;
    margin: -30rpx -30rpx 20rpx -30rpx;
  }

  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 30rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .section-desc {
      margin-left: 10rpx;
      font-size: 24rpx;
      color: #999;
    }

    .required {
      margin-left: 8rpx;
      color: #ff4d4f;
      font-size: 32rpx;
    }
  }
}

// 设备类型卡片
.type-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;

  .type-card {
    background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
    border-radius: 16rpx;
    padding: 30rpx 20rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: all 0.3s;
    border: 2rpx solid transparent;

    &.active {
      background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
      border-color: #667eea;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
    }

    .type-icon {
      margin-bottom: 16rpx;
    }

    .type-name {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 8rpx;
    }

    .type-desc {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 连接方式选择器
.comm-type-selector {
  display: flex;
  gap: 20rpx;

  .comm-type-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
    border-radius: 16rpx;
    padding: 30rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s;

    &.active {
      background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
      border-color: #667eea;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
    }

    .comm-type-label {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-top: 16rpx;
    }
  }
}

// 表单项
.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .form-label {
    font-size: 28rpx;
    color: #666;
    min-width: 180rpx;
  }

  .required {
    color: #ff4d4f;
    margin-left: 8rpx;
    margin-right: 16rpx;
  }

  .form-input {
    flex: 1;
    font-size: 28rpx;
    color: #333;
    text-align: right;
  }

  .form-textarea {
    flex: 1;
    min-height: 120rpx;
    padding: 16rpx;
    background: #f6f8fb;
    border-radius: 12rpx;
    font-size: 28rpx;
    color: #333;
    margin-top: 16rpx;
    width: 100%;
  }

  .form-value {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    .value-text {
      font-size: 28rpx;
      color: #333;

      &.placeholder {
        color: #999;
      }
    }
  }
}

// 开关项
.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;

  .switch-label {
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
  }
}

// 信息提示框
.info-box {
  display: flex;
  align-items: flex-start;
  background: #e6f7ff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-top: 20rpx;

  .info-text {
    flex: 1;
    font-size: 24rpx;
    color: #1890ff;
    line-height: 1.6;
    margin-left: 12rpx;
  }
}

// 底部按钮
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  background: white;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);

  .action-btn {
    flex: 1;
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 20rpx;
    font-size: 28rpx;
    font-weight: 600;
    border: none;

    &.secondary {
      background: linear-gradient(135deg, #f6f8fb 0%, #e9ecef 100%);
      color: #666;
    }

    &.primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

      &:active {
        opacity: 0.8;
      }

      &[disabled] {
        opacity: 0.5;
      }
    }
  }
}
</style>
