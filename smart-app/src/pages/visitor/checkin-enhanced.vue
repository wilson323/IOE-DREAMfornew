<template>
  <view class="visitor-checkin-enhanced">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <text class="back-btn" @click="goBack">‹</text>
      <text class="nav-title">访客登记</text>
      <text class="help-btn" @click="showHelp">帮助</text>
    </view>

    <!-- 身份证扫描区域 -->
    <view class="id-card-section">
      <view class="section-header">
        <text class="section-title">身份信息</text>
        <text class="scan-tip">支持身份证扫描自动填充</text>
      </view>
      
      <view class="scan-buttons">
        <button class="scan-btn camera" @click="scanIDCard">
          <text class="btn-icon">📷</text>
          <text class="btn-text">拍照识别</text>
        </button>
        <button class="scan-btn reader" @click="readIDCard">
          <text class="btn-icon">💳</text>
          <text class="btn-text">读卡器</text>
        </button>
      </view>
    </view>

    <!-- 访客信息表单 -->
    <view class="visitor-form">
      <view class="form-item">
        <text class="form-label">
          <text class="required-mark">*</text>
          姓名
        </text>
        <input
          class="form-input"
          v-model="formData.visitorName"
          placeholder="请输入姓名或扫描身份证"
          :disabled="idCardScanned"
        />
        <text v-if="errors.visitorName" class="error-tip">{{ errors.visitorName }}</text>
      </view>

      <view class="form-item">
        <text class="form-label">
          <text class="required-mark">*</text>
          身份证号
        </text>
        <input
          class="form-input"
          v-model="formData.idCard"
          placeholder="请输入身份证号或扫描"
          :disabled="idCardScanned"
        />
        <text v-if="errors.idCard" class="error-tip">{{ errors.idCard }}</text>
      </view>

      <view class="form-item">
        <text class="form-label">
          <text class="required-mark">*</text>
          手机号
        </text>
        <input
          class="form-input"
          type="tel"
          v-model="formData.phone"
          placeholder="请输入手机号"
        />
        <text v-if="errors.phone" class="error-tip">{{ errors.phone }}</text>
      </view>

      <view class="form-item">
        <text class="form-label">来访公司</text>
        <input
          class="form-input"
          v-model="formData.company"
          placeholder="请输入来访公司"
        />
      </view>

      <view class="form-item">
        <text class="form-label">
          <text class="required-mark">*</text>
          访问事由
        </text>
        <textarea
          class="form-textarea"
          v-model="formData.purpose"
          placeholder="请输入访问事由"
          maxlength="200"
        />
        <text v-if="errors.purpose" class="error-tip">{{ errors.purpose }}</text>
        <text class="char-count">{{ formData.purpose.length }}/200</text>
      </view>
    </view>

    <!-- 拍照区域 -->
    <view class="photo-section">
      <view class="section-header">
        <text class="section-title">访客照片</text>
        <text class="photo-tip">请拍摄访客正面照片</text>
      </view>
      
      <view class="photo-wrapper">
        <view v-if="!photoUrl" class="photo-placeholder" @click="takePhoto">
          <text class="placeholder-icon">📸</text>
          <text class="placeholder-text">点击拍照</text>
        </view>
        <view v-else class="photo-preview">
          <image :src="photoUrl" mode="aspectFill" class="photo-image" />
          <view class="photo-actions">
            <button class="photo-btn retake" @click="takePhoto">
              <text>重拍</text>
            </button>
            <button class="photo-btn delete" @click="deletePhoto">
              <text>删除</text>
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button
        :class="['submit-btn', { disabled: !canSubmit || submitting }]"
        :disabled="!canSubmit || submitting"
        @click="submitCheckIn"
      >
        <text>{{ submitting ? '提交中...' : '确认登记' }}</text>
      </button>
    </view>

    <!-- OCR处理中提示 -->
    <view v-if="ocrProcessing" class="ocr-loading">
      <view class="loading-content">
        <text class="loading-icon">⏳</text>
        <text class="loading-text">正在识别身份证...</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import visitorApi from '@/api/business/visitor/visitor-api'
import { ocrApi } from '@/api/business/visitor/visitor-api'
import { validateRequired, validatePhone, validateIdCard, FormValidator } from '@/utils/form-validation'
import { compressImage } from '@/utils/performance-optimizer'
import idCardReaderManager from '@/utils/idcard-reader'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const ocrProcessing = ref(false)
const idCardScanned = ref(false)
const photoUrl = ref('')

const formData = reactive({
  visitorName: '',
  idCard: '',
  phone: '',
  company: '',
  purpose: ''
})

const errors = reactive({})

// 表单验证规则
const validator = new FormValidator({
  visitorName: [
    { required: true, label: '姓名' },
    { minLength: 2, maxLength: 20, label: '姓名' }
  ],
  idCard: [
    { required: true, label: '身份证号' },
    {
      pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
      message: '身份证号格式不正确'
    }
  ],
  phone: [
    { required: true, label: '手机号' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确'
    }
  ],
  purpose: [
    { required: true, label: '访问事由' },
    { minLength: 5, maxLength: 200, label: '访问事由' }
  ]
})

const canSubmit = computed(() => {
  return formData.visitorName &&
         formData.idCard &&
         formData.phone &&
         formData.purpose &&
         photoUrl.value
})

// 扫描身份证（OCR识别）
const scanIDCard = async () => {
  try {
    // 调用相机拍照
    const imagePath = await chooseImage()

    ocrProcessing.value = true

    // 调用OCR识别API
    const result = await ocrApi.recognizeIdCard(imagePath, 'FRONT')

    if (result.success && result.data) {
      const ocrResult = result.data

      // 自动填充表单
      // 注意：OCR返回的字段名可能因服务提供商而异，需要根据实际返回结果调整
      formData.visitorName = ocrResult.name || ocrResult.visitorName || ''
      formData.idCard = ocrResult.idCard || ocrResult.idCardNumber || ocrResult.idNo || ''

      // 可选：填充其他字段（如果OCR返回）
      if (ocrResult.gender) {
        // 可以根据需要添加性别字段
      }
      if (ocrResult.birthday || ocrResult.birthDate) {
        // 可以根据需要添加生日字段
      }
      if (ocrResult.address || ocrResult.addr) {
        // 可以根据需要添加地址字段
      }

      idCardScanned.value = true
      ocrProcessing.value = false

      uni.showToast({ title: '识别成功', icon: 'success' })
      uni.vibrateShort()
    } else {
      throw new Error(result.message || '识别失败')
    }
  } catch (error) {
    ocrProcessing.value = false
    console.error('身份证识别失败:', error)

    // 降级方案：如果OCR失败，允许手动输入
    uni.showModal({
      title: '识别失败',
      content: error.message || 'OCR识别失败，请手动输入身份证信息',
      showCancel: false,
      confirmText: '确定'
    })
  }
}

// 读取身份证（读卡器）
const readIDCard = async () => {
  try {
    uni.showToast({
      title: '请将身份证放在读卡器上',
      icon: 'none',
      duration: 2000
    })

    ocrProcessing.value = true

    // 检测读卡器是否可用
    const available = await idCardReaderManager.checkReaderAvailable()
    if (!available) {
      throw new Error('未检测到身份证读卡器，请检查硬件连接')
    }

    // 读取身份证信息
    const idCardData = await idCardReaderManager.readIdCard()

    // 自动填充表单
    formData.visitorName = idCardData.name || ''
    formData.idCard = idCardData.idCard || ''

    // 可选：填充其他字段
    if (idCardData.gender) {
      // 可以根据需要添加性别字段
    }
    if (idCardData.birthday) {
      // 可以根据需要添加生日字段
    }
    if (idCardData.address) {
      // 可以根据需要添加地址字段
    }

    idCardScanned.value = true
    ocrProcessing.value = false

    uni.showToast({ title: '读取成功', icon: 'success' })
    uni.vibrateShort()
  } catch (error) {
    ocrProcessing.value = false
    console.error('身份证读卡失败:', error)

    uni.showModal({
      title: '读卡失败',
      content: error.message || '身份证读卡失败，请检查硬件连接或使用OCR识别',
      showCancel: true,
      cancelText: '取消',
      confirmText: '使用OCR',
      success: (res) => {
        if (res.confirm) {
          // 用户选择使用OCR识别
          scanIDCard()
        }
      }
    })
  }
}

// 选择图片
const chooseImage = () => {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      sourceType: ['camera'],
      success: (res) => {
        resolve(res.tempFilePaths[0])
      },
      fail: reject
    })
  })
}

// 拍照
const takePhoto = async () => {
  try {
    const filePath = await chooseImage()

    // 压缩图片
    const compressed = await compressImage(filePath, {
      quality: 80,
      maxWidth: 800,
      maxHeight: 800
    })

    photoUrl.value = compressed
    uni.vibrateShort()
  } catch (error) {
    console.error('拍照失败:', error)
  }
}

// 删除照片
const deletePhoto = () => {
  photoUrl.value = ''
  uni.vibrateShort()
}

// 提交登记
const submitCheckIn = async () => {
  // 表单验证
  const result = validator.validate(formData)
  if (!result.valid) {
    Object.assign(errors, result.errors)
    const firstError = Object.values(result.errors)[0]
    uni.showToast({ title: firstError, icon: 'none' })
    return
  }

  if (!photoUrl.value) {
    uni.showToast({ title: '请拍摄访客照片', icon: 'none' })
    return
  }

  submitting.value = true

  try {
    // 上传照片
    const uploadRes = await uploadPhoto(photoUrl.value)

    // 提交登记
    const checkInData = {
      ...formData,
      photoUrl: uploadRes.url
    }

    const res = await visitorApi.createVisitorRegistration(checkInData)

    if (res.code === 1) {
      uni.showToast({ title: '登记成功', icon: 'success' })
      uni.vibrateLong()

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (error) {
    console.error('登记失败:', error)
    uni.showToast({ title: '登记失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

// 上传照片
const uploadPhoto = (filePath) => {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${import.meta.env.VITE_APP_API_URL}/support/file/upload`,
      filePath,
      name: 'file',
      formData: { folder: 'visitor' },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 1) {
          resolve(data.data)
        } else {
          reject(new Error(data.message))
        }
      },
      fail: reject
    })
  })
}

const showHelp = () => {
  uni.showModal({
    title: '使用帮助',
    content: '1. 点击"拍照识别"扫描访客身份证\n2. 系统自动识别并填充信息\n3. 补充其他必填信息\n4. 拍摄访客正面照片\n5. 点击"确认登记"完成',
    showCancel: false
  })
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-system/variables.scss';

.visitor-checkin-enhanced {
  min-height: 100vh;
  background: $bg-color;
}

.status-bar {
  background: #fff;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1px solid $border-color;
  
  .back-btn {
    font-size: 48rpx;
  }
  
  .nav-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
  }
  
  .help-btn {
    font-size: $font-size-base;
    color: $primary-color;
  }
}

.id-card-section {
  margin: $spacing-md $spacing-md 0;
  background: #fff;
  border-radius: $border-radius-base;
  padding: $spacing-md;
  
  .section-header {
    margin-bottom: $spacing-md;
  }
  
  .section-title {
    display: block;
    font-size: $font-size-md;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin-bottom: 8rpx;
  }
  
  .scan-tip {
    display: block;
    font-size: $font-size-xs;
    color: $text-tertiary;
  }
  
  .scan-buttons {
    display: flex;
    gap: $spacing-md;
    
    .scan-btn {
      flex: 1;
      height: 120rpx;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      border-radius: $border-radius-base;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      box-shadow: $shadow-md;
      
      &:active {
        transform: scale(0.98);
      }
      
      .btn-icon {
        font-size: 48rpx;
        margin-bottom: 8rpx;
      }
      
      .btn-text {
        font-size: $font-size-base;
        color: #fff;
        font-weight: $font-weight-medium;
      }
    }
  }
}

.visitor-form {
  margin: $spacing-md;
  background: #fff;
  border-radius: $border-radius-base;
  padding: $spacing-md;
  
  .form-item {
    margin-bottom: $spacing-lg;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .form-label {
      display: block;
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $text-primary;
      margin-bottom: 12rpx;
      
      .required-mark {
        color: $error-color;
        margin-right: 4rpx;
      }
    }
    
    .form-input {
      height: $input-height-default;
      padding: 0 $spacing-md;
      background: $bg-color;
      border-radius: $border-radius-sm;
      font-size: $font-size-base;
    }
    
    .form-textarea {
      width: 100%;
      min-height: 200rpx;
      padding: $spacing-md;
      background: $bg-color;
      border-radius: $border-radius-sm;
      font-size: $font-size-base;
      line-height: 1.6;
    }
    
    .error-tip {
      display: block;
      font-size: $font-size-xs;
      color: $error-color;
      margin-top: 8rpx;
    }
    
    .char-count {
      display: block;
      font-size: $font-size-xs;
      color: $text-tertiary;
      text-align: right;
      margin-top: 8rpx;
    }
  }
}

.photo-section {
  margin: 0 $spacing-md $spacing-md;
  background: #fff;
  border-radius: $border-radius-base;
  padding: $spacing-md;
  
  .section-header {
    margin-bottom: $spacing-md;
  }
  
  .section-title {
    display: block;
    font-size: $font-size-md;
    font-weight: $font-weight-bold;
    color: $text-primary;
    margin-bottom: 8rpx;
  }
  
  .photo-tip {
    display: block;
    font-size: $font-size-xs;
    color: $text-tertiary;
  }
  
  .photo-wrapper {
    .photo-placeholder {
      height: 400rpx;
      background: $bg-color;
      border: 2rpx dashed $border-color;
      border-radius: $border-radius-base;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      
      &:active {
        background: darken($bg-color, 5%);
      }
      
      .placeholder-icon {
        font-size: 80rpx;
        margin-bottom: 16rpx;
      }
      
      .placeholder-text {
        font-size: $font-size-base;
        color: $text-tertiary;
      }
    }
    
    .photo-preview {
      position: relative;
      
      .photo-image {
        width: 100%;
        height: 400rpx;
        border-radius: $border-radius-base;
      }
      
      .photo-actions {
        display: flex;
        gap: $spacing-md;
        margin-top: $spacing-md;
        
        .photo-btn {
          flex: 1;
          height: 72rpx;
          border: none;
          border-radius: $border-radius-sm;
          font-size: $font-size-base;
          
          &.retake {
            background: $bg-color;
            color: $text-primary;
          }
          
          &.delete {
            background: #fff1f0;
            color: $error-color;
          }
        }
      }
    }
  }
}

.submit-section {
  padding: 0 $spacing-md $spacing-md;
  
  .submit-btn {
    width: 100%;
    height: 120rpx;
    background: linear-gradient(135deg, $success-color 0%, #389e0d 100%);
    border: none;
    border-radius: 60rpx;
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: #fff;
    box-shadow: 0 8rpx 24rpx rgba(82, 196, 26, 0.4);
    
    &:active:not(.disabled) {
      transform: scale(0.98);
    }
    
    &.disabled {
      background: #d9d9d9;
      box-shadow: none;
    }
  }
}

.ocr-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  
  .loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 80rpx;
    background: #fff;
    border-radius: 24rpx;
    
    .loading-icon {
      font-size: 80rpx;
      margin-bottom: 24rpx;
      animation: rotate 1s linear infinite;
    }
    
    .loading-text {
      font-size: $font-size-md;
      color: $text-primary;
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>

