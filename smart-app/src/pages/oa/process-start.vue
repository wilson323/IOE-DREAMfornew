<template>
  <view class="process-start-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">发起{{ processName }}</text>
        <text class="submit-btn" @click="submitProcess">提交</text>
      </view>
    </view>

    <!-- 表单内容 -->
    <scroll-view class="form-scroll" scroll-y>
      <view class="form-container">
        <!-- 动态表单 -->
        <view class="dynamic-form">
          <view
            v-for="(field, index) in formFields"
            :key="index"
            class="form-group"
          >
            <text class="form-label" :class="{ required: field.required }">
              {{ field.label }}
            </text>

            <!-- 输入框 -->
            <input
              v-if="field.type === 'input'"
              class="form-input"
              v-model="formData[field.key]"
              :placeholder="field.placeholder"
              :maxlength="field.maxLength || 100"
            />

            <!-- 文本域 -->
            <textarea
              v-else-if="field.type === 'textarea'"
              class="form-textarea"
              v-model="formData[field.key]"
              :placeholder="field.placeholder"
              :maxlength="field.maxLength || 500"
            ></textarea>

            <!-- 数字输入 -->
            <input
              v-else-if="field.type === 'number'"
              class="form-input"
              v-model="formData[field.key]"
              type="digit"
              :placeholder="field.placeholder"
            />

            <!-- 日期选择 -->
            <view
              v-else-if="field.type === 'date'"
              class="form-picker"
              @click="showDatePicker(field)"
            >
              <text class="picker-text" :class="{ placeholder: !formData[field.key] }">
                {{ formData[field.key] || field.placeholder }}
              </text>
              <text class="picker-arrow">›</text>
            </view>

            <!-- 时间选择 -->
            <view
              v-else-if="field.type === 'time'"
              class="form-picker"
              @click="showTimePicker(field)"
            >
              <text class="picker-text" :class="{ placeholder: !formData[field.key] }">
                {{ formData[field.key] || field.placeholder }}
              </text>
              <text class="picker-arrow">›</text>
            </view>

            <!-- 日期时间选择 -->
            <view
              v-else-if="field.type === 'datetime'"
              class="form-picker"
              @click="showDateTimePicker(field)"
            >
              <text class="picker-text" :class="{ placeholder: !formData[field.key] }">
                {{ formData[field.key] || field.placeholder }}
              </text>
              <text class="picker-arrow">›</text>
            </view>

            <!-- 单选 -->
            <view v-else-if="field.type === 'radio'" class="radio-group">
              <view
                v-for="option in field.options"
                :key="option.value"
                class="radio-item"
                @click="formData[field.key] = option.value"
              >
                <view class="radio-icon" :class="{ checked: formData[field.key] === option.value }">
                  <view class="radio-dot" v-if="formData[field.key] === option.value"></view>
                </view>
                <text class="radio-label">{{ option.label }}</text>
              </view>
            </view>

            <!-- 多选 -->
            <view v-else-if="field.type === 'checkbox'" class="checkbox-group">
              <view
                v-for="option in field.options"
                :key="option.value"
                class="checkbox-item"
                @click="toggleCheckbox(field.key, option.value)"
              >
                <view class="checkbox-icon" :class="{ checked: isCheckboxChecked(field.key, option.value) }">
                  <view class="checkbox-tick" v-if="isCheckboxChecked(field.key, option.value)">✓</view>
                </view>
                <text class="checkbox-label">{{ option.label }}</text>
              </view>
            </view>

            <!-- 下拉选择 -->
            <view
              v-else-if="field.type === 'select'"
              class="form-picker"
              @click="showSelectPicker(field)"
            >
              <text class="picker-text" :class="{ placeholder: !formData[field.key] }">
                {{ formData[field.key] ? getFieldLabel(field, formData[field.key]) : field.placeholder }}
              </text>
              <text class="picker-arrow">›</text>
            </view>

            <!-- 人员选择 -->
            <view
              v-else-if="field.type === 'user'"
              class="form-picker"
              @click="showUserPicker(field)"
            >
              <text class="picker-text" :class="{ placeholder: !formData[field.key] }">
                {{ formData[field.key] ? formData[field.key].name : field.placeholder }}
              </text>
              <text class="picker-arrow">›</text>
            </view>

            <!-- 部门选择 -->
            <view
              v-else-if="field.type === 'department'"
              class="form-picker"
              @click="showDepartmentPicker(field)"
            >
              <text class="picker-text" :class="{ placeholder: !formData[field.key] }">
                {{ formData[field.key] ? formData[field.key].name : field.placeholder }}
              </text>
              <text class="picker-arrow">›</text>
            </view>

            <!-- 文件上传 -->
            <view v-else-if="field.type === 'file'" class="file-upload">
              <view class="upload-area" @click="chooseFile(field)">
                <text class="upload-icon">📎</text>
                <text class="upload-text">选择文件</text>
              </view>
              <view v-if="formData[field.key] && formData[field.key].length > 0" class="file-list">
                <view
                  v-for="(file, fileIndex) in formData[field.key]"
                  :key="fileIndex"
                  class="file-item"
                >
                  <text class="file-icon">📄</text>
                  <text class="file-name">{{ file.name }}</text>
                  <text class="file-size">{{ formatFileSize(file.size) }}</text>
                  <text class="file-remove" @click="removeFile(field.key, fileIndex)">✕</text>
                </view>
              </view>
            </view>

            <!-- 图片上传 -->
            <view v-else-if="field.type === 'image'" class="image-upload">
              <view class="upload-area" @click="chooseImage(field)">
                <text class="upload-icon">📷</text>
                <text class="upload-text">选择图片</text>
              </view>
              <view v-if="formData[field.key] && formData[field.key].length > 0" class="image-list">
                <view
                  v-for="(image, imageIndex) in formData[field.key]"
                  :key="imageIndex"
                  class="image-item"
                >
                  <image class="image-preview" :src="image.url" mode="aspectFill" />
                  <text class="image-remove" @click="removeImage(field.key, imageIndex)">✕</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 审批流程说明 -->
        <view class="process-info">
          <text class="info-title">审批流程</text>
          <view class="process-flow">
            <view
              v-for="(node, index) in processNodes"
              :key="index"
              class="flow-node"
            >
              <view class="node-dot" :class="{ active: index === 0 }"></view>
              <view class="node-content">
                <text class="node-name">{{ node.name }}</text>
                <text class="node-type">{{ node.type }}</text>
              </view>
              <view v-if="index < processNodes.length - 1" class="flow-line"></view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 日期选择器 -->
    <picker
      v-if="showDatePickerModal"
      mode="date"
      :value="currentPickerValue"
      @change="onDateChange"
      @cancel="showDatePickerModal = false"
    ></picker>

    <!-- 时间选择器 -->
    <picker
      v-if="showTimePickerModal"
      mode="time"
      :value="currentPickerValue"
      @change="onTimeChange"
      @cancel="showTimePickerModal = false"
    ></picker>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import oaApi from '@/api/business/oa/oa-api.js'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

// 页面参数
const processId = ref('')
const processName = ref('')

// 页面状态
const loading = ref(false)
const submitting = ref(false)
const showDatePickerModal = ref(false)
const showTimePickerModal = ref(false)
const currentField = ref(null)
const currentPickerValue = ref('')

// 表单数据
const formData = reactive({})
const formFields = ref([])

// 流程节点
const processNodes = ref([])

// 页面生命周期
onMounted(() => {
  init()
})

// 初始化
const init = async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  processId.value = currentPage.options.processId || ''
  processName.value = currentPage.options.processName || '流程'

  if (processId.value) {
    await loadProcessForm()
    await loadProcessFlow()
  }
}

// 加载流程表单
const loadProcessForm = async () => {
  try {
    const res = await oaApi.getProcessForm(processId.value)
    if (res.code === 1 && res.data) {
      formFields.value = res.data.fields || []

      // 初始化表单数据
      formFields.value.forEach(field => {
        if (field.type === 'checkbox') {
          formData[field.key] = []
        } else if (field.type === 'file' || field.type === 'image') {
          formData[field.key] = []
        } else {
          formData[field.key] = field.defaultValue || ''
        }
      })
    }
  } catch (error) {
    console.error('加载流程表单失败:', error)
    uni.showToast({ title: '加载表单失败', icon: 'none' })
  }
}

// 加载流程节点
const loadProcessFlow = async () => {
  try {
    const res = await oaApi.getProcessNodes(processId.value)
    if (res.code === 1 && res.data) {
      processNodes.value = res.data
    }
  } catch (error) {
    console.error('加载流程节点失败:', error)
  }
}

// 显示日期选择器
const showDatePicker = (field) => {
  currentField.value = field
  currentPickerValue.value = formData[field.key] || ''
  showDatePickerModal.value = true
}

// 显示时间选择器
const showTimePicker = (field) => {
  currentField.value = field
  currentPickerValue.value = formData[field.key] || ''
  showTimePickerModal.value = true
}

// 显示日期时间选择器
const showDateTimePicker = (field) => {
  currentField.value = field
  currentPickerValue.value = formData[field.key] || ''
  // 这里可以使用第三方日期时间选择器组件
  uni.showModal({
    title: '选择日期时间',
    content: '请选择日期和时间',
    success: (res) => {
      if (res.confirm) {
        formData[field.key] = new Date().toISOString()
      }
    }
  })
}

// 显示选择器
const showSelectPicker = (field) => {
  const options = field.options.map(option => ({
    text: option.label,
    value: option.value
  }))

  uni.showActionSheet({
    itemList: field.options.map(option => option.label),
    success: (res) => {
      if (res.tapIndex >= 0 && res.tapIndex < field.options.length) {
        formData[field.key] = field.options[res.tapIndex].value
      }
    }
  })
}

// 显示人员选择器
const showUserPicker = (field) => {
  uni.navigateTo({
    url: `/pages/oa/select-user?field=${field.key}`
  })
}

// 显示部门选择器
const showDepartmentPicker = (field) => {
  uni.navigateTo({
    url: `/pages/oa/select-department?field=${field.key}`
  })
}

// 选择文件
const chooseFile = (field) => {
  uni.chooseFile({
    count: field.multiple ? 9 : 1,
    success: (res) => {
      const files = res.tempFiles.map(file => ({
        name: file.name,
        size: file.size,
        path: file.path
      }))

      if (field.multiple) {
        formData[field.key] = [...(formData[field.key] || []), ...files]
      } else {
        formData[field.key] = files
      }
    }
  })
}

// 选择图片
const chooseImage = (field) => {
  uni.chooseImage({
    count: field.multiple ? 9 : 1,
    success: (res) => {
      const images = res.tempFilePaths.map(path => ({
        url: path,
        name: path.split('/').pop(),
        size: 0
      }))

      if (field.multiple) {
        formData[field.key] = [...(formData[field.key] || []), ...images]
      } else {
        formData[field.key] = images
      }
    }
  })
}

// 切换多选框
const toggleCheckbox = (fieldKey, value) => {
  const values = formData[fieldKey] || []
  const index = values.indexOf(value)

  if (index >= 0) {
    values.splice(index, 1)
  } else {
    values.push(value)
  }

  formData[fieldKey] = values
}

// 检查多选框是否选中
const isCheckboxChecked = (fieldKey, value) => {
  const values = formData[fieldKey] || []
  return values.includes(value)
}

// 移除文件
const removeFile = (fieldKey, index) => {
  const files = formData[fieldKey] || []
  files.splice(index, 1)
  formData[fieldKey] = files
}

// 移除图片
const removeImage = (fieldKey, index) => {
  const images = formData[fieldKey] || []
  images.splice(index, 1)
  formData[fieldKey] = images
}

// 日期改变
const onDateChange = (e) => {
  if (currentField.value) {
    formData[currentField.value.key] = e.detail.value
    showDatePickerModal.value = false
  }
}

// 时间改变
const onTimeChange = (e) => {
  if (currentField.value) {
    formData[currentField.value.key] = e.detail.value
    showTimePickerModal.value = false
  }
}

// 获取字段标签
const getFieldLabel = (field, value) => {
  const option = field.options.find(opt => opt.value === value)
  return option ? option.label : value
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(2)} ${units[unitIndex]}`
}

// 提交流程
const submitProcess = async () => {
  // 验证必填字段
  for (const field of formFields.value) {
    if (field.required && !formData[field.key]) {
      uni.showToast({
        title: `请填写${field.label}`,
        icon: 'none'
      })
      return
    }
  }

  try {
    submitting.value = true
    const res = await oaApi.startProcess(processId.value, {
      formData: formData,
      attachments: extractAttachments()
    })

    if (res.code === 1) {
      uni.showToast({ title: '发起成功', icon: 'success' })
      uni.vibrateShort()
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (error) {
    console.error('提交流程失败:', error)
    uni.showToast({ title: '发起失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

// 提取附件
const extractAttachments = () => {
  const attachments = []

  formFields.value.forEach(field => {
    if ((field.type === 'file' || field.type === 'image') && formData[field.key]) {
      attachments.push(...formData[field.key])
    }
  })

  return attachments
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.process-start-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  background: #fff;
}

.nav-bar {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 32rpx;
  }

  .back-btn {
    font-size: 48rpx;
    color: rgba(0, 0, 0, 0.85);
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    flex: 1;
    text-align: center;
  }

  .submit-btn {
    font-size: 28rpx;
    color: #1890ff;
    font-weight: 600;
  }
}

.form-scroll {
  height: calc(100vh - 176rpx);
}

.form-container {
  padding: 32rpx;
}

.dynamic-form {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .form-group {
    margin-bottom: 32rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .form-label {
      font-size: 28rpx;
      color: rgba(0, 0, 0, 0.85);
      display: block;
      margin-bottom: 16rpx;

      &.required::after {
        content: '*';
        color: #ff4d4f;
        margin-left: 4rpx;
      }
    }

    .form-input {
      width: 100%;
      height: 88rpx;
      border: 1px solid #d9d9d9;
      border-radius: 8rpx;
      padding: 0 24rpx;
      font-size: 28rpx;
    }

    .form-textarea {
      width: 100%;
      min-height: 200rpx;
      border: 1px solid #d9d9d9;
      border-radius: 8rpx;
      padding: 24rpx;
      font-size: 28rpx;
    }

    .form-picker {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 88rpx;
      border: 1px solid #d9d9d9;
      border-radius: 8rpx;
      padding: 0 24rpx;

      .picker-text {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        flex: 1;

        &.placeholder {
          color: rgba(0, 0, 0, 0.25);
        }
      }

      .picker-arrow {
        font-size: 32rpx;
        color: rgba(0, 0, 0, 0.45);
      }
    }

    .radio-group {
      .radio-item {
        display: flex;
        align-items: center;
        padding: 16rpx 0;

        .radio-icon {
          width: 40rpx;
          height: 40rpx;
          border: 2rpx solid #d9d9d9;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 24rpx;

          &.checked {
            border-color: #1890ff;
            background: #1890ff;

            .radio-dot {
              width: 12rpx;
              height: 12rpx;
              background: #fff;
              border-radius: 50%;
            }
          }
        }

        .radio-label {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.85);
        }
      }
    }

    .checkbox-group {
      .checkbox-item {
        display: flex;
        align-items: center;
        padding: 16rpx 0;

        .checkbox-icon {
          width: 40rpx;
          height: 40rpx;
          border: 2rpx solid #d9d9d9;
          border-radius: 8rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 24rpx;

          &.checked {
            border-color: #1890ff;
            background: #1890ff;

            .checkbox-tick {
              font-size: 24rpx;
              color: #fff;
            }
          }
        }

        .checkbox-label {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.85);
        }
      }
    }

    .file-upload,
    .image-upload {
      .upload-area {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 200rpx;
        border: 2rpx dashed #d9d9d9;
        border-radius: 8rpx;
        background: #fafafa;

        .upload-icon {
          font-size: 48rpx;
          margin-bottom: 16rpx;
        }

        .upload-text {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .file-list,
      .image-list {
        margin-top: 16rpx;

        .file-item,
        .image-item {
          display: flex;
          align-items: center;
          padding: 16rpx;
          background: #f5f5f5;
          border-radius: 8rpx;
          margin-bottom: 12rpx;

          .file-icon,
          .image-preview {
            width: 40rpx;
            height: 40rpx;
            margin-right: 16rpx;
            border-radius: 4rpx;
          }

          .image-preview {
            width: 60rpx;
            height: 60rpx;
          }

          .file-name {
            font-size: 26rpx;
            color: rgba(0, 0, 0, 0.85);
            flex: 1;
          }

          .file-size {
            font-size: 24rpx;
            color: rgba(0, 0, 0, 0.45);
            margin-right: 16rpx;
          }

          .file-remove,
          .image-remove {
            font-size: 32rpx;
            color: #ff4d4f;
          }
        }
      }
    }
  }
}

.process-info {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .info-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    display: block;
    margin-bottom: 24rpx;
  }

  .process-flow {
    .flow-node {
      display: flex;
      align-items: flex-start;
      margin-bottom: 24rpx;
      position: relative;

      &:last-child {
        margin-bottom: 0;
      }

      .node-dot {
        width: 24rpx;
        height: 24rpx;
        border-radius: 50%;
        background: #d9d9d9;
        margin-right: 24rpx;
        margin-top: 8rpx;
        z-index: 2;

        &.active {
          background: #1890ff;
        }
      }

      .node-content {
        flex: 1;

        .node-name {
          font-size: 28rpx;
          font-weight: 600;
          color: rgba(0, 0, 0, 0.85);
          display: block;
          margin-bottom: 4rpx;
        }

        .node-type {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .flow-line {
        position: absolute;
        left: 11rpx;
        top: 32rpx;
        bottom: -24rpx;
        width: 2rpx;
        background: #e8e8e8;
        z-index: 1;
      }
    }
  }
}
</style>