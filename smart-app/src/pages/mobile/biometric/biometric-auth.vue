<template>
  <view class="biometric-auth-page">
    <view class="header">
      <text class="title">生物识别认证</text>
      <text class="subtitle">请验证您的身份以继续操作</text>
    </view>

    <view class="content">
      <!-- 认证类型选择 -->
      <view v-if="!authMode" class="auth-modes">
        <text class="section-title">选择认证方式</text>
        <view class="modes-grid">
          <view
            v-for="mode in availableModes"
            :key="mode.type"
            class="mode-item"
            :class="{ disabled: !mode.available }"
            @click="selectAuthMode(mode)"
          >
            <view class="mode-icon">
              <text class="icon-text">{{ mode.icon }}</text>
            </view>
            <text class="mode-name">{{ mode.name }}</text>
            <text class="mode-desc">{{ mode.description }}</text>
            <text v-if="!mode.available" class="mode-unavailable">设备不支持</text>
          </view>
        </view>
      </view>

      <!-- 人脸识别 -->
      <view v-if="authMode === 'face'" class="auth-section">
        <view class="auth-header">
          <text class="auth-title">人脸识别</text>
          <button class="switch-btn" @click="switchAuthMode">
            <text>切换方式</text>
          </button>
        </view>

        <view class="face-scanner">
          <view class="scanner-frame">
            <camera
              v-if="cameraActive"
              :device-position="'front'"
              :flash="'off'"
              @stop="onCameraStop"
              @error="onCameraError"
              class="camera-view"
            />
            <view v-else class="camera-placeholder">
              <text class="placeholder-icon">📷</text>
              <text class="placeholder-text">准备启动摄像头</text>
            </view>

            <!-- 扫描框 -->
            <view class="scan-frame">
              <view class="scan-corner top-left"></view>
              <view class="scan-corner top-right"></view>
              <view class="scan-corner bottom-left"></view>
              <view class="scan-corner bottom-right"></view>
              <view v-if="scanning" class="scan-line"></view>
            </view>
          </view>

          <view class="scanner-status">
            <text class="status-text">{{ scanStatus }}</text>
            <text v-if="scanTip" class="status-tip">{{ scanTip }}</text>
          </view>
        </view>

        <view class="auth-controls">
          <button class="control-btn start" @click="startFaceScan" :disabled="scanning">
            <text>{{ scanning ? '识别中...' : '开始识别' }}</text>
          </button>
          <button class="control-btn cancel" @click="cancelAuth">
            <text>取消</text>
          </button>
        </view>
      </view>

      <!-- 指纹识别 -->
      <view v-if="authMode === 'fingerprint'" class="auth-section">
        <view class="auth-header">
          <text class="auth-title">指纹识别</text>
          <button class="switch-btn" @click="switchAuthMode">
            <text>切换方式</text>
          </button>
        </view>

        <view class="fingerprint-scanner">
          <view class="fingerprint-icon" :class="{ scanning: fingerprintScanning }">
            <text class="finger-icon">👆</text>
            <view v-if="fingerprintScanning" class="finger-scanning"></view>
          </view>

          <view class="scanner-status">
            <text class="status-text">{{ fingerprintStatus }}</text>
            <text v-if="fingerprintTip" class="status-tip">{{ fingerprintTip }}</text>
          </view>
        </view>

        <view class="auth-controls">
          <button class="control-btn start" @click="startFingerprintScan" :disabled="fingerprintScanning">
            <text>{{ fingerprintScanning ? '验证中...' : '开始验证' }}</text>
          </button>
          <button class="control-btn cancel" @click="cancelAuth">
            <text>取消</text>
          </button>
        </view>
      </view>

      <!-- 虹膜识别 -->
      <view v-if="authMode === 'iris'" class="auth-section">
        <view class="auth-header">
          <text class="auth-title">虹膜识别</text>
          <button class="switch-btn" @click="switchAuthMode">
            <text>切换方式</text>
          </button>
        </view>

        <view class="iris-scanner">
          <view class="iris-frame">
            <camera
              v-if="cameraActive"
              :device-position="'front'"
              :flash="'auto'"
              @stop="onCameraStop"
              @error="onCameraError"
              class="camera-view iris-camera"
            />
            <view v-else class="camera-placeholder">
              <text class="placeholder-icon">👁️</text>
              <text class="placeholder-text">准备虹膜扫描</text>
            </view>

            <!-- 虹膜扫描区域 -->
            <view class="iris-scan-area">
              <view class="iris-circle">
                <view class="iris-inner"></view>
                <view v-if="irisScanning" class="iris-scanning-ring"></view>
              </view>
            </view>
          </view>

          <view class="scanner-status">
            <text class="status-text">{{ irisStatus }}</text>
            <text v-if="irisTip" class="status-tip">{{ irisTip }}</text>
          </view>
        </view>

        <view class="auth-controls">
          <button class="control-btn start" @click="startIrisScan" :disabled="irisScanning">
            <text>{{ irisScanning ? '扫描中...' : '开始扫描' }}</text>
          </button>
          <button class="control-btn cancel" @click="cancelAuth">
            <text>取消</text>
          </button>
        </view>
      </view>

      <!-- 认证结果 -->
      <view v-if="authResult" class="auth-result" :class="authResult.success ? 'success' : 'error'">
        <view class="result-icon">
          <text class="icon-text">{{ authResult.success ? '✅' : '❌' }}</text>
        </view>
        <text class="result-title">{{ authResult.title }}</text>
        <text class="result-message">{{ authResult.message }}</text>

        <view v-if="authResult.success" class="result-actions">
          <button class="action-btn primary" @click="onAuthSuccess">
            <text>继续操作</text>
          </button>
        </view>
        <view v-else class="result-actions">
          <button class="action-btn retry" @click="retryAuth">
            <text>重试</text>
          </button>
          <button class="action-btn switch" @click="switchAuthMode">
            <text>切换方式</text>
          </button>
        </view>
      </view>

      <!-- 设置管理 -->
      <view class="settings-section">
        <text class="section-title">生物识别设置</text>
        <view class="setting-items">
          <view class="setting-item" @click="manageBiometrics">
            <view class="setting-info">
              <text class="setting-name">管理生物特征</text>
              <text class="setting-desc">添加或删除生物特征信息</text>
            </view>
            <text class="setting-arrow">></text>
          </view>

          <view class="setting-item" @click="showSecuritySettings">
            <view class="setting-info">
              <text class="setting-name">安全设置</text>
              <text class="setting-desc">认证失败处理、备用验证等</text>
            </view>
            <text class="setting-arrow">></text>
          </view>

          <view class="setting-item">
            <view class="setting-info">
              <text class="setting-name">生物识别开关</text>
              <text class="setting-desc">启用或禁用生物识别功能</text>
            </view>
            <switch
              :checked="biometricEnabled"
              @change="toggleBiometric"
              color="#667eea"
            />
          </view>
        </view>
      </view>
    </view>

    <!-- 生物特征管理弹窗 -->
    <uni-popup ref="biometricManage" type="bottom" :mask-click="false">
      <view class="manage-popup">
        <view class="manage-header">
          <text class="manage-title">生物特征管理</text>
          <button class="close-btn" @click="closeManage">
            <text class="close-icon">×</text>
          </button>
        </view>
        <view class="manage-content">
          <view class="registered-section">
            <text class="subsection-title">已注册的生物特征</text>
            <view v-if="registeredBiometrics.length === 0" class="empty-registered">
              <text class="empty-icon">📝</text>
              <text class="empty-text">暂无已注册的生物特征</text>
            </view>
            <view v-else class="registered-list">
              <view
                v-for="biometric in registeredBiometrics"
                :key="biometric.id"
                class="registered-item"
              >
                <view class="item-info">
                  <text class="item-type">{{ getBiometricTypeName(biometric.type) }}</text>
                  <text class="item-name">{{ biometric.name }}</text>
                  <text class="item-time">注册时间: {{ formatTime(biometric.createTime) }}</text>
                </view>
                <button class="delete-btn" @click="deleteBiometric(biometric)">
                  <text class="delete-icon">🗑️</text>
                </button>
              </view>
            </view>
          </view>

          <view class="add-section">
            <text class="subsection-title">添加新的生物特征</text>
            <view class="add-options">
              <button
                v-for="type in availableBiometricTypes"
                :key="type.type"
                class="add-option-btn"
                :class="{ disabled: type.disabled }"
                @click="addBiometric(type)"
                :disabled="type.disabled"
              >
                <text class="option-icon">{{ type.icon }}</text>
                <text class="option-name">{{ type.name }}</text>
                <text v-if="type.disabled" class="option-reason">{{ type.reason }}</text>
              </button>
            </view>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
export default {
  name: 'BiometricAuth',
  data() {
    return {
      authMode: null, // face, fingerprint, iris
      cameraActive: false,
      scanning: false,
      fingerprintScanning: false,
      irisScanning: false,
      authResult: null,
      biometricEnabled: true,

      scanStatus: '请将面部对准扫描框',
      scanTip: '保持光线充足，面部清晰可见',

      fingerprintStatus: '请将手指放在指纹传感器上',
      fingerprintTip: '请使用已注册的手指进行验证',

      irisStatus: '请将眼睛对准扫描区域',
      irisTip: '请保持眼睛睁开，注视扫描中心',

      availableModes: [
        {
          type: 'face',
          name: '人脸识别',
          description: '使用面部特征进行身份验证',
          icon: '👤',
          available: true
        },
        {
          type: 'fingerprint',
          name: '指纹识别',
          description: '使用指纹进行快速验证',
          icon: '👆',
          available: true
        },
        {
          type: 'iris',
          name: '虹膜识别',
          description: '高安全性的虹膜特征验证',
          icon: '👁️',
          available: true
        }
      ],

      registeredBiometrics: [
        {
          id: 'face_001',
          type: 'face',
          name: '正面面部',
          createTime: Date.now() - 30 * 24 * 60 * 60 * 1000
        },
        {
          id: 'finger_001',
          type: 'fingerprint',
          name: '右手拇指',
          createTime: Date.now() - 25 * 24 * 60 * 60 * 1000
        }
      ],

      availableBiometricTypes: [
        {
          type: 'face',
          name: '面部识别',
          icon: '👤',
          disabled: false
        },
        {
          type: 'fingerprint',
          name: '指纹识别',
          icon: '👆',
          disabled: false
        },
        {
          type: 'iris',
          name: '虹膜识别',
          icon: '👁️',
          disabled: false,
          reason: '需要专业设备支持'
        }
      ]
    }
  },

  mounted() {
    this.checkBiometricSupport()
    this.loadBiometricSettings()
  },

  methods: {
    async checkBiometricSupport() {
      try {
        // 检查设备生物识别支持
        // #ifdef APP-PLUS
        const biometricManager = uni.requireNativePlugin('Biometric-Manager')
        if (biometricManager) {
          const support = await biometricManager.checkSupport()
          this.updateAvailableModes(support)
        }
        // #endif

        // 检查摄像头支持
        // #ifdef APP-PLUS || H5
        const cameraSupport = await this.checkCameraSupport()
        this.availableModes.forEach(mode => {
          if (mode.type === 'face' || mode.type === 'iris') {
            mode.available = cameraSupport
          }
        })
        // #endif

      } catch (error) {
        console.error('检查生物识别支持失败:', error)
      }
    },

    async checkCameraSupport() {
      try {
        // #ifdef APP-PLUS
        return await uni.checkIsSupportSoterAuthentication()
        // #endif

        // #ifdef H5
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'user' }
        })
        stream.getTracks().forEach(track => track.stop())
        return true
        // #endif

        return true
      } catch (error) {
        console.error('摄像头不支持:', error)
        return false
      }
    },

    updateAvailableModes(support) {
      this.availableModes.forEach(mode => {
        switch (mode.type) {
          case 'fingerprint':
            mode.available = support.fingerprint || false
            break
          case 'face':
            mode.available = support.faceID || support.face || false
            break
          case 'iris':
            mode.available = support.iris || false
            break
        }
      })
    },

    loadBiometricSettings() {
      try {
        const settings = uni.getStorageSync('biometric_settings')
        if (settings) {
          this.biometricEnabled = settings.enabled
        }
      } catch (error) {
        console.error('加载生物识别设置失败:', error)
      }
    },

    saveBiometricSettings() {
      try {
        uni.setStorageSync('biometric_settings', {
          enabled: this.biometricEnabled
        })
      } catch (error) {
        console.error('保存生物识别设置失败:', error)
      }
    },

    selectAuthMode(mode) {
      if (!mode.available) return

      this.authMode = mode.type
      this.authResult = null
      this.initAuthMode()
    },

    initAuthMode() {
      switch (this.authMode) {
        case 'face':
          this.initFaceRecognition()
          break
        case 'fingerprint':
          this.initFingerprintRecognition()
          break
        case 'iris':
          this.initIrisRecognition()
          break
      }
    },

    async initFaceRecognition() {
      try {
        // 启动摄像头
        this.cameraActive = true
        this.scanStatus = '请将面部对准扫描框'
      } catch (error) {
        console.error('初始化人脸识别失败:', error)
        this.authResult = {
          success: false,
          title: '初始化失败',
          message: '无法启动摄像头，请检查权限设置'
        }
      }
    },

    async initFingerprintRecognition() {
      try {
        // #ifdef APP-PLUS
        const fingerprintManager = uni.requireNativePlugin('Fingerprint-Manager')
        if (fingerprintManager) {
          const isAvailable = await fingerprintManager.isAvailable()
          if (!isAvailable) {
            throw new Error('设备不支持指纹识别')
          }
        }
        // #endif

        this.fingerprintStatus = '请将手指放在指纹传感器上'
      } catch (error) {
        console.error('初始化指纹识别失败:', error)
        this.authResult = {
          success: false,
          title: '初始化失败',
          message: '设备不支持指纹识别或权限不足'
        }
      }
    },

    async initIrisRecognition() {
      try {
        this.cameraActive = true
        this.irisStatus = '请将眼睛对准扫描区域'
      } catch (error) {
        console.error('初始化虹膜识别失败:', error)
        this.authResult = {
          success: false,
          title: '初始化失败',
          message: '无法启动虹膜扫描设备'
        }
      }
    },

    async startFaceScan() {
      if (this.scanning) return

      this.scanning = true
      this.scanStatus = '正在识别面部特征...'
      this.scanTip = '请保持面部在扫描框内'

      try {
        // 模拟人脸识别过程
        await this.simulateFaceRecognition()

        // 模拟识别结果
        const success = Math.random() > 0.2 // 80%成功率

        if (success) {
          this.authResult = {
            success: true,
            title: '验证成功',
            message: '面部识别验证通过'
          }

          // 触发震动反馈
          uni.vibrateShort()
        } else {
          this.authResult = {
            success: false,
            title: '验证失败',
            message: '无法识别您的面部特征，请重试'
          }
        }
      } catch (error) {
        console.error('人脸识别失败:', error)
        this.authResult = {
          success: false,
          title: '识别失败',
          message: '人脸识别过程中发生错误'
        }
      } finally {
        this.scanning = false
        this.cameraActive = false
      }
    },

    async simulateFaceRecognition() {
      // 模拟识别延迟
      await new Promise(resolve => setTimeout(resolve, 2000))

      // 模拟进度更新
      this.scanStatus = '分析面部特征...'
      await new Promise(resolve => setTimeout(resolve, 1000))

      this.scanStatus = '匹配生物特征数据库...'
      await new Promise(resolve => setTimeout(resolve, 1000))
    },

    async startFingerprintScan() {
      if (this.fingerprintScanning) return

      this.fingerprintScanning = true
      this.fingerprintStatus = '正在验证指纹...'

      try {
        // #ifdef APP-PLUS
        const fingerprintManager = uni.requireNativePlugin('Fingerprint-Manager')
        if (fingerprintManager) {
          const result = await fingerprintManager.authenticate({
            reason: '请验证您的指纹以继续操作'
          })

          this.authResult = {
            success: result.success,
            title: result.success ? '验证成功' : '验证失败',
            message: result.success ? '指纹验证通过' : result.error || '指纹验证失败'
          }
        } else {
          // 模拟指纹识别
          await this.simulateFingerprintRecognition()
          this.authResult = {
            success: true,
            title: '验证成功',
            message: '指纹验证通过'
          }
        }
        // #else
        // 模拟指纹识别
        await this.simulateFingerprintRecognition()
        this.authResult = {
          success: true,
          title: '验证成功',
          message: '指纹验证通过'
        }
        // #endif

        if (this.authResult.success) {
          uni.vibrateShort()
        }
      } catch (error) {
        console.error('指纹识别失败:', error)
        this.authResult = {
          success: false,
          title: '验证失败',
          message: '指纹验证过程中发生错误'
        }
      } finally {
        this.fingerprintScanning = false
      }
    },

    async simulateFingerprintRecognition() {
      // 模拟识别延迟
      await new Promise(resolve => setTimeout(resolve, 1500))

      this.fingerprintStatus = '读取指纹数据...'
      await new Promise(resolve => setTimeout(resolve, 800))

      this.fingerprintStatus = '匹配指纹模板...'
      await new Promise(resolve => setTimeout(resolve, 700))
    },

    async startIrisScan() {
      if (this.irisScanning) return

      this.irisScanning = true
      this.irisStatus = '正在扫描虹膜特征...'
      this.irisTip = '请保持眼睛注视扫描中心'

      try {
        // 模拟虹膜识别过程
        await this.simulateIrisRecognition()

        // 模拟识别结果
        const success = Math.random() > 0.15 // 85%成功率

        if (success) {
          this.authResult = {
            success: true,
            title: '验证成功',
            message: '虹膜识别验证通过'
          }

          uni.vibrateShort()
        } else {
          this.authResult = {
            success: false,
            title: '验证失败',
            message: '无法识别您的虹膜特征，请重试'
          }
        }
      } catch (error) {
        console.error('虹膜识别失败:', error)
        this.authResult = {
          success: false,
          title: '识别失败',
          message: '虹膜识别过程中发生错误'
        }
      } finally {
        this.irisScanning = false
        this.cameraActive = false
      }
    },

    async simulateIrisRecognition() {
      // 模拟识别延迟
      await new Promise(resolve => setTimeout(resolve, 2500))

      this.irisStatus = '捕获虹膜图像...'
      await new Promise(resolve => setTimeout(resolve, 1200))

      this.irisStatus = '提取虹膜特征...'
      await new Promise(resolve => setTimeout(resolve, 1300))

      this.irisStatus = '匹配虹膜数据库...'
      await new Promise(resolve => setTimeout(resolve, 1000))
    },

    switchAuthMode() {
      this.authMode = null
      this.authResult = null
      this.cameraActive = false
      this.scanning = false
      this.fingerprintScanning = false
      this.irisScanning = false
    },

    cancelAuth() {
      uni.showModal({
        title: '取消认证',
        content: '确定要取消生物识别认证吗？',
        success: (res) => {
          if (res.confirm) {
            this.resetAuth()
            uni.navigateBack()
          }
        }
      })
    },

    resetAuth() {
      this.authMode = null
      this.authResult = null
      this.cameraActive = false
      this.scanning = false
      this.fingerprintScanning = false
      this.irisScanning = false
    },

    retryAuth() {
      this.authResult = null
      this.initAuthMode()
    },

    onAuthSuccess() {
      // 认证成功回调
      uni.showToast({
        title: '认证成功',
        icon: 'success'
      })

      // 返回或继续操作
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    },

    toggleBiometric(e) {
      this.biometricEnabled = e.detail.value
      this.saveBiometricSettings()

      if (!this.biometricEnabled) {
        uni.showToast({
          title: '已关闭生物识别',
          icon: 'success'
        })
      } else {
        uni.showToast({
          title: '已开启生物识别',
          icon: 'success'
        })
      }
    },

    manageBiometrics() {
      this.$refs.biometricManage.open()
    },

    closeManage() {
      this.$refs.biometricManage.close()
    },

    showSecuritySettings() {
      uni.navigateTo({
        url: '/pages/mobile/security/security-settings'
      })
    },

    addBiometric(type) {
      if (type.disabled) return

      uni.showModal({
        title: '添加生物特征',
        content: `确定要添加${type.name}吗？`,
        success: (res) => {
          if (res.confirm) {
            this.registerNewBiometric(type)
          }
        }
      })
    },

    async registerNewBiometric(type) {
      uni.showLoading({
        title: '注册中...'
      })

      try {
        // 模拟注册过程
        await new Promise(resolve => setTimeout(resolve, 3000))

        const newBiometric = {
          id: `${type.type}_${Date.now()}`,
          type: type.type,
          name: `新${type.name}`,
          createTime: Date.now()
        }

        this.registeredBiometrics.push(newBiometric)

        uni.hideLoading()
        uni.showToast({
          title: '注册成功',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '注册失败',
          icon: 'error'
        })
      }
    },

    deleteBiometric(biometric) {
      uni.showModal({
        title: '删除确认',
        content: `确定要删除"${biometric.name}"吗？`,
        success: (res) => {
          if (res.confirm) {
            const index = this.registeredBiometrics.findIndex(b => b.id === biometric.id)
            if (index > -1) {
              this.registeredBiometrics.splice(index, 1)
              uni.showToast({
                title: '删除成功',
                icon: 'success'
              })
            }
          }
        }
      })
    },

    getBiometricTypeName(type) {
      const typeNames = {
        face: '面部识别',
        fingerprint: '指纹识别',
        iris: '虹膜识别'
      }
      return typeNames[type] || type
    },

    formatTime(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleDateString()
    },

    onCameraStop() {
      this.cameraActive = false
    },

    onCameraError(error) {
      console.error('摄像头错误:', error)
      this.cameraActive = false
      this.authResult = {
        success: false,
        title: '摄像头错误',
        message: '无法访问摄像头，请检查权限设置'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.biometric-auth-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  text-align: center;
  color: white;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.subtitle {
  font-size: 28rpx;
  opacity: 0.9;
}

.content {
  padding: 30rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
  display: block;
}

.auth-modes {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.modes-grid {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.mode-item {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-radius: 12rpx;
  background: #f8f9fa;
  border: 2rpx solid transparent;

  &:not(.disabled) {
    &:active {
      background: #e9ecef;
      border-color: #667eea;
    }
  }

  &.disabled {
    opacity: 0.6;
  }
}

.mode-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.icon-text {
  font-size: 40rpx;
}

.mode-info {
  flex: 1;
}

.mode-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 4rpx;
}

.mode-desc {
  font-size: 24rpx;
  color: #666;
  display: block;
}

.mode-unavailable {
  font-size: 22rpx;
  color: #e74c3c;
  background: #fdeaea;
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
}

.auth-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.auth-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.auth-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.switch-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  background: #f8f9fa;
  border: none;
  color: #667eea;
  font-size: 24rpx;
}

.face-scanner,
.fingerprint-scanner,
.iris-scanner {
  margin-bottom: 30rpx;
}

.scanner-frame {
  position: relative;
  width: 300rpx;
  height: 300rpx;
  margin: 0 auto 30rpx;
  border-radius: 20rpx;
  overflow: hidden;
  background: #000;
}

.camera-view {
  width: 100%;
  height: 100%;
}

.camera-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #1a1a1a;
}

.placeholder-icon {
  font-size: 60rpx;
  margin-bottom: 16rpx;
}

.placeholder-text {
  color: #666;
  font-size: 24rpx;
}

.scan-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200rpx;
  height: 200rpx;
  border: 2rpx solid rgba(102, 126, 234, 0.8);
  border-radius: 12rpx;
}

.scan-corner {
  position: absolute;
  width: 30rpx;
  height: 30rpx;
  border: 4rpx solid #667eea;

  &.top-left {
    top: -2rpx;
    left: -2rpx;
    border-right: none;
    border-bottom: none;
  }

  &.top-right {
    top: -2rpx;
    right: -2rpx;
    border-left: none;
    border-bottom: none;
  }

  &.bottom-left {
    bottom: -2rpx;
    left: -2rpx;
    border-right: none;
    border-top: none;
  }

  &.bottom-right {
    bottom: -2rpx;
    right: -2rpx;
    border-left: none;
    border-top: none;
  }
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, #667eea, transparent);
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% { top: 0; }
  100% { top: 100%; }
}

.fingerprint-icon {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30rpx;
  position: relative;
}

.finger-icon {
  font-size: 100rpx;
  color: white;
}

.finger-scanning {
  position: absolute;
  top: -10rpx;
  left: -10rpx;
  right: -10rpx;
  bottom: -10rpx;
  border: 4rpx solid rgba(102, 126, 234, 0.5);
  border-radius: 50%;
  animation: fingerprint-scan 1.5s ease-in-out infinite;
}

@keyframes fingerprint-scan {
  0% { transform: scale(0.9); opacity: 1; }
  100% { transform: scale(1.1); opacity: 0; }
}

.iris-frame {
  position: relative;
  width: 250rpx;
  height: 250rpx;
  margin: 0 auto 30rpx;
  border-radius: 50%;
  overflow: hidden;
  background: #000;
}

.iris-camera {
  width: 100%;
  height: 100%;
}

.iris-scan-area {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 150rpx;
  height: 150rpx;
}

.iris-circle {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3rpx solid rgba(102, 126, 234, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.iris-inner {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.3);
}

.iris-scanning-ring {
  position: absolute;
  top: -20rpx;
  left: -20rpx;
  right: -20rpx;
  bottom: -20rpx;
  border: 2rpx solid transparent;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: iris-scan 1s linear infinite;
}

@keyframes iris-scan {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.scanner-status {
  text-align: center;
}

.status-text {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.status-tip {
  font-size: 22rpx;
  color: #666;
}

.auth-controls {
  display: flex;
  gap: 20rpx;
}

.control-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
  color: white;

  &.start {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    &[disabled] {
      background: #ccc;
    }
  }

  &.cancel {
    background: #95a5a6;
  }
}

.auth-result {
  background: white;
  border-radius: 16rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  &.success {
    border-top: 4rpx solid #27ae60;
  }

  &.error {
    border-top: 4rpx solid #e74c3c;
  }
}

.result-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.result-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.result-message {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 30rpx;
}

.result-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
  color: white;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.retry {
    background: #3498db;
  }

  &.switch {
    background: #95a5a6;
  }
}

.settings-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.setting-items {
  display: flex;
  flex-direction: column;
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
}

.setting-info {
  flex: 1;
}

.setting-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  display: block;
  margin-bottom: 4rpx;
}

.setting-desc {
  font-size: 24rpx;
  color: #666;
}

.setting-arrow {
  font-size: 24rpx;
  color: #666;
}

.manage-popup {
  background: white;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
}

.manage-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.manage-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f8f9fa;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  font-size: 32rpx;
  color: #666;
}

.manage-content {
  padding: 30rpx;
}

.registered-section,
.add-section {
  margin-bottom: 40rpx;
}

.subsection-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.empty-registered {
  text-align: center;
  padding: 60rpx 40rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.empty-icon {
  font-size: 60rpx;
  display: block;
  margin-bottom: 16rpx;
}

.empty-text {
  font-size: 26rpx;
  color: #666;
}

.registered-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.registered-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.item-info {
  flex: 1;
}

.item-type {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 4rpx;
}

.item-name {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-bottom: 4rpx;
}

.item-time {
  font-size: 22rpx;
  color: #999;
}

.delete-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #fdeaea;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon {
  font-size: 24rpx;
}

.add-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.add-option-btn {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border: 2rpx solid transparent;

  &:not(.disabled) {
    &:active {
      background: #e9ecef;
      border-color: #667eea;
    }
  }

  &.disabled {
    opacity: 0.6;
  }
}

.option-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 30rpx;
  color: white;
}

.option-name {
  flex: 1;
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.option-reason {
  font-size: 22rpx;
  color: #e74c3c;
  background: #fdeaea;
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
}
</style>