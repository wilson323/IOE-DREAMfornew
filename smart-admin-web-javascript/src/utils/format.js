/**
 * 格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {number|string|Date} timestamp 时间戳或日期对象
 * @param {string} format 格式化模式
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(timestamp, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!timestamp) return '-';

  const date = new Date(timestamp);
  if (isNaN(date.getTime())) return '-';

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  const milliseconds = String(date.getMilliseconds()).padStart(3, '0');

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
    .replace('SSS', milliseconds);
}

/**
 * 格式化相对时间
 * @param {number|string|Date} timestamp 时间戳
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(timestamp) {
  if (!timestamp) return '-';

  const now = Date.now();
  const time = new Date(timestamp).getTime();
  const diff = now - time;

  if (diff < 1000) {
    return '刚刚';
  } else if (diff < 60000) {
    return `${Math.floor(diff / 1000)}秒前`;
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`;
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`;
  } else if (diff < 2592000000) {
    return `${Math.floor(diff / 86400000)}天前`;
  } else {
    return formatDateTime(timestamp, 'YYYY-MM-DD');
  }
}

/**
 * 格式化持续时间
 * @param {number} duration 持续时间（毫秒）
 * @returns {string} 格式化后的持续时间字符串
 */
export function formatDuration(duration) {
  if (!duration || duration < 0) return '0秒';

  const seconds = Math.floor(duration / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days > 0) {
    return `${days}天${hours % 24}小时${minutes % 60}分钟`;
  } else if (hours > 0) {
    return `${hours}小时${minutes % 60}分钟${seconds % 60}秒`;
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds % 60}秒`;
  } else {
    return `${seconds}秒`;
  }
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @param {number} decimals 小数位数
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatFileSize(bytes, decimals = 2) {
  if (bytes === 0) return '0 B';

  const k = 1024;
  const dm = decimals < 0 ? 0 : decimals;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}

/**
 * 格式化字节数（别名：formatBytes）
 * @param {number} bytes 字节数
 * @param {number} decimals 小数位数
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatBytes(bytes, decimals = 2) {
  return formatFileSize(bytes, decimals);
}

/**
 * 格式化数字
 * @param {number} num 数字
 * @param {Object} options 格式化选项
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(num, options = {}) {
  if (num === null || num === undefined) return '-';

  const {
    decimals = 2,
    separator = ',',
    prefix = '',
    suffix = '',
    unit = '',
    showPlus = false
  } = options;

  let formatted = Number(num).toFixed(decimals);

  // 添加千位分隔符
  if (separator) {
    formatted = formatted.replace(/\B(?=(\d{3})+(?!\d))/g, separator);
  }

  // 添加符号
  if (showPlus && num > 0) {
    formatted = '+' + formatted;
  }

  return `${prefix}${formatted}${unit}${suffix}`;
}

/**
 * 格式化百分比
 * @param {number} value 数值
 * @param {number} total 总数
 * @param {number} decimals 小数位数
 * @returns {string} 格式化后的百分比字符串
 */
export function formatPercentage(value, total, decimals = 1) {
  if (!total || total === 0) return '0%';

  const percentage = (value / total) * 100;
  return `${percentage.toFixed(decimals)}%`;
}

/**
 * 格式化货币
 * @param {number} amount 金额
 * @param {string} currency 货币符号
 * @param {Object} options 格式化选项
 * @returns {string} 格式化后的货币字符串
 */
export function formatCurrency(amount, currency = '¥', options = {}) {
  return formatNumber(amount, {
    ...options,
    prefix: currency,
    decimals: options.decimals || 2
  });
}

/**
 * 格式化增长率
 * @param {number} current 当前值
 * @param {number} previous 之前的值
 * @param {number} decimals 小数位数
 * @returns {Object} 包含格式化字符串和趋势的对象
 */
export function formatGrowthRate(current, previous, decimals = 1) {
  if (previous === 0) {
    return {
      text: current === 0 ? '0%' : '∞%',
      direction: 'up',
      value: current === 0 ? 0 : Infinity,
      color: current > 0 ? '#52c41a' : '#ff4d4f'
    };
  }

  const rate = ((current - previous) / previous) * 100;
  const direction = rate > 0 ? 'up' : rate < 0 ? 'down' : 'stable';

  return {
    text: `${rate > 0 ? '+' : ''}${rate.toFixed(decimals)}%`,
    direction,
    value: rate,
    color: rate > 0 ? '#52c41a' : rate < 0 ? '#ff4d4f' : '#faad14'
  };
}

/**
 * 格式化速率
 * @param {number} value 数值
 * @param {string} unit 单位
 * @param {number} interval 时间间隔（秒）
 * @returns {string} 格式化后的速率字符串
 */
export function formatRate(value, unit, interval = 1) {
  if (!value || value === 0) return `0 ${unit}/s`;

  const rate = value / interval;
  return `${formatNumber(rate)} ${unit}/s`;
}

/**
 * 格式化IP地址
 * @param {string|number} ip IP地址或数字
 * @returns {string} 格式化后的IP地址
 */
export function formatIPAddress(ip) {
  if (typeof ip === 'string') {
    return ip;
  }

  // 将数字转换为IP地址
  return [
    (ip >>> 24) & 255,
    (ip >>> 16) & 255,
    (ip >>> 8) & 255,
    ip & 255
  ].join('.');
}

/**
 * 格式化MAC地址
 * @param {string} mac MAC地址
 * @param {string} separator 分隔符
 * @returns {string} 格式化后的MAC地址
 */
export function formatMacAddress(mac, separator = ':') {
  if (!mac) return '-';

  // 移除所有非数字和非字母字符
  const cleaned = mac.replace(/[^a-fA-F0-9]/g, '');

  // 插入分隔符
  const formatted = cleaned.match(/.{1,2}/g)?.join(separator) || cleaned;

  return formatted.toUpperCase();
}

/**
 * 格式化版本号
 * @param {string|number} version 版本号
 * @returns {string} 格式化后的版本号
 */
export function formatVersion(version) {
  if (!version) return '-';

  const str = String(version);

  // 如果已经是标准格式，直接返回
  if (/^\d+\.\d+\.\d+/.test(str)) {
    return str;
  }

  // 尝试转换为标准格式
  const num = Number(version);
  if (!isNaN(num)) {
    const major = Math.floor(num / 10000);
    const minor = Math.floor((num % 10000) / 100);
    const patch = num % 100;
    return `${major}.${minor}.${patch}`;
  }

  return str;
}

/**
 * 格式化JSON字符串
 * @param {any} obj 对象
 * @param {number} space 缩进空格数
 * @returns {string} 格式化后的JSON字符串
 */
export function formatJSON(obj, space = 2) {
  try {
    return JSON.stringify(obj, null, space);
  } catch (error) {
    return String(obj);
  }
}

/**
 * 格式化错误信息
 * @param {Error|string} error 错误对象或字符串
 * @returns {string} 格式化后的错误信息
 */
export function formatError(error) {
  if (!error) return '-';

  if (typeof error === 'string') {
    return error;
  }

  if (error instanceof Error) {
    return `${error.name}: ${error.message}`;
  }

  return String(error);
}

/**
 * 格式化手机号码
 * @param {string} phone 手机号码
 * @returns {string} 格式化后的手机号码
 */
export function formatPhoneNumber(phone) {
  if (!phone) return '-';

  const cleaned = phone.replace(/\D/g, '');

  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`;
  }

  return phone;
}

/**
 * 格式化银行卡号
 * @param {string} cardNumber 银行卡号
 * @returns {string} 格式化后的银行卡号
 */
export function formatBankCard(cardNumber) {
  if (!cardNumber) return '-';

  const cleaned = cardNumber.replace(/\D/g, '');

  // 每4位添加空格，并隐藏中间部分
  if (cleaned.length >= 8) {
    const visible = cleaned.slice(0, 4) + ' **** **** ' + cleaned.slice(-4);
    return visible;
  }

  return cardNumber;
}

/**
 * 格式化颜色值
 * @param {string} color 颜色值
 * @returns {string} 格式化后的颜色值
 */
export function formatColor(color) {
  if (!color) return '-';

  // 如果是十六进制颜色
  if (color.startsWith('#')) {
    return color.toUpperCase();
  }

  // 如果是RGB颜色
  if (color.startsWith('rgb')) {
    return color;
  }

  // 如果是颜色名称，保持原样
  return color;
}

/**
 * 格式化URL
 * @param {string} url URL字符串
 * @param {number} maxLength 最大长度
 * @returns {string} 格式化后的URL
 */
export function formatURL(url, maxLength = 50) {
  if (!url) return '-';

  try {
    const urlObj = new URL(url);
    let formatted = urlObj.hostname + urlObj.pathname;

    if (formatted.length > maxLength) {
      formatted = formatted.substring(0, maxLength - 3) + '...';
    }

    return formatted;
  } catch (error) {
    return url.length > maxLength ? url.substring(0, maxLength - 3) + '...' : url;
  }
}

/**
 * 格式化文本（截断）
 * @param {string} text 文本
 * @param {number} maxLength 最大长度
 * @param {string} suffix 后缀
 * @returns {string} 格式化后的文本
 */
export function formatText(text, maxLength = 100, suffix = '...') {
  if (!text) return '-';

  if (text.length <= maxLength) {
    return text;
  }

  return text.substring(0, maxLength - suffix.length) + suffix;
}

/**
 * 格式化枚举值
 * @param {string|number} value 枚举值
 * @param {Object} enumMap 枚举映射
 * @returns {string} 格式化后的枚举值
 */
export function formatEnum(value, enumMap) {
  if (value === null || value === undefined) return '-';

  return enumMap[value] || String(value);
}

/**
 * 格式化布尔值
 * @param {boolean|any} value 布尔值
 * @param {Array} options 选项文本 [true文本, false文本]
 * @returns {string} 格式化后的布尔值
 */
export function formatBoolean(value, options = ['是', '否']) {
  if (value === null || value === undefined) return '-';

  return Boolean(value) ? options[0] : options[1];
}

/**
 * 格式化数组
 * @param {Array} arr 数组
 * @param {string} separator 分隔符
 * @param {number} maxItems 最大显示项数
 * @returns {string} 格式化后的数组字符串
 */
export function formatArray(arr, separator = ', ', maxItems = 3) {
  if (!Array.isArray(arr) || arr.length === 0) return '-';

  if (arr.length <= maxItems) {
    return arr.join(separator);
  }

  return arr.slice(0, maxItems).join(separator) + ` ... (+${arr.length - maxItems}项)`;
}