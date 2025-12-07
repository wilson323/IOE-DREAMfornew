/**
 * 统一表单验证规则库
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-04
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

/**
 * 手机号验证
 */
export const validatePhone = (value) => {
  const pattern = /^1[3-9]\d{9}$/
  if (!value) {
    return { valid: false, message: '请输入手机号' }
  }
  if (!pattern.test(value)) {
    return { valid: false, message: '手机号格式不正确' }
  }
  return { valid: true }
}

/**
 * 身份证号验证
 */
export const validateIdCard = (value) => {
  const pattern = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!value) {
    return { valid: false, message: '请输入身份证号' }
  }
  if (!pattern.test(value)) {
    return { valid: false, message: '身份证号格式不正确' }
  }
  return { valid: true }
}

/**
 * 邮箱验证
 */
export const validateEmail = (value) => {
  const pattern = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/
  if (!value) {
    return { valid: false, message: '请输入邮箱' }
  }
  if (!pattern.test(value)) {
    return { valid: false, message: '邮箱格式不正确' }
  }
  return { valid: true }
}

/**
 * 车牌号验证
 */
export const validateVehicleNumber = (value) => {
  const pattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5}$/
  if (!value) {
    return { valid: false, message: '请输入车牌号' }
  }
  if (!pattern.test(value)) {
    return { valid: false, message: '车牌号格式不正确' }
  }
  return { valid: true }
}

/**
 * 必填验证
 */
export const validateRequired = (value, fieldName = '该字段') => {
  if (value === null || value === undefined || value === '') {
    return { valid: false, message: `${fieldName}不能为空` }
  }
  if (typeof value === 'string' && value.trim() === '') {
    return { valid: false, message: `${fieldName}不能为空` }
  }
  return { valid: true }
}

/**
 * 长度验证
 */
export const validateLength = (value, min, max, fieldName = '该字段') => {
  if (!value) {
    return { valid: true }  // 如果为空，由required验证处理
  }

  const length = value.length

  if (min && length < min) {
    return { valid: false, message: `${fieldName}长度不能少于${min}个字符` }
  }
  if (max && length > max) {
    return { valid: false, message: `${fieldName}长度不能超过${max}个字符` }
  }
  return { valid: true }
}

/**
 * 数值范围验证
 */
export const validateRange = (value, min, max, fieldName = '该字段') => {
  const num = Number(value)

  if (isNaN(num)) {
    return { valid: false, message: `${fieldName}必须是数字` }
  }

  if (min !== undefined && num < min) {
    return { valid: false, message: `${fieldName}不能小于${min}` }
  }
  if (max !== undefined && num > max) {
    return { valid: false, message: `${fieldName}不能大于${max}` }
  }
  return { valid: true }
}

/**
 * 日期验证
 */
export const validateDate = (value, fieldName = '日期') => {
  if (!value) {
    return { valid: false, message: `请选择${fieldName}` }
  }

  const date = new Date(value)
  if (isNaN(date.getTime())) {
    return { valid: false, message: `${fieldName}格式不正确` }
  }
  return { valid: true }
}

/**
 * 日期范围验证
 */
export const validateDateRange = (startDate, endDate) => {
  if (!startDate || !endDate) {
    return { valid: false, message: '请选择日期范围' }
  }

  const start = new Date(startDate)
  const end = new Date(endDate)

  if (start > end) {
    return { valid: false, message: '开始日期不能晚于结束日期' }
  }
  return { valid: true }
}

/**
 * 金额验证
 */
export const validateAmount = (value, min = 0, max = 999999.99) => {
  const num = Number(value)

  if (isNaN(num)) {
    return { valid: false, message: '金额格式不正确' }
  }

  if (num < min) {
    return { valid: false, message: `金额不能小于${min}元` }
  }
  if (num > max) {
    return { valid: false, message: `金额不能大于${max}元` }
  }

  // 验证小数位数
  const decimalPart = value.toString().split('.')[1]
  if (decimalPart && decimalPart.length > 2) {
    return { valid: false, message: '金额最多保留2位小数' }
  }

  return { valid: true }
}

/**
 * 表单验证器类
 */
export class FormValidator {
  constructor(rules) {
    this.rules = rules
    this.errors = {}
  }

  /**
   * 验证单个字段
   */
  validateField(field, value) {
    const fieldRules = this.rules[field]
    if (!fieldRules) return { valid: true }

    for (const rule of fieldRules) {
      let result

      // 必填验证
      if (rule.required) {
        result = validateRequired(value, rule.label || field)
        if (!result.valid) return result
      }

      // 跳过空值的其他验证
      if (!value && !rule.required) continue

      // 模式验证
      if (rule.pattern) {
        if (!rule.pattern.test(value)) {
          return { valid: false, message: rule.message || '格式不正确' }
        }
      }

      // 长度验证
      if (rule.minLength || rule.maxLength) {
        result = validateLength(value, rule.minLength, rule.maxLength, rule.label || field)
        if (!result.valid) return result
      }

      // 数值范围验证
      if (rule.min !== undefined || rule.max !== undefined) {
        result = validateRange(value, rule.min, rule.max, rule.label || field)
        if (!result.valid) return result
      }

      // 自定义验证函数
      if (rule.validator) {
        result = rule.validator(value)
        if (result && !result.valid) return result
      }
    }

    return { valid: true }
  }

  /**
   * 验证整个表单
   */
  validate(formData) {
    this.errors = {}
    let isValid = true

    for (const field in this.rules) {
      const result = this.validateField(field, formData[field])
      if (!result.valid) {
        this.errors[field] = result.message
        isValid = false
      }
    }

    return {
      valid: isValid,
      errors: this.errors
    }
  }

  /**
   * 获取字段错误信息
   */
  getFieldError(field) {
    return this.errors[field] || ''
  }

  /**
   * 清除验证错误
   */
  clearErrors() {
    this.errors = {}
  }
}

/**
 * 常用验证规则
 */
export const commonRules = {
  // 用户名
  username: [
    { required: true, label: '用户名' },
    { minLength: 2, maxLength: 20, label: '用户名' }
  ],

  // 密码
  password: [
    { required: true, label: '密码' },
    { minLength: 6, maxLength: 20, label: '密码' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,}$/,
      message: '密码必须包含大小写字母和数字'
    }
  ],

  // 手机号
  phone: [
    { required: true, label: '手机号' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确'
    }
  ],

  // 身份证号
  idCard: [
    { required: true, label: '身份证号' },
    {
      pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
      message: '身份证号格式不正确'
    }
  ],

  // 邮箱
  email: [
    { required: false, label: '邮箱' },
    {
      pattern: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/,
      message: '邮箱格式不正确'
    }
  ],

  // 车牌号
  vehicleNumber: [
    { required: true, label: '车牌号' },
    {
      pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-Z0-9]{5}$/,
      message: '车牌号格式不正确'
    }
  ],

  // 金额
  amount: [
    { required: true, label: '金额' },
    {
      validator: (value) => validateAmount(value, 0.01, 999999.99)
    }
  ]
}

export default {
  validatePhone,
  validateIdCard,
  validateEmail,
  validateVehicleNumber,
  validateRequired,
  validateLength,
  validateRange,
  validateDate,
  validateDateRange,
  validateAmount,
  FormValidator,
  commonRules
}

