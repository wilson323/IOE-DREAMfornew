/**
 * 格式化工具函数库
 * 提供各种数据格式化功能，包括日期、数字、文本、文件等格式化
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-05
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

/**
 * 日期时间格式化
 * @param {number|string|Date} dateValue - 日期值
 * @param {string} formatStr - 格式化字符串，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDateTime(dateValue, formatStr = 'YYYY-MM-DD HH:mm:ss') {
  if (!dateValue && dateValue !== 0) {
    return '-'
  }

  try {
    const date = new Date(dateValue)
    if (isNaN(date.getTime())) {
      return '-'
    }

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    return formatStr
      .replace('YYYY', year)
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds)
  } catch (error) {
    console.warn('Date format error:', error)
    return '-'
  }
}

/**
 * 时间格式化
 * @param {number|string|Date} timeValue - 时间值
 * @param {string} formatStr - 格式化字符串，默认 'HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(timeValue, formatStr = 'HH:mm:ss') {
  return formatDateTime(timeValue, formatStr)
}

/**
 * 相对时间格式化
 * @param {number|string|Date} dateValue - 日期值
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(dateValue) {
  if (!dateValue) {
    return '-'
  }

  try {
    const date = new Date(dateValue)
    if (isNaN(date.getTime())) {
      return '-'
    }

    const now = Date.now()
    const diff = now - date.getTime()
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)

    if (seconds < 10) {
      return '刚刚'
    } else if (seconds < 60) {
      return `${seconds}秒前`
    } else if (minutes < 60) {
      return `${minutes}分钟前`
    } else if (hours < 24) {
      return `${hours}小时前`
    } else if (days < 7) {
      return `${days}天前`
    } else {
      return formatDateTime(dateValue, 'MM-DD HH:mm')
    }
  } catch (error) {
    console.warn('Relative time format error:', error)
    return '-'
  }
}

/**
 * 持续时间格式化
 * @param {number} milliseconds - 毫秒数
 * @returns {string} 格式化后的持续时间字符串
 */
export function formatDuration(milliseconds) {
  if (!milliseconds || milliseconds <= 0) {
    return '0秒'
  }

  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  const remainingSeconds = seconds % 60
  const remainingMinutes = minutes % 60
  const remainingHours = hours % 24

  const parts = []

  if (days > 0) {
    parts.push(`${days}天`)
  }

  if (remainingHours > 0 || days > 0) {
    parts.push(`${remainingHours}小时`)
  }

  if (remainingMinutes > 0 || remainingHours > 0 || days > 0) {
    parts.push(`${remainingMinutes}分钟`)
  }

  if (remainingSeconds > 0 || parts.length === 0) {
    parts.push(`${remainingSeconds}秒`)
  }

  return parts.join('')
}

/**
 * 文件大小格式化
 * @param {number} bytes - 字节数
 * @param {number} decimals - 小数位数，默认2位
 * @returns {string} 格式化后的文件大小
 */
export function formatFileSize(bytes, decimals = 2) {
  if (bytes === null || bytes === undefined || isNaN(bytes)) {
    return '-'
  }

  if (bytes === 0) {
    return '0 B'
  }

  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseFloat((bytes / Math.pow(k, i)).toFixed(decimals)) + ' ' + sizes[i]
}

/**
 * 字节格式化（formatFileSize的别名）
 * @param {number} bytes - 字节数
 * @param {number} decimals - 小数位数，默认2位
 * @returns {string} 格式化后的文件大小
 */
export function formatBytes(bytes, decimals = 2) {
  return formatFileSize(bytes, decimals)
}

/**
 * 数字格式化
 * @param {number} value - 数值
 * @param {Object} options - 格式化选项
 * @param {string} options.separator - 千位分隔符，默认','
 * @param {number} options.decimals - 小数位数，默认2
 * @param {string} options.prefix - 前缀
 * @param {string} options.suffix - 后缀
 * @param {boolean} options.showPlus - 是否显示正号
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(value, options = {}) {
  if (value === null || value === undefined || isNaN(value)) {
    return '-'
  }

  const {
    separator = ',',
    decimals = 2,
    prefix = '',
    suffix = '',
    showPlus = false
  } = options

  let formattedValue = parseFloat(value).toFixed(decimals)

  // 添加正号
  if (showPlus && value > 0) {
    formattedValue = '+' + formattedValue
  }

  // 添加千位分隔符
  if (separator) {
    const integerPart = formattedValue.split('.')[0]
    const decimalPart = formattedValue.split('.')[1] || ''
    const withSeparator = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, separator)
    formattedValue = decimalPart ? `${withSeparator}.${decimalPart}` : withSeparator
  }

  return prefix + formattedValue + suffix
}

/**
 * 百分比格式化
 * @param {number} value - 数值
 * @param {number} total - 总数
 * @param {number} decimals - 小数位数，默认1
 * @returns {string} 百分比字符串
 */
export function formatPercentage(value, total, decimals = 1) {
  if (value === null || value === undefined || total === null || total === undefined) {
    return '-'
  }

  if (total === 0) {
    return '0%'
  }

  const percentage = (value / total) * 100
  return `${percentage.toFixed(decimals)}%`
}

/**
 * 货币格式化
 * @param {number} value - 数值
 * @param {string} symbol - 货币符号，默认'¥'
 * @param {Object} options - 格式化选项
 * @returns {string} 格式化后的货币字符串
 */
export function formatCurrency(value, symbol = '¥', options = {}) {
  return formatNumber(value, {
    decimals: options.decimals || 2,
    separator: options.separator || ',',
    prefix: symbol,
    showPlus: options.showPlus
  })
}

/**
 * 增长率格式化
 * @param {number} current - 当前值
 * @param {number} previous - 之前值
 * @param {number} decimals - 小数位数，默认1
 * @returns {Object} 包含方向、文本和值的对象
 */
export function formatGrowthRate(current, previous, decimals = 1) {
  if (current === null || current === undefined || previous === null || previous === undefined) {
    return { direction: 'neutral', text: '-', value: 0 }
  }

  if (previous === 0) {
    return current === 0
      ? { direction: 'neutral', text: '0%', value: 0 }
      : { direction: 'up', text: '∞%', value: Infinity }
  }

  const growthRate = ((current - previous) / previous) * 100
  const direction = growthRate > 0 ? 'up' : growthRate < 0 ? 'down' : 'neutral'
  const text = `${growthRate >= 0 ? '+' : ''}${growthRate.toFixed(decimals)}%`

  return { direction, text, value: growthRate }
}

/**
 * 手机号格式化
 * @param {string} phoneNumber - 手机号
 * @returns {string} 格式化后的手机号
 */
export function formatPhoneNumber(phoneNumber) {
  if (!phoneNumber) {
    return '-'
  }

  const cleaned = phoneNumber.replace(/\D/g, '')

  if (cleaned.length !== 11) {
    return phoneNumber
  }

  return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`
}

/**
 * 银行卡号格式化
 * @param {string} cardNumber - 银行卡号
 * @returns {string} 格式化后的银行卡号
 */
export function formatBankCard(cardNumber) {
  if (!cardNumber) {
    return '-'
  }

  const cleaned = cardNumber.replace(/\D/g, '')

  if (cleaned.length <= 4) {
    return cleaned
  }

  if (cleaned.length < 16) {
    return cleaned
  }

  return `${cleaned.slice(0, 4)} **** **** ${cleaned.slice(-4)}`
}

/**
 * 文本截断
 * @param {string} text - 文本内容
 * @param {number} maxLength - 最大长度，默认100
 * @returns {string} 截断后的文本
 */
export function formatText(text, maxLength = 100) {
  if (!text) {
    return '-'
  }

  if (text.length <= maxLength) {
    return text
  }

  return text.slice(0, maxLength) + '...'
}

/**
 * 布尔值格式化
 * @param {boolean|any} value - 布尔值
 * @param {Array} labels - 自定义标签数组 [trueLabel, falseLabel]，默认['是', '否']
 * @returns {string} 格式化后的布尔值字符串
 */
export function formatBoolean(value, labels = ['是', '否']) {
  if (value === null || value === undefined) {
    return '-'
  }

  return Boolean(value) ? labels[0] : labels[1]
}

/**
 * 数组格式化
 * @param {Array} array - 数组
 * @param {string} separator - 分隔符，默认', '
 * @param {number} maxItems - 最大显示项数，默认5
 * @returns {string} 格式化后的数组字符串
 */
export function formatArray(array, separator = ', ', maxItems = 5) {
  if (!Array.isArray(array) || array.length === 0) {
    return '-'
  }

  if (array.length <= maxItems) {
    return array.join(separator)
  }

  const displayed = array.slice(0, maxItems)
  const remaining = array.length - maxItems
  return `${displayed.join(separator)} ... (+${remaining}项)`
}