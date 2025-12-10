<template>
  <view class="smart-form">
    <!-- 表单头部 -->
    <view v-if="title || subtitle" class="smart-form__header">
      <text class="smart-form__title">{{ title }}</text>
      <text v-if="subtitle" class="smart-form__subtitle">{{ subtitle }}</text>
    </view>

    <!-- 表单内容 -->
    <view class="smart-form__content">
      <view
        v-for="(field, index) in fields"
        :key="field.key || index"
        :class="[
          'smart-form__field',
          `smart-form__field--${field.type || 'input'}`,
          { 'smart-form__field--error': getFieldError(field.key) }
        ]"
      >
        <!-- 字段标签 -->
        <view v-if="field.label" class="smart-form__label">
          <text class="smart-form__label-text">
            {{ field.label }}
            <text v-if="field.required" class="smart-form__required">*</text>
          </text>
          <text v-if="field.tooltip" class="smart-form__tooltip" @click="showTooltip(field.tooltip)">
            <text>?</text>
          </text>
        </view>

        <!-- 输入框 -->
        <view v-if="field.type === 'input' || !field.type" class="smart-form__input-wrapper">
          <input
            :type="field.inputType || 'text'"
            :value="formData[field.key]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :maxlength="field.maxlength"
            :class="[
              'smart-form__input',
              { 'smart-form__input--error': getFieldError(field.key) }
            ]"
            @input="handleInput(field.key, $event)"
            @blur="handleBlur(field.key)"
          />
          <view v-if="field.clearable && formData[field.key]" class="smart-form__clear" @click="clearField(field.key)">
            <text>✕</text>
          </view>
        </view>

        <!-- 文本域 -->
        <textarea
          v-else-if="field.type === 'textarea'"
          :value="formData[field.key]"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
          :maxlength="field.maxlength"
          :class="[
            'smart-form__textarea',
            { 'smart-form__textarea--error': getFieldError(field.key) }
          ]"
          :style="{
            height: field.rows ? `${field.rows * 80}rpx` : '160rpx'
          }"
          @input="handleInput(field.key, $event)"
          @blur="handleBlur(field.key)"
        />
        {{ field.rows ? '' : '\n' }}

        <!-- 选择器 -->
        <picker
          v-else-if="field.type === 'select'"
          :value="getSelectValue(field.key, field.options)"
          :range="field.options"
          :range-key="field.rangeKey || 'text'"
          :disabled="field.disabled"
          @change="handleSelect(field.key, $event)"
        >
          <view :class="[
            'smart-form__select',
            { 'smart-form__select--placeholder': !formData[field.key] },
            { 'smart-form__select--error': getFieldError(field.key) }
          ]">
            <text>{{ getSelectText(field.key, field.options) }}</text>
            <text class="smart-form__select-arrow">▼</text>
          </view>
        </picker>

        <!-- 日期选择器 -->
        <picker
          v-else-if="field.type === 'date'"
          mode="date"
          :value="formData[field.key]"
          :disabled="field.disabled"
          @change="handleDateChange(field.key, $event)"
        >
          <view :class="[
            'smart-form__select',
            { 'smart-form__select--placeholder': !formData[field.key] },
            { 'smart-form__select--error': getFieldError(field.key) }
          ]">
            <text>{{ formData[field.key] || field.placeholder || '请选择日期' }}</text>
            <text class="smart-form__select-arrow">📅</text>
          </view>
        </picker>

        <!-- 时间选择器 -->
        <picker
          v-else-if="field.type === 'time'"
          mode="time"
          :value="formData[field.key]"
          :disabled="field.disabled"
          @change="handleTimeChange(field.key, $event)"
        >
          <view :class="[
            'smart-form__select',
            { 'smart-form__select--placeholder': !formData[field.key] },
            { 'smart-form__select--error': getFieldError(field.key) }
          ]">
            <text>{{ formData[field.key] || field.placeholder || '请选择时间' }}</text>
            <text class="smart-form__select-arrow">🕐</text>
          </view>
        </picker>

        <!-- 单选框组 -->
        <view v-else-if="field.type === 'radio'" class="smart-form__radio-group">
          <view
            v-for="(option, optionIndex) in field.options"
            :key="optionIndex"
            :class="[
              'smart-form__radio-item',
              { 'smart-form__radio-item--checked': formData[field.key] === option.value }
            ]"
            @click="handleRadioChange(field.key, option.value)"
          >
            <view class="smart-form__radio-icon">
              <view v-if="formData[field.key] === option.value" class="smart-form__radio-dot"></view>
            </view>
            <text class="smart-form__radio-text">{{ option.text || option.label }}</text>
          </view>
        </view>

        <!-- 多选框组 -->
        <view v-else-if="field.type === 'checkbox'" class="smart-form__checkbox-group">
          <view
            v-for="(option, optionIndex) in field.options"
            :key="optionIndex"
            :class="[
              'smart-form__checkbox-item',
              { 'smart-form__checkbox-item--checked': isCheckboxChecked(field.key, option.value) }
            ]"
            @click="handleCheckboxChange(field.key, option.value)"
          >
            <view class="smart-form__checkbox-icon">
              <text v-if="isCheckboxChecked(field.key, option.value)" class="smart-form__checkbox-check">✓</text>
            </view>
            <text class="smart-form__checkbox-text">{{ option.text || option.label }}</text>
          </view>
        </view>

        <!-- 开关 -->
        <view v-else-if="field.type === 'switch'" class="smart-form__switch-wrapper">
          <view
            :class="[
              'smart-form__switch',
              { 'smart-form__switch--checked': formData[field.key] }
            ]"
            @click="handleSwitchChange(field.key)"
          >
            <view class="smart-form__switch-slider"></view>
          </view>
          <text class="smart-form__switch-text">{{ formData[field.key] ? (field.checkedText || '开启') : (field.uncheckedText || '关闭') }}</text>
        </view>

        <!-- 数字输入框 -->
        <view v-else-if="field.type === 'number'" class="smart-form__input-wrapper">
          <input
            type="number"
            :value="formData[field.key]"
            :placeholder="field.placeholder"
            :disabled="field.disabled"
            :min="field.min"
            :max="field.max"
            :step="field.step"
            :class="[
              'smart-form__input',
              { 'smart-form__input--error': getFieldError(field.key) }
            ]"
            @input="handleInput(field.key, $event)"
            @blur="handleBlur(field.key)"
          />
        </view>

        <!-- 文件上传 -->
        <view v-else-if="field.type === 'upload'" class="smart-form__upload">
          <view class="smart-form__upload-list">
            <view
              v-for="(file, fileIndex) in getUploadFiles(field.key)"
              :key="fileIndex"
              class="smart-form__upload-item"
            >
              <image
                v-if="isImageFile(file)"
                :src="file.url || file.path"
                class="smart-form__upload-preview"
                mode="aspectFill"
                @click="previewImage(file)"
              />
              <view v-else class="smart-form__upload-file">
                <text class="smart-form__upload-file-name">{{ file.name || file.path }}</text>
                <text class="smart-form__upload-file-remove" @click="removeUploadFile(field.key, fileIndex)">✕</text>
              </view>
            </view>
          </view>
          <view v-if="!field.disabled && (!field.maxFiles || getUploadFiles(field.key).length < field.maxFiles)" class="smart-form__upload-btn" @click="chooseFile(field)">
            <text class="smart-form__upload-btn-icon">+</text>
            <text class="smart-form__upload-btn-text">{{ field.uploadText || '选择文件' }}</text>
          </view>
        </view>

        <!-- 自定义字段 -->
        <slot
          v-else
          :name="`field-${field.type}`"
          :field="field"
          :value="formData[field.key]"
          :error="getFieldError(field.key)"
          :onChange="(value) => setFieldValue(field.key, value)"
        />

        <!-- 错误信息 -->
        <view v-if="getFieldError(field.key)" class="smart-form__error">
          <text class="smart-form__error-text">{{ getFieldError(field.key) }}</text>
        </view>

        <!-- 帮助信息 -->
        <view v-if="field.help" class="smart-form__help">
          <text class="smart-form__help-text">{{ field.help }}</text>
        </view>
      </view>
    </view>

    <!-- 表单底部 -->
    <view class="smart-form__footer">
      <view class="smart-form__actions">
        <button
          v-for="(action, index) in actions"
          :key="index"
          :class="[
            'smart-form__action',
            `smart-form__action--${action.type || 'default'}`,
            { 'smart-form__action--loading': action.loading },
            { 'smart-form__action--disabled': action.disabled }
          ]"
          :disabled="action.disabled || action.loading"
          @click="handleActionClick(action, index)"
        >
          <view v-if="action.loading" class="smart-form__action-loading">
            <view class="smart-form__action-spinner"></view>
          </view>
          <text class="smart-form__action-text">{{ action.text }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { DataValidator } from '@/utils/ux-enhancement.js'

// Props定义
const props = defineProps({
  // 表单配置
  fields: {
    type: Array,
    required: true
  },
  initialData: {
    type: Object,
    default: () => ({})
  },

  // 标题
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },

  // 操作按钮
  actions: {
    type: Array,
    default: () => [
      { text: '取消', type: 'default' },
      { text: '提交', type: 'primary', loading: false }
    ]
  },

  // 验证配置
  validateOnChange: {
    type: Boolean,
    default: true
  },
  validateOnBlur: {
    type: Boolean,
    default: true
  }
})

// Emits定义
const emit = defineEmits([
  'submit',
  'cancel',
  'field-change',
  'field-blur',
  'action-click'
])

// 响应式数据
const formData = reactive({})
const fieldErrors = reactive({})
const touchedFields = reactive(new Set())

// 初始化表单数据
const initializeFormData = () => {
  props.fields.forEach(field => {
    if (props.initialData[field.key] !== undefined) {
      formData[field.key] = props.initialData[field.key]
    } else if (field.defaultValue !== undefined) {
      formData[field.key] = field.defaultValue
    } else {
      // 根据字段类型设置默认值
      switch (field.type) {
        case 'checkbox':
          formData[field.key] = field.defaultValue || []
          break
        case 'switch':
          formData[field.key] = field.defaultValue || false
          break
        case 'upload':
          formData[field.key] = field.defaultValue || []
          break
        default:
          formData[field.key] = ''
      }
    }
  })
}

// 监听初始化数据变化
watch(() => props.initialData, (newData) => {
  Object.keys(newData).forEach(key => {
    if (formData.hasOwnProperty(key)) {
      formData[key] = newData[key]
    }
  })
}, { deep: true })

// 初始化
initializeFormData()

// 计算属性
const validationRules = computed(() => {
  const rules = {}
  props.fields.forEach(field => {
    if (field.rules) {
      rules[field.key] = {
        label: field.label,
        rules: field.rules
      }
    }
  })
  return rules
})

// 方法
const handleInput = (fieldKey, event) => {
  const value = event.detail.value
  setFieldValue(fieldKey, value)

  if (props.validateOnChange) {
    validateField(fieldKey)
  }
}

const handleBlur = (fieldKey) => {
  touchedFields.add(fieldKey)

  if (props.validateOnBlur) {
    validateField(fieldKey)
  }

  emit('field-blur', {
    field: fieldKey,
    value: formData[fieldKey]
  })
}

const handleSelect = (fieldKey, event) => {
  const field = props.fields.find(f => f.key === fieldKey)
  const selectedOption = field.options[event.detail.value]
  const value = selectedOption.value || selectedOption

  setFieldValue(fieldKey, value)

  if (props.validateOnChange) {
    validateField(fieldKey)
  }
}

const handleDateChange = (fieldKey, event) => {
  setFieldValue(fieldKey, event.detail.value)
}

const handleTimeChange = (fieldKey, event) => {
  setFieldValue(fieldKey, event.detail.value)
}

const handleRadioChange = (fieldKey, value) => {
  setFieldValue(fieldKey, value)
}

const handleCheckboxChange = (fieldKey, value) => {
  const currentValue = formData[fieldKey] || []
  const newValue = [...currentValue]
  const index = newValue.indexOf(value)

  if (index === -1) {
    newValue.push(value)
  } else {
    newValue.splice(index, 1)
  }

  setFieldValue(fieldKey, newValue)
}

const handleSwitchChange = (fieldKey) => {
  setFieldValue(fieldKey, !formData[fieldKey])
}

const handleActionClick = (action, index) => {
  if (action.disabled || action.loading) {
    return
  }

  if (action.type === 'primary' || action.type === 'submit') {
    // 提交表单
    const validation = validateForm()
    if (validation.valid) {
      emit('submit', {
        data: { ...formData },
        action,
        index
      })
    } else {
      // 显示验证错误
      Object.keys(validation.errors).forEach(fieldKey => {
        fieldErrors[fieldKey] = validation.errors[fieldKey]
      })
    }
  } else if (action.type === 'cancel') {
    emit('cancel', {
      data: { ...formData },
      action,
      index
    })
  }

  emit('action-click', {
    data: { ...formData },
    action,
    index
  })
}

const setFieldValue = (fieldKey, value) => {
  const oldValue = formData[fieldKey]
  formData[fieldKey] = value

  // 清除该字段的错误
  if (fieldErrors[fieldKey]) {
    fieldErrors[fieldKey] = ''
  }

  emit('field-change', {
    field: fieldKey,
    value,
    oldValue
  })
}

const clearField = (fieldKey) => {
  setFieldValue(fieldKey, '')
}

const showTooltip = (tooltip) => {
  uni.showModal({
    title: '提示',
    content: tooltip,
    showCancel: false
  })
}

// 验证相关方法
const validateField = (fieldKey) => {
  const rules = validationRules.value[fieldKey]
  if (!rules) {
    return { valid: true }
  }

  const fieldData = { [fieldKey]: formData[fieldKey] }
  const validation = DataValidator.validateForm(fieldData, { [fieldKey]: rules })

  if (!validation.valid) {
    fieldErrors[fieldKey] = validation.errors[0]
  } else {
    fieldErrors[fieldKey] = ''
  }

  return validation
}

const validateForm = () => {
  return DataValidator.validateForm(formData, validationRules.value)
}

const clearValidation = () => {
  Object.keys(fieldErrors).forEach(key => {
    fieldErrors[key] = ''
  })
  touchedFields.clear()
}

// 选择器相关方法
const getSelectValue = (fieldKey, options) => {
  const value = formData[fieldKey]
  const index = options.findIndex(option => option.value === value || option === value)
  return index === -1 ? 0 : index
}

const getSelectText = (fieldKey, options) => {
  const value = formData[fieldKey]
  const option = options.find(opt => opt.value === value || opt === value)
  return option ? (option.text || option.label || option) : ''
}

// 多选框相关方法
const isCheckboxChecked = (fieldKey, value) => {
  const currentValues = formData[fieldKey] || []
  return currentValues.includes(value)
}

// 文件上传相关方法
const getUploadFiles = (fieldKey) => {
  return formData[fieldKey] || []
}

const isImageFile = (file) => {
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  const fileUrl = file.url || file.path || ''
  return imageExtensions.some(ext => fileUrl.toLowerCase().endsWith(ext))
}

const chooseFile = (field) => {
  uni.chooseImage({
    count: field.maxFiles || 9,
    success: (res) => {
      const newFiles = res.tempFiles.map(file => ({
        path: file.path,
        size: file.size,
        name: file.name || `image_${Date.now()}`
      }))

      const currentFiles = getUploadFiles(field.key) || []
      setFieldValue(field.key, [...currentFiles, ...newFiles])
    }
  })
}

const removeUploadFile = (fieldKey, index) => {
  const currentFiles = getUploadFiles(fieldKey) || []
  currentFiles.splice(index, 1)
  setFieldValue(fieldKey, currentFiles)
}

const previewImage = (file) => {
  uni.previewImage({
    urls: [file.url || file.path],
    current: file.url || file.path
  })
}

// 错误相关方法
const getFieldError = (fieldKey) => {
  return fieldErrors[fieldKey] || ''
}

// 暴露方法给父组件
defineExpose({
  validateForm,
  validateField,
  clearValidation,
  setFieldValue,
  getFormData: () => ({ ...formData }),
  resetForm: () => {
    clearValidation()
    initializeFormData()
  }
})
</script>

<style lang="scss" scoped>
.smart-form {
  background: #ffffff;
  border-radius: 12rpx;
  overflow: hidden;
}

.smart-form__header {
  padding: 32rpx 24rpx 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
  text-align: center;
}

.smart-form__title {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8rpx;
}

.smart-form__subtitle {
  display: block;
  font-size: 26rpx;
  color: #8c8c8c;
}

.smart-form__content {
  padding: 24rpx;
}

.smart-form__field {
  margin-bottom: 40rpx;

  &--error {
    .smart-form__input,
    .smart-form__textarea,
    .smart-form__select {
      border-color: #ff4d4f;
      background: #fff2f0;
    }
  }
}

.smart-form__label {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.smart-form__label-text {
  font-size: 30rpx;
  color: #262626;
  font-weight: 500;
}

.smart-form__required {
  color: #ff4d4f;
  margin-left: 4rpx;
}

.smart-form__tooltip {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e6f7ff;
  color: #1890ff;
  border-radius: 50%;
  font-size: 20rpx;
  cursor: pointer;
}

.smart-form__input-wrapper {
  position: relative;
}

.smart-form__input,
.smart-form__textarea {
  width: 100%;
  padding: 24rpx 20rpx;
  border: 2rpx solid #d9d9d9;
  border-radius: 8rpx;
  font-size: 30rpx;
  color: #262626;
  background: #ffffff;
  transition: all 0.3s ease;

  &:focus {
    border-color: #1890ff;
    box-shadow: 0 0 0 4rpx rgba(24, 144, 255, 0.1);
  }

  &::placeholder {
    color: #bfbfbf;
  }

  &:disabled {
    background: #f5f5f5;
    color: #c0c0c0;
    cursor: not-allowed;
  }

  &--error {
    border-color: #ff4d4f;
    background: #fff2f0;
  }
}

.smart-form__textarea {
  resize: vertical;
  min-height: 160rpx;
}

.smart-form__clear {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 36rpx;
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ddd;
  border-radius: 50%;
  font-size: 20rpx;
  color: #666;
  cursor: pointer;
}

.smart-form__select {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 24rpx 20rpx;
  border: 2rpx solid #d9d9d9;
  border-radius: 8rpx;
  font-size: 30rpx;
  color: #262626;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    border-color: #1890ff;
  }

  &--placeholder {
    color: #bfbfbf;
  }

  &--error {
    border-color: #ff4d4f;
    background: #fff2f0;
  }
}

.smart-form__select-arrow {
  font-size: 24rpx;
  color: #c0c0c0;
}

.smart-form__radio-group {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.smart-form__radio-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  cursor: pointer;
}

.smart-form__radio-icon {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid #d9d9d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.smart-form__radio-dot {
  width: 20rpx;
  height: 20rpx;
  background: #1890ff;
  border-radius: 50%;
}

.smart-form__radio-item--checked {
  .smart-form__radio-icon {
    border-color: #1890ff;
  }
}

.smart-form__radio-text {
  font-size: 30rpx;
  color: #262626;
}

.smart-form__checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.smart-form__checkbox-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  cursor: pointer;
}

.smart-form__checkbox-icon {
  width: 40rpx;
  height: 40rpx;
  border: 3rpx solid #d9d9d9;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.smart-form__checkbox-check {
  color: #1890ff;
  font-size: 24rpx;
  font-weight: bold;
}

.smart-form__checkbox-item--checked {
  .smart-form__checkbox-icon {
    border-color: #1890ff;
    background: #1890ff;
  }
}

.smart-form__checkbox-text {
  font-size: 30rpx;
  color: #262626;
}

.smart-form__switch-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.smart-form__switch {
  width: 96rpx;
  height: 48rpx;
  background: #ccc;
  border-radius: 24rpx;
  position: relative;
  cursor: pointer;
  transition: background-color 0.3s ease;

  &--checked {
    background: #1890ff;
  }
}

.smart-form__switch-slider {
  position: absolute;
  top: 4rpx;
  left: 4rpx;
  width: 40rpx;
  height: 40rpx;
  background: #ffffff;
  border-radius: 50%;
  transition: transform 0.3s ease;
}

.smart-form__switch--checked .smart-form__switch-slider {
  transform: translateX(48rpx);
}

.smart-form__switch-text {
  font-size: 28rpx;
  color: #666;
}

.smart-form__upload {
  border: 2rpx dashed #d9d9d9;
  border-radius: 8rpx;
  padding: 24rpx;
}

.smart-form__upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.smart-form__upload-item {
  position: relative;
}

.smart-form__upload-preview {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
}

.smart-form__upload-file {
  width: 120rpx;
  height: 120rpx;
  border: 2rpx solid #d9d9d9;
  border-radius: 8rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx;
  position: relative;
}

.smart-form__upload-file-name {
  font-size: 20rpx;
  color: #666;
  text-align: center;
  word-break: break-all;
  line-height: 1.2;
}

.smart-form__upload-file-remove {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 32rpx;
  height: 32rpx;
  background: rgba(255, 77, 79, 0.8);
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16rpx;
}

.smart-form__upload-btn {
  width: 120rpx;
  height: 120rpx;
  border: 2rpx dashed #d9d9d9;
  border-radius: 8rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    border-color: #1890ff;
    background: #f0f8ff;
  }
}

.smart-form__upload-btn-icon {
  font-size: 48rpx;
  color: #c0c0c0;
}

.smart-form__upload-btn-text {
  font-size: 20rpx;
  color: #8c8c8c;
}

.smart-form__error {
  margin-top: 12rpx;
}

.smart-form__error-text {
  font-size: 24rpx;
  color: #ff4d4f;
  line-height: 1.4;
}

.smart-form__help {
  margin-top: 12rpx;
}

.smart-form__help-text {
  font-size: 24rpx;
  color: #8c8c8c;
  line-height: 1.4;
}

.smart-form__footer {
  padding: 24rpx;
  border-top: 2rpx solid #f0f0f0;
  background: #fafafa;
}

.smart-form__actions {
  display: flex;
  gap: 20rpx;
}

.smart-form__action {
  flex: 1;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8rpx;
  font-size: 30rpx;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;

  &--default {
    background: #f5f5f5;
    color: #666;

    &:active {
      background: #e0e0e0;
    }
  }

  &--primary {
    background: #1890ff;
    color: #ffffff;

    &:active {
      background: #1677ff;
    }
  }

  &--success {
    background: #52c41a;
    color: #ffffff;

    &:active {
      background: #46a614;
    }
  }

  &--warning {
    background: #faad14;
    color: #ffffff;

    &:active {
      background: #d48806;
    }
  }

  &--error {
    background: #ff4d4f;
    color: #ffffff;

    &:active {
      background: #dc3545;
    }
  }

  &--loading {
    cursor: not-allowed;
    opacity: 0.8;
  }

  &--disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }
}

.smart-form__action-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.smart-form__action-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
  border-top: 3rpx solid #ffffff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.smart-form__action-text {
  font-weight: 500;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>