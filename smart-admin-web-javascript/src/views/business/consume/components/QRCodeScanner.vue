<template>
  <div class="qr-scanner">
    <a-modal
      :open="open"
      :title="title"
      :width="400"
      :footer="null"
      @cancel="handleCancel"
    >
      <div class="scanner-content">
        <div class="scanner-container">
          <video
            ref="videoRef"
            class="scanner-video"
            :class="{ 'scanning': isScanning }"
            autoplay
            muted
            playsinline
          ></video>
          <canvas ref="canvasRef" class="scanner-canvas"></canvas>
        </div>

        <div class="scanner-info">
          <div v-if="isScanning" class="scanning-indicator">
            <div class="spinner"></div>
            <span>正在扫描二维码...</span>
          </div>
          <div v-else-if="scanResult" class="result-display">
            <div class="result-icon" :class="{ success: scanResult.success, error: !scanResult.success }">
              <Icon :type="scanResult.success ? 'ios-checkmark-circle' : 'ios-close-circle'" size="32" />
            </div>
            <div class="result-content">
              <div class="result-title">
                {{ scanResult.success ? '扫描成功' : '扫描失败' }}
              </div>
              <div class="result-message">{{ scanResult.message }}</div>
              <div v-if="scanResult.data" class="result-data">
                <div v-if="scanResult.data.type === 'PRODUCT'" class="product-info">
                  <div class="product-name">{{ scanResult.data.name }}</div>
                  <div class="product-price">¥{{ scanResult.data.price }}</div>
                </div>
                <div v-else class="general-info">
                  <pre>{{ JSON.stringify(scanResult.data, null, 2) }}</pre>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="instruction">
            <div class="instruction-icon">
              <Icon type="ios-camera" size="48" />
            </div>
            <div class="instruction-text">
              <p>请将二维码放入框内进行扫描</p>
              <p class="small-text">支持商品码、订单码等多种格式</p>
            </div>
          </div>
        </div>

        <div class="scanner-actions">
          <a-button @click="startScanning" :loading="isScanning">
            <template #icon><Icon type="ios-camera" /></template>
            {{ isScanning ? '停止扫描' : '开始扫描' }}
          </a-button>
          <a-button @click="selectFromFile" :disabled="isScanning">
            <template #icon><Icon type="ios-folder" /></template>
            从文件选择
          </a-button>
        </div>

        <!-- 隐藏的文件输入 -->
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleFileSelect"
        />
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { onMounted, onUnmounted } from 'vue'

// Props
const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '二维码扫描'
  }
})

// Emits
const emit = defineEmits(['scan', 'cancel'])

// 响应式数据
const videoRef = ref()
const canvasRef = ref()
const fileInputRef = ref()

const isScanning = ref(false)
const scanResult = ref(null)

const scanningStream = ref(null)
const animationFrameId = ref(null)

// 监听props变化
watch(() => props.open, (newVal) => {
  if (newVal) {
    initScanner()
  } else {
    stopScanning()
    scanResult.value = null
  }
})

// 生命周期
onMounted(() => {
  checkCameraSupport()
})

onUnmounted(() => {
  stopScanning()
})

// 检查摄像头支持
const checkCameraSupport = async () => {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    const videoDevices = devices.filter(device => device.kind === 'videoinput')

    if (videoDevices.length === 0) {
      console.warn('未找到摄像头设备')
    } else {
      console.log('找到摄像头设备:', videoDevices)
    }
  } catch (error) {
    console.error('检查摄像头支持失败:', error)
  }
}

// 初始化扫描器
const initScanner = () => {
  requestAnimationFrame(() => {
    if (videoRef.value && canvasRef.value) {
      // 初始化Canvas尺寸
      const video = videoRef.value
      const canvas = canvasRef.value

      canvas.width = 300
      canvas.height = 300
      canvas.style.width = '300px'
      canvas.style.height = '300px'

      // 开始摄像头预览
      startCameraPreview()
    }
  })
}

// 开始摄像头预览
const startCameraPreview = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: 'environment'
      }
    })

    if (videoRef.value) {
      videoRef.value.srcObject = stream
      scanningStream.value = stream
    }
  } catch (error) {
    console.error('摄像头访问失败:', error)
    const errorMessage = getCameraErrorMessage(error)
    scanResult.value = {
      success: false,
      message: errorMessage,
      data: null
    }
  }
}

// 获取摄像头错误信息
const getCameraErrorMessage = (error) => {
  if (error.name === 'NotAllowedError') {
    return '摄像头访问被拒绝，请在浏览器设置中允许摄像头权限'
  } else if (error.name === 'NotFoundError') {
    return '未找到摄像头设备'
  } else if (error.name === 'NotReadableError') {
      return '摄像头已被其他应用占用'
  } else if (error.name === 'OverconstrainedError') {
      return '摄像头硬件错误或权限不足'
  } else {
    return '摄像头初始化失败: ' + error.message
  }
}

// 开始扫描
const startScanning = () => {
  if (isScanning.value) {
    stopScanning()
    return
  }

  if (!scanningStream.value) {
    scanResult.value = {
      success: false,
      message: '摄像头未初始化',
      data: null
    }
    return
  }

  isScanning.value = true
  scanResult.value = null

  // 开始扫描循环
  scanQRCode()
}

// 停止扫描
const stopScanning = () => {
  isScanning.value = false

  if (animationFrameId.value) {
    cancelAnimationFrame(animationFrame.value)
    animationFrameId.value = null
  }
}

// 扫描二维码
const scanQRCode = () => {
  if (!videoRef.value || !canvasRef.value || !isScanning.value) {
    return
  }

  const video = videoRef.value
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')

  const scan = () => {
    if (video.readyState === video.HAVE_ENOUGH_DATA) {
      context.drawImage(video, 0, 0, canvas.width, canvas.height)

      try {
        // TODO: 这里应该使用真实的二维码扫描库，如jsQR
        // const imageData = context.getImageData(0, 0, canvas.width, canvas.height)
        // const code = jsQR(imageData.data, imageData.width, imageData.height)

        // 模拟扫描过程
        if (Math.random() < 0.05) { // 5%的概率模拟扫描成功
          simulateQRScan()
        }
      } catch (error) {
        console.error('二维码扫描错误:', error)
      }

      if (isScanning.value) {
        animationFrameId.value = requestAnimationFrame(scan)
      }
    } else {
      if (isScanning.value) {
        animationFrameId.value = requestAnimationFrame(scan)
      }
    }
  }

  scan()
}

// 模拟二维码扫描（用于演示）
const simulateQRScan = () => {
  // 模拟不同类型的二维码扫描结果
  const types = ['PRODUCT', 'ORDER', 'USER', 'DEVICE']
  const type = types[Math.floor(Math.random() * types.length)]

  let data = null
  let message = ''

  switch (type) {
    case 'PRODUCT':
      data = {
        type: 'PRODUCT',
        code: 'P' + Date.now().toString().slice(-8),
        name: '精选商品',
        price: (Math.random() * 100 + 1).toFixed(2)
      }
      message = '商品扫描成功'
      break
    case 'ORDER':
      data = {
        type: 'ORDER',
        code: 'O' + Date.now().toString().slice(-8),
        orderId: 'ORD' + Date.now().toString().slice(-10)
      }
      message = '订单扫描成功'
      break
    case 'USER':
      data = {
        type: 'USER',
        code: 'U' + Date.now().toString().slice(-8),
        userId: 'USER' + Date.now().toString().slice(-6)
      }
      message = '用户信息扫描成功'
      break
    case 'DEVICE':
      data = {
        type: 'DEVICE',
        code: 'D' + Date.now().toString().slice(-8),
        deviceId: 'DEVICE' + Date.now().toString().slice(-6)
      }
      message = '设备信息扫描成功'
      break
    default:
      data = {
        type: 'UNKNOWN',
        code: 'Q' + Date.now().toString().slice(-8)
      }
      message = '二维码识别成功'
  }

  scanResult.value = {
    success: true,
    message: message,
    data: data
  }

  // 3秒后重置状态
  setTimeout(() => {
    if (scanResult.value?.success) {
      emit('scan', scanResult.value)
      scanResult.value = null
    }
  }, 3000)
}

// 从文件选择
const selectFromFile = () => {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    scanResult.value = {
      success: false,
      message: '请选择图片文件',
      data: null
    }
    return
  }

  // 创建FileReader读取文件
  const reader = new FileReader()
  reader.onload = (e) => {
    const img = new Image()
    img.onload = () => {
      try {
        // TODO: 使用真实的二维码扫描库
        // const code = jsQR(img)
        simulateFileScan()
      } catch (error) {
        console.error('图片二维码扫描错误:', error)
        scanResult.value = {
          success: false,
          message: '无法识别图片中的二维码',
          data: null
        }
      }
    }
    img.src = e.target.result
  }
  reader.readAsDataURL(file)
}

// 模拟文件扫描
const simulateFileScan = () => {
  // 模拟文件扫描结果
  const types = ['PRODUCT', 'ORDER', 'USER']
  const type = types[Math.floor(Math.random() * types.length)]

  let data = null
  let message = ''

  switch (type) {
    case 'PRODUCT':
      data = {
        type: 'PRODUCT',
        code: 'FILE_' + Date.now().toString().slice(-6),
        name: '文件识别商品',
        price: (Math.random() * 50 + 10).toFixed(2)
      }
      message = '文件商品识别成功'
      break
    case 'ORDER':
      data = {
        type: 'ORDER',
        code: 'FILE_' + Date.now().toString().slice(-6),
        orderId: 'FILE_ORD' + Date.now().toString().slice(-8)
      }
      message = '文件订单识别成功'
      break
    case 'USER':
      data = {
        type: 'USER',
        code: 'FILE_' + Date.now().toString().slice(-6),
        userId: 'FILE_USER' + Date.now().toString().slice(-4)
      }
      message = '文件用户信息识别成功'
      break
    default:
      data = {
        type: 'UNKNOWN',
        code: 'FILE_' + Date.now().toString().slice(-6)
      }
      message = '文件二维码识别成功'
  }

  scanResult.value = {
    success: true,
    message: message,
    data: data
  }

  emit('scan', scanResult.value)
}

// 取消
const handleCancel = () => {
  stopScanning()
  emit('cancel')
}
</script>

<style scoped>
.scanner-content {
  text-align: center;
  padding: 20px 0;
}

.scanner-container {
  position: relative;
  width: 300px;
  height: 300px;
  margin: 0 auto;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
}

.scanner-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.scanner-canvas {
  display: none;
}

.scanner-info {
  margin-top: 20px;
  min-height: 100px;
}

.scanning-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #667eea;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e2e8f0;
  border-top: 3px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.result-display {
  text-align: center;
}

.result-icon {
  margin-bottom: 12px;
}

.result-icon.success {
  color: #52c41a;
}

.result-icon.error {
  color: #f5222d;
}

.result-content {
  color: #333;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.result-message {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.result-data {
  margin-top: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 6px;
  text-align: left;
}

.product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  font-weight: 500;
}

.product-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.general-info pre {
  margin: 0;
  font-size: 12px;
  color: #666;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f8fafc;
  padding: 8px;
  border-radius: 4px;
  max-height: 100px;
  overflow-y: auto;
}

.instruction {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 150px;
  color: #999;
}

.instruction-icon {
  color: #ccc;
  margin-bottom: 12px;
}

.instruction-text {
  text-align: center;
}

.small-text {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
}

.scanner-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}
</style>