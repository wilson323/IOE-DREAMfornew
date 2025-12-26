<template>
  <view class="device-settings-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="nav-back" @click="goBack">
          <uni-icons type="back" size="20" color="#fff"></uni-icons>
        </view>
        <text class="nav-title">高级设置</text>
        <view class="nav-action" @click="saveSettings">
          <text class="save-text">保存</text>
        </view>
      </view>
    </view>

    <!-- 主内容区 -->
    <scroll-view scroll-y class="content-scroll" :style="{ paddingTop: navbarHeight + 'px' }">
      <!-- 验证参数 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">验证参数</text>
          <text class="section-desc">设置开门验证方式</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">验证方式</text>
          <picker mode="selector" :range="verifyModes" @change="onVerifyModeChange">
            <view class="setting-value">
              <text class="value-text">{{ currentVerifyMode }}</text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <view class="setting-item">
          <text class="setting-label">刷卡开门</text>
          <switch
            :checked="settings.cardEnabled"
            color="#667eea"
            @change="settings.cardEnabled = $event.detail.value"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">密码开门</text>
          <switch
            :checked="settings.passwordEnabled"
            color="#667eea"
            @change="settings.passwordEnabled = $event.detail.value"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">人脸开门</text>
          <switch
            :checked="settings.faceEnabled"
            color="#667eea"
            @change="settings.faceEnabled = $event.detail.value"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">指纹开门</text>
          <switch
            :checked="settings.fingerprintEnabled"
            color="#667eea"
            @change="settings.fingerprintEnabled = $event.detail.value"
          />
        </view>
      </view>

      <!-- 门禁参数 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">门禁参数</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">开门时间(秒)</text>
          <input
            v-model="settings.openTime"
            class="setting-input"
            type="number"
            placeholder="3"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">门磁检测</text>
          <switch
            :checked="settings.doorSensorEnabled"
            color="#667eea"
            @change="settings.doorSensorEnabled = $event.detail.value"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">超时报警(秒)</text>
          <input
            v-model="settings.timeoutAlarm"
            class="setting-input"
            type="number"
            placeholder="30"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">胁迫码</text>
          <input
            v-model="settings.duressCode"
            class="setting-input"
            type="number"
            placeholder="输入胁迫码"
            maxlength="10"
          />
        </view>
      </view>

      <!-- 反潜回设置 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">反潜回(APB)</text>
          <text class="section-desc">防止重复验证</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">启用反潜回</text>
          <switch
            :checked="settings.apbEnabled"
            color="#667eea"
            @change="onApbEnabledChange"
          />
        </view>

        <view v-if="settings.apbEnabled" class="setting-sub-item">
          <text class="setting-label">反潜回模式</text>
          <picker mode="selector" :range="apbModes" @change="onApbModeChange">
            <view class="setting-value">
              <text class="value-text">{{ currentApbMode }}</text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <view v-if="settings.apbEnabled" class="setting-sub-item">
          <text class="setting-label">反潜回区域</text>
          <picker mode="selector" :range="apbAreas" @change="onApbAreaChange">
            <view class="setting-value">
              <text class="value-text">{{ currentApbArea || '请选择' }}</text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>
      </view>

      <!-- 多重验证 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">多重验证</text>
          <text class="section-desc">需要多种方式同时验证</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">启用多重验证</text>
          <switch
            :checked="settings.multiFactorEnabled"
            color="#667eea"
            @change="settings.multiFactorEnabled = $event.detail.value"
          />
        </view>

        <view v-if="settings.multiFactorEnabled" class="info-box">
          <uni-icons type="info" size="16" color="#1890ff"></uni-icons>
          <text class="info-text">启用后，用户需要同时通过两种验证方式才能开门</text>
        </view>
      </view>

      <!-- 时区设置 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">时区设置</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">时区</text>
          <picker mode="selector" :range="timezones" @change="onTimezoneChange">
            <view class="setting-value">
              <text class="value-text">{{ currentTimezone }}</text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>
      </view>

      <!-- NTP服务器 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">NTP服务器</text>
          <text class="section-desc">自动同步时间</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">启用NTP</text>
          <switch
            :checked="settings.ntpEnabled"
            color="#667eea"
            @change="settings.ntpEnabled = $event.detail.value"
          />
        </view>

        <view v-if="settings.ntpEnabled" class="setting-sub-item">
          <text class="setting-label">NTP服务器</text>
          <input
            v-model="settings.ntpServer"
            class="setting-input"
            placeholder="例: pool.ntp.org"
          />
        </view>

        <view v-if="settings.ntpEnabled" class="setting-sub-item">
          <text class="setting-label">同步间隔(分钟)</text>
          <input
            v-model="settings.ntpInterval"
            class="setting-input"
            type="number"
            placeholder="60"
          />
        </view>
      </view>

      <!-- 人脸识别服务器 -->
      <view v-if="settings.faceEnabled" class="form-section">
        <view class="section-header">
          <text class="section-title">人脸识别服务器</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">服务器地址</text>
          <input
            v-model="settings.faceServerUrl"
            class="setting-input"
            placeholder="http://192.168.1.100:8080"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">识别阈值</text>
          <slider
            :value="settings.faceThreshold"
            @change="settings.faceThreshold = $event.detail.value"
            min="50"
            max="100"
            show-value
            active-color="#667eea"
          />
        </view>

        <view class="setting-item">
          <text class="setting-label">活体检测</text>
          <switch
            :checked="settings.livenessEnabled"
            color="#667eea"
            @change="settings.livenessEnabled = $event.detail.value"
          />
        </view>
      </view>

      <!-- Wiegand接口 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">Wiegand接口</text>
        </view>

        <view class="setting-item">
          <text class="setting-label">WG输出格式</text>
          <picker mode="selector" :range="wgFormats" @change="onWgFormatChange">
            <view class="setting-value">
              <text class="value-text">{{ currentWgFormat }}</text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <view class="setting-item">
          <text class="setting-label">WG字节序</text>
          <picker mode="selector" :range="wgByteOrders" @change="onWgByteOrderChange">
            <view class="setting-value">
              <text class="value-text">{{ currentWgByteOrder }}</text>
              <uni-icons type="right" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>
      </view>

      <!-- 扩展参数 -->
      <view class="form-section">
        <view class="section-header">
          <text class="section-title">扩展参数</text>
          <text class="section-desc">JSON格式配置</text>
        </view>

        <view class="extended-params">
          <textarea
            v-model="extendedParamsJson"
            class="params-textarea"
            placeholder='{"key": "value"}'
            maxlength="1000"
          />
          <view class="params-hint">
            <text class="hint-text">输入JSON格式的扩展参数</text>
          </view>
        </view>
      </view>

      <!-- 底部占位 -->
      <view style="height: 120rpx;"></view>
    </scroll-view>

    <!-- 保存提示 -->
    <view v-if="showSaveTip" class="save-tip">
      <text class="tip-text">设置已保存</text>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'

// 导航栏高度
const navbarHeight = ref(44)

// 设备ID（从路由参数获取）
const deviceId = ref('')

// 设置数据
const settings = reactive({
  // 验证参数
  verifyMode: 1,              // 验证方式 1-单验证 2-双重验证 3-多重验证
  cardEnabled: true,          // 刷卡
  passwordEnabled: false,     // 密码
  faceEnabled: true,          // 人脸
  fingerprintEnabled: false,  // 指纹

  // 门禁参数
  openTime: 3,                // 开门时间(秒)
  doorSensorEnabled: true,    // 门磁检测
  timeoutAlarm: 30,           // 超时报警(秒)
  duressCode: '',             // 胁迫码

  // 反潜回
  apbEnabled: false,          // 启用反潜回
  apbMode: 1,                 // 反潜回模式 1-区域APB 2-全局APB
  apbArea: '',                // 反潜回区域

  // 多重验证
  multiFactorEnabled: false,  // 启用多重验证

  // 时区
  timezone: 'Asia/Shanghai',   // 时区

  // NTP
  ntpEnabled: true,           // 启用NTP
  ntpServer: 'pool.ntp.org',  // NTP服务器
  ntpInterval: 60,            // 同步间隔(分钟)

  // 人脸识别
  faceServerUrl: '',          // 人脸服务器地址
  faceThreshold: 80,          // 识别阈值
  livenessEnabled: true,      // 活体检测

  // Wiegand
  wgFormat: 1,                // WG格式 1-WG26 2-WG34 3-WG66
  wgByteOrder: 1              // WG字节序 1-高字节在前 2-低字节在前
})

// 扩展参数JSON
const extendedParamsJson = ref('{}')

// 保存提示
const showSaveTip = ref(false)

// 验证方式列表
const verifyModes = ['单验证', '双重验证', '多重验证']
const currentVerifyMode = computed(() => verifyModes[settings.verifyMode - 1])

// 反潜回模式
const apbModes = ['区域APB', '全局APB']
const currentApbMode = computed(() => apbModes[settings.apbMode - 1])

// 反潜回区域
const apbAreas = ['区域A', '区域B', '区域C', '全局']
const currentApbArea = computed(() => settings.apbArea || '请选择')

// 时区列表
const timezones = [
  'Asia/Shanghai',
  'Asia/Tokyo',
  'America/New_York',
  'Europe/London',
  'UTC'
]
const currentTimezone = computed(() => settings.timezone)

// WG格式
const wgFormats = ['WG26', 'WG34', 'WG66']
const currentWgFormat = computed(() => wgFormats[settings.wgFormat - 1])

// WG字节序
const wgByteOrders = ['高字节在前', '低字节在前']
const currentWgByteOrder = computed(() => wgByteOrders[settings.wgByteOrder - 1])

// 验证方式改变
const onVerifyModeChange = (e) => {
  settings.verifyMode = e.detail.value + 1
}

// 反潜回启用改变
const onApbEnabledChange = (e) => {
  settings.apbEnabled = e.detail.value
  if (!e.detail.value) {
    settings.apbMode = 1
    settings.apbArea = ''
  }
}

// 反潜回模式改变
const onApbModeChange = (e) => {
  settings.apbMode = e.detail.value + 1
}

// 反潜回区域改变
const onApbAreaChange = (e) => {
  settings.apbArea = apbAreas[e.detail.value]
}

// 时区改变
const onTimezoneChange = (e) => {
  settings.timezone = timezones[e.detail.value]
}

// WG格式改变
const onWgFormatChange = (e) => {
  settings.wgFormat = e.detail.value + 1
}

// WG字节序改变
const onWgByteOrderChange = (e) => {
  settings.wgByteOrder = e.detail.value + 1
}

// 保存设置
const saveSettings = async () => {
  try {
    uni.showLoading({ title: '保存中...' })

    const params = { ...settings }

    // 解析扩展参数
    try {
      params.extendedParams = JSON.parse(extendedParamsJson.value)
    } catch (e) {
      uni.hideLoading()
      uni.showToast({ title: '扩展参数JSON格式错误', icon: 'none' })
      return
    }

    const res = await deviceApi.updateDeviceSettings(deviceId.value, params)

    uni.hideLoading()

    if (res.code === 200) {
      // 显示保存成功提示
      showSaveTip.value = true
      setTimeout(() => {
        showSaveTip.value = false
      }, 2000)

      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      uni.showToast({ title: res.message || '保存失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('保存设置失败:', error)
    uni.showToast({ title: '网络错误，请重试', icon: 'none' })
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 加载设备设置
const loadDeviceSettings = async () => {
  try {
    const res = await deviceApi.getDeviceDetail(deviceId.value)
    if (res.code === 200 && res.data) {
      const data = res.data
      // 更新设置
      Object.assign(settings, {
        verifyMode: data.verifyMode || 1,
        cardEnabled: data.cardEnabled ?? true,
        passwordEnabled: data.passwordEnabled ?? false,
        faceEnabled: data.faceEnabled ?? true,
        fingerprintEnabled: data.fingerprintEnabled ?? false,
        openTime: data.openTime || 3,
        doorSensorEnabled: data.doorSensorEnabled ?? true,
        timeoutAlarm: data.timeoutAlarm || 30,
        duressCode: data.duressCode || '',
        apbEnabled: data.apbEnabled ?? false,
        apbMode: data.apbMode || 1,
        apbArea: data.apbArea || '',
        multiFactorEnabled: data.multiFactorEnabled ?? false,
        timezone: data.timezone || 'Asia/Shanghai',
        ntpEnabled: data.ntpEnabled ?? true,
        ntpServer: data.ntpServer || 'pool.ntp.org',
        ntpInterval: data.ntpInterval || 60,
        faceServerUrl: data.faceServerUrl || '',
        faceThreshold: data.faceThreshold || 80,
        livenessEnabled: data.livenessEnabled ?? true,
        wgFormat: data.wgFormat || 1,
        wgByteOrder: data.wgByteOrder || 1
      })

      // 扩展参数
      if (data.extendedParams) {
        extendedParamsJson.value = JSON.stringify(data.extendedParams, null, 2)
      }
    }
  } catch (error) {
    console.error('加载设备设置失败:', error)
  }
}

onMounted(() => {
  // 获取系统信息
  const systemInfo = uni.getSystemInfoSync()
  navbarHeight.value = systemInfo.statusBarHeight + 44

  // 获取设备ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  if (options.deviceId) {
    deviceId.value = options.deviceId
    loadDeviceSettings()
  }
})
</script>

<style lang="scss" scoped>
.device-settings-page {
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

    .nav-action {
      .save-text {
        font-size: 28rpx;
        color: white;
        font-weight: 500;
      }
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
    margin-bottom: 20rpx;

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
  }
}

// 设置项
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

  .setting-value {
    display: flex;
    align-items: center;

    .value-text {
      font-size: 28rpx;
      color: #666;
      margin-right: 8rpx;
    }
  }

  .setting-input {
    flex: 1;
    text-align: right;
    font-size: 28rpx;
    color: #333;
  }
}

// 子设置项（缩进）
.setting-sub-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0 24rpx 40rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .setting-label {
    font-size: 26rpx;
    color: #666;
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

// 扩展参数区
.extended-params {
  margin-top: 10rpx;

  .params-textarea {
    width: 100%;
    min-height: 200rpx;
    padding: 20rpx;
    background: #f6f8fb;
    border-radius: 12rpx;
    font-size: 26rpx;
    color: #333;
    line-height: 1.6;
    font-family: monospace;
  }

  .params-hint {
    margin-top: 12rpx;

    .hint-text {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 保存提示
.save-tip {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  border-radius: 12rpx;
  padding: 30rpx 60rpx;
  z-index: 9999;

  .tip-text {
    font-size: 28rpx;
    color: white;
  }
}
</style>
